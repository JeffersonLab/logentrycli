/**
 * Program to allow creation of logentries via the command line
 * @author Theo Larrieu
 */
package org.jlab.elog;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;

/**
 * Class that makes a logentry from command line arguments
 * @author theo
 */
public class LogEntryCLI {
  
  //Version identifier.  Increment for new releases and 
  //don't forget to tag the version in git repositior to match!
  public static final String VERSION_ID = "1.2";;
  
  /**
   * Attempt to make an entry.  Exit with status code of
   * 0 upon success or non-zero upon failure.  
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    Options options = buildOptions();
    CommandLineParser parser = new BasicParser();
    try {
        // parse the command line arguments
        CommandLine line = parser.parse( options, args );
        if( line.hasOption( "help" ) ) {
          showUsage(options);
          System.exit(0);
        }
        int status = makeEntry(line);
        System.exit(status);
    } catch( ParseException exp ) {
        // oops, something went wrong
        System.err.println( exp.getMessage() );
        showUsage( options );
        System.exit(1);
    } 
    System.exit(1); // Should always exit before this.
  }
  
  
  /**
   * Assign arguments extracted from the command line to Logentry API
   * methods and then call an appropriate sumbit method.
   * @param line
   * @return int 0 after successful submission, non-zero after failure
   */
  private static int makeEntry(CommandLine line) throws ParseException{
    
    //Unless otherwise specified, entires are plain text
    Body.ContentType bodyFmt = Body.ContentType.TEXT;
    
    try {
      
      /*  If we don't take over checking for required options
       * the user will get an error message when he types -h
       * or --help that he is missing required fields -t and -l
       */ 
      ArrayList<String> missing = new ArrayList();
      if (! line.hasOption("title")){
        missing.add("title (-t)");
      }
      if (! line.hasOption("logbook")){
        missing.add("logbook (-l)");
      }
      if (missing.size() > 0){
        String[] missingArr = missing.toArray(new String[0]);
        String message = "Missing required option(s): "+join(missingArr);
        // Throw the same ParseException that commons-cli would have
        throw new ParseException(message);
      }
      
      LogEntry elog = new LogEntry(line.getOptionValue( "title" ),
                                   join(line.getOptionValues( "logbook" )));
      if( line.hasOption( "html" ) ) {
        bodyFmt = Body.ContentType.HTML;
      }
      
      if( line.hasOption( "body" ) ) {
        if (line.getOptionValue( "body" ).equals("-")){
          elog.setBody(readStdIn(),bodyFmt);
        }else{
          elog.setBody(readFile(line.getOptionValue( "body" )),bodyFmt);
        }
      }

      /**
       * The user may specify multiple attachments and optionally
       * captions to go with them.  We'll receive both sets as 
       * arrays via respective getOptionValues() calls and then we
       * match them up using their indeces.  The first caption goes
       * with the first attachment, the second with the second, and so
       * on.
       */
      if( line.hasOption( "attach" ) ) {
        String captionToUse = new String();
        String[] attArr = line.getOptionValues( "attach" );
        for (int i=0; i<attArr.length; i++){
          // With attachments we have to look for a corresponding caption
          if( line.hasOption( "caption" ) ) {
            String[] captions = line.getOptionValues( "caption" );
            if (i < captions.length){
              captionToUse = captions[i];
            }else{
              captionToUse = null;
            } 
          }
          elog.addAttachment(attArr[i],captionToUse);
        }
      }
      
      if( line.hasOption( "tag" ) ) {
        elog.addTags(line.getOptionValues( "tag" ));
      }
      
      if( line.hasOption( "link" ) ) {
        String[] links = line.getOptionValues( "link" );
        for (int i=0; i<links.length; i++){
          Reference ref = new Reference("logbook", links[i]);
          elog.addReference(ref);
        }
        
      }
      
      if( line.hasOption( "notify" ) ) {
        elog.setEmailNotify(line.getOptionValues( "notify" ));
      }
      
      if( line.hasOption( "entrymaker" ) ) {
        elog.addEntryMakers(line.getOptionValues( "entrymaker" ));
      }
      
      if( line.hasOption( "xml" ) ) {
        System.out.println(elog.getXML());
      }
      
      if( line.hasOption( "nosubmit" ) ) {
        return 0;
      }
      
      // Finally we do the submit.  
      // There are 4 possibilities:
      //   with alt-certificate, noqueue
      //   with alt-certificate, with queue
      //   without alt-certificate, noqueue
      //   without alt-certificate, with queue      
      long submitResult;
      if( line.hasOption( "cert" ) ) {
        String certificate = line.getOptionValue( "cert" );
        if( line.hasOption( "noqueue" ) ) {
          submitResult = elog.submitNow(certificate);
        }else{
          submitResult = elog.submit(certificate);
        }      
      }else{  
        if( line.hasOption( "noqueue" ) ) {
          submitResult = elog.submitNow();
        }else{
          submitResult = elog.submit();
        }
      }
      if (submitResult > 0){
        System.out.println("The Entry was saved with lognumber "+submitResult);
      }else{
        System.out.println("The Entry was placed in the entry queue");
      }
      
      
    } catch (Exception e){
      System.out.println(e.getMessage());
      return 1;
    }
    return 0;
  }
  
  
  /**
   * Prints a help/usage message of acceptable options
   * @param options 
   */
  private static void showUsage(Options options){
      HelpFormatter formatter = new HelpFormatter();
      String newLine = System.getProperty("line.separator");
      System.out.println("logentry command line utility version "+VERSION_ID);
      formatter.printHelp("logentry [options]"+newLine, null, options,
              newLine+"The options tag, logbook, attachment, and notify may " +
              "be included more than once to make multiple inclusions. "+
              "For more help see: "+
              "https://logbooks.jlab.org/content/unix-command-line");
  } 
  
   
  /**
   * Defines the acceptable command line options and their parameters.
   * We don't use the commons-cli isRequired because it throws an
   * error before we can check to see if the user just wanted help
   * (-h or --help).
   * @return 
   */
  private static Options buildOptions(){
    Option help = OptionBuilder.withArgName("help")
                                .withLongOpt("help")
                                .withDescription(  "Prints this message" )
                                .create( "h" );
    Option logbook  = OptionBuilder.withArgName("logbook")
                                .withLongOpt("logbook")
                                .hasArg()
                                .withDescription(  "(required) A valid logbook name" )
                                .create( "l" );
    Option title  = OptionBuilder.withArgName("title")
                                .withLongOpt("title")
                                .hasArg()
                                .withDescription(  "(required) The title/keywords for the entry (max 255 chars)" )
                                .create( "t" );    
    Option attachment  = OptionBuilder.withArgName("file")
                                .withLongOpt("attach")
                                .hasArg()
                                .withDescription( "A /path/to/a/file to attach" )
                                .create( "a" );
    Option body  = OptionBuilder.withArgName("body")
                                .withLongOpt("body")
                                .hasArg()
                                .withDescription( "A /path/to/a/text/file or '-' to read StdIn" )
                                .create( "b" );   
    Option tag  = OptionBuilder.withArgName("tag")
                                .withLongOpt("tag")
                                .hasArg()
                                .withDescription( "A valid tag" )
                                .create( "g" );
    Option entrymaker  = OptionBuilder.withArgName("entrymaker")
                                .withLongOpt("entrymaker")
                                .hasArg()
                                .withDescription( "Name(s) of person(s) making the entry" )
                                .create( "e" );
    Option notify  = OptionBuilder.withArgName("notify")
                                .withLongOpt("notify")
                                .hasArg()
                                .withDescription( "An email address" )
                                .create( "n" );
    Option caption  = OptionBuilder.withArgName("caption")
                                .withLongOpt("caption")
                                .hasArg()
                                .withDescription( "Caption(s) to go with attachment(s)" )
                                .create( "c" );
    Option link  = OptionBuilder.withArgName("lognumber")
                                .withLongOpt("link")
                                .withDescription("Link to the specified existing lognumber" )
                                .hasArg()                    
                                .create();    
    Option html  = OptionBuilder.withLongOpt("html")
                                .withDescription("Interpret body as HTML instead of text" )
                                .create();
    Option xml  = OptionBuilder.withLongOpt("xml")
                                .withDescription("Print XML version of logentry to StdOut" )
                                .create();
    Option noqueue  = OptionBuilder.withLongOpt("noqueue")
                                .withDescription("Do not queue entry. Exit with error if immediate submit fails" )
                                .create();
    Option nosubmit  = OptionBuilder.withLongOpt("nosubmit")
                                .withDescription("Do not submit entry." )
                                .create();
    Option cert  = OptionBuilder.withArgName("certificate")
                                .withLongOpt("cert")
                                .hasArg()
                                .withDescription(  "The path to a PEM format logbook SSL certificate file to use" )
                                .create();
    
    
    Options options = new Options();
    options.addOption( help );
    options.addOption( logbook );
    options.addOption( title );
    options.addOption( attachment );
    options.addOption( body );
    options.addOption( tag );
    options.addOption( entrymaker );
    options.addOption( notify );
    options.addOption( caption );
    options.addOption( link );
    options.addOption( html );
    options.addOption( xml );
    options.addOption( noqueue );
    options.addOption( nosubmit );
    options.addOption( cert );
    
    return options;
  }
  
   
  /**
   * Slurps a file into a string
   * @param path
   * @return
   * @throws IOException 
   * @see  http://stackoverflow.com/questions/326390/how-to-create-a-java-string-from-the-contents-of-a-file
   */
  private static String readFile(String path) throws IOException {
    FileInputStream stream = new FileInputStream(new File(path));
    try {
      FileChannel fc = stream.getChannel();
      MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
      /* Instead of using default, pass in a decoder. */
      return Charset.forName("UTF-8").decode(bb).toString();
    }
    finally {
      stream.close();
    }
  }
  
  /**
   * Returns the contents of StdIn as a string
   * @return 
   */
  private static String readStdIn(){
    String body = null; 
    // Read the input from stdin
    InputStream in = System.in;
    try {
        body = IOUtils.toString(System.in); 
    } catch (IOException ioe) {
        System.err.println("IO error trying to read message!");
        System.exit(1);
    } finally {
        IOUtils.closeQuietly(in);
    }
    return body;
  }
    
  /**
   * Concentenates the contents of the array of strings with commas.
   * How annoying that Java Strings don't have a native join function
   */
  private static String join(String[] strArr){
    String joined = new String();
    for (int i=0; i<strArr.length; i++){
      if (i > 0){ joined = joined + ", ";}
      joined = joined + strArr[i];      
    }
    return joined;
  }
  
}

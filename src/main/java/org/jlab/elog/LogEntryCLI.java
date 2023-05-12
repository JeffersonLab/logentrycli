/**
 * Program to allow creation of logentries via the command line
 *
 * @author Theo Larrieu
 */
package org.jlab.elog;

import org.jlab.jlog.LogEntry;
import org.jlab.jlog.Body;
import org.jlab.jlog.Reference;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
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
    public static final String VERSION_ID = "2.0";

    /**
     * Attempt to make an entry.  Exit with status code of
     * 0 upon success or non-zero upon failure.
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Options options = buildOptions();
        CommandLineParser parser = new DefaultParser();
        try {
            // parse the command line arguments
            CommandLine line = parser.parse(options, args);
            if (line.hasOption("help")) {
                showUsage(options);
                System.exit(0);
            }
            int status = makeEntry(line);
            System.exit(status);
        } catch (ParseException exp) {
            // oops, something went wrong
            System.err.println(exp.getMessage());
            showUsage(options);
            System.exit(1);
        }
        System.exit(1); // Should always exit before this.
    }


    /**
     * Assign arguments extracted from the command line to Logentry API
     * methods and then call an appropriate sumbit method.
     * @param line command line to be parsed
     * @return int 0 after successful submission, non-zero after failure
     */
    private static int makeEntry(CommandLine line) throws ParseException {

        //Unless otherwise specified, entries are plain text
        Body.ContentType bodyFmt = Body.ContentType.TEXT;

        try {

            ArrayList<String> missing = new ArrayList<>();
            /*  If we don't take over checking for required options
             * the user will get an error message when he types -h
             * or --help that he is missing required fields -t and -l
             */
            if (!line.hasOption("title")) {
                missing.add("title (-t)");
            }
            if (!line.hasOption("logbook")) {
                missing.add("logbook (-l)");
            }
            if (missing.size() > 0) {
                String[] missingArr = missing.toArray(new String[0]);
                String message = "Missing required option(s): " + join(missingArr);
                // Throw the same ParseException that commons-cli would have
                throw new ParseException(message);
            }

            LogEntry elog = new LogEntry(line.getOptionValue("title"),
                    join(line.getOptionValues("logbook")));
            if (line.hasOption("html")) {
                bodyFmt = Body.ContentType.HTML;
            }

            if (line.hasOption("body")) {
                if (line.getOptionValue("body").equals("-")) {
                    elog.setBody(readStdIn(), bodyFmt);
                } else {
                    elog.setBody(readFile(line.getOptionValue("body")), bodyFmt);
                }
            }

            /*
             * The user may specify multiple attachments and optionally
             * captions to go with them.  We'll receive both sets as
             * arrays via respective getOptionValues() calls and then we
             * match them up using their index.  The first caption goes
             * with the first attachment, the second with the second, and so
             * on.
             */
            if (line.hasOption("attach")) {
                String captionToUse = "";
                String[] attArr = line.getOptionValues("attach");
                for (int i = 0; i < attArr.length; i++) {
                    // With attachments we have to look for a corresponding caption
                    if (line.hasOption("caption")) {
                        String[] captions = line.getOptionValues("caption");
                        if (i < captions.length) {
                            captionToUse = captions[i];
                        } else {
                            captionToUse = null;
                        }
                    }
                    elog.addAttachment(attArr[i], captionToUse);
                }
            }

            if (line.hasOption("tag")) {
                elog.addTags(line.getOptionValues("tag"));
            }

            if (line.hasOption("link")) {
                String[] links = line.getOptionValues("link");
                for (String link : links) {
                    Reference ref = new Reference("logbook", link);
                    elog.addReference(ref);
                }

            }

            if (line.hasOption("notify")) {
                elog.setEmailNotify(line.getOptionValues("notify"));
            }

            if (line.hasOption("entrymaker")) {
                elog.addEntryMakers(line.getOptionValues("entrymaker"));
            }

            if (line.hasOption("cert")) {
                String certificate = line.getOptionValue("cert");
                elog.setClientCertificatePath(certificate, true);
            }


            if (line.hasOption("xml")) {
                System.out.println(elog.getXML());
            }

            if (line.hasOption("nosubmit")) {
                return 0;
            }

            // Finally we do the submit.
            long submitResult;
            if (line.hasOption("noqueue")) {
                submitResult = elog.submitNow();
            } else {
                submitResult = elog.submit();
            }
            if (submitResult > 0) {
                System.out.println("The Entry was saved with lognumber " + submitResult);
            } else {
                System.out.println("The Entry was placed in the entry queue");
            }


        } catch (Exception e) {
            System.out.println(e.getMessage());
            return 1;
        }
        return 0;
    }


    /**
     * Prints a help/usage message of acceptable options
     * @param options the Options collection
     */
    private static void showUsage(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        String newLine = System.getProperty("line.separator");
        System.out.println("logentry command line utility version " + VERSION_ID);
        formatter.printHelp("logentry [options]" + newLine, null, options,
                newLine + "The options tag, logbook, attachment, and notify may " +
                        "be included more than once to make multiple inclusions. " +
                        "For more help see: " +
                        "https://logbooks.jlab.org/content/unix-command-line");
    }


    /**
     * Defines the acceptable command line options and their parameters.
     * We don't use the commons-cli isRequired because it throws an
     * error before we can check to see if the user just wanted help
     * (-h or --help).
     * @return Options
     */
    private static Options buildOptions() {
        Option help = Option.builder("h")
                .longOpt("help")
                .desc("Prints this message")
                .build();
        Option logbook = Option.builder("l")
                .longOpt("logbook")
                .hasArg()
                .desc("(required) A valid logbook name")
                .build();
        Option title = Option.builder("t")
                .longOpt("title")
                .hasArg()
                .desc("(required) The title/keywords for the entry (max 255 chars)")
                .build();
        Option attachment = Option.builder("a")
                .longOpt("attach")
                .hasArg()
                .desc("A /path/to/a/file to attach")
                .build();
        Option body = Option.builder("b")
                .longOpt("body")
                .hasArg()
                .desc("A /path/to/a/text/file or '-' to read StdIn")
                .build();
        Option tag = Option.builder("g")
                .longOpt("tag")
                .hasArg()
                .desc("A valid tag")
                .build();
        Option entrymaker = Option.builder("e")
                .longOpt("entrymaker")
                .hasArg()
                .desc("Name(s) of person(s) making the entry")
                .build();
        Option notify = Option.builder("n")
                .longOpt("notify")
                .hasArg()
                .desc("An email address")
                .build();
        Option caption = Option.builder("c")
                .longOpt("caption")
                .hasArg()
                .desc("Caption(s) to go with attachment(s)")
                .build();
        Option link = Option.builder("link")
                .desc("Link to the specified existing lognumber")
                .hasArg()
                .build();
        Option html = Option.builder("html")
                .desc("Interpret body as HTML instead of text")
                .build();
        Option xml = Option.builder("xml")
                .desc("Print XML version of logentry to StdOut")
                .build();
        Option noqueue = Option.builder("noqueue")
                .desc("Do not queue entry. Exit with error if immediate submit fails")
                .build();
        Option nosubmit = Option.builder("nosubmit")
                .desc("Do not submit entry.")
                .build();
        Option cert = Option.builder("cert")
                .hasArg()
                .desc("The path to a PEM format logbook SSL certificate file to use")
                .build();


        Options options = new Options();
        options.addOption(help);
        options.addOption(logbook);
        options.addOption(title);
        options.addOption(attachment);
        options.addOption(body);
        options.addOption(tag);
        options.addOption(entrymaker);
        options.addOption(notify);
        options.addOption(caption);
        options.addOption(link);
        options.addOption(html);
        options.addOption(xml);
        options.addOption(noqueue);
        options.addOption(nosubmit);
        options.addOption(cert);

        return options;
    }


    /**
     * Slurps a file into a string
     * @param path file system path
     * @return the contents of the file
     * @throws IOException file I/O exception
     * @see  <a href="http://stackoverflow.com/questions/326390/how-to-create-a-java-string-from-the-contents-of-a-file">
     *     stackoverflow discussion</a>
     */
    private static String readFile(String path) throws IOException {
        try (FileInputStream stream = new FileInputStream(path)) {
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            /* Instead of using default, pass in a decoder. */
            return StandardCharsets.UTF_8.decode(bb).toString();
        }
    }

    /**
     * {@return the contents of StdIn as a string}
     */
    private static String readStdIn() {
        String body = null;
        // Read the input from stdin
        InputStream in = System.in;
        try {
            body = IOUtils.toString(System.in, StandardCharsets.UTF_8);
        } catch (IOException ioe) {
            System.err.println("IO error trying to read message!");
            System.exit(1);
        } finally {
            IOUtils.closeQuietly(in);
        }
        return body;
    }

    /**
     * Concatenates the contents of the array of strings with commas.
     * How annoying that Java Strings don't have a native join function
     */
    private static String join(String[] strArr) {
        StringBuilder joined = new StringBuilder();
        for (int i = 0; i < strArr.length; i++) {
            if (i > 0) {
                joined.append(", ");
            }
            joined.append(strArr[i]);
        }
        return joined.toString();
    }

}

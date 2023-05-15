# logentrycli
A utility for making entries to https://logbooks.jlab.org via command line.  It provides a wrapper around [JeffersonLab/jlog](https://github.com/JeffersonLab/jlog) via invokable shell scripts for Linux and Windows.


## Build
This project is built with [Java 17](https://adoptium.net/) (compiled to Java 8 bytecode), and uses the [Gradle](https://gradle.org/) build tool to automatically download dependencies and build the project from source:

```
git clone https://github.com/JeffersonLab/logentrycli
cd logentrycli
./gradlew clean
./gradlew make
```
**Note**: If you do not already have Gradle installed, it will be installed automatically by the wrapper script included in the source
**Note for JLab On-Site Users**: Jefferson Lab has an intercepting [proxy](https://gist.github.com/slominskir/92c25a033db93a90184a5994e71d0b78) 
so it's necessary to tell gradle to trust the SSL certificate that it uses. 

```
# Example for an ACE Linux
./gradlew -Djavax.net.ssl.trustStore=/etc/pki/ca-trust/extracted/java/cacerts make
```

## Usage

The simplest way to execut the program is using the wrapper scripts located in the bin directory which contains
logentry.bat for Windows and logentry for linux.

```
C:\Users\theo\Projects\logentrycli> .\bin\logentry -cert .\temp.pem -t test -l TLOG
The Entry was saved with lognumber 4157833
```

Executing the program with the -h flag will print the following usage instructions.

```
PS C:\Users\theo\Projects\logentrycli> .\bin\logentry -h                              
logentry command line utility version 2.0
usage: logentry [options]
 -a,--attach <arg>       A /path/to/a/file to attach
 -b,--body <arg>         A /path/to/a/text/file or '-' to read StdIn
 -c,--caption <arg>      Caption(s) to go with attachment(s)
    --cert <arg>         The path to a PEM format logbook SSL certificate
                         file to use
    --config <arg>       The path to an alternative properties
                         configuration file
 -e,--entrymaker <arg>   Name(s) of person(s) making the entry
 -g,--tag <arg>          A valid tag
 -h,--help               Prints this message
 -html,--html            Interpret body as HTML instead of text
 -l,--logbook <arg>      (required) A valid logbook name
 -n,--notify <arg>       An email address
    --noqueue            Do not queue entry. Exit with error if immediate
                         submit fails
    --nosubmit           Do not submit entry.
 -t,--title <arg>        (required) The title/keywords for the entry (max
                         255 chars)
    --xml                Print XML version of logentry to StdOut

The options tag, logbook, attachment, and notify may be included more than
once to make multiple inclusions. For more help see:
https://logbooks.jlab.org/content/unix-command-line

```

## Installed Executables

The logentry utility is pre-installed and available at the following paths:

### ACE Path
```
/cs/certified/apps/logentrycli/2.0/bin/logentry -h
```

### CUE Path
On Linux
```
/site/ace/certified/apps/logentrycli/2.0/bin/logentry -h
```

On Windows
```
k:\ace\certified\apps\logentrycli\2.0\bin\logentrycli -h
```

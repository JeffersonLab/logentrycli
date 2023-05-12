# logentrycli
A utility for making entries to https://logbooks.jlab.org via command line.


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

## Execute

The simplest way to execut the program is using the wrapper scripts located in the bin directory which contains
logentry.bat for Windows and logentry for linux.

## Usage
Executing the program with the -h flag will print the following usage instructions.

```
PS C:\Users\theo\Projects> .\bin\logentry -h
logentry command line utility version 1.2
usage: logentry [options]
 -a,--attach <file>             A /path/to/a/file to attach
 -b,--body <body>               A /path/to/a/text/file or '-' to read
                                StdIn
 -c,--caption <caption>         Caption(s) to go with attachment(s)
    --cert <certificate>        The path to a PEM format logbook SSL
                                certificate file to use
 -e,--entrymaker <entrymaker>   Name(s) of person(s) making the entry
 -g,--tag <tag>                 A valid tag
 -h,--help                      Prints this message
    --html                      Interpret body as HTML instead of text
 -l,--logbook <logbook>         (required) A valid logbook name
    --link <lognumber>          Link to the specified existing lognumber
 -n,--notify <notify>           An email address
    --noqueue                   Do not queue entry. Exit with error if
                                immediate submit fails
    --nosubmit                  Do not submit entry.
 -t,--title <title>             (required) The title/keywords for the
                                entry (max 255 chars)
    --xml                       Print XML version of logentry to StdOut

The options tag, logbook, attachment, and notify may be included more than
once to make multiple inclusions. For more help see:
https://logbooks.jlab.org/content/unix-command-line
```

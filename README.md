# logentrycli
A utility for making entries to logbooks.jlab.org via command line.


## Build
This project is built with [Java 17](https://adoptium.net/) (compiled to Java 8 bytecode), and uses the [Gradle 7](https://gradle.org/) build tool to automatically download dependencies and build the project from source:

```
git clone https://github.com/JeffersonLab/logentrycli
cd logentrycli
./gradlew build
```

**Note**: If you do not already have Gradle installed, it will be installed automatically by the wrapper script included in the source

**Note for JLab On-Site Users**: Jefferson Lab has an intercepting [proxy](https://gist.github.com/slominskir/92c25a033db93a90184a5994e71d0b78)
```
./gradlew -Djavax.net.ssl.trustStore=/etc/pki/ca-trust/extracted/java/cacerts build
```


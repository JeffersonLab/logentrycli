# Install Steps for /cs/certified

The following steps are used to build and install the application in /cs/certified

## RHEL7 Compatibility Note
Note that although the compilation in step #3 must use Java 17 (available on RHEL9, but not on 7), the
resulting jar file from the compilation produces compatible bytecode will run on java 11 or newer.

Unfortunately, on ACE RHEL7, the default widely available Java runtime is version 8.  It is necessary
to install a newer version of java on any RHEL7 system where this tool must run.

## Steps

### Download Source Code
On an RHEL9 dev workstation, change to a clean working directory and download the desired release version from github.  Releases are available in .zip and .tar.gz format.  The example below will assume the .tar.gz.

Download the "Source Code (tar.gz)" link from https://github.com/JeffersonLab/logentrycli/releases

### Unpack Source Code

```bash
tar xfz logentrycli-2.1.tar.gz
cd logentrycli-2.1
```

### Compile/Build using gradle

```bash
# The special SSL certificates may be needed to operate through JLab intercepting proxy
./gradlew -Djavax.net.ssl.trustStore=/etc/pki/ca-trust/extracted/java/cacerts clean
./gradlew -Djavax.net.ssl.trustStore=/etc/pki/ca-trust/extracted/java/cacerts make
```

### Copy bin and lib to install location.

```bash
mkdir /cs/certified/apps/logentrycli/2.1/
cp -r bin lib /cs/certified/apps/logentrycli/2.1/
chmod 555 /cs/certified/apps/logentrycli/2.1/bin/logentry
```

### Perform a simplye sanity test
```bash
# Make a minimalist log entry
# User executing the test must have a valid ~/.elogcert
/cs/certified/apps/logentrycli/2.1/bin/logentry -t test -l TLOG
```

### Rotate the PRO link

```bash
cd /cs/certifieds/apps/logentrycli
rm PRO; ln -s 2.1 PRO
```
If necessary, create a symbolic link to the PRO wrapper script
from /usr/csite/certified/bin to the PRO version
```bash
cd  /usr/csite/certified/bin
ln -s  /cs/certified/apps/logentrycli/PRO/bin/logentry .
```

### Test/Run on RHEL7
One can use the host devl77 where openjdk 18 has been installed as of this writing.
```bash
setenv JAVA_HOME /usr/lib/jvm/java-18-openjdk-18.0.2.1.9-1.rolling.el7.x86_64/
/cs/certified/apps/logentrycli/2.1/bin/logentry -t test -l TLOG
```



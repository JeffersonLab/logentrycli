# Release notes for *logentry*

## Version 2.0 - May 16, 2023
- Switch from ant to gradle for building
- Update org.apache.commons.cli dependencies to version 1.5 
  - Update code to avoid use of methods deprecated by new version 
- Update org.apache.commons.io dependencies to version 2.11
  - Update code to avoid use of methods deprecated by new version 
- Update jlog dependency to version 5.0
  - Modify code for new submit method signature
- Add bin/logentry.bat for executing on Windows
- Add README.md with general build help

## Version 1.3 - September 16, 2013
- Add --link option for referencing prior log entries.

## Version 1.2 - September 16, 2013
 - Add --link option for referencing prior log entries.
 - Version number is printed as part of -h option

## Version 1.0 - April 3, 2013
 - This is the initial release of the Electronic Logbooks command line tool.

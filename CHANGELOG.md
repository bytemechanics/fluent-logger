# Version 2.2.0
---
Utility minor enhancements to optimize CPU consumption on log generation

## Changes
* Done Feature: Add level enabled function to check if a certain level is enabled from the FluentLogger 
* Done Feature: Add message supplier method in all log levels
* Done Feature: Add support for Maven plugin logs
* Now if you want to return to the original logger factury only need to add property LOGGER_FACTORY_ADAPTER_KEY with an empty string

# Version 2.1.1
---
OSGi metadata

## Changes
* Added apache felix to publish OSGi metadata


# Version 2.1.0
---
Custom underlaying logging api manually

## Changes
* Fixed #16 FluentLogger.child should inherit the same loggerAdapter
* Implement issue #15 : Allow to setup manually the underlaying logging api


# Version 2.0.2
---
Fix release with minor enhancement

## Changes
* Fixed When an argument is null logger raises java.lang.NullPointerException
* Done Feature: Generate child logger from a previous one


# Version 2.0.2
---
Fix release with minor enhancement

## Changes
* Fixed When an argument is null logger raises java.lang.NullPointerException
* Done Feature: Generate child logger from a previous one


# Version 2.0.1
---
Minor release (Fix release)

## Changes
* Fixed undesired dependency with log4j-2.x on log4j-1.x causing NoClassDefFound
* Fixed copied classes classpath to the new one

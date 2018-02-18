# Fluent Logger
[![Quality Gate](https://sonarcloud.io/api/badges/gate?key=org.bytemechanics%3Afluent-logger)](https://sonarcloud.io/dashboard/index/org.bytemechanics%3Afluent-logger)
[![Coverage](https://sonarcloud.io/api/badges/measure?key=org.bytemechanics%3Afluent-logger&metric=coverage)](https://sonarcloud.io/dashboard/index/org.bytemechanics%3Afluent-logger)

Simple logging abstraction with standard java logging system

## Motivation
Simplify parameter replacement in logging messages in order to avoid message processing if logger is not enabled

## Quick start
1. First of all include the Jar file in your compile and execution classpath.
**Maven**
```Maven
	<dependency>
		<groupId>org.bytemechanics</groupId>
		<artifactId>fluent-logger</artifactId>
		<version>X.X.X</version>
	</dependency>
```
**Graddle**
```Gradle
dependencies {
    compile 'org.bytemechanics:fluent-logger:X.X.X'
}
```
1. Get logger instance
```Java
package mypackage;
import org.bytemechanics.fluentlogger.FluentLogger;
public class MyClass{
	private static final FluentLogger logger=FluentLogger.getLogger(MyClass.class);
}
```
1. Write a log
```Java
logger.trace("myMessage {} with {}","param1",2,exception);
```


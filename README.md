# Fluent Logger
[![Latest version](https://maven-badges.herokuapp.com/maven-central/org.bytemechanics/fluent-logger/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.bytemechanics/fluent-logger/badge.svg)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=org.bytemechanics%3Afluent-logger&metric=alert_status)](https://sonarcloud.io/dashboard/index/org.bytemechanics%3Afluent-logger)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=org.bytemechanics%3Afluent-logger&metric=coverage)](https://sonarcloud.io/dashboard/index/org.bytemechanics%3Afluent-logger)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

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


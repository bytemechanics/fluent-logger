/*
 * Copyright 2019 Byte Mechanics.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bytemechanics.logger.adapters;

/**
 * Logger API embeded providers list
 * @author afarre
 * @since 2.1.0
 */
public enum LoggerAPIProvider{
	
	/** Log4j provider*/
	LOG4J("org.apache.log4j.Logger","org.bytemechanics.logger.adapters.impl.LoggerLog4jImpl"),
	/** Log4j2 provider using extension*/
	LOG4J2E("org.apache.logging.log4j.spi.ExtendedLoggerWrapper","org.bytemechanics.logger.adapters.impl.LoggerLog4j2Extension"),
	/** Log4j2 provider*/
	LOG4J2("org.apache.logging.log4j.Logger","org.bytemechanics.logger.adapters.impl.LoggerLog4j2Impl"),
	/** Java Logging provider*/
	JSR("java.util.logging.Logger","org.bytemechanics.logger.adapters.impl.LoggerJSRLoggingImpl"),
	/** Console provider*/
	CONSOLE("java.lang.System","org.bytemechanics.logger.adapters.impl.LoggerConsoleImpl"),
	;

	/** Class to use to detect the API */
	public final String detectionClassName;
	/** Class to use to instance the API */
	public final String implementationClassName;
	
	LoggerAPIProvider(final String _detectionClassName,final String _implementationClassName){
		this.detectionClassName=_detectionClassName;
		this.implementationClassName=_implementationClassName;
	}

	/**
	 * Detection class, classloader retrieve
	 * @return detection class from detectionClassName
	 * @throws java.lang.ClassNotFoundException if detectionClassName does not exist in classpath
	 * @see LoggerAPIProvider#detectionClassName
	 */
	public Class getDetectionClass() throws ClassNotFoundException {
		return Class.forName(detectionClassName);
	}
	/**
	 * Implementation class, classloader retrieve
	 * @return detection class from detectionClassName
	 * @throws java.lang.ClassNotFoundException if detectionClassName does not exist in classpath
	 * @see LoggerAPIProvider#implementationClassName
	 */
	public Class getImplementationClass() throws ClassNotFoundException {
		return Class.forName(implementationClassName);
	}
}

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
package org.bytemechanics.logger.internal;

import java.util.function.Function;

/**
 * Factory to instantiate Loggers
 * @author afarre
 */
public enum LoggerFactory implements ReflectionLoaderStrategy{
	
	LOG4J("org.apache.log4j.Logger","org.bytemechanics.fluentlogger.internal.impl.LoggerLog4jImpl"),
	LOG4J2("org.apache.logging.log4j.Logger","org.bytemechanics.fluentlogger.internal.impl.LoggerLog4j2Impl"),
	JSR("java.util.logging.Logger","org.bytemechanics.fluentlogger.internal.impl.LoggerJSRLoggingImpl"),
	;

	private static final Function<String,LoggerAdapter> factory=ReflectionLoaderStrategy.getFactory(LoggerFactory.class);
	
	private final String targetClass;
	private final String implementation;
	
	LoggerFactory(final String _targetClass,final String _implementation){
		this.targetClass=_targetClass;
		this.implementation=_implementation;
	}
	
	@Override
	public String getTargetClassName(){
		return this.targetClass;
	}
	@Override
	public String getImplementationClassName(){
		return this.implementation;
	}
	
	
	public static LoggerAdapter getLogger(final String _name){
		return LoggerFactory.factory.apply(_name);
	}
}

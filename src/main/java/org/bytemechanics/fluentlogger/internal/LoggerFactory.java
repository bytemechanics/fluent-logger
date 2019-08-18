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
package org.bytemechanics.fluentlogger.internal;

import java.lang.reflect.Constructor;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.bytemechanics.fluentlogger.exceptions.NoLoggingAPIPresent;
import org.bytemechanics.fluentlogger.internal.commons.functional.LambdaUnchecker;
import org.bytemechanics.fluentlogger.internal.commons.string.SimpleFormat;

/**
 * Factory to instantiate Loggers
 * @author afarre
 */
public enum LoggerFactory {
	
	LOG4J("org.apache.log4j.Logger","org.bytemechanics.fluentlogger.internal.impl.LoggerLog4jImpl"),
	LOG4J2("org.apache.logging.log4j.Logger","org.bytemechanics.fluentlogger.internal.impl.LoggerLog4j2Impl"),
	JSR("java.util.logging.Logger","org.bytemechanics.fluentlogger.internal.impl.LoggerJSRLoggingImpl"),
	;

	private static final Function<String,LoggerAdapter> factory=Stream.of(LoggerFactory.values())
																		.sequential()
																		.filter(LoggerFactory::isAPIPresent)
																		.map(LambdaUnchecker.uncheckedFunction(LoggerFactory::getImplementation))
																		.map(LambdaUnchecker.uncheckedFunction(LoggerFactory::getConstructor))
																		.map(LoggerFactory::buidFactory)
																		.findFirst()
																			.orElseThrow(NoLoggingAPIPresent::new);
	
	private final String targetClass;
	private final String implementation;
	
	LoggerFactory(final String _targetClass,final String _implementation){
		this.targetClass=_targetClass;
		this.implementation=_implementation;
	}
	
	public boolean isAPIPresent(){
		
		boolean reply=false;
		
		try {
			reply=(Class.forName(targetClass)!=null);
		} catch (ClassNotFoundException|LinkageError ex) {
			//We do nothing because we are detecting if the class exist
			Logger.getLogger(LoggerFactory.class.getName()).log(Level.FINEST,ex, () -> SimpleFormat.format("API class {} for {} not found!", targetClass,name()));
		}
		
		return reply;
	}
	public Class<? extends LoggerAdapter> getImplementation() throws ClassNotFoundException{
		return (Class<? extends LoggerAdapter>) Class.forName(implementation);
	}
	public static Constructor<? extends LoggerAdapter> getConstructor(final Class<? extends LoggerAdapter> _adapter) throws NoSuchMethodException{
		return _adapter.getConstructor(String.class);
	}
	public static Function<String,LoggerAdapter> buidFactory(final Constructor<? extends LoggerAdapter> _constructor){
		return LambdaUnchecker.uncheckedFunction(loggerName -> _constructor.newInstance(loggerName));
	}
		
	public static LoggerAdapter getLogger(final String _name){
		return LoggerFactory.factory.apply(_name);
	}
}

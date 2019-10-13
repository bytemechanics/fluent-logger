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

import java.lang.reflect.Constructor;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.bytemechanics.fluentlogger.internal.commons.functional.LambdaUnchecker;
import org.bytemechanics.fluentlogger.internal.commons.string.SimpleFormat;
import org.bytemechanics.logger.internal.impl.LoggerConsoleImpl;

/**
 *
 * @author afarre
 */
public interface ReflectionLoaderStrategy {
	
	public String name();
	public String getTargetClassName();
	public String getImplementationClassName();
	
	default <T> Class<? extends T> forName(final String _name,final Class<T> _class) throws ClassNotFoundException,LinkageError{
		return (Class<? extends T>)Class.forName(_name);
	}
	default boolean isAPIPresent(){
		
		boolean reply=false;
		
		try {
			reply=(forName(getTargetClassName(),Object.class)!=null);
		} catch (ClassNotFoundException|LinkageError ex) {
			//We do nothing because we are detecting if the class exist
			Logger.getLogger(LoggerFactory.class.getName()).log(Level.FINEST,ex, () -> SimpleFormat.format("API class {} for {} not found!", getTargetClassName(),name()));
		}
		
		return reply;
	}
	default Class<? extends LoggerAdapter> getImplementationClass() throws ClassNotFoundException{
		return forName(getImplementationClassName(),LoggerAdapter.class);
	}
	public static Constructor<? extends LoggerAdapter> getConstructor(final Class<? extends LoggerAdapter> _adapter) throws NoSuchMethodException{
		return _adapter.getConstructor(String.class);
	}
	public static Function<String,LoggerAdapter> buidFactory(final Constructor<? extends LoggerAdapter> _constructor){
		return LambdaUnchecker.uncheckedFunction(loggerName -> _constructor.newInstance(loggerName));
	}
	public static Function<String,LoggerAdapter> consoleLogger(){
		System.out.println("[WARNING] FluentLogger: No logging API present in classpath: Log4j, Log4j2 or Logging api not found, all logging INFO or greater priority will be printed into console\n"
							+ "\tIf you want to remove this message or print into file, please import into your classpath  Log4j, Log4j2 or Logging api");
		return LoggerConsoleImpl::new;
	}

	public static Function<String,LoggerAdapter> getFactory(final Class<? extends ReflectionLoaderStrategy> _class){
		return Stream.of(_class.getEnumConstants())
						.sequential()
						.filter(ReflectionLoaderStrategy::isAPIPresent)
						.map(LambdaUnchecker.uncheckedFunction(ReflectionLoaderStrategy::getImplementationClass))
						.map(LambdaUnchecker.uncheckedFunction(ReflectionLoaderStrategy::getConstructor))
						.map(ReflectionLoaderStrategy::buidFactory)
						.findFirst()
							.orElse(ReflectionLoaderStrategy.consoleLogger());
	}
}

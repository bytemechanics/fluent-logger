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
package org.bytemechanics.logger.internal.factory.utils;

import java.lang.reflect.Constructor;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.bytemechanics.logger.adapters.LoggerAPIProvider;
import org.bytemechanics.logger.adapters.LoggerAdapter;
import org.bytemechanics.logger.internal.commons.functional.LambdaUnchecker;
import org.bytemechanics.logger.internal.commons.string.SimpleFormat;

/**
 * Utility class to find correct logger API using reflection
 * @author afarre
 * @since 2.0.0
 */
public class LoggerReflectionUtils{

	/**
	 * Verify if the _api exist by checking the LoggerAPIProvider detectionClass
	 * @param _api logger API to check
	 * @return true if the detectionClass exist in classpath false otherwise
	 * @see LoggerAPIProvider#getDetectionClass() 
	 */
	protected boolean existAPI(final LoggerAPIProvider _api){
		
		boolean reply=false;
		
		try {
			reply=(_api.getDetectionClass()!=null);
		} catch (ClassNotFoundException|LinkageError ex) {
			//We do nothing because we are detecting if the class exist
			Logger.getLogger(LoggerReflectionUtils.class.getName()).log(Level.FINEST,ex, () -> SimpleFormat.format("Logging API {} Class {} not found!",_api.detectionClassName));
		}
		
		return reply;
	}
	/**
	 * Get the constructor of the given _api using the LoggerAPIProvider implementation class
	 * @param <T> logger adapter type
	 * @param _api logger API to check
	 * @return String parameterized constructor of the implementation class if exist null otherwise
	 * @see LoggerAPIProvider#getImplementationClass() 
	 */
	@SuppressWarnings("unchecked")
	protected <T extends LoggerAdapter> Constructor<T> getAPIConstructor(final LoggerAPIProvider _api){
		
		Constructor<T> reply=null;
		
		try {
			final Class<T> loggerAdapterClass=_api.getImplementationClass();
			if(!LoggerAdapter.class.isAssignableFrom(loggerAdapterClass))
				throw new ClassCastException(SimpleFormat.format("Class {} does not implement {}",loggerAdapterClass,LoggerAdapter.class));
			reply=loggerAdapterClass.getConstructor(String.class);
		} catch (ClassNotFoundException e) {
			Logger.getLogger(LoggerReflectionUtils.class.getName()).log(Level.SEVERE, null, e);
			System.err.println(SimpleFormat.format("[WARNING] Detected API {} with {} but can not find implementation class {}",_api.name(),_api.detectionClassName,_api.implementationClassName));
		} catch (LinkageError e) {
			Logger.getLogger(LoggerReflectionUtils.class.getName()).log(Level.SEVERE, null, e);
			System.err.println(SimpleFormat.format("[WARNING] Detected API {} with {} but implementation {} has linkage errors {}",_api.name(),_api.detectionClassName,_api.implementationClassName,e.getMessage()));
		} catch (NoSuchMethodException e) {
			Logger.getLogger(LoggerReflectionUtils.class.getName()).log(Level.SEVERE, null, e);
			System.err.println(SimpleFormat.format("[WARNING] Detected API {} but can not find constructor(String) for class {}",_api.name(),_api.implementationClassName));
		} catch (ClassCastException e) {
			Logger.getLogger(LoggerReflectionUtils.class.getName()).log(Level.SEVERE, null, e);
			System.err.println(SimpleFormat.format("[WARNING] Detected API {} with implementation {} but does not implements {}",_api.name(),_api.implementationClassName,LoggerAdapter.class));
		} catch (SecurityException e) {
			Logger.getLogger(LoggerReflectionUtils.class.getName()).log(Level.SEVERE, null, e);
			System.err.println(SimpleFormat.format("[WARNING] Detected API {} but can not access to {}.constructor(String)",_api.name(),_api.implementationClassName));
		}
		
		return reply;
	}
	/**
	 * Get a LoggerAdapter lambda provider from string unchecking any possible checked exception
	 * @param _constructor constructor to use
	 * @return Function to retrieve LoggerAdapter from a given string logger name
	 * @see LoggerAdapter
	 */
	protected Function<String,LoggerAdapter> buildFactory(final Constructor<? extends LoggerAdapter> _constructor){
		return LambdaUnchecker.uncheckedFunction(_constructor::newInstance);
	}

	/**
	 * Build logger factory from the given loggerAPI returning an optional with the function to generate loggerAdapter from the logger name
	 * @param _loggerAPIProvider the logger api provider to generate the logger
	 * @return optional with the function to generate loggerAdapter from the logger name
	 * @see LoggerAPIProvider
	 * @see LoggerAdapter
	 */
	public Optional<Function<String,LoggerAdapter>> getLoggerFactory(final LoggerAPIProvider _loggerAPIProvider){
		return Optional.ofNullable(_loggerAPIProvider)
						.map(this::getAPIConstructor)
						.filter(Objects::nonNull)
						.map(this::buildFactory);
	}

	/**
	 * Get the first a LoggerAdapter lambda provider from string available from LoggerAPIProvider enum values
	 * @param _loggerAPIProviders stream of logger api providers
	 * @param _defaultLoggerSupplier default lambda if no API provider found
	 * @return Function to retrieve LoggerAdapter from a given string logger name
	 * @see LoggerAPIProvider
	 * @see LoggerAdapter
	 */
	public Function<String,LoggerAdapter> findLoggerFactory(final Stream<LoggerAPIProvider> _loggerAPIProviders,final Supplier<Function<String,LoggerAdapter>> _defaultLoggerSupplier){
		return _loggerAPIProviders
						.sequential()
						.filter(this::existAPI)
						.map(this::getLoggerFactory)
						.filter(Optional::isPresent)
						.map(Optional::get)
						.findFirst()
							.orElseGet(_defaultLoggerSupplier);
	}

	/**
	 * Determine if the object is a Throwable instance
	 * @param _object object to check
	 * @return true if is a Throwable instance
	 */
	public static boolean isThrowable(final Object _object){
		return (_object!=null)&&(Throwable.class.isAssignableFrom(_object.getClass()));
	}

	/**
	 * Cast to throwable
	 * @param _object object to cast
	 * @return the same object cast as throwable
	 */
	public static Throwable castThrowable(final Object _object){
		return (Throwable)_object;
	}
}

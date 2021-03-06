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
package org.bytemechanics.logger.factory;

import org.apache.maven.plugin.logging.Log;
import static org.bytemechanics.logger.FluentLogger.LOGGER_FACTORY_ADAPTER_KEY;
import org.bytemechanics.logger.adapters.LoggerAdapter;
import org.bytemechanics.logger.adapters.impl.LoggerMavenPluginImpl;

/**
 * Logger factory implementation to use FluentLogger in maven plugin. You must instance as try-with-resource before creating the logger.
 * <br>
 * Example:
 * <code>
 *		try(LoggerFactoryMavenPluginImpl instance=new LoggerFactoryMavenPluginImpl(_logger)){
 *			final FluentLogger logger=FluentLogger.of("my-logger")
 *													.prefixed("my-");
 *			(...)
 *		}
 * </code>
 * @author afarre
 * @since 2.2.0
 */
public class LoggerFactoryMavenPluginImpl implements LoggerFactoryAdapter,AutoCloseable{

	private static final InheritableThreadLocal<Log> INSTANCE=new InheritableThreadLocal<>();
	
	
	/**
	 * Constructor to use when already initialized
	 */
	public LoggerFactoryMavenPluginImpl(){
	}
	/**
	 * Constructor to use with try-with-resources
	 * @param _log underlaying logger
	 * @see org.apache.maven.plugin.logging.Log
	 */
	public LoggerFactoryMavenPluginImpl(final Log _log){
		System.setProperty(LOGGER_FACTORY_ADAPTER_KEY, LoggerFactoryMavenPluginImpl.class.getName());
		LoggerFactoryMavenPluginImpl.INSTANCE.set(_log);
	}
	
	
	@Override
	public LoggerAdapter getLogger(String _logger) {
		return new LoggerMavenPluginImpl(_logger,LoggerFactoryMavenPluginImpl.INSTANCE.get());
	}

	@Override
	public void close() {
		LoggerFactoryMavenPluginImpl.INSTANCE.remove();
		System.setProperty(LOGGER_FACTORY_ADAPTER_KEY, "");
	}
}

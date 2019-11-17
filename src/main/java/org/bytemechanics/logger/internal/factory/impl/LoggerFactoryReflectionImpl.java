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
package org.bytemechanics.logger.internal.factory.impl;

import java.util.function.Function;
import java.util.stream.Stream;
import org.bytemechanics.logger.adapters.LoggerAPIProvider;
import org.bytemechanics.logger.adapters.LoggerAdapter;
import org.bytemechanics.logger.adapters.impl.LoggerConsoleImpl;
import org.bytemechanics.logger.factory.LoggerFactoryAdapter;
import org.bytemechanics.logger.internal.factory.utils.LoggerReflectionUtils;

/**
 * Logger factory reflection implementation
 * @author afarre
 * @since 2.0.0
 */
public class LoggerFactoryReflectionImpl implements LoggerFactoryAdapter{

	private final Function<String,LoggerAdapter> loggerFactory;
	
	
	public LoggerFactoryReflectionImpl(){
		this(new LoggerReflectionUtils()
					.findLoggerFactory(Stream.of(LoggerAPIProvider.values())
												.filter(apiProvider -> !LoggerAPIProvider.CONSOLE.equals(apiProvider))
										,LoggerFactoryReflectionImpl::consoleLogger));
	}
	public LoggerFactoryReflectionImpl(final Function<String,LoggerAdapter> _loggerFactory){
		this.loggerFactory=_loggerFactory;
	}
	
	
	@Override
	public LoggerAdapter getLogger(String _logger) {
		return loggerFactory.apply(_logger);
	}

	
	public static Function<String,LoggerAdapter> consoleLogger(){
		System.out.println("[WARNING] FluentLogger: No logging API present in classpath: Log4j, Log4j2 or Logging api not found, all logging INFO or greater priority will be printed into console\n"
							+ "\tIf you want to remove this message or print into file, please import into your classpath  Log4j, Log4j2 or Logging api");
		return LoggerConsoleImpl::new;
	}
}

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
package org.bytemechanics.logger.internal.adapters.impl;

import java.util.Optional;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.spi.AbstractLogger;
import org.apache.logging.log4j.spi.ExtendedLoggerWrapper;
import org.bytemechanics.fluentlogger.FluentLogger;
import org.bytemechanics.logger.internal.LogBean;
import org.bytemechanics.logger.internal.adapters.LoggerAdapter;

/**
 * Logger adapter Log4j 2 alternative implementation using log4j2 extensions
 * @see https://logging.apache.org/log4j/2.x/
 * @author afarre
 * @since 2.0.0
 */
public class LoggerLog4j2Extension extends ExtendedLoggerWrapper implements LoggerAdapter {

	private static final Level[] LEVEL_TRANSLATION = {Level.TRACE, Level.DEBUG, Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR, Level.FATAL};

	public LoggerLog4j2Extension(final String _logName) {
		this(LogManager.getLogger(_logName));
	}
	public LoggerLog4j2Extension(final Logger _logger) {
		super((AbstractLogger) _logger, _logger.getName(),_logger.getMessageFactory());
	}

	protected Level translateLevel(org.bytemechanics.logger.Level _level){
		return LEVEL_TRANSLATION[_level.index];
	}
	
	@Override
	public String getName() {
		return super.getName();
	}

	@Override
	public boolean isEnabled(org.bytemechanics.logger.Level _level) {
		return Optional.of(_level)
						.map(this::translateLevel)
						.map(this::isEnabled)
						.orElse(false);
	}
	@Override
	public void log(final LogBean _log) {
		final Level level=translateLevel(_log.getLevel());
		logIfEnabled(FluentLogger.class.getName(),level,null,_log.getMessage(),_log.getStacktrace().orElse(null));
	}
}

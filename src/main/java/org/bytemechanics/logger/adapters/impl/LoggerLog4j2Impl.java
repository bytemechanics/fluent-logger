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
package org.bytemechanics.logger.adapters.impl;

import java.util.Optional;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytemechanics.logger.adapters.LoggerAdapter;
import org.bytemechanics.logger.beans.LogBean;

/**
 * Logger adapter Log4j 2 implementation
 * @see <a href="https://logging.apache.org/log4j/2.x/">https://logging.apache.org/log4j/2.x/</a>
 * @author afarre
 * @since 2.1.0
 */
public class LoggerLog4j2Impl implements LoggerAdapter {

	private static final Level[] LEVEL_TRANSLATION = {Level.TRACE, Level.DEBUG, Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR, Level.FATAL};

	
	private final Logger internalLogger;

	
	public LoggerLog4j2Impl(final String _logName) {
		this(LogManager.getLogger(_logName));
	}
	public LoggerLog4j2Impl(final Logger _logger) {
		this.internalLogger = _logger;
	}

	protected Level translateLevel(org.bytemechanics.logger.Level _level){
		return LEVEL_TRANSLATION[_level.index];
	}

	@Override
	public String getName() {
		return this.internalLogger.getName();
	}

	@Override
	public boolean isEnabled(org.bytemechanics.logger.Level _level) {
		return Optional.of(_level)
						.map(this::translateLevel)
						.map(this.internalLogger::isEnabled)
						.orElse(false);
	}
	@Override
	public void log(final LogBean _log) {
		final Level level=translateLevel(_log.getLevel());
		this.internalLogger.log(level,_log.getMessage(),_log.getThrowable().orElse(null));
	}
}

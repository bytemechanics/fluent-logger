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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.bytemechanics.logger.internal.LogBean;
import org.bytemechanics.logger.internal.adapters.LoggerAdapter;

/**
 * Logger adapter Log4j implementation
 * @see <a href="https://logging.apache.org/log4j/1.2/">https://logging.apache.org/log4j/1.2/</a>
 * @author afarre
 * @since 2.0.0
 */
public class LoggerLog4jImpl implements LoggerAdapter {

	private static final Level[] LEVEL_TRANSLATION = {Level.TRACE, Level.DEBUG, Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR, Level.FATAL};
	private static final Set<String> SKIPPED_CLASS_NAMES = Stream.of(LoggerLog4jImpl.class.getName(),org.apache.logging.log4j.Logger.class.getName())
																	.collect(Collectors.toSet());

	
	private final Logger internalLogger;

	
	public LoggerLog4jImpl(final String _logName) {
		this(Logger.getLogger(_logName));
	}
	public LoggerLog4jImpl(final Logger _logger) {
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
						.map(this.internalLogger::isEnabledFor)
						.orElse(false);
	}
	@Override
	public void log(final LogBean _log) {
		final StackTraceElement stack=_log.getSource(SKIPPED_CLASS_NAMES);
		final Level level=translateLevel(_log.getLevel());
		this.internalLogger.log(stack.getClassName(),level,_log.getMessage().get(),_log.getThrowable().orElse(null));
	}
}

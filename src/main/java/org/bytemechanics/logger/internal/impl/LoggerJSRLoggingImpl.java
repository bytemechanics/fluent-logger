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
package org.bytemechanics.logger.internal.impl;

import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bytemechanics.logger.internal.LogBean;
import org.bytemechanics.logger.internal.LoggerAdapter;

/**
 *
 * @author afarre
 */
public class LoggerJSRLoggingImpl implements LoggerAdapter {

	private static final Level[] LEVEL_TRANSLATION = {Level.FINEST, Level.FINER, Level.FINE, Level.INFO, Level.WARNING, Level.SEVERE, Level.SEVERE};
	private static final Set<String> SKIPPED_CLASS_NAMES = Stream.of(LoggerJSRLoggingImpl.class.getName())
																	.collect(Collectors.toSet());

	
	@SuppressWarnings("NonConstantLogger")
	private final Logger internalLogger;

	
	public LoggerJSRLoggingImpl(final String _logName) {
		this(Logger.getLogger(_logName));
	}
	public LoggerJSRLoggingImpl(final Logger _logger) {
		this.internalLogger = _logger;
	}

	protected Level translateLevel(org.bytemechanics.logger.Level _level){
		return LEVEL_TRANSLATION[_level.index];
	}
	
	@Override
	public boolean isEnabled(org.bytemechanics.logger.Level _level) {
		return Optional.of(_level)
						.map(this::translateLevel)
						.map(this.internalLogger::isLoggable)
						.orElse(false);
	}
	@Override
	public void log(final LogBean _log) {
		final StackTraceElement stack=_log.getSource(SKIPPED_CLASS_NAMES);
		final Level level=translateLevel(_log.getLevel());
		this.internalLogger.logp(level,stack.getClassName(),stack.getMethodName(),_log.getStacktrace().orElse(null),_log.getMessage());
	}
}

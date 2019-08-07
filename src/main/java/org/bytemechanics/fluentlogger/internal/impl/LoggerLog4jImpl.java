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
package org.bytemechanics.fluentlogger.internal.impl;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.bytemechanics.fluentlogger.internal.beans.LogBean;
import org.bytemechanics.fluentlogger.internal.LoggerAdapter;

/**
 *
 * @author afarre
 */
public class LoggerLog4jImpl implements LoggerAdapter {

	private static final Level[] LEVEL_TRANSLATION = {Level.TRACE, Level.DEBUG, Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR, Level.FATAL};

	
	private final Logger internalLogger;

	
	public LoggerLog4jImpl(final String _logName) {
		this.internalLogger = Logger.getLogger(_logName);
	}

	
	@Override
	public boolean isEnabled(org.bytemechanics.fluentlogger.Level _level) {
		return this.internalLogger.isEnabledFor(LEVEL_TRANSLATION[_level.index]);
	}
	@Override
	public void log(final LogBean _log) {
		this.internalLogger.log(_log.getSourceClass()
								,LEVEL_TRANSLATION[_log.getLevel().index]
								,_log.getMessage().get()
								,_log.getStacktrace()
										.orElse(null));
	}
}

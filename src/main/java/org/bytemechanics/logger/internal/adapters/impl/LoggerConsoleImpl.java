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

import org.bytemechanics.fluentlogger.internal.commons.string.SimpleFormat;
import org.bytemechanics.logger.Level;
import org.bytemechanics.logger.internal.LogBean;
import org.bytemechanics.logger.internal.adapters.LoggerAdapter;

/**
 * Standard output logger adapter (intended to use only as backup)
 * @author afarre
 * @since 2.0.0
 */
public class LoggerConsoleImpl implements LoggerAdapter {

	private static final String PATTERN ="{} [{}] ({}): {}";

	
	private final String logName;

	
	public LoggerConsoleImpl(final String _logName) {
		this.logName = _logName;
	}

	protected String getMessage(final LogBean _log){
		return SimpleFormat.format(PATTERN,_log.getTime(),_log.getLevel().name(),this.logName,_log.getMessage().get());
	}
	
	@Override
	public String getName() {
		return this.logName;
	}

	@Override
	public boolean isEnabled(final Level _level) {
		return Level.INFO.index<=_level.index;
	}
	
	@Override
	public void log(final LogBean _log) {
		
		final String message=getMessage(_log);
		
		switch (_log.getLevel()) {
			case CRITICAL:
				System.err.println(message);
				break;
			case ERROR:
				System.err.println(message);
				break;
			default:
				System.out.println(message);
		}
	}
}

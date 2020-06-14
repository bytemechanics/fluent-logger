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
package org.bytemechanics.logger.adapters;

import org.bytemechanics.logger.Level;

/**
 * Logger adapter to implement to provider lower level API support
 * @author afarre
 * @since 2.1.0
 */
public interface LoggerAdapter {
    
	/**
	 * Returns the logger name
	 * @return logger name
	 */
    public String getName();
    
	/**
	 * Check if an specific _level is enabled
	 * @param _level fluent logger level
	 * @return true if level is enabled in the underlying API
	 */
    public boolean isEnabled(final Level _level);
	/**
	 * Check if the given _log would be printed if logged
	 * @param _log log message to check
	 * @return true if this _log level is enabled in the underlying API
	 * @see LoggerAdapter#isEnabled(org.bytemechanics.logger.Level) 
	 */
    public default boolean isEnabled(final Log _log){
		return this.isEnabled(_log.getLevel());
	}
	
	/**
	 * Send the given _log to the underlying API
	 * @param _log the LogAdapter representing the log
	 */
    public void log(final Log _log);
}

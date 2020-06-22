/*
 * Copyright 2020 Byte Mechanics.
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

import org.bytemechanics.logger.Level;
import org.bytemechanics.logger.adapters.Log;
import org.bytemechanics.logger.adapters.LoggerAdapter;

/**
 * Maven plugin log adapter
 * @author afarre
 */
public class LoggerMavenPluginImpl implements LoggerAdapter{

	private final String name;
	private final org.apache.maven.plugin.logging.Log underlayingLog;
	
	
	public LoggerMavenPluginImpl(final String _name,final org.apache.maven.plugin.logging.Log _log){
		this.name=_name;
		this.underlayingLog=_log;
	}

	public org.apache.maven.plugin.logging.Log getUnderlayingLog() {
		return underlayingLog;
	}
	
	
	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public boolean isEnabled(final Level _level) {
		
		boolean reply;
		
		switch(_level){
			case FINEST:
			case TRACE:
			case DEBUG:		reply=this.underlayingLog.isDebugEnabled();
							break;
			case INFO:		reply=this.underlayingLog.isInfoEnabled();
							break;
			case WARNING:	reply=this.underlayingLog.isWarnEnabled();
							break;
			default:		reply=this.underlayingLog.isErrorEnabled();
		}
		
		return reply;
	}

	@Override
	public void log(final Log _log) {

		final String message=_log.getMessage()
									.get();
		final Throwable exception=_log.getThrowable()
										.orElse(null);

		switch(_log.getLevel()){
			case FINEST:
			case TRACE:
			case DEBUG:		this.underlayingLog.debug(message,exception);
							break;
			case INFO:		this.underlayingLog.info(message,exception);
							break;
			case WARNING:	this.underlayingLog.warn(message,exception);
							break;
			default:		this.underlayingLog.error(message,exception);
		}
	}
}

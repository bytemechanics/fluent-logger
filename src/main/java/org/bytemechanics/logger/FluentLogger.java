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
package org.bytemechanics.logger;

import org.bytemechanics.fluentlogger.internal.commons.lang.ArrayUtils;
import org.bytemechanics.logger.internal.LogBean;
import org.bytemechanics.logger.internal.LoggerAdapter;
import org.bytemechanics.logger.internal.LoggerFactory;

/**
 * Simple logging system to log to java logging with more user friendly manner
 * @author afarre
 * @since 2.0.0
 */
public final class FluentLogger {
	
    @SuppressWarnings("NonConstantLogger")
    private final LoggerAdapter loggerAdapter;
	private final String prefix;
    private final Object[] args;

    private FluentLogger(final LoggerAdapter _loggerAdapter, final String _prefix, final Object... _args) {
        this.loggerAdapter = _loggerAdapter;
        this.prefix = _prefix;
        this.args = _args;
    }

	/**
     * Get NEW fluent logger instance with the given name, adds the _prefix to any message
     * @param _prefix prefix to append at the begining of any message writen to this log and after any previous prefix at this logger
     * @return fluent logger instance
     */
    public FluentLogger prefixed(final String _prefix){
		return new FluentLogger(this.loggerAdapter,this.prefix+_prefix,this.args);
	}
	/**
     * Get NEW fluent logger instance from class canonincal name, adds the _prefix to any message using the _initialArgs as first replacement parameters
     * @param _initialArgs replacement values to add as placeholders for any message written, is appended at the end of any other arguments at this logger
     * @return fluent logger instance
     */
    public FluentLogger with(final Object... _initialArgs){
		return new FluentLogger(this.loggerAdapter,this.prefix,ArrayUtils.concat(this.args,_initialArgs));
	}

	
    public FluentLogger log(final LogBean _log) {
		if(this.loggerAdapter.isEnabled(_log)){
			this.loggerAdapter.log(_log);
		}
        return this;
    }
	
	public FluentLogger log(final Level _level,final String _message, final Object... _args) {

		if(this.loggerAdapter.isEnabled(_level)){
			this.loggerAdapter.log(LogBean.of(_level)
												.message(prefix).args(this.args)
												.message(_message).args(_args));
		}
		return this;
    }
	
	
	
	/**
     * Get NEW fluent logger instance with the given name
     * @param _name logger name
     * @return fluent logger instance
     */
    public static final FluentLogger of(final String _name){
		return new FluentLogger(LoggerFactory.getLogger(_name),"");
	}
	/**
     * Get NEW fluent logger instance from class canonincal name
     * @param _class from extract the logger instance
     * @return fluent logger instance
     */
    public static final FluentLogger of(final Class<?> _class){
		return new FluentLogger(LoggerFactory.getLogger(_class.getName()),"");
	}
	
	
	public FluentLogger finest(final Throwable _exception) {
        return finest("", _exception);
    }

    public FluentLogger finest(final String _message, final Object... _args) {
        return log(Level.FINEST,null, _message, _args);
    }

    public FluentLogger trace(final Throwable _exception) {
        return trace("", _exception);
    }

    public FluentLogger trace(final String _message, final Object... _args) {
        return log(Level.TRACE,null, _message, _args);
    }

    public FluentLogger debug(final Throwable _exception) {
        return debug("", _exception);
    }

    public FluentLogger debug(final String _message, final Object... _args) {
        return log(Level.DEBUG,null, _message, _args);
    }

    public FluentLogger info(final Throwable _exception) {
        return info("", _exception);
    }

    public FluentLogger info(final String _message, final Object... _args) {
        return log(Level.INFO,null, _message, _args);
    }

    public FluentLogger warning(final Throwable _exception) {
        return warning("", _exception);
    }

    public FluentLogger warning(final String _message, final Object... _args) {
        return log(Level.WARNING,null, _message, _args);
    }

    public FluentLogger error(final Throwable _exception) {
        return error("", _exception);
    }

    public FluentLogger error(final String _message, final Object... _args) {
        return log(Level.ERROR,null, _message, _args);
    }

    public FluentLogger critical(final Throwable _exception) {
        return critical("", _exception);
    }

    public FluentLogger critical(final String _message, final Object... _args) {
        return log(Level.CRITICAL,null, _message, _args);
    }
}

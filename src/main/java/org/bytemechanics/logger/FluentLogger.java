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
import org.bytemechanics.fluentlogger.internal.commons.string.SimpleFormat;
import org.bytemechanics.logger.internal.LogBean;
import org.bytemechanics.logger.internal.adapters.LoggerAdapter;
import org.bytemechanics.logger.internal.factory.LoggerFactoryAdapter;
import org.bytemechanics.logger.internal.factory.impl.LoggerFactoryReflectionImpl;

/**
 * Simple logging system to log to java logging with more user friendly manner
 * @author afarre
 * @since 2.0.0
 */
public final class FluentLogger {
	
	/** Logger factory system property key used to load a distinct logger factory*/
	public static final String LOGGER_FACTORY_ADAPTER_KEY="fluent.logger.adapter.factory";
	
	protected static LoggerFactoryAdapter LOGGER_FACTORY=new LoggerFactoryReflectionImpl();
    
	@SuppressWarnings("NonConstantLogger")
    protected final LoggerAdapter loggerAdapter;
	protected final String prefix;
    protected final Object[] args;

    private FluentLogger(final LoggerAdapter _loggerAdapter, final String _prefix, final Object... _args) {
        this.loggerAdapter = _loggerAdapter;
        this.prefix = _prefix;
        this.args = _args;
    }

		
	/**
	 * Get the current LoggerFactoryAdapter instance
	 * @return the default LoggerFactoryAdapter instance or the configured as system property buy LOGGER_FACTORY_ADAPTER_KEY
	 * @see FluentLogger#LOGGER_FACTORY_ADAPTER_KEY
	 */
	protected static LoggerFactoryAdapter getLoggerFactory(){

		final String className=System.getProperty(LOGGER_FACTORY_ADAPTER_KEY);
		if((className!=null)&&(!LOGGER_FACTORY.getClass().getName().equals(className))){
			synchronized(FluentLogger.class){
				if(!LOGGER_FACTORY.getClass().getName().equals(className)){
					final Class factoryClazzCandidate;
					try {
						factoryClazzCandidate = Class.forName(className);
						LOGGER_FACTORY=((Class<LoggerFactoryAdapter>)factoryClazzCandidate).newInstance();
					} catch (ClassCastException e) {
						System.err.println(SimpleFormat.format("WARNING: Configured logger factory propery {} class {} does not implement LoggerFactoryAdapter. Error message: {}"
																+ "\n\tTo remove this message please remove this attribute from system properties or configure a correct class",LOGGER_FACTORY_ADAPTER_KEY,className,e.getMessage()));
					} catch (ClassNotFoundException e) {
						System.err.println(SimpleFormat.format("WARNING: Configured logger factory propery {} class {} does not exist. Error message: {}"
																+ "\n\tTo remove this message please remove this attribute from system properties or configure a correct class",LOGGER_FACTORY_ADAPTER_KEY,className,e.getMessage()));
					} catch (InstantiationException e) {
						System.err.println(SimpleFormat.format("WARNING: Configured logger factory propery {} class {} can not be instantiated or does not have default constructor. Error message: {}"
																+ "\n\tTo remove this message please remove this attribute from system properties or configure a correct class",LOGGER_FACTORY_ADAPTER_KEY,className,e.getMessage()));
					} catch (IllegalAccessException e) {
						System.err.println(SimpleFormat.format("WARNING: Configured logger factory propery {} class {} has non public default contructor. Error message: {}"
																+ "\n\tTo remove this message please remove this attribute from system properties or configure a correct class",LOGGER_FACTORY_ADAPTER_KEY,className,e.getMessage()));
					}
				}
			}
		}
		return LOGGER_FACTORY;
	}
	
	
	/**
     * Get NEW fluent logger instance with the given name
     * @param _name logger name
     * @return fluent logger instance
     */
    public static final FluentLogger of(final String _name){
		if(_name==null)
			throw new NullPointerException("Can not retrieve logger from null name");
		return new FluentLogger(getLoggerFactory().getLogger(_name),"");
	}
	/**
     * Get NEW fluent logger instance from class canonincal name
     * @param _class from extract the logger instance
     * @return fluent logger instance
     */
    public static final FluentLogger of(final Class<?> _class){
		if(_class==null)
			throw new NullPointerException("Can not retrieve logger from null class");
		return new FluentLogger(getLoggerFactory().getLogger(_class.getName()),"");
	}
	/**
     * Get NEW fluent logger instance with the given name, adds the _prefix to any message
     * @param _prefix prefix to append at the begining of any message writen to this log and after any previous prefix at this logger
     * @return fluent logger instance
     */
    public FluentLogger prefixed(final String _prefix){
		return (_prefix==null)? this : new FluentLogger(this.loggerAdapter,this.prefix+_prefix,this.args);
	}
	/**
     * Get NEW fluent logger instance from class canonincal name, adds the _prefix to any message using the _initialArgs as first replacement parameters
     * @param _initialArgs replacement values to add as placeholders for any message written, is appended at the end of any other arguments at this logger
     * @return fluent logger instance
     */
    public FluentLogger with(final Object... _initialArgs){
		return new FluentLogger(this.loggerAdapter,this.prefix,ArrayUtils.concat(this.args,_initialArgs));
	}

	
	/**
	 * Allows send the given _log to the underlying logger API if the log level is enabled
	 * @param _log log to send
	 * @return same FluentLogger instance
	 */
    public FluentLogger log(final LogBean _log) {
		if(this.loggerAdapter.isEnabled(_log)){
			this.loggerAdapter.log(_log);
		}
        return this;
    }
	/**
	 * Allows send the given _message replacing the _args prefixed with any previous registered prefix and arguments to the underlying logger API
	 * @param _level log level
	 * @param _message log message
	 * @param _args log replacement arguments with the pattern '{}'
	 * @return same FluentLogger instance
	 */
	public FluentLogger log(final Level _level,final String _message, final Object... _args) {

		if(this.loggerAdapter.isEnabled(_level)){
			this.loggerAdapter.log(LogBean.of(_level)
											.message(prefix).args(this.args)
											.message(_message).args(_args));
		}
		return this;
    }

	/**
	 * Returns the logger name
	 * @return logger name
	 */
    public String getName(){
		return this.loggerAdapter.getName();
	}

	/**
	 * Log the given _exception to a finest level
	 * @param _exception exception to log
	 * @return this logger
	 */
	public FluentLogger finest(final Throwable _exception) {
		return log(Level.FINEST, "", _exception);
    }
	/**
	 * Log the given _message and _args to a finest level
	 * @param _message log message
	 * @param _args log replacement arguments with the pattern '{}'
	 * @return this logger
	 */
    public FluentLogger finest(final String _message, final Object... _args) {
        return log(Level.FINEST, _message, _args);
    }

	/**
	 * Log the given _exception to a trace level
	 * @param _exception exception to log
	 * @return this logger
	 */
    public FluentLogger trace(final Throwable _exception) {
		return log(Level.TRACE, "", _exception);
    }
	/**
	 * Log the given _message and _args to a trace level
	 * @param _message log message
	 * @param _args log replacement arguments with the pattern '{}'
	 * @return this logger
	 */
    public FluentLogger trace(final String _message, final Object... _args) {
        return log(Level.TRACE, _message, _args);
    }

	/**
	 * Log the given _exception to a debug level
	 * @param _exception exception to log
	 * @return this logger
	 */
    public FluentLogger debug(final Throwable _exception) {
		return log(Level.DEBUG, "", _exception);
    }
	/**
	 * Log the given _message and _args to a debug level
	 * @param _message log message
	 * @param _args log replacement arguments with the pattern '{}'
	 * @return this logger
	 */
    public FluentLogger debug(final String _message, final Object... _args) {
        return log(Level.DEBUG, _message, _args);
    }

	/**
	 * Log the given _exception to a info level
	 * @param _exception exception to log
	 * @return this logger
	 */
    public FluentLogger info(final Throwable _exception) {
		return log(Level.INFO, "", _exception);
    }
	/**
	 * Log the given _message and _args to a info level
	 * @param _message log message
	 * @param _args log replacement arguments with the pattern '{}'
	 * @return this logger
	 */
     public FluentLogger info(final String _message, final Object... _args) {
        return log(Level.INFO, _message, _args);
    }

 	/**
	 * Log the given _exception to a warning level
	 * @param _exception exception to log
	 * @return this logger
	 */
   public FluentLogger warning(final Throwable _exception) {
		return log(Level.WARNING, "", _exception);
    }
	/**
	 * Log the given _message and _args to a warning level
	 * @param _message log message
	 * @param _args log replacement arguments with the pattern '{}'
	 * @return this logger
	 */
    public FluentLogger warning(final String _message, final Object... _args) {
        return log(Level.WARNING, _message, _args);
    }

	/**
	 * Log the given _exception to a error level
	 * @param _exception exception to log
	 * @return this logger
	 */
    public FluentLogger error(final Throwable _exception) {
		return log(Level.ERROR, "", _exception);
    }
	/**
	 * Log the given _message and _args to a error level
	 * @param _message log message
	 * @param _args log replacement arguments with the pattern '{}'
	 * @return this logger
	 */
     public FluentLogger error(final String _message, final Object... _args) {
        return log(Level.ERROR, _message, _args);
    }

	/**
	 * Log the given _exception to a critical level
	 * @param _exception exception to log
	 * @return this logger
	 */
    public FluentLogger critical(final Throwable _exception) {
		return log(Level.CRITICAL, "", _exception);
    }
	/**
	 * Log the given _message and _args to a critical level
	 * @param _message log message
	 * @param _args log replacement arguments with the pattern '{}'
	 * @return this logger
	 */
     public FluentLogger critical(final String _message, final Object... _args) {
        return log(Level.CRITICAL, _message, _args);
    }
}

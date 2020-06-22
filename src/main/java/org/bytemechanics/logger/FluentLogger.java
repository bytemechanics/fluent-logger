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

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import org.bytemechanics.logger.adapters.Log;
import org.bytemechanics.logger.adapters.LoggerAPIProvider;
import org.bytemechanics.logger.adapters.LoggerAdapter;
import org.bytemechanics.logger.beans.LogBean;
import org.bytemechanics.logger.beans.LogSupplierBean;
import org.bytemechanics.logger.factory.LoggerFactoryAdapter;
import org.bytemechanics.logger.internal.commons.lang.ArrayUtils;
import org.bytemechanics.logger.internal.commons.string.SimpleFormat;
import org.bytemechanics.logger.internal.factory.impl.LoggerFactoryReflectionImpl;
import org.bytemechanics.logger.internal.factory.utils.LoggerReflectionUtils;

/**
 * Simple logging system to log to java logging with more user friendly manner
 * @author afarre
 * @since 2.0.0
 */
public final class FluentLogger {
	
	/** Logger factory system property key used to load a distinct logger factory*/
	public static final String LOGGER_FACTORY_ADAPTER_KEY="fluent.logger.adapter.factory";
	
	protected static LoggerFactoryAdapter loggerFactory=new LoggerFactoryReflectionImpl();
    
	protected final Function<String,LoggerAdapter> apiLoggerSupplier;
	@SuppressWarnings("NonConstantLogger")
    protected final String name;
    protected final LoggerAdapter loggerAdapter;
	protected final String prefix;
    protected final Object[] args;

    protected FluentLogger(final Function<String,LoggerAdapter> _apiLoggerSupplier,final String _name, final String _prefix, final Object... _args) {
		this(_apiLoggerSupplier, _apiLoggerSupplier.apply(_name), _name, _prefix, _args);
    }
    private FluentLogger(final Function<String,LoggerAdapter> _apiLoggerSupplier,LoggerAdapter _loggerAdapter,final String _name, final String _prefix, final Object... _args) {
        this.apiLoggerSupplier=_apiLoggerSupplier;
		this.loggerAdapter = _loggerAdapter;
		this.name=_name;
        this.prefix = _prefix;
        this.args = _args;
    }

	private static String getClassNameOrDefault(final String _factoryClassName){
		return Optional.ofNullable(_factoryClassName)
						.map(String::trim)
						.filter(className -> !className.isEmpty())
						.orElse(LoggerFactoryReflectionImpl.class.getName());
				
	}
	private static boolean isNotCurrentFactory(final String _factoryClassName){
		return !loggerFactory.getClass().getName().equals(_factoryClassName);
				
	}
	@SuppressWarnings("unchecked")
	private static void replaceFactory(final String _factoryClassName){
		synchronized(FluentLogger.class){
			if(!loggerFactory.getClass().getName().equals(_factoryClassName)){
				final Class factoryClazzCandidate;
				try {
					factoryClazzCandidate = Class.forName(_factoryClassName);
					loggerFactory=((Class<LoggerFactoryAdapter>)factoryClazzCandidate).newInstance();
				} catch (ClassCastException e) {
					System.err.println(SimpleFormat.format("WARNING: Configured logger factory propery {} class {} does not implement LoggerFactoryAdapter. Error message: {}\n\tTo remove this message please remove this attribute from system properties or configure a correct class",LOGGER_FACTORY_ADAPTER_KEY,_factoryClassName,e.getMessage()));
				} catch (ClassNotFoundException e) {
					System.err.println(SimpleFormat.format("WARNING: Configured logger factory propery {} class {} does not exist. Error message: {}\n\tTo remove this message please remove this attribute from system properties or configure a correct class",LOGGER_FACTORY_ADAPTER_KEY,_factoryClassName,e.getMessage()));
				} catch (InstantiationException e) {
					System.err.println(SimpleFormat.format("WARNING: Configured logger factory propery {} class {} can not be instantiated or does not have default constructor. Error message: {}\n\tTo remove this message please remove this attribute from system properties or configure a correct class",LOGGER_FACTORY_ADAPTER_KEY,_factoryClassName,e.getMessage()));
				} catch (IllegalAccessException e) {
					System.err.println(SimpleFormat.format("WARNING: Configured logger factory propery {} class {} has non public default contructor. Error message: {}\n\tTo remove this message please remove this attribute from system properties or configure a correct class",LOGGER_FACTORY_ADAPTER_KEY,_factoryClassName,e.getMessage()));
				}
			}
		}
	}
		
	/**
	 * Get the current LoggerFactoryAdapter instance
	 * @return the default LoggerFactoryAdapter instance or the configured as system property buy LOGGER_FACTORY_ADAPTER_KEY
	 * @see FluentLogger#LOGGER_FACTORY_ADAPTER_KEY
	 */
	protected static LoggerFactoryAdapter getLoggerFactory(){

		Optional.ofNullable(LOGGER_FACTORY_ADAPTER_KEY)
				.map(System::getProperty)
				.map(FluentLogger::getClassNameOrDefault)
				.filter(FluentLogger::isNotCurrentFactory)
				.ifPresent(FluentLogger::replaceFactory);

		return loggerFactory;
	}
	
	/**
     * Get NEW fluent logger instance with the given name
     * @param _name logger name
     * @return fluent logger instance
     */
    public static final FluentLogger of(final String _name){
		if(_name==null)
			throw new NullPointerException("Can not retrieve logger from null name");
		final LoggerFactoryAdapter factory=getLoggerFactory();
		return new FluentLogger(factory::getLogger,_name,"");
	}
	/**
     * Get NEW fluent logger instance with the given name
     * @param _name logger name
	 * @param _provider logger api provider to use for this logger
     * @return fluent logger instance
     */
    public static final FluentLogger of(final String _name,final LoggerAPIProvider _provider){
		if(_name==null)
			throw new NullPointerException("Can not retrieve logger from null name");
		return Optional.ofNullable(_provider)
						.flatMap((new LoggerReflectionUtils())::getLoggerFactory)
						.map(function -> new FluentLogger(function,_name,""))
						.orElseThrow(() -> new NullPointerException("Can not retrieve logger from null provider"));
	}	
	/**
     * Get NEW fluent logger instance with the given name
     * @param _name logger name
	 * @param _apiLoggerSupplier function to provider an api logger instance from the given name
     * @return fluent logger instance
     */
    public static final FluentLogger of(final String _name,final Function<String,LoggerAdapter> _apiLoggerSupplier){
		if(_name==null)
			throw new NullPointerException("Can not retrieve logger from null name");
		return Optional.ofNullable(_apiLoggerSupplier)
						.map(function -> new FluentLogger(function,_name,""))
						.orElseThrow(() -> new NullPointerException("Can not retrieve logger from null apiLoggerSupplier"));
	}	
	/**
     * Get NEW fluent logger instance from class canonincal name
     * @param _class from extract the logger instance
     * @return fluent logger instance
     */
    public static final FluentLogger of(final Class<?> _class){
		if(_class==null)
			throw new NullPointerException("Can not retrieve logger from null class");
		final LoggerFactoryAdapter factory=getLoggerFactory();
		return new FluentLogger(factory::getLogger,_class.getName(),"");
	}
	/**
     * Get NEW fluent logger instance from class canonincal name
     * @param _class from extract the logger instance
     * @param _provider logger api provider to use for this logger
     * @return fluent logger instance
     */
    public static final FluentLogger of(final Class<?> _class,final LoggerAPIProvider _provider){
		if(_class==null)
			throw new NullPointerException("Can not retrieve logger from null class");
		System.out.println("my function"+(new LoggerReflectionUtils()).getLoggerFactory(_provider));
		return Optional.ofNullable(_provider)
						.flatMap((new LoggerReflectionUtils())::getLoggerFactory)
						.map(function -> new FluentLogger(function,_class.getName(),""))
						.orElseThrow(() -> new NullPointerException("Can not retrieve logger from null provider"));
	}	
	/**
     * Get NEW fluent logger instance from class canonincal name
     * @param _class from extract the logger instance
     * @param _apiLoggerSupplier function to provider an api logger instance from the given class name
     * @return fluent logger instance
     */
    public static final FluentLogger of(final Class<?> _class,final Function<String,LoggerAdapter> _apiLoggerSupplier){
		if(_class==null)
			throw new NullPointerException("Can not retrieve logger from null class");
		return Optional.ofNullable(_apiLoggerSupplier)
						.map(function -> new FluentLogger(function,_class.getName(),""))
						.orElseThrow(() -> new NullPointerException("Can not retrieve logger from null apiLoggerSupplier"));
	}	
	
	/**
     * Get NEW fluent logger instance with the current logger name suffixed with the given name using the current logger prefixes and arguments
     * @param _suffix logger name
     * @return fluent logger instance using the current logger prefixes and arguments
     */
    public final FluentLogger child(final String _suffix){
		if(_suffix==null)
			throw new NullPointerException("Can not retrieve logger from null _suffix");
		return new FluentLogger(this.apiLoggerSupplier,String.join(".",getName(),_suffix),this.prefix,this.args);
	}

	/**
     * Get NEW fluent logger instance with the given name, adds the _prefix to any message
     * @param _prefix prefix to append at the begining of any message writen to this log and after any previous prefix at this logger
     * @return fluent logger instance
     */
    public FluentLogger prefixed(final String _prefix){
		return (_prefix==null)? this : new FluentLogger(this.apiLoggerSupplier,this.loggerAdapter,this.name,this.prefix+_prefix,this.args);
	}
	/**
     * Get NEW fluent logger instance from class canonincal name, adds the _prefix to any message using the _initialArgs as first replacement parameters
     * @param _initialArgs replacement values to add as placeholders for any message written, is appended at the end of any other arguments at this logger
     * @return fluent logger instance
     */
    public FluentLogger with(final Object... _initialArgs){
		return new FluentLogger(this.apiLoggerSupplier,this.loggerAdapter,this.name,this.prefix,ArrayUtils.concat(this.args,_initialArgs));
	}

	/**
     * Return the current prefix
     * @return current prefix
	 * @since 2.2.0
     */
    public String getPrefix(){
		return this.prefix;
	}
	/**
     * Return the current prefix arguments
     * @return Object array with the current prefix arguments
	 * @since 2.2.0
     */
    public Object[] getArgs(){
		return this.args;
	}
	
	/**
	 * Allows send the given _log to the underlying logger API if the log level is enabled
	 * @param _log log to send
	 * @return same FluentLogger instance
	 */
    public FluentLogger log(final Log _log) {
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
											.message(getPrefix()).args(getArgs())
											.message(_message).args(_args));
		}
		return this;
    }
	/**
	 * Allows send the given _messageSupplier and the _exception to the underlying logger API
	 * @param _level log level
	 * @param _messageSupplier log message supplier
	 * @param _exception log throwable attachment
	 * @return same FluentLogger instance
	 */
	public FluentLogger log(final Level _level,final Supplier<String> _messageSupplier, final Throwable _exception) {

		if(this.loggerAdapter.isEnabled(_level)){
			this.loggerAdapter.log(LogSupplierBean.of(_level,
																() -> (SimpleFormat.format(getPrefix(), getArgs())+_messageSupplier.get()),
																_exception));
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
	 * Returns the status of the current log for the given level
	 * @param _level 
	 * @return true if the _level is enabled, false otherwise
	 * @since 2.2.0
	 */
    public boolean isEnabled(final Level _level){
		return this.loggerAdapter.isEnabled(_level);
	}
	
	/**
	 * Returns the status of the current log for Level FINEST
	 * @return true if FINEST is enabled, false otherwise
	 * @since 2.2.0
	 */
	public boolean isFinestEnabled(){
		return isEnabled(Level.FINEST);
	}
	/**
	 * Returns the status of the current log for Level TRACE
	 * @return true if TRACE is enabled, false otherwise
	 * @since 2.2.0
	 */
	public boolean isTraceEnabled(){
		return isEnabled(Level.TRACE);
	}
	/**
	 * Returns the status of the current log for Level DEBUG
	 * @return true if DEBUG is enabled, false otherwise
	 * @since 2.2.0
	 */
	public boolean isDebugEnabled(){
		return isEnabled(Level.DEBUG);
	}
	/**
	 * Returns the status of the current log for Level INFO
	 * @return true if INFO is enabled, false otherwise
	 * @since 2.2.0
	 */
	public boolean isInfoEnabled(){
		return isEnabled(Level.INFO);
	}
	/**
	 * Returns the status of the current log for Level WARNING
	 * @return true if WARNING is enabled, false otherwise
	 * @since 2.2.0
	 */
	public boolean isWarningEnabled(){
		return isEnabled(Level.WARNING);
	}
	/**
	 * Returns the status of the current log for Level ERROR
	 * @return true if ERROR is enabled, false otherwise
	 * @since 2.2.0
	 */
	public boolean isErrorEnabled(){
		return isEnabled(Level.ERROR);
	}
	/**
	 * Returns the status of the current log for Level CRITICAL
	 * @return true if CRITICAL is enabled, false otherwise
	 * @since 2.2.0
	 */
	public boolean isCriticalEnabled(){
		return isEnabled(Level.CRITICAL);
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
	 * Log the given _exception to a finest level
	 * @param _messageSupplier log message supplier
	 * @return this logger
	 */
	public FluentLogger finest(final Supplier<String> _messageSupplier) {
		return log(Level.FINEST,_messageSupplier,null);
    }
	/**
	 * Log the given _exception to a finest level
	 * @param _exception exception to log
	 * @param _messageSupplier log message supplier
	 * @return this logger
	 */
	public FluentLogger finest(final Throwable _exception,final Supplier<String> _messageSupplier) {
		return log(Level.FINEST,_messageSupplier,_exception);
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
	 * Log the given _exception to a finest level
	 * @param _messageSupplier log message supplier
	 * @return this logger
	 */
	public FluentLogger trace(final Supplier<String> _messageSupplier) {
		return log(Level.TRACE,_messageSupplier,null);
    }
	/**
	 * Log the given _exception to a finest level
	 * @param _exception exception to log
	 * @param _messageSupplier log message supplier
	 * @return this logger
	 */
	public FluentLogger trace(final Throwable _exception,final Supplier<String> _messageSupplier) {
		return log(Level.TRACE,_messageSupplier,_exception);
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
	 * Log the given _exception to a finest level
	 * @param _messageSupplier log message supplier
	 * @return this logger
	 */
	public FluentLogger debug(final Supplier<String> _messageSupplier) {
		return log(Level.DEBUG,_messageSupplier,null);
    }
	/**
	 * Log the given _exception to a finest level
	 * @param _exception exception to log
	 * @param _messageSupplier log message supplier
	 * @return this logger
	 */
	public FluentLogger debug(final Throwable _exception,final Supplier<String> _messageSupplier) {
		return log(Level.DEBUG,_messageSupplier,_exception);
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
	 * Log the given _exception to a finest level
	 * @param _messageSupplier log message supplier
	 * @return this logger
	 */
	public FluentLogger info(final Supplier<String> _messageSupplier) {
		return log(Level.INFO,_messageSupplier,null);
    }
	/**
	 * Log the given _exception to a finest level
	 * @param _exception exception to log
	 * @param _messageSupplier log message supplier
	 * @return this logger
	 */
	public FluentLogger info(final Throwable _exception,final Supplier<String> _messageSupplier) {
		return log(Level.INFO,_messageSupplier,_exception);
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
	 * Log the given _exception to a finest level
	 * @param _messageSupplier log message supplier
	 * @return this logger
	 */
	public FluentLogger warning(final Supplier<String> _messageSupplier) {
		return log(Level.WARNING,_messageSupplier,null);
    }
	/**
	 * Log the given _exception to a finest level
	 * @param _exception exception to log
	 * @param _messageSupplier log message supplier
	 * @return this logger
	 */
	public FluentLogger warning(final Throwable _exception,final Supplier<String> _messageSupplier) {
		return log(Level.WARNING,_messageSupplier,_exception);
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
	 * Log the given _exception to a finest level
	 * @param _messageSupplier log message supplier
	 * @return this logger
	 */
	public FluentLogger error(final Supplier<String> _messageSupplier) {
		return log(Level.ERROR,_messageSupplier,null);
    }
	/**
	 * Log the given _exception to a finest level
	 * @param _exception exception to log
	 * @param _messageSupplier log message supplier
	 * @return this logger
	 */
	public FluentLogger error(final Throwable _exception,final Supplier<String> _messageSupplier) {
		return log(Level.ERROR,_messageSupplier,_exception);
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
	 * Log the given _exception to a finest level
	 * @param _messageSupplier log message supplier
	 * @return this logger
	 */
	public FluentLogger critical(final Supplier<String> _messageSupplier) {
		return log(Level.CRITICAL,_messageSupplier,null);
    }
	/**
	 * Log the given _exception to a finest level
	 * @param _exception exception to log
	 * @param _messageSupplier log message supplier
	 * @return this logger
	 */
	public FluentLogger critical(final Throwable _exception,final Supplier<String> _messageSupplier) {
		return log(Level.CRITICAL,_messageSupplier,_exception);
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

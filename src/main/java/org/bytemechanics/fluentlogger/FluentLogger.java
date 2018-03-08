package org.bytemechanics.fluentlogger;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.bytemechanics.fluentlogger.internal.commons.string.SimpleFormat;

/**
 * Simple logging system to log to java logging with more user friendly manner
 * @author afarre
 * @since 1.0.0
 */
public class FluentLogger{

	private static final String UNKNOWN_STACKTRACE = "unknown";

	@SuppressWarnings("NonConstantLogger")
	private final Logger logger;
	private final String prefix;
	private final Object[] args;

	
	protected FluentLogger(final Logger _logger, final String _prefix, final Object... _args) {
		if (_logger==null) {
			throw new NullPointerException("logger is null");
		}
		this.logger=_logger;
		this.prefix=Optional.ofNullable(_prefix)
							.orElse("");
		this.args=_args;
	}

	/**
	 * Get fluent logger instance from class cannonincal name
	 * @param _class from extract the logger instance
	 * @return fluent logger instance
	 */
	public static final FluentLogger getLogger(final Class<?> _class) {
		return new FluentLogger(Logger.getLogger(_class.getName()),null);
	}
	/**
	 * Get fluent logger instance with the given name
	 * @param _logName logger name
	 * @return fluent logger instance
	 */
	public static final FluentLogger getLogger(final String _logName) {
		return new FluentLogger(Logger.getLogger(_logName),null);
	}
	/**
	 * Wraps the given java logging logger instance into a fluent logger one
	 * @param _logger underalying logger instance
	 * @return fluent logger instance
	 */
	public static final FluentLogger getLogger(final Logger _logger) {
		return new FluentLogger(_logger,null);
	}
	/**
	 * Get fluent logger instance from class cannonincal name, adds the _prefix to any message using the _initialArgs as first replacement parameters
	 * @param _class from extract the logger instance
	 * @param _prefix prefix to append at the beggining of any message writen to this log
	 * @param _initialArgs replacement values to add at the beggining of the message parameters in order to replace into message placeholder
	 * @return fluent logger instance
	 */
	public static final FluentLogger getLogger(final Class<?> _class, final String _prefix, final Object... _initialArgs) {
		return new FluentLogger(Logger.getLogger(_class.getName()), _prefix, _initialArgs);
	}
	/**
	 * Get fluent logger instance with the given name, adds the _prefix to any message using the _initialArgs as first replacement parameters
	 * @param _logName logger name
	 * @param _prefix prefix to append at the beggining of any message writen to this log
	 * @param _initialArgs replacement values to add at the beggining of the message parameters in order to replace into message placeholder
	 * @return fluent logger instance
	 */
	public static final FluentLogger getLogger(final String _logName, final String _prefix, final Object... _initialArgs) {
		return new FluentLogger(Logger.getLogger(_logName), _prefix, _initialArgs);
	}
	/**
	 * Wraps the given java logging logger instance into a fluent logger one, adds the _prefix to any message using the _initialArgs as first replacement parameters
	 * @param _logger underalying logger instance
	 * @param _prefix prefix to append at the beggining of any message writen to this log
	 * @param _initialArgs replacement values to add at the beggining of the message parameters in order to replace into message placeholder
	 * @return fluent logger instance
	 */
	public static final FluentLogger getLogger(final Logger _logger, final String _prefix, final Object... _initialArgs) {
		return new FluentLogger(_logger, _prefix, _initialArgs);
	}

	
	private FluentLogger internalLog(final Level _level,final Supplier<Throwable> _exception,final Supplier<String> _supplier) {

		if(this.logger.isLoggable(_level)){
			final String[] caller=Stream.of(Thread.currentThread().getStackTrace())
											.filter(stacktrace -> !stacktrace.getClassName().equals(FluentLogger.class.getName()))
											.map(stacktrace -> new String[]{stacktrace.getClassName(), stacktrace.getMethodName()})
											.findFirst()
											.orElse(new String[]{UNKNOWN_STACKTRACE, UNKNOWN_STACKTRACE});
			this.logger.logp(_level, caller[0], caller[1],_exception.get(),_supplier);
		}
		return this;
	}
	public FluentLogger log(final Level _level,final Throwable _exception,final Supplier<String> _supplier) {
		return internalLog(_level,() -> _exception,() -> SimpleFormat.format(this.prefix, this.args)
															.concat(_supplier.get()));
	}
	private Object[] concatArguments(final Object... _args){
		
		final Object[] reply=Arrays.copyOf(this.args, this.args.length+_args.length);
		System.arraycopy(_args, 0, reply, this.args.length,_args.length);
		
		return reply;
	}
	public FluentLogger log(final Level _level, final String _message, final Object... _args) {

		return this.internalLog(_level
								,() -> Optional.of(_args)
												.filter(argsr -> argsr.length>0)
												.filter(argsr -> argsr[argsr.length-1]!=null)
												.filter(argsr -> Throwable.class.isAssignableFrom(argsr[argsr.length-1].getClass()))
												.map(argsr -> (Throwable) argsr[argsr.length-1])
												.orElse(null)
								,() -> Optional.ofNullable(_message)
												.map(this.prefix::concat)
												.map(message -> SimpleFormat.format(message, concatArguments(_args)))
												.orElseGet(this::getFormattedPrefix));
	}

	
	public FluentLogger finest(final Throwable _exception) {
		return finest("", _exception);
	}
	public FluentLogger finest(final String _message, final Object... _args) {
		return log(Level.FINEST, _message, _args);
	}

	public FluentLogger trace(final Throwable _exception) {
		return trace("", _exception);
	}
	public FluentLogger trace(final String _message, final Object... _args) {
		return log(Level.FINER, _message, _args);
	}

	public FluentLogger debug(final Throwable _exception) {
		return debug("", _exception);
	}
	public FluentLogger debug(final String _message, final Object... _args) {
		return log(Level.FINE, _message, _args);
	}

	public FluentLogger info(final Throwable _exception) {
		return info("", _exception);
	}
	public FluentLogger info(final String _message, final Object... _args) {
		return log(Level.INFO, _message, _args);
	}

	public FluentLogger warning(final Throwable _exception) {
		return warning("", _exception);
	}
	public FluentLogger warning(final String _message, final Object... _args) {
		return log(Level.WARNING, _message, _args);
	}

	public FluentLogger error(final Throwable _exception) {
		return error("", _exception);
	}
	public FluentLogger error(final String _message, final Object... _args) {
		return log(Level.SEVERE, _message, _args);
	}
	
	public FluentLogger severe(final Throwable _exception) {
		return severe("", _exception);
	}
	public FluentLogger severe(final String _message, final Object... _args) {
		return log(Level.SEVERE, _message, _args);
	}

	protected Logger getUnderlayingLogger(){
		return this.logger;
	}
	public Optional<String> getPrefix(){
		return Optional.ofNullable(this.prefix)
							.filter(prefixR -> !prefixR.isEmpty());
	}
	public String getFormattedPrefix(){
		return getPrefix()
					.map(prefixR -> SimpleFormat.format(prefixR, this.args))
					.orElse("");
	}
	public Optional<Object[]> getArguments(){
		return Optional.ofNullable(this.args)
							.map(arguments -> Arrays.copyOf(arguments, arguments.length));
	}
}

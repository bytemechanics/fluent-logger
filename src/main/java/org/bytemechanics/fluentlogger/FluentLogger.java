package org.bytemechanics.fluentlogger;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.bytemechanics.fluentlogger.internal.SimpleFormat;

/**
 * @author afarre
 */
public class FluentLogger{

	@SuppressWarnings("NonConstantLogger")
	private final Logger logger;
	private final String prefix;

	
	protected FluentLogger(final Class _class) {
		this(_class.getName(), "");
	}
	protected FluentLogger(final String _logName) {
		this(_logName, "");
	}
	protected FluentLogger(final Logger _logger) {
		this(_logger, "");
	}
	protected FluentLogger(final Class _class, final String _prefix, final Object... _args) {
		this(_class.getName(), _prefix, _args);
	}
	protected FluentLogger(final String _logName, final String _prefix, final Object... _args) {
		this(Logger.getLogger(_logName), _prefix, _args);
	}
	protected FluentLogger(final Logger _logger, final String _prefix, final Object... _args) {
		if (_logger==null) {
			throw new NullPointerException("logger is null");
		}
		this.logger=_logger;
		this.prefix=Optional.ofNullable(_prefix)
							.map(pref -> SimpleFormat.format(pref, _args))
							.orElse("");
	}

	
	public static final FluentLogger getLogger(final Class<?> _class) {
		return new FluentLogger(_class);
	}
	public static final FluentLogger getLogger(final String _logName) {
		return new FluentLogger(_logName);
	}
	public static final FluentLogger getLogger(final Logger _logger) {
		return new FluentLogger(_logger);
	}
	public static final FluentLogger getLogger(final Class<?> _class, final String _prefix, final Object... _args) {
		return new FluentLogger(_class, _prefix, _args);
	}
	public static final FluentLogger getLogger(final String _logName, final String _prefix, final Object... _args) {
		return new FluentLogger(_logName, _prefix, _args);
	}
	public static final FluentLogger getLogger(final Logger _logger, final String _prefix, final Object... _args) {
		return new FluentLogger(_logger, _prefix, _args);
	}

	
	public FluentLogger log(final Level _level, final String _message, final Object... _args) {

		if(this.logger.isLoggable(_level)){
			final String[] caller=Stream.of(Thread.currentThread().getStackTrace())
				.skip(1)
				.filter(stacktrace -> !stacktrace.getClassName().equals(FluentLogger.class.getName()))
				.map(stacktrace -> new String[]{stacktrace.getClassName(), stacktrace.getMethodName()})
				.findFirst()
				.orElse(new String[]{"unknown", "unknown"});
			if ((_args.length>0)
				&&(_args[_args.length-1]!=null)
				&&(Throwable.class.isAssignableFrom(_args[_args.length-1].getClass()))) {
				this.logger.logp(_level, caller[0], caller[1], (Throwable) _args[_args.length-1], () -> this.prefix+Optional.ofNullable(_message)
																											.map(message -> SimpleFormat.format(message, _args))
																											.orElse(""));
			} else {
				this.logger.logp(_level, caller[0], caller[1], () -> this.prefix+Optional.ofNullable(_message)
																		.map(message -> SimpleFormat.format(message, _args))
																		.orElse(""));
			}
		}
		return this;
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

	protected Logger getUnderlayingLogger(){
		return this.logger;
	}
	public Optional<String> getPrefix(){
		return Optional.ofNullable((this.prefix.isEmpty())? null : this.prefix);
	}
}

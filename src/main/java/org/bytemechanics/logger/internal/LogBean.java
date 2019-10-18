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
package org.bytemechanics.logger.internal;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bytemechanics.fluentlogger.internal.commons.lang.ArrayUtils;
import org.bytemechanics.fluentlogger.internal.commons.string.SimpleFormat;
import org.bytemechanics.logger.FluentLogger;
import org.bytemechanics.logger.Level;
import org.bytemechanics.logger.internal.adapters.LoggerAdapter;
import org.bytemechanics.logger.internal.factory.LoggerFactoryAdapter;
import org.bytemechanics.logger.internal.factory.impl.LoggerFactoryReflectionImpl;
import org.bytemechanics.logger.internal.factory.utils.LoggerReflectionUtils;

/**
 * Log message bean
 * @author afarre
 * @since 2.0.0
 */
public class LogBean {

	private static final String UNKNOWN_STACKTRACE = "unknown";
	private static final Set<String> SKIPPED_CLASS_NAMES = Stream.of(Thread.class.getName(),LogBean.class.getName(),FluentLogger.class.getName(),LoggerFactoryAdapter.class.getName(),LoggerFactoryReflectionImpl.class.getName(),LoggerReflectionUtils.class.getName(),LoggerAdapter.class.getName())
																	.collect(Collectors.toSet());

	private final LocalDateTime time;
	private final Level level;
	private final List<String> message;
	private final List<Object[]> args;
	

	private LogBean(final Level _level) {
		this(_level,LocalDateTime.now(),new ArrayList<>(),new ArrayList<>());
	}
	private LogBean(final Level _level,final LocalDateTime _time) {
		this(_level,_time,new ArrayList<>(),new ArrayList<>());
	}
	private LogBean(final Level _level,final LocalDateTime _time,final List<String> _message,final List<Object[]> _args) {
		this.time=_time;
		this.level=_level;
		this.message=_message;
		this.args=_args;
	}

	/**
	 * Append to the current message
	 * @param _message new message to append
	 * @return this logBean
	 */
	public LogBean message(final String _message) {
		this.message.add(_message);
		return this;
	}
	/**
	 * Append new arguments to the current argument queue
	 * @param _args arguments to append
	 * @return this logBean
	 */
	public LogBean args(final Object... _args) {
		this.args.add(_args);
		return this;
	}
	/**
	 * Replaces current log time with the given one
	 * @param _time time to set
	 * @return new logBean with the new time
	 */
	public LogBean time(final LocalDateTime _time) {
		return new LogBean(this.level, _time, this.message, this.args);
	}

	/**
	 * Retrieve the current log time
	 * @return LocalDateTime
	 */
	public LocalDateTime getTime() {
		return time;
	}
	/**
	 * Retrieve the current log level
	 * @return Level
	 */
	public Level getLevel() {
		return level;
	}
	/**
	 * Retrieve supplier for the string message
	 * @return Supplier that provides the message with the replaced arguments once called
	 */
	public Supplier<String> getMessage() {
		return () -> SimpleFormat.format(this.message.stream()
														.collect(Collectors.joining())
										,this.args.stream()
													.reduce(ArrayUtils::concat)
													.orElse(new Object[0]));
	}
	/**
	 * Retrieve stacktrace element log source
	 * @return stacktrace element
	 */
	public StackTraceElement getSource(){
		return getSource(Collections.emptySet());
	}
	/**
	 * Retrieve stacktrace element log source skipping the given classes from the recovered stacktrace
	 * @param _classesToSkip classes to skip from the log stacktrace to reach the correct source
	 * @return stacktrace element
	 */
	public StackTraceElement getSource(final Set<String> _classesToSkip){
		final Set<String> skippedClasses=new HashSet<>(SKIPPED_CLASS_NAMES);
		skippedClasses.addAll(_classesToSkip);
		return Stream.of(Thread.currentThread().getStackTrace())
								.filter(stack -> !skippedClasses.contains(stack.getClassName()))
								.findFirst()
								.orElse(new StackTraceElement(UNKNOWN_STACKTRACE, UNKNOWN_STACKTRACE, UNKNOWN_STACKTRACE, 0));
	}

	private static boolean isThrowable(final Object _object){
		return Throwable.class.isAssignableFrom(_object.getClass());
	}
	private static Throwable castThrowable(final Object _object){
		return (Throwable)_object;
	}
	/**
	 * Retrieve an optional of throwable from the previously given arguments
	 * @return throwable optional
	 */
	public Optional<Throwable> getThrowable() {
		return this.args.stream()
						.flatMap(Stream::of)
						.filter(LogBean::isThrowable)
						.map(LogBean::castThrowable)
						.findFirst();
	}

	/**@see Object#hashCode()  */
	@Override
	public int hashCode() {
		int hash = 0;
		int ic1=0;
		for(Object[] argArray:this.args){
			for(Object arg:argArray){
				hash = 41 * hash + (ic1++) + Objects.hashCode(arg);
			}
		}
		hash = 41 * hash + Objects.hashCode(this.level);
		hash = 41 * hash + Objects.hashCode(this.message);
		hash = 41 * hash + Objects.hashCode(this.time);
		return hash;
	}
	/**@see Object#equals()  */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final LogBean other = (LogBean) obj;
		if (this.level != other.level) {
			return false;
		}
		if (!Objects.equals(this.message, other.message)) {
			return false;
		}
		if (!Objects.equals(this.time, other.time)) {
			return false;
		}
		if(this.args == other.args)
			return true;
		if(this.args != null && other.args == null){
			return false;
		}else if(this.args != null && other.args == null){
			return false;
		}
		Object[] objects1=this.args.stream()
										.reduce(ArrayUtils::concat)
										.orElse(new Object[0]);
		Object[] objects2=other.args.stream()
										.reduce(ArrayUtils::concat)
										.orElse(new Object[0]);
		return Arrays.equals(objects1, objects2);
	}
	/**@see Object#toString()  */
	@Override
	public String toString() {
		return SimpleFormat.format("LogBean{level={}, message={}, args={}}",
											 level, message, args);
	}
	
	/** 
	 * Utility static constructor to instance LogBean in fluent mode  
	 * @param _level log level
	 * @return new LogBean instance using the given _level
	 */
	public static LogBean of(final Level _level){
		return new LogBean(_level);
	}
}

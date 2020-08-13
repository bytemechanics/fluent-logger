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
package org.bytemechanics.logger.beans;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bytemechanics.logger.Level;
import org.bytemechanics.logger.adapters.Log;
import org.bytemechanics.logger.internal.commons.lang.ArrayUtils;
import org.bytemechanics.logger.internal.commons.string.SimpleFormat;
import org.bytemechanics.logger.internal.factory.utils.LoggerReflectionUtils;

/**
 * Log message bean
 * @author afarre
 * @since 2.1.0
 */
public class LogBean implements Log{

	private final LocalDateTime time;
	private final Level level;
	private final List<String> message;
	private final List<Object[]> args;
	

	protected LogBean(final Level _level) {
		this(_level,LocalDateTime.now(),new ArrayList<>(),new ArrayList<>());
	}
	protected LogBean(final Level _level,final LocalDateTime _time,final List<String> _message,final List<Object[]> _args) {
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
		this.args.add(Optional.ofNullable(_args)
						  .orElse(new Object[]{null}));

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

	/** @see Log#getTime() */
	@Override
	public LocalDateTime getTime() {
		return time;
	}
	
	/** @see Log#getLevel() */
	@Override
	public Level getLevel() {
		return level;
	}
	
	/** @see Log#getMessage() */
	@Override
	public Supplier<String> getMessage() {
		return () -> SimpleFormat.format(this.message.stream()
											 .collect(Collectors.joining())
								  ,this.args.stream()
											 .reduce(ArrayUtils::concat)
											 .orElse(new Object[0]));
	}

	/** @see Log#getThrowable() */
	@Override
	public Optional<Throwable> getThrowable() {
		return this.args.stream()
						.flatMap(Stream::of)
						.filter(LoggerReflectionUtils::isThrowable)
						.map(LoggerReflectionUtils::castThrowable)
						.findFirst();
	}

	/** @see Object#hashCode()  */
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
	/**@see Object#equals(java.lang.Object) */
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
		final Object[] objects1=Optional.ofNullable(this.args)
										.flatMap(arg -> arg.stream()
															.reduce(ArrayUtils::concat))
										.orElse(new Object[0]);
		final Object[] objects2=Optional.ofNullable(other.args)
										.flatMap(arg -> arg.stream()
															.reduce(ArrayUtils::concat))
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

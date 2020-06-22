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
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import org.bytemechanics.logger.Level;
import org.bytemechanics.logger.adapters.Log;
import org.bytemechanics.logger.internal.commons.string.SimpleFormat;

/**
 * Log message bean
 * @author afarre
 * @since 2.2.0
 */
public class LogSupplierBean implements Log{

	private final LocalDateTime time;
	private final Level level;
	private final Supplier<String> message;
	private final Throwable throwable;
	

	protected LogSupplierBean(final Level _level,final Supplier<String> _message,final Throwable _throwable) {
		this(_level,LocalDateTime.now(),_message,_throwable);
	}
	protected LogSupplierBean(final Level _level,final LocalDateTime _time,final Supplier<String> _message,final Throwable _throwable) {
		this.time=_time;
		this.level=_level;
		this.message=_message;
		this.throwable=_throwable;
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
		return this.message;
	}

	/** @see Log#getThrowable() */
	@Override
	public Optional<Throwable> getThrowable() {
		return Optional.ofNullable(this.throwable);
	}

	/** @see Object#hashCode()  */
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 37 * hash + Objects.hashCode(this.time);
		hash = 37 * hash + Objects.hashCode(this.level);
		hash = 37 * hash + Objects.hashCode(this.message.get());
		hash = 37 * hash + Objects.hashCode(this.throwable);
		return hash;
	}

	/** @see Object#equals(java.lang.Object) */
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
		final LogSupplierBean other = (LogSupplierBean) obj;
		if (!Objects.equals(this.time, other.time)) {
			return false;
		}
		if (this.level != other.level) {
			return false;
		}
		if (!Objects.equals(this.message.get(), other.message.get())) {
			return false;
		}
		return Objects.equals(this.throwable, other.throwable);
	}
	
	/**@see Object#toString()  */
	@Override
	public String toString() {
		return SimpleFormat.format("LogBean{level={}, message={}}",
											 level, message);
	}
	
	/** 
	 * Utility static constructor to instance LogBean in fluent mode  
	 * @param _level log level
	 * @param _message log message supplier
	 * @param _throwable log exception
	 * @return new LogBean instance using the given _level
	 */
	public static LogSupplierBean of(final Level _level,final Supplier<String> _message,final Throwable _throwable){
		return new LogSupplierBean(_level,_message,_throwable);
	}
}

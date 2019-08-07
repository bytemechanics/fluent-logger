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
package org.bytemechanics.fluentlogger.internal.beans;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import org.bytemechanics.fluentlogger.Level;
import org.bytemechanics.fluentlogger.internal.commons.string.SimpleFormat;

/**
 * Log message bean
 * @author afarre
 */
public class LogBean {

	private final Level level;
	private final Supplier<String> message;
	private final String sourceClass;
	private final String sourceMethod;
	private final Optional<Throwable> stacktrace;

	public LogBean(final Level _level, final Supplier<String> _message, final String _sourceClass, final String _sourceMethod, final Optional<Throwable> _stacktrace) {
		this.level = _level;
		this.message = _message;
		this.sourceClass = _sourceClass;
		this.sourceMethod = _sourceMethod;
		this.stacktrace = _stacktrace;
	}

	public Level getLevel() {
		return level;
	}
	public Supplier<String> getMessage() {
		return message;
	}
	public String getSourceClass() {
		return sourceClass;
	}
	public String getSourceMethod() {
		return sourceMethod;
	}
	public Optional<Throwable> getStacktrace() {
		return stacktrace;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 41 * hash + Objects.hashCode(this.level);
		hash = 41 * hash + Objects.hashCode(this.message);
		hash = 41 * hash + Objects.hashCode(this.sourceClass);
		hash = 41 * hash + Objects.hashCode(this.sourceMethod);
		hash = 41 * hash + Objects.hashCode(this.stacktrace);
		return hash;
	}
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
		if (!Objects.equals(this.sourceClass, other.sourceClass)) {
			return false;
		}
		if (!Objects.equals(this.sourceMethod, other.sourceMethod)) {
			return false;
		}
		if (this.level != other.level) {
			return false;
		}
		if (!Objects.equals(this.message, other.message)) {
			return false;
		}
		return Objects.equals(this.stacktrace, other.stacktrace);
	}
	@Override
	public String toString() {
		return SimpleFormat.format("LogBean{level={}, message={}, sourceClass={}, sourceMethod={}, stacktrace={}}",
											 level, message, sourceClass, sourceMethod, stacktrace);
	}

	public static class LogBeanBuilder {

		private Level level;
		private Supplier<String> message;
		private String sourceClass;
		private String sourceMethod;
		private Throwable stacktrace;

		public LogBeanBuilder level(final Level _level) {
			this.level = _level;
			return this;
		}
		public LogBeanBuilder message(final Supplier<String> _message) {
			this.message = _message;
			return this;
		}
		public LogBeanBuilder sourceClass(final String _sourceClass) {
			sourceClass = _sourceClass;
			return this;
		}
		public LogBeanBuilder sourceMethod(final String _sourceMethod) {
			sourceMethod = _sourceMethod;
			return this;
		}
		public LogBeanBuilder stacktrace(final Throwable _stacktrace) {
			this.stacktrace = _stacktrace;
			return this;
		}

		public LogBean build() {
			return new LogBean(this.level, this.message, this.sourceClass, this.sourceMethod, Optional.ofNullable(this.stacktrace));
		}

		@Override
		public String toString() {
			return SimpleFormat.format("LogBean{level={}, message={}, sourceClass={}, sourceMethod={}, stacktrace={}}",
												level, message, sourceClass, sourceMethod, stacktrace);
		}
	}

	public static LogBeanBuilder builder() {
		return new LogBeanBuilder();
	}
}

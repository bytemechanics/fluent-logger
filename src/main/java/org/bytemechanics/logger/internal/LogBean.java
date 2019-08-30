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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bytemechanics.fluentlogger.FluentLogger;
import org.bytemechanics.fluentlogger.internal.commons.lang.ArrayUtils;
import org.bytemechanics.logger.Level;
import org.bytemechanics.fluentlogger.internal.commons.string.SimpleFormat;

/**
 * Log message bean
 * @author afarre
 */
public class LogBean {

	private static final String UNKNOWN_STACKTRACE = "unknown";
	private static final Set<String> SKIPPED_CLASS_NAMES = Stream.of(LogBean.class.getName(),FluentLogger.class.getName(),LoggerFactory.class.getName(),LoggerAdapter.class.getName())
																	.collect(Collectors.toSet());

	private final Level level;
	private final List<String> message=new ArrayList<>();
	private final List<Object[]> args=new ArrayList<>();
	

	private LogBean(final Level _level) {
		this.level = _level;
	}

	
	public LogBean message(final String _message) {
		this.message.add(_message);
		return this;
	}
	public LogBean args(final Object... _args) {
		this.args.add(_args);
		return this;
	}

	public Level getLevel() {
		return level;
	}
	public Supplier<String> getMessage() {
		return () -> SimpleFormat.format(this.message.stream()
														.collect(Collectors.joining())
										,this.args.stream()
													.reduce(ArrayUtils::concat));
	}
	public StackTraceElement getSource(){
		return getSource(Collections.emptySet());
	}
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
	public Optional<Throwable> getStacktrace() {
		return this.args.stream()
						.flatMap(Stream::of)
						.map(Object::getClass)
						.filter(LogBean::isThrowable)
						.map(LogBean::castThrowable)
						.findFirst();
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 41 * hash + Objects.hashCode(this.level);
		hash = 41 * hash + Objects.hashCode(this.message);
		hash = 41 * hash + Objects.hashCode(this.args);
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
		if (this.level != other.level) {
			return false;
		}
		if (!Objects.equals(this.message, other.message)) {
			return false;
		}
		return Objects.equals(this.args, other.args);
	}
	@Override
	public String toString() {
		return SimpleFormat.format("LogBean{level={}, message={}, args={}}",
											 level, message, args);
	}
	
	public static LogBean of(final Level _level){
		return new LogBean(_level);
	}
}

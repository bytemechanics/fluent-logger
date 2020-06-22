/*
 * Copyright 2020 Byte Mechanics.
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
package org.bytemechanics.logger.adapters;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bytemechanics.logger.FluentLogger;
import org.bytemechanics.logger.Level;
import org.bytemechanics.logger.beans.LogBean;
import org.bytemechanics.logger.factory.LoggerFactoryAdapter;
import org.bytemechanics.logger.internal.factory.impl.LoggerFactoryReflectionImpl;
import org.bytemechanics.logger.internal.factory.utils.LoggerReflectionUtils;

/**
 * Log message interface
 * @author afarre
 * @since 2.2.0
 */
public interface Log {
	
	public static final String UNKNOWN_STACKTRACE = "unknown";
	public static final Set<String> SKIPPED_CLASS_NAMES = Collections.unmodifiableSet(
																	Stream.of(Thread.class.getName(),LogBean.class.getName(),FluentLogger.class.getName(),LoggerFactoryAdapter.class.getName(),LoggerFactoryReflectionImpl.class.getName(),LoggerReflectionUtils.class.getName(),LoggerAdapter.class.getName(),Log.class.getName())
																				.collect(Collectors.toSet()));

	/**
	 * Return the log level
	 * @return log level
	 * @see Level
	 */
	public Level getLevel();

	/**
	 * Return the log timestamp
	 * @return log LocalDateTime
	 */
	public LocalDateTime getTime();

	/**
	 * Return a supplier of the log message
	 * @return log message supplier
	 */
	public Supplier<String> getMessage();

	/**
	 * Return an optional of the throwable attached to the log
	 * @return log throwable optional
	 */
	public Optional<Throwable> getThrowable();
	
	/**
	 * Retrieve stacktrace element log source
	 * @return stacktrace element
	 */
	public default StackTraceElement getSource(){
		return getSource(Collections.emptySet());
	}

	/**
	 * Retrieve stacktrace element log source skipping the given classes from the recovered stacktrace
	 * @param _classesToSkip classes to skip from the log stacktrace to reach the correct source
	 * @return stacktrace element
	 */
	public default StackTraceElement getSource(final Set<String> _classesToSkip){
		final Set<String> skippedClasses=new HashSet<>(SKIPPED_CLASS_NAMES);
		skippedClasses.addAll(_classesToSkip);
		return Stream.of(Thread.currentThread().getStackTrace())
								.filter(stack -> !skippedClasses.contains(stack.getClassName()))
								.findFirst()
								.orElse(new StackTraceElement(UNKNOWN_STACKTRACE, UNKNOWN_STACKTRACE, UNKNOWN_STACKTRACE, 0));
	}

}

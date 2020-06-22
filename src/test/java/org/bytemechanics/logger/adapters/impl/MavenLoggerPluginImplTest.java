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
package org.bytemechanics.logger.adapters.impl;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Stream;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import org.apache.maven.plugin.logging.Log;
import org.bytemechanics.logger.Level;
import org.bytemechanics.logger.beans.LogBean;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * @author afarre
 */
public class MavenLoggerPluginImplTest {
	
	@BeforeAll
	public static void setup() throws IOException{
		System.out.println(">>>>> MavenLoggerPluginImplTest >>>> setupSpec");
		try(InputStream inputStream = MavenLoggerPluginImplTest.class.getResourceAsStream("/logging.properties")){
			LogManager.getLogManager().readConfiguration(inputStream);
		}catch (final IOException e){
			Logger.getAnonymousLogger().severe("Could not load default logging.properties file");
			Logger.getAnonymousLogger().severe(e.getMessage());
		}
	}
	@BeforeEach
    void beforeEachTest(final TestInfo testInfo) {
        System.out.println(">>>>> "+this.getClass().getSimpleName()+" >>>> "+testInfo.getTestMethod().map(Method::getName).orElse("Unkown")+""+testInfo.getTags().toString()+" >>>> "+testInfo.getDisplayName());
    }


	@ParameterizedTest(name="getName should return the name ({0}) given in constructor")
	@ValueSource(strings = {"my-name1","my-name2"})
	public void getName(final String _name,final @Mocked Log _logger){
		final LoggerMavenPluginImpl instance=new LoggerMavenPluginImpl(_name, _logger);
		Assertions.assertAll(() -> Assertions.assertEquals(_logger,instance.getUnderlayingLog())
							,() -> Assertions.assertEquals(_name, instance.getName()));
	}
	
	static Stream<Arguments> translateLevelDatapack() {
	    return Stream.of(
			Arguments.of(Level.FINEST,Level.DEBUG),		
			Arguments.of(Level.TRACE,Level.DEBUG),		
			Arguments.of(Level.DEBUG,Level.DEBUG),		
			Arguments.of(Level.INFO,Level.INFO),		
			Arguments.of(Level.WARNING,Level.WARNING),		
			Arguments.of(Level.ERROR,Level.ERROR),		
			Arguments.of(Level.CRITICAL,Level.ERROR)	
		);
	}
	@ParameterizedTest(name ="isEnabled of level {0} should delegate the log to level {1}")
	@MethodSource("translateLevelDatapack")
	public void isEnabled(final Level _level,final Level _delegatedLevel,final @Mocked Log _logger){
		
		final LoggerMavenPluginImpl instance=new LoggerMavenPluginImpl("my-name", _logger);
		
		new Expectations(){{
			_logger.isDebugEnabled(); result=(_delegatedLevel==Level.DEBUG)? true : false; times=(_delegatedLevel==Level.DEBUG)? 1 : 0;
			_logger.isInfoEnabled(); result=(_delegatedLevel==Level.INFO)? true : false; times=(_delegatedLevel==Level.INFO)? 1 : 0;
			_logger.isWarnEnabled(); result=(_delegatedLevel==Level.WARNING)? true : false; times=(_delegatedLevel==Level.WARNING)? 1 : 0;
			_logger.isErrorEnabled(); result=(_delegatedLevel==Level.ERROR)? true : false; times=(_delegatedLevel==Level.ERROR)? 1 : 0;
		}};
		
		Assertions.assertTrue(instance.isEnabled(_level));
	}
	
	@ParameterizedTest(name ="Log of level {0} should delegate the log to level {1} with message")
	@MethodSource("translateLevelDatapack")
	public void log(final Level _level,final Level _delegatedLevel,final @Mocked Log _logger, final @Mocked LogBean _logBean){
		
		final LoggerMavenPluginImpl instance=new LoggerMavenPluginImpl("my-name", _logger);
		
		final String EXPECTED_MESSAGE="my-message";
		final Supplier<String> SUPPLIER_MESSAGE=() -> EXPECTED_MESSAGE;
		final Optional<Throwable> OPTIONAL_THROWABLE=Optional.empty();
		
		new Expectations(){{
			_logBean.getLevel(); result=_level;
			_logBean.getMessage(); result=SUPPLIER_MESSAGE;
			_logBean.getThrowable(); result=OPTIONAL_THROWABLE;
		}};
		
		instance.log(_logBean);

		new Verifications(){{
			_logger.debug(EXPECTED_MESSAGE,null); times=(_delegatedLevel==Level.DEBUG)? 1 : 0;
			_logger.info(EXPECTED_MESSAGE,null); times=(_delegatedLevel==Level.INFO)? 1 : 0;
			_logger.warn(EXPECTED_MESSAGE,null); times=(_delegatedLevel==Level.WARNING)? 1 : 0;
			_logger.error(EXPECTED_MESSAGE,null); times=(_delegatedLevel==Level.ERROR)? 1 : 0;
		}};
	}
	
	@ParameterizedTest(name ="Log of level {0} should delegate the log to level {1} with message and exception")
	@MethodSource("translateLevelDatapack")
	public void log_withException(final Level _level,final Level _delegatedLevel,final @Mocked Log _logger, final @Mocked LogBean _logBean){
		
		final LoggerMavenPluginImpl instance=new LoggerMavenPluginImpl("my-name", _logger);
		
		final String EXPECTED_MESSAGE="my-message";
		final Supplier<String> SUPPLIER_MESSAGE=() -> EXPECTED_MESSAGE;
		final Throwable EXPECTED_THROWABLE=new NullPointerException("my-exception");
		final Optional<Throwable> OPTIONAL_THROWABLE=Optional.ofNullable(EXPECTED_THROWABLE);
		
		new Expectations(){{
			_logBean.getLevel(); result=_level;
			_logBean.getMessage(); result=SUPPLIER_MESSAGE;
			_logBean.getThrowable(); result=OPTIONAL_THROWABLE;
		}};
		
		instance.log(_logBean);

		new Verifications(){{
			_logger.debug(EXPECTED_MESSAGE,EXPECTED_THROWABLE); times=(_delegatedLevel==Level.DEBUG)? 1 : 0;
			_logger.info(EXPECTED_MESSAGE,EXPECTED_THROWABLE); times=(_delegatedLevel==Level.INFO)? 1 : 0;
			_logger.warn(EXPECTED_MESSAGE,EXPECTED_THROWABLE); times=(_delegatedLevel==Level.WARNING)? 1 : 0;
			_logger.error(EXPECTED_MESSAGE,EXPECTED_THROWABLE); times=(_delegatedLevel==Level.ERROR)? 1 : 0;
		}};
	}
}

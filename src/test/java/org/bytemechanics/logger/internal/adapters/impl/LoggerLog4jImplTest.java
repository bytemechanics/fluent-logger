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
package org.bytemechanics.logger.internal.adapters.impl;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Stream;
import mockit.Delegate;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import org.apache.log4j.Priority;
import org.bytemechanics.fluentlogger.internal.commons.functional.LambdaUnchecker;
import org.bytemechanics.logger.Level;
import org.bytemechanics.logger.internal.LogBean;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * @author afarre
 */
public class LoggerLog4jImplTest {
	
	@BeforeAll
	public static void setup() throws IOException{
		System.out.println(">>>>> LoggerLog4jImplTest >>>> setupSpec");
		try(InputStream inputStream = LambdaUnchecker.class.getResourceAsStream("/logging.properties")){
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

	@Mocked 
	@Injectable
	@SuppressWarnings("NonConstantLogger")
	private org.apache.log4j.Logger underlayingLogger;
	@Tested
	private LoggerLog4jImpl logger;

	
	@Test
	@DisplayName("GetName should call to underlaying logger getName")
	public void testGetName(){
		
		new Expectations() {{
			underlayingLogger.getName(); result="my-log-name"; times=1;
		}};
		
		Assertions.assertEquals("my-log-name",logger.getName());
	}
	
	static Stream<Arguments> translateLevelDatapack() {
	    return Stream.of(
			Arguments.of(Level.FINEST,org.apache.log4j.Level.TRACE),		
			Arguments.of(Level.TRACE,org.apache.log4j.Level.DEBUG),		
			Arguments.of(Level.DEBUG,org.apache.log4j.Level.DEBUG),		
			Arguments.of(Level.INFO,org.apache.log4j.Level.INFO),		
			Arguments.of(Level.WARNING,org.apache.log4j.Level.WARN),		
			Arguments.of(Level.ERROR,org.apache.log4j.Level.ERROR),		
			Arguments.of(Level.CRITICAL,org.apache.log4j.Level.FATAL)	
		);
	}
	@ParameterizedTest(name ="Log level={0} translation should be equal to level={1}")
	@MethodSource("translateLevelDatapack")
	public void testTranslateLevel(final Level _level,final org.apache.log4j.Level _levelTranslated){
		Assertions.assertEquals(_levelTranslated,logger.translateLevel(_level));
	}
	
	
	static Stream<Arguments> logLevelDatapack() {
	    return Stream.of(
			Arguments.of(Level.FINEST,false),		
			Arguments.of(Level.TRACE,true),		
			Arguments.of(Level.DEBUG,true),		
			Arguments.of(Level.INFO,true),		
			Arguments.of(Level.WARNING,true),		
			Arguments.of(Level.ERROR,true),		
			Arguments.of(Level.CRITICAL,true)	
		);
	}
	@SuppressWarnings("Convert2Lambda")
	@ParameterizedTest(name ="Log level={0} is enabled should answer={1}")
	@MethodSource("logLevelDatapack")
	public void testisEnabled(final Level _level,final boolean _enabled){

		new Expectations() {{
			underlayingLogger.isEnabledFor((org.apache.log4j.Level)any); 
				result=new Delegate<org.apache.log4j.Level>() {
							public boolean delegate(org.apache.log4j.Level _receivedLevel) throws Exception {
								return (_receivedLevel.isGreaterOrEqual(Priority.DEBUG));
							}
						};
				times=1;
		}};

		Assertions.assertEquals(_enabled,logger.isEnabled(_level));
	}
	
	static Stream<Arguments> logDatapack() {
	    return Stream.of(
			Arguments.of(LogBean.of(Level.FINEST).message("my-message {} {}").args("hallo",1).args(new RuntimeException("myRuntimeException"))),		
			Arguments.of(LogBean.of(Level.TRACE).message("my-message {} {}")),		
			Arguments.of(LogBean.of(Level.DEBUG).message("my-message 5 {} {}").args("hallo",1)),		
			Arguments.of(LogBean.of(Level.INFO).message("my-message {} {}").args("hallo",1)),		
			Arguments.of(LogBean.of(Level.WARNING).message("my-message {} {}").args("hallo",1)),		
			Arguments.of(LogBean.of(Level.ERROR).message("my-message 2").args("hallo2",1)),		
			Arguments.of(LogBean.of(Level.CRITICAL).message("my-message 4{} {}").args("hallo").args(new IOException("myIOException2")))	
		);
	}
	@ParameterizedTest(name ="Log logBean={0} should write log={1}")
	@MethodSource("logDatapack")
	public void testLog(final LogBean _log){

		final org.apache.log4j.Level translatedLevel=logger.translateLevel(_log.getLevel());
		final String message=_log.getMessage().get();
		new Expectations() {{
			underlayingLogger.log("org.bytemechanics.logger.internal.adapters.impl.LoggerLog4jImplTest",translatedLevel,message,_log.getStacktrace().orElse(null)); 
				times=1;
		}};
		logger.log(_log);
	}
}

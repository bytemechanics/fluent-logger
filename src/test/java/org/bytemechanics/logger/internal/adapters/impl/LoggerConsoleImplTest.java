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
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Stream;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import org.bytemechanics.logger.Level;
import org.bytemechanics.logger.internal.LogBean;
import org.bytemechanics.logger.internal.commons.functional.LambdaUnchecker;
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
public class LoggerConsoleImplTest {
	
	@BeforeAll
	public static void setup() throws IOException{
		System.out.println(">>>>> LoggerConsoleImplTest >>>> setupSpec");
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

	@Injectable 
	private final String logName = "my-log-name";
	@Tested
	private LoggerConsoleImpl logger;

	
	@Test
	@DisplayName("GetName should call to underlaying logger getName")
	public void testGetName(){
		Assertions.assertEquals(logName,logger.getName());
	}
	
	static Stream<Arguments> logLevelDatapack() {
	    return Stream.of(
			Arguments.of(Level.FINEST,false),		
			Arguments.of(Level.TRACE,false),		
			Arguments.of(Level.DEBUG,false),		
			Arguments.of(Level.INFO,true),		
			Arguments.of(Level.WARNING,true),		
			Arguments.of(Level.ERROR,true),		
			Arguments.of(Level.CRITICAL,true)	
		);
	}
	@ParameterizedTest(name ="Log level={0} is enabled should answer={1}")
	@MethodSource("logLevelDatapack")
	public void testisEnabled(final Level _level,final boolean _enabled){
		Assertions.assertEquals(_enabled,logger.isEnabled(_level));
	}
	
	static Stream<Arguments> logMessageDatapack() {
		final LocalDateTime time=LocalDateTime.of(2019, Month.JANUARY, 1, 1, 1, 1);
	    return Stream.of(
			Arguments.of(LogBean.of(Level.FINEST).time(time).message("my-message {} {}").args("hallo",1),"2019-01-01T01:01:01 [FINEST] (my-log-name): my-message hallo 1"),		
			Arguments.of(LogBean.of(Level.TRACE).time(time).message("my-message {} {}"),"2019-01-01T01:01:01 [TRACE] (my-log-name): my-message null null"),		
			Arguments.of(LogBean.of(Level.DEBUG).time(time).message("my-message 5 {} {}").args("hallo",1),"2019-01-01T01:01:01 [DEBUG] (my-log-name): my-message 5 hallo 1"),		
			Arguments.of(LogBean.of(Level.INFO).time(time).message("my-message {} {}").args("hallo",1),"2019-01-01T01:01:01 [INFO] (my-log-name): my-message hallo 1"),		
			Arguments.of(LogBean.of(Level.WARNING).time(time).message("my-message {} {}").args("hallo",1),"2019-01-01T01:01:01 [WARNING] (my-log-name): my-message hallo 1"),		
			Arguments.of(LogBean.of(Level.ERROR).time(time).message("my-message 2").args("hallo2",1),"2019-01-01T01:01:01 [ERROR] (my-log-name): my-message 2"),		
			Arguments.of(LogBean.of(Level.CRITICAL).time(time).message("my-message 4{} {}").args("hallo",1),"2019-01-01T01:01:01 [CRITICAL] (my-log-name): my-message 4hallo 1")	
		);
	}
	@ParameterizedTest(name ="Log logBean={0} should write log={1}")
	@MethodSource("logMessageDatapack")
	public void testGetMessage(final LogBean _log,final String _message){
		Assertions.assertEquals(_message,logger.getMessage(_log));
	}

	static Stream<Arguments> logDatapack() {
	    return Stream.of(
			Arguments.of(LogBean.of(Level.FINEST),true),		
			Arguments.of(LogBean.of(Level.TRACE),true),		
			Arguments.of(LogBean.of(Level.DEBUG),true),		
			Arguments.of(LogBean.of(Level.INFO),true),		
			Arguments.of(LogBean.of(Level.WARNING),true),		
			Arguments.of(LogBean.of(Level.ERROR),false),		
			Arguments.of(LogBean.of(Level.CRITICAL),false)	
		);
	}
	@ParameterizedTest(name ="Log logBean={0} should write log={1}")
	@MethodSource("logDatapack")
	public void testLog(final LogBean _log,final boolean _isStandardOut,@Mocked PrintStream _standardOut,@Mocked PrintStream _errorOut){

		PrintStream originalStandardOut=System.out;
        System.setOut(_standardOut);
		PrintStream originalStandardErr=System.err;
        System.setErr(_errorOut);
		try{
			final String logMessage=logger.getMessage(_log);
			new Expectations() {{
				_standardOut.println(logMessage); times=(_isStandardOut)? 1 : 0;
				_errorOut.println(logMessage); times=(_isStandardOut)? 0 : 1;
			}};
			logger.log(_log);
		}finally{
			System.setOut(originalStandardOut);
			System.setErr(originalStandardErr);
		}
	}
}

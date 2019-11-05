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
package org.bytemechanics.logger.internal.factory.utils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.function.Function;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Tested;
import org.bytemechanics.fluentlogger.internal.commons.functional.LambdaUnchecker;
import org.bytemechanics.logger.internal.adapters.LoggerAPIProvider;
import org.bytemechanics.logger.internal.adapters.LoggerAdapter;
import org.bytemechanics.logger.internal.adapters.impl.LoggerConsoleImpl;
import org.bytemechanics.logger.internal.adapters.impl.LoggerJSRLoggingImpl;
import org.bytemechanics.logger.internal.factory.impl.LoggerFactoryReflectionImpl;
import org.bytemechanics.logger.mocks.NoSuchMethodLoggerAdapter;
import org.bytemechanics.logger.mocks.SecurityLoggerAdapter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 * @author afarre
 */
public class LoggerReflectionUtilsTest {
	
	@Tested
	@Mocked
	LoggerReflectionUtils loggerReflectionUtils;
	
	@BeforeAll
	public static void setup() throws IOException{
		System.out.println(">>>>> LoggerReflectionUtilsTest >>>> setup");
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

	@Test
	@DisplayName("Is API Present presence found")
	public void testExistAPI(@Mocked LoggerAPIProvider _loggerAPI) throws ClassNotFoundException{

		new Expectations() {{
			_loggerAPI.getDetectionClass(); result=String.class; times = 1;
	    }};
		Assertions.assertTrue(loggerReflectionUtils.existAPI(_loggerAPI));
	}
	@Test
	@DisplayName("Is API Present presence not found")
	public void testIsAPIPresent_notFound(@Mocked LoggerAPIProvider _loggerAPI) throws ClassNotFoundException{

		new Expectations() {{
			_loggerAPI.getDetectionClass(); result=new ClassNotFoundException("class-not-found"); times = 1;
	    }};
		Assertions.assertFalse(loggerReflectionUtils.existAPI(_loggerAPI));
	}
	@Test
	@DisplayName("Is API Present raises linkage error")
	public void testIsAPIPresent_linkage(@Mocked LoggerAPIProvider _loggerAPI) throws ClassNotFoundException{

		new Expectations() {{
			_loggerAPI.getDetectionClass(); result=new LinkageError("linkage.error"); times = 1;
	    }};
		Assertions.assertFalse(loggerReflectionUtils.existAPI(_loggerAPI));
	}

	@Test
	@DisplayName("Get API Constructor class found")
	@SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
	public void testGetAPIConstructor_found(@Mocked LoggerAPIProvider _loggerAPI) throws ClassNotFoundException, NoSuchMethodException{

		new Expectations() {{
			_loggerAPI.getImplementationClass(); result=LoggerJSRLoggingImpl.class; times = 1;
	    }};
		Assertions.assertEquals(LoggerJSRLoggingImpl.class.getConstructor(String.class),loggerReflectionUtils.getAPIConstructor(_loggerAPI));
	}
	@Test
	@DisplayName("Get API Constructor class not found")
	public void testGetAPIConstructor_classNotFound(@Mocked LoggerAPIProvider _loggerAPI) throws ClassNotFoundException, NoSuchMethodException{

		new Expectations() {{
			_loggerAPI.getImplementationClass(); result=new ClassNotFoundException("implementation.not.found"); times = 1;
	    }};
		Assertions.assertNull(loggerReflectionUtils.getAPIConstructor(_loggerAPI));
	}
	@Test
	@DisplayName("Get API Constructor class linkage error")
	public void testGetAPIConstructor_linkage(@Mocked LoggerAPIProvider _loggerAPI) throws ClassNotFoundException, NoSuchMethodException{

		new Expectations() {{
			_loggerAPI.getImplementationClass(); result=new LinkageError("implementation.linkage"); times = 1;
	    }};
		Assertions.assertNull(loggerReflectionUtils.getAPIConstructor(_loggerAPI));
	}
	@Test
	@DisplayName("Get API Constructor class class cast")
	public void testGetAPIConstructor_classCast(@Mocked LoggerAPIProvider _loggerAPI) throws ClassNotFoundException, NoSuchMethodException{

		new Expectations() {{
			_loggerAPI.getImplementationClass(); result=String.class; times = 1;
	    }};
		Assertions.assertNull(loggerReflectionUtils.getAPIConstructor(_loggerAPI));
	}
	@Test
	@DisplayName("Get API Constructor class no such method")
	public void testGetAPIConstructor_NoSuchMethod(@Mocked LoggerAPIProvider _loggerAPI) throws ClassNotFoundException, NoSuchMethodException{

		new Expectations() {{
			_loggerAPI.getImplementationClass(); result=NoSuchMethodLoggerAdapter.class; times = 1;
	    }};
		Assertions.assertNull(loggerReflectionUtils.getAPIConstructor(_loggerAPI));
	}
	@Test
	@DisplayName("Get API Constructor class security exception")
	public void testGetAPIConstructor_SecurityException(@Mocked LoggerAPIProvider loggerAPI) throws ClassNotFoundException, NoSuchMethodException{

		new Expectations() {{
			loggerAPI.getImplementationClass(); result=SecurityLoggerAdapter.class; times = 1;
	    }};
		Assertions.assertNull(loggerReflectionUtils.getAPIConstructor(loggerAPI));
	}

	@Test
	@DisplayName("Factory builder")
	public void testBuidFactory_success(){

		Constructor<? extends LoggerAdapter> constructor;
		
		try{
			constructor=LoggerConsoleImpl.class.getConstructor(String.class); 
		}catch(IllegalArgumentException |NoSuchMethodException |SecurityException ex) {
			throw new RuntimeException(ex);
		}
		
		final Function<String,LoggerAdapter> loggerFactory=loggerReflectionUtils.buildFactory(constructor);
		Assertions.assertNotNull(loggerFactory);
		Assertions.assertNotNull(loggerFactory.apply("myLog"));
	}

	@SuppressWarnings("Convert2Lambda")
	@Test
	@DisplayName("Logger factory finder success")
	public void testFindLoggerFactory_success(){

		Constructor<? extends LoggerAdapter> constructor;
		
		try{
			constructor=LoggerJSRLoggingImpl.class.getConstructor(String.class); 
		}catch(IllegalArgumentException |NoSuchMethodException |SecurityException ex) {
			throw new RuntimeException(ex);
		}
		
		new Expectations() {{
			loggerReflectionUtils.existAPI(LoggerAPIProvider.LOG4J); result=false; times = 1;
			loggerReflectionUtils.existAPI(LoggerAPIProvider.LOG4J2E); result=false; times = 1;
			loggerReflectionUtils.existAPI(LoggerAPIProvider.LOG4J2); result=false; times = 1;
			loggerReflectionUtils.existAPI(LoggerAPIProvider.JSR); result=true; times = 1;
			loggerReflectionUtils.getAPIConstructor(LoggerAPIProvider.LOG4J); times = 0;
			loggerReflectionUtils.getAPIConstructor(LoggerAPIProvider.LOG4J2E); times = 0;
			loggerReflectionUtils.getAPIConstructor(LoggerAPIProvider.LOG4J2); times = 0;
			loggerReflectionUtils.getAPIConstructor(LoggerAPIProvider.JSR); result=constructor; times = 1;
		}};
		
		final Function<String,LoggerAdapter> loggerFactory=loggerReflectionUtils.findLoggerFactory(LoggerFactoryReflectionImpl::consoleLogger);

		Assertions.assertNotNull(loggerFactory);
		LoggerAdapter logger=loggerFactory.apply("myLog");
		Assertions.assertNotNull(logger);
		Assertions.assertTrue(loggerFactory.apply("myLog") instanceof LoggerJSRLoggingImpl);
	}

	@SuppressWarnings("Convert2Lambda")
	@Test
	@DisplayName("Logger factory finder failure")
	public void testFindLoggerFactory_failure(){

		new Expectations() {{
			loggerReflectionUtils.existAPI(LoggerAPIProvider.LOG4J); result=false; times = 1;
			loggerReflectionUtils.existAPI(LoggerAPIProvider.LOG4J2E); result=false; times = 1;
			loggerReflectionUtils.existAPI(LoggerAPIProvider.LOG4J2); result=false; times = 1;
			loggerReflectionUtils.existAPI(LoggerAPIProvider.JSR); result=false; times = 1;
			loggerReflectionUtils.getAPIConstructor(LoggerAPIProvider.LOG4J); times = 0;
			loggerReflectionUtils.getAPIConstructor(LoggerAPIProvider.LOG4J2E); times = 0;
			loggerReflectionUtils.getAPIConstructor(LoggerAPIProvider.LOG4J2); times = 0;
			loggerReflectionUtils.getAPIConstructor(LoggerAPIProvider.JSR); times = 0;
		}};
		
		final Function<String,LoggerAdapter> loggerFactory=loggerReflectionUtils.findLoggerFactory(LoggerFactoryReflectionImpl::consoleLogger);

		Assertions.assertNotNull(loggerFactory);
		LoggerAdapter logger=loggerFactory.apply("myLog");
		Assertions.assertNotNull(logger);
		Assertions.assertTrue(loggerFactory.apply("myLog") instanceof LoggerConsoleImpl);
	}
}

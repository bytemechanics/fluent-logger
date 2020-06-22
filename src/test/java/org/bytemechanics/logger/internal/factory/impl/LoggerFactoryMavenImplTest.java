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
package org.bytemechanics.logger.internal.factory.impl;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import mockit.Expectations;
import mockit.Mocked;
import org.apache.maven.plugin.logging.Log;
import org.bytemechanics.logger.FluentLogger;
import org.bytemechanics.logger.adapters.LoggerAdapter;
import org.bytemechanics.logger.adapters.impl.LoggerMavenPluginImpl;
import org.bytemechanics.logger.factory.LoggerFactoryMavenPluginImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 *
 * @author afarre
 */
public class LoggerFactoryMavenImplTest {

	@BeforeAll
	public static void setup() throws IOException{
		System.out.println(">>>>> LoggerFactoryMavenImplTest >>>> setup");
		try(InputStream inputStream = LoggerFactoryMavenImplTest.class.getResourceAsStream("/logging.properties")){
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
	
	
	@ParameterizedTest(name="getLogger({0}) from non-existent underlaying logger return a logger named as {0} of LoggerMavenPluginImpl but with null logger")
	@ValueSource(strings = {"my-name1","my-name2"})
	public void getLogger_noLogger(final String _name){

		LoggerFactoryMavenPluginImpl instance=new LoggerFactoryMavenPluginImpl();
		LoggerAdapter loggerAdapter=instance.getLogger(_name);
		Assertions.assertEquals(_name,loggerAdapter.getName());
		Assertions.assertEquals(LoggerMavenPluginImpl.class,loggerAdapter.getClass());
		Assertions.assertNull(((LoggerMavenPluginImpl)loggerAdapter).getUnderlayingLog());
	}
	@ParameterizedTest(name="getLogger({0}) wo a logger named as {0} of LoggerMavenPluginImpl")
	@ValueSource(strings = {"my-name1","my-name2"})
	public void getLogger(final String _name,final @Mocked Log _logger){

		try(LoggerFactoryMavenPluginImpl instance=new LoggerFactoryMavenPluginImpl(_logger)){
			LoggerAdapter loggerAdapter=instance.getLogger(_name);
			Assertions.assertEquals(_name,loggerAdapter.getName());
			Assertions.assertEquals(LoggerMavenPluginImpl.class,loggerAdapter.getClass());
			Assertions.assertSame(_logger,((LoggerMavenPluginImpl)loggerAdapter).getUnderlayingLog());
		}
	}

	@ParameterizedTest(name="getLogger({0}) should return a logger named as {0} of LoggerMavenPluginImpl")
	@ValueSource(strings = {"my-name1","my-name2"})
	public void getLogger_Inherited(final String _name,final @Mocked Log _logger) throws Throwable{

		try(LoggerFactoryMavenPluginImpl instance=new LoggerFactoryMavenPluginImpl(_logger)){
			LoggerAdapter loggerAdapter=instance.getLogger(_name);
			Assertions.assertEquals(_name,loggerAdapter.getName());
			Assertions.assertEquals(LoggerMavenPluginImpl.class,loggerAdapter.getClass());
			Assertions.assertSame(_logger,((LoggerMavenPluginImpl)loggerAdapter).getUnderlayingLog());
			FutureTask<Boolean> future=new FutureTask<>(() -> {
					LoggerFactoryMavenPluginImpl instance2=new LoggerFactoryMavenPluginImpl();
					LoggerAdapter loggerAdapter2=instance2.getLogger("inherited"+_name);
					Assertions.assertEquals("inherited"+_name,loggerAdapter2.getName());
					Assertions.assertEquals(LoggerMavenPluginImpl.class,loggerAdapter2.getClass());
					Assertions.assertSame(_logger,((LoggerMavenPluginImpl)loggerAdapter2).getUnderlayingLog());
					return true;
			});
			future.run();
			Assertions.assertTrue(future.get());
		}catch(ExecutionException e){
			throw e.getCause();
		}
	}
	
	@ParameterizedTest(name="getLogger({0}) should return a logger named as {0} of LoggerMavenPluginImpl")
	@ValueSource(strings = {"my-name1","my-name2"})
	public void getLogger_Inherited_withLogger(final String _name,final @Mocked Log _logger) throws Throwable{

		new Expectations() {{
			_logger.isDebugEnabled(); result=true; times=1;
			_logger.isInfoEnabled(); result=true; times=1;
			_logger.debug("my-message",null); times=1;
			_logger.info("my-inherited-message",null); times=1;
		}};

		try(LoggerFactoryMavenPluginImpl instance=new LoggerFactoryMavenPluginImpl(_logger)){
			final FluentLogger logger=FluentLogger.of("my-logger")
													.prefixed("my-")
													.debug("message");
			Assertions.assertEquals("my-logger",logger.getName());
			FutureTask<Boolean> future=new FutureTask<>(() -> {
				final FluentLogger inheritedLogger=logger.child("my-child")
															.prefixed("inherited-")
															.info("message");
				Assertions.assertEquals("my-logger.my-child",inheritedLogger.getName()); 
				return true;
			});
			future.run();
			Assertions.assertTrue(future.get());
		}catch(ExecutionException e){
			throw e.getCause();
		}
	}
}

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
package org.bytemechanics.logger;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import org.bytemechanics.fluentlogger.internal.commons.functional.LambdaUnchecker;
import org.bytemechanics.logger.internal.adapters.LoggerAdapter;
import org.bytemechanics.logger.internal.adapters.impl.LoggerConsoleImpl;
import org.bytemechanics.logger.internal.factory.LoggerFactoryAdapter;
import org.bytemechanics.logger.internal.factory.impl.LoggerFactoryReflectionImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;


/**
 * @author afarre
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FluentLoggerTest {

	static class DummieLoggerFactory implements LoggerFactoryAdapter{

		public static FluentLoggerTest test;
		
		@Override
		public LoggerAdapter getLogger(String _logger) {
			return new LoggerConsoleImpl(_logger);
		}
	}
	static class DummieLoggerFactoryNoConstructor implements LoggerFactoryAdapter{
		
		public DummieLoggerFactoryNoConstructor(final String _a){
		}

		@Override
		public LoggerAdapter getLogger(String _logger) {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}
	}
	static class DummieLoggerFactoryPrivateConstructor implements LoggerFactoryAdapter{
		
		private DummieLoggerFactoryPrivateConstructor(){
		}

		@Override
		public LoggerAdapter getLogger(String _logger) {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}
	}
	
	@BeforeAll
	public static void setup() throws IOException{
		System.out.println(">>>>> FluentLoggerTest >>>> setup");
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
		System.setProperty(FluentLogger.LOGGER_FACTORY_ADAPTER_KEY,DummieLoggerFactory.class.getName());
    }
	@AfterEach
    void afterEachTest(final TestInfo testInfo) {
		System.getProperties().remove(FluentLogger.LOGGER_FACTORY_ADAPTER_KEY);
    }
	

	@Test
	@Order(1)
	@DisplayName("Get logger factory with no LOGGER_FACTORY_ADAPTER_KEY property set returns the default instance")
	public void testGetLoggerFactory_notSet(){
		System.getProperties().remove(FluentLogger.LOGGER_FACTORY_ADAPTER_KEY);
		LoggerFactoryAdapter instance=FluentLogger.LOGGER_FACTORY;
		Assertions.assertSame(instance,FluentLogger.getLoggerFactory());
	}
	@Test
	@Order(2)
	@DisplayName("Get logger factory with LOGGER_FACTORY_ADAPTER_KEY as same class of the current instance should return the same instance")
	public void testGetLoggerFactory_sameClass(){
		LoggerFactoryAdapter instance=FluentLogger.LOGGER_FACTORY;
		System.setProperty(FluentLogger.LOGGER_FACTORY_ADAPTER_KEY,instance.getClass().getName());
		Assertions.assertSame(instance,FluentLogger.getLoggerFactory());
	}
	@Test
	@Order(3)
	@DisplayName("Get logger factory with LOGGER_FACTORY_ADAPTER_KEY with non implementing LoggerFactoryAdapter class should return same instance")
	public void testGetLoggerFactory_wrongClass(){
		System.setProperty(FluentLogger.LOGGER_FACTORY_ADAPTER_KEY,String.class.getName());
		LoggerFactoryAdapter instance=FluentLogger.LOGGER_FACTORY;
		Assertions.assertSame(instance,FluentLogger.getLoggerFactory());
	}
	@Test
	@Order(4)
	@DisplayName("Get logger factory with LOGGER_FACTORY_ADAPTER_KEY with non existent class class should return same instance")
	public void testGetLoggerFactory_notFound(){
		System.setProperty(FluentLogger.LOGGER_FACTORY_ADAPTER_KEY,DummieLoggerFactoryNoConstructor.class.getName());
		LoggerFactoryAdapter instance=FluentLogger.LOGGER_FACTORY;
		Assertions.assertSame(instance,FluentLogger.getLoggerFactory());
	}
	@Test
	@Order(5)
	@DisplayName("Get logger factory with LOGGER_FACTORY_ADAPTER_KEY with non empty constructor class class should return same instance")
	public void testGetLoggerFactory_noEmptyConstructor(){
		System.setProperty(FluentLogger.LOGGER_FACTORY_ADAPTER_KEY,DummieLoggerFactoryPrivateConstructor.class.getName());
		LoggerFactoryAdapter instance=FluentLogger.LOGGER_FACTORY;
		Assertions.assertSame(instance,FluentLogger.getLoggerFactory());
	}
	@Test
	@Order(6)
	@DisplayName("Get logger factory with LOGGER_FACTORY_ADAPTER_KEY with correct class should return distinct instance")
	public void testGetLoggerFactory_newSucc(){
		System.setProperty(FluentLogger.LOGGER_FACTORY_ADAPTER_KEY,DummieLoggerFactory.class.getName());
		LoggerFactoryAdapter instance=FluentLogger.LOGGER_FACTORY;
		Assertions.assertTrue(instance instanceof LoggerFactoryReflectionImpl);
		LoggerFactoryAdapter actual=FluentLogger.getLoggerFactory();
		Assertions.assertNotSame(instance,actual);
		Assertions.assertTrue(actual instanceof DummieLoggerFactory);
	}
	
	
	@Test
	@Order(7)
	@DisplayName("Helper of(Class:null) must raise a nullPointerException")
	@SuppressWarnings("ThrowableResultIgnored")
	public void testOf_Class_Null(){
		Assertions.assertThrows(NullPointerException.class,
								() -> FluentLogger.of((Class)null));
	}
	@Test
	@Order(7)
	@DisplayName("Helper of(Class:non-null) must return a fluentlogger instance")
	public void testOf_Class_NonNull(){
		final FluentLogger logger=FluentLogger.of(FluentLoggerTest.class);
		Assertions.assertNotNull(logger);
		Assertions.assertEquals(FluentLoggerTest.class.getName(),logger.loggerAdapter.getName());
		Assertions.assertEquals(FluentLoggerTest.class.getName(),logger.getName());
	}
	@Test
	@Order(7)
	@DisplayName("Helper of(String:null) must raise a nullPointerException")
	@SuppressWarnings("ThrowableResultIgnored")
	public void testOf_String_Null(){
		Assertions.assertThrows(NullPointerException.class,
								() -> FluentLogger.of((String)null));
	}
	@Test
	@Order(7)
	@DisplayName("Helper of(String:non-null) must return a fluentlogger instance")
	public void testOf_String_NonNull(){
		final FluentLogger logger=FluentLogger.of("my-logger");
		Assertions.assertNotNull(logger);
		Assertions.assertEquals("my-logger",logger.loggerAdapter.getName());
		Assertions.assertEquals("my-logger",logger.getName());
	}
	@Test
	@Order(7)
	@DisplayName("Helper prefixed with null must return an empty prefix")
	@SuppressWarnings("ThrowableResultIgnored")
	public void testPrefixed_Null(){
		final FluentLogger logger=FluentLogger.of("my-logger").prefixed(null);
		Assertions.assertNotNull(logger);
		Assertions.assertEquals("my-logger",logger.getName());
		Assertions.assertEquals("",logger.prefix);
	}
	@Test
	@Order(7)
	@DisplayName("Helper prefixed with not null value must return a prefixed logger")
	public void testPrefixed_NotNull(){
		final FluentLogger logger=FluentLogger.of("my-logger").prefixed("myprefix1");
		Assertions.assertNotNull(logger);
		Assertions.assertEquals("my-logger",logger.getName());
		Assertions.assertEquals("myprefix1",logger.prefix);
	}
	@Test
	@Order(7)
	@DisplayName("Prefixes should be concatenated")
	public void testPrefixed_contatenate(){
		final FluentLogger logger=FluentLogger.of("my-logger").prefixed("myprefix1").prefixed("myprefix2");
		Assertions.assertNotNull(logger);
		Assertions.assertEquals("my-logger",logger.getName());
		Assertions.assertEquals("myprefix1myprefix2",logger.prefix);
	}
	@Test
	@Order(7)
	@DisplayName("Concatenate null prefix should return the previous prefix")
	public void testPrefixed_contatenate_null(){
		final FluentLogger logger=FluentLogger.of("my-logger").prefixed("myprefix1").prefixed(null);
		Assertions.assertNotNull(logger);
		Assertions.assertEquals("my-logger",logger.getName());
		Assertions.assertEquals("myprefix1",logger.prefix);
	}
}

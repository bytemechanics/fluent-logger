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
import java.util.function.Function;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import mockit.Delegate;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import org.bytemechanics.logger.adapters.LoggerAPIProvider;
import org.bytemechanics.logger.adapters.LoggerAdapter;
import org.bytemechanics.logger.adapters.impl.LoggerConsoleImpl;
import org.bytemechanics.logger.beans.LogBean;
import org.bytemechanics.logger.factory.LoggerFactoryAdapter;
import org.bytemechanics.logger.internal.commons.functional.LambdaUnchecker;
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
    void afterEachTest() {
		System.getProperties().remove(FluentLogger.LOGGER_FACTORY_ADAPTER_KEY);
    }
	
	@Injectable 
	String prefix="";
	@Injectable
	@Mocked
	LoggerAdapter loggerAdapter;
	
	@Test
	@Order(1)
	@DisplayName("Get logger factory with no LOGGER_FACTORY_ADAPTER_KEY property set returns the default instance")
	public void testGetLoggerFactory_notSet(){
		System.getProperties().remove(FluentLogger.LOGGER_FACTORY_ADAPTER_KEY);
		LoggerFactoryAdapter instance=FluentLogger.loggerFactory;
		Assertions.assertSame(instance,FluentLogger.getLoggerFactory());
	}
	@Test
	@Order(2)
	@DisplayName("Get logger factory with LOGGER_FACTORY_ADAPTER_KEY as same class of the current instance should return the same instance")
	public void testGetLoggerFactory_sameClass(){
		LoggerFactoryAdapter instance=FluentLogger.loggerFactory;
		System.setProperty(FluentLogger.LOGGER_FACTORY_ADAPTER_KEY,instance.getClass().getName());
		Assertions.assertSame(instance,FluentLogger.getLoggerFactory());
	}
	@Test
	@Order(3)
	@DisplayName("Get logger factory with LOGGER_FACTORY_ADAPTER_KEY with non implementing LoggerFactoryAdapter class should return same instance")
	public void testGetLoggerFactory_wrongClass(){
		System.setProperty(FluentLogger.LOGGER_FACTORY_ADAPTER_KEY,String.class.getName());
		LoggerFactoryAdapter instance=FluentLogger.loggerFactory;
		Assertions.assertSame(instance,FluentLogger.getLoggerFactory());
	}
	@Test
	@Order(4)
	@DisplayName("Get logger factory with LOGGER_FACTORY_ADAPTER_KEY with non existent class class should return same instance")
	public void testGetLoggerFactory_notFound(){
		System.setProperty(FluentLogger.LOGGER_FACTORY_ADAPTER_KEY,DummieLoggerFactoryNoConstructor.class.getName());
		LoggerFactoryAdapter instance=FluentLogger.loggerFactory;
		Assertions.assertSame(instance,FluentLogger.getLoggerFactory());
	}
	@Test
	@Order(5)
	@DisplayName("Get logger factory with LOGGER_FACTORY_ADAPTER_KEY with non empty constructor class class should return same instance")
	public void testGetLoggerFactory_noEmptyConstructor(){
		System.setProperty(FluentLogger.LOGGER_FACTORY_ADAPTER_KEY,DummieLoggerFactoryPrivateConstructor.class.getName());
		LoggerFactoryAdapter instance=FluentLogger.loggerFactory;
		Assertions.assertSame(instance,FluentLogger.getLoggerFactory());
	}
	@Test
	@Order(6)
	@DisplayName("Get logger factory with LOGGER_FACTORY_ADAPTER_KEY with correct class should return distinct instance")
	public void testGetLoggerFactory_newSucc(){
		System.setProperty(FluentLogger.LOGGER_FACTORY_ADAPTER_KEY,DummieLoggerFactory.class.getName());
		LoggerFactoryAdapter instance=FluentLogger.loggerFactory;
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
	@DisplayName("Helper of(Class:null,LoggerAPIProvider:non-null) must raise a nullPointerException")
	@SuppressWarnings("ThrowableResultIgnored")
	public void testOf_loggerAPI_Class_Null(){
		Assertions.assertThrows(NullPointerException.class,
								() -> FluentLogger.of((Class)null,LoggerAPIProvider.JSR));
	}
	@Test
	@Order(7)
	@DisplayName("Helper of(Class:non-null,LoggerAPIProvider:null) must return a fluentlogger instance")
	@SuppressWarnings("ThrowableResultIgnored")
	public void testOf_loggerAPI_null_Class_NonNull(){
		Assertions.assertThrows(NullPointerException.class,
								() -> FluentLogger.of(FluentLoggerTest.class,(LoggerAPIProvider)null));
	}
	@Test
	@Order(7)
	@DisplayName("Helper of(Class:non-null,LoggerAPIProvider:non-null) must return a fluentlogger instance")
	public void testOf_loggerAPI_Class_NonNull(){
		final FluentLogger logger=FluentLogger.of(FluentLoggerTest.class,LoggerAPIProvider.CONSOLE);
		Assertions.assertNotNull(logger);
		Assertions.assertEquals(FluentLoggerTest.class.getName(),logger.loggerAdapter.getName());
		Assertions.assertEquals(FluentLoggerTest.class.getName(),logger.getName());
		Assertions.assertEquals(LoggerConsoleImpl.class,logger.loggerAdapter.getClass());
	}
	@Test
	@Order(7)
	@DisplayName("Helper of(Class:null,Function:non-null) must raise a nullPointerException")
	@SuppressWarnings("ThrowableResultIgnored")
	public void testOf_Function_Class_Null(){
		Assertions.assertThrows(NullPointerException.class,
								() -> FluentLogger.of((Class)null,name -> new LoggerConsoleImpl(name)));
	}
	@Test
	@Order(7)
	@DisplayName("Helper of(Class:non-null,Function:null) must return a fluentlogger instance")
	@SuppressWarnings("ThrowableResultIgnored")
	public void testOf_Function_null_Class_NonNull(){
		Assertions.assertThrows(NullPointerException.class,
								() -> FluentLogger.of(FluentLoggerTest.class,(Function<String,LoggerAdapter>)null));
	}
	@Test
	@Order(7)
	@DisplayName("Helper of(Class:non-null,Function:non-null) must return a fluentlogger instance")
	public void testOf_Function_Class_NonNull(){
		final FluentLogger logger=FluentLogger.of(FluentLoggerTest.class,name -> new LoggerConsoleImpl(name));
		Assertions.assertNotNull(logger);
		Assertions.assertEquals(FluentLoggerTest.class.getName(),logger.loggerAdapter.getName());
		Assertions.assertEquals(FluentLoggerTest.class.getName(),logger.getName());
		Assertions.assertEquals(LoggerConsoleImpl.class,logger.loggerAdapter.getClass());
	}
	@Test
	@Order(7)
	@DisplayName("Helper of(String:null,LoggerAPIProvider:non-null) must raise a nullPointerException")
	@SuppressWarnings("ThrowableResultIgnored")
	public void testOf_loggerAPI_String_Null(){
		Assertions.assertThrows(NullPointerException.class,
								() -> FluentLogger.of((String)null,LoggerAPIProvider.JSR));
	}
	@Test
	@Order(7)
	@DisplayName("Helper of(String:non-null,LoggerAPIProvider:null) must raise a nullPointerException")
	@SuppressWarnings("ThrowableResultIgnored")
	public void testOf_loggerAPI_null_String_NonNull(){
		Assertions.assertThrows(NullPointerException.class,
								() -> FluentLogger.of("my-logger",(LoggerAPIProvider)null));
	}
	@Test
	@Order(7)
	@DisplayName("Helper of(String:non-null,LoggerAPIProvider:non-null) must return a fluentlogger instance")
	public void testOf_loggerAPI_String_NonNull(){
		final FluentLogger logger=FluentLogger.of("my-logger",LoggerAPIProvider.CONSOLE);
		Assertions.assertNotNull(logger);
		Assertions.assertEquals("my-logger",logger.loggerAdapter.getName());
		Assertions.assertEquals("my-logger",logger.getName());
		Assertions.assertEquals(LoggerConsoleImpl.class,logger.loggerAdapter.getClass());
	}
	@Test
	@Order(7)
	@DisplayName("Helper of(String:null,Function:non-null) must raise a nullPointerException")
	@SuppressWarnings("ThrowableResultIgnored")
	public void testOf_Function_String_Null(){
		Assertions.assertThrows(NullPointerException.class,
								() -> FluentLogger.of((String)null,name -> new LoggerConsoleImpl(name)));
	}
	@Test
	@Order(7)
	@DisplayName("Helper of(String:non-null,Function:null) must raise a nullPointerException")
	@SuppressWarnings("ThrowableResultIgnored")
	public void testOf_Function_null_String_NonNull(){
		Assertions.assertThrows(NullPointerException.class,
								() -> FluentLogger.of("my-logger",(Function<String,LoggerAdapter>)null));
	}
	@Test
	@Order(7)
	@DisplayName("Helper of(String:non-null,Function:non-null) must return a fluentlogger instance")
	public void testOf_Function_String_NonNull(){
		final FluentLogger logger=FluentLogger.of("my-logger",name -> new LoggerConsoleImpl(name));
		Assertions.assertNotNull(logger);
		Assertions.assertEquals("my-logger",logger.loggerAdapter.getName());
		Assertions.assertEquals("my-logger",logger.getName());
		Assertions.assertEquals(LoggerConsoleImpl.class,logger.loggerAdapter.getClass());
	}
	
	@Test
	@Order(7)
	@DisplayName("Helper child(String:null) must raise a nullPointerException")
	@SuppressWarnings("ThrowableResultIgnored")
	public void testChild_String_Null(){
		final FluentLogger parent=FluentLogger.of("my-logger");
		Assertions.assertThrows(NullPointerException.class,
								() -> parent.child(null));
	}
	@Test
	@Order(7)
	@DisplayName("Helper child(String:non-null) must return a fluentlogger instance")
	public void testChild_String_NonNull(){
		final FluentLogger parent=FluentLogger.of("my-logger")
												.prefixed("my-prefix{}")
												.with("arg1",2);
		final FluentLogger child=parent.child("my-child")
												.prefixed("my-prefix2{}")
												.with(2.2d);
		Assertions.assertNotNull(child);
		Assertions.assertEquals("my-logger.my-child",child.loggerAdapter.getName());
		Assertions.assertEquals("my-logger.my-child",child.getName());
		Assertions.assertEquals("my-prefix{}my-prefix2{}",child.prefix);
		Assertions.assertArrayEquals(new Object[]{"arg1",2,2.2d},child.args);
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
	
	@Test
	@Order(8)
	@DisplayName("Replacing multiple args should create a concatenated array")
	public void testWith_contatenate_null(){
		final FluentLogger logger=FluentLogger.of("my-logger").with("myparg1").with(null,"myparg2").with(2,3,5);
		Assertions.assertNotNull(logger);
		Assertions.assertEquals("my-logger",logger.getName());
		Assertions.assertArrayEquals(new Object[]{"myparg1",null,"myparg2",2,3,5},logger.args);
	}

	
	@Test
	@Order(8)
	@DisplayName("Log lobBean should be cascaded to loggerAdapter.log(logBean)")
	public void testLog_LogBean(@Tested FluentLogger _logger){
		
		final String message="my-message({},{},{},{})";
		final Object[] messageArguments=new Object[]{"myparg1",null,"myparg2",2,3,5};
		
		final LogBean logBean=LogBean.of(Level.TRACE)
										.message(message).args(messageArguments);

		new Expectations(){{
			loggerAdapter.isEnabled(logBean); result=true; times=1;
			loggerAdapter.log(logBean); times=1;
		}};
		Assertions.assertEquals(_logger,_logger.log(logBean));
	}

	@Test
	@Order(8)
	@DisplayName("Log message with args should be accumulated to and call to loggerAdapter.log(logBean) with the prefixes before")
	@SuppressWarnings("Convert2Lambda")
	public void testLog_Message(@Tested FluentLogger _logger){
		
		final String message="my-message({},{},{},{})";
		final Object[] messageArguments=new Object[]{"myparg1",null,"myparg2",2,3,5};
		
		final LogBean logBean=LogBean.of(Level.TRACE)
										.message(message).args(messageArguments);

		new Expectations(){{
			loggerAdapter.isEnabled(Level.TRACE); result=true; times=1;
			loggerAdapter.log((LogBean)any);
			result = new Delegate() {
				void log(LogBean _logbean) {
					Assertions.assertEquals(logBean.getLevel(),_logbean.getLevel());
					Assertions.assertEquals(logBean.getMessage().get(),_logbean.getMessage().get());
				}
			 };
			times=1;
		}};
		Assertions.assertEquals(_logger,_logger.log(Level.TRACE, message, messageArguments));
	}

	@Test
	@Order(9)
	@DisplayName("Finest with throwable should log an empty message with an argument with an exception")
	@SuppressWarnings("Convert2Lambda")
	public void testFinest_throwable(@Tested FluentLogger _logger){
		
		final Throwable e=new Exception("my-error");

		new Expectations(){{
			loggerAdapter.isEnabled(Level.FINEST); result=true; times=1;
			loggerAdapter.log((LogBean)any);
			result = new Delegate() {
				void log(LogBean _logbean) {
					Assertions.assertEquals(Level.FINEST,_logbean.getLevel());
					Assertions.assertEquals("",_logbean.getMessage().get());
					Assertions.assertTrue(_logbean.getThrowable().isPresent());
					Assertions.assertEquals(e,_logbean.getThrowable().get());
				}
			 };
			times=1;
		}};
		Assertions.assertEquals(_logger,_logger.finest(e));
	}	
	@Test
	@Order(9)
	@DisplayName("Finest with message and arguments should log the given message with the given prefix and arguments with an exception if present into arguments")
	@SuppressWarnings("Convert2Lambda")
	public void testFinest_message(@Tested FluentLogger _logger){
		final FluentLogger logger=_logger.prefixed("my-prefix[{}-{}]::").with("first","second",999);
		final Throwable e=new Exception("my-error");

		new Expectations(){{
			loggerAdapter.isEnabled(Level.FINEST); result=true; times=1;
			loggerAdapter.log((LogBean)any);
			result = new Delegate() {
				void log(LogBean _logbean) {
					Assertions.assertEquals(Level.FINEST,_logbean.getLevel());
					Assertions.assertEquals("my-prefix[first-second]::999-myMessageWithArgs",_logbean.getMessage().get());
					Assertions.assertTrue(_logbean.getThrowable().isPresent());
					Assertions.assertEquals(e,_logbean.getThrowable().get());
				}
			 };
			times=1;
		}};
		Assertions.assertEquals(logger,logger.finest("{}-{}Message{}","my","WithArgs",e));
	}

	@Test
	@Order(9)
	@DisplayName("Trace with throwable should log an empty message with an argument with an exception")
	@SuppressWarnings("Convert2Lambda")
	public void testTrace_throwable(@Tested FluentLogger _logger){
		
		final Throwable e=new Exception("my-error");

		new Expectations(){{
			loggerAdapter.isEnabled(Level.TRACE); result=true; times=1;
			loggerAdapter.log((LogBean)any);
			result = new Delegate() {
				void log(LogBean _logbean) {
					Assertions.assertEquals(Level.TRACE,_logbean.getLevel());
					Assertions.assertEquals("",_logbean.getMessage().get());
					Assertions.assertTrue(_logbean.getThrowable().isPresent());
					Assertions.assertEquals(e,_logbean.getThrowable().get());
				}
			 };
			times=1;
		}};
		Assertions.assertEquals(_logger,_logger.trace(e));
	}	
	@Test
	@Order(9)
	@DisplayName("Trace with message and arguments should log the given message with the given prefix and arguments with an exception if present into arguments")
	@SuppressWarnings("Convert2Lambda")
	public void testTrace_message(@Tested FluentLogger _logger){
		final FluentLogger logger=_logger.prefixed("my-prefix[{}-{}]::").with("first","second",999);
		final Throwable e=new Exception("my-error");

		new Expectations(){{
			loggerAdapter.isEnabled(Level.TRACE); result=true; times=1;
			loggerAdapter.log((LogBean)any);
			result = new Delegate() {
				void log(LogBean _logbean) {
					Assertions.assertEquals(Level.TRACE,_logbean.getLevel());
					Assertions.assertEquals("my-prefix[first-second]::999-myMessageWithArgs",_logbean.getMessage().get());
					Assertions.assertTrue(_logbean.getThrowable().isPresent());
					Assertions.assertEquals(e,_logbean.getThrowable().get());
				}
			 };
			times=1;
		}};
		Assertions.assertEquals(logger,logger.trace("{}-{}Message{}","my","WithArgs",e));
	}

	@Test
	@Order(9)
	@DisplayName("Debug with throwable should log an empty message with an argument with an exception")
	@SuppressWarnings("Convert2Lambda")
	public void testDebug_throwable(@Tested FluentLogger _logger){
		
		final Throwable e=new Exception("my-error");

		new Expectations(){{
			loggerAdapter.isEnabled(Level.DEBUG); result=true; times=1;
			loggerAdapter.log((LogBean)any);
			result = new Delegate() {
				void log(LogBean _logbean) {
					Assertions.assertEquals(Level.DEBUG,_logbean.getLevel());
					Assertions.assertEquals("",_logbean.getMessage().get());
					Assertions.assertTrue(_logbean.getThrowable().isPresent());
					Assertions.assertEquals(e,_logbean.getThrowable().get());
				}
			 };
			times=1;
		}};
		Assertions.assertEquals(_logger,_logger.debug(e));
	}	
	@Test
	@Order(9)
	@DisplayName("Debug with message and arguments should log the given message with the given prefix and arguments with an exception if present into arguments")
	@SuppressWarnings("Convert2Lambda")
	public void testDebug_message(@Tested FluentLogger _logger){
		final FluentLogger logger=_logger.prefixed("my-prefix[{}-{}]::").with("first","second",999);
		final Throwable e=new Exception("my-error");

		new Expectations(){{
			loggerAdapter.isEnabled(Level.DEBUG); result=true; times=1;
			loggerAdapter.log((LogBean)any);
			result = new Delegate() {
				void log(LogBean _logbean) {
					Assertions.assertEquals(Level.DEBUG,_logbean.getLevel());
					Assertions.assertEquals("my-prefix[first-second]::999-myMessageWithArgs",_logbean.getMessage().get());
					Assertions.assertTrue(_logbean.getThrowable().isPresent());
					Assertions.assertEquals(e,_logbean.getThrowable().get());
				}
			 };
			times=1;
		}};
		Assertions.assertEquals(logger,logger.debug("{}-{}Message{}","my","WithArgs",e));
	}

	@Test
	@Order(9)
	@DisplayName("Info with throwable should log an empty message with an argument with an exception")
	@SuppressWarnings("Convert2Lambda")
	public void testInfo_throwable(@Tested FluentLogger _logger){
		
		final Throwable e=new Exception("my-error");

		new Expectations(){{
			loggerAdapter.isEnabled(Level.INFO); result=true; times=1;
			loggerAdapter.log((LogBean)any);
			result = new Delegate() {
				void log(LogBean _logbean) {
					Assertions.assertEquals(Level.INFO,_logbean.getLevel());
					Assertions.assertEquals("",_logbean.getMessage().get());
					Assertions.assertTrue(_logbean.getThrowable().isPresent());
					Assertions.assertEquals(e,_logbean.getThrowable().get());
				}
			 };
			times=1;
		}};
		Assertions.assertEquals(_logger,_logger.info(e));
	}	
	@Test
	@Order(9)
	@DisplayName("Info with message and arguments should log the given message with the given prefix and arguments with an exception if present into arguments")
	@SuppressWarnings("Convert2Lambda")
	public void testInfo_message(@Tested FluentLogger _logger){
		final FluentLogger logger=_logger.prefixed("my-prefix[{}-{}]::").with("first","second",999);
		final Throwable e=new Exception("my-error");

		new Expectations(){{
			loggerAdapter.isEnabled(Level.INFO); result=true; times=1;
			loggerAdapter.log((LogBean)any);
			result = new Delegate() {
				void log(LogBean _logbean) {
					Assertions.assertEquals(Level.INFO,_logbean.getLevel());
					Assertions.assertEquals("my-prefix[first-second]::999-myMessageWithArgs",_logbean.getMessage().get());
					Assertions.assertTrue(_logbean.getThrowable().isPresent());
					Assertions.assertEquals(e,_logbean.getThrowable().get());
				}
			 };
			times=1;
		}};
		Assertions.assertEquals(logger,logger.info("{}-{}Message{}","my","WithArgs",e));
	}

	@Test
	@Order(9)
	@DisplayName("Warning with throwable should log an empty message with an argument with an exception")
	@SuppressWarnings("Convert2Lambda")
	public void testWarning_throwable(@Tested FluentLogger _logger){
		
		final Throwable e=new Exception("my-error");

		new Expectations(){{
			loggerAdapter.isEnabled(Level.WARNING); result=true; times=1;
			loggerAdapter.log((LogBean)any);
			result = new Delegate() {
				void log(LogBean _logbean) {
					Assertions.assertEquals(Level.WARNING,_logbean.getLevel());
					Assertions.assertEquals("",_logbean.getMessage().get());
					Assertions.assertTrue(_logbean.getThrowable().isPresent());
					Assertions.assertEquals(e,_logbean.getThrowable().get());
				}
			 };
			times=1;
		}};
		Assertions.assertEquals(_logger,_logger.warning(e));
	}	
	@Test
	@Order(9)
	@DisplayName("Warning with message and arguments should log the given message with the given prefix and arguments with an exception if present into arguments")
	@SuppressWarnings("Convert2Lambda")
	public void testWarning_message(@Tested FluentLogger _logger){
		final FluentLogger logger=_logger.prefixed("my-prefix[{}-{}]::").with("first","second",999);
		final Throwable e=new Exception("my-error");

		new Expectations(){{
			loggerAdapter.isEnabled(Level.WARNING); result=true; times=1;
			loggerAdapter.log((LogBean)any);
			result = new Delegate() {
				void log(LogBean _logbean) {
					Assertions.assertEquals(Level.WARNING,_logbean.getLevel());
					Assertions.assertEquals("my-prefix[first-second]::999-myMessageWithArgs",_logbean.getMessage().get());
					Assertions.assertTrue(_logbean.getThrowable().isPresent());
					Assertions.assertEquals(e,_logbean.getThrowable().get());
				}
			 };
			times=1;
		}};
		Assertions.assertEquals(logger,logger.warning("{}-{}Message{}","my","WithArgs",e));
	}

	@Test
	@Order(9)
	@DisplayName("Error with throwable should log an empty message with an argument with an exception")
	@SuppressWarnings("Convert2Lambda")
	public void testError_throwable(@Tested FluentLogger _logger){
		
		final Throwable e=new Exception("my-error");

		new Expectations(){{
			loggerAdapter.isEnabled(Level.ERROR); result=true; times=1;
			loggerAdapter.log((LogBean)any);
			result = new Delegate() {
				void log(LogBean _logbean) {
					Assertions.assertEquals(Level.ERROR,_logbean.getLevel());
					Assertions.assertEquals("",_logbean.getMessage().get());
					Assertions.assertTrue(_logbean.getThrowable().isPresent());
					Assertions.assertEquals(e,_logbean.getThrowable().get());
				}
			 };
			times=1;
		}};
		Assertions.assertEquals(_logger,_logger.error(e));
	}	
	@Test
	@Order(9)
	@DisplayName("Error with message and arguments should log the given message with the given prefix and arguments with an exception if present into arguments")
	@SuppressWarnings("Convert2Lambda")
	public void testError_message(@Tested FluentLogger _logger){
		final FluentLogger logger=_logger.prefixed("my-prefix[{}-{}]::").with("first","second",999);
		final Throwable e=new Exception("my-error");

		new Expectations(){{
			loggerAdapter.isEnabled(Level.ERROR); result=true; times=1;
			loggerAdapter.log((LogBean)any);
			result = new Delegate() {
				void log(LogBean _logbean) {
					Assertions.assertEquals(Level.ERROR,_logbean.getLevel());
					Assertions.assertEquals("my-prefix[first-second]::999-myMessageWithArgs",_logbean.getMessage().get());
					Assertions.assertTrue(_logbean.getThrowable().isPresent());
					Assertions.assertEquals(e,_logbean.getThrowable().get());
				}
			 };
			times=1;
		}};
		Assertions.assertEquals(logger,logger.error("{}-{}Message{}","my","WithArgs",e));
	}

	@Test
	@Order(9)
	@DisplayName("Critical with throwable should log an empty message with an argument with an exception")
	@SuppressWarnings("Convert2Lambda")
	public void testCritical_throwable(@Tested FluentLogger _logger){
		
		final Throwable e=new Exception("my-error");

		new Expectations(){{
			loggerAdapter.isEnabled(Level.CRITICAL); result=true; times=1;
			loggerAdapter.log((LogBean)any);
			result = new Delegate() {
				void log(LogBean _logbean) {
					Assertions.assertEquals(Level.CRITICAL,_logbean.getLevel());
					Assertions.assertEquals("",_logbean.getMessage().get());
					Assertions.assertTrue(_logbean.getThrowable().isPresent());
					Assertions.assertEquals(e,_logbean.getThrowable().get());
				}
			 };
			times=1;
		}};
		Assertions.assertEquals(_logger,_logger.critical(e));
	}	
	@Test
	@Order(9)
	@DisplayName("Critical with message and arguments should log the given message with the given prefix and arguments with an exception if present into arguments")
	@SuppressWarnings("Convert2Lambda")
	public void testCritical_message(@Tested FluentLogger _logger){
		final FluentLogger logger=_logger.prefixed("my-prefix[{}-{}]::").with("first","second",999);
		final Throwable e=new Exception("my-error");

		new Expectations(){{
			loggerAdapter.isEnabled(Level.CRITICAL); result=true; times=1;
			loggerAdapter.log((LogBean)any);
			result = new Delegate() {
				void log(LogBean _logbean) {
					Assertions.assertEquals(Level.CRITICAL,_logbean.getLevel());
					Assertions.assertEquals("my-prefix[first-second]::999-myMessageWithArgs",_logbean.getMessage().get());
					Assertions.assertTrue(_logbean.getThrowable().isPresent());
					Assertions.assertEquals(e,_logbean.getThrowable().get());
				}
			 };
			times=1;
		}};
		Assertions.assertEquals(logger,logger.critical("{}-{}Message{}","my","WithArgs",e));
	}
}

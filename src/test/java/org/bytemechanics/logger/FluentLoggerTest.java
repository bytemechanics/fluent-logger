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
import java.util.stream.Stream;
import mockit.Delegate;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import org.bytemechanics.logger.adapters.Log;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;


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
	String _name="my-loggerName";
	@Injectable 
	String _prefix="";
	@Injectable
	@Mocked
	LoggerAdapter _loggerAdapter;
	@Injectable
	@Mocked
	Function<String,LoggerAdapter> _apiLoggerSupplier;
	
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
		Assertions.assertEquals(FluentLoggerTest.class.getName(),logger.getName());
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
	public void testLog_Log(@Tested FluentLogger _logger){
		
		final String message="my-message({},{},{},{})";
		final Object[] messageArguments=new Object[]{"myparg1",null,"myparg2",2,3,5};
		
		final Log logBean=LogBean.of(Level.TRACE)
										.message(message).args(messageArguments);

		new Expectations(){{
			_loggerAdapter.isEnabled(logBean); result=true; times=1;
			_loggerAdapter.log(logBean); times=1;
		}};
		Assertions.assertEquals(_logger,_logger.log(logBean));
	}

	@Test
	@Order(8)
	@DisplayName("Log message with args should be accumulated to and call to loggerAdapter.log(logBean) with the prefixes before")
	@SuppressWarnings("Convert2Lambda")
	public void testLog_Message(){
		
		final FluentLogger logger=new FluentLogger(name -> _loggerAdapter,"myName","");
		final String message="my-message({},{},{},{})";
		final Object[] messageArguments=new Object[]{"myparg1",null,"myparg2",2,3,5};
		
		final LogBean logBean=LogBean.of(Level.TRACE)
										.message(message).args(messageArguments);

		new Expectations(){{
			_loggerAdapter.isEnabled(Level.TRACE); result=true; times=1;
			_loggerAdapter.log((LogBean)any);
			result = new Delegate() {
				void log(LogBean _logbean) {
					Assertions.assertEquals(logBean.getLevel(),_logbean.getLevel());
					Assertions.assertEquals(logBean.getMessage().get(),_logbean.getMessage().get());
				}
			 };
			times=1;
		}};
		Assertions.assertEquals(logger,logger.log(Level.TRACE, message, messageArguments));
	}
	
	@Test
	@Order(9)
	@DisplayName("Finest with throwable should log an empty message with an argument with an exception")
	@SuppressWarnings("Convert2Lambda")
	public void testFinest_throwable(){
		
		final FluentLogger logger=new FluentLogger(name -> _loggerAdapter,"myName","");
		final Throwable e=new Exception("my-error");

		new Expectations(){{
			_loggerAdapter.isEnabled(Level.FINEST); result=true; times=1;
			_loggerAdapter.log((LogBean)any);
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
		Assertions.assertEquals(logger,logger.finest(e));
	}	
	@Test
	@Order(9)
	@DisplayName("Finest with message and arguments should log the given message with the given prefix and arguments with an exception if present into arguments")
	@SuppressWarnings("Convert2Lambda")
	public void testFinest_message(){
		
		final FluentLogger logger=new FluentLogger(name -> _loggerAdapter,"my-name","my-prefix[{}-{}]::","first","second",999);
		final Throwable e=new Exception("my-error");

		new Expectations(){{
			_loggerAdapter.isEnabled(Level.FINEST); result=true; times=1;
			_loggerAdapter.log((LogBean)any);
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
	public void testTrace_throwable(){
		
		final FluentLogger logger=new FluentLogger(name -> _loggerAdapter,"my-name","");
		final Throwable e=new Exception("my-error");

		new Expectations(){{
			_loggerAdapter.isEnabled(Level.TRACE); result=true; times=1;
			_loggerAdapter.log((LogBean)any);
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
		Assertions.assertEquals(logger,logger.trace(e));
	}	
	@Test
	@Order(9)
	@DisplayName("Trace with message and arguments should log the given message with the given prefix and arguments with an exception if present into arguments")
	@SuppressWarnings("Convert2Lambda")
	public void testTrace_message(@Tested FluentLogger _logger){
		
		final FluentLogger logger=new FluentLogger(name -> _loggerAdapter,"my-name","my-prefix[{}-{}]::","first","second",999);
		final Throwable e=new Exception("my-error");

		new Expectations(){{
			_loggerAdapter.isEnabled(Level.TRACE); result=true; times=1;
			_loggerAdapter.log((LogBean)any);
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
			_loggerAdapter.isEnabled(Level.DEBUG); result=true; times=1;
			_loggerAdapter.log((LogBean)any);
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
	public void testDebug_message(){
		
		final FluentLogger logger=new FluentLogger(name -> _loggerAdapter,"my-name","my-prefix[{}-{}]::","first","second",999);
		final Throwable e=new Exception("my-error");

		new Expectations(){{
			_loggerAdapter.isEnabled(Level.DEBUG); result=true; times=1;
			_loggerAdapter.log((LogBean)any);
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
			_loggerAdapter.isEnabled(Level.INFO); result=true; times=1;
			_loggerAdapter.log((LogBean)any);
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
	public void testInfo_message(){
		
		final FluentLogger logger=new FluentLogger(name -> _loggerAdapter,"my-name","my-prefix[{}-{}]::","first","second",999);
		final Throwable e=new Exception("my-error");

		new Expectations(){{
			_loggerAdapter.isEnabled(Level.INFO); result=true; times=1;
			_loggerAdapter.log((LogBean)any);
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
			_loggerAdapter.isEnabled(Level.WARNING); result=true; times=1;
			_loggerAdapter.log((LogBean)any);
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
	public void testWarning_message(){

		final FluentLogger logger=new FluentLogger(name -> _loggerAdapter,"my-name","my-prefix[{}-{}]::","first","second",999);
		final Throwable e=new Exception("my-error");

		new Expectations(){{
			_loggerAdapter.isEnabled(Level.WARNING); result=true; times=1;
			_loggerAdapter.log((LogBean)any);
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
			_loggerAdapter.isEnabled(Level.ERROR); result=true; times=1;
			_loggerAdapter.log((LogBean)any);
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
	public void testError_message(){

		final FluentLogger logger=new FluentLogger(name -> _loggerAdapter,"my-name","my-prefix[{}-{}]::","first","second",999);
		final Throwable e=new Exception("my-error");

		new Expectations(){{
			_loggerAdapter.isEnabled(Level.ERROR); result=true; times=1;
			_loggerAdapter.log((LogBean)any);
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
			_loggerAdapter.isEnabled(Level.CRITICAL); result=true; times=1;
			_loggerAdapter.log((LogBean)any);
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
	public void testCritical_message(){
		
		final FluentLogger logger=new FluentLogger(name -> _loggerAdapter,"my-name","my-prefix[{}-{}]::","first","second",999);
		final Throwable e=new Exception("my-error");

		new Expectations(){{
			_loggerAdapter.isEnabled(Level.CRITICAL); result=true; times=1;
			_loggerAdapter.log((LogBean)any);
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

	static Stream<Arguments> enabledDatapack(){
		return Stream.of(	Arguments.of(Level.FINEST,true),
							Arguments.of(Level.FINEST,false),
							Arguments.of(Level.TRACE,true),
							Arguments.of(Level.TRACE,false),
							Arguments.of(Level.DEBUG,true),
							Arguments.of(Level.DEBUG,false),
							Arguments.of(Level.INFO,true),
							Arguments.of(Level.INFO,false),
							Arguments.of(Level.WARNING,true),
							Arguments.of(Level.WARNING,false),
							Arguments.of(Level.DEBUG,true),
							Arguments.of(Level.DEBUG,false),
							Arguments.of(Level.CRITICAL,true),
							Arguments.of(Level.CRITICAL,false)
						);
	}
	@Order(10)
	@ParameterizedTest(name="Helper isEnabled({0}) must return a {1}")
	@MethodSource("enabledDatapack")
	public void testIsEnabled(final Level _level,final boolean _expected,@Tested FluentLogger _logger){

		new Expectations() {{
			_loggerAdapter.isEnabled(_level); result=_expected; times=1;
		}};

		Assertions.assertEquals(_expected,_logger.isEnabled(_level));
	}

	@Test
	@Order(11)
	@DisplayName("Call isFinestEnabled when finest is enabled should return true")
	public void testIsFinestEnabled(@Tested FluentLogger _logger){

		new Expectations() {{
			_loggerAdapter.isEnabled(Level.FINEST); result=true; times=1;
		}};

		Assertions.assertEquals(true,_logger.isFinestEnabled());
	}
	@Test
	@Order(11)
	@DisplayName("Call isFinestEnabled when finest is not enabled should return false")
	public void testIsFinestEnabled_false(@Tested FluentLogger _logger){

		new Expectations() {{
			_loggerAdapter.isEnabled(Level.FINEST); result=false; times=1;
		}};

		Assertions.assertEquals(false,_logger.isFinestEnabled());
	}
	@Test
	@Order(11)
	@DisplayName("Call isTraceEnabled when Trace is enabled should return true")
	public void testIsTraceEnabled(@Tested FluentLogger _logger){

		new Expectations() {{
			_loggerAdapter.isEnabled(Level.TRACE); result=true; times=1;
		}};

		Assertions.assertEquals(true,_logger.isTraceEnabled());
	}
	@Test
	@Order(11)
	@DisplayName("Call isTraceEnabled when Trace is not enabled should return false")
	public void testIsTraceEnabled_false(@Tested FluentLogger _logger){

		new Expectations() {{
			_loggerAdapter.isEnabled(Level.TRACE); result=false; times=1;
		}};

		Assertions.assertEquals(false,_logger.isTraceEnabled());
	}
	@Test
	@Order(11)
	@DisplayName("Call isDebugEnabled when Debug is enabled should return true")
	public void testIsDebugEnabled(@Tested FluentLogger _logger){

		new Expectations() {{
			_loggerAdapter.isEnabled(Level.DEBUG); result=true; times=1;
		}};

		Assertions.assertEquals(true,_logger.isDebugEnabled());
	}
	@Test
	@Order(11)
	@DisplayName("Call isDebugEnabled when Debug is not enabled should return false")
	public void testIsDebugEnabled_false(@Tested FluentLogger _logger){

		new Expectations() {{
			_loggerAdapter.isEnabled(Level.DEBUG); result=false; times=1;
		}};

		Assertions.assertEquals(false,_logger.isDebugEnabled());
	}
	@Test
	@Order(11)
	@DisplayName("Call isInfoEnabled when Info is enabled should return true")
	public void testIsInfoEnabled(@Tested FluentLogger _logger){

		new Expectations() {{
			_loggerAdapter.isEnabled(Level.INFO); result=true; times=1;
		}};

		Assertions.assertEquals(true,_logger.isInfoEnabled());
	}
	@Test
	@Order(11)
	@DisplayName("Call isInfoEnabled when Info is not enabled should return false")
	public void testIsInfoEnabled_false(@Tested FluentLogger _logger){

		new Expectations() {{
			_loggerAdapter.isEnabled(Level.INFO); result=false; times=1;
		}};

		Assertions.assertEquals(false,_logger.isInfoEnabled());
	}
	@Test
	@Order(11)
	@DisplayName("Call isWarningEnabled when Warning is enabled should return true")
	public void testIsWarningEnabled(@Tested FluentLogger _logger){

		new Expectations() {{
			_loggerAdapter.isEnabled(Level.WARNING); result=true; times=1;
		}};

		Assertions.assertEquals(true,_logger.isWarningEnabled());
	}
	@Test
	@Order(11)
	@DisplayName("Call isWarningEnabled when Warning is not enabled should return false")
	public void testIsWarningEnabled_false(@Tested FluentLogger _logger){

		new Expectations() {{
			_loggerAdapter.isEnabled(Level.WARNING); result=false; times=1;
		}};

		Assertions.assertEquals(false,_logger.isWarningEnabled());
	}
	@Test
	@Order(11)
	@DisplayName("Call isErrorEnabled when Error is enabled should return true")
	public void testIsErrorEnabled(@Tested FluentLogger _logger){

		new Expectations() {{
			_loggerAdapter.isEnabled(Level.ERROR); result=true; times=1;
		}};

		Assertions.assertEquals(true,_logger.isErrorEnabled());
	}
	@Test
	@Order(11)
	@DisplayName("Call isErrorEnabled when Error is not enabled should return false")
	public void testIsErrorEnabled_false(@Tested FluentLogger _logger){

		new Expectations() {{
			_loggerAdapter.isEnabled(Level.ERROR); result=false; times=1;
		}};

		Assertions.assertEquals(false,_logger.isErrorEnabled());
	}
	@Test
	@Order(11)
	@DisplayName("Call isCriticalEnabled when critical is enabled should return true")
	public void testIsCriticalEnabled(@Tested FluentLogger _logger){

		new Expectations() {{
			_loggerAdapter.isEnabled(Level.CRITICAL); result=true; times=1;
		}};

		Assertions.assertEquals(true,_logger.isCriticalEnabled());
	}
	@Test
	@Order(11)
	@DisplayName("Call isCriticalEnabled when critical is not enabled should return false")
	public void testIsCriticalEnabled_false(@Tested FluentLogger _logger){

		new Expectations() {{
			_loggerAdapter.isEnabled(Level.CRITICAL); result=false; times=1;
		}};

		Assertions.assertEquals(false,_logger.isCriticalEnabled());
	}


	@Test
	@Order(20)
	@DisplayName("Log message with supplier call to loggerAdapter.log(logBean) with the prefixes before")
	@SuppressWarnings("Convert2Lambda")
	public void testLog_Supplier(@Mocked LoggerAdapter _adapter){
		
		final FluentLogger logger=new FluentLogger(name -> _adapter,"myName","my-message({},{},{},{})","myparg1",null,"myparg2",2,3,5);

		final String message="HA!";
		final Throwable cause=new NullPointerException();
		
		new Expectations(){{
			_adapter.isEnabled(Level.TRACE); result=true; times=1;
			_adapter.log((LogBean)any);
			result = new Delegate() {
				void log(Log _logbean) {
					Assertions.assertEquals(Level.TRACE,_logbean.getLevel());
					Assertions.assertEquals("my-message(myparg1,null,myparg2,2)HA!",_logbean.getMessage().get());
					Assertions.assertSame(cause,_logbean.getThrowable().get());
				}
			 };
			times=1;
		}};
		Assertions.assertEquals(logger,logger.log(Level.TRACE, ()-> message, cause));
	}
	@Test
	@Order(20)
	@DisplayName("Log message with supplier call to loggerAdapter.finest(Throable,Supplier) with the prefixes before")
	@SuppressWarnings("Convert2Lambda")
	public void testFinest_Supplier(@Mocked LoggerAdapter _adapter){
		
		final FluentLogger logger=new FluentLogger(name -> _adapter,"myName","my-message({},{},{},{})","myparg1",null,"myparg2",2,3,5);

		final String message="HA!";
		
		new Expectations(){{
			_adapter.isEnabled(Level.FINEST); result=true; times=1;
			_adapter.log((LogBean)any);
			result = new Delegate() {
				void log(Log _logbean) {
					Assertions.assertEquals(Level.FINEST,_logbean.getLevel());
					Assertions.assertEquals("my-message(myparg1,null,myparg2,2)HA!",_logbean.getMessage().get());
					Assertions.assertFalse(_logbean.getThrowable().isPresent());
				}
			 };
			times=1;
		}};
		Assertions.assertEquals(logger,logger.finest(()-> message));
	}
	@Test
	@Order(20)
	@DisplayName("Log message with supplier call to loggerAdapter.finest(Throable,Supplier) with the prefixes before")
	@SuppressWarnings("Convert2Lambda")
	public void testFinest_Throwable_Supplier(@Mocked LoggerAdapter _adapter){
		
		final FluentLogger logger=new FluentLogger(name -> _adapter,"myName","my-message({},{},{},{})","myparg1",null,"myparg2",2,3,5);

		final String message="HA!";
		final Throwable cause=new NullPointerException();
		
		new Expectations(){{
			_adapter.isEnabled(Level.FINEST); result=true; times=1;
			_adapter.log((LogBean)any);
			result = new Delegate() {
				void log(Log _logbean) {
					Assertions.assertEquals(Level.FINEST,_logbean.getLevel());
					Assertions.assertEquals("my-message(myparg1,null,myparg2,2)HA!",_logbean.getMessage().get());
					Assertions.assertSame(cause,_logbean.getThrowable().get());
				}
			 };
			times=1;
		}};
		Assertions.assertEquals(logger,logger.finest(cause,()-> message));
	}

	@Test
	@Order(20)
	@DisplayName("Log message with supplier call to loggerAdapter.trace(Throable,Supplier) with the prefixes before")
	@SuppressWarnings("Convert2Lambda")
	public void testTrace_Supplier(@Mocked LoggerAdapter _adapter){
		
		final FluentLogger logger=new FluentLogger(name -> _adapter,"myName","my-message({},{},{},{})","myparg1",null,"myparg2",2,3,5);

		final String message="HA!";
		
		new Expectations(){{
			_adapter.isEnabled(Level.TRACE); result=true; times=1;
			_adapter.log((LogBean)any);
			result = new Delegate() {
				void log(Log _logbean) {
					Assertions.assertEquals(Level.TRACE,_logbean.getLevel());
					Assertions.assertEquals("my-message(myparg1,null,myparg2,2)HA!",_logbean.getMessage().get());
					Assertions.assertFalse(_logbean.getThrowable().isPresent());
				}
			 };
			times=1;
		}};
		Assertions.assertEquals(logger,logger.trace(()-> message));
	}
	@Test
	@Order(20)
	@DisplayName("Log message with supplier call to loggerAdapter.trace(Throable,Supplier) with the prefixes before")
	@SuppressWarnings("Convert2Lambda")
	public void testTrace_Throwable_Supplier(@Mocked LoggerAdapter _adapter){
		
		final FluentLogger logger=new FluentLogger(name -> _adapter,"myName","my-message({},{},{},{})","myparg1",null,"myparg2",2,3,5);

		final String message="HA!";
		final Throwable cause=new NullPointerException();
		
		new Expectations(){{
			_adapter.isEnabled(Level.TRACE); result=true; times=1;
			_adapter.log((LogBean)any);
			result = new Delegate() {
				void log(Log _logbean) {
					Assertions.assertEquals(Level.TRACE,_logbean.getLevel());
					Assertions.assertEquals("my-message(myparg1,null,myparg2,2)HA!",_logbean.getMessage().get());
					Assertions.assertSame(cause,_logbean.getThrowable().get());
				}
			 };
			times=1;
		}};
		Assertions.assertEquals(logger,logger.trace(cause,()-> message));
	}
	
	@Test
	@Order(20)
	@DisplayName("Log message with supplier call to loggerAdapter.debug(Throable,Supplier) with the prefixes before")
	@SuppressWarnings("Convert2Lambda")
	public void testDebug_Supplier(@Mocked LoggerAdapter _adapter){
		
		final FluentLogger logger=new FluentLogger(name -> _adapter,"myName","my-message({},{},{},{})","myparg1",null,"myparg2",2,3,5);

		final String message="HA!";
		
		new Expectations(){{
			_adapter.isEnabled(Level.DEBUG); result=true; times=1;
			_adapter.log((LogBean)any);
			result = new Delegate() {
				void log(Log _logbean) {
					Assertions.assertEquals(Level.DEBUG,_logbean.getLevel());
					Assertions.assertEquals("my-message(myparg1,null,myparg2,2)HA!",_logbean.getMessage().get());
					Assertions.assertFalse(_logbean.getThrowable().isPresent());
				}
			 };
			times=1;
		}};
		Assertions.assertEquals(logger,logger.debug(()-> message));
	}
	@Test
	@Order(20)
	@DisplayName("Log message with supplier call to loggerAdapter.debug(Throable,Supplier) with the prefixes before")
	@SuppressWarnings("Convert2Lambda")
	public void testDebug_Throwable_Supplier(@Mocked LoggerAdapter _adapter){
		
		final FluentLogger logger=new FluentLogger(name -> _adapter,"myName","my-message({},{},{},{})","myparg1",null,"myparg2",2,3,5);

		final String message="HA!";
		final Throwable cause=new NullPointerException();
		
		new Expectations(){{
			_adapter.isEnabled(Level.DEBUG); result=true; times=1;
			_adapter.log((LogBean)any);
			result = new Delegate() {
				void log(Log _logbean) {
					Assertions.assertEquals(Level.DEBUG,_logbean.getLevel());
					Assertions.assertEquals("my-message(myparg1,null,myparg2,2)HA!",_logbean.getMessage().get());
					Assertions.assertSame(cause,_logbean.getThrowable().get());
				}
			 };
			times=1;
		}};
		Assertions.assertEquals(logger,logger.debug(cause,()-> message));
	}

	@Test
	@Order(20)
	@DisplayName("Log message with supplier call to loggerAdapter.info(Throable,Supplier) with the prefixes before")
	@SuppressWarnings("Convert2Lambda")
	public void testInfo_Supplier(@Mocked LoggerAdapter _adapter){
		
		final FluentLogger logger=new FluentLogger(name -> _adapter,"myName","my-message({},{},{},{})","myparg1",null,"myparg2",2,3,5);

		final String message="HA!";
		
		new Expectations(){{
			_adapter.isEnabled(Level.INFO); result=true; times=1;
			_adapter.log((LogBean)any);
			result = new Delegate() {
				void log(Log _logbean) {
					Assertions.assertEquals(Level.INFO,_logbean.getLevel());
					Assertions.assertEquals("my-message(myparg1,null,myparg2,2)HA!",_logbean.getMessage().get());
					Assertions.assertFalse(_logbean.getThrowable().isPresent());
				}
			 };
			times=1;
		}};
		Assertions.assertEquals(logger,logger.info(()-> message));
	}
	@Test
	@Order(20)
	@DisplayName("Log message with supplier call to loggerAdapter.info(Throable,Supplier) with the prefixes before")
	@SuppressWarnings("Convert2Lambda")
	public void testInfo_Throwable_Supplier(@Mocked LoggerAdapter _adapter){
		
		final FluentLogger logger=new FluentLogger(name -> _adapter,"myName","my-message({},{},{},{})","myparg1",null,"myparg2",2,3,5);

		final String message="HA!";
		final Throwable cause=new NullPointerException();
		
		new Expectations(){{
			_adapter.isEnabled(Level.INFO); result=true; times=1;
			_adapter.log((LogBean)any);
			result = new Delegate() {
				void log(Log _logbean) {
					Assertions.assertEquals(Level.INFO,_logbean.getLevel());
					Assertions.assertEquals("my-message(myparg1,null,myparg2,2)HA!",_logbean.getMessage().get());
					Assertions.assertSame(cause,_logbean.getThrowable().get());
				}
			 };
			times=1;
		}};
		Assertions.assertEquals(logger,logger.info(cause,()-> message));
	}

	@Test
	@Order(20)
	@DisplayName("Log message with supplier call to loggerAdapter.warning(Throable,Supplier) with the prefixes before")
	@SuppressWarnings("Convert2Lambda")
	public void testWarning_Supplier(@Mocked LoggerAdapter _adapter){
		
		final FluentLogger logger=new FluentLogger(name -> _adapter,"myName","my-message({},{},{},{})","myparg1",null,"myparg2",2,3,5);

		final String message="HA!";
		
		new Expectations(){{
			_adapter.isEnabled(Level.WARNING); result=true; times=1;
			_adapter.log((LogBean)any);
			result = new Delegate() {
				void log(Log _logbean) {
					Assertions.assertEquals(Level.WARNING,_logbean.getLevel());
					Assertions.assertEquals("my-message(myparg1,null,myparg2,2)HA!",_logbean.getMessage().get());
					Assertions.assertFalse(_logbean.getThrowable().isPresent());
				}
			 };
			times=1;
		}};
		Assertions.assertEquals(logger,logger.warning(()-> message));
	}
	@Test
	@Order(20)
	@DisplayName("Log message with supplier call to loggerAdapter.warning(Throable,Supplier) with the prefixes before")
	@SuppressWarnings("Convert2Lambda")
	public void testWarning_Throwable_Supplier(@Mocked LoggerAdapter _adapter){
		
		final FluentLogger logger=new FluentLogger(name -> _adapter,"myName","my-message({},{},{},{})","myparg1",null,"myparg2",2,3,5);

		final String message="HA!";
		final Throwable cause=new NullPointerException();
		
		new Expectations(){{
			_adapter.isEnabled(Level.WARNING); result=true; times=1;
			_adapter.log((LogBean)any);
			result = new Delegate() {
				void log(Log _logbean) {
					Assertions.assertEquals(Level.WARNING,_logbean.getLevel());
					Assertions.assertEquals("my-message(myparg1,null,myparg2,2)HA!",_logbean.getMessage().get());
					Assertions.assertSame(cause,_logbean.getThrowable().get());
				}
			 };
			times=1;
		}};
		Assertions.assertEquals(logger,logger.warning(cause,()-> message));
	}

	@Test
	@Order(20)
	@DisplayName("Log message with supplier call to loggerAdapter.error(Throable,Supplier) with the prefixes before")
	@SuppressWarnings("Convert2Lambda")
	public void testError_Supplier(@Mocked LoggerAdapter _adapter){
		
		final FluentLogger logger=new FluentLogger(name -> _adapter,"myName","my-message({},{},{},{})","myparg1",null,"myparg2",2,3,5);

		final String message="HA!";
		
		new Expectations(){{
			_adapter.isEnabled(Level.ERROR); result=true; times=1;
			_adapter.log((LogBean)any);
			result = new Delegate() {
				void log(Log _logbean) {
					Assertions.assertEquals(Level.ERROR,_logbean.getLevel());
					Assertions.assertEquals("my-message(myparg1,null,myparg2,2)HA!",_logbean.getMessage().get());
					Assertions.assertFalse(_logbean.getThrowable().isPresent());
				}
			 };
			times=1;
		}};
		Assertions.assertEquals(logger,logger.error(()-> message));
	}
	@Test
	@Order(20)
	@DisplayName("Log message with supplier call to loggerAdapter.error(Throable,Supplier) with the prefixes before")
	@SuppressWarnings("Convert2Lambda")
	public void testError_Throwable_Supplier(@Mocked LoggerAdapter _adapter){
		
		final FluentLogger logger=new FluentLogger(name -> _adapter,"myName","my-message({},{},{},{})","myparg1",null,"myparg2",2,3,5);

		final String message="HA!";
		final Throwable cause=new NullPointerException();
		
		new Expectations(){{
			_adapter.isEnabled(Level.ERROR); result=true; times=1;
			_adapter.log((LogBean)any);
			result = new Delegate() {
				void log(Log _logbean) {
					Assertions.assertEquals(Level.ERROR,_logbean.getLevel());
					Assertions.assertEquals("my-message(myparg1,null,myparg2,2)HA!",_logbean.getMessage().get());
					Assertions.assertSame(cause,_logbean.getThrowable().get());
				}
			 };
			times=1;
		}};
		Assertions.assertEquals(logger,logger.error(cause,()-> message));
	}

	@Test
	@Order(20)
	@DisplayName("Log message with supplier call to loggerAdapter.critical(Throable,Supplier) with the prefixes before")
	@SuppressWarnings("Convert2Lambda")
	public void testCritical_Supplier(@Mocked LoggerAdapter _adapter){
		
		final FluentLogger logger=new FluentLogger(name -> _adapter,"myName","my-message({},{},{},{})","myparg1",null,"myparg2",2,3,5);

		final String message="HA!";
		
		new Expectations(){{
			_adapter.isEnabled(Level.CRITICAL); result=true; times=1;
			_adapter.log((LogBean)any);
			result = new Delegate() {
				void log(Log _logbean) {
					Assertions.assertEquals(Level.CRITICAL,_logbean.getLevel());
					Assertions.assertEquals("my-message(myparg1,null,myparg2,2)HA!",_logbean.getMessage().get());
					Assertions.assertFalse(_logbean.getThrowable().isPresent());
				}
			 };
			times=1;
		}};
		Assertions.assertEquals(logger,logger.critical(()-> message));
	}
	@Test
	@Order(20)
	@DisplayName("Log message with supplier call to loggerAdapter.critical(Throable,Supplier) with the prefixes before")
	@SuppressWarnings("Convert2Lambda")
	public void testCritical_Throwable_Supplier(@Mocked LoggerAdapter _adapter){
		
		final FluentLogger logger=new FluentLogger(name -> _adapter,"myName","my-message({},{},{},{})","myparg1",null,"myparg2",2,3,5);

		final String message="HA!";
		final Throwable cause=new NullPointerException();
		
		new Expectations(){{
			_adapter.isEnabled(Level.CRITICAL); result=true; times=1;
			_adapter.log((LogBean)any);
			result = new Delegate() {
				void log(Log _logbean) {
					Assertions.assertEquals(Level.CRITICAL,_logbean.getLevel());
					Assertions.assertEquals("my-message(myparg1,null,myparg2,2)HA!",_logbean.getMessage().get());
					Assertions.assertSame(cause,_logbean.getThrowable().get());
				}
			 };
			times=1;
		}};
		Assertions.assertEquals(logger,logger.critical(cause,()-> message));
	}
}

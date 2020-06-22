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
package org.bytemechanics.logger.beans;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bytemechanics.logger.Level;
import org.bytemechanics.logger.adapters.Log;
import org.bytemechanics.logger.internal.commons.string.SimpleFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * @author afarre
 */
public class LogSupplierBeanTest {
	
	@BeforeAll
	public static void setup() throws IOException{
		System.out.println(">>>>> LogSupplierBeanTest >>>> setupSpec");
		try(InputStream inputStream = LogSupplierBean.class.getResourceAsStream("/logging.properties")){
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

	@SuppressWarnings({"ThrowableInstanceNotThrown", "ThrowableInstanceNeverThrown"})
	@ParameterizedTest(name ="LogSupplierBean Constructor from level={0}")
	@EnumSource(Level.class)
	public void testCompleteConstructor(final Level _level){
		
		final IOException throwable=new IOException("MyIOException");
		final LocalDateTime time=LocalDateTime.now();
		Log bean=new LogSupplierBean(_level,time,() -> SimpleFormat.format("myMessage {} {} {} {}", "arg1",2,"arg3"),throwable);
		Assertions.assertEquals(_level,bean.getLevel());
		Assertions.assertEquals("myMessage arg1 2 arg3 null",bean.getMessage().get());
		Assertions.assertEquals(time,bean.getTime());
		Assertions.assertEquals("org.bytemechanics.logger.beans.LogSupplierBeanTest",bean.getSource().getClassName());
		Assertions.assertEquals("testCompleteConstructor",bean.getSource().getMethodName());
		Assertions.assertEquals(throwable,bean.getThrowable().get());
	}

	@SuppressWarnings({"ThrowableInstanceNotThrown", "ThrowableInstanceNeverThrown"})
	@ParameterizedTest(name ="LogSupplierBean builder from level={0}")
	@EnumSource(Level.class)
	public void testBuilder(final Level _level){
		
		final IOException throwable=new IOException("MyIOException");
		Log bean=LogSupplierBean.of(_level,() -> SimpleFormat.format("myMessage {} {} {} {}", "arg1",2,"arg3"),throwable);
		Assertions.assertEquals(_level,bean.getLevel());
		Assertions.assertEquals("myMessage arg1 2 arg3 null",bean.getMessage().get());
		Assertions.assertEquals("org.bytemechanics.logger.beans.LogSupplierBeanTest",bean.getSource().getClassName());
		Assertions.assertEquals("testBuilder",bean.getSource().getMethodName());
		Assertions.assertEquals(throwable,bean.getThrowable().get());
	}
	
	@SuppressWarnings({"ThrowableInstanceNotThrown", "ThrowableInstanceNeverThrown", "AssertEqualsBetweenInconvertibleTypes"})
	@ParameterizedTest(name ="LogSupplierBean builder from level={0}")
	@EnumSource(Level.class)
	public void testBuilderNoStacktrace(final Level _level){
		
		Log bean=LogSupplierBean.of(_level,() -> SimpleFormat.format("myMessage {} {} {} {}", "arg1",2,"arg3"),null);
		Assertions.assertEquals(_level,bean.getLevel());
		Assertions.assertEquals("myMessage arg1 2 arg3 null",bean.getMessage().get());
		Assertions.assertEquals("org.bytemechanics.logger.beans.LogSupplierBeanTest",bean.getSource().getClassName());
		Assertions.assertEquals("testBuilderNoStacktrace",bean.getSource().getMethodName());
		Assertions.assertEquals(Optional.empty(),bean.getThrowable());
	}	
	
	@Test
	public void testGetSourceWithSkippedClasses(){
		
		Log bean=LogSupplierBean.of(Level.ERROR,() -> SimpleFormat.format("myMessage {} {} {} {}", "arg1",2,"arg3"),null);
		StackTraceElement stacktrace=bean.getSource(Stream.of(LogSupplierBeanTest.class.getName(),"myClass").collect(Collectors.toSet()));
		Assertions.assertEquals("sun.reflect.NativeMethodAccessorImpl",stacktrace.getClassName());
		Assertions.assertEquals("invoke0",stacktrace.getMethodName());
	}
	
	@Test
	public void testGetSourceWithNoMatch(){
		
		Log bean=LogSupplierBean.of(Level.ERROR,() -> SimpleFormat.format("myMessage {} {} {} {}", "arg1",2,"arg3"),null);
		StackTraceElement stacktrace=bean.getSource(Stream.of(LogSupplierBeanTest.class.getName()
																,"sun.reflect.NativeMethodAccessorImpl"
																,"java.lang.reflect.Method"
																,"sun.reflect.DelegatingMethodAccessorImpl"
																,"org.junit.platform.commons.util.ReflectionUtils"
																,"org.junit.jupiter.engine.execution.MethodInvocation"
																,"org.junit.platform.engine.support.hierarchical.NodeTestTask"
																,"org.junit.jupiter.engine.execution.InvocationInterceptorChain$ValidatingInvocation"
																,"org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine"
																,"org.junit.jupiter.engine.extension.TimeoutExtension"
																,"org.junit.platform.launcher.core.DefaultLauncher"
																,"org.apache.maven.surefire.junitplatform.JUnitPlatformProvider"
																,"org.junit.jupiter.engine.execution.ExecutableInvoker$ReflectiveInterceptorCall"
																,"org.junit.jupiter.engine.execution.ExecutableInvoker"
																,"org.junit.platform.engine.support.hierarchical.ThrowableCollector"
																,"org.junit.jupiter.engine.execution.InvocationInterceptorChain"
																,"org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor"
																,"org.junit.platform.engine.support.hierarchical.Node"
																,"java.util.ArrayList"
																,"org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutor"
																,"org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService"
																,"org.junit.jupiter.engine.execution.InvocationInterceptorChain$InterceptedInvocation"
																,"org.apache.maven.surefire.booter.ForkedBooter")
															.collect(Collectors.toSet()));
		Assertions.assertEquals("unknown",stacktrace.getClassName());
		Assertions.assertEquals("unknown",stacktrace.getMethodName());
		Assertions.assertEquals("unknown",stacktrace.getFileName());
		Assertions.assertEquals(0,stacktrace.getLineNumber());
	}

	@Test
	public void testEquals2() throws InterruptedException{
		
		final LocalDateTime time=LocalDateTime.now();
		Thread.sleep(2);
		final String pattern="prefix({},{},{},{},{}):::my-message({},{},{},{}";
		final Object[] arguments=new Object[]{null,"myparg2",2,3,5,"myparg1",null,"myparg2",2,3,5};

		LogSupplierBean logBean1=new LogSupplierBean(Level.TRACE,time,() -> SimpleFormat.format(pattern, arguments),null);
		LogSupplierBean logBean2=new LogSupplierBean(Level.TRACE,time,() -> SimpleFormat.format(pattern, arguments),null);
		
		Assertions.assertEquals(logBean1,logBean2);
	}
	
	
	@Test //TODO Fail
	public void testHashCode() throws InterruptedException{
		final LocalDateTime time=LocalDateTime.now();
		Thread.sleep(2);
		final IOException throwable=new IOException("MyIOException");
		final String pattern="prefix({},{},{},{},{}):::my-message({},{},{},{}";
		final Object[] arguments=new Object[]{null,"myparg2",2,3,5,"myparg1",null,"myparg2",2,3,5};

		LogSupplierBean logBean1=new LogSupplierBean(Level.TRACE,time,() -> SimpleFormat.format(pattern, arguments),throwable);
		LogSupplierBean logBean2=new LogSupplierBean(Level.TRACE,time,() -> SimpleFormat.format(pattern, arguments),throwable);
		Assertions.assertEquals(logBean1.hashCode(),logBean2.hashCode());

	}
}

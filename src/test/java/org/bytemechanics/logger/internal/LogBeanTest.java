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
package org.bytemechanics.logger.internal;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bytemechanics.fluentlogger.internal.commons.functional.LambdaUnchecker;
import org.bytemechanics.logger.Level;
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
public class LogBeanTest {
	
	@BeforeAll
	public static void setup() throws IOException{
		System.out.println(">>>>> LogBeanTest >>>> setupSpec");
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

	@SuppressWarnings({"ThrowableInstanceNotThrown", "ThrowableInstanceNeverThrown"})
	@ParameterizedTest(name ="LogBean builder from level={0}")
	@EnumSource(Level.class)
	public void testBuilder(final Level _level){
		
		final IOException throwable=new IOException("MyIOException");
		final LocalDateTime time=LocalDateTime.now();
		LogBean bean=LogBean.of(_level)
							.message("myMessage {} {} {} {}")
							.args("arg1",2)
							.args("arg3")
							.args(throwable)
							.time(time);
		Assertions.assertEquals(_level,bean.getLevel());
		Assertions.assertEquals("myMessage arg1 2 arg3 java.io.IOException: MyIOException",bean.getMessage().get());
		Assertions.assertEquals(time,bean.getTime());
		Assertions.assertEquals("org.bytemechanics.logger.internal.LogBeanTest",bean.getSource().getClassName());
		Assertions.assertEquals("testBuilder",bean.getSource().getMethodName());
		Assertions.assertEquals(throwable,bean.getThrowable().get());
	}
	
	@SuppressWarnings({"ThrowableInstanceNotThrown", "ThrowableInstanceNeverThrown", "AssertEqualsBetweenInconvertibleTypes"})
	@ParameterizedTest(name ="LogBean builder from level={0}")
	@EnumSource(Level.class)
	public void testBuilderNoStacktrace(final Level _level){
		
		final LocalDateTime time=LocalDateTime.now();
		LogBean bean=LogBean.of(_level)
							.message("myMessage {} {} {} {}")
							.args("arg1",2)
							.args("arg3")
							.time(time);
		Assertions.assertEquals(_level,bean.getLevel());
		Assertions.assertEquals("myMessage arg1 2 arg3 null",bean.getMessage().get());
		Assertions.assertEquals(time,bean.getTime());
		Assertions.assertEquals("org.bytemechanics.logger.internal.LogBeanTest",bean.getSource().getClassName());
		Assertions.assertEquals("testBuilderNoStacktrace",bean.getSource().getMethodName());
		Assertions.assertEquals(Optional.empty(),bean.getThrowable());
	}	
	
	@Test
	public void testGetSourceWithSkippedClasses(){
		
		final LocalDateTime time=LocalDateTime.now();
		LogBean bean=LogBean.of(Level.ERROR)
							.message("myMessage {} {} {} {}")
							.args("arg1",2)
							.args("arg3")
							.time(time);
		StackTraceElement stacktrace=bean.getSource(Stream.of(LogBeanTest.class.getName(),"myClass").collect(Collectors.toSet()));
		Assertions.assertEquals("sun.reflect.NativeMethodAccessorImpl",stacktrace.getClassName());
		Assertions.assertEquals("invoke0",stacktrace.getMethodName());
	}
	
	@Test
	public void testGetSourceWithNoMatch(){
		
		final LocalDateTime time=LocalDateTime.now();
		LogBean bean=LogBean.of(Level.ERROR)
							.message("myMessage {} {} {} {}")
							.args("arg1",2)
							.args("arg3")
							.time(time);
		StackTraceElement stacktrace=bean.getSource(Stream.of(LogBeanTest.class.getName()
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
		final String prefix="prefix({},{},{},{},{}):::";
		final Object[] prefixArguments=new Object[]{null,"myparg2",2,3,5};
		final String message="my-message({},{},{},{})";
		final Object[] messageArguments=new Object[]{"myparg1",null,"myparg2",2,3,5};

		final LogBean logBean1=LogBean.of(Level.TRACE).time(time)
										.message(prefix).args(prefixArguments)
										.message(message).args(messageArguments);
		final LogBean logBean2=LogBean.of(Level.TRACE).time(time)
										.message(prefix).args(prefixArguments)
										.message(message).args(messageArguments);
		
		Assertions.assertEquals(logBean1,logBean2);
	}
	
	@Test
	public void testEquals() throws InterruptedException{
		final LocalDateTime time=LocalDateTime.now();
		Thread.sleep(2);
		final IOException throwable=new IOException("MyIOException");
		
		LogBean bean1=LogBean.of(Level.ERROR).message("myMessage").args("arg1",2).args(throwable).args("arg3").time(time);
		LogBean bean2=LogBean.of(Level.ERROR).message("myMessage").time(time).args("arg1",2).args(throwable).args("arg3");
		Assertions.assertTrue(bean1.equals(bean2));
		
		bean2=LogBean.of(Level.ERROR).time(time).args(throwable).args("arg1",2).args("arg3").message("myMessage");
		Assertions.assertFalse(bean1.equals(bean2));

		bean2=LogBean.of(Level.DEBUG).message("myMessage").args("arg1",2).args(throwable).args("arg3").time(time);
		Assertions.assertFalse(bean1.equals(bean2));
		bean2=LogBean.of(Level.ERROR).message("myMessage2").args("arg1",2).args(throwable).args("arg3").time(time);
		Assertions.assertFalse(bean1.equals(bean2));
		bean2=LogBean.of(Level.ERROR).message("myMessage").args("arg1",2).args(throwable).time(time);
		Assertions.assertFalse(bean1.equals(bean2));
		bean2=LogBean.of(Level.ERROR).message("myMessage").args("arg1",2).args("arg3").time(time);
		Assertions.assertFalse(bean1.equals(bean2));
		bean2=LogBean.of(Level.ERROR).message("myMessage").args("arg1",2).args(throwable).args("arg3",2).time(time);
		Assertions.assertFalse(bean1.equals(bean2));
		bean2=LogBean.of(Level.ERROR).message("myMessage").args("arg1",2).args(throwable).args("arg3");
		Assertions.assertFalse(bean1.equals(bean2));
		bean2=LogBean.of(Level.ERROR).message("myMessage").args("arg1",2).args(throwable).args("arg3").time(LocalDateTime.now());
		Assertions.assertFalse(bean1.equals(bean2));
	}
	
	@Test //TODO Fail
	public void testHashCode() throws InterruptedException{
		final LocalDateTime time=LocalDateTime.now();
		Thread.sleep(2);
		final IOException throwable=new IOException("MyIOException");
		
		LogBean bean1=LogBean.of(Level.ERROR).message("myMessage").args("arg1",2).args(throwable).args("arg3").time(time);
		LogBean bean2=LogBean.of(Level.ERROR).message("myMessage").time(time).args("arg1",2).args(throwable).args("arg3");
		Assertions.assertEquals(bean1.hashCode(),bean2.hashCode());

		bean2=LogBean.of(Level.ERROR).message("myMessage").args("arg1",2).args("arg3").args(throwable).time(time);
		Assertions.assertNotEquals(bean1.hashCode(),bean2.hashCode());
		
		bean2=LogBean.of(Level.DEBUG).message("myMessage").args("arg1",2).args(throwable).args("arg3").time(time);
		Assertions.assertNotEquals(bean1.hashCode(),bean2.hashCode());
		bean2=LogBean.of(Level.TRACE).message("myMessage").args("arg1",2).args(throwable).args("arg3").time(time);
		Assertions.assertNotEquals(bean1.hashCode(),bean2.hashCode());
		bean2=LogBean.of(Level.ERROR).message("myMessage2").args("arg1",2).args(throwable).args("arg3").time(time);
		Assertions.assertNotEquals(bean1.hashCode(),bean2.hashCode());
		bean2=LogBean.of(Level.ERROR).message("myMessage").args("arg1",2).args(throwable).time(time);
		Assertions.assertNotEquals(bean1.hashCode(),bean2.hashCode());
		bean2=LogBean.of(Level.ERROR).message("myMessage").args("arg1",2).args("arg3").time(time);
		Assertions.assertNotEquals(bean1.hashCode(),bean2.hashCode());
		bean2=LogBean.of(Level.ERROR).message("myMessage").args("arg1",2).args(throwable).args("arg3",2).time(time);
		Assertions.assertNotEquals(bean1.hashCode(),bean2.hashCode());
		bean2=LogBean.of(Level.ERROR).message("myMessage").args("arg1",2).args(throwable).args("arg3");
		Assertions.assertNotEquals(bean1.hashCode(),bean2.hashCode());
		bean2=LogBean.of(Level.ERROR).message("myMessage").args("arg1",2).args(throwable).args("arg3").time(LocalDateTime.now());
		Assertions.assertNotEquals(bean1.hashCode(),bean2.hashCode());
	}
}

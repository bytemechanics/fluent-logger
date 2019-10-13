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
import java.util.logging.LogManager;
import java.util.logging.Logger;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import org.bytemechanics.fluentlogger.internal.commons.functional.LambdaUnchecker;
import org.bytemechanics.logger.tests.LoggerAdapterImpl;
import org.bytemechanics.logger.tests.ReflectionLoaderStrategyImpl;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 * @author afarre
 */
public class ReflectionLoggerStrategyTest {
	
	@Mocked
	LoggerAdapterImpl loggerAdapter;
	
	@Tested
	@Injectable
	ReflectionLoaderStrategyImpl reflectionLoader;
	
	@BeforeAll
	public static void setup() throws IOException{
		System.out.println(">>>>> ReflectionLoggerStrategyTest >>>> setup");
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
	@DisplayName("Detect API presence found")
	public void testIsAPIPresent_found() throws ClassNotFoundException{

		new Expectations() {{
			reflectionLoader.getTargetClassName(); result="classname.found"; times = 1;
			reflectionLoader.forName("classname.found",Object.class); result=String.class; times = 1; 
	    }};
		Assert.assertTrue(reflectionLoader.isAPIPresent());
	}
	@Test
	@DisplayName("Detect API presence not found")
	public void testIsAPIPresent_notFound() throws ClassNotFoundException{

		new Expectations() {{
			reflectionLoader.getTargetClassName(); result="classname.not.found"; times = 1;
			reflectionLoader.forName("classname.not.found",Object.class); result=new ClassNotFoundException("classname.not.found"); times = 1; 
	    }};
		Assert.assertFalse(reflectionLoader.isAPIPresent());
	}
	@Test
	@DisplayName("Detect API raises linkage error")
	public void testIsAPIPresent_linkage() throws ClassNotFoundException{

		new Expectations() {{
			reflectionLoader.getTargetClassName(); result="linkage.error"; times = 1;
			reflectionLoader.forName("linkage.error",Object.class); result=new LinkageError("linkage.error"); times = 1; 
	    }};
		Assert.assertFalse(reflectionLoader.isAPIPresent());
	}

	@Test
	@DisplayName("Get implementation class found")
	@SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
	public void testGetImplementationClass_found() throws ClassNotFoundException{

		new Expectations() {{
			reflectionLoader.getImplementationClassName(); result=LoggerAdapterImpl.class.getName(); times = 1;
			reflectionLoader.forName(LoggerAdapterImpl.class.getName(),LoggerAdapter.class); result=LoggerAdapterImpl.class; times = 1; 
	    }};
		Assert.assertEquals(LoggerAdapterImpl.class,reflectionLoader.getImplementationClass());
	}
	@Test
	@DisplayName("Get implementation class not found")
	@SuppressWarnings("ThrowableResultIgnored")
	public void testGetImplementationClass_notFound() throws ClassNotFoundException{

		new Expectations() {{
			reflectionLoader.getImplementationClassName(); result="classname.not.found"; times = 1;
			reflectionLoader.forName("classname.not.found",LoggerAdapter.class); result=new ClassNotFoundException("classname.not.found"); times = 1; 
	    }};
		Assertions.assertThrows(ClassNotFoundException.class
								,() -> reflectionLoader.getImplementationClass()
								,"should fail with ClassNotFound");
	}
	@Test
	@DisplayName("Get implementation class linkage error")
	@SuppressWarnings("ThrowableResultIgnored")
	public void testGetImplementationClass_linkage() throws ClassNotFoundException{

		new Expectations() {{
			reflectionLoader.getImplementationClassName(); result="linkage.error"; times = 1;
			reflectionLoader.forName("linkage.error",LoggerAdapter.class); result=new LinkageError("linkage.error"); times = 1; 
	    }};
		Assertions.assertThrows(LinkageError.class
								,() -> reflectionLoader.getImplementationClass()
								,"should fail with LinkageError");
	}
}

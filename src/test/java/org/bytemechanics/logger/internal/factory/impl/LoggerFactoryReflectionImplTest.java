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
import java.util.function.Function;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import org.bytemechanics.fluentlogger.internal.commons.functional.LambdaUnchecker;
import org.bytemechanics.logger.internal.adapters.LoggerAdapter;
import org.bytemechanics.logger.internal.adapters.impl.LoggerConsoleImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 *
 * @author afarre
 */
public class LoggerFactoryReflectionImplTest {

	static class Factory implements Function<String,LoggerAdapter>{
		@Override
		public LoggerAdapter apply(String t) {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}
	}
	
	@Injectable
	@Mocked
	Factory loggerFactory;
	
	@Tested
	@Mocked
	LoggerFactoryReflectionImpl factory;
			
	@BeforeAll
	public static void setup() throws IOException{
		System.out.println(">>>>> LoggerFactoryReflectionImplTest >>>> setup");
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
	@DisplayName("Retrieve logger test")
	public void testGetLogger(){

		LoggerAdapter consoleLogger=new LoggerConsoleImpl("my.logger");
		
		new Expectations() {{
			loggerFactory.apply("my.logger"); result=consoleLogger; times = 1;
	    }};
		Assertions.assertEquals(consoleLogger,factory.getLogger("my.logger"));
	}
}

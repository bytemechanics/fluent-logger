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
package org.bytemechanics.logger.internal.adapters;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Stream;
import mockit.Injectable;
import mockit.Tested;
import org.bytemechanics.logger.Level;
import org.bytemechanics.logger.internal.LogBean;
import org.bytemechanics.logger.internal.adapters.impl.LoggerConsoleImpl;
import org.bytemechanics.logger.internal.commons.functional.LambdaUnchecker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * @author afarre
 */
public class LoggerAdapterTest {
	
	@BeforeAll
	public static void setup() throws IOException{
		System.out.println(">>>>> LoggerAdapterTest >>>> setupSpec");
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
	
	static Stream<Arguments> logBeanEnabledDatapack() {
	    return Stream.of(
			Arguments.of(LogBean.of(Level.FINEST),false),		
			Arguments.of(LogBean.of(Level.TRACE),false),		
			Arguments.of(LogBean.of(Level.DEBUG),false),		
			Arguments.of(LogBean.of(Level.INFO),true),		
			Arguments.of(LogBean.of(Level.WARNING),true),		
			Arguments.of(LogBean.of(Level.ERROR),true),		
			Arguments.of(LogBean.of(Level.CRITICAL),true)	
		);
	}
	@ParameterizedTest(name ="Log level={0} is enabled should answer={1}")
	@MethodSource("logBeanEnabledDatapack")
	public void testisEnabled(final LogBean _lobBean,final boolean _enabled){
		Assertions.assertEquals(_enabled,logger.isEnabled(_lobBean));
	}
}

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

import java.util.Set;
import java.util.function.Supplier;
import org.bytemechanics.logger.Level;

/**
 * Logger adapter
 * @author afarre
 */
public interface LoggerAdapter {
    
    public boolean isEnabled(final Level _level);
    public default boolean isEnabled(final LogBean _log){
		return this.isEnabled(_log.getLevel());
	}
	
    public void log(final LogBean _logBean);
}

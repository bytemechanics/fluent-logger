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
package org.bytemechanics.logger.internal.factory;

import org.bytemechanics.logger.internal.adapters.LoggerAdapter;

/**
 * Logger factory adapter
 * @author afarre
 * @since 2.0.0
 */
public interface LoggerFactoryAdapter {
    
	/**
	 * Return an instance of LoggerAdapter from the given _name
	 * @param _logger Name of the logger to instantiate
	 * @return An instance of LoggerAdapter or null
	 * @see LoggerAdapter
	 */
    public LoggerAdapter getLogger(final String _logger);
}

/*
 * Copyright (c) 2008-2019, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hazelcast.addon.security.jaas.simple;

import java.util.Properties;

import com.hazelcast.config.GroupConfig;
import com.hazelcast.security.Credentials;
import com.hazelcast.security.ICredentialsFactory;
import com.hazelcast.security.UsernamePasswordCredentials;

/**
 * SimpleCredentialsFactory is a clear text credentials factory for JAAS
 * authentication.
 * 
 * @author dpark
 *
 */
public class SimpleCredentialsFactory implements ICredentialsFactory {
	private String username;
	private String pw;

	@Override
	public void configure(GroupConfig groupConfig, Properties properties) {
		username = properties.getProperty("username");
		pw = properties.getProperty("pw");
	}

	@Override
	public Credentials newCredentials() {
		return new UsernamePasswordCredentials(username, pw);
	}

	@Override
	public void destroy() {
		username = null;
		pw = null;
	}
}

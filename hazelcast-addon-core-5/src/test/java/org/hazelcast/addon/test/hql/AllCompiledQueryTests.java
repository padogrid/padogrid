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

package org.hazelcast.addon.test.hql;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Tests a suite of all CompiledQuery tests which require only a localhost
 * cluster running on the default port (localhost:5701).
 * 
 * @author dpark
 *
 */
@RunWith(Suite.class)
@SuiteClasses({ AliasTest.class, AndOrTest.class, BetweenTest.class, EntriesTest.class, EqualityTest.class,
		InTest.class, LikeTest.class, KeysTest.class, MiscTest.class, RegexTest.class, UndefinedMapTest.class, ValuesTest.class })
public class AllCompiledQueryTests {

}

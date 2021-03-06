/*
 * Copyright 2006 The Apache Software Foundation.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openjpa.lib.util;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

/**
 * Test UUID generation.
 *
 * @author Abe White
 */
public class TestUUIDGenerator extends TestCase {

    public void testUniqueString() {
        Set seen = new HashSet();
        for (int i = 0; i < 10000; i++)
            assertTrue(seen.add(UUIDGenerator.nextString()));
    }

    public void testUniqueHex() {
        Set seen = new HashSet();
        for (int i = 0; i < 10000; i++)
            assertTrue(seen.add(UUIDGenerator.nextHex()));
    }
}

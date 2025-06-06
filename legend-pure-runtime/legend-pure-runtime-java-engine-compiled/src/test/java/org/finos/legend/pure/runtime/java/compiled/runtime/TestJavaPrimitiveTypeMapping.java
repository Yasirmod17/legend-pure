// Copyright 2020 Goldman Sachs
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.finos.legend.pure.runtime.java.compiled.runtime;

import org.eclipse.collections.api.list.ListIterable;
import org.finos.legend.pure.runtime.java.compiled.generation.JavaPurePrimitiveTypeMapping;
import org.junit.Assert;
import org.junit.Test;

public class TestJavaPrimitiveTypeMapping
{
    @Test
    public void testChunk()
    {
        doTest("hello", new String[]{"hello"});
        doTest("helloworld", new String[]{"hello", "world"});
        doTest("hello\\\\world", new String[]{"hello", "\\\\wor", "ld"}); //split with escape at start of chunk
        doTest("llo\\\\world", new String[]{"llo\\\\", "world"}); //split with escape at end of chunk
        doTest("ello\\\"world", new String[]{"ello", "\\\"worl", "d"}); //split in middle of escape
    }

    private void doTest(String value, String[] expected)
    {
        ListIterable<String> chunks = JavaPurePrimitiveTypeMapping.chunk(value, 5);

        Assert.assertArrayEquals(expected, chunks.toArray());
        Assert.assertEquals(value, chunks.makeString(""));
    }
}

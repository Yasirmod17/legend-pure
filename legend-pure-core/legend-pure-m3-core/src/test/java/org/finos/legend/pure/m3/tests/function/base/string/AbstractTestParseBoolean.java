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

package org.finos.legend.pure.m3.tests.function.base.string;

import org.finos.legend.pure.m3.tests.AbstractPureTestWithCoreCompiled;
import org.junit.After;
import org.junit.Test;

public abstract class AbstractTestParseBoolean extends AbstractPureTestWithCoreCompiled
{
    @After
    public void cleanRuntime()
    {
        runtime.delete("fromString.pure");
        runtime.compile();
    }

    @Test
    public void testBasicParse()
    {
           compileTestSource("fromString.pure",
                    "function test():Boolean[1]\n" +
                            "{\n" +
                            "   assert(true == 'true'->parseBoolean(), |'');\n" +
                            "}\n");
            this.execute("test():Boolean[1]");
    }

    @Test
    public void testEvalParse()
    {
       compileTestSource("fromString.pure",
                "function test():Boolean[1]\n" +
                        "{\n" +
                        "   assert(true == parseBoolean_String_1__Boolean_1_->eval('true'), |'');\n" +
                        "}\n");
        this.execute("test():Boolean[1]");
    }
}

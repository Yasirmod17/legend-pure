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

package org.finos.legend.pure.runtime.java.interpreted.function.base.asserts;

import org.finos.legend.pure.m3.execution.FunctionExecution;
import org.finos.legend.pure.m3.tests.function.base.PureExpressionTest;
import org.finos.legend.pure.runtime.java.interpreted.FunctionExecutionInterpreted;
import org.junit.Test;

public class TestAssertContains extends PureExpressionTest
{
    @Test
    public void testFailure()
    {
        assertExpressionRaisesPureException("[1, 2, 5, 2, 'a', true, %2014-02-01, 'c'] does not contain false", 3, 9, "assertContains([1, 2, 5, 2, 'a', true, %2014-02-01, 'c'], false)",
                "function meta::pure::functions::asserts::assertContains(collection:Any[*], value:Any[1]):Boolean[1]\n" +
                "{\n" +
                "    assertContains($collection, $value, | format('%s does not contain %r', [$collection->map(v | $v->toRepresentation())->joinStrings('[', ', ', ']'), $value]));\n" +
                "}\n" +
                "\n" +
                "function meta::pure::functions::asserts::assertContains(collection:Any[*], value:Any[1], message:String[1]):Boolean[1]\n" +
                "{\n" +
                "    assert($collection->contains($value), $message);\n" +
                "}\n" +
                "\n" +
                "function meta::pure::functions::asserts::assertContains(collection:Any[*], value:Any[1], formatString:String[1], formatArgs:Any[*]):Boolean[1]\n" +
                "{\n" +
                "    assert($collection->contains($value), $formatString, $formatArgs);\n" +
                "}\n" +
                "\n" +
                "function meta::pure::functions::asserts::assertContains(collection:Any[*], value:Any[1], message:Function<{->String[1]}>[1]):Boolean[1]\n" +
                "{\n" +
                "    assert($collection->contains($value), $message);\n" +
                "}\n" +
                "function meta::pure::functions::asserts::assert(condition:Boolean[1], formatString:String[1], formatArgs:Any[*]):Boolean[1]\n" +
                "{\n" +
                "    assert($condition, | format($formatString, $formatArgs));\n" +
                "}"
        );
    }

    @Override
    protected FunctionExecution getFunctionExecution()
    {
        return new FunctionExecutionInterpreted();
    }
}

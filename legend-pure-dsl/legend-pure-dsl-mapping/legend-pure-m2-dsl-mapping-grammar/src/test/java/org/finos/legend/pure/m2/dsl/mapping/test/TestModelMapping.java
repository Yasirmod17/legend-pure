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

package org.finos.legend.pure.m2.dsl.mapping.test;

import org.finos.legend.pure.m2.dsl.mapping.M2MappingProperties;
import org.finos.legend.pure.m3.coreinstance.meta.external.store.model.PureInstanceSetImplementation;
import org.finos.legend.pure.m3.coreinstance.meta.external.store.model.PurePropertyMapping;
import org.finos.legend.pure.m3.coreinstance.meta.pure.mapping.EnumerationMapping;
import org.finos.legend.pure.m3.coreinstance.meta.pure.mapping.Mapping;
import org.finos.legend.pure.m3.navigation.M3Properties;
import org.finos.legend.pure.m3.navigation.PackageableElement.PackageableElement;
import org.finos.legend.pure.m3.tools.test.ToFix;
import org.finos.legend.pure.m4.coreinstance.CoreInstance;
import org.finos.legend.pure.m4.coreinstance.SourceInformation;
import org.finos.legend.pure.m4.exception.PureCompilationException;
import org.finos.legend.pure.m4.serialization.grammar.antlr.PureParserException;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class TestModelMapping extends AbstractPureMappingTestWithCoreCompiled
{
    @BeforeClass
    public static void setUp()
    {
        setUpRuntime();
    }

    @After
    public void cleanRuntime()
    {
        runtime.delete("mapping.pure");
        runtime.delete("model.pure");
        runtime.delete("projection.pure");
        runtime.compile();
    }

    @Test
    public void testLiteralMapping()
    {
        runtime.createInMemorySource("model.pure",
                "Enum myEnum" +
                        "{" +
                        "   a,b" +
                        "}" +
                        "Class Firm" +
                        "{" +
                        "  legalName : String[1];" +
                        "  count : Integer[1];" +
                        "  flag : Boolean[1];" +
                        "  date : Date[1];" +
                        "  f_val :Float[1];" +
                        "  enumVal : myEnum[1];" +
                        "}");

        String source = "###Mapping\n" +
                "Mapping test::TestMapping\n" +
                "(\n" +
                "  Firm : Pure\n" +
                "  {\n" +
                "    legalName : 'name',\n" +
                "    count : 2," +
                "    flag : true," +
                "    f_val : 1.0," +
                "    date : %2005-10-10," +
                "    enumVal : myEnum.a\n" +
                "  }\n" +
                ")";
        runtime.createInMemorySource("mapping.pure", source);
        runtime.compile();
        assertSetSourceInformation(source, "Firm");
    }

    @Test
    public void testLiteralEnumErrorMapping()
    {
        runtime.createInMemorySource("model.pure",
                "Enum myEnum" +
                        "{" +
                        "   a,b" +
                        "}" +
                        "Class Firm" +
                        "{" +
                        "  enumVal : myEnum[1];" +
                        "}");

        runtime.createInMemorySource("mapping.pure",
                "###Mapping\n" +
                        "Mapping test::TestMapping\n" +
                        "(\n" +
                        "  Firm : Pure\n" +
                        "  {\n" +
                        "    enumVal : myEnum.z\n" +
                        "  }\n" +
                        ")");

        PureCompilationException e = Assert.assertThrows(PureCompilationException.class, runtime::compile);
        assertPureException(PureCompilationException.class,
                "The enum value 'z' can't be found in the enumeration myEnum",
                "mapping.pure", 6, 22, 6, 22, 6, 22, e);
    }

    @Test
    public void testCompileFailureWithMappingTests()
    {
        runtime.createInMemorySource("model.pure",
                "###Mapping\n" +
                        "Mapping my::query::TestMapping\n" +
                        "(\n" +
                        "MappingTests\n" +
                        "   [\n" +
                        "      test1\n" +
                        "      (\n" +
                        "         ~query: {x:ui::ClassA|$x.propA},\n" +
                        "         ~inputData: [],\n" +
                        "         ~assert: 'assertString'\n" +
                        "      ),\n" +
                        "      defaultTest\n" +
                        "      (\n" +
                        "         ~query: {|model::domain::Target.all()->graphFetchChecked(#{ClassNotHere{name}}#)->serialize(#{model::domain::Target{name}}#)},\n" +
                        "         ~inputData: \n" +
                        "           [" +
                        "           <Object,model::domain::Source, '{\"oneName\":\"oneName 2\",\"anotherName\":\"anotherName 16\",\"oneDate\":\"2020-02-05\",\"anotherDate\":\"2020-04-13\",\"oneNumber\":24,\"anotherNumber\":29}'>," +
                        "           <Object,SourceClass, '{\"oneName\":\"oneName 2\",\"anotherName\":\"anotherName 16\",\"oneDate\":\"2020-02-05\",\"anotherDate\":\"2020-04-13\",\"oneNumber\":24,\"anotherNumber\":29}'>" +
                        "           ]," +
                        "         ~assert: '{\"defects\":[],\"value\":{\"name\":\"oneName 2\"},\"source\":{\"defects\":[],\"value\":{\"oneName\":\"oneName 2\"},\"source\":{\"number\":1,\"record\":\"{\"oneName\":\"oneName 2\",\"anotherName\":\"anotherName 16\",\"oneDate\":\"2020-02-05\",\"anotherDate\":\"2020-04-13\",\"oneNumber\":24,\"anotherNumber\":29}\"}}}'\n" +
                        "      )\n" +
                        "   ]" +
                        ")");
        PureParserException e = Assert.assertThrows(PureParserException.class, runtime::compile);
        assertPureException(PureParserException.class,
                "Grammar Tests in Mapping currently not supported in Pure",
                "model.pure", 4, 1, 4, 1, 4, 12, e);
    }

    @Test
    public void testMappingWithSource()
    {
        runtime.createInMemorySource("model.pure",
                "Class Firm" +
                        "{" +
                        "  legalName : String[1];" +
                        "}" +
                        "Class pack::FirmSource" +
                        "{" +
                        "   name : String[1];" +
                        "}");

        String source = "###Mapping\n" +
                "Mapping test::TestMapping\n" +
                "(\n" +
                "  Firm : Pure\n" +
                "  {" +
                "    ~src pack::FirmSource\n" +
                "    legalName : $src.name\n" +
                "  }\n" +
                ")";
        runtime.createInMemorySource("mapping.pure", source);
        runtime.compile();
        assertSetSourceInformation(source, "Firm");
    }

    @Test
    public void testMappingWithSourceInt()
    {
        runtime.createInMemorySource("model.pure",
                "Class Firm" +
                        "{" +
                        "  legalName : String[1];" +
                        "  val : Integer[1];" +
                        "}" +
                        "Class AB" +
                        "{" +
                        "   vale : Integer[1];" +
                        "}");

        String source = "###Mapping\n" +
                "Mapping test::TestMapping\n" +
                "(\n" +
                "  Firm : Pure\n" +
                "  {" +
                "    ~src AB\n" +
                "    legalName : ['a','b']->map(k|$k+'Yeah!')->joinStrings(','),\n" +
                "    val : $src.vale\n" +
                "  }\n" +
                ")";
        runtime.createInMemorySource("mapping.pure", source);
        runtime.compile();
        assertSetSourceInformation(source, "Firm");
    }

    @Test
    public void testProjectionClassMapping()
    {
        runtime.createInMemorySource("model.pure",
                "Enum myEnum" +
                        "{" +
                        "   a,b" +
                        "}" +
                        "Class Firm" +
                        "{" +
                        "  legalName : String[1];" +
                        "  count : Integer[1];" +
                        "  flag : Boolean[1];" +
                        "  date : Date[1];" +
                        "  f_val :Float[1];" +
                        "  enumVal : myEnum[1];" +
                        "}");

        String source = "###Mapping\n" +
                "Mapping test::TestMapping\n" +
                "(\n" +
                "  FirmProjection : Pure\n" +
                "  {\n" +
                "    legalName : 'name',\n" +
                "    count : 2," +
                "    flag : true," +
                "    f_val : 1.0," +
                "    date : %2005-10-10," +
                "    enumVal : myEnum.a\n" +
                "  }\n" +
                ")";
        runtime.createInMemorySource("mapping.pure", source);
        runtime.createInMemorySource("projection.pure", "Class FirmProjection projects Firm\n" +
                "{\n" +
                "   *" +
                "}");
        runtime.compile();
        assertSetSourceInformation(source, "FirmProjection");
    }

    @Test
    public void testMappingWithSourceWrongProperty()
    {
        runtime.createInMemorySource("model.pure",
                "Class Firm" +
                        "{" +
                        "  legalName : String[1];" +
                        "}" +
                        "Class pack::FirmSource" +
                        "{" +
                        "   name : String[1];" +
                        "}");

        runtime.createInMemorySource("mapping.pure",
                "###Mapping\n" +
                        "Mapping test::TestMapping\n" +
                        "(\n" +
                        "  Firm : Pure\n" +
                        "  {" +
                        "    ~src pack::FirmSource\n" +
                        "    legalName : $src.nameX\n" +
                        "  }\n" +
                        ")");

        PureCompilationException e = Assert.assertThrows(PureCompilationException.class, runtime::compile);
        assertPureException(PureCompilationException.class,
                "Can't find the property 'nameX' in the class pack::FirmSource",
                "mapping.pure", 6, 22, 6, 22, 6, 26, e);
    }

    @Test
    public void testMappingWithSourceError()
    {
        runtime.createInMemorySource("model.pure",
                "Class Firm" +
                        "{" +
                        "  legalName : String[1];" +
                        "}");

        runtime.createInMemorySource("mapping.pure",
                "###Mapping\n" +
                        "Mapping test::TestMapping\n" +
                        "(\n" +
                        "  Firm : Pure\n" +
                        "  {" +
                        "    ~src pack::FirmSource\n" +
                        "    legalName : 'name'\n" +
                        "  }\n" +
                        ")");

        PureCompilationException e = Assert.assertThrows(PureCompilationException.class, runtime::compile);
        assertPureException(PureCompilationException.class,
                "pack::FirmSource has not been defined!",
                "mapping.pure", 5, 10, 5, 10, 5, 13, e);
    }

    @Test
    public void testMappingWithTypeMismatch()
    {
        runtime.createInMemorySource("model.pure",
                "Class Firm" +
                        "{" +
                        "  legalName : String[1];" +
                        "}");

        runtime.createInMemorySource("mapping.pure",
                "###Mapping\n" +
                        "Mapping test::TestMapping\n" +
                        "(\n" +
                        "  Firm : Pure\n" +
                        "  {" +
                        "    legalName : 1\n" +
                        "  }\n" +
                        ")");

        PureCompilationException e = Assert.assertThrows(PureCompilationException.class, runtime::compile);
        assertPureException(PureCompilationException.class,
                "Type Error: 'Integer' not a subtype of 'String'",
                "mapping.pure", 5, 17, 5, 17, 5, 17, e);
    }


    @Test
    public void testMappingWithMultiplicityMismatch()
    {
        runtime.createInMemorySource("model.pure",
                "Class Firm" +
                        "{" +
                        "  legalName : String[1];" +
                        "}");

        runtime.createInMemorySource("mapping.pure",
                "###Mapping\n" +
                        "Mapping test::TestMapping\n" +
                        "(\n" +
                        "  Firm : Pure\n" +
                        "  {" +
                        "    legalName : ['a','b']\n" +
                        "  }\n" +
                        ")");

        PureCompilationException e = Assert.assertThrows(PureCompilationException.class, runtime::compile);
        assertPureException(PureCompilationException.class,
                "Multiplicity Error: The property 'legalName' has a multiplicity range of [1] when the given expression has a multiplicity range of [2]",
                "mapping.pure", 5, 17, 5, 17, 5, 25, e);
    }


    @Test
    public void testFilter()
    {
        runtime.createInMemorySource("model.pure",
                "Class Firm" +
                        "{" +
                        "  legalName : String[1];" +
                        "}" +
                        "Class FirmSource" +
                        "{" +
                        "   val : String[1];" +
                        "}");

        String source = "###Mapping\n" +
                "Mapping test::TestMapping\n" +
                "(\n" +
                "  Firm[firm] : Pure\n" +
                "  {\n" +
                "    ~src FirmSource\n" +
                "    ~filter $src.val == 'ok'\n" +
                "    legalName : $src.val\n" +
                "  }\n" +
                ")";
        runtime.createInMemorySource("mapping.pure", source);
        runtime.compile();
        assertSetSourceInformation(source, "Firm");
        CoreInstance mapping = runtime.getCoreInstance("test::TestMapping");
        Assert.assertNotNull(mapping);
        Assert.assertEquals(new SourceInformation("mapping.pure", 2, 1, 2, 15, 10, 1), mapping.getSourceInformation());
        PureInstanceSetImplementation classMapping = (PureInstanceSetImplementation) mapping.getValueInValueForMetaPropertyToManyWithKey(M2MappingProperties.classMappings, M3Properties.id, "firm");
        Assert.assertNotNull(classMapping);
        Assert.assertEquals(new SourceInformation("mapping.pure", 4, 3, 9, 3), classMapping.getSourceInformation());
        Assert.assertNotNull(classMapping._filter());
        Assert.assertNotNull(classMapping._filter().getSourceInformation());
    }

    @Test
    public void testFilterError()
    {
        runtime.createInMemorySource("model.pure",
                "Class Firm" +
                        "{" +
                        "  legalName : String[1];" +
                        "}" +
                        "Class FirmSource" +
                        "{" +
                        "   val : String[1];" +
                        "}");

        runtime.createInMemorySource("mapping.pure",
                "###Mapping\n" +
                        "Mapping test::TestMapping\n" +
                        "(\n" +
                        "  Firm : Pure\n" +
                        "  {" +
                        "    ~src FirmSource\n" +
                        "    ~filter $src.valX == 'ok'" +
                        "    legalName : $src.val\n" +
                        "  }\n" +
                        ")");

        PureCompilationException e = Assert.assertThrows(PureCompilationException.class, runtime::compile);
        assertPureException(PureCompilationException.class,
                "Can't find the property 'valX' in the class FirmSource",
                "mapping.pure", 6, 18, 6, 18, 6, 21, e);
    }


    @Test
    public void testFilterTypeError()
    {
        runtime.createInMemorySource("model.pure",
                "Class Firm" +
                        "{" +
                        "  legalName : String[1];" +
                        "}" +
                        "Class FirmSource" +
                        "{" +
                        "   val : String[1];" +
                        "}");

        runtime.createInMemorySource("mapping.pure",
                "###Mapping\n" +
                        "Mapping test::TestMapping\n" +
                        "(\n" +
                        "  Firm : Pure\n" +
                        "  {" +
                        "    ~src FirmSource\n" +
                        "    ~filter $src.val" +
                        "    legalName : $src.val\n" +
                        "  }\n" +
                        ")");

        PureCompilationException e = Assert.assertThrows(PureCompilationException.class, runtime::compile);
        assertPureException(PureCompilationException.class,
                "A filter should be a Boolean expression",
                "mapping.pure", 6, 18, 6, 18, 6, 20, e);
    }

    @Test
    public void testComplexTypePropertyMapping()
    {
        runtime.createInMemorySource("model.pure",
                "Class Person" +
                        "{" +
                        "   firms : Firm[*];" +
                        "}" +
                        "Class Firm" +
                        "{" +
                        "  legalName : String[1];" +
                        "}" +
                        "Class _Person" +
                        "{" +
                        "   firms : _Firm[*];" +
                        "}" +
                        "Class _Firm" +
                        "{" +
                        "  legalName : String[1];" +
                        "}");

        String source = "###Mapping\n" +
                "Mapping test::TestMapping\n" +
                "(\n" +
                "  Firm : Pure\n" +
                "  {\n" +
                "    ~src _Firm\n" +
                "    legalName : $src.legalName" +
                "  }\n" +
                "  Person : Pure\n" +
                "  {\n" +
                "    ~src _Person\n" +
                "    firms : $src.firms" +
                "  }\n" +
                ")";
        runtime.createInMemorySource("mapping.pure", source);
        runtime.compile();
        assertSetSourceInformation(source, "Firm");
    }

    @Test
    public void testComplexTypePropertyMappingError()
    {
        runtime.createInMemorySource("model.pure",
                "Class Person" +
                        "{" +
                        "   firms : Firm[*];" +
                        "}" +
                        "Class Firm" +
                        "{" +
                        "  legalName : String[1];" +
                        "}" +
                        "Class _Person" +
                        "{" +
                        "   name : String[1];" +
                        "   firms : _Firm[*];" +
                        "}" +
                        "Class _Firm" +
                        "{" +
                        "  legalName : String[1];" +
                        "}");

        runtime.createInMemorySource("mapping.pure",
                "###Mapping\n" +
                        "Mapping test::TestMapping\n" +
                        "(\n" +
                        "  Firm : Pure\n" +
                        "  {\n" +
                        "    ~src _Person" +
                        "    legalName  : $src.name\n" +
                        "  }\n" +
                        "  Person : Pure\n" +
                        "  {\n" +
                        "    ~src _Person\n" +
                        "    firms : $src.firms" +
                        "  }\n" +
                        ")");

        PureCompilationException e = Assert.assertThrows(PureCompilationException.class, runtime::compile);
        assertPureException(PureCompilationException.class,
                "Type Error: '_Person' is not '_Firm'",
                "mapping.pure", 11, 18, 11, 18, 11, 22, e);
    }

    @Test
    public void testComplexTypePropertyMappingWithWrongTargetIdError()
    {
        runtime.createInMemorySource("model.pure",
                "Class Person" +
                        "{" +
                        "   firms : Firm[*];" +
                        "}" +
                        "Class Firm" +
                        "{" +
                        "  legalName : String[1];" +
                        "}" +
                        "Class _Person" +
                        "{" +
                        "   name : String[1];" +
                        "   firms : _Firm[*];" +
                        "}" +
                        "Class _Firm" +
                        "{" +
                        "  legalName : String[1];" +
                        "}");

        runtime.createInMemorySource("mapping.pure",
                "###Mapping\n" +
                        "Mapping test::TestMapping\n" +
                        "(\n" +
                        "  Firm : Pure\n" +
                        "  {\n" +
                        "    ~src _Person" +
                        "    legalName  : $src.name\n" +
                        "  }\n" +
                        "  Person : Pure\n" +
                        "  {\n" +
                        "    ~src _Person\n" +
                        "    firms[f2] : $src.firms" +
                        "  }\n" +
                        ")");

        PureCompilationException e = Assert.assertThrows(PureCompilationException.class, runtime::compile);
        assertPureException(PureCompilationException.class,
                "The set implementation 'f2' is unknown in the mapping 'TestMapping'",
                "mapping.pure", 11, 5, 11, 5, 11, 9, e);
    }

    @Test
    public void testMilestonedMappingWithLatestDate()
    {
        runtime.createInMemorySource("model.pure",
                "Class Firm\n" +
                        "{\n" +
                        "  legalName : String[1];\n" +
                        "  employees : Person[*];\n" +
                        "}\n" +
                        "Class <<temporal.businesstemporal>> Person\n" +
                        "{\n" +
                        "  name : String[1];\n" +
                        "}\n" +
                        "Class TargetFirm\n" +
                        "{\n" +
                        "  legalName : String[1];\n" +
                        "  employeeNames : String[*];\n" +
                        "}\n");

        String source = "###Mapping\n" +
                "Mapping test::TestMapping\n" +
                "(\n" +
                "  TargetFirm : Pure\n" +
                "  {\n" +
                "    ~src Firm" +
                "    legalName : $src.legalName,\n" +
                "    employeeNames : $src.employees(%latest)->map(e | $e.name)\n" +
                "  }\n" +
                ")";
        runtime.createInMemorySource("mapping.pure", source);
        runtime.compile();
        assertSetSourceInformation(source, "TargetFirm");
    }

    @Test
    public void testM2MMappingWithEnumerationMapping()
    {
        runtime.createInMemorySource("model.pure",
                "###Pure\n" +
                        "import my::*;\n" +
                        "\n" +
                        "Class my::SourceProduct\n" +
                        "{\n" +
                        "   id : Integer[1];\n" +
                        "   state : String[1];\n" +
                        "}\n" +
                        "\n" +
                        "Class my::TargetProduct\n" +
                        "{\n" +
                        "   id : Integer[1];\n" +
                        "   state : State[1];\n" +
                        "}\n" +
                        "\n" +
                        "Enum my::State\n" +
                        "{\n" +
                        "   ACTIVE,\n" +
                        "   INACTIVE\n" +
                        "}"
        );

        runtime.createInMemorySource("mapping.pure",
                "###Mapping\n" +
                        "import my::*;\n" +
                        "\n" +
                        "Mapping my::modelMapping\n" +
                        "(\n" +
                        "   TargetProduct : Pure\n" +
                        "   {\n" +
                        "      ~src SourceProduct\n" +
                        "      id : $src.id,\n" +
                        "      state : EnumerationMapping StateMapping : $src.state\n" +
                        "   }\n" +
                        "   \n" +
                        "   State : EnumerationMapping StateMapping\n" +
                        "   {\n" +
                        "      ACTIVE : 'ACTIVE',\n" +
                        "      INACTIVE : 'INACTIVE'\n" +
                        "   }\n" +
                        ")"
        );

        runtime.compile();
        Mapping mapping = (Mapping) runtime.getCoreInstance("my::modelMapping");
        PureInstanceSetImplementation m2mMapping = mapping._classMappings().selectInstancesOf(PureInstanceSetImplementation.class).getFirst();

        PurePropertyMapping purePropertyMapping1 = m2mMapping._propertyMappings().selectInstancesOf(PurePropertyMapping.class).getFirst();
        Assert.assertNull(purePropertyMapping1._transformer());

        PurePropertyMapping purePropertyMapping2 = m2mMapping._propertyMappings().selectInstancesOf(PurePropertyMapping.class).getLast();
        Assert.assertNotNull(purePropertyMapping2._transformer());
        Assert.assertTrue(purePropertyMapping2._transformer() instanceof EnumerationMapping);

        EnumerationMapping<?> transformer = (EnumerationMapping<?>) purePropertyMapping2._transformer();
        Assert.assertEquals("StateMapping", transformer._name());
        Assert.assertEquals("my::State", PackageableElement.getUserPathForPackageableElement(transformer._enumeration()));
        Assert.assertEquals(2, transformer._enumValueMappings().size());
    }

    @Test
    public void testM2MMappingWithInvalidEnumerationMapping()
    {
        runtime.createInMemorySource("model.pure",
                "###Pure\n" +
                        "import my::*;\n" +
                        "\n" +
                        "Class my::SourceProduct\n" +
                        "{\n" +
                        "   id : Integer[1];\n" +
                        "   state : String[1];\n" +
                        "}\n" +
                        "\n" +
                        "Class my::TargetProduct\n" +
                        "{\n" +
                        "   id : Integer[1];\n" +
                        "   state : State[1];\n" +
                        "}\n" +
                        "\n" +
                        "Enum my::State\n" +
                        "{\n" +
                        "   ACTIVE,\n" +
                        "   INACTIVE\n" +
                        "}\n" +
                        "\n" +
                        "Enum my::Option\n" +
                        "{\n" +
                        "   CALL,\n" +
                        "   PUT\n" +
                        "}\n"
        );

        runtime.createInMemorySource("mapping.pure",
                "###Mapping\n" +
                        "import my::*;\n" +
                        "\n" +
                        "Mapping my::modelMapping\n" +
                        "(\n" +
                        "   TargetProduct : Pure\n" +
                        "   {\n" +
                        "      ~src SourceProduct\n" +
                        "      id : $src.id,\n" +
                        "      state : EnumerationMapping OptionMapping : $src.state\n" +
                        "   }\n" +
                        "   \n" +
                        "   Option : EnumerationMapping OptionMapping\n" +
                        "   {\n" +
                        "      CALL : 'ACTIVE',\n" +
                        "      PUT : 'INACTIVE'\n" +
                        "   }\n" +
                        ")"
        );

        PureCompilationException e = Assert.assertThrows(PureCompilationException.class, runtime::compile);
        assertPureException(PureCompilationException.class, "Property : [state] is of type : [my::State] but enumeration mapping : [OptionMapping] is defined on enumeration : [my::Option].", "mapping.pure", 10, 7, e);
    }

    @Test
    @Ignore
    @ToFix
    public void testMissingRequiredPropertyError()
    {
        compileTestSource("model.pure",
                "Class test::SourceClass\n" +
                        "{\n" +
                        "    prop1 : String[1];\n" +
                        "}\n" +
                        "\n" +
                        "Class test::TargetClass\n" +
                        "{\n" +
                        "    prop2 : String[1];\n" +
                        "    prop3 : Integer[1];\n" +
                        "}\n");

        PureCompilationException e = Assert.assertThrows(PureCompilationException.class, () -> compileTestSource(
                "mapping.pure",
                "###Mapping\n" +
                        "import test::*;\n" +
                        "Mapping test::TestMapping\n" +
                        "(\n" +
                        "    TargetClass : Pure\n" +
                        "    {\n" +
                        "        ~src SourceClass\n" +
                        "        prop2 : $src.prop1\n" +
                        "    }\n" +
                        ")\n"));
        assertPureException(PureCompilationException.class, "The following required properties for test::TargetClass are not mapped: prop3", "/test/mapping.pure", 5, 5, 5, 5, 5, 15, e);
    }

    @Test
    public void testMappingWithMerge()
    {
        String source =
                "Class  example::SourcePersonWithFirstName\n" +
                        "{\n" +
                        "   id:Integer[1];\n" +
                        "   firstName:String[1];\n" +
                        "}\n" +
                        "\n" +
                        "\n" +
                        "Class example::SourcePersonWithLastName\n" +
                        "{\n" +
                        "   id:Integer[1];\n" +
                        "   lastName:String[1];\n" +
                        "}\n" +
                        "Class example::Person\n" +
                        "{\n" +
                        "   firstName:String[1];\n" +
                        "   lastName:String[1];\n" +
                        "}\n" +

                        "\n" +
                        "function meta::pure::router::operations::merge(o:meta::pure::mapping::OperationSetImplementation[1]):meta::pure::mapping::SetImplementation[*] {[]}\n" +

                        "###Mapping\n" +
                        "Mapping  example::MergeModelMappingSourceWithMatch\n" +
                        "(\n" +
                        "   *example::Person : Operation\n" +
                        "           {\n" +
                        "             meta::pure::router::operations::merge_OperationSetImplementation_1__SetImplementation_MANY_([p1,p2,p3],{p1:example::SourcePersonWithFirstName[1], p2:example::SourcePersonWithLastName[1],p4:example::SourcePersonWithLastName[1] | $p1.id ==  $p2.id })\n" +

                        "           }\n" +
                        "\n" +
                        "   example::Person[p1] : Pure\n" +
                        "            {\n" +
                        "               ~src example::SourcePersonWithFirstName\n" +
                        "               firstName : $src.firstName\n" +
                        "            }\n" +
                        "\n" +
                        "   example::Person[p2] : Pure\n" +
                        "            {\n" +
                        "               ~src example::SourcePersonWithLastName\n" +
                        "        lastName :  $src.lastName\n" +
                        "            }\n" +
                        "   example::Person[p3] : Pure\n" +
                        "            {\n" +
                        "               ~src example::SourcePersonWithLastName\n" +
                        "        lastName :  $src.lastName\n" +
                        "            }\n" +

                        "\n" +
                        ")";


        runtime.createInMemorySource("mapping.pure", source);
        runtime.compile();
    }

    @Test
    public void testMappingWithMergeInvalidReturn()
    {
        String source =
                "Class  example::SourcePersonWithFirstName\n" +
                        "{\n" +
                        "   id:Integer[1];\n" +
                        "   firstName:String[1];\n" +
                        "}\n" +
                        "\n" +
                        "\n" +
                        "Class example::SourcePersonWithLastName\n" +
                        "{\n" +
                        "   id:Integer[1];\n" +
                        "   lastName:String[1];\n" +
                        "}\n" +
                        "Class example::Person\n" +
                        "{\n" +
                        "   firstName:String[1];\n" +
                        "   lastName:String[1];\n" +
                        "}\n" +

                        "\n" +
                        "function meta::pure::router::operations::merge(o:meta::pure::mapping::OperationSetImplementation[1]):meta::pure::mapping::SetImplementation[*] {[]}\n" +

                        "###Mapping\n" +
                        "Mapping  example::MergeModelMappingSourceWithMatch\n" +
                        "(\n" +
                        "   *example::Person : Operation\n" +
                        "           {\n" +
                        "             meta::pure::router::operations::merge_OperationSetImplementation_1__SetImplementation_MANY_([p1,p2,p3],{p1:example::SourcePersonWithFirstName[1], p2:example::SourcePersonWithLastName[1],p4:example::SourcePersonWithLastName[1] |  'test' })\n" +

                        "           }\n" +
                        "\n" +
                        "   example::Person[p1] : Pure\n" +
                        "            {\n" +
                        "               ~src example::SourcePersonWithFirstName\n" +
                        "               firstName : $src.firstName\n" +
                        "            }\n" +
                        "\n" +
                        "   example::Person[p2] : Pure\n" +
                        "            {\n" +
                        "               ~src example::SourcePersonWithLastName\n" +
                        "        lastName :  $src.lastName\n" +
                        "            }\n" +
                        "   example::Person[p3] : Pure\n" +
                        "            {\n" +
                        "               ~src example::SourcePersonWithLastName\n" +
                        "        lastName :  $src.lastName\n" +
                        "            }\n" +

                        "\n" +
                        ")";


        runtime.createInMemorySource("mapping.pure", source);

        PureCompilationException e = Assert.assertThrows(PureCompilationException.class, runtime::compile);
        assertPureException(PureCompilationException.class,
                "Merge validation function for class: Person does not return Boolean",
                "mapping.pure", 23, 5, 23, 14, 26, 12, e);
    }

    @Test
    public void testMappingWithMergeInvalidParameter()
    {
        String source =
                "Class  example::SourcePersonWithFirstName\n" +
                        "{\n" +
                        "   id:Integer[1];\n" +
                        "   firstName:String[1];\n" +
                        "}\n" +
                        "\n" +
                        "\n" +
                        "Class example::SourcePersonWithLastName\n" +
                        "{\n" +
                        "   id:Integer[1];\n" +
                        "   lastName:String[1];\n" +
                        "}\n" +
                        "Class example::Person\n" +
                        "{\n" +
                        "   id:Integer[1];\n" +
                        "   firstName:String[1];\n" +
                        "   lastName:String[1];\n" +
                        "}\n" +

                        "\n" +
                        "function meta::pure::router::operations::merge(o:meta::pure::mapping::OperationSetImplementation[1]):meta::pure::mapping::SetImplementation[*] {[]}\n" +

                        "###Mapping\n" +
                        "Mapping  example::MergeModelMappingSourceWithMatch\n" +
                        "(\n" +
                        "   *example::Person : Operation\n" +
                        "           {\n" +
                        "             meta::pure::router::operations::merge_OperationSetImplementation_1__SetImplementation_MANY_([p1,p2,p3],{p1:example::Person[1], p2:example::SourcePersonWithLastName[1],p4:example::SourcePersonWithLastName[1] | $p1.id ==  $p2.id })\n" +

                        "           }\n" +
                        "\n" +
                        "   example::Person[p1] : Pure\n" +
                        "            {\n" +
                        "               ~src example::SourcePersonWithFirstName\n" +
                        "               firstName : $src.firstName\n" +
                        "            }\n" +
                        "\n" +
                        "   example::Person[p2] : Pure\n" +
                        "            {\n" +
                        "               ~src example::SourcePersonWithLastName\n" +
                        "        lastName :  $src.lastName\n" +
                        "            }\n" +
                        "   example::Person[p3] : Pure\n" +
                        "            {\n" +
                        "               ~src example::SourcePersonWithLastName\n" +
                        "        lastName :  $src.lastName\n" +
                        "            }\n" +

                        "\n" +
                        ")";


        runtime.createInMemorySource("mapping.pure", source);
        PureCompilationException e = Assert.assertThrows(PureCompilationException.class, runtime::compile);
        assertPureException(PureCompilationException.class,
                "Merge validation function for class: Person has an invalid parameter. All parameters must be a src class of a merged set",
                "mapping.pure", 24, 5, 24, 14, 27, 12, e);
    }

    @Test
    public void testLocalPropertyWithInvalidType()
    {
        runtime.createInMemorySource("model.pure",
                "Class Firm\n" +
                        "{\n" +
                        "  legalName : String[1];\n" +
                        "}\n" +
                        "\n" +
                        "Class FirmSource\n" +
                        "{\n" +
                        "  allNames : String[1..*];\n" +
                        "}\n");
        runtime.createInMemorySource("mapping.pure",
                "###Mapping\n" +
                        "Mapping test::TestMapping\n" +
                        "(\n" +
                        "  Firm : Pure\n" +
                        "  {\n" +
                        "    ~src FirmSource\n" +
                        "    legalName : $src.allNames->at(0),\n" +
                        "    +otherNames:Strixng[*] : $src.allNames->tail()\n" +
                        "  }\n" +
                        ")");
        PureCompilationException e = Assert.assertThrows(PureCompilationException.class, runtime::compile);
        assertPureException(PureCompilationException.class, "Strixng has not been defined!", "mapping.pure", 8, 17, 8, 17, 8, 23, e);
    }

    @Test
    public void testLocalPropertyTypeError()
    {
        runtime.createInMemorySource("model.pure",
                "Class Firm\n" +
                        "{\n" +
                        "  legalName : String[1];\n" +
                        "}\n" +
                        "\n" +
                        "Class FirmSource\n" +
                        "{\n" +
                        "  allNames : String[1..*];\n" +
                        "}\n");
        runtime.createInMemorySource("mapping.pure",
                "###Mapping\n" +
                        "Mapping test::TestMapping\n" +
                        "(\n" +
                        "  Firm : Pure\n" +
                        "  {\n" +
                        "    ~src FirmSource\n" +
                        "    legalName : $src.allNames->at(0),\n" +
                        "    +otherNames:String[*] : $src.allNames->size()\n" +
                        "  }\n" +
                        ")");
        PureCompilationException e = Assert.assertThrows(PureCompilationException.class, runtime::compile);
        assertPureException(PureCompilationException.class, "Type Error: 'Integer' not a subtype of 'String'", "mapping.pure", 8, 44, e);
    }
}

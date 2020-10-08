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

package org.finos.legend.pure.runtime.java.extension.external.json.shared;

import org.finos.legend.pure.m3.coreinstance.meta.pure.metamodel.type.Class;
import org.finos.legend.pure.m3.coreinstance.meta.pure.metamodel.type.Enumeration;
import org.finos.legend.pure.m4.coreinstance.CoreInstance;
import org.finos.legend.pure.m4.coreinstance.primitive.date.DateTime;
import org.finos.legend.pure.m4.coreinstance.primitive.date.LatestDate;
import org.finos.legend.pure.m4.coreinstance.primitive.date.PureDate;
import org.finos.legend.pure.runtime.java.extension.external.shared.conversion.ClassConversion;
import org.finos.legend.pure.runtime.java.extension.external.shared.conversion.Conversion;
import org.finos.legend.pure.runtime.java.extension.external.shared.conversion.ConversionCache;
import org.finos.legend.pure.runtime.java.extension.external.shared.conversion.ConversionContext;
import org.finos.legend.pure.runtime.java.extension.external.shared.conversion.EnumerationConversion;
import org.finos.legend.pure.runtime.java.extension.external.shared.conversion.MapConversion;
import org.finos.legend.pure.runtime.java.extension.external.shared.conversion.PrimitiveConversion;
import org.finos.legend.pure.runtime.java.extension.external.shared.conversion.UnitConversion;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class JsonSerializationCache extends ConversionCache
{
    @Override
    protected Map<PrimitiveConversion.PurePrimitive, PrimitiveConversion<?, ?>> constructPrimitiveConversions()
    {
        Map<PrimitiveConversion.PurePrimitive, PrimitiveConversion<?, ?>> primitiveConverters = new HashMap<>();
        primitiveConverters.put(PrimitiveConversion.PurePrimitive.Integer, PrimitiveConversion.noOpConversion(Long.class, "Integer", false));
        primitiveConverters.put(PrimitiveConversion.PurePrimitive.String, PrimitiveConversion.noOpConversion(String.class, "String", false));
        primitiveConverters.put(PrimitiveConversion.PurePrimitive.Number, PrimitiveConversion.noOpConversion(Number.class, "Number", false));
        primitiveConverters.put(PrimitiveConversion.PurePrimitive.Boolean, PrimitiveConversion.noOpConversion(Boolean.class, "Boolean", false));
        primitiveConverters.put(PrimitiveConversion.PurePrimitive.Float, new PrimitiveConversion<Double, Number>()
        {
            @Override
            public Double apply(Double value, ConversionContext context)
            {
                return value;
            }

            @Override
            public String pureTypeAsString()
            {
                return "Number";
            }
        });
        primitiveConverters.put(PrimitiveConversion.PurePrimitive.Decimal, new PrimitiveConversion<BigDecimal, Number>()
        {
            @Override
            public BigDecimal apply(BigDecimal value, ConversionContext context)
            {
                return value;
            }

            @Override
            public String pureTypeAsString()
            {
                return "Number";
            }
        });
        primitiveConverters.put(PrimitiveConversion.PurePrimitive.Date, new PrimitiveConversion<PureDate, String>()
        {
            @Override
            public String apply(PureDate value, ConversionContext context)
            {
                return value.toString();
            }

            @Override
            public String pureTypeAsString()
            {
                return "Date";
            }

        });
        primitiveConverters.put(PrimitiveConversion.PurePrimitive.DateTime, new PrimitiveConversion<DateTime, String>()
        {
            @Override
            public String apply(DateTime value, ConversionContext context)
            {
                return this.potentiallyFormatDateTime(value, ((JsonSerializationContext)context).getDateTimeFormat());
            }

            @Override
            public String pureTypeAsString()
            {
                return "DateTime";
            }
        });
        primitiveConverters.put(PrimitiveConversion.PurePrimitive.StrictDate, new PrimitiveConversion<PureDate, String>()
        {
            @Override
            public String apply(PureDate value, ConversionContext context)
            {
                return value.toString();
            }

            @Override
            public String pureTypeAsString()
            {
                return "StrictDate";
            }
        });
        primitiveConverters.put(PrimitiveConversion.PurePrimitive.LatestDate, new PrimitiveConversion<LatestDate, String>()
        {
            @Override
            public String apply(LatestDate value, ConversionContext context)
            {
                return "%latest";
            }

            @Override
            public String pureTypeAsString()
            {
                return "LatestDate";
            }
        });
        return primitiveConverters;
    }

    @Override
    protected ClassConversion<?, ?> newClassConversion(Class clazz, ConversionContext context)
    {
        return new JsonClassSerialization<>(clazz);
    }

    @Override
    protected MapConversion<?, ?> newMapConversion(ConversionContext context)
    {
        return new JsonMapSerialization<>();
    }

    @Override
    protected EnumerationConversion<?, ?> newEnumerationConversion(Enumeration enumeration, ConversionContext context)
    {
        return new JsonEnumerationSerialization<>(enumeration);
    }

    @Override
    protected UnitConversion<?, ?> newUnitConversion(CoreInstance type, ConversionContext context)
    {
        return new JsonUnitSerialization<>(type);
    }

    @Override
    protected Conversion<?, ?> newGenericAndAnyTypeConversion(ConversionContext context)
    {
        return JsonGenericAndAnyTypeSerialization.JSON_GENERIC_AND_ANY_TYPE_SERIALIZATION;
    }
}

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

package org.finos.legend.pure.m3.compiler.postprocessing.processor.milestoning;

import org.eclipse.collections.api.block.function.Function;
import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.api.list.ListIterable;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.block.factory.Functions;
import org.eclipse.collections.impl.block.factory.Predicates;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.tuple.Tuples;
import org.eclipse.collections.impl.utility.ArrayIterate;
import org.finos.legend.pure.m3.navigation.M3Paths;
import org.finos.legend.pure.m3.navigation.M3PropertyPaths;
import org.finos.legend.pure.m3.compiler.Context;
import org.finos.legend.pure.m3.navigation.Instance;
import org.finos.legend.pure.m3.compiler.PropertyOwnerStrategy;
import org.finos.legend.pure.m3.navigation.profile.Profile;
import org.finos.legend.pure.m3.navigation.type.Type;
import org.finos.legend.pure.m3.coreinstance.meta.pure.metamodel.PropertyOwner;
import org.finos.legend.pure.m3.coreinstance.meta.pure.metamodel._import.ImportStub;
import org.finos.legend.pure.m3.coreinstance.meta.pure.metamodel.extension.ElementWithStereotypes;
import org.finos.legend.pure.m3.coreinstance.meta.pure.metamodel.function.property.Property;
import org.finos.legend.pure.m3.coreinstance.meta.pure.metamodel.function.property.QualifiedProperty;
import org.finos.legend.pure.m3.coreinstance.meta.pure.metamodel.type.Class;
import org.finos.legend.pure.m3.coreinstance.meta.pure.metamodel.type.FunctionType;
import org.finos.legend.pure.m3.coreinstance.meta.pure.metamodel.valuespecification.InstanceValue;
import org.finos.legend.pure.m3.navigation.ProcessorSupport;
import org.finos.legend.pure.m4.coreinstance.CoreInstance;
import org.finos.legend.pure.m4.exception.PureCompilationException;

public class MilestoningFunctions
{
    public static final String MILESTONING = "milestoning";
    private static final String MILESTONING_PATH = "meta::pure::profiles::" + MILESTONING;
    static final String MILESTONE_LAMBDA_VARIABLE_NAME = "v_milestone";
    static final String GENERATED_MILESTONING_STEREOTYPE_VALUE = "generatedmilestoningproperty";
    static final String GENERATED_MILESTONING_DATE_STEREOTYPE_VALUE = "generatedmilestoningdateproperty";
    static final String GENERATED_MILESTONING_STEREOTYPE = "<<" + MILESTONING_PATH + "." + GENERATED_MILESTONING_STEREOTYPE_VALUE + ">>";
    static final String GENERATED_MILESTONING_DATE_STEREOTYPE = "<<" + MILESTONING_PATH + "." + GENERATED_MILESTONING_DATE_STEREOTYPE_VALUE + ">>";
    private static final String GENERATED_MILESTONING_PATH_SUFFIX = MILESTONING_PATH + "@" + GENERATED_MILESTONING_STEREOTYPE_VALUE;
    private static final String GENERATED_MILESTONING_DATE_PATH_SUFFIX = MILESTONING_PATH + "@" + GENERATED_MILESTONING_DATE_STEREOTYPE_VALUE;
    private static final String EDGEPOINT_PROPERTY_NAME_SUFFIX = "AllVersions";
    private static final String RANGE_PROPERTY_NAME_SUFFIX = "AllVersionsInRange";
    public static final String MILESTONING_GET_ALL_FUNCTION_PATH = "meta::pure::functions::collection::getAll_Class_1__Date_1__T_MANY_";
    public static final String BITEMPORAL_MILESTONING_GET_ALL_FUNCTION_PATH = "meta::pure::functions::collection::getAll_Class_1__Date_1__Date_1__T_MANY_";

    public static class IsMilestonePropertyPredicate implements Predicate<CoreInstance>
    {
        private final ProcessorSupport processorSupport;

        public IsMilestonePropertyPredicate(ProcessorSupport processorSupport)
        {
            this.processorSupport = processorSupport;
        }

        @Override
        public boolean accept(CoreInstance property)
        {
            return isGeneratedMilestoningProperty(property, this.processorSupport);
        }
    }

    public static class IsMilestoneDatePropertyPredicate implements Predicate<CoreInstance>
    {
        private final ProcessorSupport processorSupport;

        public IsMilestoneDatePropertyPredicate(ProcessorSupport processorSupport)
        {
            this.processorSupport = processorSupport;
        }

        @Override
        public boolean accept(CoreInstance property)
        {
            return isGeneratedMilestoningDateProperty(property, this.processorSupport);
        }
    }

    public static Function<CoreInstance, ListIterable<CoreInstance>> toInstanceValues(final ProcessorSupport processorSupport)
    {
        return new Function<CoreInstance, ListIterable<CoreInstance>>()
        {
            @Override
            public ListIterable<CoreInstance> valueOf(CoreInstance coreInstance)
            {
                return (ListIterable<CoreInstance>)(coreInstance instanceof InstanceValue ?
                        ((InstanceValue)coreInstance)._valuesCoreInstance() : FastList.newListWith(coreInstance));
            }
        };
    }

    public static Predicate<CoreInstance> isLatestDate(final ProcessorSupport processorSupport)
    {
        return new Predicate<CoreInstance>()
        {
            @Override
            public boolean accept(CoreInstance coreInstance)
            {
                return Instance.instanceOf(coreInstance, M3Paths.LatestDate, processorSupport);
            }
        };
    }

    static final Function<org.finos.legend.pure.m3.coreinstance.meta.pure.metamodel.function.Function, String> TO_FUNCTION_NAME = new Function<org.finos.legend.pure.m3.coreinstance.meta.pure.metamodel.function.Function, String>()
    {
        @Override
        public String valueOf(org.finos.legend.pure.m3.coreinstance.meta.pure.metamodel.function.Function coreInstance)
        {
            return coreInstance._functionName();
        }
    };

    private static Function<CoreInstance, String> toStereotypeName(final ProcessorSupport processorSupport)
    {
        return new Function<CoreInstance, String>()
        {
            @Override
            public String valueOf(CoreInstance stereotype)
            {
                if (stereotype instanceof ImportStub)
                {
                    String idOrPath = ((ImportStub)stereotype)._idOrPath();
                    return idOrPath.split("@")[1];
                }
                else
                {
                    return CoreInstance.GET_NAME.valueOf(stereotype);
                }
            }
        };
    }

    private static final Function<MilestoningStereotype, ListIterable<String>> GET_DATE_PROPERTY_NAME = new Function<MilestoningStereotype, ListIterable<String>>()
    {
        @Override
        public ListIterable<String> valueOf(MilestoningStereotype milestoningStereotype)
        {
            return milestoningStereotype.getTemporalDatePropertyNames();
        }
    };

    public static final Function<MilestoningStereotype, String> GET_PLATFORM_NAME = new Function<MilestoningStereotype, String>()
    {
        @Override
        public String valueOf(MilestoningStereotype milestoningStereotype)
        {
            return milestoningStereotype.getPurePlatformStereotypeName();
        }
    };


    private static final Function<MilestoningStereotypeEnum, Pair<String, MilestoningStereotypeEnum>> MILESTONINGSTEREOTYPE_TO_PURE_NAMED_STEREOTYPE_PAIR = new Function<MilestoningStereotypeEnum, Pair<String, MilestoningStereotypeEnum>>()
    {
        @Override
        public Pair<String, MilestoningStereotypeEnum> valueOf(MilestoningStereotypeEnum milestoningStereotype)
        {
            return Tuples.pair(milestoningStereotype.getPurePlatformStereotypeName(), milestoningStereotype);
        }
    };

    private MilestoningFunctions()
    {
    }

    public static boolean isEdgePointProperty(CoreInstance property, ProcessorSupport processorSupport)
    {
        return isGeneratedMilestoningProperty(property, processorSupport) &&
                Instance.instanceOf(property, M3Paths.Property, processorSupport) &&
                property.getName().endsWith(EDGEPOINT_PROPERTY_NAME_SUFFIX);
    }

    public static boolean isAllVersionsInRangeProperty(CoreInstance property, ProcessorSupport processorSupport)
    {
        return isGeneratedMilestoningProperty(property, processorSupport) && property.getValueForMetaPropertyToOne("name").getName().endsWith(RANGE_PROPERTY_NAME_SUFFIX);
    }

    public static boolean isGeneratedMilestoningProperty(CoreInstance property, final ProcessorSupport processorSupport, final String stereotype, final String milestoningPathSuffix)
    {
        CoreInstance profile = processorSupport.package_getByUserPath(M3Paths.Milestoning);
        final CoreInstance milestoningStereotype = Profile.findStereotype(profile, stereotype);

        ListIterable<? extends CoreInstance> stereotypes = property instanceof ElementWithStereotypes ? ((ElementWithStereotypes)property)._stereotypesCoreInstance().toList() : Lists.immutable.<CoreInstance>empty();
        return stereotypes.detect(new Predicate<CoreInstance>()
        {
            @Override
            public boolean accept(CoreInstance stereotype)
            {
                boolean result;
                if (stereotype == null)
                {
                    result = false;
                }
                else if (stereotype instanceof ImportStub)
                {
                    String idOrPath = ((ImportStub)stereotype)._idOrPath();
                    result = idOrPath.endsWith(milestoningPathSuffix);
                }
                else
                {
                    result = milestoningStereotype.equals(stereotype);
                }
                return result;
            }
        }) != null;
    }

    public static boolean isGeneratedMilestoningProperty(CoreInstance property, final ProcessorSupport processorSupport)
    {
        return isGeneratedMilestoningProperty(property, processorSupport, GENERATED_MILESTONING_STEREOTYPE_VALUE, GENERATED_MILESTONING_PATH_SUFFIX);
    }

    public static boolean isGeneratedMilestoningDateProperty(CoreInstance property, final ProcessorSupport processorSupport)
    {
        return isGeneratedMilestoningProperty(property, processorSupport, GENERATED_MILESTONING_DATE_STEREOTYPE_VALUE, GENERATED_MILESTONING_DATE_PATH_SUFFIX);
    }

    public static boolean isAutoGeneratedMilestoningNamedDateProperty(Property property, ProcessorSupport processorSupport)
    {
        return isGeneratedMilestoningDateProperty(property, processorSupport) && MILESTONING.equals(property._name());
    }

    public static MilestonedPropertyMetaData getMilestonedMetaDataForProperty(QualifiedProperty property, ProcessorSupport processorSupport)
    {
        CoreInstance returnType = org.finos.legend.pure.m3.navigation.importstub.ImportStub.withImportStubByPass(property._genericType()._rawTypeCoreInstance(), processorSupport);
        ListIterable<MilestoningStereotypeEnum> stereotypes = getTemporalStereoTypesFromTopMostNonTopTypeGeneralizations(returnType, processorSupport);
        ListIterable<String> classTemporalStereotypes = stereotypes.collect(MILESTONINGSTEREOTYPE_TO_PURE_NAMED_STEREOTYPE_PAIR).collect(Functions.<String>firstOfPair());
        ListIterable<String> temporalDatePropertyNamesForStereotypes = stereotypes.flatCollect(GET_DATE_PROPERTY_NAME).distinct();
        return new MilestonedPropertyMetaData(classTemporalStereotypes, temporalDatePropertyNamesForStereotypes);
    }

    public static boolean isGeneratedQualifiedProperty(CoreInstance property, ProcessorSupport processorSupport)
    {
        return Instance.instanceOf(property, M3Paths.QualifiedProperty, processorSupport) && isGeneratedMilestoningProperty(property, processorSupport) && !isAllVersionsInRangeProperty(property, processorSupport);
    }

    public static boolean isGeneratedQualifiedPropertyWithWithAllMilestoningDatesSpecified(CoreInstance property, ProcessorSupport processorSupport)
    {
        boolean result = false;
        if (isGeneratedQualifiedProperty(property, processorSupport))
        {
            return getParametersCount((QualifiedProperty)property, processorSupport) == getCountOfParametersSatisfyingMilestoningDateRequirments((QualifiedProperty)property, processorSupport);
        }
        return result;
    }

    public static boolean isGeneratedMilestonedQualifiedPropertyWithMissingDates(CoreInstance property, ProcessorSupport processorSupport)
    {
        if (isGeneratedQualifiedProperty(property, processorSupport))
        {
            return getParametersCount((QualifiedProperty)property, processorSupport) != getCountOfParametersSatisfyingMilestoningDateRequirments((QualifiedProperty)property, processorSupport);
        }
        return false;
    }

    private static int getParametersCount(QualifiedProperty qualifiedProperty, ProcessorSupport processorSupport)
    {
        FunctionType functionType = (FunctionType)processorSupport.function_getFunctionType(qualifiedProperty);
        return functionType._parameters().size();
    }

    private static int getCountOfParametersSatisfyingMilestoningDateRequirments(QualifiedProperty milestonedQualifiedProperty, ProcessorSupport processorSupport)
    {
        if (!isGeneratedMilestoningProperty(milestonedQualifiedProperty, processorSupport))
        {
            throw new PureCompilationException("Unable to get milestoning date parameters for non milestoned QualifiedProperty: " + milestonedQualifiedProperty.getName());
        }
        Class returnType = (Class)org.finos.legend.pure.m3.navigation.importstub.ImportStub.withImportStubByPass(milestonedQualifiedProperty._genericType()._rawTypeCoreInstance(), processorSupport);
        MilestoningStereotypeEnum milestoningStereotype = getTemporalStereoTypesFromTopMostNonTopTypeGeneralizations(returnType, processorSupport).getFirst();
        return 1 + milestoningStereotype.getTemporalDatePropertyNames().size();
    }

    public static ListIterable<MilestoningStereotypeEnum> getTemporalStereoTypesFromTopMostNonTopTypeGeneralizations(CoreInstance cls, final ProcessorSupport processorSupport)
    {
        MutableSet<CoreInstance> leafUserDefTypes = Type.getTopMostNonTopTypeGeneralizations(cls, processorSupport);

        return leafUserDefTypes.flatCollect(new Function<CoreInstance, ListIterable<MilestoningStereotypeEnum>>()
        {
            @Override
            public ListIterable<MilestoningStereotypeEnum> valueOf(CoreInstance coreInstance)
            {
                return getTemporalStereoTypesExcludingParents(coreInstance, processorSupport);
            }
        }).toList().distinct();
    }

    public static ListIterable<String> getTemporalStereoTypePropertyNamesFromTopMostNonTopTypeGeneralizations(CoreInstance cls, ProcessorSupport processorSupport)
    {
        ListIterable<MilestoningStereotypeEnum> stereotypes = getTemporalStereoTypesFromTopMostNonTopTypeGeneralizations(cls, processorSupport);
        return stereotypes.flatCollect(GET_DATE_PROPERTY_NAME).distinct();
    }

    public static ListIterable<String> getAllTemporalStereoTypePropertyNamesFromTopMostNonTopTypeGeneralizations(CoreInstance cls, ProcessorSupport processorSupport)
    {
        return getTemporalStereoTypePropertyNamesFromTopMostNonTopTypeGeneralizations(cls, processorSupport).toList().with(MILESTONING).distinct();
    }

    public static ListIterable<MilestoningStereotypeEnum> getTemporalStereoTypesExcludingParents(CoreInstance cls, ProcessorSupport processorSupport)
    {
        ListIterable<? extends CoreInstance> clsStereotypes = cls instanceof ElementWithStereotypes ? ((ElementWithStereotypes)cls)._stereotypesCoreInstance().toList() : Lists.immutable.<CoreInstance>empty();
        ListIterable<String> clsStereotypeNames = clsStereotypes.collect(toStereotypeName(processorSupport));
        MutableList<Pair<String, MilestoningStereotypeEnum>> temporalStereoTypeNames = ArrayIterate.collect(MilestoningStereotypeEnum.values(), MILESTONINGSTEREOTYPE_TO_PURE_NAMED_STEREOTYPE_PAIR);
        return temporalStereoTypeNames.select(Predicates.attributePredicate(Functions.<String>firstOfPair(), Predicates.in(clsStereotypeNames))).collect(Functions.<MilestoningStereotypeEnum>secondOfPair());
    }

    public static String getEdgePointPropertyName(String propertyName)
    {
        return propertyName + EDGEPOINT_PROPERTY_NAME_SUFFIX;
    }

    public static String getRangePropertyName(String propertyName)
    {
        return propertyName + RANGE_PROPERTY_NAME_SUFFIX;
    }

    public static String getSourceEdgePointPropertyName(String propertyName)
    {
        return propertyName.replaceAll(EDGEPOINT_PROPERTY_NAME_SUFFIX, "");
    }

    static void setProperties(ListIterable<? extends CoreInstance> properties, PropertyOwner propertyOwner, Context context)
    {
        if (!properties.equals(PropertyOwnerStrategy.PROPERTY_OWNER_STRATEGY_FUNCTION.valueOf(propertyOwner).properties(propertyOwner)))
        {
            propertyOwner.setKeyValues(M3PropertyPaths.properties, properties);
            updateAndInvalidate(propertyOwner, context);
        }
    }

    static void setQualifiedProperties(ListIterable<? extends CoreInstance> properties, PropertyOwner propertyOwner, Context context)
    {
        if (!properties.equals(PropertyOwnerStrategy.PROPERTY_OWNER_STRATEGY_FUNCTION.valueOf(propertyOwner).qualifiedProperties(propertyOwner)))
        {
            propertyOwner.setKeyValues(M3PropertyPaths.qualifiedProperties, properties);
            updateAndInvalidate(propertyOwner, context);
        }
    }

    public static void updateAndInvalidate(CoreInstance ci, Context context)
    {
        context.update(ci);
        if (ci.hasBeenValidated())
        {
            ci.markNotValidated();
        }
    }
}
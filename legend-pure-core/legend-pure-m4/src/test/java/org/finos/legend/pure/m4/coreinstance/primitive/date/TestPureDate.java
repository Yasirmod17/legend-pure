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

package org.finos.legend.pure.m4.coreinstance.primitive.date;

import org.junit.Assert;
import org.junit.Test;

public class TestPureDate
{
    @Test
    public void testFormat()
    {
        PureDate date = DateFunctions.newPureDate(2014, 3, 10, 16, 12, 35, "070004235");
        Assert.assertEquals("2014", date.format("yyyy"));

        Assert.assertEquals("2014-3", date.format("yyyy-M"));
        Assert.assertEquals("2014-03", date.format("yyyy-MM"));
        Assert.assertEquals("2014-003", date.format("yyyy-MMM"));
        Assert.assertEquals("2014-03-10", date.format("yyyy-MM-d"));
        Assert.assertEquals("2014-03-10", date.format("yyyy-MM-dd"));
        Assert.assertEquals("2014-03-10 4:12:35PM", date.format("yyyy-MM-dd h:mm:ssa"));
        Assert.assertEquals("2014-03-10 16:12:35.070004235 GMT", date.format("yyyy-MM-dd HH:mm:ss.SSSS z"));
        Assert.assertEquals("2014-03-10T16:12:35.070004235+0000", date.format("yyyy-MM-dd\"T\"HH:mm:ss.SSSSZ"));
        Assert.assertEquals("2014-03-10 16:12:35.070Z", date.format("yyyy-MM-dd HH:mm:ss.SSSX"));
    }

    @Test
    public void testFormatWithTimeZoneShift()
    {
        PureDate date = DateFunctions.newPureDate(2014, 1, 1, 1, 1, 1, "070004235");
        Assert.assertEquals("2014-01-01 01:01:01.070+0000", date.format("yyyy-MM-dd HH:mm:ss.SSSZ"));
        Assert.assertEquals("2014-01-01 01:01:01.070 GMT", date.format("yyyy-MM-dd HH:mm:ss.SSS z"));
        Assert.assertEquals("2014-01-01 01:01:01.070Z", date.format("yyyy-MM-dd HH:mm:ss.SSSX"));

        Assert.assertEquals("2013-12-31 20:01:01.070-0500", date.format("[EST]yyyy-MM-dd HH:mm:ss.SSSZ"));
        Assert.assertEquals("2013-12-31 20:01:01.070 EST", date.format("[EST]yyyy-MM-dd HH:mm:ss.SSS z"));
        Assert.assertEquals("2013-12-31 20:01:01.070-05", date.format("[EST]yyyy-MM-dd HH:mm:ss.SSSX"));

        Assert.assertEquals("2013-12-31 19:01:01.070-0600", date.format("[CST]yyyy-MM-dd HH:mm:ss.SSSZ"));
        Assert.assertEquals("2013-12-31 19:01:01.070 CST", date.format("[CST]yyyy-MM-dd HH:mm:ss.SSS z"));
        Assert.assertEquals("2013-12-31 19:01:01.070-06", date.format("[CST]yyyy-MM-dd HH:mm:ss.SSSX"));

        Assert.assertEquals("2014-01-01 02:01:01.070+0100", date.format("[CET]yyyy-MM-dd HH:mm:ss.SSSZ"));
        Assert.assertEquals("2014-01-01 02:01:01.070 CET", date.format("[CET]yyyy-MM-dd HH:mm:ss.SSS z"));
        Assert.assertEquals("2014-01-01 02:01:01.070+01", date.format("[CET]yyyy-MM-dd HH:mm:ss.SSSX"));
        Assert.assertEquals("2014-01-01 02:01:01.070 CET", date.format("[\"CET\"]yyyy-MM-dd HH:mm:ss.SSS z"));

        // Longer time zone names
        Assert.assertEquals("2014-01-01 01:01:01.070+0000", date.format("[Europe/Lissabon]yyyy-MM-dd HH:mm:ss.SSSZ"));
        Assert.assertEquals("2014-01-01 01:01:01.070 Europe/Lissabon", date.format("[Europe/Lissabon]yyyy-MM-dd HH:mm:ss.SSS z"));
        Assert.assertEquals("2014-01-01 01:01:01.070Z", date.format("[Lissabon]yyyy-MM-dd HH:mm:ss.SSSX"));
        Assert.assertEquals("2014-01-01 01:01:01.070 Europe/Lissabon", date.format("[\"Europe/Lissabon\"]yyyy-MM-dd HH:mm:ss.SSS z"));

        Assert.assertEquals("2014-01-01 00:01:01.070-0100", date.format("[Atlantic/Azores]yyyy-MM-dd HH:mm:ss.SSSZ"));
        Assert.assertEquals("2014-01-01 00:01:01.070 Atlantic/Azores", date.format("[Atlantic/Azores]yyyy-MM-dd HH:mm:ss.SSS z"));
        Assert.assertEquals("2014-01-01 00:01:01.070-01", date.format("[Atlantic/Azores]yyyy-MM-dd HH:mm:ss.SSSX"));

        // Time zone is illegal after date or time has been formatted and written out
        Assert.assertEquals("Time zone can only be set at the beginning of the format string",
                Assert.assertThrows(IllegalArgumentException.class, () -> date.format("yyyy-MM-dd HH:mm:ss.SSS [CET]z")).getMessage());
        Assert.assertEquals("Time zone can only be set at the beginning of the format string",
                Assert.assertThrows(IllegalArgumentException.class, () -> date.format("yyyy-MM-dd [CET]HH:mm:ss.SSSz")).getMessage());
        Assert.assertEquals("Time zone can only be set at the beginning of the format string",
                Assert.assertThrows(IllegalArgumentException.class, () -> date.format("yyyy-MM-dd[CET] HH:mm:ss.SSSz")).getMessage());

        Assert.assertEquals("Fails when end quotes missing in time zone definition",
                "Missing closing quotes in time zone definition: [\"CET]yyyy-MM-dd HH:mm:ss.SSS z",
                Assert.assertThrows(IllegalArgumentException.class, () -> date.format("[\"CET]yyyy-MM-dd HH:mm:ss.SSS z")).getMessage());
    }

    @Test
    public void testFormatWithTimeZoneShiftButNoHour()
    {
        PureDate date = StrictDate.newStrictDate(2015, 8, 15);
        Assert.assertEquals("2015-08-15", date.format("yyyy-MM-dd"));
        Assert.assertEquals("2015-08-15", date.format("[EST]yyyy-MM-dd"));
        Assert.assertEquals("2015-08-15", date.format("[CST]yyyy-MM-dd"));
        Assert.assertEquals("2015-08-15", date.format("[CET]yyyy-MM-dd"));
    }

    @Test
    public void testFormatWithMultipleTimeZonesFails()
    {
        PureDate date = DateFunctions.newPureDate(2014, 1, 1, 1, 1, 1, "070004235");
        Assert.assertEquals("Fails when the time zones are the same",
                "Time zone can only be set at the beginning of the format string",
                Assert.assertThrows(IllegalArgumentException.class, () -> date.format("[EST]yyyy-MM-dd [EST]HH:mm:ss.SSSZ")).getMessage());
        Assert.assertEquals("Fails when the time zones are different",
                "Time zone can only be set at the beginning of the format string",
                Assert.assertThrows(IllegalArgumentException.class, () -> date.format("[EST]yyyy-MM-dd [CST] HH:mm:ss.SSSZ")).getMessage());
    }

    @Test
    public void testFormatRefersToNonexistentComponent()
    {
        PureDate date = DateFunctions.newPureDate(2014, 1, 1);
        IllegalArgumentException e = Assert.assertThrows(IllegalArgumentException.class, () -> date.format("[EST]yyyy-MM-dd HH:mm:ss.SSSZ"));
        Assert.assertEquals("Date has no hour: 2014-01-01", e.getMessage());
    }

    @Test
    public void testInvalidSubseconds()
    {
        IllegalArgumentException e1 = Assert.assertThrows(IllegalArgumentException.class, () -> DateFunctions.newPureDate(2016, 5, 17, 10, 26, 33, null));
        Assert.assertEquals("Invalid subsecond value: null", e1.getMessage());

        IllegalArgumentException e2 = Assert.assertThrows(IllegalArgumentException.class, () -> DateFunctions.newPureDate(2016, 5, 17, 10, 26, 33, ""));
        Assert.assertEquals("Invalid subsecond value: \"\"", e2.getMessage());

        IllegalArgumentException e3 = Assert.assertThrows(IllegalArgumentException.class, () -> DateFunctions.newPureDate(2016, 5, 17, 10, 26, 33, "789as9898"));
        Assert.assertEquals("Invalid subsecond value: \"789as9898\"", e3.getMessage());

        IllegalArgumentException e4 = Assert.assertThrows(IllegalArgumentException.class, () -> DateFunctions.newPureDate(2016, 5, 17, 10, 26, 33, "-789"));
        Assert.assertEquals("Invalid subsecond value: \"-789\"", e4.getMessage());
    }

    @Test
    public void testAddYears()
    {
        Assert.assertEquals(DateFunctions.newPureDate(2017, 2, 28), DateFunctions.newPureDate(2016, 2, 29).addYears(1));
        Assert.assertEquals(DateFunctions.newPureDate(2020, 2, 29), DateFunctions.newPureDate(2016, 2, 29).addYears(4));

        RuntimeException e = Assert.assertThrows(RuntimeException.class, () -> DateFunctions.newPureDate(2016, 2, 29).addYears(12345678912L));
        Assert.assertEquals("Date incremented beyond supported bounds", e.getMessage());
    }

    @Test
    public void testAddMonths()
    {
        Assert.assertEquals(DateFunctions.newPureDate(2017, 2, 28), DateFunctions.newPureDate(2017, 1, 31).addMonths(1));
        Assert.assertEquals(DateFunctions.newPureDate(2020, 2, 29), DateFunctions.newPureDate(2020, 1, 31).addMonths(1));
        Assert.assertEquals(DateFunctions.newPureDate(2017, 3, 31), DateFunctions.newPureDate(2017, 1, 31).addMonths(2));
        Assert.assertEquals(DateFunctions.newPureDate(2017, 4, 30), DateFunctions.newPureDate(2017, 1, 31).addMonths(3));

        Assert.assertEquals(DateFunctions.newPureDate(2010, 1, 29), DateFunctions.newPureDate(2012, 2, 29).addMonths(-25));
    }

    @Test
    public void testAddDays()
    {
        Assert.assertEquals(DateFunctions.newPureDate(2017, 3, 1), DateFunctions.newPureDate(2017, 2, 28).addDays(1));
        Assert.assertEquals(DateFunctions.newPureDate(2020, 2, 29), DateFunctions.newPureDate(2020, 2, 28).addDays(1));
        Assert.assertEquals(DateFunctions.newPureDate(2015, 3, 30), DateFunctions.newPureDate(2015, 4, 16).addDays(-17));
        Assert.assertEquals(DateFunctions.newPureDate(2015, 3, 30), DateFunctions.newPureDate(2014, 3, 30).addDays(365));
        Assert.assertEquals(DateFunctions.newPureDate(2013, 3, 30), DateFunctions.newPureDate(2014, 3, 30).addDays(-365));
    }

    @Test
    public void testAddMilliseconds()
    {
        PureDate date = DateFunctions.newPureDate(2016, 5, 17, 10, 26, 33, "780013429");

        Assert.assertSame(date, date.addMilliseconds(0));

        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 34, "779013429"), date.addMilliseconds(999));
        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 32, "781013429"), date.addMilliseconds(-999));

        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 33, "783013429"), date.addMilliseconds(3));
        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 33, "777013429"), date.addMilliseconds(-3));

        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 34, "780013429"), date.addMilliseconds(1000));
        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 32, "780013429"), date.addMilliseconds(-1000));
        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 43, "780013429"), date.addMilliseconds(10000));
        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 23, "780013429"), date.addMilliseconds(-10000));
        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 44, "603013429"), date.addMilliseconds(10823));
        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 22, "957013429"), date.addMilliseconds(-10823));
    }

    @Test
    public void testAddMicroseconds()
    {
        PureDate date = DateFunctions.newPureDate(2016, 5, 17, 10, 26, 33, "780013429");

        Assert.assertSame(date, date.addMicroseconds(0));

        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 33, "781012429"), date.addMicroseconds(999));
        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 33, "779014429"), date.addMicroseconds(-999));

        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 33, "780016429"), date.addMicroseconds(3));
        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 33, "780010429"), date.addMicroseconds(-3));

        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 33, "781013429"), date.addMicroseconds(1000));
        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 33, "779013429"), date.addMicroseconds(-1000));
        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 33, "790013429"), date.addMicroseconds(10000));
        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 33, "770013429"), date.addMicroseconds(-10000));
        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 33, "790836429"), date.addMicroseconds(10823));
        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 33, "769190429"), date.addMicroseconds(-10823));

        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 34, "780013429"), date.addMicroseconds(1_000_000));
        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 32, "780013429"), date.addMicroseconds(-1_000_000));
        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 43, "780013429"), date.addMicroseconds(10_000_000));
        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 23, "780013429"), date.addMicroseconds(-10_000_000));
        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 44, "603014429"), date.addMicroseconds(10_823_001));
        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 22, "957012429"), date.addMicroseconds(-10_823_001));
    }

    @Test
    public void testAddNanoseconds()
    {
        PureDate date = DateFunctions.newPureDate(2016, 5, 17, 10, 26, 33, "780013429");

        Assert.assertSame(date, date.addNanoseconds(0));

        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 33, "780014428"), date.addNanoseconds(999));
        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 33, "780012430"), date.addNanoseconds(-999));

        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 33, "780013432"), date.addNanoseconds(3));
        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 33, "780013426"), date.addNanoseconds(-3));

        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 33, "780014429"), date.addNanoseconds(1000));
        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 33, "780012429"), date.addNanoseconds(-1000));
        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 33, "780023429"), date.addNanoseconds(10000));
        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 33, "780003429"), date.addNanoseconds(-10000));
        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 33, "780024252"), date.addNanoseconds(10823));
        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 33, "780002606"), date.addNanoseconds(-10823));

        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 34, "780013429"), date.addNanoseconds(1_000_000_000));
        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 32, "780013429"), date.addNanoseconds(-1_000_000_000));

        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 43, "780013429"), date.addNanoseconds(10_000_000_000L));
        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 23, "780013429"), date.addNanoseconds(-10_000_000_000L));
        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 44, "603013430"), date.addNanoseconds(10_823_000_001L));
        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 22, "957013428"), date.addNanoseconds(-10_823_000_001L));

        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 18, 10, 26, 33, "780013430"), date.addNanoseconds(86_400_000_000_001L));
        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 16, 10, 26, 33, "780013428"), date.addNanoseconds(-86_400_000_000_001L));

        Assert.assertEquals(DateFunctions.newPureDate(2016, 6, 6, 10, 26, 36, "780013430"), date.addNanoseconds(1_728_003_000_000_001L));
        Assert.assertEquals(DateFunctions.newPureDate(2016, 4, 27, 10, 26, 30, "780013428"), date.addNanoseconds(-1_728_003_000_000_001L));

        Assert.assertEquals(DateFunctions.newPureDate(2021, 11, 7, 10, 26, 36, "780013430"), date.addNanoseconds(172_800_003_000_000_001L));
        Assert.assertEquals(DateFunctions.newPureDate(2010, 11, 25, 10, 26, 30, "780013428"), date.addNanoseconds(-172_800_003_000_000_001L));
    }

    @Test
    public void testAddSubseconds()
    {
        PureDate date = DateFunctions.newPureDate(2016, 5, 17, 10, 26, 33, "780013429");

        Assert.assertSame(date, date.addSubseconds("0"));
        Assert.assertSame(date, date.addSubseconds("00000000000000000000000000000000000"));

        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 33, "880013429"), date.addSubseconds("1"));
        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 33, "880013429"), date.addSubseconds("1000000"));

        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 34, "580013429"), date.addSubseconds("8"));

        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 33, "790013429"), date.addSubseconds("01"));
        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 33, "800013429"), date.addSubseconds("02"));

        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 34, "779013429"), date.addSubseconds("999"));
        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 33, "783013429"), date.addSubseconds("003"));

        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 34, "582213500"), date.addSubseconds("802200071"));
    }

    @Test
    public void testSubtractSubseconds()
    {
        PureDate date = DateFunctions.newPureDate(2016, 5, 17, 10, 26, 33, "780013429");

        Assert.assertSame(date, date.subtractSubseconds("0"));
        Assert.assertSame(date, date.subtractSubseconds("00000000000000000000000000000000000"));

        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 33, "680013429"), date.subtractSubseconds("1"));
        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 33, "680013429"), date.subtractSubseconds("1000000"));

        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 32, "980013429"), date.subtractSubseconds("8"));

        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 33, "770013429"), date.subtractSubseconds("01"));
        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 33, "760013429"), date.subtractSubseconds("02"));

        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 32, "781013429"), date.subtractSubseconds("999"));
        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 33, "777013429"), date.subtractSubseconds("003"));

        Assert.assertEquals(DateFunctions.newPureDate(2016, 5, 17, 10, 26, 32, "977813358"), date.subtractSubseconds("802200071"));
    }
}

/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.common.time;

import org.elasticsearch.test.ESTestCase;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

public class DateFormattersTests extends ESTestCase {

    // the epoch milli parser is a bit special, as it does not use date formatter, see comments in DateFormatters
    public void testEpochMilliParser() {
        CompoundDateTimeFormatter formatter = DateFormatters.forPattern("epoch_millis");

        DateTimeParseException e = expectThrows(DateTimeParseException.class, () -> formatter.parse("invalid"));
        assertThat(e.getMessage(), containsString("invalid number"));

        // different zone, should still yield the same output, as epoch is time zoned independent
        ZoneId zoneId = randomZone();
        CompoundDateTimeFormatter zonedFormatter = formatter.withZone(zoneId);
        assertThat(zonedFormatter.printer.getZone(), is(zoneId));

        // test with negative and non negative values
        assertThatSameDateTime(formatter, zonedFormatter, randomNonNegativeLong() * -1);
        assertThatSameDateTime(formatter, zonedFormatter, randomNonNegativeLong());
        assertThatSameDateTime(formatter, zonedFormatter, 0);
        assertThatSameDateTime(formatter, zonedFormatter, -1);
        assertThatSameDateTime(formatter, zonedFormatter, 1);

        // format() output should be equal as well
        assertSameFormat(formatter, randomNonNegativeLong() * -1);
        assertSameFormat(formatter, randomNonNegativeLong());
        assertSameFormat(formatter, 0);
        assertSameFormat(formatter, -1);
        assertSameFormat(formatter, 1);
    }

    private void assertThatSameDateTime(CompoundDateTimeFormatter formatter, CompoundDateTimeFormatter zonedFormatter, long millis) {
        String millisAsString = String.valueOf(millis);
        ZonedDateTime formatterZonedDateTime = DateFormatters.toZonedDateTime(formatter.parse(millisAsString));
        ZonedDateTime zonedFormatterZonedDateTime = DateFormatters.toZonedDateTime(zonedFormatter.parse(millisAsString));
        assertThat(formatterZonedDateTime.toInstant().toEpochMilli(), is(zonedFormatterZonedDateTime.toInstant().toEpochMilli()));
    }

    private void assertSameFormat(CompoundDateTimeFormatter formatter, long millis) {
        String millisAsString = String.valueOf(millis);
        TemporalAccessor accessor = formatter.parse(millisAsString);
        assertThat(millisAsString, is(formatter.format(accessor)));
    }
}

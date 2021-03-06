/*
 * Copyright 2017 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.gov.gchq.gaffer.sketches.datasketches.frequencies.function.aggregate;

import com.yahoo.memory.NativeMemory;
import com.yahoo.sketches.ArrayOfStringsSerDe;
import com.yahoo.sketches.frequencies.ItemsSketch;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import uk.gov.gchq.gaffer.function.SimpleAggregateFunction;
import uk.gov.gchq.gaffer.function.annotation.Inputs;
import uk.gov.gchq.gaffer.function.annotation.Outputs;

/**
 * A <code>StringsSketchAggregator</code> is a {@link SimpleAggregateFunction} that takes in
 * {@link ItemsSketch}s of {@link String}s and merges them together using {@link ItemsSketch#merge(ItemsSketch)}.
 *
 * NB: We cannot provide a generic aggregator for any type T as we need to clone the first sketch that is
 * supplied to the <code>_aggregate</code> method and that requires serialising and deserialising which
 * requires a specific serialiser.
 */
@Inputs(ItemsSketch.class)
@Outputs(ItemsSketch.class)
public class StringsSketchAggregator extends SimpleAggregateFunction<ItemsSketch<String>> {
    private static final ArrayOfStringsSerDe SERIALISER = new ArrayOfStringsSerDe();
    private ItemsSketch<String> sketch;

    @Override
    public void init() {
    }

    @Override
    protected void _aggregate(final ItemsSketch<String> input) {
        if (input != null) {
            if (sketch == null) {
                // It would be better to create a new ItemsSketch using something like
                // sketch = new LongsSketch(input.getMaximumMapCapacity());
                // but this doesn't work as getMaximumMapCapacity() doesn't return the right parameter
                // to pass to the constructor. We don't want to just set sketch = input as we want
                // to clone input. Instead we have to pay the expense of creating a new one
                // by deserialising.
                sketch = ItemsSketch.getInstance(new NativeMemory(input.toByteArray(SERIALISER)), SERIALISER);
            } else {
                sketch.merge(input);
            }
        }
    }

    @Override
    protected ItemsSketch<String> _state() {
        return sketch;
    }

    @Override
    public StringsSketchAggregator statelessClone() {
        final StringsSketchAggregator clone = new StringsSketchAggregator();
        clone.init();
        return clone;
    }

    /**
     * As an {@link ItemsSketch} does not have an <code>equals()</code> method, the serialised form of the extracted
     * {@link ItemsSketch} is used.
     *
     * @param o the object to test
     * @return true if o equals this object
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final StringsSketchAggregator that = (StringsSketchAggregator) o;
        final byte[] serialisedUnion = sketch == null ? null : sketch.toByteArray(SERIALISER);
        final byte[] thatSerialisedUnion = that.sketch == null ? null : that.sketch.toByteArray(SERIALISER);

        return new EqualsBuilder()
                .append(inputs, that.inputs)
                .append(outputs, that.outputs)
                .append(serialisedUnion, thatSerialisedUnion)
                .isEquals();
    }

    @Override
    public int hashCode() {
        final byte[] serialisedSketch = sketch == null ? null : sketch.toByteArray(SERIALISER);

        if (serialisedSketch != null) {
            return new HashCodeBuilder(17, 37)
                    .append(inputs)
                    .append(outputs)
                    .append(serialisedSketch)
                    .toHashCode();
        } else {
            return new HashCodeBuilder(17, 37)
                    .append(inputs)
                    .append(outputs)
                    .toHashCode();
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("inputs", inputs)
                .append("outputs", outputs)
                .append("sketch", sketch)
                .toString();
    }
}

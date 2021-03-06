/*
 * Copyright 2016 Crown Copyright
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

package uk.gov.gchq.gaffer.function;

import uk.gov.gchq.gaffer.function.annotation.Inputs;
import uk.gov.gchq.gaffer.function.annotation.Outputs;

@Inputs({Object.class, Object.class})
@Outputs(String.class)
public class ExampleTransformFunction extends TransformFunction {
    @Override
    public Object[] transform(final Object[] input) {
        return new Object[]{input[0].toString() + input[1] + " transformed"};
    }

    @Override
    public ExampleTransformFunction statelessClone() {
        return new ExampleTransformFunction();
    }
}
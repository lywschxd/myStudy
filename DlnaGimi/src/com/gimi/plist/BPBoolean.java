/*
 * Copyright 2011 Daniel Rendall
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

package com.gimi.plist;


/**
 * Represents a boolean value in a plist. There are only two, therefore they're
 * available as immutable constants TRUE and FALSE.
 */
public class BPBoolean extends BPItem {

    public final static BPBoolean TRUE = new BPBoolean(true);
    public final static BPBoolean FALSE = new BPBoolean(false);

    public static BPBoolean get(boolean value) {
        return value ? TRUE : FALSE;
    }

    private final boolean value;

    private BPBoolean(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BPBoolean bpBoolean = (BPBoolean) o;

        if (value != bpBoolean.value) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (value ? 1 : 0);
    }

    @Override
    public String toString() {
        return Boolean.toString(value);
    }

    @Override
    public Type getType() {
        return Type.Boolean;
    }

    @Override
    public void accept(BPVisitor visitor) {
        visitor.visit(this);
    }
}

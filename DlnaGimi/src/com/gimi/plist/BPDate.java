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

import android.renderscript.Type;


/**
 * Represents a Date in a plist - NOT YET IMPLEMENTED!
 */
public class BPDate extends BPItem {
    
    private final byte[] data;

    public BPDate(byte[] data) {
        this.data = data;
        log.warning("BPDate not yet implemented");
    }

    @Override
    public String toString() {
        return "BPDate{" +
                "data=" + data +
                '}';
    }

    @Override
    public Type getType() {
        return Type.Date;
    }

    @Override
    public void accept(BPVisitor visitor) {
        visitor.visit(this);
    }
}

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
 * Represents the unique Null value as the static field Instance.
 */
public class BPNull extends BPItem {

    public final static BPNull Instance = new BPNull();
    private BPNull() {};


    @Override
    public String toString() {
        return "NULL";
    }
    
    @Override
    public Type getType() {
        return Type.Null;
    }

    @Override
    public void accept(BPVisitor visitor) {
        visitor.visit(this);
    }
}

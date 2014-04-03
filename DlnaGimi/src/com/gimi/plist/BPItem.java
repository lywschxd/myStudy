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


import java.util.Map;
import java.util.logging.Logger;

/**
 * Base class for all things which can be stored in a binary plist.
 */
public abstract class BPItem {
    protected final static Logger log = Logger.getLogger(BPItem.class.getSimpleName());

    /**
     * An enumeration of the different types of BPItem. Subclasses will return one of
     * these in response to getType()
     */
    public enum Type {Array, Boolean, Data, Date, Dict, Int, Null, Real, Set, String, Uid}

    void expand(BinaryPlistDecoder decoder) throws BinaryPlistException {
        // nothing to do
    }

    /**
     * Whether or not the current BPItem can be the root item in a plist - true only for
     * collection types BPDict, BPSet and BPArray
     */
    boolean canBeRoot() {
        return false;
    }

    /**
     * Whether this item has been expanded during a decoding pass. Used for internal
     * housekeeping during deserialization, external clients should not need to worry 
     * about the state of this.
     */
    boolean isExpanded() {
        return true;
    }

    /**
     * Returns the type of this BPItem from the Type enumeration. Useful for avoiding
     * nasty instanceof comparisons.
     */
    public abstract Type getType();
    
    /**
     * Implements the Visitor pattern (see GOF book) to allow operations on a BPlist
     * structure.
     */
    public abstract void accept(BPVisitor visitor);
}

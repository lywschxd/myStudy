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
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents an Int in a plist. Plists allow for ints to be both signed and unsigned, and
 * anything from -(2^63) to 2^63-1 (signed) or 0 to 2^64 - 1 (unsigned). This means that 
 * their representation in Java is a bit of a mess, since Java just has int and long types,
 * both of which are signed.
 *
 * So, this class stores both an int value and a long value, a size (Int or Long) which tells
 * you which value is correct, and an additional isUnsigned flag which comes into play when
 * representing numbers out of the range of Java's long type.
 *
 * Once created, BPInt values are immutable. However, two BPInts with the same value will be
 * two separate objects - there's no interning. This could change (e.g. we could maintain a
 * cache of most commonly used values) but there's a time - memory trade-off involved and
 * unless a program uses a lot of plists, it's probably not worth the extra complexity.
 *
 * TODO: Document examples. Also consider the TODO in the from() method about returning
 * unsigned int values between 2^31 and 2^32 - 1
 */
public class BPInt extends BPItem {
    public enum Size {Int, Long}

    private final int iValue;
    private final long lValue;
    private final Size size;
    private final boolean isUnsigned;

    public static BPInt get(int value) {
        return new BPInt(value);
    }

    public static BPInt get(long value) {
        return new BPInt(value, false);
    }

    public static BPInt from(byte[] bytes) {
        // note - all negative numbers are represented by 8 bytes.
        int size = bytes.length;
        boolean highestBitIsSet = ((0x80 & bytes[0]) == 128);
        if (size == 1) {
            return new BPInt(0xff & bytes[0]);
        } else if (size == 2) {
            return new BPInt(((0xff & bytes[0]) << 8) | (0xff & bytes[1]));
        } else if (size == 4) {
            if (highestBitIsSet) {
                // in this case, we have a number between 2^31 and 2^32 - 1. This won't fit in an int, so we have to force it to be a long
                // TODO - should we return it as a number of type Int, but marked as isUnsigned, meaning that
                // clients should use the long value instead? That might be more consistent.
                return new BPInt(((0xff & (long)bytes[0]) << 24) | ((0xff & (long)bytes[1]) << 16) | ((0xff & (long)bytes[2]) << 8) | (0xff & (long)bytes[3]), false);
            } else {
                return new BPInt(((0xff & bytes[0]) << 24) | ((0xff & bytes[1]) << 16) | ((0xff & bytes[2]) << 8) | (0xff & bytes[3]));
            }
        } else if (size == 8) {
            if (highestBitIsSet) {
                long msb = (long) (((0xff & (long)~bytes[0]) << 24) | ((0xff & (long)~bytes[1]) << 16) | ((0xff & (long)~bytes[2]) << 8) | (0xff & (long)~bytes[3]));
                long lsb = (long) (((0xff & (long)~bytes[4]) << 24) | ((0xff & (long)~bytes[5]) << 16) | ((0xff & (long)~bytes[6]) << 8) | (0xff & (long)~bytes[7]));
                long value = (-((msb << 32) + lsb))-1;
                return (value < Integer.MIN_VALUE) ? new BPInt(value, false) : new BPInt((int)value);
            } else {
                long msb = (long) (((0xff & (long)bytes[0]) << 24) | ((0xff & (long)bytes[1]) << 16) | ((0xff & (long)bytes[2]) << 8) | (0xff & (long)bytes[3]));
                long lsb = (long) (((0xff & (long)bytes[4]) << 24) | ((0xff & (long)bytes[5]) << 16) | ((0xff & (long)bytes[6]) << 8) | (0xff & (long)bytes[7]));
                return new BPInt((msb << 32) + lsb, false);
            }
        } else {
            // size = 16 - this is used to represent unsigned ints between 2^63 and 2^64 - 1 so they
            // can be distinguished from signed ints between -(2^63) and 0.
            long msb = (long) (((0xff & (long)bytes[8]) << 24) | ((0xff & (long)bytes[9]) << 16) | ((0xff & (long)bytes[10]) << 8) | (0xff & (long)bytes[11]));
            long lsb = (long) (((0xff & (long)bytes[12]) << 24) | ((0xff & (long)bytes[13]) << 16) | ((0xff & (long)bytes[14]) << 8) | (0xff & (long)bytes[15]));
            return new BPInt((msb << 32) + lsb, true);
        }
    }

    private BPInt(int iValue) {
        this.iValue = iValue;
        this.lValue = iValue;
        this.isUnsigned = false;
        this.size = Size.Int;
    }

    private BPInt(long lValue, boolean isUnsigned) {
        this.iValue = (int) lValue;
        this.lValue = lValue;
        this.isUnsigned = isUnsigned;
        this.size = Size.Long;
    }

    public int getValue() {
        return iValue;
    }

    public long getLongValue() {
        return lValue;
    }

    public Size getSize() {
        return size;
    }
    
    public boolean isUnsigned() {
        return isUnsigned;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BPInt bpInt = (BPInt) o;

        if (lValue != bpInt.lValue) return false;

        return true;
    }

    @Override
    public String toString() {
        return (size == Size.Long) ? Long.toString(lValue) : Integer.toString(iValue);
    }

    @Override
    public int hashCode() {
        return (size == Size.Long) ? (int)(lValue ^ (lValue >>> 32 )) : iValue;
    }

    @Override
    public Type getType() {
        return Type.Int;
    }

    @Override
    public void accept(BPVisitor visitor) {
        visitor.visit(this);
    }

}

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
 * Represents a Real value - NOT YET IMPLEMENTED
 */
public class BPReal extends BPItem {
    public enum Size {Float, Double}

    private final float fValue;
    private final double dValue;
    private final Size size;
    private final boolean isUnsigned;


    private BPReal(float fValue) {
        this.fValue = fValue;
        this.dValue = fValue;
        this.isUnsigned = false;
        this.size = Size.Float;
    }

    private BPReal(double dValue, boolean isUnsigned) {
        this.fValue = (float) dValue;
        this.dValue = dValue;
        this.isUnsigned = false;
        this.size = Size.Double;
    }

    public static BPReal from(byte[] bytes) {
        // note - all negative numbers are represented by 8 bytes.
        int size = bytes.length;
        boolean highestBitIsSet = ((0x80 & bytes[0]) == 128);
        if (size == 1) {
//            return new BPReal(0xff & bytes[0]);
            log.warning("Size is 1");
        } else if (size == 2) {
//            return new BPReal(((0xff & bytes[0]) << 8) | (0xff & bytes[1]));
            log.warning("Size is 2");
        } else if (size == 4) {
            if (highestBitIsSet) {
                // in this case, we have a number between 2^31 and 2^32 - 1. This won't fit in an int, so we have to force it to be a long
                // TODO - should we return it as a number of type Int, but marked as isUnsigned, meaning that
                // clients should use the long value instead? That might be more consistent.
//                return new BPReal(((0xff & (long)bytes[0]) << 24) | ((0xff & (long)bytes[1]) << 16) | ((0xff & (long)bytes[2]) << 8) | (0xff & (long)bytes[3]), false);
                log.warning("Size is 4, highest bit set");
            } else {
//                return new BPReal(((0xff & bytes[0]) << 24) | ((0xff & bytes[1]) << 16) | ((0xff & bytes[2]) << 8) | (0xff & bytes[3]));
                log.warning("Size is 4, highest bit NOT set");
            }
        } else if (size == 8) {
            if (highestBitIsSet) {
//                long msb = (long) (((0xff & (long)~bytes[0]) << 24) | ((0xff & (long)~bytes[1]) << 16) | ((0xff & (long)~bytes[2]) << 8) | (0xff & (long)~bytes[3]));
//                long lsb = (long) (((0xff & (long)~bytes[4]) << 24) | ((0xff & (long)~bytes[5]) << 16) | ((0xff & (long)~bytes[6]) << 8) | (0xff & (long)~bytes[7]));
//                long value = (-((msb << 32) + lsb))-1;
//                return (value < Integer.MIN_VALUE) ? new BPInt(value, false) : new BPInt((int)value);
                log.warning("Size is 8, highest bit set");
            } else {
                log.warning("Size is 8, highest bit NOT set");
                long msb = (long) (((0xff & (long)bytes[0]) << 24) | ((0xff & (long)bytes[1]) << 16) | ((0xff & (long)bytes[2]) << 8) | (0xff & (long)bytes[3]));
                long lsb = (long) (((0xff & (long)bytes[4]) << 24) | ((0xff & (long)bytes[5]) << 16) | ((0xff & (long)bytes[6]) << 8) | (0xff & (long)bytes[7]));
                return new BPReal(Double.longBitsToDouble((msb << 32) + lsb), false);
//                return new BPReal((msb << 32) + lsb, false);
            }
        } else {
            // size = 16 - this is used to represent unsigned ints between 2^63 and 2^64 - 1 so they
            // can be distinguished from signed ints between -(2^63) and 0.
//            long msb = (long) (((0xff & (long)bytes[8]) << 24) | ((0xff & (long)bytes[9]) << 16) | ((0xff & (long)bytes[10]) << 8) | (0xff & (long)bytes[11]));
//            long lsb = (long) (((0xff & (long)bytes[12]) << 24) | ((0xff & (long)bytes[13]) << 16) | ((0xff & (long)bytes[14]) << 8) | (0xff & (long)bytes[15]));
//            return new BPReal((msb << 32) + lsb, true);
            log.warning("Size is 16");
        }
        return new BPReal(0.0f);
    }

    public float getFloatValue() {
        return fValue;
    }

    public double getDoubleValue() {
        return dValue;
    }

    public Size getSize() {
        return size;
    }

    public boolean isUnsigned() {
        return isUnsigned;
    }

    @Override
    public String toString() {
        return Double.toString(dValue);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BPReal bpReal = (BPReal) o;

        if (Double.compare(bpReal.dValue, dValue) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        long temp = dValue != +0.0d ? Double.doubleToLongBits(dValue) : 0L;
        return (int) (temp ^ (temp >>> 32));
    }

    @Override
    public Type getType() {
        return Type.Real;
    }

    @Override
    public void accept(BPVisitor visitor) {
        visitor.visit(this);
    }
}


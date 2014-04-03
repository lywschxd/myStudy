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

import java.nio.ByteBuffer;

/**
 * Class which will return offsets read from a ByteBuffer. Three implementations are
 * available, depending on whether the ByteBuffer is representing offsets as bytes,
 * shorts or ints.
 *
 * TODO: This presumably advances a pointer in the ByteBuffer when getOffset is called
 * - document this behaviour explicitly.
 */
abstract class BinaryPlistOffsetReader {

    static BinaryPlistOffsetReader create(int byteSize) throws BinaryPlistException {
        switch (byteSize) {
            case 1:
                return new BinaryPlistOffsetReader1();
            case 2:
                return new BinaryPlistOffsetReader2();
            case 4:
                return new BinaryPlistOffsetReader4();
            default:
                throw new BinaryPlistException("Can't cope with " + byteSize + " size ints");
        }
    }

    abstract int getOffset(ByteBuffer bytes);


    private static class BinaryPlistOffsetReader1 extends BinaryPlistOffsetReader {
        @Override
        int getOffset(ByteBuffer bytes) {
            return 0x000000ff & bytes.get();
        }
    }

    private static class BinaryPlistOffsetReader2 extends BinaryPlistOffsetReader {
        @Override
        int getOffset(ByteBuffer bytes) {
            return 0x0000ffff & bytes.getShort();
        }
    }

    private static class BinaryPlistOffsetReader4 extends BinaryPlistOffsetReader {
        @Override
        int getOffset(ByteBuffer bytes) {
            return (int) bytes.getInt();
        }
    }
}

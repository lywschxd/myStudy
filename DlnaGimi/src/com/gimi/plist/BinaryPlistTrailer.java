package com.gimi.plist;

import java.nio.ByteBuffer;

public class BinaryPlistTrailer {

	private final short sortVersion;
    private final short offsetIntSize;
    private final short objectRefSize;
    private final long numObjects;
    private final long topObject;
    private final long offsetTableOffset;
    
	static BinaryPlistTrailer build(byte[] data) {
		ByteBuffer bytes = ByteBuffer.wrap(data);
		bytes.position(5);
		short _sortVersion = bytes.get();
		short _offsetIntSize = bytes.get();
		short _objectRefSize = bytes.get();
		long _numObjects = bytes.getLong();
		long _topObject = bytes.getLong();
		long _offsetTableOffset = bytes.getLong();
		return new BinaryPlistTrailer(_sortVersion, _offsetIntSize,
				_objectRefSize, _numObjects, _topObject, _offsetTableOffset);
	}
	
	private BinaryPlistTrailer(short sortVersion, short offsetIntSize,
			short objectRefSize, long numObjects, long topObject,
			long offsetTableOffset) {
		// To change body of created methods use File | Settings | File
		// Templates.
		this.sortVersion = sortVersion;
		this.offsetIntSize = offsetIntSize;
		this.objectRefSize = objectRefSize;
		this.numObjects = numObjects;
		this.topObject = topObject;
		this.offsetTableOffset = offsetTableOffset;
	}
	
	long getOffsetTableOffset() {
        return offsetTableOffset;
    }
	
	short getOffsetIntSize() {
		return offsetIntSize;
	}
	
	short getObjectRefSize() {
        return objectRefSize;
    }
	
	long getTopObject() {
        return topObject;
    }
	
}

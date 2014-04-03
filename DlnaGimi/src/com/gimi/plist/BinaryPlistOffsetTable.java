package com.gimi.plist;

public class BinaryPlistOffsetTable {
	private final int[] offsets;
	private final int size;

	BinaryPlistOffsetTable(int[] offsets) {
		this.offsets = offsets;
		this.size = offsets.length;
	}

	static BinaryPlistOffsetTable build(byte[] data, int offsetSize)
			throws BinaryPlistException {
		if (data.length % offsetSize != 0) {
			throw new BinaryPlistException(
					"Length of data not commensurate with offset size");
		}
		final int numberOfOffsets = data.length / offsetSize;
		int[] offsetArray = new int[numberOfOffsets];
		byte[] buffer = new byte[offsetSize];

		for (int i = 0; i < numberOfOffsets; i++) {
			System.arraycopy(data, i * offsetSize, buffer, 0, offsetSize);
			offsetArray[i] = BPInt.from(buffer).getValue() - 8; // offsets
																// relative to
																// start of data
		}
		
		return new BinaryPlistOffsetTable(offsetArray);
	}
	 
	int get(int offset) {
        return offsets[offset];
    }

    int get(long offset) {
        return offsets[(int)offset];
    }
}

package com.gimi.plist;

import java.nio.ByteBuffer;

public class BinaryPlistHeader {
	
	public final static long MAGIC_1 = 0x62706c69; // bpli
    public final static long MAGIC_2 = 0x73743030; // st..
    
    private final int fileFormatVersion;
    
	private BinaryPlistHeader(int fileFormatVersion) {
		this.fileFormatVersion = fileFormatVersion;
	}

	static BinaryPlistHeader build(byte[] data) throws BinaryPlistException {
		ByteBuffer bytes = ByteBuffer.wrap(data);
		long first = bytes.getInt();
		long second = bytes.getInt();
		
		if ((first != MAGIC_1) || (second != MAGIC_2)) {
//			log.warning("Magic numbers wrong - were "
//					+ formatter.format("%1$2x %2$2x", first, second));
			throw new BinaryPlistException("Bad magic number");
		}
		
		return new BinaryPlistHeader((int) (second & 0x0000ffff));
	}
	
	int getFileFormatVersion() {
		return fileFormatVersion;
	}
	
}

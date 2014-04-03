package com.gimi.plist;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Logger;

public class BinaryPlistDecoder {
	private final BinaryPlistOffsetTable offsetTable;

	private final BinaryPlistHeader header;
	private final BinaryPlistOffsetReader offsetReader;
	private final BinaryPlistTrailer trailer;
	private final ByteBuffer data;

	BinaryPlistDecoder(BinaryPlistHeader header, BinaryPlistTrailer trailer,
			byte[] data, BinaryPlistOffsetTable offsetTable,
			BinaryPlistOffsetReader offsetReader) {
		this.header = header;
		this.data = ByteBuffer.wrap(data);
		this.offsetTable = offsetTable;
		this.trailer = trailer;
		this.offsetReader = offsetReader;
	}

	private final static Logger log = Logger.getLogger(BinaryPlistDecoder.class.getSimpleName());
	private final Map<Integer, BPItem> items = new HashMap<Integer, BPItem>(); // offset,
	private final Queue<Integer> offsetsToExpand = new LinkedList<Integer>();

	BPItem decode() throws BinaryPlistException {

		BPItem item = getItemAtIndex((int) (trailer.getTopObject()));

		while (offsetsToExpand.peek() != null) {
			int offset = offsetsToExpand.poll();
			BPItem itemToExpand = items.get(offset);
			itemToExpand.expand(this);
		}

		return item;

	}

	BPItem getItemAtIndex(int index) {
		return getItem(offsetTable.get(index));
	}

	BPItem getItem(int offset) {
	        if (items.containsKey(offset)) {
	            log.fine("Already have item at offset " + offset);
	            return items.get(offset);
	        } else {
	            // mark whatever we return for expansion
	            log.fine("Marking item at offset " + offset + " for expansion");
	            offsetsToExpand.offer(offset);
	            BPItem toReturn = BPNull.Instance;

	            data.position(offset);
	            short next = (short) data.get();
	            switch (next) {
	                case BinaryPlist.NULL:
	                    log.fine("Null");
	                    toReturn = BPNull.Instance;
	                    break;
	                case BinaryPlist.BOOL_FALSE:
	                    log.fine("Bool_False");
	                    toReturn = BPBoolean.FALSE;
	                    break;
	                case BinaryPlist.BOOL_TRUE:
	                    log.fine("Bool_True");
	                    toReturn = BPBoolean.TRUE;
	                    break;
	                case BinaryPlist.FILL:
	                    log.fine("Fill");
	                    toReturn = BPNull.Instance;
	                    break;
	                case BinaryPlist.DATE:
	                    log.fine("Date");
	                    byte[] dateData = new byte[8];
	                    data.get(dateData);
	                    toReturn = new BPDate(dateData);
	                    break;
	                default:
	                    final short littleNibble = (short) (next & 0x000f);
	                    final short bigNibble = (short) (next & 0x00f0);
	                    switch (bigNibble) {
	                        case BinaryPlist.INT:
	                            int numIntBytes = twoToThe(littleNibble);
	                            log.fine(String.format("Int %d bytes", numIntBytes));
	                            byte[] intData = new byte[numIntBytes];
	                            data.get(intData);
	                            toReturn = BPInt.from(intData);
	                            break;
	                        case BinaryPlist.REAL:
	                            int numRealBytes = twoToThe(littleNibble);
	                            log.fine(String.format("Real %d bytes", numRealBytes));
	                            byte[] realData = new byte[numRealBytes];
	                            data.get(realData);
	                            toReturn = BPReal.from(realData);
	                            break;
	                        case BinaryPlist.DATA:
	                            int numDataBytes = (littleNibble < 0x0f) ? littleNibble : readAnInt();
	                            log.fine(String.format("Data %d bytes", numDataBytes));
	                            byte[] dataData = new byte[numDataBytes];
	                            data.get(dataData);
	                            toReturn = new BPData(dataData);
	                            break;
	                        case BinaryPlist.STRING_ASCII:
	                            int numStringAsciiChars = (littleNibble < 0x0f) ? littleNibble : readAnInt();
	                            log.fine(String.format("String_Ascii %d chars", numStringAsciiChars));
	                            byte[] stringAsciiData = new byte[numStringAsciiChars];
	                            data.get(stringAsciiData);
	                            final BPString bpStringAscii = BPString.ascii(stringAsciiData);
	                            log.fine("String: " + bpStringAscii.getValue());
	                            toReturn = bpStringAscii;
	                            break;
	                        case BinaryPlist.STRING_UNICODE:
	                            int numStringUnicodeChars = (littleNibble < 0x0f) ? littleNibble : readAnInt();
	                            log.fine(String.format("String_Unicode %d chars", numStringUnicodeChars));
	                            byte[] stringUnicodeData = new byte[numStringUnicodeChars << 1];
	                            data.get(stringUnicodeData);
	                            final BPString bpStringUnicode = BPString.unicode(stringUnicodeData);
	                            log.fine("String: " + bpStringUnicode.getValue());
	                            toReturn = bpStringUnicode;
	                            break;
	                        case BinaryPlist.UID:
	                            int numUidBytes = littleNibble + 1;
	                            log.fine(String.format("UID %d bytes", numUidBytes));

	                            byte[] uidData = new byte[numUidBytes];
	                            data.get(uidData);
	                            toReturn = new BPUid(uidData);
	                            break;
	                        case BinaryPlist.ARRAY:
	                            int numArrayItems = (littleNibble < 0x0f) ? littleNibble : readAnInt();
	                            log.fine(String.format("Array %d items", numArrayItems));
	                            int[] arrayItemOffsets = new int[numArrayItems];
	                            for (int i=0; i< numArrayItems; i++) {
	                                arrayItemOffsets[i] = offsetReader.getOffset(data);
	                            }
	                            toReturn = new BPArray(arrayItemOffsets);
	                            break;
	                        case BinaryPlist.SET:
	                            int numSetItems = (littleNibble < 0x0f) ? littleNibble : readAnInt();
	                            log.fine(String.format("Set %d items", numSetItems));
	                            int[] setItemOffsets = new int[numSetItems];
	                            for (int i=0; i< numSetItems; i++) {
	                                setItemOffsets[i] = offsetReader.getOffset(data);
	                            }
	                            toReturn = new BPSet(setItemOffsets);
	                            break;
	                        case BinaryPlist.DICT:
	                            int numDictItems = (littleNibble < 0x0f) ? littleNibble : readAnInt();
	                            log.fine(String.format("Dict %d items", numDictItems));
	                            int[] keyOffsets = new int[numDictItems];
	                            int[] valueOffsets = new int[numDictItems];
	                            for (int i=0; i< numDictItems; i++) {
	                                keyOffsets[i] = offsetReader.getOffset(data);
	                            }
	                            for (int i=0; i< numDictItems; i++) {
	                                valueOffsets[i] = offsetReader.getOffset(data);
	                            }
	                            toReturn = new BPDict(keyOffsets, valueOffsets);
	                            break;
	                        default:
	                            log.fine("Unused");
	                            toReturn = BPNull.Instance;
	                            break;

	                    }
	            }
	            items.put(offset, toReturn);
	            return toReturn;
	        }
	    }

	private int readAnInt() {
		short next = data.get();
		final short littleNibble = (short) (next & 0x000f);
		final short bigNibble = (short) (next & 0x00f0);
		if (bigNibble != BinaryPlist.INT) {
			throw new RuntimeException(
					"Asked to read an int, but next thing in stream wasn't one");
		}
		int numIntBytes = twoToThe(littleNibble);
		byte[] intData = new byte[numIntBytes];
		data.get(intData);
		BPInt ret = BPInt.from(intData);
		return ret.getValue();
	}

	private int twoToThe(short exponent) {
		switch (exponent) {
		case 0:
			return 1;
		case 1:
			return 2;
		case 2:
			return 4;
		case 3:
			return 8;
		case 4:
			return 16;
		case 5:
			return 32;
		case 6:
			return 64;
		case 7:
			return 128;
		case 8:
			return 256;
		case 9:
			return 512;
		case 10:
			return 1024;
		case 11:
			return 2048;
		case 12:
			return 4096;
		case 13:
			return 8192;
		case 14:
			return 16384;
		case 15:
			return 32768;
		default:
			return 65536;
		}
	}
}

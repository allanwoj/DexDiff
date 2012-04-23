package item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class ClassDataItem {

	public long staticFieldsSize;
	public long instanceFieldsSize;
	public long directMethodsSize;
	public long virtualMethodsSize;
	public EncodedField[] staticFields;
	public EncodedField[] instanceFields;
	public EncodedMethod[] directMethods;
	public EncodedMethod[] virtualMethods;
	
	public ClassDataItem(long staticFieldsSize, long instanceFieldsSize,
			long directMethodsSize, long virtualMethodsSize,
			EncodedField[] staticFields, EncodedField[] instanceFields,
			EncodedMethod[] directMethods, EncodedMethod[] virtualMethods) {
		this.staticFieldsSize = staticFieldsSize;
		this.instanceFieldsSize = instanceFieldsSize;
		this.directMethodsSize = directMethodsSize;
		this.virtualMethodsSize = virtualMethodsSize;
		this.staticFields = staticFields;
		this.instanceFields = instanceFields;
		this.directMethods = directMethods;
		this.virtualMethods = virtualMethods;
	}
	
	public boolean isEqual(ClassDataItem other, long[] fieldIndexMap, long[] methodIndexMap, long[] codeItemIndexMap) {
		if (staticFieldsSize != other.staticFieldsSize || instanceFieldsSize != other.instanceFieldsSize ||
				directMethodsSize != other.directMethodsSize || virtualMethodsSize != other.virtualMethodsSize) {
			return false;
		}
		
		for (int i = 0; i < staticFieldsSize; ++i) {
			if (i == 0) {
				if (fieldIndexMap[(int) staticFields[0].fieldIdDiff] != other.staticFields[0].fieldIdDiff||
						staticFields[0].flags != other.staticFields[0].flags) {
					return false;
				}
			} else {
				if (fieldIndexMap[(int) (staticFields[0].fieldIdDiff + staticFields[i].fieldIdDiff)] !=
					other.staticFields[0].fieldIdDiff + other.staticFields[i].fieldIdDiff ||
						staticFields[i].flags != other.staticFields[i].flags) {
					return false;
				}
			}
		}
		
		for (int i = 0; i < instanceFieldsSize; ++i) {
			if (i == 0) {
				if (fieldIndexMap[(int) instanceFields[0].fieldIdDiff] != other.instanceFields[0].fieldIdDiff||
						instanceFields[0].flags != other.instanceFields[0].flags) {
					return false;
				}
			} else {
				if (fieldIndexMap[(int) (instanceFields[0].fieldIdDiff + instanceFields[i].fieldIdDiff)] !=
					other.instanceFields[0].fieldIdDiff + other.instanceFields[i].fieldIdDiff ||
						instanceFields[i].flags != other.instanceFields[i].flags) {
					return false;
				}
			}
		}
		
		
		for (int i = 0; i < directMethodsSize; ++i) {
			if (directMethods[i].flags != other.directMethods[i].flags) {
				return false;
			}
			
			if (directMethods[i].codeItemIndex != -1 && codeItemIndexMap[(int)directMethods[i].codeItemIndex] != other.directMethods[i].codeItemIndex) {
				return false;
			}
			
			if (i == 0) {
				if (methodIndexMap[(int) directMethods[0].methodIdDiff] != other.directMethods[0].methodIdDiff) {
					return false;
				}
			} else {
				if (methodIndexMap[(int) (directMethods[0].methodIdDiff + directMethods[i].methodIdDiff)] !=
					other.directMethods[0].methodIdDiff + other.directMethods[i].methodIdDiff) {
					return false;
				}
			}
		}
		
		for (int i = 0; i < virtualMethodsSize; ++i) {
			if (virtualMethods[i].flags != other.virtualMethods[i].flags) {
				return false;
			}
			
			if (virtualMethods[i].codeItemIndex != -1 && codeItemIndexMap[(int)virtualMethods[i].codeItemIndex] != other.virtualMethods[i].codeItemIndex) {
				return false;
			}
			
			if (i == 0) {
				if (methodIndexMap[(int) virtualMethods[0].methodIdDiff] != other.virtualMethods[0].methodIdDiff) {
					return false;
				}
			} else {
				if (methodIndexMap[(int) (virtualMethods[0].methodIdDiff + virtualMethods[i].methodIdDiff)] !=
					other.virtualMethods[0].methodIdDiff + other.virtualMethods[i].methodIdDiff) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	public byte[] getBytes(long[] fieldIndexMap, long[] methodIndexMap, long[] codeItemIndexMap, long[] codeItemPointerMap) {
		ArrayList<Byte> l = new ArrayList<Byte>();
		l.addAll(writeULeb128((int)staticFieldsSize));
		l.addAll(writeULeb128((int)instanceFieldsSize));
		l.addAll(writeULeb128((int)directMethodsSize));
		l.addAll(writeULeb128((int)virtualMethodsSize));
		
		for (int i = 0; i < staticFieldsSize; ++i) {
			if (i == 0) {
				l.addAll(writeULeb128((int)fieldIndexMap[(int)staticFields[0].fieldIdDiff]));
			} else {
				l.addAll(writeULeb128((int)fieldIndexMap[(int)(staticFields[0].fieldIdDiff + staticFields[i].fieldIdDiff)] - (int)fieldIndexMap[(int)staticFields[0].fieldIdDiff]));
			}
			l.addAll(writeULeb128((int)staticFields[i].flags));
		}
		
		for (int i = 0; i < instanceFieldsSize; ++i) {
			if (i == 0) {
				l.addAll(writeULeb128((int)fieldIndexMap[(int)instanceFields[0].fieldIdDiff]));
			} else {
				l.addAll(writeULeb128((int)fieldIndexMap[(int)(instanceFields[0].fieldIdDiff + instanceFields[i].fieldIdDiff)] - (int)fieldIndexMap[(int)instanceFields[0].fieldIdDiff]));
			}
			l.addAll(writeULeb128((int)instanceFields[i].flags));
		}
		
		
		for (int i = 0; i < directMethodsSize; ++i) {
			if (i == 0) {
				l.addAll(writeULeb128((int)methodIndexMap[(int)directMethods[0].methodIdDiff]));
			} else {
				l.addAll(writeULeb128((int)methodIndexMap[(int)(directMethods[0].methodIdDiff + directMethods[i].methodIdDiff)] - (int)methodIndexMap[(int)directMethods[0].methodIdDiff]));
			}
			l.addAll(writeULeb128((int)directMethods[i].flags));
			if (directMethods[i].codeItemIndex == -1) {
				l.addAll(writeULeb128(0));
			} else {
				l.addAll(writeULeb128((int)codeItemPointerMap[(int)codeItemIndexMap[directMethods[i].codeItemIndex]]));
			}
		}
		
		for (int i = 0; i < virtualMethodsSize; ++i) {
			if (i == 0) {
				l.addAll(writeULeb128((int)methodIndexMap[(int)virtualMethods[0].methodIdDiff]));
			} else {
				l.addAll(writeULeb128((int)methodIndexMap[(int)(virtualMethods[0].methodIdDiff + virtualMethods[i].methodIdDiff)] - (int)methodIndexMap[(int)virtualMethods[0].methodIdDiff]));
			}
			l.addAll(writeULeb128((int)virtualMethods[i].flags));
			if (virtualMethods[i].codeItemIndex == -1) {
				l.addAll(writeULeb128(0));
			} else {
				l.addAll(writeULeb128((int)codeItemPointerMap[(int)codeItemIndexMap[virtualMethods[i].codeItemIndex]]));
			}
		}
		
		byte[] ret = new byte[l.size()];
		Iterator<Byte> iter = l.iterator();
		int count = 0;
		
		while (iter.hasNext()) {
			ret[count++] = iter.next();
		}
		
		return ret;
	}
	
	public byte[] getOutput() {
		ArrayList<Byte> l = new ArrayList<Byte>();
		l.addAll(writeULeb128((int)staticFieldsSize));
		l.addAll(writeULeb128((int)instanceFieldsSize));
		l.addAll(writeULeb128((int)directMethodsSize));
		l.addAll(writeULeb128((int)virtualMethodsSize));
		
		for (int i = 0; i < staticFieldsSize; ++i) {
			l.addAll(writeULeb128((int)staticFields[i].fieldIdDiff));
			l.addAll(writeULeb128((int)staticFields[i].flags));
		}
		
		for (int i = 0; i < instanceFieldsSize; ++i) {
			l.addAll(writeULeb128((int)instanceFields[i].fieldIdDiff));
			l.addAll(writeULeb128((int)instanceFields[i].flags));
		}
		
		
		for (int i = 0; i < directMethodsSize; ++i) {
			l.addAll(writeULeb128((int)directMethods[i].methodIdDiff));
			l.addAll(writeULeb128((int)directMethods[i].flags));
			l.addAll(writeULeb128((int)directMethods[i].codeItemOffset));
		}
		
		for (int i = 0; i < virtualMethodsSize; ++i) {
			l.addAll(writeULeb128((int)virtualMethods[i].methodIdDiff));
			l.addAll(writeULeb128((int)virtualMethods[i].flags));
			l.addAll(writeULeb128((int)virtualMethods[i].codeItemOffset));
		}
		
		byte[] ret = new byte[4 + l.size()];
		Iterator<Byte> iter = l.iterator();
		int count = 0;
		byte[] temp = write32bit(l.size());
		for (int i = 0; i < 4; ++i)
			ret[count++] = temp[i];
		
		while (iter.hasNext()) {
			ret[count++] = iter.next();
		}
		
		return ret;
	}
	
	public byte[] write32bit(long data) {
		byte[] output = new byte[4];
		
		for(int i = 0; i < 4; ++i) {
			output[i] = (byte)((data >> (i*8)) & 0xFF);
		}

		return output;
	}
	
	Collection<Byte> writeULeb128(int value) {
		ArrayList<Byte> l = new ArrayList<Byte>();
		long remaining = (value & 0xFFFFFFFFL) >> 7;
        long lValue = value;
        int count = 0;

        while (remaining != 0) {

        	l.add((byte)((int)(lValue & 0x7f) | 0x80));
            lValue = remaining;
            remaining >>= 7;
            count++;
        }

			l.add((byte)((int)(lValue & 0x7f)));
        return l;
	}
}

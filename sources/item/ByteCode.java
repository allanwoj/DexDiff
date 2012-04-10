package item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class ByteCode {

	public int op;
	ArrayList<Byte> data;
	
	public ByteCode(int op, ArrayList<Byte> data) {
		this.op = op;
		this.data = data;
	}
	
	public Collection<Byte> getOutput(long[] stringIndexMap, long[] typeIndexMap, long[] methodIndexMap) {
		
		ArrayList<Byte> l = new ArrayList<Byte>();
		
		l.add((byte)op);
		
		if (op == 0x1A) {
			l.add(data.get(1));
			// string_index
			int buff = (data.get(2) << 8) | data.get(3);
			int newIndex = (int)stringIndexMap[buff];
			l.add((byte)(newIndex >> 8));
			l.add((byte)(newIndex & 0x0F));
		} else if (op == 0x1B) {

			l.add(data.get(1));
			// string_index
			long buff = (data.get(2) << 8) | data.get(3);
			buff = (buff << 16) | data.get(4);
			buff = (buff << 24) | data.get(5);
			long newIndex = stringIndexMap[(int)buff];
			
			l.add((byte)((newIndex >> 24) & 0x000F));
			l.add((byte)((newIndex >> 16) & 0x000F));
			l.add((byte)((newIndex >> 8) & 0x000F));
			l.add((byte)(newIndex & 0x000F));
		} else if (op == 0x1C) {
			l.add(data.get(1));
			// type_index
			int buff = (data.get(2) << 8) | data.get(3);
			int newIndex = (int)typeIndexMap[buff];
			l.add((byte)(newIndex >> 8));
			l.add((byte)(newIndex & 0x0F));
		} else if (op == 0x1F) {
			l.add(data.get(1));
			// type_index
			int buff = (data.get(2) << 8) | data.get(3);
			int newIndex = (int)typeIndexMap[buff];
			l.add((byte)(newIndex >> 8));
			l.add((byte)(newIndex & 0x0F));
		} else if (op == 0x20) { //
			l.add(data.get(1));
			l.add(data.get(2));
			// type_index
			int buff = (data.get(3) << 8) | data.get(4);
			int newIndex = (int)typeIndexMap[buff];
			l.add((byte)(newIndex >> 8));
			l.add((byte)(newIndex & 0x0F));
		} else if (op == 0x22) { //??????
			l.add(data.get(1));
			// type_index
			int buff = (data.get(2) << 8) | data.get(3);
			int newIndex = (int)typeIndexMap[buff];
			l.add((byte)(newIndex >> 8));
			l.add((byte)(newIndex & 0x0F));
		} else if (op == 0x23) { //??????
			l.add(data.get(1));
			// type_index
			int buff = (data.get(2) << 8) | data.get(3);
			int newIndex = (int)typeIndexMap[buff];
			l.add((byte)(newIndex >> 8));
			l.add((byte)(newIndex & 0x0F));
		} else if (op == 0x24) { //??????
			int b = data.get(1);
			l.add((byte)b);
			int regno = (b & 0xF0) >> 4;
			int count = 0;
			while (count < (1+regno)/2) {
				l.add(data.get(2 + count));
				++count;
			}
			if ((1+regno)/2 % 2 != 0) {
				l.add(data.get(2 + count++));
			}
			// type_index
			int buff = (data.get(count) << 8) | data.get(count + 1);
			int newIndex = (int)typeIndexMap[buff];
			l.add((byte)(newIndex >> 8));
			l.add((byte)(newIndex & 0x0F));
		} else if (op == 0x25) {
			l.add(data.get(1));
			l.add(data.get(2));
			// type_index
			int buff = (data.get(3) << 8) | data.get(4);
			int newIndex = (int)typeIndexMap[buff];
			l.add((byte)(newIndex >> 8));
			l.add((byte)(newIndex & 0x0F));
			
			l.add(data.get(5));
			l.add(data.get(6));
		} else if (op >= 0x6E && op <= 0x72) {
			int b = data.get(1);
			l.add((byte)b);
			
			// method_index
			int buff = (data.get(2) << 8) | data.get(3);
			int newIndex = (int)methodIndexMap[buff];
			l.add((byte)(newIndex >> 8));
			l.add((byte)(newIndex & 0x0F));
			
			int regno = (b & 0xF0) >> 4;
			l.add(data.get(4));
			int count = 0;
			while (count < (1+regno)/2) {
				l.add(data.get(4 + count));
				++count;
			}
			if ((1+regno)/2 % 2 != 0) {
				l.add(data.get(4 + count));
			}
		} else  {
			return data;
		}
		return l;
	}
	
	public boolean isEqual(ByteCode update, long[] stringIndexMap, long[] typeIndexMap, long[] methodIndexMap) {
		
		if (!data.get(0).equals(update.data.get(0))) {
			return false;
		}
		
		if (op == 0x1A) {
			if (!data.get(1).equals(update.data.get(1))) {
				return false;
			}
			// string_index
			int buff = (data.get(2) << 8) | data.get(3);
			int expectedIndex = (int)stringIndexMap[buff];
			int actualIndex = (update.data.get(2) << 8) | update.data.get(3);
			if (expectedIndex != actualIndex) { 
				return false;
			}
			
		} else if (op == 0x1B) {
			if (!data.get(1).equals(update.data.get(1))) {
				return false;
			}
			// string_index
			long buff = (data.get(2) << 8) | data.get(3);
			buff = (buff << 16) | data.get(4);
			buff = (buff << 24) | data.get(5);
			long expectedIndex = stringIndexMap[(int)buff];
			long actualIndex = (update.data.get(2) << 8) | update.data.get(3);
			actualIndex = (buff << 16) | update.data.get(4);
			actualIndex = (buff << 24) | update.data.get(5);
			
			if (expectedIndex != actualIndex) { 
				return false;
			}
		} else if (op == 0x1C) {
			if (!data.get(1).equals(update.data.get(1))) {
				return false;
			}
			// type_index
			int buff = (data.get(2) << 8) | data.get(3);
			int expectedIndex = (int)typeIndexMap[buff];
			int actualIndex = (update.data.get(2) << 8) | update.data.get(3);
			if (expectedIndex != actualIndex) { 
				return false;
			}
		} else if (op == 0x1F) {
			if (!data.get(1).equals(update.data.get(1))) {
				return false;
			}
			// type_index
			int buff = (data.get(2) << 8) | data.get(3);
			int expectedIndex = (int)typeIndexMap[buff];
			int actualIndex = (update.data.get(2) << 8) | update.data.get(3);
			if (expectedIndex != actualIndex) { 
				return false;
			}
		} else if (op == 0x20) { //
			if (!data.get(1).equals(update.data.get(1)) ||
					!data.get(2).equals(update.data.get(2))) {
				return false;
			}
			// type_index
			int buff = (data.get(3) << 8) | data.get(4);
			int expectedIndex = (int)typeIndexMap[buff];
			int actualIndex = (update.data.get(3) << 8) | update.data.get(4);
			if (expectedIndex != actualIndex) { 
				return false;
			}
		} else if (op == 0x22) { //??????
			if (!data.get(1).equals(update.data.get(1))) {
				return false;
			}
			// type_index
			int buff = (data.get(2) << 8) | data.get(3);
			int expectedIndex = (int)typeIndexMap[buff];
			int actualIndex = (update.data.get(2) << 8) | update.data.get(3);
			if (expectedIndex != actualIndex) { 
				return false;
			}
		} else if (op == 0x23) { //??????
			if (!data.get(1).equals(update.data.get(1))) {
				return false;
			}
			// type_index
			int buff = (data.get(2) << 8) | data.get(3);
			int expectedIndex = (int)typeIndexMap[buff];
			int actualIndex = (update.data.get(2) << 8) | update.data.get(3);
			if (expectedIndex != actualIndex) { 
				return false;
			}
		} else if (op == 0x24) { //??????
			if (!data.get(1).equals(update.data.get(1))) {
				return false;
			}
			int b = data.get(1);
			int regno = (b & 0xF0) >> 4;
			int count = 0;
			while (count < (1+regno)/2) {
				if (!data.get(2 + count).equals(update.data.get(2 + count))) {
					return false;
				}
				++count;
			}
			if ((1+regno)/2 % 2 != 0) {
				if (!data.get(2 + count).equals(update.data.get(2 + count))) {
					return false;
				}
				++count;
			}
			// type_index
			int buff = (data.get(count) << 8) | data.get(count + 1);
			int expectedIndex = (int)typeIndexMap[buff];
			int actualIndex = (update.data.get(count) << 8) | update.data.get(count + 1);
			if (expectedIndex != actualIndex) { 
				return false;
			}
		} else if (op == 0x25) {
			if (!data.get(1).equals(update.data.get(1)) ||
					!data.get(2).equals(update.data.get(2))) {
				return false;
			}
			// type_index
			int buff = (data.get(3) << 8) | data.get(4);
			int expectedIndex = (int)typeIndexMap[buff];
			int actualIndex = (update.data.get(3) << 8) | update.data.get(4);
			if (expectedIndex != actualIndex) { 
				return false;
			}
			
			if (!data.get(5).equals(update.data.get(5)) ||
					!data.get(6).equals(update.data.get(6))) {
				return false;
			}
		} else if (op >= 0x6E && op <= 0x72) {
			if (!data.get(1).equals(update.data.get(1))) {
				return false;
			}
			int b = data.get(1);
			
			// method_index
			int buff = (data.get(2) << 8) | data.get(3);
			int expectedIndex = (int)methodIndexMap[buff];
			int actualIndex = (update.data.get(2) << 8) | update.data.get(3);
			if (expectedIndex != actualIndex) { 
				return false;
			}
			
			int regno = (b & 0xF0) >> 4;
			int count = 0;
			while (count < (1+regno)/2) {
				if (!data.get(4 + count).equals(update.data.get(4 + count))) {
					return false;
				}
				++count;
			}
			if ((1+regno)/2 % 2 != 0) {
				if (!data.get(4 + count).equals(update.data.get(4 + count))) {
					return false;
				}
			}
		} else  {
			if (data.size() != update.data.size()) {
				return false;
			}
			
			Iterator<Byte> it1 = data.iterator();
			Iterator<Byte> it2 = update.data.iterator();
			
			while (it1.hasNext()) {
				if (!it1.next().equals(it2.next())) {
					return false;
				}
			}
		}
		
		return true;
	}
	
}

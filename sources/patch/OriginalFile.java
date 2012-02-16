package patch;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class OriginalFile {

	String stringTable;
	int[] data = new int[100];
	int stringTableIndex = 0;
	int dataIndex = 0;
	int dataSize = 0;
	
	public OriginalFile(String fileName) {
		try {
			BufferedReader file = new BufferedReader(new FileReader(fileName));
			stringTable = file.readLine();
			String buf;
			while(true) {
				buf = file.readLine();
				if (buf == null) {
					break;
				}
				data[dataSize++] = Integer.parseInt(buf);
			}
			file.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	char getTableData() {
		return stringTable.charAt(stringTableIndex++);
	}
	
	int getData() {
		return data[dataIndex++];
	}
	
	boolean hasData() {
		return dataIndex < dataSize;
	}
	
}

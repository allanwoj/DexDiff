package patch;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class OriginalFile {

	String data;
	int index = 0;
	
	public OriginalFile(String fileName) {
		try {
			BufferedReader file = new BufferedReader(new FileReader(fileName));
			data = file.readLine();
			file.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	char getData() {
		return data.charAt(index++);
	}
	
}

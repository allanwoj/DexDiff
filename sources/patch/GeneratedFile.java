package patch;

import java.io.FileNotFoundException;
import java.io.PrintStream;

public class GeneratedFile {
	PrintStream out = null;
	public GeneratedFile(String fileName) {
		try {
			out = new PrintStream(fileName);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void write(char data) {
		out.append(data);
	}
}

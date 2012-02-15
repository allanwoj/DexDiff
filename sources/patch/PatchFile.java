package patch;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashSet;

public class PatchFile {

	private LinkedHashSet<PatchCommand> commands;
	private Iterator<PatchCommand> it;
	private String data;
	int index = 0;
	
	public PatchFile(String fileName) {
		commands = new LinkedHashSet<PatchCommand>();
		data = new String();
		try {
			FileReader file = new FileReader(fileName);
			int type = -1;
			int size = 0;
			while(true) {
				type = file.read() - 48;
				if (type == 3) {
					while(true) {
						int d = file.read();
						if (d == -1)
							break;
						data += (char)d;
					}
					break;
				}
				size = file.read() - 48;
				commands.add(new PatchCommand(type, size));
				file.read();
			}
			file.close();
			it = commands.iterator();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	PatchCommand getNextCommand() {
		return it.next();
	}
	
	boolean hasCommands() {
		return it.hasNext();
	}
	
	char getData() {
		return data.charAt(index++);
	}
}

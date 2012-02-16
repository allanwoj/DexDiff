package patch;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashSet;

public class PatchFile {

	private LinkedHashSet<PatchCommand> commands;
	private LinkedHashSet<PatchCommand> dataCommands;
	private Iterator<PatchCommand> it;
	private Iterator<PatchCommand> dataIt;
	private String data;
	int index = 0;
	
	public PatchFile(String fileName) {
		commands = new LinkedHashSet<PatchCommand>();
		dataCommands = new LinkedHashSet<PatchCommand>();
		data = new String();
		try {
			FileReader file = new FileReader(fileName);
			int type = -1;
			int size = 0;
			// Read string table commands
			while(true) {
				type = file.read() - 48;
				if (type == -38) {
					break;
				}
				size = file.read() - 48;
				commands.add(new PatchCommand(type, size));
				file.read();
			}
			
			// Read data commands
			while(true) {
				type = file.read() - 48;
				if (type == -38) {
					break;
				}
				size = file.read() - 48;
				dataCommands.add(new PatchCommand(type, size));
				file.read();
			}
			
			while(true) {
				int d = file.read();
				if (d == -1)
					break;
				data += (char)d;
			}
			
			file.close();
			it = commands.iterator();
			dataIt = dataCommands.iterator();
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
	
	PatchCommand getNextDataCommand() {
		return dataIt.next();
	}
	
	boolean hasDataCommands() {
		return dataIt.hasNext();
	}
	
	char getData() {
		return data.charAt(index++);
	}
}

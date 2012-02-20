package patch;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashSet;

public class DexPatchFile {
	private LinkedHashSet<PatchCommand> commands;
	private Iterator<PatchCommand> it;
	private LinkedHashSet<String> data;
	private Iterator<String> dataIt;
	private long stringOffset;
	
	public DexPatchFile(String fileName) {
		commands = new LinkedHashSet<PatchCommand>();
		data = new LinkedHashSet<String>();
		try {
			BufferedReader file = new BufferedReader(new FileReader(fileName));
			String buf;
			int size = 0;
			int type;
			
			stringOffset = Long.parseLong(file.readLine());
			
			// Read string table commands
			while(true) {
				buf = file.readLine();
				if (buf.equals("4")) {
					break;
				}
				type = buf.charAt(0) - 48;
				size = Integer.parseInt(buf.substring(2));
				commands.add(new PatchCommand(type, size));
			}
			
			while(true) {
				String buff = file.readLine();
				if (buff == null)
					break;
				data.add(buff);
			}
			
			file.close();
			it = commands.iterator();
			dataIt = data.iterator();
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
	
	String getNextData() {
		return dataIt.next();
	}
	
	boolean hasData() {
		return dataIt.hasNext();
	}
	
	long getOffset() {
		return stringOffset;
	}
}

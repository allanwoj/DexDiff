package patch;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class DexPatchFile {
	private List<PatchCommand> stringCommands;
	private List<PatchCommand> typeCommands;
	private List<PatchCommand> fieldCommands;
	private List<PatchCommand> protoCommands;
	private List<PatchCommand> methodCommands;
	private List<PatchCommand> typeListCommands;
	private Iterator<PatchCommand> stringIt;
	private Iterator<PatchCommand> typeIt;
	private Iterator<PatchCommand> fieldIt;
	private Iterator<PatchCommand> protoIt;
	private Iterator<PatchCommand> methodIt;
	private Iterator<PatchCommand> typeListIt;
	private List<String> data;
	private Iterator<String> dataIt;
	private long stringOffset;
	
	public DexPatchFile(String fileName) {
		stringCommands = new LinkedList<PatchCommand>();
		typeCommands = new LinkedList<PatchCommand>();
		fieldCommands = new LinkedList<PatchCommand>();
		protoCommands = new LinkedList<PatchCommand>();
		methodCommands = new LinkedList<PatchCommand>();
		typeListCommands = new LinkedList<PatchCommand>();
		data = new LinkedList<String>();
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
				stringCommands.add(new PatchCommand(type, size));
			}
			
			// Read type_id commands
			while(true) {
				buf = file.readLine();
				if (buf.equals("5")) {
					break;
				}
				type = buf.charAt(0) - 48;
				size = Integer.parseInt(buf.substring(2));
				typeCommands.add(new PatchCommand(type, size));
			}
			
			// Read field_id commands
			while(true) {
				buf = file.readLine();
				if (buf.equals("6")) {
					break;
				}
				type = buf.charAt(0) - 48;
				size = Integer.parseInt(buf.substring(2));
				fieldCommands.add(new PatchCommand(type, size));
			}
			
			// Read proto_id commands
			while(true) {
				buf = file.readLine();
				if (buf.equals("7")) {
					break;
				}
				type = buf.charAt(0) - 48;
				size = Integer.parseInt(buf.substring(2));
				protoCommands.add(new PatchCommand(type, size));
			}
			
			// Read method_id commands
			while(true) {
				buf = file.readLine();
				if (buf.equals("8")) {
					break;
				}
				type = buf.charAt(0) - 48;
				size = Integer.parseInt(buf.substring(2));
				methodCommands.add(new PatchCommand(type, size));
			}
			
			// Read type_list commands
			while(true) {
				buf = file.readLine();
				if (buf.equals("9")) {
					break;
				}
				type = buf.charAt(0) - 48;
				size = Integer.parseInt(buf.substring(2));
				typeListCommands.add(new PatchCommand(type, size));
			}
			
			// Read patch data
			while(true) {
				String buff = file.readLine();
				if (buff == null)
					break;
				data.add(buff);
			}
			
			file.close();
			stringIt = stringCommands.iterator();
			typeIt = typeCommands.iterator();
			fieldIt = fieldCommands.iterator();
			protoIt = protoCommands.iterator();
			methodIt = methodCommands.iterator();
			typeListIt = typeListCommands.iterator();
			dataIt = data.iterator();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	PatchCommand getNextStringCommand() {
		return stringIt.next();
	}
	
	boolean hasStringCommands() {
		return stringIt.hasNext();
	}
	
	PatchCommand getNextTypeCommand() {
		return typeIt.next();
	}
	
	boolean hasTypeCommands() {
		return typeIt.hasNext();
	}
	
	PatchCommand getNextFieldCommand() {
		return fieldIt.next();
	}
	
	boolean hasFieldCommands() {
		return fieldIt.hasNext();
	}
	
	PatchCommand getNextProtoCommand() {
		return protoIt.next();
	}
	
	boolean hasProtoCommands() {
		return protoIt.hasNext();
	}
	
	PatchCommand getNextMethodCommand() {
		return methodIt.next();
	}
	
	boolean hasMethodCommands() {
		return methodIt.hasNext();
	}
	
	PatchCommand getNextTypeListCommand() {
		return typeListIt.next();
	}
	
	boolean hasTypeListCommands() {
		return typeListIt.hasNext();
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

package patch;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
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
	private List<PatchCommand> debugInfoCommands;
	private List<PatchCommand> codeItemCommands;
	private List<PatchCommand> annotationItemCommands;
	private List<PatchCommand> annotationSetItemCommands;
	private List<PatchCommand> annotationSetRefListCommands;
	private List<PatchCommand> annotationsDirectoryItemCommands;
	private Iterator<PatchCommand> stringIt;
	private Iterator<PatchCommand> typeIt;
	private Iterator<PatchCommand> fieldIt;
	private Iterator<PatchCommand> protoIt;
	private Iterator<PatchCommand> methodIt;
	private Iterator<PatchCommand> typeListIt;
	private Iterator<PatchCommand> debugInfoIt;
	private Iterator<PatchCommand> codeItemIt;
	private Iterator<PatchCommand> annotationItemIt;
	private Iterator<PatchCommand> annotationSetItemIt;
	private Iterator<PatchCommand> annotationSetRefListIt;
	private Iterator<PatchCommand> annotationsDirectoryItemIt;
	private LinkedList<List<Byte>> data;
	private Iterator<List<Byte>> dataIt;
	private long stringOffset;
	private long typeListOffset;
	private long debugInfoItemOffset;
	private long annotationItemOffset;
	private long annotationSetItemOffset;
	private long annotationSetRefListOffset;
	private long annotationsDirectoryItemOffset;
	private long codeItemOffset;
	RandomAccessFile file;
	
	public DexPatchFile(String fileName) {
		stringCommands = new LinkedList<PatchCommand>();
		typeCommands = new LinkedList<PatchCommand>();
		fieldCommands = new LinkedList<PatchCommand>();
		protoCommands = new LinkedList<PatchCommand>();
		methodCommands = new LinkedList<PatchCommand>();
		typeListCommands = new LinkedList<PatchCommand>();
		debugInfoCommands = new LinkedList<PatchCommand>();
		codeItemCommands = new LinkedList<PatchCommand>();
		annotationItemCommands = new LinkedList<PatchCommand>();
		annotationSetItemCommands = new LinkedList<PatchCommand>();
		annotationSetRefListCommands = new LinkedList<PatchCommand>();
		annotationsDirectoryItemCommands = new LinkedList<PatchCommand>();
		data = new LinkedList<List<Byte>>();
		try {
			//BufferedReader file = new BufferedReader(new FileReader(fileName));
			file = new RandomAccessFile("out/connect.patch", "r");
			int size = 0;
			int type;
			
			stringOffset = Long.parseLong(file.readLine());
			typeListOffset = Long.parseLong(file.readLine());
			debugInfoItemOffset = Long.parseLong(file.readLine());
			codeItemOffset = Long.parseLong(file.readLine());
			annotationItemOffset = Long.parseLong(file.readLine());
			annotationSetItemOffset = Long.parseLong(file.readLine());
			annotationSetRefListOffset = Long.parseLong(file.readLine());
			annotationsDirectoryItemOffset = Long.parseLong(file.readLine());
			
			// Read string table commands
			while(true) {
				type = read8Bit();
				if (type == 4) {
					break;
				}
				size = read16Bit();
				stringCommands.add(new PatchCommand(type, size));
			}
			
			// Read type_id commands
			while(true) {
				type = read8Bit();
				if (type == 4) {
					break;
				}
				size = read16Bit();
				typeCommands.add(new PatchCommand(type, size));
			}
			
			// Read field_id commands
			while(true) {
				type = read8Bit();
				if (type == 4) {
					break;
				}
				size = read16Bit();
				fieldCommands.add(new PatchCommand(type, size));
			}
			
			// Read type_list commands
			while(true) {
				type = read8Bit();
				if (type == 4) {
					break;
				}
				size = read16Bit();
				typeListCommands.add(new PatchCommand(type, size));
			}
			
			// Read proto_id commands
			while(true) {
				type = read8Bit();
				if (type == 4) {
					break;
				}
				size = read16Bit();
				protoCommands.add(new PatchCommand(type, size));
			}
			
			// Read method_id commands
			while(true) {
				type = read8Bit();
				if (type == 4) {
					break;
				}
				size = read16Bit();
				methodCommands.add(new PatchCommand(type, size));
			}
			
			// Read debug_info_item commands
			while(true) {
				type = read8Bit();
				if (type == 4) {
					break;
				}
				size = read16Bit();
				debugInfoCommands.add(new PatchCommand(type, size));
			}
			
			// Read code_item commands
			while(true) {
				type = read8Bit();
				if (type == 4) {
					break;
				}
				size = read16Bit();
				codeItemCommands.add(new PatchCommand(type, size));
			}
			
			// Read annotation_item commands
			while(true) {
				type = read8Bit();
				if (type == 4) {
					break;
				}
				size = read16Bit();
				annotationItemCommands.add(new PatchCommand(type, size));
			}
			
			// Read annotation_set_item commands
			while(true) {
				type = read8Bit();
				if (type == 4) {
					break;
				}
				size = read16Bit();
				annotationSetItemCommands.add(new PatchCommand(type, size));
			}
			
			// Read annotation_set_ref_list commands
			while(true) {
				type = read8Bit();
				if (type == 4) {
					break;
				}
				size = read16Bit();
				annotationSetRefListCommands.add(new PatchCommand(type, size));
			}
			
			// Read annotations_directory_item commands
			while(true) {
				type = read8Bit();
				if (type == 4) {
					break;
				}
				size = read16Bit();
				annotationsDirectoryItemCommands.add(new PatchCommand(type, size));
			}
			
			long range;
			// Read patch data
			while(true) {
				range = read32Bit();
				if (range == 0) {
					break;
				}
				LinkedList<Byte> by = new LinkedList<Byte>();
				for (long i = 0; i < range; ++i) {
					by.add((byte)read8Bit());
				}
				
				data.add(by);
			}
			
			file.close();
			stringIt = stringCommands.iterator();
			typeIt = typeCommands.iterator();
			fieldIt = fieldCommands.iterator();
			protoIt = protoCommands.iterator();
			methodIt = methodCommands.iterator();
			typeListIt = typeListCommands.iterator();
			debugInfoIt = debugInfoCommands.iterator();
			codeItemIt = codeItemCommands.iterator();
			annotationItemIt = annotationItemCommands.iterator();
			annotationSetItemIt = annotationSetItemCommands.iterator();
			annotationSetRefListIt = annotationSetRefListCommands.iterator();
			annotationsDirectoryItemIt = annotationsDirectoryItemCommands.iterator();
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
	
	PatchCommand getNextDebugInfoCommand() {
		return debugInfoIt.next();
	}
	
	boolean hasDebugInfoCommands() {
		return debugInfoIt.hasNext();
	}
	
	PatchCommand getNextCodeItemCommand() {
		return codeItemIt.next();
	}
	
	boolean hasCodeItemCommands() {
		return codeItemIt.hasNext();
	}
	
	PatchCommand getNextAnnotationItemCommand() {
		return annotationItemIt.next();
	}
	
	boolean hasAnnotationItemCommands() {
		return annotationItemIt.hasNext();
	}
	
	PatchCommand getNextAnnotationSetItemCommand() {
		return annotationSetItemIt.next();
	}
	
	boolean hasAnnotationSetItemCommands() {
		return annotationSetItemIt.hasNext();
	}
	
	PatchCommand getNextAnnotationSetRefListCommand() {
		return annotationSetRefListIt.next();
	}
	
	boolean hasAnnotationSetRefListCommands() {
		return annotationSetRefListIt.hasNext();
	}
	
	PatchCommand getNextAnnotationsDirectoryItemCommand() {
		return annotationsDirectoryItemIt.next();
	}
	
	boolean hasAnnotationsDirectoryItemCommands() {
		return annotationsDirectoryItemIt.hasNext();
	}
	
	List<Byte> getNextData() {
		return dataIt.next();
	}
	
	boolean hasData() {
		return dataIt.hasNext();
	}
	
	long getStringOffset() {
		return stringOffset;
	}
	
	long getTypeListOffset() {
		return typeListOffset;
	}
	
	long getDebugInfoItemOffset() {
		return debugInfoItemOffset;
	}
	
	long getCodeItemOffset() {
		return codeItemOffset;
	}
	
	long getAnnotationItemOffset() {
		return annotationItemOffset;
	}
	
	long getAnnotationSetItemOffset() {
		return annotationSetItemOffset;
	}
	
	long getAnnotationSetRefListOffset() {
		return annotationSetRefListOffset;
	}
	
	long getAnnotationsDirectoryItemOffset() {
		return annotationsDirectoryItemOffset;
	}
	
	public int read8Bit() throws IOException {
        int result = file.read();
        return result;
    }
	
	public int read16Bit() throws IOException {
        int result = read8Bit();
        result |= ( read8Bit() << 8 );
        return result;
    }
	
	public long read32Bit() throws IOException {
        long result = (long)read16Bit();
        result |= ((long)read16Bit()) << 16;
        return result;
    }
}

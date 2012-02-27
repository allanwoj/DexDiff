package patch;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;


public class ApplyPatch {

	
	public static void main(String[] args) {
		DexOriginalFile original = new DexOriginalFile();
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(args[0], "r");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		original.setRandomAccessFile(raf);
		original.parse();
		DexPatchFile patch = new DexPatchFile(args[1]);
		GeneratedFile stringIdsFile = new GeneratedFile(args[2]);
		GeneratedFile typeIdsFile = new GeneratedFile("out/type.dex");
		GeneratedFile fieldIdsFile = new GeneratedFile("out/field.dex");
		GeneratedFile stringFile = new GeneratedFile("out/data.dex");
		long[] stringIndexMap = new long[10000];
		long[] typeIndexMap = new long[10000];
		long[] fieldIndexMap = new long[10000];
		PatchCommand command;
		int fileIndex = 0;
		int mapIndex = 0;
		long current = patch.getOffset();
		stringIdsFile.write(current);
		String buf;
		
		// Generate patched string_ids and string_data_items
		while(patch.hasStringCommands()) {
			command = patch.getNextStringCommand();
			
			if (command.type == 0) {
				// KEEP
				for(int i = 0; i < command.size; ++i) {
					buf = original.getStringData();
					current += buf.length() + 1;
					stringIdsFile.write(current);
					stringFile.write(buf);
					stringFile.write(0);
					stringIndexMap[mapIndex++] = fileIndex++;
				}
			} else if (command.type == 1) {
				// ADD
				for(int i = 0; i < command.size; ++i) {
					buf = patch.getNextData();
					int size = buf.length();
					int size_buf = 2;
					while (size > 127) {
						size = size >> 8;
						++size_buf;
					}
					current += buf.length() + size_buf;
					// TODO Handle cases when length is larger than 127.
					stringIdsFile.write(current);
					stringFile.write(buf.length());
					stringFile.write(buf);
					stringFile.write((char)0);
					++fileIndex;
				}
			} else if (command.type == 2) {
				// DELETE
				for(int i = 0; i < command.size; ++i) {
					original.getStringData();
					stringIndexMap[mapIndex++] = -1;
				}
			}
		}
		
		
		System.out.println("DONE");
		fileIndex = 0;
		mapIndex = 0;
		
		// Generate patched type_ids
		while(patch.hasTypeCommands()) {
			command = patch.getNextTypeCommand();
			if (command.type == 0) {
				// KEEP
				for(int i = 0; i < command.size; ++i) {
					typeIdsFile.write((stringIndexMap[original.getTypeIdData()]));
					typeIndexMap[mapIndex++] = fileIndex++;
				}
			} else if (command.type == 1) {
				// ADD
				for(int i = 0; i < command.size; ++i) {
					typeIdsFile.write(Long.parseLong(patch.getNextData()));
					++fileIndex;
				}
			} else if (command.type == 2) {
				// DELETE
				for(int i = 0; i < command.size; ++i) {
					original.getTypeIdData();
					stringIndexMap[mapIndex++] = -1;
				}
			}
		}
		
		fileIndex = 0;
		mapIndex = 0;
		FieldIdItem item = null;
		// Generate patched field_ids
		while(patch.hasFieldCommands()) {
			command = patch.getNextFieldCommand();
			if (command.type == 0) {
				// KEEP
				for(int i = 0; i < command.size; ++i) {
					item = original.getFieldIdData();
					fieldIdsFile.write16bit(typeIndexMap[item.classId]);
					fieldIdsFile.write16bit(typeIndexMap[item.typeId]);
					fieldIdsFile.write(stringIndexMap[item.nameId]);
					fieldIndexMap[mapIndex++] = fileIndex++;
				}
			} else if (command.type == 1) {
				// ADD
				for(int i = 0; i < command.size; ++i) {
					fieldIdsFile.write16bit(Long.parseLong(patch.getNextData()));
					fieldIdsFile.write16bit(Long.parseLong(patch.getNextData()));
					fieldIdsFile.write(Long.parseLong(patch.getNextData()));
					++fileIndex;
				}
			} else if (command.type == 2) {
				// DELETE
				for(int i = 0; i < command.size; ++i) {
					original.getFieldIdData();
					fieldIndexMap[mapIndex++] = -1;
				}
			}
		}
	}
	
	/*
	public static void main(String[] args) {
		OriginalFile original = new OriginalFile(args[0]);
		PatchFile patch = new PatchFile(args[1]);
		GeneratedFile generated = new GeneratedFile(args[2]);
		int[] indexMap = new int[100];
		PatchCommand command;
		int fileIndex = 0;
		int mapIndex = 0;
		while(patch.hasCommands()) {
			command = patch.getNextCommand();
			
			if (command.type == 0) {
				// KEEP
				for(int i = 0; i < command.size; ++i) {
					generated.write(original.getTableData());
					indexMap[mapIndex++] = fileIndex++;
				}
			} else if (command.type == 1) {
				// ADD
				for(int i = 0; i < command.size; ++i) {
					generated.write(patch.getData());
					++fileIndex;
				}
			} else if (command.type == 2) {
				// DELETE
				for(int i = 0; i < command.size; ++i) {
					original.getTableData();
					indexMap[mapIndex++] = -1;
				}
			}
		}
		
		System.out.println("DONE");
		
		for (int i = 0; i < mapIndex; ++i) {
			System.out.print(i + " ");
		}
		System.out.println(" ");
		for (int i = 0; i < mapIndex; ++i) {
			System.out.print(indexMap[i] + " ");
		}
		
		while(patch.hasDataCommands()) {
			command = patch.getNextDataCommand();
			if (command.type == 0) {
				// KEEP
				for(int i = 0; i < command.size; ++i) {
					generated.write((char)10);
					generated.write((char)(indexMap[original.getData()] + 48));
				}
			} else if (command.type == 1) {
				// ADD
				for(int i = 0; i < command.size; ++i) {
					generated.write((char)10);
					generated.write(patch.getData());
				}
			} else if (command.type == 2) {
				// DELETE
				for(int i = 0; i < command.size; ++i) {
					System.out.print("Delete: " + indexMap[original.getData()]);
				}
			}
		}
		
		
		
	}*/

}

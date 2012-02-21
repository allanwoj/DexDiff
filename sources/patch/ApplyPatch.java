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
		GeneratedFile tableFile = new GeneratedFile(args[2]);
		GeneratedFile stringFile = new GeneratedFile("out/data.dex");
		int[] indexMap = new int[10000];
		PatchCommand command;
		int fileIndex = 0;
		int mapIndex = 0;
		long current = patch.getOffset();
		tableFile.write(current);
		String buf;
		while(patch.hasCommands()) {
			command = patch.getNextCommand();
			
			if (command.type == 0) {
				// KEEP
				for(int i = 0; i < command.size; ++i) {
					buf = original.getStringData();
					current += buf.length() + 1;
					tableFile.write(current);
					stringFile.write(buf);
					stringFile.write(0);
					indexMap[mapIndex++] = fileIndex++;
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
					tableFile.write(current);
					stringFile.write(buf.length());
					stringFile.write(buf);
					stringFile.write((char)0);
					++fileIndex;
				}
			} else if (command.type == 2) {
				// DELETE
				for(int i = 0; i < command.size; ++i) {
					original.getStringData();
					indexMap[mapIndex++] = -1;
				}
			}
		}
		
		System.out.println("DONE");
		
		/*for (int i = 0; i < mapIndex; ++i) {
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
		*/
		
		
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

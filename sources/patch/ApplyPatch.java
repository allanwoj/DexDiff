package patch;


public class ApplyPatch {

	/**
	 * @param args
	 */
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
		
		
		
	}

}

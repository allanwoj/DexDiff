package patch;

enum PatchCommandType {
	KEEP, ADD, DELETE
}

public class PatchCommand {
	int type;
	int size;
	
	public PatchCommand(int type, int size) {
		this.type = type;
		this.size = size;
	}
}

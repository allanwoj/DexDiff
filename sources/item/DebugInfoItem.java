package item;

import java.util.Collection;

public class DebugInfoItem {

	public long lineStart;
	public long parametersSize;
	public long[] parameterNames;
	public Collection<DebugByteCode> debugByteCode;

	public DebugInfoItem(long lineStart, long parametersSize,
			long[] parameterNames, Collection<DebugByteCode> debugByteCode) {
		this.lineStart = lineStart;
		this.parametersSize = parametersSize;
		this.parameterNames = parameterNames;
		this.debugByteCode = debugByteCode;
	}
	
	
}

package item;

import java.util.ArrayList;
import java.util.Collection;

public class EncodedField {

	public long fieldIdDiff;
	public long flags;

	public EncodedField(long fieldIdDiff, long flags) {
		this.fieldIdDiff = fieldIdDiff;
		this.flags = flags;
	}
}

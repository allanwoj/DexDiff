package generate;

import item.DexItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import patch.DexOriginalFile;
import patch.GeneratedFile;
import patch.MapManager;

public class GeneratePatch {

	public static void main(String[] args) {
		DexOriginalFile original = new DexOriginalFile();
		DexOriginalFile update = new DexOriginalFile();
		original.setDumpOff();
		update.setDumpOff();
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(args[0], "r");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		original.setRandomAccessFile(raf);
		original.parse();
		try {
			raf = new RandomAccessFile(args[1], "r");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		update.setRandomAccessFile(raf);
		update.parse();
		
		GeneratedFile patchFile = new GeneratedFile(args[2]);
		GeneratedFile dataFile = new GeneratedFile("data.diff");
		MapManager mm = new MapManager();
		
		patchFile.write(((Long)update.getStringDataItemOffset()).toString() +"\n");
		patchFile.write(((Long)update.getTypeListOffset()).toString() +"\n");
		patchFile.write(((Long)update.getDebugInfoItemOffset()).toString() +"\n");
		patchFile.write(((Long)update.getCodeItemOffset()).toString() +"\n");
		patchFile.write(((Long)update.getAnnotationItemOffset()).toString() +"\n");
		patchFile.write(((Long)update.getAnnotationSetItemOffset()).toString() +"\n");
		patchFile.write(((Long)update.getAnnotationSetRefListOffset()).toString() +"\n");
		patchFile.write(((Long)update.getAnnotationsDirectoryItemOffset()).toString() +"\n");
		patchFile.write(((Long)update.getClassDataItemOffset()).toString() +"\n");
		patchFile.write(((Long)update.getEncodedArrayItemOffset()).toString() +"\n");
		
		// string_data_item
		List<PCommand> l = lcs(original.stringData, update.stringData, mm);
		writePatchCommands(l, mm.stringIndexMap, patchFile, dataFile);
		
		// type_id
		l = lcs(original.typeIds, update.typeIds, mm);
		writePatchCommands(l, mm.typeIndexMap, patchFile, dataFile);
		
		// field_id_item
		l = lcs(original.fieldIds, update.fieldIds, mm);
		writePatchCommands(l, mm.fieldIndexMap, patchFile, dataFile);
		
		// type_list
		l = lcs(original.typeLists, update.typeLists, mm);
		writePatchCommands(l, mm.typeListIndexMap, patchFile, dataFile);
		
		// proto_id
		l = lcs(original.protoIds, update.protoIds, mm);
		writePatchCommands(l, mm.protoIndexMap, patchFile, dataFile);
		
		// method_id
		l = lcs(original.methodIds, update.methodIds, mm);
		writePatchCommands(l, mm.methodIndexMap, patchFile, dataFile);
		
		// debug_info_item
		l = lcs(original.debugInfoItems, update.debugInfoItems, mm);
		writePatchCommands(l, mm.debugInfoItemMap, patchFile, dataFile);
		
		// code_item
		l = lcs(original.codeItems, update.codeItems, mm);
		writePatchCommands(l, mm.codeItemIndexMap, patchFile, dataFile);
		
		// annotation_item
		l = lcs(original.annotationItems, update.annotationItems, mm);
		writePatchCommands(l, mm.annotationItemIndexMap, patchFile, dataFile);
		
		// annotation_set_item
		l = lcs(original.annotationSetItems, update.annotationSetItems, mm);
		writePatchCommands(l, mm.annotationSetItemIndexMap, patchFile, dataFile);
		
		// annotation_set_ref_list
		l = lcs(original.annotationSetRefList, update.annotationSetRefList, mm);
		writePatchCommands(l, mm.annotationSetRefListIndexMap, patchFile, dataFile);

		// annotations_directory_item
		l = lcs(original.annotationsDirectoryItems, update.annotationsDirectoryItems, mm);
		writePatchCommands(l, mm.annotationsDirectoryItemIndexMap, patchFile, dataFile);
		
		// class_data_item
		l = lcs(original.classDataItems, update.classDataItems, mm);
		writePatchCommands(l, mm.classDataItemIndexMap, patchFile, dataFile);
		
		// encododed_array_item
		l = lcs(original.encodedArrayItems, update.encodedArrayItems, mm);
		writePatchCommands(l, mm.encodedArrayItemIndexMap, patchFile, dataFile);
		
		// class_def_item
		l = lcs(original.classDefItems, update.classDefItems, mm);
		writePatchCommands(l, mm.classDefItemIndexMap, patchFile, dataFile);

		
		byte[] header = update.getHeader();
		dataFile.write((long)header.length);
		dataFile.write(header);
		Collection<Byte> mapList = update.getMapList();
		dataFile.write((long)mapList.size());
		dataFile.write(mapList);
		
		//~~~~~//
		dataFile.write((long)0x0fffffff);
		patchFile.close();
		dataFile.close();
		writeCompleteFile(args[2], "data.diff");
		//~~~~~//
		
		File f = new File("data.diff");
		f.delete();
		System.out.print("Patch Generated");
	}
    
    // DexItem lcs
    public static List<PCommand> lcs(DexItem[] a, DexItem[] b, MapManager mm) {
	    int[][] lengths = new int[a.length+1][b.length+1];
	 
	    // row 0 and column 0 are initialized to 0 already
	 
	    for (int i = 0; i < a.length; i++) {
	        for (int j = 0; j < b.length; j++) {
	        	if (a[i].isEqual(b[j], mm)) {
	        		lengths[i+1][j+1] = lengths[i][j] + 1;
	            } else {
	                lengths[i+1][j+1] =
	                    Math.max(lengths[i+1][j], lengths[i][j+1]);
	            }
	        }
	    }
	    
	    // read the substring out from the matrix
	    List<PCommand> l = new LinkedList<PCommand>();
	    for (int x = a.length, y = b.length;
	         x != 0 || y != 0; ) {
	        if (x > 0 && lengths[x][y] == lengths[x-1][y]) {
	        	l.add(new PCommand(2)); // DELETE
	        	x--;
	        } else if (y > 0 && lengths[x][y] == lengths[x][y-1]) {
	        	l.add(new PCommand(1, b[y-1].getRawData())); // INSERT
	        	y--;
	        } else {
	        	l.add(new PCommand(0)); // KEEP
	        	x--;
	            y--;
	        }
	    }
	 
	    return l;
	}
	
	private static class PCommand {
		public int type;
		List<Byte> data;
		
		public PCommand(int type) {
			this.type = type;
			this.data = null;
		}
		
		public PCommand(int type, List<Byte> data) {
			this.type = type;
			this.data = data;
		}
	}
	
	static void writeCompleteFile(String patchFile, String dataFile) {
		try {
			//GeneratedFile completeFile = new GeneratedFile("out/complete.patch");
			File f1 = new File(patchFile);
			File f2 = new File(dataFile);
		
			InputStream in = new FileInputStream(f2);
			OutputStream out = new FileOutputStream(f1, true);
			
			byte[] buf = new byte[1024];
			  
			int len;
			 
			while ((len = in.read(buf)) > 0){
				out.write(buf, 0, len);
			}
			in.close();

			out.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	static void writePatchCommands(List<PCommand> l, long[] indexMap,
			GeneratedFile patchFile, GeneratedFile dataFile) {
		if (!l.isEmpty()) {
			ListIterator<PCommand> it = l.listIterator(l.size());
			PCommand c = it.previous();
			int fileIndex = 0;
			int mapIndex = 0;
			while (true) {
				int val = c.type;
				int nx = c.type;
				long count = 0;
				while (nx == val) {
					if(nx == 0) {
						indexMap[mapIndex++] = fileIndex++;
					} else if(nx == 1) {
						dataFile.write(c.data);
						++fileIndex;
					} else if(nx == 2) {
						indexMap[mapIndex++] = -1;
					}
					
					++count;
					
					if (!it.hasPrevious())
						break;
					
					c = it.previous();
					nx = c.type;
				}
				patchFile.write(val);
				patchFile.write16bit(count);
				
				if (!it.hasPrevious()) {
					if(nx != val) {
						if(nx == 0) {
							indexMap[mapIndex++] = fileIndex++;
						} else if(nx == 1) {
							dataFile.write(c.data);
							++fileIndex;
						} else if(nx == 2) {
							indexMap[mapIndex++] = -1;
						}
						patchFile.write(val);
						patchFile.write16bit(1L);
					}
					break;
				}
			}
		}
		patchFile.write(4);
	}
	
}

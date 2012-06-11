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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
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
		patchFile.write(((Long)update.getOverflow()).toString() +"\n");
		
		ModifyManager m = new ModifyManager();
		PCommandManager l = new PCommandManager();
		
		lcs(original.stringData, update.stringData, mm, mm.stringIndexMap, l.stringCommands, m.stringMods);
		lcs(original.typeIds, update.typeIds, mm, mm.typeIndexMap, l.typeCommands, m.typeMods);
		lcs(original.fieldIds, update.fieldIds, mm, mm.fieldIndexMap, l.fieldCommands, m.fieldMods);
		lcs(original.typeLists, update.typeLists, mm, mm.typeListIndexMap, l.typeListCommands, m.typeListMods);
		lcs(original.protoIds, update.protoIds, mm, mm.protoIndexMap, l.protoCommands, m.protoMods);
		lcs(original.methodIds, update.methodIds, mm, mm.methodIndexMap, l.methodCommands, m.methodMods);
		lcs(original.debugInfoItems, update.debugInfoItems, mm, mm.debugInfoItemMap, l.debugInfoCommands, m.debugInfoMods);
		lcs(original.codeItems, update.codeItems, mm, mm.codeItemIndexMap, l.codeCommands, m.codeMods);
		lcs(original.annotationItems, update.annotationItems, mm, mm.annotationItemIndexMap, l.annotationCommands, m.annotationMods);
		lcs(original.annotationSetItems, update.annotationSetItems, mm, mm.annotationSetItemIndexMap, l.annotationSetCommands, m.annotationSetMods);
		lcs(original.annotationSetRefList, update.annotationSetRefList, mm, mm.annotationSetRefListIndexMap, l.annotationSetRefListCommands, m.annotationSetRefListMods);
		lcs(original.annotationsDirectoryItems, update.annotationsDirectoryItems, mm, mm.annotationsDirectoryItemIndexMap, l.annotationsDirectoryCommands, m.annotationsDirectoryMods);
		lcs(original.classDataItems, update.classDataItems, mm, mm.classDataItemIndexMap, l.classDataCommands, m.classDataMods);
		lcs(original.encodedArrayItems, update.encodedArrayItems, mm, mm.encodedArrayItemIndexMap, l.encodedArrayCommands, m.encodedArrayMods);
		lcs(original.classDefItems, update.classDefItems, mm, mm.classDefItemIndexMap, l.classDefCommands, m.classDefMods);
		
		PCommand ccc = null;
		int bbb = -1;
		ListIterator<PCommand> itt = l.codeCommands.listIterator(l.codeCommands.size());
		while (itt.hasPrevious()) {
			ccc = itt.previous();
			if (ccc.type == 0 || ccc.type == 2) {
				++bbb;
				System.out.println("bbb[" + bbb + "]: " + ccc.type);
			}
		}
		
		alter(original, update, m, mm, m.stringMods, l.stringCommands, l, mm.stringIndexMap);
		alter(original, update, m, mm, m.typeMods, l.typeCommands, l, mm.typeIndexMap);
		alter(original, update, m, mm, m.fieldMods, l.fieldCommands, l, mm.fieldIndexMap);
		alter(original, update, m, mm, m.typeListMods, l.typeListCommands, l, mm.typeListIndexMap);
		alter(original, update, m, mm, m.protoMods, l.protoCommands, l, mm.protoIndexMap);
		alter(original, update, m, mm, m.methodMods, l.methodCommands, l, mm.methodIndexMap);
		alter(original, update, m, mm, m.debugInfoMods, l.debugInfoCommands, l, mm.debugInfoItemMap);
		alter(original, update, m, mm, m.codeMods, l.codeCommands, l, mm.codeItemIndexMap);
		alter(original, update, m, mm, m.annotationMods, l.annotationCommands, l, mm.annotationItemIndexMap);
		alter(original, update, m, mm, m.annotationSetMods, l.annotationSetCommands, l, mm.annotationSetItemIndexMap);
		alter(original, update, m, mm, m.annotationSetRefListMods, l.annotationSetRefListCommands, l, mm.annotationSetRefListIndexMap);
		alter(original, update, m, mm, m.annotationsDirectoryMods, l.annotationsDirectoryCommands, l, mm.annotationsDirectoryItemIndexMap);
		alter(original, update, m, mm, m.classDataMods, l.classDataCommands, l, mm.classDataItemIndexMap);
		alter(original, update, m, mm, m.encodedArrayMods, l.encodedArrayCommands, l, mm.encodedArrayItemIndexMap);
		alter(original, update, m, mm, m.classDefMods, l.classDefCommands, l, mm.classDefItemIndexMap);
		
		writePatchCommands(l.stringCommands, mm.stringIndexMap, patchFile, dataFile);
		writePatchCommands(l.typeCommands, mm.typeIndexMap, patchFile, dataFile);
		writePatchCommands(l.fieldCommands, mm.fieldIndexMap, patchFile, dataFile);
		writePatchCommands(l.typeListCommands, mm.typeListIndexMap, patchFile, dataFile);
		writePatchCommands(l.protoCommands, mm.protoIndexMap, patchFile, dataFile);
		writePatchCommands(l.methodCommands, mm.methodIndexMap, patchFile, dataFile);
		writePatchCommands(l.debugInfoCommands, mm.debugInfoItemMap, patchFile, dataFile);
		writePatchCommands(l.codeCommands, mm.codeItemIndexMap, patchFile, dataFile);
		writePatchCommands(l.annotationCommands, mm.annotationItemIndexMap, patchFile, dataFile);
		writePatchCommands(l.annotationSetCommands, mm.annotationSetItemIndexMap, patchFile, dataFile);
		writePatchCommands(l.annotationSetRefListCommands, mm.annotationSetRefListIndexMap, patchFile, dataFile);
		writePatchCommands(l.annotationsDirectoryCommands, mm.annotationsDirectoryItemIndexMap, patchFile, dataFile);
		writePatchCommands(l.classDataCommands, mm.classDataItemIndexMap, patchFile, dataFile);
		writePatchCommands(l.encodedArrayCommands, mm.encodedArrayItemIndexMap, patchFile, dataFile);
		writePatchCommands(l.classDefCommands, mm.classDefItemIndexMap, patchFile, dataFile);
		
		
		
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
	
	private static void alter(DexOriginalFile original, DexOriginalFile update, ModifyManager m, MapManager mm, List<Modify> mods, List<PCommand> commands, PCommandManager pm, long[] indexMap) {
		Iterator<Modify> it = mods.iterator();
		Modify modify = null;
		List<Mod> up = new ArrayList<Mod>();
		int bestScore = 0;
		
		PCommand ccc = null;
		int bbb = -1;
		ListIterator<PCommand> itt = commands.listIterator(commands.size());
		while (itt.hasPrevious()) {
			ccc = itt.previous();
			if (ccc.type == 0 || ccc.type == 2) {
				++bbb;
				System.out.println("bbb[" + bbb + "]: " + ccc.type);
			}
		}
		
		while(it.hasNext()) {
			modify = it.next();
			int mappedStart = (int) ((modify.index == 0) ? 0 : (indexMap[modify.index - 1] + 1));
			System.out.println("(" + modify.index + ", " + modify.deleted + ", " + modify.inserted + ")");
			List<Integer> upd = new ArrayList<Integer>();
			int best = 0;
			for (int i = 0; i < modify.deleted; ++i) {
				
				for (int j = 0; j < modify.inserted; ++j) {
					indexMap[modify.index + i] = mappedStart + j;
					int score = modify(original, update, m, mm);
					if (score > bestScore) {
						bestScore = score;
						best = j;
					}
				}
				indexMap[modify.index + i] = mappedStart + best;
				upd.add(best);
				
			}
			up.add(new Mod(modify.index, upd));
		}
		mods.clear();
		
		//TODO update patch commands for affected items
		
		reAdjust(original.typeIds, update.typeIds, m.typeMods, pm.typeCommands, mm, mm.typeIndexMap);
		reAdjust(original.fieldIds, update.fieldIds, m.fieldMods,pm.fieldCommands, mm, mm.fieldIndexMap);
		reAdjust(original.typeLists, update.typeLists, m.typeListMods, pm.typeListCommands, mm, mm.typeListIndexMap);
		reAdjust(original.protoIds, update.protoIds, m.protoMods, pm.protoCommands, mm, mm.protoIndexMap);
		reAdjust(original.methodIds, update.methodIds, m.methodMods, pm.methodCommands, mm, mm.methodIndexMap);
		reAdjust(original.debugInfoItems, update.debugInfoItems, m.debugInfoMods, pm.debugInfoCommands, mm, mm.debugInfoItemMap);
		reAdjust(original.codeItems, update.codeItems, m.codeMods, pm.codeCommands, mm, mm.codeItemIndexMap);
		reAdjust(original.annotationItems, update.annotationItems, m.annotationMods, pm.annotationCommands, mm, mm.annotationItemIndexMap);
		reAdjust(original.annotationSetItems, update.annotationSetItems, m.annotationSetMods, pm.annotationSetCommands, mm, mm.annotationSetItemIndexMap);
		reAdjust(original.annotationSetRefList, update.annotationSetRefList, m.annotationSetRefListMods, pm.annotationSetRefListCommands, mm, mm.annotationSetRefListIndexMap);
		reAdjust(original.annotationsDirectoryItems, update.annotationsDirectoryItems, m.annotationsDirectoryMods, pm.annotationsDirectoryCommands, mm, mm.annotationsDirectoryItemIndexMap);
		reAdjust(original.classDataItems, update.classDataItems, m.classDataMods, pm.classDataCommands, mm, mm.classDataItemIndexMap);
		reAdjust(original.encodedArrayItems, update.encodedArrayItems, m.encodedArrayMods, pm.encodedArrayCommands, mm, mm.encodedArrayItemIndexMap);
		reAdjust(original.classDefItems, update.classDefItems, m.classDefMods, pm.classDefCommands, mm, mm.classDefItemIndexMap);
		
		
		Iterator<Mod> it1 = up.iterator();
		ListIterator<PCommand> it2 = commands.listIterator(commands.size());
		Mod mod = null;
		PCommand c = null;
		int cIndex = commands.size();
		int count = -1;
		while (it1.hasNext()) {
			mod = it1.next();
			System.out.println("T(" + mod.index + ", " + mod.update.toString() + ")");
			while (count < mod.index) {
				c = it2.previous();
				--cIndex;
				if (c.type == 0 || c.type == 2) {
					++count;
					System.out.println("count[" + count + "]: " + c.type);
				}
			}
			
			
			for (int i = 0; i < mod.update.size(); ++i) {
				System.out.println("Type: " + c.type);
				c = it2.previous();
				commands.remove(cIndex);
				--cIndex;
				++count;
				it2 = commands.listIterator(cIndex);
			}
			
			--count;
			
			int prev = 0;
			for (int i = 0; i < mod.update.size(); ++i) {
				while(prev < mod.update.get(i)) {
					c = it2.previous();
					--cIndex;
					++prev;
				}
				commands.add(cIndex + 1, new PCommand(3));
				it2 = commands.listIterator(cIndex);
			}
		}
	}
	
	private static class Mod {
		int index;
		List<Integer> update;
		public Mod(int index, List<Integer> update) {
			this.index = index;
			this.update = update;
		}
	}
	
	private static class Pair {
		int index;
		int update;
		public Pair(int index, int update) {
			this.index = index;
			this.update = update;
		}
	}
	
	private static int modify(DexOriginalFile original, DexOriginalFile update, ModifyManager m, MapManager mm) {
		int score = 0;
		score += getScore(original.typeIds, update.typeIds, m.typeMods, mm, mm.typeIndexMap);
		score += getScore(original.fieldIds, update.fieldIds, m.fieldMods, mm, mm.fieldIndexMap);
		score += getScore(original.typeLists, update.typeLists, m.typeListMods, mm, mm.typeListIndexMap);
		score += getScore(original.protoIds, update.protoIds, m.protoMods, mm, mm.protoIndexMap);
		score += getScore(original.methodIds, update.methodIds, m.methodMods, mm, mm.methodIndexMap);
		score += getScore(original.debugInfoItems, update.debugInfoItems, m.debugInfoMods, mm, mm.debugInfoItemMap);
		score += getScore(original.codeItems, update.codeItems, m.codeMods, mm, mm.codeItemIndexMap);
		score += getScore(original.annotationItems, update.annotationItems, m.annotationMods, mm, mm.annotationItemIndexMap);
		score += getScore(original.annotationSetItems, update.annotationSetItems, m.annotationSetMods, mm, mm.annotationSetItemIndexMap);
		score += getScore(original.annotationSetRefList, update.annotationSetRefList, m.annotationSetRefListMods, mm, mm.annotationSetRefListIndexMap);
		score += getScore(original.annotationsDirectoryItems, update.annotationsDirectoryItems, m.annotationsDirectoryMods, mm, mm.annotationsDirectoryItemIndexMap);
		score += getScore(original.classDataItems, update.classDataItems, m.classDataMods, mm, mm.classDataItemIndexMap);
		score += getScore(original.encodedArrayItems, update.encodedArrayItems, m.encodedArrayMods, mm, mm.encodedArrayItemIndexMap);
		score += getScore(original.classDefItems, update.classDefItems, m.classDefMods, mm, mm.classDefItemIndexMap);
		
		return score;
	}
	
	
	public static int getScore(DexItem[] original, DexItem[] update, List<Modify> m, MapManager mm, long[] indexMap) {
		// proto_item
		Iterator<Modify> it = m.iterator();
		Modify modify = null;
		int score = 0;
		while(it.hasNext()) {
			modify = it.next();
			int mappedStart = (int) ((modify.index == 0) ? 0 : (indexMap[modify.index - 1] + 1));
			for (int i = 0; i < modify.deleted; ++i) {
				for (int j = 0; j < modify.inserted; ++j) {
					if (original[modify.index + i].isEqual(update[mappedStart + j], mm)) {
						++score;
					}
				}
			}
		}
		return score;
	}
	
	
	public static void reAdjust(DexItem[] original, DexItem[] update, List<Modify> m, List<PCommand> commands, MapManager mm, long[] indexMap) {
		// proto_item
		Iterator<Modify> it = m.iterator();
		Modify modify = null;
		List<Mod> changes = new ArrayList<Mod>();
		int count = 0;
		while(it.hasNext()) {
			modify = it.next();
			List<Integer> upd = new ArrayList<Integer>();
			boolean hasChanged = false;
			int mappedStart = (int) ((modify.index == 0) ? 0 : (indexMap[modify.index - 1] + 1));
			for (int i = 0; i < modify.deleted; ++i) {
				int best = -1;
				for (int j = 0; j < modify.inserted; ++j) {
					if (original[modify.index + i].isEqual(update[mappedStart + j], mm)) {
						best = j;
						hasChanged = true;
					}
				}
				upd.add(best);
			}
			if (hasChanged) {
				modify = m.remove(count);
				--count;
				changes.add(new Mod(modify.index, upd));
				
				int tempIndex = modify.index;
				int available = modify.inserted;
				int lastTemp = -1;
				Iterator<Integer> iter = upd.iterator();
				while (tempIndex < modify.index + modify.deleted && available > 0) {
					int counter = 0;
					
					int temp = 0;
					while (iter.hasNext()) {
						temp = iter.next().intValue();
						if (temp != -1) {
							break;
						}
						++counter;
					}
					
					if (temp == -1)
						temp = modify.inserted;
					
					if (counter > 0 && (temp > lastTemp + 1)) {
						++count;
						m.add(count, new Modify(tempIndex, temp - lastTemp - 1, counter));
					}
					available = modify.inserted - temp - 1;
					tempIndex += 1 + counter;
					lastTemp = temp;
				}
				
				it = m.listIterator(count + 1);
			}
			++count;
		}
		
		Iterator<Mod> it1 = changes.iterator();
		ListIterator<PCommand> it2 = commands.listIterator(commands.size());
		Mod mod = null;
		PCommand c = null;
		int cIndex = commands.size();
		int cc = -1;
		while (it1.hasNext()) {
			mod = it1.next();
			//System.out.println("T(" + mod.index + ", " + mod.update.toString() + ")");
			while (cc < mod.index) {
				c = it2.previous();
				--cIndex;
				if (c.type == 0 || c.type == 2) {
					++cc;
					//System.out.println("cc[" + cc + "]: " + c.type);
				}
			}
			
			for (int i = 0; i < mod.update.size(); ++i) {
				//System.out.println("TType: " + c.type);
				c = it2.previous();
				commands.remove(cIndex);
				--cIndex;
				++cc;
				it2 = commands.listIterator(cIndex);
			}
			--cc;
			
			
			int prev = 0;
			boolean lastDel = false;
			for (int i = 0; i < mod.update.size(); ++i) {
				if (mod.update.get(i) == -1) {
					commands.add(cIndex + 1, new PCommand(2));
					lastDel = true;
				} else {
					while (prev < mod.update.get(i)) {
						c = it2.previous();
						--cIndex;
						++prev;
					}
					commands.remove(cIndex);
					commands.add(cIndex, new PCommand(0));
					++prev;
					--cIndex;
					lastDel = false;
				}
				
				it2 = commands.listIterator(cIndex);
			}
			
			if (!lastDel) {
				++cIndex;
				it2 = commands.listIterator(cIndex);
			}
		}
		
	}

	// DexItem lcs
	public static void lcs(DexItem[] a, DexItem[] b, MapManager mm, long[] indexMap, List<PCommand> l, List<Modify> m) {
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
	    
	    int inserted = 0;
	    int deleted = 0;
	    
	    int mapIndex = a.length - 1;
	    int fileIndex = b.length - 1;
	    // read the substring out from the matrix
	    //List<PCommand> l = new LinkedList<PCommand>();
	    for (int x = a.length, y = b.length;
	         x != 0 || y != 0; ) {
	    	if (y > 0 && lengths[x][y] == lengths[x][y-1]) {
		       	l.add(new PCommand(1, b[y-1].getRawData())); // INSERT
		       	++inserted;
		       	y--;
		       	--fileIndex;
		    }else if (x > 0 && lengths[x][y] == lengths[x-1][y]) {
	        	l.add(new PCommand(2)); // DELETE
	        	indexMap[mapIndex--] = -1;
	        	++deleted;
	        	x--;
	        } else {
	        	l.add(new PCommand(0)); // KEEP
	        	if (inserted > 0 && deleted > 0) {
	        		m.add(0, new Modify(mapIndex + 1, inserted, deleted));
	        	}
	        	x--;
	            y--;
	            indexMap[mapIndex--] = fileIndex--;
	            inserted = 0;
	            deleted = 0;
	        }
	    }
	    
	    if (inserted > 0 && deleted > 0) {
    		m.add(new Modify(mapIndex, inserted, deleted));
    	}
	}
	
	private static class Modify {
		public int index;
		public int inserted;
		public int deleted;
		
		public Modify(int index, int inserted, int deleted) {
			this.index = index;
			this.inserted = inserted;
			this.deleted = deleted;
		}
	}
	
	
	private static class ModifyManager {
		List<Modify> stringMods = new ArrayList<Modify>();
		List<Modify> typeMods = new ArrayList<Modify>();
		List<Modify> fieldMods = new ArrayList<Modify>();
		List<Modify> typeListMods = new ArrayList<Modify>();
		List<Modify> protoMods = new ArrayList<Modify>();
		List<Modify> methodMods = new ArrayList<Modify>();
		List<Modify> debugInfoMods = new ArrayList<Modify>();
		List<Modify> codeMods = new ArrayList<Modify>();
		List<Modify> annotationMods = new ArrayList<Modify>();
		List<Modify> annotationSetMods = new ArrayList<Modify>();
		List<Modify> annotationSetRefListMods = new ArrayList<Modify>();
		List<Modify> annotationsDirectoryMods = new ArrayList<Modify>();
		List<Modify> classDataMods = new ArrayList<Modify>();
		List<Modify> encodedArrayMods = new ArrayList<Modify>();
		List<Modify> classDefMods = new ArrayList<Modify>();
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
	
	private static class PCommandManager {
		List<PCommand> stringCommands = new ArrayList<PCommand>();
		List<PCommand> typeCommands = new ArrayList<PCommand>();
		List<PCommand> fieldCommands = new ArrayList<PCommand>();
		List<PCommand> typeListCommands = new ArrayList<PCommand>();
		List<PCommand> protoCommands = new ArrayList<PCommand>();
		List<PCommand> methodCommands = new ArrayList<PCommand>();
		List<PCommand> debugInfoCommands = new ArrayList<PCommand>();
		List<PCommand> codeCommands = new ArrayList<PCommand>();
		List<PCommand> annotationCommands = new ArrayList<PCommand>();
		List<PCommand> annotationSetCommands = new ArrayList<PCommand>();
		List<PCommand> annotationSetRefListCommands = new ArrayList<PCommand>();
		List<PCommand> annotationsDirectoryCommands = new ArrayList<PCommand>();
		List<PCommand> classDataCommands = new ArrayList<PCommand>();
		List<PCommand> encodedArrayCommands = new ArrayList<PCommand>();
		List<PCommand> classDefCommands = new ArrayList<PCommand>();
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
						//indexMap[mapIndex++] = fileIndex++;
					} else if(nx == 1) {
						dataFile.write(c.data);
						++fileIndex;
					} else if(nx == 2) {
						//indexMap[mapIndex++] = -1;
					} else if(nx == 3) {
						//indexMap[mapIndex++] = ++fileIndex;
						//--fileindex;
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
							//indexMap[mapIndex++] = fileIndex++;
						} else if(nx == 1) {
							dataFile.write(c.data);
							++fileIndex;
						} else if(nx == 2) {
							//indexMap[mapIndex++] = -1;
						} else if(nx == 3) {
							//indexMap[mapIndex++] = ++fileIndex;
							//--fileindex;
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

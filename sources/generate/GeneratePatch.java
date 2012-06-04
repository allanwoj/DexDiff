package generate;

import item.AnnotationItem;
import item.AnnotationSetItem;
import item.AnnotationSetRefList;
import item.AnnotationsDirectoryItem;
import item.ClassDataItem;
import item.ClassDefItem;
import item.CodeItem;
import item.DebugByteCode;
import item.DebugInfoItem;
import item.EncodedArray;
import item.FieldIdItem;
import item.MethodIdItem;
import item.ProtoIdItem;
import item.TypeList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
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
		
		String[] data1 = { "a", "b", "c", "d", "e", "f", "g" };
		String[] data2 = { "a", "f", "b", "c", "e", "f", "g", "s" };
		List<PCommand> l = lcs2(original.stringData, update.stringData);
		//List<PCommand> l = lcs2(data1, data2);
		
		// string_data_item
		ListIterator<PCommand> it = l.listIterator(l.size());
		PCommand c = it.previous();
		int fileIndex = 0;
		int mapIndex = 0;
		while (true) {
			int val = c.type;
			int nx = c.type;
			long count = 0;
			while (nx == val) {
				if (nx == 0) {
					mm.stringIndexMap[mapIndex++] = fileIndex++;
				} else if (nx == 1) {
					dataFile.write(c.data);
					++fileIndex;
				} else if (nx == 2) {
					mm.stringIndexMap[mapIndex++] = -1;
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
					if (nx == 0) {
						mm.stringIndexMap[mapIndex++] = fileIndex++;
					} else if (nx == 1) {
						dataFile.write(c.data);
						++fileIndex;
					} else if (nx == 2) {
						mm.stringIndexMap[mapIndex++] = -1;
					}
					patchFile.write(val);
					patchFile.write16bit(1L);
				}
				break;
			}
		}
		
		patchFile.write(4);
		
		// type_id
		l = lcs2(original.typeIds, update.typeIds, mm.stringIndexMap);
		it = l.listIterator(l.size());
		c = it.previous();
		fileIndex = 0;
		mapIndex = 0;
		while (true) {
			int val = c.type;
			int nx = c.type;
			long count = 0;
			while (nx == val) {
				if (nx == 0) {
					mm.typeIndexMap[mapIndex++] = fileIndex++;
				} else if (nx == 1) {
					dataFile.write(4L);
					dataFile.write((long)c.index);
					++fileIndex;
				} else if (nx == 2) {
					mm.typeIndexMap[mapIndex++] = -1;
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
					if (nx == 0) {
						mm.typeIndexMap[mapIndex++] = fileIndex++;
					} else if (nx == 1) {
						dataFile.write(4L);
						dataFile.write((long)c.index);
						++fileIndex;
					} else if (nx == 2) {
						mm.typeIndexMap[mapIndex++] = -1;
					}
					patchFile.write(val);
					patchFile.write16bit(1L);
				}
				break;
			}
		}
		
		patchFile.write(4);
		
		// field_id_item
		l = lcs2(original.fieldIds, update.fieldIds, mm);
		it = l.listIterator(l.size());
		c = it.previous();
		fileIndex = 0;
		mapIndex = 0;
		while (true) {
			int val = c.type;
			int nx = c.type;
			long count = 0;
			while (nx == val) {
				if (nx == 0) {
					mm.fieldIndexMap[mapIndex++] = fileIndex++;
				} else if (nx == 1) {
					dataFile.write(c.data);
					++fileIndex;
				} else if (nx == 2) {
					mm.fieldIndexMap[mapIndex++] = -1;
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
					if (nx == 0) {
						mm.fieldIndexMap[mapIndex++] = fileIndex++;
					} else if (nx == 1) {
						dataFile.write(c.data);
						++fileIndex;
					} else if (nx == 2) {
						mm.fieldIndexMap[mapIndex++] = -1;
					}
					patchFile.write(val);
					patchFile.write16bit(1L);
				}
				break;
			}
		}
		
		patchFile.write(4);
		
		// type_list
		l = lcs2(original.typeLists, update.typeLists, mm);
		it = l.listIterator(l.size());
		c = it.previous();
		fileIndex = 0;
		mapIndex = 0;
		while (true) {
			int val = c.type;
			int nx = c.type;
			long count = 0;
			while (nx == val) {
				if(nx == 0) {
					mm.typeListIndexMap[mapIndex++] = fileIndex++;
				} else if(nx == 1) {
					dataFile.write(c.data);
					++fileIndex;
				} else if(nx == 2) {
					mm.typeListIndexMap[mapIndex++] = -1;
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
						mm.typeListIndexMap[mapIndex++] = fileIndex++;
					} else if(nx == 1) {
						dataFile.write(c.data);
						++fileIndex;
					} else if(nx == 2) {
						mm.typeListIndexMap[mapIndex++] = -1;
					}
					patchFile.write(val);
					patchFile.write16bit(1L);
				}
				break;
			}
		}
		
		patchFile.write(4);
		
		// proto_id
		l = lcs2(original.protoIds, update.protoIds, mm);
		it = l.listIterator(l.size());
		c = it.previous();
		fileIndex = 0;
		mapIndex = 0;
		while (true) {
			int val = c.type;
			int nx = c.type;
			long count = 0;
			while (nx == val) {
				if (nx == 0) {
					mm.protoIndexMap[mapIndex++] = fileIndex++;
				} else if (nx == 1) {
					dataFile.write(c.data);
					++fileIndex;
				} else if (nx == 2) {
					mm.protoIndexMap[mapIndex++] = -1;
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
					if (nx == 0) {
						mm.protoIndexMap[mapIndex++] = fileIndex++;
					} else if (nx == 1) {
						dataFile.write(c.data);
						++fileIndex;
					} else if (nx == 2) {
						mm.protoIndexMap[mapIndex++] = -1;
					}
					patchFile.write(val);
					patchFile.write16bit(1L);
				}
				break;
			}
		}
		
		patchFile.write(4);
		
		// method_id
		l = lcs2(original.methodIds, update.methodIds, mm);
		it = l.listIterator(l.size());
		c = it.previous();
		fileIndex = 0;
		mapIndex = 0;
		while (true) {
			int val = c.type;
			int nx = c.type;
			long count = 0;
			while (nx == val) {
				if (nx == 0) {
					mm.methodIndexMap[mapIndex++] = fileIndex++;
				} else if (nx == 1) {
					dataFile.write(c.data);
					++fileIndex;
				} else if (nx == 2) {
					mm.methodIndexMap[mapIndex++] = -1;
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
					if (nx == 0) {
						mm.methodIndexMap[mapIndex++] = fileIndex++;
					} else if (nx == 1) {
						dataFile.write(c.data);
						++fileIndex;
					} else if (nx == 2) {
						mm.methodIndexMap[mapIndex++] = -1;
					}
					patchFile.write(val);
					patchFile.write16bit(1L);
				}
				break;
			}
		}
		
		
		patchFile.write(4);
		
		// debug_info_item
		l = lcs2(original.debugInfoItems, update.debugInfoItems, mm);
		it = l.listIterator(l.size());
		c = it.previous();
		fileIndex = 0;
		mapIndex = 0;
		while (true) {
			int val = c.type;
			int nx = c.type;
			long count = 0;
			while (nx == val) {
				if (nx == 0) {
					mm.debugInfoItemMap[mapIndex++] = fileIndex++;
				} else if (nx == 1) {
					dataFile.write(c.data);
					++fileIndex;
				} else if (nx == 2) {
					mm.debugInfoItemMap[mapIndex++] = -1;
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
					if (nx == 0) {
						mm.debugInfoItemMap[mapIndex++] = fileIndex++;
					} else if (nx == 1) {
						dataFile.write(c.data);
						++fileIndex;
					} else if (nx == 2) {
						mm.debugInfoItemMap[mapIndex++] = -1;
					}
					patchFile.write(val);
					patchFile.write16bit(1L);
				}
				break;
			}
		}
		
		patchFile.write(4);
		
		
		
		// code_item
		l = lcs2(original.codeItems, update.codeItems, mm);
		it = l.listIterator(l.size());
		c = it.previous();
		fileIndex = 0;
		mapIndex = 0;
		while (true) {
			int val = c.type;
			int nx = c.type;
			long count = 0;
			while (nx == val) {
				if (nx == 0) {
					mm.codeItemIndexMap[mapIndex++] = fileIndex++;
				} else if (nx == 1) {
					dataFile.write(c.data);
					++fileIndex;
				} else if (nx == 2) {
					mm.codeItemIndexMap[mapIndex++] = -1;
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
					if (nx == 0) {
						mm.codeItemIndexMap[mapIndex++] = fileIndex++;
					} else if (nx == 1) {
						dataFile.write(c.data);
						++fileIndex;
					} else if (nx == 2) {
						mm.codeItemIndexMap[mapIndex++] = -1;
					}
					patchFile.write(val);
					patchFile.write16bit(1L);
				}
				break;
			}
		}
		
		patchFile.write(4);
		
		
		// annotation_item
		l = lcs2(original.annotationItems, update.annotationItems, mm);
		it = l.listIterator(l.size());
		c = it.previous();
		fileIndex = 0;
		mapIndex = 0;
		while (true) {
			int val = c.type;
			int nx = c.type;
			long count = 0;
			while (nx == val) {
				if (nx == 0) {
					mm.annotationItemIndexMap[mapIndex++] = fileIndex++;
				} else if (nx == 1) {
					dataFile.write(c.data);
					++fileIndex;
				} else if (nx == 2) {
					mm.annotationItemIndexMap[mapIndex++] = -1;
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
					if (nx == 0) {
						mm.annotationItemIndexMap[mapIndex++] = fileIndex++;
					} else if (nx == 1) {
						dataFile.write(c.data);
						++fileIndex;
					} else if (nx == 2) {
						mm.annotationItemIndexMap[mapIndex++] = -1;
					}
					patchFile.write(val);
					patchFile.write16bit(1L);
				}
				break;
			}
		}
		
		patchFile.write(4);
		
		// annotation_set_item
		l = lcs2(original.annotationSetItems, update.annotationSetItems, mm);
		it = l.listIterator(l.size());
		c = it.previous();
		fileIndex = 0;
		mapIndex = 0;
		while (true) {
			int val = c.type;
			int nx = c.type;
			long count = 0;
			while (nx == val) {
				if (nx == 0) {
					mm.annotationSetItemIndexMap[mapIndex++] = fileIndex++;
				} else if (nx == 1) {
					dataFile.write(c.data);
					++fileIndex;
				} else if (nx == 2) {
					mm.annotationSetItemIndexMap[mapIndex++] = -1;
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
					if (nx == 0) {
						mm.annotationSetItemIndexMap[mapIndex++] = fileIndex++;
					} else if (nx == 1) {
						dataFile.write(c.data);
						++fileIndex;
					} else if (nx == 2) {
						mm.annotationSetItemIndexMap[mapIndex++] = -1;
					}
					patchFile.write(val);
					patchFile.write16bit(1L);
				}
				break;
			}
		}
		
		patchFile.write(4);
		
		// annotation_set_ref_list
		l = lcs2(original.annotationSetRefList, update.annotationSetRefList, mm);
		if (!l.isEmpty()) {
			it = l.listIterator(l.size());
			c = it.previous();
			fileIndex = 0;
			mapIndex = 0;
			while (true) {
				int val = c.type;
				int nx = c.type;
				long count = 0;
				while (nx == val) {
					if (nx == 0) {
						mm.annotationSetRefListIndexMap[mapIndex++] = fileIndex++;
					} else if (nx == 1) {
						dataFile.write(c.data);
						++fileIndex;
					} else if (nx == 2) {
						mm.annotationSetRefListIndexMap[mapIndex++] = -1;
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
						if (nx == 0) {
							mm.annotationSetRefListIndexMap[mapIndex++] = fileIndex++;
						} else if (nx == 1) {
							dataFile.write(c.data);
							++fileIndex;
						} else if (nx == 2) {
							mm.annotationSetRefListIndexMap[mapIndex++] = -1;
						}
						patchFile.write(val);
						patchFile.write16bit(1L);
					}
					break;
				}
			}
		}
		
		patchFile.write(4);
		
		// annotations_directory_item
		l = lcs2(original.annotationsDirectoryItems, update.annotationsDirectoryItems, mm);
		it = l.listIterator(l.size());
		c = it.previous();
		fileIndex = 0;
		mapIndex = 0;
		while (true) {
			int val = c.type;
			int nx = c.type;
			long count = 0;
			while (nx == val) {
				if (nx == 0) {
					mm.annotationsDirectoryItemIndexMap[mapIndex++] = fileIndex++;
				} else if (nx == 1) {
					dataFile.write(c.data);
					++fileIndex;
				} else if (nx == 2) {
					mm.annotationsDirectoryItemIndexMap[mapIndex++] = -1;
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
					if (nx == 0) {
						mm.annotationsDirectoryItemIndexMap[mapIndex++] = fileIndex++;
					} else if (nx == 1) {
						dataFile.write(c.data);
						++fileIndex;
					} else if (nx == 2) {
						mm.annotationsDirectoryItemIndexMap[mapIndex++] = -1;
					}
					patchFile.write(val);
					patchFile.write16bit(1L);
				}
				break;
			}
		}
		
		patchFile.write(4);
		
		
		
		// class_data_item
		l = lcs2(original.classDataItems, update.classDataItems, mm);
		it = l.listIterator(l.size());
		c = it.previous();
		long[] classDataItemIndexMap = new long[10000];
		fileIndex = 0;
		mapIndex = 0;
		while (true) {
			int val = c.type;
			int nx = c.type;
			long count = 0;
			while (nx == val) {
				if (nx == 0) {
					classDataItemIndexMap[mapIndex++] = fileIndex++;
				} else if (nx == 1) {
					dataFile.write(c.data);
					++fileIndex;
				} else if (nx == 2) {
					classDataItemIndexMap[mapIndex++] = -1;
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
					if (nx == 0) {
						classDataItemIndexMap[mapIndex++] = fileIndex++;
					} else if (nx == 1) {
						dataFile.write(c.data);
						++fileIndex;
					} else if (nx == 2) {
						classDataItemIndexMap[mapIndex++] = -1;
					}
					patchFile.write(val);
					patchFile.write16bit(1L);
				}
				break;
			}
		}
		
		patchFile.write(4);
		
		// encododed_array_item
		l = lcs2(original.encodedArrayItems, update.encodedArrayItems, mm);
		fileIndex = 0;
		mapIndex = 0;
		if (!l.isEmpty()) {
			it = l.listIterator(l.size());
			c = it.previous();
			while (true) {
				int val = c.type;
				int nx = c.type;
				long count = 0;
				while (nx == val) {
					if (nx == 0) {
						mm.encodedArrayItemIndexMap[mapIndex++] = fileIndex++;
					} else if (nx == 1) {
						dataFile.write(c.data);
						++fileIndex;
					} else if (nx == 2) {
						mm.encodedArrayItemIndexMap[mapIndex++] = -1;
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
						if (nx == 0) {
							mm.encodedArrayItemIndexMap[mapIndex++] = fileIndex++;
						} else if (nx == 1) {
							dataFile.write(c.data);
							++fileIndex;
						} else if (nx == 2) {
							mm.encodedArrayItemIndexMap[mapIndex++] = -1;
						}
						patchFile.write(val);
						patchFile.write16bit(1L);
					}
					break;
				}
			}
		}
		
		patchFile.write(4);
		
		
		
		// class_def_item
		l = lcs2(original.classDefItems, update.classDefItems, mm);
		it = l.listIterator(l.size());
		c = it.previous();
		fileIndex = 0;
		mapIndex = 0;
		while (true) {
			int val = c.type;
			int nx = c.type;
			long count = 0;
			while (nx == val) {
				if (nx == 0) {
					mm.classDefItemIndexMap[mapIndex++] = fileIndex++;
				} else if (nx == 1) {
					dataFile.write(c.data);
					++fileIndex;
				} else if (nx == 2) {
					mm.classDefItemIndexMap[mapIndex++] = -1;
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
					if (nx == 0) {
						mm.classDefItemIndexMap[mapIndex++] = fileIndex++;
					} else if (nx == 1) {
						dataFile.write(c.data);
						++fileIndex;
					} else if (nx == 2) {
						mm.classDefItemIndexMap[mapIndex++] = -1;
					}
					patchFile.write(val);
					patchFile.write16bit(1L);
				}
				break;
			}
		}
		
		patchFile.write(4);
		
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
	
	
	public static List<PCommand> lcs2(String[] a, String[] b) {
	    int[][] lengths = new int[a.length+1][b.length+1];
	 
	    // row 0 and column 0 are initialized to 0 already
	 
	    for (int i = 0; i < a.length; i++)
	        for (int j = 0; j < b.length; j++)
	            if (a[i].equals(b[j]))
	                lengths[i+1][j+1] = lengths[i][j] + 1;
	            else
	                lengths[i+1][j+1] =
	                    Math.max(lengths[i+1][j], lengths[i][j+1]);
	 
	    // read the substring out from the matrix
	    List<PCommand> l = new LinkedList<PCommand>();
	    for (int x = a.length, y = b.length;
	         x != 0 && y != 0; ) {
	        if (lengths[x][y] == lengths[x-1][y]) {
	        	l.add(new PCommand(2));
	        	x--;
	        } else if (lengths[x][y] == lengths[x][y-1]) {
	        	byte[] by = new byte[b[y-1].length() + 3];
	        	int size = b[y-1].length() - 1;
	        	by[0] = (byte) size;
	        	by[1] = (byte) (size >> 8);
	        	by[2] = (byte) (size >> 16);
	        	by[3] = (byte) (size >> 24);
	        	b[y-1].getBytes(1, b[y-1].length(), by, 4);
	        	l.add(new PCommand(1, by));
	        	y--;
	            
	        } else {
	        	l.add(new PCommand(0));
	        	
	        	x--;
	            y--;
	        }
	    }
	 
	    return l;
	}
	
	public static List<PCommand> lcs2(int[] a, int[] b, long[] stringIndexMap) {
	    int[][] lengths = new int[a.length+1][b.length+1];
	 
	    // row 0 and column 0 are initialized to 0 already
	 
	    for (int i = 0; i < a.length; i++)
	        for (int j = 0; j < b.length; j++)
	            if (stringIndexMap[a[i]] == b[j])
	                lengths[i+1][j+1] = lengths[i][j] + 1;
	            else
	                lengths[i+1][j+1] =
	                    Math.max(lengths[i+1][j], lengths[i][j+1]);
	 
	    // read the substring out from the matrix
	    List<PCommand> l = new LinkedList<PCommand>();
	    for (int x = a.length, y = b.length;
	         x != 0 && y != 0; ) {
	        if (lengths[x][y] == lengths[x-1][y]) {
	        	l.add(new PCommand(2));
	        	x--;
	        } else if (lengths[x][y] == lengths[x][y-1]) {
	        	l.add(new PCommand(1, b[y-1]));
	        	y--;
	            
	        } else {
	        	l.add(new PCommand(0));
	        	
	        	x--;
	            y--;
	        }
	    }
	 
	    return l;
	}
	
	public static List<PCommand> lcs2(FieldIdItem[] a, FieldIdItem[] b, MapManager mm) {
	    int[][] lengths = new int[a.length+1][b.length+1];
	 
	    // row 0 and column 0 are initialized to 0 already
	 
	    for (int i = 0; i < a.length; i++)
	        for (int j = 0; j < b.length; j++)
	            if (a[i].isEqual(b[j], mm)) {
	                lengths[i+1][j+1] = lengths[i][j] + 1;
	            } else {
	                lengths[i+1][j+1] =
	                    Math.max(lengths[i+1][j], lengths[i][j+1]);
	            }
	 
	    // read the substring out from the matrix
	    List<PCommand> l = new LinkedList<PCommand>();
	    for (int x = a.length, y = b.length;
	         x != 0 && y != 0; ) {
	        if (lengths[x][y] == lengths[x-1][y]) {
	        	l.add(new PCommand(2));
	        	x--;
	        } else if (lengths[x][y] == lengths[x][y-1]) {
	        	byte[] by = b[y-1].getOutput(true);
	        	
	        	l.add(new PCommand(1, by));
	        	y--;
	            
	        } else {
	        	l.add(new PCommand(0));
	        	
	        	x--;
	            y--;
	        }
	    }
	 
	    return l;
	}
	
	public static List<PCommand> lcs2(TypeList[] a, TypeList[] b, MapManager mm) {
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
	         x != 0 && y != 0; ) {
	        if (lengths[x][y] == lengths[x-1][y]) {
	        	l.add(new PCommand(2));
	        	x--;
	        } else if (lengths[x][y] == lengths[x][y-1]) {
	        	byte[] by = b[y-1].getOutput(true);
	        	l.add(new PCommand(1, by));
	        	y--;
	            
	        } else {
	        	l.add(new PCommand(0));
	        	
	        	x--;
	            y--;
	        }
	    }
	 
	    return l;
	}
	
	public static List<PCommand> lcs2(ProtoIdItem[] a, ProtoIdItem[] b, MapManager mm) {
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
	         x != 0 && y != 0; ) {
	        if (lengths[x][y] == lengths[x-1][y]) {
	        	l.add(new PCommand(2));
	        	x--;
	        } else if (lengths[x][y] == lengths[x][y-1]) {
	        	byte[] by = b[y-1].getOutput(true);
	        	l.add(new PCommand(1, by));
	        	y--;
	        } else {
	        	l.add(new PCommand(0));
	        	
	        	x--;
	            y--;
	        }
	    }
	 
	    return l;
	}
	
	 // method ids
    public static List<PCommand> lcs2(MethodIdItem[] a, MethodIdItem[] b, MapManager mm) {
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
	         x != 0 && y != 0; ) {
	        if (lengths[x][y] == lengths[x-1][y]) {
	        	l.add(new PCommand(2));
	        	x--;
	        } else if (lengths[x][y] == lengths[x][y-1]) {
	        	byte[] by = b[y-1].getOutput(true);
	        	l.add(new PCommand(1, by));
	        	y--;
	            
	        } else {
	        	l.add(new PCommand(0));
	        	
	        	x--;
	            y--;
	        }
	    }
	 
	    return l;
	}
    
    // debug_info_item
    public static List<PCommand> lcs2(DebugInfoItem[] a, DebugInfoItem[] b, MapManager mm) {
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
	         x != 0 && y != 0; ) {
	        if (lengths[x][y] == lengths[x-1][y]) {
	        	l.add(new PCommand(2));
	        	x--;
	        } else if (lengths[x][y] == lengths[x][y-1]) {
	        	l.add(new PCommand(1, b[y-1].getByteCode(true)));
	        	y--;
	            
	        } else {
	        	l.add(new PCommand(0));
	        	
	        	x--;
	            y--;
	        }
	    }
	 
	    return l;
	}
    
    
    // code_item
    public static List<PCommand> lcs2(CodeItem[] a, CodeItem[] b, MapManager mm) {
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
	        	l.add(new PCommand(2));
	        	x--;
	        } else if (y > 0 && lengths[x][y] == lengths[x][y-1]) {
	        	l.add(new PCommand(1, b[y-1].getNaiveOutput(true)));
	        	y--;
	            
	        } else {
	        	l.add(new PCommand(0));
	        	
	        	x--;
	            y--;
	        }
	    }
	 
	    return l;
	}
    
    // annotation_item
    public static List<PCommand> lcs2(AnnotationItem[] a, AnnotationItem[] b, MapManager mm) {
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
	        	l.add(new PCommand(2));
	        	x--;
	        } else if (y > 0 && lengths[x][y] == lengths[x][y-1]) {
	        	l.add(new PCommand(1, b[y-1].getOutput()));
	        	y--;
	            
	        } else {
	        	l.add(new PCommand(0));
	        	
	        	x--;
	            y--;
	        }
	    }
	 
	    return l;
	}
    
    // annotation_set_item
    public static List<PCommand> lcs2(AnnotationSetItem[] a, AnnotationSetItem[] b, MapManager mm) {
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
	        	l.add(new PCommand(2));
	        	x--;
	        } else if (y > 0 && lengths[x][y] == lengths[x][y-1]) {
	        	l.add(new PCommand(1, b[y-1].getOutput()));
	        	y--;
	            
	        } else {
	        	l.add(new PCommand(0));
	        	
	        	x--;
	            y--;
	        }
	    }
	 
	    return l;
	}
    
    // annotation_set_ref_list
    public static List<PCommand> lcs2(AnnotationSetRefList[] a, AnnotationSetRefList[] b, MapManager mm) {
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
	        	l.add(new PCommand(2));
	        	x--;
	        } else if (y > 0 && lengths[x][y] == lengths[x][y-1]) {
	        	l.add(new PCommand(1, b[y-1].getOutput()));
	        	y--;
	            
	        } else {
	        	l.add(new PCommand(0));
	        	
	        	x--;
	            y--;
	        }
	    }
	 
	    return l;
	}
    
    
    // annotations_directory_item
    public static List<PCommand> lcs2(AnnotationsDirectoryItem[] a, AnnotationsDirectoryItem[] b, MapManager mm) {
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
	        	l.add(new PCommand(2));
	        	x--;
	        } else if (y > 0 && lengths[x][y] == lengths[x][y-1]) {
	        	l.add(new PCommand(1, b[y-1].getOutput()));
	        	y--;
	            
	        } else {
	        	l.add(new PCommand(0));
	        	
	        	x--;
	            y--;
	        }
	    }
	 
	    return l;
	}
    
    // class_data_item
    public static List<PCommand> lcs2(ClassDataItem[] a, ClassDataItem[] b, MapManager mm) {
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
	        	l.add(new PCommand(2));
	        	x--;
	        } else if (y > 0 && lengths[x][y] == lengths[x][y-1]) {
	        	l.add(new PCommand(1, b[y-1].getOutput()));
	        	y--;
	            
	        } else {
	        	l.add(new PCommand(0));
	        	
	        	x--;
	            y--;
	        }
	    }
	 
	    return l;
	}
    
    // encoded_array_item
    public static List<PCommand> lcs2(EncodedArray[] a, EncodedArray[] b, MapManager mm) {
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
	        	l.add(new PCommand(2));
	        	x--;
	        } else if (y > 0 && lengths[x][y] == lengths[x][y-1]) {
	        	l.add(new PCommand(1, b[y-1].getOutput()));
	        	y--;
	            
	        } else {
	        	l.add(new PCommand(0));
	        	
	        	x--;
	            y--;
	        }
	    }
	 
	    return l;
	}
    
    // class_def_item
    public static List<PCommand> lcs2(ClassDefItem[] a, ClassDefItem[] b, MapManager mm) {
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
	        	l.add(new PCommand(2));
	        	x--;
	        } else if (y > 0 && lengths[x][y] == lengths[x][y-1]) {
	        	l.add(new PCommand(1, b[y-1].getOutput()));
	        	y--;
	            
	        } else {
	        	l.add(new PCommand(0));
	        	
	        	x--;
	            y--;
	        }
	    }
	 
	    return l;
	}
	
	private static class PCommand {
		public int type;
		byte[] data;
		int index;
		
		public PCommand(int type) {
			this.type = type;
			this.data = null;
		}
		
		public PCommand(int type, byte[] data) {
			this.type = type;
			this.data = data;
		}
		
		public PCommand(int type, Byte[] data) {
			this.type = type;
			this.data = new byte[data.length];
			for (int i = 0; i < data.length; ++i) {
				this.data[i] = data[i];
			}
		}
		
		public PCommand(int type, Collection<Byte> data) {
			this.type = type;
			this.data = new byte[data.size()];
			Iterator<Byte> it = data.iterator();
			int count = 0;
			while (it.hasNext()) {
				this.data[count++] = it.next();
			}
		}
		
		public PCommand(int type, int index) {
			this.type = type;
			this.index = index;
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
}

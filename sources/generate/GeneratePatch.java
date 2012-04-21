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

public class GeneratePatch {

	public static void main(String[] args) {
		DexOriginalFile original = new DexOriginalFile();
		DexOriginalFile update = new DexOriginalFile();
		original.setDumpOff();
		update.setDumpOff();
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile("testfiles/connect2.dex", "r");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		original.setRandomAccessFile(raf);
		original.parse();
		try {
			raf = new RandomAccessFile("testfiles/connect3.dex", "r");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		update.setRandomAccessFile(raf);
		update.parse();
		
		GeneratedFile patchFile = new GeneratedFile("out/connect.patch");
		GeneratedFile dataFile = new GeneratedFile("out/data.patch");
		
		patchFile.write(((Long)update.getStringDataItemOffset()).toString() +"\n");
		patchFile.write(((Long)update.getTypeListOffset()).toString() +"\n");
		patchFile.write(((Long)update.getDebugInfoItemOffset()).toString() +"\n");
		patchFile.write(((Long)update.getCodeItemOffset()).toString() +"\n");
		patchFile.write(((Long)update.getAnnotationItemOffset()).toString() +"\n");
		patchFile.write(((Long)update.getAnnotationSetItemOffset()).toString() +"\n");
		patchFile.write(((Long)update.getAnnotationSetRefListOffset()).toString() +"\n");
		patchFile.write(((Long)update.getAnnotationsDirectoryItemOffset()).toString() +"\n");
		
		String[] data1 = { "a", "b", "c", "d", "e", "f", "g" };
		String[] data2 = { "a", "f", "b", "c", "e", "f", "g", "s" };
		List<PCommand> l = lcs2(original.stringData, update.stringData);
		//List<PCommand> l = lcs2(data1, data2);
		
		// string_data_item
		ListIterator<PCommand> it = l.listIterator(l.size());
		PCommand c = it.previous();
		long[] stringIndexMap = new long[10000];
		int fileIndex = 0;
		int mapIndex = 0;
		while (true) {
			int val = c.type;
			int nx = c.type;
			long count = 0;
			while (nx == val) {
				if (nx == 0) {
					stringIndexMap[mapIndex++] = fileIndex++;
				} else if (nx == 1) {
					dataFile.write(c.data);
					++fileIndex;
				} else if (nx == 2) {
					stringIndexMap[mapIndex++] = -1;
				}
				++count;
				
				if (!it.hasPrevious())
					break;
				
				c = it.previous();
				nx = c.type;
			}
			
			patchFile.write(val);
			patchFile.write16bit(count);
			
			//patchFile.write(((Integer)val).toString() + " " + ((Integer)count).toString() + "\n");
			
			if (!it.hasPrevious()) {
				if(nx != val) {
					if (nx == 0) {
						stringIndexMap[mapIndex++] = fileIndex++;
					} else if (nx == 1) {
						dataFile.write(c.data);
						++fileIndex;
					} else if (nx == 2) {
						stringIndexMap[mapIndex++] = -1;
					}
					patchFile.write(val);
					patchFile.write16bit(1L);
					//patchFile.write(((Integer)nx).toString() + " " + "1\n");
				}
				break;
			}
		}
		
		patchFile.write(4);
		
		// type_id
		l = lcs2(original.typeIds, update.typeIds, original.stringData, update.stringData);
		it = l.listIterator(l.size());
		c = it.previous();
		long[] typeIndexMap = new long[10000];
		fileIndex = 0;
		mapIndex = 0;
		while (true) {
			int val = c.type;
			int nx = c.type;
			long count = 0;
			while (nx == val) {
				if (nx == 0) {
					typeIndexMap[mapIndex++] = fileIndex++;
				} else if (nx == 1) {
					dataFile.write(4L);
					dataFile.write((long)c.index);
					++fileIndex;
				} else if (nx == 2) {
					typeIndexMap[mapIndex++] = -1;
				}
				
				++count;
				
				if (!it.hasPrevious())
					break;
				
				c = it.previous();
				nx = c.type;
			}
			
			patchFile.write(val);
			patchFile.write16bit(count);
			//patchFile.write(((Integer)val).toString() + " " + ((Integer)count).toString() + "\n");
			
			if (!it.hasPrevious()) {
				if(nx != val) {
					if (nx == 0) {
						typeIndexMap[mapIndex++] = fileIndex++;
					} else if (nx == 1) {
						dataFile.write(4L);
						dataFile.write((long)c.index);
						++fileIndex;
					} else if (nx == 2) {
						typeIndexMap[mapIndex++] = -1;
					}
					patchFile.write(val);
					patchFile.write16bit(1L);
					//patchFile.write(((Integer)nx).toString() + " " + "1\n");
				}
				break;
			}
		}
		
		patchFile.write(4);
		
		// field_id_item
		l = lcs2(original.fieldIds, update.fieldIds, original.typeIds, update.typeIds, original.stringData, update.stringData);
		it = l.listIterator(l.size());
		c = it.previous();
		long[] fieldIndexMap = new long[10000];
		fileIndex = 0;
		mapIndex = 0;
		while (true) {
			int val = c.type;
			int nx = c.type;
			long count = 0;
			while (nx == val) {
				if (nx == 0) {
					fieldIndexMap[mapIndex++] = fileIndex++;
				} else if (nx == 1) {
					dataFile.write(c.data);
					++fileIndex;
				} else if (nx == 2) {
					fieldIndexMap[mapIndex++] = -1;
				}
				
				++count;
				
				if (!it.hasPrevious())
					break;
				
				c = it.previous();
				nx = c.type;
			}
			patchFile.write(val);
			patchFile.write16bit(count);
			//patchFile.write(((Integer)val).toString() + " " + ((Integer)count).toString() + "\n");
			
			if (!it.hasPrevious()) {
				if(nx != val) {
					if (nx == 0) {
						fieldIndexMap[mapIndex++] = fileIndex++;
					} else if (nx == 1) {
						dataFile.write(c.data);
						++fileIndex;
					} else if (nx == 2) {
						fieldIndexMap[mapIndex++] = -1;
					}
					patchFile.write(val);
					patchFile.write16bit(1L);
					//patchFile.write(((Integer)nx).toString() + " " + "1\n");
				}
				break;
			}
		}
		
		patchFile.write(4);
		
		// type_list
		l = lcs2(original.typeLists, update.typeLists, original.typeIds, update.typeIds, original.stringData, update.stringData);
		it = l.listIterator(l.size());
		c = it.previous();
		long[] typeListIndexMap = new long[10000];
		fileIndex = 0;
		mapIndex = 0;
		while (true) {
			int val = c.type;
			int nx = c.type;
			long count = 0;
			while (nx == val) {
				if(nx == 0) {
					typeListIndexMap[mapIndex++] = fileIndex++;
				} else if(nx == 1) {
					dataFile.write(c.data);
					++fileIndex;
				} else if(nx == 2) {
					typeListIndexMap[mapIndex++] = -1;
				}
				
				++count;
				
				if (!it.hasPrevious())
					break;
				
				c = it.previous();
				nx = c.type;
			}
			patchFile.write(val);
			patchFile.write16bit(count);
			//patchFile.write(((Integer)val).toString() + " " + ((Integer)count).toString() + "\n");
			
			if (!it.hasPrevious()) {
				if(nx != val) {
					if(nx == 0) {
						typeListIndexMap[mapIndex++] = fileIndex++;
					} else if(nx == 1) {
						dataFile.write(c.data);
						++fileIndex;
					} else if(nx == 2) {
						typeListIndexMap[mapIndex++] = -1;
					}
					patchFile.write(val);
					patchFile.write16bit(1L);
					//patchFile.write(((Integer)nx).toString() + " " + "1\n");
				}
				break;
			}
		}
		
		patchFile.write(4);
		
		// proto_id
		l = lcs2(original.protoIds, update.protoIds, typeListIndexMap, typeIndexMap, stringIndexMap);
		it = l.listIterator(l.size());
		c = it.previous();
		long[] protoIndexMap = new long[10000];
		fileIndex = 0;
		mapIndex = 0;
		while (true) {
			int val = c.type;
			int nx = c.type;
			long count = 0;
			while (nx == val) {
				if (nx == 0) {
					protoIndexMap[mapIndex++] = fileIndex++;
				} else if (nx == 1) {
					dataFile.write(c.data);
					++fileIndex;
				} else if (nx == 2) {
					protoIndexMap[mapIndex++] = -1;
				}
				
				++count;
				
				if (!it.hasPrevious())
					break;
				
				c = it.previous();
				nx = c.type;
			}
			patchFile.write(val);
			patchFile.write16bit(count);
			//patchFile.write(((Integer)val).toString() + " " + ((Integer)count).toString() + "\n");
			
			if (!it.hasPrevious()) {
				if(nx != val) {
					if (nx == 0) {
						protoIndexMap[mapIndex++] = fileIndex++;
					} else if (nx == 1) {
						dataFile.write(c.data);
						++fileIndex;
					} else if (nx == 2) {
						protoIndexMap[mapIndex++] = -1;
					}
					patchFile.write(val);
					patchFile.write16bit(1L);
					//patchFile.write(((Integer)nx).toString() + " " + "1\n");
				}
				break;
			}
		}
		
		patchFile.write(4);
		
		// method_id
		l = lcs2(original.methodIds, update.methodIds, typeIndexMap, protoIndexMap, stringIndexMap);
		it = l.listIterator(l.size());
		c = it.previous();
		long[] methodIndexMap = new long[10000];
		fileIndex = 0;
		mapIndex = 0;
		while (true) {
			int val = c.type;
			int nx = c.type;
			long count = 0;
			while (nx == val) {
				if (nx == 0) {
					methodIndexMap[mapIndex++] = fileIndex++;
				} else if (nx == 1) {
					dataFile.write(c.data);
					++fileIndex;
				} else if (nx == 2) {
					methodIndexMap[mapIndex++] = -1;
				}
				
				++count;
				
				if (!it.hasPrevious())
					break;
				
				c = it.previous();
				nx = c.type;
			}
			patchFile.write(val);
			patchFile.write16bit(count);
			//patchFile.write(((Integer)val).toString() + " " + ((Integer)count).toString() + "\n");
			
			if (!it.hasPrevious()) {
				if(nx != val) {
					if (nx == 0) {
						methodIndexMap[mapIndex++] = fileIndex++;
					} else if (nx == 1) {
						dataFile.write(c.data);
						++fileIndex;
					} else if (nx == 2) {
						methodIndexMap[mapIndex++] = -1;
					}
					patchFile.write(val);
					patchFile.write16bit(1L);
					//patchFile.write(((Integer)nx).toString() + " " + "1\n");
				}
				break;
			}
		}
		
		
		patchFile.write(4);
		
		// debug_info_item
		l = lcs2(original.debugInfoItems, update.debugInfoItems, typeIndexMap, stringIndexMap);
		it = l.listIterator(l.size());
		c = it.previous();
		long[] debugInfoIndexMap = new long[10000];
		fileIndex = 0;
		mapIndex = 0;
		while (true) {
			int val = c.type;
			int nx = c.type;
			long count = 0;
			while (nx == val) {
				if (nx == 0) {
					debugInfoIndexMap[mapIndex++] = fileIndex++;
				} else if (nx == 1) {
					dataFile.write(c.data);
					++fileIndex;
				} else if (nx == 2) {
					debugInfoIndexMap[mapIndex++] = -1;
				}
				
				++count;
				
				if (!it.hasPrevious())
					break;
				
				c = it.previous();
				nx = c.type;
			}
			patchFile.write(val);
			patchFile.write16bit(count);
			//patchFile.write(((Integer)val).toString() + " " + ((Integer)count).toString() + "\n");
			
			if (!it.hasPrevious()) {
				if(nx != val) {
					if (nx == 0) {
						debugInfoIndexMap[mapIndex++] = fileIndex++;
					} else if (nx == 1) {
						dataFile.write(c.data);
						++fileIndex;
					} else if (nx == 2) {
						debugInfoIndexMap[mapIndex++] = -1;
					}
					patchFile.write(val);
					patchFile.write16bit(1L);
					//patchFile.write(((Integer)nx).toString() + " " + "1\n");
				}
				break;
			}
		}
		
		patchFile.write(4);
		
		
		
		// code_item
		l = lcs2(original.codeItems, update.codeItems, fieldIndexMap, methodIndexMap, stringIndexMap, typeIndexMap, debugInfoIndexMap);
		it = l.listIterator(l.size());
		c = it.previous();
		long[] codeItemIndexMap = new long[10000];
		fileIndex = 0;
		mapIndex = 0;
		while (true) {
			int val = c.type;
			int nx = c.type;
			long count = 0;
			while (nx == val) {
				if (nx == 0) {
					codeItemIndexMap[mapIndex++] = fileIndex++;
				} else if (nx == 1) {
					dataFile.write(c.data);
					++fileIndex;
				} else if (nx == 2) {
					codeItemIndexMap[mapIndex++] = -1;
				}
				
				++count;
				
				if (!it.hasPrevious())
					break;
				
				c = it.previous();
				nx = c.type;
			}
			patchFile.write(val);
			patchFile.write16bit(count);
			//patchFile.write(((Integer)val).toString() + " " + ((Integer)count).toString() + "\n");
			
			if (!it.hasPrevious()) {
				if(nx != val) {
					if (nx == 0) {
						codeItemIndexMap[mapIndex++] = fileIndex++;
					} else if (nx == 1) {
						dataFile.write(c.data);
						++fileIndex;
					} else if (nx == 2) {
						codeItemIndexMap[mapIndex++] = -1;
					}
					patchFile.write(val);
					patchFile.write16bit(1L);
					//patchFile.write(((Integer)nx).toString() + " " + "1\n");
				}
				break;
			}
		}
		
		patchFile.write(4);
		
		
		// annotation_item
		l = lcs2(original.annotationItems, update.annotationItems, fieldIndexMap, methodIndexMap, stringIndexMap, typeIndexMap);
		it = l.listIterator(l.size());
		c = it.previous();
		long[] annotationsItemIndexMap = new long[10000];
		fileIndex = 0;
		mapIndex = 0;
		while (true) {
			int val = c.type;
			int nx = c.type;
			long count = 0;
			while (nx == val) {
				if (nx == 0) {
					annotationsItemIndexMap[mapIndex++] = fileIndex++;
				} else if (nx == 1) {
					dataFile.write(c.data);
					++fileIndex;
				} else if (nx == 2) {
					annotationsItemIndexMap[mapIndex++] = -1;
				}
				
				++count;
				
				if (!it.hasPrevious())
					break;
				
				c = it.previous();
				nx = c.type;
			}
			patchFile.write(val);
			patchFile.write16bit(count);
			//patchFile.write(((Integer)val).toString() + " " + ((Integer)count).toString() + "\n");
			
			if (!it.hasPrevious()) {
				if(nx != val) {
					if (nx == 0) {
						annotationsItemIndexMap[mapIndex++] = fileIndex++;
					} else if (nx == 1) {
						dataFile.write(c.data);
						++fileIndex;
					} else if (nx == 2) {
						annotationsItemIndexMap[mapIndex++] = -1;
					}
					patchFile.write(val);
					patchFile.write16bit(1L);
					//patchFile.write(((Integer)nx).toString() + " " + "1\n");
				}
				break;
			}
		}
		
		patchFile.write(4);
		
		// annotation_set_item
		l = lcs2(original.annotationSetItems, update.annotationSetItems, annotationsItemIndexMap);
		it = l.listIterator(l.size());
		c = it.previous();
		long[] annotationSetItemIndexMap = new long[10000];
		fileIndex = 0;
		mapIndex = 0;
		while (true) {
			int val = c.type;
			int nx = c.type;
			long count = 0;
			while (nx == val) {
				if (nx == 0) {
					annotationSetItemIndexMap[mapIndex++] = fileIndex++;
				} else if (nx == 1) {
					dataFile.write(c.data);
					++fileIndex;
				} else if (nx == 2) {
					annotationSetItemIndexMap[mapIndex++] = -1;
				}
				
				++count;
				
				if (!it.hasPrevious())
					break;
				
				c = it.previous();
				nx = c.type;
			}
			patchFile.write(val);
			patchFile.write16bit(count);
			//patchFile.write(((Integer)val).toString() + " " + ((Integer)count).toString() + "\n");
			
			if (!it.hasPrevious()) {
				if(nx != val) {
					if (nx == 0) {
						annotationSetItemIndexMap[mapIndex++] = fileIndex++;
					} else if (nx == 1) {
						dataFile.write(c.data);
						++fileIndex;
					} else if (nx == 2) {
						annotationSetItemIndexMap[mapIndex++] = -1;
					}
					patchFile.write(val);
					patchFile.write16bit(1L);
					//patchFile.write(((Integer)nx).toString() + " " + "1\n");
				}
				break;
			}
		}
		
		patchFile.write(4);
		
		// annotation_set_ref_list
		l = lcs2(original.annotationSetRefList, update.annotationSetRefList, annotationSetItemIndexMap);
		long[] annotationSetRefListIndexMap = new long[10000];
		for (int i = 0; i < 10000; ++i) {
			annotationSetRefListIndexMap[i] = 0;
		}
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
						annotationSetRefListIndexMap[mapIndex++] = fileIndex++;
					} else if (nx == 1) {
						dataFile.write(c.data);
						++fileIndex;
					} else if (nx == 2) {
						annotationSetRefListIndexMap[mapIndex++] = -1;
					}
					
					++count;
					
					if (!it.hasPrevious())
						break;
					
					c = it.previous();
					nx = c.type;
				}
				patchFile.write(val);
				patchFile.write16bit(count);
				//patchFile.write(((Integer)val).toString() + " " + ((Integer)count).toString() + "\n");
				
				if (!it.hasPrevious()) {
					if(nx != val) {
						if (nx == 0) {
							annotationSetRefListIndexMap[mapIndex++] = fileIndex++;
						} else if (nx == 1) {
							dataFile.write(c.data);
							++fileIndex;
						} else if (nx == 2) {
							annotationSetRefListIndexMap[mapIndex++] = -1;
						}
						patchFile.write(val);
						patchFile.write16bit(1L);
						//patchFile.write(((Integer)nx).toString() + " " + "1\n");
					}
					break;
				}
			}
		}
		
		patchFile.write(4);
		
		// annotations_directory_item
		l = lcs2(original.annotationsDirectoryItems, update.annotationsDirectoryItems, fieldIndexMap, methodIndexMap, annotationSetItemIndexMap, annotationSetRefListIndexMap);
		it = l.listIterator(l.size());
		c = it.previous();
		long[] annotationsDirectoryItemIndexMap = new long[10000];
		fileIndex = 0;
		mapIndex = 0;
		while (true) {
			int val = c.type;
			int nx = c.type;
			long count = 0;
			while (nx == val) {
				if (nx == 0) {
					annotationsDirectoryItemIndexMap[mapIndex++] = fileIndex++;
				} else if (nx == 1) {
					dataFile.write(c.data);
					++fileIndex;
				} else if (nx == 2) {
					annotationsDirectoryItemIndexMap[mapIndex++] = -1;
				}
				
				++count;
				
				if (!it.hasPrevious())
					break;
				
				c = it.previous();
				nx = c.type;
			}
			patchFile.write(val);
			patchFile.write16bit(count);
			//patchFile.write(((Integer)val).toString() + " " + ((Integer)count).toString() + "\n");
			
			if (!it.hasPrevious()) {
				if(nx != val) {
					if (nx == 0) {
						annotationsDirectoryItemIndexMap[mapIndex++] = fileIndex++;
					} else if (nx == 1) {
						dataFile.write(c.data);
						++fileIndex;
					} else if (nx == 2) {
						annotationsDirectoryItemIndexMap[mapIndex++] = -1;
					}
					patchFile.write(val);
					patchFile.write16bit(1L);
					//patchFile.write(((Integer)nx).toString() + " " + "1\n");
				}
				break;
			}
		}
		
		patchFile.write(4);
		
		
		
		// class_data_item
		l = lcs2(original.classDataItems, update.classDataItems, fieldIndexMap, methodIndexMap, codeItemIndexMap);
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
			//patchFile.write(((Integer)val).toString() + " " + ((Integer)count).toString() + "\n");
			
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
					//patchFile.write(((Integer)nx).toString() + " " + "1\n");
				}
				break;
			}
		}
		
		patchFile.write(4);
		
		// encododed_array_item
		l = lcs2(original.encodedArrayItems, update.encodedArrayItems, fieldIndexMap, methodIndexMap, stringIndexMap, typeIndexMap);
		long[] encodedArrayItemIndexMap = new long[10000];
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
						encodedArrayItemIndexMap[mapIndex++] = fileIndex++;
					} else if (nx == 1) {
						dataFile.write(c.data);
						++fileIndex;
					} else if (nx == 2) {
						encodedArrayItemIndexMap[mapIndex++] = -1;
					}
					
					++count;
					
					if (!it.hasPrevious())
						break;
					
					c = it.previous();
					nx = c.type;
				}
				patchFile.write(val);
				patchFile.write16bit(count);
				//patchFile.write(((Integer)val).toString() + " " + ((Integer)count).toString() + "\n");
				
				if (!it.hasPrevious()) {
					if(nx != val) {
						if (nx == 0) {
							encodedArrayItemIndexMap[mapIndex++] = fileIndex++;
						} else if (nx == 1) {
							dataFile.write(c.data);
							++fileIndex;
						} else if (nx == 2) {
							encodedArrayItemIndexMap[mapIndex++] = -1;
						}
						patchFile.write(val);
						patchFile.write16bit(1L);
						//patchFile.write(((Integer)nx).toString() + " " + "1\n");
					}
					break;
				}
			}
		}
		
		patchFile.write(4);
		
		
		
		// class_def_item
		l = lcs2(original.classDefItems, update.classDefItems, annotationsDirectoryItemIndexMap, classDataItemIndexMap, encodedArrayItemIndexMap, stringIndexMap, typeIndexMap, typeListIndexMap);
		it = l.listIterator(l.size());
		c = it.previous();
		long[] classDefItemIndexMap = new long[10000];
		fileIndex = 0;
		mapIndex = 0;
		while (true) {
			int val = c.type;
			int nx = c.type;
			long count = 0;
			while (nx == val) {
				if (nx == 0) {
					classDefItemIndexMap[mapIndex++] = fileIndex++;
				} else if (nx == 1) {
					dataFile.write(c.data);
					++fileIndex;
				} else if (nx == 2) {
					classDefItemIndexMap[mapIndex++] = -1;
				}
				
				++count;
				
				if (!it.hasPrevious())
					break;
				
				c = it.previous();
				nx = c.type;
			}
			patchFile.write(val);
			patchFile.write16bit(count);
			//patchFile.write(((Integer)val).toString() + " " + ((Integer)count).toString() + "\n");
			
			if (!it.hasPrevious()) {
				if(nx != val) {
					if (nx == 0) {
						classDefItemIndexMap[mapIndex++] = fileIndex++;
					} else if (nx == 1) {
						dataFile.write(c.data);
						++fileIndex;
					} else if (nx == 2) {
						classDefItemIndexMap[mapIndex++] = -1;
					}
					patchFile.write(val);
					patchFile.write16bit(1L);
					//patchFile.write(((Integer)nx).toString() + " " + "1\n");
				}
				break;
			}
		}
		
		patchFile.write(4);
		
		
		//~~~~~//
		dataFile.write(0L);
		patchFile.close();
		dataFile.close();
		writeCompleteFile("out/connect.patch", "out/data.patch");
		//~~~~~//
		
		System.out.print("done");
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
	
	public static List<PCommand> lcs2(int[] a, int[] b, String[] aData, String[] bData) {
	    int[][] lengths = new int[a.length+1][b.length+1];
	 
	    // row 0 and column 0 are initialized to 0 already
	 
	    for (int i = 0; i < a.length; i++)
	        for (int j = 0; j < b.length; j++)
	            if (aData[a[i]].equals(bData[b[j]]))
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
	
	public static List<PCommand> lcs2(FieldIdItem[] a, FieldIdItem[] b, int[] aTypes, int[] bTypes, String[] aData, String[] bData) {
	    int[][] lengths = new int[a.length+1][b.length+1];
	 
	    // row 0 and column 0 are initialized to 0 already
	 
	    for (int i = 0; i < a.length; i++)
	        for (int j = 0; j < b.length; j++)
	            if (aData[(int) a[i].nameId].equals(bData[(int) b[j].nameId])
	            		&& aData[aTypes[a[i].classId]].equals(bData[bTypes[b[j].classId]])
	            		&& aData[aTypes[a[i].typeId]].equals(bData[bTypes[b[j].typeId]])) {
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
	
	public static List<PCommand> lcs2(TypeList[] a, TypeList[] b, int[] aTypes, int[] bTypes, String[] aData, String[] bData) {
	    int[][] lengths = new int[a.length+1][b.length+1];
	 
	    // row 0 and column 0 are initialized to 0 already
	 
	    for (int i = 0; i < a.length; i++) {
	        for (int j = 0; j < b.length; j++) {
	            boolean isEqual = true;
	            if (a[i].size != b[j].size) {
	            	isEqual = false;
	            } else {
	            	for(int k = 0; k < a[i].size; ++k) {
	            		if(!aData[aTypes[a[i].types[k]]].equals(bData[bTypes[b[j].types[k]]])) {
	            			isEqual = false;
	            			break;
	            		}
	            	}
	            }
	            
	        	if (isEqual) {
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
	
	public static List<PCommand> lcs2(ProtoIdItem[] a, ProtoIdItem[] b, long[] typeListIndexMap, long[] typeIndexMap, long[] stringIndexMap) {
	    int[][] lengths = new int[a.length+1][b.length+1];
	 
	    // row 0 and column 0 are initialized to 0 already
	 
	    for (int i = 0; i < a.length; i++) {
	        for (int j = 0; j < b.length; j++) {
	        	boolean isEqual = true;
	        	
	        	if (stringIndexMap[(int)a[i].shorty] != b[j].shorty ||
	        			typeIndexMap[(int)a[i].type] != b[j].type) {
	        		isEqual = false;
	        	}
	        	
	        	if (a[i].typeListIndex == -1 || b[j].typeListIndex == -1) {
	        		if(a[i].typeListIndex != b[j].typeListIndex) {
	        			isEqual = false;
	        		}
	        	} else if (typeListIndexMap[(int)a[i].typeListIndex] != b[j].typeListIndex) {
	        		isEqual = false;
	        	}
	        	
	        	
	        	
	        	if (isEqual) {
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
    public static List<PCommand> lcs2(MethodIdItem[] a, MethodIdItem[] b, long[] typeIndexMap, long[] protoIndexMap, long[] stringIndexMap) {
	    int[][] lengths = new int[a.length+1][b.length+1];
	 
	    // row 0 and column 0 are initialized to 0 already
	 
	    for (int i = 0; i < a.length; i++) {
	        for (int j = 0; j < b.length; j++) {
	        	boolean isEqual = true;
	        	
	        	if (typeIndexMap[(int)a[i].classId] != b[j].classId ||
	        			protoIndexMap[(int)a[i].proto] != b[j].proto ||
	        			stringIndexMap[(int)a[i].name] != b[j].name) {
	        		isEqual = false;
	        	}
	        	
	        	if (isEqual) {
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
    public static List<PCommand> lcs2(DebugInfoItem[] a, DebugInfoItem[] b, long[] typeIndexMap, long[] stringIndexMap) {
	    int[][] lengths = new int[a.length+1][b.length+1];
	 
	    // row 0 and column 0 are initialized to 0 already
	 
	    for (int i = 0; i < a.length; i++) {
	        for (int j = 0; j < b.length; j++) {
	        	boolean isEqual = true;
	        	if (i == 716 && j == 716)
	        		System.out.println("sup");
	        	if (a[i].lineStart != b[j].lineStart || a[i].parametersSize != b[j].parametersSize ||
	        			a[i].debugByteCode.size() != b[j].debugByteCode.size()) {
	        		isEqual = false;
	        	} else {
	        		for (int k = 0; k < a[i].parametersSize; ++k) {
	        			if (a[i].parameterNames[k] != -1 && b[j].parameterNames[k] != -1) {
		        			if (stringIndexMap[(int)a[i].parameterNames[k]] != b[j].parameterNames[k]) {
		        				isEqual = false;
		        				break;
		        			}
	        			}
	        			
	        		}
	        		
	        		if (isEqual) {
	        			Iterator<DebugByteCode> it1 = a[i].debugByteCode.iterator();
	        			Iterator<DebugByteCode> it2 = b[j].debugByteCode.iterator();
	        			
	        			while (it1.hasNext()) {
	        				DebugByteCode b1 = it1.next();
	        				DebugByteCode b2 = it2.next();
	        				
	        				if (!b1.isEqual(b2, typeIndexMap, stringIndexMap)) {
	        					isEqual = false;
	        					break;
	        				}
	        			}
	        		}
	        	}
	        	
	        	if (isEqual) {
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
	        	System.out.println("Del deb");
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
    public static List<PCommand> lcs2(CodeItem[] a, CodeItem[] b, long[] fieldIndexMap, long[] methodIndexMap, long[] stringIndexMap, long[] typeIndexMap, long[] debugInfoIndexMap) {
	    int[][] lengths = new int[a.length+1][b.length+1];
	 
	    // row 0 and column 0 are initialized to 0 already
	 
	    for (int i = 0; i < a.length; i++) {
	        for (int j = 0; j < b.length; j++) {
	        	/*boolean isEqual = true;
	        	
	        	if (a[i].registersSize != b[j].registersSize || a[i].insSize != b[j].insSize ||
	        			a[i].outsSize != b[j].outsSize || a[i].triesSize != b[j].triesSize ||
	        			a[i].insnsSize != b[j].insnsSize || a[i].padding != b[j].padding ||
	        			a[i].times != b[j].times ||	debugInfoIndexMap[a[i].debugInfoIndex] != b[j].debugInfoIndex) {
	        		isEqual = false;
	        	} else {
	        		for (int k = 0; k < a[i].triesSize; ++k) {
	        			if (!a[i].tries[k].isEqual(b[j].tries[k])) {
	        				isEqual = false;
	        				break;
	        			}
	        		}
	        		
	        		if (isEqual && a[i].triesSize > 0) {
	        			if (!a[i].handlers.isEqual(b[j].handlers, typeIndexMap)) {
	        				isEqual = false;
	        			}
	        		}
	        		
	        		if (isEqual) {
	        			for (int k = 0; k < a[i].instructions.length; ++k) {
	        				if (a[i].ins[k] != b[j].instructions[k]) {
	        					isEqual = false;
	        				}
	        			}
	        		}
	        	}*/
	        	
	        	if (i == 985 && j == 985)
	        		System.out.println("sup");
	        	
	        	if (a[i].isEqual(b[j], fieldIndexMap, methodIndexMap, stringIndexMap, typeIndexMap, debugInfoIndexMap)) {
	        		System.out.println(i + " " + j);
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
	    	if ( x == 4 || y == 4)
	    		System.out.print("hi");
	        if (x > 0 && lengths[x][y] == lengths[x-1][y]) {
	        	l.add(new PCommand(2));
	        	x--;
	        	System.out.println("Del");
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
    public static List<PCommand> lcs2(AnnotationItem[] a, AnnotationItem[] b, long[] fieldIndexMap, long[] methodIndexMap, long[] stringIndexMap, long[] typeIndexMap) {
	    int[][] lengths = new int[a.length+1][b.length+1];
	 
	    // row 0 and column 0 are initialized to 0 already
	 
	    for (int i = 0; i < a.length; i++) {
	        for (int j = 0; j < b.length; j++) {
	        	if (a[i].isEqual(b[j], fieldIndexMap, methodIndexMap, stringIndexMap, typeIndexMap)) {
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
    public static List<PCommand> lcs2(AnnotationSetItem[] a, AnnotationSetItem[] b, long[] annotationItemMap) {
	    int[][] lengths = new int[a.length+1][b.length+1];
	 
	    // row 0 and column 0 are initialized to 0 already
	 
	    for (int i = 0; i < a.length; i++) {
	        for (int j = 0; j < b.length; j++) {
	        	if (a[i].isEqual(b[j], annotationItemMap)) {
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
    public static List<PCommand> lcs2(AnnotationSetRefList[] a, AnnotationSetRefList[] b, long[] annotationSetItemMap) {
	    int[][] lengths = new int[a.length+1][b.length+1];
	 
	    // row 0 and column 0 are initialized to 0 already
	 
	    for (int i = 0; i < a.length; i++) {
	        for (int j = 0; j < b.length; j++) {
	        	if (a[i].isEqual(b[j], annotationSetItemMap)) {
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
    public static List<PCommand> lcs2(AnnotationsDirectoryItem[] a, AnnotationsDirectoryItem[] b, long[] fieldIndexMap, long[] methodIndexMap, long[] annotationSetItemIndexMap, long[] annotationSetRefListIndexMap) {
	    int[][] lengths = new int[a.length+1][b.length+1];
	 
	    // row 0 and column 0 are initialized to 0 already
	 
	    for (int i = 0; i < a.length; i++) {
	        for (int j = 0; j < b.length; j++) {
	        	if (a[i].isEqual(b[j], fieldIndexMap, methodIndexMap, annotationSetItemIndexMap, annotationSetRefListIndexMap)) {
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
    public static List<PCommand> lcs2(ClassDataItem[] a, ClassDataItem[] b, long[] fieldIndexMap, long[] methodIndexMap, long[] codeItemIndexMap) {
	    int[][] lengths = new int[a.length+1][b.length+1];
	 
	    // row 0 and column 0 are initialized to 0 already
	 
	    for (int i = 0; i < a.length; i++) {
	        for (int j = 0; j < b.length; j++) {
	        	if (a[i].isEqual(b[j], fieldIndexMap, methodIndexMap, codeItemIndexMap)) {
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
    public static List<PCommand> lcs2(EncodedArray[] a, EncodedArray[] b, long[] fieldIndexMap, long[] methodIndexMap, long[] stringIndexMap, long[] typeIndexMap) {
	    int[][] lengths = new int[a.length+1][b.length+1];
	 
	    // row 0 and column 0 are initialized to 0 already
	 
	    for (int i = 0; i < a.length; i++) {
	        for (int j = 0; j < b.length; j++) {
	        	if (a[i].isEqual(b[j], fieldIndexMap, methodIndexMap, stringIndexMap, typeIndexMap)) {
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
    public static List<PCommand> lcs2(ClassDefItem[] a, ClassDefItem[] b, long[] annotationsDirectoryItemIndexMap, long[] classDataItemIndexMap, long[] encodedArrayItemIndexMap, long[] stringIndexMap, long[] typeIndexMap, long[] typeListIndexMap) {
	    int[][] lengths = new int[a.length+1][b.length+1];
	 
	    // row 0 and column 0 are initialized to 0 already
	 
	    for (int i = 0; i < a.length; i++) {
	        for (int j = 0; j < b.length; j++) {
	        	if (a[i].isEqual(b[j], annotationsDirectoryItemIndexMap, classDataItemIndexMap, encodedArrayItemIndexMap, stringIndexMap, typeIndexMap, typeListIndexMap)) {
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

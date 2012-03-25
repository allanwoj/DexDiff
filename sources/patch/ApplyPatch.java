package patch;

import item.AnnotationItem;
import item.AnnotationSetItem;
import item.AnnotationSetRefList;
import item.AnnotationsDirectoryItem;
import item.ClassDataItem;
import item.CodeItem;
import item.DebugByteCode;
import item.DebugInfoItem;
import item.EncodedAnnotation;
import item.EncodedValue;
import item.FieldIdItem;
import item.MethodIdItem;
import item.ProtoIdItem;
import item.TypeList;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.Iterator;


public class ApplyPatch {

	
	public static void main(String[] args) {
		DexOriginalFile original = new DexOriginalFile();
		original.setDumpOff();
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(args[0], "r");
			//raf = new RandomAccessFile("testfiles/connect3.dex", "r");
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
		GeneratedFile protoFile = new GeneratedFile("out/proto.dex");
		GeneratedFile methodFile = new GeneratedFile("out/method.dex");
		GeneratedFile typeListFile = new GeneratedFile("out/type_list.dex");
		GeneratedFile annDirItemFile = new GeneratedFile("out/annotations_directory_item.dex");
		GeneratedFile classDataItemFile = new GeneratedFile("out/class_data_item.dex");
		GeneratedFile annotationItemFile = new GeneratedFile("out/annotation_item.dex");
		GeneratedFile annotationSetItemFile = new GeneratedFile("out/annotation_set_item.dex");
		GeneratedFile annotationSetRefListFile = new GeneratedFile("out/annotation_set_ref_list.dex");
		GeneratedFile debugInfoItemFile = new GeneratedFile("out/debug_info_item.dex");
		GeneratedFile codeItemFile = new GeneratedFile("out/code_item.dex");
		long[] stringIndexMap = new long[10000];
		long[] typeIndexMap = new long[10000];
		long[] fieldIndexMap = new long[10000];
		long[] protoIndexMap = new long[10000];
		long[] methodIndexMap = new long[10000];
		long[] typeListIndexMap = new long[10000];
		long[] typeListPointerMap = new long[10000];
		long[] annotationsDirectoryItemMap = new long[10000];
		long[] classDataItemMap = new long[10000];
		long[] annotationItemMap = new long[10000];
		long[] annotationSetItemMap = new long[10000];
		long[] annotationSetRefListMap = new long[10000];
		long[] debugInfoItemMap = new long[10000];
		long[] codeItemMap = new long[10000];
		PatchCommand command;
		int fileIndex = 0;
		int mapIndex = 0;
		long currentStringOffset = patch.getStringOffset();
		stringIdsFile.write(currentStringOffset);
		String buf;
		
		// Generate patched string_ids and string_data_items
		while(patch.hasStringCommands()) {
			command = patch.getNextStringCommand();
			
			if (command.type == 0) {
				// KEEP
				for(int i = 0; i < command.size; ++i) {
					buf = original.getStringData();
					currentStringOffset += buf.length() + 1;
					stringIdsFile.write(currentStringOffset);
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
					currentStringOffset += buf.length() + size_buf;
					// TODO Handle cases when length is larger than 127.
					stringIdsFile.write(currentStringOffset);
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
					typeIndexMap[mapIndex++] = -1;
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
		
		
		
		
		
		fileIndex = 0;
		mapIndex = 0;
		MethodIdItem methodItem = null;
		// Generate patched method_ids
		while(patch.hasMethodCommands()) {
			command = patch.getNextMethodCommand();
			if (command.type == 0) {
				// KEEP
				for(int i = 0; i < command.size; ++i) {
					methodItem = original.getMethodIdData();
					methodFile.write16bit(typeIndexMap[(int)methodItem.classId]);
					methodFile.write16bit(protoIndexMap[(int)methodItem.proto]);
					methodFile.write(stringIndexMap[(int)methodItem.name]);
					methodIndexMap[mapIndex++] = fileIndex++;
				}
			} else if (command.type == 1) {
				// ADD
				for(int i = 0; i < command.size; ++i) {
					methodFile.write16bit(Long.parseLong(patch.getNextData()));
					methodFile.write16bit(Long.parseLong(patch.getNextData()));
					methodFile.write(Long.parseLong(patch.getNextData()));
					++fileIndex;
				}
			} else if (command.type == 2) {
				// DELETE
				for(int i = 0; i < command.size; ++i) {
					original.getMethodIdData();
					methodIndexMap[mapIndex++] = -1;
				}
			}
		}
		
		// type_list
		fileIndex = 0;
		mapIndex = 0;
		TypeList typeList;
		long tempPointer = patch.getTypeListOffset();
		int pointerIndex = 0;
		while(patch.hasTypeListCommands()) {
			command = patch.getNextTypeListCommand();
			if (command.type == 0) {
				// KEEP
				for(int i = 0; i < command.size; ++i) {
					typeListIndexMap[mapIndex++] = fileIndex++;
					typeListPointerMap[pointerIndex++] = tempPointer;
					typeList = original.getTypeList();
					typeListFile.write(typeList.size);
					for (int j = 0; j < typeList.size; ++j) {
						typeListFile.write16bit(typeIndexMap[typeList.types[j]]);
					}
					if (typeList.size % 2 == 1) {
						typeListFile.write16bit(0L);
						tempPointer += 2;
					}
					tempPointer += 4 + typeList.size * 2;
					
				}
			} else if (command.type == 1) {
				// ADD
				for(int i = 0; i < command.size; ++i) {
					++fileIndex;
					typeListPointerMap[pointerIndex++] = tempPointer;
					long tlsize = Long.parseLong(patch.getNextData());
					typeListFile.write(tlsize);
					for (int j = 0; j < tlsize; ++j) {
						typeListFile.write16bit(Long.parseLong(patch.getNextData()));
					}
					if (tlsize % 2 == 1) {
						typeListFile.write16bit(0L);
						tempPointer += 2;
					}
					tempPointer += 4 + tlsize * 2;
				}
			} else if (command.type == 2) {
				// DELETE
				for(int i = 0; i < command.size; ++i) {
					original.getTypeList();
					typeListIndexMap[mapIndex++] = -1;
				}
			}
		}
		
		
		fileIndex = 0;
		mapIndex = 0;
		ProtoIdItem protoItem = null;
		// Generate patched proto_ids
		while(patch.hasProtoCommands()) {
			command = patch.getNextProtoCommand();
			if (command.type == 0) {
				// KEEP
				for(int i = 0; i < command.size; ++i) {
					protoItem = original.getProtoIdData();
					protoFile.write(stringIndexMap[(int)protoItem.shorty]);
					protoFile.write(typeIndexMap[(int)protoItem.type]);
					if (protoItem.typeListIndex == -1) {
						protoFile.write(0L);
					} else {
						protoFile.write(typeListPointerMap[(int) typeListIndexMap[protoItem.typeListIndex]]);
					}
					protoIndexMap[mapIndex++] = fileIndex++;
				}
			} else if (command.type == 1) {
				// ADD
				for(int i = 0; i < command.size; ++i) {
					protoFile.write(Long.parseLong(patch.getNextData()));
					protoFile.write(Long.parseLong(patch.getNextData()));
					protoFile.write(Long.parseLong(patch.getNextData()));
					++fileIndex;
				}
			} else if (command.type == 2) {
				// DELETE
				for(int i = 0; i < command.size; ++i) {
					original.getProtoIdData();
					protoIndexMap[mapIndex++] = -1;
				}
			}
		}
		
		// annotations_directory_item
		fileIndex = 0;
		mapIndex = 0;
		AnnotationsDirectoryItem annItem;
		int annDirSize = (int)original.getAnnotationsDirectoryItemSize();
		for (int i = 0; i < annDirSize; ++i) {
			annItem = original.getAnnotationsDirectoryItem();
			annDirItemFile.write(0L);
			annDirItemFile.write(annItem.fieldsSize);
			annDirItemFile.write(annItem.annMethodsSize);
			annDirItemFile.write(annItem.annParamsSize);
			
			for (int j = 0; j < annItem.fieldsSize; ++j) {
				annDirItemFile.write(fieldIndexMap[(int)annItem.fieldAnnotations[j].fieldId]);
				annDirItemFile.write(0L);
			}
			
			for (int j = 0; j < annItem.annMethodsSize; ++j) {
				annDirItemFile.write(methodIndexMap[(int)annItem.methodAnnotations[j].methodId]);
				annDirItemFile.write(0L);
			}
			
			for (int j = 0; j < annItem.annParamsSize; ++j) {
				annDirItemFile.write(methodIndexMap[(int)annItem.parameterAnnotations[j].methodId]);
				annDirItemFile.write(0L);
			}
		}
		
		// class_data_item
		fileIndex = 0;
		mapIndex = 0;
		ClassDataItem classDataItem;
		int classDataItemSize = (int)original.getClassDataItemSize();
		for (int i = 0; i < classDataItemSize; ++i) {
			classDataItem = original.getClassDataItem();
			classDataItemFile.writeULeb128((int) classDataItem.staticFieldsSize);
			classDataItemFile.writeULeb128((int) classDataItem.instanceFieldsSize);
			classDataItemFile.writeULeb128((int) classDataItem.directMethodsSize);
			classDataItemFile.writeULeb128((int) classDataItem.virtualMethodsSize);
			
			if (classDataItem.staticFieldsSize > 0) {
				long start = classDataItem.staticFields[0].diff;
				classDataItemFile.writeULeb128((int)fieldIndexMap[(int)start]);
				classDataItemFile.writeULeb128((int)classDataItem.staticFields[0].flags);
				for (int j = 1; j < classDataItem.staticFieldsSize; ++j) {
					classDataItemFile.writeULeb128((int)fieldIndexMap[(int)(classDataItem.staticFields[j].diff + start)]);
					classDataItemFile.writeULeb128((int)classDataItem.staticFields[j].flags);
				}
			}
			
			if (classDataItem.instanceFieldsSize > 0) {
				long start = classDataItem.instanceFields[0].diff;
				classDataItemFile.writeULeb128((int)fieldIndexMap[(int)start]);
				classDataItemFile.writeULeb128((int)classDataItem.instanceFields[0].flags);
				for (int j = 1; j < classDataItem.instanceFieldsSize; ++j) {
					classDataItemFile.writeULeb128((int)fieldIndexMap[(int)(classDataItem.instanceFields[j].diff + start)]);
					classDataItemFile.writeULeb128((int)classDataItem.instanceFields[j].flags);
				}
			}
			
			
			
			
			if (classDataItem.directMethodsSize > 0) {
				long start = classDataItem.directMethods[0].diff;
				classDataItemFile.writeULeb128((int)methodIndexMap[(int)start]);
				classDataItemFile.writeULeb128((int)classDataItem.directMethods[0].flags);
				classDataItemFile.writeULeb128(0);
				for (int j = 1; j < classDataItem.directMethodsSize; ++j) {
					classDataItemFile.writeULeb128((int)methodIndexMap[(int)(classDataItem.directMethods[j].diff + start)]);
					classDataItemFile.writeULeb128((int)classDataItem.directMethods[j].flags);
					classDataItemFile.writeULeb128(0);
				}
			}
			
			if (classDataItem.virtualMethodsSize > 0) {
				long start = classDataItem.virtualMethods[0].diff;
				classDataItemFile.writeULeb128((int)methodIndexMap[(int)start]);
				classDataItemFile.writeULeb128((int)classDataItem.virtualMethods[0].flags);
				classDataItemFile.writeULeb128(0);
				for (int j = 1; j < classDataItem.virtualMethodsSize; ++j) {
					classDataItemFile.writeULeb128((int)methodIndexMap[(int)(classDataItem.virtualMethods[j].diff + start)]);
					classDataItemFile.writeULeb128((int)classDataItem.virtualMethods[j].flags);
					classDataItemFile.writeULeb128(0);
				}
			}
			
		}
		
		// annotation_item
		fileIndex = 0;
		mapIndex = 0;
		AnnotationItem annotationItem;
		EncodedAnnotation enAnn;
		int annSize = (int)original.getAnnotationItemSize();
		for (int i = 0; i < annSize; ++i) {
			annotationItem = original.getAnnotationItem();
			annotationItemFile.write(annotationItem.getVisibility());
			enAnn = annotationItem.getAnnotation();
			annotationItemFile.write(enAnn.getData(fieldIndexMap, methodIndexMap, stringIndexMap, typeIndexMap));
		}
		
		// annotation_set_item
		fileIndex = 0;
		mapIndex = 0;
		AnnotationSetItem annotationSetItem;
		int annSetSize = (int)original.getAnnotationSetItemSize();
		for (int i = 0; i < annSetSize; ++i) {
			annotationSetItem = original.getAnnotationSetItem();
			annotationSetItemFile.write(annotationSetItem.size);
			for (int j = 0 ; j < annotationSetItem.size; ++j) {
				annotationSetItemFile.write(0L);
			}
		}
		
		// annotation_set_ref_list
		fileIndex = 0;
		mapIndex = 0;
		AnnotationSetRefList annotationSetRefList;
		int annSetRefListSize = (int)original.getAnnotationSetRefListSize();
		for (int i = 0; i < annSetRefListSize; ++i) {
			annotationSetRefList = original.getAnnotationSetRefList();
			annotationSetRefListFile.write(annotationSetRefList.size);
			for (int j = 0 ; j < annotationSetRefList.size; ++j) {
				annotationSetRefListFile.write(0L);
			}
		}
		
		// debug_info_item
		fileIndex = 0;
		mapIndex = 0;
		DebugInfoItem debugInfoItem;
		int debugInfoItemSize = (int)original.getDebugInfoItemSize();
		for (int i = 0; i < debugInfoItemSize; ++i) {
			debugInfoItem = original.getDebugInfoItem();
			debugInfoItemFile.writeULeb128((int)debugInfoItem.lineStart);
			debugInfoItemFile.writeULeb128((int)debugInfoItem.parametersSize);
			for (int j = 0; j < debugInfoItem.parametersSize; ++j) {
				if (debugInfoItem.parameterNames[j] == -1) {
					debugInfoItemFile.writeULeb128(0);
				} else {
					debugInfoItemFile.writeULeb128(1 + (int)stringIndexMap[(int)debugInfoItem.parameterNames[j]]);
				}
			}
			
			Iterator<DebugByteCode> it = debugInfoItem.debugByteCode.iterator();
			DebugByteCode code = null;
			while (it.hasNext()) {
				code = it.next();
				debugInfoItemFile.write(code.value);
				if (code.value == 1) {
					debugInfoItemFile.writeULeb128((int)code.addrDiff);
        		} else if (code.value == 2) {
        			debugInfoItemFile.writeSLeb128((int)code.lineDiff);
        		} else if (code.value == 3) {
        			debugInfoItemFile.writeULeb128((int)code.registerNum);
        			debugInfoItemFile.writeULeb128(1 + (int)stringIndexMap[(int)code.name]);
        			debugInfoItemFile.writeULeb128(1 + (int)typeIndexMap[(int)code.type]);
        		} else if (code.value == 4) {
        			debugInfoItemFile.writeULeb128((int)code.registerNum);
        			debugInfoItemFile.writeULeb128(1 + (int)stringIndexMap[(int)code.name]);
        			debugInfoItemFile.writeULeb128(1 + (int)typeIndexMap[(int)code.type]);
        			debugInfoItemFile.writeULeb128(1 + (int)stringIndexMap[(int)code.sig]);
        		} else if (code.value == 5 || code.value == 6) {
        			debugInfoItemFile.writeULeb128((int)code.registerNum);
        		} else if (code.value == 9) {
        			debugInfoItemFile.writeULeb128(1 + (int)stringIndexMap[(int)code.name]);
        		}
			}
				
			
			debugInfoItemFile.writeULeb128((int)debugInfoItem.lineStart);
		}
		
		
		fileIndex = 0;
		mapIndex = 0;
		CodeItem codeItem;
		int codeItemSize = (int)original.getCodeItemSize();
		for (int i = 0; i < codeItemSize; ++i) {
			codeItem = original.getCodeItem();
			codeItemFile.write16bit(codeItem.registersSize);
			codeItemFile.write16bit(codeItem.insSize);
			codeItemFile.write16bit(codeItem.outsSize);
			codeItemFile.write16bit(codeItem.triesSize);
			codeItemFile.write(0L);
			codeItemFile.write(codeItem.insnsSize);
			for (int j = 0; j < codeItem.insnsSize; ++j) {
				codeItemFile.write16bit(codeItem.insns[j]);
			}
			if (codeItem.insnsSize % 2 == 1)
				codeItemFile.write16bit(0);
			
			for (int j = 0; j < codeItem.triesSize; ++j) {
				codeItemFile.write(codeItem.tries[j].startAddr);
				codeItemFile.write16bit(codeItem.tries[j].insnCount);
				codeItemFile.write16bit(codeItem.tries[j].handlerOffset);
			}
			
			if (codeItem.triesSize > 0) {
				codeItemFile.writeULeb128((int)codeItem.handlers.size);
			
				for (int j = 0; j < codeItem.handlers.size; ++j) {
					codeItemFile.writeSLeb128((int)codeItem.handlers.list[j].size);
					for (int k = 0; k < Math.abs(codeItem.handlers.list[j].size); ++k) {
						codeItemFile.writeULeb128((int)typeIndexMap[(int)codeItem.handlers.list[j].handlers[k].type]);
						codeItemFile.writeULeb128((int)codeItem.handlers.list[j].handlers[k].addr);
					}
					if (codeItem.handlers.list[j].size <= 0)
						codeItemFile.writeULeb128((int)codeItem.handlers.list[j].catchAllAddr);
				}
				
				for (int j = 0; j < codeItem.times; ++j) {
					codeItemFile.write(0);
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

package patch;

import item.AnnotationItem;
import item.AnnotationSetItem;
import item.AnnotationSetRefList;
import item.AnnotationsDirectoryItem;
import item.ByteCode;
import item.ClassDataItem;
import item.ClassDefItem;
import item.CodeItem;
import item.DebugByteCode;
import item.DebugInfoItem;
import item.EncodedAnnotation;
import item.EncodedArray;
import item.EncodedValue;
import item.FieldIdItem;
import item.MethodIdItem;
import item.ProtoIdItem;
import item.TypeIdItem;
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
import java.util.List;


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
		GeneratedFile stringIdsFile = new GeneratedFile("string_id.dex");
		GeneratedFile typeIdsFile = new GeneratedFile("type.dex");
		GeneratedFile fieldIdsFile = new GeneratedFile("field.dex");
		GeneratedFile stringFile = new GeneratedFile("string_data_item.dex");
		GeneratedFile protoFile = new GeneratedFile("proto.dex");
		GeneratedFile methodFile = new GeneratedFile("method.dex");
		GeneratedFile typeListFile = new GeneratedFile("type_list.dex");
		GeneratedFile annotationsDirectoryItemFile = new GeneratedFile("annotations_directory_item.dex");
		GeneratedFile classDataItemFile = new GeneratedFile("class_data_item.dex");
		GeneratedFile encodedArrayItemFile = new GeneratedFile("encoded_array_item.dex");
		GeneratedFile classDefItemFile = new GeneratedFile("class_def_item.dex");
		GeneratedFile annotationItemFile = new GeneratedFile("annotation_item.dex");
		GeneratedFile annotationSetItemFile = new GeneratedFile("annotation_set_item.dex");
		GeneratedFile annotationSetRefListFile = new GeneratedFile("annotation_set_ref_list.dex");
		GeneratedFile debugInfoItemFile = new GeneratedFile("debug_info_item.dex");
		GeneratedFile codeItemFile = new GeneratedFile("code_item.dex");
		GeneratedFile headerFile = new GeneratedFile(args[2]);
		GeneratedFile mapListFile = new GeneratedFile("map_list.dex");
		
		MapManager mm = new MapManager();
		PatchCommand command;
		int fileIndex = 0;
		int mapIndex = 0;
		long currentStringOffset = patch.getStringOffset();
		stringIdsFile.write(currentStringOffset);
		String buf;
		List<Byte> buffer;
		
		// Generate patched string_ids and string_data_items
		while(patch.hasStringCommands()) {
			command = patch.getNextStringCommand();
			
			if (command.type == 0) {
				// KEEP
				for(int i = 0; i < command.size; ++i) {
					buf = original.getStringData().data;
					currentStringOffset += buf.length() + 1;
					if (patch.hasStringCommands() || i < command.size - 1)
						stringIdsFile.write(currentStringOffset);
					stringFile.write(buf);
					stringFile.write(0);
					mm.stringIndexMap[mapIndex++] = fileIndex++;
				}
			} else if (command.type == 1) {
				// ADD
				for(int i = 0; i < command.size; ++i) {
					buffer = patch.getNextData();
					int size = buffer.size();
					int size_buf = 2;
					while (size > 127) {
						size = size >> 8;
						++size_buf;
					}
					currentStringOffset += buffer.size() + size_buf;
					// TODO Handle cases when length is larger than 127.
					if (patch.hasStringCommands() || i < command.size - 1)
						stringIdsFile.write(currentStringOffset);
					stringFile.write(buffer.size());
					stringFile.write(buffer);
					stringFile.write((char)0);
					++fileIndex;
				}
			} else if (command.type == 2) {
				// DELETE
				for(int i = 0; i < command.size; ++i) {
					original.getStringData();
					mm.stringIndexMap[mapIndex++] = -1;
				}
			} else if (command.type == 3) {
				// MODIFY
				for(int i = 0; i < command.size; ++i) {
					original.getStringData();
					mm.stringIndexMap[mapIndex++] = fileIndex;
				}
			}
		}
		
		fileIndex = 0;
		mapIndex = 0;
		TypeIdItem typeItem = null;
		// Generate patched type_ids
		while(patch.hasTypeCommands()) {
			command = patch.getNextTypeCommand();
			if (command.type == 0) {
				// KEEP
				for(int i = 0; i < command.size; ++i) {
					typeItem = original.getTypeIdData();
					typeIdsFile.write((mm.stringIndexMap[typeItem.stringIndex]));
					mm.typeIndexMap[mapIndex++] = fileIndex++;
				}
			} else if (command.type == 1) {
				// ADD
				for(int i = 0; i < command.size; ++i) {
					typeIdsFile.write(patch.getNextData());
					++fileIndex;
				}
			} else if (command.type == 2) {
				// DELETE
				for(int i = 0; i < command.size; ++i) {
					original.getTypeIdData();
					mm.typeIndexMap[mapIndex++] = -1;
				}
			} else if (command.type == 3) {
				// MODIFY
				for(int i = 0; i < command.size; ++i) {
					original.getTypeIdData();
					mm.typeIndexMap[mapIndex++] = fileIndex;
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
					fieldIdsFile.write16bit(mm.typeIndexMap[item.classId]);
					fieldIdsFile.write16bit(mm.typeIndexMap[item.typeId]);
					fieldIdsFile.write(mm.stringIndexMap[(int) item.nameId]);
					mm.fieldIndexMap[mapIndex++] = fileIndex++;
				}
			} else if (command.type == 1) {
				// ADD
				for(int i = 0; i < command.size; ++i) {
					fieldIdsFile.write(patch.getNextData());
					++fileIndex;
				}
			} else if (command.type == 2) {
				// DELETE
				for(int i = 0; i < command.size; ++i) {
					original.getFieldIdData();
					mm.fieldIndexMap[mapIndex++] = -1;
				}
			} else if (command.type == 3) {
				// MODIFY
				for(int i = 0; i < command.size; ++i) {
					original.getFieldIdData();
					mm.fieldIndexMap[mapIndex++] = fileIndex;
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
					mm.typeListIndexMap[mapIndex++] = fileIndex++;
					mm.typeListPointerMap[pointerIndex++] = tempPointer;
					typeList = original.getTypeList();
					typeListFile.write(typeList.size);
					for (int j = 0; j < typeList.size; ++j) {
						typeListFile.write16bit(mm.typeIndexMap[typeList.types[j]]);
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
					mm.typeListPointerMap[pointerIndex++] = tempPointer;
					
					List<Byte> li = patch.getNextData();
					tempPointer += li.size();
					typeListFile.write(li);
					/*long tlsize = Long.parseLong(patch.getNextData());
					typeListFile.write(tlsize);
					for (int j = 0; j < tlsize; ++j) {
						typeListFile.write16bit(Long.parseLong(patch.getNextData()));
					}
					if (tlsize % 2 == 1) {
						typeListFile.write16bit(0L);
						tempPointer += 2;
					}
					tempPointer += 4 + tlsize * 2;*/
				}
			} else if (command.type == 2) {
				// DELETE
				for(int i = 0; i < command.size; ++i) {
					original.getTypeList();
					mm.typeListIndexMap[mapIndex++] = -1;
				}
			} else if (command.type == 3) {
				// MODIFY
				for(int i = 0; i < command.size; ++i) {
					original.getTypeList();
					mm.typeListIndexMap[mapIndex++] = fileIndex;
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
					protoFile.write(mm.stringIndexMap[(int)protoItem.shorty]);
					protoFile.write(mm.typeIndexMap[(int)protoItem.type]);
					if (protoItem.typeListIndex == -1) {
						protoFile.write(0L);
					} else {
						protoFile.write(mm.typeListPointerMap[(int) mm.typeListIndexMap[protoItem.typeListIndex]]);
					}
					mm.protoIndexMap[mapIndex++] = fileIndex++;
				}
			} else if (command.type == 1) {
				// ADD
				for(int i = 0; i < command.size; ++i) {
					protoFile.write(patch.getNextData());
					++fileIndex;
				}
			} else if (command.type == 2) {
				// DELETE
				for(int i = 0; i < command.size; ++i) {
					original.getProtoIdData();
					mm.protoIndexMap[mapIndex++] = -1;
				}
			} else if (command.type == 3) {
				// MODIFY
				for(int i = 0; i < command.size; ++i) {
					original.getProtoIdData();
					mm.protoIndexMap[mapIndex++] = fileIndex;
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
					methodFile.write16bit(mm.typeIndexMap[(int)methodItem.classId]);
					methodFile.write16bit(mm.protoIndexMap[(int)methodItem.proto]);
					methodFile.write(mm.stringIndexMap[(int)methodItem.name]);
					mm.methodIndexMap[mapIndex++] = fileIndex++;
				}
			} else if (command.type == 1) {
				// ADD
				for(int i = 0; i < command.size; ++i) {
					methodFile.write(patch.getNextData());
					++fileIndex;
				}
			} else if (command.type == 2) {
				// DELETE
				for(int i = 0; i < command.size; ++i) {
					original.getMethodIdData();
					mm.methodIndexMap[mapIndex++] = -1;
				}
			} else if (command.type == 3) {
				// MODIFY
				for(int i = 0; i < command.size; ++i) {
					original.getMethodIdData();
					mm.methodIndexMap[mapIndex++] = fileIndex;
				}
			}
		}
		
		
		// debug_info_item
		fileIndex = 0;
		mapIndex = 0;
		DebugInfoItem debugInfoItem = null;
		tempPointer = patch.getDebugInfoItemOffset();
		pointerIndex = 0;
		int debugInfoItemSize = (int)original.getDebugInfoItemSize();
		while(patch.hasDebugInfoCommands()) {
			command = patch.getNextDebugInfoCommand();
			if (command.type == 0) {
				// KEEP
				for(int i = 0; i < command.size; ++i) {
					debugInfoItem = original.getDebugInfoItem();
					
					mm.debugInfoItemMap[mapIndex++] = fileIndex++;
					mm.debugInfoItemPointerMap[pointerIndex++] = tempPointer;
					
					List<Byte> temp = debugInfoItem.getModifiedData(mm);
					tempPointer += temp.size();
					
					debugInfoItemFile.write(temp);
					
					/*debugInfoItemFile.writeULeb128((int)debugInfoItem.lineStart);
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
					debugInfoItemFile.write(0);*/
				}
				
			} else if (command.type == 1) {
				// ADD
				for(int i = 0; i < command.size; ++i) {
					mm.debugInfoItemPointerMap[pointerIndex++] = tempPointer;
					List<Byte> temp = patch.getNextData();
					tempPointer += temp.size();
					debugInfoItemFile.write(temp);
					++fileIndex;
					
				}
			} else if (command.type == 2) {
				// DELETE
				for(int i = 0; i < command.size; ++i) {
					original.getDebugInfoItem();
					mm.debugInfoItemMap[mapIndex++] = -1;
				}
			} else if (command.type == 3) {
				// MODIFY
				for(int i = 0; i < command.size; ++i) {
					original.getDebugInfoItem();
					mm.debugInfoItemMap[mapIndex++] = fileIndex;
				}
			}
			
		}
		
		
		fileIndex = 0;
		mapIndex = 0;
		CodeItem codeItem = null;
		tempPointer = patch.getCodeItemOffset();
		pointerIndex = 0;
		// Generate patched code_items
		while(patch.hasCodeItemCommands()) {
			command = patch.getNextCodeItemCommand();
			if (command.type == 0) {
				// KEEP
				for(int i = 0; i < command.size; ++i) {
					mm.codeItemIndexMap[mapIndex++] = fileIndex++;
					mm.codeItemPointerMap[pointerIndex++] = tempPointer;
					codeItem = original.getCodeItem();
					List<Byte> temp = codeItem.getModifiedData(mm);
					tempPointer += temp.size();
					codeItemFile.write(temp);
				}
			} else if (command.type == 1) {
				// ADD
				for(int i = 0; i < command.size; ++i) {
					mm.codeItemPointerMap[pointerIndex++] = tempPointer;
					List<Byte> temp = patch.getNextData();
					tempPointer += temp.size();
					codeItemFile.write(temp);
					++fileIndex;
				}
			} else if (command.type == 2) {
				// DELETE
				for(int i = 0; i < command.size; ++i) {
					original.getCodeItem();
					mm.codeItemIndexMap[mapIndex++] = -1;
				}
			} else if (command.type == 3) {
				// MODIFY
				for(int i = 0; i < command.size; ++i) {
					original.getCodeItem();
					mm.codeItemIndexMap[mapIndex++] = fileIndex;
				}
			}
		}
		
		
		// annotation_item
		fileIndex = 0;
		mapIndex = 0;
		AnnotationItem annotationItem = null;
		tempPointer = patch.getAnnotationItemOffset();
		pointerIndex = 0;
		// Generate patched annotation_items
		while(patch.hasAnnotationItemCommands()) {
			command = patch.getNextAnnotationItemCommand();
			if (command.type == 0) {
				// KEEP
				for(int i = 0; i < command.size; ++i) {
					mm.annotationItemIndexMap[mapIndex++] = fileIndex++;
					mm.annotationItemPointerMap[pointerIndex++] = tempPointer;
					annotationItem = original.getAnnotationItem();
					List<Byte> temp = annotationItem.getModifiedData(mm);
					tempPointer += temp.size();
					annotationItemFile.write(temp);
				}
			} else if (command.type == 1) {
				// ADD
				for(int i = 0; i < command.size; ++i) {
					mm.annotationItemPointerMap[pointerIndex++] = tempPointer;
					List<Byte> temp = patch.getNextData();
					tempPointer += temp.size();
					annotationItemFile.write(temp);
					++fileIndex;
				}
			} else if (command.type == 2) {
				// DELETE
				for(int i = 0; i < command.size; ++i) {
					original.getAnnotationItem();
					mm.annotationItemIndexMap[mapIndex++] = -1;
				}
			} else if (command.type == 3) {
				// MODIFY
				for(int i = 0; i < command.size; ++i) {
					original.getAnnotationItem();
					mm.annotationItemIndexMap[mapIndex++] = fileIndex;
				}
			}
		}
		
		
		// annotation_set_item
		fileIndex = 0;
		mapIndex = 0;
		AnnotationSetItem annotationSetItem = null;
		tempPointer = patch.getAnnotationSetItemOffset();
		pointerIndex = 0;
		// Generate patched annotation_items
		while(patch.hasAnnotationSetItemCommands()) {
			command = patch.getNextAnnotationSetItemCommand();
			if (command.type == 0) {
				// KEEP
				for(int i = 0; i < command.size; ++i) {
					mm.annotationSetItemIndexMap[mapIndex++] = fileIndex++;
					mm.annotationSetItemPointerMap[pointerIndex++] = tempPointer;
					annotationSetItem = original.getAnnotationSetItem();
					List<Byte> temp = annotationSetItem.getModifiedData(mm);
					tempPointer += temp.size();
					annotationSetItemFile.write(temp);
				}
			} else if (command.type == 1) {
				// ADD
				for(int i = 0; i < command.size; ++i) {
					mm.annotationSetItemPointerMap[pointerIndex++] = tempPointer;
					List<Byte> temp = patch.getNextData();
					tempPointer += temp.size();
					annotationSetItemFile.write(temp);
					++fileIndex;
				}
			} else if (command.type == 2) {
				// DELETE
				for(int i = 0; i < command.size; ++i) {
					original.getAnnotationSetItem();
					mm.annotationSetItemIndexMap[mapIndex++] = -1;
				}
			} else if (command.type == 3) {
				// MODIFY
				for(int i = 0; i < command.size; ++i) {
					original.getAnnotationSetItem();
					mm.annotationSetItemIndexMap[mapIndex++] = fileIndex;
				}
			}
		}
		
		
		// annotation_set_ref_list
		fileIndex = 0;
		mapIndex = 0;
		AnnotationSetRefList annotationSetRefList = null;
		tempPointer = patch.getAnnotationSetRefListOffset();
		pointerIndex = 0;
		// Generate patched annotation_items
		while(patch.hasAnnotationSetRefListCommands()) {
			command = patch.getNextAnnotationSetRefListCommand();
			if (command.type == 0) {
				// KEEP
				for(int i = 0; i < command.size; ++i) {
					mm.annotationSetRefListIndexMap[mapIndex++] = fileIndex++;
					mm.annotationSetRefListPointerMap[pointerIndex++] = tempPointer;
					annotationSetRefList= original.getAnnotationSetRefList();
					List<Byte> temp = annotationSetRefList.getModifiedData(mm);
					tempPointer += temp.size();
					annotationSetRefListFile.write(temp);
				}
			} else if (command.type == 1) {
				// ADD
				for(int i = 0; i < command.size; ++i) {
					mm.annotationSetRefListPointerMap[pointerIndex++] = tempPointer;
					List<Byte> temp = patch.getNextData();
					tempPointer += temp.size();
					annotationSetRefListFile.write(temp);
					++fileIndex;
				}
			} else if (command.type == 2) {
				// DELETE
				for(int i = 0; i < command.size; ++i) {
					original.getAnnotationSetRefList();
					mm.annotationSetRefListIndexMap[mapIndex++] = -1;
				}
			} else if (command.type == 3) {
				// MODIFY
				for(int i = 0; i < command.size; ++i) {
					original.getAnnotationSetRefList();
					mm.annotationSetRefListIndexMap[mapIndex++] = fileIndex;
				}
			}
		}
		
		
		// annotations_directory_item
		fileIndex = 0;
		mapIndex = 0;
		AnnotationsDirectoryItem annotationsDirectoryItem = null;
		tempPointer = patch.getAnnotationsDirectoryItemOffset();
		pointerIndex = 0;
		// Generate patched annotations_directory_items
		while(patch.hasAnnotationsDirectoryItemCommands()) {
			command = patch.getNextAnnotationsDirectoryItemCommand();
			if (command.type == 0) {
				// KEEP
				for(int i = 0; i < command.size; ++i) {
					mm.annotationsDirectoryItemIndexMap[mapIndex++] = fileIndex++;
					mm.annotationsDirectoryItemPointerMap[pointerIndex++] = tempPointer;
					annotationsDirectoryItem = original.getAnnotationsDirectoryItem();
					List<Byte> temp = annotationsDirectoryItem.getModifiedData(mm);
					tempPointer += temp.size();
					annotationsDirectoryItemFile.write(temp);
				}
			} else if (command.type == 1) {
				// ADD
				for(int i = 0; i < command.size; ++i) {
					mm.annotationsDirectoryItemPointerMap[pointerIndex++] = tempPointer;
					List<Byte> temp = patch.getNextData();
					tempPointer += temp.size();
					annotationsDirectoryItemFile.write(temp);
					++fileIndex;
				}
			} else if (command.type == 2) {
				// DELETE
				for(int i = 0; i < command.size; ++i) {
					original.getAnnotationsDirectoryItem();
					mm.annotationsDirectoryItemIndexMap[mapIndex++] = -1;
				}
			} else if (command.type == 3) {
				// MODIFY
				for(int i = 0; i < command.size; ++i) {
					original.getAnnotationsDirectoryItem();
					mm.annotationsDirectoryItemIndexMap[mapIndex++] = fileIndex;
				}
			}
		}
		
		// class_data_item
		fileIndex = 0;
		mapIndex = 0;
		ClassDataItem classDataItem = null;
		tempPointer = patch.getClassDataItemOffset();
		pointerIndex = 0;
		// Generate patched class_data_items
		while(patch.hasClassDataItemCommands()) {
			command = patch.getNextClassDataItemCommand();
			if (command.type == 0) {
				// KEEP
				for(int i = 0; i < command.size; ++i) {
					mm.classDataItemIndexMap[mapIndex++] = fileIndex++;
					mm.classDataItemPointerMap[pointerIndex++] = tempPointer;
					classDataItem = original.getClassDataItem();
					List<Byte> temp = classDataItem.getModifiedData(mm);
					tempPointer += temp.size();
					classDataItemFile.write(temp);
				}
			} else if (command.type == 1) {
				// ADD
				for(int i = 0; i < command.size; ++i) {
					mm.classDataItemPointerMap[pointerIndex++] = tempPointer;
					List<Byte> temp = patch.getNextData();
					tempPointer += temp.size();
					classDataItemFile.write(temp);
					++fileIndex;
				}
			} else if (command.type == 2) {
				// DELETE
				for(int i = 0; i < command.size; ++i) {
					original.getClassDataItem();
					mm.classDataItemIndexMap[mapIndex++] = -1;
				}
			} else if (command.type == 3) {
				// MODIFY
				for(int i = 0; i < command.size; ++i) {
					original.getClassDataItem();
					mm.classDataItemIndexMap[mapIndex++] = fileIndex;
				}
			}
		}
		
		// encoded_array_item
		fileIndex = 0;
		mapIndex = 0;
		EncodedArray encodedArrayItem = null;
		tempPointer = patch.getEncodedArrayItemOffset();
		pointerIndex = 0;
		// Generate patched encoded_array_items
		while(patch.hasEncodedArrayItemCommands()) {
			command = patch.getNextEncodedArrayItemCommand();
			if (command.type == 0) {
				// KEEP
				for(int i = 0; i < command.size; ++i) {
					mm.encodedArrayItemIndexMap[mapIndex++] = fileIndex++;
					mm.encodedArrayItemPointerMap[pointerIndex++] = tempPointer;
					encodedArrayItem = original.getEncodedArrayItem();
					List<Byte> temp = encodedArrayItem.getModifiedData(mm);
					tempPointer += temp.size();
					encodedArrayItemFile.write(temp);
				}
			} else if (command.type == 1) {
				// ADD
				for(int i = 0; i < command.size; ++i) {
					mm.encodedArrayItemPointerMap[pointerIndex++] = tempPointer;
					List<Byte> temp = patch.getNextData();
					tempPointer += temp.size();
					encodedArrayItemFile.write(temp);
					++fileIndex;
				}
			} else if (command.type == 2) {
				// DELETE
				for(int i = 0; i < command.size; ++i) {
					original.getEncodedArrayItem();
					mm.encodedArrayItemIndexMap[mapIndex++] = -1;
				}
			} else if (command.type == 3) {
				// MODIFY
				for(int i = 0; i < command.size; ++i) {
					original.getEncodedArrayItem();
					mm.encodedArrayItemIndexMap[mapIndex++] = fileIndex;
				}
			}
		}
		
		// class_def_item
		fileIndex = 0;
		mapIndex = 0;
		ClassDefItem classDefItem = null;
		// Generate patched class_def_items
		while(patch.hasClassDefItemCommands()) {
			command = patch.getNextClassDefItemCommand();
			if (command.type == 0) {
				// KEEP
				for(int i = 0; i < command.size; ++i) {
					mm.classDefItemIndexMap[mapIndex++] = fileIndex++;
					classDefItem = original.getClassDefItem();
					List<Byte> temp = classDefItem.getModifiedData(mm);
					classDefItemFile.write(temp);
				}
			} else if (command.type == 1) {
				// ADD
				for(int i = 0; i < command.size; ++i) {
					List<Byte> temp = patch.getNextData();
					classDefItemFile.write(temp);
					++fileIndex;
				}
			} else if (command.type == 2) {
				// DELETE
				for(int i = 0; i < command.size; ++i) {
					original.getClassDefItem();
					mm.classDefItemIndexMap[mapIndex++] = -1;
				}
			} else if (command.type == 3) {
				// MODIFY
				for(int i = 0; i < command.size; ++i) {
					original.getClassDefItem();
					mm.classDefItemIndexMap[mapIndex++] = fileIndex;
				}
			}
		}
		
		// header
		List<Byte> temp = patch.getNextData();
		headerFile.write(temp);
		
		// map_list
		temp = patch.getNextData();
		for (int i = 0; i < patch.getOverflow(); ++i) {
			mapListFile.write(0);
		}
		mapListFile.write(temp);
		
		stringIdsFile.close();
		typeIdsFile.close();
		fieldIdsFile.close();
		stringFile.close();
		protoFile.close();
		methodFile.close();
		typeListFile.close();
		annotationsDirectoryItemFile.close();
		classDataItemFile.close();
		encodedArrayItemFile.close();
		classDefItemFile.close();
		annotationItemFile.close();
		annotationSetItemFile.close();
		annotationSetRefListFile.close();
		debugInfoItemFile.close();
		codeItemFile.close();
		headerFile.close();
		mapListFile.close();
		
		writeCompleteFile(args[2], "string_id.dex");
		writeCompleteFile(args[2], "type.dex");
		writeCompleteFile(args[2], "proto.dex");
		writeCompleteFile(args[2], "field.dex");
		writeCompleteFile(args[2], "method.dex");
		writeCompleteFile(args[2], "class_def_item.dex");
		writeCompleteFile(args[2], "annotation_set_item.dex");
		writeCompleteFile(args[2], "code_item.dex");
		writeCompleteFile(args[2], "annotations_directory_item.dex");
		writeCompleteFile(args[2], "type_list.dex");
		writeCompleteFile(args[2], "string_data_item.dex");
		writeCompleteFile(args[2], "debug_info_item.dex");
		writeCompleteFile(args[2], "annotation_item.dex");
		writeCompleteFile(args[2], "class_data_item.dex");
		writeCompleteFile(args[2], "map_list.dex");
		
		
		File f = new File("string_id.dex");
		f.delete();
		f = new File("type.dex");
		f.delete();
		f = new File("proto.dex");
		f.delete();
		f = new File("field.dex");
		f.delete();
		f = new File("method.dex");
		f.delete();
		f = new File("class_def_item.dex");
		f.delete();
		f = new File("annotation_set_item.dex");
		f.delete();
		f = new File("code_item.dex");
		f.delete();
		f = new File("annotations_directory_item.dex");
		f.delete();
		f = new File("type_list.dex");
		f.delete();
		f = new File("string_data_item.dex");
		f.delete();
		f = new File("debug_info_item.dex");
		f.delete();
		f = new File("annotation_item.dex");
		f.delete();
		f = new File("class_data_item.dex");
		f.delete();
		f = new File("map_list.dex");
		f.delete();
		f = new File("annotation_set_ref_list.dex");
		f.delete();
		f = new File("encoded_array_item.dex");
		f.delete();
		f = new File("string_id.dex");
		f.delete();
		
		
	}
	
	
	static void writeCompleteFile(String patchFile, String dataFile) {
		try {
			//GeneratedFile completeFile = new GeneratedFile("complete.patch");
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

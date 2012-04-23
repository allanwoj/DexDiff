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
		GeneratedFile stringIdsFile = new GeneratedFile("out/string_id.dex");
		GeneratedFile typeIdsFile = new GeneratedFile("out/type.dex");
		GeneratedFile fieldIdsFile = new GeneratedFile("out/field.dex");
		GeneratedFile stringFile = new GeneratedFile("out/string_data_item.dex");
		GeneratedFile protoFile = new GeneratedFile("out/proto.dex");
		GeneratedFile methodFile = new GeneratedFile("out/method.dex");
		GeneratedFile typeListFile = new GeneratedFile("out/type_list.dex");
		GeneratedFile annotationsDirectoryItemFile = new GeneratedFile("out/annotations_directory_item.dex");
		GeneratedFile classDataItemFile = new GeneratedFile("out/class_data_item.dex");
		GeneratedFile encodedArrayItemFile = new GeneratedFile("out/encoded_array_item.dex");
		GeneratedFile classDefItemFile = new GeneratedFile("out/class_def_item.dex");
		GeneratedFile annotationItemFile = new GeneratedFile("out/annotation_item.dex");
		GeneratedFile annotationSetItemFile = new GeneratedFile("out/annotation_set_item.dex");
		GeneratedFile annotationSetRefListFile = new GeneratedFile("out/annotation_set_ref_list.dex");
		GeneratedFile debugInfoItemFile = new GeneratedFile("out/debug_info_item.dex");
		GeneratedFile codeItemFile = new GeneratedFile("out/code_item.dex");
		GeneratedFile headerFile = new GeneratedFile("out/update.dex");
		GeneratedFile mapListFile = new GeneratedFile("out/map_list.dex");
		long[] stringIndexMap = new long[10000];
		long[] typeIndexMap = new long[10000];
		long[] fieldIndexMap = new long[10000];
		long[] protoIndexMap = new long[10000];
		long[] methodIndexMap = new long[10000];
		long[] typeListIndexMap = new long[10000];
		long[] typeListPointerMap = new long[10000];
		long[] classDataItemIndexMap = new long[10000];
		long[] classDataItemPointerMap = new long[10000];
		long[] encodedArrayItemIndexMap = new long[10000];
		long[] encodedArrayItemPointerMap = new long[10000];
		long[] classDefItemIndexMap = new long[10000];
		long[] annotationItemIndexMap = new long[10000];
		long[] annotationItemPointerMap = new long[10000];
		long[] annotationSetItemIndexMap = new long[10000];
		long[] annotationSetItemPointerMap = new long[10000];
		long[] annotationSetRefListIndexMap = new long[10000];
		long[] annotationSetRefListPointerMap = new long[10000];
		long[] annotationsDirectoryItemIndexMap = new long[10000];
		long[] annotationsDirectoryItemPointerMap = new long[10000];
		long[] debugInfoItemMap = new long[10000];
		long[] debugInfoItemPointerMap = new long[10000];
		long[] codeItemIndexMap = new long[10000];
		long[] codeItemPointerMap = new long[10000];
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
					buf = original.getStringData();
					currentStringOffset += buf.length() + 1;
					if (patch.hasStringCommands() || i < command.size - 1)
						stringIdsFile.write(currentStringOffset);
					stringFile.write(buf);
					stringFile.write(0);
					stringIndexMap[mapIndex++] = fileIndex++;
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
					typeIdsFile.write(patch.getNextData());
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
					fieldIdsFile.write(stringIndexMap[(int) item.nameId]);
					fieldIndexMap[mapIndex++] = fileIndex++;
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
					fieldIndexMap[mapIndex++] = -1;
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
					protoFile.write(patch.getNextData());
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
					methodFile.write(patch.getNextData());
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
					
					debugInfoItemMap[mapIndex++] = fileIndex++;
					debugInfoItemPointerMap[pointerIndex++] = tempPointer;
					
					byte[] temp = debugInfoItem.getByteCode(stringIndexMap, typeIndexMap);
					tempPointer += temp.length;
					
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
					debugInfoItemPointerMap[pointerIndex++] = tempPointer;
					List<Byte> temp = patch.getNextData();
					tempPointer += temp.size();
					debugInfoItemFile.write(temp);
					++fileIndex;
					
				}
			} else if (command.type == 2) {
				// DELETE
				for(int i = 0; i < command.size; ++i) {
					original.getDebugInfoItem();
					debugInfoItemMap[mapIndex++] = -1;
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
					codeItemIndexMap[mapIndex++] = fileIndex++;
					codeItemPointerMap[pointerIndex++] = tempPointer;
					codeItem = original.getCodeItem();
					byte[] temp = codeItem.getOutput(fieldIndexMap, methodIndexMap, stringIndexMap, typeIndexMap, debugInfoItemMap, debugInfoItemPointerMap);
					tempPointer += temp.length;
					codeItemFile.write(temp);
				}
			} else if (command.type == 1) {
				// ADD
				for(int i = 0; i < command.size; ++i) {
					codeItemPointerMap[pointerIndex++] = tempPointer;
					List<Byte> temp = patch.getNextData();
					tempPointer += temp.size();
					codeItemFile.write(temp);
					++fileIndex;
				}
			} else if (command.type == 2) {
				// DELETE
				for(int i = 0; i < command.size; ++i) {
					original.getCodeItem();
					codeItemIndexMap[mapIndex++] = -1;
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
					annotationItemIndexMap[mapIndex++] = fileIndex++;
					annotationItemPointerMap[pointerIndex++] = tempPointer;
					annotationItem = original.getAnnotationItem();
					byte[] temp = annotationItem.getBytes(fieldIndexMap, methodIndexMap, stringIndexMap, typeIndexMap);
					tempPointer += temp.length;
					annotationItemFile.write(temp);
				}
			} else if (command.type == 1) {
				// ADD
				for(int i = 0; i < command.size; ++i) {
					annotationItemPointerMap[pointerIndex++] = tempPointer;
					List<Byte> temp = patch.getNextData();
					tempPointer += temp.size();
					annotationItemFile.write(temp);
					++fileIndex;
				}
			} else if (command.type == 2) {
				// DELETE
				for(int i = 0; i < command.size; ++i) {
					original.getAnnotationItem();
					annotationItemIndexMap[mapIndex++] = -1;
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
					annotationSetItemIndexMap[mapIndex++] = fileIndex++;
					annotationSetItemPointerMap[pointerIndex++] = tempPointer;
					annotationSetItem = original.getAnnotationSetItem();
					byte[] temp = annotationSetItem.getBytes(annotationItemIndexMap, annotationItemPointerMap);
					tempPointer += temp.length;
					annotationSetItemFile.write(temp);
				}
			} else if (command.type == 1) {
				// ADD
				for(int i = 0; i < command.size; ++i) {
					annotationSetItemPointerMap[pointerIndex++] = tempPointer;
					List<Byte> temp = patch.getNextData();
					tempPointer += temp.size();
					annotationSetItemFile.write(temp);
					++fileIndex;
				}
			} else if (command.type == 2) {
				// DELETE
				for(int i = 0; i < command.size; ++i) {
					original.getAnnotationSetItem();
					annotationSetItemIndexMap[mapIndex++] = -1;
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
					annotationSetRefListIndexMap[mapIndex++] = fileIndex++;
					annotationSetRefListPointerMap[pointerIndex++] = tempPointer;
					annotationSetRefList= original.getAnnotationSetRefList();
					byte[] temp = annotationSetRefList.getBytes(annotationSetItemIndexMap, annotationSetItemPointerMap);
					tempPointer += temp.length;
					annotationSetRefListFile.write(temp);
				}
			} else if (command.type == 1) {
				// ADD
				for(int i = 0; i < command.size; ++i) {
					annotationSetRefListPointerMap[pointerIndex++] = tempPointer;
					List<Byte> temp = patch.getNextData();
					tempPointer += temp.size();
					annotationSetRefListFile.write(temp);
					++fileIndex;
				}
			} else if (command.type == 2) {
				// DELETE
				for(int i = 0; i < command.size; ++i) {
					original.getAnnotationSetRefList();
					annotationSetRefListIndexMap[mapIndex++] = -1;
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
			if (tempPointer > 403264)
				System.out.print("hmmm");
			if (command.type == 0) {
				// KEEP
				for(int i = 0; i < command.size; ++i) {
					annotationsDirectoryItemIndexMap[mapIndex++] = fileIndex++;
					annotationsDirectoryItemPointerMap[pointerIndex++] = tempPointer;
					annotationsDirectoryItem = original.getAnnotationsDirectoryItem();
					byte[] temp = annotationsDirectoryItem.getBytes(fieldIndexMap, methodIndexMap, annotationSetItemIndexMap, annotationSetItemPointerMap, annotationSetRefListIndexMap, annotationSetRefListPointerMap);
					tempPointer += temp.length;
					annotationsDirectoryItemFile.write(temp);
				}
			} else if (command.type == 1) {
				// ADD
				for(int i = 0; i < command.size; ++i) {
					annotationsDirectoryItemPointerMap[pointerIndex++] = tempPointer;
					List<Byte> temp = patch.getNextData();
					tempPointer += temp.size();
					annotationsDirectoryItemFile.write(temp);
					++fileIndex;
				}
			} else if (command.type == 2) {
				// DELETE
				for(int i = 0; i < command.size; ++i) {
					original.getAnnotationsDirectoryItem();
					annotationsDirectoryItemIndexMap[mapIndex++] = -1;
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
					classDataItemIndexMap[mapIndex++] = fileIndex++;
					classDataItemPointerMap[pointerIndex++] = tempPointer;
					classDataItem = original.getClassDataItem();
					byte[] temp = classDataItem.getBytes(fieldIndexMap, methodIndexMap, codeItemIndexMap, codeItemPointerMap);
					tempPointer += temp.length;
					classDataItemFile.write(temp);
				}
			} else if (command.type == 1) {
				// ADD
				for(int i = 0; i < command.size; ++i) {
					classDataItemPointerMap[pointerIndex++] = tempPointer;
					List<Byte> temp = patch.getNextData();
					tempPointer += temp.size();
					classDataItemFile.write(temp);
					++fileIndex;
				}
			} else if (command.type == 2) {
				// DELETE
				for(int i = 0; i < command.size; ++i) {
					original.getClassDataItem();
					classDataItemIndexMap[mapIndex++] = -1;
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
					encodedArrayItemIndexMap[mapIndex++] = fileIndex++;
					encodedArrayItemPointerMap[pointerIndex++] = tempPointer;
					encodedArrayItem = original.getEncodedArrayItem();
					Collection<Byte> temp = encodedArrayItem.getData(fieldIndexMap, methodIndexMap, stringIndexMap, typeIndexMap);
					tempPointer += temp.size();
					encodedArrayItemFile.write(temp);
				}
			} else if (command.type == 1) {
				// ADD
				for(int i = 0; i < command.size; ++i) {
					encodedArrayItemPointerMap[pointerIndex++] = tempPointer;
					List<Byte> temp = patch.getNextData();
					tempPointer += temp.size();
					encodedArrayItemFile.write(temp);
					++fileIndex;
				}
			} else if (command.type == 2) {
				// DELETE
				for(int i = 0; i < command.size; ++i) {
					original.getEncodedArrayItem();
					encodedArrayItemIndexMap[mapIndex++] = -1;
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
					classDefItemIndexMap[mapIndex++] = fileIndex++;
					classDefItem = original.getClassDefItem();
					byte[] temp = classDefItem.getData(annotationsDirectoryItemIndexMap, annotationsDirectoryItemPointerMap, classDataItemIndexMap, classDataItemPointerMap, encodedArrayItemIndexMap, encodedArrayItemPointerMap, stringIndexMap, typeIndexMap, typeListIndexMap, typeListPointerMap);
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
					classDefItemIndexMap[mapIndex++] = -1;
				}
			}
		}
		
		// header
		List<Byte> temp = patch.getNextData();
		headerFile.write(temp);
		
		// map_list
		temp = patch.getNextData();
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
		
		writeCompleteFile("out/update.dex", "out/string_id.dex");
		writeCompleteFile("out/update.dex", "out/type.dex");
		writeCompleteFile("out/update.dex", "out/proto.dex");
		writeCompleteFile("out/update.dex", "out/field.dex");
		writeCompleteFile("out/update.dex", "out/method.dex");
		writeCompleteFile("out/update.dex", "out/class_def_item.dex");
		writeCompleteFile("out/update.dex", "out/annotation_set_item.dex");
		writeCompleteFile("out/update.dex", "out/code_item.dex");
		writeCompleteFile("out/update.dex", "out/annotations_directory_item.dex");
		writeCompleteFile("out/update.dex", "out/type_list.dex");
		writeCompleteFile("out/update.dex", "out/string_data_item.dex");
		writeCompleteFile("out/update.dex", "out/debug_info_item.dex");
		writeCompleteFile("out/update.dex", "out/annotation_item.dex");
		writeCompleteFile("out/update.dex", "out/class_data_item.dex");
		writeCompleteFile("out/update.dex", "out/map_list.dex");
		
		
		File f = new File("out/string_id.dex");
		f.delete();
		f = new File("out/type.dex");
		f.delete();
		f = new File("out/proto.dex");
		f.delete();
		f = new File("out/field.dex");
		f.delete();
		f = new File("out/method.dex");
		f.delete();
		f = new File("out/class_def_item.dex");
		f.delete();
		f = new File("out/annotation_set_item.dex");
		f.delete();
		f = new File("out/code_item.dex");
		f.delete();
		f = new File("out/annotations_directory_item.dex");
		f.delete();
		f = new File("out/type_list.dex");
		f.delete();
		f = new File("out/string_data_item.dex");
		f.delete();
		f = new File("out/debug_info_item.dex");
		f.delete();
		f = new File("out/annotation_item.dex");
		f.delete();
		f = new File("out/class_data_item.dex");
		f.delete();
		f = new File("out/map_list.dex");
		f.delete();
		f = new File("out/annotation_set_ref_list.dex");
		f.delete();
		f = new File("out/encoded_array_item.dex");
		f.delete();
		f = new File("out/string_id.dex");
		f.delete();
		
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

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
import item.EncodedCatchHandler;
import item.EncodedCatchHandlerList;
import item.EncodedField;
import item.EncodedMethod;
import item.EncodedTypeAddrPair;
import item.EncodedValue;
import item.FieldAnnotation;
import item.FieldIdItem;
import item.MethodAnnotation;
import item.MethodIdItem;
import item.ParameterAnnotation;
import item.ProtoIdItem;
import item.StringDataItem;
import item.TryItem;
import item.TypeIdItem;
import item.TypeList;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import android.DecodedInstruction;
import android.InstructionCodec;

import hu.uw.pallergabor.dedexer.DexParser;

public class DexOriginalFile extends DexParser {

	// Offsets and sizes
	private long fileSize;
    private long headerSize;
    private long linkSize;
    private long linkOffset;
    private long mapOffset;
    private long stringIdsSize;
    private long stringIdsOffset;
    private long typeIdsSize;
    private long typeIdsOffset;
    private long protoIdsSize;
    private long protoIdsOffset;
    private long fieldIdsSize;
    private long fieldIdsOffset;
    private long methodIdsSize;
    private long methodIdsOffset;
    private long classDefsSize;
    private long classDefsOffset;
    private long dataSize;
    private long dataOffset;
    
    private long typeListSize;
    private long typeListOffset;
    
    private long annotationSetRefListSize;
    private long annotationSetRefListOffset;
    
    private long annotationSetItemSize;
    private long annotationSetItemOffset;
    
    private long classDataItemSize;
    private long classDataItemOffset;
    
    private long codeItemSize;
    private long codeItemOffset;
    
    private long stringDataItemSize;
    private long stringDataItemOffset;
    
    private long debugInfoItemSize;
    private long debugInfoItemOffset;
    
    private long annotationItemSize;
    private long annotationItemOffset;
    
    private long encodedArrayItemSize;
    private long encodedArrayItemOffset;
    
    private long annotationsDirectoryItemSize;
    private long annotationsDirectoryItemOffset;
    
    private long overflow;
    
    public StringDataItem[] stringData;
    int stringDataIndex = 0;
    
    public TypeIdItem[] typeIds;
    int typeIdsIndex = 0;
    
    public FieldIdItem[] fieldIds;
    int fieldIdsIndex = 0;
    
    public ProtoIdItem[] protoIds;
    int protoIdsIndex = 0;
    
    public MethodIdItem[] methodIds;
    int methodIdsIndex = 0;
    
    HashMap<Long, Integer> annotationOffsetMap;
    public AnnotationItem[] annotationItems;
    int annotationItemsIndex = 0;
    
    HashMap<Long, Integer> annotationSetOffsetMap;
    public AnnotationSetItem[] annotationSetItems;
    int annotationSetItemsIndex = 0;
    
    HashMap<Long, Integer> annotationSetRefListOffsetMap;
    public AnnotationSetRefList[] annotationSetRefList;
    int annotationSetRefListIndex = 0;
    
    HashMap<Long, Integer> typeListOffsetMap;
    public TypeList[] typeLists;
    int typeListIndex = 0;
    
    HashMap<Long, Integer> annotationsDirectoryItemOffsetMap;
    public AnnotationsDirectoryItem[] annotationsDirectoryItems;
    int annotationsDirectoryItemsIndex = 0;
    
    public HashMap<Long, Integer> classDataItemOffsetMap;
    public ClassDataItem[] classDataItems;
    int classDataItemsIndex = 0;
    
    public HashMap<Long, Integer> encodedArrayItemOffsetMap;
    public EncodedArray[] encodedArrayItems;
    int encodedArrayItemIndex = 0;
    
    public ClassDefItem[] classDefItems;
    int classDefItemsIndex = 0;
    
    public HashMap<Long, Integer> debugInfoOffsetMap;
    public DebugInfoItem[] debugInfoItems;
    int debugInfoItemIndex = 0;
    
    HashMap<Long, Integer> codeItemOffsetMap;
    public CodeItem[] codeItems;
    int codeItemIndex = 0;
    
    public byte[] header;
    Collection<Byte> mapList;
    
    OutputStream outStream = null;
    
    private void doWrite(String data) {
    	/*byte[] b = new byte[data.length()];
		data.getBytes(0, data.length(), b, 0);
    	try {
			outStream.write(b);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
    }

	@Override
	public void parse() {
		/*try {
			outStream = new FileOutputStream("out/log.log");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		try {
			setFilePosition(0x0L);
			header = new byte[112];
			for(int i = 0; i < 112; ++i)
				header[i] = (byte)read8Bit();
			
			setFilePosition(0x20L);
			fileSize = read32Bit();
	        headerSize = read32Bit();
	        parseExpected32Bit( 0x12345678L );
	        linkSize = read32Bit();
	        linkOffset = readFileOffset();
	        mapOffset = readFileOffset();
	        stringIdsSize = read32Bit();
	        stringIdsOffset = readFileOffset();
	        typeIdsSize = read32Bit();
	        typeIdsOffset = readFileOffset();
	        protoIdsSize = read32Bit();
	        protoIdsOffset = readFileOffset();
	        fieldIdsSize = read32Bit();
	        fieldIdsOffset = readFileOffset();
	        methodIdsSize = read32Bit();
	        methodIdsOffset = readFileOffset();
	        classDefsSize = read32Bit();
	        classDefsOffset = readFileOffset();
	        dataSize = read32Bit();
	        dataOffset = readFileOffset();
			
	        setFilePosition(stringIdsOffset);
	        long stringsPos[] = new long[(int)stringIdsSize];
	        // Read the string offsets first
	        for(int i = 0 ; i < stringIdsSize; ++i) {
	        	stringsPos[i] = readFileOffset();
	        }
	        
	        typeIds = new TypeIdItem[(int)typeIdsSize];
	        // Read type_ids
	        for(int i = 0 ; i < typeIdsSize; ++i) {
	        	typeIds[i] = new TypeIdItem((int) read32Bit());
	        }
	        
	        stringData = new StringDataItem[(int)stringIdsSize];
	        // Read string_data_items
	        for(int i = 0 ; i < stringIdsSize; ++i) {
	        	setFilePosition(stringsPos[i]);
	        	stringData[i] = new StringDataItem(readString());
	        }
	        
	        setFilePosition(fieldIdsOffset);
	        fieldIds = new FieldIdItem[(int) fieldIdsSize];
	        // Read field_ids
	        for (int i = 0; i < fieldIdsSize; ++i) {
				fieldIds[i] = new FieldIdItem(read16Bit(), read16Bit(), (int)read32Bit());
			}
	        
	        
	        
	        setFilePosition(methodIdsOffset);
	        methodIds = new MethodIdItem[(int) methodIdsSize];
	        // Read proto_ids
	        for (int i = 0; i < methodIdsSize; ++i) {
				methodIds[i] = new MethodIdItem(read16Bit(), read16Bit(), read32Bit());
			}
	        
	        setFilePosition(mapOffset);
	        mapList = new ArrayList<Byte>();
	        // Read offsets
	        long mapSize = read32Bit();
	        mapList.addAll(write32bit(mapSize));
	        
	        for (int i = 0; i < mapSize; ++i) {
	        	int type = read16Bit();
	        	int unused = read16Bit();
	        	long size = read32Bit();
	        	long offset = read32Bit();
	        	
	        	mapList.addAll(write16bit(type));
	        	mapList.addAll(write16bit(unused));
	        	mapList.addAll(write32bit(size));
	        	mapList.addAll(write32bit(offset));
	        	
	        	
	        	switch (type) {
		        	/*case 0:break;
		        	case 1:break;
		        	case 2:break;
		        	case 3:break;
		        	case 4:break;
		        	case 5:break;
		        	case 6:break;
		        	case 4096:break;*/
		        	case 4097:
		        		typeListSize = size;
		        		typeListOffset = offset;
		        		break;
		        	case 4098:
		        		annotationSetRefListSize = size;
		        		annotationSetRefListOffset = offset;
		        		break;
		        	case 4099:
		        		annotationSetItemSize = size;
		        		annotationSetItemOffset = offset;
		        		break;
		        	case 8192:
		        		classDataItemSize = size;
		        		classDataItemOffset = offset;
		        		break;
		        	case 8193:
		        		codeItemSize = size;
		        		codeItemOffset = offset;
		        		break;
		        	case 8194:
		        		stringDataItemSize = size;
		        		stringDataItemOffset = offset;
		        		break;
		        	case 8195:
		        		debugInfoItemSize = size;
		        		debugInfoItemOffset = offset;
		        		break;
		        	case 8196:
		        		annotationItemSize = size;
		        		annotationItemOffset = offset;
		        		break;
		        	case 8197:
		        		encodedArrayItemSize = size;
		        		encodedArrayItemOffset = offset;
		        		break;
		        	case 8198:
		        		annotationsDirectoryItemSize = size;
		        		annotationsDirectoryItemOffset = offset;
		        		break;
		        	default:
		        		//read32Bit();
		        		//read32Bit();
	        	}
	        }
	        
	        setFilePosition(typeListOffset);
	        typeLists = new TypeList[(int)typeListSize];
	        typeListOffsetMap = new HashMap<Long, Integer>();
	        typeListOffsetMap.put(0L, -1);
	        int tlsize = 0;
	        int[] data = null;
	        // Read type_list
	        for (int i = 0; i < typeListSize; ++i) {
	        	typeListOffsetMap.put(position, i);
	        	doWrite("typeList[" + i + "]: \n");
	        	tlsize = (int)read32Bit();
	        	data = new int[tlsize];
	        	for (int j = 0; j < tlsize; ++j) {
	        		data[j] = read16Bit();
	        		doWrite(data[j] + ": " + stringData[typeIds[data[j]].stringIndex].data + "\n");
	        	}
	        	if (tlsize % 2 == 1)
	        		read16Bit();
	        	typeLists[i] = new TypeList(tlsize, data);
	        }
	        
	        setFilePosition(debugInfoItemOffset);
	        debugInfoItems = new DebugInfoItem[(int)debugInfoItemSize];
	        debugInfoOffsetMap = new HashMap<Long, Integer>();
	        debugInfoOffsetMap.put(0L, -1);
	        // Read debug_info_item
	        for (int i = 0; i < debugInfoItemSize; ++i) {
	        	debugInfoOffsetMap.put(position, i);
	        	
	        	long lineStart = readULEB128();
	        	long paramSize = readULEB128();
	        	doWrite("debugInfoItem[" + i + "]:" + lineStart + " " + paramSize + "\n");
	        	long[] names = new long[(int)paramSize];
	        	for (int j = 0; j < paramSize; ++j) {
	        		names[j] = readULEB128p1();
	        		if(names[j] == -1) {
	        			doWrite("NO_NAME\n");
	        		} else {
	        			doWrite(stringData[(int)names[j]].data + "\n");
	        		}
	        	}
	        	ArrayList<DebugByteCode> byteCode = new ArrayList<DebugByteCode>();
	        	int instruction = read8Bit();
	        	while(instruction != 0) {
	        		DebugByteCode b = null;
	        		if (instruction == 1) {
	        			b = new DebugByteCode(instruction, readULEB128(), 0, 0, 0, 0, 0);
	        		} else if (instruction == 2) {
	        			b = new DebugByteCode(instruction, 0, readSLEB128(), 0, 0, 0, 0);
	        		} else if (instruction == 3) {
	        			int reg = readULEB128();
	        			long name = readULEB128p1();
	        			long type = readULEB128p1();
	        			b = new DebugByteCode(instruction, 0, 0, name, type, 0, reg);
	        		} else if (instruction == 4) {
	        			int reg = readULEB128();
	        			long name = readULEB128p1();
	        			long type = readULEB128p1();
	        			long sig = readULEB128p1();
	        			b = new DebugByteCode(instruction, 0, 0, name, type, sig, reg);
	        		} else if (instruction == 5 || instruction == 6) {
	        			int reg = readULEB128();
	        			b = new DebugByteCode(instruction, 0, 0, 0, 0, 0, reg);
	        		} else if (instruction == 9) {
	        			long name = readULEB128p1();
	        			b = new DebugByteCode(instruction, 0, 0, name, 0, 0, 0);
	        		} else {
	        			b = new DebugByteCode(instruction, 0, 0, 0, 0, 0, 0);
	        		}
	        		byteCode.add(b);
	        		instruction = read8Bit();
	        	}
	        	debugInfoItems[i] = new DebugInfoItem(lineStart, paramSize, names, byteCode);
	        }
	        
	        
	        setFilePosition(codeItemOffset);
	        codeItems = new CodeItem[(int)codeItemSize];
	        codeItemOffsetMap = new HashMap<Long, Integer>();
	        codeItemOffsetMap.put(0L, -1);
	        AndroidCodeInput codeInput = new AndroidCodeInput(this);
	        // Read code_item
	        for (int i = 0; i < codeItemSize; ++i) {
	        	codeItemOffsetMap.put(position, i);
	        	int regSize = read16Bit();
	        	int insSize = read16Bit();
	        	int outsSize = read16Bit();
	        	int triesSize = read16Bit();
	        	long debugInfoOff = read32Bit();
	        	long insnsSize = read32Bit();
	        	byte[] insns = new byte[2 * (int)insnsSize];
	        	
	        	
	        	for (int j = 0; j < 2 * insnsSize; ++j) {
	        		insns[j] = (byte)read8Bit();
	        	}
	        	
	        	Collection<DecodedInstruction> decodedInstructions =
	                DecodedInstruction.decodeAll(insns);
	        	
	        	
	        	
	        	
	        	
	        	int padding = -1;
	        	if (insnsSize % 2 == 1)
	        		padding = read16Bit();
	        	TryItem[] tries = new TryItem[triesSize];
	        	for (int j = 0; j < triesSize; ++j) {
	        		tries[j] = new TryItem(read32Bit(), read16Bit(), read16Bit());
	        	}
	        	
	        	EncodedCatchHandlerList l = null;
	        	long currPos = position;
	        	if (triesSize > 0) {
	        		int listSize = readULEB128();
		        	EncodedCatchHandler[] list = new EncodedCatchHandler[listSize];
		        	for (int j = 0; j < listSize; ++j) {
		        		int s = readSLEB128();
		        		int ss = Math.abs(s);
		        		EncodedTypeAddrPair[] hand = new EncodedTypeAddrPair[ss];
		        		for (int k = 0; k < ss; ++k) {
		        			hand[k] = new EncodedTypeAddrPair(readULEB128(), readULEB128());
		        		}
		        		long catchAllAddr = -1;
		        		if (s <= 0) {
		        			catchAllAddr = readULEB128();
		        		}
		        		list[j] = new EncodedCatchHandler(s, hand, catchAllAddr);
		        	}
		        	l = new EncodedCatchHandlerList(listSize, list);
	        	}
	        	
	        	long times = (4 - ((position - currPos) % 4)) % 4;
	        	for (int j = 0; j < times; ++j)
	        		read8Bit();
	        	
	        	codeItems[i] = new CodeItem(regSize, insSize, outsSize, triesSize, debugInfoOffsetMap.get(debugInfoOff),
	        			debugInfoOff, insnsSize, null, insns, padding, tries, l, (int)times, decodedInstructions);
	        }
	        
	        
	        
	        
	        
	        setFilePosition(annotationItemOffset);
	        annotationItems = new AnnotationItem[(int)annotationItemSize];
	        annotationOffsetMap = new HashMap<Long, Integer>();
	        annotationOffsetMap.put(0L, -1);
	        // Read annotation_item
	        for (int i = 0; i < annotationItemSize; ++i) {
	        	annotationOffsetMap.put(position, i);
	        	int visibility = read8Bit();
	        	int type = readULEB128();
	        	int size = readULEB128();
	        	annotationItems[i] = new AnnotationItem(visibility, new EncodedAnnotation(type, size));
	        	for (int j = 0; j < size; ++j) {
	        		int name = readULEB128();
	        		Byte b = (byte) read8Bit();
	        		int valueType = b & 0x1F;
	        		int valueArg = (b & 0xFF) >> 5;

	        		if (valueType == 0x1C) {
	        			int anSize = readULEB128();
	        			EncodedValue[] vals = new EncodedValue[anSize];
	        			for (int k = 0; k < anSize; ++k) {
	        				vals[k] = getEncodedValue();
	        			}
	        			annotationItems[i].getAnnotation().values[j] = new EncodedValue(name, valueType, valueArg, new EncodedArray(anSize, vals));
	        			break;
	        		} else if (valueType == 0x1D) {
	        			annotationItems[i].getAnnotation().values[j] = new EncodedValue(name, valueType, valueArg, getEncodedAnnotation());
	        			break;
	        		} else if (valueType == 0x1E || valueArg == 0x1F) {
	        			annotationItems[i].getAnnotation().values[j] = new EncodedValue(name, valueType, valueArg);
	        			break;
	        		}
	        	
	        		Byte[] by = new Byte[valueArg + 1];
	        		for (int k = 0; k <= valueArg; ++k) {
	        			by[k] = (byte)read8Bit();
	        		}
	        		
	        		annotationItems[i].getAnnotation().values[j] = new EncodedValue(name, valueType, valueArg, by);
	        		
	        	}
	        	
	        }
	        
	        
	        setFilePosition(annotationSetItemOffset);
	        annotationSetItems = new AnnotationSetItem[(int)annotationSetItemSize];
	        annotationSetOffsetMap = new HashMap<Long, Integer>();
	        annotationSetOffsetMap.put(0L, -1);
	        // Read annotation_set_item
	        for (int i = 0; i < annotationSetItemSize; ++i) {
	        	annotationSetOffsetMap.put(position, i);
	        	long size = read32Bit();
	        	long[] entriesOffsets = new long[(int)size];
	        	int[] offsetIndex = new int[(int)size];
	        	for (int j = 0; j < size; ++j) {
	        		entriesOffsets[j] = read32Bit();
	        		offsetIndex[j] = annotationOffsetMap.get(entriesOffsets[j]);
	        	}
	        	annotationSetItems[i] = new AnnotationSetItem(size, entriesOffsets, offsetIndex);
	        }
	        
	        setFilePosition(annotationSetRefListOffset);
	        annotationSetRefList = new AnnotationSetRefList[(int)annotationSetRefListSize];
	        annotationSetRefListOffsetMap = new HashMap<Long, Integer>();
	        annotationSetRefListOffsetMap.put(0L, -1);
	        // Read annotation_set_ref_list
	        for (int i = 0; i < annotationSetRefListSize; ++i) {
	        	annotationSetRefListOffsetMap.put(position, i);
	        	long size = read32Bit();
	        	long[] offsets = new long[(int)size];
	        	int[] offsetIndex = new int[(int)size];
	        	for (int j = 0; j < size; ++j) {
	        		offsets[j] = read32Bit();
	        		offsetIndex[j] = annotationSetOffsetMap.get(offsets[j]);
	        	}
	        	annotationSetRefList[i] = new AnnotationSetRefList(size, offsets, offsetIndex);
	        }
	        
	        
	        
	        
	        setFilePosition(protoIdsOffset);
	        protoIds = new ProtoIdItem[(int) protoIdsSize];
	        // Read proto_ids
	        for (int i = 0; i < protoIdsSize; ++i) {
	        	long name = read32Bit();
	        	long type = read32Bit();
	        	long off = read32Bit();
				protoIds[i] = new ProtoIdItem(name, type, typeListOffsetMap.get(off), off);
			}
	        
	        setFilePosition(annotationsDirectoryItemOffset);
	        annotationsDirectoryItems = new AnnotationsDirectoryItem[(int)annotationsDirectoryItemSize];
	        annotationsDirectoryItemOffsetMap = new HashMap<Long, Integer>();
	        annotationsDirectoryItemOffsetMap.put(0L, -1);
	        long offset = 0;
	        long fieldsSize = 0;
	        long methodsSize = 0;
	        long paramsSize = 0;
	        // Read annotation_directory_item
	        for (int i = 0; i < annotationsDirectoryItemSize; ++i) {
	        	annotationsDirectoryItemOffsetMap.put(position, i);
	        	offset = read32Bit();
	        	fieldsSize = read32Bit();
	        	methodsSize = read32Bit();
	        	paramsSize = read32Bit();
	        	FieldAnnotation[] f = null;
	        	MethodAnnotation[] m = null;
	        	ParameterAnnotation[] p = null;
	        	if (fieldsSize > 0) {
	        		f = new FieldAnnotation[(int)fieldsSize];
	        		for (int j = 0; j < fieldsSize; ++j) {
	        			long fieldId = read32Bit();
	        			long off = read32Bit();
	        			f[j] = new FieldAnnotation(fieldId, annotationSetOffsetMap.get(off), off);
	        		}
	        	}
	        	if (methodsSize > 0) {
	        		m = new MethodAnnotation[(int)methodsSize];
	        		for (int j = 0; j < methodsSize; ++j) {
	        			long methId = read32Bit();
	        			long off = read32Bit();
	        			m[j] = new MethodAnnotation(methId, annotationSetOffsetMap.get(off), off);
	        		}
	        	}
	        	if (paramsSize > 0) {
	        		p = new ParameterAnnotation[(int)paramsSize];
	        		for (int j = 0; j < paramsSize; ++j) {
	        			long methId = read32Bit();
	        			long off = read32Bit();
	        			p[j] = new ParameterAnnotation(methId, annotationSetRefListOffsetMap.get(off), off);
	        		}
	        	}
	        	
	        	annotationsDirectoryItems[i] = new AnnotationsDirectoryItem(offset, annotationSetOffsetMap.get(offset), fieldsSize, methodsSize, paramsSize, f, m, p);
	        	
	        }
	        
	        encodedArrayItems = new EncodedArray[(int)encodedArrayItemSize];
        	encodedArrayItemOffsetMap = new HashMap<Long, Integer>();
	        encodedArrayItemOffsetMap.put(0L, -1);
	        if (encodedArrayItemOffset != 0) {
	        	setFilePosition(encodedArrayItemOffset);
	        	
		        offset = 0;
		        // Read encoded_array_item
		        for (int i = 0; i < encodedArrayItemSize; ++i) {
		        	encodedArrayItemOffsetMap.put(position, i);
		        	encodedArrayItems[i] = getEncodedArray();
		        }
	        }
	        
	        setFilePosition(classDataItemOffset);
	        classDataItems = new ClassDataItem[(int)classDataItemSize];
	        classDataItemOffsetMap = new HashMap<Long, Integer>();
	        classDataItemOffsetMap.put(0L, -1);
	        long sFieldsSize = 0;
	        long iFieldsSize = 0;
	        long dMethodsSize = 0;
	        long vMethodsSize = 0;
	        // Read class_data_item
	        for (int i = 0; i < classDataItemSize; ++i) {
	        	classDataItemOffsetMap.put(position, i);
	        	sFieldsSize = readULEB128();
	        	iFieldsSize = readULEB128();
	        	dMethodsSize = readULEB128();
	        	vMethodsSize = readULEB128();
	        	
	        	EncodedField[] staticFields = null;
	        	EncodedField[] instanceFields = null;
	        	EncodedMethod[] directMethods = null;
	        	EncodedMethod[] virtualMethods = null;
	        	
	        	if (sFieldsSize > 0) {
	        		staticFields = new EncodedField[(int)sFieldsSize];
	        		for (int j = 0; j < sFieldsSize; ++j) {
	        			staticFields[j] = new EncodedField(readULEB128(), readULEB128());
	        		}
	        	}
	        	
	        	if (iFieldsSize > 0) {
	        		instanceFields = new EncodedField[(int)iFieldsSize];
	        		for (int j = 0; j < iFieldsSize; ++j) {
	        			instanceFields[j] = new EncodedField(readULEB128(), readULEB128());
	        		}
	        	}
	        	
	        	if (dMethodsSize > 0) {
	        		directMethods = new EncodedMethod[(int)dMethodsSize];
	        		for (int j = 0; j < dMethodsSize; ++j) {
	        			long diff = readULEB128();
	        			long flags = readULEB128();
	        			long codeOff = readULEB128();
	        			directMethods[j] = new EncodedMethod(diff, flags, codeItemOffsetMap.get(codeOff), codeOff);
	        		}
	        	}
	        	
	        	if (vMethodsSize > 0) {
	        		virtualMethods = new EncodedMethod[(int)vMethodsSize];
	        		for (int j = 0; j < vMethodsSize; ++j) {
	        			long diff = readULEB128();
	        			long flags = readULEB128();
	        			long codeOff = readULEB128();
	        			virtualMethods[j] = new EncodedMethod(diff, flags, codeItemOffsetMap.get(codeOff), codeOff);
	        		}
	        	}
	        	
	        	classDataItems[i] = new ClassDataItem(sFieldsSize, iFieldsSize, dMethodsSize, vMethodsSize,
	        			staticFields, instanceFields, directMethods, virtualMethods);
	        	
	        }
	        overflow = mapOffset - position;
	        
	        setFilePosition(classDefsOffset);
	        classDefItems = new ClassDefItem[(int)classDefsSize];
	        // Read class_def_item
	        for (int i = 0; i < classDefsSize; ++i) {
	        	long classId = read32Bit();
	        	long accessFlags = read32Bit();
	        	long superclassId = read32Bit();
	        	long interfacesOffset = read32Bit();
	        	long sourceFileId = read32Bit();
	        	long annotationsOffset = read32Bit();
	        	long classDataOffset = read32Bit();
	        	long staticValuesOffset = read32Bit();
	        	
	        	classDefItems[i] = new ClassDefItem(classId, accessFlags, superclassId, typeListOffsetMap.get(interfacesOffset),
	        			interfacesOffset, sourceFileId, annotationsDirectoryItemOffsetMap.get(annotationsOffset), annotationsOffset, classDataItemOffsetMap.get(classDataOffset), classDataOffset, encodedArrayItemOffsetMap.get(staticValuesOffset), staticValuesOffset);
	        }
	        
	        
	        
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private EncodedArray getEncodedArray() throws IOException {
		int anSize = readULEB128();
		EncodedValue[] vals = new EncodedValue[anSize];
		for (int k = 0; k < anSize; ++k) {
			vals[k] = getEncodedValue();
		}
		return new EncodedArray(anSize, vals);
	}
	
	private EncodedValue getEncodedValue() throws IOException {
		Byte b = (byte) read8Bit();
		int valueType = b & 0x1F;
		int valueArg = (b & 0xFF) >> 5;

		if (valueType == 0x1C) {
			int anSize = readULEB128();
			EncodedValue[] vals = new EncodedValue[anSize];
			for (int k = 0; k < anSize; ++k) {
				vals[k] = getEncodedValue();
			}
			return new EncodedValue(-1, valueType, valueArg, new EncodedArray(anSize, vals));
		} else if (valueType == 0x1D) {
			return new EncodedValue(-1, valueType, valueArg, getEncodedAnnotation());
		} else if (valueType == 0x1E || valueType == 0x1F) {
			return new EncodedValue(-1, valueType, valueArg);
		}
	
		Byte[] by = new Byte[valueArg + 1];
		for (int k = 0; k <= valueArg; ++k) {
			by[k] = (byte)read8Bit();
		}
		
		return new EncodedValue(-1, valueType, valueArg, by);
	}
	
	private EncodedAnnotation getEncodedAnnotation() throws IOException {
		int type = readULEB128();
    	int size = readULEB128();
    	EncodedAnnotation annotation = new EncodedAnnotation(type, size);
    	
    	for (int j = 0; j < size; ++j) {
    		int name = readULEB128();
    		Byte b = (byte) read8Bit();
    		int valueType = b & 0x1F;
    		int valueArg = (b & 0xFF) >> 5;

    		if (valueArg == 0x1C) {
    			int anSize = readULEB128();
    			EncodedValue[] vals = new EncodedValue[anSize];
    			for (int k = 0; k < anSize; ++k) {
    				vals[k] = getEncodedValue();
    			}
    			annotation.values[j] = new EncodedValue(name, valueType, valueArg, new EncodedArray(anSize, vals));
    			break;
    		} else if (valueArg == 0x1D) {
    			annotation.values[j] = new EncodedValue(name, valueType, valueArg, getEncodedAnnotation());
    			break;
    		} else if (valueArg == 0x1E || valueArg == 0x1F) {
    			annotation.values[j] = new EncodedValue(name, valueType, valueArg);
    		}
    	
    		Byte[] by = new Byte[valueArg + 1];
    		for (int k = 0; k <= valueArg; ++k) {
    			by[k] = (byte)read8Bit();
    		}
    		annotation.values[j] = new EncodedValue(name, valueType, valueArg, by);
    	}
    	return annotation;
	}
	
	public Collection<Byte> write32bit(long data) {
		ArrayList<Byte> output = new ArrayList<Byte>();
		
		for(int i = 0; i < 4; ++i) {
			output.add((byte)((data >> (i*8)) & 0xFF));
		}

		return output;
	}
	
	public Collection<Byte> write16bit(int data) {
		ArrayList<Byte> output = new ArrayList<Byte>();
		
		for(int i = 0; i < 2; ++i) {
			output.add((byte)((data >> (i*8)) & 0xFF));
		}

		return output;
	}
	
	public StringDataItem getStringData() {
		return stringData[stringDataIndex++];
	}
	
	public TypeIdItem getTypeIdData() {
		return typeIds[typeIdsIndex++];
	}
	
	public FieldIdItem getFieldIdData() {
		return fieldIds[fieldIdsIndex++];
	}
	
	public ProtoIdItem getProtoIdData() {
		return protoIds[protoIdsIndex++];
	}
	
	public MethodIdItem getMethodIdData() {
		return methodIds[methodIdsIndex++];
	}
	
	public AnnotationItem getAnnotationItem() {
		return annotationItems[annotationItemsIndex++];
	}
	
	public AnnotationSetItem getAnnotationSetItem() {
		return annotationSetItems[annotationSetItemsIndex++];
	}
	
	public AnnotationSetRefList getAnnotationSetRefList() {
		return annotationSetRefList[annotationSetRefListIndex++];
	}
	
	public TypeList getTypeList() {
		return typeLists[typeListIndex++];
	}
	
	public AnnotationsDirectoryItem getAnnotationsDirectoryItem() {
		return annotationsDirectoryItems[annotationsDirectoryItemsIndex++];
	}
	
	public EncodedArray getEncodedArrayItem() {
		return encodedArrayItems[encodedArrayItemIndex++];
	}
	
	public ClassDataItem getClassDataItem() {
		return classDataItems[classDataItemsIndex++];
	}
	
	public ClassDefItem getClassDefItem() {
		return classDefItems[classDefItemsIndex++];
	}
	
	public DebugInfoItem getDebugInfoItem() {
		return debugInfoItems[debugInfoItemIndex++];
	}
	
	public CodeItem getCodeItem() {
		return codeItems[codeItemIndex++];
	}
	
	public long getFileSize() {
        return fileSize;
    }

    public long getHeaderSize() {
        return headerSize;
    }

    public long getLinkSize() {
        return linkSize;
    }

    public long getLinkOffset() {
        return linkOffset;
    }

    public long getMapOffset() {
        return mapOffset;
    }

    public long getStringIdsSize() {
        return stringIdsSize;
    }

    public long getStringIdsOffset() {
        return stringIdsOffset;
    }

    public long getTypeIdsSize() {
        return typeIdsSize;
    }

    public long getTypeIdsOffset() {
        return typeIdsOffset;
    }

    public long getProtoIdsSize() {
        return protoIdsSize;
    }

    public long getProtoIdsOffset() {
        return protoIdsOffset;
    }

    public long getFieldIdsSize() {
        return fieldIdsSize;
    }

    public long getFieldIdsOffset() {
        return fieldIdsOffset;
    }

    public long getMethodIdsSize() {
        return methodIdsSize;
    }

    public long getMethodIdsOffset() {
        return methodIdsOffset;
    }

    public long getClassDefsSize() {
        return classDefsSize;
    }

    public long getClassDefsOffset() {
        return classDefsOffset;
    }

    public long getDataSize() {
        return dataSize;
    }

    public long getDataOffset() {
        return dataOffset;
    }
    
    public long getAnnotationItemSize() {
        return annotationItemSize;
    }

    public long getAnnotationItemOffset() {
        return annotationItemOffset;
    }
    
    public long getAnnotationSetItemSize() {
        return annotationSetItemSize;
    }

    public long getAnnotationSetItemOffset() {
        return annotationSetItemOffset;
    }
    
    public long getAnnotationSetRefListSize() {
        return annotationSetRefListSize;
    }

    public long getAnnotationSetRefListOffset() {
        return annotationSetRefListOffset;
    }
    
    public long getTypeListSize() {
        return typeListSize;
    }
    
    public long getTypeListOffset() {
        return typeListOffset;
    }
    
    public long getAnnotationsDirectoryItemSize() {
        return annotationsDirectoryItemSize;
    }
    
    public long getAnnotationsDirectoryItemOffset() {
        return annotationsDirectoryItemOffset;
    }
    
    public long getEncodedArrayItemSize() {
        return encodedArrayItemSize;
    }
    
    public long getEncodedArrayItemOffset() {
        return encodedArrayItemOffset;
    }
    
    public long getClassDataItemSize() {
        return classDataItemSize;
    }
    
    public long getClassDataItemOffset() {
        return classDataItemOffset;
    }
    
    public long getDebugInfoItemSize() {
        return debugInfoItemSize;
    }
    
    public long getDebugInfoItemOffset() {
        return debugInfoItemOffset;
    }
    
    public long getCodeItemSize() {
        return codeItemSize;
    }
    
    public long getCodeItemOffset() {
        return codeItemOffset;
    }
    
    public long getStringDataItemSize() {
        return stringDataItemSize;
    }
    
    public long getStringDataItemOffset() {
        return stringDataItemOffset;
    }
    
    public long getOverflow() {
        return overflow;
    }
    
    public byte[] getHeader() {
    	return header;
    }
    
    public Collection<Byte> getMapList() {
    	return mapList;
    }
    
}

package patch;

import item.AnnotationItem;
import item.AnnotationSetItem;
import item.AnnotationSetRefList;
import item.AnnotationsDirectoryItem;
import item.ByteCode;
import item.ClassDataItem;
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
import item.TryItem;
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
    
    public String[] stringData;
    int stringDataIndex = 0;
    
    public int[] typeIds;
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
    
    public ClassDataItem[] classDataItems;
    int classDataItemsIndex = 0;
    
    public HashMap<Long, Integer> debugInfoOffsetMap;
    public DebugInfoItem[] debugInfoItems;
    int debugInfoItemIndex = 0;
    
    HashMap<Long, Integer> codeItemOffsetMap;
    public CodeItem[] codeItems;
    int codeItemIndex = 0;
    
    OutputStream outStream = null;
    
    private void doWrite(String data) {
    	byte[] b = new byte[data.length()];
		data.getBytes(0, data.length(), b, 0);
    	try {
			outStream.write(b);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

	@Override
	public void parse() {
		try {
			outStream = new FileOutputStream("out/log.log");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
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
	        
	        typeIds = new int[(int)typeIdsSize];
	        // Read type_ids
	        for(int i = 0 ; i < typeIdsSize; ++i) {
	        	typeIds[i] = (int) read32Bit();
	        }
	        
	        stringData = new String[(int)stringIdsSize];
	        // Read string_data_items
	        for(int i = 0 ; i < stringIdsSize; ++i) {
	        	setFilePosition(stringsPos[i]);
	        	stringData[i] = readString();
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
	        // Read offsets
	        int mapSize = (int)read32Bit();
	        for (int i = 0; i < mapSize; ++i) {
	        	int type = read16Bit();
	        	read16Bit();
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
		        		typeListSize = read32Bit();
		        		typeListOffset = read32Bit();
		        		break;
		        	case 4098:
		        		annotationSetRefListSize = read32Bit();
		        		annotationSetRefListOffset = read32Bit();
		        		break;
		        	case 4099:
		        		annotationSetItemSize = read32Bit();
		        		annotationSetItemOffset = read32Bit();
		        		break;
		        	case 8192:
		        		classDataItemSize = read32Bit();
		        		classDataItemOffset = read32Bit();
		        		break;
		        	case 8193:
		        		codeItemSize = read32Bit();
		        		codeItemOffset = read32Bit();
		        		break;
		        	case 8194:
		        		stringDataItemSize = read32Bit();
		        		stringDataItemOffset = read32Bit();
		        		break;
		        	case 8195:
		        		debugInfoItemSize = read32Bit();
		        		debugInfoItemOffset = read32Bit();
		        		break;
		        	case 8196:
		        		annotationItemSize = read32Bit();
		        		annotationItemOffset = read32Bit();
		        		break;
		        	case 8197:
		        		encodedArrayItemSize = read32Bit();
		        		encodedArrayItemOffset = read32Bit();
		        		break;
		        	case 8198:
		        		annotationsDirectoryItemSize = read32Bit();
		        		annotationsDirectoryItemOffset = read32Bit();
		        		break;
		        	default:
		        		read32Bit();
		        		read32Bit();
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
	        		doWrite(data[j] + ": " + stringData[typeIds[data[j]]] + "\n");
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
	        			doWrite(stringData[(int)names[j]] + "\n");
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
	        	if(i == 775)
	        		System.out.print("hi");
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
	        	
	        	System.out.print("hi");
	        	Collection<DecodedInstruction> decodedInstructions =
	                DecodedInstruction.decodeAll(insns);
	        	
	        	
	        	/// Alt implementation ///
	        	/*
	        	long insCount = 0;
	        	ArrayList<ByteCode> code = new ArrayList<ByteCode>();
	        	while (insCount < 2 * insnsSize) {
	        		int op = read8Bit();
	        		++insCount;
	        		ArrayList<Byte> args = new ArrayList<Byte>();
	        		args.add((byte)op);
	        		long buff;
	        		if (op == 0x00) {
	        			System.out.println("nop");
	        			//args.add((byte)read8Bit());
	        			//++insCount;
	        		} else if (op == 0x01) {
	        			args.add((byte)read8Bit());
	        			++insCount;
	        		} else if (op == 0x02) {
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			insCount += 3;
	        		} else if (op == 0x03) { //?
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			insCount += 5;
	        		} else if (op == 0x04) {
	        			args.add((byte)read8Bit());
	        			++insCount;
	        		} else if (op == 0x05) {
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			insCount += 3;
	        		} else if (op == 0x06) { //?
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			insCount += 5;
	        		} else if (op == 0x07) {
	        			args.add((byte)read8Bit());
	        			++insCount;
	        		} else if (op == 0x08) {
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			insCount += 3;
	        		} else if (op == 0x09) { //?
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			insCount += 5;
	        		} else if (op == 0x0A) {
	        			args.add((byte)read8Bit());
	        			++insCount;
	        		} else if (op == 0x0B) {
	        			args.add((byte)read8Bit());
	        			++insCount;
	        		} else if (op == 0x0C) {
	        			args.add((byte)read8Bit());
	        			++insCount;
	        		} else if (op == 0x0D) {
	        			args.add((byte)read8Bit());
	        			++insCount;
	        		} else if (op == 0x0E) {
	        			System.out.println("void");
	        			//args.add((byte)read8Bit());
	        			//++insCount;
	        		} else if (op == 0x0F) {
	        			args.add((byte)read8Bit());
	        			++insCount;
	        		} else if (op == 0x10) {
	        			args.add((byte)read8Bit());
	        			++insCount;
	        		} else if (op == 0x11) {
	        			args.add((byte)read8Bit());
	        			++insCount;
	        		} else if (op == 0x12) {
	        			args.add((byte)read8Bit());
	        			++insCount;
	        		} else if (op == 0x13) {
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			insCount += 3;
	        		} else if (op == 0x14) {
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			insCount += 5;
	        		} else if (op == 0x15) {
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			insCount += 3;
	        		} else if (op == 0x16) {
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			insCount += 3;
	        		} else if (op == 0x17) {
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			insCount += 5;
	        		} else if (op == 0x18) {
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			insCount += 9;
	        		} else if (op == 0x19) {
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			insCount += 3;
	        		} else if (op == 0x1A) {
	        			args.add((byte)read8Bit());
	        			// string_index
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			insCount += 3;
	        		} else if (op == 0x1B) {
	        			args.add((byte)read8Bit());
	        			// string_index
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			insCount += 5;
	        		} else if (op == 0x1C) {
	        			args.add((byte)read8Bit());
	        			// type_index
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			insCount += 3;
	        		} else if (op == 0x1D) {
	        			args.add((byte)read8Bit());
	        			++insCount;
	        		} else if (op == 0x1E) {
	        			args.add((byte)read8Bit());
	        			++insCount;
	        		} else if (op == 0x1F) {
	        			args.add((byte)read8Bit());
	        			// type_index
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			insCount += 3;
	        		} else if (op == 0x20) { //
	        			args.add((byte)read8Bit());
	        			// type_index
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			insCount += 3;
	        		} else if (op == 0x21) {
	        			args.add((byte)read8Bit());
	        			++insCount;
	        		} else if (op == 0x22) { //??????
	        			args.add((byte)read8Bit());
	        			// type_index
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			insCount += 3;
	        		} else if (op == 0x23) { //??????
	        			args.add((byte)read8Bit());
	        			//type_index
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			insCount += 3;
	        		} else if (op == 0x24) { //??????
	        			int b = read8Bit();
	        			args.add((byte)b);
	        			int regno = (b & 0xF0) >> 4;
	        			for (int j = 0 ; j < (1+regno)/2 ; ++j) {
	        				args.add((byte)read8Bit());
	        				++insCount;
	        			}
	        			if ((1+regno)/2 % 2 != 0) {
	        				args.add((byte)read8Bit());
	        				++insCount;
	        			}
	        			// type_index
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			insCount += 3;
	        		} else if (op == 0x25) {
	        			args.add((byte)read8Bit());
	        			// type_index
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			insCount += 5;
	        		} else if (op == 0x26) { //TODO test this further
	        			args.add((byte)read8Bit());
	        			// Offset
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			insCount += 3;
	        		} else if (op == 0x27) {
	        			args.add((byte)read8Bit());
	        			++insCount;
	        		} else if (op == 0x28) {
	        			args.add((byte)read8Bit());
	        			++insCount;
	        		} else if (op == 0x29) { // Padded
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			insCount += 3;
	        		} else if (op == 0x2A) { //?
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			insCount += 5;
	        		} else if (op == 0x2B) {
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			insCount += 5;
	        		} else if (op == 0x2C) {
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			insCount += 5;
	        		} else if (op >= 0x2D && op <= 0x31) {
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			insCount += 3;
	        		} else if (op >= 0x32 && op <= 0x37) {
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			insCount += 3;
	        		} else if (op >= 0x38 && op <= 0x3D) {
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			insCount += 3;
	        		} else if (op >= 0x3E && op <= 0x43) {
	        			System.out.println(":(");
	        			args.add((byte)read8Bit());
	        			insCount += 1;
	        		} else if (op >= 0x44 && op <= 0x51) {
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			insCount += 3;
	        		} else if (op >= 0x52 && op <= 0x5F) {
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			insCount += 3;
	        		} else if (op >= 0x60 && op <= 0x6D) {
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			insCount += 3;
	        		} else if (op >= 0x6E && op <= 0x72) {
	        			int b = read8Bit();
	        			args.add((byte)b);
	        			
	        			// method_index
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			
	        			insCount += 3;
	        			
	        			int regno = (b & 0xF0) >> 4;
	        			for (int j = 0 ; j < (1+regno)/2 ; ++j) {
	        				args.add((byte)read8Bit());
	        				++insCount;
	        			}
	        			if ((1+regno)/2 % 2 != 0) {
	        				args.add((byte)read8Bit());
	        				++insCount;
	        			}
	        		} else if (op == 0x73) {
	        			System.out.println(":(");
	        			args.add((byte)read8Bit());
	        			insCount += 1;
	        		} else if (op >= 0x74 && op <= 0x78) {
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			insCount += 5;
	        		} else if (op >= 0x79 && op <= 0x7A) {
	        			System.out.println(":(");
	        			args.add((byte)read8Bit());
	        			insCount += 1;
	        		} else if (op >= 0x7B && op <= 0x8F) {
	        			args.add((byte)read8Bit());
	        			++insCount;
	        		} else if (op >= 0x90 && op <= 0xAF) {
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			insCount += 3;
	        		} else if (op >= 0xB0 && op <= 0xCF) {
	        			args.add((byte)read8Bit());
	        			++insCount;
	        		} else if (op >= 0xD0 && op <= 0xD7) {
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			insCount += 3;
	        		} else if (op >= 0xD8 && op <= 0xE2) {
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			insCount += 3;
	        		} else if (op == 0xE3) {
	        			args.add((byte)read8Bit());
	        			// field_index
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			insCount += 3;
	        		} else if (op == 0xE4) {
	        			args.add((byte)read8Bit());
	        			// field_index
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			insCount += 3;
	        		} else if (op == 0xE5) {
	        			args.add((byte)read8Bit());
	        			// field_index
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			insCount += 3;
	        		} else if (op == 0xE6) {
	        			args.add((byte)read8Bit());
	        			// field_index
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			insCount += 3;
	        		} else if (op == 0xE7) {
	        			args.add((byte)read8Bit());
	        			// field_index
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			insCount += 3;
	        		} else if (op == 0xE8) {
	        			args.add((byte)read8Bit());
	        			// field_index
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			insCount += 3;
	        		} else if (op == 0xE9) {
	        			args.add((byte)read8Bit());
	        			// field_index
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			insCount += 3;
	        		} else if (op == 0xEA) {
	        			args.add((byte)read8Bit());
	        			// field_index
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			insCount += 3;
	        		} else if (op == 0xEB) {
	        			args.add((byte)read8Bit());
	        			// field_index
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			insCount += 3;
	        		} else if (op == 0xEC) {
	        			System.out.println(":(");
	        		} else if (op == 0xED) {
	        			System.out.println(":( ED");
	        		} else if (op == 0xEE) {
	        			System.out.println(":( EE");
	        		} else if (op == 0xEF) {
	        			System.out.println(":( EF");
	        		} else if (op == 0xF0) {
	        			System.out.println(":( F0");
	        			int b = read8Bit();
	        			args.add((byte)b);
	        			
	        			// method_index
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			
	        			insCount += 3;
	        			
	        			int regno = (b & 0xF0) >> 4;
	        			for (int j = 0 ; j < (1+regno)/2 ; ++j) {
	        				args.add((byte)read8Bit());
	        				++insCount;
	        			}
	        			if ((1+regno)/2 % 2 != 0) {
	        				args.add((byte)read8Bit());
	        				++insCount;
	        			}
	        		} else if (op == 0xF1) {
	        			System.out.println(":( F1");
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        		} else if (op == 0xF2) {
	        			System.out.println(":( F2");
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        		} else if (op == 0xF3) {
	        			System.out.println(":( F3");
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        		} else if (op == 0xF4) {
	        			System.out.println(":( F4");
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        		} else if (op == 0xF5) {
	        			System.out.println(":( F5");
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        		} else if (op == 0xF6) {
	        			System.out.println(":( F6");
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        		} else if (op == 0xF7) {
	        			System.out.println(":( F7");
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        			args.add((byte)read8Bit());
	        		} else if (op == 0xF8) {
	        			System.out.println(":( F8");
	        		} else if (op == 0xF9) {
	        			System.out.println(":( F9");
	        		} else if (op == 0xFA) {
	        			System.out.println(":( FA");
	        		} else if (op == 0xFB) {
	        			System.out.println(":( FB");
	        		} else if (op == 0xFC) {
	        			System.out.println(":( FC");
	        		} else if (op == 0xFD) {
	        			System.out.println(":( FD");
	        		} else if (op == 0xFE) {
	        			System.out.println(":( FE");
	        		} else if (op == 0xFF) {
	        			System.out.println(":( FF");
	        			int nextOp = read8Bit();
	        			args.add((byte)nextOp);
		        		++insCount;
		        		if (nextOp == 0x00) {
		        			System.out.println(":( FF00");
		        		} else if (nextOp == 0x01) {
		        			System.out.println(":( FF01");
		        		} else if (nextOp == 0x02) {
		        			System.out.println(":( FF02");
		        		} else if (nextOp == 0x03) {
		        			System.out.println(":( FF03");
		        		} else if (nextOp == 0x04) {
		        			System.out.println(":( FF04");
		        		} else if (nextOp == 0x05) {
		        			System.out.println(":( FF05");
		        		} else if (nextOp == 0x06) {
		        			System.out.println(":( FF06");
		        		} else if (nextOp == 0x07) {
		        			System.out.println(":( FF07");
		        		} else if (nextOp == 0x08) {
		        			System.out.println(":( FF08");
		        		} else if (nextOp == 0x09) {
		        			System.out.println(":( FF09");
		        		} else if (nextOp == 0x0A) {
		        			System.out.println(":( FF0A");
		        		} else if (nextOp == 0x0B) {
		        			System.out.println(":( FF0B");
		        		} else if (nextOp == 0x0C) {
		        			System.out.println(":( FF0C");
		        		} else if (nextOp == 0x0D) {
		        			System.out.println(":( FF0D");
		        		} else if (nextOp == 0x0E) {
		        			System.out.println(":( FF0E");
		        		} else if (nextOp == 0x0F) {
		        			System.out.println(":( FF0F");
		        		} else if (nextOp == 0x10) {
		        			System.out.println(":( FF10");
		        		} else if (nextOp == 0x11) {
		        			System.out.println(":( FF11");
		        		} else if (nextOp == 0x12) {
		        			System.out.println(":( FF12");
		        		} else if (nextOp == 0x13) {
		        			System.out.println(":( FF13");
		        		} else if (nextOp == 0x14) {
		        			System.out.println(":( FF14");
		        		} else if (nextOp == 0x15) {
		        			System.out.println(":( FF15");
		        		} else if (nextOp == 0x16) {
		        			System.out.println(":( FF16");
		        		} else if (nextOp == 0x17) {
		        			System.out.println(":( FF17");
		        		} else if (nextOp == 0x18) {
		        			System.out.println(":( FF18");
		        		} else if (nextOp == 0x19) {
		        			System.out.println(":( FF19");
		        		} else if (nextOp == 0x1A) {
		        			System.out.println(":( FF1A");
		        		} else if (nextOp == 0x1B) {
		        			System.out.println(":( FF1B");
		        		} else if (nextOp == 0x1C) {
		        			System.out.println(":( FF1C");
		        		} else if (nextOp == 0x1D) {
		        			System.out.println(":( FF1D");
		        		} else if (nextOp == 0x1E) {
		        			System.out.println(":( FF1E");
		        		} else if (nextOp == 0x1F) {
		        			System.out.println(":( FF1F");
		        		} else if (nextOp == 0x20) {
		        			System.out.println(":( FF20");
		        		} else if (nextOp == 0x21) {
		        			System.out.println(":( FF21");
		        		} else if (nextOp == 0x22) {
		        			System.out.println(":( FF22");
		        		} else if (nextOp == 0x23) {
		        			System.out.println(":( FF23");
		        		} else if (nextOp == 0x24) {
		        			System.out.println(":( FF24");
		        		} else if (nextOp == 0x25) {
		        			System.out.println(":( FF25");
		        		} else if (nextOp == 0x26) {
		        			System.out.println(":( FF26");
		        		} else if (nextOp == 0xF2) {
		        			System.out.println(":( FFF2");
		        		} else if (nextOp == 0xF3) {
		        			System.out.println(":( FFF3");
		        		} else if (nextOp == 0xF4) {
		        			System.out.println(":( FFF4");
		        		} else if (nextOp == 0xF5) {
		        			System.out.println(":( FFF5");
		        		} else if (nextOp == 0xF6) {
		        			System.out.println(":( FFF6");
		        		} else if (nextOp == 0xF7) {
		        			System.out.println(":( FFF7");
		        		} else if (nextOp == 0xF8) {
		        			System.out.println(":( FFF8");
		        		} else if (nextOp == 0xF9) {
		        			System.out.println(":( FFF9");
		        		} else if (nextOp == 0xFA) {
		        			System.out.println(":( FFFA");
		        		} else if (nextOp == 0xFB) {
		        			System.out.println(":( FFFB");
		        		} else if (nextOp == 0xFC) {
		        			System.out.println(":( FFFC");
		        		} else if (nextOp == 0xFD) {
		        			System.out.println(":( FFFD");
		        		} else if (nextOp == 0xFE) {
		        			System.out.println(":( FFFE");
		        		}
	        		}
	        		
	        		code.add(new ByteCode(op, args));
	        		
	        	}
	        	*/
	        	//////////////////////////
	        	
	        	
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
	        		if (name == -1) {
	        			System.out.print("oo");
	        		}
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
	        
	        setFilePosition(classDataItemOffset);
	        classDataItems = new ClassDataItem[(int)classDataItemSize];
	        long sFieldsSize = 0;
	        long iFieldsSize = 0;
	        long dMethodsSize = 0;
	        long vMethodsSize = 0;
	        // Read class_data_item
	        for (int i = 0; i < classDataItemSize; ++i) {
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
	        
	        
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private EncodedValue getEncodedValue() throws IOException {
		Byte b = (byte) read8Bit();
		int valueType = b & 0x1F;
		int valueArg = (b & 0xFF) >> 5;

		if (valueArg == 0x1C) {
			int anSize = readULEB128();
			EncodedValue[] vals = new EncodedValue[anSize];
			for (int k = 0; k < anSize; ++k) {
				vals[k] = getEncodedValue();
			}
			return new EncodedValue(-1, valueType, valueArg, new EncodedArray(anSize, vals));
		} else if (valueArg == 0x1D) {
			return new EncodedValue(-1, valueType, valueArg, getEncodedAnnotation());
		} else if (valueArg == 0x1E || valueArg == 0x1F) {
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
	
	public String getStringData() {
		return stringData[stringDataIndex++];
	}
	
	public int getTypeIdData() {
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
	
	public ClassDataItem getClassDataItem() {
		return classDataItems[classDataItemsIndex++];
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
    
}

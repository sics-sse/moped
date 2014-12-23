/*
 * Copyright 2004-2008 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * only, as published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included in the LICENSE file that accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 16 Network Circle, Menlo
 * Park, CA 94025 or visit www.sun.com if you need additional
 * information or have any questions.
 */

/**
 * This file contains functions for loading the bootstrap suite from a file and relocating the
 * contained object memory. The structure of an object memory file is described in the javadoc
 * comment of the java.lang.ObjectMemory class.
 */

// Set to true to enable tracing of the bootstrap suite file as it's read
#define TRACE_SUITE false

#ifdef FLASH_MEMORY

// the next definition needs to be kept in sync with suite converter
#define NUMBER_OF_BYTES_IN_BYTECODE_HEADER (3 * sizeof(UWord))

UWord loadBootstrapSuiteFromFlash(
		char *bootstrapSuiteFile,
		Address *romStart,
		Address *suite,
		int *hash) {
	// ROM starts at the flash address set on command line
	Address javabytecodesbase = (Address) atoi(bootstrapSuiteFile);
	*suite = (void *)(getUWord(javabytecodesbase, 0) + javabytecodesbase);
	*hash = (int)getUWord(javabytecodesbase, 1);
	UWord size=getUWord(javabytecodesbase, 2);
	*romStart=(void *)(javabytecodesbase + NUMBER_OF_BYTES_IN_BYTECODE_HEADER);

	diagnosticWithValue("javabytecodesbase: ", (int) javabytecodesbase);
	diagnosticWithValue("rootOffset: ", (int) *suite);
	diagnosticWithValue("hash: ", getUWord(javabytecodesbase, 0));
	diagnosticWithValue("suite size: ", size);
	diagnosticWithValue("romStart: ", (int) *romStart);
	return size;
}

#else /* FLASH_MEMORY */

/**
 * Wrapper for a file input stream that provides a subset of the functionality
 * of the java.io.DataInputStream class.
 */
typedef struct {
    ByteAddress in;  // the byte array
    UWord size;      // the size of the byte array
    UWord pos;       // the current read position
} DataInputStream;

void new_DataInputStream_open(DataInputStream *dis, unsigned char * fileArray, unsigned int size) {
	(*dis).size = size;
	if ((*dis).size == -1) {
//			fprintf(stderr, "No such file '%s'\n", fileArray);
			stopVM(-1);
	}

	//(*dis).in = newBuffer((*dis).size, "DataInputStream_open", true);
	//strcpy(fileArray, (*dis).in);
	//strncpy((*dis).in, fileArray, size);
	(*dis).in = (ByteAddress)fileArray;
	//snprintf((*dis).in, size, "%s", fileArray);
	(*dis).pos = 0;
}
/**
 * Initializes a DataInputStream.
 *
 * @param  dis   the DataInputStream to initialize
 * @param  file  the name of the file to initialize the stream from
 */
void DataInputStream_open(DataInputStream *dis, const char *file) {
	(*dis).size = getFileSize(file);
	if ((*dis).size == -1) {
		fprintf(stderr, "No such file '%s'\n", file);
		stopVM(-1);
	}

	// Note: the default way to allocate memory written by zeni
	(*dis).in = newBuffer((*dis).size, "DataInputStream_open", true);

	// Note: modified way to allocate memory written by zeni
	//(*dis).in = memget(sizeof(char)*(*dis).size);

	if (readFile(file, (*dis).in, (*dis).size) != (*dis).size) {
		printf("Error reading '%s'\n", file);
		stopVM(-1);
	}
	(*dis).pos = 0;
}

/**
 * Returns the number of bytes that can still be read from a DataInputStream stream.
 *
 * @param  dis   the DataInputStream to query
 * @return the number of available bytes
 */
unsigned int DataInputStream_available(DataInputStream *dis) {
	return (*dis).size - (*dis).pos;
}

/**
 * Returns the number of bytes that have been read from a DataInputStream stream.
 *
 * @param  dis   the DataInputStream to query
 * @return the number of bytes that have been read
 */
unsigned int DataInputStream_readSoFar(DataInputStream *dis) {
	return (*dis).pos;
}

/**
 * Reads an unsigned byte from a DataInputStream.
 *
 * @param  dis    the DataInputStream to read from
 * @param  prefix the prefix used when tracing this read
 * @return the value read
 */
UWord DataInputStream_readUnsignedByte(DataInputStream *dis, const char *prefix) {
	UWord value;

	if ((*dis).pos >= (*dis).size) {
		fatalVMError("EOFException");
	}
	value = (*dis).in[(*dis).pos++];
#if TRACE_SUITE
	if (prefix != null) {
		fprintf(stderr, "%s:%u\n", prefix, (value & 0xFF));
	}
#endif /* TRACE_SUITE */
	return value & 0xFF;
}

/**
 * Reads an unsigned short from a DataInputStream.
 *
 * @param  dis    the DataInputStream to read from
 * @param  prefix the prefix used when tracing this read
 * @return the value read
 */
UWord DataInputStream_readUnsignedShort(DataInputStream *dis,
		const char *prefix) {
	int b1 = DataInputStream_readUnsignedByte(dis, null);
	int b2 = DataInputStream_readUnsignedByte(dis, null);
	UWord value = ((b1 << 8) + (b2 << 0)) & 0xFFFF;
#if TRACE_SUITE
	fprintf(stderr, "%s:%u\n", prefix, value);
#endif /* TRACE_SUITE */
	return value;
}

/**
 * Reads an int from a DataInputStream.
 *
 * @param  dis    the DataInputStream to read from
 * @param  prefix the prefix used when tracing this read
 * @return the value read
 */
int DataInputStream_readInt(DataInputStream *dis, const char *prefix) {
	int b1 = DataInputStream_readUnsignedByte(dis, null);
	int b2 = DataInputStream_readUnsignedByte(dis, null);
	int b3 = DataInputStream_readUnsignedByte(dis, null);
	int b4 = DataInputStream_readUnsignedByte(dis, null);
	UWord value = ((b1 << 24) + (b2 << 16) + (b3 << 8) + (b4 << 0));
#if TRACE_SUITE
	fprintf(stderr, "%s:%u\n", prefix, value);
#endif /* TRACE_SUITE */
	return value;
}

/**
 * Reads some bytes from a DataInputStream into a buffer.
 *
 * @param  dis    the DataInputStream to read from
 * @param  buf    the buffer to store into
 * @param  size   the size of the buffer which also specifies the number of bytes to read
 * @param  prefix the prefix used when tracing this read
 * @return the value read
 */
void DataInputStream_readFully(DataInputStream *dis, ByteAddress buf,
		UWord size, const char *prefix) {
	unsigned int i;
	for (i = 0; i != size; ++i) {
//	while (i != size) {
		buf[i] = DataInputStream_readUnsignedByte(dis, null);
	}
#if TRACE_SUITE
	fprintf(stderr, format("%s:{read %A bytes}\n"), prefix, size);
#endif /* TRACE_SUITE */
}

/**
 * Skips a number of bytes in a DataInputStream.
 *
 * @param  dis    the DataInputStream to operate on
 * @param  n      the number of bytes to skip
 * @param  prefix the prefix used when tracing this read
 */
void DataInputStream_skip(DataInputStream *dis, UWord n, const char* prefix) {
	unsigned int i = 0;
	while (i++ != n) {
		DataInputStream_readUnsignedByte(dis, null);
	}
#if TRACE_SUITE
	fprintf(stderr, format("%s:{skipped %A bytes}\n"), prefix, n);
#endif /* TRACE_SUITE */
}

/**
 * Closes a DataInputStream.
 *
 * @param dis  the DataInputStream to close
 */
void DataInputStream_close(DataInputStream *dis) {
	if ((*dis).in != null) {
		// written by zeni
		freeBuffer((*dis).in);
		(*dis).in = null;
	}
}

void new_DataInputStream_close(DataInputStream *dis) {
	if ((*dis).in != null) {
		// written by zeni
		//freeBuffer((*dis).in);
		(*dis).in = null;
	}
}

// new loadBootstrapSuite
UWord new_loadBootstrapSuite(unsigned char * fileArray, unsigned int fileSize, Address buffer, UWord size,
		Address *suite, int *hash) {
	DataInputStream dis;
	UWord suiteOffset;
	ByteAddress oopMap;
	UWord oopMapLength;
	unsigned int i;
	int attributes;
	boolean hasTypemap;
	int pad/*, fileSize*/;
	if (fileSize == -1) {
		fprintf(stderr, "Bootstrap suite file '%s' not found\n", fileArray);
		fatalVMError("");
	} else if (size < fileSize) {
		fatalVMError("buffer size is too small for bootstrap suite");
	}
	/*
	 * Try to open the suite file.
	 */
	new_DataInputStream_open(&dis, fileArray, fileSize);
	/*
	 * Read 'magic'
	 */
	if (DataInputStream_readInt(&dis, "magic") != 0xdeadbeef) {
		fatalVMError("bad magic in bootstrap suite");
	}

	/*
	 * Read (and ignore for now) version identifiers
	 */
	DataInputStream_readUnsignedShort(&dis, "minor_version");
	DataInputStream_readUnsignedShort(&dis, "major_version");

	/*
	 * Read 'attributes'
	 */
	attributes = DataInputStream_readInt(&dis, "attributes");


	hasTypemap = ((attributes
			& com_sun_squawk_ObjectMemoryFile_ATTRIBUTE_TYPEMAP) != 0);
	if (((attributes & com_sun_squawk_ObjectMemoryFile_ATTRIBUTE_32BIT) != 0)
			== SQUAWK_64) {
		fatalVMError("word size in bootstrap suite is incorrect");
	}
	if (((attributes & com_sun_squawk_ObjectMemoryFile_ATTRIBUTE_BIGENDIAN) != 0)
			!= PLATFORM_BIG_ENDIAN) {
		fatalVMError("endianess in bootstrap suite is incorrect");
	}
	/*
	 * Read and ignore 'parent_hash'
	 */
	DataInputStream_readInt(&dis, "parent_hash");
	/*
	 * Read the length of 'parent_url' and verify that it is 0
	 */
	if (DataInputStream_readUnsignedShort(&dis, "parent_url") != 0) {
		fatalVMError("bootstrap suite should have no parent");
	}
	/*
	 * Read 'root'
	 */
	suiteOffset = DataInputStream_readInt(&dis, "root");
	/*
	 * Read 'size'
	 */
	size = DataInputStream_readInt(&dis, "size");
	/*
	 * Read 'oopmap'
	 */
	oopMapLength = ((size / HDR_BYTES_PER_WORD) + 7) / 8;
	oopMap = (ByteAddress) buffer + size;
//	oopMap = dis.in + dis.pos;
//	dis.pos += oopMapLength;
	DataInputStream_readFully(&dis, oopMap, oopMapLength, "oopmap");
	/*
	 * Skip the padding for 'memory' to be word aligned
	 */
	pad = roundUpToWord(DataInputStream_readSoFar(&dis))
			- DataInputStream_readSoFar(&dis);
	DataInputStream_skip(&dis, pad, "padding");
//	dis.pos += pad;
	/*
	 * Read 'memory'
	 */
	DataInputStream_readFully(&dis, buffer, size, "memory");
//	buffer = dis.in + dis.pos;
//	dis.pos += size;
	/*
	 * Calculate the hash of the object memory while it is still in canonical form.
	 */
	*hash = size;
	for (i = 0; i != size; ++i) {
		setType((ByteAddress) buffer + i, AddressType_BYTE, 1);
		*hash += getByte(buffer, i);
	}

	/*
	 * Relocate all the pointers in the object memory.
	 */
#if TRACE_SUITE
	{
		FILE *pointers = fopen("squawk.suite.pointers", "w");
		int cardinality = 0;
#endif /* TRACE_SUITE */

	for (i = 0; i != oopMapLength; ++i) {
		int oopMapByte = oopMap[i];
		int bit;

		for (bit = 0; bit != 8; ++bit) {
			if ((oopMapByte & (1 << bit)) != 0) {
				int offset = (i * 8) + bit;
				UWord pointer;
				setType((UWordAddress) buffer + offset, AddressType_UWORD,
						HDR_BYTES_PER_WORD);
				pointer = getUWord(buffer, offset);
				if (pointer != 0) {
					setUWord(buffer, offset, pointer + (UWord) buffer);
				}
#if TRACE_SUITE
				++cardinality;
				fprintf(pointers, format("%A: %A -> %A\n"), offset*HDR_BYTES_PER_WORD, pointer, pointer + (UWord)buffer);
#endif /* TRACE_SUITE */
			}
		}
	}
#if TRACE_SUITE
	fprintf(stderr, "oopmap:{cardinality = %d}\n", cardinality);
	fclose(pointers);
}
#endif /* TRACE_SUITE */
	/*
	 * Initialize the suite pointer.
	 */
	*suite = (ByteAddress) buffer + suiteOffset;

#if TYPEMAP
	if (hasTypemap) {
		ByteAddress p = buffer;
		int i;

		for (i = 0; i != size; ++i) {
			char type = DataInputStream_readUnsignedByte(&dis, "type");
			setType(p, type, 1);
			p++;
		}
	}
#endif /* TYPEMAP */
	/*
	 * Close the data input stream.
	 */
	new_DataInputStream_close(&dis);
	return size;
}
/**
 * Loads the bootstrap suite from a file into a given buffer.
 *
 * @param file    the name of the file to load from
 * @param buffer  the buffer into which the object memory is to be loaded
 * @param size    the size of the buffer. This must at least equal to the size of the file
 * @param suite   OUT: the pointer to the suite
 * @param hash    OUT: the hash of the object memory in canonical form
 * @return the size of the object memory
 */
UWord loadBootstrapSuite(const char *file, Address buffer, UWord size,
		Address *suite, int *hash) {
	DataInputStream dis;
	UWord suiteOffset;
	ByteAddress oopMap;
	UWord oopMapLength;
	unsigned int i;
	int attributes;
	boolean hasTypemap;
	int pad, fileSize;

	fileSize = getFileSize(file);
	if (fileSize == -1) {
		fprintf(stderr, "Bootstrap suite file '%s' not found\n", file);
		fatalVMError("");
	} else if (size < fileSize) {
		fatalVMError("buffer size is too small for bootstrap suite");
	}
	/*
	 * Try to open the suite file.
	 */
	DataInputStream_open(&dis, file);

	/*
	 * Read 'magic'
	 */
	if (DataInputStream_readInt(&dis, "magic") != 0xdeadbeef) {
		fatalVMError("bad magic in bootstrap suite");
	}

	/*
	 * Read (and ignore for now) version identifiers
	 */
	DataInputStream_readUnsignedShort(&dis, "minor_version");
	DataInputStream_readUnsignedShort(&dis, "major_version");

	/*
	 * Read 'attributes'
	 */
	attributes = DataInputStream_readInt(&dis, "attributes");
	hasTypemap = ((attributes
			& com_sun_squawk_ObjectMemoryFile_ATTRIBUTE_TYPEMAP) != 0);
	if (((attributes & com_sun_squawk_ObjectMemoryFile_ATTRIBUTE_32BIT) != 0)
			== SQUAWK_64) {
		fatalVMError("word size in bootstrap suite is incorrect");
	}
	if (((attributes & com_sun_squawk_ObjectMemoryFile_ATTRIBUTE_BIGENDIAN) != 0)
			!= PLATFORM_BIG_ENDIAN) {
		fatalVMError("endianess in bootstrap suite is incorrect");
	}
	/*
	 * Read and ignore 'parent_hash'
	 */
	DataInputStream_readInt(&dis, "parent_hash");
	/*
	 * Read the length of 'parent_url' and verify that it is 0
	 */
	if (DataInputStream_readUnsignedShort(&dis, "parent_url") != 0) {
		fatalVMError("bootstrap suite should have no parent");
	}
	/*
	 * Read 'root'
	 */
	suiteOffset = DataInputStream_readInt(&dis, "root");
	/*
	 * Read 'size'
	 */
	size = DataInputStream_readInt(&dis, "size");
	/*
	 * Read 'oopmap'
	 */
	oopMapLength = ((size / HDR_BYTES_PER_WORD) + 7) / 8;
	oopMap = (ByteAddress) buffer + size;

	DataInputStream_readFully(&dis, oopMap, oopMapLength, "oopmap");
	/*
	 * Skip the padding for 'memory' to be word aligned
	 */
	pad = roundUpToWord(DataInputStream_readSoFar(&dis))
			- DataInputStream_readSoFar(&dis);
	DataInputStream_skip(&dis, pad, "padding");
	/*
	 * Read 'memory'
	 */
	DataInputStream_readFully(&dis, buffer, size, "memory");
	/*
	 * Calculate the hash of the object memory while it is still in canonical form.
	 */
	*hash = size;
	for (i = 0; i != size; ++i) {
		setType((ByteAddress) buffer + i, AddressType_BYTE, 1);
		*hash += getByte(buffer, i);
	}

	/*
	 * Relocate all the pointers in the object memory.
	 */
#if TRACE_SUITE
	{
		FILE *pointers = fopen("squawk.suite.pointers", "w");
		int cardinality = 0;
#endif /* TRACE_SUITE */

	for (i = 0; i != oopMapLength; ++i) {
		int oopMapByte = oopMap[i];
		int bit;

		for (bit = 0; bit != 8; ++bit) {
			if ((oopMapByte & (1 << bit)) != 0) {
				int offset = (i * 8) + bit;
				UWord pointer;
				setType((UWordAddress) buffer + offset, AddressType_UWORD,
						HDR_BYTES_PER_WORD);
				pointer = getUWord(buffer, offset);
				if (pointer != 0) {
					setUWord(buffer, offset, pointer + (UWord) buffer);
				}
#if TRACE_SUITE
				++cardinality;
				fprintf(pointers, format("%A: %A -> %A\n"), offset*HDR_BYTES_PER_WORD, pointer, pointer + (UWord)buffer);
#endif /* TRACE_SUITE */
			}
		}
	}

#if TRACE_SUITE
	fprintf(stderr, "oopmap:{cardinality = %d}\n", cardinality);
	fclose(pointers);
}
#endif /* TRACE_SUITE */

	/*
	 * Initialize the suite pointer.
	 */
	*suite = (ByteAddress) buffer + suiteOffset;

#if TYPEMAP
	if (hasTypemap) {
		ByteAddress p = buffer;
		int i;

		for (i = 0; i != size; ++i) {
			char type = DataInputStream_readUnsignedByte(&dis, "type");
			setType(p, type, 1);
			p++;
		}
	}
#endif /* TYPEMAP */
	/*
	 * Close the data input stream.
	 */
	DataInputStream_close(&dis);
	return size;
}

#endif /* FLASH_MEMORY */

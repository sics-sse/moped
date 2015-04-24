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

#define TRUE 1
#define FALSE 0
#define DB_MAX_BREAKPOINTS 20
#define DB_CMD_BUF_SIZE 100

int db_bp_set; // TRUE if we have any breakpoints set [optimisation]
int db_debug_enabled = TRUE; //Enabled by default
int db_app_addr = 0; // 0 - will be set as the app starts (if any)

typedef struct db_breakpoint {
	ByteAddress db_ip; // 0 if not set
} Db_breakpoint;

Db_breakpoint db_bp_table[DB_MAX_BREAKPOINTS];
ByteAddress db_current_ips[DB_MAX_BREAKPOINTS + 1]; // parallel structure for performance

/* Using idx as the starting point, search buf for the next occurrance
 * of null, ':' or ',' and return index to the next byte.
 */
int skip (char* buf, int idx) {
	while (idx < DB_CMD_BUF_SIZE) {
		if ((buf[idx] == 0) || (buf[idx] == ':') || (buf[idx] == ',')) {
			return idx+1;
		}
		idx++;
	}
	iprintf("ERROR - fell off end of debug command buffer\n");
}

void db_output(char* cmd) {
	iprintf(cmd);
	fflush(stdout);
}

char* db_input(char* buf, int bufSize) {
	return autosar_fgets(buf, bufSize, stdin);
}

int db_printf0(char* format) {
    char buf[200];
    int res = siprintf(buf, format);
    if (res == -1) {
		iprintf("ERROR - Debug call to asprintf failed\n");
		exit(-1);
    }
    db_output(buf);
    return res;
}

int db_printf1(char* format, int i1) {
    char buf[200];
    int res = siprintf(buf, format, i1);
    if (res == -1) {
		iprintf("ERROR - Debug call to asprintf failed\n");
		exit(-1);
    }
    db_output(buf);
    return res;
}

int db_printf2(char* format, int i1, int i2) {
    char buf[200];
    int res = siprintf(buf, format, i1, i2);
    if (res == -1) {
		iprintf("ERROR - Debug call to sprintf failed\n");
		exit(-1);
    }
    db_output(buf);
    return res;
}

void db_send_ready() {
    db_printf0("\n");
#ifdef FLASH_MEMORY
	/*
	 * We add 0x30 to the base address of the bootstrap flash area to skip the
	 * suite header. This number will change if the size of the header changes.
	 * The address used here should match the address saved by the FlashObjectMemoryLoader
	 * in the loadMemory method. It is the address immediately after the pad.
	 * Breakpoint locations are calculated by adding to this address the offset
	 * to the method and the offset from there to the specific line number, both as
	 * specified in the .sym file.
	 */
    db_printf2("*DEBUG*:R:%i:%i\n", (int)com_sun_squawk_VM_romStart, db_app_addr+0x30);
#else
    db_printf1("*DEBUG*:R:%i\n", (int)com_sun_squawk_VM_romStart);
#endif

}

void db_send_bp_hit(int bpnum) {
    db_printf1("*DEBUG*:B:H:%i\n", bpnum);
}

void db_send_exit() {
    db_printf0("*DEBUG*:X\n");
}

void db_send_data_result(int db_opcode, ByteAddress actual_ip, UWordAddress actual_fp) {
    UWordAddress return_fp;
    UWordAddress p;
    UWordAddress sp0;
    int nlocals;
    ByteAddress mid;
    if (db_opcode == OPC_EXTEND) {
        mid = actual_ip - 1;
    } else if (db_opcode == OPC_EXTEND0) {
        mid = actual_ip - 1;
    } else {
        mid = (ByteAddress)getObject(actual_fp, FP_method);
    }
    nlocals = getLocalCount(mid);

    db_printf0("*DEBUG*:D:R:S:");

    p = actual_fp;
    return_fp = (UWordAddress)getUWord(p, FP_returnFP);
    p = p + FP_parm0; // move to first parameter
    db_printf0("P:");
    while (p < (actual_fp + FP_parm0 + db_get_parameter_count(mid) - 1)) {
		db_printf1("%i,", getUWord(p, 0));
		p++;
    }
	db_printf1("%i", getUWord(p, 0));
    db_printf0(":L:");
    p = actual_fp;
    --p; // skip the method id
    sp0 = actual_fp - (nlocals - 1);
    while (p >= sp0) {
        UWord value = getUWord(p, 0);
        if (value == DEADBEEF) {
            db_printf0("X");
        } else {
            db_printf1("%i", value);
            }
        if (p != sp0) {
            db_printf0(",");
        }
        --p;
    }
    db_printf0("\n");
}

void db_send_memory(int addr, int size) {
	unsigned int * ptr = (unsigned int *) addr;
	int i;
    db_printf0("*DEBUG*:D:R:M:");

	for (i = 1; i <= size; i++) {
		db_printf1("%i", *ptr++);
		if (i != size) {
			db_printf0(",");
		}
	}
    db_printf0("\n");
}

void db_process_data_cmd(char* buf, int db_opcode, ByteAddress actual_ip, UWordAddress actual_fp) {
	// format = "%9s:%c:%c:%i,%i";
	char subcmd;
	char subsubcmd;
	int i1, i2;
	int index = 0;
	index = skip(buf, index); // skip the header
	index = skip(buf, index); // skip the cmd
	subcmd = buf[index];
	index = skip(buf, index);
	subsubcmd = buf[index];
	index = skip(buf, index);
	i1 = atoi(&buf[index]);
	index = skip(buf, index);
	i2 = atoi(&buf[index]);

	switch (subcmd) {
		case 'S': {
			// set - not implemented yet
			iprintf("ERROR - Debug data set cmd is not implemented\n");
			exit(-1);
			break;
		}
		case 'G': {
			// get
			switch (subsubcmd) {
				case 'S': {
					// get state
					db_send_data_result(db_opcode, actual_ip, actual_fp);
					break;
				}
				case 'M': {
					// get memory
					db_send_memory(i1, i2);
					break;
				}
				default: {
					iprintf("ERROR - Debug data subcmd %c is not valid\n", subsubcmd);
					exit(-1);
				}
			}
			break;
		}
		default: {
			iprintf("ERROR - Debug data cmd %c is not valid\n", subcmd);
			exit(-1);
		}
	}
}

/* Maintain a parallel list of the current breakpoint ips (with 1 subtracted)
 * to support faster checking
 */
void db_regenerate_current_ips() {
	int i;
	int j=0;
	for (i = 0; i < DB_MAX_BREAKPOINTS; i++) {
		if (db_bp_table[i].db_ip != 0) {
			db_current_ips[j++] = db_bp_table[i].db_ip + 1; // should be sync'd with -1 in do_checkBreak
		}
	}
	for (i = j; i < DB_MAX_BREAKPOINTS; i++) {
		db_current_ips[i] = 0;
	}
}

void db_process_break_cmd(char* buf) {
	//format = "%9s:%c:%i:%i";
	char subcmd;
	int bpnum, addr;
	int i;
	int index = 0;
	index = skip(buf, index); // skip the header
	index = skip(buf, index); // skip the cmd
	subcmd = buf[index];
	index = skip(buf, index);
	bpnum = atoi(&buf[index]);
	index = skip(buf, index);
	addr = atoi(&buf[index]);

	if (bpnum < 0 || bpnum >= DB_MAX_BREAKPOINTS) {
		iprintf("ERROR - Breakpoint number %i is not valid\n", bpnum);
		exit(-1);
	}
	switch (subcmd) {
		case 'S': {
			// set
			char op = *((ByteAddress)addr);
			switch (op) {
				case OPC_EXTEND: {
					addr = addr + 2;
					break;
				}
                case OPC_EXTEND0: {
                	addr++;
                	break;
                }
			}
			db_bp_table[bpnum].db_ip = (ByteAddress)addr;
			db_bp_set = TRUE;
			db_regenerate_current_ips();
			break;
		}
		case 'C': {
			// clear
			db_bp_table[bpnum].db_ip = 0;
			for (i = 0; i < DB_MAX_BREAKPOINTS; i++) {
				db_bp_set = FALSE;
				if (db_bp_table[i].db_ip != 0) {
					db_bp_set = TRUE;
					break;
				}
			}
			db_regenerate_current_ips();
			break;
		}
		default: {
			iprintf("ERROR - Debug break cmd %c is not valid\n", subcmd);
			exit(-1);
		}
	}
}

void db_process_client_commands(int db_opcode, ByteAddress actual_ip, UWordAddress actual_fp) {
	char buf[DB_CMD_BUF_SIZE];
	// format = "%7s:%c";
	char* hdr;
	char cmd;
	int done = FALSE;
	char * result;
	int index;

	while (!done) {
		index = 0;
		result = db_input(buf, 100);
		if (result == 0) {
			// io error
			iprintf("ERROR - No data read\n");
			exit(-1);
		}
		hdr = buf;
		index = skip(buf, index);
		buf[index-1] = 0; // null terminate the header
		cmd = buf[index];

		//validate header
		if (strcmp(hdr, "*DEBUG*") != 0) {
			// bad header
			iprintf("ERROR - Debug cmd header not *DEBUG*\n");
			exit(-1);
		}
		switch (cmd) {
			case 'B': {
				db_process_break_cmd(buf);
				break;
			}
			case 'C': {
				// continue
				done = TRUE;
				break;
			}
			case 'D': {
				// data
				db_process_data_cmd(buf, db_opcode, actual_ip, actual_fp);
				break;
			}
			default: {
				iprintf("ERROR - Debug cmd %c is not valid\n", cmd);
				exit(-1);
			}
		}
	}
}

void db_prepare() {
	if (db_debug_enabled) {
		int i;
		for (i = 0; i < DB_MAX_BREAKPOINTS; i++) {
			db_bp_table[i].db_ip = 0;
			db_current_ips[i]=0;
		}
		db_current_ips[DB_MAX_BREAKPOINTS]=0;

		db_bp_set = FALSE;
		db_send_ready();
		db_process_client_commands(0, 0, 0);
	}
}

INLINE void db_checkBreak(int opcode, ByteAddress actual_ip, UWordAddress actual_fp) {
	if (db_debug_enabled) {
		if (db_bp_set) {
			// check for hit
			int i=0;
			ByteAddress bp_ip;
			while (bp_ip = db_current_ips[i++]) {
				if (bp_ip == actual_ip) {
					// Breakpoint found - now repeat search more leisurely on the "real" breakpoints.
					int j;
					for (j = 0; j < DB_MAX_BREAKPOINTS; j++) {
						if (db_bp_table[j].db_ip == (actual_ip - 1)) { //TODO - check that -1 is correct
							// hit
							db_send_bp_hit(j);
							db_process_client_commands(opcode, actual_ip, actual_fp);
							break;
						}
					}
				}
			}
		}
	}
}

void db_vm_exiting() {
	if (db_debug_enabled) {
		db_send_exit();
	}
}

int db_get_parameter_count(Address mp) {
	int b0 = getByte(mp, HDR_methodInfoStart) & 0xFF;
    if (b0 < 128) {
    	return b0 >> 2;
    } else {
        return minfoValue(mp, 3);
	}
}


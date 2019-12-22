
public class PrintTrace
{
	public int exec_count = 0;
	CpuComponents cpu;
	
	public PrintTrace(CpuComponents cpu) {
		this.cpu = cpu;
	}
	
	///  PRINT INST.  ///
	public void printInstruction(int opcode, boolean printLess) {
		String inst = null;
		
		switch(cpu.memory[opcode]) {

				// 0x00 - 0x0f

			case 0x00:
				inst = "NOP";
				break;
			case 0x01:
				inst = "LXI B, #" + toHex04((cpu.memory[opcode + 2] << 8) | cpu.memory[opcode + 1]);
				break;
			case 0x02:
				inst = "STAX B";
				break;
			case 0x03:
				inst = "INX B";
				break;
			case 0x04:
				inst = "INR B";
				break;
			case 0x05:
				inst = "DCR B";
				break;
			case 0x06:
				inst = "MVI B, #" + toHex02(cpu.memory[opcode + 1]);
				break;
			case 0x07:
				inst = "RLC";
				break;
			case 0x08:
				inst = " - ";
				break;
			case 0x09:
				inst = "DAD B";
				break;
			case 0x0a:
				inst = "LDAX B";
				break;
			case 0x0b:
				inst = "DCX B";
				break;
			case 0x0c:
				inst = "INR C";
				break;
			case 0x0d:
				inst = "DCR C";
				break;
			case 0x0e:
				inst = "MVI C, #" + toHex02(cpu.memory[opcode + 1]);
				break;
			case 0x0f:
				inst = "RRC";
				break;

				// 0x10 - 0x1f

			case 0x10:
				inst = " - ";
				break;
			case 0x11:
				inst = "LXI D, #" + toHex04((cpu.memory[opcode + 2] << 8) | cpu.memory[opcode + 1]);
				break;
			case 0x12:
				inst = "STAX B";
				break;
			case 0x13:
				inst = "INX D";
				break;
			case 0x14:
				inst = "INR D";
				break;
			case 0x15:
				inst = "DCR D";
				break;
			case 0x16:
				inst = "MVI D, #" + toHex02(cpu.memory[opcode + 1]);
				break;
			case 0x17:
				inst = "RAL";
				break;
			case 0x18:
				inst = " - ";
				break;
			case 0x19:
				inst = "DAD D";
				break;
			case 0x1a:
				inst = "LDAX D";
				break;
			case 0x1b:
				inst = "DCX D";
				break;
			case 0x1c:
				inst = "INR E";
				break;
			case 0x1d:
				inst = "DCR E";
				break;
			case 0x1e:
				inst = "MVI E, #" + toHex02(cpu.memory[opcode + 1]);
				break;
			case 0x1f:
				inst = "RAR";
				break;

				/////     0x20 - 0x2f     /////

			case 0x20:
				inst = " - ";
				break;
			case 0x21:
				inst = "LXI H, #" + toHex04((cpu.memory[opcode + 2] << 8) | cpu.memory[opcode + 1]);
				break;
			case 0x22:
				inst = "SHLD #$" + toHex04((cpu.memory[opcode + 2] << 8) | cpu.memory[opcode + 1]);
				break;
			case 0x23:
				inst = "INX H";
				break;
			case 0x24:
				inst = "INR H";
				break;
			case 0x25:
				inst = "DCR H";
				break;
			case 0x26:
				inst = "MVI H, #" + toHex02(cpu.memory[opcode + 1]);
				break;
			case 0x28:
				inst = " - ";
				break;
			case 0x29:
				inst = "DAD H";
				break;
			case 0x2a:
				inst = "LHLD #$" + toHex04((cpu.memory[opcode + 2] << 8) | cpu.memory[opcode + 1]);
				break;
			case 0x2b:
				inst = "DCX H";
				break;
			case 0x2c:
				inst = "INR L";
				break;
			case 0x2d:
				inst = "DCR L";
				break;
			case 0x2e:
				inst = "MVI L, #" + toHex02(cpu.memory[opcode + 1]);
				break;
			case 0x2f:
				inst = "CMA";
				break;

				/////     0x30 - 0x3f     /////

			case 0x31:
				inst = "LXI SP, #$" + toHex04((cpu.memory[opcode + 2] << 8) | cpu.memory[opcode + 1]);
				break;
			case 0x32:
				inst = "STA #$" + toHex04((cpu.memory[opcode + 2] << 8) | cpu.memory[opcode + 1]);
				break;
			case 0x33:
				inst = "INX SP";
				break;
			case 0x34:
				inst = "INR M";
				break;
			case 0x35:
				inst = "DCR M";
				break;
			case 0x36:
				inst = "MVI M, #" + toHex02(cpu.memory[opcode + 1]);
				break;
			case 0x37:
				inst = "STC";
				break;
			case 0x38:
				inst = " - ";
				break;
			case 0x39:
				inst = "DAD SP";
				break;
			case 0x3a:
				inst = "LDA #$" + toHex02(cpu.memory[opcode + 2]) + toHex02(cpu.memory[opcode + 1]);
				break;
			case 0x3b:
				inst = "DCX SP";
				break;
			case 0x3c:
				inst = "INR A";
				break;
			case 0x3d:
				inst = "DCR A";
				break;
			case 0x3e:
				inst = "MVI A, #" + toHex02(cpu.memory[opcode + 1]);
				break;
			case 0x3f:
				inst = "CMC";
				break;

				/////     0x40 - 0x4f     /////

			case 0x40:
				inst = "MOV B, B";
				break;
			case 0x41:
				inst = "MOV B, C";
				break;
			case 0x42:
				inst = "MOV B, D";
				break;
			case 0x43:
				inst = "MOV B, E";
				break;
			case 0x44:
				inst = "MOV B, H";
				break;
			case 0x45:
				inst = "MOV B, L";
				break;
			case 0x46:
				inst = "MOV B, M";
				break;
			case 0x47:
				inst = "MOV B, A";
				break;
			case 0x48:
				inst = "MOV C, B";
				break;
			case 0x49:
				inst = "MOV C, C";
				break;
			case 0x4a:
				inst = "MOV C, D";
				break;
			case 0x4b:
				inst = "MOV C, E";
				break;
			case 0x4c:
				inst = "MOV C, H";
				break;
			case 0x4d:
				inst = "MOV C, L";
				break;
			case 0x4e:
				inst = "MOV C, M";
				break;
			case 0x4f:
				inst = "MOV C, A";
				break;
			case 0x50:
				inst = "MOV D, B";
				break;
			case 0x51:
				inst = "MOV D, C";
				break;
			case 0x52:
				inst = "MOV D, D";
				break;
			case 0x53:
				inst = "MOV D, E";
				break;
			case 0x54:
				inst = "MOV D, H";
				break;
			case 0x55:
				inst = "MOV D, L";
				break;
			case 0x56:
				inst = "MOV D, M";
				break;
			case 0x57:
				inst = "MOV D, A";
				break;
			case 0x58:
				inst = "MOV E, B";
				break;
			case 0x59:
				inst = "MOV E, C";
				break;
			case 0x5a:
				inst = "MOV E, D";
				break;
			case 0x5b:
				inst = "MOV E, E";
				break;
			case 0x5c:
				inst = "MOV E, H";
				break;
			case 0x5d:
				inst = "MOV E, L";
				break;
			case 0x5e:
				inst = "MOV E, M";
				break;
			case 0x5f:
				inst = "MOV E, A";
				break;
			case 0x60:
				inst = "MOV H, B";
				break;
			case 0x61:
				inst = "MOV H, C";
				break;
			case 0x62:
				inst = "MOV H, D";
				break;
			case 0x63:
				inst = "MOV H, E";
				break;
			case 0x64:
				inst = "MOV H, H";
				break;
			case 0x65:
				inst = "MOV H, L";
				break;
			case 0x66:
				inst = "MOV H, M";
				break;
			case 0x67:
				inst = "MOV H, A";
				break;
			case 0x68:
				inst = "MOV L, B";
				break;
			case 0x69:
				inst = "MOV L, C";
				break;
			case 0x6a:
				inst = "MOV L, D";
				break;
			case 0x6b:
				inst = "MOV L, E";
				break;
			case 0x6c:
				inst = "MOV L, H";
				break;
			case 0x6d:
				inst = "MOV L, L";
				break;
			case 0x6e:
				inst = "MOV L, M";
				break;
			case 0x6f:
				inst = "MOV L, A";
				break;
			case 0x70:
				inst = "MOV M, B";
				break;
			case 0x71:
				inst = "MOV M, C";
				break;
			case 0x72:
				inst = "MOV M, D";
				break;
			case 0x73:
				inst = "MOV M, E";
				break;
			case 0x74:
				inst = "MOV M, H";
				break;
			case 0x75:
				inst = "MOV M, L";
				break;
			case 0x76:
				inst = "HLT";
				break;
			case 0x77:
				inst = "MOV M, A";
				break;
			case 0x78:
				inst = "MOV A, B";
				break;
			case 0x79:
				inst = "MOV A, C";
				break;
			case 0x7a:
				inst = "MOV A, D";
				break;
			case 0x7b:
				inst = "MOV A, E";
				break;
			case 0x7c:
				inst = "MOV A, H";
				break;
			case 0x7d:
				inst = "MOV A, L";
				break;
			case 0x7e:
				inst = "MOV A, M";
				break;
			case 0x7f:
				inst = "MOV A, A";
				break;

				/////     0x80 - 0x8f     /////

			case 0x80:
				inst = "ADD B";
				break;
			case 0x81:
				inst = "ADD C";
				break;
			case 0x82:
				inst = "ADD D";
				break;
			case 0x83:
				inst = "ADD E";
				break;
			case 0x84:
				inst = "ADD H";
				break;
			case 0x85:
				inst = "ADD L";
				break;
			case 0x86:
				inst = "ADD M";
				break;
			case 0x87:
				inst = "ADD A";
				break;
			case 0x88:
				inst = "ADC B";
				break;
			case 0x89:
				inst = "ADC C";
				break;
			case 0x8a:
				inst = "ADC D";
				break;
			case 0x8b:
				inst = "ADC E";
				break;
			case 0x8c:
				inst = "ADC H";
				break;
			case 0x8d:
				inst = "ADC L";
				break;
			case 0x8e:
				inst = "ADC M";
				break;
			case 0x8f:
				inst = "ADC A";
				break;

				/////     0x90 - 0x9f     /////

			case 0x90:
				inst = "SUB B";
				break;
			case 0x91:
				inst = "SUB C";
				break;
			case 0x92:
				inst = "SUB D";
				break;
			case 0x93:
				inst = "SUB E";
				break;
			case 0x94:
				inst = "SUB H";
				break;
			case 0x95:
				inst = "SUB L";
				break;
			case 0x96:
				inst = "SUB M";
				break;
			case 0x97:
				inst = "SUB A";
				break;
			case 0x98:
				inst = "SBB B";
				break;
			case 0x99:
				inst = "SBB C";
				break;
			case 0x9a:
				inst = "SBB D";
				break;
			case 0x9b:
				inst = "SBB E";
				break;
			case 0x9c:
				inst = "SBB H";
				break;
			case 0x9d:
				inst = "SBB L";
				break;
			case 0x9e:
				inst = "SBB M";
				break;
			case 0x9f:
				inst = "SBB A";
				break;

				/////     0xa0 - 0xaf     /////

			case 0xa0:
				inst = "ANA B";
				break;
			case 0xa1:
				inst = "ANA C";
				break;
			case 0xa2:
				inst = "ANA D";
				break;
			case 0xa3:
				inst = "ANA E";
				break;
			case 0xa4:
				inst = "ANA H";
				break;
			case 0xa5:
				inst = "ANA L";
				break;
			case 0xa6:
				inst = "ANA M";
				break;
			case 0xa7:
				inst = "ANA A";
				break;
			case 0xa8:
				inst = "XRA B";
				break;
			case 0xa9:
				inst = "XRA C";
				break;
			case 0xaa:
				inst = "XRA D";
				break;
			case 0xab:
				inst = "XRA E";
				break;
			case 0xac:
				inst = "XRA H";
				break;
			case 0xad:
				inst = "XRA L";
				break;
			case 0xae:
				inst = "XRA M";
				break;
			case 0xaf:
				inst = "XRA A";
				break;

				/////     0xb0 - 0xbf     /////

			case 0xb0:
				inst = "ORA B";
				break;
			case 0xb1:
				inst = "ORA C";
				break;
			case 0xb2:
				inst = "ORA D";
				break;
			case 0xb3:
				inst = "ORA E";
				break;
			case 0xb4:
				inst = "ORA H";
				break;
			case 0xb5:
				inst = "ORA L";
				break;
			case 0xb6:
				inst = "ORA M";
				break;
			case 0xb7:
				inst = "ORA A";
				break;
			case 0xb8:
				inst = "CMP B";
				break;
			case 0xb9:
				inst = "CMP C";
				break;
			case 0xba:
				inst = "CMP D";
				break;
			case 0xbb:
				inst = "CMP E";
				break;
			case 0xbc:
				inst = "CMP H";
				break;
			case 0xbd:
				inst = "CMP L";
				break;
			case 0xbe:
				inst = "CMP M";
				break;
			case 0xbf:
				inst = "CMP A";
				break;

				/////     0xc0 - 0xcf     /////

			case 0xc0:
				inst = "RNZ";
				break;
			case 0xc1:
				inst = "POP B";
				break;
			case 0xc2:
				inst = "JNZ $" + toHex04((cpu.memory[opcode + 2] << 8) | cpu.memory[opcode + 1]);
				break;
			case 0xc3:
				inst = "JMP #$" + toHex04((cpu.memory[opcode + 2] << 8) | cpu.memory[opcode + 1]);
				break;
			case 0xc4:
				inst = "CNZ #$" + toHex04((cpu.memory[opcode + 2] << 8) | cpu.memory[opcode + 1]);
				break;
			case 0xc5:
				inst = "PUSH B";
				break;
			case 0xc6:
				inst = "ADI #" + toHex02(cpu.memory[opcode + 1]);
				break;
			case 0xc7:
				inst = "RST 0";
				break;
			case 0xc8:
				inst = "RZ";
				break;
			case 0xc9:
				inst = "RET";
				break;
			case 0xca:
				inst = "JZ $" + toHex04((cpu.memory[opcode + 2] << 8) | cpu.memory[opcode + 1]);
				break;
			case 0xcb:
				inst = " - ";
				break;
			case 0xcc:
				inst = "CZ #$" + toHex04((cpu.memory[opcode + 2] << 8) | cpu.memory[opcode + 1]);
				break;
			case 0xcd:
				inst = "CALL $" + toHex04((cpu.memory[opcode + 2] << 8) | cpu.memory[opcode + 1]);
				break;
			case 0xce:
				inst = "ACI #" + toHex02(cpu.memory[opcode + 1]);
				break;
			case 0xcf:
				inst = "RST 1";
				break;
				/////     0xd0 - 0xdf     /////

			case 0xd0:
				inst = "RNC";
				break;
			case 0xd1:
				inst = "POP D";
				break;
			case 0xd2:
				inst = "JNC #$" + toHex04((cpu.memory[opcode + 2] << 8) | cpu.memory[opcode + 1]);
				break;
			case 0xd3:
				inst = "OUT #" + toHex02(cpu.memory[opcode + 1]);
				break; // PORT?
			case 0xd4:
				inst = "CNC #$" + toHex04((cpu.memory[opcode + 2] << 8) | cpu.memory[opcode + 1]);
				break;
			case 0xd5:
				inst = "PUSH D";
				break;
			case 0xd6:
				inst = "SUI #" + toHex02(cpu.memory[opcode + 1]);
				break;
			case 0xd7:
				inst = "RST 2";
				break;
			case 0xd8:
				inst = "RC";
				break;
			case 0xda:
				inst = "JC #$" + toHex04((cpu.memory[opcode + 2] << 8) | cpu.memory[opcode + 1]);
				break;
			case 0xdc:
				inst = "CC #$" + toHex04((cpu.memory[opcode + 2] << 8) | cpu.memory[opcode + 1]);
				break;
			case 0xdd:
				inst = " - ";
				break;
			case 0xde:
				inst = "SBI #" + toHex02(cpu.memory[opcode + 1]);
				break;
			case 0xdf:
				inst = "RST 3";
				break;
				/////     0xe0 - 0xef     /////

			case 0xe0:
				inst = "RPO";
				break;
			case 0xe1:
				inst = "POP H";
				break;
			case 0xe2:
				inst = "JPO #$" + toHex04((cpu.memory[opcode + 2] << 8) | cpu.memory[opcode + 1]);
				break;
			case 0xe3:
				inst = "XTHL";
				break;
			case 0xe4:
				inst = "CPO $" + toHex04((cpu.memory[opcode + 2] << 8) | cpu.memory[opcode + 1]);
				break;
			case 0xe5:
				inst = "PUSH H";
				break;
			case 0xe6:
				inst = "ANI #" + toHex02(cpu.memory[opcode + 1]);
				break;
			case 0xe7:
				inst = "RST 4";
				break;
			case 0xe8:
				inst = "RPE";
				break;
			case 0xe9:
				inst = "PCHL";
				break;
			case 0xea:
				inst = "JPE #$" + toHex04((cpu.memory[opcode + 2] << 8) | cpu.memory[opcode + 1]);
				break;
			case 0xeb:
				inst = "XCHG";
				break;
			case 0xec:
				inst = "CPE #$" + toHex04((cpu.memory[opcode + 2] << 8) | cpu.memory[opcode + 1]);
				break;
			case 0xed:
				inst = " - ";
				break;
			case 0xee:
 				inst = "XRI #" + toHex02(cpu.memory[opcode + 1]);
				break;
			case 0xef:
				inst = "RST 5";
				break;
				/////     0xf0 - 0xff     /////

			case 0xf0:
				inst = "RP";
				break;
			case 0xf1:
				inst = "POP PSW";
				break;
			case 0xf2:
				inst = "JP #$" + toHex04((cpu.memory[opcode + 2] << 8) | cpu.memory[opcode + 1]);
				break;
			case 0xf3:
				inst = "DI";
				break;
			case 0xf4:
				inst = "CP #$" + toHex04((cpu.memory[opcode + 2] << 8) | cpu.memory[opcode + 1]);
				break;
			case 0xf5:
				inst = "PUSH PSW";
				break;
			case 0xf6:
				inst = "ORI #" + toHex02(cpu.memory[opcode + 1]);
				break;
			case 0xf7:
				inst = "RST 6";
				break;
			case 0xf8:
				inst = "RM";
				break;
			case 0xf9:
				inst = "SPHL";
				break;
			case 0xfa:
				inst = "JM #$" + toHex04((cpu.memory[opcode + 2] << 8) | cpu.memory[opcode + 1]);
				break;
			case 0xfb:
				inst = "EI";
				break;
			case 0xfc:
				inst = "CM #$" + toHex04((cpu.memory[opcode + 2] << 8) | cpu.memory[opcode + 1]);
				break;
			case 0xfd:
				inst = " - ";
				break;
			case 0xfe:
 				inst = "CPI #" + toHex02(cpu.memory[opcode + 1]);
				break;
			case 0xff:
				inst = "RST 7";
				break;
			default:
				inst = "" + toHex02(cpu.memory[opcode]) + " is not implemented!";
				System.out.println(inst);
				return;

		}
		if (printLess) {
			System.out.println(toHex04(cpu.PC) + "  " + inst);
		} else {
			// Cycle
			exec_count++;
			
			// Print registers
			System.out.println(
				"B: " + toHex02(cpu.B) + " | C: " + toHex02(cpu.C) + " | D: " + toHex02(cpu.D) +
				" | E: " + toHex02(cpu.E) + " | H: " + toHex02(cpu.H) + " | L: " + toHex02(cpu.L) +
				" | M: " + toHex02(cpu.memory[cpu.memory[cpu.H << 8] | cpu.memory[cpu.L]])  + " | A: " + toHex02(cpu.A));

			// Print Flags
			System.out.println("CY: " + cpu.cc.CY + " | ZR: " + cpu.cc.Z + " | PA: " + cpu.cc.P + " | SN: " + cpu.cc.S  + " | AC: " + cpu.cc.AC);

			// Print Stack Pointer and its contents (TPS = Top stack; BMS = Bottom stack)
			System.out.print("SP: " + toHex04(cpu.SP) + " | ");
			if (cpu.SP != 0) {
				System.out.println("TPS: " + toHex02(cpu.memory[cpu.SP]) + " $" + toHex04(cpu.SP) + " | BMS: " + toHex02(cpu.memory[cpu.SP + 1]) + " $" + toHex04(cpu.SP + 1));
			} else {
				System.out.println("Stack Pointer at 0");
			}
			
			System.out.println("CYCLE: " + exec_count + " | FA: " + toHex04(opcode - Interpreter.realAddr) + " | PC: " + toHex04(opcode) + " (" + toHex02(cpu.memory[opcode]) + ")" + " " + inst);

			// Print Separator
			System.out.println();
		}
	}
	
	public String toHex04(int value) {
		return String.format("%04x", value);
	}

	public String toHex02(int value) {
		return String.format("%02x", value);
	}
}

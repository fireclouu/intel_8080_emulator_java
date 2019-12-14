import Cpu.*;

public class CpuEmulation
{
	// NOTES
	/*
	   - Use objects as parameter for function/subroutine when re-assigning (e.g. *someobject*.value = *somevariable*)
	   since there's no pointer in java
	   - strict handling of x > 0xff, java only has signed data types
	*/
	
	///  DECLARE CPU COMPONENTS  ///
	public static Component B, C, D, E, H, L, A;
	public static Component PC, SP;
	public static Flags Z, S, P, CY, AC;
	
	///  MEMORY  ///
	private int[] memory;
	
	///  PSW FLAG POSITIONS  ///
	final static int 
	PSW_FLAG_POS_CY = 0b_0000_0001, // on bit pos 0
	PSW_FLAG_POS_PA = 0b_0000_0100, // on bit pos 2
	PSW_FLAG_POS_AC = 0b_0001_0000, // on bit pos 4
	PSW_FLAG_POS_ZE = 0b_0100_0000, // on bit pos 6
	PSW_FLAG_POS_SN = 0b_1000_0000; // on bit pos 7
	
	/* 
	 pos 0 = carry
	 pos 2 = parity
	 pos 4 = aux carry
	 pos 6 = zero
	 pos 7 = sign
	 */
	
	///  CONSTRUCTOR  ///
	public CpuEmulation(int[] rom) {
		init();
		memory = rom;
		System.out.println("Start emulator...\n");
		
		// Start emulation
		emulate8080();
		
		System.out.println("\nEnd emulator...");
	}
	
	/////   MAIN EMULATION   /////
	public void emulate8080() {
		while(PC.value < Main.PROGRAM_LENGTH) {
			// opcode
			int opcode = PC.value;
			
			// HL (M)
			int addr = ((H.value << 8) | L.value);
			
			// increment PC
			PC.value++;
			
			// print instruction
			printInstruction(opcode);
			
			switch(memory[opcode]) {
				
				/////   0x00 - 0x0f   /////
				
				case 0x00:
					break; // NOP
				case 0x01:
					LXI(opcode, B, C);
					break; // LXI B, D16
				case 0x02:
					STA(B.value, C.value);
					break; // STAX B
				case 0x03:
					INX(B, C);
					break; // INX B
				case 0x04:
					INR(B);
					break; // INR B
				case 0x05:
					DCR(B);
					break; // DCR B
				case 0x06:
					MVI(opcode, B);
					break; // MVI B, D8
				case 0x07:
					
					if (CY.flag != 0) {
						A.value <<= 1; // left shift
						CY.flag = 0;
					}
					
					break; // RLC
				case 0x08:
					break; // -
				case 0x09:
					DAD(B, C);
					break; //DAD B
				case 0x0a:
					LDA(B.value, C.value);
					break; // LDAX B
				case 0x0b:
					DCX(B, C);
					break; // DCX B
				case 0x0c:
					INR(C);
					break; // INR C
				case 0x0d:
					DCR(C);
					break; // DCR C
				case 0x0e:
					MVI(opcode, C);
					break; // MVI C, D8
				case 0x0f:
					
					if (CY.flag != 0) {
						A.value >>>= 1; // right shift (zero fill)
						CY.flag = 0;
					}
					
					break; // RRC
					
				//////   0x10 - 0x1f   /////
					
				case 0x10:
					break; // -
				case 0x11:
					LXI(opcode, D, E);
					break; // LXI D, D16
				case 0x12:
					STA(D.value, E.value);
					break; // STAX D
				case 0x13:
					INX(D, E);
					break; // INX D
				case 0x14:
					INR(D);
					break; // INR D
				case 0x15:
					DCR(D);
					break; // DCR D
				case 0x16:
					MVI(opcode, D);
					break; // MVI D, D8
				case 0x18:
					break; // -
				case 0x19:
					DAD(D, E);
					break; //DAD D
				case 0x1a:
					LDA(D.value, E.value);
					break; // LDAX D
				case 0x1b:
					DCX(D, E);
					break; // DCX D
				case 0x1c:
					INR(E);
					break; // INR E
				case 0x1d:
					DCR(E);
					break; // DCR E
				case 0x1e:
					MVI(opcode, E);
					break; // MVI E, D8
					
				//////   0x20 - 0x2f   /////
					
				case 0x20:
					break; // -
				case 0x21:
					LXI(opcode, H, L);
					break; // LXI H, D16
				case 0x23:
					INX(H, L);
					break; // INX H
				case 0x24:
					INR(H);
					break; // INR H
				case 0x25:
					DCR(H);
					break; // DCR H
				case 0x26:
					MVI(opcode, H);
					break; // MVI H, D8
				case 0x29:
					DAD(H, L);
					break; //DAD H
				case 0x2b:
					DCX(H, L);
					break; // DCX H
				case 0x2c:
					INR(L);
					break; // INR L
				case 0x2d:
					DCR(L);
					break; // DCR L
				case 0x2e:
					MVI(opcode, L);
					break; // MVI L, D8
					
				//////   0x30 - 0x3f   /////
					
				case 0x31:
					LXI(opcode, SP);
					break; // LXI SP, D16
				case 0x32:
					STA(memory[opcode + 2], memory[opcode + 1]);
					PC.value += 2;
					break; // STA adr
				case 0x33:
					INX(SP);
					break; // INX SP
				case 0x34:
					INR(addr);
					break; // INR M
				case 0x35:
					DCR(addr);
					break; // DCR M
				case 0x36:
					MVI(opcode, addr);
					break; // MVI M, D8
				case 0x39:
					DAD(SP);
					break; //DAD SP
				case 0x3a:
					LDA(memory[opcode + 2], memory[opcode + 1]);
					PC.value += 2;
					break; // LDA adr
				case 0x3b:
					DCX(SP);
					break; // DCX SP
				case 0x3d:
					DCR(A);
					break; // DCR A
				case 0x3e:
					MVI(opcode, A);
					break; // MVI A, D8
					
				//////   0x40 - 0x4f   /////
					
				case 0x40:
					B.value = B.value;
					break; // MOV B, B
				case 0x41:
					B.value = C.value;
					break; // MOV B, C
				case 0x42:
					B.value = D.value;
					break; // MOV B, D
				case 0x43:
					B.value = E.value;
					break; // MOV B, E
				case 0x44:
					B.value = H.value;
					break; // MOV B, H
				case 0x45:
					B.value = L.value;
					break; // MOV B, L
				case 0x46:
					B.value = memory[addr];
					break; // MOV B, M
				case 0x47:
					B.value = A.value;
					break; // MOV B, A
				case 0x48:
					C.value = B.value;
					break; // MOV C, B
				case 0x49:
					C.value = C.value;
					break; // MOV C, C
				case 0x4a:
					C.value = D.value;
					break; // MOV C, D
				case 0x4b:
					C.value = E.value;
					break; // MOV C, E
				case 0x4c:
					C.value = H.value;
					break; // MOV C, H
				case 0x4d:
					C.value = L.value;
					break; // MOV C, L
				case 0x4e:
					C.value = memory[addr];
					break; // MOV C, M
				case 0x4f:
					C.value = A.value;
					break; // MOV C, A
					
				//////   0x50 - 0x5f   /////
					
				case 0x50:
					D.value = B.value;
					break; // MOV D, B
				case 0x51:
					D.value = C.value;
					break; // MOV D, C
				case 0x52:
					D.value = D.value;
					break; // MOV D, D
				case 0x53:
					D.value = E.value;
					break; // MOV D, E
				case 0x54:
					D.value = H.value;
					break; // MOV D, H
				case 0x55:
					D.value = L.value;
					break; // MOV D, L
				case 0x56:
					D.value = memory[addr];
					break; // MOV D, M
				case 0x57:
					D.value = A.value;
					break; // MOV D, A
				case 0x58:
					E.value = B.value;
					break; // MOV E, B
				case 0x59:
					E.value = C.value;
					break; // MOV E, C
				case 0x5a:
					E.value = D.value;
					break; // MOV E, D
				case 0x5b:
					E.value = E.value;
					break; // MOV E, E
				case 0x5c:
					E.value = H.value;
					break; // MOV E, H
				case 0x5d:
					E.value = L.value;
					break; // MOV E, L
				case 0x5e:
					E.value = memory[addr];
					break; // MOV E, M
				case 0x5f:
					E.value = A.value;
					break; // MOV E, A
					
				//////   0x60 - 0x6f   /////
					
				case 0x60:
					H.value = B.value;
					break; // MOV H, B
				case 0x61:
					H.value = C.value;
					break; // MOV H, C
				case 0x62:
					H.value = D.value;
					break; // MOV H, D
				case 0x63:
					H.value = E.value;
					break; // MOV H, E
				case 0x64:
					H.value = H.value;
					break; // MOV H, H
				case 0x65:
					H.value = L.value;
					break; // MOV H, L
				case 0x66:
					H.value = memory[addr];
					break; // MOV H, M
				case 0x67:
					H.value = A.value;
					break; // MOV H, A
				case 0x68:
					L.value = B.value;
					break; // MOV L, B
				case 0x69:
					L.value = C.value;
					break; // MOV L, C
				case 0x6a:
					L.value = D.value;
					break; // MOV L, D
				case 0x6b:
					L.value = E.value;
					break; // MOV L, E
				case 0x6c:
					L.value = H.value;
					break; // MOV L, H
				case 0x6d:
					L.value = L.value;
					break; // MOV L, L
				case 0x6e:
					L.value = memory[addr];
					break; // MOV L, M
				case 0x6f:
					L.value = A.value;
					break; // MOV L, A
					
				//////   0x70 - 0x7f   /////
					
				case 0x70:
					memory[addr] = B.value;
					break; // MOV M, B
				case 0x71:
					memory[addr] = C.value;
					break; // MOV M, C
				case 0x72:
					memory[addr] = D.value;
					break; // MOV M, D
				case 0x73:
					memory[addr] = E.value;
					break; // MOV M, E
				case 0x74:
					memory[addr] = H.value;
					break; // MOV M, H
				case 0x75:
					memory[addr] = L.value;
					break; // MOV M, L
				case 0x76:
					return; // HLT
				case 0x77:
					memory[addr] = A.value;
					break; // MOV M, A
				case 0x78:
					A.value = B.value;
					break; // MOV A, B
				case 0x79:
					A.value = C.value;
					break; // MOV A, C
				case 0x7a:
					A.value = D.value;
					break; // MOV A, D
				case 0x7b:
					A.value = E.value;
					break; // MOV A, E
				case 0x7c:
					A.value = H.value;
					break; // MOV A, H
				case 0x7d:
					A.value = L.value;
					break; // MOV A, L
				case 0x7e:
					A.value = memory[addr];
					break; // MOV A, M
				case 0x7f:
					A.value = A.value;
					break; // MOV A, A
					
				//////   0x80 - 0x8f   /////
					
				case 0x80:
					ADD(B.value);
					break; // ADD B
				case 0x81:
					ADD(C.value);
					break; // ADD C
				case 0x82:
					ADD(D.value);
					break; // ADD D
				case 0x83:
					ADD(E.value);
					break; // ADD E
				case 0x84:
					ADD(H.value);
					break; // ADD H
				case 0x85:
					ADD(L.value);
					break; // ADD L
				case 0x86:
					ADD(memory[addr]);
					break; // ADD M
				case 0x87:
					ADD(A.value);
					break; // ADD A
					
				//////   0x90 - 0x9f   /////
					
				
				//////   0xa0 - 0xaf   /////
				
				case 0xa8:
					XRA(B.value);
					break; // XRA B
				case 0xa9:
					XRA(C.value);
					break; // XRA C
				case 0xaa:
					XRA(D.value);
					break; // XRA D
				case 0xab:
					XRA(E.value);
					break; // XRA E
				case 0xac:
					XRA(H.value);
					break; // XRA H
				case 0xad:
					XRA(L.value);
					break; // XRA L
				case 0xae:
					XRA(memory[addr]);
					break; // XRA M
				case 0xaf:
					XRA(A.value);
					break; // XRA A
					
				//////   0xb0 - 0xbf   /////
					
				//////   0xc0 - 0xcf   /////
				
				case 0xc1:
					POP(B, C);
					break; // POP B
				case 0xc2:
					if(Z.flag == 0) {
						PC.value = (memory[opcode + 2] << 8) | memory[opcode + 1];
					} else {
						PC.value += 2;
					}
					break; // JNZ adr
				case 0xc3:
					PC.value = (memory[opcode + 2] << 8) | memory[opcode + 1];
					break; // JMP adr
				case 0xc5:
					PUSH(B, C);
					break; // PUSH B
				case 0xc6:
					ADD(memory[opcode + 1]);
					PC.value++;
					break; // ADI D8
				case 0xc9:
					RET();
					break; // RET
				case 0xcd:
					CALL(opcode);
					break; // CALL adr
					
				//////   0xd0 - 0xdf   /////
					
				case 0xd1:
					POP(D, E);
					break; // POP D
				case 0xd3:
					PC.value++;
					break; // OUT D8
				case 0xd5:
					PUSH(D, E);
					break; // PUSH D
					
				//////   0xe0 - 0xef   /////
					
				case 0xe1:
					POP(H, L);
					break; // POP H
				case 0xe4:
					if (P.flag == 0) {
						CALL(opcode);
					} else {
						PC.value += 2;
					}
					break; // CPO adr
				case 0xe5:
					PUSH(H, L);
					break; // PUSH H
				case 0xe6:
					ANI(opcode);
					break; // ANI D8
				case 0xeb:
					XCHG();
					break; // XCHG (HL to DE vice-versa)
					
				//////   0xf0 - 0xff   /////
					
				case 0xf1:
					POP_PSW();
					break; // POP PSW
				case 0xf5:
					PUSH_PSW();
					break; // PUSH PSW
				case 0xfe:
					CMP(memory[opcode + 1]);
					break; // CPI D8

				default:
					return;
			}
		}
	}
	
	///  PRINT INST.  ///
	public void printInstruction(int opcode) {
		String inst = null;
		
		switch(memory[opcode]) {
			
			// 0x00 - 0x0f
			
			case 0x00:
				inst = "NOP";
				break;
			case 0x01:
				inst = "LXI B, #" + toHex02(memory[opcode + 2]) + toHex02(memory[opcode + 1]);
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
				inst = "MVI B, #" + toHex02(memory[opcode + 1]);
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
				inst = "MVI C, #" + toHex02(memory[opcode + 1]);
				break;
			case 0x0f:
				inst = "RRC";
				break;
				
			// 0x10 - 0x1f
				
			case 0x10:
				inst = " - ";
				break;
			case 0x11:
				inst = "LXI D, #" + toHex02(memory[opcode + 2]) + toHex02(memory[opcode + 1]);
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
				inst = "MVI D, #" + toHex02(memory[opcode + 1]);
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
				inst = "MVI E, #" + toHex02(memory[opcode + 1]);
				break;
			case 0x20:
				inst = " - ";
				break;
			case 0x21:
				inst = "LXI H, #" + toHex02(memory[opcode + 2]) + toHex02(memory[opcode + 1]);
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
				inst = "MVI H, #" + toHex02(memory[opcode + 1]);
				break;
			case 0x29:
				inst = "DAD H";
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
				inst = "MVI L, #" + toHex02(memory[opcode + 1]);
				break;
			case 0x31:
				inst = "LXI SP, #" + toHex02(memory[opcode + 2]) + toHex02(memory[opcode + 1]);
				break;
			case 0x32:
				inst = "STA #$" + toHex02(memory[opcode + 2]) + toHex02(memory[opcode + 1]);
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
				inst = "MVI M, #" + toHex02(memory[opcode + 1]);
				break;
			case 0x39:
				inst = "DAD SP";
				break;
			case 0x3a:
				inst = "LDA #$" + toHex02(memory[opcode + 2]) + toHex02(memory[opcode + 1]);
				break;
			case 0x3b:
				inst = "DCX SP";
				break;
			case 0x3d:
				inst = "DCR A";
				break;
			case 0x3e:
				inst = "MVI A, #" + toHex02(memory[opcode + 1]);
				break;
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
			case 0xc1:
				inst = "POP B";
				break;
			case 0xc2:
				inst = "JNZ $" + toHex04((memory[opcode + 2] << 8) + memory[opcode + 1]) + " | ZF: " + Z.flag;
				break;
			case 0xc3:
				inst = "JMP $" + toHex04((memory[opcode + 2] << 8) + memory[opcode+1]);
				break;
			case 0xc5:
				inst = "PUSH B";
				break;
			case 0xc6:
				inst = "ADI #" + toHex02(memory[opcode + 1]);
				break;
			case 0xc9:
				inst = "RET";
				break;
			case 0xcd:
				inst = "CALL $" + toHex04((memory[opcode + 2] << 8) + memory[opcode + 1]);
				break;
			case 0xd1:
				inst = "POP D";
				break;
			case 0xd3:
				inst = "OUT #" + toHex02(memory[opcode + 1]);
				break; // PORT?
			case 0xd5:
				inst = "PUSH D";
				break;
			case 0xe1:
				inst = "POP H";
				break;
			case 0xe4:
				inst = "CPO $" + toHex04((memory[opcode + 2] << 8) + memory[opcode + 1]);
				break;
			case 0xe5:
				inst = "PUSH H";
				break;
			case 0xe6:
				inst = "ANI #" + toHex02(memory[opcode + 1]);
				break;
			case 0xeb:
				inst = "XCHG";
				break;
			case 0xf1:
				inst = "POP PSW";
				break;
			case 0xf5:
				inst = "PUSH PSW";
				break;
			case 0xfe:
				inst = "CPI #" + toHex02(memory[opcode + 1]);
				break;
			default:
				inst = "" + toHex02(memory[opcode]) + " is not implemented!";
		}
		
		System.out.println("SP: " + toHex04(SP.value) + " | " + toHex04(opcode) + ": " + inst);
	}
	
	
	///  SUBROUTINES  ///
	private void ADD(int var) {
		int res = A.value + var;
		
		flags_BCD(res);
		
		A.value = res & 0xff;
	}
	
	private void ANI(int opcode) {
		int res = A.value & memory[opcode + 1];
		flags_BCD(res);
		CY.flag = 0;	// override BCD flag result
		
		PC.value++;
	}
	
	private void CALL(int opcode) {
		int nextAddr = opcode + 3;
		memory[SP.value - 1] = ((nextAddr >> 8) & 0xff); // Store rightmost 8bit addr
		memory[SP.value - 2] = (nextAddr & 0xff);
		SP.value -= 2;
		
		PC.value = (memory[opcode + 2] << 8) | memory[opcode + 1];
	}
	
	// Dedicate individual flag checks
	private void CMP(int var) {
		// a + (two comp. immediate)
		// complement — defined also as "another set" e.g. another set of binary 1 is binary 0!
		// similar to a - immediate
		// int twoComp = ((~opbyte & 0xff) + 1);
		int res = A.value - var;
		
		Z.flag = (res == 0) ? (byte) 1 : 0;
		S.flag = ((res & 0x80) == 0x80) ? (byte) 1 : 0;
		P.flag = parityFlag(res);  // ensuring only checks for 8-bit variable
		CY.flag = (res > A.value) ? 1 : (byte) 0;  // TODO : verify
		AC.flag = (res > 0x9) ? (byte) 1 : 0;
		
		PC.value++;
	}
	
	private void DAD(Component... rp) {
		int HL = (H.value << 8) | L.value; // (bits) 8 + 8 = 16
		
		int pair;
		if(rp.length == 2) {
			pair = (rp[0].value << 8) | rp[1].value;
		} else 
		{
			pair = rp[0].value;
		}
		
		int res = HL + pair; // may result greater than 16 bit, raise CY if occured
		
		CY.flag = ((res & 0xffff_0000) > 0) ? (byte) 1 : 0; // cut all values from lower 16 bit and check if higher 16 bit has value
		
		H.value = (res & 0xff00) >> 8;	// store higher 8-bit to H
		L.value = (res & 0xff);			// store lower  8-bit to L
	}

	private void DCR(Component reg) {
		int res = reg.value - 1;
		
		Z.flag = (res == 0) ? (byte) 1 : 0;
		S.flag = ((res & 0x80) == 0x80) ? (byte) 1 : 0; // Masking
		P.flag = parityFlag(res & 0xff);
		
		reg.value = res & 0xff;
	}
	
	private void DCX(Component... rp) {
		if (rp.length == 2) {
			// Register Pair
			rp[1].value--;

			if (rp[1].value == 0xff) {
				rp[0].value--;
			}

			rp[1].value &= 0xff;
			rp[0].value &= 0xff;
			
		} else {
			// 16-bit variable (SP)
			rp[0].value = (rp[0].value - 1) & 0xffff;
		}
		
	}
	
	private void INR(Component reg) {
		int res = reg.value + 1;
		reg.value = res & 0xff; // ensure it only takes 8-bit
		
		flags_Arith(res);
		
	}
	
	private void INX(Component... rp) {
		if (rp.length == 2) {
			rp[1].value++;
			
			if (rp[1].value > 0xff) {
				rp[0].value++;
			}
			
			rp[1].value &= 0xff;
			rp[0].value &= 0xff;
			
		} else {
			rp[0].value = (rp[0].value + 1) & 0xffff;
		}
	}
	
	private void LDA(int hi_nib, int lo_nib) {
		int addr = (hi_nib << 8) | lo_nib;
		A.value = memory[addr];
	}
	
	private void LXI(int opcode, Component... rp) {
		if(rp.length == 2) {
			// Register Pair
			rp[0].value = memory[opcode + 2];	// RP 1 = byte 3
			rp[1].value = memory[opcode + 1];	// RP 2 = byte 2
		} else {
			// 16-bit variable (SP)
			rp[0].value = ((memory[opcode + 2] << 8) | memory[opcode + 1]) & 0xffff;
		}
		
		PC.value += 2;
		
	}
	
	private void MVI(int opcode, Component reg) {
		reg.value = memory[opcode + 1];
	
		PC.value++;
	}
	
	private void POP(Component... rp) {
		rp[1].value = memory[SP.value];
		rp[0].value = memory[SP.value + 1];
		SP.value += 2;
	}
	
	private void POP_PSW() {
		int PSW = memory[SP.value];
		
		CY.flag = ((PSW & PSW_FLAG_POS_CY) != 0) ? (byte) 1 : 0;
		P.flag  = ((PSW & PSW_FLAG_POS_PA) != 0) ? (byte) 1 : 0;
		AC.flag = ((PSW & PSW_FLAG_POS_AC) != 0) ? (byte) 1 : 0;
		Z.flag  = ((PSW & PSW_FLAG_POS_ZE) != 0) ? (byte) 1 : 0;
		S.flag  = ((PSW & PSW_FLAG_POS_SN) != 0) ? (byte) 1 : 0;
		
		A.value = memory[SP.value + 1];

		SP.value += 2;
	}
	
	private void PUSH(Component... rp) {
		memory[SP.value - 1] = rp[0].value; // Push rp1 to sp - 1
		memory[SP.value - 2] = rp[1].value; // Push rp2 to sp - 2
		SP.value -= 2;
	}
	
	private void PUSH_PSW() {
		// A and PSW (formed BCD on flags , plus its filler value)

		memory[SP.value - 1] = A.value;
		
		/*
		 pos 1, 3, 5 have fixed value
		 pos 1 = 1
		 pos 3, pos 5 = 0
		*/

		/* 
		 pos 0 = carry
		 pos 2 = parity
		 pos 4 = aux carry
		 pos 6 = zero
		 pos 7 = sign
		*/

		memory[SP.value - 2] = (
			(CY.flag & PSW_FLAG_POS_CY) 	|	// place carry flag status on pos 0
			(0b10)		  					|	// place fixed value "1" on pos 1
			(P.flag  << PSW_FLAG_POS_PA)  	|	// place parity flag status on pos 2
			(AC.flag << PSW_FLAG_POS_AC) 	| 	// place aux. carry flag status on pos 4
			(Z.flag  << PSW_FLAG_POS_ZE)  	|	// place zero flag status on pos 6
			(S.flag  << PSW_FLAG_POS_SN))	| 	// place sign flag status on pos 7
			(0x0);				// set zero skipped empty positions
			
		SP.value -= 2;

	}
	
	private void RET() {
		int addr = (memory[SP.value + 1] << 8) | memory[SP.value];
		SP.value += 2;
		PC.value = addr;
	}
	
	private void STA(int hi_nib, int lo_nib) {
		int addr = (hi_nib << 8) | lo_nib;
		memory[addr] = A.value;
	}
	
	private void XCHG() {
		int hold_h = H.value;
		int hold_l = L.value;
		
		H.value = D.value;
		L.value = E.value;
		
		D.value = hold_h;
		E.value = hold_l;
	}
	
	private void XRA(int var) {
		int res = A.value ^ var;
		
		flags_BCD(var);
		CY.flag = 0;
		AC.flag = 0;
		
		A.value = res;
	}
	
	///  HL SUBROUTINES  ///
	
	private void DCR(int address) {
		int res = (memory[address] - 1);
		
		Z.flag = (res == 0) ? (byte) 1 : 0;
		S.flag = ((res & 0x80) == 0x80) ? (byte) 1 : 0; // Masking
		P.flag = parityFlag(res & 0xff);
		
		memory[address] = res & 0xff;
	}
	
	private void INR(int address) {
		int res = memory[address] + 1;
		memory[address] = res & 0xff; // ensure it only takes 8-bit
		
		flags_Arith(res);
	}
	
	private void MVI(int opcode, int address) {
		memory[address] = memory[opcode + 1];
		
		PC.value++;
	}
	
	///  FLAGS  ///
	private void flags_BCD(int result) {
		CY.flag = (result > 0xff) ? (byte) 1 : 0;
		Z.flag = (result == 0) ? (byte) 1 : 0;
		S.flag = ((result & 0x80) == 0x80) ? (byte) 1 : 0;
		P.flag = parityFlag(result & 0xff);  // ensuring only checks for 8-bit variable
		AC.flag = (result > 0x09) ? (byte) 1 : 0;
	}
	
	private void flags_Arith(int result) {
		// CY.flag = (result > 0xff) ? (byte) 1 : 0;
		Z.flag = (result == 0) ? (byte) 1 : 0;
		S.flag = ((result & 0x80) == 0x80) ? (byte) 1 : 0;
		P.flag = parityFlag(result & 0xff);  // ensuring only checks for 8-bit variable
		AC.flag = (result > 0x09) ? (byte) 1 : 0;
	}
	
	private byte parityFlag(int result) {
		int res = Integer.toBinaryString(result).replaceAll("0", "").length(); // Simple workaround to get count of flipped binary
		return (res % 2 == 0) ? (byte) 1 : 0;
	}
	
	
	///  MISC. METHODS  ///
	private void init() {
		B = new Component();
		C = new Component();
		D = new Component();
		E = new Component();
		H = new Component();
		L = new Component();
		A = new Component();
		
		PC = new Component();
		SP = new Component();
		
		Z = new Flags();
		S = new Flags();
		P = new Flags();
		CY = new Flags();
		AC = new Flags();
	}
	
	private String toHex04(int value) {
		return String.format("%04x", value);
	}
	
	private String toHex02(int value) {
		return String.format("%02x", value);
	}
}

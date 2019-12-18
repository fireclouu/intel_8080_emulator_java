import Cpu.*;

public class CpuEmulation
{
	// NOTES
	/*
	   - Use objects as parameter for function/subroutine when re-assignment needs to be performed (e.g. *someobject*.value = *somevariable*)
	  	 since there's no pointer in java
	   - strict handling of 0xff (8 bit), 0xffff (16 bit) addresses, java only offers signed data types
	*/
	
	/// OFFSET (use to correctly display memory address of ROMS that is not loaded on 0x0)
	int directAddr = Main.romAddr[0];
	
	///  DECLARE CPU COMPONENTS  ///
	public static Component B, C, D, E, H, L, A;
	public static Component PC, SP;
	public static Flags Z, S, P, CY, AC;
	
	///  MEMORY  ///
	private int[] memory;
	
	///  PSW FLAG POSITIONS  ///
	final static int 
		PSW_FLAG_POS_CY = 0b_0000_0001, // on bit pos 0 (Carry)
		PSW_FLAG_POS_PA = 0b_0000_0100, // on bit pos 2 (Parity)
		PSW_FLAG_POS_AC = 0b_0001_0000, // on bit pos 4 (Aux. carry)
		PSW_FLAG_POS_ZE = 0b_0100_0000, // on bit pos 6 (Zero)
		PSW_FLAG_POS_SN = 0b_1000_0000; // on bit pos 7 (Sign)
	
	public CpuEmulation(int[] rom) {
		init();
		memory = rom;
		System.out.println("Start emulator...\n");
		
		// TESTING PURPOSES
		TEST_OVERRIDE();
		
		// Start emulation loop
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
			
			// increment PC every calls
			PC.value++;
			
			// print instruction
			printInstruction(opcode);
			
			switch(memory[opcode]) {
				
				/////   0x00 - 0x0f   /////
				
				case 0x00:
					break; // NOP
				case 0x01:
					LXI(opcode, B, C);
					PC.value += 2;
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
					PC.value++;
					break; // MVI B, D8
				case 0x07:
					RLC();
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
					PC.value++;
					break; // MVI C, D8
				case 0x0f:
					RRC();	
					break; // RRC
					
				//////   0x10 - 0x1f   /////
					
				case 0x10:
					break; // -
				case 0x11:
					LXI(opcode, D, E);
					PC.value += 2;
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
					PC.value++;
					break; // MVI D, D8
				case 0x17:
					RAL();
					break; // RAL
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
					PC.value++;
					break; // MVI E, D8
				case 0x1f:
					RAR();
					break; // RAR
					
				//////   0x20 - 0x2f   /////
					
				case 0x20:
					break; // -
				case 0x21:
					LXI(opcode, H, L);
					PC.value += 2;
					break; // LXI H, D16
				case 0x22:
					SHLD(opcode);
					break; // SHLD adr
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
					PC.value++;
					break; // MVI H, D8
				// Case 0x27 DAA (BCD)
				case 0x28:
					break; // -
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
					PC.value++;
					break; // MVI L, D8
					
				//////   0x30 - 0x3f   /////
					
				case 0x31:
					LXI(opcode, SP);
					PC.value += 2;
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
					PC.value++;
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
					PC.value++;
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
					// System.exit(0); // terminate program
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
				case 0x88:
					ADC(B.value);
					break; // ADC B
				case 0x89:
					ADC(C.value);
					break; // ADC C
				case 0x8a:
					ADC(D.value);
					break; // ADC D
				case 0x8b:
					ADC(E.value);
					break; // ADC E
				case 0x8c:
					ADC(H.value);
					break; // ADC H
				case 0x8d:
					ADC(L.value);
					break; // ADC L
				case 0x8e:
					ADC(memory[addr]);
					break; // ADC M
				case 0x8f:
					ADC(A.value);
					break; // ADC A
					
				//////   0x90 - 0x9f   /////
					
				
				//////   0xa0 - 0xaf   /////
				
				case 0xa0:
					ANA(B.value);
					break; // ANA B
				case 0xa1:
					ANA(C.value);
					break; // ANA C
				case 0xa2:
					ANA(D.value);
					break; // ANA D
				case 0xa3:
					ANA(E.value);
					break; // ANA E
				case 0xa4:
					ANA(H.value);
					break; // ANA H
				case 0xa5:
					ANA(L.value);
					break; // ANA L
				case 0xa6:
					ANA(memory[addr]);
					break; // ANA M
				case 0xa7:
					ANA(A.value);
					break; // ANA A
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
				
				case 0xb0:
					ORA(B.value);
					break; // ORA B
				case 0xb1:
					ORA(C.value);
					break; // ORA C
				case 0xb2:
					ORA(D.value);
					break; // ORA D
				case 0xb3:
					ORA(E.value);
					break; // ORA E
				case 0xb4:
					ORA(H.value);
					break; // ORA H
				case 0xb5:
					ORA(L.value);
					break; // ORA L
				case 0xb6:
					ORA(memory[addr]);
					break; // ORA M
				case 0xb7:
					ORA(A.value);
					break; // ORA A
				case 0xb8:
					CMP(B.value);
					break; // CMP B
				case 0xb9:
					CMP(C.value);
					break; // CMP C
				case 0xba:
					CMP(D.value);
					break; // CMP D
				case 0xbb:
					CMP(E.value);
					break; // CMP E
				case 0xbc:
					CMP(H.value);
					break; // CMP H
				case 0xbd:
					CMP(L.value);
					break; // CMP L
				case 0xbe:
					CMP(memory[addr]);
					break; // CMP M
				case 0xbf:
					CMP(A.value);
					break; // CMP A
					
				//////   0xc0 - 0xcf   /////
				
				case 0xc0: 
					if (Z.flag == 0) {
						RET();
					}
					break; // RNZ
				case 0xc1:
					POP(B, C);
					break; // POP B
				case 0xc2:
					if (Z.flag == 0) {
						PC.value = (memory[opcode + 2] << 8) | memory[opcode + 1];
					} else {
						PC.value += 2;
					}
					break; // JNZ adr
				case 0xc3:
					PC.value = (memory[opcode + 2] << 8) | memory[opcode + 1];
					break; // JMP adr
				case 0xc4:
					if (Z.flag == 0) {
						CALL(opcode);
					} else {
						PC.value += 2;
					}
					break; // CNZ adr
				case 0xc5:
					PUSH(B, C);
					break; // PUSH B
				case 0xc6:
					ADD(memory[opcode + 1]);
					PC.value++;
					break; // ADI D8
				case 0xc8:
					if (Z.flag == 1) {
						RET();
					}
					break; // RZ
				case 0xc9:
					RET();
					break; // RET
				case 0xca:
					if (Z.flag == 1) {
						PC.value = (memory[opcode + 2] << 8) | memory[opcode + 1];
					} else {
						PC.value += 2;
					}
					break; // JZ adr
				case 0xcc:
					if (Z.flag == 1) {
						CALL(opcode);
					} else {
						PC.value += 2;
					}
					break; // CZ adr
				case 0xcd:
					//CALL(opcode);
					TEST_DIAG(opcode);
					break; // CALL adr
				case 0xce:
					ADC(memory[opcode + 1]);
					PC.value++;
					break;  // ACI D8
					
				//////   0xd0 - 0xdf   /////
				
				case 0xd0: 
					if (CY.flag == 0) {
						RET();
					}
					break; // RNC
				case 0xd1:
					POP(D, E);
					break; // POP D
				case 0xd2:
					if (CY.flag == 0) {
						PC.value = (memory[opcode + 2] << 8) | memory[opcode + 1];
					} else {
						PC.value += 2;
					}
					break; // JNC adr
				case 0xd3:
					PC.value++;
					break; // OUT D8
				case 0xd4:
					if (CY.flag == 0) {
						CALL(opcode);
					} else {
						PC.value += 2;
					}
					break; // CNC adr
				case 0xd5:
					PUSH(D, E);
					break; // PUSH D
				case 0xd6:
					SUB(memory[opcode + 1]);
					PC.value++;
					break; // SUI D8
				case 0xd8: 
					if (CY.flag == 1) {
						RET();
					}
					break; // RC
				case 0xda:
					if (CY.flag == 1) {
						PC.value = (memory[opcode + 2] << 8) | memory[opcode + 1];
					} else {
						PC.value += 2;
					}
					break; // JC adr
				case 0xdc:
					if (CY.flag == 1) {
						CALL(opcode);
					} else {
						PC.value += 2;
					}
					break; // CC adr
				case 0xde:
					SBB(memory[opcode + 1]);
					PC.value++;
					break; // SBI D8
					
				//////   0xe0 - 0xef   /////
				
				case 0xe0: 
					if (P.flag == 0) {
						RET();
					}
					break; // RPO
				case 0xe1:
					POP(H, L);
					break; // POP H
				case 0xe2:
					if (P.flag == 0) {
						PC.value = (memory[opcode + 2] << 8) | memory[opcode + 1];
					} else {
						PC.value += 2;
					}
					break; // JPO adr
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
					ANA(memory[opcode + 1]);
					PC.value++;
					break; // ANI D8
				case 0xe8: 
					if (P.flag == 1) {
						RET();
					}
					break; // RPE
				case 0xea:
					if (P.flag == 1) {
						PC.value = (memory[opcode + 2] << 8) | memory[opcode + 1];
					} else {
						PC.value += 2;
					}
					break; // JPE adr
				case 0xeb:
					XCHG();
					break; // XCHG (HL to DE vice-versa)
				case 0xec:
					if (P.flag == 1) {
						CALL(opcode);
					} else {
						PC.value += 2;
					}
					break; // CPE adr
				case 0xee:
					XRA(memory[opcode + 1]);
					PC.value++;
					break; // XRI D8
					
				//////   0xf0 - 0xff   /////
				
				case 0xf0: 
					if (S.flag == 0) {
						RET();
					}
					break; // RP
				case 0xf1:
					POP_PSW();
					break; // POP PSW
				case 0xf2:
					if (S.flag == 0) {
						PC.value = (memory[opcode + 2] << 8) | memory[opcode + 1];
					} else {
						PC.value += 2;
					}
					break; // JP adr
				case 0xf4:
					if (S.flag == 0) {
						CALL(opcode);
					} else {
						PC.value += 2;
					}
					break; // CP adr
				case 0xf5:
					PUSH_PSW();
					break; // PUSH PSW
				case 0xf6:
					ORA(memory[opcode + 1]);
					PC.value++;
					break; // ORI D8
				case 0xf8: 
					if (S.flag == 1) {
						RET();
					}
					break; // RM
				case 0xfa:
					if (S.flag == 1) {
						PC.value = (memory[opcode + 2] << 8) | memory[opcode + 1];
					} else {
						PC.value += 2;
					}
					break; // JM adr	
				case 0xfb:
					// TODO: needs to implement, when interrupts added
					break; // EI
				case 0xfc:
					if (S.flag == 1) {
						CALL(opcode);
					} else {
						PC.value += 2;
					}
					break; // CM adr
				case 0xfe:
					CMP(memory[opcode + 1]);
					PC.value++;
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
				inst = "MVI E, #" + toHex02(memory[opcode + 1]);
				break;
			case 0x1f:
				inst = "RAR";
				break;
			case 0x20:
				inst = " - ";
				break;
			case 0x21:
				inst = "LXI H, #" + toHex02(memory[opcode + 2]) + toHex02(memory[opcode + 1]);
				break;
			case 0x22:
				inst = "SHLD #$" + toHex02(memory[opcode + 2]) + toHex02(memory[opcode + 1]);
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
			case 0x28:
				inst = " - ";
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
				inst = "JNZ $" + toHex04((memory[opcode + 2] << 8) | memory[opcode + 1]);
				break;
			case 0xc3:
				inst = "JMP #$" + toHex04((memory[opcode + 2] << 8) | memory[opcode + 1]);
				break;
			case 0xc4:
				inst = "CNZ #$" + toHex04((memory[opcode + 2] << 8) | memory[opcode + 1]);
				break;
			case 0xc5:
				inst = "PUSH B";
				break;
			case 0xc6:
				inst = "ADI #" + toHex02(memory[opcode + 1]);
				break;
			case 0xc8:
				inst = "RZ";
				break;
			case 0xc9:
				inst = "RET";
				break;
			case 0xca:
				inst = "JZ $" + toHex04((memory[opcode + 2] << 8) | memory[opcode + 1]);
				break;
			case 0xcc:
				inst = "CZ #$" + toHex04((memory[opcode + 2] << 8) | memory[opcode + 1]);
				break;
			case 0xcd:
				inst = "CALL $" + toHex04((memory[opcode + 2] << 8) | memory[opcode + 1]);
				break;
			case 0xce:
				inst = "ACI #" + toHex02(memory[opcode + 1]);
				break;
				
			/////     0xd0 - 0xdf     /////
			
			case 0xd0:
				inst = "RNC";
				break;
			case 0xd1:
				inst = "POP D";
				break;
			case 0xd2:
				inst = "JNC #$" + toHex04((memory[opcode + 2] << 8) | memory[opcode + 1]);
				break;
			case 0xd3:
				inst = "OUT #" + toHex02(memory[opcode + 1]);
				break; // PORT?
			case 0xd4:
				inst = "CNC #$" + toHex04((memory[opcode + 2] << 8) | memory[opcode + 1]);
				break;
			case 0xd5:
				inst = "PUSH D";
				break;
			case 0xd6:
				inst = "SUI #" + toHex02(memory[opcode + 1]);
				break;
			case 0xd8:
				inst = "RC";
				break;
			case 0xda:
				inst = "JC #$" + toHex04((memory[opcode + 2] << 8) | memory[opcode + 1]);
				break;
			case 0xdc:
				inst = "CC #$" + toHex04((memory[opcode + 2] << 8) | memory[opcode + 1]);
				break;
			case 0xde:
				inst = "SBI #" + toHex02(memory[opcode + 1]);
				break;
				
			/////     0xe0 - 0xef     /////
			
			case 0xe0:
				inst = "RPO";
				break;
			case 0xe1:
				inst = "POP H";
				break;
			case 0xe2:
				inst = "JPO #$" + toHex04((memory[opcode + 2] << 8) | memory[opcode + 1]);
				break;
			case 0xe4:
				inst = "CPO $" + toHex04((memory[opcode + 2] << 8) | memory[opcode + 1]);
				break;
			case 0xe5:
				inst = "PUSH H";
				break;
			case 0xe6:
				inst = "ANI #" + toHex02(memory[opcode + 1]);
				break;
			case 0xe8:
				inst = "RPE";
				break;
			case 0xea:
				inst = "JPE #$" + toHex04((memory[opcode + 2] << 8) | memory[opcode + 1]);
				break;
			case 0xeb:
				inst = "XCHG";
				break;
			case 0xec:
				inst = "CPE #$" + toHex04((memory[opcode + 2] << 8) | memory[opcode + 1]);
				break;
			case 0xee:
 				inst = "XRI #" + toHex02(memory[opcode + 1]);
				break;
				
			/////     0xf0 - 0xff     /////
			
			case 0xf0:
				inst = "RP";
				break;
			case 0xf1:
				inst = "POP PSW";
				break;
			case 0xf2:
				inst = "JP #$" + toHex04((memory[opcode + 2] << 8) | memory[opcode + 1]);
				break;
			case 0xf4:
				inst = "CP #$" + toHex04((memory[opcode + 2] << 8) | memory[opcode + 1]);
				break;
			case 0xf5:
				inst = "PUSH PSW";
				break;
			case 0xf6:
				inst = "ORI #" + toHex02(memory[opcode + 1]);
				break;
			case 0xf8:
				inst = "RM";
				break;
			case 0xfa:
				inst = "JM #$" + toHex04((memory[opcode + 2] << 8) | memory[opcode + 1]);
				break;
			case 0xfb:
				inst = "EI (unimplemented)"; // Skipped
				break;
			case 0xfc:
				inst = "CM #$" + toHex04((memory[opcode + 2] << 8) | memory[opcode + 1]);
				break;
			case 0xfe:
 				inst = "CPI #" + toHex02(memory[opcode + 1]);
				break;
			default:
				inst = "" + toHex02(memory[opcode]) + " is not implemented!";
				System.out.println(inst);
				return;
				
		}
		
		System.out.println(
			"B: " + toHex02(B.value) + " | C: " + toHex02(C.value) + " | D: " + toHex02(D.value) +
			" | E: " + toHex02(E.value) + " | H: " + toHex02(H.value) + " | L: " + toHex02(L.value) +
			" | M: " + toHex02(memory[memory[H.value<<8] | memory[L.value]])  + " | A: " + toHex02(A.value));
			
		System.out.println(
			"CY: " + CY.flag + " | ZR: " + Z.flag + " | PA: " + P.flag + " | SN: " + S.flag /*+ " | AC: " + AC.flag*/);
		
		System.out.println("SP: " + toHex04(SP.value) + " | (" + toHex02(memory[opcode]) + ") | FILE_ADDR: " + toHex04(opcode - this.directAddr) + " | PC: " + toHex04(opcode) + "  " + inst);
		
		System.out.println();
	}
	
	// TODO: verify aux. cary, failing
	///  SUBROUTINES  ///
	private void ADC(int var) {
		int res = (A.value + var) + CY.flag;
	
		flags_BCD(res);
		
		A.value = res & 0xff;
	}
	
	private void ADD(int var) {
		int res = A.value + var;
		
		flags_BCD(res);
		
		A.value = res & 0xff;
	}
	
	private void ANA(int var) {
		A.value = (A.value & var);
		
		flags_zsp(A.value);
		
		CY.flag = 0;
	}
	
	private void CALL(int opcode) {
		int nextAddr = opcode + 3;
		memory[SP.value - 1] = ((nextAddr >> 8) & 0xff);
		memory[SP.value - 2] = (nextAddr & 0xff);
		SP.value -= 2;
		
		PC.value = (memory[opcode + 2] << 8) | memory[opcode + 1];
	}
	
	private void CMP(int var) {
		// (two's) complement — defined also as "another set" e.g. another set of binary 1 is binary 0!
		int res = A.value + ((~var + 1) & 0xff);
		
		Z.flag = ((res & 0xff) == 0) ? (byte) 1 : 0;
		S.flag = ((res & 0x80) == 0x80) ? (byte) 1 : 0;
		P.flag = parityFlag(res & 0xff);  // ensuring only checks for 8-bit variable
		CY.flag = (var > A.value) ? 1: (byte) 0; // minuend greater than subtrahend will likely result to overflow of 0xff (borrowing)
		// AC.flag = -1;
	}
	
	private void DAD(Component... rp) {
		int HL = (H.value << 8) | L.value; // addr = 16bit
		
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
		reg.value = res & 0xff;
		
		flags_zsp(res);
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
		reg.value = res & 0xff;
		
		flags_zsp(res);
		
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
			rp[0].value = memory[opcode + 2];
			rp[1].value = memory[opcode + 1];
		} else {
			// 16-bit variable (SP)
			rp[0].value = ((memory[opcode + 2] << 8) | memory[opcode + 1]);
		}
	}
	
	private void MVI(int opcode, Component reg) {
		reg.value = memory[opcode + 1];
	}
	
	private void ORA(int var) {
		int res = A.value | var;
		
		flags_zsp(res);
		CY.flag = 0; // fixed value
		
		A.value = res;
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
		// A and PSW (formed binary value via flags , plus its filler value)

		memory[SP.value - 1] = A.value;
		
		/*
			pos 1, 3, 5 have fixed value
			pos 1 = 1
			pos 3 and pos 5 = 0
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
			(S.flag  << PSW_FLAG_POS_SN) )	| 	// place sign flag status on pos 7
			(0x0);								// set zero to skipped empty positions
			
		SP.value -= 2;

	}
	
	private void RAL() {
		int tmp = A.value;
		A.value = (tmp << 1) | CY.flag;
		CY.flag = (byte) (tmp >> 7);
	}
	
	private void RAR() {
		int tmp = A.value;
		int mask = ((A.value >>> 7) << 7); // Right shift to 7, left shift to 7 (0x80 or 0x0)
		A.value = (tmp >> 1) | mask;
		CY.flag = (byte) (tmp & 0x1);
	}
	
	private void RET() {
		int addr = (memory[SP.value + 1] << 8) | memory[SP.value];
		SP.value += 2;
		PC.value = addr;
	}
	
	private void RLC() {
		int tmp = A.value;
		A.value = (tmp << 1) | (tmp >>> 7); // Rotate left shift, then rotate 7 right shift, flipping bit 0 if 1 (carry)	
		CY.flag = ((tmp >>> 7) > 0) ? (byte) 1 : 0; // carry if bit 7 is 1
	}
	
	private void RRC() {
		int tmp = A.value;
		A.value = (tmp >>> 1) | (tmp << 7); // Rotate right shift (zero fill), then rotate 7 right shift, flipping bit 0 if 1 (carry)
		CY.flag = (byte) (tmp & 0x1); // verify
	}
	
	private void SHLD(int opcode) {
		int addr = (memory[opcode + 2]) << 8 | memory[opcode + 1];
		memory[addr + 1] = H.value;
		memory[addr] = L.value;
	}
	
	private void SBB(int var) {
		int res = ( A.value + ((~var + 1) & 0xff) ) + ((~CY.flag + 1) & 0xff);
		
		Z.flag = ((res & 0xff) == 0) ? (byte) 1 : 0;
		S.flag = ((res & 0x80) == 0x80) ? (byte) 1 : 0;
		P.flag = parityFlag(res & 0xff);
		CY.flag = (var > A.value) ? 1: (byte) 0; // minuend greater than subtrahend will likely result to overflow of 0xff (borrowing)
		// AC.flag = -1; // NULL 

		A.value = res & 0xff;
	}
	
	private void STA(int hi_nib, int lo_nib) {
		int addr = (hi_nib << 8) | lo_nib;
		memory[addr] = A.value;
	}
	
	private void SUB(int var) {
		int res = A.value + ((~var + 1) & 0xff);
		
		Z.flag = ((res & 0xff) == 0) ? (byte) 1 : 0;
		S.flag = ((res & 0x80) == 0x80) ? (byte) 1 : 0;
		P.flag = parityFlag(res & 0xff);
		CY.flag = (var > A.value) ? 1: (byte) 0; // minuend greater than subtrahend will likely result to overflow of 0xff (borrowing)
		// AC.flag = -1; // NULL
		
		A.value = res & 0xff;
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
		
		flags_zsp(res);
		CY.flag = 0;
		// AC.flag = 0; // fixed?
		
		A.value = res;
	}
	
	///  HL SUBROUTINES  ///
	
	private void DCR(int address) {
		int res = (memory[address] - 1);
		
		Z.flag = ((res & 0xff)== 0) ? (byte) 1 : 0;
		S.flag = ((res & 0x80) == 0x80) ? (byte) 1 : 0;
		P.flag = parityFlag(res & 0xff);
		
		memory[address] = res & 0xff;
	}
	
	private void INR(int address) {
		int res = memory[address] + 1;
		memory[address] = res & 0xff; // ensure it only takes 8-bit
		
		flags_zsp(res);
	}
	
	private void MVI(int opcode, int address) {
		memory[address] = memory[opcode + 1];
	}
	
	///  FLAGS  ///
	private void flags_BCD(int result) {
		CY.flag = (result > 0xff) ? (byte) 1 : 0;
		Z.flag = ((result & 0xff) == 0) ? (byte) 1 : 0;
		S.flag = ((result & 0x80) == 0x80) ? (byte) 1 : 0;
		P.flag = parityFlag(result & 0xff);  // ensuring only checks for 8-bit variable
		// AC.flag = -1; // NULL
	}
	
	private void flags_zsp(int result) {
		Z.flag = ((result & 0xff)== 0) ? (byte) 1 : 0;
		S.flag = ((result & 0x80) == 0x80) ? (byte) 1 : 0;
		P.flag = parityFlag(result & 0xff);  // ensuring only checks for 8-bit variable
		// AC.flag = -1; // NULL
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
	
	// CPU OVERRIDE
	private void TEST_OVERRIDE() {
		// Direct PC to loaded address
		PC.value = this.directAddr;
		
		// fix sp
		memory[368] = 0x07;
		
		// SKIP DAA inst
		memory[0x59c] = 0xc3;
		memory[0x59d] = 0xc2;
		memory[0x59e] = 0x05;
	}
	
	private void TEST_DIAG(int opcode) {
		// SOURCE: kpmiller — Full 8080 emulation
		if (5 == ((memory[opcode + 2] << 8) | memory[opcode + 1])) {
			
			if (C.value == 9) {
				int offset = (D.value << 8) | (E.value);
				int str = offset + 3;  //skip the prefix bytes
				char read;
				
				while ((read = (char)memory[str]) != '$') {
					System.out.print(read);
					str++;
				}
				
				System.out.println();
				
			} else if (C.value == 2) {
				System.out.println ("print char routine called\n");
			}
			
		} else if (0 ==  ((memory[opcode + 2] << 8) | memory[opcode + 1])) {
			// System.exit(0);
			System.out.println("-- System called for exit --");
		} else {
			int  ret = PC.value + 2;
			memory[SP.value - 1] = (ret >> 8) & 0xff;
			memory[SP.value - 2] = (ret & 0xff);
			SP.value=(SP.value - 2) & 0xffff;
			PC.value= (memory[opcode + 2] << 8) | memory[opcode + 1];
		}
	}
}

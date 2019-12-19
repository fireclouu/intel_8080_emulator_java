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
	static int directAddr = Main.romAddr[0];
	
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
		
		
	///  MISC  ///
	final int MAX_INT = 2_147_483_647;
	
	public CpuEmulation(int[] rom) {
		init();
		memory = rom;
		System.out.println("Start emulator...\n");
		
		// TESTING PURPOSES
		AUTO_TEST();
		
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
			PrintComponent.printInstruction(opcode, memory, false);
			
			/*if (PrintComponent.exec_count == 20) {
				PAUSE_THREAD(MAX_INT);
			}*/
			
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
					PC.value += 2;
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
				case 0x2a:
					LHLD(opcode);
					PC.value += 2;
					break; // LHLD adr
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
				case 0x2f:
					A.value = (~A.value & 0xff);
					break; // CMA
					
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
				case 0x37:
					CY.flag = 1;
					break; // STC
				case 0x38:
					break; // -
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
				case 0x3c:
					INR(A);
					break; // INR A
				case 0x3d:
					DCR(A);
					break; // DCR A
				case 0x3e:
					MVI(opcode, A);
					PC.value++;
					break; // MVI A, D8
				case 0x3f:
					CY.flag = (CY.flag == 1) ? (byte) 0 : 1;
					break; // CMC
					
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
					System.out.println("HLT CALLED!");
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
				
				case 0x90:
					SUB (B.value);
					break; // SUB B
				case 0x91:
					SUB (C.value);
					break; // SUB C
				case 0x92:
					SUB (D.value);
					break; // SUB D
				case 0x93:
					SUB (E.value);
					break; // SUB E
				case 0x94:
					SUB (H.value);
					break; // SUB H
				case 0x95:
					SUB (L.value);
					break; // SUB L
				case 0x96:
					SUB (memory[addr]);
					break; // SUB M
				case 0x97:
					SUB (A.value);
					break; // SUB A
				case 0x98:
					SBB (B.value);
					break; // SBB B
				case 0x99:
					SBB (C.value);
					break; // SBB C
				case 0x9a:
					SBB (D.value);
					break; // SBB D
				case 0x9b:
					SBB (E.value);
					break; // SBB E
				case 0x9c:
					SBB (H.value);
					break; // SBB H
				case 0x9d:
					SBB (L.value);
					break; // SBB L
				case 0x9e:
					SBB (memory[addr]);
					break; // SBB M
				case 0x9f:
					SBB (A.value);
					break; // SBB A
				
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
					
				// case 0xc7  // RST 0
				
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
				case 0xcb:
					break; // -
				case 0xcc:
					if (Z.flag == 1) {
						CALL(opcode);
					} else {
						PC.value += 2;
					}
					break; // CZ adr
				case 0xcd:
					// CALL(opcode);
					TEST_DIAG(opcode);
					break; // CALL adr
				case 0xce:
					ADC(memory[opcode + 1]);
					PC.value++;
					break;  // ACI D8
					
				// case 0xcf  // RST 1
					
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
					
				// case 0xd7  // RST 2
					
				case 0xd8: 
					if (CY.flag == 1) {
						RET();
					}
					break; // RC
				case 0xd9:
					break; // -
				case 0xda:
					if (CY.flag == 1) {
						PC.value = (memory[opcode + 2] << 8) | memory[opcode + 1];
					} else {
						PC.value += 2;
					}
					break; // JC adr
					
				// case 0xdb  // IN D8 (PC + 1) special
					
				case 0xdc:
					if (CY.flag == 1) {
						CALL(opcode);
					} else {
						PC.value += 2;
					}
					break; // CC adr
				case 0xdd:
					break; // -
				case 0xde:
					SBB(memory[opcode + 1]);
					PC.value++;
					break; // SBI D8
					
				// case 0xdf  // RST 3
					
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
				case 0xe3:
					XTHL();
					break; // XTHL
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
					
				// case 0xe7 // RST 4
					
				case 0xe8: 
					if (P.flag == 1) {
						RET();
					}
					break; // RPE
				case 0xe9:
					PC.value = addr;
					break; // PCHL
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
				case 0xed:
					break; // -
				case 0xee:
					XRA(memory[opcode + 1]);
					PC.value++;
					break; // XRI D8
					
				// case 0xef // RST 5
					
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
					
				// case 0xf3  // DI special
					
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
					
				// case 0xf7 // RST 6
					
				case 0xf8: 
					if (S.flag == 1) {
						RET();
					}
					break; // RM
				case 0xf9:
					SPHL(addr);
					break; // SPHL
				case 0xfa:
					if (S.flag == 1) {
						PC.value = (memory[opcode + 2] << 8) | memory[opcode + 1];
					} else {
						PC.value += 2;
					}
					break; // JM adr	
					
					
				case 0xfb:
					// TODO: needs to implement, when interrupts added
					break; // EI (special)
					
					
				case 0xfc:
					if (S.flag == 1) {
						CALL(opcode);
					} else {
						PC.value += 2;
					}
					break; // CM adr
				case 0xfd:
					break; // -
				case 0xfe:
					CMP(memory[opcode + 1]);
					PC.value++;
					break; // CPI D8
					
				// case 0xff // RST 7
					
				default:
					return;
			}
		}
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
		memory[ (SP.value - 1) & 0xffff ] = ((nextAddr >> 8) & 0xff);
		memory[ (SP.value - 2) & 0xffff ] = (nextAddr & 0xff);
		SP.value = (SP.value - 2) & 0xffff;
		
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
			// 0 - 1 , in java, will result to -1 instead of 255 or 0xff, so it must be ANDed to 0xff
			if ((rp[1].value & 0xff) == 0xff) { 
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
	
	private void LHLD(int opcode) {
		H.value = memory[opcode + 2];
		L.value = memory[opcode + 1];
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
		rp[0].value = memory[ (SP.value + 1) & 0xffff ];
		SP.value = (SP.value + 2) & 0xffff;
	}
	
	private void POP_PSW() {
		int PSW = memory[SP.value];
		
		CY.flag = ((PSW & PSW_FLAG_POS_CY) != 0) ? (byte) 1 : 0;
		P.flag  = ((PSW & PSW_FLAG_POS_PA) != 0) ? (byte) 1 : 0;
		AC.flag = ((PSW & PSW_FLAG_POS_AC) != 0) ? (byte) 1 : 0;
		Z.flag  = ((PSW & PSW_FLAG_POS_ZE) != 0) ? (byte) 1 : 0;
		S.flag  = ((PSW & PSW_FLAG_POS_SN) != 0) ? (byte) 1 : 0;
		
		A.value = memory[ (SP.value + 1) & 0xffff ];

		SP.value = (SP.value + 2) & 0xffff;
	}
	
	private void PUSH(Component... rp) {
		memory[(SP.value - 1) & 0xffff] = rp[0].value; // Push rp1 to sp - 1
		memory[(SP.value - 2) & 0xffff] = rp[1].value; // Push rp2 to sp - 2
		SP.value = (SP.value - 2) & 0xffff;
	}
	
	private void PUSH_PSW() {
		// A and PSW (formed binary value via flags , plus its filler value)
		
		memory[ (SP.value - 1) & 0xffff ] = A.value;
		
		// prepare variable higher than 0xff, but with 0's in bit 0-7
		// this way, it serves as flags' default state waiting to be flipped, like a template
		// also helps to retain flags proper positioning
		int PSW = 0x100;
		
		// skip pos 5 and 3, it does not need to be flipped since it is by default, a 0 value
		PSW =
			(S.flag     <<  7)  |   // place sign flag status on pos 7
			(Z.flag     <<  6)  |   // place zero flag status on pos 6
			(AC.flag    <<  4)  |   // place aux. carry flag status on pos 4
			(P.flag     <<  2)  |   // place parity flag status on pos 2
			(1          <<  1)  |   // place fixed value "1" on pos 1
			(CY.flag         )  ;   // place carry flag status on pos 0
		
		memory[ (SP.value - 2) & 0xffff ] = (PSW & 0xff); // cut to 8 bit after
		
		SP.value = (SP.value - 2) & 0xffff;
	}
	
	private void RAL() {
		int res = (A.value + CY.flag) << 1; // verify
		CY.flag = (res > 0xff) ? (byte) 1 : 0;
		A.value = res & 0xff;
	}
	
	private void RAR() {
		int res = (A.value + CY.flag) >> 1; // verify
		CY.flag = (byte) (A.value & 0x1); // leftover bit 0 as carry
		A.value = (res | CY.flag) & 0xff;
	}
	
	private void RET() {
		int addr = (memory[ (SP.value + 1) & 0xffff ] << 8) | memory[SP.value];
		SP.value = (SP.value + 2) & 0xffff;
		PC.value = addr;
	}
	
	private void RLC() {
		int res = (A.value << 1); // Rotate left shift
		CY.flag = (res > 0xff) ? (byte) 1 : 0; // normal carry check
		A.value = (res + CY.flag) & 0xff; // rotated value plus its carry flag 
	}
	
	private void RRC() {
		int res = (A.value >>> 1); // Rotate right shift (zero fill)
		CY.flag = (byte) (A.value & 0x1); // leftover bit 0 as carry
		A.value = (res | (CY.flag << 7)) & 0xff; // update Accumulator with rotated value with its carry flag leftmost bit (0xff)
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
	
	private void SHLD(int opcode) {
		int addr = (memory[opcode + 2] << 8) | memory[opcode + 1];
		
		memory[addr + 1] = H.value;
		memory[addr] = L.value;
	}
	
	private void SPHL(int address) {
		SP.value = address;
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
		// SWAP H and D
		H.value = H.value + D.value;
		D.value = H.value - D.value;
		H.value = H.value - D.value;
		
		// SWAP L and E
		L.value = L.value + E.value;
		E.value = L.value - E.value;
		L.value = L.value - E.value;
	}
	
	private void XRA(int var) {
		int res = A.value ^ var;
		
		flags_zsp(res);
		CY.flag = 0;
		// AC.flag = 0; // fixed?
		
		A.value = res;
	}
	
	private void XTHL() {
		// SWAP H and Top + 1  SP (under of top stack)
		H.value = H.value + memory[ (SP.value + 1) & 0xffff ];
		memory[ (SP.value + 1) & 0xffff ] = H.value - memory[ (SP.value + 1) & 0xffff ];
		H.value = H.value - memory[ (SP.value + 1) & 0xffff ];

		// SWAP L and Top SP (top stack)
		L.value = L.value + memory[SP.value];
		memory[SP.value] = L.value - memory[SP.value];
		L.value = L.value - memory[SP.value];
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
	
	// CPU OVERRIDE
	private void AUTO_TEST() {
		switch (Main.romName[0]) {
			case "cpudiag.asm":
				TEST_OVERRIDE_CPUDIAG();
				break;
			case "8080EX1.COM":
				TEST_OVERRIDE_EX1();
				break;
		}
	}
	
	private void TEST_OVERRIDE_CPUDIAG() {
		// Direct PC to loaded address
		PC.value = this.directAddr;
		
		// fix sp
		memory[368] = 0x07;
		
		// SKIP DAA inst
		memory[0x59c] = 0xc3;
		memory[0x59d] = 0xc2;
		memory[0x59e] = 0x05;
	}
	
	private void TEST_OVERRIDE_EX1() {
		PC.value = this.directAddr;
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
				PAUSE_THREAD(1000);
			}
 		else if (C.value == 2) {
				System.out.println ("print char routine called\n");
			}
			
		} else if (0 ==  ((memory[opcode + 2] << 8) | memory[opcode + 1])) {
			// System.exit(0);
			System.out.println("-- System called for exit --");
		} else {
			int  ret = PC.value + 2;
			memory[ (SP.value - 1) & 0xffff ] = (ret >> 8) & 0xff;
			memory[ (SP.value - 2) & 0xffff ] = (ret & 0xff);
			SP.value=(SP.value - 2) & 0xffff;
			PC.value= (memory[opcode + 2] << 8) | memory[opcode + 1];
		}
	}
	
	private void PAUSE_THREAD(int mills) {
		try {
			Thread.sleep(mills);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

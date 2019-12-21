
public class Interpreter
{
	/// NOTES
	/*
		- strict handling of 0xff (8 bit), 0xffff (16 bit) addresses, java only offers signed data types
	*/
	 
 	/// CPU
	private CpuComponents cpu;
	
	/// OFFSET (use to correctly display memory address of ROMS that is not loaded on array 0)
	public static int realAddr = Main.romAddr[0];
	
	/// CONSTRUCTOR
	public Interpreter(short memory[], CpuComponents cpu) {
		init(memory, cpu);
	}
	
	/// MAIN EMULATION
	public byte emulate8080(int pc) {
		// temporary containers
		int res;
		
		// cpu cycle
		byte cycle = 1;
		
		// opcode
		int opcode = pc;

		// HL (M)
		int addr = ((cpu.H << 8) | cpu.L);

		// increment PC every calls
		cpu.PC++;

		switch (cpu.memory[pc]) {

				/////   0x00 - 0x0f   /////

			case 0x00:
				break; // NOP
			case 0x01:
				cpu.B = cpu.memory[opcode + 2];
				cpu.C = cpu.memory[opcode + 1];
				cpu.PC += 2;
				cycle = 3;
				break; // LXI B, D16
			case 0x02:
				STA(cpu.B, cpu.C);
				break; // STAX B
			case 0x03:
			{
				cpu.C++;
				if (cpu.C > 0xff) {
					cpu.B++;
				}
				cpu.B &= 0xff;
				cpu.C &= 0xff;
			}
				break; // INX B
			case 0x04:
				res = cpu.B + 1;
				cpu.B = (short) (res & 0xff);
				flags_zsp(res);
				break; // INR B
			case 0x05:
				res = cpu.B - 1;
				cpu.B = (short) (res & 0xff);
				flags_zsp(res);
				break; // DCR B
			case 0x06:
				cpu.B = cpu.memory[opcode + 1];
				cpu.PC++;
				cycle = 2;
				break; // MVI B, D8
			case 0x07:
				RLC();
				break; // RLC
			case 0x08:
				break; // -
			case 0x09:
				DAD(cpu.B, cpu.C);
				break; //DAD B
			case 0x0a:
				LDA(cpu.B, cpu.C);
				break; // LDAX B
			case 0x0b:
			{
				cpu.C--;
				if ((cpu.C & 0xff) == 0xff) { 
					cpu.B--;
				}
				cpu.C &= 0xff;
				cpu.B &= 0xff;
			}
				break; // DCX B
			case 0x0c:
				res = cpu.C + 1;
				cpu.C = (short) (res & 0xff);
				flags_zsp(res);
				break; // INR C
			case 0x0d:
				res = cpu.C - 1;
				cpu.C = (short) (res & 0xff);
				flags_zsp(res);
				break; // DCR C
			case 0x0e:
				cpu.C = cpu.memory[opcode + 1];
				cpu.PC++;
				cycle = 2;
				break; // MVI C, D8
			case 0x0f:
				RRC();	
				break; // RRC

				//////   0x10 - 0x1f   /////

			case 0x10:
				break; // -
			case 0x11:
				cpu.D = cpu.memory[opcode + 2];
				cpu.E = cpu.memory[opcode + 1];
				cpu.PC += 2;
				cycle = 3;
				break; // LXI D, D16
			case 0x12:
				STA(cpu.D, cpu.E);
				break; // STAX D
			case 0x13:
			{
				cpu.E++;
				if (cpu.E > 0xff) {
					cpu.D++;
				}
				cpu.D &= 0xff;
				cpu.E &= 0xff;
			}
				break; // INX D
			case 0x14:
				res = cpu.D + 1;
				cpu.D = (short) (res & 0xff);
				flags_zsp(res);
				break; // INR D
			case 0x15:
				res = cpu.D - 1;
				cpu.D = (short) (res & 0xff);
				flags_zsp(res);
				break; // DCR D
			case 0x16:
				cpu.D = cpu.memory[opcode + 1];
				cpu.PC++;
				cycle = 2;
				break; // MVI D, D8
			case 0x17:
				RAL();
				break; // RAL
			case 0x18:
				break; // -
			case 0x19:
				DAD(cpu.D, cpu.E);
				break; //DAD D
			case 0x1a:
				LDA(cpu.D, cpu.E);
				break; // LDAX D
			case 0x1b:
			{
				cpu.E--;
				if ((cpu.E & 0xff) == 0xff) { 
					cpu.D--;
				}
				cpu.E &= 0xff;
				cpu.D &= 0xff;
			}
				break; // DCX D
			case 0x1c:
				res = cpu.E + 1;
				cpu.E = (short) (res & 0xff);
				flags_zsp(res);
				break; // INR E
			case 0x1d:
				res = cpu.E - 1;
				cpu.E = (short) (res & 0xff);
				flags_zsp(res);
				break; // DCR E
			case 0x1e:
				cpu.E = cpu.memory[opcode + 1];
				cpu.PC++;
				cycle = 2;
				break; // MVI E, D8
			case 0x1f:
				RAR();
				break; // RAR

				//////   0x20 - 0x2f   /////

			case 0x20:
				break; // -
			case 0x21:
				cpu.H = cpu.memory[opcode + 2];
				cpu.L = cpu.memory[opcode + 1];
				cpu.PC += 2;
				cycle = 3;
				break; // LXI H, D16
			case 0x22:
				SHLD(opcode);
				cpu.PC += 2;
				cycle = 3;
				break; // SHLD adr
			case 0x23:
			{
				cpu.L++;
				if (cpu.L > 0xff) {
					cpu.H++;
				}
				cpu.H &= 0xff;
				cpu.L &= 0xff;
			}
				break; // INX H
			case 0x24:
				res = cpu.H + 1;
				cpu.H = (short) (res & 0xff);
				flags_zsp(res);
				break; // INR H
			case 0x25:
				res = cpu.H - 1;
				cpu.H = (short) (res & 0xff);
				flags_zsp(res);
				break; // DCR H
			case 0x26:
				cpu.H = cpu.memory[opcode + 1];
				cpu.PC++;
				cycle = 2;
				break; // MVI H, D8

				// Case 0x27 DAA (BCD)

			case 0x28:
				break; // -
			case 0x29:
				DAD(cpu.H, cpu.L);
				break; //DAD H
			case 0x2a:
				LHLD(opcode);
				cpu.PC += 2;
				cycle = 3;
				break; // LHLD adr
			case 0x2b:
			{
				cpu.L--;
				if ((cpu.L & 0xff) == 0xff) { 
					cpu.H--;
				}
				cpu.L &= 0xff;
				cpu.H &= 0xff;
			}	
				break; // DCX H
			case 0x2c:
				res = cpu.L + 1;
				cpu.L = (short) (res & 0xff);
				flags_zsp(res);
				break; // INR L
			case 0x2d:
				res = cpu.L - 1;
				cpu.L = (short) (res & 0xff);
				flags_zsp(res);
				break; // DCR L
			case 0x2e:
				cpu.L = cpu.memory[opcode + 1];
				cpu.PC++;
				cycle = 2;
				break; // MVI L, D8
			case 0x2f:
				cpu.A = (short) ((~cpu.A & 0xff));
				break; // CMA

				//////   0x30 - 0x3f   /////

			case 0x31:
				cpu.SP = ((cpu.memory[opcode + 2] << 8) | cpu.memory[opcode + 1]);
				cpu.PC += 2;
				cycle = 3;
				break; // LXI SP, D16
			case 0x32:
				STA(cpu.memory[opcode + 2], cpu.memory[opcode + 1]);
				cpu.PC += 2;
				cycle = 3;
				break; // STA adr
			case 0x33:
				cpu.SP = (short) ((cpu.SP + 1) & 0xffff);
				break; // INX SP
			case 0x34:
				res = cpu.memory[addr] + 1;
				cpu.memory[addr] = (short) (res & 0xff);
				flags_zsp(res);
				break; // INR M
			case 0x35:
				res = cpu.memory[addr] - 1;
				cpu.memory[addr] = (short) (res & 0xff);
				flags_zsp(res);
				break; // DCR M
			case 0x36:
				cpu.memory[addr] = cpu.memory[opcode + 1];
				cpu.PC++;
				cycle = 2;
				break; // MVI M, D8
			case 0x37:
				cpu.cc.CY = 1;
				break; // STC
			case 0x38:
				break; // -
			case 0x39:
				DAD(cpu.SP);
				break; //DAD SP
			case 0x3a:
				LDA(cpu.memory[opcode + 2], cpu.memory[opcode + 1]);
				cpu.PC += 2;
				cycle = 3;
				break; // LDA adr
			case 0x3b:
				cpu.SP = (cpu.SP - 1) & 0xffff;
				break; // DCX SP
			case 0x3c:
				res = cpu.A + 1;
				cpu.A = (short) (res & 0xff);
				flags_zsp(res);
				break; // INR A
			case 0x3d:
				res = cpu.A - 1;
				cpu.A = (short) (res & 0xff);
				flags_zsp(res);
				break; // DCR A
			case 0x3e:
				cpu.A = cpu.memory[opcode + 1];
				cpu.PC++;
				cycle = 2;
				break; // MVI A, D8
			case 0x3f:
				cpu.cc.CY = (cpu.cc.CY == 1) ? (byte) 0 : 1;
				break; // CMC

				//////   0x40 - 0x4f   /////

			case 0x40:
				cpu.B = cpu.B;
				break; // MOV B, B
			case 0x41:
				cpu.B = cpu.C;
				break; // MOV B, C
			case 0x42:
				cpu.B = cpu.D;
				break; // MOV B, D
			case 0x43:
				cpu.B = cpu.E;
				break; // MOV B, E
			case 0x44:
				cpu.B = cpu.H;
				break; // MOV B, H
			case 0x45:
				cpu.B = cpu.L;
				break; // MOV B, L
			case 0x46:
				cpu.B = cpu.memory[addr];
				break; // MOV B, M
			case 0x47:
				cpu.B = cpu.A;
				break; // MOV B, A
			case 0x48:
				cpu.C = cpu.B;
				break; // MOV C, B
			case 0x49:
				cpu.C = cpu.C;
				break; // MOV C, C
			case 0x4a:
				cpu.C = cpu.D;
				break; // MOV C, D
			case 0x4b:
				cpu.C = cpu.E;
				break; // MOV C, E
			case 0x4c:
				cpu.C = cpu.H;
				break; // MOV C, H
			case 0x4d:
				cpu.C = cpu.L;
				break; // MOV C, L
			case 0x4e:
				cpu.C = cpu.memory[addr];
				break; // MOV C, M
			case 0x4f:
				cpu.C = cpu.A;
				break; // MOV C, A

				//////   0x50 - 0x5f   /////

			case 0x50:
				cpu.D = cpu.B;
				break; // MOV D, B
			case 0x51:
				cpu.D = cpu.C;
				break; // MOV D, C
			case 0x52:
				cpu.D = cpu.D;
				break; // MOV D, D
			case 0x53:
				cpu.D = cpu.E;
				break; // MOV D, E
			case 0x54:
				cpu.D = cpu.H;
				break; // MOV D, H
			case 0x55:
				cpu.D = cpu.L;
				break; // MOV D, L
			case 0x56:
				cpu.D = cpu.memory[addr];
				break; // MOV D, M
			case 0x57:
				cpu.D = cpu.A;
				break; // MOV D, A
			case 0x58:
				cpu.E = cpu.B;
				break; // MOV E, B
			case 0x59:
				cpu.E = cpu.C;
				break; // MOV E, C
			case 0x5a:
				cpu.E = cpu.D;
				break; // MOV E, D
			case 0x5b:
				cpu.E = cpu.E;
				break; // MOV E, E
			case 0x5c:
				cpu.E = cpu.H;
				break; // MOV E, H
			case 0x5d:
				cpu.E = cpu.L;
				break; // MOV E, L
			case 0x5e:
				cpu.E = cpu.memory[addr];
				break; // MOV E, M
			case 0x5f:
				cpu.E = cpu.A;
				break; // MOV E, A

				//////   0x60 - 0x6f   /////

			case 0x60:
				cpu.H = cpu.B;
				break; // MOV H, B
			case 0x61:
				cpu.H = cpu.C;
				break; // MOV H, C
			case 0x62:
				cpu.H = cpu.D;
				break; // MOV H, D
			case 0x63:
				cpu.H = cpu.E;
				break; // MOV H, E
			case 0x64:
				cpu.H = cpu.H;
				break; // MOV H, H
			case 0x65:
				cpu.H = cpu.L;
				break; // MOV H, L
			case 0x66:
				cpu.H = cpu.memory[addr];
				break; // MOV H, M
			case 0x67:
				cpu.H = cpu.A;
				break; // MOV H, A
			case 0x68:
				cpu.L = cpu.B;
				break; // MOV L, B
			case 0x69:
				cpu.L = cpu.C;
				break; // MOV L, C
			case 0x6a:
				cpu.L = cpu.D;
				break; // MOV L, D
			case 0x6b:
				cpu.L = cpu.E;
				break; // MOV L, E
			case 0x6c:
				cpu.L = cpu.H;
				break; // MOV L, H
			case 0x6d:
				cpu.L = cpu.L;
				break; // MOV L, L
			case 0x6e:
				cpu.L = cpu.memory[addr];
				break; // MOV L, M
			case 0x6f:
				cpu.L = cpu.A;
				break; // MOV L, A

				//////   0x70 - 0x7f   /////

			case 0x70:
				cpu.memory[addr] = cpu.B;
				break; // MOV M, B
			case 0x71:
				cpu.memory[addr] = cpu.C;
				break; // MOV M, C
			case 0x72:
				cpu.memory[addr] = cpu.D;
				break; // MOV M, D
			case 0x73:
				cpu.memory[addr] = cpu.E;
				break; // MOV M, E
			case 0x74:
				cpu.memory[addr] = cpu.H;
				break; // MOV M, H
			case 0x75:
				cpu.memory[addr] = cpu.L;
				break; // MOV M, L
			case 0x76:
				// System.exit(0); // terminate program
				System.out.println("HLT CALLED!");
				return -1; // HLT
			case 0x77:
				cpu.memory[addr] = cpu.A;
				break; // MOV M, A
			case 0x78:
				cpu.A = cpu.B;
				break; // MOV A, B
			case 0x79:
				cpu.A = cpu.C;
				break; // MOV A, C
			case 0x7a:
				cpu.A = cpu.D;
				break; // MOV A, D
			case 0x7b:
				cpu.A = cpu.E;
				break; // MOV A, E
			case 0x7c:
				cpu.A = cpu.H;
				break; // MOV A, H
			case 0x7d:
				cpu.A = cpu.L;
				break; // MOV A, L
			case 0x7e:
				cpu.A = cpu.memory[addr];
				break; // MOV A, M
			case 0x7f:
				cpu.A = cpu.A;
				break; // MOV A, A

				//////   0x80 - 0x8f   /////

			case 0x80:
				ADD(cpu.B);
				break; // ADD B
			case 0x81:
				ADD(cpu.C);
				break; // ADD C
			case 0x82:
				ADD(cpu.D);
				break; // ADD D
			case 0x83:
				ADD(cpu.E);
				break; // ADD E
			case 0x84:
				ADD(cpu.H);
				break; // ADD H
			case 0x85:
				ADD(cpu.L);
				break; // ADD L
			case 0x86:
				ADD(cpu.memory[addr]);
				break; // ADD M
			case 0x87:
				ADD(cpu.A);
				break; // ADD A
			case 0x88:
				ADC(cpu.B);
				break; // ADC B
			case 0x89:
				ADC(cpu.C);
				break; // ADC C
			case 0x8a:
				ADC(cpu.D);
				break; // ADC D
			case 0x8b:
				ADC(cpu.E);
				break; // ADC E
			case 0x8c:
				ADC(cpu.H);
				break; // ADC H
			case 0x8d:
				ADC(cpu.L);
				break; // ADC L
			case 0x8e:
				ADC(cpu.memory[addr]);
				break; // ADC M
			case 0x8f:
				ADC(cpu.A);
				break; // ADC A

				//////   0x90 - 0x9f   /////

			case 0x90:
				SUB(cpu.B);
				break; // SUB B
			case 0x91:
				SUB(cpu.C);
				break; // SUB C
			case 0x92:
				SUB(cpu.D);
				break; // SUB D
			case 0x93:
				SUB(cpu.E);
				break; // SUB E
			case 0x94:
				SUB(cpu.H);
				break; // SUB H
			case 0x95:
				SUB(cpu.L);
				break; // SUB L
			case 0x96:
				SUB(cpu.memory[addr]);
				break; // SUB M
			case 0x97:
				SUB(cpu.A);
				break; // SUB A
			case 0x98:
				SBB(cpu.B);
				break; // SBB B
			case 0x99:
				SBB(cpu.C);
				break; // SBB C
			case 0x9a:
				SBB(cpu.D);
				break; // SBB D
			case 0x9b:
				SBB(cpu.E);
				break; // SBB E
			case 0x9c:
				SBB(cpu.H);
				break; // SBB H
			case 0x9d:
				SBB(cpu.L);
				break; // SBB L
			case 0x9e:
				SBB(cpu.memory[addr]);
				break; // SBB M
			case 0x9f:
				SBB(cpu.A);
				break; // SBB A

				//////   0xa0 - 0xaf   /////

			case 0xa0:
				ANA(cpu.B);
				break; // ANA B
			case 0xa1:
				ANA(cpu.C);
				break; // ANA C
			case 0xa2:
				ANA(cpu.D);
				break; // ANA D
			case 0xa3:
				ANA(cpu.E);
				break; // ANA E
			case 0xa4:
				ANA(cpu.H);
				break; // ANA H
			case 0xa5:
				ANA(cpu.L);
				break; // ANA L
			case 0xa6:
				ANA(cpu.memory[addr]);
				break; // ANA M
			case 0xa7:
				ANA(cpu.A);
				break; // ANA A
			case 0xa8:
				XRA(cpu.B);
				break; // XRA B
			case 0xa9:
				XRA(cpu.C);
				break; // XRA C
			case 0xaa:
				XRA(cpu.D);
				break; // XRA D
			case 0xab:
				XRA(cpu.E);
				break; // XRA E
			case 0xac:
				XRA(cpu.H);
				break; // XRA H
			case 0xad:
				XRA(cpu.L);
				break; // XRA L
			case 0xae:
				XRA(cpu.memory[addr]);
				break; // XRA M
			case 0xaf:
				XRA(cpu.A);
				break; // XRA A

				//////   0xb0 - 0xbf   /////

			case 0xb0:
				ORA(cpu.B);
				break; // ORA B
			case 0xb1:
				ORA(cpu.C);
				break; // ORA C
			case 0xb2:
				ORA(cpu.D);
				break; // ORA D
			case 0xb3:
				ORA(cpu.E);
				break; // ORA E
			case 0xb4:
				ORA(cpu.H);
				break; // ORA H
			case 0xb5:
				ORA(cpu.L);
				break; // ORA L
			case 0xb6:
				ORA(cpu.memory[addr]);
				break; // ORA M
			case 0xb7:
				ORA(cpu.A);
				break; // ORA A
			case 0xb8:
				CMP(cpu.B);
				break; // CMP B
			case 0xb9:
				CMP(cpu.C);
				break; // CMP C
			case 0xba:
				CMP(cpu.D);
				break; // CMP D
			case 0xbb:
				CMP(cpu.E);
				break; // CMP E
			case 0xbc:
				CMP(cpu.H);
				break; // CMP H
			case 0xbd:
				CMP(cpu.L);
				break; // CMP L
			case 0xbe:
				CMP(cpu.memory[addr]);
				break; // CMP M
			case 0xbf:
				CMP(cpu.A);
				break; // CMP A

				//////   0xc0 - 0xcf   /////

			case 0xc0: 
				if (cpu.cc.Z == 0) {
					RET();
				}
				break; // RNZ
			case 0xc1:
				cpu.C = cpu.memory[cpu.SP];
				cpu.B = cpu.memory[(cpu.SP + 1) & 0xffff];
				cpu.SP = (cpu.SP + 2) & 0xffff;
				break; // POP B
			case 0xc2:
				if (cpu.cc.Z == 0) {
					cpu.PC = (cpu.memory[opcode + 2] << 8) | cpu.memory[opcode + 1];
				} else {
					cpu.PC += 2;
				}
				cycle = 3;
				break; // JNZ adr
			case 0xc3:
				cpu.PC = (cpu.memory[opcode + 2] << 8) | cpu.memory[opcode + 1];
				cycle = 3;
				break; // JMP adr
			case 0xc4:
				if (cpu.cc.Z == 0) {
					CALL(opcode);
				} else {
					cpu.PC += 2;
				}
				cycle = 3;
				break; // CNZ adr
			case 0xc5:
				cpu.memory[(cpu.SP - 1) & 0xffff] = cpu.B;
				cpu.memory[(cpu.SP - 2) & 0xffff] = cpu.C;
				cpu.SP = (cpu.SP - 2) & 0xffff;
				break; // PUSH B
			case 0xc6:
				ADD(cpu.memory[opcode + 1]);
				cpu.PC++;
				cycle = 2;
				break; // ADI D8
			case 0xc7:
				CALL(0x00);
				cycle = 3;
				break; // RST 0
			case 0xc8:
				if (cpu.cc.Z == 1) {
					RET();
				}
				break; // RZ
			case 0xc9:
				RET();
				break; // RET
			case 0xca:
				if (cpu.cc.Z == 1) {
					cpu.PC = (cpu.memory[opcode + 2] << 8) | cpu.memory[opcode + 1];
				} else {
					cpu.PC += 2;
				}
				cycle = 3;
				break; // JZ adr
			case 0xcb:
				break; // -
			case 0xcc:
				if (cpu.cc.Z == 1) {
					CALL(opcode);
				} else {
					cpu.PC += 2;
				}
				cycle = 3;
				break; // CZ adr
			case 0xcd:
				// CALL(opcode);
				TEST_DIAG(opcode);
				cycle = 3;
				break; // CALL adr
			case 0xce:
				ADC(cpu.memory[opcode + 1]);
				cpu.PC++;
				cycle = 2;
				break;  // ACI D8
			case 0xcf:
				CALL(0x08);
				cycle = 3;
				break; // RST 1

				//////   0xd0 - 0xdf   /////

			case 0xd0: 
				if (cpu.cc.CY == 0) {
					RET();
				}
				break; // RNC
			case 0xd1:
				cpu.E = cpu.memory[cpu.SP];
				cpu.D = cpu.memory[(cpu.SP + 1) & 0xffff];
				cpu.SP = (cpu.SP + 2) & 0xffff;
				break; // POP D
			case 0xd2:
				if (cpu.cc.CY == 0) {
					cpu.PC = (cpu.memory[opcode + 2] << 8) | cpu.memory[opcode + 1];
				} else {
					cpu.PC += 2;
				}
				cycle = 3;
				break; // JNC adr
			case 0xd3:
				cpu.PC++;
				cycle = 2;
				break; // OUT D8
			case 0xd4:
				if (cpu.cc.CY == 0) {
					CALL(opcode);
				} else {
					cpu.PC += 2;
				}
				cycle = 3;
				break; // CNC adr
			case 0xd5:
				cpu.memory[(cpu.SP - 1) & 0xffff] = cpu.D;
				cpu.memory[(cpu.SP - 2) & 0xffff] = cpu.E;
				cpu.SP = (cpu.SP - 2) & 0xffff;
				break; // PUSH D
			case 0xd6:
				SUB(cpu.memory[opcode + 1]);
				cpu.PC++;
				cycle = 2;
				break; // SUI D8
			case 0xd7:
				CALL(0x10);
				cycle = 3;
				break;// RST 2
			case 0xd8: 
				if (cpu.cc.CY == 1) {
					RET();
				}
				break; // RC
			case 0xd9:
				break; // -
			case 0xda:
				if (cpu.cc.CY == 1) {
					cpu.PC = (cpu.memory[opcode + 2] << 8) | cpu.memory[opcode + 1];
				} else {
					cpu.PC += 2;
				}
				cycle = 3;
				break; // JC adr

				// case 0xdb  // IN D8 (PC + 1) special

			case 0xdc:
				if (cpu.cc.CY == 1) {
					CALL(opcode);
				} else {
					cpu.PC += 2;
				}
				cycle = 3;
				break; // CC adr
			case 0xdd:
				break; // -
			case 0xde:
				SBB(cpu.memory[opcode + 1]);
				cpu.PC++;
				cycle = 2;
				break; // SBI D8
			case 0xdf:
				CALL(0x18);
				cycle = 3;
				break;// RST 3

				//////   0xe0 - 0xef   /////

			case 0xe0: 
				if (cpu.cc.P == 0) {
					RET();
				}
				break; // RPO
			case 0xe1:
				cpu.L = cpu.memory[cpu.SP];
				cpu.H = cpu.memory[(cpu.SP + 1) & 0xffff];
				cpu.SP = (cpu.SP + 2) & 0xffff;
				break; // POP H
			case 0xe2:
				if (cpu.cc.P == 0) {
					cpu.PC = (cpu.memory[opcode + 2] << 8) | cpu.memory[opcode + 1];
				} else {
					cpu.PC += 2;
				}
				cycle = 3;
				break; // JPO adr
			case 0xe3:
				XTHL();
				break; // XTHL
			case 0xe4:
				if (cpu.cc.P == 0) {
					CALL(opcode);
				} else {
					cpu.PC += 2;
				}
				cycle = 3;
				break; // CPO adr
			case 0xe5:
				cpu.memory[(cpu.SP - 1) & 0xffff] = cpu.H;
				cpu.memory[(cpu.SP - 2) & 0xffff] = cpu.L;
				cpu.SP = (cpu.SP - 2) & 0xffff;
				break; // PUSH H
			case 0xe6:
				ANA(cpu.memory[opcode + 1]);
				cpu.PC++;
				cycle = 2;
				break; // ANI D8
			case 0xe7:
				CALL(0x20);
				cycle = 3;
				break;// RST 4
			case 0xe8: 
				if (cpu.cc.P == 1) {
					RET();
				}
				break; // RPE
			case 0xe9:
				cpu.PC = addr;
				break; // PCHL
			case 0xea:
				if (cpu.cc.P == 1) {
					cpu.PC = (cpu.memory[opcode + 2] << 8) | cpu.memory[opcode + 1];
				} else {
					cpu.PC += 2;
				}
				cycle = 3;
				break; // JPE adr
			case 0xeb:
				XCHG();
				break; // XCHG (HL to DE vice-versa)
			case 0xec:
				if (cpu.cc.P == 1) {
					CALL(opcode);
				} else {
					cpu.PC += 2;
				}
				cycle = 3;
				break; // CPE adr
			case 0xed:
				break; // -
			case 0xee:
				XRA(cpu.memory[opcode + 1]);
				cpu.PC++;
				cycle = 2;
				break; // XRI D8
			case 0xef:
				CALL(0x28);
				cycle = 3;
				break; // RST 5
		
				//////   0xf0 - 0xff   /////

			case 0xf0: 
				if (cpu.cc.S == 0) {
					RET();
				}
				break; // RP
			case 0xf1:
				POP_PSW();
				break; // POP PSW
			case 0xf2:
				if (cpu.cc.S == 0) {
					cpu.PC = (cpu.memory[opcode + 2] << 8) | cpu.memory[opcode + 1];
				} else {
					cpu.PC += 2;
				}
				cycle = 3;
				break; // JP adr

				// case 0xf3  // DI special
			case 0xf3:
				break; // stub DI

			case 0xf4:
				if (cpu.cc.S == 0) {
					CALL(opcode);
				} else {
					cpu.PC += 2;
				}
				cycle = 3;
				break; // CP adr
			case 0xf5:
				PUSH_PSW();
				break; // PUSH PSW
			case 0xf6:
				ORA(cpu.memory[opcode + 1]);
				cpu.PC++;
				cycle = 2;
				break; // ORI D8
			case 0xf7:
				CALL(0x30);
				cycle = 3;
				break;// RST 6
			case 0xf8: 
				if (cpu.cc.S == 1) {
					RET();
				}
				break; // RM
			case 0xf9:
				SPHL(addr);
				break; // SPHL
			case 0xfa:
				if (cpu.cc.S == 1) {
					cpu.PC = (cpu.memory[opcode + 2] << 8) | cpu.memory[opcode + 1];
				} else {
					cpu.PC += 2;
				}
				cycle = 3;
				break; // JM adr	


			case 0xfb:
				// TODO: needs to implement, when interrupts added
				break; // EI (special)


			case 0xfc:
				if (cpu.cc.S == 1) {
					CALL(opcode);
				} else {
					cpu.PC += 2;
				}
				cycle = 3;
				break; // CM adr
			case 0xfd:
				break; // -
			case 0xfe:
				CMP(cpu.memory[opcode + 1]);
				cpu.PC++;
				cycle = 2;
				break; // CPI D8
			case 0xff:
				CALL(0x38);
				cycle = 3;
				break;// RST 7
			default:
				return -1;
		}
		
		return cycle;
	}

	// TODO: implement aux. carry
	/// SUBROUTINES
	private void ADC(int var) {
		int res = (cpu.A + var) + cpu.cc.CY;

		flags_BCD(res);

		cpu.A = (short) (res & 0xff);
	}

	private void ADD(int var) {
		int res = cpu.A + var;
		flags_BCD(res);
		cpu.A = (short) (res & 0xff);
	}

	private void ANA(int var) {
		cpu.A = (short) (cpu.A & var);
		flags_zsp(cpu.A);
		cpu.cc.CY = 0;
	}

	private void CALL(int opcode) {
		int nextAddr = opcode + 3;
		cpu.memory[(cpu.SP - 1) & 0xffff] = (short) ((nextAddr >> 8) & 0xff);
		cpu.memory[(cpu.SP - 2) & 0xffff] = (short) (nextAddr & 0xff);
		cpu.SP = (cpu.SP - 2) & 0xffff;

		cpu.PC = (cpu.memory[opcode + 2] << 8) | cpu.memory[opcode + 1];
	}

	private void CMP(int var) {
		// (two's) complement — defined also as "another set" e.g. another set of binary 1 is binary 0!
		int res = cpu.A + ((~var + 1) & 0xff);

		cpu.cc.Z = ((res & 0xff) == 0) ? (byte) 1 : 0;
		cpu.cc.S = ((res & 0x80) == 0x80) ? (byte) 1 : 0;
		cpu.cc.P = parityFlag(res & 0xff);  // ensuring only checks for 8-bit variable
		cpu.cc.CY = (var > cpu.A) ? 1: (byte) 0; // minuend greater than subtrahend will likely result to overflow of 0xff (borrowing)
		// cpu.cc.AC = -1;
	}

	private void DAD(int... var) {
		int HL = (cpu.H << 8) | cpu.L; // addr = 16bit

		int pair;
		if (var.length == 2) {
			pair = (var[0] << 8) | var[1];
		} else {
			pair = var[0];
		}

		int res = HL + pair; // may result greater than 16 bit, raise CY if occured

		cpu.cc.CY = ((res & 0xffff_0000) > 0) ? (byte) 1 : 0; // cut all values from lower 16 bit and check if higher 16 bit has value
		
		cpu.H = (short) ((res & 0xff00) >> 8);	// store higher 8-bit to H
		cpu.L = (short) (res & 0xff);			// store lower  8-bit to L
	}

	private void LDA(int hi_nib, int lo_nib) {
		int addr = (hi_nib << 8) | lo_nib;
		cpu.A = cpu.memory[addr];
	}

	private void LHLD(int opcode) {
		int addr = (cpu.memory[opcode + 2] << 8) | cpu.memory[opcode + 1];

		cpu.H = cpu.memory[addr + 1];
		cpu.L = cpu.memory[addr];
	}

	private void ORA(int var) {
		int res = cpu.A | var;

		flags_zsp(res);
		cpu.cc.CY = 0; // fixed value

		cpu.A = (short) (res);
	}

	private void POP_PSW() {
		int PSW = cpu.memory[cpu.SP];

		cpu.cc.CY = ((PSW & cpu.PSW_FLAG_POS_CY) != 0) ? (byte) 1 : 0;
		cpu.cc.P  = ((PSW & cpu.PSW_FLAG_POS_PA) != 0) ? (byte) 1 : 0;
		cpu.cc.AC = ((PSW & cpu.PSW_FLAG_POS_AC) != 0) ? (byte) 1 : 0;
		cpu.cc.Z  = ((PSW & cpu.PSW_FLAG_POS_ZE) != 0) ? (byte) 1 : 0;
		cpu.cc.S  = ((PSW & cpu.PSW_FLAG_POS_SN) != 0) ? (byte) 1 : 0;
		
		cpu.A = cpu.memory[(cpu.SP + 1) & 0xffff];
		cpu.SP = (cpu.SP + 2) & 0xffff;
	}

	private void PUSH_PSW() {
		// A and PSW (formed binary value via flags , plus its filler value)

		cpu.memory[(cpu.SP - 1) & 0xffff] = cpu.A;

		// prepare variable higher than 0xff, but with 0's in bit 0-7
		// add 2 to comply with fixed value on bit pos. 1
		// this way, it serves as flags' default state waiting to be flipped, like a template
		// also helps to retain flags proper positioning
		int PSW = 0x102;

		// skip pos 5 and 3, it does not need to be flipped since it is by default, a 0 value
		PSW =
			(cpu.cc.S     <<  7)  |   // place sign flag status on pos 7
			(cpu.cc.Z     <<  6)  |   // place zero flag status on pos 6
			(cpu.cc.AC    <<  4)  |   // place aux. carry flag status on pos 4
			(cpu.cc.P     <<  2)  |   // place parity flag status on pos 2
			(cpu.cc.CY)           ;   // place carry flag status on pos 0

		cpu.memory[(cpu.SP - 2) & 0xffff] = (short) (PSW & 0xff); // cut to 8 bit after
		cpu.SP = (cpu.SP - 2) & 0xffff;
	}

	private void RAL() {
		int res = (cpu.A + cpu.cc.CY) << 1; // verify
		cpu.cc.CY = (res > 0xff) ? (byte) 1 : 0;
		cpu.A = (short) (res & 0xff);
	}

	private void RAR() {
		int res = (cpu.A + cpu.cc.CY) >> 1; // verify
		cpu.cc.CY = (byte) (cpu.A & 0x1); // leftover bit 0 as carry
		cpu.A = (short) ((res | cpu.cc.CY) & 0xff);
	}

	private void RET() {
		int addr = (cpu.memory[(cpu.SP + 1) & 0xffff] << 8) | cpu.memory[cpu.SP];
		cpu.SP = (cpu.SP + 2) & 0xffff;
		cpu.PC = addr;
	}

	private void RLC() {
		int res = (cpu.A << 1); // Rotate left shift
		cpu.cc.CY = (res > 0xff) ? (byte) 1 : 0; // normal carry check
		cpu.A = (short) ((res + cpu.cc.CY) & 0xff); // rotated value plus its carry flag 
	}

	private void RRC() {
		int res = (cpu.A >>> 1); // Rotate right shift (zero fill)
		cpu.cc.CY = (byte) (cpu.A & 0x1); // leftover bit 0 as carry
		cpu.A = (short) ((res | (cpu.cc.CY << 7)) & 0xff); // update Accumulator with rotated value with its carry flag leftmost bit (0xff)
	}

	private void SBB(int var) {
		int res = (cpu.A + ((~var + 1) & 0xff)) + ((~cpu.cc.CY + 1) & 0xff);

		cpu.cc.Z = ((res & 0xff) == 0) ? (byte) 1 : 0;
		cpu.cc.S = ((res & 0x80) == 0x80) ? (byte) 1 : 0;
		cpu.cc.P = parityFlag(res & 0xff);
		cpu.cc.CY = (var > cpu.A) ? 1: (byte) 0; // minuend greater than subtrahend will likely result to overflow of 0xff (borrowing)
		// cpu.cc.AC = -1; // NULL 

		cpu.A = (short) (res & 0xff);
	}

	private void SHLD(int opcode) {
		int addr = (cpu.memory[opcode + 2] << 8) | cpu.memory[opcode + 1];

		cpu.memory[addr + 1] = cpu.H;
		cpu.memory[addr] = cpu.L;
	}

	private void SPHL(int address) {
		cpu.SP = address;
	}

	private void STA(int hi_nib, int lo_nib) {
		int addr = (hi_nib << 8) | lo_nib;
		cpu.memory[addr] = cpu.A;
	}

	private void SUB(int var) {
		int res = cpu.A + ((~var + 1) & 0xff);

		cpu.cc.Z = ((res & 0xff) == 0) ? (byte) 1 : 0;
		cpu.cc.S = ((res & 0x80) == 0x80) ? (byte) 1 : 0;
		cpu.cc.P = parityFlag(res & 0xff);
		cpu.cc.CY = (var > cpu.A) ? 1: (byte) 0; // minuend greater than subtrahend will likely result to overflow of 0xff (borrowing)
		// cpu.cc.AC = -1; // NULL

		cpu.A = (short) (res & 0xff);
	}

	private void XCHG() {
		// SWAP H and D
		cpu.H = (short) (cpu.H + cpu.D);
		cpu.D = (short) (cpu.H - cpu.D);
		cpu.H = (short) (cpu.H - cpu.D);

		// SWAP L and E
		cpu.L = (short) (cpu.L + cpu.E);
		cpu.E = (short) (cpu.L - cpu.E);
		cpu.L = (short) (cpu.L - cpu.E);
	}

	private void XRA(int var) {
		int res = cpu.A ^ var;

		flags_zsp(res);
		cpu.cc.CY = 0;
		// cpu.cc.AC = 0; // fixed?

		cpu.A = (short) (res);
	}

	private void XTHL() {
		// SWAP H and Top + 1  SP (under of top stack)
		cpu.H = (short) (cpu.H + cpu.memory[(cpu.SP + 1) & 0xffff]);
		cpu.memory[(cpu.SP + 1) & 0xffff] = (short) (cpu.H - cpu.memory[(cpu.SP + 1) & 0xffff]);
		cpu.H = (short) (cpu.H - cpu.memory[(cpu.SP + 1) & 0xffff]);

		// SWAP L and Top SP (top stack)
		cpu.L = (short) (cpu.L + cpu.memory[cpu.SP]);
		cpu.memory[cpu.SP] = (short) (cpu.L - cpu.memory[cpu.SP]);
		cpu.L = (short) (cpu.L - cpu.memory[cpu.SP]);
	}

	/// FLAGS
	private void flags_BCD(int result) {
		cpu.cc.CY = (result > 0xff) ? (byte) 1 : 0;
		cpu.cc.Z = ((result & 0xff) == 0) ? (byte) 1 : 0;
		cpu.cc.S = ((result & 0x80) == 0x80) ? (byte) 1 : 0;
		cpu.cc.P = parityFlag(result & 0xff);  // ensuring only checks for 8-bit variable
		// cpu.cc.AC = -1; // NULL
	}

	private void flags_zsp(int result) {
		cpu.cc.Z = ((result & 0xff) == 0) ? (byte) 1 : 0;
		cpu.cc.S = ((result & 0x80) == 0x80) ? (byte) 1 : 0;
		cpu.cc.P = parityFlag(result & 0xff);  // ensuring only checks for 8-bit variable
		// cpu.cc.AC = -1; // NULL
	}


	private byte parityFlag(int result) {
		int res = Integer.toBinaryString(result).replaceAll("0", "").length(); // Simple workaround to get count of flipped binary
		return (res % 2 == 0) ? (byte) 1 : 0;
	}
	
	/// INIT
	private void init(short memory[], CpuComponents cpu) {
		// cpu
		this.cpu = cpu;
		
		// testing purposes
		AUTO_TEST();
	}
	
	///  MISC  ///
	private final int MAX_INT = 2_147_483_647;
	
	// CPU OVERRIDE
	private void AUTO_TEST() {
		switch (Main.romName[0]) {
			case "cpudiag.bin":
				TEST_OVERRIDE_CPUDIAG();
				break;
			case "8080EX1.COM":
				TEST_OVERRIDE_EX1();
				break;
		}
	}

	private void TEST_OVERRIDE_CPUDIAG() {
		// Direct PC to loaded address
		cpu.PC = this.realAddr;

		// SKIP DAA inst
		cpu.memory[0x59c] = 0xc3;
		cpu.memory[0x59d] = 0xc2;
		cpu.memory[0x59e] = 0x05;
	}

	private void TEST_OVERRIDE_EX1() {
		cpu.PC = this.realAddr;
	}

	private void TEST_DIAG(int opcode) {
		// SOURCE: kpmiller — Full 8080 emulation
		if (5 == ((cpu.memory[opcode + 2] << 8) | cpu.memory[opcode + 1])) {

			if (cpu.C == 9) {
				int offset = (cpu.D << 8) | (cpu.E);
				int str = offset + 3;  //skip the prefix bytes
				char read;

				while ((read = (char)cpu.memory[str]) != '$') {
					System.out.print(read);
					str++;
				}

				System.out.println();
				PAUSE_THREAD(1000);
			} else if (cpu.C == 2) {
				System.out.println("print char routine called\n");
			}

		} else if (0 ==  ((cpu.memory[opcode + 2] << 8) | cpu.memory[opcode + 1])) {
			// System.exit(0);
			System.out.println("-- System called for exit --");
		} else {
			int  ret = cpu.PC + 2;
			cpu.memory[(cpu.SP - 1) & 0xffff] = (short) ((ret >> 8) & 0xff);
			cpu.memory[(cpu.SP - 2) & 0xffff] = (short) (ret & 0xff);
			cpu.SP = (cpu.SP - 2) & 0xffff;
			cpu.PC = (cpu.memory[opcode + 2] << 8) | cpu.memory[opcode + 1];
		}
	}

	public void PAUSE_THREAD(int mills) {
		try {
			Thread.sleep(mills);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

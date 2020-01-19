
import BaseClass.AppUtils;

public class Interpreter
{
	// strict handling of 0xff (8 bit), 0xffff (16 bit) addresses
	// java only offers signed data types

	boolean test_finished = false;
	static short cyc = 0;
	static long cycle = 0;
	// SOURCES: superzazu
	static short OPCODES_CYCLES[] = {
		//  0   1   2   3   4   5   6   7   8   9   A   B   C   D   E   F
		4,  10, 7,  5,  5,  5,  7,  4,  4,  10, 7,  5,  5,  5,  7,  4,  // 0
		4,  10, 7,  5,  5,  5,  7,  4,  4,  10, 7,  5,  5,  5,  7,  4,  // 1
		4,  10, 16, 5,  5,  5,  7,  4,  4,  10, 16, 5,  5,  5,  7,  4,  // 2
		4,  10, 13, 5,  10, 10, 10, 4,  4,  10, 13, 5,  5,  5,  7,  4,  // 3
		5,  5,  5,  5,  5,  5,  7,  5,  5,  5,  5,  5,  5,  5,  7,  5,  // 4
		5,  5,  5,  5,  5,  5,  7,  5,  5,  5,  5,  5,  5,  5,  7,  5,  // 5
		5,  5,  5,  5,  5,  5,  7,  5,  5,  5,  5,  5,  5,  5,  7,  5,  // 6
		7,  7,  7,  7,  7,  7,  7,  7,  5,  5,  5,  5,  5,  5,  7,  5,  // 7
		4,  4,  4,  4,  4,  4,  7,  4,  4,  4,  4,  4,  4,  4,  7,  4,  // 8
		4,  4,  4,  4,  4,  4,  7,  4,  4,  4,  4,  4,  4,  4,  7,  4,  // 9
		4,  4,  4,  4,  4,  4,  7,  4,  4,  4,  4,  4,  4,  4,  7,  4,  // A
		4,  4,  4,  4,  4,  4,  7,  4,  4,  4,  4,  4,  4,  4,  7,  4,  // B
		5,  10, 10, 10, 11, 11, 7,  11, 5,  10, 10, 10, 11, 17, 7,  11, // C
		5,  10, 10, 10, 11, 11, 7,  11, 5,  10, 10, 10, 11, 17, 7,  11, // D
		5,  10, 10, 18, 11, 11, 7,  11, 5,  5,  10, 4,  11, 17, 7,  11, // E
		5,  10, 10, 4,  11, 11, 7,  11, 5,  5,  10, 4,  11, 17, 7,  11  // F
	};

	/// MAIN EMULATION
	public short emulate8080(CpuComponents cpu) {
		// temporary containers
		int res;
	
		// opcode
		int opcode = cpu.PC;

		// HL (M)
		int addr = ((cpu.H << 8) | cpu.L);

		// increment PC every calls
		cpu.PC++;

		// cycles
		cyc = OPCODES_CYCLES[cpu.memory[opcode]];
		
		switch (cpu.memory[opcode]) {
				/////   0x00 - 0x0f   /////

			case 0x00:
				break; // NOP
			case 0x01:
				cpu.B = cpu.memory[opcode + 2];
				cpu.C = cpu.memory[opcode + 1];
				cpu.PC += 2;
				break; // LXI B, D16
			case 0x02:
				STA(cpu, cpu.B, cpu.C);
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
				i8080_flag_ac(cpu, cpu.B, 1);
				res = cpu.B + 1;
				cpu.B = (short) (res & 0xff);
				flags_zsp(cpu, res);
				break; // INR B
			case 0x05:
				i8080_flag_ac(cpu, cpu.B, -1);
				res = cpu.B - 1;
				cpu.B = (short) (res & 0xff);
				flags_zsp(cpu, res);
				break; // DCR B
			case 0x06:
				cpu.B = cpu.memory[opcode + 1];
				cpu.PC++;
				break; // MVI B, D8
			case 0x07:
				RLC(cpu);
				break; // RLC
			case 0x08:
				break; // -
			case 0x09:
				DAD(cpu, cpu.B, cpu.C);
				break; //DAD B
			case 0x0a:
				LDA(cpu, cpu.B, cpu.C);
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
				i8080_flag_ac(cpu, cpu.C, 1);
				res = cpu.C + 1;
				cpu.C = (short) (res & 0xff);
				flags_zsp(cpu, res);
				break; // INR C
			case 0x0d:
				i8080_flag_ac(cpu, cpu.C, -1);
				res = cpu.C - 1;
				cpu.C = (short) (res & 0xff);
				flags_zsp(cpu, res);
				break; // DCR C
			case 0x0e:
				cpu.C = cpu.memory[opcode + 1];
				cpu.PC++;
				break; // MVI C, D8
			case 0x0f:
				RRC(cpu);	
				break; // RRC

				//////   0x10 - 0x1f   /////

			case 0x10:
				break; // -
			case 0x11:
				cpu.D = cpu.memory[opcode + 2];
				cpu.E = cpu.memory[opcode + 1];
				cpu.PC += 2;
				break; // LXI D, D16
			case 0x12:
				STA(cpu, cpu.D, cpu.E);
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
				i8080_flag_ac(cpu, cpu.D, 1);
				res = cpu.D + 1;
				cpu.D = (short) (res & 0xff);
				flags_zsp(cpu, res);
				break; // INR D
			case 0x15:
				i8080_flag_ac(cpu, cpu.D, -1);
				res = cpu.D - 1;
				cpu.D = (short) (res & 0xff);
				flags_zsp(cpu, res);
				break; // DCR D
			case 0x16:
				cpu.D = cpu.memory[opcode + 1];
				cpu.PC++;
				break; // MVI D, D8
			case 0x17:
				RAL(cpu);
				break; // RAL
			case 0x18:
				break; // -
			case 0x19:
				DAD(cpu, cpu.D, cpu.E);
				break; //DAD D
			case 0x1a:
				LDA(cpu, cpu.D, cpu.E);
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
				i8080_flag_ac(cpu, cpu.E, 1);
				res = cpu.E + 1;
				cpu.E = (short) (res & 0xff);
				flags_zsp(cpu, res);
				break; // INR E
			case 0x1d:
				i8080_flag_ac(cpu, cpu.E, -1);
				res = cpu.E - 1;
				cpu.E = (short) (res & 0xff);
				flags_zsp(cpu, res);
				break; // DCR E
			case 0x1e:
				cpu.E = cpu.memory[opcode + 1];
				cpu.PC++;
				break; // MVI E, D8
			case 0x1f:
				RAR(cpu);
				break; // RAR

				//////   0x20 - 0x2f   /////

			case 0x20:
				break; // -
			case 0x21:
				cpu.H = cpu.memory[opcode + 2];
				cpu.L = cpu.memory[opcode + 1];
				cpu.PC += 2;
				break; // LXI H, D16
			case 0x22:
				SHLD(cpu, opcode);
				cpu.PC += 2;
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
				i8080_flag_ac(cpu, cpu.H, 1);
				res = cpu.H + 1;
				cpu.H = (short) (res & 0xff);
				flags_zsp(cpu, res);
				break; // INR H
			case 0x25:
				i8080_flag_ac(cpu, cpu.H, -1);
				res = cpu.H - 1;
				cpu.H = (short) (res & 0xff);
				flags_zsp(cpu, res);
				break; // DCR H
			case 0x26:
				cpu.H = cpu.memory[opcode + 1];
				cpu.PC++;
				break; // MVI H, D8

			case 0x27:
				// SOURCE: superzazu
				// get least significant nibble and add 6 if >9
				// same as most significant nibble
				
				byte cy = cpu.cc.CY;
				short correction = 0;

				short lsb = (short) (cpu.A & 0xf);
				short msb = (short) (cpu.A >> 4);

				if (cpu.cc.AC == 1 || lsb > 9) {
					correction += 0x06;
				}
				
				if (cpu.cc.CY == 1 || msb > 9 || (msb >= 9 && lsb > 9)) {
					correction += 0x60;
					cy = 1;
				}
				
				ADD(cpu, correction);
				
				cpu.cc.CY = cy;
				
				break; // DAA

			case 0x28:
				break; // -
			case 0x29:
				DAD(cpu, cpu.H, cpu.L);
				break; //DAD H
			case 0x2a:
				LHLD(cpu, opcode);
				cpu.PC += 2;
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
				i8080_flag_ac(cpu, cpu.L, 1);
				res = cpu.L + 1;
				cpu.L = (short) (res & 0xff);
				flags_zsp(cpu, res);
				break; // INR L
			case 0x2d:
				i8080_flag_ac(cpu, cpu.L, -1);
				res = cpu.L - 1;
				cpu.L = (short) (res & 0xff);
				flags_zsp(cpu, res);
				break; // DCR L
			case 0x2e:
				cpu.L = cpu.memory[opcode + 1];
				cpu.PC++;
				break; // MVI L, D8
			case 0x2f:
				cpu.A = (short) ((~cpu.A & 0xff));
				break; // CMA

				//////   0x30 - 0x3f   /////

			case 0x31:
				cpu.SP = ((cpu.memory[opcode + 2] << 8) | cpu.memory[opcode + 1]);
				cpu.PC += 2;
				break; // LXI SP, D16
			case 0x32:
				STA(cpu, cpu.memory[opcode + 2], cpu.memory[opcode + 1]);
				cpu.PC += 2;
				break; // STA adr
			case 0x33:
				cpu.SP = (short) ((cpu.SP + 1) & 0xffff);
				break; // INX SP
			case 0x34:
				i8080_flag_ac(cpu, cpu.memory[addr], 1);
				res = cpu.memory[addr] + 1;
				cpu.memory[addr] = (short) (res & 0xff);
				flags_zsp(cpu, res);
				break; // INR M
			case 0x35:
				i8080_flag_ac(cpu, cpu.memory[addr], -1);
				res = cpu.memory[addr] - 1;
				cpu.memory[addr] = (short) (res & 0xff);
				flags_zsp(cpu, res);
				break; // DCR M
			case 0x36:
				cpu.memory[addr] = cpu.memory[opcode + 1];
				cpu.PC++;
				break; // MVI M, D8
			case 0x37:
				cpu.cc.CY = 1;
				break; // STC
			case 0x38:
				break; // -
			case 0x39:
				DAD(cpu, cpu.SP);
				break; //DAD SP
			case 0x3a:
				LDA(cpu, cpu.memory[opcode + 2], cpu.memory[opcode + 1]);
				cpu.PC += 2;
				break; // LDA adr
			case 0x3b:
				cpu.SP = (cpu.SP - 1) & 0xffff;
				break; // DCX SP
			case 0x3c:
				i8080_flag_ac(cpu, cpu.A, 1);
				res = cpu.A + 1;
				cpu.A = (short) (res & 0xff);
				flags_zsp(cpu, res);
				break; // INR A
			case 0x3d:
				i8080_flag_ac(cpu, cpu.A, -1);
				res = cpu.A - 1;
				cpu.A = (short) (res & 0xff);
				flags_zsp(cpu, res);
				break; // DCR A
			case 0x3e:
				cpu.A = cpu.memory[opcode + 1];
				cpu.PC++;
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
				//System.exit(0); // terminate program
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
				ADD(cpu, cpu.B);
				break; // ADD B
			case 0x81:
				ADD(cpu, cpu.C);
				break; // ADD C
			case 0x82:
				ADD(cpu, cpu.D);
				break; // ADD D
			case 0x83:
				ADD(cpu, cpu.E);
				break; // ADD E
			case 0x84:
				ADD(cpu, cpu.H);
				break; // ADD H
			case 0x85:
				ADD(cpu, cpu.L);
				break; // ADD L
			case 0x86:
				ADD(cpu, cpu.memory[addr]);
				break; // ADD M
			case 0x87:
				ADD(cpu, cpu.A);
				break; // ADD A
			case 0x88:
				ADC(cpu, cpu.B);
				break; // ADC B
			case 0x89:
				ADC(cpu, cpu.C);
				break; // ADC C
			case 0x8a:
				ADC(cpu, cpu.D);
				break; // ADC D
			case 0x8b:
				ADC(cpu, cpu.E);
				break; // ADC E
			case 0x8c:
				ADC(cpu, cpu.H);
				break; // ADC H
			case 0x8d:
				ADC(cpu, cpu.L);
				break; // ADC L
			case 0x8e:
				ADC(cpu, cpu.memory[addr]);
				break; // ADC M
			case 0x8f:
				ADC(cpu, cpu.A);
				break; // ADC A

				//////   0x90 - 0x9f   /////

			case 0x90:
				SUB(cpu, cpu.B);
				break; // SUB B
			case 0x91:
				SUB(cpu, cpu.C);
				break; // SUB C
			case 0x92:
				SUB(cpu, cpu.D);
				break; // SUB D
			case 0x93:
				SUB(cpu, cpu.E);
				break; // SUB E
			case 0x94:
				SUB(cpu, cpu.H);
				break; // SUB H
			case 0x95:
				SUB(cpu, cpu.L);
				break; // SUB L
			case 0x96:
				SUB(cpu, cpu.memory[addr]);
				break; // SUB M
			case 0x97:
				SUB(cpu, cpu.A);
				break; // SUB A
			case 0x98:
				SBB(cpu, cpu.B);
				break; // SBB B
			case 0x99:
				SBB(cpu, cpu.C);
				break; // SBB C
			case 0x9a:
				SBB(cpu, cpu.D);
				break; // SBB D
			case 0x9b:
				SBB(cpu, cpu.E);
				break; // SBB E
			case 0x9c:
				SBB(cpu, cpu.H);
				break; // SBB H
			case 0x9d:
				SBB(cpu, cpu.L);
				break; // SBB L
			case 0x9e:
				SBB(cpu, cpu.memory[addr]);
				break; // SBB M
			case 0x9f:
				SBB(cpu, cpu.A);
				break; // SBB A

				//////   0xa0 - 0xaf   /////

			case 0xa0:
				ANA(cpu, cpu.B);
				break; // ANA B
			case 0xa1:
				ANA(cpu, cpu.C);
				break; // ANA C
			case 0xa2:
				ANA(cpu, cpu.D);
				break; // ANA D
			case 0xa3:
				ANA(cpu, cpu.E);
				break; // ANA E
			case 0xa4:
				ANA(cpu, cpu.H);
				break; // ANA H
			case 0xa5:
				ANA(cpu, cpu.L);
				break; // ANA L
			case 0xa6:
				ANA(cpu, cpu.memory[addr]);
				break; // ANA M
			case 0xa7:
				ANA(cpu, cpu.A);
				break; // ANA A
			case 0xa8:
				XRA(cpu, cpu.B);
				break; // XRA B
			case 0xa9:
				XRA(cpu, cpu.C);
				break; // XRA C
			case 0xaa:
				XRA(cpu, cpu.D);
				break; // XRA D
			case 0xab:
				XRA(cpu, cpu.E);
				break; // XRA E
			case 0xac:
				XRA(cpu, cpu.H);
				break; // XRA H
			case 0xad:
				XRA(cpu, cpu.L);
				break; // XRA L
			case 0xae:
				XRA(cpu, cpu.memory[addr]);
				break; // XRA M
			case 0xaf:
				XRA(cpu, cpu.A);
				break; // XRA A

				//////   0xb0 - 0xbf   /////

			case 0xb0:
				ORA(cpu, cpu.B);
				break; // ORA B
			case 0xb1:
				ORA(cpu, cpu.C);
				break; // ORA C
			case 0xb2:
				ORA(cpu, cpu.D);
				break; // ORA D
			case 0xb3:
				ORA(cpu, cpu.E);
				break; // ORA E
			case 0xb4:
				ORA(cpu, cpu.H);
				break; // ORA H
			case 0xb5:
				ORA(cpu, cpu.L);
				break; // ORA L
			case 0xb6:
				ORA(cpu, cpu.memory[addr]);
				break; // ORA M
			case 0xb7:
				ORA(cpu, cpu.A);
				break; // ORA A
			case 0xb8:
				CMP(cpu, cpu.B);
				break; // CMP B
			case 0xb9:
				CMP(cpu, cpu.C);
				break; // CMP C
			case 0xba:
				CMP(cpu, cpu.D);
				break; // CMP D
			case 0xbb:
				CMP(cpu, cpu.E);
				break; // CMP E
			case 0xbc:
				CMP(cpu, cpu.H);
				break; // CMP H
			case 0xbd:
				CMP(cpu, cpu.L);
				break; // CMP L
			case 0xbe:
				CMP(cpu, cpu.memory[addr]);
				break; // CMP M
			case 0xbf:
				CMP(cpu, cpu.A);
				break; // CMP A

				//////   0xc0 - 0xcf   /////

			case 0xc0: 
				i8080_cond_ret(cpu, cpu.cc.Z == 0);
				break; // RNZ
			case 0xc1:
				cpu.C = cpu.memory[cpu.SP];
				cpu.B = cpu.memory[(cpu.SP + 1) & 0xffff];
				cpu.SP = (cpu.SP + 2) & 0xffff;
				break; // POP B
			case 0xc2:
				i8080_cond_jmp(cpu, opcode, cpu.cc.Z == 0);
				break; // JNZ adr
			case 0xc3:
				JMP(cpu, opcode);
				break; // JMP adr
			case 0xc4:
				i8080_cond_call(cpu, opcode, cpu.cc.Z == 0);
				break; // CNZ adr
			case 0xc5:
				cpu.memory[(cpu.SP - 1) & 0xffff] = cpu.B;
				cpu.memory[(cpu.SP - 2) & 0xffff] = cpu.C;
				cpu.SP = (cpu.SP - 2) & 0xffff;
				break; // PUSH B
			case 0xc6:
				ADD(cpu, cpu.memory[opcode + 1]);
				cpu.PC++;
				break; // ADI D8
			case 0xc7:
				GenerateInterrupt(cpu, 0x00);
				break; // RST 0
			case 0xc8:
				i8080_cond_ret(cpu, cpu.cc.Z == 1);
				break; // RZ
			case 0xc9:
				RET(cpu);
				break; // RET
			case 0xca:
				i8080_cond_jmp(cpu, opcode, cpu.cc.Z == 1);
				break; // JZ adr
			case 0xcb:
				break; // -
			case 0xcc:
				i8080_cond_call(cpu, opcode, cpu.cc.Z == 1);
				break; // CZ adr
			case 0xcd:
				CALL(cpu, opcode);
				break; // CALL adr
			case 0xce:
				ADC(cpu, cpu.memory[opcode + 1]);
				cpu.PC++;
				break; // ACI D8
			case 0xcf:
				GenerateInterrupt(cpu, 0x08);
				break; // RST 1

				//////   0xd0 - 0xdf   /////

			case 0xd0: 
				i8080_cond_ret(cpu, cpu.cc.CY == 0);
				break; // RNC
			case 0xd1:
				cpu.E = cpu.memory[cpu.SP];
				cpu.D = cpu.memory[(cpu.SP + 1) & 0xffff];
				cpu.SP = (cpu.SP + 2) & 0xffff;
				break; // POP D
			case 0xd2:
				i8080_cond_jmp(cpu, opcode, cpu.cc.CY == 0);
				break; // JNC adr
			case 0xd3:
				if (AppUtils.Machine.DEBUG) port_out();
				cpu.PC++;
				break; // OUT D8
			case 0xd4:
				i8080_cond_call(cpu, opcode, cpu.cc.CY == 0);
				break; // CNC adr
			case 0xd5:
				cpu.memory[(cpu.SP - 1) & 0xffff] = cpu.D;
				cpu.memory[(cpu.SP - 2) & 0xffff] = cpu.E;
				cpu.SP = (cpu.SP - 2) & 0xffff;
				break; // PUSH D
			case 0xd6:
				SUB(cpu, cpu.memory[opcode + 1]);
				cpu.PC++;
				break; // SUI D8
			case 0xd7:
				GenerateInterrupt(cpu, 0x10);
				break; // RST 2
			case 0xd8: 
				i8080_cond_ret(cpu, cpu.cc.CY == 1);
				break; // RC
			case 0xd9:
				break; // -
			case 0xda:
				i8080_cond_jmp(cpu, opcode, cpu.cc.CY == 1);
				break; // JC adr
			case 0xdb:
				if (AppUtils.Machine.DEBUG) cpu.A = port_in(cpu);
				cpu.PC++;
				break; // IN D8 (stub) (Load I/O to Accumulator)
			case 0xdc:
				i8080_cond_call(cpu, opcode, cpu.cc.CY == 1);
				break; // CC adr
			case 0xdd:
				break; // -
			case 0xde:
				SBB(cpu, cpu.memory[opcode + 1]);
				cpu.PC++;
				break; // SBI D8
			case 0xdf:
				GenerateInterrupt(cpu, 0x18);
				break; // RST 3

				//////   0xe0 - 0xef   /////

			case 0xe0: 
				i8080_cond_ret(cpu, cpu.cc.P == 0);
				break; // RPO
			case 0xe1:
				cpu.L = cpu.memory[cpu.SP];
				cpu.H = cpu.memory[(cpu.SP + 1) & 0xffff];
				cpu.SP = (cpu.SP + 2) & 0xffff;
				break; // POP H
			case 0xe2:
				i8080_cond_jmp(cpu, opcode, cpu.cc.P == 0);
				break; // JPO adr
			case 0xe3:
				XTHL(cpu);
				break; // XTHL
			case 0xe4:
				i8080_cond_call(cpu, opcode, cpu.cc.P == 0);
				break; // CPO adr
			case 0xe5:
				cpu.memory[(cpu.SP - 1) & 0xffff] = cpu.H;
				cpu.memory[(cpu.SP - 2) & 0xffff] = cpu.L;
				cpu.SP = (cpu.SP - 2) & 0xffff;
				break; // PUSH H
			case 0xe6:
				ANA(cpu, cpu.memory[opcode + 1]);
				cpu.PC++;
				break; // ANI D8
			case 0xe7:
				GenerateInterrupt(cpu, 0x20);
				break; // RST 4
			case 0xe8: 
				i8080_cond_ret(cpu, cpu.cc.P == 1);
				break; // RPE
			case 0xe9:
				cpu.PC = addr;
				break; // PCHL
			case 0xea:
				i8080_cond_jmp(cpu, opcode, cpu.cc.P == 1);
				break; // JPE adr
			case 0xeb:
				XCHG(cpu);
				break; // XCHG (HL to DE vice-versa)
			case 0xec:
				i8080_cond_call(cpu, opcode, cpu.cc.P == 1);
				break; // CPE adr
			case 0xed:
				break; // -
			case 0xee:
				XRA(cpu, cpu.memory[opcode + 1]);
				cpu.PC++;
				break; // XRI D8
			case 0xef:
				GenerateInterrupt(cpu, 0x28);
				break; // RST 5

				//////   0xf0 - 0xff   /////

			case 0xf0: 
				i8080_cond_ret(cpu, cpu.cc.S == 0);
				break; // RP
			case 0xf1:
				POP_PSW(cpu);
				break; // POP PSW
			case 0xf2:
				i8080_cond_jmp(cpu, opcode, cpu.cc.S == 0);
				break; // JP adr
			case 0xf3:
				cpu.int_enable = 0;
				break; // DI
			case 0xf4:
				i8080_cond_call(cpu, opcode, cpu.cc.S == 0);
				break; // CP adr
			case 0xf5:
				PUSH_PSW(cpu);
				break; // PUSH PSW
			case 0xf6:
				ORA(cpu, cpu.memory[opcode + 1]);
				cpu.PC++;
				break; // ORI D8
			case 0xf7:
				GenerateInterrupt(cpu, 0x30);
				break; // RST 6
			case 0xf8: 
				i8080_cond_ret(cpu, cpu.cc.S == 1);
				break; // RM
			case 0xf9:
				SPHL(cpu, addr);
				break; // SPHL
			case 0xfa:
				i8080_cond_jmp(cpu, opcode, cpu.cc.S == 1);
				break; // JM adr	
			case 0xfb:
				cpu.int_enable = 1;
				break; // EI
			case 0xfc:
				i8080_cond_call(cpu, opcode, cpu.cc.S == 1);
				break; // CM adr
			case 0xfd:
				break; // -
			case 0xfe:
				CMP(cpu, cpu.memory[opcode + 1]);
				cpu.PC++;
				break; // CPI D8
			case 0xff:
				GenerateInterrupt(cpu, 0x38);
				break; // RST 7
		}

		return cyc;
	}

	/// INTERRUPT
	public void GenerateInterrupt(CpuComponents cpu, int interrupt_num) {
		// PUSH PC
		cpu.memory[(cpu.SP - 1) & 0xffff] = (short) ((cpu.PC & 0xff00) >> 8);
		cpu.memory[(cpu.SP - 2) & 0xffff] = (short) (cpu.PC & 0xff);
		cpu.SP = (cpu.SP - 2) & 0xffff;

		cpu.PC = 8 * interrupt_num;
		cpu.int_enable = 0;
	}

	/// SUBROUTINES
	private void i8080_cond_call(CpuComponents cpu, int opcode, boolean cond) {
		if (cond) {
			CALL(cpu, opcode);
			cyc += 6;
		} else {
			cpu.PC += 2;
		}
	}

	private void i8080_cond_jmp(CpuComponents cpu, int opcode, boolean cond) {
		if (cond) {
			JMP(cpu, opcode);
		} else {
			cpu.PC += 2;
		}
	}
	
	private void i8080_cond_ret(CpuComponents cpu, boolean cond) {
		if (cond) {
			RET(cpu);
			cyc += 6;
		}
	}

	private void ADC(CpuComponents cpu, int var) {
		i8080_flag_ac(cpu, cpu.A, var + cpu.cc.CY); // half carry
		int res = (cpu.A + var) + cpu.cc.CY;
		flags_zspc(cpu, res);
		cpu.A = (short) (res & 0xff);
	}

	private void ADD(CpuComponents cpu, int var) {
		i8080_flag_ac(cpu, cpu.A, var); // half carry
		int res = cpu.A + var;
		flags_zspc(cpu, res);
		cpu.A = (short) (res & 0xff);
	}

	private void ANA(CpuComponents cpu, int var) {
		cpu.A = (short) (cpu.A & var);
		flags_zsp(cpu, cpu.A);
		cpu.cc.CY = 0;
		
		// SOURCE — superzazu
		cpu.cc.AC = (((cpu.A | var) & 0x08) != 0) ? (byte) 1 : 0;
	}

	private void CALL(CpuComponents cpu, int opcode) {
		int nextAddr = opcode + 3;
		cpu.memory[(cpu.SP - 1) & 0xffff] = (short) ((nextAddr >> 8) & 0xff);
		cpu.memory[(cpu.SP - 2) & 0xffff] = (short) (nextAddr & 0xff);
		cpu.SP = (cpu.SP - 2) & 0xffff;

		JMP(cpu, opcode);
	}

	private void CMP(CpuComponents cpu, int var) {
		// (two's) complement — defined also as "another set" e.g. another set of binary 1 is binary 0!
		i8080_flag_ac(cpu, cpu.A, -var); // half carry
		int res = (cpu.A - var) & 0xffff; // providing 0xfff would result to java not reading its sign value, thus a positive number

		cpu.cc.Z = ((res & 0xff) == 0) ? (byte) 1 : 0;
		cpu.cc.S = ((res & 0x80) == 0x80) ? (byte) 1 : 0;
		cpu.cc.P = parityFlag(res & 0xff); // ensuring only checks for 8-bit variable
		cpu.cc.CY = (res > 0xff) ? 1: (byte) 0;
		
		//cpu.cc.AC = (byte) ((~((cpu.A ^ res ^ var) & 0x10) + 1) & 0x1);
		//cpu.cc.AC = (byte) 10;
	}

	private void DAD(CpuComponents cpu, int... var) {
		int HL = (cpu.H << 8) | cpu.L; // addr = 16bit

		int pair;
		
		if (var.length == 2) {
			pair = (var[0] << 8) | var[1];
		} else {
			pair = var[0];
		}

		int res = HL + pair; // may result greater than 16 bit, raise CY if occured

		cpu.cc.CY = ((res & 0xf_0000) > 0) ? (byte) 1 : 0; // cut all values from lower 16 bit and check if higher 16 bit has value

		cpu.H = (short) ((res & 0xff00) >> 8);	// store higher 8-bit to H
		cpu.L = (short) (res & 0xff);			// store lower  8-bit to L
	}

	private void JMP(CpuComponents cpu, int opcode) {
		cpu.PC = (cpu.memory[opcode + 2] << 8) | cpu.memory[opcode + 1];
	}

	private void LDA(CpuComponents cpu, int hi_nib, int lo_nib) {
		int addr = (hi_nib << 8) | lo_nib;
		cpu.A = cpu.memory[addr];
	}

	private void LHLD(CpuComponents cpu, int opcode) {
		int addr = (cpu.memory[opcode + 2] << 8) | cpu.memory[opcode + 1];

		cpu.H = cpu.memory[addr + 1];
		cpu.L = cpu.memory[addr];
	}

	private void ORA(CpuComponents cpu, int var) {
		cpu.A |= var;

		flags_zsp(cpu, cpu.A);
		cpu.cc.CY = 0; // fixed value
		cpu.cc.AC = 0;
	}

	private void POP_PSW(CpuComponents cpu) {
		int PSW = cpu.memory[cpu.SP];

		cpu.cc.CY = ((PSW & cpu.PSW_FLAG_POS_CY) != 0) ? (byte) 1 : 0;
		cpu.cc.P  = ((PSW & cpu.PSW_FLAG_POS_PA) != 0) ? (byte) 1 : 0;
		cpu.cc.AC = ((PSW & cpu.PSW_FLAG_POS_AC) != 0) ? (byte) 1 : 0;
		cpu.cc.Z  = ((PSW & cpu.PSW_FLAG_POS_ZE) != 0) ? (byte) 1 : 0;
		cpu.cc.S  = ((PSW & cpu.PSW_FLAG_POS_SN) != 0) ? (byte) 1 : 0;

		cpu.A = cpu.memory[(cpu.SP + 1) & 0xffff];
		cpu.SP = (cpu.SP + 2) & 0xffff;
	}

	private void PUSH_PSW(CpuComponents cpu) {
		// A and PSW (formed binary value via flags , plus its filler value)

		cpu.memory[(cpu.SP - 1) & 0xffff] = cpu.A;

		// prepare variable higher than 0xff, but with 0's in bit 0-7
		// this way, it serves as flags' default state waiting to be flipped, like a template
		// also helps to retain flags proper positioning
		int PSW = 0x100;

		// skip pos 5 and 3, it does not need to be flipped since it is by default, a 0 value
		PSW =
			(cpu.cc.S     <<  7)  |   // place sign flag status on pos 7
			(cpu.cc.Z     <<  6)  |   // place zero flag status on pos 6
			(cpu.cc.AC    <<  4)  |   // place aux. carry flag status on pos 4
			(cpu.cc.P     <<  2)  |   // place parity flag status on pos 2
			(1            <<  1)  |
			(cpu.cc.CY    <<  0)  ;   // place carry flag status on pos 0

		cpu.memory[(cpu.SP - 2) & 0xffff] = (short) (PSW & 0xff); // cut to 8 bit after
		cpu.SP = (cpu.SP - 2) & 0xffff;
	}

	private void RAL(CpuComponents cpu) {
		final byte cy = cpu.cc.CY;
		cpu.cc.CY = (byte) ((cpu.A >> 7) & 0xf);
		cpu.A = (short) (((cpu.A << 1) | cy) & 0xff);
	}

	private void RAR(CpuComponents cpu) {
		final byte cy = cpu.cc.CY;
		cpu.cc.CY = (byte) (cpu.A & 1);
		cpu.A = (short) (((cpu.A >> 1) | (cy << 7)) & 0xff);
	}

	private void RET(CpuComponents cpu) {
		int addr = (cpu.memory[(cpu.SP + 1) & 0xffff] << 8) | cpu.memory[cpu.SP];
		cpu.SP = (cpu.SP + 2) & 0xffff;
		cpu.PC = addr;
	}

	private void RLC(CpuComponents cpu) {
		cpu.cc.CY = (byte) (cpu.A >> 7); // get bit 7 as carry
		cpu.A = (short) (((cpu.A << 1) | cpu.cc.CY) & 0xff); // rotate to left, wrapping its content
	}

	private void RRC(CpuComponents cpu) {
		cpu.cc.CY = (byte) (cpu.A & 1); // get bit 0 as carry
		cpu.A = (short) ((cpu.A >> 1) | (cpu.cc.CY << 7) & 0xff); // rotate to right, wrapping its contents by placing bit 0 to bit 7
	}

	private void SBB(CpuComponents cpu, int var) {
		i8080_flag_ac(cpu, cpu.A, -var + -cpu.cc.CY); // half carry
		int res = (cpu.A + (-var + -cpu.cc.CY)) & 0xfff;
		
		flags_zsp(cpu, res);
		cpu.cc.CY = (res > 0xff) ? 1: (byte) 0; // minuend greater than subtrahend will likely result to overflow of 0xff (borrowing)

		cpu.A = (short) (res & 0xff);
	}

	private void SHLD(CpuComponents cpu, int opcode) {
		int addr = (cpu.memory[opcode + 2] << 8) | cpu.memory[opcode + 1];

		cpu.memory[addr + 1] = cpu.H;
		cpu.memory[addr] = cpu.L;
	}

	private void SPHL(CpuComponents cpu, int address) {
		cpu.SP = address;
	}

	private void STA(CpuComponents cpu, int hi_nib, int lo_nib) {
		int addr = (hi_nib << 8) | lo_nib;
		cpu.memory[addr] = cpu.A;
	}

	private void SUB(CpuComponents cpu, int var) {
		i8080_flag_ac(cpu, cpu.A, -var); // half carry

		int res = (cpu.A + (-var) & 0xfff);

		flags_zsp(cpu, res);
		cpu.cc.CY = (res > 0xff) ? 1: (byte) 0; // minuend greater than subtrahend will likely result to overflow of 0xff (borrowing)
		
		cpu.A = (short) (res & 0xff);
	}

	private void XCHG(CpuComponents cpu) {
		// SWAP H and D
		cpu.H = (short) (cpu.H + cpu.D);
		cpu.D = (short) (cpu.H - cpu.D);
		cpu.H = (short) (cpu.H - cpu.D);

		// SWAP L and E
		cpu.L = (short) (cpu.L + cpu.E);
		cpu.E = (short) (cpu.L - cpu.E);
		cpu.L = (short) (cpu.L - cpu.E);
	}

	private void XRA(CpuComponents cpu, int var) {
		cpu.A ^= var;

		flags_zsp(cpu, cpu.A);
		cpu.cc.CY = 0; // fixed value
		cpu.cc.AC = 0; 
		
	}

	private void XTHL(CpuComponents cpu) {
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
	private void flags_zspc(CpuComponents cpu, int result) {
		cpu.cc.CY = (result > 0xff) ? (byte) 1 : 0;
		cpu.cc.Z = ((result & 0xff) == 0) ? (byte) 1 : 0;
		cpu.cc.S = ((result & 0x80) == 0x80) ? (byte) 1 : 0;
		cpu.cc.P = parityFlag(result & 0xff);  // ensuring only checks for 8-bit variable
	}

	private void flags_zsp(CpuComponents cpu, int result) {
		cpu.cc.Z = ((result & 0xff) == 0) ? (byte) 1 : 0;
		cpu.cc.S = ((result & 0x80) == 0x80) ? (byte) 1 : 0;
		cpu.cc.P = parityFlag(result & 0xff);  // ensuring only checks for 8-bit variable
	}

	private void i8080_flag_ac(CpuComponents cpu, int... var) {
		cpu.cc.AC = ( ((var[0] & 0xf) + (var[1] & 0xf)) > 0xf) ? (byte) 1 : (byte) 0;
	}


	private byte parityFlag(int result) {
		int res = 0;
		
		for(int i = 0; i < 8; i++) {
			if (((result >> i) & 0x1) == 1) res++;
		}
		
		return (res % 2 == 0) ? (byte) 1 : 0;
	}

	// Custom
	private short port_in(CpuComponents cpu) {
		int operation = cpu.C;

		if (operation == 2) {
			System.out.printf("%c", cpu.E);
		}
		else if (operation == 9) {
			int addr = (cpu.D << 8) | cpu.E;
			do {
				System.out.printf("%c", cpu.memory[addr++]);
			} while (cpu.memory[addr] != '$');
		}

		return 0xff;
	}
	
	private void port_out() {
		test_finished = true;
	}
}

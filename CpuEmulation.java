import Cpu.*;

public class CpuEmulation
{
	///  DECLARE CPU COMPONENTS  ///
	public static Component B, C, D, E, H, L, A;
	public static Component PC, SP;
	public static Flags Z, S, P, CY, AC;
	private int[] memory;
	
	///  CONSTRUCTOR  ///
	public CpuEmulation(int[] rom) {
		init();
		memory = rom;
		System.out.println("Start emulator...\n");
		
		// Start emulator...
		emulate8080();
		
		System.out.println("\nEnd emulator...");
	}
	
	/////   MAIN EMULATION   /////
	public void emulate8080() {
		while(PC.value < 0x4000) {
			// store opcode
			int opcode = PC.value;
			
			// HL (M)
			int addr = (H.value << 8) | L.value;
			
			// increment PC
			PC.value++;
			
			// print instruction
			printInstruction(opcode);
			
			switch(getInstruction(opcode)) {
				case 0x00:
					break; // NOP
				case 0x01:
					LXI(opcode, B, C);
					break; // LXI B, D16
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
				case 0x0a:
					LDAX(B, C);
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
				case 0x11:
					LXI(opcode, D, E);
					break; // LXI D, D16
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
				case 0x1a:
					LDAX(D, E);
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
				case 0x31:
					LXI(opcode, SP);
					break; // LXI SP, D16
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
				case 0x3b:
					DCX(SP);
					break; // DCX SP
				case 0x3d:
					DCR(A);
					break; // DCR A
				case 0x3e:
					MVI(opcode, A);
					break; // MVI A, D8
				case 0x77:
					memory[addr] = A.value;
					break; // MOV M, A
				case 0xc2:
					if(Z.flag == 0) {
						PC.value = (getInstruction(opcode + 2) << 8) | getInstruction(opcode + 1);
					} else {
						PC.value += 2;
					}
					break; // JNZ adr
				case 0xc3:
					PC.value = (getInstruction(opcode + 2) << 8) | getInstruction(opcode + 1);
					break; // JMP adr
				case 0xcd:
					CALL(opcode);
					break; // CALL adr
				default:
					return;
			}
		}
	}
	
	///  PRINT INST.  ///
	public void printInstruction(int opcode) {
		String inst = null;
		
		switch(getInstruction(opcode)) {
			case 0x00:
				inst = "NOP";
				break;
			case 0x01:
				inst = "LXI B, #" + toHex02(getInstruction(opcode + 2)) + toHex02(getInstruction(opcode + 1));
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
				inst = "MVI B, #" + toHex02(getInstruction(opcode + 1));
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
				inst = "MVI C, #" + toHex02(getInstruction(opcode + 1));
				break;
			case 0x11:
				inst = "LXI D, #" + toHex02(getInstruction(opcode + 2)) + toHex02(getInstruction(opcode + 1));
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
				inst = "MVI D, #" + toHex02(getInstruction(opcode + 1));
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
				inst = "MVI E, #" + toHex02(getInstruction(opcode + 1));
				break;
			case 0x20:
				inst = " - ";
				break;
			case 0x21:
				inst = "LXI H, #" + toHex02(getInstruction(opcode + 2)) + toHex02(getInstruction(opcode + 1));
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
				inst = "MVI H, #" + toHex02(getInstruction(opcode + 1));
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
				inst = "MVI L, #" + toHex02(getInstruction(opcode + 1));
				break;
			case 0x31:
				inst = "LXI SP, #" + toHex02(getInstruction(opcode + 2)) + toHex02(getInstruction(opcode + 1));
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
			case 0x3b:
				inst = "DCX SP";
				break;
			case 0x3d:
				inst = "DCR A";
				break;
			case 0x77:
				inst = "MOV M, A";
				break;
			case 0xc2:
				inst = "JNZ $" + toHex04((getInstruction(opcode+2) << 8) + getInstruction(opcode+1)) + " | ZF: " + Z.flag;
				break;
			case 0xc3:
				inst = "JMP $" + toHex04((getInstruction(opcode+2) << 8) + getInstruction(opcode+1));
				break;
			case 0xcd:
				inst = "CALL $" + toHex04((getInstruction(opcode+2) << 8) + getInstruction(opcode+1));
				break;
			default:
				inst = "" + toHex02(getInstruction(opcode)) + " is not implemented!";
		}
		
		System.out.println("SP: " + toHex04(SP.value) + " | " + toHex04(opcode) + ": " + inst);
	}
	
	
	///  SUBROUTINES  ///
	private void CALL(int opcode) {
		int nextAddr = opcode + 3;
		memory[SP.value - 1] = ((nextAddr >> 8) & 0xff); // Store rightmost 8bit addr
		memory[SP.value - 2] = (nextAddr & 0xff);
		SP.value = (SP.value - 2);
		
		PC.value = (getInstruction(opcode + 2) << 8) | getInstruction(opcode + 1);
	}
	
	private void DCR(Component reg) {
		reg.value--;
		flags_zsp_ac(reg.value);
		reg.value &= 0xff;
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
		reg.value++;
		flags_zsp_ac(reg.value);
		reg.value &= 0xff; // ensure it only takes 8-bit
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
	
	private void LDAX(Component... rp) {
		int addr = (rp[0].value << 8) | rp[1].value;
		A.value = getInstruction(addr);
	}
	
	private void LXI(int opcode, Component... rp) {
		if(rp.length == 2) {
			// Register Pair
			rp[0].value = getInstruction(opcode + 2);	// RP 1 = byte 3
			rp[1].value = getInstruction(opcode + 1);	// RP 2 = byte 2
		} else {
			// 16-bit variable (SP)
			rp[0].value = ((getInstruction(opcode + 2) << 8) | getInstruction(opcode + 1)) & 0xffff;
		}
		
	}
	
	private void MVI(int opcode, Component reg) {
		reg.value = getInstruction(opcode + 1);
	}
	
	
	///  HL SUBROUTINES  ///
	
	private void DCR(int address) {
		memory[address]--;
		flags_zsp_ac(memory[address]);
		memory[address] &= 0xff;
	}
	
	private void INR(int address) {
		memory[address]++;
		flags_zsp_ac(memory[address]);
		memory[address] &= 0xff; // ensure it only takes 8-bit
	}
	
	private void MVI(int opcode, int address) {
		memory[address] = getInstruction(opcode + 1);
	}
	
	///  FLAGS  ///
	private void flags_zsp_ac(int result) {
		Z.flag = (result == 0) ? (byte) 1 : 0;
		S.flag = ((result & 0x80) == 0x80) ? (byte) 1 : 0;
		P.flag = parityFlag(result);
		AC.flag = (result > 0x9) ? (byte) 1 : 0;
	}
	
	private byte parityFlag(int result) {
		int res = Integer.toBinaryString(result).replaceAll("0", "").length();
		return (res % 2 == 0) ? (byte) 1 : 0;
	}
	
	// Get instruction
	private int getInstruction(int pc) {
		return memory[pc];
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

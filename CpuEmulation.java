import Cpu.*;

public class CpuEmulation
{
	///  DECLARE CPU COMPONENTS  ///
	public static Component B, C, D, E, H, L, A;
	public static Component PC, SP;
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
			
			// increment PC
			PC.value++;
			
			// print instruction
			printInstruction(opcode);
			
			switch(mem(opcode)) {
				case 0x00:
					break; // NOP
				case 0xc3:
					PC.value = (mem(opcode + 2) << 8) | mem(opcode + 1);
					break; // JMP adr
				default:
					return;
			}
		}
	}
	
	///  PRINT INST.  ///
	public void printInstruction(int opcode) {
		String inst = null;
		
		switch(mem(opcode)) {
			case 0x00:
				inst = "NOP";
				break;
			case 0xc3:
				inst = "JMP " + toHex04((mem(opcode+2) << 8) + mem(opcode+1));
				break;
			default:
				inst = "" + toHex02(mem(opcode)) + " is not implemented!";
		}
		
		System.out.println(toHex04(opcode) + ": " + inst);
	}
	
	///  Subroutines  ///
	
	// Get instruction
	private int mem(int pc) {
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
	}
	
	private String toHex04(int value) {
		return String.format("%04x", value);
	}
	
	private String toHex02(int value) {
		return String.format("%02x", value);
	}
}

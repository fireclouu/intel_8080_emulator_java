
public class Emulation
{
	///  DECLARE CPU COMPONENTS  ///
	public CpuComponents cpu;

	/// PRINT INSTRUCTION
	PrintTrace pTrace;
	
	///  INTERPRETER  ///
	public Interpreter interpreter;

	public Emulation(short memory[]) {
		System.out.println("Start emulator...\n");
		
		init(memory);
		startEmulation();
		
		System.out.println("\nEnd emulator...");
	}

	public void startEmulation() {

		// loop flag
		boolean done = false;
		int opcode;
		
		while(!done) {		
			opcode = cpu.PC;
			
			// print instruction
			pTrace.printInstruction(opcode, true);
			
			// emulation
			interpreter.emulate8080(cpu.PC);
		}
	}
	
	private void GenerateInterrupt() {
		
	}
	
	private void init(short memory[]) {
		cpu = new CpuComponents(Main.PROGRAM_LENGTH, memory);
		interpreter = new Interpreter(memory, cpu);
		pTrace = new PrintTrace(this.cpu);
	}
}

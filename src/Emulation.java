import Cpu.*;

public class Emulation
{
	///  DECLARE CPU COMPONENTS  ///
	public CpuComponents cpu;

	///  INTERPRETER  ///
	public Interpreter interpreter;

	public Emulation(short memory[]) {
		init(memory);
		startEmulation();
	}

	public void startEmulation() {

		// run flag
		boolean done = false;

		int a; // breakpoint
		while(!done) {
			interpreter.emulate8080(cpu.PC);
		}
	}
	
	private void init(short memory[]) {
		cpu = new CpuComponents(Main.PROGRAM_LENGTH, memory);
		interpreter = new Interpreter(memory, cpu);
	}
}

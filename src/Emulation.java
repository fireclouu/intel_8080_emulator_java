
import java.time.*;
import java.util.*;
import java.util.concurrent.*;

public class Emulation
{
	///  DECLARE CPU COMPONENTS  ///
	public CpuComponents cpu;
	
	///  TIMER  ///
	long now = 0; // init
	long lastTime = 0; // init
	
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
		
		// Run @ 2 MHz
		while(!done) {	
			// 2MHz = execute every 5e-7 secs
			// System.nanoTime() = billionth of a sec. (epoch)
			// nano / 1000 = millionth of a sec
			
			now = getusec();	
			
			if (lastTime == 0) {
				lastTime = now;
			}
			
			// measured in microseconds
			long elapse = now - lastTime;
			long cycle_needed = 2 * elapse;
			long cycle = 0; // reset every succeeding usec passed
			
			while(cycle_needed > cycle) {
				opcode = cpu.PC;
			
				// print instruction
				pTrace.printInstruction(opcode, true);
			
				// emulation
				cycle += interpreter.emulate8080(cpu.PC);
			}
			
			lastTime = now;
		}
	}
	
	private long getusec() {
		return TimeUnit.MICROSECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS);
	}
	
	private void GenerateInterrupt() {
		
	}
	
	private void init(short memory[]) {
		cpu = new CpuComponents(Main.PROGRAM_LENGTH, memory);
		interpreter = new Interpreter(memory, cpu);
		pTrace = new PrintTrace(this.cpu);
	}
}

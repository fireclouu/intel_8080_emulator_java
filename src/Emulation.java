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
	long million = 1_000_000; // microsec template
	
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
	
	int counter = 0;
	public void startEmulation() {

		// loop flag
		boolean done = false;
		
		// Run @ 2 MHz (verify since untested)
		while(!done) {	
			// 2MHz = execute every 5e-7 secs
			// System.nanoTime() = billionth of a sec. (epoch)
			now = getMicroSec();
			
			if (lastTime == 0) {
				lastTime = now;
			}
			
			// measured in microseconds
			long elapse = now - lastTime;
			int cycle_needed = (int) (2 * elapse);
			
			int cycles = 0; // reset every succeeding usec passed
			
			while(cycle_needed > cycles) {
				// print instruction
			//	pTrace.printInstruction(cpu, cpu.PC, true); // enabling this slows (?) emulation
			
				// emulation
				cycles += interpreter.emulate8080(this.cpu);
			}
		
			lastTime = now;
		}
	}
	
	private long getMicroSec() {
		long microSec = System.nanoTime(); // static variable!
		return (long) ((System.currentTimeMillis() * 1e3) + (microSec - (million * (microSec / million))) / 1000);
	}
	
	private void init(short memory[]) {
		cpu = new CpuComponents(memory);
		interpreter = new Interpreter(cpu);
		pTrace = new PrintTrace();
	}
}

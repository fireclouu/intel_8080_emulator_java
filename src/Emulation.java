import java.time.*;
import java.util.*;
import java.util.concurrent.*;

public class Emulation
{
	///  DECLARE CPU COMPONENTS  ///
	public CpuComponents cpu;
	
	///  TIMER and INTERRUPT  ///
	long timerNow = 0; // init
	long timerLastRecord = 0; // init
	long million = 1_000_000; // microsec template
	byte whichInterrupt;
	long nextInterrupt;
	
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
			timerNow = getMicroSec();
			
			if (timerLastRecord == 0) {
				timerLastRecord = timerNow;
				nextInterrupt = timerLastRecord + 16_000;
				whichInterrupt = 1;
			}
			
			if (cpu.int_enable && (timerNow > nextInterrupt)){
				if (whichInterrupt == 1) {
					interpreter.GenerateInterrupt(cpu, (byte) 1);
					whichInterrupt = 2;
				} else {
					interpreter.GenerateInterrupt(cpu, (byte) 2);
					whichInterrupt = 1;
				}
				
				nextInterrupt = (long) (timerNow + 8000.0);
			}
			
			// measured in microseconds
			long elapse = timerNow - timerLastRecord;
			long cycle_needed = (elapse * 2);
			
			int cycles = 0; // reset every succeeding usec passed
			
			/// while((cycles < cycle_needed)) {
				// print instruction
				pTrace.printInstruction(cpu, cpu.PC, true); // enabling this slows (?) emulation
				
				// emulation
				cycles += interpreter.emulate8080(cpu);
			//}
			timerLastRecord = timerNow;
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

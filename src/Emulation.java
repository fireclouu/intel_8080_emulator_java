import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.apache.http.util.*;

public class Emulation
{
	///  DECLARE CPU COMPONENTS  ///
	public CpuComponents cpu;
	
	///  TIMER and INTERRUPT  ///
	long timerNow = 0; // init
	long timerLastRecord = 0; // init
	long usec = 1_000_000; // microsec template
	byte whichInterrupt;
	long nextInterrupt;
	
	int refreshRate = (int) ((1 / 60) * usec);
	
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
		
		// Run @ 2 MHz (verify since untested)
		while(!done) {	
			// 2MHz = execute every 5e-7 secs
			// System.nanoTime() = billionth of a sec. (epoch)
			/*
			timerNow = getMicroSec();
			
			if (timerLastRecord == 0) {
				timerLastRecord = timerNow;
				nextInterrupt = timerLastRecord + refreshRate;
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
			*/
			//while((cycles < cycle_needed)) {
				// print instruction
				
				// pTrace.printInstruction(cpu, cpu.PC, Main.printLess); // enabling this slows (?) emulation
				
				// emulation
				
				// FOR TESTS
	//private void run_test(CpuComponents cpu) {
		
		interpreter.i8080_init(cpu);
		
			// SOURCE: superzazu — intel 8080 c99
			// inject "out 1,a" at 0x0000 (signal to stop the test)
			cpu.memory[0x0000] = 0xD3;
			cpu.memory[0x0001] = 0x00;

			// inject "in a,0" at 0x0005 (signal to output some characters)
			cpu.memory[0x0005] = 0xDB;
			cpu.memory[0x0006] = 0x00;
			cpu.memory[0x0007] = 0xC9;
		
			// jump pc to 0x100 (to avoid executing test_finished to true);
			cpu.PC = ProgramUtils.Rom.ROM_ADDRESS[0];
			
		interpreter.test_finished = false;
		
		
		
		
		// debugger
		
		long nb = 0;
		
		int offset = 2000; // dont change over time
		int cyc_debug = 23_610;
		
		//int low = 49837;
		//int expect = low + 1;
		while(!interpreter.test_finished) {
			// loop terminated by port_out
			/*
			if(interpreter.cycle > cyc_debug + offset) {
			} else if (interpreter.cycle > cyc_debug) {
				pTrace.printInstruction(cpu, cpu.PC, Main.printLess);
			}
			*/
			/*
			if ((interpreter.cycle >= low) & (interpreter.cycle == expect)) {
				System.out.println("ok!");
				return;
			} else if (interpreter.cycle >= low + offset) {
				System.out.println("failing...");
				return;
			}
			*/
			//pTrace.printInstruction(cpu, ProgramUtils.Machine.PRINT_LESS);
			interpreter.cycle += interpreter.emulate8080(cpu);
			//nb += interpreter.cycle;
			//s = i.next();
		}
		
		//return;
		
		// test end
//	}
					// NORMAL RUNS
//				pTrace.printInstruction(cpu, cpu.PC, Main.printLess);
//				interpreter.emulate8080(cpu);
			//}
			
			// timerLastRecord = timerNow;
		}
	}
	
	private long getMicroSec() {
		long microSec = System.nanoTime(); // static variable!
		return (long) ((System.currentTimeMillis() * 1e3) + (microSec - (usec * (microSec / usec))) / 1000);
	}
	
	private void init(short memory[]) {
		cpu = new CpuComponents(memory);
		interpreter = new Interpreter(cpu);
		pTrace = new PrintTrace();
	}
}

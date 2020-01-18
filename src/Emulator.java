
import java.util.Date;
import BaseClass.AppUtils;
import BaseClass.DisplayAdapter;

public class Emulator
{
	Interpreter interpreter;
	PrintTrace print;
	
	private final long USEC = 1_000_000; // template
	private final long REFRESH_RATE = (int) ((1 / 60) * USEC);
	
	private long timerNow = 0;
	private long timerLastRecord = 0;
	private byte whichInterrupt;
	private long nextInterrupt;
	
	private boolean test = false;
	
	public Emulator() {
		init();
	}
	
	public void startEmulation(CpuComponents cpu, DisplayAdapter display) {
		System.out.println("Start emulator...\n");
		AUTO_TEST(cpu);
		
		// loop flag
		boolean done = false;
		// Run @ 2 MHz (verify since untested)
		// loop here!
		// while(!done) {
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
		if (test) {
			while(!interpreter.test_finished) {
				interpreter.emulate8080(cpu);
				//print.printInstruction(cpu, AppUtils.Machine.PRINT_LESS);
			}
		} else {
			while(true) {
				interpreter.emulate8080(cpu);
				print.printInstruction(cpu, AppUtils.Machine.PRINT_LESS);
			}
		}
					// NORMAL RUNS
//				pTrace.printInstruction(cpu, cpu.PC, Main.printLess);
//				interpreter.emulate8080(cpu);
			//}
			
			// timerLastRecord = timerNow;
		//}
		System.out.println("\n\nEND: " + new Date().toString());
		System.out.println("\nEnd emulator...");
	}
	
	private long getMicroSec() {
		long microSec = System.nanoTime(); // static variable!
		return (long) ((System.currentTimeMillis() * 1e3) + (microSec - (USEC * (microSec / USEC))) / 1000);
	}
	
	private void init() {
		interpreter = new Interpreter();
		print = new PrintTrace();
	}
	
	// CPU OVERRIDE

	private void AUTO_TEST(CpuComponents cpu) {
		switch (AppUtils.File.FILE_NAME[0]) {
			case "cpudiag.bin":
			case "8080EX1.COM":
			case "8080EXER.COM":
			case "CPUTEST.COM":
			case "8080EXM.COM":
			case "8080PRE.COM":
			case "TST8080.COM":
				TEST_OVERRIDE_GENERIC(cpu);
				dialog();
				debug_injects(cpu);
				test = !test;
				break;
		}
	}
	
	private void debug_injects(CpuComponents cpu) {
		// SOURCE: superzazu — intel 8080 c99
		// inject "out 1,a" at 0x0000 (signal to stop the test)
		cpu.memory[0x0000] = 0xD3;
		cpu.memory[0x0001] = 0x00;
		// inject "in a,0" at 0x0005 (signal to output some characters)
		cpu.memory[0x0005] = 0xDB;
		cpu.memory[0x0006] = 0x00;
		cpu.memory[0x0007] = 0xC9;
		// jump pc to 0x100 (to avoid executing test_finished to true);
		cpu.PC = AppUtils.File.ROM_ADDRESS[0];

		interpreter.test_finished = false;
	}
	
	
	private void dialog() {
		if (!AppUtils.Machine.DEBUG) System.out.println("debug is off!");
		System.out.println("CPU EXERCISER \nSTART:  " + new Date().toString());
		System.out.println();
		PAUSE_THREAD(1000);
	}
	
	private void TEST_OVERRIDE_GENERIC(CpuComponents cpu) {
		cpu.PC = AppUtils.File.ROM_ADDRESS[0];
	}
	
	public static void PAUSE_THREAD(int mills) {
		try {
			Thread.sleep(mills);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

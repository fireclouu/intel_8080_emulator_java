
import BaseClass.*;
import java.util.*;

public strictfp class Emulator
{
	Interpreter interpreter;
	PrintTrace print;
	
	///  INTERRUPTS  ///
	private final double USEC = 1_000_000; // template
//	private final long REFRESH_RATE = (int) ((1 / 60) * USEC);
//	
	private final double expectedExec = 1.0/2_000_000;
	private double timerNow = 0;
	private double timerLast = 0;
	private double customMhz = (expectedExec * (USEC * 10)) ;
	
	public Emulator() {
		init();
	}
	
	private void init() {
		interpreter = new Interpreter();
		print = new PrintTrace();
	}
	
	public void startEmulation(CpuComponents cpu, DisplayAdapter display) {
		
		if (AppUtils.Machine.DEBUG) {
			runTests(cpu);
			return;
		}

		boolean done = false;
		while (!done) {
			// run emulation here
			
			double checkNow = 0;
			double checkLast = 0;
			int sys_cycle = 0;
			// steady 2 MHz
			while(true) {
				timerNow = getNano();
				checkNow = timerNow;
				
				while((checkNow > checkLast + (USEC)) && interpreter.cycle <= 2_000_000) {
					interpreter.cycle += interpreter.emulate8080(cpu);
					sys_cycle++;
				}

				if (checkNow > checkLast + (USEC)) {
					interpreter.cycle = 0; // reset
					sys_cycle = 0;
					checkLast = checkNow;
				}
				
				if(timerNow >= timerLast + customMhz) {
					interpreter.cycle += interpreter.emulate8080(cpu);
					sys_cycle++;
					timerLast = timerNow;
				}
				//System.out.println(expectedExec * ((USEC) * 10));
				//print.printInstruction(cpu, AppUtils.Machine.PRINT_LESS);
				
				// catchup cycle
				
				
			}
		}
	}
	
	private long getNano() {
		return System.nanoTime() / 1_000;
	}
	
	// DEBUGGING
	public static void PAUSE_THREAD(int mills) {
		try {
			Thread.sleep(mills);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void runTests(CpuComponents cpu) {
		System.out.println("Test: " + PlatformAdapter.TEST_NAME + "\nSTART: " + AppUtils.getTime());
		System.out.println("______________________________");
		
		addMsg("Test: " + PlatformAdapter.TEST_NAME + "\nSTART: " + AppUtils.getTime());
		addMsg("______________________________\n");
		
		while(!interpreter.test_finished) {
			interpreter.cycle += interpreter.emulate8080(cpu);
			// print.printInstruction(cpu, AppUtils.Machine.PRINT_LESS);
			print.check_overflow(cpu);
		}

		System.out.println();
		System.out.println("______________________________");
		System.out.println("END:   " + AppUtils.getTime());
		System.out.println("\n***\n");

		addMsg("\n______________________________");
		addMsg("END:   " + AppUtils.getTime());
		addMsg("\n***\n");
	}
	
	// Builder
	private void addMsg(char c) {
		PlatformAdapter.BUILD_MSG[PlatformAdapter.TEST_INDEX] += c;
	}
	private void addMsg(String str) {
		PlatformAdapter.BUILD_MSG[PlatformAdapter.TEST_INDEX] += str + "\n";
	}
}

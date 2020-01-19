
import BaseClass.*;
import java.util.*;

public class Emulator
{
	Interpreter interpreter;
	PrintTrace print;
	
	///  INTERRUPTS  ///
//	private final long USEC = 1_000_000; // template
//	private final long REFRESH_RATE = (int) ((1 / 60) * USEC);
//	
//	private long timerNow = 0;
//	private long timerLastRecord = 0;
//	private byte whichInterrupt;
//	private long nextInterrupt;
	
	public Emulator() {
		init();
	}
	
	public void startEmulation(CpuComponents cpu, DisplayAdapter display) {
		
		if (AppUtils.Machine.DEBUG) {
			testrun(cpu);
			return;
		}

		boolean done = false;
		while (!done) {
			// devise 2 MHz cycle
			
				// run emulation here
			
			while(true) {
				interpreter.emulate8080(cpu);
				print.printInstruction(cpu, AppUtils.Machine.PRINT_LESS);
			}
			
			// update display here
			
			// check interrupt here
			
		}
	}
	
	private void init() {
		interpreter = new Interpreter();
		print = new PrintTrace();
	}
	
//	private long getMicroSec() {
//		long microSec = System.nanoTime(); // static variable!
//		return (long) ((System.currentTimeMillis() * 1e3) + (microSec - (USEC * (microSec / USEC))) / 1000);
//	}
	
	// DEBUGGING
	public static void PAUSE_THREAD(int mills) {
		try {
			Thread.sleep(mills);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void testrun(CpuComponents cpu) {
		System.out.println("Test: " + PlatformAdapter.TEST_NAME + "\nSTART: " + gettime());
		System.out.println("______________________________");

		while(!interpreter.test_finished) {
			interpreter.cycle += interpreter.emulate8080(cpu);
			debug_check_overflow(cpu);
		}

		System.out.println();
		System.out.println("______________________________");
		System.out.println("END:   " + gettime());
		System.out.println("\n***");
	}
	
	private void debug_check_overflow(CpuComponents cpu) {
		if (cpu.A > 0xff | cpu.B > 0xff | cpu.C > 0xff | cpu.D > 0xff |
			cpu.E > 0xff | cpu.H > 0xff | cpu.L > 0xff | cpu.PC > 0xffff | cpu.SP > 0xffff |
			cpu.cc.AC > 0x1 | cpu.cc.CY > 0x1 | cpu.cc.P > 0x1 |
			cpu.cc.S > 0x1 | cpu.cc.Z > 0x1) {
			print.printInstruction(cpu, AppUtils.Machine.PRINT_LESS);
		}
	}

	private String gettime() {
		Date date = new Date();
		return String.format("%02d", date.getHours()) + ":" +
			String.format("%02d", date.getMinutes()) + ":" +
			String.format("%02d", date.getSeconds());
	}
}

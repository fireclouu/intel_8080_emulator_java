package BaseClass;

import java.io.*;
import CpuComponents;
import Emulator;

public abstract class PlatformAdapter implements Runnable
{
	private Thread mainThread;
	protected Emulator emulator;
	protected short[] file;
	protected AppUtils.File fileUtils;
	protected AppUtils.Machine machineUtils;

	protected CpuComponents cpu;
	protected DisplayAdapter display;

	public static String OUT_MSG = "System OK!";
	
	public static String TEST_NAME;
	public static int TEST_INDEX = 0;
	
	// Stream file
	public abstract InputStream openFile(String romName);
	public abstract void createDisplay();
	
	@Override
	public void run() {
		emulator.startEmulation(cpu, display);
	}

	// Main
	public void startOp() {
		// check if file is test file
		if (isTestFile()) {
			startTest();
			return;
		}
		
		// Start emulation
		startMain();
	}

	public void init() {
		// initialize cpu components
		cpu = new CpuComponents();
		
		// initialize emulator
		emulator = new Emulator();
	
		// identify and create display
		createDisplay();
		
		// display draw signal
		display.readyToDraw = false;
	}
	
	private void startMain() {
		// reset objects
		init();
		
		// load and start emulation
		if(loadSplitFiles() == 0) {
			mainThread = new Thread(this);
			mainThread.start();
		}
	}
	
	private void startTest() {
		AppUtils.Machine.DEBUG = true;
		
		for(String name : fileUtils.FILES) {
			TEST_NAME = name;

			// reset objects
			init();

			// load file and injects
			// SOURCE: superzazu — intel 8080 c99
			// inject "out 1,a" at 0x0000 (signal to stop the test)
			cpu.memory[0x0000] = 0xD3;
			cpu.memory[0x0001] = 0x00;
			// inject "in a,0" at 0x0005 (signal to output some characters)
			cpu.memory[0x0005] = 0xDB;
			cpu.memory[0x0006] = 0x00;
			cpu.memory[0x0007] = 0xC9;
			// jump pc to 0x100 (to avoid executing test_finished to true);
			cpu.PC = 0x0100;

			loadFile(name, 0x100);

			// start emulation
			mainThread = new Thread(this);
			mainThread.start();

			// avoid overlaps
			try {
				mainThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	}
	
	// Read and buffer file
	public void loadFile(String filename, int addr) {
		// read file stream
		InputStream file = openFile(filename);
		
		// piece container
		short read;
		
		try
		{
			while ((read = (short) file.read()) != -1) {
				cpu.memory[addr++] = read;
			}
		} catch (IOException e) {
			OUT_MSG = filename + " cannot be read!";
		}
			
	}
	
	private byte loadSplitFiles() {
		int counter = 0;
		
		for(String files : fileUtils.FILES) {
			if (isAvailable(files)) {
				loadFile(files, fileUtils.ROM_ADDRESS[counter++]);
			} else {
				display.readyToDraw = true;
				System.out.println(OUT_MSG);
				return 1; // ERROR
			}
		}
		
		return 0; // SUCCESS
	}
	
	// File check
	private boolean isAvailable(String filename) {

		if (fileUtils.FILES.length == 0) {
			OUT_MSG = "No files specified.";
			return false;
		}

		if (fileUtils.ROM_ADDRESS.length == 0) {
			OUT_MSG = "File online, but no starting memory address specified.";
			return false;
		}

		if (fileUtils.ROM_ADDRESS.length != fileUtils.FILES.length) {
			OUT_MSG = "File online, but roms and memory address unaligned.";
			return false;
		}

		try
		{
			if (openFile(filename) == null) {
				OUT_MSG = "File \"" + filename + "\" could not be found.";
				return false;
			}

		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
			return false;
		}

		OUT_MSG = "File online , loaded successfully!";

		return true;
	}
	
	private boolean isTestFile() {
		for(String name : fileUtils.FILES) {
			switch (name) {
				case "cpudiag.bin":
				case "8080EX1.COM":
				case "8080EXER.COM":
				case "CPUTEST.COM":
				case "8080EXM.COM":
				case "8080PRE.COM":
				case "TST8080.COM":
					return true;
			}
		}
		
		return false;
	}
}

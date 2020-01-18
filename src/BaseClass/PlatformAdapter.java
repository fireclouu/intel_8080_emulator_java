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

	// Stream file
	public abstract InputStream openFile(String romName);
	public abstract void makeDisplay();

	@Override
	public void run() {
		emulator.startEmulation(cpu, display);
	}

	// Main
	public void startOp() {
		if (!isAvailable(fileUtils.FILE_NAME)) {
			display.isMemLoaded = true;
			System.out.println(OUT_MSG);
			return;
		}

		init(); // let override instantiation first before calling these
		mainThread.start();
	}

	public void init() {
		file = loadFile(fileUtils.FILE_NAME);
		this.cpu = new CpuComponents(file);
		mainThread = new Thread(this);
		emulator = new Emulator();
		makeDisplay();

		display.isMemLoaded = false;
	}

	// Read and buffer file
	public short[] loadFile(String[] filename) {
		short[] holder = new short[machineUtils.PROGRAM_LENGTH];
		InputStream file;
		short read;
		int addr;

		for (int i = 0; i < fileUtils.FILE_NAME.length; i++) {
			file = openFile(fileUtils.FILE_NAME[i]);
			addr = fileUtils.ROM_ADDRESS[i];

			try {
				while ((read = (short) file.read()) != -1) {
					holder[addr++] = read;
				}
			} catch (IOException e) {
				OUT_MSG = fileUtils.ROM_ADDRESS[i] + " cannot be read!";
				return null;
			}
		}

		return holder;
	}

	// Check file availability
	private boolean isAvailable(String[] filename) {

		if (fileUtils.FILE_NAME.length == 0) {
			OUT_MSG = "No files specified.";
			return false;
		}

		if (fileUtils.ROM_ADDRESS.length == 0) {
			OUT_MSG = "File online, but no starting memory address specified.";
			return false;
		}

		if (fileUtils.ROM_ADDRESS.length != fileUtils.FILE_NAME.length) {
			OUT_MSG = "File online, but roms and memory address unaligned.";
			return false;
		}

		try
		{
			for (int i = 0; i < fileUtils.FILE_NAME.length; i++) {
				if (openFile(fileUtils.FILE_NAME[i]) == null) {
					OUT_MSG = "File \"" + fileUtils.FILE_NAME[i] + "\" could not be found.";
					return false;
				}
			}

		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
			return false;
		}

		OUT_MSG = "File online , loaded successfully!";

		return true;
	}
}

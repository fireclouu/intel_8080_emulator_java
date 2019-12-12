import java.util.*;
import java.io.*;
import Cpu.*;

public class Main
{
	static CpuEmulation cpu;
	
	final static String STORAGE_INTERNAL = "/sdcard/AppProjects/raw8080emu/src/";
	final static String FILE_NAME = "invaders";
	
	public static void main(String[] args) {
		startEmulator(FILE_NAME);
	}
	
	// MAIN
	public static void startEmulator(String romName) {
		initRom(romName);
		cpu = new CpuEmulation(loadRom(romName));
	}
	
	// ROM
	private static void initRom(String romName) {
		try
		{
			RomInfo.title = romName;
			RomInfo.length = getFileLength(romName);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	// Load ROM
	private static int[] loadRom(String romName) {
		InputStream file = null;
		int tmp[] = new int[RomInfo.length];
		
		try
		{
			file = new FileInputStream(STORAGE_INTERNAL + romName);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}

		try
		{
			for(int i = 0; i < RomInfo.length; i++) {
				tmp[i] = file.read();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return tmp;
	}
	
	// Return file length
	private static int getFileLength(String romName) throws FileNotFoundException, IOException {
		FileInputStream file = new FileInputStream(STORAGE_INTERNAL + romName);
		int size = 0;
		
		while(file.read() != -1) {
			size++;
		}
		
		return size;
	}
	
	
}

import java.util.*;
import java.io.*;
import Cpu.*;

public class Main
{
	public static final int PROGRAM_LENGTH = 0x4000;
	
	static CpuEmulation cpu;
	
	final static String STORAGE_INTERNAL = "/sdcard/AppProjects/raw8080v2/src/";
	final static String FILE_NAME = "invaders";
	
	public static void main(String[] args) {
		startEmulator(FILE_NAME);
	}
	
	// MAIN
	public static void startEmulator(String romName) {
		initRom(romName);
		cpu = new CpuEmulation(loadSplitRom());
	}
	
	// ROM
	private static void initRom(String romName) {
			RomInfo.title = romName;
			RomInfo.length = PROGRAM_LENGTH;
	}
	
	// Load ROM
	private static int[] loadRoms(String romName) {
		InputStream file = null;
		int tmp[] = new int[PROGRAM_LENGTH];
		
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
	
	// LOAD SPLITROM
	private static int[] loadSplitRom() {
		String[] romName = {
			"invaders.h", "invaders.g", "invaders.f", "invaders.e"
		};
		
		int[] romAddr = {
			0x0, 0x800, 0x1000, 0x1800
		};
		
		// prepare empty container
		int[] holder = new int[PROGRAM_LENGTH];
		
		for(int i = 0; i < romName.length; i++) {
			InputStream is = openFile(romName[i]);
			int readFile = 0;
			
			int currentAddr = romAddr[i];
			
			try
			{
				int counter = 0;
				
				while ((readFile = is.read()) != -1)
				{
					holder[currentAddr + counter] = readFile;
					counter++;
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		return holder;
	}
	
	// Inputstream
	private static InputStream openFile(String romName) {
		try
		{
			return new FileInputStream(STORAGE_INTERNAL + romName);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			System.exit(0);
		}
		
		return null;
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

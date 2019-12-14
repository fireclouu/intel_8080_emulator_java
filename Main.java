import java.util.*;
import java.io.*;
import Cpu.*;

public class Main
{
	public static final int PROGRAM_LENGTH = 0x4000;
	static final String[] romName = {
		"invaders.h", "invaders.g", "invaders.f", "invaders.e"
	};
	static final int[] romAddr = {
		0x0, 0x800, 0x1000, 0x1800
	};
	
	
	private static CpuEmulation cpu;
	
	final static String STORAGE_INTERNAL = "/sdcard/AppProjects/raw8080v2/src/";
	final static String FILE_NAME = "invaders";
	
	public static void main(String[] args) {
		startEmulator(FILE_NAME);
	}
	
	// MAIN
	public static void startEmulator(String romName) {
		if(fileExist()) {
			System.out.println("File online!");
		} else {
			System.out.println("File could not be found!");
			return;
		}
		
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
			
			for(int i = 0; i < RomInfo.length; i++) {
				tmp[i] = file.read();
			}
		}
		catch (FileNotFoundException e)
		{
			System.out.println("File/s not found.");
			return null;
		}
		catch (IOException e)
		{
			System.out.println("File cannot be read!");
			return null;
		}

		return tmp;
	}
	
	// LOAD SPLITROM
	private static int[] loadSplitRom() {
		
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
	
	// Early checks
	private static boolean fileExist() {
		for(int i = 0; i < romName.length; i++) {
			if(openFile(romName[i]) == null) {
				return false;
			}
		}
		
		return true;
	}
	
	// Inputstream
	private static InputStream openFile(String romName) {
		try
		{
			return new FileInputStream(STORAGE_INTERNAL + romName);
		}
		catch (FileNotFoundException e)
		{
			return null;
		}
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

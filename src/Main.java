import java.util.*;
import java.io.*;

public class Main
{
	///   ROM LENGTH   ///
	public  static final int PROGRAM_LENGTH = 0x10_000;
	private static final String STORAGE_INTERNAL = "~/src/";	// Change file's path

	///   SPLIT ROMS LIST   ///
	static final String[] romName = {
		"invaders.h",
		"invaders.g",
		"invaders.f",
		"invaders.e"
		// "cpudiag.bin"
		// "8080EX1.COM"
	};
	
	///   LOAD ADDRESS   ///
	static final int[] romAddr = {
		0x0000,
		0x0800,
		0x1000,
		0x1800
		// 0x0100
	};
	
	// MAIN
	public static void main(String[] args) {
		startEmulator();
	}
	
	// EMULATION
	public static void startEmulator() {
		if(fileExist(romName)) {
			System.out.println("File online!");
		} else {
			System.out.println("File could not be found!");
			return;
		}
		
		new Emulation(loadRom(romName));
	}
	
	// LOAD ROM
	private static short[] loadRom(String... fileName) {
		// Prepare empty container
		short[] holder = new short[PROGRAM_LENGTH];
		
		if (fileName.length >= 2) {
			
			for(int i = 0; i < romName.length; i++) {
				InputStream file = openFile(romName[i]);
				short readFile = 0;
				int currentAddr = romAddr[i];
			
				try	{
					int counter = 0;
				
					while ((readFile = (short) file.read()) != -1) {
						holder[currentAddr + counter] = readFile;
						counter++;
					}
				
				} catch (IOException e) {
					System.out.println(romAddr[i] + " cannot be read!");
					return holder;
				}
			}
			
		} else {
			try {
				InputStream file = openFile(fileName[0]);
				short readFile = 0;
				int counter = 0;
				
				while ((readFile = (short) file.read()) != -1) {
					holder[counter] = readFile;
					counter++;
				}

			} catch (IOException e) {
				System.out.println(romName + " cannot be read!");
				return holder;
			}
		}
		
		return holder;
	}
	
	// FILE EXISTENCE CHECK
	private static boolean fileExist(String... fileName) {
		if(fileName.length >= 2) {
			for(int i = 0; i < romName.length; i++) {
				
				if (openFile(romName[i]) == null) {
					return false;
				}
			}
		} else {
			
			if (openFile(fileName[0]) == null) {
				return false;
			}
		}
		
		return true;
	}
	
	// FILE SUBROUTINE
	private static InputStream openFile(String romName) {
		try {
			return new FileInputStream(STORAGE_INTERNAL + romName);
		} catch (FileNotFoundException e) {
			return null;
		}
	}
	
	// ACTUAL ROM FILELENGTH
	private static int getFileLength(String romName) {
		InputStream file = openFile(romName);
		int size = 0;
		
		try
		{
	
			while (file.read() != -1) {
				size++;
			}
			
		} catch (IOException e) {
			return -1;
		}

		return size;
	}
}

// unified variable names related to file handling

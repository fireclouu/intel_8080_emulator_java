import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;

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
		//0x0100
	};
	
	// MAIN
	public static void main(String[] args) {
		startEmulator();
	}
	
	// EMULATION
	public static void startEmulator() {
		if(!fileExist(romName)) {
			return;
		}
		
		new Emulation(loadRom(romName));
	}
	
	// LOAD ROM
	private static short[] loadRom(String[] fileName) {
		// Prepare empty container
		short[] holder = new short[PROGRAM_LENGTH];
		
		if (fileName.length > 1) {
			
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
					holder[romAddr[0] + counter] = readFile;
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
	private static boolean fileExist(String[] fileName) {
		try {
			if(fileName.length > 1) {
				for(int i = 0; i < romName.length; i++) {
				
					if (openFile(romName[i]) == null) {
						System.out.println("File " + romName[i] + " could not be found.");
						return false;
					}
				}
			} else {
			
				if (openFile(fileName[0]) == null) {
					System.out.println("No files specified..");
					return false;
				}
			}
			
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("No files specified.");
			return false;
		}
		
		if (romAddr.length == 0) {
			System.out.println("File online, but no starting memory address specified.");
			return false;
		}
		
		if (romAddr.length != romName.length) {
			System.out.println("File online, but roms and memory address unaligned.");
			return false;
		}
		
		System.out.println("File online , loaded successfully!");
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

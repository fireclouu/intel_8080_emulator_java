import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.util.*;

public class Main
{
	
	// MAIN
	public static void main(String[] args) {
		startEmulator();
	}
	
	// EMULATION
	public static void startEmulator() {
		if(!fileExist(ProgramUtils.Rom.FILE_NAME)) {
			return;
		}
		
		new Emulation(loadRom(ProgramUtils.Rom.FILE_NAME));
	}
	
	// LOAD ROM
	private static short[] loadRom(String[] fileName) {
		InputStream file;
		short read;
		short[] holder = new short[ProgramUtils.Machine.PROGRAM_LENGTH];
		
		if (fileName.length > 1) {
			
			for(int i = 0; i < ProgramUtils.Rom.FILE_NAME.length; i++) {
				file = openFile(ProgramUtils.Rom.FILE_NAME[i]);
				int currentAddr = ProgramUtils.Rom.ROM_ADDRESS[i];
			
				try	{
					for(int a = 0; (read = (short) file.read()) != -1; a++) {
						holder[currentAddr + a] = read;
					}
				
				} catch (IOException e) {
					System.out.println(ProgramUtils.Rom.ROM_ADDRESS[i] + " cannot be read!");
					return null;
				}
			}
			
		} else {
			try {
				file = openFile(fileName[0]);
				for(int a = 0; (read = (short) file.read()) != -1; a++) {
					holder[ProgramUtils.Rom.ROM_ADDRESS[0] + a] = read;
				}

			} catch (IOException e) {
				System.out.println(ProgramUtils.Rom.FILE_NAME + " cannot be read!");
				return null;
			}
		}
		
		return holder;
	}
	
	// FILE EXISTENCE CHECK
	private static boolean fileExist(String[] fileName) {
		try {
			if(fileName.length > 1) {
				for(int i = 0; i < ProgramUtils.Rom.FILE_NAME.length; i++) {
				
					if (openFile(ProgramUtils.Rom.FILE_NAME[i]) == null) {
						System.out.println("File " + ProgramUtils.Rom.FILE_NAME[i] + " could not be found.");
						return false;
					}
				}
			} else {
			
				if (openFile(fileName[0]) == null) {
					System.out.println("No files specified.");
					return false;
				}
			}
			
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("No files specified.");
			return false;
		}
		
		if (ProgramUtils.Rom.ROM_ADDRESS.length == 0) {
			System.out.println("File online, but no starting memory address specified.");
			return false;
		}
		
		if (ProgramUtils.Rom.ROM_ADDRESS.length != ProgramUtils.Rom.FILE_NAME.length) {
			System.out.println("File online, but roms and memory address unaligned.");
			return false;
		}
		
		System.out.println("File online , loaded successfully!");
		return true;
	}
	
	// FILE SUBROUTINE
	private static InputStream openFile(String romName) {
		try {
			return new FileInputStream(ProgramUtils.Machine.STORAGE_LOCATION + romName);
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

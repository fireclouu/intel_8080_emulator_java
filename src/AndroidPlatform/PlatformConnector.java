package com.fireclouu.intel8080emu;

import android.app.*;
import android.content.*;
import android.os.*;
import android.widget.*;
import com.fireclouu.intel8080emu.Emulator.*;
import java.io.*;

public class PlatformConnector
{
	Context context;
	CpuComponents cpu; // global
	Handler handler;
	Runnable runnable;
	DisplayView mDisplay;
	
	///   ROM LENGTH   ///
	public  static final int PROGRAM_LENGTH = 0x100_000;

	///   SPLIT ROMS LIST   ///
	public static final String[] romName = {
		"invaders.h",
		"invaders.g",
		"invaders.f",
		"invaders.e"
		//"cpudiag.bin"
		//"8080EX1.COM"
	};
	
	///   LOAD ADDRESS   ///
	public static final int[] romAddr = {
		0x0000,
		0x0800,
		0x1000,
		0x1800
		//0x0100
	};
	
	public PlatformConnector(Context context, DisplayView mDisplay) {
		this.mDisplay = mDisplay;
		this.context = context;
		init();
	}
	
	public void startEmulator() {
		
		Thread t = new Thread() {
			@Override
			public void run() {
				getDisp(mDisplay);
			}
			
			private void getDisp(DisplayView disp) {
				new Emulation(cpu, disp);
			}
			
		};
		
		t.start();
	}
	
	private void init() {
		cpu = new CpuComponents(loadRom((romName)));
		
	}
	
	// LOAD ROM
	private short[] loadRom(String[] fileName) {
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
	
	// FILE READER
	private InputStream openFile(String romName) {
		try {
			return ((Activity)context).getAssets().open(romName);
		} catch (IOException e) {
			return null;
		}
	}
}

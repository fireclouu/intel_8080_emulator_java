package BaseClass;

public final class AppUtils
{
	private AppUtils() { } // prevent instatiation

	public static final class File {
		private File() { }

		public static final String[] FILES = {
			//"invaders.h", "invaders.g",
			//"invaders.f", "invaders.e"

			///  TESTS  ///
			"TST8080.COM",
			//"8080EX1.COM"
			//"8080EXER.COM"
			"8080PRE.COM",
			//"CPUTEST.COM"
			"8080EXM.COM"
		};

		public static final int[] ROM_ADDRESS = {
			0x0000,
			0x0800,
			0x1000,
			0x1800
			//0x0100
		};
	}

	public static final class Machine {
		private Machine() { }

		// DO NOT MODIFY WHEN EMULATING SPACE INVADERS
		public static boolean DEBUG = false;

		public static final boolean PRINT_LESS = true;
		public static final int PROGRAM_LENGTH = 0x10_000;
		public static final String STORAGE_LOCATION = "/sdcard/Download/";

	}
}

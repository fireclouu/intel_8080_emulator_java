
import java.io.*;
import BaseClass.PlatformAdapter;
import BaseClass.*;

public class Platform extends PlatformAdapter
{

	@Override
	public void createDisplay() {
		// stub
	}
	
	@Override
	public InputStream openFile(String romName) {
		FileInputStream fis = null;
		
		try
		{
			fis = new FileInputStream(machineUtils.STORAGE_LOCATION + romName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return fis;
	}
	
}


public class Main
{
	public static void main(String[] args) {
		// Notifier
		System.out.println("--- Main thread started.");
		
		Platform platform = new Platform();	
		platform.startOp();
		
		// Notifier
		System.out.println("--- Main thread ended.");
	}
	
}

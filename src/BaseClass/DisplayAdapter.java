package BaseClass;

public interface DisplayAdapter
{
	public final byte ORIENTATION_DEFAULT = 0;
	public final byte ORIENTATION_COUNTERCLOCK = 1;

	public boolean readyToDraw;

	public void setDraws(short[] memory);
}

package zzl.bestidear.BluetoothChat.Tools;

public class SensorInfo
{
	private int type = 0;
	private float x = 0;
	private float y = 0;
	private float z = 0;
	private int accuracy = 0;
	
	public SensorInfo(int type, float x, float y, float z, int accuracy) {
		this.type = type;
		this.x = x;
		this.y = y;
		this.z = z;
		this.accuracy = accuracy;
	}
	
	public int getType()
	{
		return this.type;
	}
	
	public float getX() {
		return x;
	}
	
	public float getY() {
		return y;
	}
	
	public float getZ() {
		return z;
	}
	
	public int getAccuracy() {
		return accuracy;
	}
}

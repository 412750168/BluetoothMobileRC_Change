package zzl.bestidear.BluetoothChat.Tools;


public class TouchInfo {
	private int action = -1;
	private int x = 0;
	private int y = 0;
	
	public TouchInfo (int action, int x, int y) {
		this.action = action;
		this.x = x;
		this.y = y;
	}
	
	public int getAction() {
		return action;
	}
	
	public int getTouchX() {
		return x;
	}
	
	public int getTouchY() {
		return y;
	}	
}

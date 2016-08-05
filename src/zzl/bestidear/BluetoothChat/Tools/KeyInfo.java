package zzl.bestidear.BluetoothChat.Tools;

public class KeyInfo {
	private int action = -1;
	private int keycode = -1;
	
	public KeyInfo(int action, int keycode) {
		this.action = action;
		this.keycode = keycode;
	}
	
	public int getAction() {
		return action;
	}
	
	public int getKeyCode() {
		return keycode;
	}
}

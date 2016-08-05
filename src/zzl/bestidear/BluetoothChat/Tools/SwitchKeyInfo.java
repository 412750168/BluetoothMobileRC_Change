package zzl.bestidear.BluetoothChat.Tools;


public class SwitchKeyInfo extends KeyInfo{

	private int keymode = -1;
	
	public SwitchKeyInfo(int action, int keycode,int mode) {
		super(action, keycode);
		keymode=mode;
	}

	public int getKeymode() {
		return keymode;
	}

}

package zzl.bestidear.BluetoothChat.Service;

import java.io.IOException;
import java.net.SocketException;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import zzl.bestidear.BluetoothChat.Service.*;
import zzl.bestidear.BluetoothChat.Tools.*;

public class NetCore {

	public static final int EVENT_ACK = 0;
	public static final int EVENT_KEY = 1;
	public static final int EVENT_TOUCH = 2;
	public static final int EVENT_TRACKBALL = 3;
	public static final int EVENT_SENSOR = 4;
	public static final int EVENT_UI_STATE = 5;
	public static final int EVENT_GET_SCREEN = 6;
	public static final int EVENT_KEY_MODE = 7;
	public static final int EVENT_SERVICE = 8;
	public static final int EVENT_SERVER_STATE = 9;
	
	public final static int CONNECTED_SUCCESS = 0x400 +1;
	public final static int CONNECTED_BAD = 	0x400 +2;
	public final static int BLUETOOTH_LINK_BROAKEN = 0x400 + 3;
	
	private static NetCore client = new NetCore();
	private BluetoothChatService mChatService = null;
	private BluetoothAdapter mBtAdapter = null;
	private boolean isTCPConnected = false;
	
	private Handler handler = null;
	private Handler netCmdHandler = null;
	private Handler activityHandler = null;
	private NetCore() {
	}

	public static NetCore getinstance() {
		return client;
	}

	public void setCMDHandler(Handler cmdhandler){
		netCmdHandler = cmdhandler;
		
	}
	
	public void setActivityHandler(Handler mactivityhandler){
		
		activityHandler = mactivityhandler;
	}
	
	public void Connect(String address) {
		
		handler = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				switch(msg.what){
				
				case CONNECTED_SUCCESS:
					isTCPConnected = true;
					netCmdHandler.obtainMessage(MessageID.CMD_CONNECT_SUCCESS).sendToTarget();
					break;
				case CONNECTED_BAD:
					isTCPConnected = false;
					netCmdHandler.obtainMessage(MessageID.CMD_CONNECT_BAD).sendToTarget();
					break;
				case BLUETOOTH_LINK_BROAKEN:
					activityHandler.obtainMessage(MessageID.FINISH_ACTIVITY).sendToTarget();
					activityHandler = null;
					break;
				}
				
				Log.d("zzl:::",isTCPConnected+" ");
			}
			
			
		};
		mChatService = new BluetoothChatService(handler);
		mChatService.start();

		mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		BluetoothDevice device = mBtAdapter.getRemoteDevice(address);
		// Attempt to connect to the device
		if (device != null && mChatService != null) {

			mChatService.connect(device, true);
			//isTCPConnected = true;
		}
		//isTCPConnected = false;
	}

	public void stop() {
		if (mChatService != null)
			mChatService.stop();
	}

	public int sendKeyVaule(KeyInfo mKey) {
		byte[] cmd = new byte[4];

		int action = mKey.getAction();
		int keycode = mKey.getKeyCode();

		// Log.d(TAG, "send key event action:" + action + " keycode:" +
		// keycode);

		if (isTCPConnected) {
			cmd[0] = EVENT_KEY;
			cmd[1] = (byte) action;
			cmd[2] = (byte) keycode;
			return TCP_SendData(cmd);
		}

		return 0;
	}

	public int sendTouchValue(TouchInfo mTouch) {
		byte[] cmd = new byte[6];

		int action = mTouch.getAction();

		// int x = (int) mTouch.getTouchX() * CurConnectedService.getWidth() /
		// 1000;
		// int y = (int) mTouch.getTouchY() * CurConnectedService. / 1000;

		int x = (int) mTouch.getTouchX() * 800 / 1000;
		int y = (int) mTouch.getTouchY() * 600 / 1000;

		if (null != mChatService) {
			cmd[0] = EVENT_TOUCH;
			cmd[1] = (byte) action;
			cmd[2] = (byte) ((x >> 8) & 0xff);
			cmd[3] = (byte) (x & 0xff);
			cmd[4] = (byte) ((y >> 8) & 0xff);
			cmd[5] = (byte) (y & 0xff);
			return TCP_SendData(cmd);
		}

		return 0;
	}

	public int TCP_SendData(byte[] cmd) {
		if (null == cmd) {
			return 0;
		} else {
			if (null != mChatService) {
				byte len[] = new byte[4];

				len[0] = (byte) ((cmd.length >> 24) & 0xff);
				len[1] = (byte) ((cmd.length >> 16) & 0xff);
				len[2] = (byte) ((cmd.length >> 8) & 0xff);
				len[3] = (byte) (cmd.length & 0xff);

				// mOutStream.write(len, 0, len.length);
				// write to buffer
				// mOutStream.write(cmd, 0, cmd.length);
				// write to stream
				// mOutStream.flush();
				/*
				 * byte sum[] = new byte[7]; sum[0]=len[0]; sum[1]=len[1];
				 * sum[2]=len[2]; sum[3]=cmd[0]; sum[4]=cmd[1]; sum[5]=cmd[2];
				 * sum[6]=cmd[3]; Log.d("zzl:::","this is send out"+ sum);
				 * mChatService.write(sum);
				 */
				mChatService.write(len, cmd);
				return 1;
			} else {
				return 0;
			}
		}
	}

	public int sendSwitchKeyVaule(SwitchKeyInfo mKey) {
		byte[] cmd = new byte[4];

		int action = mKey.getAction();
		int keycode = mKey.getKeyCode();
		int keymode = mKey.getKeymode();

		if (isTCPConnected) {
			cmd[0] = EVENT_KEY_MODE;
			cmd[1] = (byte) action;
			cmd[2] = (byte) keycode;
			cmd[3] = (byte) keymode;

			return TCP_SendData(cmd);
		}

		return 0;
	}
}

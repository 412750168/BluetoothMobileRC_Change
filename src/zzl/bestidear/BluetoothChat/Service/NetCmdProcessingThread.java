package zzl.bestidear.BluetoothChat.Service;

import zzl.bestidear.BluetoothChat.Tools.*;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class NetCmdProcessingThread extends Thread {

	private static Handler CPMHandler = null;
	private Handler ResultMsgHandler = null;
	private Handler tempHandler = null;
	private static NetCmdProcessingThread NCPThread = null;

	@Override
	public void run() {
		// TODO Auto-generated method stub
		Looper.prepare();

		CPMHandler = new Handler(new CmdProcMsgCallback());
		synchronized (this) {
			notifyAll();
		}

		Looper.loop();
	}

	private NetCmdProcessingThread() {
	}

	public static NetCmdProcessingThread getInstance() {
		if (NCPThread == null) {
			NCPThread = new NetCmdProcessingThread();
			NCPThread.start();

			synchronized (NCPThread) {
				try {
					NCPThread.wait();
				} catch (InterruptedException e) {

				}
			}
		}
		return NCPThread;
	}

	public void setFeedbackHandler(Handler mHandler) {
		ResultMsgHandler = mHandler;
	}

	public Handler getCmdProcMsgHandler() {
		return CPMHandler;
	}

	private void sendResultMessage(int what, int arg1, int arg2, Object obj) {
		Message msg = Message.obtain();
		msg.what = what;
		msg.arg1 = arg1;
		msg.arg2 = arg2;
		msg.obj = obj;
		ResultMsgHandler.sendMessage(msg);
	}

	class CmdProcMsgCallback implements Handler.Callback {
		private String TAG = this.getClass().getName();

		@Override
		public boolean handleMessage(Message msg) {
			int ret = 0;

			switch (msg.what) {
			case MessageID.CMD_CONNECT:
				
				String address = (String) msg.obj;
				
				tempHandler = new Handler(){

					@Override
					public void handleMessage(Message msg) {
						// TODO Auto-generated method stub
						switch(msg.what){
							case MessageID.CMD_CONNECT_SUCCESS:
								sendResultMessage(MessageID.CMD_CONNECT_DONE, 0, 0, null);
							case MessageID.CMD_CONNECT_BAD:
								sendResultMessage(MessageID.CMD_CONNECT_FAILED, 0, 0, null);
						}
					}
					
				};
				NetCore.getinstance().setCMDHandler(tempHandler);
				NetCore.getinstance().Connect(address);//不进行状态 判断 ，CMD_CONNECT_SUCCESS 为确定连接成功

				break;
				
			

			case MessageID.CMD_SEND_KEY:
				KeyInfo mKey = (KeyInfo) msg.obj;
				Log.d(TAG, "CMD_SEND_KEY " + mKey.getKeyCode());
				if (NetCore.getinstance().sendKeyVaule(mKey) == 0) {
					sendResultMessage(MessageID.CMD_SEND_KEY_FAILED, 0, 0, 0);
				} else {

				}
				break;

			case MessageID.CMD_SEND_TOUCH:
				TouchInfo mTouch = (TouchInfo) msg.obj;
				Log.d(TAG, "CMD_SEND_TOUCH" + mTouch.getAction());
				ret = NetCore.getinstance().sendTouchValue(mTouch);
				if (ret == 0) {
					sendResultMessage(MessageID.CMD_SEND_TOUCH_FAILED, 0, 0, 0);
				} else {
					// TCPTransMonitoringThread.getInstance().waitingUp();
				}
				break;

			case MessageID.CMD_DISCONNECT:
				NetCore.getinstance().stop();
				// sendResultMessage(MessageID.CMD_DISCONNECT_DONE, 0, 0, null);
				break;

			case MessageID.CMD_SEND_KEY_MODE:
				SwitchKeyInfo keycode = (SwitchKeyInfo) msg.obj;
				Log.d(TAG, "CMD_SEND_KEY_MODE" + keycode.getKeymode());
				ret = NetCore.getinstance().sendSwitchKeyVaule(keycode);
				if (ret == 0) {
					sendResultMessage(MessageID.CMD_SEND_KEY_MODE_FAILED, 0, 0,
							0);
				} else {
					// TCPTransMonitoringThread.getInstance().waitingUp();
				}
				break;
			}

			return true;
		}
	}
}

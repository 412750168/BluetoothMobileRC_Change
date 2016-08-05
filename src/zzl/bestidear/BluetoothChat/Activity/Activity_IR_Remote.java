package zzl.bestidear.BluetoothChat.Activity;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import net.bestidear.BluetoothMobileRC.R;

import zzl.bestidear.BluetoothChat.Service.NetCmdProcessingThread;
import zzl.bestidear.BluetoothChat.Service.NetCore;
import zzl.bestidear.BluetoothChat.Tools.*;
import android.os.*;
import android.app.*;
import android.content.*;
import android.hardware.*;
import android.util.Log;
import android.view.View.*;
import android.view.*;
import android.widget.*;

public class Activity_IR_Remote extends Activity
{
	private final String TAG = this.getClass().getName();
	
	public static final String RESULT_NAME = "result";
	public static final String RETURN_BY_PK_BUTTON = "RETURN_BY_PK_BUTTON";
	public static final String RETURN_BY_BACK_KEY = "RETURN_BY_BACK_KEY";
	public static final String RETURN_BY_WIFI_DOWN = "RETURN_BY_WIFI_DOWN";
	
	private Context ctx = this;
	private Handler Cmd_Handler = null;
	private Handler temp_Handler = null;
	
	private AlertDialog popUpWin = null;
	
	private ArrayList<Button> Ir_Buttons = new ArrayList<Button>();

	private Button Ir_Remote_Button_Keyboard = null;
	
	private SensorManager sm = null;
	private Sensor sr = null;
	private Vibrator vib = null;
	
	private OnTouchListener Button_OnTouchListener = new OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				Button btn = (Button) v;
				int keycode = 0;
				switch(btn.getId())
				{
					case R.id.Ir_Remote_Button_Power:
						keycode = getResources().getInteger(R.integer.keycode_power);
						break;
					case R.id.Ir_Remote_Button_Home:
						keycode = getResources().getInteger(R.integer.keycode_home);
						break;
					case R.id.Ir_Remote_Button_Up:
						keycode = getResources().getInteger(R.integer.keycode_up);
						break;
					case R.id.Ir_Remote_Button_Down:
						keycode = getResources().getInteger(R.integer.keycode_down);
						break;
					case R.id.Ir_Remote_Button_Left:
						keycode = getResources().getInteger(R.integer.keycode_left);
						break;
					case R.id.Ir_Remote_Button_Right:
						keycode = getResources().getInteger(R.integer.keycode_right);
						break;
					case R.id.Ir_Remote_Button_OK:
						keycode = getResources().getInteger(R.integer.keycode_center);
						break;
					case R.id.Ir_Remote_Button_Menu:
						keycode = getResources().getInteger(R.integer.keycode_menu);
						break;
					case R.id.Ir_Remote_Button_Volume_Up:
						keycode = getResources().getInteger(R.integer.keycode_vol_plus);
						break;
					case R.id.Ir_Remote_Button_Volume_Down:
						keycode = getResources().getInteger(R.integer.keycode_vol_minus);
						break;
					case R.id.Ir_Remote_Button_Back:
						keycode = getResources().getInteger(R.integer.keycode_back);
						break;
					default:
						//Log.d(TAG, "OnTouchListener Unknown Button ID");
						break;
				}
				
				switch(event.getAction())
				{
					case MotionEvent.ACTION_UP:
					case MotionEvent.ACTION_DOWN:
						KeyInfo mKey = new KeyInfo(event.getAction(), keycode);
						Message msg = Message.obtain();
						msg.what = MessageID.CMD_SEND_KEY;
						msg.arg1 = 0;
						msg.arg2 = 0;
						msg.obj = mKey;
						Cmd_Handler.sendMessage(msg);
						break;
					default:
						//Log.d(TAG, "OnTouchListener Unknown Action Type");
						break;
				}
				return false;
			}
		};
	
	private static int SHAKE_INTERVAL = 1000;
	private long LAST_SHAKE_MARK = 0;
	private SensorEventListener SEListener = new SensorEventListener()
	{
		public void onAccuracyChanged(Sensor sensor, int accuracy)
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onSensorChanged(SensorEvent event)
		{
			// TODO Auto-generated method stub
			float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            int accuracy = event.accuracy;
            SensorInfo mSensor = new SensorInfo(Sensor.TYPE_ACCELEROMETER, x, y, z, accuracy);
            Message msg = Message.obtain();
			msg.what = MessageID.CMD_SEND_SENSOR;
			msg.arg1 = 0;
			msg.arg2 = 0;
			msg.obj = mSensor;
			Cmd_Handler.sendMessage(msg);
			
			long currentTime = System.currentTimeMillis();
			if(SHAKE_INTERVAL < (currentTime - LAST_SHAKE_MARK))
			{
				if(Math.abs(x) > 18 || Math.abs(y) > 18 || Math.abs(z) > 18)
				{
					LAST_SHAKE_MARK = currentTime;
					vib.vibrate(500);
				}
			}
		}
	};
		
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ir_remote);
		
		popUpWin = new AlertDialog.Builder(ctx).setCancelable(false)
				.setPositiveButton(R.string.confirm,
					new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog,
							int whichButton)
						{
							Intent intent = new Intent();
							intent.putExtra(RESULT_NAME, RETURN_BY_WIFI_DOWN);
							setResult(RESULT_OK, intent);
							finish();
						}
					})
				.create();
		
		Cmd_Handler = NetCmdProcessingThread.getInstance().getCmdProcMsgHandler();
		
		Ir_Buttons.add((Button) findViewById(R.id.Ir_Remote_Button_Power));
		Ir_Buttons.add((Button) findViewById(R.id.Ir_Remote_Button_Home));
		Ir_Buttons.add((Button) findViewById(R.id.Ir_Remote_Button_Up));
		Ir_Buttons.add((Button) findViewById(R.id.Ir_Remote_Button_Down));
		Ir_Buttons.add((Button) findViewById(R.id.Ir_Remote_Button_Left));
		Ir_Buttons.add((Button) findViewById(R.id.Ir_Remote_Button_Right));
		Ir_Buttons.add((Button) findViewById(R.id.Ir_Remote_Button_OK));
		Ir_Buttons.add((Button) findViewById(R.id.Ir_Remote_Button_Menu));
		Ir_Buttons.add((Button) findViewById(R.id.Ir_Remote_Button_Volume_Up));
		Ir_Buttons.add((Button) findViewById(R.id.Ir_Remote_Button_Volume_Down));
		Ir_Buttons.add((Button) findViewById(R.id.Ir_Remote_Button_Back));
		
		for(Button btn : Ir_Buttons)
		{
			btn.setOnTouchListener(Button_OnTouchListener);
		}
		
		Ir_Remote_Button_Keyboard = (Button) findViewById(R.id.Ir_Remote_Button_Keyboard);
		Ir_Remote_Button_Keyboard.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					Intent intent = new Intent();
					intent.putExtra(RESULT_NAME, RETURN_BY_PK_BUTTON);
					setResult(RESULT_OK, intent);
					finish();
				}
			});
		
		//((TextView) findViewById(R.id.Ir_Remote_TextView_ServerName))
		//	.setText(Activity_ServiceList.copyCurConnectedService().getName());
		
		sm = (SensorManager) getSystemService(SENSOR_SERVICE);
		sr = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		
		vib = (Vibrator) this.getSystemService(Service.VIBRATOR_SERVICE);
		
		//TCPTransMonitoringThread.getInstance().registerMsgHandler(IncomingMsgHandler);
		//ConnectivityMonitor.registerMsgHandler(IncomingMsgHandler);
		NetCore.getinstance().setActivityHandler(new Handler(){

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				switch(msg.what){
				case MessageID.FINISH_ACTIVITY:
					Intent intent = new Intent();
					intent.putExtra(RESULT_NAME, RETURN_BY_BACK_KEY);
					setResult(RESULT_OK, intent);
					finish();
					break;
				}
			}
		
		});
	
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		//sm.registerListener(SEListener, sr, SensorManager.SENSOR_DELAY_GAME);
		Log.d(TAG, "onResume Finished");
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		//sm.unregisterListener(SEListener);
		Log.d(TAG, "onPause Finished");
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		//TCPTransMonitoringThread.getInstance().unregisterMsgHandler(IncomingMsgHandler);
		//ConnectivityMonitor.unregisterMsgHandler(IncomingMsgHandler);
		
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event)
	{
		boolean ret = false;
		switch(event.getAction())
		{
			case KeyEvent.ACTION_UP:
				switch(keyCode)
				{
					case KeyEvent.KEYCODE_BACK:
						Log.d("zzl:::","this is key back");
						Intent intent = new Intent();
						intent.putExtra(RESULT_NAME, RETURN_BY_BACK_KEY);
						setResult(RESULT_OK, intent);
						finish();
						ret = true;
						break;
					default:
						break;
				}
				break;
			default:
				break;
		}
		return ret;
	}
	
	

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode== KeyEvent.KEYCODE_BACK){
			Intent intent = new Intent();
			intent.putExtra(RESULT_NAME, RETURN_BY_BACK_KEY);
			setResult(RESULT_OK, intent);
			finish();
			
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private class IncomingMsgCallback implements Handler.Callback
	{
		@Override
		public boolean handleMessage(Message msg)
		{
			switch(msg.what)
			{
				case MessageID.WIFI_TCP_TRANS_DOWN:
					
					//ServiceSet.getInstance().removeEntryByIP(NetCore.getinstance().getCurConnectedService().getIp());
					if(popUpWin.isShowing())
					{
						popUpWin.cancel();
					}
					popUpWin.setTitle(R.string.service_timeout);
					popUpWin.show();
					
					break;
				case MessageID.WIFI_HOTSPOT_DISCONNECTED:
					if(popUpWin.isShowing())
					{
						popUpWin.cancel();
					}
					popUpWin.setTitle(R.string.wifi_state_error);
					popUpWin.show();
					break;
				case MessageID.CMD_SEND_KEY_DONE:
					break;
				case MessageID.CMD_SEND_KEY_FAILED:
					Log.d(TAG, "Send Cmd Failed.");
					break;
			}
			return false;
		}
		
	}
	
	
	private Handler IncomingMsgHandler = new Handler(new IncomingMsgCallback());
}

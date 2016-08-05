package zzl.bestidear.BluetoothChat.Activity;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import zzl.bestidear.BluetoothChat.Service.NetCmdProcessingThread;
import zzl.bestidear.BluetoothChat.Service.NetCore;
import zzl.bestidear.BluetoothChat.Tools.KeyInfo;
import zzl.bestidear.BluetoothChat.Tools.MessageID;

import net.bestidear.BluetoothMobileRC.R;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.Toast;
import android.app.*;
import android.content.*;

public class Activity_TV_Remote extends Activity
{
	private String TAG = this.getClass().getName();
	
	public static final String RESULT_NAME = "result";
	public static final String RETURN_BY_PK_BUTTON = "RETURN_BY_PK_BUTTON";
	public static final String RETURN_BY_BACK_KEY = "RETURN_BY_BACK_KEY";
	public static final String RETURN_BY_WIFI_DOWN = "RETURN_BY_WIFI_DOWN";
	
	private Handler NetCmdMsgHandler = null;
	private AlertDialog popUp_Warining = null;
	private AlertDialog popUp_Switch = null;
	
	private int Baseline_DigitKeyCode = 0;
	private ArrayList<Button> DigitButtons = new ArrayList<Button>();
	private ArrayList<Button> FuncButtons = new ArrayList<Button>();
	
	private int KeyCode_Home = 0;
	private int KeyCode_Switch = 0;
	private int KeyCode_Channel_Up = 0;
	private int KeyCode_Channel_Down = 0;
	private int KeyCode_OK = 0;
	private int KeyCode_Volume_Plus = 0;
	private int KeyCode_Volume_Minus = 0;
	private int KeyCode_Menu = 0;
	private int KeyCode_Back = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tv_remote);
		
		popUp_Warining = new AlertDialog.Builder(this).setCancelable(false)
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
		
		popUp_Switch = new AlertDialog.Builder(this).setCancelable(true).create();
		View popUpView = ((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE))
				.inflate(R.layout.popup_menu, (ViewGroup) findViewById(R.id.popup_menu_root));
		popUp_Switch.setView(popUpView);
		
		Baseline_DigitKeyCode = this.getResources().getInteger(R.integer.keycode_0);
		NetCmdMsgHandler  = NetCmdProcessingThread.getInstance().getCmdProcMsgHandler();
		
		KeyCode_Home = getResources().getInteger(R.integer.keycode_home);
		KeyCode_Switch = 0;
		KeyCode_Channel_Up = getResources().getInteger(R.integer.keycode_up);
		KeyCode_Channel_Down = getResources().getInteger(R.integer.keycode_down);
		KeyCode_OK = getResources().getInteger(R.integer.keycode_center);
		KeyCode_Volume_Plus = getResources().getInteger(R.integer.keycode_right);
		KeyCode_Volume_Minus = getResources().getInteger(R.integer.keycode_left);
		KeyCode_Menu = getResources().getInteger(R.integer.keycode_menu);
		KeyCode_Back = getResources().getInteger(R.integer.keycode_back);
		
		activeDigitButtons();
		activeFuncButtons();
		
		((Button) findViewById(R.id.TV_Remote_Button_Keyboard)).setOnClickListener(
				new OnClickListener()
				{
					public void onClick(View v)
					{
						/*
						if(popUp_Switch.isShowing())
						{
							popUp_Switch.cancel();
						}
						popUp_Switch.show();
						*/
						
						Intent intent = new Intent();
						intent.putExtra(RESULT_NAME, RETURN_BY_PK_BUTTON);
						setResult(RESULT_OK, intent);
						finish();
					}
				});
		
		((Button) popUpView.findViewById(R.id.Popup_Menu_Button_C)).setAlpha((float) 0.5);
		((Button) popUpView.findViewById(R.id.Popup_Menu_Button_C)).setEnabled(false);
		((Button) popUpView.findViewById(R.id.Popup_Menu_Button_B)).setOnClickListener(
				new OnClickListener()
				{
					public void onClick(View v)
					{
						popUp_Switch.dismiss();
						finish();
						Intent itn = new Intent();
						itn.setClass(Activity_TV_Remote.this, Activity_PointerKeyboard.class);
						startActivity(itn);
					}
				});
		((Button) popUpView.findViewById(R.id.Popup_Menu_Button_A)).setOnClickListener(
				new OnClickListener()
				{
					public void onClick(View v)
					{
						popUp_Switch.dismiss();
						finish();
						Intent itn = new Intent();
						itn.setClass(Activity_TV_Remote.this, Activity_IR_Remote.class);
						startActivity(itn);
					}
				});
		
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
	protected void onStart()
	{
		super.onStart();
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
	}
	
	@Override
	protected void onStop()
	{
		super.onStop();
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
	
	private OnTouchListener FuncButton_OTL = new OnTouchListener()
	{
		public boolean onTouch(View v, MotionEvent event)
		{
			switch(event.getAction())
			{
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_DOWN:
					int KeyCode = 0;
					Button btn = (Button) v;
					switch(btn.getId())
					{
						case R.id.TV_Channel_Switch:
							KeyCode = KeyCode_Switch;
							break;
						case R.id.TV_Channel_Home:
							KeyCode = KeyCode_Home;
							break;
						case R.id.TV_Remote_Button_Channel_Up:
							KeyCode = KeyCode_Channel_Up;
							break;
						case R.id.TV_Remote_Button_Channel_Down:
							KeyCode = KeyCode_Channel_Down;
							break;
						case R.id.TV_Remote_Button_Volume_Plus:
							KeyCode = KeyCode_Volume_Plus;
							break;
						case R.id.TV_Remote_Button_Volume_Minus:
							KeyCode = KeyCode_Volume_Minus;
							break;
						case R.id.TV_Remote_Button_OK:
							KeyCode = KeyCode_OK;
							break;
						case R.id.TV_Remote_Button_Menu:
							KeyCode = KeyCode_Menu;
							break;
						case R.id.TV_Remote_Button_Back:
							KeyCode = KeyCode_Back;
							break;
						default:
							break;
					}
					sendKeyCode(KeyCode, event.getAction());
					break;
				default:
					break;
			}
			return false;
		}
	};
	
	private void activeFuncButtons()
	{
		FuncButtons.clear();
		FuncButtons.add((Button) findViewById(R.id.TV_Channel_Switch));
		FuncButtons.add((Button) findViewById(R.id.TV_Channel_Home));
		FuncButtons.add((Button) findViewById(R.id.TV_Remote_Button_Channel_Up));
		FuncButtons.add((Button) findViewById(R.id.TV_Remote_Button_Channel_Down));
		FuncButtons.add((Button) findViewById(R.id.TV_Remote_Button_OK));
		FuncButtons.add((Button) findViewById(R.id.TV_Remote_Button_Volume_Plus));
		FuncButtons.add((Button) findViewById(R.id.TV_Remote_Button_Volume_Minus));
		FuncButtons.add((Button) findViewById(R.id.TV_Remote_Button_Menu));
		FuncButtons.add((Button) findViewById(R.id.TV_Remote_Button_Back));
		
		for(Button btn : FuncButtons)
		{
			btn.setOnTouchListener(FuncButton_OTL);
		}
	}
	
	private OnTouchListener DigitButton_OTL = new OnTouchListener()
	{
		public boolean onTouch(View v, MotionEvent event)
		{
			int btn_index = 0;
			for(Button abtn : DigitButtons)
			{
				if(abtn.equals((Button) v))
				{
					btn_index = DigitButtons.indexOf(abtn);
				}
			}
			
			switch(event.getAction())
			{
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_DOWN:
					sendKeyCode(Baseline_DigitKeyCode + btn_index, event.getAction());
					break;
				default:
					break;
			}
			
			return false;
		}
	};
	
	private void sendKeyCode(int keyCode, int action)
	{
		KeyInfo mKey = new KeyInfo(action, keyCode);
		Message msg = Message.obtain();
		msg.what = MessageID.CMD_SEND_KEY;
		msg.arg1 = 0;
		msg.arg2 = 0;
		msg.obj = mKey;
		NetCmdMsgHandler.sendMessage(msg);
	}
	
	private void activeDigitButtons()
	{
		DigitButtons.clear();
		DigitButtons.add((Button) findViewById(R.id.TV_Channel_0));
		DigitButtons.add((Button) findViewById(R.id.TV_Channel_1));
		DigitButtons.add((Button) findViewById(R.id.TV_Channel_2));
		DigitButtons.add((Button) findViewById(R.id.TV_Channel_3));
		DigitButtons.add((Button) findViewById(R.id.TV_Channel_4));
		DigitButtons.add((Button) findViewById(R.id.TV_Channel_5));
		DigitButtons.add((Button) findViewById(R.id.TV_Channel_6));
		DigitButtons.add((Button) findViewById(R.id.TV_Channel_7));
		DigitButtons.add((Button) findViewById(R.id.TV_Channel_8));
		DigitButtons.add((Button) findViewById(R.id.TV_Channel_9));
		
		for(Button btn : DigitButtons)
		{
			btn.setOnTouchListener(DigitButton_OTL);
		}
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
					if(popUp_Warining.isShowing())
					{
						popUp_Warining.cancel();
					}
					popUp_Warining.setTitle(R.string.service_timeout);
					popUp_Warining.show();
					break;
				case MessageID.WIFI_HOTSPOT_DISCONNECTED:
					if(popUp_Warining.isShowing())
					{
						popUp_Warining.cancel();
					}
					popUp_Warining.setTitle(R.string.wifi_state_error);
					popUp_Warining.show();
					break;
				case MessageID.CMD_SEND_KEY_DONE:
				case MessageID.CMD_SEND_KEY_MODE_DONE:
				case MessageID.CMD_SEND_TOUCH_DONE:
					break;
				case MessageID.CMD_SEND_KEY_FAILED:
				case MessageID.CMD_SEND_KEY_MODE_FAILED:
				case MessageID.CMD_SEND_TOUCH_FAILED:
					Log.d(TAG, "Send Cmd Failed, network error.");
					break;
				default:
					break;
			}
			return false;
		}
		
	}
	
	//private Handler IncomingMsgHandler = new Handler(new IncomingMsgCallback());
}

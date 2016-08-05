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

import zzl.bestidear.BluetoothChat.Service.LayoutInfo;
import zzl.bestidear.BluetoothChat.Service.NetCmdProcessingThread;
import zzl.bestidear.BluetoothChat.Service.NetCore;
import zzl.bestidear.BluetoothChat.Tools.KeyInfo;
import zzl.bestidear.BluetoothChat.Tools.MessageID;
import zzl.bestidear.BluetoothChat.Tools.SwitchKeyInfo;
import zzl.bestidear.BluetoothChat.Tools.TouchInfo;

import android.os.*;
import android.app.*;
import android.content.*;
import android.util.Log;
import android.view.*;
import android.view.View.*;
import android.widget.*;

public class Activity_PointerKeyboard extends Activity {
	private String TAG = this.getClass().getName();
	private Context ctx = this;

	public static final String RESULT_NAME = "result";
	public static final String RETURN_BY_PK_BUTTON = "RETURN_BY_PK_BUTTON";
	public static final String RETURN_BY_BACK_KEY = "RETURN_BY_BACK_KEY";
	public static final String RETURN_BY_WIFI_DOWN = "RETURN_BY_WIFI_DOWN";

	private AlertDialog popUpWin = null;

	private LayoutInfo li = null;
	private float TAP_DELTA = (float) 0.15;
	private final long TAP_TIMEOUT = 500;
	private float SLIDE_DELTA = (float) 0.5;
	private final long SLIDE_TIMEOUT = 256;
	private RelativeLayout layout_for_keyboard = null;
	private View TouchArea = null;

	private Handler NetCmdMsgHandler = null;
	private int KeyCode_Pointer_Switch = 0;

	private int Baseline_AlphaKeyCode = 0;
	private ArrayList<Button> AlphaButtons = new ArrayList<Button>();
	private int Baseline_DigitKeyCode = 0;
	private ArrayList<Button> DigitButtons = new ArrayList<Button>();

	private int KeyCode_Del = 0;
	private int KeyCode_Enter = 0;
	private int KeyCode_Space = 0;
	private int KeyCode_Shift_Left = 0;
	private int KeyCode_Alt_Left = 0;
	private int KeyCode_Sym = 0;
	private ArrayList<Button> FunctionButtons = new ArrayList<Button>();

	private int KeyCode_Plus = 0;
	private int KeyCode_Equals = 0;
	private int KeyCode_Grave = 0;
	private int KeyCode_Minus = 0;
	private int KeyCode_Semicolon = 0;
	private int KeyCode_Apostrophe = 0;
	private int KeyCode_Comma = 0;
	private int KeyCode_Period = 0;
	private int KeyCode_Slash = 0;
	private int KeyCode_Bracket_Left = 0;
	private int KeyCode_Bracket_Right = 0;
	private int KeyCode_Backslash = 0;
	private int KeyCode_Star = 0;
	private int KeyCode_Pound = 0;
	private int KeyCode_At = 0;
	private ArrayList<Button> PunctuationButtons = new ArrayList<Button>();

	private int KeyCode_Mouse_Select = 0;
	private int KeyCode_Back = 0;
	private int KeyCode_PageDown = 93;
	private int KeyCode_PageUp = 92;
	private Button Keyboard_Button_Pointer = null;
	private Button Keyboard_Button_Mouse_Left = null;
	private Button Keyboard_Button_Mouse_Right = null;

	private OnTouchListener AlphaButton_OTL = new OnTouchListener() {
		public boolean onTouch(View v, MotionEvent event) {
			int btn_index = 0;
			for (Button abtn : AlphaButtons) {
				if (abtn.equals((Button) v)) {
					btn_index = AlphaButtons.indexOf(abtn);
				}
			}

			switch (event.getAction()) {
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_DOWN:
				sendKeyCode(Baseline_AlphaKeyCode + btn_index,
						event.getAction());
				break;
			default:
				// Log.d(TAG, "AlphaButton_OTL Unknown Action Type");
				break;
			}

			return false;
		}
	};

	private OnTouchListener DigitButton_OTL = new OnTouchListener() {
		public boolean onTouch(View v, MotionEvent event) {
			int btn_index = 0;
			for (Button abtn : DigitButtons) {
				if (abtn.equals((Button) v)) {
					btn_index = DigitButtons.indexOf(abtn);
				}
			}

			switch (event.getAction()) {
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_DOWN:
				sendKeyCode(Baseline_DigitKeyCode + btn_index,
						event.getAction());
				break;
			default:
				break;
			}

			return false;
		}
	};

	private OnTouchListener MouseKey_OTL = new OnTouchListener() {
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_UP:
				switch (v.getId()) {
				case R.id.Keyboard_Button_Mouse_Left:
					sendKeyCode(KeyCode_Mouse_Select, event.getAction());
					break;
				case R.id.Keyboard_Button_Mouse_Right:
					sendKeyCode(KeyCode_Back, event.getAction());
					break;
				default:
					break;
				}
				break;
			default:
				break;
			}
			return false;
		}
	};

	private class TouchData {
		float loc_x = 0;
		float loc_y = 0;
		int action = 0;
		final long time_ms;

		public TouchData(float loc_x, float loc_y, int action, long time_ms) {
			this.loc_x = loc_x;
			this.loc_y = loc_y;
			this.action = action;
			this.time_ms = time_ms;
		}

		public boolean isPosNear(TouchData td) {
			if ((Math.abs(this.loc_x - td.loc_x) < TAP_DELTA)
					&& (Math.abs(this.loc_y - td.loc_y) < TAP_DELTA))
				return true;
			return false;
		}

		public boolean isTimeClose(TouchData td) {
			if ((Math.abs(this.time_ms - td.time_ms) < TAP_TIMEOUT))
				return true;
			return false;
		}

		public float getPosX() {
			return this.loc_x;
		}

		public float getPosY() {
			return this.loc_y;
		}

		public int getAction() {
			return this.action;
		}

		public long getTimeStamp() {
			return this.time_ms;
		}
	}

	ArrayList<TouchData> tapData = new ArrayList<TouchData>();
	ArrayList<TouchData> slideData = new ArrayList<TouchData>();
	ArrayList<TouchData> gestureData = new ArrayList<TouchData>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pointer_keyboard);

		popUpWin = new AlertDialog.Builder(ctx)
				.setCancelable(false)
				.setPositiveButton(R.string.confirm,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								Intent intent = new Intent();
								intent.putExtra(RESULT_NAME,
										RETURN_BY_WIFI_DOWN);
								setResult(RESULT_OK, intent);
								finish();
							}
						}).create();

		NetCmdMsgHandler = NetCmdProcessingThread.getInstance()
				.getCmdProcMsgHandler();

		Log.d(TAG, (li = new LayoutInfo(this)).toString());
		TAP_DELTA = (float) TAP_DELTA * li.getScreenDpi();
		SLIDE_DELTA = (float) SLIDE_DELTA * li.getScreenDpi();

		layout_for_keyboard = (RelativeLayout) findViewById(R.id.for_keyboard);
		TouchArea = findViewById(R.id.Keyboard_Button_Pointer);

		KeyCode_Pointer_Switch = this.getResources().getInteger(
				R.integer.keycode_mouse_switch);

		KeyCode_Plus = this.getResources().getInteger(R.integer.keycode_plus);
		KeyCode_Equals = this.getResources().getInteger(
				R.integer.keycode_equals);
		KeyCode_Grave = this.getResources().getInteger(R.integer.keycode_grave);
		KeyCode_Minus = this.getResources().getInteger(R.integer.keycode_minus);
		KeyCode_Semicolon = this.getResources().getInteger(
				R.integer.keycode_semicolon);
		KeyCode_Apostrophe = this.getResources().getInteger(
				R.integer.keycode_apostrophe);
		KeyCode_Comma = this.getResources().getInteger(R.integer.keycode_comma);
		KeyCode_Period = this.getResources().getInteger(
				R.integer.keycode_period);
		KeyCode_Slash = this.getResources().getInteger(R.integer.keycode_slash);
		KeyCode_Bracket_Left = this.getResources().getInteger(
				R.integer.keycode_left_bracket);
		KeyCode_Bracket_Right = this.getResources().getInteger(
				R.integer.keycode_right_bracket);
		KeyCode_Backslash = this.getResources().getInteger(
				R.integer.keycode_backslash);
		KeyCode_Star = this.getResources().getInteger(R.integer.keycode_star);
		KeyCode_Pound = this.getResources().getInteger(R.integer.keycode_pound);
		KeyCode_At = this.getResources().getInteger(R.integer.keycode_at);

		Baseline_AlphaKeyCode = this.getResources().getInteger(
				R.integer.keycode_A);
		Baseline_DigitKeyCode = this.getResources().getInteger(
				R.integer.keycode_0);

		KeyCode_Del = this.getResources().getInteger(R.integer.keycode_del);
		KeyCode_Enter = this.getResources().getInteger(R.integer.keycode_enter);
		KeyCode_Space = this.getResources().getInteger(R.integer.keycode_space);
		KeyCode_Shift_Left = this.getResources().getInteger(
				R.integer.keycode_shift_left);
		KeyCode_Alt_Left = this.getResources().getInteger(
				R.integer.keycode_alt_left);
		KeyCode_Sym = this.getResources().getInteger(R.integer.keycode_sym);

		tapData.clear();

		activePointerButtons();
		switchToAlphaNumKeyboard();

		((Button) findViewById(R.id.Keyboard_Button_IR))
				.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						Intent intent = new Intent();
						intent.putExtra(RESULT_NAME, RETURN_BY_PK_BUTTON);
						setResult(RESULT_OK, intent);
						finish();
					}

				});

		//TCPTransMonitoringThread.getInstance().registerMsgHandler(
		//		IncomingMsgHandler);
		//ConnectivityMonitor.registerMsgHandler(IncomingMsgHandler);
		//Log.d(TAG, "onCreate Finished");
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
	protected void onStart() {
		super.onStart();
		Log.d(TAG, "onStart Finished");
	}

	@Override
	protected void onResume() {
		super.onResume();
		activePointer();
		Log.d(TAG, "onResume Finished");
	}

	@Override
	protected void onPause() {
		super.onPause();
		deactivePointer();
		Log.d(TAG, "onPause Finished");
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d(TAG, "onStop Finished");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		//TCPTransMonitoringThread.getInstance().unregisterMsgHandler(
		//		IncomingMsgHandler);
		//ConnectivityMonitor.unregisterMsgHandler(IncomingMsgHandler);
		Log.d(TAG, "onDestroy Finished");
		
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		boolean ret = false;
		switch (event.getAction()) {
		case KeyEvent.ACTION_UP:
			switch (keyCode) {
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
	
	private void switchToAlphaNumKeyboard() {
		LinearLayout keyboard_an_layout = (LinearLayout) LayoutInflater
				.from(this).inflate(R.layout.keyboard_alphanum, null)
				.findViewById(R.id.keyboard_alphanum);

		layout_for_keyboard.removeAllViews();
		layout_for_keyboard.addView(keyboard_an_layout);

		((Button) findViewById(R.id.Keyboard_Button_Switch))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						switchToSymnumKeyboard();
					}
				});

		activeAlphaButtons();
		activeDigitButtons();
		activePunctuationButtons();
		activeFunctionButtons();
	}

	private void switchToSymnumKeyboard() {
		LinearLayout keyboard_sn_layout = (LinearLayout) LayoutInflater
				.from(this).inflate(R.layout.keyboard_symnum, null)
				.findViewById(R.id.keyboard_symnum);

		layout_for_keyboard.removeAllViews();
		layout_for_keyboard.addView(keyboard_sn_layout);

		((Button) findViewById(R.id.Keyboard_Button_Switch_sn))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						switchToAlphaNumKeyboard();
					}
				});

		activeDigitButtons_sn();
		activePunctuationButtons_sn();
		activeFunctionButtons_sn();
	}

	private OnTouchListener Punc_OTL_sn = new OnTouchListener() {
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_DOWN:
				int KeyCode = 0;
				Button btn = (Button) v;
				switch (btn.getId()) {
				case R.id.Keyboard_Button_Plus_sn:
					KeyCode = KeyCode_Plus;
					break;
				case R.id.Keyboard_Button_Equal_sn:
					KeyCode = KeyCode_Equals;
					break;
				case R.id.Keyboard_Button_Grave_sn:
					KeyCode = KeyCode_Grave;
					break;
				case R.id.Keyboard_Button_Minus_sn:
					KeyCode = KeyCode_Minus;
					break;
				case R.id.Keyboard_Button_Semicolon_sn:
					KeyCode = KeyCode_Semicolon;
					break;
				case R.id.Keyboard_Button_Apostrophe_sn:
					KeyCode = KeyCode_Apostrophe;
					break;
				case R.id.Keyboard_Button_Comma_sn:
					KeyCode = KeyCode_Comma;
					break;
				case R.id.Keyboard_Button_Period_sn:
					KeyCode = KeyCode_Period;
					break;
				case R.id.Keyboard_Button_Slash_sn:
					KeyCode = KeyCode_Slash;
					break;
				case R.id.Keyboard_Button_Bracket_Left_sn:
					KeyCode = KeyCode_Bracket_Left;
					break;
				case R.id.Keyboard_Button_Bracket_Right_sn:
					KeyCode = KeyCode_Bracket_Right;
					break;
				case R.id.Keyboard_Button_Backslash_sn:
					KeyCode = KeyCode_Backslash;
					break;
				case R.id.Keyboard_Button_Star_sn:
					KeyCode = KeyCode_Star;
					break;
				case R.id.Keyboard_Button_Pound_sn:
					KeyCode = KeyCode_Pound;
					break;
				case R.id.Keyboard_Button_At_sn:
					KeyCode = KeyCode_At;
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

	private void activePunctuationButtons_sn() {
		PunctuationButtons.clear();
		PunctuationButtons
				.add((Button) findViewById(R.id.Keyboard_Button_Plus_sn));
		PunctuationButtons
				.add((Button) findViewById(R.id.Keyboard_Button_Equal_sn));
		PunctuationButtons
				.add((Button) findViewById(R.id.Keyboard_Button_Grave_sn));
		PunctuationButtons
				.add((Button) findViewById(R.id.Keyboard_Button_Minus_sn));
		PunctuationButtons
				.add((Button) findViewById(R.id.Keyboard_Button_Semicolon_sn));
		PunctuationButtons
				.add((Button) findViewById(R.id.Keyboard_Button_Apostrophe_sn));
		PunctuationButtons
				.add((Button) findViewById(R.id.Keyboard_Button_Comma_sn));
		PunctuationButtons
				.add((Button) findViewById(R.id.Keyboard_Button_Period_sn));
		PunctuationButtons
				.add((Button) findViewById(R.id.Keyboard_Button_Slash_sn));
		PunctuationButtons
				.add((Button) findViewById(R.id.Keyboard_Button_Bracket_Left_sn));
		PunctuationButtons
				.add((Button) findViewById(R.id.Keyboard_Button_Bracket_Right_sn));
		PunctuationButtons
				.add((Button) findViewById(R.id.Keyboard_Button_Backslash_sn));
		PunctuationButtons
				.add((Button) findViewById(R.id.Keyboard_Button_Star_sn));
		PunctuationButtons
				.add((Button) findViewById(R.id.Keyboard_Button_Pound_sn));
		PunctuationButtons
				.add((Button) findViewById(R.id.Keyboard_Button_At_sn));

		for (Button btn : PunctuationButtons) {
			btn.setOnTouchListener(Punc_OTL_sn);
		}
	}

	private OnTouchListener Punc_OTL = new OnTouchListener() {
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_DOWN:
				int KeyCode = 0;
				Button btn = (Button) v;
				switch (btn.getId()) {
				case R.id.Keyboard_Button_Comma:
					KeyCode = KeyCode_Comma;
					break;
				case R.id.Keyboard_Button_Period:
					KeyCode = KeyCode_Period;
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

	private void activePunctuationButtons() {
		PunctuationButtons.clear();
		PunctuationButtons
				.add((Button) findViewById(R.id.Keyboard_Button_Comma));
		PunctuationButtons
				.add((Button) findViewById(R.id.Keyboard_Button_Period));

		for (Button btn : PunctuationButtons) {
			btn.setOnTouchListener(Punc_OTL);
		}
	}

	private void sendTouchInfo(float pos_x, float pos_y, int action) {
		TouchInfo mTouch = new TouchInfo(action, TouchCtoSTrans_X(pos_x),
				TouchCtoSTrans_Y(pos_y));
		Message msg = Message.obtain();
		msg.what = MessageID.CMD_SEND_TOUCH;
		msg.arg1 = 0;
		msg.arg2 = 0;
		msg.obj = mTouch;
		NetCmdMsgHandler.sendMessage(msg);
	}

	private enum gestureState {
		START, TOUCH_DOWN, TOUCH_MOVE, TOUCH_UP, MOUSE_DOWN, MOUSE_HOLD, MOUSE_DRAG, MOUSE_UP,
	};

	private gestureState gState = gestureState.START;
	private OnTouchListener Pointer_PageOp_Drag_Tap_OTL = new OnTouchListener() {
		public boolean onTouch(View v, MotionEvent event) {
			float touch_x = event.getX();
			float touch_y = event.getY();

			/* Touch Data Restriction */
			if (touch_x < 0)
				touch_x = 0;
			if (touch_y < 0)
				touch_y = 0;
			if (touch_x > v.getWidth())
				touch_x = v.getWidth();
			if (touch_y > v.getHeight())
				touch_y = v.getHeight();

			Log.d(TAG, "Touch_X = " + touch_x + ", Touch_Y = " + touch_y
					+ ", Action = " + event.getAction());

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				gState = gestureState.TOUCH_DOWN;
				TouchData td_down = new TouchData(touch_x, touch_y,
						event.getAction(), System.currentTimeMillis());
				if (gestureData.size() > 0) {
					gestureData.add(td_down);
					gState = gestureState.MOUSE_DOWN;
					for (TouchData td : gestureData) {
						if (td.isPosNear(gestureData.get(0))
								&& td.isTimeClose(gestureData.get(0))) {
							continue;
						} else {
							gState = gestureState.TOUCH_DOWN;
							sendTouchInfo(touch_x, touch_y, event.getAction());
							break;
						}
					}
					if (gState == gestureState.MOUSE_DOWN) {
						sendKeyCode(KeyCode_Mouse_Select,
								MotionEvent.ACTION_DOWN);
					}
					gestureData.clear();
				} else {
					sendTouchInfo(touch_x, touch_y, event.getAction());
				}
				gestureData.add(td_down);
				break;
			case MotionEvent.ACTION_MOVE:
				TouchData td_move = new TouchData(touch_x, touch_y,
						event.getAction(), System.currentTimeMillis());

				switch (gState) {
				case MOUSE_DOWN:
					/*
					 * sendTouchInfo(gestureData.get(0).getPosX(),
					 * gestureData.get(0).getPosY(),
					 * gestureData.get(0).getAction());
					 */
					if (td_move.isPosNear(gestureData.get(0))) {
						gState = gestureState.MOUSE_HOLD;
					} else {
						gState = gestureState.MOUSE_DRAG;
						sendTouchInfo(touch_x, touch_y, event.getAction());
					}
					break;
				case MOUSE_HOLD:
					if (!td_move.isPosNear(gestureData.get(0))) {
						gState = gestureState.MOUSE_DRAG;
						sendTouchInfo(touch_x, touch_y, event.getAction());
					}
					break;
				case TOUCH_DOWN:
					gState = gestureState.TOUCH_MOVE;
				case TOUCH_MOVE:
				case MOUSE_DRAG:
					sendTouchInfo(touch_x, touch_y, event.getAction());
					break;
				default:
					break;
				}

				break;
			case MotionEvent.ACTION_UP:
				switch (gState) {
				case MOUSE_DRAG:
					sendTouchInfo(touch_x, touch_y, event.getAction());
				case MOUSE_DOWN:
				case MOUSE_HOLD:
					sendKeyCode(KeyCode_Mouse_Select, MotionEvent.ACTION_UP);
					gestureData.clear();
					break;
				case TOUCH_MOVE:
					if ((System.currentTimeMillis() - gestureData.get(0)
							.getTimeStamp()) < SLIDE_TIMEOUT) {
						float pos_delta = gestureData.get(0).getPosY()
								- touch_y;

						if (Math.abs(pos_delta) > SLIDE_DELTA) {
							if (pos_delta > 0) {
								// Page Down
								sendKeyCode(KeyCode_PageDown,
										MotionEvent.ACTION_DOWN);
								sendKeyCode(KeyCode_PageDown,
										MotionEvent.ACTION_UP);
							} else {
								// Page Up
								sendKeyCode(KeyCode_PageUp,
										MotionEvent.ACTION_DOWN);
								sendKeyCode(KeyCode_PageUp,
										MotionEvent.ACTION_UP);
							}
							gestureData.clear();
						}
					}
				case TOUCH_DOWN:
					sendTouchInfo(touch_x, touch_y, event.getAction());
					break;
				default:
					break;
				}
				break;
			}

			return false;
		}

	};

	private void activePointerButtons() {
		KeyCode_Mouse_Select = this.getResources().getInteger(
				R.integer.keycode_mouse_select);
		KeyCode_Back = this.getResources().getInteger(R.integer.keycode_back);

		Keyboard_Button_Pointer = (Button) findViewById(R.id.Keyboard_Button_Pointer);
		Keyboard_Button_Pointer.setOnTouchListener(Pointer_PageOp_Drag_Tap_OTL);

		Keyboard_Button_Mouse_Right = (Button) findViewById(R.id.Keyboard_Button_Mouse_Right);
		Keyboard_Button_Mouse_Right.setOnTouchListener(MouseKey_OTL);
		Keyboard_Button_Mouse_Left = (Button) findViewById(R.id.Keyboard_Button_Mouse_Left);
		Keyboard_Button_Mouse_Left.setOnTouchListener(MouseKey_OTL);
	}

	private void activePointer() {
		sendMouseCode(1);
	}

	private void deactivePointer() {
		sendMouseCode(0);
	}

	private void sendMouseCode(int mouseMode) {
		SwitchKeyInfo mode_key_down = new SwitchKeyInfo(
				MotionEvent.ACTION_DOWN, KeyCode_Pointer_Switch, mouseMode);
		SwitchKeyInfo mode_key_up = new SwitchKeyInfo(MotionEvent.ACTION_UP,
				KeyCode_Pointer_Switch, mouseMode);
		Message msg_down = Message.obtain();
		msg_down.what = MessageID.CMD_SEND_KEY_MODE;
		msg_down.arg1 = 0;
		msg_down.arg2 = 0;
		msg_down.obj = mode_key_down;
		NetCmdMsgHandler.sendMessage(msg_down);

		Message msg_up = Message.obtain();
		msg_up.what = MessageID.CMD_SEND_KEY_MODE;
		msg_up.arg1 = 0;
		msg_up.arg2 = 0;
		msg_up.obj = mode_key_up;
		NetCmdMsgHandler.sendMessage(msg_up);
	}

	private void activeAlphaButtons() {
		AlphaButtons.clear();
		AlphaButtons.add((Button) findViewById(R.id.Keyboard_Button_Alpha_A));
		AlphaButtons.add((Button) findViewById(R.id.Keyboard_Button_Alpha_B));
		AlphaButtons.add((Button) findViewById(R.id.Keyboard_Button_Alpha_C));
		AlphaButtons.add((Button) findViewById(R.id.Keyboard_Button_Alpha_D));
		AlphaButtons.add((Button) findViewById(R.id.Keyboard_Button_Alpha_E));
		AlphaButtons.add((Button) findViewById(R.id.Keyboard_Button_Alpha_F));
		AlphaButtons.add((Button) findViewById(R.id.Keyboard_Button_Alpha_G));

		AlphaButtons.add((Button) findViewById(R.id.Keyboard_Button_Alpha_H));
		AlphaButtons.add((Button) findViewById(R.id.Keyboard_Button_Alpha_I));
		AlphaButtons.add((Button) findViewById(R.id.Keyboard_Button_Alpha_J));
		AlphaButtons.add((Button) findViewById(R.id.Keyboard_Button_Alpha_K));
		AlphaButtons.add((Button) findViewById(R.id.Keyboard_Button_Alpha_L));
		AlphaButtons.add((Button) findViewById(R.id.Keyboard_Button_Alpha_M));
		AlphaButtons.add((Button) findViewById(R.id.Keyboard_Button_Alpha_N));

		AlphaButtons.add((Button) findViewById(R.id.Keyboard_Button_Alpha_O));
		AlphaButtons.add((Button) findViewById(R.id.Keyboard_Button_Alpha_P));
		AlphaButtons.add((Button) findViewById(R.id.Keyboard_Button_Alpha_Q));
		AlphaButtons.add((Button) findViewById(R.id.Keyboard_Button_Alpha_R));
		AlphaButtons.add((Button) findViewById(R.id.Keyboard_Button_Alpha_S));
		AlphaButtons.add((Button) findViewById(R.id.Keyboard_Button_Alpha_T));

		AlphaButtons.add((Button) findViewById(R.id.Keyboard_Button_Alpha_U));
		AlphaButtons.add((Button) findViewById(R.id.Keyboard_Button_Alpha_V));
		AlphaButtons.add((Button) findViewById(R.id.Keyboard_Button_Alpha_W));
		AlphaButtons.add((Button) findViewById(R.id.Keyboard_Button_Alpha_X));
		AlphaButtons.add((Button) findViewById(R.id.Keyboard_Button_Alpha_Y));
		AlphaButtons.add((Button) findViewById(R.id.Keyboard_Button_Alpha_Z));

		for (Button btn : AlphaButtons) {
			btn.setOnTouchListener(AlphaButton_OTL);
		}
	}

	private void activeDigitButtons() {
		DigitButtons.clear();
		DigitButtons.add((Button) findViewById(R.id.Keyboard_Button_Digit_0));
		DigitButtons.add((Button) findViewById(R.id.Keyboard_Button_Digit_1));
		DigitButtons.add((Button) findViewById(R.id.Keyboard_Button_Digit_2));
		DigitButtons.add((Button) findViewById(R.id.Keyboard_Button_Digit_3));
		DigitButtons.add((Button) findViewById(R.id.Keyboard_Button_Digit_4));
		DigitButtons.add((Button) findViewById(R.id.Keyboard_Button_Digit_5));
		DigitButtons.add((Button) findViewById(R.id.Keyboard_Button_Digit_6));
		DigitButtons.add((Button) findViewById(R.id.Keyboard_Button_Digit_7));
		DigitButtons.add((Button) findViewById(R.id.Keyboard_Button_Digit_8));
		DigitButtons.add((Button) findViewById(R.id.Keyboard_Button_Digit_9));

		for (Button btn : DigitButtons) {
			btn.setOnTouchListener(DigitButton_OTL);
		}
	}

	private void activeDigitButtons_sn() {
		DigitButtons.clear();
		DigitButtons
				.add((Button) findViewById(R.id.Keyboard_Button_Digit_0_sn));
		DigitButtons
				.add((Button) findViewById(R.id.Keyboard_Button_Digit_1_sn));
		DigitButtons
				.add((Button) findViewById(R.id.Keyboard_Button_Digit_2_sn));
		DigitButtons
				.add((Button) findViewById(R.id.Keyboard_Button_Digit_3_sn));
		DigitButtons
				.add((Button) findViewById(R.id.Keyboard_Button_Digit_4_sn));
		DigitButtons
				.add((Button) findViewById(R.id.Keyboard_Button_Digit_5_sn));
		DigitButtons
				.add((Button) findViewById(R.id.Keyboard_Button_Digit_6_sn));
		DigitButtons
				.add((Button) findViewById(R.id.Keyboard_Button_Digit_7_sn));
		DigitButtons
				.add((Button) findViewById(R.id.Keyboard_Button_Digit_8_sn));
		DigitButtons
				.add((Button) findViewById(R.id.Keyboard_Button_Digit_9_sn));

		for (Button btn : DigitButtons) {
			btn.setOnTouchListener(DigitButton_OTL);
		}
	}

	private void sendKeyCode(int keyCode, int action) {
		KeyInfo mKey = new KeyInfo(action, keyCode);
		Message msg = Message.obtain();
		msg.what = MessageID.CMD_SEND_KEY;
		msg.arg1 = 0;
		msg.arg2 = 0;
		msg.obj = mKey;
		NetCmdMsgHandler.sendMessage(msg);
	}

	private OnTouchListener Func_OTL = new OnTouchListener() {
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_DOWN:
				int KeyCode = 0;
				Button btn = (Button) v;
				switch (btn.getId()) {
				case R.id.Keyboard_Button_Del:
					KeyCode = KeyCode_Del;
					break;
				case R.id.Keyboard_Button_Enter:
					KeyCode = KeyCode_Enter;
					break;
				case R.id.Keyboard_Button_Space:
					KeyCode = KeyCode_Space;
					break;
				case R.id.Keyboard_Button_Shift_Left:
					KeyCode = KeyCode_Shift_Left;
					break;
				case R.id.Keyboard_Button_Alt_Left:
					KeyCode = KeyCode_Alt_Left;
					break;
				case R.id.Keyboard_Button_Sym:
					KeyCode = KeyCode_Sym;
					break;
				default:
					// Log.d(TAG, "Func_OTL Unknown View Id");
					break;
				}
				sendKeyCode(KeyCode, event.getAction());
				break;
			default:
				// Log.d(TAG, "Func_OTL Unknown Action Type");
				break;
			}
			return false;
		}
	};

	private OnTouchListener Func_OTL_sn = new OnTouchListener() {
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_DOWN:
				int KeyCode = 0;
				Button btn = (Button) v;
				switch (btn.getId()) {
				case R.id.Keyboard_Button_Del_sn:
					KeyCode = KeyCode_Del;
					break;
				case R.id.Keyboard_Button_Enter_sn:
					KeyCode = KeyCode_Enter;
					break;
				case R.id.Keyboard_Button_Space_sn:
					KeyCode = KeyCode_Space;
					break;
				case R.id.Keyboard_Button_Shift_Left_sn:
					KeyCode = KeyCode_Shift_Left;
					break;
				case R.id.Keyboard_Button_Alt_Left_sn:
					KeyCode = KeyCode_Alt_Left;
					break;
				case R.id.Keyboard_Button_Sym_sn:
					KeyCode = KeyCode_Sym;
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

	private void activeFunctionButtons() {
		FunctionButtons.clear();
		FunctionButtons.add((Button) findViewById(R.id.Keyboard_Button_Del));
		FunctionButtons.add((Button) findViewById(R.id.Keyboard_Button_Enter));
		FunctionButtons.add((Button) findViewById(R.id.Keyboard_Button_Space));
		FunctionButtons
				.add((Button) findViewById(R.id.Keyboard_Button_Shift_Left));
		FunctionButtons
				.add((Button) findViewById(R.id.Keyboard_Button_Alt_Left));
		FunctionButtons.add((Button) findViewById(R.id.Keyboard_Button_Sym));

		for (Button btn : FunctionButtons) {
			btn.setOnTouchListener(Func_OTL);
		}
	}

	private void activeFunctionButtons_sn() {
		FunctionButtons.clear();
		FunctionButtons.add((Button) findViewById(R.id.Keyboard_Button_Del_sn));
		FunctionButtons
				.add((Button) findViewById(R.id.Keyboard_Button_Enter_sn));
		FunctionButtons
				.add((Button) findViewById(R.id.Keyboard_Button_Space_sn));
		FunctionButtons
				.add((Button) findViewById(R.id.Keyboard_Button_Shift_Left_sn));
		FunctionButtons
				.add((Button) findViewById(R.id.Keyboard_Button_Alt_Left_sn));
		FunctionButtons.add((Button) findViewById(R.id.Keyboard_Button_Sym_sn));

		for (Button btn : FunctionButtons) {
			btn.setOnTouchListener(Func_OTL_sn);
		}
	}

	private class IncomingMsgCallback implements Handler.Callback {
		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case MessageID.WIFI_TCP_TRANS_DOWN:
				//ServiceSet.getInstance().removeEntryByIP(
				//		NetCore.getinstance().getCurConnectedService().getIp());
				if (popUpWin.isShowing()) {
					popUpWin.cancel();
				}
				popUpWin.setTitle(R.string.service_timeout);
				popUpWin.show();
				break;
			case MessageID.WIFI_HOTSPOT_DISCONNECTED:
				if (popUpWin.isShowing()) {
					popUpWin.cancel();
				}
				popUpWin.setTitle(R.string.wifi_state_error);
				popUpWin.show();
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

	private Handler IncomingMsgHandler = new Handler(new IncomingMsgCallback());

	private int TouchCtoSTrans_X(float touch_x) {
		return (int) touch_x * 1000 / TouchArea.getWidth() / 2;
	}

	private int TouchCtoSTrans_Y(float touch_y) {
		return (int) touch_y * 1000 / TouchArea.getHeight() / 2;
	}
}

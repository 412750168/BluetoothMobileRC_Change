/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package zzl.bestidear.BluetoothChat.Activity;

import java.util.Set;

import net.bestidear.BluetoothMobileRC.R;

import zzl.bestidear.BluetoothChat.Tools.MessageID;
import zzl.bestidear.BluetoothChat.Service.NetCmdProcessingThread;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * This Activity appears as a dialog. It lists any paired devices and devices
 * detected in the area after discovery. When a device is chosen by the user,
 * the MAC address of the device is sent back to the parent Activity in the
 * result Intent.
 */
public class DeviceListActivity extends Activity {
	// Debugging
	private static final String TAG = "DeviceListActivity";
	private static final boolean D = true;
	private static final int REQUEST_ENABLE_BT = 3;

	private static final int CODE_FOR_IR_REMOTE = 0x400+1;
	private static final int CODE_FOR_POINTERKEYBOARD = 0x400+2;
	private static final int CODE_FOR_TV_REMOTE = 0x400+3;

	// Return Intent extra
	public static String EXTRA_DEVICE_ADDRESS = "device_address";

	// Member fields
	private BluetoothAdapter mBtAdapter;
	private ArrayAdapter<String> mPairedDevicesArrayAdapter;
	private ArrayAdapter<String> mNewDevicesArrayAdapter;
	// private BluetoothChatService mChatService = null;

	private int i = 0;

	private Handler General_IncomingMsg_Handler = new Handler(
			new IncomingMsgCallback());
	private Handler NetCmd_OutgoingMsg_Handler = null;
	
	private ProgressDialog dialog2 = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Setup the window
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.device_list);

		// Set result CANCELED in case the user backs out
		setResult(Activity.RESULT_CANCELED);

		// Get local Bluetooth adapter
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (mBtAdapter == null) {
			Toast.makeText(this, "Your device is not support Bluetooth",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		if (!mBtAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			// Otherwise, setup the chat session
		} else {
			// if (mChatService == null) setupChat();
			initData();
		}

		NetCmdProcessingThread.getInstance().setFeedbackHandler(
				General_IncomingMsg_Handler);
		NetCmd_OutgoingMsg_Handler = NetCmdProcessingThread.getInstance()
				.getCmdProcMsgHandler();
		
		// mChatService = new BluetoothChatService(this);
		// mChatService.start();

	}

	private void sendNetCmdMessage(int what, int arg1, int arg2, Object obj) {
		Message msg = Message.obtain();
		msg.what = what;
		msg.arg1 = arg1;
		msg.arg2 = arg2;
		msg.obj = obj;
		NetCmd_OutgoingMsg_Handler.sendMessage(msg);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub

		String result = null;

		switch (requestCode) {

			case REQUEST_ENABLE_BT:// 这是蓝牙设置的返回
				// When the request to enable Bluetooth returns
				if (resultCode == Activity.RESULT_OK) {
					// Bluetooth is now enabled, so set up a chat session
					initData();
				} else {
					// User did not enable Bluetooth or an error occurred
					Log.d(TAG, "BT not enabled");
					Toast.makeText(this, R.string.bt_not_enabled_leaving,
							Toast.LENGTH_SHORT).show();
					finish();
				}
				break;
	
			case CODE_FOR_IR_REMOTE:// 这是IR remote activity 返回
	
				result = data.getExtras().getString(Activity_IR_Remote.RESULT_NAME);
	
				if (result.equals(Activity_IR_Remote.RETURN_BY_PK_BUTTON)) {
					Intent intent = new Intent(this, Activity_PointerKeyboard.class);
					startActivityForResult(intent, CODE_FOR_POINTERKEYBOARD);
				}
	
				if (result.equals(Activity_IR_Remote.RETURN_BY_BACK_KEY)) {
					sendNetCmdMessage(MessageID.CMD_DISCONNECT, 0, 0, null);// 把 tcp
					initData();														// 给关闭了
					break;
				}
				break;
	
			case CODE_FOR_POINTERKEYBOARD:
				result = data.getExtras().getString(
						Activity_PointerKeyboard.RESULT_NAME);
				if (result.equals(Activity_PointerKeyboard.RETURN_BY_PK_BUTTON)) {
					Intent intent = new Intent(this, Activity_TV_Remote.class);
					startActivityForResult(intent, CODE_FOR_TV_REMOTE);
				}
				if (result.equals(Activity_PointerKeyboard.RETURN_BY_BACK_KEY)) {
					// TCPTransMonitoringThread.getInstance().stopMonitoring();
					sendNetCmdMessage(MessageID.CMD_DISCONNECT, 0, 0, null);
					initData();
					break;
				}
	
				break;
				
			case CODE_FOR_TV_REMOTE:
				result = data.getExtras().getString(Activity_TV_Remote.RESULT_NAME);
				if(result.equals(Activity_TV_Remote.RETURN_BY_PK_BUTTON))
				{
					Intent intent = new Intent(this, Activity_IR_Remote.class);
					startActivityForResult(intent, CODE_FOR_IR_REMOTE);
				}
				
				if(result.equals(Activity_TV_Remote.RETURN_BY_BACK_KEY))
				{
					//TCPTransMonitoringThread.getInstance().stopMonitoring();
					sendNetCmdMessage(MessageID.CMD_DISCONNECT, 0, 0, null);
					initData();
					break;
				}
				break;
				
				default :
					break;
			
		}
	}

	private void initData() {

		// Initialize the button to perform device discovery
		Button scanButton = (Button) findViewById(R.id.button_scan);
		scanButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// String s = "zzl:::"+i++;
				// mChatService.write(s.getBytes());
				doDiscovery();
				// v.setVisibility(View.GONE);
			}
		});

		// Initialize array adapters. One for already paired devices and
		// one for newly discovered devices
		mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this,
				R.layout.device_name);
		mNewDevicesArrayAdapter = new ArrayAdapter<String>(this,
				R.layout.device_name);

		// Find and set up the ListView for paired devices
		ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
		pairedListView.setAdapter(mPairedDevicesArrayAdapter);
		pairedListView.setOnItemClickListener(mDeviceClickListener);

		// Find and set up the ListView for newly discovered devices
		ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
		newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
		newDevicesListView.setOnItemClickListener(mDeviceClickListener);

		// Register for broadcasts when a device is discovered
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		this.registerReceiver(mReceiver, filter);

		// Register for broadcasts when discovery has finished
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		this.registerReceiver(mReceiver, filter);

		// Get the local Bluetooth adapter
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();

		// Get a set of currently paired devices
		Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

		// If there are paired devices, add each one to the ArrayAdapter
		if (pairedDevices.size() > 0) {
			findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
			for (BluetoothDevice device : pairedDevices) {
				mPairedDevicesArrayAdapter.add(device.getName() + "\n"
						+ device.getAddress());
			}
		} else {
			String noDevices = getResources().getText(R.string.none_paired)
					.toString();
			mPairedDevicesArrayAdapter.add(noDevices);
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// Make sure we're not doing discovery anymore
		if (mBtAdapter != null) {
			mBtAdapter.cancelDiscovery();
		}

		// Unregister broadcast listeners
		if (mReceiver.isInitialStickyBroadcast())
			this.unregisterReceiver(mReceiver);

		// if (mChatService != null)
		// mChatService.stop();

	}

	/**
	 * Start device discover with the BluetoothAdapter
	 */
	private void doDiscovery() {
		if (D)
			Log.d(TAG, "doDiscovery()");

		// Indicate scanning in the title
		setProgressBarIndeterminateVisibility(true);
		setTitle(R.string.scanning);

		// Turn on sub-title for new devices
		findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

		// If we're already discovering, stop it
		if (mBtAdapter.isDiscovering()) {
			mBtAdapter.cancelDiscovery();
		}

		// Request discover from BluetoothAdapter
		mBtAdapter.startDiscovery();
	}

	// The on-click listener for all devices in the ListViews
	private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
			// Cancel discovery because it's costly and we're about to connect
			mBtAdapter.cancelDiscovery();

			// Get the device MAC address, which is the last 17 chars in the
			// View
			String info = ((TextView) v).getText().toString();
			if(info.length() > 16){
			String address = info.substring(info.length() - 17);

			if (address != null)
				
				dialog2 = new ProgressDialog(DeviceListActivity.this);
				dialog2.setTitle("Connected to Bluetooth paired!");
				dialog2.setMessage("Please wait!");
				dialog2.setCancelable(true);
				dialog2.show();
				sendNetCmdMessage(MessageID.CMD_CONNECT, 0, 0, address);
			}
			// mBtAdapter = BluetoothAdapter.getDefaultAdapter();

			// BluetoothDevice device = mBtAdapter.getRemoteDevice(address);
			// Attempt to connect to the device
			// if(device != null && mChatService != null){
			// mChatService.connect(device, true);

			// }
			// Create the result Intent and include the MAC address
			// Intent intent = new Intent();
			// intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

			// Set result and finish this Activity
			// setResult(Activity.RESULT_OK, intent);
			// finish();
		}
	};

	// The BroadcastReceiver that listens for discovered devices and
	// changes the title when discovery is finished
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// If it's already paired, skip it, because it's been listed
				// already
				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
					mNewDevicesArrayAdapter.add(device.getName() + "\n"
							+ device.getAddress());
				}
				// When discovery is finished, change the Activity title
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
					.equals(action)) {
				setProgressBarIndeterminateVisibility(false);
				setTitle(R.string.select_device);
				if (mNewDevicesArrayAdapter.getCount() == 0) {
					String noDevices = getResources().getText(
							R.string.none_found).toString();
					mNewDevicesArrayAdapter.add(noDevices);
				}
			}
		}
	};

	private class IncomingMsgCallback implements Handler.Callback {
		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case MessageID.CMD_CONNECT_DONE:
				if(dialog2 != null)
					dialog2.dismiss();
				gotoKeyView();
				break;
			case MessageID.CMD_CONNECT_FAILED:
				if(dialog2 != null)
					dialog2.dismiss();
			}
			return false;
		}

	}

	private void gotoKeyView() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this, Activity_IR_Remote.class);
		startActivityForResult(intent, CODE_FOR_IR_REMOTE);
	}
}

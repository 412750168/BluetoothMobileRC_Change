package zzl.bestidear.BluetoothChat.Tools;

public final class MessageID
{
	private static final int MSG_INT = 100;
	
	public static final int CMD_CONNECT					= 0;
	public static final int CMD_SEND_KEY				= 1;
	public static final int CMD_SEND_TOUCH				= 2;
	public static final int CMD_SEND_SENSOR				= 3;
	public static final int CMD_DISCONNECT				= 4;
	public static final int CMD_SEND_GET_PICTURE		= 5;
	public static final int CMD_SEND_KEY_MODE			= 6;
	public static final int CMD_SEND_DATA				= 7;
	public static final int CMD_QUERY_SERVICE			= 8;
	public static final int CMD_HEARTBEAT			= 9;
	
	public static final int CMD_CONNECT_SUCCESS = 0x400 +1;
	public static final int CMD_CONNECT_BAD =  0x400 +2;
	
	public static final int CMD_CONNECT_DONE			= MSG_INT + 0;
	public static final int CMD_SEND_KEY_DONE			= MSG_INT + 1;
	public static final int CMD_SEND_TOUCH_DONE			= MSG_INT + 2;
	public static final int CMD_SEND_SENSOR_DONE		= MSG_INT + 3;
	public static final int CMD_DISCONNECT_DONE			= MSG_INT + 4;
	public static final int CMD_SEND_GET_PICTURE_DONE	= MSG_INT + 5;
	public static final int CMD_SEND_KEY_MODE_DONE		= MSG_INT + 6;
	public static final int CMD_SEND_DATA_DONE			= MSG_INT + 7;
	public static final int CMD_QUERY_SERVICE_DONE		= MSG_INT + 8;
	public static final int CMD_HEARTBEAR_DONE			= MSG_INT + 9;
	
	public static final int CMD_CONNECT_FAILED			= (2 * MSG_INT) + 0;
	public static final int CMD_SEND_KEY_FAILED			= (2 * MSG_INT) + 1;
	public static final int CMD_SEND_TOUCH_FAILED		= (2 * MSG_INT) + 2;
	public static final int CMD_SEND_SENSOR_FAILED		= (2 * MSG_INT) + 3;
	public static final int CMD_DISCONNECT_FAILED		= (2 * MSG_INT) + 4;
	public static final int CMD_SEND_GET_PICTURE_FAILED	= (2 * MSG_INT) + 5;
	public static final int CMD_SEND_KEY_MODE_FAILED	= (2 * MSG_INT) + 6;
	public static final int CMD_SEND_DATA_FAILED		= (2 * MSG_INT) + 7;
	public static final int CMD_QUERY_SERVICE_FAILED	= (2 * MSG_INT) + 8;
	public static final int CMD_HEARTBEAR_FAILED		= (2 * MSG_INT) + 9;
	
	public static final int SERVICE_INFO_RECEIVED		= (3 * MSG_INT) + 0;
	public static final int SERVICE_INFO_EXPIRED		= (3 * MSG_INT) + 1;
	public static final int SERVICE_INFO_DISCONNECTED	= (3 * MSG_INT) + 2;
	
	public static final int WIFI_HOTSPOT_CONNECTED		= (4 * MSG_INT) + 0;
	public static final int WIFI_HOTSPOT_DISCONNECTED	= (4 * MSG_INT) + 1;
	public static final int WIFI_TCP_TRANS_DOWN			= (4 * MSG_INT) + 2;
	
	public static final int FINISH_ACTIVITY = 0x500 + 1;
	
	public static enum CMD
	{
		CONNECT, CONNECT_DONE, CONNECT_FAILED,
		SEND_KEY, SEND_KEY_DONE, SEND_KEY_FAILED,
	};
}

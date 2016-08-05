package zzl.bestidear.BluetoothChat.Service;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;

public class LayoutInfo
{
	private float screenDensity = 0;
	private int screenWidth = 0;
	private int screenHeight = 0;
	private int screenDPI = DisplayMetrics.DENSITY_DEFAULT;
	private float xdpi = 0;
	private float ydpi = 0;
	
	@TargetApi(Build.VERSION_CODES.DONUT)
	public LayoutInfo(Context ctx)
	{
		screenDensity = ctx.getResources().getDisplayMetrics().density;
		screenWidth = ctx.getResources().getDisplayMetrics().widthPixels;
		screenHeight = ctx.getResources().getDisplayMetrics().heightPixels;
		screenDPI = ctx.getResources().getDisplayMetrics().densityDpi;
		xdpi = ctx.getResources().getDisplayMetrics().xdpi;
		ydpi = ctx.getResources().getDisplayMetrics().xdpi;
	}
	
	@Override
	public String toString()
	{
		String output = "Layout Info: Screen Density = " + screenDensity + ", " +
				"Screen Width = " + screenWidth + ", " +
				"Screen Height = " + screenHeight + ", ";
		
		switch(screenDPI)
		{
			case DisplayMetrics.DENSITY_LOW:
				output += "DENSITY_LOW: 120";
				break;
			case DisplayMetrics.DENSITY_MEDIUM:
				output += "DENSITY_MEDIUM: 160";
				break;
			case DisplayMetrics.DENSITY_TV:
				output += "DENSITY_TV: 213";
				break;
			case DisplayMetrics.DENSITY_HIGH:
				output += "DENSITY_HIGH: 240";
				break;
			case DisplayMetrics.DENSITY_XHIGH:
				output += "DENSITY_XHIGH: 320";
				break;
			case DisplayMetrics.DENSITY_XXHIGH:
				output += "DENSITY_XXHIGH: 480";
				break;
		}
		
		output += ", ";
		output += "xdpi = " + xdpi + ", ydpi = " + ydpi + ".";
		
		return output;
	}
	
	public int getScreenDpi()
	{
		return this.screenDPI;
	}
}

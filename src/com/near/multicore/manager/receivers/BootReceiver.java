package com.near.multicore.manager.receivers;

import com.near.multicore.manager.utils.SysTools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class BootReceiver extends BroadcastReceiver {
	
	SysTools sys;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			SharedPreferences prefs = context.getSharedPreferences("boot_prefs", Context.MODE_PRIVATE);
			if(prefs.getBoolean("SET_ON_BOOT", false))
				return;
			else {
				sys = new SysTools();
				boolean hotplug_on = prefs.getBoolean("HOTPLUG_ON", true);
				if(hotplug_on)
					sys.writeToFile("echo on > /sys/devices/virtual/misc/multi_core/hotplug_on");
				else {
					sys.writeToFile("echo off > /sys/devices/virtual/misc/multi_core/hotplug_on");
					int cores_on = prefs.getInt("NR_OF_CORES", 4);
					sys.writeToFile("echo " + cores_on + " > /sys/devices/virtual/misc/multi_core/cores_on");
				}
			}
		}
	}
	
}

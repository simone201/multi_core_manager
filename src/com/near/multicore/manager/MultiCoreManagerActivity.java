package com.near.multicore.manager;

import com.near.multicore.manager.R;
import com.near.multicore.manager.utils.SysTools;

import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.widget.Toast;

import java.io.File;

public class MultiCoreManagerActivity extends PreferenceActivity {
	
	private Resources res;
	
	private CheckBoxPreference multiSwitch;
	private ListPreference coresList;
	private CheckBoxPreference setBoot;
	private Preference author;
	private Preference online;
	
	private boolean state = false;
	private boolean oldState = false;
	
	SysTools sys;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
               
        sys = new SysTools();
        
        if(!checkSupport()) {
        	Toast.makeText(MultiCoreManagerActivity.this, "Your device doesn't support Multi-Core Interface!", Toast.LENGTH_LONG).show();
        	MultiCoreManagerActivity.this.finish();
        	return;
        }
        
        if(!checkGovernor()) {
        	Toast.makeText(MultiCoreManagerActivity.this, "Please select a non-hotplug governor!", Toast.LENGTH_LONG).show();
        	MultiCoreManagerActivity.this.finish();
        	return;
        }
        
        initiateUI();
        
        // Dynamic Hotplug
        multiSwitch.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
        	@Override
            public boolean onPreferenceChange(Preference preference,
                    Object newValue) {
        		updateCoresList();
        		return true;
        	}
        });
        
        setBoot.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
        	@Override
            public boolean onPreferenceChange(Preference preference,
                    Object newValue) {
        		setOnBoot(Boolean.parseBoolean(String.valueOf(newValue)));
        		return true;
        	}
        });
        
        // Select the nr of cores enabled
        coresList.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
        	@Override
            public boolean onPreferenceChange(Preference preference,
                    Object newValue) {
        		int value = Integer.parseInt((String) newValue);
        		changeCores(value);
        		return true;
        	}
        });
        
        // Link to author thread
        author.setOnPreferenceClickListener(new OnPreferenceClickListener() {
        	@Override
        	public boolean onPreferenceClick(Preference pref) {
        		Intent myIntent = new Intent(Intent.ACTION_VIEW,
        				Uri.parse("http://forum.xda-developers.com/showthread.php?t=1741385"));
        				startActivity(myIntent);
        		return true;
        	}
        });
        
        final Handler myHandler = new Handler() {
    		@Override
    		public void handleMessage(Message msg) {
    			online.setTitle(getResources().getString(R.string.cores_online_title) + " " + String.valueOf(msg.arg1));
    		}
    	};
        
        (new Thread(new Runnable() {
            
    		@Override
    		public void run() {
    			
    			while(true) {
    				Message msg = myHandler.obtainMessage();
    				msg.arg1 = 1;
    				try {
    					Thread.sleep(1000);
    					if(SysTools.getSystemString("/sys/devices/system/cpu/cpu1/online").equals("1")) {
    						msg.arg1 = 2;
    						if(SysTools.getSystemString("/sys/devices/system/cpu/cpu2/online").equals("1")) {
    							msg.arg1 = 3;
    							if(SysTools.getSystemString("/sys/devices/system/cpu/cpu3/online").equals("1")) {
    								msg.arg1 = 4;
    							}
    						}
    					}
    					myHandler.sendMessage(msg);
    				} catch (InterruptedException e) {
    					myHandler.sendMessage(msg);
    					e.printStackTrace();
    				}
    			}
    		}
    		
        })).start();
    }
    
    // Initializing UI
    private void initiateUI() {
    	addPreferencesFromResource(R.layout.main);
    	
    	res = getResources();
    	
    	multiSwitch = (CheckBoxPreference) findPreference(res.getString(R.string.multicore_pref));
    	coresList = (ListPreference) findPreference(res.getString(R.string.cores_nr_prefs));
    	setBoot = (CheckBoxPreference) findPreference(res.getString(R.string.prefSAVEBOOT));
    	author = (Preference) findPreference(res.getString(R.string.author_prefs));
    	online = (Preference) findPreference(res.getString(R.string.cores_on_prefs));
    	
    	coresList.setEnabled(!checkHotplugOn());
    	coresList.setDefaultValue(getCores());
    	
    	multiSwitch.setChecked(checkHotplugOn());
    }
    
    // Set on Boot
    private void setOnBoot(boolean state) {
    	savePreferences();
    }
    
    // Save Preferences for save on boot
    private void savePreferences() {
    	SharedPreferences prefs = getSharedPreferences("boot_prefs", Context.MODE_PRIVATE);
    	Editor edit = prefs.edit();
    	edit.putBoolean("HOTPLUG_ON", multiSwitch.isChecked());
    	edit.putBoolean("SET_ON_BOOT", setBoot.isChecked());
    	edit.putInt("NR_OF_CORES", Integer.parseInt(coresList.getValue()));
    	edit.commit();
    }
    
    // Updating Cores ListPreference (and set hotplug on/off)
    private void updateCoresList() {
    	if(oldState) {
    		state = false;
    		oldState = state;
    	} else {
    		state = true;
    		oldState = state;
    	}
    	
    	coresList.setEnabled(!state);
    	coresList.setValue("4");
    	
    	String value;
    	
    	if(state)
    		value = "on";
    	else
    		value = "off";
    	
    	sys.writeToFile("echo " + value + " > /sys/devices/virtual/misc/multi_core/hotplug_on");
    }
    
    // Get the nr of cores enabled in the interface
    private int getCores() {
    	return Integer.parseInt(sys.getSystemFileString("/sys/devices/virtual/misc/multi_core/cores_on"));
    }
    
    // Set the nr of cores enabled in the interface
    private void changeCores(int value) {
    	sys.writeToFile("echo " + value + " > /sys/devices/virtual/misc/multi_core/cores_on");
    }
    
    // Check interface support
    private boolean checkSupport() {
    	File f = new File("/sys/devices/virtual/misc/multi_core");
    	return f.exists();
    }
    
    // Check the current governor (it should not be an hotplug-aware governor)
    private boolean checkGovernor() {
    	
    	String gov = sys.getSystemFileString("/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor");
    	
    	if(gov.equals("pegasusq") || gov.equals("hotplug") || gov.equals("abyssplug"))
    		return false;
    	else
    		return true;
    }
    
    // Check if hotplug is enabled/disabled
    private boolean checkHotplugOn() {
    	String hotplug = sys.getSystemFileString("/sys/devices/virtual/misc/multi_core/hotplug_on");
    	
    	if(hotplug.equals("on")) {
    		oldState = true;
    		return true;
    	}
    	else {
    		oldState = false;
    		return false;
    	}
    }
    
}
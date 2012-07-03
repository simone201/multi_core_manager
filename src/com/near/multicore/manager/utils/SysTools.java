package com.near.multicore.manager.utils;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class SysTools {
	private static final String TAG = "SysTools";

	public String getSystemFileString(String filePath) {
		Log l = new Log();
		
        String line;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("cat " + filePath);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        }
        catch (IOException ex) {
            l.e(TAG, "Unable to read " + filePath, ex);
            return null;
        }
        finally {
            if (input != null) {
                try {
                    input.close();
                }
                catch (IOException e) {
                    l.e(TAG, "Exception while closing InputStream", e);
                }
            }
        }
        return line;
    }
	
	// Just for pleasure
	public static String getSystemString(String filePath) {
		Log l = new Log();
		
        String line;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("cat " + filePath);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        }
        catch (IOException ex) {
            l.e(TAG, "Unable to read " + filePath, ex);
            return null;
        }
        finally {
            if (input != null) {
                try {
                    input.close();
                }
                catch (IOException e) {
                    l.e(TAG, "Exception while closing InputStream", e);
                }
            }
        }
        return line;
    }
	
	// Get a command shell output
	public String getCommandOutput(String cmd) {
		Log l = new Log();
		
        String line;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        }
        catch (IOException ex) {
            l.e(TAG, "Unable to run: " + cmd, ex);
            return null;
        }
        finally {
            if (input != null) {
                try {
                    input.close();
                }
                catch (IOException e) {
                    l.e(TAG, "Exception while closing InputStream", e);
                }
            }
        }
        return line;
    }
	
	// Writing to a file
	public void writeToFile(String s) {
		Process p = null;
	    try {
	    	p = Runtime.getRuntime().exec("su");
	        DataOutputStream os = new DataOutputStream(p.getOutputStream());
	        os.writeBytes("exec " + s + "\n");
	        os.flush();
	        p.waitFor();
	        p.destroy();
	    }
	    catch (Exception e) {
	        p = null;
	    }
	}
	
}

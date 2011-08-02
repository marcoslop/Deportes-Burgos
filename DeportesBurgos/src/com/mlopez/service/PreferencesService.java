package com.mlopez.service;


import com.mlopez.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferencesService {

	private static Context context;
	
	private static final String DNI = "dni";
	private static final String PASSWORD = "password";
	
	
	private static SharedPreferences getPreferences (){
		PreferenceManager.setDefaultValues(context, R.xml.preferences, false);
		SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
		return p;
	}
	
	public static String getDni (){
		SharedPreferences p = getPreferences();
		String dni = p.getString(DNI, "");
		return dni.trim();
	}
	
	public static String getPassword (){
		SharedPreferences p = getPreferences();
		String password = p.getString(PASSWORD, "");
		return password.trim();
	}
	
	public static boolean isLoginConfigured (){
		if ("".equals(getDni()) || "".equals(getPassword())){
			return false;
		}
		return true;
	}

	public static void setContext(Context c) {
		context = c;
	}
	
	
}

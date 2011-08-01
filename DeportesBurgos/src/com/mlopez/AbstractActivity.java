package com.mlopez;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public abstract class AbstractActivity extends Activity{
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.acercade:
			Intent intentResults = new Intent(this, AcercaDeActivity.class);
        	startActivity(intentResults);
			return true;
		case R.id.preferences:
			Intent preferences = new Intent (this, PreferencesFromXml.class);
			startActivity(preferences);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	
}

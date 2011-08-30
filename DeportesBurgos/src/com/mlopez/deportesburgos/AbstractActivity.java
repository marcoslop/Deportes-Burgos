package com.mlopez.deportesburgos;

import com.mlopez.deportesburgos.R;
import com.mlopez.deportesburgos.service.PreferencesService;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public abstract class AbstractActivity extends Activity{
	
	protected Activity mainContent = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mainContent = this;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
//		case R.id.acercade:
//			Intent intentResults = new Intent(this, AcercaDeActivity.class);
//        	startActivity(intentResults);
//			return true;
		case R.id.preferences:
			Intent preferences = new Intent (this, PreferencesFromXml.class);
			startActivity(preferences);
			return true;
		case R.id.reservas:
			if (!PreferencesService.isLoginConfigured()){
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage("Para ver las reservas es necesario configurar un dni y contraseña con la que conectarse. ¿Desea hacerlo ahora?")
				.setCancelable(true)
				.setPositiveButton("Si", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
						Intent preferences = new Intent (mainContent, PreferencesFromXml.class);
						startActivity(preferences);
					}
				})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
				AlertDialog alert = builder.create();
				alert.show();
			}else{
				Intent pref = new Intent (this, ReservasActivity.class);
				startActivity(pref);
				return true;
			}
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	
}

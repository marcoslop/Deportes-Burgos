package com.mlopez;

import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.mlopez.beans.Deporte;
import com.mlopez.beans.Lugar;
import com.mlopez.service.DeportesService;
import com.mlopez.service.PreferencesService;

public class DeportesBurgosActivity extends AbstractActivity {

	final Handler mHandler = new Handler();

	private Activity mainContent = null;

	private boolean userConfigured = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mainContent = this;

		setContentView(R.layout.main);

		init ();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		//Si el usuario no tenía configurado usuario y contraseña antes y ahora si hay que ampliar el abanico de fechas.
		if (!userConfigured && PreferencesService.isLoginConfigured()){
			paintDiasSpinner();
		}
	}

	private void init (){
		PreferencesService.setContext(this);

		paintDeportesSpinner();
		paintLugaresSpinner(null);
		paintDiasSpinner();

		Button buttonSearch = (Button) findViewById(R.id.buttonSearch);
		buttonSearch.setOnClickListener(new SearchListener());
	}

	private void paintDeportesSpinner (){
		//Pintamos todas los deportes
		Spinner spinnerActivity = (Spinner) findViewById(R.id.spinnerActivity);
		List<Deporte> deportes = DeportesService.getAllDeportes();
		ArrayAdapter<Deporte> deportesAdapter = new ArrayAdapter<Deporte>(this, android.R.layout.simple_spinner_item, deportes);
		deportesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerActivity.setAdapter(deportesAdapter);
		spinnerActivity.setOnItemSelectedListener(new DeporteItemSelectedListener());
	}

	private void paintLugaresSpinner (Deporte deporte){
		//Pintamos todas los lugares
		Spinner spinnerWhere = (Spinner) findViewById(R.id.spinnerWhere);
		List<Lugar> lugares = null;
		if (deporte == null){
			lugares = DeportesService.getAllLugares();
		}else{
			lugares = deporte.getLugares();
		}
		ArrayAdapter<Lugar> deportesAdapter = new ArrayAdapter<Lugar>(this, android.R.layout.simple_spinner_item, lugares);
		deportesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerWhere.setAdapter(deportesAdapter);
	}

	private void paintDiasSpinner (){
		//Pintamos todas los deportes
		Spinner spinnerDay = (Spinner) findViewById(R.id.spinnerDay);
		//Recuperamos la posición del elemento seleccionado porque puede ser que estemos ampliando
		// las fechas actuales. Después de ello habrá que seguir marcando la posición actual.
		int selectedItemPosition = spinnerDay.getSelectedItemPosition();
		List<String> fechas = DeportesService.getFechas();
		ArrayAdapter<String> fechasAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, fechas);
		fechasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerDay.setAdapter(fechasAdapter);

		if (selectedItemPosition >= 0){
			spinnerDay.setSelection(selectedItemPosition);
		}
		if (PreferencesService.isLoginConfigured()){
			userConfigured = true;
		}
	}


	final Runnable mUpdateResults = new Runnable() {
		public void run() {
			//Cargar la pantalla de resultados.
			Intent intentResults = new Intent(mainContent, SearchResultsActivity.class);
			startActivity(intentResults);
		}
	};

	private void doSearch () {
		final ProgressDialog dialog = ProgressDialog.show(this, "", "Buscando", true);
		new Thread() {
			public void run() {
				try {
					Spinner spinnerActivity = (Spinner) findViewById(R.id.spinnerActivity);
					Deporte selectedDeporte = (Deporte) spinnerActivity.getSelectedItem();
					Spinner spinnerLugar = (Spinner) findViewById(R.id.spinnerWhere);
					Lugar selectedLugar = (Lugar) spinnerLugar.getSelectedItem();
					Spinner spinnerDay = (Spinner) findViewById(R.id.spinnerDay);
					String selectedDia = (String)spinnerDay.getSelectedItem();
					DeportesService.searchActivities(selectedDeporte.getCode(), selectedLugar.getCode(), selectedDia);
					mHandler.post(mUpdateResults);
				} catch (Exception e) {
					e.printStackTrace();
					final String errorMessage = e.getMessage();
					mHandler.post(new Runnable() {
						public void run() {
							Toast.makeText(mainContent, errorMessage, Toast.LENGTH_LONG).show();
						}
					});
				}
				dialog.dismiss();
			}
		}.start();
	}

	public class DeporteItemSelectedListener implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> parent,
				View view, int pos, long id) {
			//Obtenemos el Deporte seleccionado
			Deporte deporte = (Deporte) parent.getItemAtPosition(pos);
			paintLugaresSpinner(deporte);
		}

		public void onNothingSelected(AdapterView parent) {}
	}

	public class SearchListener implements OnClickListener {

		@Override
		public void onClick(View view) {
			doSearch();
		}        
	}
	
}
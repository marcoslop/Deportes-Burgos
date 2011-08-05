package com.mlopez;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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

	

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		init ();
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
	}


	final Runnable mUpdateResults = new Runnable() {
		public void run() {
			//Cargar la pantalla de resultados.
			Intent intentResults = new Intent(mainContent, SearchResultsActivity.class);
			startActivity(intentResults);
		}
	};

	private void doSearch () {
		//Primero comprobamos que si el usuario no está logeado no haga una busqueda de un día mayor de 8 dias. 
		final Spinner spinnerDay = (Spinner) findViewById(R.id.spinnerDay);
		if (!PreferencesService.isLoginConfigured() && spinnerDay.getSelectedItemPosition() >= 8){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Para realizar búsquedas de más de 8 días es necesario configurar un dni y contraseña con la que conectarse. ¿Desea hacerlo ahora?")
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
			final ProgressDialog dialog = ProgressDialog.show(this, "", "Buscando", true);		
			new Thread() {
				public void run() {
					try {
						Spinner spinnerActivity = (Spinner) findViewById(R.id.spinnerActivity);
						Deporte selectedDeporte = (Deporte) spinnerActivity.getSelectedItem();
						Spinner spinnerLugar = (Spinner) findViewById(R.id.spinnerWhere);
						Lugar selectedLugar = (Lugar) spinnerLugar.getSelectedItem();
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
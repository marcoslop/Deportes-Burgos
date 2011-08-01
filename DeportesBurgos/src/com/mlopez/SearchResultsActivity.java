package com.mlopez;

import java.util.List;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.mlopez.beans.Hora;
import com.mlopez.beans.Pista;
import com.mlopez.service.DeportesService;

public class SearchResultsActivity extends AbstractActivity implements OnClickListener{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.results);
		
		TableLayout layout = (TableLayout)findViewById(R.id.tLayout);
		
		//layout.setLayoutParams( new TableLayout.LayoutParams(4,5) );
		//layout.setPadding(1,1,1,1);

		List<Pista> pistas = DeportesService.getLastSearchResultsPistas();


		//Añadimos la fila de las pistas
		TableRow tr = new TableRow(this);
		int index = 0;
		for (Pista pista : pistas) {
			TextView tv = new TextView(this);
			tv.setText(pista.getComplejo()+"\n"+pista.getNombre());
			tv.setPadding(5, 0, 0, 0);
			tr.addView(tv);
			
			layout.setColumnStretchable(index, true);
			index++;
		}
		layout.addView(tr);

		//Nos recorremos las horas de la primera pista porque suponemos que todas van a tener la misma
		// cantidad de horas.

		Resources res = getResources();
		
		index = 0;
		for (Hora h : pistas.get(0).getHoras()){
			tr = new TableRow(this);
			//Nos recorremos la hora en cuestión de todas las pistas y creamos un botón
			for (Pista pista : pistas) {
				Hora hora = pista.getHoras().get(index);
				if (hora.isVisible()){
					Button b = new Button (this);
					b.setText(hora.getHora());
					b.setTextSize(12.0f);
					b.setTextColor(Color.rgb( 0, 0, 0));
					
					if (hora.isDisponible()){
						b.setBackgroundDrawable(res.getDrawable(R.drawable.free_button));
						b.setOnClickListener(this);
						//b.setTextColor(Color.rgb( 0, 255, 0));
						//b.setBackgroundColor(Color.rgb( 0, 255, 0));
					}else{
						b.setEnabled(false);
						b.setBackgroundDrawable(res.getDrawable(R.drawable.busy_button));
						//b.setTextColor(Color.rgb( 255, 0, 0));
						//b.setBackgroundColor(Color.rgb( 255, 0, 0));
					}
					//b.setOnClickListener(this);
					b.setPadding(5, 5, 5, 5);
					tr.addView(b, 30,50);
				}
			}
			layout.addView(tr);
			index++;
		}
		//super.setContentView(layout);
		
	}

	@Override
	public void onClick(View arg0) {
		Toast.makeText(this, "Reserva de instalaciones no implementado todavia. Habrá que esperar a la siguiente versión", Toast.LENGTH_LONG).show();
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
		case R.id.acercade:
			Intent intentResults = new Intent(this, AcercaDeActivity.class);
        	startActivity(intentResults);
			return true;
//		case R.id.preferences:
//			Intent preferences = new Intent (this, PreferencesFromXml.class);
//			startActivity(preferences);
//			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}

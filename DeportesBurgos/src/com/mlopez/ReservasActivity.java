package com.mlopez;

import java.util.List;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.mlopez.beans.InfoReserva;
import com.mlopez.service.DeportesService;
import com.mlopez.service.DeportesServiceException;

public class ReservasActivity extends AbstractActivity {

	private List<InfoReserva> reservas;
	protected ListView listReservas;

	protected Activity mainContent = null;

	final Handler mHandler = new Handler();
	
	private static final int VER_DETALLE = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mainContent = this;
		setContentView(R.layout.reservas);

		listReservas = (ListView)findViewById(R.id.listReservas);
		registerForContextMenu(listReservas);
		doSearchReservas();
	}

	private void doSearchReservas () {
		final ProgressDialog dialog = ProgressDialog.show(this, "", "Cargando reservas", true);
		new Thread() {
			public void run() {
				try {
					reservas = DeportesService.getMisReservas();
					mHandler.post(new Runnable() {
						public void run() {
							listReservas.setAdapter(new ArrayAdapter<InfoReserva>(mainContent, 
									android.R.layout.simple_list_item_1, reservas));
							listReservas.setTextFilterEnabled(true);
						}
					});
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

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (v.getId()==R.id.listReservas) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
			menu.setHeaderTitle("Acciones");
			MenuItem item = menu.add(Menu.NONE, VER_DETALLE, VER_DETALLE, "Ver detalle");
			Intent intent = new Intent();
			intent.putExtra("reservaId", reservas.get(info.position).getIdReserva());
			item.setIntent(intent);
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (VER_DETALLE == item.getItemId()){
			//Vamos al detalle
			String reservaId = item.getIntent().getStringExtra("reservaId");
			try {
				String htmlReserva = DeportesService.getDatosReserva(reservaId);
				Intent intentReserva = new Intent(mainContent, ResumenReservaActivity.class);
				intentReserva.putExtra("reserva", htmlReserva);
	        	startActivity(intentReserva);
			} catch (DeportesServiceException e) {
				Toast.makeText(mainContent, e.getMessage(), Toast.LENGTH_LONG).show();
			}
		}
		return true;
	}

}

package com.mlopez;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.mlopez.beans.InfoReserva;
import com.mlopez.service.DeportesService;
import com.mlopez.service.DeportesServiceException;
import com.mlopez.service.PreferencesService;

public class ReservasActivity extends AbstractActivity {

	private List<InfoReserva> reservas;
	protected ListView listReservas;

	protected Activity mainContent = null;

	final Handler mHandler = new Handler();
	
	private static final int VER_DETALLE = 0;
	private static final int ANULAR = 1;

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
			Intent intent = new Intent();
			intent.putExtra("reservaId", reservas.get(info.position).getIdReserva());
			
			MenuItem item = menu.add(Menu.NONE, VER_DETALLE, VER_DETALLE, "Ver detalle");
			item.setIntent(intent);
			item = menu.add(Menu.NONE, ANULAR, ANULAR, "Anular");
			item.setIntent(intent);
		}
	}
	
	@Override
	public boolean onContextItemSelected(final MenuItem item) {
		if (VER_DETALLE == item.getItemId()){
			final ProgressDialog dialog = ProgressDialog.show(this, "", "Cargando detalle", true);
			new Thread() {
				public void run() {
					try {
						//Vamos al detalle
						String reservaId = item.getIntent().getStringExtra("reservaId");
						final String htmlReserva = DeportesService.getDatosReserva(reservaId);
						mHandler.post(new Runnable() {
							public void run() {
								Intent intentReserva = new Intent(mainContent, ResumenReservaActivity.class);
								intentReserva.putExtra("reserva", htmlReserva);
					        	startActivity(intentReserva);
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
		}else if (ANULAR == item.getItemId()){
			final ProgressDialog dialog = ProgressDialog.show(this, "", "Anulando", true);
			new Thread() {
				public void run() {
					try {
						//Anulamos la reserva
						String reservaId = item.getIntent().getStringExtra("reservaId");
						DeportesService.anularReserva(reservaId);
						mHandler.post(new Runnable() {
							public void run() {
								Toast.makeText(mainContent, "Reserva anulada correctamente", Toast.LENGTH_LONG).show();
								doSearchReservas();
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
		return true;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_reservas, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.actualizar:
			doSearchReservas();
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}

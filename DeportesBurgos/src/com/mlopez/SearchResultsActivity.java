package com.mlopez;

import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.mlopez.beans.Deporte;
import com.mlopez.beans.Hora;
import com.mlopez.beans.InfoReserva;
import com.mlopez.beans.Lugar;
import com.mlopez.beans.Pista;
import com.mlopez.service.DeportesService;
import com.mlopez.service.DeportesServiceException;
import com.mlopez.service.PreferencesService;

public class SearchResultsActivity extends AbstractActivity {

	final Handler mHandler = new Handler();

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
						b.setOnClickListener(new MyOnClickButtonListener(hora));
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

	public class MyOnClickButtonListener implements OnClickListener{

		private Hora hora;

		public MyOnClickButtonListener(Hora hora) {
			this.hora = hora;
		}

		@Override
		public void onClick(View v) {
			//Miramos si el usuario no tiene configurado dni y contraseña.
			if (!PreferencesService.isLoginConfigured()){
				AlertDialog.Builder builder = new AlertDialog.Builder(mainContent);
				builder.setMessage("Es necesario configur un dni y contraseña con la que conectarse. ¿Desea hacerlo ahora?")
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
				final ProgressDialog dialogLoading = ProgressDialog.show(mainContent, "", "Consultando", true);
				new Thread() {
					public void run() {
						try {
							final InfoReserva info = DeportesService.getInfoReserva(hora);
							mHandler.post(new Runnable() {
								public void run() {
									final Dialog dialog = new Dialog(mainContent);

									dialog.setContentView(R.layout.reserva_dialog);
									dialog.setTitle("Reserva de instalación");

									TextView text = (TextView) dialog.findViewById(R.id.textoReservaFecha);
									text.setText("Fecha: "+hora.getFecha()+" "+hora.getHora());
									text = (TextView) dialog.findViewById(R.id.textoReservaComplejo);
									text.setText(hora.getPista().getComplejo());
									text = (TextView) dialog.findViewById(R.id.textoReservaLugar);
									text.setText(hora.getPista().getNombre());
									text = (TextView) dialog.findViewById(R.id.textoReservaImporte);
									text.setText("Importe: "+info.getImporte());
									if (info.getSuple1()!=null && !"".equals(info.getSuple1().trim()) && !"0.00".equals(info.getSuple1().trim())){
										CheckBox checkbox = (CheckBox)dialog.findViewById(R.id.checkReservaLuz);
										checkbox.setText(" Luz: "+info.getSuple1());
										checkbox.setVisibility(View.VISIBLE);
									}

									Button botonReservaOk = (Button) dialog.findViewById(R.id.botonReservaOk);
									botonReservaOk.setOnClickListener(new ReservaClickListener(dialog, info, hora));

									Button botonReservaCancelar = (Button) dialog.findViewById(R.id.botonReservaCancelar);
									botonReservaCancelar.setOnClickListener(new OnClickListener() {
										@Override
										public void onClick(View v) {
											dialog.dismiss();
										}
									});
									dialog.show();
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
						dialogLoading.dismiss();
					}

				}.start();
			}		
		}
	}
	
	public class ReservaClickListener implements OnClickListener {

		private Dialog dialog;
		private InfoReserva reserva;
		private Hora hora;
		
		public ReservaClickListener(Dialog dialog, InfoReserva reserva, Hora hora) {
			this.dialog = dialog;
			this.reserva = reserva;
			this.hora = hora;
		}
		
		@Override
		public void onClick(View view) {
			dialog.dismiss();
			final ProgressDialog dialogReservando = ProgressDialog.show(mainContent, "", "Reservando", true);

			new Thread() {

				public void run() {
					CheckBox checkbox = (CheckBox)dialog.findViewById(R.id.checkReservaLuz);
					boolean luz = checkbox.isChecked();
					try {
						final String html = DeportesService.reservar(reserva, hora, luz);
						mHandler.post(new Runnable() {
							public void run() {
								//mainContent.finish();
								Intent intentReserva = new Intent(mainContent, ResumenReservaActivity.class);
								intentReserva.putExtra("reserva", html);
					        	startActivity(intentReserva);
					        	Toast.makeText(mainContent, "Reserva realizada correctamente. Acuerdate de imprimir la reserva desde la web", Toast.LENGTH_LONG).show();
					        	finish();
							}
						});
					} catch (DeportesServiceException e) {
						e.printStackTrace();
						final String errorMessage = e.getMessage();
						mHandler.post(new Runnable() {
							public void run() {
								Toast.makeText(mainContent, errorMessage, Toast.LENGTH_LONG).show();
							}
						});
					}
					dialogReservando.dismiss();
				}

			}.start();
		}
		
	}
	
	
}



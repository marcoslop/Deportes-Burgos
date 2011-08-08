package com.mlopez.beans;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class InfoReserva implements Serializable{

	private static final long serialVersionUID = 1323261982920358349L;
	
	private String importe;
	private String suple1;
	private String sessionId;
	private Hora hora;
	private String idReserva;
	
	public String getImporte() {
		return importe;
	}
	public void setImporte(String importe) {
		this.importe = importe;
	}
	public String getSuple1() {
		return suple1;
	}
	public void setSuple1(String suple1) {
		this.suple1 = suple1;
	}
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public Hora getHora() {
		return hora;
	}
	public void setHora(Hora hora) {
		this.hora = hora;
	}
	
	public String getIdReserva() {
		return idReserva;
	}
	public void setIdReserva(String idReserva) {
		this.idReserva = idReserva;
	}
	@Override
	public String toString() {
		String fecha = hora.getFecha();
		try {
			SimpleDateFormat sdfOrigen = new SimpleDateFormat("dd/MM/yyyy");
			SimpleDateFormat sdfDestino = new SimpleDateFormat("EEEEE dd/MM/yyyy", new Locale("es_ES"));
			Date date = sdfOrigen.parse(hora.getFecha());
			fecha = sdfDestino.format(date);
			fecha = fecha.substring(0,1).toUpperCase() + fecha.substring(1);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return hora.getPista().getComplejo()+"\n"+
		hora.getPista().getNombre()+"\n"+
		fecha+" "+hora.getHora()+
		"\nPrecio: "+importe;
	}
	
}
package com.mlopez.beans;

public class Hora {

	private Pista pista; 
	private String hora;
	private String disp;
	private String fecha;
	private String position;
	private String code;
	
	public Hora(Pista pista, String hora, String disp) {
		this.hora = hora.replaceAll("&nbsp;", "");
		this.disp = disp;
		this.pista = pista;
	}
	
	public String getHora() {
		return hora;
	}
	
	public void setHora(String hora) {
		this.hora = hora;
	}
	
	public String getDisp() {
		return disp;
	}
	
	public void setDisp(String disp) {
		this.disp = disp;
	}
	
	public boolean isDisponible (){
		return "verde".equals(disp);
	}
	
	public boolean isVisible () {
		return !"azul".equals(disp);
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public String getFecha() {
		return fecha;
	}

	public void setFecha(String fecha) {
		this.fecha = fecha;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Pista getPista() {
		return pista;
	}

	public void setPista(Pista pista) {
		this.pista = pista;
	}
	
	
}
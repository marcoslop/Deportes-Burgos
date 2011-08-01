package com.mlopez.beans;

public class Hora {

	private String hora;
	private String disp;
	
	public Hora(String hora, String disp) {
		this.hora = hora.replaceAll("&nbsp;", "");
		this.disp = disp;
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
	
}
package com.mlopez.beans;

import java.util.ArrayList;
import java.util.List;

public class Pista {

	private String nombre;
	private String complejo;
	List<Hora> horas;
	
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public String getComplejo() {
		return complejo;
	}
	public void setComplejo(String complejo) {
		this.complejo = complejo;
	}
	public List<Hora> getHoras() {
		if (horas == null){
			horas = new ArrayList<Hora>();
		}
		return horas;
	}
	public void setHoras(List<Hora> horas) {
		this.horas = horas;
	}
	
	public void addHora (Hora hora){
		 getHoras().add(hora);
	}
	
}

package com.mlopez.deportesburgos.beans;

import java.util.ArrayList;
import java.util.List;

public class Deporte {

	private String code;
	private String name;
	private List<Lugar> lugares;
	
	public Deporte(String code, String name) {
		this.code = code;
		this.name = name;
	}
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Lugar> getLugares() {
		if (lugares == null){
			lugares = new ArrayList<Lugar>();
		}
		return lugares;
	}
	public void setLugares(List<Lugar> lugares) {
		this.lugares = lugares;
	}

	public void addLugar (Lugar lugar){
		getLugares().add(lugar);
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
}

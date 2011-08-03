package com.mlopez.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.mlopez.beans.Deporte;
import com.mlopez.beans.Hora;
import com.mlopez.beans.InfoReserva;
import com.mlopez.beans.Lugar;
import com.mlopez.beans.Pista;

public class DeportesService {

	private static final String DEPORTES_HOST = "http://213.0.30.212:8080/deporteson/";

	private static final String LOGIN_SERVLET = "validarlogin.php";
	private static final String SEARCH_SERVLET = "generainstalaciones.php";
	private static final String INFO_HORA = "reservainstalacion.php";
	private static final String RESERVA_HORA = "altareser.php";

	private static final String EXTERNAL_SESSION_ID = "PHPSESSID";

	private static DefaultHttpClient client;
	
	private static DefaultHttpClient getHttpClient (){
		if (client == null){
			client = getNewHttpClient();
		}
		return client;
	}
	
	private static DefaultHttpClient getNewHttpClient (){
		client = new DefaultHttpClient();
		return client;
	}
	
	private static String login () throws DeportesServiceException{
		DefaultHttpClient client = getHttpClient ();
		HttpPost post = new HttpPost(DEPORTES_HOST+LOGIN_SERVLET);
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("codigo", PreferencesService.getDni()));
		params.add(new BasicNameValuePair("pass", PreferencesService.getPassword()));
		String response = null;
		try{
			UrlEncodedFormEntity ent = new UrlEncodedFormEntity (params, HTTP.UTF_8);
			post.setEntity(ent);
			HttpResponse responsePOST = client.execute(post);
			response = EntityUtils.toString(responsePOST.getEntity());
		}catch (Throwable t){
			throw new DeportesServiceException("Error al conectar con el servidor", t);
		}
		if (!response.contains("<respuesta>valido</respuesta>")){
			throw new DeportesServiceException("dni y contrase�a incorrectas");
		}
		List<Cookie> cookies = client.getCookieStore().getCookies();
		for (Cookie cookie : cookies) {
			String cookieName = cookie.getName();
			if (EXTERNAL_SESSION_ID.equals(cookieName)){
				String cookieValue = cookie.getValue();
				return cookieValue;
			}
		}
		throw new DeportesServiceException("No se ha recibido la cookie del servidor");
	}
	
	public static List<Pista> parseResponse (String result, String fecha, String deporteCode){
		List<Pista> pistas = new ArrayList<Pista>();

		//El separador va a ser el string <td class="tddatos"
		String delimitador = "<td class=\"tddatos\"";
		int index = 0;
		int i = 0;
		Pista pista = null;
		while (index>=0){
			index =	result.indexOf(delimitador);
			result = result.substring(index+delimitador.length());
			result = result.substring(result.indexOf('>')+1);
			int nextIndexOf = result.indexOf(delimitador);
			if (nextIndexOf<0){
				nextIndexOf = result.length();
			}
			String token = result.substring(0, nextIndexOf);

			if (i % 3 == 0){
				//Estamos en una nueva Pista
				pista = new Pista();
			}else if (i % 3 == 1){
				//Aqui nos encontramos con el nombre de la pista y el complejo
				//Formato : PISTA SQUASH N� 1<br><span class="letraazul2">COMP. R�O VENA</span>
				String nombrePista = token.substring(0, token.indexOf("<br>"));
				String nombreComplejo = token.substring(token.indexOf('>', token.indexOf("<span class="))+1,token.indexOf("</span"));
				nombreComplejo = nombreComplejo.replaceAll("PISCINA", "");
				nombreComplejo = nombreComplejo.replaceAll("PTVO.", "");
				nombreComplejo = nombreComplejo.replaceAll("COMP.", "");
				nombreComplejo = nombreComplejo.replaceAll("FUTBOL", "");
				nombreComplejo = nombreComplejo.replaceAll("FUTBOL", "");
				pista.setNombre(nombrePista.trim());
				pista.setComplejo(nombreComplejo.trim());
			}else{
				//Aqui vienen las horas, a partir del texto <td class="blanco">
				String startDelim = "<td class=\"blanco\">";
				token = token.substring(token.indexOf(startDelim)+startDelim.length());
				String horaDelim = "<td class=\"";
				int horaIndex = token.indexOf(horaDelim);
				while (horaIndex >= 0){
					token = token.substring(horaIndex+horaDelim.length());
					String disponibilidad = token.substring(0, token.indexOf('\"'));

					String id = null;
					//Para que el elemento tenga id, la disponibilidad debe ser diferente de azul.
					if (!"azul".equals(disponibilidad)){
						//Buscamos el id del elemento
						String idDelim = "id=\"";
						int idIndex = token.indexOf(idDelim);
						token = token.substring(idIndex+idDelim.length());
						id = token.substring(0, token.indexOf('\"'));
					}

					token = token.substring(token.indexOf('>')+1);
					int nextHoraIndexOf = token.indexOf("</td>");
					String horaString = token.substring(0, nextHoraIndexOf);
					Hora hora = new Hora(pista, horaString, disponibilidad);
					hora.setFecha(fecha);
					if (id!=null){
						//El formato del id es: SASQ020010. De lo que los 8 primeros digitos es el codigo, y los dos ultimos es la posicion.
						String idCode = id.substring(0,8);
						String position = id.substring(8);
						hora.setCode(idCode);
						hora.setPosition(position);
					}
					pista.addHora(hora);
					horaIndex = token.indexOf(horaDelim);
				}
				pistas.add(pista);
			}
			i++;
		}
		return pistas;
	}

	public static void searchActivities (String activity, String where, String day) throws DeportesServiceException{

		DefaultHttpClient client = getNewHttpClient ();
		HttpPost post = new HttpPost(DEPORTES_HOST+SEARCH_SERVLET);
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("act", activity));
		params.add(new BasicNameValuePair("comp", where));
		params.add(new BasicNameValuePair("dia", day));
		params.add(new BasicNameValuePair("tipo", "1"));
		if (PreferencesService.isLoginConfigured()){
			String cookieValue = login();
			//params.add(new BasicNameValuePair(EXTERNAL_SESSION_ID, cookieValue));
		}
		String response = null;
		try{
			UrlEncodedFormEntity ent = new UrlEncodedFormEntity (params, HTTP.UTF_8);
			post.setEntity(ent);
			HttpResponse responsePOST = client.execute(post);
			response = EntityUtils.toString(responsePOST.getEntity());
		}catch (Throwable t){
			//			response = "<div  id=\"textoDefinible\"></div><br />" +
			//					"<table class=\"tabledatos\" align=\"center\" cellpadding=\"3\" cellspacing=\"0\" width=\"790px\"><td class=\"tddatoscab\" align=\"center\" colspan=3 style=\"padding-bottom:0px\"><img src=\"imagenes/atras.gif\" align=\"middle\" style=\"padding-bottom:0px;padding-right:300px;width:30px;cursor:pointer;visibility:hidden;\" />&nbsp;&nbsp;29/07/2011&nbsp;&nbsp;<img src=\"imagenes/adelante.gif\" align=\"middle\" style=\"padding-bottom:0px;padding-left:300px;cursor:pointer\" onclick=\"buscar2('30/07/2011')\"/></td><br />" +
			//					"Undefined index:  loginadm in  on line <b>256</b><br />" +
			//					"<tr class=\"trdatosimpar\"><td class=\"tddatos\" style=\"text-align:left;width:50px;\">29/07/11</td><td class=\"tddatos\" style=\"text-align:left;width:150px;\">PISTA SQUASH N� 1<br><span class=\"letraazul2\">COMP. R�O VENA</span></td><td class=\"tddatos\"><table cellpadding=0 cellspacing=0 style=\"position:relative\"><tr ><td class=\"blanco\"></td><td class=\"azul\">&nbsp;08:00&nbsp;</td><td class=\"azul\">&nbsp;09:00&nbsp;</td><td class=\"azul\">&nbsp;10:00&nbsp;</td><td class=\"azul\">&nbsp;11:00&nbsp;</td><td class=\"azul\">&nbsp;12:00&nbsp;</td><td class=\"azul\">&nbsp;13:00&nbsp;</td><td class=\"rojo\" onmouseover=\"muestraUsuario(this.id);\" onmouseout=\"ocultaUsuario(this.id);\" id=\"RVSQ01007\" >&nbsp;14:00&nbsp;</td><td class=\"rojo\" onmouseover=\"muestraUsuario(this.id);\" onmouseout=\"ocultaUsuario(this.id);\" id=\"RVSQ01008\" >&nbsp;15:00&nbsp;</td><td class=\"rojo\" onmouseover=\"muestraUsuario(this.id);\" onmouseout=\"ocultaUsuario(this.id);\" id=\"RVSQ01009\" >&nbsp;16:00&nbsp;</td><td class=\"rojo\" onmouseover=\"muestraUsuario(this.id);\" onmouseout=\"ocultaUsuario(this.id);\" id=\"RVSQ010010\" >&nbsp;17:00&nbsp;</td><td class=\"rojo\" onmouseover=\"muestraUsuario(this.id);\" onmouseout=\"ocultaUsuario(this.id);\" id=\"RVSQ010011\" >&nbsp;18:00&nbsp;</td><td class=\"rojo\" onmouseover=\"muestraUsuario(this.id);\" onmouseout=\"ocultaUsuario(this.id);\" id=\"RVSQ010012\" >&nbsp;19:00&nbsp;</td><td class=\"rojo\" onmouseover=\"muestraUsuario(this.id);\" onmouseout=\"ocultaUsuario(this.id);\" id=\"RVSQ010013\" >&nbsp;20:00&nbsp;</td><td class=\"rojo\" onmouseover=\"muestraUsuario(this.id);\" onmouseout=\"ocultaUsuario(this.id);\" id=\"RVSQ010014\" >&nbsp;21:00&nbsp;</td><td class=\"rojo\" onmouseover=\"muestraUsuario(this.id);\" onmouseout=\"ocultaUsuario(this.id);\" id=\"RVSQ010015\" >&nbsp;22:00&nbsp;</td></tr><tr></tr></table><tr class=\"trdatospar\"><td class=\"tddatos\" style=\"text-align:left;width:50px;\">29/07/11</td><td class=\"tddatos\" style=\"text-align:left;width:150px;\">PISTA SQUASH N� 2<br><span class=\"letraazul2\">COMP. R�O VENA</span></td><td class=\"tddatos\"><table cellpadding=0 cellspacing=0 style=\"position:relative\"><tr ><td class=\"blanco\"></td><td class=\"azul\">&nbsp;08:00&nbsp;</td><td class=\"azul\">&nbsp;09:00&nbsp;</td><td class=\"azul\">&nbsp;10:00&nbsp;</td><td class=\"azul\">&nbsp;11:00&nbsp;</td><td class=\"azul\">&nbsp;12:00&nbsp;</td><td class=\"azul\">&nbsp;13:00&nbsp;</td><td class=\"rojo\" onmouseover=\"muestraUsuario(this.id);\" onmouseout=\"ocultaUsuario(this.id);\" id=\"RVSQ02007\" >&nbsp;14:00&nbsp;</td><td class=\"rojo\" onmouseover=\"muestraUsuario(this.id);\" onmouseout=\"ocultaUsuario(this.id);\" id=\"RVSQ02008\" >&nbsp;15:00&nbsp;</td><td class=\"rojo\" onmouseover=\"muestraUsuario(this.id);\" onmouseout=\"ocultaUsuario(this.id);\" id=\"RVSQ02009\" >&nbsp;16:00&nbsp;</td><td class=\"rojo\" onmouseover=\"muestraUsuario(this.id);\" onmouseout=\"ocultaUsuario(this.id);\" id=\"RVSQ020010\" >&nbsp;17:00&nbsp;</td><td class=\"rojo\" onmouseover=\"muestraUsuario(this.id);\" onmouseout=\"ocultaUsuario(this.id);\" id=\"RVSQ020011\" >&nbsp;18:00&nbsp;</td><td class=\"rojo\" onmouseover=\"muestraUsuario(this.id);\" onmouseout=\"ocultaUsuario(this.id);\" id=\"RVSQ020012\" >&nbsp;19:00&nbsp;</td><td class=\"rojo\" onmouseover=\"muestraUsuario(this.id);\" onmouseout=\"ocultaUsuario(this.id);\" id=\"RVSQ020013\" >&nbsp;20:00&nbsp;</td><td class=\"rojo\" onmouseover=\"muestraUsuario(this.id);\" onmouseout=\"ocultaUsuario(this.id);\" id=\"RVSQ020014\" >&nbsp;21:00&nbsp;</td><td class=\"rojo\" onmouseover=\"muestraUsuario(this.id);\" onmouseout=\"ocultaUsuario(this.id);\" id=\"RVSQ020015\" >&nbsp;22:00&nbsp;</td></tr><tr></tr></table><tr class=\"trdatosimpar\"><td class=\"tddatos\" style=\"text-align:left;width:50px;\">29/07/11</td><td class=\"tddatos\" style=\"text-align:left;width:150px;\">PISTA SQUASH N� 1<br><span class=\"letraazul2\">COMP. SAN AMARO</span></td><td class=\"tddatos\"><table cellpadding=0 cellspacing=0 style=\"position:relative\"><tr ><td class=\"blanco\"></td><td class=\"azul\">&nbsp;08:00&nbsp;</td><td class=\"azul\">&nbsp;09:00&nbsp;</td><td class=\"azul\">&nbsp;10:00&nbsp;</td><td class=\"azul\">&nbsp;11:00&nbsp;</td><td class=\"azul\">&nbsp;12:00&nbsp;</td><td class=\"azul\">&nbsp;13:00&nbsp;</td><td class=\"verde\" onmouseover=\"dentro('SASQ0100','7','29/07/2011',0)\" onmouseout=\"fuera('SASQ0100','7','29/07/2011')\" onclick=\"prereserva('SASQ0100','7','29/07/2011',0)\" id=\"SASQ01007\">&nbsp;14:00&nbsp;</td><td class=\"verde\" onmouseover=\"dentro('SASQ0100','8','29/07/2011',0)\" onmouseout=\"fuera('SASQ0100','8','29/07/2011')\" onclick=\"prereserva('SASQ0100','8','29/07/2011',0)\" id=\"SASQ01008\">&nbsp;15:00&nbsp;</td><td class=\"verde\" onmouseover=\"dentro('SASQ0100','9','29/07/2011',0)\" onmouseout=\"fuera('SASQ0100','9','29/07/2011')\" onclick=\"prereserva('SASQ0100','9','29/07/2011',0)\" id=\"SASQ01009\">&nbsp;16:00&nbsp;</td><td class=\"verde\" onmouseover=\"dentro('SASQ0100','10','29/07/2011',0)\" onmouseout=\"fuera('SASQ0100','10','29/07/2011')\" onclick=\"prereserva('SASQ0100','10','29/07/2011',0)\" id=\"SASQ010010\">&nbsp;17:00&nbsp;</td><td class=\"verde\" onmouseover=\"dentro('SASQ0100','11','29/07/2011',0)\" onmouseout=\"fuera('SASQ0100','11','29/07/2011')\" onclick=\"prereserva('SASQ0100','11','29/07/2011',0)\" id=\"SASQ010011\">&nbsp;18:00&nbsp;</td><td class=\"verde\" onmouseover=\"dentro('SASQ0100','12','29/07/2011',0)\" onmouseout=\"fuera('SASQ0100','12','29/07/2011')\" onclick=\"prereserva('SASQ0100','12','29/07/2011',0)\" id=\"SASQ010012\">&nbsp;19:00&nbsp;</td><td class=\"rojo\" onmouseover=\"muestraUsuario(this.id);\" onmouseout=\"ocultaUsuario(this.id);\" id=\"SASQ010013\" >&nbsp;20:00&nbsp;</td><td class=\"verde\" onmouseover=\"dentro('SASQ0100','14','29/07/2011',0)\" onmouseout=\"fuera('SASQ0100','14','29/07/2011')\" onclick=\"prereserva('SASQ0100','14','29/07/2011',0)\" id=\"SASQ010014\">&nbsp;21:00&nbsp;</td><td class=\"rojo\" onmouseover=\"muestraUsuario(this.id);\" onmouseout=\"ocultaUsuario(this.id);\" id=\"SASQ010015\" >&nbsp;22:00&nbsp;</td></tr><tr></tr></table><tr class=\"trdatospar\"><td class=\"tddatos\" style=\"text-align:left;width:50px;\">29/07/11</td><td class=\"tddatos\" style=\"text-align:left;width:150px;\">PISTA SQUASH N� 2<br><span class=\"letraazul2\">COMP. SAN AMARO</span></td><td class=\"tddatos\"><table cellpadding=0 cellspacing=0 style=\"position:relative\"><tr ><td class=\"blanco\"></td><td class=\"azul\">&nbsp;08:00&nbsp;</td><td class=\"azul\">&nbsp;09:00&nbsp;</td><td class=\"azul\">&nbsp;10:00&nbsp;</td><td class=\"azul\">&nbsp;11:00&nbsp;</td><td class=\"azul\">&nbsp;12:00&nbsp;</td><td class=\"azul\">&nbsp;13:00&nbsp;</td><td class=\"verde\" onmouseover=\"dentro('SASQ0200','7','29/07/2011',0)\" onmouseout=\"fuera('SASQ0200','7','29/07/2011')\" onclick=\"prereserva('SASQ0200','7','29/07/2011',0)\" id=\"SASQ02007\">&nbsp;14:00&nbsp;</td><td class=\"verde\" onmouseover=\"dentro('SASQ0200','8','29/07/2011',0)\" onmouseout=\"fuera('SASQ0200','8','29/07/2011')\" onclick=\"prereserva('SASQ0200','8','29/07/2011',0)\" id=\"SASQ02008\">&nbsp;15:00&nbsp;</td><td class=\"verde\" onmouseover=\"dentro('SASQ0200','9','29/07/2011',0)\" onmouseout=\"fuera('SASQ0200','9','29/07/2011')\" onclick=\"prereserva('SASQ0200','9','29/07/2011',0)\" id=\"SASQ02009\">&nbsp;16:00&nbsp;</td><td class=\"rojo\" onmouseover=\"muestraUsuario(this.id);\" onmouseout=\"ocultaUsuario(this.id);\" id=\"SASQ020010\" >&nbsp;17:00&nbsp;</td><td class=\"rojo\" onmouseover=\"muestraUsuario(this.id);\" onmouseout=\"ocultaUsuario(this.id);\" id=\"SASQ020011\" >&nbsp;18:00&nbsp;</td><td class=\"rojo\" onmouseover=\"muestraUsuario(this.id);\" onmouseout=\"ocultaUsuario(this.id);\" id=\"SASQ020012\" >&nbsp;19:00&nbsp;</td><td class=\"rojo\" onmouseover=\"muestraUsuario(this.id);\" onmouseout=\"ocultaUsuario(this.id);\" id=\"SASQ020013\" >&nbsp;20:00&nbsp;</td><td class=\"verde\" onmouseover=\"dentro('SASQ0200','14','29/07/2011',0)\" onmouseout=\"fuera('SASQ0200','14','29/07/2011')\" onclick=\"prereserva('SASQ0200','14','29/07/2011',0)\" id=\"SASQ020014\">&nbsp;21:00&nbsp;</td><td class=\"rojo\" onmouseover=\"muestraUsuario(this.id);\" onmouseout=\"ocultaUsuario(this.id);\" id=\"SASQ020015\" >&nbsp;22:00&nbsp;</td></tr><tr></tr></table></table><p></p>";
			throw new DeportesServiceException("Error al conectar con el servidor", t);
		}
		try{
			lastSearchResultsPistas = parseResponse(response, day, activity);
		}catch (Throwable t){
			throw new DeportesServiceException("Error al procesar la respuesta del servidor", t);
		}
		if (lastSearchResultsPistas.size() == 0 || lastSearchResultsPistas == null){
			throw new DeportesServiceException("La b�squeda no ha dado ning�n resultado");
		}
	}

	private static List<Pista> lastSearchResultsPistas = null;
	private static List<Deporte> deportes = null;
	private static List<Lugar> lugares = null;

	public static List<Deporte> getAllDeportes (){
		if (deportes == null){
			initDeportesYLugares();
		}
		return deportes;
	}

	public static List<Lugar> getAllLugares (){
		if (lugares == null){
			initDeportesYLugares();
		}
		return lugares;
	}

	public static List<Pista> getLastSearchResultsPistas() {
		return lastSearchResultsPistas;
	}

	public static List<String> getFechas (){
		List<String> fechas = new ArrayList<String>();
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		int numDays = 8;
		if (PreferencesService.isLoginConfigured()){
			numDays = 15;
		}
		for (int i=0;i<numDays;i++){
			fechas.add(sdf.format(calendar.getTime()));
			calendar.add(Calendar.DATE, 1);
		}
		return fechas;
	}

	public static String reservar (InfoReserva reserva, Hora hora, boolean luz) throws DeportesServiceException{
		//POST /deporteson/altareser.php HTTP/1.1
		//forma=2&importe=4.40&importe2=&inst=TAFR0100&suple1=0&suple2=0&suple3=0&suple4=0&suple5=0&nocache=0.8836063221096992
		//
		DefaultHttpClient client = getHttpClient ();
		HttpPost post = new HttpPost(DEPORTES_HOST+RESERVA_HORA);
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("forma", "2"));
		params.add(new BasicNameValuePair("importe", reserva.getImporte()));
		params.add(new BasicNameValuePair("importe2", ""));
		params.add(new BasicNameValuePair("inst", hora.getCode()));
		if (luz){
			params.add(new BasicNameValuePair("suple1", reserva.getSuple1()));
		}else{
			params.add(new BasicNameValuePair("suple1", "0"));
		}
		params.add(new BasicNameValuePair("suple2", "0"));
		params.add(new BasicNameValuePair("suple3", "0"));
		params.add(new BasicNameValuePair("suple4", "0"));
		params.add(new BasicNameValuePair("suple5", "0"));
		
		//params.add(new BasicNameValuePair(EXTERNAL_SESSION_ID, reserva.getSessionId()));
		try{
			UrlEncodedFormEntity ent = new UrlEncodedFormEntity (params, HTTP.UTF_8);
			post.setEntity(ent);
			HttpResponse responsePOST = client.execute(post);
			String response = EntityUtils.toString(responsePOST.getEntity());
			return response;
		}catch (Throwable t){
			throw new DeportesServiceException("Error obteniendo detalle de la actividad",t);
		}
	}
	
	public static InfoReserva getInfoReserva (Hora hora) throws DeportesServiceException{
		InfoReserva reserva = new InfoReserva();
		DefaultHttpClient client = getHttpClient ();
		HttpPost post = new HttpPost(DEPORTES_HOST+INFO_HORA);
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("inst", hora.getCode()));
		params.add(new BasicNameValuePair("posicion", hora.getPosition()));
		params.add(new BasicNameValuePair("fecha", hora.getFecha()));
		params.add(new BasicNameValuePair("tam", "0"));
		params.add(new BasicNameValuePair("numaut", ""));
		params.add(new BasicNameValuePair("apli", ""));
		params.add(new BasicNameValuePair("tipinst", ""));
		String cookieValue = login();
		//params.add(new BasicNameValuePair(EXTERNAL_SESSION_ID, cookieValue));
		
		String response = null;
		try{
			UrlEncodedFormEntity ent = new UrlEncodedFormEntity (params, HTTP.UTF_8);
			post.setEntity(ent);
			HttpResponse responsePOST = client.execute(post);
			response = EntityUtils.toString(responsePOST.getEntity());
		}catch (Throwable t){
			throw new DeportesServiceException("Error obteniendo detalle de la actividad",t);
		}
		try{
			String importeDelim = "<importe>";
			String importeDelimEnd = "</importe>";
			String importe = response.substring(response.indexOf(importeDelim)+importeDelim.length(), response.indexOf(importeDelimEnd));
			String importeSuple1 = "<suple1>";
			String importeSuple1End = "</suple1>";
			String suple1 = response.substring(response.indexOf(importeSuple1)+importeSuple1.length(), response.indexOf(importeSuple1End));
			reserva.setImporte(importe);
			reserva.setSuple1(suple1);
		}catch (Throwable t){
			throw new DeportesServiceException("Error parseando respuesta del servidor en el detall de la actividad",t);
		}
		//POST /deporteson/reservainstalacion.php HTTP/1.1
		//inst=SASQ0100&posicion=8&fecha=02%2F08%2F2011&tam=0&numaut=&apli=&tipinst=&nocache=0.08210459491237998

		//<respuesta><importe>3.80</importe><suple1>2.10</suple1><suple2>0.00</suple2><suple3>0.00</suple3><suple4>0.00</suple4><suple5>0.00</suple5><tope>0</tope><tipv>1</tipv><bon><b><bono>0</bono><bs1>0</bs1><bs2>0</bs2><bs3>0</bs3><bs4>0</bs4><bs5>0</bs5></b></bon></respuesta>
		reserva.setSessionId(cookieValue);
		return reserva;
	}

	private static void initDeportesYLugares (){
		lugares = new ArrayList<Lugar>();
		Lugar todos = new Lugar("todos", "Todos");
		lugares.add(todos);
		Lugar plantio = new Lugar(" CP", "COMP. EL PLANT�O");
		lugares.add(plantio);
		Lugar esther = new Lugar(" SM", "COMP. ESTHER SAN MIGUEL");
		lugares.add(esther);
		Lugar talamillo = new Lugar(" TA", "COMP. TALAMILLO");
		lugares.add(talamillo);
		Lugar riovena = new Lugar(" RV", "COMP. RIO VENA");
		lugares.add(riovena);
		Lugar spsf = new Lugar(" SP", "COMP. S.P.S.F");
		lugares.add(spsf);		
		Lugar amaro = new Lugar(" SA", "COMP. SAN AMARO");
		lugares.add(amaro);
		Lugar futbolPlantio = new Lugar(" FP", "FUTBOL EL PLANT�O");
		lugares.add(futbolPlantio);
		Lugar futbolSedano = new Lugar(" FS", "FUTBOL SEDANO");
		lugares.add(futbolSedano);
		Lugar piscinaCapiscol = new Lugar(" CA", "PISCINA CAPISCOL");
		lugares.add(piscinaCapiscol);
		Lugar piscinaSanAgustin = new Lugar(" AG", "PISCINA SAN AGUSTIN");
		lugares.add(piscinaSanAgustin);
		Lugar carlosSerna = new Lugar(" MS", "PTVO. CARLOS SERNA");
		lugares.add(carlosSerna);
		Lugar ptvoPlantio = new Lugar(" PP", "PTVO. EL PLANT�O");
		lugares.add(ptvoPlantio);
		Lugar ptvoJavierGomez = new Lugar(" AM", "PTVO. JAVIER G�MEZ");
		lugares.add(ptvoJavierGomez);
		Lugar ptvoLavaderos = new Lugar(" LV", "PTVO. LAVADEROS");
		lugares.add(ptvoLavaderos);
		Lugar ptvoMarianoGaspar = new Lugar(" MG", "PTVO. MARIANO GASPAR");
		lugares.add(ptvoMarianoGaspar);
		Lugar ptvoPisones = new Lugar(" PI", "PTVO. PISONES");
		lugares.add(ptvoPisones);

		deportes = new ArrayList<Deporte>();

		Deporte deportesTodos = new Deporte("todos", "Todos");
		deportesTodos.setLugares(lugares);
		deportes.add(deportesTodos);
		
		Deporte boxeo = new Deporte(" BX", "Boxeo");
		boxeo.addLugar(todos);
		boxeo.addLugar(ptvoPlantio);
		deportes.add(boxeo);

		Deporte escalada = new Deporte(" ES", "Escalada");
		escalada.addLugar(todos);
		escalada.addLugar(riovena);
		deportes.add(escalada);

		Deporte esgrima = new Deporte(" SG", "Esgrima");
		esgrima.addLugar(todos);
		esgrima.addLugar(ptvoPlantio);
		deportes.add(esgrima);

		Deporte fronton = new Deporte(" FR", "Front�n");
		fronton.addLugar(todos);
		fronton.addLugar(talamillo);
		fronton.addLugar(spsf);
		fronton.addLugar(ptvoLavaderos);
		deportes.add(fronton);

		Deporte futbol = new Deporte(" FT", "Futbol");
		futbol.addLugar(todos);
		futbol.addLugar(esther);
		futbol.addLugar(talamillo);
		futbol.addLugar(futbolPlantio);
		futbol.addLugar(futbolSedano);
		deportes.add(futbol);

		Deporte gimnasia = new Deporte(" GN", "Gimnasia");
		gimnasia.addLugar(todos);
		gimnasia.addLugar(spsf);
		deportes.add(gimnasia);

		Deporte halterofilia = new Deporte(" HT", "Halterofilia");
		halterofilia.addLugar(todos);
		halterofilia.addLugar(ptvoPlantio);
		deportes.add(halterofilia);

		Deporte natacion = new Deporte(" NT", "Natacion");
		natacion.addLugar(todos);
		natacion.addLugar(plantio);
		natacion.addLugar(amaro);
		natacion.addLugar(piscinaCapiscol);
		natacion.addLugar(piscinaSanAgustin);
		deportes.add(natacion);

		Deporte padel = new Deporte(" PD", "Padel");
		padel.addLugar(todos);
		padel.addLugar(amaro);
		deportes.add(padel);

		Deporte polideportiva = new Deporte(" PO", "Polideportiva");
		polideportiva.addLugar(todos);
		polideportiva.addLugar(plantio);
		polideportiva.addLugar(esther);
		polideportiva.addLugar(talamillo);
		polideportiva.addLugar(riovena);
		polideportiva.addLugar(amaro);
		polideportiva.addLugar(carlosSerna);
		polideportiva.addLugar(ptvoPlantio);
		polideportiva.addLugar(ptvoJavierGomez);
		polideportiva.addLugar(ptvoLavaderos);
		polideportiva.addLugar(ptvoMarianoGaspar);
		polideportiva.addLugar(ptvoPisones);
		deportes.add(polideportiva);

		Deporte polideportiva13 = new Deporte(" P3", "Polideportiva 1/3");
		polideportiva13.addLugar(todos);
		polideportiva13.addLugar(esther);
		polideportiva13.addLugar(talamillo);
		polideportiva13.addLugar(riovena);
		polideportiva13.addLugar(ptvoJavierGomez);
		polideportiva13.addLugar(ptvoMarianoGaspar);
		polideportiva13.addLugar(ptvoPisones);
		deportes.add(polideportiva13);

		Deporte polivalente = new Deporte(" PL", "Polivalente");
		polivalente.addLugar(todos);
		polivalente.addLugar(ptvoPisones);
		deportes.add(polivalente);

		Deporte rugby = new Deporte(" RG", "Rugby");
		rugby.addLugar(todos);
		rugby.addLugar(amaro);
		deportes.add(rugby);

		Deporte squash = new Deporte(" SQ", "Squash");
		squash.addLugar(todos);
		squash.addLugar(riovena);
		squash.addLugar(amaro);
		deportes.add(squash);

		Deporte tenis = new Deporte(" TN", "Tenis");
		tenis.addLugar(todos);
		tenis.addLugar(plantio);
		tenis.addLugar(riovena);
		tenis.addLugar(amaro);
		deportes.add(tenis);

		Deporte tenisMesa = new Deporte(" TM", "Tenis de mesa");
		tenisMesa.addLugar(todos);
		tenisMesa.addLugar(plantio);
		deportes.add(tenisMesa);
	}

}

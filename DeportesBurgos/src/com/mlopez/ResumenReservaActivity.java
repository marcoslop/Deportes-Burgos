package com.mlopez;

import android.os.Bundle;
import android.webkit.WebView;
import android.widget.Toast;

public class ResumenReservaActivity extends AbstractActivity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		WebView webview = new WebView(this);
		setContentView(webview);

		String reserva = "<html><body>"+getIntent().getStringExtra("reserva")+"</body></html>";
		reserva = quitarSeccionImprimir(reserva);
		webViewLoadData(webview, reserva);
		Toast.makeText(this, "Reserva realizada correctamente. Acuerdate de imprimir la reserva desde la web", Toast.LENGTH_LONG).show();
	}
	
	private void webViewLoadData(WebView web, String html) {
        StringBuilder buf = new StringBuilder(html.length());
        for (char c : html.toCharArray()) {
            switch (c) {
              case '#':  buf.append("%23"); break;
              case '%':  buf.append("%25"); break;
              case '\'': buf.append("%27"); break;
              case '?':  buf.append("%3f"); break;
              case 'á':  buf.append("&aacute;"); break;
              case 'é':  buf.append("&eacute;"); break;
              case 'í':  buf.append("&iacute;"); break;
              case 'ó':  buf.append("&oacute;"); break;
              case 'ú':  buf.append("&uacute;"); break;
              case 'Á':  buf.append("&Aacute;"); break;
              case 'É':  buf.append("&Eacute;"); break;
              case 'Í':  buf.append("&Iacute;"); break;
              case 'Ó':  buf.append("&Oacute;"); break;
              case 'Ú':  buf.append("&Uacute;"); break;
              case 'ñ':  buf.append("&ntilde;"); break;
              case 'Ñ':  buf.append("&Ntilde;"); break;
              case 'º':  buf.append(""); break;
              default:
                buf.append(c);
                break;
            }
        }
        web.loadData(buf.toString(), "text/html", "utf-8");
    }
	
	private static String quitarSeccionImprimir (String reserva){
		String section = "<div class=\"oculto\"><center><input type=\"button\" value=\"Imprimir\" onClick=\"imprimir('imprimir')\"></center><br><p align=\"center\"><a href=\"alqInst.php?alq=1\" >Volver</a></p></div>";
		int index = reserva.indexOf(section);
		if (index >= 0){
			String nuevoTexto = reserva.substring(0, index) + reserva.substring(index + section.length());
			return nuevoTexto;			
		}
		return section;
	}
	
	public static void main(String[] args) {
		System.out.println(quitarSeccionImprimir("hola<div class=\"oculto\"><center><input type=\"button\" value=\"Imprimir\" onClick=\"imprimir('imprimir')\"></center><br><p align=\"center\"><a href=\"alqInst.php?alq=1\" >Volver</a></p></div>hola"));
	}
	
	
	/*
	 * 
	 * <center><div class="oculto"><b>Reserva Finalizada</b><br><br><br></div><div id="dos" style="text-align:center;margin-left:auto;margin-right:auto;" ><div id="imprimir"><table  class="ticket" cellspacing=0 style="margin-left:auto;margin-right:auto;border:1px solid black;">
<tr><td  rowspan=4  style="border-bottom:1px solid black;border-right:1px solid black;"  align="center"><img src="resizer.php?imgfile=imagenes/escudoburgos.jpg&max_width=148&max_height=75"></td></tr><tr>
	<td align="center"  class="cabticket" colspan=5 style="font-weight:bold;width:80%;">
Datos de la reserva</td>
</tr>
<tr>
	<td colspan=1 style="border-top:1px solid black;border-bottom:1px solid black;border-right:1px solid black;width:16%;font-weight:bold;margin-left:5px;" >Fecha</td>
	<td colspan=1 style="border-top:1px solid black;border-bottom:1px solid black;border-right:1px solid black;width:16%;font-weight:bold;margin-left:5px;" >Hora</td>
	<td colspan=3 style="border-top:1px solid black;border-bottom:1px solid black;width:48%;font-weight:bold;margin-left:5px;">Referencia</td>
</tr>
<tr>
	<td  colspan=1 style="width:16%;border-bottom:1px solid black;border-right:1px solid black;">03/08/11</td>
	<td  colspan=1 style="border-bottom:1px solid black;border-right:1px solid black;">18:11:21</td>
	<td colspan=3 style="border-bottom:1px solid black;">FZCC8X</td>
</tr>
<tr><td colspan=6>&nbsp;</td></tr>
<tr><td colspan=3 class="subcabticket" style="width:48%;border-bottom:1px solid black;border-right:1px solid black;font-weight:bold;border-top:1px solid black;">Datos del Usuario</td><td colspan=3 class="subcabticket"  style="border-bottom:1px solid black;font-weight:bold;border-top:1px solid black;width:48%">Datos de la Reserva</td></tr>
	<tr><td  style="width:16%;font-weight:bold;text-align:left;padding-left:5px;">Nombre:</td><td  style="border-right:1px solid black;text-align:left;width:32%" colspan=2>MARCOS LOPEZ MIGUEL</td>
		<td  style="font-weight:bold;text-align:left;padding-left:5px;width:16%;">Instalación:</td><td  style="text-align:left;width:32%;" colspan=2>COMP. JOSÉ LUIS TALAMILLO<br>COMP. JOSÉ LUIS TALAMILLO FRONTÓN Nº 1</td></tr>
	<tr><td  style="font-weight:bold;text-align:left;padding-left:5px;">Abonado Nº:</td><td  style="border-right:1px solid black;text-align:left;" colspan=2>P21752</td>
		<td  style="font-weight:bold;text-align:left;padding-left:5px;">Fecha Reserva:</td><td  style="text-align:left;" colspan=2>13/08/2011</td></tr>
	<tr><td  style="font-weight:bold;text-align:left;padding-left:5px;">Suplementos:</td><td  style="border-right:1px solid black;text-align:left;" colspan=2>No</td>
		<td  style="font-weight:bold;text-align:left;padding-left:5px;">Hora Reserva</td><td  style="text-align:left;" colspan=2>14:00-15:00</td></tr>	
	<tr><td  colspan=6 style="border-top:1px solid black;font-weight:bold;text-align:center;padding-left:5px;"><u>Importe Cargado: </u> 4.40 euros <u>Forma de Pago</u>:  Domiciliación Bancaria</td></tr></table>
 
</td></tr></table><span style="text-align:center">Solo se podrá hacer uso de esta reserva si el abonado titular de la misma es uno de los usuarios, debiendo acreditar su identidad con el carnet de abonado y este ticket</span></div></div><center><div class="oculto"><center><input type="button" value="Imprimir" onClick="imprimir('imprimir')"></center><br><p align="center"><a href="alqInst.php?alq=1" >Volver</a></p></div> </body>		</div>




	 */

}

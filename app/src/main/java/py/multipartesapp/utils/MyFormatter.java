package py.multipartes2.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by Adolfo on 01/06/2015.
 */
public class MyFormatter {

    static DecimalFormat formateador = new DecimalFormat("###,###.##");

    public static String formatDistance(Double value){
        int distance = value.intValue();
        if(distance >= 1000){
            int k = distance / 1000;
            int m = distance - (k*1000);
            m = m / 100;
            return String.valueOf(k) + (m>0?("."+String.valueOf(m)):"") + " Km" +(k>1?"s":"");
        } else {
            return String.valueOf(distance) + " metro"+(distance==1?"":"s");
        }
    }

    public static String formatMoney (String value){
        String formatted = "";
        String valorLimpio = cleanMoney(value);
        if (!valorLimpio.isEmpty()){
            int  i = Integer.valueOf(valorLimpio);
            formatted = formateador.format(i).toString();
        }
        return formatted;
    }

    public static String formatearMoneda (String valor){
        String formatted = "";
        String valorLimpio = cleanMoney(valor);
        if (!valorLimpio.isEmpty()){
            int  i = Integer.valueOf(valorLimpio);
            //formatted = formateador.format(i).toString();

            NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.GERMANY);
            DecimalFormat decimalFormat = (DecimalFormat)numberFormat;
            decimalFormat.applyPattern("###,###.###");
            formatted = decimalFormat.format(i);
        }
        return formatted;
    }

    public static String cleanMoney (String value){
        String valorLimpio = value.replaceAll("[.]","");
        valorLimpio = valorLimpio.replaceAll("[,]","");
        return  valorLimpio;
    }
}

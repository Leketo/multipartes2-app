package py.multipartesapp.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    public static String convertDateFromServer(String date ) throws ParseException {
        SimpleDateFormat spf=new SimpleDateFormat("yyyy-MM-dd");
        Date newDate=spf.parse(date);
        spf= new SimpleDateFormat("dd/MM/yyyy hh:mm");
        date = spf.format(newDate);
        System.out.println(date);
        return date;
    }
}

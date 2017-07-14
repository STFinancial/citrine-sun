package test;

import java.text.DateFormat;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Created by Timothy on 1/3/17.
 */
public class datetest {
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSX";

    public static void main(String args[]) {
        //           0 2 4 6 8 0 2 4 6 8 0 2
        String ts = "2017-03-17T23:46:16.663397Z";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT).withZone(ZoneId.of("UTC"));
//        DateFormat format = new SimpleDateFormat(DATE_FORMAT);
        LocalDateTime d = LocalDateTime.from(formatter.parse(ts));
        System.out.println(d.atZone(ZoneOffset.UTC).toEpochSecond());

//        System.out.println(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.FULL).fo(new Date()));
//        try {
////            Date d = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.DEFAULT).parse("2014-02-10 01:19:37");
//            Date d = DateFormat.getDateTimeInstance().parse("2014-02-10");
//            System.out.println(d.getTime());
//        } catch (ParseException e) {
//            e.printStackTrace();
//            System.out.println(e.getMessage());
//        }

    }

}

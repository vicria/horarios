package ar.vicria.horario.services.util;

import lombok.experimental.UtilityClass;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

@UtilityClass
public class DateUtil {

    public static DateTime startOfThisWeek() {
        DateTime now = new DateTime(System.currentTimeMillis());
        return now.withDayOfWeek(DateTimeConstants.MONDAY).withTimeAtStartOfDay();
    }

    public static DateTime startOfNextWeek() {
        DateTime now = new DateTime(System.currentTimeMillis()).plusDays(7);
        return now.withDayOfWeek(DateTimeConstants.MONDAY).withTimeAtStartOfDay();
    }

    public static DateTime endOfThisWeek() {
        DateTime now = new DateTime(System.currentTimeMillis());
        return now.withDayOfWeek(DateTimeConstants.SUNDAY).withTime(23, 59, 59, 999);
    }

    public static DateTime endOfNextWeek() {
        DateTime now = new DateTime(System.currentTimeMillis()).plusDays(7);
        return now.withDayOfWeek(DateTimeConstants.SUNDAY).withTime(23, 59, 59, 999);
    }
}

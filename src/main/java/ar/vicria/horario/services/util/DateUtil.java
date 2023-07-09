package ar.vicria.horario.services.util;

import lombok.experimental.UtilityClass;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

@UtilityClass
public class DateUtil {

    private static DateTime now() {
        return new DateTime(System.currentTimeMillis());
    }

    public static DateTime startOfThisWeek() {
        var now = now();
        return now.withDayOfWeek(DateTimeConstants.MONDAY).withTimeAtStartOfDay();
    }

    public static DateTime startOfNextWeek() {
        var now = now().plusDays(7);
        return now.withDayOfWeek(DateTimeConstants.MONDAY).withTimeAtStartOfDay();
    }

    public static DateTime endOfThisWeek() {
        var now = now();
        return now.withDayOfWeek(DateTimeConstants.SUNDAY).withTime(23, 59, 59, 999);
    }

    public static DateTime endOfNextWeek() {
        var now = now().plusDays(7);
        return now.withDayOfWeek(DateTimeConstants.SUNDAY).withTime(23, 59, 59, 999);
    }
}

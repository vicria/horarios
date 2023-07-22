package ar.vicria.horario.services.util;

import lombok.experimental.UtilityClass;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

@UtilityClass
public class DateUtil {

    public static DateTime now() {
        return new DateTime(System.currentTimeMillis());
    }

    public static DateTime startOfWeek(int days) {
        var now = now().plusDays(days);
        return now.withDayOfWeek(DateTimeConstants.MONDAY).withTimeAtStartOfDay();
    }

    public static DateTime endOfWeek(int days) {
        var now = now().plusDays(days);
        return now.withDayOfWeek(DateTimeConstants.SUNDAY).withTime(23, 59, 59, 999);
    }

    public static String week(int days) {
        DateTime start = startOfWeek(days);
        DateTime end = endOfWeek(days);
        return textWeek(start,end);
    }

    private static String textWeek(DateTime start, DateTime end) {
        int dayOfMonthStart = start.getDayOfMonth();
        int monthOfYearStart = start.getMonthOfYear();
        int dayOfMonthEnd = end.getDayOfMonth();
        int monthOfYearEnd = end.getMonthOfYear();
        return String.format("(%s.%s-%s.%s)",
                dayOfMonthStart >= 9 ? dayOfMonthStart : "0" + dayOfMonthStart,
                monthOfYearStart >= 9 ? monthOfYearStart : "0" + monthOfYearStart,
                dayOfMonthEnd >= 9 ? dayOfMonthEnd : "0" + dayOfMonthEnd,
                monthOfYearEnd >= 9 ? monthOfYearEnd : "0" + monthOfYearEnd);
    }
}

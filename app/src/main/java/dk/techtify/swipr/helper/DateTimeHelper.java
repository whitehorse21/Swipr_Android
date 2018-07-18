package dk.techtify.swipr.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.format.DateFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import dk.techtify.swipr.R;

/**
 * Created by Pavel on 1/18/2017.
 */
public class DateTimeHelper {

    public static String getFormattedDate(long time, String format) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time);

        return getFormattedDate(c, format);
    }

    public static String getFormattedDate(Calendar calendar, String format) {
        return DateFormat.format(format, calendar).toString();
    }

    public static Calendar parseDateString(String date, String format) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.ENGLISH);
        try {
            cal.setTime(sdf.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return cal;
    }

    public static final String showMessageTime(Context context, boolean showAtForToday, long created) {
        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(created);

        Calendar now = Calendar.getInstance();

        final String timeFormatString = "kk:mm";
        final String dateTimeFormatString = "d MMM, kk:mm";
        if (now.get(Calendar.DATE) == time.get(Calendar.DATE)) {
            return (showAtForToday ? context.getResources().getString(R.string.at) + " " : "") +
                    DateFormat.format(timeFormatString, time);
        } else if (now.get(Calendar.DATE) - time.get(Calendar.DATE) == 1) {
            return (showAtForToday ? context.getResources().getString(R.string.yesterday_at)
                    .toLowerCase() : context.getResources().getString(R.string.yesterday_at)) +
                    " " + DateFormat.format(timeFormatString, time);
        } else if (now.get(Calendar.YEAR) == time.get(Calendar.YEAR)) {
            String dt = DateFormat.format(dateTimeFormatString, time).toString();
            return showAtForToday ? dt.replace(",", " " + context.getResources().getString(R.string.at)) : dt;
        } else {
            String dt = DateFormat.format("d MMM yyyy, kk:mm", time).toString();
            return showAtForToday ? dt.replace(",", " " + context.getResources().getString(R.string.at)) : dt;
        }
    }

    @SuppressLint("DefaultLocale")
    public static String getFormattedCounter(long time) {
        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(time),
                TimeUnit.MILLISECONDS.toMinutes(time) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(time)),
                TimeUnit.MILLISECONDS.toSeconds(time) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time)));
    }
}

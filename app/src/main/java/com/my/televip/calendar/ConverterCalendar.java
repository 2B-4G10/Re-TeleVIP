package com.my.televip.calendar;

import com.my.televip.Configs.ConfigManager;
import com.my.televip.language.Keys;
import com.my.televip.language.Translator;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.chrono.HijrahDate;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import ir.huri.jcal.JalaliCalendar;

public class ConverterCalendar {

    public static HijrahDate toHijri(long date) {
        ZonedDateTime zdt = Instant
                .ofEpochMilli(date)
                .atZone(ZoneId.systemDefault());

        return HijrahDate.from(zdt);
    }


    public static HijrahDate toHijri(Calendar calendar) {
        ZonedDateTime zdt = Instant
                .ofEpochMilli(calendar.getTimeInMillis())
                .atZone(ZoneId.systemDefault());

        return HijrahDate.from(zdt);
    }

    public static HijrahDate toHijri(Date date) {
        ZonedDateTime zdt = Instant
                .ofEpochMilli(date.getTime())
                .atZone(ZoneId.systemDefault());

        return HijrahDate.from(zdt);
    }

    public static JalaliCalendar toJalali(long date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);

        return new JalaliCalendar(calendar.getTime());
    }


    public static JalaliCalendar toJalali(Calendar calendar) {
        return new JalaliCalendar(calendar.getTime());
    }

    public static JalaliCalendar toJalali(Date date) {
        return new JalaliCalendar(date);
    }

    public static CalendarDate toCalendar(long date) {
        if (ConfigManager.customCalendar == null){
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(date);
            return new CalendarDate(calendar);
        }
        if (ConfigManager.customCalendar.getCustomCalendar() == 1) {
            return new CalendarDate(toHijri(date));
        } else if (ConfigManager.customCalendar.getCustomCalendar() == 2) {
            return new CalendarDate(toJalali(date));
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        return new CalendarDate(calendar);
    }

    public static CalendarDate toCalendar(Date date) {
        if (ConfigManager.customCalendar.getCustomCalendar() == 1) {
            return new CalendarDate(toHijri(date));
        } else if (ConfigManager.customCalendar.getCustomCalendar() == 2) {
            return new CalendarDate(toJalali(date));
        }
        return null;
    }

    public static CalendarDate toCalendar(Calendar calendar) {
        if (ConfigManager.customCalendar == null){
            return new CalendarDate(calendar);
        }
        if (ConfigManager.customCalendar.getCustomCalendar() == 1) {
            return new CalendarDate(toHijri(calendar));
        } else if (ConfigManager.customCalendar.getCustomCalendar() == 2) {
            return new CalendarDate(toJalali(calendar));
        }
        return new CalendarDate(calendar);
    }

    public static String formatDate(long date) {
        try {
            CalendarDate today = ConverterCalendar.toCalendar(System.currentTimeMillis());
            CalendarDate yesterday = ConverterCalendar.toCalendar(System.currentTimeMillis() - 86400000L);

            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            String dateHour = sdf.format(new Date(date));

            CalendarDate edited = ConverterCalendar.toCalendar(date);

            int dateDay = edited.getDay();
            int dateYear = edited.getYear();
            int dateMonth = edited.getMonth();
            String dateMonthName = edited.getMonthName();

            if (sameDay(edited, today)) {
                return Translator.get(Keys.TodayAt) + dateHour;
            } else if (sameDay(edited, yesterday)) {
                return Translator.get(Keys.YesterdayAt) + dateHour;
            } else if (Math.abs(System.currentTimeMillis() - date) < 31536000000L) {
                String dayMonth = Translator.isDayFirst() ? dateDay + " " + dateMonthName : dateMonthName + " " + dateDay;
                return Translator.get(Keys.Edited) + dayMonth + " " + dateHour;
            } else {
                return Translator.get(Keys.Edited) + dateYear + "/" + dateMonth + "/" + dateDay + " " + dateHour;
            }
        } catch (Exception e) {}
        return null;
    }

    private static boolean sameDay(CalendarDate a, CalendarDate b) {
        return a.getYear() == b.getYear() && a.getMonth() == b.getMonth() && a.getDay() == b.getDay();
    }

}

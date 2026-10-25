package com.my.televip.calendar;

import com.my.televip.language.Keys;
import com.my.televip.language.Translator;

import java.time.chrono.HijrahDate;
import java.time.temporal.ChronoField;
import java.util.Calendar;

import ir.huri.jcal.JalaliCalendar;

public class CalendarDate {

    private static final String[] HIJRI_MONTHS = {
            Keys.HijriMonthMuharram, Keys.HijriMonthSafar, Keys.HijriMonthRabiAlAwal,
            Keys.HijriMonthRabiAlThani, Keys.HijriMonthJumadaAlAwal, Keys.HijriMonthJumadaAlThani,
            Keys.HijriMonthRajab, Keys.HijriMonthShaban, Keys.HijriMonthRamadan,
            Keys.HijriMonthShawwal, Keys.HijriMonthDhulQidah, Keys.HijriMonthDhulHijjah
    };

    private static final String[] PERSIAN_MONTHS = {
            Keys.PersianMonthFarvardin, Keys.PersianMonthOrdibehesht, Keys.PersianMonthKhordad,
            Keys.PersianMonthTir, Keys.PersianMonthMordad, Keys.PersianMonthShahrivar,
            Keys.PersianMonthMehr, Keys.PersianMonthAban, Keys.PersianMonthAzar,
            Keys.PersianMonthDey, Keys.PersianMonthBahman, Keys.PersianMonthEsfand
    };

    private static final String[] GREGORIAN_MONTHS = {
            Keys.GregorianMonthJanuary, Keys.GregorianMonthFebruary, Keys.GregorianMonthMarch,
            Keys.GregorianMonthApril, Keys.GregorianMonthMay, Keys.GregorianMonthJune,
            Keys.GregorianMonthJuly, Keys.GregorianMonthAugust, Keys.GregorianMonthSeptember,
            Keys.GregorianMonthOctober, Keys.GregorianMonthNovember, Keys.GregorianMonthDecember
    };

    private final int year;
    /** 1 to 12. */
    private final int month;
    private final String monthKey;
    private final int day;

    public CalendarDate(HijrahDate hijrahDate) {
        day = hijrahDate.get(ChronoField.DAY_OF_MONTH);
        month = hijrahDate.get(ChronoField.MONTH_OF_YEAR);
        year = hijrahDate.get(ChronoField.YEAR);
        monthKey = HIJRI_MONTHS[month - 1];
    }

    public CalendarDate(JalaliCalendar jalaliCalendar) {
        year = jalaliCalendar.getYear();
        month = jalaliCalendar.getMonth();
        day = jalaliCalendar.getDay();
        monthKey = PERSIAN_MONTHS[month - 1];
    }

    public CalendarDate(Calendar calendar) {
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1;   // Calendar counts months from 0
        day = calendar.get(Calendar.DAY_OF_MONTH);
        monthKey = GREGORIAN_MONTHS[month - 1];
    }

    public int getDay() {
        return day;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    /** The month's name as it goes with a day ("12 March"). */
    public String getMonthName() {
        return Translator.get(monthKey);
    }

    /**
     * The month's name on its own or with just the year ("March 2025"). Languages that say it
     * differently there (Russian: "марта" with a day, "Март" without) have a "Standalone" text.
     */
    public String getStandaloneMonthName() {
        String standalone = Translator.optional(monthKey + "Standalone");
        return standalone != null ? standalone : getMonthName();
    }
}

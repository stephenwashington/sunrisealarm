package minervaheavyindustries.sunrisealarm;


import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.minervaheavyindustries.sunrisealarm.SunriseCalculator;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class SunriseCalculatorTest {

    public void logCalendar(Calendar c){
        SimpleDateFormat sunriseDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss z", Locale.US);
        sunriseDate.setCalendar(c);
        Log.d("logCalendar", sunriseDate.format(c.getTime()));
    }

    public Calendar setupCalendar(int year, int month, int day, int hour,
                                int minute, int second, String timezone){
        month--;
        Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone(timezone));
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, second);
        c.set(Calendar.MILLISECOND, 0);
        return c;
    }

    public double getSunriseTime(Calendar c){
        return c.get(Calendar.HOUR_OF_DAY) + c.get(Calendar.MINUTE) / 60. +
                c.get(Calendar.SECOND) / 3600;
    }

    /*@Test
    public void sunriseCalculator_TestJulianDayCalculator(){
        Calendar c = setupCalendar(1957, 10, 4, 0, 0, 0, "UTC");
        assertEquals(2436115.5, SunriseCalculator.calcJulianDay(c), 0.01);

        c = setupCalendar(333, 1, 27, 0, 0, 0, "UTC");
        assertEquals(1842712.5, SunriseCalculator.calcJulianDay(c), 0.01);
    }

    @Test
    public void sunriseCalculator_TestGregorianDayCalculator(){
        Calendar c;

        c = SunriseCalculator.calcGregorianDate(2436116.31);
        assertEquals(c.get(Calendar.YEAR), 1957);
        assertEquals(c.get(Calendar.MONTH)+1, 10);
        assertEquals(c.get(Calendar.DAY_OF_MONTH), 4);
        assertEquals(c.get(Calendar.HOUR_OF_DAY), 19);
        assertEquals(c.get(Calendar.MINUTE), 26);
        assertEquals(c.get(Calendar.SECOND), 24);

        c = SunriseCalculator.calcGregorianDate(1842713);
        assertEquals(c.get(Calendar.YEAR), 333);
        assertEquals(c.get(Calendar.MONTH)+1, 1);
        assertEquals(c.get(Calendar.DAY_OF_MONTH), 27);
        assertEquals(c.get(Calendar.HOUR_OF_DAY), 12);
        assertEquals(c.get(Calendar.MINUTE), 0);
        assertEquals(c.get(Calendar.SECOND), 0);
    }

    @Test
    public void sunriseCalculator_TestObliquityEcliptic(){
        Calendar c = setupCalendar(1962, 4, 8, 0, 0, 0, "EDT");
        double jd = SunriseCalculator.calcJulianDay(c);
        double epsilon = SunriseCalculator.calcObliquityEcliptic(jd);
        assertEquals(23.4424, epsilon, 0.01);
    }

    @Test
    public void sunriseCalculator_TestRightAscensionAndDeclination(){
        Calendar c = setupCalendar(1992, 10, 13, 0, 0, 0, "UTC");
        double jd = SunriseCalculator.calcJulianDay(c);
        double solarCoordinates[] = SunriseCalculator.calcSolarCoordinates(jd);
        double alpha = solarCoordinates[0];
        double delta = solarCoordinates[1];
        assertEquals(2448908.5, jd, 0.001);
        assertEquals(198.38083, alpha, 0.001);
        assertEquals(-7.78507, delta, 0.001);
    }*/

    @Test
    public void sunriseCalculator_TestCalculator(){
        Calendar c, sunrise;
        double sunriseTime;

        // Test #1 - Make sure it works generally
        c = setupCalendar(2016, 2, 14, 0, 0, 0, "UTC");
        sunrise = SunriseCalculator.getSunrise(38.53, -77.03, c);
        sunriseTime = getSunriseTime(sunrise);
        logCalendar(sunrise);
        assertEquals(12.02, sunriseTime, 0.05);

        // Test #2 - Make sure it handles cases where we get sunrise after it's already happened
        c = setupCalendar(2016, 2, 14, 15, 0, 0, "UTC");
        sunrise = SunriseCalculator.getSunrise(38.53, -77.03, c);
        sunriseTime = getSunriseTime(sunrise);
        logCalendar(sunrise);
        assertEquals(12.00, sunriseTime, 0.05);
        assertEquals(15, sunrise.get(Calendar.DAY_OF_MONTH));

        // Test #3 - Test Different Time Zones
        c = setupCalendar(2016, 2, 14, 0, 0, 0, "UTC");

    }
}

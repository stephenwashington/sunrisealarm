package minervaheavyindustries.sunrisealarm;


import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.minervaheavyindustries.sunrisealarm.SunriseCalculator;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class SunriseCalculatorTest {

    public void logCalendar(DateTime dt){
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss Z");
        Log.d("logCalendar", formatter.print(dt));
    }

    public DateTime setupDateTime(int year, int month, int day, int hour,
                                  int minute, int second, String timezone){
        return new DateTime(year, month, day, hour, minute, second, DateTimeZone.forID(timezone));
    }

    public double getSunriseTime(DateTime dt){
        return dt.getHourOfDay() + dt.getMinuteOfHour() / 60. + dt.getSecondOfMinute() / 3600.;
    }

    @Test
    public void sunriseCalculator_TestCalculator(){
        DateTime dt, sunrise, sunriseLocal;
        double sunriseTime;

        // Test #1 - Make sure it works generally
        dt = setupDateTime(2016, 2, 14, 0, 0, 0, "America/New_York");
        sunrise = SunriseCalculator.getSunrise(38.53, -77.03, dt);
        sunriseLocal = sunrise.withZone(dt.getZone());
        sunriseTime = getSunriseTime(sunriseLocal);
        logCalendar(sunriseLocal);
        assertEquals(7.02, sunriseTime, 0.05);

        // Test #2 - Make sure it handles cases where we get sunrise after it's already happened
        dt = setupDateTime(2016, 2, 14, 15, 0, 0, "America/New_York");
        sunrise = SunriseCalculator.getSunrise(38.53, -77.03, dt);
        sunriseLocal = sunrise.withZone(dt.getZone());
        sunriseTime = getSunriseTime(sunriseLocal);
        logCalendar(sunriseLocal);
        assertEquals(7.0, sunriseTime, 0.05);

        // Test #3 - Make sure it properly handles Daylight Savings
        dt = setupDateTime(2016, 7, 4, 0, 0, 0, "America/New_York");
        sunrise = SunriseCalculator.getSunrise(40.72, -74.02, dt);
        sunriseLocal = sunrise.withZone(dt.getZone());
        sunriseTime = getSunriseTime(sunriseLocal);
        logCalendar(sunriseLocal);
        assertEquals(5.5, sunriseTime, 0.05);

        // Test #4 - Test Same Time, Different Time Zones
        dt = setupDateTime(2016, 2, 14, 0, 0, 0, "America/Chicago");
        sunrise = SunriseCalculator.getSunrise(41.85, -87.65, dt);
        sunriseLocal = sunrise.withZone(dt.getZone());
        sunriseTime = getSunriseTime(sunriseLocal);
        logCalendar(sunriseLocal);
        assertEquals(6.8, sunriseTime, 0.05);

        dt = setupDateTime(2016, 2, 14, 0, 0, 0, "America/Denver");
        sunrise = SunriseCalculator.getSunrise(39.43, -104.59, dt);
        sunriseLocal = sunrise.withZone(dt.getZone());
        sunriseTime = getSunriseTime(sunriseLocal);
        logCalendar(sunriseLocal);
        assertEquals(6.9, sunriseTime, 0.05);

        dt = setupDateTime(2016, 2, 14, 0, 0, 0, "Asia/Tokyo");
        sunrise = SunriseCalculator.getSunrise(35.62, 139.73, dt);
        sunriseLocal = sunrise.withZone(dt.getZone());
        sunriseTime = getSunriseTime(sunriseLocal);
        logCalendar(sunriseLocal);
        assertEquals(6.5, sunriseTime, 0.05);

        dt = setupDateTime(2016, 2, 14, 0, 0, 0, "Asia/Shanghai");
        sunrise = SunriseCalculator.getSunrise(39.92, 116.42, dt);
        sunriseLocal = sunrise.withZone(dt.getZone());
        sunriseTime = getSunriseTime(sunriseLocal);
        logCalendar(sunriseLocal);
        assertEquals(7.17, sunriseTime, 0.05);

        dt = setupDateTime(2016, 2, 14, 0, 0, 0, "Asia/Kolkata");
        sunrise = SunriseCalculator.getSunrise(28.6, 77.2, dt);
        sunriseLocal = sunrise.withZone(dt.getZone());
        sunriseTime = getSunriseTime(sunriseLocal);
        logCalendar(sunriseLocal);
        assertEquals(7.02, sunriseTime, 0.05);

        dt = setupDateTime(2016, 2, 14, 0, 0, 0, "Europe/Moscow");
        sunrise = SunriseCalculator.getSunrise(55.75, 37.58, dt);
        sunriseLocal = sunrise.withZone(dt.getZone());
        sunriseTime = getSunriseTime(sunriseLocal);
        logCalendar(sunriseLocal);
        assertEquals(7.96, sunriseTime, 0.05);

        dt = setupDateTime(2016, 2, 14, 0, 0, 0, "Europe/Berlin");
        sunrise = SunriseCalculator.getSunrise(52.33, 13.30, dt);
        sunriseLocal = sunrise.withZone(dt.getZone());
        sunriseTime = getSunriseTime(sunriseLocal);
        logCalendar(sunriseLocal);
        assertEquals(7.43, sunriseTime, 0.05);

        dt = setupDateTime(2016, 2, 14, 0, 0, 0, "Europe/London");
        sunrise = SunriseCalculator.getSunrise(51.5, 0.13, dt);
        sunriseLocal = sunrise.withZone(dt.getZone());
        sunriseTime = getSunriseTime(sunriseLocal);
        logCalendar(sunriseLocal);
        assertEquals(7.28, sunriseTime, 0.05);

        dt = setupDateTime(2016, 2, 14, 0, 0, 0, "America/Sao_Paulo");
        sunrise = SunriseCalculator.getSunrise(-22.9, -43.23, dt);
        sunriseLocal = sunrise.withZone(dt.getZone());
        sunriseTime = getSunriseTime(sunriseLocal);
        logCalendar(sunriseLocal);
        assertEquals(6.68, sunriseTime, 0.05);

    }
}

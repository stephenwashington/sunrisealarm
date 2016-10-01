package com.minervaheavyindustries.sunrisealarm;


import java.util.Calendar;
import java.util.TimeZone;

public class SunriseCalculator {

    // Astronomical Algorithms, Ch 15
    public static Calendar getSunrise(double latitude, double longitude, Calendar now){
        Calendar sunrise = calcSunrise(latitude, longitude, now, false);
        return (now.compareTo(sunrise) > 0) ? calcSunrise(latitude, longitude, now, true) : sunrise;
     }

    private static double putInRange(double a, int min, int max){
        if (a > max){
            while (a > max) {
                a -= (max - min);
            }
        }

        if (a < min){
            while (a < min) {
                a += (max - min);
            }
        }
        return a;

    }

    private static double sinr(double a) {
        return Math.sin(Math.toRadians(a));
    }

    private static double cosr(double a) {
        return Math.cos(Math.toRadians(a));
    }

    // Astronomical Algorithms, Chapter 7
    private static double calcJulianDay(Calendar c){
        int y = c.get(Calendar.YEAR);
        int m = c.get(Calendar.MONTH);
        int d = c.get(Calendar.DAY_OF_MONTH);

        m++; // Calendar stores MONTH as 0-indexed
        if (m <= 2){
            y--;
            m += 12;
        }

        int a = (int)(y / 100.);
        int b = (y >= 1582 && m >= 10 && d >= 4) ? 2 - a + (int)(a / 4.) : 0;
        return (int)(365.25*(y + 4716)) + (int)(30.6001*(m+1)) + d + b - 1524.5;
    }

    // Astronomical Algorithms, Chapter 7
    private static Calendar calcGregorianDate(double julianDate){
        int A, B, C, D, E, Z;
        double F;
        julianDate += 0.5;
        Z = (int)(julianDate);
        F = julianDate - (int)(julianDate);
        if (Z >= 2299161){
            int alpha = (int)((Z - 1867216.25) / 36524.25);
            A = Z + 1 + alpha - (int)(alpha / 4.);
        } else{
            A = Z;
        }

        B = A + 1524;
        C = (int)((B - 122.1) / 365.25);
        D = (int)(365.25*C);
        E = (int)((B - D) / 30.6001);
        double dayWithFraction = B - D - (int)(30.6001*E) + F;
        int month = (E < 14) ? E - 1 : E - 13;
        int year = (month > 2) ? C - 4716 : C - 4715;
        int day = (int)dayWithFraction;
        double dayFraction = dayWithFraction - (int)(dayWithFraction);
        double hours = dayFraction * 24;
        int hour = (int)hours;
        int minute = (int)(hours * 60) % 60;
        int second = (int)(hours * 60 * 60) % 60;

        Calendar gregorianUTC = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        gregorianUTC.set(Calendar.YEAR, year);
        gregorianUTC.set(Calendar.MONTH, month-1);
        gregorianUTC.set(Calendar.DAY_OF_MONTH, day);
        gregorianUTC.set(Calendar.HOUR_OF_DAY, hour);
        gregorianUTC.set(Calendar.MINUTE, minute);
        gregorianUTC.set(Calendar.SECOND, second);
        gregorianUTC.set(Calendar.MILLISECOND, 0);

        return gregorianUTC;
    }

    private static double getJulianCentury(double jd){
        return (jd - 2451545.0) / 36525;
    }

    // http://eclipse.gsfc.nasa.gov/SEhelp/deltatpoly2004.html
    // Note that this is only accurate for the years 2005-2050
    private static double calcDeltaT(Calendar c){
        double y = (c.get(Calendar.YEAR) + (c.get(Calendar.MONTH) - 0.5)/12);
        double t = y - 2000;
        return 62.92 + 0.32217*t + 0.005589*t*t;
     }

    // Astronomical Algorithms, Chapter 12
    private static double calcGreenwichApparentSiderealTime(double jd){
        double T = getJulianCentury(jd);
        double gmst = putInRange(100.46061837 + 36000.770053608*T +
                0.000387933*T*T - T*T*T/38710000, 0, 360);
        double epsilon = calcObliquityEcliptic(jd);
        double deltaPsi = calcNutationInLongitude(jd);
        return gmst + (deltaPsi * cosr(epsilon));
    }

    // Astronomical Algorithms, Chapter 25
    private static double[] calcSolarCoordinates(double jd){
        double T = getJulianCentury(jd);
        double l = 280.46646 + 36000.76983*T + 0.0003032*T*T;
        double L0 = putInRange(l, 0, 360);
        double M0 = 357.52911 + 35999.05029*T - 0.0001537*T*T;
        double M = putInRange(M0, 0, 360);
        double C = (1.914602 - 0.004817*T - 0.000014*T*T)*sinr(M) +
                (0.019993 - 0.000101*T)*sinr(2*M) +
                (0.000289)*sinr(3*M);
        double trueLongitude = L0 + C;
        double OMEGA = 125.04 - 1934.136*T;
        double lambda = trueLongitude - 0.00569 - 0.00478*sinr(OMEGA);
        double epsilon = calcObliquityEcliptic(jd) + 0.00256*cosr(OMEGA);
        double alpha = Math.toDegrees(Math.atan2(cosr(epsilon) *
                sinr(lambda), cosr(lambda)));
        double delta = Math.toDegrees(Math.asin(sinr(epsilon) * sinr(lambda)));
        return (new double[]{putInRange(alpha, 0, 360), delta});
    }

    // Astronomical Algorithms, Chapter 22
    private static double calcObliquityEcliptic(double jd){
        double T = getJulianCentury(jd);
        double U = T / 100;
        double epsilon0 = 23.439291 - 1.30026*U - 4.306e-4*U*U - 0.55535*U*U*U - 0.014272*U*U*U*U -
                0.06935*U*U*U*U*U - 0.010847*U*U*U*U*U*U + 0.001978*U*U*U*U*U*U*U +
                0.0077417*U*U*U*U*U*U*U*U + 0.001608*U*U*U*U*U*U*U*U*U +
                6.806e-4*U*U*U*U*U*U*U*U*U*U;
        double OMEGA = putInRange(125.04452 - 1934.136231*T + 0.0020708*T*T + T*T*T/450000, 0, 360);
        double L = putInRange(280.4465 + 36000.7698*T, 0, 360);
        double LPrime = putInRange(218.3165  + 481267.8813*T, 0, 360);
        double deltaEpsilon = 5e-4*cosr(OMEGA) + 1.58e-4*cosr(2*L) +
                2.78e-5*cosr(2*LPrime) - 2.5e-5*cosr(2*OMEGA);
        return epsilon0 + deltaEpsilon;
    }

    // Astronomical Algorithms, Chapter 22
    private static double calcNutationInLongitude(double jd){
        double T = getJulianCentury(jd);
        double OMEGA = putInRange(125.04452 - 1934.136231*T + 0.0020708*T*T + T*T*T/450000, 0, 360);
        double L = putInRange(280.4465 + 36000.7698*T, 0, 360);
        double LPrime = putInRange(218.3165  + 481267.8813*T, 0, 360);
        return -0.004778*cosr(OMEGA) + 3.667e-4*cosr(2*L) -
                6.39e-5*cosr(2*LPrime) - 5.83e-5*cosr(2*OMEGA);
    }

    // Astronomical Algorithms, Chapter 15
    // 'tomorrow' param if we need to calculate today's sunrise, or tomorrow's
    private static Calendar calcSunrise(double latitude, double longitude, Calendar now, boolean tomorrow){

        longitude *= -1; //positive west of greenwich

        double jd = calcJulianDay(now);
        if (tomorrow) { jd++;}
        double gast = calcGreenwichApparentSiderealTime(jd);
        double solarCoordinates1[] = calcSolarCoordinates(jd-1);
        double solarCoordinates2[] = calcSolarCoordinates(jd);
        double solarCoordinates3[] = calcSolarCoordinates(jd+1);

        double alpha1 = solarCoordinates1[0];
        double delta1 = solarCoordinates1[1];
        double alpha2 = solarCoordinates2[0];
        double delta2 = solarCoordinates2[1];
        double alpha3 = solarCoordinates3[0];
        double delta3 = solarCoordinates3[1];

        double h0 = -0.8333;
        double cosHourAngle = (sinr(h0) - sinr(latitude)*sinr(delta2)) /
                (cosr(latitude)*cosr(delta2));
        double H0;
        if (cosHourAngle > -1 && cosHourAngle < 1) {
            H0 = putInRange(Math.toDegrees(Math.acos(cosHourAngle)), 0, 180);
        }
        else {
            return null; //sunrise won't happen now because you're too close to a pole
        }

        double m0 = putInRange((alpha2 + longitude - gast)/360, 0, 1);
        double m1 = putInRange(m0 - H0/360, 0, 1);

        //correction for m1
        double deltam;
        do {
            double deltaT = calcDeltaT(now);
            double n = m1 + deltaT/86400;

            double a = alpha2 - alpha1;
            double b = alpha3 - alpha2;
            double c = alpha1 + alpha3 - 2*alpha2;
            double alpha = alpha2 + (n/2)*(a + b + n*c);

            a = delta2 - delta1;
            b = delta3 - delta2;
            c = delta1 + delta3 - 2*delta2;
            double delta = delta2 + (n/2)*(a + b + n*c);
            double theta0 = gast + 360.985647 * m1;
            double H = putInRange(theta0 - longitude - alpha, -180, 180);
            double h = Math.toDegrees(Math.asin(sinr(latitude)*sinr(delta) +
                    cosr(latitude)*cosr(delta)*cosr(H)));
            deltam = (h - h0) / (360*cosr(delta)*cosr(latitude)*sinr(H));
            m1 += deltam;
        } while(Math.abs(deltam) > 0.0001);

        return calcGregorianDate(jd + m1);
    }
}

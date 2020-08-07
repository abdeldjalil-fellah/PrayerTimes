package jalil.prayertimes;

// http://praytimes.org/code/git/?a=viewblob&p=PrayTimes&h=093f77d6cc83b53fb12e9900803d5fa75dacd110&hb=HEAD&f=v1/java/PrayTime.java

public class PrayTime {

    private double JulianDate;
    private String Date = "2018-11-18";

    public void setDate(String date) {

        this.Date = date;
        int[] dateComponenets = Utilities.dateComponents(date);

        this.JulianDate = julianDate(dateComponenets[0], dateComponenets[1], dateComponenets[2]) - this.LocationLongitude / (15.0 * 24.0);
    }

    public String getDate() {
        return this.Date;
    }

    double LocationLatitude; // latitude
    double LocationLongitude; // longitude
    double LocationTimezone; // time-zone

    double ParamFajrAngle = 18.0;
    double ParamAsrShadowFactor = 1.0;
    double ParamIshaAngle = 17.0;

    private double[] Offsets = {0, 0, 0, 0, 0, 0};

    PrayTime() {
    }

    final static int PRAYER_INDEX_FAJR = 0;
    final static int PRAYER_INDEX_SHURUQ = 1;
    final static int PRAYER_INDEX_DHUHR = 2;
    final static int PRAYER_INDEX_ASR = 3;
    final static int PRAYER_INDEX_MAGHRIB = 4;
    final static int PRAYER_INDEX_ISHA = 5;
    final static int PRAYER__COUNT = 6;

    // compute prayer times at given julian date
    // subh, shuruq, dhuha, dhuhr, asr, asrEnd, maghrib, isha
    String[] getPrayerTimes() {
        double[] times = {5, 7, 13, 16, 18, 20}; // default times

        int iterations = 1;
        for (int i = 1; i <= iterations; i++) {
            times = computeTimes(times);
        }

        times = adjustWithTimezoneAndLongitude(times);
        times = adjustWithOffset(times);

        return timesToString(times);
    }

    // ---------------------- Trigonometric Functions -----------------------
    // range reduce angle in degrees.
    private double fixangle(double a) {
        a = a - (360 * (Math.floor(a / 360.0)));
        a = a < 0 ? (a + 360) : a;
        return a;
    }

    // range reduce hours to 0..23
    private double fixhour(double a) {
        a = a - 24.0 * Math.floor(a / 24.0);
        a = a < 0 ? (a + 24) : a;
        return a;
    }

    // radian to degree
    private double radiansToDegrees(double alpha) {
        return ((alpha * 180.0) / Math.PI);
    }

    // deree to radian
    private double DegreesToRadians(double alpha) {
        return ((alpha * Math.PI) / 180.0);
    }

    // degree sin
    private double dsin(double d) {
        return (Math.sin(DegreesToRadians(d)));
    }

    // degree cos
    private double dcos(double d) {
        return (Math.cos(DegreesToRadians(d)));
    }

    // degree tan
    private double dtan(double d) {
        return (Math.tan(DegreesToRadians(d)));
    }

    // degree arcsin
    private double darcsin(double x) {
        double val = Math.asin(x);
        return radiansToDegrees(val);
    }

    // degree arccos
    private double darccos(double x) {
        double val = Math.acos(x);
        return radiansToDegrees(val);
    }

    // degree arctan2
    private double darctan2(double y, double x) {
        double val = Math.atan2(y, x);
        return radiansToDegrees(val);
    }

    // degree arccot
    private double darccot(double x) {
        double val = Math.atan2(1.0, x);
        return radiansToDegrees(val);
    }

    // ---------------------- Julian Date Functions -----------------------
    // calculate julian date from a calendar date
    private double julianDate(int year, int month, int day) {

        if (month <= 2) {
            year -= 1;
            month += 12;
        }
        double A = Math.floor(year / 100.0);

        double B = 2 - A + Math.floor(A / 4.0);

        double JD = Math.floor(365.25 * (year + 4716))
                + Math.floor(30.6001 * (month + 1)) + day + B - 1524.5;

        return JD;
    }

    // ---------------------- Calculation Functions -----------------------
    // References:
    // http://www.ummah.net/astronomy/saltime
    // http://aa.usno.navy.mil/faq/docs/SunApprox.html
    // compute declination angle of sun and equation of time
    private double[] sunPosition(double jd) {

        double D = jd - 2451545;
        double g = fixangle(357.529 + 0.98560028 * D);
        double q = fixangle(280.459 + 0.98564736 * D);
        double L = fixangle(q + (1.915 * dsin(g)) + (0.020 * dsin(2 * g)));

        // double R = 1.00014 - 0.01671 * [self dcos:g] - 0.00014 * [self dcos:
        // (2*g)];
        double e = 23.439 - (0.00000036 * D);
        double d = darcsin(dsin(e) * dsin(L));
        double RA = (darctan2((dcos(e) * dsin(L)), (dcos(L)))) / 15.0;
        RA = fixhour(RA);
        double EqT = q / 15.0 - RA;
        double[] sPosition = new double[2];
        sPosition[0] = d;
        sPosition[1] = EqT;

        return sPosition;
    }

    // compute equation of time
    private double equationOfTime(double jd) {
        double eq = sunPosition(jd)[1];
        return eq;
    }

    // compute declination angle of sun
    private double sunDeclination(double jd) {
        double d = sunPosition(jd)[0];
        return d;
    }

    // compute mid-day (Dhuhr, Zawal) time
    private double computeMidDay(double t) {
        double T = equationOfTime(this.JulianDate + t);
        double Z = fixhour(12 - T);
        return Z;
    }

    // compute time for a given angle G
    private double getTimeOfElevation(double G, double t) {

        double D = sunDeclination(this.JulianDate + t);
        double Z = computeMidDay(t);
        double Beg = -dsin(G) - dsin(D) * dsin(this.LocationLatitude);
        double Mid = dcos(D) * dcos(this.LocationLatitude);
        double V = darccos(Beg / Mid) / 15.0;

        return Z + (G > 90 ? -V : V);
    }

    // Shafii: step=1, Hanafi: step=2
    private double computeAsr(double step, double t) {
        double D = sunDeclination(this.JulianDate + t);
        double G = -darccot(step + dtan(Math.abs(this.LocationLatitude - D)));
        return getTimeOfElevation(G, t);
    }

    // convert double hours to 24h format
    private String toTime(double time) {

        if (Double.isNaN(time)) {
            return Constants.INVALID_STRING;
        }

        time = fixhour(time);

        int hours = (int) time;
        int minutes = (int) (time * 60) % 60;
        int seconds = (int) (time * (60 * 60)) % 60;

        return Utilities.timeString(hours, minutes, seconds, 0, true);
    }

    // ---------------------- Compute Prayer Times -----------------------
    // compute prayer times at given julian date
    private double[] computeTimes(double[] times) {

        double[] t = dayPortion(times);

        double fajr = this.getTimeOfElevation(180 - ParamFajrAngle, t[PRAYER_INDEX_FAJR]);
        double shuruk = this.getTimeOfElevation(180 - 0.833, t[PRAYER_INDEX_SHURUQ]);
        double dhuhr = this.computeMidDay(t[PRAYER_INDEX_DHUHR]);
        double asr = this.computeAsr(ParamAsrShadowFactor, t[PRAYER_INDEX_ASR]);
        double maghrib = this.getTimeOfElevation(0.833, t[PRAYER_INDEX_MAGHRIB]);
        double isha = this.getTimeOfElevation(ParamIshaAngle, t[PRAYER_INDEX_ISHA]);

        return new double[]{fajr, shuruk, dhuhr, asr, maghrib, isha};
    }

    // adjust times in a prayer time array
    private double[] adjustWithTimezoneAndLongitude(double[] times) {

        for (int i = 0; i < times.length; i++) {
            times[i] += this.LocationTimezone - this.LocationLongitude / 15.0;
        }

        return times;
    }

    // convert times array to given time format
    private String[] timesToString(double[] times) {

        String[] result = new String[PRAYER__COUNT];

        for (int i = 0; i < PRAYER__COUNT; i++) {
            result[i] = toTime(times[i]);
        }
        return result;
    }

    // convert hours to day portions
    private double[] dayPortion(double[] times) {
        for (int i = 0; i < PRAYER__COUNT; i++) {
            times[i] /= 24;
        }
        return times;
    }

    private double[] adjustWithOffset(double[] times) {
        for (int i = 0; i < times.length; i++) {
            times[i] = times[i] + this.Offsets[i];
        }

        return times;
    }
}

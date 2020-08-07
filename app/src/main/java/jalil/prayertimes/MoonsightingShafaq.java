package jalil.prayertimes;

class MoonsightingShafaq {

    // https://github.com/batoulapps/adhan-java/blob/master/adhan/src/main/java/com/batoulapps/adhan/PrayerTimes.java

    private static double seasonAdjustedTwilight
            (
                    double latitude, int dayOfYear, int year, double suntimeJdUtc,
                    double a, double b, double c, double d, int direction
            ) {

        final double adjustmentMinutes;
        final int dyy = daysSinceSolstice(dayOfYear, year, latitude);

        if (dyy < 91) {
            adjustmentMinutes = a + (b - a) / 91.0 * dyy;
        } else if (dyy < 137) {
            adjustmentMinutes = b + (c - b) / 46.0 * (dyy - 91);
        } else if (dyy < 183) {
            adjustmentMinutes = c + (d - c) / 46.0 * (dyy - 137);
        } else if (dyy < 229) {
            adjustmentMinutes = d + (c - d) / 46.0 * (dyy - 183);
        } else if (dyy < 275) {
            adjustmentMinutes = c + (b - c) / 46.0 * (dyy - 229);
        } else {
            adjustmentMinutes = b + (a - b) / 91.0 * (dyy - 275);
        }

        return suntimeJdUtc + direction * (adjustmentMinutes / 1440.0); /* 1440 : minutes in a day */
    }

    static double seasonAdjustedTwilightFajr(double latitude, int dayOfYear, int year, double sunriseJdUtc) {

        double latitudeAbs = Math.abs(latitude);

        double a = 75 + 28.65 / 55.0 * latitudeAbs;
        double b = 75 + 19.44 / 55.0 * latitudeAbs;
        double c = 75 + 32.74 / 55.0 * latitudeAbs;
        double d = 75 + 48.10 / 55.0 * latitudeAbs;

        return seasonAdjustedTwilight(latitude, dayOfYear, year, sunriseJdUtc, a, b, c, d, -1);
    }

    static double seasonAdjustedTwilightIshaGeneral(double latitude, int dayOfYear, int year, double sunsetJdUtc) {

        double latitudeAbs = Math.abs(latitude);

        double a = 75 + 25.60 / 55.0 * latitudeAbs;
        double b = 75 + 02.05 / 55.0 * latitudeAbs;
        double c = 75 - 09.21 / 55.0 * latitudeAbs;
        double d = 75 + 06.14 / 55.0 * latitudeAbs;

        return seasonAdjustedTwilight(latitude, dayOfYear, year, sunsetJdUtc, a, b, c, d, 1);
    }

    static double seasonAdjustedTwilightIshaAhmer(double latitude, int dayOfYear, int year, double sunsetJdUtc) {

        double latitudeAbs = Math.abs(latitude);

        double a = 62 + 17.40 / 55.0 * latitudeAbs;
        double b = 62 - 07.16 / 55.0 * latitudeAbs;
        double c = 62 + 05.12 / 55.0 * latitudeAbs;
        double d = 62 + 19.44 / 55.0 * latitudeAbs;

        return seasonAdjustedTwilight(latitude, dayOfYear, year, sunsetJdUtc, a, b, c, d, 1);
    }

    static double seasonAdjustedTwilightIshaAbyadh(double latitude, int dayOfYear, int year, double sunsetJdUtc) {

        double latitudeAbs = Math.abs(latitude);

        double a = 75 + 25.60 / 55.0 * latitudeAbs;
        double b = 75 + 07.16 / 55.0 * latitudeAbs;
        double c = 75 + 36.84 / 55.0 * latitudeAbs;
        double d = 75 + 81.84 / 55.0 * latitudeAbs;

        return seasonAdjustedTwilight(latitude, dayOfYear, year, sunsetJdUtc, a, b, c, d, 1);
    }

    private static int daysSinceSolstice(int dayOfYear, int year, double latitude) {

        int daysSinceSolistice;
        final int northernOffset = 10;
        boolean isLeapYear = isLeapYear(year);

        final int southernOffset = isLeapYear ? 173 : 172;
        final int daysInYear = isLeapYear ? 366 : 365;

        if (latitude >= 0) {
            daysSinceSolistice = dayOfYear + northernOffset;
            if (daysSinceSolistice >= daysInYear) {
                daysSinceSolistice = daysSinceSolistice - daysInYear;
            }
        } else {
            daysSinceSolistice = dayOfYear - southernOffset;
            if (daysSinceSolistice < 0) {
                daysSinceSolistice = daysSinceSolistice + daysInYear;
            }
        }

        return daysSinceSolistice;
    }

    private static boolean isLeapYear(int year) {
        return year % 4 == 0 && !(year % 100 == 0 && year % 400 != 0);
    }
}

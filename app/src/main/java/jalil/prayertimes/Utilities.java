package jalil.prayertimes;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by jalil on 22/10/2018.
 */
class Utilities {

    static void logDebug(String message) {
        Log.d(Constants.APP_TAG, message);
    }

    static void logError(String message) {
        Log.e(Constants.APP_TAG, message);
    }

    static String pathCombine(File path1, String path2) {
        return new File(path1, path2).getPath();
    }

    static int secondsOf(String time) {

        if (time.equals(Constants.INVALID_STRING)) return 0;

        return Integer.parseInt(time.substring(0, 2)) * Constants.SECONDS_IN_HOUR
                + Integer.parseInt(time.substring(3, 5)) * 60
                + (time.length() == 8 ? Integer.parseInt(time.substring(6)) : 0);
    }

    static String timeOf(int totalSeconds, boolean withSeconds) {

        if (totalSeconds < 0) {
            totalSeconds = totalSeconds + Constants.SECONDS_IN_DAY;
        } else if (totalSeconds >= Constants.SECONDS_IN_DAY) {
            totalSeconds = totalSeconds % Constants.SECONDS_IN_DAY;
        }

        int hours = totalSeconds / Constants.SECONDS_IN_HOUR;
        int minutes = (totalSeconds % Constants.SECONDS_IN_HOUR) / 60;
        int seconds = totalSeconds % 60;

        return timeString(hours, minutes, seconds, 0, withSeconds);
    }

    static String getHourName(int hour) {

        if (hour < 1) return Constants.INVALID_STRING;
        if (hour == 12) return "آخر ساعة";

        return String.format(Locale.ROOT, "%s %d", Constants.TITLE_HOUR, hour);
    }

    static String getHourMessage(int hour) {

        if (hour < 1) return Constants.INVALID_STRING;
        if (hour == 12) return "آ.ساعة";

        return String.format(Locale.ROOT, "%s%d", Constants.MESSAGE_HOUR, hour);
    }

    static String getMonthNameHijri(int month) {

        if (month < 1 || month > 12) return Constants.INVALID_STRING;

        switch (month) {
            case 1:
                return "الْمُحَرَّم";
            case 2:
                return "صَفَر";
            case 3:
                return "رَبِيع1";
            case 4:
                return "رَبِيع2";
            case 5:
                return "جُمَادَى1";
            case 6:
                return "جُمَادَى2";
            case 7:
                return "رَجَب";
            case 8:
                return "شَعْبَان";
            case 9:
                return "رَمَضَان";
            case 10:
                return "شَوَّال";
            case 11:
                return "ذُو الْقَعْدَة";
            case 12:
                return "ذُو الْحِجَّة";
            default:
                return Constants.INVALID_STRING;
        }
    }

    static String getMonthNameGregorianAssyrian(int month) {

        if (month < 1 || month > 12) return Constants.INVALID_STRING;

        switch (month) {
            case 1:
                return "يَنَاير" + "/" + "كانون2";
            case 2:
                return "فِبْراير" + "/" + "شُباط";
            case 3:
                return "مارِس" + "/" + "آذار";
            case 4:
                return "أَبريل" + "/" + "نَيْسان";
            case 5:
                return "مايو" + "/" + "أَيَّار";
            case 6:
                return "يونيو" + "/" + "حَزِيرَان";
            case 7:
                return "يوليو" + "/" + "تَمُّوز";
            case 8:
                return "أُغُسْطُس" + "/" + "آب";
            case 9:
                return "سِبْتمبر" + "/" + "أَيْلول";
            case 10:
                return "أكتوبر" + "/" + "تَشْرِين1";
            case 11:
                return "نوفمبر" + "/" + "تَشْرِين2";
            case 12:
                return "ديسمبر" + "/" + "كانون1";
            default:
                return Constants.INVALID_STRING;
        }
    }

    static double valueOf(String text, int index, int itemLength, boolean hasSign) {
        int startIndex = index * itemLength + index;
        return Utilities.toDouble(text.substring(startIndex, startIndex + itemLength), hasSign);
    }

    static int[] iqamaArrayOf(String iqama) {

        int[] array = new int[5];

        for (int i = 0; i < 5; i++) {
            array[i] = (int) valueOf(iqama, i, 2, false);
        }

        return array;
    }

    static int[] offsetArrayOf(String offset) {

        int[] array = new int[6];

        for (int i = 0; i < 6; i++) {
            array[i] = (int) valueOf(offset, i, 3, true);
        }

        return array;
    }

    private static String remaining(int sec1, int sec2, boolean withSeconds) {
        return timeOf(sec2 - sec1, withSeconds);
    }

    private static String timeR(int refSec, String time, int iqM, String name, boolean withSeconds) {

        if (time.equals(Constants.INVALID_STRING)) return Constants.INVALID_STRING;

        int timeS = Utilities.secondsOf(time);

        int divider = withSeconds ? 1 : 60;
        int multiplier = withSeconds ? 60 : 1;

        int n = refSec / divider;
        int t = timeS / divider;

        if (t - n > 0) {
            return name + " بعد " + Utilities.remaining(refSec, timeS, withSeconds);
        }

        if (n - t < 2 * multiplier) {
            return name + " الآن";
        }

        if (iqM > 0) {

            int timeIq = t + iqM * multiplier;

            if (timeIq - n > 0) {
                return "الإقامة بعد " + Utilities.remaining(refSec, timeIq * divider, withSeconds);
            }

            if (n - timeIq < 5 * multiplier) {
                return "الصلاة الآن";
            }
        }

        return null;
    }

    static String timeG(int refSec, String time, int iqM, String name, boolean withSeconds) {
        String timeR = timeR(refSec, time, iqM, name, withSeconds);
        if (timeR != null) return timeR;
        return name + " منذ " + Utilities.remaining(Utilities.secondsOf(time), refSec, withSeconds);
    }

    static String[] wgMessage
            (
                    int refSec, String subh, String shuruq, String dhuhr, String asr, String maghrib, String isha,
                    int fajrIq, int shuruqIq, int dhuhrIq, int asrIq, int maghribIq, int ishaIq,
                    boolean withSeconds, boolean jumuaa
            ) {

        String subhR = timeR(refSec, subh, fajrIq, Constants.TITLE_SUBH, withSeconds);
        if (subhR != null) return new String[]{Constants.TITLE_SUBH, subhR};

        String shuruqR = timeR(refSec, shuruq, shuruqIq, Constants.TITLE_SUNRISE, withSeconds);
        if (shuruqR != null) return new String[]{Constants.TITLE_SUNRISE, shuruqR};

        String dhuhrR = timeR(refSec, dhuhr, dhuhrIq, jumuaa ? Constants.TITLE_JUMUAA : Constants.TITLE_DHUHR, withSeconds);
        if (dhuhrR != null) return new String[]{Constants.TITLE_DHUHR, dhuhrR};

        String asrR = timeR(refSec, asr, asrIq, Constants.TITLE_ASR, withSeconds);
        if (asrR != null) return new String[]{Constants.TITLE_ASR, asrR};

        String maghribR = timeR(refSec, maghrib, maghribIq, Constants.TITLE_MAGHRIB, withSeconds);
        if (maghribR != null) return new String[]{Constants.TITLE_MAGHRIB, maghribR};

        String ishaR = timeR(refSec, isha, ishaIq, Constants.TITLE_ISHA, withSeconds);
        if (ishaR != null) return new String[]{Constants.TITLE_ISHA, ishaR};

        subhR = Constants.TITLE_SUBH + " بعد " + Utilities.remaining(refSec, Utilities.secondsOf(subh) + 24 * 3600, withSeconds);
        return new String[]{Constants.TITLE_SUBH, subhR};
    }

    static double toDouble(String d, boolean requirePositivePrefix) {
        try {
            DecimalFormat formatter = getDecimalFormatterHalfUp("#.#");
            if (requirePositivePrefix) formatter.setPositivePrefix("+");
            else formatter.setPositivePrefix("");
            return formatter.parse(d).doubleValue();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    static int[] dateComponents(String date) {

        if (date.equals(Constants.INVALID_STRING)) return null;

        return new int[]{
                Integer.parseInt(date.substring(0, 4)),
                Integer.parseInt(date.substring(5, 7)),
                Integer.parseInt(date.substring(8, 10))
        };
    }

    static int[] timeComponents(String time) {

        if (time.equals(Constants.INVALID_STRING)) return null;

        return new int[]{
                Integer.parseInt(time.substring(0, 2)),
                Integer.parseInt(time.substring(3, 5)),
                time.length() == 8 ? Integer.parseInt(time.substring(6)) : 0
        };
    }

    private static String getTime(String datetime) {
        if (datetime.length() > 10) return datetime.substring(11);
        else return timeString(0, 0, 0, 0, true);
    }

    static String adjustDateGreg(String datetime, int field, int amount, boolean returnTime) {

        Calendar calendar = GregorianCalendar.getInstance();
        String time = Utilities.getTime(datetime);

        int[] dateComponents = dateComponents(datetime);
        int[] timeComponents = timeComponents(time);

        calendar.set(dateComponents[0], dateComponents[1] - 1, dateComponents[2],
                timeComponents[0], timeComponents[1], timeComponents[2]
        );

        calendar.add(field, amount);

        return returnTime ? Variables.dateFormatYmdHms.format(calendar.getTime()) : Variables.dateFormatYmd.format(calendar.getTime());
    }

    static String adjustTime(String time, int plusSeconds, boolean withSeconds) {

        if (time.equals(Constants.INVALID_STRING)) return Constants.INVALID_STRING;

        int[] timeComponents = timeComponents(time);

        return timeString(timeComponents[0], timeComponents[1], timeComponents[2], plusSeconds, withSeconds);
    }

    public static String dateString(int year, int month, int day) {
        return String.format(Locale.ROOT, "%04d-%02d-%02d", year, month, day);
    }

    public static String dateString(int[] dateComponents) {
        return dateString(dateComponents[0], dateComponents[1], dateComponents[2]);
    }

    static String timeString(int hours, int minutes, int seconds, int plusSeconds, boolean returnSeconds) {

        if (plusSeconds != 0) {
            Calendar c = new GregorianCalendar();
            c.set(Calendar.HOUR_OF_DAY, hours);
            c.set(Calendar.MINUTE, minutes);
            c.set(Calendar.SECOND, seconds);

            c.add(Calendar.SECOND, plusSeconds);

            hours = c.get(Calendar.HOUR_OF_DAY);
            minutes = c.get(Calendar.MINUTE);
            seconds = c.get(Calendar.SECOND);
        }

        if (!returnSeconds) {

            if (seconds >= 1) {
                seconds = 0;
                minutes += 1;
            }
            if (minutes == 60) {
                minutes = 0;
                hours += 1;
            }
            if (hours == 24) {
                hours = 0;
            }
        }

        return String.format(Locale.ROOT, "%02d:%02d", hours, minutes) +
                (returnSeconds ? String.format(Locale.ROOT, ":%02d", seconds) : "");
    }

    static int[] adjustHijri(int hYear, int hMonth, int hDay, int adjustByDays) {

        if (adjustByDays != 0) {

            hDay += adjustByDays;

            if (hDay > 30) {
                hDay -= 30;
                hMonth++;
            } else if (hDay < 1) {
                hDay += 30;
                hMonth--;
            }

            if (hMonth > 12) {
                hMonth -= 12;
                hYear++;
            } else if (hMonth < 1) {
                hMonth += 12;
                hYear--;
            }
        }

        return new int[]{hYear, hMonth, hDay};
    }

    static int[] adjustHijri(String dateHijri, int adjustByDays) {

        int[] dateComponents = dateComponents(dateHijri);
        return adjustHijri(dateComponents[0], dateComponents[1], dateComponents[2], adjustByDays);
    }

    static String toDateGregorian
            (
                    String dateHijri,
                    int method,
                    int inputAdjustedByDays,
                    Context context
            ) {

        if (dateHijri.equals(Constants.INVALID_STRING)) return Constants.INVALID_STRING;

        switch (method) {

            case ActivityMain.HIJRI_METHOD_ACTUAL:

                return HijriMARW.toGregorian(context, dateHijri, inputAdjustedByDays);

            case ActivityMain.HIJRI_METHOD_UMM_AL_QURA:

                return HijriUAQ.toGregorian(context, dateHijri, inputAdjustedByDays);

            case ActivityMain.HIJRI_METHOD_UHC:

                return HijriUHC.toGregorian(context, dateHijri, inputAdjustedByDays);

            default:
            case ActivityMain.HIJRI_METHOD_QUAE:

                int[] dateHijriComponents = dateComponents(dateHijri);
                int[] dateGregQuae = HijriQuae.toGregorian(dateHijriComponents[0], dateHijriComponents[1], dateHijriComponents[2], inputAdjustedByDays);
                if (dateGregQuae[0] == -1) return Constants.INVALID_STRING;

                return Utilities.dateString(dateGregQuae);
        }
    }

    static String toDateHijri
            (
                    String dateGreg, int adjustOutputByDays,
                    int method, Context context,
                    boolean corrected, String time, String sunset
            ) {

        if (dateGreg.equals(Constants.INVALID_STRING)) return Constants.INVALID_STRING;

        if (corrected && Utilities.secondsOf(time) >= Utilities.secondsOf(sunset)) {
            dateGreg = Utilities.adjustDateGreg(dateGreg, Calendar.DAY_OF_MONTH, 1, false);
        }

        switch (method) {

            case ActivityMain.HIJRI_METHOD_ACTUAL:

                return HijriMARW.toHijri(context, dateGreg, adjustOutputByDays);

            case ActivityMain.HIJRI_METHOD_UMM_AL_QURA:

                return HijriUAQ.toHijri(context, dateGreg, adjustOutputByDays);

            case ActivityMain.HIJRI_METHOD_UHC:

                return HijriUHC.toHijri(context, dateGreg, adjustOutputByDays);

            default:
            case ActivityMain.HIJRI_METHOD_QUAE:

                int[] dateGregComponents = dateComponents(dateGreg);
                int[] dateHijriQuae = HijriQuae.toHijri(dateGregComponents[0], dateGregComponents[1], dateGregComponents[2], adjustOutputByDays);
                if (dateHijriQuae[0] == -1) return Constants.INVALID_STRING;

                return Utilities.dateString(dateHijriQuae);
        }
    }

    static Calendar toUTC(String datetime, TimeZone timezone) {
        Calendar calendar = GregorianCalendar.getInstance(timezone);
        String time = getTime(datetime);

        int[] dateComponents = dateComponents(datetime);
        int[] timeComponents = timeComponents(time);

        calendar.set(dateComponents[0], dateComponents[1] - 1, dateComponents[2],
                timeComponents[0], timeComponents[1], timeComponents[2]);
        calendar.add(Calendar.MILLISECOND, -timezone.getRawOffset());

        return calendar;
    }

    static Calendar fromUTC(String datetime, TimeZone timezone) {

        Calendar calendar = GregorianCalendar.getInstance(timezoneUTC());
        String time = getTime(datetime);

        int[] dateComponents = dateComponents(datetime);
        int[] timeComponents = timeComponents(time);

        calendar.set(dateComponents[0], dateComponents[1] - 1, dateComponents[2],
                timeComponents[0], timeComponents[1], timeComponents[2]);
        calendar.add(Calendar.MILLISECOND, timezone.getRawOffset());

        return calendar;
    }

    static TimeZone timezoneCalcCity(String offset) {
        return TimeZone.getTimeZone("GMT" + offset);
    }

    private static TimeZone timezoneUTC() {
        return TimeZone.getTimeZone("UTC");
    }

    static String getResponseFromURL(String urlString, boolean returnRedirectURL) {

        StringBuilder stringBuilder = new StringBuilder();

        try {
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("User-Agent", "PrayerTimesApp");
            urlConnection.setReadTimeout(5000 /* milliseconds */);
            urlConnection.setConnectTimeout(5000 /* milliseconds */);

            if (returnRedirectURL) {
                urlConnection.setInstanceFollowRedirects(false);
                urlConnection.connect();
                stringBuilder.append(urlConnection.getHeaderField("Location"));
            } else {

                urlConnection.connect();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }

                bufferedReader.close();
            }

            urlConnection.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return stringBuilder.toString();
    }

    static DecimalFormat getDecimalFormatterHalfUp(String pattern) {
        DecimalFormat decimalFormatter = (DecimalFormat) NumberFormat.getInstance(Locale.ROOT);
        decimalFormatter.applyPattern(pattern);
        decimalFormatter.setRoundingMode(RoundingMode.HALF_UP);
        return decimalFormatter;
    }

    static String daysAsString(double daysAsDouble) {
        int days = (int) daysAsDouble;
        double hoursAsDouble = (daysAsDouble - days) * 24;
        int hours = (int) hoursAsDouble;
        double minutesAsDouble = (hoursAsDouble - hours) * 60;

        return String.format(Locale.ROOT, "%02d:%s/%02d", hours, getDecimalFormatterHalfUp("00").format(minutesAsDouble), days);
    }

    private static String formatCoordinate(double coordinate) {
        return Variables.decimalFormatterCoordinate.format(coordinate);
    }

    private static String formatTimeZone(int timezoneOffset) {
        String sign = timezoneOffset >= 0 ? "+" : "-";
        timezoneOffset = Math.abs(timezoneOffset);

        int hours = timezoneOffset / 3600;
        int minutes = (timezoneOffset % 3600) / 60;

        return String.format(Locale.ROOT, "%s%02d:%02d", sign, hours, minutes);
    }

    private static int getTimeZoneOffset(double latitude, double longitude) {

        int timezoneOffset = Constants.DEFAULT_LOCATION_OFFSET;

        String timezonedb = getResponseFromURL("http://api.timezonedb.com/v2.1/get-time-zone?key=OVH8X0T0XLJG&format=json&fields=gmtOffset&by=position&lat=" + latitude + "&lng=" + longitude, false);

        try {
            JSONObject jsonObject = new JSONObject(timezonedb);
            timezoneOffset = jsonObject.optInt("gmtOffset");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return timezoneOffset;
    }

    static String[] getLocationDetails(Context context, int method, String language, double latitude, double longitude) {

        String locationName = Constants.DEFAULT_LOCATION_NAME;

        if (method == Constants.LOCATION_METHOD_GOOGLE) {
            Geocoder geocoder = new Geocoder(context, new Locale(language));
            List<Address> addresses;
            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
                if (addresses.size() > 0) {
                    Address address = addresses.get(0);
                    if (address.getLocality() != null) locationName = address.getLocality();
                    else if (address.getAdminArea() != null) locationName = address.getAdminArea();
                    else locationName = address.getCountryName();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            String nominatim = getResponseFromURL("https://nominatim.openstreetmap.org/reverse?format=json&addressdetails=1&lat=" + latitude + "&lon=" + longitude + "&accept-language=" + language + "&zoom=11", false);
            try {
                JSONObject jsonResult = new JSONObject(nominatim);
                JSONObject jsonAddress = jsonResult.getJSONObject("address");
                locationName = jsonAddress.optString("city");

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        int timezoneOffset = getTimeZoneOffset(latitude, longitude);

        return new String[]{locationName, formatCoordinate(latitude), formatCoordinate(longitude), formatTimeZone(timezoneOffset)};
    }

    static String[] getLocationDetails(Context context, int method, String language, String locationName, String countryCode) {

        double latitude = 0;
        double longitude = 0;
        String outputLocationName = Constants.DEFAULT_LOCATION_NAME;

        if (method == Constants.LOCATION_METHOD_GOOGLE) {
            Geocoder geocoder = new Geocoder(context, new Locale(language));
            List<Address> addresses;
            try {
                addresses = geocoder.getFromLocationName(locationName + ", " + countryCode, 1);
                if (addresses.size() > 0) {
                    Address address = addresses.get(0);
                    latitude = address.getLatitude();
                    longitude = address.getLongitude();
                    outputLocationName = address.getLocality() == null ? address.getAdminArea() : address.getLocality();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            locationName = locationName.replaceAll(" ", "%20");
            String nominatim = getResponseFromURL("https://nominatim.openstreetmap.org/search?format=json&polygon=0&addressdetails=1&countrycodes=" + countryCode + "&accept-language=" + language + "&q=" + locationName, false);

            try {
                JSONArray jsonResultsArray = new JSONArray(nominatim);
                if (jsonResultsArray.length() > 0) {
                    JSONObject jsonResult = jsonResultsArray.getJSONObject(0);
                    latitude = jsonResult.optDouble("lat");
                    longitude = jsonResult.optDouble("lon");
                    JSONObject jsonAddress = jsonResult.getJSONObject("address");
                    outputLocationName = jsonAddress.optString("city");
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        int timezoneOffset = getTimeZoneOffset(latitude, longitude);

        return new String[]{outputLocationName, formatCoordinate(latitude), formatCoordinate(longitude), formatTimeZone(timezoneOffset)};
    }

    private static final float LOW_PASS_ALPHA = 0.33f; /* 0 ≤ α ≤ 1 ; a smaller value basically means more smoothing */

    static float[] applyLowPassFilter(float[] input, float[] output) {
        if (output == null) return input;

        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + LOW_PASS_ALPHA * (input[i] - output[i]);
        }

        return output;
    }

    /**
     * return [-180, 0] : counter clockwise, [0, 180] : clockwise
     */
    static double shortestAngleDegrees(double angleFrom, double angleTo) {
        return (angleTo - angleFrom + 540) % 360 - 180;
    }

    static double shadowAzimuth(double sunAzimuth) {
        return (Math.toDegrees(sunAzimuth) + 180) % 360;
    }

    /**
     * @param smallestAngle
     * @return sunRightLeft, sunFrontBehind, shadowRightLeft, shadowFrontBehind
     */
    static String[] positionSunShadow(double smallestAngle) {

        if (smallestAngle == 0)
            return new String[]{Constants.POSITION_NONE, Constants.POSITION_FRONT, Constants.POSITION_NONE, Constants.POSITION_BEHIND};
        else if (smallestAngle == -Math.PI)
            return new String[]{Constants.POSITION_NONE, Constants.POSITION_BEHIND, Constants.POSITION_NONE, Constants.POSITION_FRONT};
        else if (smallestAngle == Math.PI / 2)
            return new String[]{Constants.POSITION_RIGHT, Constants.POSITION_NONE, Constants.POSITION_LEFT, Constants.POSITION_NONE};
        else if (smallestAngle == -Math.PI / 2)
            return new String[]{Constants.POSITION_LEFT, Constants.POSITION_NONE, Constants.POSITION_RIGHT, Constants.POSITION_NONE};
        else if (smallestAngle < 0) {
            if (smallestAngle < -Math.PI / 2)
                return new String[]{Constants.POSITION_LEFT, Constants.POSITION_BEHIND, Constants.POSITION_RIGHT, Constants.POSITION_FRONT};
            else
                return new String[]{Constants.POSITION_LEFT, Constants.POSITION_FRONT, Constants.POSITION_RIGHT, Constants.POSITION_BEHIND};
        } else {
            if (smallestAngle < Math.PI / 2)
                return new String[]{Constants.POSITION_RIGHT, Constants.POSITION_FRONT, Constants.POSITION_LEFT, Constants.POSITION_BEHIND};
            else
                return new String[]{Constants.POSITION_RIGHT, Constants.POSITION_BEHIND, Constants.POSITION_LEFT, Constants.POSITION_FRONT};
        }
    }

    static void hideKeyboard(Context context, View view) {
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    static Dialog createLocationDialog(final Activity context, final String language) {

        final Dialog dialog = new Dialog(context, R.style.DialogStyle);
        dialog.setContentView(R.layout.dialog_location);

        final EditText editLocationName = dialog.findViewById(R.id.dialog_localtion_name);
        final EditText editCountryCode = dialog.findViewById(R.id.dialog_country_code);
        final EditText editAccuracy = dialog.findViewById(R.id.dialog_accuracy);

        final Button buttonSearchName = dialog.findViewById(R.id.dialog_button_search_name);
        buttonSearchName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(context, v);

                RadioButton radioGoogle = dialog.findViewById(R.id.dialog_radio_google);
                int method = radioGoogle.isChecked() ? Constants.LOCATION_METHOD_GOOGLE : Constants.LOCATION_METHOD_OSM;

                new AsyncLocationByName(context, editLocationName.getText().toString(), editCountryCode.getText().toString(), method, language).execute();
            }
        });

        final Button buttonSearchCurrent = dialog.findViewById(R.id.dialog_button_current);
        buttonSearchCurrent.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                hideKeyboard(context, v);

                if (EasyPermissions.hasPermissions(context, Manifest.permission.ACCESS_FINE_LOCATION)) {

                    Variables.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                    boolean gpsEnabled = Variables.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    boolean networkEnabled = Variables.locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                    if (!gpsEnabled && !networkEnabled) {
                        AlertDialog.Builder dialogLocationSettings = new AlertDialog.Builder(context);
                        dialogLocationSettings.setMessage("خدمة تحديد الموقع غير مفعلة.");

                        dialogLocationSettings.setPositiveButton("إعدادات", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                context.startActivity(intent);
                            }
                        });

                        dialogLocationSettings.setNegativeButton("إلغاء", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        dialogLocationSettings.show();
                    } else {
                        Variables.locationAccuracy = Float.parseFloat(editAccuracy.getText().toString());

                        Variables.progressFindCurrentLocation = new ProgressDialog(context) {
                            @Override
                            public void onBackPressed() {
                                finishCurrentLocationSearch(context);
                            }
                        };

                        Variables.progressFindCurrentLocation.setMessage("يرجى الانتظار...");
                        Variables.progressFindCurrentLocation.setCancelable(false);
                        Variables.progressFindCurrentLocation.show();

                        Variables.locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, (LocationListener) context);
                        Variables.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) context);
                    }
                } else {
                    EasyPermissions.requestPermissions(context, "", Constants.REQUEST_CODE_PERMISSIONS, Manifest.permission.ACCESS_FINE_LOCATION);
                }
            }
        });

        return dialog;
    }

    static void finishCurrentLocationSearch(Context context) {

        if (Variables.locationManager != null)
            Variables.locationManager.removeUpdates((LocationListener) context);

        if (Variables.progressFindCurrentLocation.isShowing()) {
            Variables.progressFindCurrentLocation.dismiss();
        }
    }

    static boolean copyAssetsFile(Context context, String assetFileName, String destinationFileFullName, boolean overwrite) {

        File file = new File(destinationFileFullName);

        if (overwrite || !file.exists()) {
            try {
                InputStream input = context.getAssets().open(assetFileName);
                OutputStream output = new FileOutputStream(destinationFileFullName);

                byte[] buffer = new byte[1024];
                int length;
                while ((length = input.read(buffer)) > 0) {
                    output.write(buffer, 0, length);
                }

                output.flush();
                output.close();
                input.close();

                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        else {
            return false;
        }
    }

    static boolean writeToTextFile(String filename, String text) {

        try {

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filename));
            bufferedWriter.write(text);
            bufferedWriter.close();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    static String readFromTextFile(String filename) {

        String output;
        try {

            BufferedReader bufferedReader = new BufferedReader(new FileReader(filename));

            String line;
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }

            bufferedReader.close();

            output = stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return output;
    }

    static boolean deleteFavorite(Context context, String name) {
        File file = new File(Variables.getFullPathFav(context, name));
        return file.delete();
    }

    static boolean deleteWidget(Context context, int appWidgetId) {
        File file = new File(Variables.getFullPathWg(context, appWidgetId));
        return file.delete();
    }

    static String readAssetsTextFile(Context context, String fileName) {

        String output = null;

        try {
            InputStream inputStream = context.getAssets().open(fileName);
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();
            output = new String(buffer);

        } catch (IOException e) {

        }

        return output;
    }

    static DayInfoMessagesOutput dayHijriInfoMessages(
            int dayOfWeek, int monthOfYear, int dayOfMonth,
            boolean appendDayOfWeek, boolean appendDayOfMonth) {

        DayInfoMessagesOutput output = new DayInfoMessagesOutput();

        if (dayOfMonth > 0) {

            if (appendDayOfWeek) {

                switch (dayOfWeek) {
                    case Calendar.FRIDAY:
                        output.messagesNas++;
                        output.dayOfWeekColor = Constants.WG_TEXTCOLOR_GREEN;
                        output.messagesBuilder.append(Constants.NEW_LINE).append("◄ الْجُمُعَة: أفضل الأيام، يوم عيد، لا يُخصّ بالصيام ولا بالقيام، ساعة الاستجابة.");
                        break;
                    case Calendar.SATURDAY:
                        output.messagesNas++;
                        output.messagesBuilder.append(Constants.NEW_LINE).append("◄ السَّبْت: النهي عن صيامه إلا فيما افتُرِض (إن صح).");
                        break;
                    case Calendar.MONDAY:
                    case Calendar.THURSDAY:
                        output.messagesNas++;
                        output.dayOfWeekColor = Constants.WG_TEXTCOLOR_BLUE;
                        output.messagesBuilder.append(Constants.NEW_LINE).append("◄ الاثنين وَالْخَمِيس: تُعرض فيهما الأعمال، يُستحب صيامهما إلا ما وافق العيدين.");
                        break;
                    case Calendar.WEDNESDAY:
                        output.messagesNas++;
                        output.messagesBuilder.append(Constants.NEW_LINE).append("◄ الأربعاء: تحري الدعاء بين صلاتي الظهر والعصر (إن صح).");
                        break;
                }

                switch (dayOfWeek) {
                    case Calendar.FRIDAY:
                    case Calendar.SATURDAY:
                    case Calendar.SUNDAY:
                    case Calendar.WEDNESDAY:
                        output.messagesNas++;
                        output.messagesBuilder.append(Constants.NEW_LINE).append("◄ الْجُمُعَة والسبت والأحد والأربعاء: اجتناب الحجامة (إن صح).");
                        break;
                    case Calendar.MONDAY:
                    case Calendar.THURSDAY:
                    case Calendar.TUESDAY:
                        output.messagesNas++;
                        output.messagesBuilder.append(Constants.NEW_LINE).append("◄ الاثنين والثلاثاء وَالْخَمِيس: الحجامة (إن صح).");
                        break;
                }

                if (dayOfWeek == Calendar.THURSDAY) {
                    output.messagesNas++;
                    output.messagesBuilder.append(Constants.NEW_LINE).append("◄ الخميس: استحباب السفر فيه.");
                }
            }

            if (appendDayOfMonth) {

                switch (dayOfMonth) {
                    case 13:
                    case 14:
                    case 15:
                        output.messagesNas++;
                        output.dayOfMonthColor = Constants.WG_TEXTCOLOR_BLUE;
                        output.messagesBuilder.append(Constants.NEW_LINE).append("◄ 13 وَ14 وَ15 (أَيَّامُ الْبِيض): يُستحب صيامها إلا 13 ذو الحجة (من أيام التشريق).");
                        break;
                    case 17:
                    case 19:
                    case 21:
                        output.messagesNas++;
                        output.dayOfMonthColor = Constants.WG_TEXTCOLOR_BLUE;
                        output.messagesBuilder.append(Constants.NEW_LINE).append("◄ 17 وَ19 وَ21: خير أيام الحجامة؛ تُفطّر الصائم؟");
                        break;
                    case 29:
                        output.messagesNas++;
                        output.dayOfMonthColor = Constants.WG_TEXTCOLOR_BLUE;
                        output.messagesBuilder.append(Constants.NEW_LINE).append("◄ 29: مراقبة الهلال بعد غروب شمسه.");
                        break;
                }

                switch (monthOfYear) {

                    case 1:
                        output.messagesNas++;
                        output.monthNameColor = Constants.WG_TEXTCOLOR_ORANGE;
                        output.messagesBuilder.append(Constants.NEW_LINE).append("◄ الْمُحَرَّم: ثالث الأشهر الحرم، أفضل الصيام بعد رمضان.");

                        if (dayOfMonth == 9) {
                            output.messagesNas++;
                            output.dayOfMonthColor = Constants.WG_TEXTCOLOR_BLUE;
                            output.messagesBuilder.append(Constants.NEW_LINE).append("◄ 9 الْمُحَرَّم: يستحب صيامه مع العاشر (عاشوراء).");
                        } else if (dayOfMonth == 10) {
                            output.messagesNas++;
                            output.dayOfMonthColor = Constants.WG_TEXTCOLOR_GREEN;
                            output.messagesBuilder.append(Constants.NEW_LINE).append("◄ 10 الْمُحَرَّم (عَاشُورَاء): صيامه يكفر سنة قبله.");
                        }
                        break;

                    case 3:
                        output.messagesOther++;
                        output.messagesBuilder.append(Constants.NEW_LINE).append("• رَبِيعٌ الْأَوَّل: مولد وهجرة ووفاة رسول الله صلى الله عليه وسلم. الاحتفال بمولده بدعة.");
                        break;

                    case 7:
                        output.messagesNas++;
                        output.monthNameColor = Constants.WG_TEXTCOLOR_ORANGE;
                        output.messagesBuilder.append(Constants.NEW_LINE).append("◄ رَجَب: رابع الأشهر الحرم.");
                        break;

                    case 8:
                        output.messagesNas++;
                        output.monthNameColor = Constants.WG_TEXTCOLOR_BLUE;
                        output.messagesBuilder.append(Constants.NEW_LINE).append("◄ شَعْبَان: شهر يُغفل عنه، تُرفع فيه الأعمال إلى الله، الإكثار من صيامه.");

                        if (dayOfMonth == 15) {
                            output.messagesNas++;
                            output.dayOfMonthColor = Constants.WG_TEXTCOLOR_PURPLE;
                            output.messagesBuilder.append(Constants.NEW_LINE).append("◄ 15 شَعْبَان (لَيْلَةُ النِّصْف): المغفرة إلا لمشرك أو مشاحن (إن صح).");
                        }

                        if (dayOfMonth >= 16) {
                            output.messagesNas++;
                            output.dayOfMonthColor = Constants.WG_TEXTCOLOR_PURPLE;
                            output.messagesBuilder.append(Constants.NEW_LINE).append("◄ 16+ شَعْبَان (النِّصْفُ الثَّانِي): النهي عن صومه (إن صح).");

                            if (dayOfMonth >= 28) {
                                output.messagesNas++;
                                output.dayOfMonthColor = Constants.WG_TEXTCOLOR_RED;
                                output.messagesBuilder.append(Constants.NEW_LINE).append("◄ 28+ شَعْبَان (يوم أو يومان قبل رمضان على نقصان الشهر): النهي عن صومها.");

                                if (dayOfMonth >= 29) {
                                    output.messagesNas++;
                                    output.dayOfMonthColor = Constants.WG_TEXTCOLOR_RED;
                                    output.messagesBuilder.append(Constants.NEW_LINE).append("◄ 29+ شَعْبَان (يوم أو يومان قبل رمضان على كمال الشهر): النهي عن صومها.");
                                }
                            }
                        }
                        break;

                    case 9:
                        output.messagesNas++;
                        output.monthNameColor = Constants.WG_TEXTCOLOR_GREEN;
                        output.messagesBuilder.append(Constants.NEW_LINE).append("◄ رَمَضَان: شهر فُرِض صيامه ورُغِّب في قيامه.");

                        if (dayOfMonth == 1) {
                            output.messagesNas++;
                            output.dayOfMonthColor = Constants.WG_TEXTCOLOR_GREEN;
                            output.messagesBuilder.append(Constants.NEW_LINE).append("◄ 1 رَمَضَان: التهنئة بدخوله.");
                        } else if (dayOfMonth >= 16) {
                            output.messagesNas++;
                            output.dayOfMonthColor = Constants.WG_TEXTCOLOR_BLUE;
                            output.messagesBuilder.append(Constants.NEW_LINE).append("◄ 16+ رَمَضَان (النِّصْفُ الآخِر): القنوت في الوتر.");

                            if (dayOfMonth >= 21) {
                                output.messagesNas++;
                                output.dayOfMonthColor = Constants.WG_TEXTCOLOR_GREEN;
                                output.messagesBuilder.append(Constants.NEW_LINE).append("◄ 21+ رَمَضَان (الْعَشْر الأَوَاخِر على كمال الشهر): إحياء الليل، تحري ليلة القدر.");

                                if (dayOfMonth >= 24) {
                                    output.messagesNas++;
                                    output.messagesBuilder.append(Constants.NEW_LINE).append("◄ 24+ رَمَضَان (السَّبْعُ الأَوَاخِر على كمال الشهر): تحري ليلة القدر.");

                                    if (dayOfMonth >= 28) {
                                        output.messagesNas++;
                                        output.messagesBuilder.append(Constants.NEW_LINE).append("◄ 28+ رَمَضَان (يوم أو يومان قبل الفطر على نقصان الشهر): جواز إخراج زكاة الفطر.");

                                        if (dayOfMonth >= 29) {
                                            output.messagesNas++;
                                            output.messagesBuilder.append(Constants.NEW_LINE).append("◄ 29+ رَمَضَان (يوم أو يومان قبل الفطر على كمال الشهر): جواز إخراج زكاة الفطر.");
                                        }
                                    }

                                    if (dayOfMonth == 24) {
                                        output.messagesNas++;
                                        output.messagesBuilder.append(Constants.NEW_LINE).append("◄ 24 رَمَضَان (سَابِعَةٌ تَبْقَى على كمال الشهر): تحري ليلة القدر.");
                                    } else if (dayOfMonth == 25) {
                                        output.messagesNas++;
                                        output.messagesBuilder.append(Constants.NEW_LINE).append("◄ 25 رَمَضَان (وِتْر، خامسة تبقى على نقصان الشهر): تحري ليلة القدر.");
                                    } else if (dayOfMonth == 26) {
                                        output.messagesNas++;
                                        output.messagesBuilder.append(Constants.NEW_LINE).append("◄ 26 رَمَضَان (خَامِسَةٌ تَبْقَى على كمال الشهر): تحري ليلة القدر.");
                                    } else if (dayOfMonth == 27) {
                                        output.messagesNas++;
                                        output.messagesBuilder.append(Constants.NEW_LINE).append("◄ 27 رَمَضَان (وِتْر، ثالثة تبقى على نقصان الشهر): تحري ليلة القدر، أحرى ليلة.");
                                    } else if (dayOfMonth == 28) {
                                        output.messagesNas++;
                                        output.messagesBuilder.append(Constants.NEW_LINE).append("◄ 28 رَمَضَان (ثَالِثَةٌ تَبْقَى على كمال الشهر): تحري ليلة القدر.");
                                    } else if (dayOfMonth == 29) {
                                        output.messagesNas++;
                                        output.messagesBuilder.append(Constants.NEW_LINE).append("◄ 29 رَمَضَان (وِتْر، آخر ليلة على نقصان الشهر): تحري ليلة القدر.");
                                    } else if (dayOfMonth == 30) {
                                        output.messagesNas++;
                                        output.messagesBuilder.append(Constants.NEW_LINE).append("◄ 30 رَمَضَان (آخِرُ لَيْلَة على كمال الشهر): تحري ليلة القدر.");
                                    }
                                } else {
                                    if (dayOfMonth == 21) {
                                        output.messagesNas++;
                                        output.messagesBuilder.append(Constants.NEW_LINE).append("◄ 21 رَمَضَان (وِتْر، تاسعة تبقى على نقصان الشهر): تحري ليلة القدر.");
                                    } else if (dayOfMonth == 22) {
                                        output.messagesNas++;
                                        output.messagesBuilder.append(Constants.NEW_LINE).append("◄ 22 رَمَضَان (تَاسِعَةٌ تَبْقَى على كمال الشهر): تحري ليلة القدر.");
                                    } else {
                                        output.messagesNas++;
                                        output.messagesBuilder.append(Constants.NEW_LINE).append("◄ 23 رَمَضَان (وِتْر، سابعة تبقى على نقصان الشهر): تحري ليلة القدر.");
                                    }
                                }
                            }
                        }
                        break;

                    case 10:
                        output.messagesNas++;
                        output.monthNameColor = Constants.WG_TEXTCOLOR_BLUE;
                        output.messagesBuilder.append(Constants.NEW_LINE).append("◄ شَوَّال: أول أشهر الحج؛ بطلان التشاؤم به؛ إتباع صيام رمضان بست من شوال.");

                        if (dayOfMonth <= 7) {
                            output.messagesNas++;
                            output.messagesBuilder.append(Constants.NEW_LINE).append("◄ 7..2 شَوَّال (الست الأولى بعد يوم العيد): أفضل صيام الست من شوال.");

                            if (dayOfMonth == 1) {
                                output.messagesNas++;
                                output.dayOfMonthColor = Constants.WG_TEXTCOLOR_GREEN;
                                output.messagesBuilder.append(Constants.NEW_LINE).append("◄ 1 شَوَّال (يَوْمُ الْفِطْر): عيد الفطر، النهي عن صيام يوم العيد، فضيلة إخراج زكاة الفطر والتكبير حتى يخرج الإمام لصلاة العيد.");
                            }
                        }
                        break;

                    case 11:
                        output.messagesNas++;
                        output.monthNameColor = Constants.WG_TEXTCOLOR_ORANGE;
                        output.messagesBuilder.append(Constants.NEW_LINE).append("◄ ذُو الْقَعْدَة: أول الأشهر الحرم وثاني أشهر الحج.");
                        break;

                    case 12:
                        output.messagesNas++;
                        output.monthNameColor = Constants.WG_TEXTCOLOR_ORANGE;
                        output.messagesBuilder.append(Constants.NEW_LINE).append("◄ ذُو الْحِجَّة: ثاني الأشهر الحرم وثالث أشهر الحج.");

                        if (dayOfMonth <= 13) {

                            output.dayOfMonthColor = Constants.WG_TEXTCOLOR_BLUE;
                            output.messagesNas++;
                            output.messagesBuilder.append(Constants.NEW_LINE).append("◄ 13..1 ذُو الْحِجَّة: التكبير المطلق، الإمساك عن الشعر والأظفار للمضحي حتى يضحي.");

                            if (dayOfMonth >= 9) {
                                output.messagesNas++;
                                output.messagesBuilder.append(Constants.NEW_LINE).append("◄ 13..9 ذُو الْحِجَّة: التكبير دبر المكتوبة لغير الحاج من صبح 9 إلى عصر 13.");
                            }

                            if (dayOfMonth >= 10) {
                                output.messagesNas++;
                                output.messagesBuilder.append(Constants.NEW_LINE).append("◄ 13..10 ذُو الْحِجَّة: التكبير دبر المكتوبة للحاج من ظهر 10 إلى عصر 13.");
                            }

                            if (dayOfMonth >= 11) {
                                output.messagesNas++;
                                output.messagesBuilder.append(Constants.NEW_LINE).append("◄ 11 وَ12 وَ13 ذُو الْحِجَّة (أَيَّامُ التَّشْرِيق): أيام عيد وأكل وشرب وذكر للّه؛ النهي عن صيام يوم العيد.");
                            } else {
                                output.messagesNas++;
                                output.messagesBuilder.append(Constants.NEW_LINE).append("◄ 10..1 ذُو الْحِجَّة (الْعَشْرُ الأُوَّل): العمل فيهن أفضل وأحب إلى الله.");

                                if (dayOfMonth == 9) {
                                    output.messagesNas++;
                                    output.dayOfMonthColor = Constants.WG_TEXTCOLOR_GREEN;
                                    output.messagesBuilder.append(Constants.NEW_LINE).append("◄ 9 ذُو الْحِجَّة (يَوْمُ عَرَفَة): صيامه يكفر سنة قبله وسنة بعده.");
                                } else if (dayOfMonth == 10) {
                                    output.messagesNas++;
                                    output.dayOfMonthColor = Constants.WG_TEXTCOLOR_GREEN;
                                    output.messagesBuilder.append(Constants.NEW_LINE).append("◄ 10 ذُو الْحِجَّة (يَوْمُ النَّحْر): عيد الأضحى.");
                                }
                            }
                        }
                        break;
                }
            }
        }

        return output;
    }

    public static void setCenteredTitle(AppCompatActivity context, String title) {

        ActionBar actionBar = context.getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        TextView textView = new TextView(context);
        textView.setText(title);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(Color.WHITE);

        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(textView);
    }

    public static Bitmap createBitmapFromView(final View view) {

        Bitmap bitmap = null;

        try {
            bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            view.draw(canvas);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bitmap;

        /*view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.layout(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
        view.draw(canvas);
        return bitmap;*/

        /*view.setDrawingCacheEnabled(true);
        view.buildDrawingCache(true);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);

        return bitmap;*/
    }

    static String bin2hex(byte[] data) {
        StringBuilder hex = new StringBuilder(data.length * 2);
        for (byte b : data)
            hex.append(String.format("%02x", b & 0xFF));
        return hex.toString();
    }

    static String sha256(String input) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            return bin2hex(digest.digest(input.getBytes("UTF-8")));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    static Dialog createPasswordDialog(final Activity context) {

        final Dialog dialog = new Dialog(context, R.style.DialogStyle);
        dialog.setContentView(R.layout.dialog_password);

        final EditText editPassword = dialog.findViewById(R.id.dialog_password);

        final Button buttonOk = dialog.findViewById(R.id.dialog_button_ok);
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(context, v);

                if (sha256(editPassword.getText().toString()).equals("4be8a58bff282710c080b0031d123a6bab02e999bd6e2228b3dd1d80b3ec7ce3")) {
                    dialog.dismiss();
                } else {
                    Toast.makeText(context, "غير صحيحة", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return dialog;
    }
}

package jalil.prayertimes;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import static android.content.Context.SENSOR_SERVICE;

/**
 * Created by jalil on 22/10/2018.
 */
class Variables {

    static ProgressDialog progressFindCurrentLocation;
    static LocationManager locationManager;
    static float locationAccuracy;

    static boolean hasCompass(Context context) {

        SensorManager sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        boolean hasSystemFeature = context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_COMPASS);

        return hasSystemFeature && sensorManager != null && sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null;
    }

    static DateFormat dateFormatHms = new SimpleDateFormat(Constants.DATE_FORMAT_HMS, Locale.ROOT);
    static DateFormat dateFormatYmd = new SimpleDateFormat(Constants.DATE_FORMAT_YMD, Locale.ROOT);
    static DateFormat dateFormatYmdHms = new SimpleDateFormat(Constants.DATE_FORMAT_YMD_HMS, Locale.ROOT);

    static DecimalFormat decimalFormatterCoordinate = Utilities.getDecimalFormatterHalfUp("+00.000000;-00.000000");
    static DecimalFormat decimalFormatter0 = Utilities.getDecimalFormatterHalfUp(Constants.DECIMAL_FORMAT_0);
    static DecimalFormat decimalFormatter2 = Utilities.getDecimalFormatterHalfUp(Constants.DECIMAL_FORMAT_2);
    static DecimalFormat decimalFormatter3 = Utilities.getDecimalFormatterHalfUp(Constants.DECIMAL_FORMAT_3);

    static int colorTransparent = Color.parseColor(Constants.WG_TEXTCOLOR_TRANSPARENT);
    static int colorWhite = Color.parseColor(Constants.WG_TEXTCOLOR_WHITE);

    static int colorBlue = Color.parseColor(Constants.WG_TEXTCOLOR_BLUE);
    static int colorGreen = Color.parseColor(Constants.WG_TEXTCOLOR_GREEN);
    static int colorOrange = Color.parseColor(Constants.WG_TEXTCOLOR_ORANGE);
    static int colorRed = Color.parseColor(Constants.WG_TEXTCOLOR_RED);
    static int colorYellow = Color.parseColor(Constants.WG_TEXTCOLOR_YELLOW);

    static int colorBlueLight = Color.parseColor(Constants.WG_TEXTCOLOR_BLUE_LIGHT);
    static int colorGreenLight = Color.parseColor(Constants.WG_TEXTCOLOR_GREEN_LIGHT);
    static int colorOrangeLight = Color.parseColor(Constants.WG_TEXTCOLOR_ORANGE_LIGHT);
    static int colorRedLight = Color.parseColor(Constants.WG_TEXTCOLOR_RED_LIGHT);

    static String getCurrentDate() {
        return getCurrentDate(0);
    }

    static String getCurrentDate(int plusDays) {
        Calendar calendar = GregorianCalendar.getInstance();
        if (plusDays != 0) calendar.add(Calendar.DAY_OF_MONTH, plusDays);
        return Variables.dateFormatYmd.format(calendar.getTime());
    }

    static String getCurrentDateTime() {
        return Variables.dateFormatYmdHms.format(GregorianCalendar.getInstance().getTime());
    }

    static String getCurrentDateTime(int offsetMinutes) {
        Calendar calendar = GregorianCalendar.getInstance();
        if (offsetMinutes != 0) calendar.add(Calendar.MINUTE, offsetMinutes);
        return Variables.dateFormatYmdHms.format(calendar.getTime());
    }

    static String getTime(String datetime) {
        return datetime.substring(11);
    }

    static String getDate(String datetime) {
        return datetime.substring(0, 10);
    }

    static int getDayOfWeekInt(String datetime, boolean nextDay) {

        Calendar calendar = GregorianCalendar.getInstance();
        int[] dateComponents = Utilities.dateComponents(datetime);
        calendar.set(dateComponents[0], dateComponents[1] - 1, dateComponents[2]);
        if (nextDay) calendar.add(Calendar.DAY_OF_MONTH, 1);
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    static boolean isDayOfWeek(String date, int calendarDayOfWeek) {

        Calendar calendar = GregorianCalendar.getInstance();
        int[] dateComponents = Utilities.dateComponents(date);
        calendar.set(dateComponents[0], dateComponents[1] - 1, dateComponents[2]);
        return calendar.get(Calendar.DAY_OF_WEEK) == calendarDayOfWeek;
    }

    static String getDayOfWeek(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.FRIDAY:
                return Constants.DAY_OF_WEEK_FRIDAY;
            case Calendar.SATURDAY:
                return Constants.DAY_OF_WEEK_SATURDAY;
            case Calendar.SUNDAY:
                return Constants.DAY_OF_WEEK_SUNDAY;
            case Calendar.MONDAY:
                return Constants.DAY_OF_WEEK_MONDAY;
            case Calendar.TUESDAY:
                return Constants.DAY_OF_WEEK_TUESDAY;
            case Calendar.WEDNESDAY:
                return Constants.DAY_OF_WEEK_WEDNESDAY;
            case Calendar.THURSDAY:
                return Constants.DAY_OF_WEEK_THURSDAY;
            default:
                return Constants.INVALID_STRING;
        }
    }

    static Gson gson = new Gson();
    static Gson gsonPrettyPrinting = new GsonBuilder().setPrettyPrinting().create();

    private static ParseQuery<ParseObject> asyncHijri;
    private static ParseQuery<ParseObject> asyncHijriUpload;

    private static AsyncVersion asyncVersion;
    static void runAsyncVersion(Context context) {
        if (asyncVersion == null || asyncVersion.getStatus() == AsyncTask.Status.FINISHED) {
            asyncVersion = new AsyncVersion(context);
            asyncVersion.execute();
        }
    }

    static void runAsyncHijriAutoUpdate(final Context context) {

        if (asyncHijri == null || !asyncHijri.isRunning()) {

            asyncHijri = ParseQuery.getQuery(Constants.PARSE_CLASS_CALENDAR);

            asyncHijri.whereLessThanOrEqualTo(Constants.PARSE_COLUMN_CALENDAR_DATE_GREG, Variables.getCurrentDate());
            asyncHijri.whereGreaterThanOrEqualTo(Constants.PARSE_COLUMN_CALENDAR_DATE_GREG, Variables.getCurrentDate(-30));
            asyncHijri.orderByDescending(Constants.PARSE_COLUMN_CALENDAR_DATE_GREG);
            asyncHijri.setLimit(1);

            asyncHijri.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null) {
                        if (objects.size() == 1) {

                            ParseObject object = objects.get(0);

                            int hijriMethod = object.getInt(Constants.PARSE_COLUMN_CALENDAR_METHOD_HIJRI);
                            int hijriAdjust = object.getInt(Constants.PARSE_COLUMN_CALENDAR_ADJUST_HIJRI);

                            if (hijriMethod != ActivityMain.prefHijriMethod(context)
                                    || hijriAdjust != ActivityMain.prefHijriAdjustDays(context)) {

                                ActivityMain.updateHijri(context, hijriMethod, hijriAdjust);

                                if (context instanceof IAsyncCompleted) {
                                    IAsyncCompleted iAsyncCompleted = (IAsyncCompleted) context;
                                    iAsyncCompleted.onHijriAutoUpdated(hijriMethod, hijriAdjust);
                                }
                            }
                        }
                    }
                }
            });
        }
    }

    static void runAsyncHijriUpload(final Context context,
                                    final int yearHijri, final int monthHijri,
                                    final String dateGreg, final int methodHijri, final int adjustHijri) {

        if (asyncHijriUpload == null || !asyncHijriUpload.isRunning()) {

            asyncHijriUpload = ParseQuery.getQuery(Constants.PARSE_CLASS_CALENDAR);

            asyncHijriUpload.whereEqualTo(Constants.PARSE_COLUMN_CALENDAR_YEAR_HIJRI, yearHijri);
            asyncHijriUpload.whereEqualTo(Constants.PARSE_COLUMN_CALENDAR_MONTH_HIJRI, monthHijri);

            asyncHijriUpload.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null) {

                        ParseObject entity = objects.size() == 0 ? new ParseObject(Constants.PARSE_CLASS_CALENDAR) : objects.get(0);

                        entity.put(Constants.PARSE_COLUMN_CALENDAR_DATE_GREG, dateGreg);
                        entity.put(Constants.PARSE_COLUMN_CALENDAR_METHOD_HIJRI, methodHijri);
                        entity.put(Constants.PARSE_COLUMN_CALENDAR_ADJUST_HIJRI, adjustHijri);
                        entity.put(Constants.PARSE_COLUMN_CALENDAR_YEAR_HIJRI, yearHijri);
                        entity.put(Constants.PARSE_COLUMN_CALENDAR_MONTH_HIJRI, monthHijri);

                        entity.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    if (context instanceof IAsyncCompleted) {
                                        IAsyncCompleted iAsyncCompleted = (IAsyncCompleted) context;
                                        iAsyncCompleted.onHijriUploaded();
                                    }
                                } else {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } else {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private static String directoryRootPath(Context context) {

        File directory = context.getExternalFilesDir(null);

        if (directory == null) return null;

        if (!directory.exists()) {
            directory.mkdirs();
        }
        return directory.getAbsolutePath();
    }

    private static File directory(Context context, String relativePath) {

        File directory = new File(directoryRootPath(context), relativePath);

        if (!directory.exists()) directory.mkdirs();

        return directory;
    }

    private static File directoryDb(Context context) {

        return directory(context, Constants.DIR_NAME_DB);
    }

    private static File directoryFav(Context context) {
        return directory(context, Constants.DIR_NAME_FAV);
    }

    private static File directoryShot(Context context) {
        return directory(context, Constants.DIR_NAME_SHOT);
    }

    private static File directoryWg(Context context) {
        return directory(context, Constants.DIR_NAME_WG);
    }

    private static String getFullPathDb(Context context, String fileName) {

        return Utilities.pathCombine(directoryDb(context), fileName);
    }

    static String getFullPathFav(Context context, String fileName) {

        return Utilities.pathCombine(directoryFav(context), fileName);
    }

    static String getFullPathShot(Context context, String fileName) {

        return Utilities.pathCombine(directoryShot(context), fileName);
    }

    static String getFullPathWg(Context context, int appWidgetId) {

        return Utilities.pathCombine(directoryWg(context), appWidgetId + "");
    }

    static String[] getFavList(Context context) {

        String[] favList = directoryFav(context).list();

        String[] files = new String[1 + favList.length];
        files[0] = Constants.FAV_DEFAULT;

        System.arraycopy(favList, 0, files, 1, favList.length);

        return files;
    }

    static String widgetCellText(String title, String value) {
        return String.format(Locale.ROOT, "%s%s%s", title, Constants.NEW_LINE, value);
    }

    static String getFullNameDb(Context context, String dbFileName) {
        return getFullPathDb(context, dbFileName);
    }
}

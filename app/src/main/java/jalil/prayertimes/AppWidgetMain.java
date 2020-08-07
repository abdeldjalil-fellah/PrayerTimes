package jalil.prayertimes;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.graphics.Color;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.text.Html;
import android.text.Spanned;
import android.util.TypedValue;
import android.widget.RemoteViews;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import static android.content.Context.SENSOR_SERVICE;
import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_HIGH;
import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM;
import static android.hardware.SensorManager.SENSOR_STATUS_UNRELIABLE;

/**
 * Implementation of Global AppWidgetMain functionality.
 */
public class AppWidgetMain extends AppWidgetProvider implements SensorEventListener {

    private static final String INTENT_ACTION_MESSAGE = "INTENT_ACTION_MESSAGE";
    private static final String INTENT_ACTION_SHADOW = "INTENT_ACTION_SHADOW";
    private static final String INTENT_ACTION_PERIOD = "INTENT_ACTION_PERIOD";
    private static final String INTENT_ACTION_MOON_COORDINATES = "INTENT_ACTION_MOON_COORDINATES";
    private static final String INTENT_ACTION_MOON_EVENTS = "INTENT_ACTION_MOON_EVENTS";
    private static final String INTENT_ACTION_SUN = "INTENT_ACTION_SUN";
    private static final String INTENT_ACTION_QIBLA = "INTENT_ACTION_QIBLA";

    private static final String INTENT_ACTION_COMPASS = "INTENT_ACTION_COMPASS";
    private static final String INTENT_EXTRA_COMPASS_LATITUDE = "INTENT_EXTRA_COMPASS_LATITUDE";
    private static final String INTENT_EXTRA_COMPASS_LONGITUDE = "INTENT_EXTRA_COMPASS_LONGITUDE";

    private static final String INTENT_ACTION_DATE = "INTENT_ACTION_DATE";
    private static final String INTENT_EXTRA_DATE_SIGN = "INTENT_EXTRA_DATE_SIGN";

    private static final String INTENT_ACTION_TIME = "INTENT_ACTION_TIME";
    private static final String INTENT_EXTRA_TIME_NAME = "INTENT_EXTRA_TIME_NAME";
    private static final String INTENT_EXTRA_TIME_TIME = "INTENT_EXTRA_TIME_TIME";
    private static final String INTENT_EXTRA_TIME_NOW = "INTENT_EXTRA_TIME_NOW";
    private static final String INTENT_EXTRA_TIME_DATETIME = "INTENT_EXTRA_TIME_DATETIME";
    private static final String INTENT_EXTRA_TIME_IQAMA = "INTENT_EXTRA_TIME_IQAMA";
    private static final String INTENT_EXTRA_TIME_SECONDS = "INTENT_EXTRA_TIME_SECONDS";

    Context context;

    static SensorManager sensorManager;
    static GeomagneticField geomagneticField = null;

    static float compassTrueAzimuth = 0;
    static boolean hasRotationVector = false;
    static int compassAccuracy = SENSOR_STATUS_UNRELIABLE;

    static PendingIntent pendingIntentActivity(Context context, int appWidgetId, Class activityClass) {
        Intent intent = new Intent(context, activityClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))); // ensure app_widget_main identity
        return PendingIntent.getActivity(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    static PendingIntent pendingIntentDialog(Context context, int appWidgetId, String title, String message) {
        Intent intent = new Intent(context, ActivityDialog.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.putExtra(ActivityDialog.EXTRA_DIALOG_TITLE, title);
        intent.putExtra(ActivityDialog.EXTRA_DIALOG_MESSAGE, message);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))); // ensure app_widget_main identity
        return PendingIntent.getActivity(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    static PendingIntent pendingIntentCompass(Context context, int appWidgetId, double latitude, double longitude) {
        Intent intent = new Intent(context, AppWidgetMain.class);
        intent.setAction(INTENT_ACTION_COMPASS);
        intent.putExtra(INTENT_EXTRA_COMPASS_LATITUDE, latitude);
        intent.putExtra(INTENT_EXTRA_COMPASS_LONGITUDE, longitude);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))); // ensure app_widget_main identity
        return PendingIntent.getBroadcast(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    static PendingIntent pendingIntentDate(Context context, int appWidgetId, int sign) {
        Intent intent = new Intent(context, AppWidgetMain.class);
        intent.setAction(INTENT_ACTION_DATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.putExtra(INTENT_EXTRA_DATE_SIGN, sign);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))); // ensure app_widget_main identity
        return PendingIntent.getBroadcast(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    static PendingIntent pendingIntentTime(Context context, int appWidgetId, String name, String time, int iq, boolean now, String datetime, boolean withSeconds) {
        Intent intent = new Intent(context, AppWidgetMain.class);
        intent.setAction(INTENT_ACTION_TIME);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.putExtra(INTENT_EXTRA_TIME_NAME, name);
        intent.putExtra(INTENT_EXTRA_TIME_TIME, time);
        intent.putExtra(INTENT_EXTRA_TIME_NOW, now);
        intent.putExtra(INTENT_EXTRA_TIME_DATETIME, datetime);
        intent.putExtra(INTENT_EXTRA_TIME_IQAMA, iq);
        intent.putExtra(INTENT_EXTRA_TIME_SECONDS, withSeconds);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))); // ensure app_widget_main identity
        return PendingIntent.getBroadcast(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    static Intent intentUpdate(Context context, int[] appWidgetIds) {
        Intent intent = new Intent(context, AppWidgetMain.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))); // ensure app_widget_main identity
        return intent;
    }

    static PendingIntent pendingIntent(Context context, int appWidgetId, String action) {
        Intent intent = new Intent(context, AppWidgetMain.class);
        intent.setAction(action);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))); // ensure app_widget_main identity
        return PendingIntent.getBroadcast(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    static void setTextSize(RemoteViews views, boolean longFormat, int sizePercent) {

        float sizeMultiplier = (float) (sizePercent / 100.0);

        float sizeHeader = 24 * sizeMultiplier;
        float sizeHeaderMessage = longFormat ? 22 * sizeMultiplier : sizeHeader;

        float sizeCell = (float) 16 * sizeMultiplier;
        float sizeFooter = 21 * sizeMultiplier;

        views.setTextViewTextSize(R.id.wg_time, TypedValue.COMPLEX_UNIT_SP, sizeHeader);
        views.setTextViewTextSize(R.id.wg_message, TypedValue.COMPLEX_UNIT_SP, sizeHeaderMessage);

        views.setTextViewTextSize(R.id.wg_cell_01_00, TypedValue.COMPLEX_UNIT_SP, sizeCell);
        views.setTextViewTextSize(R.id.wg_cell_01_01, TypedValue.COMPLEX_UNIT_SP, sizeCell);
        views.setTextViewTextSize(R.id.wg_cell_01_02, TypedValue.COMPLEX_UNIT_SP, sizeCell);
        views.setTextViewTextSize(R.id.wg_cell_01_03, TypedValue.COMPLEX_UNIT_SP, sizeCell);
        views.setTextViewTextSize(R.id.wg_cell_01_04, TypedValue.COMPLEX_UNIT_SP, sizeCell);

        views.setTextViewTextSize(R.id.wg_cell_02_00, TypedValue.COMPLEX_UNIT_SP, sizeCell);
        views.setTextViewTextSize(R.id.wg_cell_02_01, TypedValue.COMPLEX_UNIT_SP, sizeCell);
        views.setTextViewTextSize(R.id.wg_cell_02_02, TypedValue.COMPLEX_UNIT_SP, sizeCell);
        views.setTextViewTextSize(R.id.wg_cell_02_03, TypedValue.COMPLEX_UNIT_SP, sizeCell);
        views.setTextViewTextSize(R.id.wg_cell_02_04, TypedValue.COMPLEX_UNIT_SP, sizeCell);

        views.setTextViewTextSize(R.id.wg_cell_03_00, TypedValue.COMPLEX_UNIT_SP, sizeCell);
        views.setTextViewTextSize(R.id.wg_cell_03_01, TypedValue.COMPLEX_UNIT_SP, sizeCell);
        views.setTextViewTextSize(R.id.wg_cell_03_02, TypedValue.COMPLEX_UNIT_SP, sizeCell);
        views.setTextViewTextSize(R.id.wg_cell_03_03, TypedValue.COMPLEX_UNIT_SP, sizeCell);
        views.setTextViewTextSize(R.id.wg_cell_03_04, TypedValue.COMPLEX_UNIT_SP, sizeCell);

        views.setTextViewTextSize(R.id.wg_cell_04_00, TypedValue.COMPLEX_UNIT_SP, sizeCell);
        views.setTextViewTextSize(R.id.wg_cell_04_01, TypedValue.COMPLEX_UNIT_SP, sizeCell);
        views.setTextViewTextSize(R.id.wg_cell_04_02, TypedValue.COMPLEX_UNIT_SP, sizeCell);
        views.setTextViewTextSize(R.id.wg_cell_04_03, TypedValue.COMPLEX_UNIT_SP, sizeCell);
        views.setTextViewTextSize(R.id.wg_cell_04_04, TypedValue.COMPLEX_UNIT_SP, sizeCell);

        views.setTextViewTextSize(R.id.wg_cell_05_00, TypedValue.COMPLEX_UNIT_SP, sizeCell);
        views.setTextViewTextSize(R.id.wg_cell_05_01, TypedValue.COMPLEX_UNIT_SP, sizeCell);
        views.setTextViewTextSize(R.id.wg_cell_05_02, TypedValue.COMPLEX_UNIT_SP, sizeCell);
        views.setTextViewTextSize(R.id.wg_cell_05_03, TypedValue.COMPLEX_UNIT_SP, sizeCell);
        views.setTextViewTextSize(R.id.wg_cell_05_04, TypedValue.COMPLEX_UNIT_SP, sizeCell);

        views.setTextViewTextSize(R.id.wg_cell_06_00, TypedValue.COMPLEX_UNIT_SP, sizeCell);
        views.setTextViewTextSize(R.id.wg_cell_06_02, TypedValue.COMPLEX_UNIT_SP, sizeCell);
        views.setTextViewTextSize(R.id.wg_cell_06_03, TypedValue.COMPLEX_UNIT_SP, sizeCell);
        views.setTextViewTextSize(R.id.wg_cell_06_04, TypedValue.COMPLEX_UNIT_SP, sizeCell);

        views.setTextViewTextSize(R.id.wg_date_hijri, TypedValue.COMPLEX_UNIT_SP, sizeFooter);
        views.setTextViewTextSize(R.id.wg_date_greg, TypedValue.COMPLEX_UNIT_SP, sizeFooter);
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget_main);

        boolean hasCompass = Variables.hasCompass(context);

        WidgetConfiguration configuration = WidgetConfiguration.fromConfigurationFile(context, appWidgetId);

        int[] iqama = Utilities.iqamaArrayOf(configuration.cLocationIqama);
        int[] offset = Utilities.offsetArrayOf(configuration.cLocationOffset);

        int cHijriMethod = ActivityMain.prefHijriMethod(context);
        int cHijriAdjust = ActivityMain.prefHijriAdjustDays(context);
        boolean cQiblaAngle = configuration.swQibla == WidgetConfiguration.SWITCH_QIBLA_ANGLE;
        boolean cShadowLength = configuration.swShadow == WidgetConfiguration.SWITCH_SHADOW_LENGTH;
        boolean cIshaEndMid = configuration.pIshaEnd == WidgetConfiguration.ISHA_END_MID;

        setTextSize(views, configuration.wLongFormat, configuration.wTextSizePercent);

        String datetime;
        boolean cNow = configuration.wDateTimeNow;

        if (cNow) {
            datetime = Variables.getCurrentDateTime();
        } else {
            datetime = configuration.wDateTimeCustom;
        }

        String date = Variables.getDate(datetime);
        String time = Variables.getTime(datetime);

        boolean isFriday = Variables.isDayOfWeek(datetime, Calendar.FRIDAY);

        String vDhuhrTitle = isFriday ? Constants.TITLE_JUMUAA : Constants.TITLE_DHUHR;

        double latitude = Utilities.valueOf(configuration.cLocationCalcParams, 0, 10, true);
        double longitude = Utilities.valueOf(configuration.cLocationCalcParams, 1, 10, true);
        TimeZone timezone = Utilities.timezoneCalcCity(configuration.cLocationCalcParams.substring(22));

        DecimalFormat decimalFormatter = configuration.wLongFormat ? Variables.decimalFormatter3 : Variables.decimalFormatter2;

        SunMoonCalculator smc = null;
        Calendar calendarUTC = Utilities.toUTC(datetime, timezone);

        try {
            smc = new SunMoonCalculator
                    (
                            calendarUTC.get(Calendar.YEAR), calendarUTC.get(Calendar.MONTH) + 1, calendarUTC.get(Calendar.DAY_OF_MONTH),
                            calendarUTC.get(Calendar.HOUR_OF_DAY), calendarUTC.get(Calendar.MINUTE), calendarUTC.get(Calendar.SECOND),
                            longitude * SunMoonCalculator.DEG_TO_RAD, latitude * SunMoonCalculator.DEG_TO_RAD, 0
                    );

            smc.calcSunAndMoon();
        } catch (Exception e) {
            e.printStackTrace();
        }

        double[] culmination = smc.getCulmination(true, false);

        String vSun;
        String vSunTitle;

        switch (configuration.swSun) {
            case WidgetConfiguration.SWITCH_SUN_AZIMUTH:
                vSunTitle = Constants.TITLE_SUN_AZIMUTH;
                vSun = Constants.SYMBOL_DEGREES + decimalFormatter.format(smc.sun.azimuth * SunMoonCalculator.RAD_TO_DEG);
                break;
            case WidgetConfiguration.SWITCH_SUN_ELEVATION:
                vSunTitle = Constants.TITLE_SUN_ELEVATION;
                String sunAltitudeSign = smc.sun.elevation < 0 ? Constants.SYMBOL_MINUS : Constants.SYMBOL_PLUS;
                vSun = sunAltitudeSign + Constants.SYMBOL_DEGREES + decimalFormatter.format(Math.abs(smc.sun.elevation) * SunMoonCalculator.RAD_TO_DEG);
                break;
            default:
                vSunTitle = Constants.TITLE_SUN_AZIMUTH;
                vSun = Constants.INVALID_STRING;
        }

        String vMoonCoordinates;
        String vMoonCoordinatesTitle;

        switch (configuration.swMoonCoordinates) {
            case WidgetConfiguration.SWITCH_MOON_COORDINATES_AZIMUTH:
                vMoonCoordinatesTitle = Constants.TITLE_MOON_AZIMUTH;
                vMoonCoordinates = Constants.SYMBOL_DEGREES + decimalFormatter.format(smc.moon.azimuth * SunMoonCalculator.RAD_TO_DEG);
                break;
            case WidgetConfiguration.SWITCH_MOON_COORDINATES_ELEVATION:
                vMoonCoordinatesTitle = Constants.TITLE_MOON_ELEVATION;
                String moonAltitudeSign = smc.moon.elevation < 0 ? Constants.SYMBOL_MINUS : Constants.SYMBOL_PLUS;
                vMoonCoordinates = moonAltitudeSign + Constants.SYMBOL_DEGREES + decimalFormatter.format(Math.abs(smc.moon.elevation) * SunMoonCalculator.RAD_TO_DEG);
                break;
            default:
                vMoonCoordinatesTitle = Constants.TITLE_MOON_AZIMUTH;
                vMoonCoordinates = Constants.INVALID_STRING;
        }

        String vMoonEvents;
        String vMoonEventsTitle;

        switch (configuration.swMoonEvents) {
            case WidgetConfiguration.SWITCH_MOON_EVENTS_SET:
                vMoonEventsTitle = Constants.TITLE_MOONSET;
                vMoonEvents = smc.moon.set > 0 ? SunMoonCalculator.getTime(smc.moon.set, timezone, configuration.wLongFormat) : Constants.INVALID_STRING;
                break;
            case WidgetConfiguration.SWITCH_MOON_EVENTS_RISE:
                vMoonEventsTitle = Constants.TITLE_MOONRISE;
                vMoonEvents = smc.moon.rise > 0 ? SunMoonCalculator.getTime(smc.moon.rise, timezone, configuration.wLongFormat) : Constants.INVALID_STRING;
                break;
            default:
                vMoonEventsTitle = Constants.TITLE_MOONSET;
                vMoonEvents = Constants.INVALID_STRING;
        }

        double moonAge = smc.jd_UT - smc.getMoonPhaseTimeLast(SunMoonCalculator.MOONPHASE.NEW_MOON);
        String vMoonAge = Utilities.daysAsString(moonAge);

        String vMoonIlluminationBestObservationTitle, vMoonIlluminationBestObservationTime = Constants.INVALID_STRING;
        int odehVisibility = -1;

        if (moonAge <= 2) {
            double[] odeh = smc.getOdehCriterion();
            odehVisibility = (int) odeh[2];
            vMoonIlluminationBestObservationTitle = Constants.TITLE_MOON_BEST_OBSERVATION_TIME;
            if (odeh[0] > 0) {
                vMoonIlluminationBestObservationTime = SunMoonCalculator.getTime(odeh[0], timezone, configuration.wLongFormat);
            }
        } else {
            vMoonIlluminationBestObservationTitle = Constants.TITLE_MOON_ILLUNINATION;
            String moonIlluminationArrow = smc.jd_UT <= smc.getMoonPhaseTimeClosest(SunMoonCalculator.MOONPHASE.FULL_MOON) ? Constants.SYMBOL_UP : Constants.SYMBOL_DOWN;
            vMoonIlluminationBestObservationTime = moonIlluminationArrow + Constants.SYMBOL_PERCENT + decimalFormatter.format(smc.moon.illuminationPhase);
        }

        double cpFajr = Utilities.valueOf(configuration.pCalcParams, 0, 5, false);
        double cpGhasaq = Utilities.valueOf(configuration.pCalcParams, 1, 5, false);
        double cpIrtifae = Utilities.valueOf(configuration.pCalcParams, 2, 5, false);
        double cpIsfirar = Utilities.valueOf(configuration.pCalcParams, 3, 5, false);
        double cpTakabud = Utilities.valueOf(configuration.pCalcParams, 4, 5, false);
        double cpZawal = Utilities.valueOf(configuration.pCalcParams, 5, 5, false);
        double cpAsrShadowFactor = Utilities.valueOf(configuration.pCalcParams, 6, 5, false);
        double cpLastPortion = Utilities.valueOf(configuration.pCalcParams, 7, 5, false);

        String vIstiwae, vIstiwaeTitle;

        switch (configuration.pIstiwae) {
            default:
            case WidgetConfiguration.ISTIWAE_LATER:
                if (smc.sun.transit >= culmination[0]) {
                    vIstiwae = SunMoonCalculator.getTime(smc.sun.transit, timezone, true);
                    vIstiwaeTitle = Constants.TITLE_SUN_TRANSIT;
                } else {
                    vIstiwae = SunMoonCalculator.getTime(culmination[0], timezone, true);
                    vIstiwaeTitle = Constants.TITLE_SUN_CULMINATION;
                }
                break;
            case WidgetConfiguration.ISTIWAE_TRANSIT:
                vIstiwae = SunMoonCalculator.getTime(smc.sun.transit, timezone, true);
                vIstiwaeTitle = Constants.TITLE_SUN_TRANSIT;
                break;
            case WidgetConfiguration.ISTIWAE_CULMINATION:
                vIstiwae = SunMoonCalculator.getTime(culmination[0], timezone, true);
                vIstiwaeTitle = Constants.TITLE_SUN_CULMINATION;
                break;
        }

        boolean istiwaeCulmination = vIstiwaeTitle.equals(Constants.TITLE_SUN_CULMINATION);

        String vTakabud;
        double takabudRad, takabudAzimuthRad;

        switch (configuration.pTakabud) {
            default:
            case WidgetConfiguration.KARAHA_UNIT_ISTIWAE_MINUTE:
                vTakabud = Utilities.adjustTime(vIstiwae, (int) (-cpTakabud * 60), true);
                break;
            case WidgetConfiguration.KARAHA_UNIT_ISTIWAE_DEGREE:
                takabudRad = cpTakabud * SunMoonCalculator.DEG_TO_RAD;
                takabudAzimuthRad = istiwaeCulmination ? culmination[2] - takabudRad : Math.PI - takabudRad;
                vTakabud = SunMoonCalculator.getTime(smc.getAzimuthTime(true, true, takabudAzimuthRad), timezone, true);
                break;

            case WidgetConfiguration.KARAHA_UNIT_ISTIWAE_DIAMETER:

                if (istiwaeCulmination) {
                    takabudRad = (2 * smc.sun.angularRadius * cpTakabud) / Math.cos(culmination[1]);
                    takabudAzimuthRad = culmination[2] - takabudRad;
                } else {
                    takabudRad = (2 * smc.sun.angularRadius * cpTakabud) / Math.cos(smc.sun.transitElevation);
                    takabudAzimuthRad = Math.PI - takabudRad;
                }

                vTakabud = SunMoonCalculator.getTime(smc.getAzimuthTime(true, true, takabudAzimuthRad), timezone, true);
                break;

            case WidgetConfiguration.KARAHA_UNIT_ISTIWAE_SHADOW_WIDTH:

                vTakabud = SunMoonCalculator.getTime(smc.getWallTime(cpTakabud, true), timezone, true);
                break;
        }

        String vZawal;
        double zawalRad, zawalAzimuthRad;

        switch (configuration.pZawal) {
            default:
            case WidgetConfiguration.KARAHA_UNIT_ISTIWAE_MINUTE:
                vZawal = Utilities.adjustTime(vIstiwae, (int) (cpZawal * 60), true);
                break;
            case WidgetConfiguration.KARAHA_UNIT_ISTIWAE_DEGREE:
                zawalRad = cpZawal * SunMoonCalculator.DEG_TO_RAD;
                zawalAzimuthRad = istiwaeCulmination ? culmination[2] + zawalRad : Math.PI + zawalRad;
                vZawal = SunMoonCalculator.getTime(smc.getAzimuthTime(true, true, zawalAzimuthRad), timezone, true);
                break;

            case WidgetConfiguration.KARAHA_UNIT_ISTIWAE_DIAMETER:

                if (istiwaeCulmination) {
                    zawalRad = (2 * smc.sun.angularRadius * cpZawal) / Math.cos(culmination[1]);
                    zawalAzimuthRad = culmination[2] + zawalRad;
                } else {
                    zawalRad = (2 * smc.sun.angularRadius * cpZawal) / Math.cos(smc.sun.transitElevation);
                    zawalAzimuthRad = Math.PI + zawalRad;
                }

                vZawal = SunMoonCalculator.getTime(smc.getAzimuthTime(true, true, zawalAzimuthRad), timezone, true);
                break;

            case WidgetConfiguration.KARAHA_UNIT_ISTIWAE_SHADOW_WIDTH:

                vZawal = SunMoonCalculator.getTime(smc.getWallTime(cpZawal, false), timezone, true);
                break;
        }

        // more accurate vZawal = SunMoonCalculator.getTime(smc.getAngularRadiusTimeAzimuth(true, true, culmination[0], culmination[2], angularRadiusMultiplier), timezone, true);

        String vSunrise = SunMoonCalculator.getTime(smc.sun.rise, timezone, configuration.wLongFormat);
        String vSunset = SunMoonCalculator.getTime(smc.sun.set, timezone, configuration.wLongFormat);

        int sunsetSeconds = Utilities.secondsOf(vSunset);
        int sunriseSeconds = Utilities.secondsOf(vSunrise);

        int timeSeconds = Utilities.secondsOf(time);
        int nightEndSecondsSunrise = Constants.SECONDS_IN_DAY + sunriseSeconds;

        int dayLengthSecondsSun = sunsetSeconds - sunriseSeconds;
        int nightLengthSecondsSun = nightEndSecondsSunrise - sunsetSeconds;

        int hourLengthDaySun = dayLengthSecondsSun / 12;
        int hourLengthNightSun = nightLengthSecondsSun / 12;

        double fajr;

        switch (configuration.pFajr) {
            default:
            case WidgetConfiguration.FAJR_ANGLE:
                smc.calcSunAndMoon(-cpFajr);
                fajr = smc.sun.rise;
                break;
            case WidgetConfiguration.FAJR_HOUR:
                fajr = smc.sun.rise - hourLengthNightSun * cpFajr / Constants.SECONDS_IN_DAY;
                break;
            case WidgetConfiguration.FAJR_MOONSIGHTING:
                fajr = MoonsightingShafaq.seasonAdjustedTwilightFajr(latitude, calendarUTC.get(Calendar.DAY_OF_YEAR), calendarUTC.get(Calendar.YEAR), smc.sun.rise);
                break;
        }

        String vFajr = SunMoonCalculator.getTime(fajr, timezone, configuration.wLongFormat);

        double ghasaq;

        switch (configuration.pGhasaq) {
            default:
            case WidgetConfiguration.GHASAQ_ANGLE:
                smc.calcSunAndMoon(-cpGhasaq);
                ghasaq = smc.sun.set;
                break;
            case WidgetConfiguration.GHASAQ_HOUR:
                ghasaq = smc.sun.set + hourLengthNightSun * cpGhasaq / Constants.SECONDS_IN_DAY;
                break;
            case WidgetConfiguration.GHASAQ_MOONSIGHTING_GENERAL:
                ghasaq = MoonsightingShafaq.seasonAdjustedTwilightIshaGeneral(latitude, calendarUTC.get(Calendar.DAY_OF_YEAR), calendarUTC.get(Calendar.YEAR), smc.sun.set);
                break;
            case WidgetConfiguration.GHASAQ_MOONSIGHTING_RED:
                ghasaq = MoonsightingShafaq.seasonAdjustedTwilightIshaAhmer(latitude, calendarUTC.get(Calendar.DAY_OF_YEAR), calendarUTC.get(Calendar.YEAR), smc.sun.set);
                break;
            case WidgetConfiguration.GHASAQ_MOONSIGHTING_WHITE:
                ghasaq = MoonsightingShafaq.seasonAdjustedTwilightIshaAbyadh(latitude, calendarUTC.get(Calendar.DAY_OF_YEAR), calendarUTC.get(Calendar.YEAR), smc.sun.set);
                break;
        }

        String vGhasaq = SunMoonCalculator.getTime(ghasaq, timezone, configuration.wLongFormat);

        String vIrtifae;

        switch (configuration.pIrtifae) {

            default:
            case WidgetConfiguration.KARAHA_UNIT_HORIZON_DEGREE:

                smc.calcSunAndMoon(cpIrtifae);
                vIrtifae = SunMoonCalculator.getTime(smc.sun.rise, timezone, configuration.wLongFormat);
                break;

            case WidgetConfiguration.KARAHA_UNIT_HORIZON_DIAMETER:

                smc.calcSunAndMoon(2 * smc.sun.angularRadius * cpIrtifae * SunMoonCalculator.RAD_TO_DEG);
                vIrtifae = SunMoonCalculator.getTime(smc.sun.rise, timezone, configuration.wLongFormat);
                break;

            case WidgetConfiguration.KARAHA_UNIT_HORIZON_MINUTE:

                vIrtifae = Utilities.adjustTime(vSunrise, (int) (cpIrtifae * 60), configuration.wLongFormat);
                break;

            case WidgetConfiguration.KARAHA_UNIT_HORIZON_HOUR:

                vIrtifae = Utilities.adjustTime(vSunrise, (int) (hourLengthDaySun * cpIrtifae), configuration.wLongFormat);
                break;
        }

        String vIsfirar, vIsfirarTitle = Constants.TITLE_ISFIRAR;

        switch (configuration.pIsfirar) {

            default:
            case WidgetConfiguration.KARAHA_UNIT_HORIZON_DEGREE:

                smc.calcSunAndMoon(cpIsfirar);
                vIsfirar = SunMoonCalculator.getTime(smc.sun.set, timezone, configuration.wLongFormat);
                break;

            case WidgetConfiguration.KARAHA_UNIT_HORIZON_DIAMETER:

                smc.calcSunAndMoon(2 * smc.sun.angularRadius * cpIsfirar * SunMoonCalculator.RAD_TO_DEG);
                vIsfirar = SunMoonCalculator.getTime(smc.sun.set, timezone, configuration.wLongFormat);
                break;

            case WidgetConfiguration.KARAHA_UNIT_HORIZON_MINUTE:

                vIsfirar = Utilities.adjustTime(vSunset, (int) (-cpIsfirar * 60), configuration.wLongFormat);
                break;

            case WidgetConfiguration.KARAHA_UNIT_HORIZON_HOUR:

                vIsfirar = Utilities.adjustTime(vSunset, (int) (-hourLengthDaySun * cpIsfirar), configuration.wLongFormat);
                break;
        }

        String vAsrCalc = Constants.INVALID_STRING;

        switch (configuration.pAsr) {

            case WidgetConfiguration.ASR_SHADOW_LENGTH:

                double zawalShadowFactor = istiwaeCulmination ? 1 / Math.tan(culmination[1]) : 1 / Math.tan(smc.sun.transitElevation);

                smc.calcSunAndMoon(Math.atan2(1, cpAsrShadowFactor + zawalShadowFactor) * SunMoonCalculator.RAD_TO_DEG); /* more precise than Math.atan(1 / (asrShadowFactor + zawalShadow)) */
                vAsrCalc = SunMoonCalculator.getTime(smc.sun.set, timezone, configuration.wLongFormat);

                break;

            case WidgetConfiguration.ASR_SHADOW_WIDTH:

                vAsrCalc = SunMoonCalculator.getTime(smc.getWallTime(cpAsrShadowFactor, false), timezone, configuration.wLongFormat);

                break;

            case WidgetConfiguration.ASR_MID:

                int ss = Utilities.secondsOf(vSunset);
                int tr = Utilities.secondsOf(vIstiwae);

                vAsrCalc = Utilities.timeOf(tr + (ss - tr) / 2, configuration.wLongFormat);

                break;
        }

        smc.calcSunAndMoon();

        String dateHijri = Utilities.toDateHijri(date, cHijriAdjust, cHijriMethod, context, false, null, null);
        String dateHijriCorrected = Utilities.toDateHijri(date, cHijriAdjust, cHijriMethod, context, true, time, vSunset);

        boolean prayersOfShownDate = true;
        String vDateHijri = dateHijri, vDateGreg = date;

        String vSubh, vSubhAwal, vDhuhr, vAsr, vMaghrib, vIsha;

        switch (configuration.pPrayersMethod) {

            default:
            case WidgetConfiguration.PRAYERS_METHOD_ACTUAL:

                try {

                    String[] row = HijriMARW.times(context, configuration.cLocationActualId, date);

                    vSubh = row[HijriMARW.INDEX_PRAYER_SUBH];
                    vDhuhr = row[HijriMARW.INDEX_PRAYER_DHUHR];
                    vAsr = row[HijriMARW.INDEX_PRAYER_ASR];
                    vMaghrib = row[HijriMARW.INDEX_PRAYER_MAGHRIB];
                    vIsha = row[HijriMARW.INDEX_PRAYER_ISHA];

                    if (!date.equals(row[HijriMARW.INDEX_DATE_GREG])) prayersOfShownDate = false;

                } catch (SQLException e) {
                    Utilities.logError(e.getMessage());

                    vSubh = Constants.INVALID_STRING;
                    vDhuhr = Constants.INVALID_STRING;
                    vAsr = Constants.INVALID_STRING;
                    vMaghrib = Constants.INVALID_STRING;
                    vIsha = Constants.INVALID_STRING;
                }
                break;

            case WidgetConfiguration.PRAYERS_METHOD_PRAYTIMES:

                PrayTime secondaryCalc = new PrayTime();

                secondaryCalc.LocationLatitude = latitude;
                secondaryCalc.LocationLongitude = longitude;
                secondaryCalc.LocationTimezone = timezone.getRawOffset() / 3600000.0;

                secondaryCalc.ParamFajrAngle = cpFajr;
                secondaryCalc.ParamAsrShadowFactor = cpAsrShadowFactor;
                secondaryCalc.ParamIshaAngle = cpGhasaq;

                secondaryCalc.setDate(date);
                String[] times = secondaryCalc.getPrayerTimes();

                vSubh = times[PrayTime.PRAYER_INDEX_FAJR];
                vDhuhr = times[PrayTime.PRAYER_INDEX_DHUHR];
                vAsr = times[PrayTime.PRAYER_INDEX_ASR];
                vMaghrib = times[PrayTime.PRAYER_INDEX_MAGHRIB];
                vIsha = times[PrayTime.PRAYER_INDEX_ISHA];

                break;

            case WidgetConfiguration.PRAYERS_METHOD_2ND_LINE:

                vSubh = vFajr;
                vDhuhr = vZawal;
                vAsr = vAsrCalc;
                vMaghrib = vSunset;
                vIsha = vGhasaq;

                break;
        }

        vSubh = Utilities.adjustTime(vSubh, offset[0] * 60, configuration.wLongFormat);
        vSubhAwal = Utilities.adjustTime(vSubh, offset[1] * 60, configuration.wLongFormat);
        vDhuhr = Utilities.adjustTime(vDhuhr, offset[2] * 60, configuration.wLongFormat);
        vAsr = Utilities.adjustTime(vAsr, offset[3] * 60, configuration.wLongFormat);
        vMaghrib = Utilities.adjustTime(vMaghrib, offset[4] * 60, configuration.wLongFormat);
        vIsha = Utilities.adjustTime(vIsha, offset[5] * 60, configuration.wLongFormat);

        int fajrSeconds = Utilities.secondsOf(vFajr);
        int ghasaqSeconds = Utilities.secondsOf(vGhasaq);

        int nightEndSecondsFajr = Constants.SECONDS_IN_DAY + fajrSeconds;

        int dayLengthSecondsSiyam = sunsetSeconds - fajrSeconds;
        int nightLengthSecondsSiyam = nightEndSecondsFajr - sunsetSeconds;
        int nightLengthSecondsQiyam = nightEndSecondsFajr - ghasaqSeconds;

        int lastPortionSeconds;
        int lastThirdSeconds = nightEndSecondsFajr - nightLengthSecondsSiyam / 3;
        int ishaEnd = sunsetSeconds + nightLengthSecondsSiyam / (cIshaEndMid ? 2 : 3);

        switch (configuration.pNightPeriod) {
            default:
            case WidgetConfiguration.NIGHT_PERIOD_SIYAM:
                lastPortionSeconds = nightEndSecondsFajr - (int) (nightLengthSecondsSiyam / cpLastPortion);
                break;

            case WidgetConfiguration.NIGHT_PERIOD_QIYAM:
                lastThirdSeconds = nightEndSecondsFajr - nightLengthSecondsQiyam / 3;
                ishaEnd = ghasaqSeconds + nightLengthSecondsQiyam / (cIshaEndMid ? 2 : 3);
                lastPortionSeconds = nightEndSecondsFajr - (int) (nightLengthSecondsQiyam / cpLastPortion);
                break;

            case WidgetConfiguration.NIGHT_PERIOD_SUN:
                lastPortionSeconds = nightEndSecondsSunrise - (int) (nightLengthSecondsSun / cpLastPortion);
                break;
        }

        String vLastThird = Utilities.timeOf(lastThirdSeconds, configuration.wLongFormat);
        String vLastPortion = Utilities.timeOf(lastPortionSeconds, configuration.wLongFormat);
        String vIshaEnd = Utilities.timeOf(ishaEnd, configuration.wLongFormat);

        String vShadowLastPortion, vShadowLastPortionTitle;

        if (smc.sun.elevation > 0) {

            double shadowFactor;
            String sunShadowArrow = smc.jd_UT <= culmination[0] ? Constants.SYMBOL_DOWN : Constants.SYMBOL_UP;

            if (cShadowLength) {
                // factor as length = 1 / tan(alt)
                vShadowLastPortionTitle = Constants.TITLE_SHADOW_LENGTH;
                shadowFactor = SunMoonCalculator.shadowFactorLength(smc.sun.elevation);
            } else {
                // factor as 180wall width = sin(wallShadowAz) / tan(alt)
                vShadowLastPortionTitle = Constants.TITLE_SHADOW_WIDTH;
                shadowFactor = SunMoonCalculator.shadowFactorWall180(smc.sun.azimuth, smc.sun.elevation);
            }

            vShadowLastPortion = Constants.SYMBOL_MULTIPLY + decimalFormatter.format(shadowFactor) + sunShadowArrow;
        } else {
            vShadowLastPortionTitle = Constants.TITLE_NIGHT_PORTION_LAST;
            vShadowLastPortion = vLastPortion;
        }

        String[] wgMessage = Utilities.wgMessage
                (
                        timeSeconds, vSubh, vSunrise, vDhuhr, vAsr, vMaghrib, vIsha,
                        iqama[0], 0, iqama[1], iqama[2], iqama[3], iqama[4],
                        configuration.wLongFormat, isFriday
                );

        if (!vDateGreg.equals(Constants.INVALID_STRING)) {
            vDateGreg = vDateGreg.replace("-", "/");
        }

        if (!vDateHijri.equals(Constants.INVALID_STRING)) {
            vDateHijri = vDateHijri.replace("-", "/");
        }

        double qiblaAzimuth = EarthCalculation.qiblaDegrees(ActivityMain.prefEarthModel(context), latitude, longitude);

        String vCompassAzimuth = hasCompass ? Constants.SYMBOL_DEGREES + Variables.decimalFormatter0.format(compassTrueAzimuth) : Constants.INVALID_STRING;

        String vQiblaTitle = Constants.TITLE_QIBLA_ANGLE, vQibla = Constants.INVALID_STRING;

        if (cQiblaAngle) {
            vQiblaTitle = Constants.TITLE_QIBLA_ANGLE;
            vQibla = Constants.SYMBOL_DEGREES + decimalFormatter.format(qiblaAzimuth);
        } else {

            try {
                double qibla = 0;

                switch (configuration.swQibla) {
                    case WidgetConfiguration.SWITCH_QIBLA_SUN_FRONT:
                        vQiblaTitle = Constants.TITLE_QIBLA_SUN_FRONT;
                        qibla = smc.getAzimuthTime(true, true, qiblaAzimuth * SunMoonCalculator.DEG_TO_RAD);
                        break;
                    case WidgetConfiguration.SWITCH_QIBLA_SUN_BEHIND:
                        vQiblaTitle = Constants.TITLE_QIBLA_SUN_BEHIND;
                        qibla = smc.getAzimuthTime(true, true, ((qiblaAzimuth + 180) % 360) * SunMoonCalculator.DEG_TO_RAD);
                        break;
                    case WidgetConfiguration.SWITCH_QIBLA_SUN_LEFT:
                        vQiblaTitle = Constants.TITLE_QIBLA_SUN_LEFT;
                        qibla = smc.getAzimuthTime(true, true, ((qiblaAzimuth + 90) % 360) * SunMoonCalculator.DEG_TO_RAD);
                        break;
                    case WidgetConfiguration.SWITCH_QIBLA_MOON_FRONT:
                        vQiblaTitle = Constants.TITLE_QIBLA_MOON_FRONT;
                        qibla = smc.getAzimuthTime(false, true, qiblaAzimuth * SunMoonCalculator.DEG_TO_RAD);
                        break;
                    case WidgetConfiguration.SWITCH_QIBLA_MOON_BEHIND:
                        vQiblaTitle = Constants.TITLE_QIBLA_MOON_BEHIND;
                        qibla = smc.getAzimuthTime(false, true, ((qiblaAzimuth + 180) % 360) * SunMoonCalculator.DEG_TO_RAD);
                        break;
                    case WidgetConfiguration.SWITCH_QIBLA_MOON_LEFT:
                        vQiblaTitle = Constants.TITLE_QIBLA_MOON_LEFT;
                        qibla = smc.getAzimuthTime(false, true, ((qiblaAzimuth + 90) % 360) * SunMoonCalculator.DEG_TO_RAD);
                        break;
                }

                vQibla = qibla == 0 ? Constants.INVALID_STRING : SunMoonCalculator.getTime(qibla, timezone, configuration.wLongFormat);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String vPeriod, vPeriodTitle;
        switch (configuration.swPeriod) {
            default:
            case WidgetConfiguration.SWITCH_PERIOD_DAY_LENGTH_SUN:
                vPeriodTitle = Constants.TITLE_DAY_LENGTH_SUN;
                vPeriod = Utilities.timeOf(dayLengthSecondsSun, configuration.wLongFormat);
                break;
            case WidgetConfiguration.SWITCH_PERIOD_NIGHT_LENGTH_SUN:
                vPeriodTitle = Constants.TITLE_NIGHT_LENGTH_SUN;
                vPeriod = Utilities.timeOf(nightLengthSecondsSun, configuration.wLongFormat);
                break;
            case WidgetConfiguration.SWITCH_PERIOD_DAY_LENGTH_SIYAM:
                vPeriodTitle = Constants.TITLE_DAY_LENGTH_SIYAM;
                vPeriod = Utilities.timeOf(dayLengthSecondsSiyam, configuration.wLongFormat);
                break;
            case WidgetConfiguration.SWITCH_PERIOD_NIGHT_LENGTH_SIYAM:
                vPeriodTitle = Constants.TITLE_NIGHT_LENGTH_SIYAM;
                vPeriod = Utilities.timeOf(nightLengthSecondsSiyam, configuration.wLongFormat);
                break;
            case WidgetConfiguration.SWITCH_PERIOD_DAY_HOUR_LENGTH:
                vPeriodTitle = Constants.TITLE_DAY_HOUR_LENGTH;
                vPeriod = Utilities.timeOf(hourLengthDaySun, configuration.wLongFormat);
                break;
            case WidgetConfiguration.SWITCH_PERIOD_NIGHT_HOUR_LENGTH:
                vPeriodTitle = Constants.TITLE_NIGHT_HOUR_LENGTH;
                vPeriod = Utilities.timeOf(hourLengthNightSun, configuration.wLongFormat);
                break;
        }

        int nextHourRank = -1, nextHourSeconds = -1;
        int timeSecondsAdjusted = timeSeconds >= sunsetSeconds ? timeSeconds : timeSeconds + Constants.SECONDS_IN_DAY;

        boolean inDayTimeSun = timeSeconds >= sunriseSeconds && timeSeconds < sunsetSeconds;
        boolean inNightTimeSun = timeSecondsAdjusted >= sunsetSeconds && timeSecondsAdjusted < nightEndSecondsSunrise;

        boolean inLastThird = timeSeconds < fajrSeconds && timeSecondsAdjusted >= lastThirdSeconds;

        if (inDayTimeSun) {
            nextHourRank = (timeSeconds - sunriseSeconds) / hourLengthDaySun + 2;
            if (nextHourRank >= 13) nextHourRank = 12;

            nextHourSeconds = sunriseSeconds + hourLengthDaySun * (nextHourRank - 1);

        } else if (inNightTimeSun) {
            nextHourRank = (timeSecondsAdjusted - sunsetSeconds) / hourLengthNightSun + 2;
            if (nextHourRank >= 13) nextHourRank = 12;

            nextHourSeconds = sunsetSeconds + hourLengthNightSun * (nextHourRank - 1);
        }

        String vHourTitle = Utilities.getHourName(nextHourRank);
        String vHourMessage = Utilities.getHourMessage(nextHourRank);
        String vHour = Utilities.timeOf(nextHourSeconds, configuration.wLongFormat);

        String vSunsetDayTitle = Constants.TITLE_SUNSET_DAY;

        int[] hijriCorrectedComponents = Utilities.dateComponents(dateHijriCorrected);

        String monthNameHijriCorrected = Utilities.getMonthNameHijri(hijriCorrectedComponents[1]);
        int dayOfWeekCorrectedInt = Variables.getDayOfWeekInt(datetime, !dateHijri.equals(dateHijriCorrected));
        String dayOfWeekCorrected = Variables.getDayOfWeek(dayOfWeekCorrectedInt);

        int[] gregComponents = Utilities.dateComponents(date);

        String monthNameGregorian = Utilities.getMonthNameGregorianAssyrian(gregComponents[1]);
        String dayOfWeekGregorian = Variables.getDayOfWeek(Variables.getDayOfWeekInt(datetime, false));

        DayInfoMessagesOutput dayInfoMessagesOutput = Utilities.dayHijriInfoMessages(dayOfWeekCorrectedInt, hijriCorrectedComponents[1], hijriCorrectedComponents[2], true, true);

        StringBuilder vBuilderDayInfo = new StringBuilder();
        Spanned vSunsetDay = Html.fromHtml(vSunsetDayTitle + "<br/>" + Constants.INVALID_STRING);

        if (hijriCorrectedComponents[2] > 0) {
            vBuilderDayInfo.append(String.format(Locale.ROOT, "%s %02d %s \u200F%04dهـ", dayOfWeekCorrected, hijriCorrectedComponents[2], monthNameHijriCorrected, hijriCorrectedComponents[0]))
                    .append(Constants.NEW_LINE);
        }

        vBuilderDayInfo.append(String.format(Locale.ROOT, "%s %02d %s \u200F%04dم", dayOfWeekGregorian, gregComponents[2], monthNameGregorian, gregComponents[0]));

        if (dayInfoMessagesOutput.messagesNas > 0) {
            vSunsetDayTitle += String.format(Locale.ROOT, " <font color='%s'>(%d)</font>", Constants.WG_TEXTCOLOR_GREEN, dayInfoMessagesOutput.messagesNas);
        }

        if (hijriCorrectedComponents[2] > 0) {

            String dayOfWeekCorrectedHtml = "<font color='" + dayInfoMessagesOutput.dayOfWeekColor + "'>" + dayOfWeekCorrected + "</font>";
            String dayHijriCorrectedHtml = String.format(Locale.ROOT, " <font color='%s'>%02d</font>", dayInfoMessagesOutput.dayOfMonthColor, hijriCorrectedComponents[2]);
            String monthNameHijriCorrectedHtml = "<font color='" + dayInfoMessagesOutput.monthNameColor + "'>" + monthNameHijriCorrected + "</font>";
            vSunsetDay = Html.fromHtml(vSunsetDayTitle + "<br/>" + dayOfWeekCorrectedHtml + " " + dayHijriCorrectedHtml + " " + monthNameHijriCorrectedHtml);

            if (dayInfoMessagesOutput.messagesNas + dayInfoMessagesOutput.messagesOther > 0) {
                vBuilderDayInfo.append(Constants.NEW_LINE).append(dayInfoMessagesOutput.messagesBuilder.toString());
            }
        }

        views.setTextViewText(R.id.wg_message, wgMessage[1]);
        views.setTextViewText(R.id.wg_time, time);
        views.setTextViewText(R.id.wg_date_hijri, vDateHijri);
        views.setTextViewText(R.id.wg_date_greg, vDateGreg);

        views.setTextViewText(R.id.wg_cell_01_00, Variables.widgetCellText(Constants.TITLE_SUBH, vSubh));
        views.setTextViewText(R.id.wg_cell_01_01, Variables.widgetCellText(vDhuhrTitle, vDhuhr));
        views.setTextViewText(R.id.wg_cell_01_02, Variables.widgetCellText(Constants.TITLE_ASR, vAsr));
        views.setTextViewText(R.id.wg_cell_01_03, Variables.widgetCellText(Constants.TITLE_MAGHRIB, vMaghrib));
        views.setTextViewText(R.id.wg_cell_01_04, Variables.widgetCellText(Constants.TITLE_ISHA, vIsha));

        views.setTextViewText(R.id.wg_cell_02_00, Variables.widgetCellText(Constants.TITLE_FAJR, vFajr));
        views.setTextViewText(R.id.wg_cell_02_01, Variables.widgetCellText(Constants.TITLE_ZAWAL, vZawal));
        views.setTextViewText(R.id.wg_cell_02_02, Variables.widgetCellText(Constants.TITLE_ASR, vAsrCalc));
        views.setTextViewText(R.id.wg_cell_02_03, Variables.widgetCellText(Constants.TITLE_SUNSET, vSunset));
        views.setTextViewText(R.id.wg_cell_02_04, Variables.widgetCellText(Constants.TITLE_GHASAQ, vGhasaq));

        views.setTextViewText(R.id.wg_cell_03_00, Variables.widgetCellText(Constants.TITLE_SUNRISE, vSunrise));
        views.setTextViewText(R.id.wg_cell_03_01, Variables.widgetCellText(Constants.TITLE_DHUHA_END, vTakabud));
        views.setTextViewText(R.id.wg_cell_03_02, Variables.widgetCellText(Constants.TITLE_ISFIRAR, vIsfirar));
        views.setTextViewText(R.id.wg_cell_03_03, Variables.widgetCellText(vMoonEventsTitle, vMoonEvents));
        views.setTextViewText(R.id.wg_cell_03_04, Variables.widgetCellText(Constants.TITLE_ISHA_END, vIshaEnd));

        views.setTextViewText(R.id.wg_cell_04_00, Variables.widgetCellText(Constants.TITLE_DHUHA, vIrtifae));
        views.setTextViewText(R.id.wg_cell_04_01, Variables.widgetCellText(vIstiwaeTitle, vIstiwae));
        views.setTextViewText(R.id.wg_cell_04_02, Variables.widgetCellText(vHourTitle, vHour));
        views.setTextViewText(R.id.wg_cell_04_03, Variables.widgetCellText(vMoonIlluminationBestObservationTitle, vMoonIlluminationBestObservationTime));
        views.setTextViewText(R.id.wg_cell_04_04, Variables.widgetCellText(Constants.TITLE_NIGHT_THIRD_LAST, vLastThird));

        views.setTextViewText(R.id.wg_cell_05_00, Variables.widgetCellText(Constants.TITLE_SUBH_AWAL, vSubhAwal));
        views.setTextViewText(R.id.wg_cell_05_01, Variables.widgetCellText(vSunTitle, vSun));
        views.setTextViewText(R.id.wg_cell_05_02, Variables.widgetCellText(vMoonCoordinatesTitle, vMoonCoordinates));
        views.setTextViewText(R.id.wg_cell_05_03, Variables.widgetCellText(Constants.TITLE_MOON_AGE, vMoonAge));
        views.setTextViewText(R.id.wg_cell_05_04, Variables.widgetCellText(vPeriodTitle, vPeriod));

        views.setTextViewText(R.id.wg_cell_06_00, vSunsetDay);
        views.setTextViewText(R.id.wg_cell_06_02, Variables.widgetCellText(Constants.TITLE_COMPASS, vCompassAzimuth));
        views.setTextViewText(R.id.wg_cell_06_03, Variables.widgetCellText(vQiblaTitle, vQibla));
        views.setTextViewText(R.id.wg_cell_06_04, Variables.widgetCellText(vShadowLastPortionTitle, vShadowLastPortion));

        /* Moon Age Visibility colors */

        String odehMessage = "معيار عودة : ";
        int crescentColor = Variables.colorWhite;

        if (odehVisibility >= 0) {
            switch (odehVisibility) {
                case SunMoonCalculator.ODEH_NOT_VISIBLE:
                    crescentColor = Variables.colorRed;
                    odehMessage += "رؤية الهلال غير ممكنة بالعين المجردة ولا بالتلسكوب";
                    break;
                case SunMoonCalculator.ODEH_VISIBLE_NEED_OPTICAL_AID:
                    crescentColor = Variables.colorOrange;
                    odehMessage += "رؤية الهلال ممكنة باستخدام التلسكوب فقط";
                    break;
                case SunMoonCalculator.ODEH_VISIBLE_MAY_NEED_OPTICAL_AID:
                    crescentColor = Variables.colorBlue;
                    odehMessage += "رؤية الهلال ممكنة باستخدام التلسكوب، ومن الممكن رؤية الهلال بالعين المجردة في حالة صفاء الغلاف الجوي التام والرصد من قبل راصد متمرس";
                    break;
                case SunMoonCalculator.ODEH_VISIBLE_NAKED_EYE:
                    crescentColor = Variables.colorGreen;
                    odehMessage += "رؤية الهلال ممكنة بالعين المجردة";
                    break;
            }
        }

        views.setTextColor(R.id.wg_cell_04_03, crescentColor);
        views.setTextColor(R.id.wg_cell_05_03, crescentColor);

        /* vLastHour color */

        if (isFriday && inDayTimeSun && nextHourRank == 12 && timeSeconds >= nextHourSeconds) {
            views.setTextColor(R.id.wg_cell_04_02, Variables.colorGreen);
        } else {
            views.setTextColor(R.id.wg_cell_04_02, Variables.colorWhite);
        }

        /* vLastThird color */

        if (inLastThird) {
            views.setTextColor(R.id.wg_cell_04_04, Variables.colorGreen);
        } else {
            views.setTextColor(R.id.wg_cell_04_04, Variables.colorWhite);
        }

        /* EarthCalculation & Compass color */

        views.setInt(R.id.wg_cell_06_02, "setBackgroundColor", Variables.colorTransparent);
        views.setInt(R.id.wg_cell_06_03, "setBackgroundColor", Variables.colorTransparent);

        /* 5 Prayers colors */

        if (prayersOfShownDate) {
            views.setTextColor(R.id.wg_cell_01_00, Variables.colorWhite);
            views.setTextColor(R.id.wg_cell_01_01, Variables.colorWhite);
            views.setTextColor(R.id.wg_cell_01_02, Variables.colorWhite);
            views.setTextColor(R.id.wg_cell_01_03, Variables.colorWhite);
            views.setTextColor(R.id.wg_cell_01_04, Variables.colorWhite);
        } else {
            views.setTextColor(R.id.wg_cell_01_00, Variables.colorOrange);
            views.setTextColor(R.id.wg_cell_01_01, Variables.colorOrange);
            views.setTextColor(R.id.wg_cell_01_02, Variables.colorOrange);
            views.setTextColor(R.id.wg_cell_01_03, Variables.colorOrange);
            views.setTextColor(R.id.wg_cell_01_04, Variables.colorOrange);
        }

        switch (wgMessage[0]) {
            case Constants.TITLE_SUBH:
                views.setTextColor(R.id.wg_cell_01_00, Variables.colorBlue);
                break;
            case Constants.TITLE_DHUHR:
                views.setTextColor(R.id.wg_cell_01_01, Variables.colorBlue);
                break;
            case Constants.TITLE_ASR:
                views.setTextColor(R.id.wg_cell_01_02, Variables.colorBlue);
                break;
            case Constants.TITLE_MAGHRIB:
                views.setTextColor(R.id.wg_cell_01_03, Variables.colorBlue);
                break;
            case Constants.TITLE_ISHA:
                views.setTextColor(R.id.wg_cell_01_04, Variables.colorBlue);
                break;
        }

        /* between times colors */

        int vBetweenStart = 0;
        int vBetweenEnd = 0;
        int betweenColor = Variables.colorWhite;

        views.setTextColor(R.id.wg_cell_02_00, Variables.colorWhite);
        views.setTextColor(R.id.wg_cell_03_00, Variables.colorWhite);

        views.setTextColor(R.id.wg_cell_04_00, Variables.colorWhite);

        views.setTextColor(R.id.wg_cell_02_01, Variables.colorWhite);
        views.setTextColor(R.id.wg_cell_03_01, Variables.colorWhite);

        views.setTextColor(R.id.wg_cell_02_02, Variables.colorWhite);
        views.setTextColor(R.id.wg_cell_03_02, Variables.colorWhite);

        views.setTextColor(R.id.wg_cell_02_03, Variables.colorWhite);

        if (timeSeconds >= Utilities.secondsOf(vFajr)) {
            if (timeSeconds < Utilities.secondsOf(vSunrise)) {
                betweenColor = Variables.colorOrange;
                vBetweenStart = R.id.wg_cell_02_00;
                vBetweenEnd = R.id.wg_cell_03_00;
            } else if (timeSeconds < Utilities.secondsOf(vIrtifae)) {
                betweenColor = Variables.colorRed;
                vBetweenStart = R.id.wg_cell_03_00;
                vBetweenEnd = R.id.wg_cell_04_00;
            } else if (timeSeconds >= Utilities.secondsOf(vTakabud)) {
                if (timeSeconds < Utilities.secondsOf(vZawal)) {
                    betweenColor = Variables.colorRed;
                    vBetweenStart = R.id.wg_cell_02_01;
                    vBetweenEnd = R.id.wg_cell_03_01;
                } else if (timeSeconds >= Utilities.secondsOf(vAsrCalc)) {
                    if (timeSeconds < Utilities.secondsOf(vIsfirar)) {
                        betweenColor = Variables.colorOrange;
                        vBetweenStart = R.id.wg_cell_02_02;
                        vBetweenEnd = R.id.wg_cell_03_02;
                    } else if (timeSeconds < Utilities.secondsOf(vSunset)) {
                        betweenColor = Variables.colorRed;
                        vBetweenStart = R.id.wg_cell_02_03;
                        vBetweenEnd = R.id.wg_cell_03_02;
                    }
                }
            }
        }

        if (Variables.isDayOfWeek(date, Calendar.WEDNESDAY) &&
                timeSeconds >= Utilities.secondsOf(vZawal) &&
                timeSeconds < Utilities.secondsOf(vAsrCalc)) {
            betweenColor = Variables.colorGreen;
            vBetweenStart = R.id.wg_cell_02_01;
            vBetweenEnd = R.id.wg_cell_02_02;
        }

        if (Variables.colorWhite != betweenColor) {
            views.setTextColor(vBetweenStart, betweenColor);
            views.setTextColor(vBetweenEnd, betweenColor);
        }

        // activities

        views.setOnClickPendingIntent(R.id.wg_image_home, pendingIntentActivity(context, appWidgetId, ActivityMain.class));
        views.setOnClickPendingIntent(R.id.wg_image_settings, pendingIntentActivity(context, appWidgetId, ActivityWidgetConfigure.class));

        // dialogs

        views.setOnClickPendingIntent(R.id.wg_cell_06_00, pendingIntentDialog(context, appWidgetId, "معلومات", vBuilderDayInfo.toString()));
        views.setOnClickPendingIntent(R.id.wg_cell_04_03, vMoonIlluminationBestObservationTitle.equals(Constants.TITLE_MOON_BEST_OBSERVATION_TIME) ?
                pendingIntentDialog(context, appWidgetId, "معلومات", odehMessage) : null);

        // date

        views.setOnClickPendingIntent(R.id.wg_date_hijri, pendingIntentDate(context, appWidgetId, -1));
        views.setOnClickPendingIntent(R.id.wg_date_greg, pendingIntentDate(context, appWidgetId, 1));

        // message

        PendingIntent pendingIntentMessage = pendingIntent(context, appWidgetId, INTENT_ACTION_MESSAGE);
        views.setOnClickPendingIntent(R.id.wg_message, pendingIntentMessage);
        views.setOnClickPendingIntent(R.id.wg_time, pendingIntentMessage);

        // actions

        views.setOnClickPendingIntent(R.id.wg_cell_06_02, hasCompass ? pendingIntentCompass(context, appWidgetId, latitude, longitude) :
                pendingIntentDialog(context, appWidgetId, "معلومات", "جهازك لا يدعم البوصلة."));

        views.setOnClickPendingIntent(R.id.wg_cell_06_03, pendingIntent(context, appWidgetId, INTENT_ACTION_QIBLA));
        views.setOnClickPendingIntent(R.id.wg_cell_05_04, pendingIntent(context, appWidgetId, INTENT_ACTION_PERIOD));
        views.setOnClickPendingIntent(R.id.wg_cell_05_02, pendingIntent(context, appWidgetId, INTENT_ACTION_MOON_COORDINATES));
        views.setOnClickPendingIntent(R.id.wg_cell_03_03, pendingIntent(context, appWidgetId, INTENT_ACTION_MOON_EVENTS));
        views.setOnClickPendingIntent(R.id.wg_cell_05_01, pendingIntent(context, appWidgetId, INTENT_ACTION_SUN));
        views.setOnClickPendingIntent(R.id.wg_cell_05_00, pendingIntentTime(context, appWidgetId, Constants.MESSAGE_SUBH_AWAL, vSubhAwal, 0, cNow, datetime, configuration.wLongFormat));
        views.setOnClickPendingIntent(R.id.wg_cell_06_04, vShadowLastPortionTitle.equals(Constants.TITLE_NIGHT_PORTION_LAST) ? null : pendingIntent(context, appWidgetId, INTENT_ACTION_SHADOW));

        // 5 prayers

        views.setOnClickPendingIntent(R.id.wg_cell_01_00, pendingIntentTime(context, appWidgetId, Constants.TITLE_SUBH, vSubh, iqama[0], cNow, datetime, configuration.wLongFormat));
        views.setOnClickPendingIntent(R.id.wg_cell_01_01, pendingIntentTime(context, appWidgetId, vDhuhrTitle, vDhuhr, iqama[1], cNow, datetime, configuration.wLongFormat));
        views.setOnClickPendingIntent(R.id.wg_cell_01_02, pendingIntentTime(context, appWidgetId, Constants.TITLE_ASR, vAsr, iqama[2], cNow, datetime, configuration.wLongFormat));
        views.setOnClickPendingIntent(R.id.wg_cell_01_03, pendingIntentTime(context, appWidgetId, Constants.TITLE_MAGHRIB, vMaghrib, iqama[3], cNow, datetime, configuration.wLongFormat));
        views.setOnClickPendingIntent(R.id.wg_cell_01_04, pendingIntentTime(context, appWidgetId, Constants.TITLE_ISHA, vIsha, iqama[4], cNow, datetime, configuration.wLongFormat));

        // 5 times

        views.setOnClickPendingIntent(R.id.wg_cell_02_00, pendingIntentTime(context, appWidgetId, Constants.TITLE_FAJR, vFajr, 0, cNow, datetime, configuration.wLongFormat));
        views.setOnClickPendingIntent(R.id.wg_cell_02_01, pendingIntentTime(context, appWidgetId, Constants.TITLE_ZAWAL, vZawal, 0, cNow, datetime, configuration.wLongFormat));
        views.setOnClickPendingIntent(R.id.wg_cell_02_02, pendingIntentTime(context, appWidgetId, Constants.TITLE_ASR_CALC, vAsrCalc, 0, cNow, datetime, configuration.wLongFormat));
        views.setOnClickPendingIntent(R.id.wg_cell_02_03, pendingIntentTime(context, appWidgetId, Constants.TITLE_SUNSET, vSunset, 0, cNow, datetime, configuration.wLongFormat));
        views.setOnClickPendingIntent(R.id.wg_cell_02_04, pendingIntentTime(context, appWidgetId, Constants.TITLE_GHASAQ, vGhasaq, 0, cNow, datetime, configuration.wLongFormat));

        // special times

        views.setOnClickPendingIntent(R.id.wg_cell_04_02, pendingIntentTime(context, appWidgetId, vHourMessage, vHour, 0, cNow, datetime, configuration.wLongFormat));
        views.setOnClickPendingIntent(R.id.wg_cell_03_00, pendingIntentTime(context, appWidgetId, Constants.TITLE_SUNRISE, vSunrise, 0, cNow, datetime, configuration.wLongFormat));
        views.setOnClickPendingIntent(R.id.wg_cell_04_00, pendingIntentTime(context, appWidgetId, Constants.TITLE_DHUHA, vIrtifae, 0, cNow, datetime, configuration.wLongFormat));
        views.setOnClickPendingIntent(R.id.wg_cell_03_02, pendingIntentTime(context, appWidgetId, Constants.MESSAGE_ISFIRAR, vIsfirar, 0, cNow, datetime, configuration.wLongFormat));
        views.setOnClickPendingIntent(R.id.wg_cell_03_04, pendingIntentTime(context, appWidgetId, Constants.MESSAGE_ISHA_END, vIshaEnd, 0, cNow, datetime, configuration.wLongFormat));

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    void compassStop() {
        compassTrueAzimuth = 0;
        this.context = null;
        geomagneticField = null;
        if (sensorManager == null) return;
        sensorManager.unregisterListener(this);
        sensorManager = null;
    }

    void compassRestart(Context context, double latitude, double longitude) {
        compassStop();
        this.context = context;

        geomagneticField = new GeomagneticField((float) latitude, (float) longitude, 0, System.currentTimeMillis());

        sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);

        hasRotationVector = sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        String action = intent.getAction();
        if (action == null) return;

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget_main);

        int[] appWidgetIds = AppWidgetManager.getInstance(context.getApplicationContext()).getAppWidgetIds(new ComponentName(context.getApplicationContext(), AppWidgetMain.class));

        int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        WidgetConfiguration configuration = appWidgetId <= AppWidgetManager.INVALID_APPWIDGET_ID ? null : WidgetConfiguration.fromConfigurationFile(context, appWidgetId);

        switch (action) {

            case INTENT_ACTION_DATE:

                int sign = intent.getIntExtra(INTENT_EXTRA_DATE_SIGN, 1);

                configuration.wDateTimeNow = false;
                configuration.wDateTimeCustom = Utilities.adjustDateGreg(configuration.wDateTimeCustom, configuration.navigateFieldCalendar(), sign * configuration.wNavigateAmount, true);

                configuration.toConfigurationFile(context, appWidgetId);

                context.sendBroadcast(intentUpdate(context, new int[]{appWidgetId}));

                break;

            case INTENT_ACTION_MESSAGE:

                if (!configuration.wDateTimeNow) {
                    configuration.wDateTimeNow = true;
                    configuration.toConfigurationFile(context, appWidgetId);
                }

                context.sendBroadcast(intentUpdate(context, new int[]{appWidgetId}));

                break;

            case INTENT_ACTION_COMPASS:

                if (sensorManager == null) {

                    double latitude = intent.getDoubleExtra(INTENT_EXTRA_COMPASS_LATITUDE, 36);
                    double longitude = intent.getDoubleExtra(INTENT_EXTRA_COMPASS_LONGITUDE, 1);
                    compassRestart(context, latitude, longitude);

                } else {

                    compassStop();
                    context.sendBroadcast(intentUpdate(context, appWidgetIds));
                }

                break;

            case INTENT_ACTION_QIBLA:

                if (sensorManager == null) {

                    configuration.swQibla = (configuration.swQibla + 1) % WidgetConfiguration.SWITCH_QIBLA__COUNT;
                    configuration.toConfigurationFile(context, appWidgetId);

                    context.sendBroadcast(intentUpdate(context, new int[]{appWidgetId}));
                }

                break;

            case INTENT_ACTION_TIME:

                boolean pNow = intent.getBooleanExtra(INTENT_EXTRA_TIME_NOW, true);
                String pName = intent.getStringExtra(INTENT_EXTRA_TIME_NAME);
                String pTime = intent.getStringExtra(INTENT_EXTRA_TIME_TIME);
                int pIqama = intent.getIntExtra(INTENT_EXTRA_TIME_IQAMA, 0);
                boolean pTimeSeconds = intent.getBooleanExtra(INTENT_EXTRA_TIME_SECONDS, false);

                String datetime = pNow ? Variables.getCurrentDateTime() : intent.getStringExtra(INTENT_EXTRA_TIME_DATETIME);
                String time = Variables.getTime(datetime);

                views.setTextViewText(R.id.wg_message, Utilities.timeG(Utilities.secondsOf(time), pTime, pIqama, pName, pTimeSeconds));
                views.setTextViewText(R.id.wg_time, time);

                AppWidgetManager.getInstance(context).partiallyUpdateAppWidget(appWidgetId, views);

                break;

            case Intent.ACTION_USER_PRESENT:
            case Intent.ACTION_SCREEN_ON:

                for (int id : appWidgetIds) {

                    WidgetConfiguration config = WidgetConfiguration.fromConfigurationFile(context, id);

                    if (!config.wDateTimeNow) {
                        config.wDateTimeNow = true;
                        config.toConfigurationFile(context, id);
                    }
                }

                context.sendBroadcast(intentUpdate(context, appWidgetIds));

                break;

            case INTENT_ACTION_PERIOD:

                configuration.swPeriod = (configuration.swPeriod + 1) % WidgetConfiguration.SWITCH_PERIOD__COUNT;
                configuration.toConfigurationFile(context, appWidgetId);

                context.sendBroadcast(intentUpdate(context, new int[]{appWidgetId}));

                break;

            case INTENT_ACTION_MOON_COORDINATES:

                configuration.swMoonCoordinates = (configuration.swMoonCoordinates + 1) % WidgetConfiguration.SWITCH_MOON_COORDINATES__COUNT;
                configuration.toConfigurationFile(context, appWidgetId);

                context.sendBroadcast(intentUpdate(context, new int[]{appWidgetId}));

                break;

            case INTENT_ACTION_MOON_EVENTS:

                configuration.swMoonEvents = (configuration.swMoonEvents + 1) % WidgetConfiguration.SWITCH_MOON_EVENTS__COUNT;
                configuration.toConfigurationFile(context, appWidgetId);

                context.sendBroadcast(intentUpdate(context, new int[]{appWidgetId}));

                break;

            case INTENT_ACTION_SUN:

                configuration.swSun = (configuration.swSun + 1) % WidgetConfiguration.SWITCH_SUN__COUNT;
                configuration.toConfigurationFile(context, appWidgetId);

                context.sendBroadcast(intentUpdate(context, new int[]{appWidgetId}));

                break;

            case INTENT_ACTION_SHADOW:

                configuration.swShadow = (configuration.swShadow + 1) % WidgetConfiguration.SWITCH_SHADOW__COUNT;
                configuration.toConfigurationFile(context, appWidgetId);

                context.sendBroadcast(intentUpdate(context, new int[]{appWidgetId}));

                break;
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        Variables.runAsyncVersion(context);
        Variables.runAsyncHijriAutoUpdate(context);

        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {

        super.onDeleted(context, appWidgetIds);

        for (int appWidgetId : appWidgetIds) {
            Utilities.deleteWidget(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
    }

    @Override
    public void onDisabled(Context context) {
        compassStop();
    }

    private float computeTrueNorthAzimuth(float magneticAzimuthRadians) {

        float trueNorthAzimuth = (float) Math.toDegrees(magneticAzimuthRadians) + 360;

        if (geomagneticField != null) {
            trueNorthAzimuth += geomagneticField.getDeclination(); // trueAzimuth = magneticAzimuth + declination
        }

        return trueNorthAzimuth % 360;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (sensorManager == null) return;

        float[] rotation = new float[9];
        float[] orientation = new float[3];

        if (hasRotationVector) {

            if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {

                SensorManager.getRotationMatrixFromVector(rotation, event.values);
                SensorManager.getOrientation(rotation, orientation);
                compassTrueAzimuth = computeTrueNorthAzimuth(orientation[0]); // orientation[0] : magneticAzimuth : [-180, 180]
            }

        } else {

            float[] gravity = null;
            float[] geomagnetic = null;

            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                gravity = Utilities.applyLowPassFilter(event.values, gravity);
            } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                geomagnetic = Utilities.applyLowPassFilter(event.values, geomagnetic);
            }

            if (gravity != null && geomagnetic != null) {
                float[] inclination = new float[9];
                if (SensorManager.getRotationMatrix(rotation, inclination, gravity, geomagnetic)) {
                    SensorManager.getOrientation(rotation, orientation);
                    compassTrueAzimuth = computeTrueNorthAzimuth(orientation[0]); // orientation[0] : magneticAzimuth : [-180, 180]
                }
            }
        }

        if (this.context == null) return;

        int[] appWidgetIds = AppWidgetManager.getInstance(context.getApplicationContext()).getAppWidgetIds(new ComponentName(context.getApplicationContext(), AppWidgetMain.class));

        for (int appWidgetId : appWidgetIds) {

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget_main);

            String colorCompassString = Constants.WG_TEXTCOLOR_RED;

            switch (compassAccuracy) {
                case SENSOR_STATUS_ACCURACY_HIGH:
                    colorCompassString = Constants.WG_TEXTCOLOR_GREEN;
                    break;
                case SENSOR_STATUS_ACCURACY_MEDIUM:
                    colorCompassString = Constants.WG_TEXTCOLOR_ORANGE;
                    break;
            }

            WidgetConfiguration configuration = WidgetConfiguration.fromConfigurationFile(context, appWidgetId);

            double latitude = Utilities.valueOf(configuration.cLocationCalcParams, 0, 10, true);
            double longitude = Utilities.valueOf(configuration.cLocationCalcParams, 1, 10, true);

            double qiblaAzimuth = EarthCalculation.qiblaDegrees(ActivityMain.prefEarthModel(context), latitude, longitude);

            double diffQibla = Utilities.shortestAngleDegrees(compassTrueAzimuth, qiblaAzimuth);
            double diffQiblaAbs = Math.abs(diffQibla);

            String colorQiblaString = Constants.WG_TEXTCOLOR_TRANSPARENT;
            String vDiffQibla = Constants.SYMBOL_DEGREES + Variables.decimalFormatter0.format(diffQiblaAbs);

            if (diffQiblaAbs <= 2.5) {
                colorQiblaString = colorCompassString;
            } else {
                if (diffQibla < 0) {
                    vDiffQibla = vDiffQibla + Constants.SYMBOL_LEFT;
                } else {
                    vDiffQibla = Constants.SYMBOL_RIGHT + vDiffQibla;
                }
            }

            int colorQibla = Color.parseColor(colorQiblaString);
            int colorCompass = Color.parseColor(colorCompassString);

            views.setInt(R.id.wg_cell_06_02, "setBackgroundColor", colorCompass);
            views.setInt(R.id.wg_cell_06_03, "setBackgroundColor", colorQibla);

            String vCompassAzimuth = Constants.SYMBOL_DEGREES + Variables.decimalFormatter0.format(compassTrueAzimuth);

            views.setTextViewText(R.id.wg_cell_06_02, Variables.widgetCellText(Constants.TITLE_COMPASS, vCompassAzimuth));
            views.setTextViewText(R.id.wg_cell_06_03, Variables.widgetCellText(Constants.TITLE_QIBLA_ANGLE, vDiffQibla));

            AppWidgetManager.getInstance(context).partiallyUpdateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

        if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            compassAccuracy = accuracy;
        }
    }
}

package jalil.prayertimes;

import android.content.Context;

import java.io.File;
import java.util.Calendar;

public class WidgetConfiguration {

    static final int PRAYERS_METHOD_ACTUAL = 0;
    static final int PRAYERS_METHOD_PRAYTIMES = 1;
    static final int PRAYERS_METHOD_2ND_LINE = 2;

    static final int ASR_SHADOW_LENGTH = 0;
    static final int ASR_SHADOW_WIDTH = 1;
    static final int ASR_MID = 2;

    public static final int SWITCH_QIBLA_ANGLE = 0;
    public static final int SWITCH_QIBLA_SUN_FRONT = 1;
    public static final int SWITCH_QIBLA_SUN_BEHIND = 2;
    public static final int SWITCH_QIBLA_SUN_LEFT = 3;
    public static final int SWITCH_QIBLA_MOON_FRONT = 4;
    public static final int SWITCH_QIBLA_MOON_BEHIND = 5;
    public static final int SWITCH_QIBLA_MOON_LEFT = 6;
    public static final int SWITCH_QIBLA__COUNT = 7;

    static final int ISTIWAE_LATER = 0;
    static final int ISTIWAE_TRANSIT = 1;
    static final int ISTIWAE_CULMINATION = 2;

    static final int NIGHT_PERIOD_SIYAM = 0;
    static final int NIGHT_PERIOD_QIYAM = 1;
    static final int NIGHT_PERIOD_SUN = 2;

    static final int KARAHA_UNIT_HORIZON_DEGREE = 0;
    static final int KARAHA_UNIT_HORIZON_DIAMETER = 1;
    static final int KARAHA_UNIT_HORIZON_MINUTE = 2;
    static final int KARAHA_UNIT_HORIZON_HOUR = 3;

    static final int KARAHA_UNIT_ISTIWAE_DEGREE = 0;
    static final int KARAHA_UNIT_ISTIWAE_DIAMETER = 1;
    static final int KARAHA_UNIT_ISTIWAE_MINUTE = 2;
    static final int KARAHA_UNIT_ISTIWAE_SHADOW_WIDTH = 3;

    static final int FAJR_ANGLE = 0;
    static final int FAJR_HOUR = 1;
    static final int FAJR_MOONSIGHTING = 2;

    static final int GHASAQ_ANGLE = 0;
    static final int GHASAQ_HOUR = 1;
    static final int GHASAQ_MOONSIGHTING_GENERAL = 2;
    static final int GHASAQ_MOONSIGHTING_RED = 3;
    static final int GHASAQ_MOONSIGHTING_WHITE = 4;

    static final int ISHA_END_MID = 0;
    static final int ISHA_END_THIRD = 1;

    static final int NAVIGATE_FIELD_SECONDS = 0;
    static final int NAVIGATE_FIELD_MINUTES = 1;
    static final int NAVIGATE_FIELD_HOURS = 2;
    static final int NAVIGATE_FIELD_DAYS = 3;
    static final int NAVIGATE_FIELD_MONTHS = 4;
    static final int NAVIGATE_FIELD_YEARS = 5;

    int navigateFieldCalendar() {

        switch (wNavigateField) {
            case NAVIGATE_FIELD_SECONDS:
                return Calendar.SECOND;
            case NAVIGATE_FIELD_MINUTES:
                return Calendar.MINUTE;
            case NAVIGATE_FIELD_HOURS:
                return Calendar.HOUR_OF_DAY;
            case NAVIGATE_FIELD_DAYS:
                return Calendar.DAY_OF_MONTH;
            case NAVIGATE_FIELD_MONTHS:
                return Calendar.MONTH;
            case NAVIGATE_FIELD_YEARS:
                return Calendar.YEAR;
        }

        return Calendar.DAY_OF_MONTH;
    }

    static final int SWITCH_PERIOD_DAY_LENGTH_SUN = 0;
    static final int SWITCH_PERIOD_NIGHT_LENGTH_SUN = 1;
    static final int SWITCH_PERIOD_DAY_LENGTH_SIYAM = 2;
    static final int SWITCH_PERIOD_NIGHT_LENGTH_SIYAM = 3;
    static final int SWITCH_PERIOD_DAY_HOUR_LENGTH = 4;
    static final int SWITCH_PERIOD_NIGHT_HOUR_LENGTH = 5;
    static final int SWITCH_PERIOD__COUNT = 6;

    static final int SWITCH_SHADOW_LENGTH = 0;
    static final int SWITCH_SHADOW_WALL_WIDTH = 1;
    static final int SWITCH_SHADOW__COUNT = 2;

    static final int SWITCH_MOON_COORDINATES_AZIMUTH = 0;
    static final int SWITCH_MOON_COORDINATES_ELEVATION = 1;
    static final int SWITCH_MOON_COORDINATES__COUNT = 2;

    static final int SWITCH_MOON_EVENTS_SET = 0;
    static final int SWITCH_MOON_EVENTS_RISE = 1;
    static final int SWITCH_MOON_EVENTS__COUNT = 2;

    static final int SWITCH_SUN_AZIMUTH = 0;
    static final int SWITCH_SUN_ELEVATION = 1;
    static final int SWITCH_SUN__COUNT = 2;

    int cLocationActualId = Constants.WDG_DEFAULT_ACTUAL_CITY_ID;

    String cLocationActualName = Constants.WDG_DEFAULT_ACTUAL_CITY_NAME;
    String cLocationCalcParams = Constants.WDG_DEFAULT_CALC_LOCATION;
    String cLocationCalcName = Constants.WDG_DEFAULT_CALC_LOCATION_NAME;
    String cLocationIqama = Constants.WDG_DEFAULT_IQAMA;
    String cLocationOffset = Constants.WDG_DEFAULT_OFFSET;

    int pPrayersMethod = Constants.WDG_DEFAULT_PRAYERS_METHOD;
    String pCalcParams = Constants.WDG_DEFAULT_CALC_PARAMS;
    int pNightPeriod = Constants.WDG_DEFAULT_NIGHT_PERIOD;

    int pIstiwae = Constants.WDG_DEFAULT_ISTIWAE;
    int pIrtifae = Constants.WDG_DEFAULT_IRTIFAE;
    int pIsfirar = Constants.WDG_DEFAULT_ISFIRAR;
    int pTakabud = Constants.WDG_DEFAULT_TAKABUD;
    int pZawal = Constants.WDG_DEFAULT_ZAWAL;

    int pFajr = Constants.WDG_DEFAULT_FAJR;
    int pGhasaq = Constants.WDG_DEFAULT_GHASAQ;
    int pIshaEnd = Constants.WDG_DEFAULT_ISHA_END;
    int pAsr = Constants.WDG_DEFAULT_ASR;

    boolean wDateTimeNow = Constants.WDG_DEFAULT_NOW;
    String wDateTimeCustom = Variables.getCurrentDateTime();
    int wTextSizePercent = Constants.WDG_DEFAULT_TEXTSIZE_PERCENT;
    int wNavigateField = Constants.WDG_DEFAULT_NAVIGATE_FIELD;
    int wNavigateAmount = Constants.WDG_DEFAULT_NAVIGATE_AMOUNT;
    boolean wLongFormat = Constants.WDG_DEFAULT_LONG_FORMAT;

    int swMoonCoordinates = Constants.WDG_DEFAULT_SWITCH_MOON_COORDINATES;
    int swMoonEvents = Constants.WDG_DEFAULT_SWITCH_MOON_EVENTS;
    int swSun = Constants.WDG_DEFAULT_SWITCH_SUN;
    int swShadow = Constants.WDG_DEFAULT_SWITCH_SHADOW;
    int swPeriod = Constants.WDG_DEFAULT_SWITCH_PERIOD;
    int swQibla = Constants.WDG_DEFAULT_SWITCH_QIBLA;

    boolean toConfigurationFile(Context context, int appWidgetId) {
        return Utilities.writeToTextFile(Variables.getFullPathWg(context, appWidgetId), Variables.gson.toJson(this));
    }

    static WidgetConfiguration fromConfigurationFile(Context context, int appWidgetId) {

        String file = Variables.getFullPathWg(context, appWidgetId);

        if (new File(file).exists()) {
            WidgetConfiguration configuration = Variables.gson.fromJson(Utilities.readFromTextFile(file), WidgetConfiguration.class);
            return configuration == null ? new WidgetConfiguration() : configuration;
        } else {
            return new WidgetConfiguration();
        }
    }

    boolean toFavoriteFile(Context context, String name) {
        return Utilities.writeToTextFile(Variables.getFullPathFav(context, name), Variables.gsonPrettyPrinting.toJson(this));
    }

    static WidgetConfiguration fromFavoriteFile(Context context, String name) {

        String file = Variables.getFullPathFav(context, name);

        if (new File(file).exists()) {
            WidgetConfiguration configuration = Variables.gsonPrettyPrinting.fromJson(Utilities.readFromTextFile(file), WidgetConfiguration.class);
            return configuration == null ? new WidgetConfiguration() : configuration;
        } else {
            return new WidgetConfiguration();
        }
    }
}

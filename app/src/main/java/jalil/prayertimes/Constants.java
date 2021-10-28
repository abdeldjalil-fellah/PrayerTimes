package jalil.prayertimes;

class Constants {

    static final String FILE_NAME_DB_MARW = "marw-0001.db";
    static final String FILE_NAME_DB_UHC = "uhc-0001.db";
    static final String FILE_NAME_DB_UAQ = "uaq-0001.db";

    static final String PARSE_CLASS_CALENDAR = "Calendar";
    static final String PARSE_COLUMN_CALENDAR_DATE_GREG = "dateGreg";
    static final String PARSE_COLUMN_CALENDAR_METHOD_HIJRI = "methodHijri";
    static final String PARSE_COLUMN_CALENDAR_ADJUST_HIJRI = "adjustHijri";
    static final String PARSE_COLUMN_CALENDAR_YEAR_HIJRI = "yearHijri";
    static final String PARSE_COLUMN_CALENDAR_MONTH_HIJRI = "monthHijri";

    static final int REQUEST_CODE_PERMISSIONS = 1;
    static final int REQUEST_CODE_WIDGET_CONFIGURE = 2;

    static final String DIR_NAME_DB = "databases";
    static final String DIR_NAME_FAV = "favorites";
    static final String DIR_NAME_WG = "widgets";
    static final String DIR_NAME_SHOT = "screenshots";

    static final String FILE_NAME_FAV_SAMPLE = "زدين مركز";

    static final String FAV_DEFAULT = "(افتراضي : مسجد الرحمة الجزائر الوسطى)";

    static final String APP_TAG = "PRAYER_TIMES";

    static final int SECONDS_IN_HOUR = 3600;
    static final int SECONDS_IN_DAY = 24 * SECONDS_IN_HOUR;

    static final String DATE_FORMAT_HMS = "HH:mm:ss";
    static final String DATE_FORMAT_YMD = "yyyy-MM-dd";
    static final String DATE_FORMAT_YMD_HMS = "yyyy-MM-dd HH:mm:ss";

    static final String DECIMAL_FORMAT_0 = "#0";
    static final String DECIMAL_FORMAT_2 = "#0.00";
    static final String DECIMAL_FORMAT_3 = "#0.000";

    static final String WG_TEXTCOLOR_TRANSPARENT = "#00000000"; /* #aarrggbb */
    static final String WG_TEXTCOLOR_WHITE = "#ffffff";

    static final String WG_TEXTCOLOR_BLUE = "#00eeff";

    static final String WG_TEXTCOLOR_PURPLE = "#ccaaff";
    static final String WG_TEXTCOLOR_GREEN = "#33ff00";
    static final String WG_TEXTCOLOR_ORANGE = "#ffaa00";
    static final String WG_TEXTCOLOR_RED = "#ff3300";
    static final String WG_TEXTCOLOR_YELLOW = "#ffff00";

    static final String WG_TEXTCOLOR_BLUE_LIGHT = "#8ba5bc";
    static final String WG_TEXTCOLOR_GREEN_LIGHT = "#d2e9af";
    static final String WG_TEXTCOLOR_ORANGE_LIGHT = "#fed8b1";
    static final String WG_TEXTCOLOR_RED_LIGHT = "#ffcccb";

    static final String INVALID_STRING = "----";

    static final String SYMBOL_DEGREES = "°";
    static final String SYMBOL_PERCENT = "%";
    static final String SYMBOL_MULTIPLY = "x";
    static final String SYMBOL_UP = "↑";
    static final String SYMBOL_DOWN = "↓";
    static final String SYMBOL_RIGHT = "→";
    static final String SYMBOL_LEFT = "←";
    static final String SYMBOL_PLUS = "+";
    static final String SYMBOL_MINUS = "-";

    static final String DEFAULT_LOCATION_NAME = "";
    static final int DEFAULT_LOCATION_OFFSET = 3600; /* +01:00 */

    static final String NEW_LINE = System.getProperty("line.separator");

    static final String TITLE_SUBH = "الصبح";
    static final String TITLE_DHUHR = "الظهر";
    static final String TITLE_JUMUAA = "الجمعة";
    static final String TITLE_ASR = "العصر";
    static final String TITLE_MAGHRIB = "المغرب";
    static final String TITLE_ISHA = "العشاء";

    static final String TITLE_FAJR = "الفجر";
    static final String TITLE_ZAWAL = "الزوال";
    static final String TITLE_ASR_CALC = "العصر";
    static final String TITLE_SUNSET = "الغروب";
    static final String TITLE_GHASAQ = "غ.الشفق";

    static final String TITLE_SUNRISE = "الشروق";
    static final String TITLE_DHUHA_END = "آخر الضحى";
    static final String TITLE_ISFIRAR = "آخر العصر";
    static final String MESSAGE_ISFIRAR = "آ.العصر";
    static final String TITLE_MOONSET = "غروب.ق";
    static final String TITLE_MOONRISE = "شروق.ق";
    static final String TITLE_ISHA_END = "آخر العشاء";
    static final String MESSAGE_ISHA_END = "آ.العشاء";

    static final String TITLE_DHUHA = "الضحى";
    static final String TITLE_SUN_TRANSIT = "العبور";
    static final String TITLE_SUN_CULMINATION = "التكبد";
    static final String TITLE_HOUR = "الساعة";
    static final String MESSAGE_HOUR = "سا";
    static final String TITLE_MOON_ILLUNINATION = "إضاءة.ق";
    static final String TITLE_MOON_BEST_OBSERVATION_TIME = "الهلال";
    static final String TITLE_NIGHT_THIRD_LAST = "الثلث الآخر";
    static final String TITLE_NIGHT_PORTION_LAST = "آخر جزء";

    static final String TITLE_SUBH_AWAL = "أول الصبح";
    static final String MESSAGE_SUBH_AWAL = "أ.الصبح";
    static final String TITLE_SHADOW_LENGTH = "طول الظل";
    static final String TITLE_SHADOW_WIDTH = "عرض الظل";
    static final String TITLE_SUN_AZIMUTH = "سمت.ش";
    static final String TITLE_SUN_ELEVATION = "ارتفاع.ش";
    static final String TITLE_MOON_AZIMUTH = "سمت.ق";
    static final String TITLE_MOON_ELEVATION = "ارتفاع.ق";
    static final String TITLE_MOON_AGE = "عمر.ق";
    static final String TITLE_DAY_LENGTH_SUN = "ي.شمسي";
    static final String TITLE_DAY_LENGTH_SIYAM = "ي.صيامي";
    static final String TITLE_NIGHT_LENGTH_SUN = "ل.شمسي";
    static final String TITLE_NIGHT_LENGTH_SIYAM = "ل.صيامي";
    static final String TITLE_DAY_HOUR_LENGTH = "سا.اليوم";
    static final String TITLE_NIGHT_HOUR_LENGTH = "سا.الليل";

    static final String TITLE_SUNSET_DAY = "اليوم الشرعي";
    static final String TITLE_COMPASS = "البوصلة";
    static final String TITLE_QIBLA_ANGLE = "القبلة";
    static final String TITLE_QIBLA_SUN_FRONT = "أمام.ش";
    static final String TITLE_QIBLA_SUN_BEHIND = "خلف.ش";
    static final String TITLE_QIBLA_SUN_LEFT = "يسار.ش";
    static final String TITLE_QIBLA_MOON_FRONT = "أمام.ق";
    static final String TITLE_QIBLA_MOON_BEHIND = "خلف.ق";
    static final String TITLE_QIBLA_MOON_LEFT = "يسار.ق";

    static final int WDG_DEFAULT_TEXTSIZE_PERCENT = 100;
    static final int WDG_DEFAULT_NAVIGATE_AMOUNT = 1;

    static final int WDG_DEFAULT_ASR = 0;

    static final int WDG_DEFAULT_PRAYERS_METHOD = WidgetConfiguration.PRAYERS_METHOD_ACTUAL;
    static final int WDG_DEFAULT_ISTIWAE = WidgetConfiguration.ISTIWAE_LATER;
    static final int WDG_DEFAULT_NIGHT_PERIOD = WidgetConfiguration.NIGHT_PERIOD_SIYAM;
    static final int WDG_DEFAULT_IRTIFAE = WidgetConfiguration.KARAHA_UNIT_HORIZON_DEGREE;
    static final int WDG_DEFAULT_ISFIRAR = WidgetConfiguration.KARAHA_UNIT_HORIZON_DEGREE;
    static final int WDG_DEFAULT_TAKABUD = WidgetConfiguration.KARAHA_UNIT_ISTIWAE_MINUTE;
    static final int WDG_DEFAULT_ZAWAL = WidgetConfiguration.KARAHA_UNIT_ISTIWAE_DIAMETER;
    static final int WDG_DEFAULT_FAJR = WidgetConfiguration.FAJR_ANGLE;
    static final int WDG_DEFAULT_GHASAQ = WidgetConfiguration.GHASAQ_ANGLE;
    static final int WDG_DEFAULT_ISHA_END = WidgetConfiguration.ISHA_END_MID;
    static final int WDG_DEFAULT_NAVIGATE_FIELD = WidgetConfiguration.NAVIGATE_FIELD_DAYS;

    static final int WDG_DEFAULT_SWITCH_SHADOW = WidgetConfiguration.SWITCH_SHADOW_LENGTH;
    static final int WDG_DEFAULT_SWITCH_MOON_COORDINATES = WidgetConfiguration.SWITCH_MOON_COORDINATES_AZIMUTH;
    static final int WDG_DEFAULT_SWITCH_MOON_EVENTS = WidgetConfiguration.SWITCH_MOON_EVENTS_SET;
    static final int WDG_DEFAULT_SWITCH_SUN = WidgetConfiguration.SWITCH_SUN_ELEVATION;
    static final int WDG_DEFAULT_SWITCH_PERIOD = WidgetConfiguration.SWITCH_PERIOD_DAY_LENGTH_SUN;
    static final int WDG_DEFAULT_SWITCH_QIBLA = WidgetConfiguration.SWITCH_QIBLA_ANGLE;

    static final int WDG_DEFAULT_ACTUAL_CITY_ID = 27;
    static final String WDG_DEFAULT_ACTUAL_CITY_NAME = "الجزائر";
    static final String WDG_DEFAULT_CALC_LOCATION = "+36.766518/+03.054137/+01:00";
    static final String WDG_DEFAULT_CALC_LOCATION_NAME = "الجزائر الوسطى";
    static final String WDG_DEFAULT_CALC_PARAMS = "18.00/17.00/03.50/03.50/10.00/00.50/01.00/06.00";
    static final String WDG_DEFAULT_IQAMA = "20/15/15/10/10";
    static final String WDG_DEFAULT_OFFSET = "+00/-20/+00/+00/+00/+00";

    static final boolean WDG_DEFAULT_NOW = true;
    static final boolean WDG_DEFAULT_LONG_FORMAT = false;

    static final String DAY_OF_WEEK_SATURDAY = "السبت";
    static final String DAY_OF_WEEK_SUNDAY = "الأحد";
    static final String DAY_OF_WEEK_MONDAY = "الاثنين";
    static final String DAY_OF_WEEK_TUESDAY = "الثلاثاء";
    static final String DAY_OF_WEEK_WEDNESDAY = "الأربعاء";
    static final String DAY_OF_WEEK_THURSDAY = "الخميس";
    static final String DAY_OF_WEEK_FRIDAY = "الجمعة";

    static final String POSITION_NONE = "";
    static final String POSITION_FRONT = "الأمام";
    static final String POSITION_BEHIND = "الخلف";
    static final String POSITION_RIGHT = "اليمين";
    static final String POSITION_LEFT = "اليسار";

    static final int LOCATION_METHOD_GOOGLE = 0;
    static final int LOCATION_METHOD_OSM = 1;

    static final String URL_LATEST_GITHUB = "https://github.com/jalil1408/PrayerTimes/releases/latest";

    static final String MAIN_SHADOW_PARAMS = "+01:00/00:10:00/060";
    static final int MAIN_SHADOW_DATETIME_TO_OFFSET_MINUTES = 180;

    static final String ABOUT_VERSION = "الإصدار: " + BuildConfig.VERSION_NAME;
}

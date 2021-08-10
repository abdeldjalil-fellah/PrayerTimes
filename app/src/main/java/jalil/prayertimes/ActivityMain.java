package jalil.prayertimes;

import android.app.Activity;
import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Html;
import android.text.Spanned;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_HIGH;
import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM;
import static android.hardware.SensorManager.SENSOR_STATUS_UNRELIABLE;

public class ActivityMain extends AppCompatActivity implements IAsyncCompleted, SensorEventListener {

    final static int APP_WIDGET_ID = 0;

    final static int HIJRI_METHOD_ACTUAL = 0;
    final static int HIJRI_METHOD_UMM_AL_QURA = 1;
    final static int HIJRI_METHOD_UHC = 2;
    final static int HIJRI_METHOD_QUAE = 3;

    int[] iqama;
    String vSunrise, odehMessage;
    String vSubh, vSubhAwal, vDhuhr, vAsr, vMaghrib, vIsha;
    String vDhuhrTitle, vShadowLastPortionTitle, vMoonIlluminationBestObservationTitle;

    DecimalFormat decimalFormatter;
    boolean hasCompass = false;
    StringBuilder vBuilderDayInfo;
    WidgetConfiguration configuration;
    double latitude, longitude, qiblaAzimuth;

    static SensorManager sensorManager;
    static GeomagneticField geomagneticField = null;

    static float compassTrueAzimuth = 0;
    static boolean hasRotationVector = false;

    static int compassAccuracy = SENSOR_STATUS_UNRELIABLE;
    static int magneticFieldAccuracy = SENSOR_STATUS_UNRELIABLE;

    public static final String PREFS_NAME = "app";

    public static final String PREF_EARTH_MODEL = "earthModel";
    public static final String PREF_HIJRI_AUTO = "hijriAuto";
    public static final String PREF_HIJRI_METHOD = "hijriMethod";
    public static final String PREF_HIJRI_ADJUST_DAYS = "hijriAdjustDays";
    public static final String PREF_LATEST_VERSION_MESSAGE = "latestVersionMessage";

    final static boolean DEFAULT_HIJRI_AUTO = true;
    final static int DEFAULT_HIJRI_METHOD = HIJRI_METHOD_QUAE;
    final static int DEFAULT_HIJRI_ADJUST_DAYS = 0;
    static final int DEFAULT_EARTH_MODEL = EarthCalculation.EARTH_MODEL_WGS84;
    static final String DEFAULT_LATEST_VERSION_MESSAGE = String.format("%s%s(للبحث عن أحدث إصدار يرجى الاتصال بالإنترنت)", Constants.ABOUT_VERSION, Constants.NEW_LINE);

    CheckBox checkHijriAuto;
    TextView viewInfo, viewHijri;
    Spinner spinnerHijriMethod, spinnerEarthModel, spinnerHijriAdjustDays;
    Button buttonSave, buttonShadow, buttonCalendar;
    ImageView wgScreenShot;
    EditText editDateGreg;

    LinearLayout linearLayoutWidget;

    ImageView ivWgConfigure;
    TextView tvWgTime, tvWgMessage;
    TextView tvWgDateHijri, tvWgDateGreg;
    TextView tvWgCell0100, tvWgCell0101, tvWgCell0102, tvWgCell0103, tvWgCell0104;
    TextView tvWgCell0200, tvWgCell0201, tvWgCell0202, tvWgCell0203, tvWgCell0204;
    TextView tvWgCell0300, tvWgCell0301, tvWgCell0302, tvWgCell0303, tvWgCell0304;
    TextView tvWgCell0400, tvWgCell0401, tvWgCell0402, tvWgCell0403, tvWgCell0404;
    TextView tvWgCell0500, tvWgCell0501, tvWgCell0502, tvWgCell0503, tvWgCell0504;
    TextView tvWgCell0600, tvWgCell0602, tvWgCell0603, tvWgCell0604;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("مواقيت الصلاة");
        //Utilities.setCenteredTitle(this, "مواقيت الصلاة");
        setFinishOnTouchOutside(false);

        View decor = getWindow().getDecorView();
        decor.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        Utilities.copyAssetsFile(this, Constants.DIR_NAME_DB + File.separator + Constants.FILE_NAME_DB_MARW, Variables.getFullNameDb(this, Constants.FILE_NAME_DB_MARW), true);
        Utilities.copyAssetsFile(this, Constants.DIR_NAME_DB + File.separator + Constants.FILE_NAME_DB_UAQ, Variables.getFullNameDb(this, Constants.FILE_NAME_DB_UAQ), true);
        Utilities.copyAssetsFile(this, Constants.DIR_NAME_DB + File.separator + Constants.FILE_NAME_DB_UHC, Variables.getFullNameDb(this, Constants.FILE_NAME_DB_UHC), true);

        hasCompass = Variables.hasCompass(this);

        tvWgTime = findViewById(R.id.wg_time);
        tvWgMessage = findViewById(R.id.wg_message);
        ivWgConfigure = findViewById(R.id.wg_image_settings);
        tvWgDateHijri = findViewById(R.id.wg_date_hijri);
        tvWgDateGreg = findViewById(R.id.wg_date_greg);

        tvWgCell0100 = findViewById(R.id.wg_cell_01_00);
        tvWgCell0101 = findViewById(R.id.wg_cell_01_01);
        tvWgCell0102 = findViewById(R.id.wg_cell_01_02);
        tvWgCell0103 = findViewById(R.id.wg_cell_01_03);
        tvWgCell0104 = findViewById(R.id.wg_cell_01_04);

        tvWgCell0200 = findViewById(R.id.wg_cell_02_00);
        tvWgCell0201 = findViewById(R.id.wg_cell_02_01);
        tvWgCell0202 = findViewById(R.id.wg_cell_02_02);
        tvWgCell0203 = findViewById(R.id.wg_cell_02_03);
        tvWgCell0204 = findViewById(R.id.wg_cell_02_04);

        tvWgCell0300 = findViewById(R.id.wg_cell_03_00);
        tvWgCell0301 = findViewById(R.id.wg_cell_03_01);
        tvWgCell0302 = findViewById(R.id.wg_cell_03_02);
        tvWgCell0303 = findViewById(R.id.wg_cell_03_03);
        tvWgCell0304 = findViewById(R.id.wg_cell_03_04);

        tvWgCell0400 = findViewById(R.id.wg_cell_04_00);
        tvWgCell0401 = findViewById(R.id.wg_cell_04_01);
        tvWgCell0402 = findViewById(R.id.wg_cell_04_02);
        tvWgCell0403 = findViewById(R.id.wg_cell_04_03);
        tvWgCell0404 = findViewById(R.id.wg_cell_04_04);

        tvWgCell0500 = findViewById(R.id.wg_cell_05_00);
        tvWgCell0501 = findViewById(R.id.wg_cell_05_01);
        tvWgCell0502 = findViewById(R.id.wg_cell_05_02);
        tvWgCell0503 = findViewById(R.id.wg_cell_05_03);
        tvWgCell0504 = findViewById(R.id.wg_cell_05_04);

        tvWgCell0600 = findViewById(R.id.wg_cell_06_00);
        tvWgCell0602 = findViewById(R.id.wg_cell_06_02);
        tvWgCell0603 = findViewById(R.id.wg_cell_06_03);
        tvWgCell0604 = findViewById(R.id.wg_cell_06_04);

        ivWgConfigure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(ActivityMain.this, ActivityWidgetConfigure.class);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, APP_WIDGET_ID);
                startActivityForResult(intent, Constants.REQUEST_CODE_WIDGET_CONFIGURE);
            }
        });

        tvWgTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateWidget(true);
            }
        });

        tvWgMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateWidget(true);
            }
        });

        tvWgDateHijri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateWidgetDate(-1);
            }
        });

        tvWgDateGreg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateWidgetDate(1);
            }
        });

        tvWgCell0600.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog("اليوم الشرعي", vBuilderDayInfo.toString());
            }
        });

        tvWgCell0403.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (vMoonIlluminationBestObservationTitle.equals(Constants.TITLE_MOON_BEST_OBSERVATION_TIME)) {

                    openDialog("رؤية الهلال", odehMessage);
                }
            }
        });

        tvWgCell0602.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (hasCompass) {

                    if (sensorManager == null) {

                        compassRestart(ActivityMain.this, latitude, longitude);

                    } else {

                        compassStop();
                        updateWidget();
                    }
                } else {
                    openDialog("تنبيه", "جهازك لا يدعم البوصلة");
                }
            }
        });

        tvWgCell0603.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (sensorManager == null) {

                    configuration.swQibla = (configuration.swQibla + 1) % WidgetConfiguration.SWITCH_QIBLA__COUNT;
                    configuration.toConfigurationFile(ActivityMain.this, APP_WIDGET_ID);

                    updateWidget();
                }
            }
        });

        tvWgCell0504.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                configuration.swPeriod = (configuration.swPeriod + 1) % WidgetConfiguration.SWITCH_PERIOD__COUNT;
                configuration.toConfigurationFile(ActivityMain.this, APP_WIDGET_ID);

                updateWidget();
            }
        });

        tvWgCell0502.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                configuration.swMoonCoordinates = (configuration.swMoonCoordinates + 1) % WidgetConfiguration.SWITCH_MOON_COORDINATES__COUNT;
                configuration.toConfigurationFile(ActivityMain.this, APP_WIDGET_ID);

                updateWidget();
            }
        });

        tvWgCell0303.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                configuration.swMoonEvents = (configuration.swMoonEvents + 1) % WidgetConfiguration.SWITCH_MOON_EVENTS__COUNT;
                configuration.toConfigurationFile(ActivityMain.this, APP_WIDGET_ID);

                updateWidget();
            }
        });

        tvWgCell0501.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                configuration.swSun = (configuration.swSun + 1) % WidgetConfiguration.SWITCH_SUN__COUNT;
                configuration.toConfigurationFile(ActivityMain.this, APP_WIDGET_ID);

                updateWidget();
            }
        });

        tvWgCell0604.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!vShadowLastPortionTitle.equals(Constants.TITLE_NIGHT_PORTION_LAST)) {

                    configuration.swShadow = (configuration.swShadow + 1) % WidgetConfiguration.SWITCH_SHADOW__COUNT;
                    configuration.toConfigurationFile(ActivityMain.this, APP_WIDGET_ID);

                    updateWidget();
                }
            }
        });

        tvWgCell0100.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                updateTime(Constants.TITLE_SUBH, vSubh, iqama[0]);
            }
        });

        tvWgCell0101.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                updateTime(vDhuhrTitle, vDhuhr, iqama[1]);
            }
        });

        tvWgCell0102.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                updateTime(Constants.TITLE_ASR, vAsr, iqama[2]);
            }
        });

        tvWgCell0103.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                updateTime(Constants.TITLE_MAGHRIB, vMaghrib, iqama[3]);
            }
        });

        tvWgCell0104.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                updateTime(Constants.TITLE_ISHA, vIsha, iqama[4]);
            }
        });

        tvWgCell0300.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                updateTime(Constants.TITLE_SUNRISE, vSunrise, 0);
            }
        });

        viewInfo = findViewById(R.id.am_view_info);
        viewInfo.setText(String.format("%s", prefLatestVersionMessage(this)));
        viewInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Variables.runAsyncVersion(ActivityMain.this);
                Toast.makeText(ActivityMain.this, "يتم الآن البحث عن نسخة جديدة...", Toast.LENGTH_LONG).show();
            }
        });

        viewHijri = findViewById(R.id.am_view_hijri);
        viewHijri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewHijri.setText(getDateHijri(getDateHijriComponents()));
            }
        });
        viewHijri.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                final int[] dateHijriComponents = getDateHijriComponents();

                if (dateHijriComponents[2] == 1) {

                    final Dialog dialog = new Dialog(ActivityMain.this, R.style.DialogStyle);
                    dialog.setContentView(R.layout.dialog_password);

                    final EditText editPassword = dialog.findViewById(R.id.dialog_password);

                    final Button buttonOk = dialog.findViewById(R.id.dialog_button_ok);
                    buttonOk.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Utilities.hideKeyboard(ActivityMain.this, v);

                            if (editPassword.getText().toString().equals(Secured.parseUploadPassword)) {

                                Variables.runAsyncHijriUpload(ActivityMain.this,
                                        dateHijriComponents[0], dateHijriComponents[1],
                                        editDateGreg.getText().toString(), prefHijriMethodFromUi(), prefHijriAdjustDaysFromUi()
                                );

                                Toast.makeText(ActivityMain.this, "[خاص] محاولة رفع إعدادات دخول الشهر الهجري", Toast.LENGTH_SHORT).show();

                                dialog.dismiss();
                            } else {
                                Toast.makeText(ActivityMain.this, "كلمة المرور غير صحيحة!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    dialog.show();
                } else {
                    Toast.makeText(ActivityMain.this, "[خاص] للرفع اختر الفاتح من الشهر الهجري", Toast.LENGTH_SHORT).show();
                }

                return false;
            }
        });

        editDateGreg = findViewById(R.id.am_edit_date_greg);
        editDateGreg.setText(Variables.getDate(Variables.getCurrentDateTime()));

        spinnerEarthModel = findViewById(R.id.am_spinner_earth_model);
        spinnerEarthModel.setAdapter(new ArrayAdapter<>(ActivityMain.this, R.layout.simple_spinner_item, new String[]{
                "نموذج صورة الأرض : إهليجي (WGS84)",
                "نموذج صورة الأرض : كروي (IUGG)"
        }));
        spinnerEarthModel.setSelection(prefEarthModelUiItemPosition());

        spinnerHijriMethod = findViewById(R.id.am_spinner_hijri_method);
        spinnerHijriMethod.setAdapter(new ArrayAdapter<>(ActivityMain.this, R.layout.simple_spinner_item, new String[]{
                "أذان الجزائر الرسمي",
                "تقويم أم القرى",
                "التقويم الهجري العالمي",
                "تقويم حسابي",
        }));
        spinnerHijriMethod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                viewHijri.setText(getDateHijri(getDateHijriComponents()));
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerHijriAdjustDays = findViewById(R.id.am_spinner_hijri_adjust_days);
        spinnerHijriAdjustDays.setAdapter(new ArrayAdapter<>(ActivityMain.this, R.layout.simple_spinner_item, new String[]{
                "+3",
                "+2",
                "+1",
                "0",
                "-1",
                "-2",
                "-3"
        }));
        spinnerHijriAdjustDays.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                viewHijri.setText(getDateHijri(getDateHijriComponents()));
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerHijriMethod.setSelection(prefHijriMethodUiItemPosition());
        spinnerHijriAdjustDays.setSelection(prefHijriAdjustDaysUiItemPosition());

        buttonSave = findViewById(R.id.am_button_save);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                savePreferences
                        (
                                ActivityMain.this,
                                prefEarthModelFromUi(),
                                prefHijriAutoFromUi(),
                                prefHijriMethodFromUi(),
                                prefHijriAdjustDaysFromUi()
                        );

                Toast.makeText(ActivityMain.this, "تم الحفظ", Toast.LENGTH_SHORT).show();
            }
        });

        buttonShadow = findViewById(R.id.am_button_shadow);
        buttonShadow.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(ActivityMain.this, ActivityShadow.class));
            }
        });

        buttonCalendar = findViewById(R.id.am_button_calendar);
        buttonCalendar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(ActivityMain.this, ActivityCalendar.class));
            }
        });

        linearLayoutWidget = findViewById(R.id.am_linear_widget);

        wgScreenShot = findViewById(R.id.wg_image_screenshot);
        wgScreenShot.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Bitmap bitmap = Utilities.createBitmapFromView(linearLayoutWidget);
                new AsyncSaveScreenshot(ActivityMain.this, bitmap, "widget").doInBackground();
            }
        });

        checkHijriAuto = findViewById(R.id.am_check_hijri_auto);
        checkHijriAuto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Variables.runAsyncHijriAutoUpdate(ActivityMain.this);
                }
            }
        });

        checkHijriAuto.setChecked(prefHijriAuto(this));

        Variables.runAsyncVersion(this);
    }

    void compassStop() {
        compassTrueAzimuth = 0;
        geomagneticField = null;
        if (sensorManager == null) return;
        sensorManager.unregisterListener(this);
        sensorManager = null;
    }

    void compassRestart(Context context, double latitude, double longitude) {

        compassStop();

        geomagneticField = new GeomagneticField((float) latitude, (float) longitude, 0, System.currentTimeMillis());
        sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);

        hasRotationVector = sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
    }

    private void openDialog(String title, String message) {
        Intent intent = new Intent(ActivityMain.this, ActivityDialog.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, APP_WIDGET_ID);
        intent.putExtra(ActivityDialog.EXTRA_DIALOG_TITLE, title);
        intent.putExtra(ActivityDialog.EXTRA_DIALOG_MESSAGE, message);
        startActivity(intent);
    }

    private void updateWidgetDate(int sign) {
        configuration.wDateTimeNow = false;
        configuration.wDateTimeCustom = Utilities.adjustDateGreg(configuration.wDateTimeCustom, configuration.navigateFieldCalendar(), sign * configuration.wNavigateAmount, true);
        configuration.toConfigurationFile(ActivityMain.this, APP_WIDGET_ID);

        updateWidget();
    }

    private void updateTime(String pName, String pTime, int pIqama) {

        String datetime = configuration.wDateTimeNow ? Variables.getCurrentDateTime() : configuration.wDateTimeCustom;
        String time = Variables.getTime(datetime);

        updateWidget();

        tvWgMessage.setText(Utilities.timeG(Utilities.secondsOf(time), pTime, pIqama, pName, configuration.wLongFormat));
    }

    private void updateWidget() {
        updateWidget(false);
    }

    private void updateWidget(boolean resetToNow) {

        configuration = WidgetConfiguration.fromConfigurationFile(this, APP_WIDGET_ID);

        if (resetToNow && !configuration.wDateTimeNow) {
            configuration.wDateTimeNow = true;
            configuration.toConfigurationFile(this, APP_WIDGET_ID);
        }

        iqama = Utilities.iqamaArrayOf(configuration.cLocationIqama);
        int[] offset = Utilities.offsetArrayOf(configuration.cLocationOffset);

        int cHijriMethod = ActivityMain.prefHijriMethod(this);
        int cHijriAdjust = ActivityMain.prefHijriAdjustDays(this);
        boolean cQiblaAngle = configuration.swQibla == WidgetConfiguration.SWITCH_QIBLA_ANGLE;
        boolean cShadowLength = configuration.swShadow == WidgetConfiguration.SWITCH_SHADOW_LENGTH;
        boolean cIshaEndMid = configuration.pIshaEnd == WidgetConfiguration.ISHA_END_MID;

        setTextSize(configuration.wLongFormat, configuration.wTextSizePercent);

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

        vDhuhrTitle = isFriday ? Constants.TITLE_JUMUAA : Constants.TITLE_DHUHR;

        latitude = Utilities.valueOf(configuration.cLocationCalcParams, 0, 10, true);
        longitude = Utilities.valueOf(configuration.cLocationCalcParams, 1, 10, true);
        TimeZone timezone = Utilities.timezoneCalcCity(configuration.cLocationCalcParams.substring(22));

        decimalFormatter = configuration.wLongFormat ? Variables.decimalFormatter3 : Variables.decimalFormatter2;

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

        String vMoonIlluminationBestObservationTime = Constants.INVALID_STRING;
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

        vSunrise = SunMoonCalculator.getTime(smc.sun.rise, timezone, configuration.wLongFormat);
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

        String dateHijri = Utilities.toDateHijri(date, cHijriAdjust, cHijriMethod, this, false, null, null);
        String dateHijriCorrected = Utilities.toDateHijri(date, cHijriAdjust, cHijriMethod, this, true, time, vSunset);

        boolean prayersOfShownDate = true;
        String vDateHijri = dateHijri, vDateGreg = date;

        switch (configuration.pPrayersMethod) {

            default:
            case WidgetConfiguration.PRAYERS_METHOD_ACTUAL:

                try {

                    String[] row = HijriMARW.times(this, configuration.cLocationActualId, date);

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

        String vShadowLastPortion;

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

        qiblaAzimuth = EarthCalculation.qiblaDegrees(ActivityMain.prefEarthModel(this), latitude, longitude);

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

        Spanned vSunsetDay = Html.fromHtml(vSunsetDayTitle + "<br/>" + Constants.INVALID_STRING);

        vBuilderDayInfo = new StringBuilder();

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

        tvWgMessage.setText(wgMessage[1]);
        tvWgTime.setText(time);
        tvWgDateHijri.setText(vDateHijri);
        tvWgDateGreg.setText(vDateGreg);

        tvWgCell0100.setText(Variables.widgetCellText(Constants.TITLE_SUBH, vSubh));
        tvWgCell0101.setText(Variables.widgetCellText(vDhuhrTitle, vDhuhr));
        tvWgCell0102.setText(Variables.widgetCellText(Constants.TITLE_ASR, vAsr));
        tvWgCell0103.setText(Variables.widgetCellText(Constants.TITLE_MAGHRIB, vMaghrib));
        tvWgCell0104.setText(Variables.widgetCellText(Constants.TITLE_ISHA, vIsha));

        tvWgCell0200.setText(Variables.widgetCellText(Constants.TITLE_FAJR, vFajr));
        tvWgCell0201.setText(Variables.widgetCellText(Constants.TITLE_ZAWAL, vZawal));
        tvWgCell0202.setText(Variables.widgetCellText(Constants.TITLE_ASR, vAsrCalc));
        tvWgCell0203.setText(Variables.widgetCellText(Constants.TITLE_SUNSET, vSunset));
        tvWgCell0204.setText(Variables.widgetCellText(Constants.TITLE_GHASAQ, vGhasaq));

        tvWgCell0300.setText(Variables.widgetCellText(Constants.TITLE_SUNRISE, vSunrise));
        tvWgCell0301.setText(Variables.widgetCellText(Constants.TITLE_DHUHA_END, vTakabud));
        tvWgCell0302.setText(Variables.widgetCellText(Constants.TITLE_ISFIRAR, vIsfirar));
        tvWgCell0303.setText(Variables.widgetCellText(vMoonEventsTitle, vMoonEvents));
        tvWgCell0304.setText(Variables.widgetCellText(Constants.TITLE_ISHA_END, vIshaEnd));

        tvWgCell0400.setText(Variables.widgetCellText(Constants.TITLE_DHUHA, vIrtifae));
        tvWgCell0401.setText(Variables.widgetCellText(vIstiwaeTitle, vIstiwae));
        tvWgCell0402.setText(Variables.widgetCellText(vHourTitle, vHour));
        tvWgCell0403.setText(Variables.widgetCellText(vMoonIlluminationBestObservationTitle, vMoonIlluminationBestObservationTime));
        tvWgCell0404.setText(Variables.widgetCellText(Constants.TITLE_NIGHT_THIRD_LAST, vLastThird));

        tvWgCell0500.setText(Variables.widgetCellText(Constants.TITLE_SUBH_AWAL, vSubhAwal));
        tvWgCell0501.setText(Variables.widgetCellText(vSunTitle, vSun));
        tvWgCell0502.setText(Variables.widgetCellText(vMoonCoordinatesTitle, vMoonCoordinates));
        tvWgCell0503.setText(Variables.widgetCellText(Constants.TITLE_MOON_AGE, vMoonAge));
        tvWgCell0504.setText(Variables.widgetCellText(vPeriodTitle, vPeriod));

        tvWgCell0600.setText(vSunsetDay);
        tvWgCell0602.setText(Variables.widgetCellText(Constants.TITLE_COMPASS, vCompassAzimuth));
        tvWgCell0603.setText(Variables.widgetCellText(vQiblaTitle, vQibla));
        tvWgCell0604.setText(Variables.widgetCellText(vShadowLastPortionTitle, vShadowLastPortion));

        /* Moon Age Visibility colors */

        odehMessage = "معيار عودة : ";
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

        tvWgCell0403.setTextColor(crescentColor);
        tvWgCell0503.setTextColor(crescentColor);

        /* vLastHour color */

        if (isFriday && inDayTimeSun && nextHourRank == 12 && timeSeconds >= nextHourSeconds) {
            tvWgCell0402.setTextColor(Variables.colorGreen);
        } else {
            tvWgCell0402.setTextColor(Variables.colorWhite);
        }

        /* vLastThird color */

        if (inLastThird) {
            tvWgCell0404.setTextColor(Variables.colorGreen);
        } else {
            tvWgCell0404.setTextColor(Variables.colorWhite);
        }

        /* EarthCalculation & Compass color */

        tvWgCell0602.setBackgroundColor(Variables.colorTransparent);
        tvWgCell0603.setBackgroundColor(Variables.colorTransparent);

        /* 5 Prayers colors */

        if (prayersOfShownDate) {
            tvWgCell0100.setTextColor(Variables.colorWhite);
            tvWgCell0101.setTextColor(Variables.colorWhite);
            tvWgCell0102.setTextColor(Variables.colorWhite);
            tvWgCell0103.setTextColor(Variables.colorWhite);
            tvWgCell0104.setTextColor(Variables.colorWhite);
        } else {
            tvWgCell0100.setTextColor(Variables.colorOrange);
            tvWgCell0101.setTextColor(Variables.colorOrange);
            tvWgCell0102.setTextColor(Variables.colorOrange);
            tvWgCell0103.setTextColor(Variables.colorOrange);
            tvWgCell0104.setTextColor(Variables.colorOrange);
        }

        switch (wgMessage[0]) {
            case Constants.TITLE_SUBH:
                tvWgCell0100.setTextColor(Variables.colorBlue);
                break;
            case Constants.TITLE_DHUHR:
                tvWgCell0101.setTextColor(Variables.colorBlue);
                break;
            case Constants.TITLE_ASR:
                tvWgCell0102.setTextColor(Variables.colorBlue);
                break;
            case Constants.TITLE_MAGHRIB:
                tvWgCell0103.setTextColor(Variables.colorBlue);
                break;
            case Constants.TITLE_ISHA:
                tvWgCell0104.setTextColor(Variables.colorBlue);
                break;
        }

        /* between times colors */

        tvWgCell0200.setTextColor(Variables.colorWhite);
        tvWgCell0300.setTextColor(Variables.colorWhite);

        tvWgCell0400.setTextColor(Variables.colorWhite);

        tvWgCell0201.setTextColor(Variables.colorWhite);
        tvWgCell0301.setTextColor(Variables.colorWhite);

        tvWgCell0202.setTextColor(Variables.colorWhite);
        tvWgCell0302.setTextColor(Variables.colorWhite);

        tvWgCell0203.setTextColor(Variables.colorWhite);

        if (timeSeconds >= Utilities.secondsOf(vFajr)) {
            if (timeSeconds < Utilities.secondsOf(vSunrise)) {
                tvWgCell0200.setTextColor(Variables.colorOrange);
                tvWgCell0300.setTextColor(Variables.colorOrange);
            } else if (timeSeconds < Utilities.secondsOf(vIrtifae)) {
                tvWgCell0300.setTextColor(Variables.colorRed);
                tvWgCell0400.setTextColor(Variables.colorRed);
            } else if (timeSeconds >= Utilities.secondsOf(vTakabud)) {
                if (timeSeconds < Utilities.secondsOf(vZawal)) {
                    tvWgCell0201.setTextColor(Variables.colorRed);
                    tvWgCell0301.setTextColor(Variables.colorRed);
                } else if (timeSeconds >= Utilities.secondsOf(vAsrCalc)) {
                    if (timeSeconds < Utilities.secondsOf(vIsfirar)) {
                        tvWgCell0202.setTextColor(Variables.colorOrange);
                        tvWgCell0302.setTextColor(Variables.colorOrange);
                    } else if (timeSeconds < Utilities.secondsOf(vSunset)) {
                        tvWgCell0203.setTextColor(Variables.colorRed);
                        tvWgCell0302.setTextColor(Variables.colorRed);
                    }
                }
            }
        }

        if (Variables.isDayOfWeek(date, Calendar.WEDNESDAY) &&
                timeSeconds >= Utilities.secondsOf(vZawal) &&
                timeSeconds < Utilities.secondsOf(vAsrCalc)) {
            tvWgCell0201.setTextColor(Variables.colorGreen);
            tvWgCell0202.setTextColor(Variables.colorGreen);
        }
    }

    private void setTextSize(boolean longFormat, int sizePercent) {

        float sizeMultiplier = (float) (sizePercent / 100.0);

        float sizeHeader = 25 * sizeMultiplier;
        float sizeHeaderMessage = longFormat ? 22 * sizeMultiplier : sizeHeader;

        float sizeCell = (float) 13.7 * sizeMultiplier;
        float sizeFooter = 24 * sizeMultiplier;

        tvWgTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeHeader);
        tvWgMessage.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeHeaderMessage);

        tvWgCell0100.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeCell);
        tvWgCell0101.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeCell);
        tvWgCell0102.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeCell);
        tvWgCell0103.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeCell);
        tvWgCell0104.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeCell);

        tvWgCell0200.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeCell);
        tvWgCell0201.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeCell);
        tvWgCell0202.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeCell);
        tvWgCell0203.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeCell);
        tvWgCell0204.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeCell);

        tvWgCell0300.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeCell);
        tvWgCell0301.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeCell);
        tvWgCell0302.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeCell);
        tvWgCell0303.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeCell);
        tvWgCell0304.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeCell);

        tvWgCell0400.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeCell);
        tvWgCell0401.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeCell);
        tvWgCell0402.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeCell);
        tvWgCell0403.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeCell);
        tvWgCell0404.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeCell);

        tvWgCell0500.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeCell);
        tvWgCell0501.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeCell);
        tvWgCell0502.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeCell);
        tvWgCell0503.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeCell);
        tvWgCell0504.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeCell);

        tvWgCell0600.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeCell);
        tvWgCell0602.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeCell);
        tvWgCell0603.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeCell);
        tvWgCell0604.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeCell);

        tvWgDateHijri.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeFooter);
        tvWgDateGreg.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeFooter);
    }

    public static void updateHijri(Context context, int hijriMethod, int hijriAdjust) {

        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();

        prefs.putInt(PREF_HIJRI_METHOD, hijriMethod);
        prefs.putInt(PREF_HIJRI_ADJUST_DAYS, hijriAdjust);

        prefs.apply();
    }

    public static void updateLatestVersion(Context context, String latestVersionMessage) {

        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();

        prefs.putString(PREF_LATEST_VERSION_MESSAGE, latestVersionMessage);

        prefs.apply();
    }

    public static void savePreferences
            (
                    Context context,
                    int earthModel,
                    boolean hijriAuto,
                    int hijriMethod,
                    int hijriAdjust
            ) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();

        prefs.putInt(PREF_EARTH_MODEL, earthModel);
        prefs.putBoolean(PREF_HIJRI_AUTO, hijriAuto);
        prefs.putInt(PREF_HIJRI_METHOD, hijriMethod);
        prefs.putInt(PREF_HIJRI_ADJUST_DAYS, hijriAdjust);

        prefs.apply();
    }

    String getDateHijri(int[] dateHijriComponents) {

        if (dateHijriComponents == null) return Constants.INVALID_STRING;

        String date = editDateGreg.getText().toString();
        String dayOfWeek = Variables.getDayOfWeek(Variables.getDayOfWeekInt(date, false));

        int yearHijri = dateHijriComponents[0];
        String monthNameHijri = Utilities.getMonthNameHijri(dateHijriComponents[1]);
        int dayHijri = dateHijriComponents[2];

        return String.format(Locale.ROOT, "%s %02d %s \u200F%04dهـ", dayOfWeek, dayHijri, monthNameHijri, yearHijri);
    }

    int[] getDateHijriComponents() {

        String date = editDateGreg.getText().toString();
        String dateHijri = Utilities.toDateHijri(date, prefHijriAdjustDaysFromUi(), prefHijriMethodFromUi(), ActivityMain.this, false, null, null);

        if (dateHijri.equals(Constants.INVALID_STRING)) return null;


        return Utilities.dateComponents(dateHijri);
    }

    public static int prefEarthModel(Context context) {

        return context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getInt(PREF_EARTH_MODEL, DEFAULT_EARTH_MODEL);
    }

    public static String prefLatestVersionMessage(Context context) {

        return context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString(PREF_LATEST_VERSION_MESSAGE, DEFAULT_LATEST_VERSION_MESSAGE);
    }

    int prefEarthModelUiItemPosition() {
        return prefEarthModel(this);
    }

    int prefEarthModelFromUi() {
        return spinnerEarthModel.getSelectedItemPosition();
    }

    public static boolean prefHijriAuto(Context context) {

        return context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getBoolean(PREF_HIJRI_AUTO, DEFAULT_HIJRI_AUTO);
    }

    boolean prefHijriAutoFromUi() {

        return checkHijriAuto.isChecked();
    }

    public static int prefHijriMethod(Context context) {

        return context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getInt(PREF_HIJRI_METHOD, DEFAULT_HIJRI_METHOD);
    }

    int prefHijriMethodUiItemPosition() {
        return prefHijriMethod(this);
    }

    int prefHijriMethodFromUi() {
        return spinnerHijriMethod.getSelectedItemPosition();
    }

    public static int prefHijriAdjustDays(Context context) {

        return context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getInt(PREF_HIJRI_ADJUST_DAYS, DEFAULT_HIJRI_ADJUST_DAYS);
    }

    int prefHijriAdjustDaysUiItemPosition() {
        return 3 - prefHijriAdjustDays(this);
    }

    int prefHijriAdjustDaysFromUi() {
        return 3 - spinnerHijriAdjustDays.getSelectedItemPosition();
    }

    @Override
    public void onVersion(String result) {
        viewInfo.setText(String.format("%s", prefLatestVersionMessage(this)));
    }

    @Override
    public void onLocation(String[] result) {

    }

    @Override
    public void onHijriAutoUpdated(int hijriMethod, int hijriAdjust) {

        spinnerHijriMethod.setSelection(prefHijriMethodUiItemPosition());
        spinnerHijriAdjustDays.setSelection(prefHijriAdjustDaysUiItemPosition());

        Toast.makeText(ActivityMain.this, "تم تحديث التاريخ الهجري", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onHijriUploaded() {
        Toast.makeText(ActivityMain.this, "تم رفع إعدادات دخول الشهر الهجري", Toast.LENGTH_SHORT).show();
    }

    private float computeTrueNorthAzimuth(float magneticAzimuthRadians) {

        float trueNorthAzimuth = (float) Math.toDegrees(magneticAzimuthRadians) + 360;

        if (geomagneticField != null) {
            trueNorthAzimuth += geomagneticField.getDeclination(); // trueAzimuth = magneticAzimuth + declination
        }

        return trueNorthAzimuth % 360;
    }

    private int computeCompassAccuracy(float[] geomagnetic) {

        if (geomagneticField == null) return SENSOR_STATUS_UNRELIABLE;

        float magneticFieldStrength = (float) Math.sqrt(geomagnetic[0] * geomagnetic[0] + geomagnetic[1] * geomagnetic[1] + geomagnetic[2] * geomagnetic[2]);
        float magneticFieldStrengthRatio = 1000 * magneticFieldStrength / geomagneticField.getFieldStrength(); // [0..1]

        return Math.round(magneticFieldAccuracy * magneticFieldStrengthRatio);
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
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                float[] geomagnetic = null;
                geomagnetic = Utilities.applyLowPassFilter(event.values, geomagnetic);
                compassAccuracy = computeCompassAccuracy(geomagnetic);
            }

        } else {

            float[] gravity = null;
            float[] geomagnetic = null;

            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                gravity = Utilities.applyLowPassFilter(event.values, gravity);
            } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                geomagnetic = Utilities.applyLowPassFilter(event.values, geomagnetic);
                compassAccuracy = computeCompassAccuracy(geomagnetic);
            }

            if (gravity != null && geomagnetic != null) {
                float[] inclination = new float[9];
                if (SensorManager.getRotationMatrix(rotation, inclination, gravity, geomagnetic)) {
                    SensorManager.getOrientation(rotation, orientation);
                    compassTrueAzimuth = computeTrueNorthAzimuth(orientation[0]); // orientation[0] : magneticAzimuth : [-180, 180]
                }
            }
        }

        String colorCompassString = Constants.WG_TEXTCOLOR_RED;

        switch (compassAccuracy) {
            case SENSOR_STATUS_ACCURACY_HIGH:
                colorCompassString = Constants.WG_TEXTCOLOR_GREEN;
                break;
            case SENSOR_STATUS_ACCURACY_MEDIUM:
                colorCompassString = Constants.WG_TEXTCOLOR_ORANGE;
                break;
        }

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

        tvWgCell0602.setBackgroundColor(colorCompass);
        tvWgCell0603.setBackgroundColor(colorQibla);

        String vCompassAzimuth = Constants.SYMBOL_DEGREES + Variables.decimalFormatter0.format(compassTrueAzimuth);

        tvWgCell0602.setText(Variables.widgetCellText(Constants.TITLE_COMPASS, vCompassAzimuth));
        tvWgCell0603.setText(Variables.widgetCellText(Constants.TITLE_QIBLA_ANGLE, vDiffQibla));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

        if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magneticFieldAccuracy = accuracy;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateWidget();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (sensorManager != null) {
            compassStop();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_CODE_WIDGET_CONFIGURE) {
            if (resultCode == Activity.RESULT_OK) {
                //updateWidget(); is called by onResume()
            }
        }
    }
}

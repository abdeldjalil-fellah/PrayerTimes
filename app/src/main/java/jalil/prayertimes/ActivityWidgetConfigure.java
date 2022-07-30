package jalil.prayertimes;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;

import java.util.Locale;

public class ActivityWidgetConfigure extends AppCompatActivity
        implements LocationListener, IAsyncCompleted {

    int appWidgetId = -1;

    Dialog dialog;
    WidgetConfiguration configuration;

    CheckBox checkLong;
    AutoCompleteTextView editActualCity;

    RadioButton radioDateTimeNow, radioDateTimeCustom;
    EditText editTextsize, editDateTime, editIqama, editCalcLocation, editCalcParams, editOffset, editNavigateAmount;
    Spinner spinnerPrayersMethod, spinnerAsr, spinnerIstiwae, spinnerNightPeriod;
    Spinner spinnerIrtifae, spinnerIsfirar, spinnerTakabud, spinnerZawal;
    Spinner spinnerFajr, spinnerGhasaq, spinnerIshaEnd, spinnerNavigateField;

    Button buttonCalcLocation, buttonSave, buttonFavExport, buttonFavImport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_configure);

        View decor = getWindow().getDecorView();
        decor.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        }

        if (appWidgetId < ActivityMain.APP_WIDGET_ID) {
            finish();
        }

        configuration = WidgetConfiguration.fromConfigurationFile(ActivityWidgetConfigure.this, appWidgetId);

        setTitle(String.format(Locale.ROOT, "إعدادات الويجات [ID:%d]", appWidgetId));

        editActualCity = findViewById(R.id.wgc_edit_actual_city);

        editTextsize = findViewById(R.id.wgc_edit_textsize_percent);
        editNavigateAmount = findViewById(R.id.wgc_edit_navigate_amount);

        editActualCity.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, HijriMARW.cities(this)));
        //editActualCity.requestFocus();

        buttonCalcLocation = findViewById(R.id.wgc_button_calc_city);
        buttonCalcLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dialog = Utilities.createLocationDialog(ActivityWidgetConfigure.this, "ar");
                dialog.show();
            }
        });

        editCalcLocation = findViewById(R.id.wgc_edit_calc_city);
        // latitude/longitude/timezoneOffset : +36.766518/+03.054137/+01:00 : [28]

        editCalcParams = findViewById(R.id.wgc_edit_calc_params);
        // fajrAngle/qarahaHorizonValue/qarahaZawalValue/asrShadowFactor/shafaqAngle : 18.00/03.50/00.50/01.00/17.00 : [29]

        editIqama = findViewById(R.id.wgc_edit_iqama);
        // cLocationIqama in minutes : subh/dhuhr/pAsr/maghrib/isha : 20/15/15/10/10 : [14]

        editOffset = findViewById(R.id.wgc_edit_offset);
        // prayers cLocationOffset in minutes : subh/subhAwal/dhuhr/pAsr/maghrib/isha : +00/-20/+00/+00/+00/+00 : [23]

        editDateTime = findViewById(R.id.wgc_edit_datetime);
        // custom datetime : yyyy-MM-dd HH:mm:ss : 2018-11-18 13:51:54 : [19]

        spinnerPrayersMethod = findViewById(R.id.wgc_spinner_prayers_method);
        spinnerPrayersMethod.setAdapter(new ArrayAdapter<>(ActivityWidgetConfigure.this, R.layout.simple_spinner_item, new String[]{
                "الصلوات الخمس : أذان الجزائر الرسمي",
                "الصلوات الخمس : PrayTimes.org",
                "الصلوات الخمس : السطر الثاني",
        }));

        spinnerIstiwae = findViewById(R.id.wgc_spinner_istiwae);
        spinnerIstiwae.setAdapter(new ArrayAdapter<>(ActivityWidgetConfigure.this, R.layout.simple_spinner_item, new String[]{
                "الاستواء : المتأخر",
                "الاستواء : العبور",
                "الاستواء : التكبد"
        }));

        spinnerAsr = findViewById(R.id.wgc_spinner_asr);
        spinnerAsr.setAdapter(new ArrayAdapter<>(ActivityWidgetConfigure.this, R.layout.simple_spinner_item, new String[]{
                "العصر : طول الظل",
                "العصر : عرض الظل",
                "العصر : المنتصف"
        }));

        spinnerNightPeriod = findViewById(R.id.wgc_spinner_night_period);
        spinnerNightPeriod.setAdapter(new ArrayAdapter<>(ActivityWidgetConfigure.this, R.layout.simple_spinner_item, new String[]{
                "الليل : صيامي",
                "الليل : قيامي",
                "الليل : شروقي"
        }));

        spinnerIrtifae = findViewById(R.id.wgc_spinner_irtifae);
        spinnerIrtifae.setAdapter(new ArrayAdapter<>(ActivityWidgetConfigure.this, R.layout.simple_spinner_item, new String[]{
                "الضحى : درجة",
                "الضحى : قرص",
                "الضحى : دقيقة",
                "الضحى : ساعة"
        }));

        spinnerIsfirar = findViewById(R.id.wgc_spinner_isfirar);
        spinnerIsfirar.setAdapter(new ArrayAdapter<>(ActivityWidgetConfigure.this, R.layout.simple_spinner_item, new String[]{
                "آخر العصر : درجة",
                "آخر العصر : قرص",
                "آخر العصر : دقيقة",
                "آخر العصر : ساعة"
        }));

        spinnerTakabud = findViewById(R.id.wgc_spinner_takabud);
        spinnerTakabud.setAdapter(new ArrayAdapter<>(ActivityWidgetConfigure.this, R.layout.simple_spinner_item, new String[]{
                "آخر الضحى : درجة",
                "آخر الضحى : قرص",
                "آخر الضحى : دقيقة",
                "آخر الضحى : ظل"
        }));

        spinnerZawal = findViewById(R.id.wgc_spinner_zawal);
        spinnerZawal.setAdapter(new ArrayAdapter<>(ActivityWidgetConfigure.this, R.layout.simple_spinner_item, new String[]{
                "الزوال : درجة",
                "الزوال : قرص",
                "الزوال : دقيقة",
                "الزوال : ظل"
        }));

        spinnerFajr = findViewById(R.id.wgc_spinner_fajr);
        spinnerFajr.setAdapter(new ArrayAdapter<>(ActivityWidgetConfigure.this, R.layout.simple_spinner_item, new String[]{
                "الفجر : زاوية",
                "الفجر : ساعة",
                "الفجر : مونسايتنج"
        }));

        spinnerGhasaq = findViewById(R.id.wgc_spinner_ghasaq);
        spinnerGhasaq.setAdapter(new ArrayAdapter<>(ActivityWidgetConfigure.this, R.layout.simple_spinner_item, new String[]{
                "غروب الشفق : زاوية",
                "غروب الشفق : ساعة",
                "غروب الشفق : ش.عام",
                "غروب الشفق : ش.أحمر",
                "غروب الشفق : ش.أبيض"
        }));

        spinnerIshaEnd = findViewById(R.id.wgc_spinner_isha_end);
        spinnerIshaEnd.setAdapter(new ArrayAdapter<>(ActivityWidgetConfigure.this, R.layout.simple_spinner_item, new String[]{
                "آخر العشاء : نصف الليل",
                "آخر العشاء : ثلث الليل"
        }));

        spinnerNavigateField = findViewById(R.id.wgc_spinner_navigate_field);
        spinnerNavigateField.setAdapter(new ArrayAdapter<>(ActivityWidgetConfigure.this, R.layout.simple_spinner_item, new String[]{
                "ثانية",
                "دقيقة",
                "ساعة",
                "يوم",
                "شهر",
                "سنة"
        }));

        radioDateTimeNow = findViewById(R.id.wgc_radio_datetime_current);

        radioDateTimeCustom = findViewById(R.id.wgc_radio_datetime_custom);
        radioDateTimeCustom.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                editDateTime.setText(Variables.getCurrentDateTime());
                return true;
            }
        });

        checkLong = findViewById(R.id.wgc_check_long);

        buttonSave = findViewById(R.id.wgc_button_save);
        buttonSave.setText(String.format(Locale.ROOT, "موافق [ID:%d]", appWidgetId));
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controlsToConfiguration().toConfigurationFile(ActivityWidgetConfigure.this, appWidgetId);

                if (appWidgetId > ActivityMain.APP_WIDGET_ID) {
                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(ActivityWidgetConfigure.this);
                    AppWidgetMain.updateAppWidget(ActivityWidgetConfigure.this, appWidgetManager, appWidgetId);
                }

                Intent intent = new Intent();
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                ActivityWidgetConfigure.this.setResult(RESULT_OK, intent);

                ActivityWidgetConfigure.this.finish();
            }
        });

        buttonFavExport = findViewById(R.id.wgc_button_favorite_export);
        buttonFavExport.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                WidgetConfiguration widgetConfiguration = new WidgetConfiguration();
                widgetConfiguration.cLocationActualId = 32;
                widgetConfiguration.cLocationActualName = "عين الدفلى";
                widgetConfiguration.cLocationCalcName = "زدين مركز";
                widgetConfiguration.cLocationCalcParams = "+36.155699/+01.821287/+01:00";
                widgetConfiguration.cLocationIqama = "20/15/15/05/10";
                widgetConfiguration.cLocationOffset = "+00/-20/+00/+00/+00/+00";

                if (widgetConfiguration.toFavoriteFile(ActivityWidgetConfigure.this, Constants.FILE_NAME_FAV_SAMPLE)) {
                    Toast.makeText(ActivityWidgetConfigure.this, "تم إضافة [" + widgetConfiguration.cLocationCalcName + "] إلى المفضلة", Toast.LENGTH_SHORT).show();
                    return true;
                } else {
                    return false;
                }
            }
        });
        buttonFavExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ActivityWidgetConfigure.this);

                TextView titleExport = new TextView(ActivityWidgetConfigure.this);
                titleExport.setText("إضافة إلى المفضلة");
                titleExport.setGravity(Gravity.CENTER);
                titleExport.setTextSize(18);

                titleExport.setPadding(10, 15, 15, 10);
                alertDialogBuilder.setCustomTitle(titleExport);

                final EditText editFileNameExport = new EditText(ActivityWidgetConfigure.this);
                editFileNameExport.setInputType(InputType.TYPE_CLASS_TEXT);
                editFileNameExport.setHint("الاسم");
                editFileNameExport.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
                editFileNameExport.setText(buttonCalcLocation.getText());

                final LinearLayoutCompat layoutExport = new LinearLayoutCompat(ActivityWidgetConfigure.this);
                layoutExport.setOrientation(LinearLayoutCompat.VERTICAL);
                layoutExport.addView(editFileNameExport);

                alertDialogBuilder.setView(layoutExport);

                alertDialogBuilder.setPositiveButton("حفظ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Utilities.hideKeyboard(ActivityWidgetConfigure.this, layoutExport);

                        if (controlsToConfiguration().toFavoriteFile(ActivityWidgetConfigure.this, editFileNameExport.getText().toString())) {
                            Toast.makeText(ActivityWidgetConfigure.this, "تمت إضافة [" + editFileNameExport.getText().toString() + "] إلى المفضلة", Toast.LENGTH_LONG).show();
                        }
                    }
                });

                alertDialogBuilder.setNegativeButton("إلغاء", null);

                alertDialogBuilder.show();
            }
        });

        buttonFavImport = findViewById(R.id.wgc_button_favorite_import);
        buttonFavImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Dialog dialog = new Dialog(ActivityWidgetConfigure.this, R.style.DialogStyle);
                dialog.setContentView(R.layout.dialog_favorite);

                final RadioGroup radioGroup = dialog.findViewById(R.id.dialog_fav_radio_group);
                final Button buttonLoadLoc = dialog.findViewById(R.id.dialog_fav_load_loc);
                final Button buttonLoadParams = dialog.findViewById(R.id.dialog_fav_load_params);
                final Button buttonDelete = dialog.findViewById(R.id.dialog_fav_delete);

                final String[] selectedFile = {Constants.FAV_DEFAULT};
                final String[] files = Variables.getFavList(ActivityWidgetConfigure.this);

                for (String file : files) {
                    RadioButton radioButton = new RadioButton(ActivityWidgetConfigure.this);
                    radioButton.setText(file);
                    radioGroup.addView(radioButton);

                    if (file.equals(Constants.FAV_DEFAULT)) radioButton.setChecked(true);
                }

                radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {

                        RadioButton radioButton = group.findViewById(checkedId);
                        selectedFile[0] = radioButton.getText().toString();
                    }
                });

                buttonLoadLoc.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (selectedFile[0].equals(Constants.FAV_DEFAULT)) {
                            configurationToControlsDefaults(true, false, false);
                        } else {
                            configurationToControls(WidgetConfiguration.fromFavoriteFile(ActivityWidgetConfigure.this, selectedFile[0]), true, false, false);
                        }

                        dialog.dismiss();
                    }
                });

                buttonLoadParams.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (selectedFile[0].equals(Constants.FAV_DEFAULT)) {
                            configurationToControlsDefaults(false, true, false);
                        } else {
                            configurationToControls(WidgetConfiguration.fromFavoriteFile(ActivityWidgetConfigure.this, selectedFile[0]), false, true, false);
                        }

                        dialog.dismiss();
                    }
                });

                buttonDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!selectedFile[0].equals(Constants.FAV_DEFAULT)) {

                            if (Utilities.deleteFavorite(ActivityWidgetConfigure.this, selectedFile[0])) {
                                Toast.makeText(ActivityWidgetConfigure.this, "تم حذف [" + selectedFile[0] + "] من المفضلة", Toast.LENGTH_LONG).show();
                            }

                            dialog.dismiss();
                        }
                    }
                });

                dialog.show();
            }
        });

        configurationToControls(configuration, true, true, true);
    }

    WidgetConfiguration controlsToConfiguration() {

        configuration.cLocationActualName = editActualCity.getText().toString();
        configuration.cLocationActualId = HijriMARW.cityId(this, configuration.cLocationActualName);

        configuration.cLocationCalcParams = editCalcLocation.getText().toString();
        configuration.cLocationCalcName = buttonCalcLocation.getText().toString();
        configuration.cLocationIqama = editIqama.getText().toString();
        configuration.cLocationOffset = editOffset.getText().toString();

        configuration.pPrayersMethod = spinnerPrayersMethod.getSelectedItemPosition();
        configuration.pCalcParams = editCalcParams.getText().toString();
        configuration.pIstiwae = spinnerIstiwae.getSelectedItemPosition();
        configuration.pNightPeriod = spinnerNightPeriod.getSelectedItemPosition();
        configuration.pIrtifae = spinnerIrtifae.getSelectedItemPosition();
        configuration.pIsfirar = spinnerIsfirar.getSelectedItemPosition();
        configuration.pTakabud = spinnerTakabud.getSelectedItemPosition();
        configuration.pZawal = spinnerZawal.getSelectedItemPosition();
        configuration.pFajr = spinnerFajr.getSelectedItemPosition();
        configuration.pGhasaq = spinnerGhasaq.getSelectedItemPosition();
        configuration.pIshaEnd = spinnerIshaEnd.getSelectedItemPosition();
        configuration.pAsr = spinnerAsr.getSelectedItemPosition();

        configuration.wDateTimeNow = radioDateTimeNow.isChecked();
        configuration.wDateTimeCustom = editDateTime.getText().toString();
        configuration.wLongFormat = checkLong.isChecked();
        configuration.wNavigateField = spinnerNavigateField.getSelectedItemPosition();
        configuration.wNavigateAmount = (int) Utilities.toDouble(editNavigateAmount.getText().toString(), false);
        configuration.wTextSizePercent = (int) Utilities.toDouble(editTextsize.getText().toString(), false);

        return configuration;
    }

    void configurationToControlsDefaults(boolean location, boolean params, boolean others) {
        configurationToControls(new WidgetConfiguration(), location, params, others);
    }

    void configurationToControls(WidgetConfiguration configuration, boolean location, boolean params, boolean others) {

        if (location) {
            editActualCity.setText(configuration.cLocationActualName);
            editCalcLocation.setText(configuration.cLocationCalcParams);
            buttonCalcLocation.setText(configuration.cLocationCalcName);
            editOffset.setText(configuration.cLocationOffset);
            editIqama.setText(configuration.cLocationIqama);
        }

        if (params) {
            editCalcParams.setText(configuration.pCalcParams);
            spinnerFajr.setSelection(configuration.pFajr);
            spinnerGhasaq.setSelection(configuration.pGhasaq);
            spinnerIshaEnd.setSelection(configuration.pIshaEnd);
            spinnerAsr.setSelection(configuration.pAsr);
            spinnerIrtifae.setSelection(configuration.pIrtifae);
            spinnerIsfirar.setSelection(configuration.pIsfirar);
            spinnerTakabud.setSelection(configuration.pTakabud);
            spinnerZawal.setSelection(configuration.pZawal);
            spinnerNightPeriod.setSelection(configuration.pNightPeriod);
            spinnerIstiwae.setSelection(configuration.pIstiwae);
            spinnerPrayersMethod.setSelection(configuration.pPrayersMethod);
        }

        if (others) {
            checkLong.setChecked(configuration.wLongFormat);
            editTextsize.setText(String.valueOf(configuration.wTextSizePercent));

            radioDateTimeNow.setChecked(configuration.wDateTimeNow);
            radioDateTimeCustom.setChecked(!configuration.wDateTimeNow);
            editDateTime.setEnabled(!configuration.wDateTimeNow);
            editDateTime.setText(configuration.wDateTimeCustom);

            editNavigateAmount.setText(String.valueOf(configuration.wNavigateAmount));
            spinnerNavigateField.setSelection(configuration.wNavigateField);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == Constants.REQUEST_CODE_PERMISSIONS) {

            boolean areAllGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_DENIED) {
                    areAllGranted = false;
                    break;
                }
            }

            if (areAllGranted) {
                recreate();
            } else {
                finish();
            }
        }
    }

    public void onRadioButtonClicked(View view) {

        boolean checked = ((RadioButton) view).isChecked();

        switch (view.getId()) {
            case R.id.wgc_radio_datetime_current:
                if (checked) {
                    editDateTime.setEnabled(false);
                }
                break;
            case R.id.wgc_radio_datetime_custom:
                if (checked) {
                    String dateTimeCustom = WidgetConfiguration.fromConfigurationFile(ActivityWidgetConfigure.this, appWidgetId).wDateTimeCustom;
                    editDateTime.setText(dateTimeCustom);
                    editDateTime.setEnabled(true);
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onLocationChanged(Location location) {

        if (location != null && location.hasAccuracy() && location.getAccuracy() <= Variables.locationAccuracy) {

            String accuracy = Utilities.getDecimalFormatterHalfUp("#").format(location.getAccuracy());
            Toast.makeText(this, "الدقة : " + accuracy + "م", Toast.LENGTH_LONG).show();

            Utilities.finishCurrentLocationSearch(ActivityWidgetConfigure.this);

            RadioButton radioGoogle = dialog.findViewById(R.id.dialog_radio_google);
            int method = radioGoogle.isChecked() ? Constants.LOCATION_METHOD_GOOGLE : Constants.LOCATION_METHOD_OSM;

            new AsyncLocationByCoordinates(ActivityWidgetConfigure.this, location, method, "ar").execute();
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onVersion(String result) {

    }

    @Override
    public void onLocation(String[] result) {

        dialog.dismiss();

        buttonCalcLocation.setText(result[0]);
        editCalcLocation.setText(String.format(Locale.ROOT, "%s/%s/%s", result[1], result[2], result[3]));
    }

    @Override
    public void onHijriAutoUpdated(int hijriMethod, int hijriAdjust) {

    }

    @Override
    public void onHijriUploaded() {

    }
}
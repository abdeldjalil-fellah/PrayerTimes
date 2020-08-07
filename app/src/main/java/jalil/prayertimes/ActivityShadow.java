package jalil.prayertimes;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import pub.devrel.easypermissions.EasyPermissions;

public class ActivityShadow extends AppCompatActivity implements LocationListener, IAsyncCompleted {

    Dialog dialog;
    boolean lastButtonClickedFrom = false;

    Button buttonCalculateShadow, buttonPointFrom, buttonPointTo, buttonDateTimeFrom, buttonDateTimeTo, buttonParams;
    AutoCompleteTextView editPointFrom, editPointTo;
    EditText editDateTimeFrom, editDateTimeTo, editParams;
    TextView viewOutput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shadow);

        setTitle("حساب الظل");
        setFinishOnTouchOutside(false);

        View decor = getWindow().getDecorView();
        decor.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        final int earthModel = ActivityMain.prefEarthModel(this);

        editPointFrom = findViewById(R.id.shadow_edit_point_from);
        editPointFrom.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, sampleLocations()));

        editPointTo = findViewById(R.id.shadow_edit_point_to);
        editPointTo.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, sampleLocations()));

        viewOutput = findViewById(R.id.shadow_view_output);

        editDateTimeFrom = findViewById(R.id.shadow_edit_datetime_from);
        editDateTimeFrom.setText(Variables.getCurrentDateTime());

        editDateTimeTo = findViewById(R.id.shadow_edit_datetime_to);
        editDateTimeTo.setText(Variables.getCurrentDateTime(Constants.MAIN_SHADOW_DATETIME_TO_OFFSET_MINUTES));

        editParams = findViewById(R.id.shadow_edit_params);
        editParams.setText(Constants.MAIN_SHADOW_PARAMS);

        buttonPointFrom = findViewById(R.id.shadow_button_point_from);
        buttonPointFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                lastButtonClickedFrom = true;
                dialog = Utilities.createLocationDialog(ActivityShadow.this, "en");
                dialog.show();
            }
        });

        buttonPointTo = findViewById(R.id.shadow_button_point_to);
        buttonPointTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                lastButtonClickedFrom = false;
                dialog = Utilities.createLocationDialog(ActivityShadow.this, "en");
                dialog.show();
            }
        });

        buttonDateTimeFrom = findViewById(R.id.shadow_button_datetime_from);
        buttonDateTimeFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editDateTimeFrom.setText(Variables.getCurrentDateTime());
                editDateTimeTo.setText(Variables.getCurrentDateTime(Constants.MAIN_SHADOW_DATETIME_TO_OFFSET_MINUTES));
            }
        });

        buttonDateTimeTo = findViewById(R.id.shadow_button_datetime_to);
        buttonDateTimeTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editDateTimeTo.setText(Variables.getCurrentDateTime(Constants.MAIN_SHADOW_DATETIME_TO_OFFSET_MINUTES));
            }
        });

        buttonParams = findViewById(R.id.shadow_button_params);
        buttonParams.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editParams.setText(Constants.MAIN_SHADOW_PARAMS);
            }
        });

        buttonCalculateShadow = findViewById(R.id.shadow_button_calculate);
        buttonCalculateShadow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {

                    String pointFrom = editPointFrom.getText().toString();
                    String pointTo = editPointTo.getText().toString();

                    if (pointFrom.length() < 21) {
                        editPointFrom.setError("غير صالح");
                    } else {
                        editPointFrom.setError(null);
                    }

                    if (pointTo.length() < 21) {
                        editPointTo.setError("غير صالح");
                    } else {
                        editPointTo.setError(null);
                    }

                    if (editPointFrom.getError() != null || editPointTo.getError() != null) return;

                    double latitudeNext = Utilities.valueOf(pointFrom, 0, 10, true);
                    double longitudeNext = Utilities.valueOf(pointFrom, 1, 10, true);

                    double latitudeEnd = Utilities.valueOf(pointTo, 0, 10, true);
                    double longitudeEnd = Utilities.valueOf(pointTo, 1, 10, true);

                    double azVehicle = EarthCalculation.azimuthDegrees(earthModel, latitudeNext, longitudeNext, latitudeEnd, longitudeEnd);
                    double azVehicleRad = azVehicle * SunMoonCalculator.DEG_TO_RAD;

                    String params = editParams.getText().toString();

                    TimeZone timezone = Utilities.timezoneCalcCity(params.substring(0, 6));

                    int stepSeconds = Utilities.secondsOf(params.substring(7, 15));
                    if (stepSeconds <= 0) stepSeconds = 60;

                    int speedKmH = (int) Utilities.toDouble(params.substring(16, 19), false);

                    String datetimeFrom = editDateTimeFrom.getText().toString();
                    String datetimeTo = editDateTimeTo.getText().toString();

                    Calendar calendarNext = Utilities.toUTC(datetimeFrom, timezone);
                    Calendar calendarEnd = Utilities.toUTC(datetimeTo, timezone);

                    final String SUN_BELOW_HORIZON = "الشمس تحت الأفق";

                    String lastMessage = "";
                    StringBuilder builder = new StringBuilder(Constants.NEW_LINE);

                    int currentSeconds = 0;
                    int secondsToEndTime = (int) (calendarEnd.getTimeInMillis() - calendarNext.getTimeInMillis()) / 1000;

                    if (secondsToEndTime < 0) {
                        editDateTimeTo.setError("ينبغي أن تكون متأخرة عن ساعة الانطلاق");
                    } else if (secondsToEndTime >= Constants.SECONDS_IN_DAY) {
                        editDateTimeTo.setError("المدة إلى ساعة التوقف ينبغي أن لا تتجاوز 24سا");
                    } else {
                        editDateTimeTo.setError(null);
                    }

                    if (editDateTimeTo.getError() != null) return;

                    int nextSeconds = nextSeconds(secondsToEndTime, currentSeconds, stepSeconds);

                    double currentKm = 0;
                    double totalKm = EarthCalculation.distanceMeters(earthModel, latitudeNext, longitudeNext, latitudeEnd, longitudeEnd) / 1000;

                    int secondsToEndPoint = speedKmH > 0 ? (int) (totalKm * 3600) / speedKmH : 0;

                    if (secondsToEndPoint >= Constants.SECONDS_IN_DAY) {
                        editPointTo.setError("المدة إلى نقطة التوقف ينبغي أن لا تتجاوز 24سا");
                    } else {
                        editPointTo.setError(null);
                    }

                    if (editPointTo.getError() != null) return;

                    boolean secondsForEndPoint = secondsToEndPoint > 0 && secondsToEndPoint < secondsToEndTime;

                    builder.append("اتجاه السير : ").append(Variables.decimalFormatter0.format(azVehicle)).append(Constants.SYMBOL_DEGREES).append(Constants.NEW_LINE);
                    builder.append("المدة إلى ساعة التوقف : ").append(Utilities.timeOf(secondsToEndTime, false)).append(" سا").append(Constants.NEW_LINE);
                    builder.append("المدة إلى نقطة التوقف : ").append(Utilities.timeOf(secondsToEndPoint, false)).append(" سا")
                            .append(" بسرعة ").append(Variables.decimalFormatter0.format(speedKmH)).append(" كم/سا").append(Constants.NEW_LINE);
                    builder.append("المسافة الخطية إلى نقطة التوقف : ").append(Variables.decimalFormatter2.format(totalKm)).append(" كم").append(Constants.NEW_LINE).append(Constants.NEW_LINE);

                    while (nextSeconds > 0) {

                        SunMoonCalculator smc = new SunMoonCalculator
                                (
                                        calendarNext.get(Calendar.YEAR), calendarNext.get(Calendar.MONTH) + 1, calendarNext.get(Calendar.DAY_OF_MONTH),
                                        calendarNext.get(Calendar.HOUR_OF_DAY), calendarNext.get(Calendar.MINUTE), calendarNext.get(Calendar.SECOND),
                                        longitudeNext * SunMoonCalculator.DEG_TO_RAD, latitudeNext * SunMoonCalculator.DEG_TO_RAD, 0
                                );

                        smc.calcSunAndMoon();

                        if (smc.sun.elevation <= 0) {

                            if (!lastMessage.equals(SUN_BELOW_HORIZON)) {
                                lastMessage = SUN_BELOW_HORIZON;

                                builder.append("◄ ").append(SunMoonCalculator.getTime(smc.jd_UT, timezone, false)).append(" ");
                                builder.append(SUN_BELOW_HORIZON).append(Constants.NEW_LINE).append(Constants.NEW_LINE);
                            }

                        } else {

                            double smallestAngle = SunMoonCalculator.smallestAngleRadians(azVehicleRad, smc.sun.azimuth);
                            double shadowAngle = SunMoonCalculator.shadowAngleRadians(azVehicleRad, smc.sun.azimuth);
                            double shadowFactorLength = SunMoonCalculator.shadowFactorLength(smc.sun.elevation);
                            double shadowFactorWidthRightLeft = SunMoonCalculator.shadowFactorWidthRightLeft(shadowAngle, smc.sun.elevation);
                            double shadowFactorWidthFrontBehind = SunMoonCalculator.shadowFactorWidthFrontBehind(shadowAngle, smc.sun.elevation);

                            String[] positionSunShadow = Utilities.positionSunShadow(smallestAngle);
                            String positionShadow = positionSunShadow[2] + " " + positionSunShadow[3];

                            if (!lastMessage.equals(positionShadow)) {

                                lastMessage = positionShadow;

                                builder.append("◄ ").append(SunMoonCalculator.getTime(smc.jd_UT, timezone, false)).append(" الظل على");
                                if (!positionSunShadow[2].equals(Constants.POSITION_NONE))
                                    builder.append(" ").append(positionSunShadow[2]).append(" بعرض ").append("x").append(Variables.decimalFormatter2.format(shadowFactorWidthRightLeft));
                                if (!positionSunShadow[3].equals(Constants.POSITION_NONE))
                                    builder.append(" ").append(positionSunShadow[3]).append(" بعرض ").append("x").append(Variables.decimalFormatter2.format(shadowFactorWidthFrontBehind));

                                builder.append(" الزاوية ").append(Variables.decimalFormatter0.format(shadowAngle * SunMoonCalculator.RAD_TO_DEG)).append(Constants.SYMBOL_DEGREES);
                                builder.append(" الطول ").append("x").append(Variables.decimalFormatter2.format(shadowFactorLength));
                                builder.append(" الشمس ").append(Variables.decimalFormatter0.format(smc.sun.azimuth * SunMoonCalculator.RAD_TO_DEG)).append(Constants.SYMBOL_DEGREES);

                                builder.append(Constants.NEW_LINE).append(Constants.NEW_LINE);
                            }
                        }

                        nextSeconds = nextSeconds(secondsForEndPoint ? secondsToEndPoint : secondsToEndTime, currentSeconds, stepSeconds);
                        currentSeconds += nextSeconds;

                        calendarNext.add(Calendar.SECOND, nextSeconds);

                        double nextKm = speedKmH * nextSeconds / 3600.0;
                        currentKm += nextKm;

                        double[] nextLocation = EarthCalculation.nextPoint(earthModel, latitudeNext, longitudeNext, azVehicle, nextKm * 1000);
                        latitudeNext = nextLocation[0];
                        longitudeNext = nextLocation[1];
                    }

                    calendarNext.add(Calendar.MILLISECOND, timezone.getRawOffset());

                    builder.append("◄ ").append(Utilities.adjustTime(Variables.dateFormatHms.format(calendarNext.getTime()), 0, false)).append(" ");
                    builder.append(secondsForEndPoint ? "نقطة" : "ساعة").append(" التوقف بعد ").append(Variables.decimalFormatter2.format(currentKm)).append(" كم في ").append(Utilities.timeOf(currentSeconds, false)).append(" سا").append(Constants.NEW_LINE);

                    viewOutput.setText(builder.toString());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public ArrayList<String> sampleLocations() {

        ArrayList<String> locations = new ArrayList<>();

        locations.add("+36.156210 +01.820170 Zeddine");
        locations.add("+36.244830 +01.805580 Rouina");
        locations.add("+36.224440 +01.672520 Attaf");
        locations.add("+36.265690 +01.970160 Ain Defla");
        locations.add("+36.260180 +02.219670 Khemis");
        locations.add("+36.469990 +02.828720 Blida");
        locations.add("+36.733770 +03.086170 Alger");

        return locations;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null && location.hasAccuracy() && location.getAccuracy() <= Variables.locationAccuracy) {

            String accuracy = Utilities.getDecimalFormatterHalfUp("#").format(location.getAccuracy());
            Toast.makeText(this, "الدقة : " + accuracy + "م", Toast.LENGTH_LONG).show();

            Utilities.finishCurrentLocationSearch(this);

            RadioButton radioGoogle = dialog.findViewById(R.id.dialog_radio_google);
            int method = radioGoogle.isChecked() ? Constants.LOCATION_METHOD_GOOGLE : Constants.LOCATION_METHOD_OSM;

            new AsyncLocationByCoordinates(this, location, method, "en").execute();
        }
    }

    int nextSeconds(int max, int current, int step) {
        int diff = max - current;
        return diff >= step ? step : diff;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onVersion(String result) {

    }

    @Override
    public void onLocation(String[] result) {

        dialog.dismiss();

        if (lastButtonClickedFrom) {
            editPointFrom.setText(String.format(Locale.ROOT, "%s %s %s", result[1], result[2], result[0]));
        } else {
            editPointTo.setText(String.format(Locale.ROOT, "%s %s %s", result[1], result[2], result[0]));
        }
    }

    @Override
    public void onHijriAutoUpdated(int hijriMethod, int hijriAdjust) {

    }

    @Override
    public void onHijriUploaded() {

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
}

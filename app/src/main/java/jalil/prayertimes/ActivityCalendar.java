package jalil.prayertimes;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedHashSet;
import java.util.Locale;

public class ActivityCalendar extends AppCompatActivity {

    Button buttonGo, buttonImage;
    EditText editYear;
    Spinner spinnerMonth;
    CheckBox checkDetails, checkCalendar, checkCalendarToday;
    TextView viewHeader, viewDetails;
    LinearLayout linearContainer, linearCalendar, linearSeparator;

    int method, adjust;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        setTitle("التقويم الهجري");
        setFinishOnTouchOutside(false);

        View decor = getWindow().getDecorView();
        decor.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        method = ActivityMain.prefHijriMethod(this);
        adjust = ActivityMain.prefHijriAdjustDays(this);

        final String todayHijri = Utilities.toDateHijri(Variables.getDate(Variables.getCurrentDateTime()), adjust, method, this, false, null, null);

        linearCalendar = findViewById(R.id.cal_calendar);
        linearSeparator = findViewById(R.id.cal_separator);
        linearContainer = findViewById(R.id.cal_linear);

        spinnerMonth = findViewById(R.id.cal_spinner_month);
        spinnerMonth.setAdapter(new ArrayAdapter<>(ActivityCalendar.this, R.layout.simple_spinner_item, new String[]{
                "المُحرَّم",
                "صَفَر",
                "رَبيع الأَوَّل",
                "رَبيع الآخِر",
                "جُمادى الأُولى",
                "جُمادى الآخِرة",
                "رَجَب",
                "شَعْبان",
                "رَمَضان",
                "شَوَّال",
                "ذو القَعْدة",
                "ذو الحِجّة"
        }));

        editYear = findViewById(R.id.cal_edit_year);

        viewDetails = findViewById(R.id.cal_details);
        viewHeader = findViewById(R.id.cal_header);

        checkDetails = findViewById(R.id.cal_check_details);
        checkCalendar = findViewById(R.id.cal_check_calendar);
        checkCalendarToday = findViewById(R.id.cal_check_calendar_today);

        checkCalendar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked) checkCalendarToday.setChecked(false);
                createCalendar();
            }
        });

        checkCalendarToday.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) checkCalendar.setChecked(true);
                createCalendar();
            }
        });

        checkDetails.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                createCalendar();
            }
        });

        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                createCalendar();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        buttonGo = findViewById(R.id.cal_button_go);
        buttonGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createCalendar();
            }
        });

        buttonImage = findViewById(R.id.cal_button_image);
        buttonImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String fileName = null;

                if (checkCalendar.isChecked() && checkDetails.isChecked()) {
                    fileName = headerHijri + " مع الملحق";
                } else {

                    if (checkCalendar.isChecked()) {
                        fileName = headerHijri;
                    }

                    if (checkDetails.isChecked()) {
                        fileName = headerHijri + " ملحق";
                    }
                }

                if (fileName != null) {
                    new AsyncSaveScreenshot(ActivityCalendar.this, Utilities.createBitmapFromView(linearContainer), fileName).doInBackground();
                }
            }
        });

        if (todayHijri.equals(Constants.INVALID_STRING)) {
            spinnerMonth.setSelection(0);
            editYear.setText("1441");
        } else {

            int[] c = Utilities.dateComponents(todayHijri);

            spinnerMonth.setSelection(c[1] - 1);
            editYear.setText(c[0] + "");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    boolean handleCell(int w, int d, Calendar calendar, int hDay) {

        boolean filled = false;

        TextView cellH = findViewById(getResources().getIdentifier("cal_cell" + w + d + "h", "id", getPackageName()));
        TextView cellG = findViewById(getResources().getIdentifier("cal_cell" + w + d + "g", "id", getPackageName()));

        if (d == (calendar.get(Calendar.DAY_OF_WEEK) % 7) + 1 && hDay <= 29) {

            cellH.setVisibility(View.VISIBLE);
            cellG.setVisibility(View.VISIBLE);

            cellH.setText(String.format(Locale.ROOT, "%d", hDay));
            cellG.setText(String.format(Locale.ROOT, "%d/%d", calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH)));

            switch (hDay) {
                case 1:
                    cellH.setBackgroundColor(Variables.colorBlueLight);
                    cellG.setBackgroundColor(Variables.colorBlueLight);
                    break;
                case 13:
                case 14:
                case 15:
                    cellH.setBackgroundColor(Variables.colorGreenLight);
                    cellG.setBackgroundColor(Variables.colorGreenLight);
                    break;
                case 17:
                case 19:
                case 21:
                    cellH.setBackgroundColor(Variables.colorRedLight);
                    cellG.setBackgroundColor(Variables.colorRedLight);
                    break;
                case 29:
                    cellH.setBackgroundColor(Variables.colorOrangeLight);
                    cellG.setBackgroundColor(Variables.colorOrangeLight);
                    break;
                default:
                    cellH.setBackgroundColor(Variables.colorTransparent);
                    cellG.setBackgroundColor(Variables.colorTransparent);
            }

            if (checkCalendarToday.isChecked() && DateUtils.isToday(calendar.getTimeInMillis())) {
                cellH.setBackgroundColor(Variables.colorYellow);
                cellG.setBackgroundColor(Variables.colorYellow);
            }

            filled = true;
        } else {
            cellH.setVisibility(View.INVISIBLE);
            cellG.setVisibility(View.INVISIBLE);

            cellH.setText("");
            cellG.setText("");

            cellH.setBackgroundColor(Variables.colorTransparent);
            cellG.setBackgroundColor(Variables.colorTransparent);
        }

        return filled;
    }

    String headerHijri = null, headerGreg = null;

    void createCalendar() {

        linearContainer.setVisibility(View.GONE);

        String dateHijriFirst = editYear.getText().toString() + "-" + String.format(Locale.ROOT, "%02d", spinnerMonth.getSelectedItemPosition() + 1) + "-01";
        String dateGregFirst = Utilities.toDateGregorian(dateHijriFirst, method, adjust, ActivityCalendar.this);

        if (dateGregFirst.equals(Constants.INVALID_STRING)) return;

        int[] cH = Utilities.dateComponents(dateHijriFirst);
        int[] cG = Utilities.dateComponents(dateGregFirst);

        int hDay = cH[2];
        int hMonth = cH[1];

        Calendar calendar = GregorianCalendar.getInstance();
        calendar.set(cG[0], cG[1] - 1, cG[2]);

        StringBuilder b = new StringBuilder();

        DayInfoMessagesOutput dayInfoMessagesOutput = Utilities.dayHijriInfoMessages(7, hMonth, hDay, true, false);
        b.append(dayInfoMessagesOutput.messagesBuilder.toString());

        for (int d = 1; d <= 6; d++) {
            dayInfoMessagesOutput = Utilities.dayHijriInfoMessages(d, hMonth, hDay, true, false);
            b.append(dayInfoMessagesOutput.messagesBuilder.toString());
        }

        for (int w = 1; w <= 5; w++) {

            for (int d = 1; d <= 7; d++) {

                if (handleCell(w, d, calendar, hDay)) {

                    dayInfoMessagesOutput = Utilities.dayHijriInfoMessages(calendar.get(Calendar.DAY_OF_WEEK), hMonth, hDay, false, true);
                    b.append(dayInfoMessagesOutput.messagesBuilder.toString());

                    hDay++;
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                }
            }
        }

        headerHijri = String.format(Locale.ROOT, "%s \u200F%04dهـ", spinnerMonth.getSelectedItem(), cH[0]);
        headerGreg = String.format(Locale.ROOT, "%s \u200F%04dم", Utilities.getMonthNameGregorianAssyrian(cG[1]), cG[0]);

        viewHeader.setText(String.format("%s - %s", headerHijri, headerGreg));

        if (checkCalendar.isChecked()) {
            linearCalendar.setVisibility(View.VISIBLE);
        } else {
            linearCalendar.setVisibility(View.GONE);
        }

        if (checkCalendar.isChecked() && checkDetails.isChecked()) {
            linearSeparator.setVisibility(View.VISIBLE);
        } else {
            linearSeparator.setVisibility(View.GONE);
        }

        if (checkDetails.isChecked()) {
            StringBuilder builder = new StringBuilder();
            for (String line : new LinkedHashSet<>(Arrays.asList(b.toString().split(Constants.NEW_LINE)))) {
                builder.append(line).append(Constants.NEW_LINE);
            }

            viewDetails.setVisibility(View.VISIBLE);
            viewDetails.setText(builder.toString().trim());
        } else {
            viewDetails.setVisibility(View.GONE);
        }

        linearContainer.setVisibility(View.VISIBLE);
    }
}

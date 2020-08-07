package jalil.prayertimes;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedHashSet;
import java.util.Locale;

public class ActivityCalendar extends AppCompatActivity {

    Button buttonGo;
    EditText editYear;
    Spinner spinnerMonth;
    CheckBox checkDetails, checkShot;
    TextView viewHeader, viewDetails;
    LinearLayout linearLayout;

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

        linearLayout = findViewById(R.id.cal_linear);

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

        if (todayHijri.equals(Constants.INVALID_STRING)) {
            spinnerMonth.setSelection(0);
            editYear.setText("1441");
        } else {

            int[] c = Utilities.dateComponents(todayHijri);

            spinnerMonth.setSelection(c[1] - 1);
            editYear.setText(c[0] + "");
        }

        viewDetails = findViewById(R.id.cal_details);
        viewHeader = findViewById(R.id.cal_header);

        checkDetails = findViewById(R.id.cal_check_details);
        checkShot = findViewById(R.id.cal_check_shot);

        buttonGo = findViewById(R.id.cal_button_go);
        buttonGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createCalendar();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        createCalendar();
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

    void createCalendar() {

        linearLayout.setVisibility(View.GONE);

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

        String headerHijri = String.format(Locale.ROOT, "%s \u200F%04dهـ", spinnerMonth.getSelectedItem(), cH[0]);
        String headerGreg = String.format(Locale.ROOT, "%s \u200F%04dم", Utilities.getMonthNameGregorianAssyrian(cG[1]), cG[0]);

        viewHeader.setText(headerHijri + " - " + headerGreg);

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

        linearLayout.setVisibility(View.VISIBLE);

        if (checkShot.isChecked()) {

            new AsyncSaveScreenshot(this, Utilities.createBitmapFromView(linearLayout), headerHijri).doInBackground();
        }
    }
}

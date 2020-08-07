package jalil.prayertimes;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class HijriMARW {

    final static String TABLE_CITIES = "CITIES";
    final static String TABLE_DATES = "DATES";
    final static String TABLE_TIMES = "TIMES";

    final static String COLUMN_CITIES_ID = "CITY_ID";
    final static String COLUMN_CITIES_NAME = "CITY_NAME";

    final static String COLUMN_DATES_HIJRI_YEAR = "HIJRI_YEAR";
    final static String COLUMN_DATES_HIJRI_MONTH = "HIJRI_MONTH";
    final static String COLUMN_DATES_HIJRI_DAY = "HIJRI_DAY";
    final static String COLUMN_DATES_GREG_YEAR = "GREG_YEAR";
    final static String COLUMN_DATES_GREG_MONTH = "GREG_MONTH";
    final static String COLUMN_DATES_GREG_DAY = "GREG_DAY";

    final static String COLUMN_TIMES_CITY_ID = "CITY_ID";
    final static String COLUMN_TIMES_GREG_YEAR = "GREG_YEAR";
    final static String COLUMN_TIMES_GREG_MONTH = "GREG_MONTH";
    final static String COLUMN_TIMES_GREG_DAY = "GREG_DAY";
    final static String COLUMN_TIMES_PRAYER_SUBH = "SUBH";
    final static String COLUMN_TIMES_PRAYER_DHUHR = "DHUHR";
    final static String COLUMN_TIMES_PRAYER_ASR = "ASR";
    final static String COLUMN_TIMES_PRAYER_MAGHRIB = "MAGHRIB";
    final static String COLUMN_TIMES_PRAYER_ISHA = "ISHA";

    public static Integer cityId(Context context, String cityName) {

        try (SQLiteDatabase db = new Database(context, Constants.FILE_NAME_DB_MARW).getReadableDatabase()) {

            Cursor cursor = db.rawQuery
                    (
                            "SELECT " + COLUMN_CITIES_ID +
                                    " FROM " + TABLE_CITIES +
                                    " WHERE " + COLUMN_CITIES_NAME + "=?",
                            new String[]{cityName}
                    );

            Integer id = null;

            if (cursor.moveToNext()) {
                id = cursor.getInt(0);
            }

            cursor.close();
            return id;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ArrayList<String> cities(Context context) {

        try (SQLiteDatabase db = new Database(context, Constants.FILE_NAME_DB_MARW).getReadableDatabase()) {

            Cursor cursor = db.query(TABLE_CITIES,
                    new String[]{COLUMN_CITIES_NAME},
                    null,
                    null,
                    null, null, null);

            ArrayList<String> cities = new ArrayList<>();
            while (cursor.moveToNext()) {
                cities.add(cursor.getString(0));
            }

            cursor.close();
            return cities;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String toHijri(Context context, String dateGreg, int adjustOutputByDays) {

        if (dateGreg.equals(Constants.INVALID_STRING)) return Constants.INVALID_STRING;

        try (SQLiteDatabase db = new Database(context, Constants.FILE_NAME_DB_MARW).getReadableDatabase()) {

            int[] dateComponents = Utilities.dateComponents(dateGreg);

            Cursor cursor = db.rawQuery
                    (
                            "SELECT " + COLUMN_DATES_HIJRI_YEAR + ", " +
                                    COLUMN_DATES_HIJRI_MONTH + ", " +
                                    COLUMN_DATES_HIJRI_DAY +
                                    " FROM " + TABLE_DATES +
                                    " WHERE " + COLUMN_DATES_GREG_YEAR + "=? AND " +
                                    COLUMN_DATES_GREG_MONTH + "=? AND " +
                                    COLUMN_DATES_GREG_DAY + "=?",
                            new String[]{
                                    String.valueOf(dateComponents[0]),
                                    String.valueOf(dateComponents[1]),
                                    String.valueOf(dateComponents[2])
                            }
                    );

            String result = Constants.INVALID_STRING;

            if (cursor.moveToNext()) {

                if (adjustOutputByDays == 0) {
                    result = Utilities.dateString(cursor.getInt(0), cursor.getInt(1), cursor.getInt(2));
                } else {
                    result = Utilities.dateString(Utilities.adjustHijri(cursor.getInt(0), cursor.getInt(1), cursor.getInt(2), adjustOutputByDays));
                }
            }

            cursor.close();
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return Constants.INVALID_STRING;
        }
    }

    public static String toGregorian(Context context, String dateHijri, int inputAdjustedByDays) {

        if (dateHijri.equals(Constants.INVALID_STRING)) return Constants.INVALID_STRING;

        int[] dateHijriComponents;

        if (inputAdjustedByDays == 0) {
            dateHijriComponents = Utilities.dateComponents(dateHijri);
        } else {
            dateHijriComponents = Utilities.adjustHijri(dateHijri, -inputAdjustedByDays);
        }

        try (SQLiteDatabase db = new Database(context, Constants.FILE_NAME_DB_MARW).getReadableDatabase()) {

            Cursor cursor = db.rawQuery
                    (
                            "SELECT " + COLUMN_DATES_GREG_YEAR + ", " +
                                    COLUMN_DATES_GREG_MONTH + ", " +
                                    COLUMN_DATES_GREG_DAY +
                                    " FROM " + TABLE_DATES +
                                    " WHERE " + COLUMN_DATES_HIJRI_YEAR + "=? AND " +
                                    COLUMN_DATES_HIJRI_MONTH + "=? AND " +
                                    COLUMN_DATES_HIJRI_DAY + "=?",
                            new String[]{
                                    String.valueOf(dateHijriComponents[0]),
                                    String.valueOf(dateHijriComponents[1]),
                                    String.valueOf(dateHijriComponents[2])
                            }
                    );

            String result = Constants.INVALID_STRING;
            if (cursor.moveToNext()) {
                result = Utilities.dateString(cursor.getInt(0), cursor.getInt(1), cursor.getInt(2));
            }

            cursor.close();
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return Constants.INVALID_STRING;
        }
    }

    final static int INDEX_DATE_GREG = 0;
    final static int INDEX_PRAYER_SUBH = 1;
    final static int INDEX_PRAYER_DHUHR = 2;
    final static int INDEX_PRAYER_ASR = 3;
    final static int INDEX_PRAYER_MAGHRIB = 4;
    final static int INDEX_PRAYER_ISHA = 5;

    public static String[] times(Context context, int cityId, String dateGreg) {

        String[] times = new String[6];

        times[INDEX_DATE_GREG] = dateGreg;

        times[INDEX_PRAYER_SUBH] = Constants.INVALID_STRING;
        times[INDEX_PRAYER_DHUHR] = Constants.INVALID_STRING;
        times[INDEX_PRAYER_ASR] = Constants.INVALID_STRING;
        times[INDEX_PRAYER_MAGHRIB] = Constants.INVALID_STRING;
        times[INDEX_PRAYER_ISHA] = Constants.INVALID_STRING;

        if (dateGreg.equals(Constants.INVALID_STRING)) return times;

        try (SQLiteDatabase db = new Database(context, Constants.FILE_NAME_DB_MARW).getReadableDatabase()) {

            int[] dateComponents = Utilities.dateComponents(dateGreg);

            Cursor cursor = db.rawQuery
                    (
                            "SELECT " + COLUMN_TIMES_GREG_YEAR + ", " +
                                    COLUMN_TIMES_GREG_MONTH + ", " +
                                    COLUMN_TIMES_GREG_DAY + ", " +
                                    COLUMN_TIMES_PRAYER_SUBH + ", " +
                                    COLUMN_TIMES_PRAYER_DHUHR + ", " +
                                    COLUMN_TIMES_PRAYER_ASR + ", " +
                                    COLUMN_TIMES_PRAYER_MAGHRIB + ", " +
                                    COLUMN_TIMES_PRAYER_ISHA +
                                    " FROM " + TABLE_TIMES +
                                    " WHERE " + COLUMN_TIMES_CITY_ID + "=? AND " +
                                    COLUMN_TIMES_GREG_YEAR + "=? AND " +
                                    COLUMN_TIMES_GREG_MONTH + "=? AND " +
                                    COLUMN_TIMES_GREG_DAY + "=?",
                            new String[]{
                                    String.valueOf(cityId),
                                    String.valueOf(dateComponents[0]),
                                    String.valueOf(dateComponents[1]),
                                    String.valueOf(dateComponents[2])
                            }
                    );

            if (cursor.moveToNext()) {
                times[INDEX_PRAYER_SUBH] = cursor.getString(3);
                times[INDEX_PRAYER_DHUHR] = cursor.getString(4);
                times[INDEX_PRAYER_ASR] = cursor.getString(5);
                times[INDEX_PRAYER_MAGHRIB] = cursor.getString(6);
                times[INDEX_PRAYER_ISHA] = cursor.getString(7);
            } else {
                cursor = db.rawQuery
                        (
                                "SELECT " + COLUMN_TIMES_GREG_YEAR + ", " +
                                        COLUMN_TIMES_GREG_MONTH + ", " +
                                        COLUMN_TIMES_GREG_DAY + ", " +
                                        COLUMN_TIMES_PRAYER_SUBH + ", " +
                                        COLUMN_TIMES_PRAYER_DHUHR + ", " +
                                        COLUMN_TIMES_PRAYER_ASR + ", " +
                                        COLUMN_TIMES_PRAYER_MAGHRIB + ", " +
                                        COLUMN_TIMES_PRAYER_ISHA +
                                        " FROM " + TABLE_TIMES +
                                        " WHERE " + COLUMN_TIMES_CITY_ID + "=? AND " +
                                        COLUMN_TIMES_GREG_MONTH + "=? AND " +
                                        COLUMN_TIMES_GREG_DAY + "=?" +
                                        " ORDER BY " + COLUMN_TIMES_GREG_YEAR + " DESC",
                                new String[]{
                                        String.valueOf(cityId),
                                        String.valueOf(dateComponents[1]),
                                        String.valueOf(dateComponents[2])
                                }
                        );

                if (cursor.moveToNext()) {
                    times[INDEX_DATE_GREG] = Utilities.dateString(cursor.getInt(0), cursor.getInt(1), cursor.getInt(2));
                    times[INDEX_PRAYER_SUBH] = cursor.getString(3);
                    times[INDEX_PRAYER_DHUHR] = cursor.getString(4);
                    times[INDEX_PRAYER_ASR] = cursor.getString(5);
                    times[INDEX_PRAYER_MAGHRIB] = cursor.getString(6);
                    times[INDEX_PRAYER_ISHA] = cursor.getString(7);
                }
            }

            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return times;
    }
}

package jalil.prayertimes;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class HijriUAQ {

    final static String TABLE_DATES = "DATES";
    final static String COLUMN_DATES_HIJRI_YEAR = "hy";
    final static String COLUMN_DATES_HIJRI_MONTH = "hm";
    final static String COLUMN_DATES_HIJRI_DAY = "hd";
    final static String COLUMN_DATES_GREG_YEAR = "gy";
    final static String COLUMN_DATES_GREG_MONTH = "gm";
    final static String COLUMN_DATES_GREG_DAY = "gd";

    public static String toHijri(Context context, String dateGreg, int adjustOutputByDays) {

        if (dateGreg.equals(Constants.INVALID_STRING)) return Constants.INVALID_STRING;

        try (SQLiteDatabase db = new Database(context, Constants.FILE_NAME_DB_UAQ).getReadableDatabase()) {

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
                            new String[]{String.valueOf(dateComponents[0]), String.valueOf(dateComponents[1]), String.valueOf(dateComponents[2])}
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

        try (SQLiteDatabase db = new Database(context, Constants.FILE_NAME_DB_UAQ).getReadableDatabase()) {

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
}

package jalil.prayertimes;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;

public class Database extends SQLiteOpenHelper {

    public Database(Context context, String dbFileName) {

        super(context, Variables.getFullNameDb(context, dbFileName), null, 1);

        Utilities.copyAssetsFile(context, Constants.DIR_NAME_DB + File.separator + dbFileName, Variables.getFullNameDb(context, dbFileName), false);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }
}

package com.example.soham.popularmoviesstage2.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.soham.popularmoviesstage2.data.MovieContract.MovieEntry;

public class MovieDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "popmovies.db";

    public static final int DATABASE_VERSION = 1;

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_MOVIES_TABLE = "CREATE TABLE " + MovieEntry.TABLE_NAME + "( "
                + MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MovieEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL,"
                + MovieEntry.COLUMN_MOVIE_TITLE + " TEXT NOT NULL,"
                + MovieEntry.COLUMN_MOVIE_PLOT + " TEXT NOT NULL,"
                + MovieEntry.COLUMN_MOVIE_RATING + " REAL NOT NULL,"
                + MovieEntry.COLUMN_MOVIE_RELEASE + " TEXT NOT NULL,"
                + MovieEntry.COLUMN_MOVIE_FAVORITE + " INTEGER NOT NULL DEFAULT 0,"
                + MovieEntry.COLUMN_MOVIE_POSTER + " TEXT NOT NULL, "
                + "UNIQUE ( " + MovieEntry.COLUMN_MOVIE_ID + " ) ON CONFLICT IGNORE"
                + ");";

        db.execSQL(SQL_CREATE_MOVIES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        final String SQL_DROP_MOVIES_TABLE = "DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME;
        db.execSQL(SQL_DROP_MOVIES_TABLE);
        onCreate(db);
    }
}

package com.example.soham.popularmoviesstage2.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class MovieProvider extends ContentProvider {

    private MovieDbHelper movieDbHelper;

    private static final int MOVIE_DATA = 101;
    private static final int MOVIE_DATA_WITH_ID = 102;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher() {

        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        uriMatcher.addURI(authority, MovieContract.PATH, MOVIE_DATA);
        uriMatcher.addURI(authority, MovieContract.PATH + "/#", MOVIE_DATA_WITH_ID);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        movieDbHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection,
                        @Nullable String selection, @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {
        Cursor cursor;
        final SQLiteDatabase sqLiteDatabase = movieDbHelper.getReadableDatabase();

        switch (sUriMatcher.match(uri)) {

            case MOVIE_DATA: {
                cursor = sqLiteDatabase.query(MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case MOVIE_DATA_WITH_ID: {

                String movieId = uri.getLastPathSegment();
                String[] selectionArguments = new String[]{movieId};
                cursor = sqLiteDatabase.query(MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        MovieContract.MovieEntry.COLUMN_MOVIE_ID + " =? ",
                        selectionArguments,
                        null,
                        null,
                        sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri : " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    //Not implementing getType for this project
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase sqLiteDatabase = movieDbHelper.getWritableDatabase();

        switch (sUriMatcher.match(uri)) {
            case MOVIE_DATA:
                sqLiteDatabase.beginTransaction();
                int rowsInserted = 0;
                try {
                    for (ContentValues value : values) {
                        long id = sqLiteDatabase.insert(MovieContract.MovieEntry.TABLE_NAME,
                                null,
                                value);
                        if (id != -1) {
                            rowsInserted++;
                        }
                    }
                    sqLiteDatabase.setTransactionSuccessful();
                } finally {
                    sqLiteDatabase.endTransaction();
                }
                if (rowsInserted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsInserted;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    //Insert method not being used
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    //Delete method not being used
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {

        final SQLiteDatabase sqLiteDatabase = movieDbHelper.getWritableDatabase();
        int rowsUpdated = 0;
        String movieId = uri.getLastPathSegment();
        String[] selectionArguments = new String[]{movieId};
        switch (sUriMatcher.match(uri)) {
            case MOVIE_DATA_WITH_ID:
                sqLiteDatabase.beginTransaction();
                try {
                    rowsUpdated = sqLiteDatabase.update(MovieContract.MovieEntry.TABLE_NAME,
                            values,
                            MovieContract.MovieEntry.COLUMN_MOVIE_ID + " =? ",
                            selectionArguments);
                    sqLiteDatabase.setTransactionSuccessful();
                } finally {
                    sqLiteDatabase.endTransaction();
                }
                if (rowsUpdated > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
        }
        return rowsUpdated;
    }
}

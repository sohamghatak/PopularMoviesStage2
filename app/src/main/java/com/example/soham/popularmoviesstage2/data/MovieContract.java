package com.example.soham.popularmoviesstage2.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class MovieContract {

    public static final String CONTENT_AUTHORITY = "com.example.soham.popularmoviesstage2";

    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH = "movies";

    public static final class MovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH)
                .build();

        public static final String TABLE_NAME = "movies";
        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_MOVIE_TITLE = "title";
        public static final String COLUMN_MOVIE_RATING = "rating";
        public static final String COLUMN_MOVIE_PLOT = "plot";
        public static final String COLUMN_MOVIE_RELEASE = "release_date";
        public static final String COLUMN_MOVIE_FAVORITE = "favorite";
        public static final String COLUMN_MOVIE_POSTER = "poster_url";

        public static Uri buildMovieUriWithId(int id) {
            return CONTENT_URI.buildUpon()
                    .appendPath(Integer.toString(id))
                    .build();
        }
    }
}
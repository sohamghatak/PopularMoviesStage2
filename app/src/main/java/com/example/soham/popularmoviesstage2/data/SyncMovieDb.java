package com.example.soham.popularmoviesstage2.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import java.util.List;

//Class to sync movie database with the value received from JSON request
public class SyncMovieDb {

    /**
     * Method to sync movies into the movies database
     *
     * @param moviesList List of movies passed in after the JSON response is parsed.
     * @param context    passed in context
     **/
    public static void syncMovies(List<Movies> moviesList, Context context) {
        //Creating a content values array for bulk insert
        ContentValues[] movieValuesArray = new ContentValues[moviesList.size()];

        if (moviesList != null && moviesList.size() != 0) {
            for (int i = 0; i < moviesList.size(); i++) {
                //Get details of a single movie
                Movies singleMovie = moviesList.get(i);
                //Create a content values object and put the values of movie one by one
                ContentValues movieValues = new ContentValues();
                movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, singleMovie.getMovieId());
                movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_RATING, singleMovie.getMovieRating());
                movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE, singleMovie.getMovieTitle());
                movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_POSTER, singleMovie.getMoviePoster());
                movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_PLOT, singleMovie.getMoviePlot());
                movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_RELEASE, singleMovie.getReleaseDate());
                movieValuesArray[i] = movieValues;
            }
        }
        if (movieValuesArray != null && movieValuesArray.length != 0) {
            ContentResolver movieContentResolver = context.getContentResolver();
            //Update database with the values
            movieContentResolver.bulkInsert(MovieContract.MovieEntry.CONTENT_URI, movieValuesArray);
        }
    }

    /**
     * Static method to update movies marked as favorite by the users
     *
     * @param context    passed in context
     * @param receiveUri Uri of the item to be updated
     * @param favorite   static value of 1 or 0 passed in as favorite is treated as a boolean in the database
     * @return true/false based on the database update
     **/
    public static boolean updateFavorites(Context context, Uri receiveUri, int favorite) {

        ContentValues movieValues = new ContentValues();
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_FAVORITE, favorite);
        ContentResolver movieContentResolver = context.getContentResolver();
        int rowsUpdated = movieContentResolver.update(receiveUri, movieValues, null, null);
        if (rowsUpdated > 0) {
            return true;
        } else {
            return false;
        }
    }
}

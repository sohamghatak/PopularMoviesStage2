package com.example.soham.popularmoviesstage2.utility;

import android.text.TextUtils;
import android.util.Log;

import com.example.soham.popularmoviesstage2.data.Movies;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

//Class to parse JSON response
public final class MovieJSONUtils {

    private static final String LOG_TAG = MovieJSONUtils.class.getSimpleName();
    //Static image URL to which the poster id would be appended
    private final static String MOVIES_POSTER_URL = "http://image.tmdb.org/t/p/w185";
    //Static youtube URL to which the trailer key/id would be appended
    private final static String TRAILER_BASE_URL = "https://www.youtube.com/watch?v=";
    //Static variables for JSON retrieval
    private final static String RESULTS = "results";
    private final static String VOTE_AVERAGE = "vote_average";
    private final static String ORIGINAL_TITLE = "original_title";
    private final static String POSTER_PATH = "poster_path";
    private final static String OVERVIEW = "overview";
    private final static String RELEASE_DATE = "release_date";
    private final static String MOVIE_ID = "id";

    /**
     * This method extracts the details from the json string which is passed along
     *
     * @param jsonResponse response string passed after the network request
     **/
    public static List<Movies> getDetailsFromJSON(String jsonResponse) {

        Movies movies;
        //Array List to store the movies object.
        List<Movies> mMoviesList = new ArrayList<>();

        //Return early if the jsonResponse is empty
        if (TextUtils.isEmpty(jsonResponse)) {
            return null;
        }
        try {
            JSONObject baseMovieJSON = new JSONObject(jsonResponse);
            JSONArray resultsJSONArray = baseMovieJSON.getJSONArray(RESULTS);
            for (int i = 0; i < resultsJSONArray.length(); i++) {
                JSONObject movieDetailsObject = resultsJSONArray.getJSONObject(i);
                int movieId = movieDetailsObject.getInt(MOVIE_ID);
                Double userRating = movieDetailsObject.getDouble(VOTE_AVERAGE);
                String originalTitle = movieDetailsObject.getString(ORIGINAL_TITLE);
                String receivePosterPath = movieDetailsObject.getString(POSTER_PATH);
                String poster = MOVIES_POSTER_URL + receivePosterPath;
                String plot = movieDetailsObject.getString(OVERVIEW);
                String releaseDate = movieDetailsObject.getString(RELEASE_DATE);
                movies = new Movies(movieId, userRating, originalTitle, poster, plot, releaseDate);
                mMoviesList.add(movies);
            }
        } catch (JSONException e) {
            Log.v(LOG_TAG, "Problem parsing JSON response");
        }
        return mMoviesList;
    }

    public static String getTrailerDetailsFromJSON(String jsonResponse) {

        String trailerUrl = null;
        //Return early if the jsonResponse is empty
        if (TextUtils.isEmpty(jsonResponse)) {
            return null;
        }
        final String TRAILER_KEY = "key";

        try {
            //Extracting the trailer key/id from the JSON response
            JSONObject baseMovieJSON = new JSONObject(jsonResponse);
            JSONArray resultsJSONArray = baseMovieJSON.getJSONArray(RESULTS);
            JSONObject trailerDetailsObject = resultsJSONArray.getJSONObject(0);
            String trailerKey = trailerDetailsObject.getString(TRAILER_KEY);
            trailerUrl = TRAILER_BASE_URL + trailerKey;
        } catch (JSONException e) {
            Log.v(LOG_TAG, "Problem parsing movie trailer JSON response");
        }
        return trailerUrl;
    }

    public static List<String> getReviewsFromJSON(String jsonResponse) {

        List<String> reviewsList = new ArrayList<>();
        //Return early if the jsonResponse is empty
        if (TextUtils.isEmpty(jsonResponse)) {
            return null;
        }

        final String REVIEWS_CONTENT = "content";

        try {
            //Extracting the movie reviews from the JSON response
            JSONObject baseMovieJSON = new JSONObject(jsonResponse);
            JSONArray resultsJSONArray = baseMovieJSON.getJSONArray(RESULTS);
            for (int i = 0; i < resultsJSONArray.length(); i++) {
                JSONObject reviewsObject = resultsJSONArray.getJSONObject(i);
                String reviews = reviewsObject.getString(REVIEWS_CONTENT);
                reviewsList.add(reviews);
            }
        } catch (JSONException e) {
            Log.v(LOG_TAG, "Problem parsing movie trailer JSON response");
        }
        return reviewsList;
    }
}

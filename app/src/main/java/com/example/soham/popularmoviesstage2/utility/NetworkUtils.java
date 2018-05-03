package com.example.soham.popularmoviesstage2.utility;

import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

//Network utilities class
public final class NetworkUtils {

    private static final String LOG_TAG = NetworkUtils.class.getSimpleName();
    //Static BASE URL and API_KEY static variable
    private final static String MOVIES_BASE_URL = "http://api.themoviedb.org/3/movie";
    //Enter your API key here
    private final static String apiKey = "";
    private final static String API_KEY = "api_key";
    private final static String VIDEOS_PATH = "videos";
    private final static String REVIEWS_PATH = "reviews";

    /**
     * This method builds a URL using a passed in String
     *
     * @param path url path that it passed in to build a URL.
     **/
    public static URL buildURL(String path) {

        URL formedURL = null;
        Uri builtURI = Uri.parse(MOVIES_BASE_URL).buildUpon()
                .appendPath(path)
                .appendQueryParameter(API_KEY, apiKey)
                .build();
        try {
            formedURL = new URL(builtURI.toString());
        } catch (MalformedURLException e) {
            Log.v(LOG_TAG, "Problem building URL");
        }
        return formedURL;
    }

    /**
     * This method builds a trailer URL using a passed in String
     *
     * @param path url path that it passed in to build a URL.
     **/
    public static URL buildTrailerURL(String path) {

        URL formedURL = null;
        Uri builtURI = Uri.parse(MOVIES_BASE_URL).buildUpon()
                .appendPath(path)
                .appendPath(VIDEOS_PATH)
                .appendQueryParameter(API_KEY, apiKey)
                .build();
        try {
            formedURL = new URL(builtURI.toString());
        } catch (MalformedURLException e) {
            Log.v(LOG_TAG, "Problem building URL");
        }
        return formedURL;
    }

    /**
     * This method builds a reviews URL using a passed in String
     *
     * @param path url path that it passed in to build a URL.
     **/
    public static URL buildReviewsURL(String path) {

        URL formedURL = null;
        Uri builtURI = Uri.parse(MOVIES_BASE_URL).buildUpon()
                .appendPath(path)
                .appendPath(REVIEWS_PATH)
                .appendQueryParameter(API_KEY, apiKey)
                .build();
        try {
            formedURL = new URL(builtURI.toString());
        } catch (MalformedURLException e) {
            Log.v(LOG_TAG, "Problem building URL");
        }
        return formedURL;
    }


    /**
     * This method makes an HTTP request and returns String jsonResponse.
     *
     * @param url url passed in to be parsed.
     **/
    public static String getHTTPResponse(URL url) throws IOException {

        //Use a StringBuilder to concatenate string
        String outputJSON = " ";
        // If the URL is null, then return early.
        if (url == null) {
            return outputJSON;
        }
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            InputStream in = urlConnection.getInputStream();
            outputJSON = readFromStream(in);
        }
        return outputJSON;
    }

    /**
     * This method reads the inputStream received from the network calls and
     * returns a String response.
     *
     * @param inputStream inputStream received as a http response from getHTTPResponse method.
     **/
    private static String readFromStream(InputStream inputStream) throws IOException {
        //Return early if inputStream is null
        if (inputStream == null) {
            return null;
        }

        StringBuilder receivedJSON = new StringBuilder();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String line = bufferedReader.readLine();
        while (line != null) {
            receivedJSON.append(line);
            line = bufferedReader.readLine();
        }
        return receivedJSON.toString();
    }

    /**
     * Helper method to get a friendly date string from string JSON date
     **/

    public static String simpleDate(String date) {
        Date parsedDate = null;
        String desiredDateString;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
        SimpleDateFormat desiredDateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        try {
            parsedDate = dateFormat.parse(date);
        } catch (ParseException e) {
            Log.v(LOG_TAG, "Problem with parsing Date");
        }
        desiredDateString = desiredDateFormat.format(parsedDate);
        return desiredDateString;
    }

}


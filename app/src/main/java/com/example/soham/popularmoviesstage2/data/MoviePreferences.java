package com.example.soham.popularmoviesstage2.data;

public class MoviePreferences {

    private static final String NOW_PLAYING = "now_playing";
    private static final String MOST_POPULAR = "popular";
    private static final String TOP_RATED = "top_rated";

    /**
     * Getter methods to get the static variables
     **/
    public static String getNowPlaying() {
        return NOW_PLAYING;
    }

    public static String getTopRated() {
        return TOP_RATED;
    }

    public static String getMostPopular() {
        return MOST_POPULAR;
    }
}

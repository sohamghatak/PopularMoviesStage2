package com.example.soham.popularmoviesstage2;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.soham.popularmoviesstage2.data.MovieContract;
import com.example.soham.popularmoviesstage2.data.Movies;
import com.example.soham.popularmoviesstage2.data.SyncMovieDb;
import com.example.soham.popularmoviesstage2.utility.MovieJSONUtils;
import com.example.soham.popularmoviesstage2.utility.NetworkUtils;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.soham.popularmoviesstage2.utility.NetworkUtils.simpleDate;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private int mFavorite;
    private Uri receivedMovieUri;
    private String trailerURL = null;
    private ArrayList<Movies> moviesList = new ArrayList<>();
    //Movie details Cursor loader ID
    private static final int MOVIE_DETAIL_LOADER_ID = 919;
    //Variables to be used while restoring savedInstanceState
    private String reviewsString = "";
    private Boolean reviewShown = false;

    //Using ButterKnife to bind views
    @BindView(R.id.pm_movie_title)
    TextView mMovieTitle;
    @BindView(R.id.pm_movie_plot)
    TextView mMoviePlot;
    @BindView(R.id.pm_movie_rating)
    TextView mMovieRating;
    @BindView(R.id.pm_release_date)
    TextView mReleaseDate;
    @BindView(R.id.pm_image_poster)
    ImageView mMoviePoster;
    @BindView(R.id.pm_fav_button)
    Button mFavButton;
    @BindView(R.id.trailer_button)
    Button mTrailerButton;
    @BindView(R.id.pm_reviews_title)
    TextView mReviewsTitle;
    @BindView(R.id.pm_movie_reviews)
    TextView mMovieReviews;
    @BindView(R.id.read_more_text_view)
    TextView mReadMore;
    @BindView(R.id.loading_reviews)
    ProgressBar mLoadingReviews;
    @BindView(R.id.detail_scroll_view)
    ScrollView mDetailScrollView;

    //Columns that needs to be selected from the database
    private static final String[] MOVIE_PROJECTION = {
            MovieContract.MovieEntry.COLUMN_MOVIE_ID,
            MovieContract.MovieEntry.COLUMN_MOVIE_TITLE,
            MovieContract.MovieEntry.COLUMN_MOVIE_PLOT,
            MovieContract.MovieEntry.COLUMN_MOVIE_RATING,
            MovieContract.MovieEntry.COLUMN_MOVIE_RELEASE,
            MovieContract.MovieEntry.COLUMN_MOVIE_FAVORITE,
            MovieContract.MovieEntry.COLUMN_MOVIE_POSTER
    };
    //Index of the columns mentioned above
    public static final int INDEX_COLUMN_MOVIE_ID = 0;
    public static final int INDEX_COLUMN_MOVIE_TITLE = 1;
    public static final int INDEX_COLUMN_MOVIE_PLOT = 2;
    public static final int INDEX_COLUMN_MOVIE_RATING = 3;
    public static final int INDEX_COLUMN_MOVIE_RELEASE = 4;
    public static final int INDEX_COLUMN_MOVIE_FAVORITE = 5;
    public static final int INDEX_COLUMN_MOVIE_POSTER = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        mMovieReviews.setVisibility(View.GONE);
        reviewShown = false;
        mReviewsTitle.setVisibility(View.VISIBLE);
        mLoadingReviews.setVisibility(View.GONE);
        final Intent receivedMovieIntent = getIntent();
        ///Get details from the received intent
        if (receivedMovieIntent != null) {
            receivedMovieUri = receivedMovieIntent.getData();
            if (receivedMovieUri == null)
                throw new NullPointerException("URI for DetailActivity cannot be null");
        }
        //Get the movie id for use in building trailer URI and reviews URI
        final String movieId = receivedMovieUri.getLastPathSegment();

        //Execute async task to fetch movie trailer
        new FetchMovieTrailerAsyncTask().execute(movieId);

        if (savedInstanceState == null) {
            //Initialize the loader
            getSupportLoaderManager().initLoader(MOVIE_DETAIL_LOADER_ID, null, this).forceLoad();
        }

        //Favorite button on click listener
        mFavButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Using drawable to set rounded edge background
                mFavButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.favorite_clicked));
                //Update the database with the new favorite value as selected by the user
                new UpdateFavoritesesAsyncTask().execute();
            }
        });
        //Trailer button onClickListener to launch first trailer of the movie on youtube
        mTrailerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                watchTrailer(trailerURL);
            }
        });
        //Reviews onClickListener
        mReviewsTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //If no connectivity do not show reviews
                if (!checkConnection()) {
                    Toast.makeText(DetailActivity.this, R.string.cannot_show_reviews, Toast.LENGTH_SHORT).show();
                    return;
                }
                //If reviews are already shown the and the user click on the movie review expandable text view again,
                // hide the reviews
                if (mMovieReviews.isShown()) {
                    reviewShown = false;
                    mReviewsTitle.setText(R.string.reviews_title_text);
                    mMovieReviews.setVisibility(View.GONE);
                    mReviewsTitle.setTextColor(getResources().getColor(R.color.reviewsHiddenTitleColor));
                    //else if the reviews are not shown then fetch the reviews through async task
                } else {
                    reviewShown = true;
                    mReviewsTitle.setTextColor(getResources().getColor(R.color.reviewsShownTitleColor));
                    mReviewsTitle.setText(R.string.showing_reviews_text);
                    mLoadingReviews.setVisibility(View.VISIBLE);
                    new FetchReviewsAsyncTask().execute(movieId);
                }
            }
        });

        //Set the max lines for movie plot. Expand and contract based on the number of lines
        mReadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        if (mMoviePlot.getMaxLines() == 5) {
                            mMoviePlot.setMaxLines(Integer.MAX_VALUE);
                            mReadMore.setText(R.string.movie_plot_read_less_text);
                            mReadMore.setTextColor(getResources().getColor(R.color.reviewsShownTitleColor));
                        } else {
                            mMoviePlot.setMaxLines(5);
                            mReadMore.setText(R.string.movie_plot_read_more_text);
                            mReadMore.setTextColor(getResources().getColor(R.color.reviewsHiddenTitleColor));
                        }
                    } else {
                        int maxLines = TextViewCompat.getMaxLines(mMoviePlot);
                        if (maxLines == 5) {
                            mMoviePlot.setMaxLines(Integer.MAX_VALUE);
                            mReadMore.setText(R.string.movie_plot_read_less_text);
                            mReadMore.setTextColor(getResources().getColor(R.color.reviewsShownTitleColor));
                        } else {
                            mMoviePlot.setMaxLines(5);
                            mReadMore.setText(R.string.movie_plot_read_more_text);
                            mReadMore.setTextColor(getResources().getColor(R.color.reviewsHiddenTitleColor));
                        }
                    }
                }
            }
        });

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Save the moviesList in outState bundle
        outState.putParcelableArrayList("detailMoviesList", moviesList);
        //Save the scroll position of the movie detail view
        outState.putIntArray("SCROLL_POSITION",
                new int[]{mDetailScrollView.getScrollX(), mDetailScrollView.getScrollY()});
        //Save the movie review in outState bundle
        outState.putString("movieReviews", reviewsString);
        //Save the review shown text view state in outState bundle
        outState.putBoolean("reviewShown", reviewShown);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        //Restore moviesList
        moviesList = savedInstanceState.getParcelableArrayList("detailMoviesList");
        //Restore movie review
        reviewsString = savedInstanceState.getString("movieReviews");
        //Restore review text view state
        reviewShown = savedInstanceState.getBoolean("reviewShown");
        //Load saved data onto views
        showSavedData(moviesList, reviewsString);
        //Restore scroll view position
        final int[] position = savedInstanceState.getIntArray("SCROLL_POSITION");
        if (position != null)
            //post method cannot run on main thread
            mDetailScrollView.post(new Runnable() {
                public void run() {
                    //Scroll to saved position
                    mDetailScrollView.scrollTo(position[0], position[1]);
                }
            });
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        switch (id) {
            case MOVIE_DETAIL_LOADER_ID:

                return new CursorLoader(this,
                        receivedMovieUri,
                        MOVIE_PROJECTION,
                        null,
                        null,
                        null);
            default:
                throw new RuntimeException("Loader Not Implemented: " + id);
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {

        boolean cursorHasValidData = false;
        if (data != null && data.moveToFirst()) {
            /* We have valid data, continue on to bind the data to the UI */
            cursorHasValidData = true;
        }

        if (!cursorHasValidData) {
            /* No data to display, simply return and do nothing */
            return;
        }

        int movieId = data.getInt(INDEX_COLUMN_MOVIE_ID);
        String movieTitle = data.getString(INDEX_COLUMN_MOVIE_TITLE);
        String moviePLot = data.getString(INDEX_COLUMN_MOVIE_PLOT);
        Double movieRating = data.getDouble(INDEX_COLUMN_MOVIE_RATING);
        String movieReleaseDate = data.getString(INDEX_COLUMN_MOVIE_RELEASE);
        mFavorite = data.getInt(INDEX_COLUMN_MOVIE_FAVORITE);
        String moviePoster = data.getString(INDEX_COLUMN_MOVIE_POSTER);
        Movies movies = new Movies(movieId, movieRating, movieTitle, moviePoster, moviePLot, movieReleaseDate, mFavorite);
        moviesList.add(movies);
        //Call helper method to load data onto views
        showData(moviesList);

    }

    //Not implementing onLoaderReset
    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.action_share:
                Intent shareIntent = shareTrailerIntent();
                startActivity(shareIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    //AsyncTask to update favorite details to the database
    class UpdateFavoritesesAsyncTask extends AsyncTask<Void, Void, Void> {

        private int isFavorite = 0;
        private boolean addedSuccessfully = false;
        private boolean removedSuccessfully = false;

        @Override
        protected Void doInBackground(Void... voids) {
            Context context = getApplicationContext();
            //Check to see if favorite is already clicked when the movie detail activity is loaded
            if (mFavorite > 0) {
                //If it is already click and the user clicks it again we update the db with the value as 0
                mFavButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.favorite_not_clicked));
                removedSuccessfully = SyncMovieDb.updateFavorites(context, receivedMovieUri, isFavorite);
            } else {
                //If favorite is not selected and the user selects favorite movie then update the db with 1
                isFavorite = 1;
                addedSuccessfully = SyncMovieDb.updateFavorites(context, receivedMovieUri, isFavorite);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            //Provide a toast message to user if the movie was added/removed from favorites
            if (addedSuccessfully) {
                Toast.makeText(getApplicationContext(), R.string.added_to_favorites, Toast.LENGTH_SHORT).show();
            }
            if (removedSuccessfully) {
                Toast.makeText(getApplicationContext(), R.string.removed_from_favorites, Toast.LENGTH_SHORT).show();
            }
            super.onPostExecute(aVoid);
        }
    }

    //AsyncTask to fetch movie trailer
    class FetchMovieTrailerAsyncTask extends AsyncTask<String, Void, URL> {

        @Override
        protected URL doInBackground(String... strings) {
            String responseJSON = null;
            URL mUrl = NetworkUtils.buildTrailerURL(strings[0]);
            try {
                responseJSON = NetworkUtils.getHTTPResponse(mUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }

            trailerURL = MovieJSONUtils.getTrailerDetailsFromJSON(responseJSON);
            return null;
        }
    }

    /**
     * Helper method to create a youtube URL and pass an intent
     *
     * @param url passed in youtube url
     **/
    private void watchTrailer(String url) {
        Intent youtubeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        Intent chooser = Intent.createChooser(youtubeIntent, String.valueOf(R.string.trailer_url_open_with));
        //Check if app exists to launch the intent activity
        if (youtubeIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(chooser);
        }
    }

    //AsyncTask to fetch movie reviews
    class FetchReviewsAsyncTask extends AsyncTask<String, Void, List<String>> {

        List<String> movieReviews = new ArrayList<>();

        @Override
        protected List<String> doInBackground(String... strings) {
            String responseJSON = null;
            URL mUrl = NetworkUtils.buildReviewsURL(strings[0]);
            try {
                responseJSON = NetworkUtils.getHTTPResponse(mUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
            movieReviews = MovieJSONUtils.getReviewsFromJSON(responseJSON);
            return movieReviews;
        }

        @Override
        protected void onPostExecute(List<String> strings) {
            mLoadingReviews.setVisibility(View.GONE);
            //Default text when there is no review
            String reviews = "There are no reviews for this movie";
            StringBuilder reviewsBuffer = new StringBuilder();
            if (strings.size() != 0) {
                for (int i = 0; i < strings.size(); i++) {
                    reviews = strings.get(i);
                    reviewsBuffer.append(reviews)
                            .append(System.getProperty("line.separator"))
                            .append("__________________________________________________________")
                            .append(System.getProperty("line.separator"));

                }
                reviewsString = reviewsBuffer.toString();
                mMovieReviews.setText(reviewsBuffer);
                mMovieReviews.setVisibility(View.VISIBLE);
            } else {
                reviewsString = reviews;
                mMovieReviews.setText(reviews);
                mMovieReviews.setVisibility(View.VISIBLE);
            }
            super.onPostExecute(strings);
        }
    }

    /**
     * Uses the ShareCompat Intent builder to share youtube trailer URL.
     **/
    private Intent shareTrailerIntent() {
        Intent shareIntent = ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText(trailerURL)
                .getIntent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        }
        return shareIntent;
    }

    /**
     * Helper method to check if internet connection is active on the device.
     **/
    private boolean checkConnection() {

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return networkInfo != null && networkInfo.isConnected();
    }

    /**
     * Utility function to count the number of words in movie plot
     * and set an expandable text view accordingly
     *
     * @param sMoviePlot plot of the movies
     **/
    private int numberOfWords(String sMoviePlot) {
        int count = 1;

        for (int i = 0; i < sMoviePlot.length() - 1; i++) {
            if ((sMoviePlot.charAt(i) == ' ') && (sMoviePlot.charAt(i + 1) != ' ')) {
                count++;
            }
        }
        return count;
    }

    /**
     * Helper method to show movies data when it is loaded the first time
     *
     * @param moviesData List of movies passed in
     **/
    private void showData(ArrayList<Movies> moviesData) {

        for (int i = 0; i < moviesData.size(); i++) {
            Movies singleMovie = moviesData.get(i);
            Uri posterUri = Uri.parse(singleMovie.getMoviePoster());

            mMovieTitle.setText(singleMovie.getMovieTitle());
            mMovieRating.setText(String.valueOf(singleMovie.getMovieRating()));
            //Check to find the number of words and set the expandable text view if needed
            int mWords = numberOfWords(singleMovie.getMoviePlot());
            if (mWords < 50) {
                mReadMore.setVisibility(View.GONE);
            } else {
                mMoviePlot.setMaxLines(5);
            }
            mMoviePlot.setText(singleMovie.getMoviePlot());
            mReleaseDate.setText(simpleDate(singleMovie.getReleaseDate()));
            Picasso.with(this)
                    .load(posterUri)
                    .placeholder(R.drawable.ic_movie_white_48dp)
                    .error(R.drawable.ic_error_outline_white_48dp)
                    .into(mMoviePoster);
            //Set the favorite button color accordingly
            if (singleMovie.getFavorite() > 0) {
                mFavButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.favorite_clicked));
            } else {
                mFavButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.favorite_not_clicked));
            }
        }
    }

    /**
     * Helper method to show movies data when it is loaded from saved instance state
     *
     * @param moviesData  List of movies passed in
     * @param movieReview movie review string value
     **/
    private void showSavedData(ArrayList<Movies> moviesData, String movieReview) {
        //Check the orientation of the device
        final int orientation = getResources().getConfiguration().orientation;
        for (int i = 0; i < moviesData.size(); i++) {
            Movies singleMovie = moviesData.get(i);
            Uri posterUri = Uri.parse(singleMovie.getMoviePoster());

            mMovieTitle.setText(singleMovie.getMovieTitle());
            mMovieRating.setText(String.valueOf(singleMovie.getMovieRating()));
            //Check to find the number of words and set the expandable text view if needed
            int mWords = numberOfWords(singleMovie.getMoviePlot());
            //If orientation is landscape remove read more test view
            getResources().getConfiguration();
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mReadMore.setVisibility(View.GONE);
            } else {
                if (mWords < 50) {
                    mReadMore.setVisibility(View.GONE);
                } else {
                    mMoviePlot.setMaxLines(5);
                }
            }
            //Set the reviews title if reviews are shown
            if (reviewShown) {
                mReviewsTitle.setTextColor(getResources().getColor(R.color.reviewsShownTitleColor));
                mReviewsTitle.setText(R.string.showing_reviews_text);
                mMovieReviews.setVisibility(View.VISIBLE);
                mMovieReviews.setText(movieReview);
            }
            mMoviePlot.setText(singleMovie.getMoviePlot());
            mReleaseDate.setText(simpleDate(singleMovie.getReleaseDate()));
            Picasso.with(this)
                    .load(posterUri)
                    .placeholder(R.drawable.ic_movie_white_48dp)
                    .error(R.drawable.ic_error_outline_white_48dp)
                    .into(mMoviePoster);
            //Set the favorite button color accordingly
            if (singleMovie.getFavorite() > 0) {
                mFavButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.favorite_clicked));
            } else {
                mFavButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.favorite_not_clicked));
            }
        }
    }
}

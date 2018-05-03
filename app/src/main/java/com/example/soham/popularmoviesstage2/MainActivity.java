package com.example.soham.popularmoviesstage2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.soham.popularmoviesstage2.data.MovieContract;
import com.example.soham.popularmoviesstage2.data.MoviePreferences;
import com.example.soham.popularmoviesstage2.data.Movies;
import com.example.soham.popularmoviesstage2.data.SyncMovieDb;
import com.example.soham.popularmoviesstage2.utility.MovieJSONUtils;
import com.example.soham.popularmoviesstage2.utility.NetworkUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.soham.popularmoviesstage2.DetailActivity.INDEX_COLUMN_MOVIE_FAVORITE;
import static com.example.soham.popularmoviesstage2.DetailActivity.INDEX_COLUMN_MOVIE_ID;
import static com.example.soham.popularmoviesstage2.DetailActivity.INDEX_COLUMN_MOVIE_PLOT;
import static com.example.soham.popularmoviesstage2.DetailActivity.INDEX_COLUMN_MOVIE_POSTER;
import static com.example.soham.popularmoviesstage2.DetailActivity.INDEX_COLUMN_MOVIE_RATING;
import static com.example.soham.popularmoviesstage2.DetailActivity.INDEX_COLUMN_MOVIE_RELEASE;
import static com.example.soham.popularmoviesstage2.DetailActivity.INDEX_COLUMN_MOVIE_TITLE;

public class MainActivity extends AppCompatActivity implements MovieAdapter.MovieAdapterOnClickHandler,
        LoaderManager.LoaderCallbacks<Cursor> {

    //private RecyclerView mRecyclerView;
    private MovieAdapter mMovieAdapter;
    //Local variable to store the menu item id clicked
    private int itemId;
    private boolean connection;
    //Using ButterKnife to bind views
    @BindView(R.id.pm_recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.pm_error_message)
    TextView mErrorMessage;
    @BindView(R.id.pm_loading_indicator)
    ProgressBar mLoadingIndicator;

    private String movieTitle;
    private Double movieRating;
    private String moviePLot;
    private String movieReleaseDate;
    private String moviePoster;
    private int movieId;
    private int mFavorite;
    //Variable to store the id of the menu option that was clicked by the user
    public static int SELECTED_CODE = 0;
    Movies movies;
    //Array List to store the movies object.
    List<Movies> mMoviesList = new ArrayList<>();
    //Favorite movie loader id
    private static final int FAVORITE_MOVIE_LOADER_ID = 934;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mLoadingIndicator.setVisibility(View.VISIBLE);
        //GridLayoutManager to populate a grid layout
        GridLayoutManager gridLayoutManager = new GridLayoutManager
                (this, getColumnSpan());
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        mMovieAdapter = new MovieAdapter(this);
        mRecyclerView.setAdapter(mMovieAdapter);

        //Below condition checks the saved instance state
        if (savedInstanceState == null) {
            nowPlayingMovies();
        } else {
            mLoadingIndicator.setVisibility(View.GONE);
            getUserSettings(savedInstanceState.getInt("selectedOption", R.id.now_playing_settings));
        }

    }

    @Override
    protected void onResume() {
        /**Added logic to navigate back to the right activity
         * when the user presses back button in action bar**/
        switch (SELECTED_CODE) {
            case R.id.favorites:
                mMoviesList.clear();
                getSupportLoaderManager().restartLoader(FAVORITE_MOVIE_LOADER_ID, null, this);
                break;
            case R.id.now_playing_settings:
                nowPlayingMovies();
                break;
            case R.id.high_rated_settings:
                highRatedMovies();
                break;
            case R.id.most_popular_settings:
                mostPopularMovies();
                break;
        }
        super.onResume();
    }

    /**
     * Save the instance using the below method and
     * storing the movie preference selected in the menu item
     **/
    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("selectedOption", itemId);
    }

    @Override
    public void onClick(Movies movies) {
        Context context = this;
        Class destinationClass = DetailActivity.class;
        Intent intentToStartDetailActivity = new Intent(context, destinationClass);
        Uri movieUri = MovieContract.MovieEntry.buildMovieUriWithId(movies.getMovieId());
        intentToStartDetailActivity.setData(movieUri);
        startActivity(intentToStartDetailActivity);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        final int favoriteMovie = 1;
        String[] selectionArguments = new String[]{String.valueOf(favoriteMovie)};
        final String[] MOVIE_PROJECTION = {
                MovieContract.MovieEntry.COLUMN_MOVIE_ID,
                MovieContract.MovieEntry.COLUMN_MOVIE_TITLE,
                MovieContract.MovieEntry.COLUMN_MOVIE_PLOT,
                MovieContract.MovieEntry.COLUMN_MOVIE_RATING,
                MovieContract.MovieEntry.COLUMN_MOVIE_RELEASE,
                MovieContract.MovieEntry.COLUMN_MOVIE_FAVORITE,
                MovieContract.MovieEntry.COLUMN_MOVIE_POSTER
        };
        switch (id) {
            case FAVORITE_MOVIE_LOADER_ID:

                return new CursorLoader(this,
                        MovieContract.MovieEntry.CONTENT_URI,
                        MOVIE_PROJECTION,
                        MovieContract.MovieEntry.COLUMN_MOVIE_FAVORITE + " =? ",
                        selectionArguments,
                        null);
            default:
                throw new RuntimeException("Loader Not Implemented: " + id);

        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        //Load favorite movies from database
        if (data != null) {
            try {
                while (data.moveToNext()) {
                    movieId = data.getInt(INDEX_COLUMN_MOVIE_ID);
                    movieTitle = data.getString(INDEX_COLUMN_MOVIE_TITLE);
                    moviePLot = data.getString(INDEX_COLUMN_MOVIE_PLOT);
                    movieRating = data.getDouble(INDEX_COLUMN_MOVIE_RATING);
                    movieReleaseDate = data.getString(INDEX_COLUMN_MOVIE_RELEASE);
                    mFavorite = data.getInt(INDEX_COLUMN_MOVIE_FAVORITE);
                    moviePoster = data.getString(INDEX_COLUMN_MOVIE_POSTER);
                    movies = new Movies(movieId, movieRating, movieTitle, moviePoster, moviePLot, movieReleaseDate);
                    mMoviesList.add(movies);
                }
                mMovieAdapter.addListMovies(mMoviesList);
                //Checks for active internet connection and then showing results based on the results accordingly
                if (!checkConnection() && mMovieAdapter.getItemCount() > 0) {
                    mLoadingIndicator.setVisibility(View.GONE);
                    showMoviesData();
                    //Snackbar message to show no internet connection and only inform user that we are only showing selected favorite movies
                    Snackbar showOnlyFavorites = Snackbar.make(mRecyclerView, R.string.show_only_favorites, Snackbar.LENGTH_SHORT);
                    //Using getView() to support API level 15 and less
                    View snackbarView = showOnlyFavorites.getView();
                    snackbarView.setBackgroundColor(getResources().getColor(R.color.reviewsHiddenTitleColor));
                    showOnlyFavorites.show();
                    return;
                }
                if (!checkConnection() && mMovieAdapter.getItemCount() == 0) {
                    mLoadingIndicator.setVisibility(View.GONE);
                    mErrorMessage.setText(R.string.error_no_fav_no_internet);
                    showErrorMessage();
                    return;
                }
                if (checkConnection() && mMovieAdapter.getItemCount() == 0) {
                    mLoadingIndicator.setVisibility(View.GONE);
                    mErrorMessage.setText(R.string.error_no_fav_active_internet);
                    showErrorMessage();
                    return;
                }
            } finally {
                //Destroy loader as it was being called twice
                getSupportLoaderManager().destroyLoader(FAVORITE_MOVIE_LOADER_ID);
                //close the cursor
                data.close();
            }
        } else {
            //return if data is null
            return;
        }

    }

    //Not implementing loader reset method
    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
    }

    /**
     * AsyncTask class to handle network operation in background thread
     **/
    @SuppressLint("StaticFieldLeak")
    public class FetchMoviesTask extends AsyncTask<String, Void, List<Movies>> {

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<Movies> doInBackground(String... strings) {
            String response = null;
            List<Movies> mMoviesList;
            URL url = NetworkUtils.buildURL(strings[0]);
            try {
                response = NetworkUtils.getHTTPResponse(url);
            } catch (IOException e) {
                Log.v(LOG_TAG, "Problem in receiving response from HTTP request");
            }

            mMoviesList = MovieJSONUtils.getDetailsFromJSON(response);
            SyncMovieDb.syncMovies(mMoviesList, getBaseContext());
            return mMoviesList;
        }

        @Override
        protected void onPostExecute(List<Movies> movies) {
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            if (movies != null) {
                showMoviesData();
                mMovieAdapter.addListMovies(movies);
            } else {
                mErrorMessage.setText(R.string.error_message);
                showErrorMessage();
            }
        }
    }

    /**
     * Method to get a list of now playing movies
     **/
    private void nowPlayingMovies() {
        //Set the selected code integer
        SELECTED_CODE = R.id.now_playing_settings;
        FetchMoviesTask fetchMoviesTask = new FetchMoviesTask();
        String nowPlaying = MoviePreferences.getNowPlaying();
        connection = checkConnection();
        //Check if internet connection is active.
        if (connection) {
            fetchMoviesTask.execute(nowPlaying);
        } else {
            //If connection is not active load favorite movies from database
            getFavoriteMovies();
        }
    }

    /**
     * Method to get a list of popular movies
     **/
    private void mostPopularMovies() {
        //Set the selected code integer
        SELECTED_CODE = R.id.most_popular_settings;
        FetchMoviesTask fetchMoviesTask = new FetchMoviesTask();
        String mostPopular = MoviePreferences.getMostPopular();
        connection = checkConnection();
        //Check if internet connection is active.
        if (connection) {
            fetchMoviesTask.execute(mostPopular);
        } else {
            //If connection is not active load favorite movies from database
            getFavoriteMovies();
        }
    }

    /**
     * Method to get a list of top rated movies
     **/
    private void highRatedMovies() {
        //Set the selected code integer
        SELECTED_CODE = R.id.high_rated_settings;
        FetchMoviesTask fetchMoviesTask = new FetchMoviesTask();
        String topRated = MoviePreferences.getTopRated();
        connection = checkConnection();
        //Check if internet connection is active.
        if (connection) {
            fetchMoviesTask.execute(topRated);
        } else {
            //If connection is not active load favorite movies from database
            getFavoriteMovies();
        }
    }

    private void getFavoriteMovies() {
        //Set the selected code integer
        SELECTED_CODE = R.id.favorites;
        if (mMoviesList != null) {
            mMoviesList.clear();
            getSupportLoaderManager().restartLoader(FAVORITE_MOVIE_LOADER_ID, null, this);
        } else {
            getSupportLoaderManager().initLoader(FAVORITE_MOVIE_LOADER_ID, null, this);
        }
    }

    /**
     * Inflate menu options
     **/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.movies_sort_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Call a method to get perform action based on menu item selected.
        getUserSettings(item.getItemId());
        return super.onOptionsItemSelected(item);
    }

    /**
     * Method to execute task based on User clicked menu options.
     **/
    private void getUserSettings(int selectedSetting) {
        itemId = selectedSetting;
        switch (itemId) {
            case R.id.now_playing_settings:
                nowPlayingMovies();
                break;
            case R.id.most_popular_settings:
                mostPopularMovies();
                break;
            case R.id.high_rated_settings:
                highRatedMovies();
                break;
            case R.id.favorites:
                getFavoriteMovies();
                break;
            case R.id.refresh:
                mErrorMessage.setText("");
                nowPlayingMovies();
                break;
        }
    }

    /**
     * This method shows the movies data and hides the error message
     **/
    private void showMoviesData() {
        mErrorMessage.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    /**
     * This method shows the error message if the movies fail to load
     **/
    private void showErrorMessage() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mErrorMessage.setVisibility(View.VISIBLE);
    }

    /**
     * This method returns the number of columns based on the screen orientation.
     **/
    private int getColumnSpan() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return 4;
        }
        return 2;
    }

    /**
     * Helper method to check if internet connection is active on the device.
     **/
    public final boolean checkConnection() {

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return networkInfo != null && networkInfo.isConnected();
    }

}

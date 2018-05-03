package com.example.soham.popularmoviesstage2;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.soham.popularmoviesstage2.data.Movies;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> {

    private Context context;
    private List<Movies> mMovies;
    private final MovieAdapterOnClickHandler mClickHandler;

    /**
     * Creates a Movie Adapter
     *
     * @param clickHandler single handler when an item it clicked
     **/
    public MovieAdapter(MovieAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }


    public class MovieAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        //Image View to hold movie poster
        public final ImageView mMoviePoster;

        public MovieAdapterViewHolder(View view) {
            super(view);
            mMoviePoster = (ImageView) view.findViewById(R.id.pm_movie_poster);
            view.setOnClickListener(this);
        }


        /**
         * This gets called by the child views during a click.
         *
         * @param v The View that was clicked
         */
        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            Movies movies = mMovies.get(adapterPosition);
            mClickHandler.onClick(movies);
        }
    }

    /**
     * Interface to receive onCLick messages
     **/
    public interface MovieAdapterOnClickHandler {
        void onClick(Movies movies);
    }

    /**
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     *
     * @param parent   ViewGroup within which these ViewHolders are contained.
     * @param viewType used to represent different type of item in RecyclerView
     **/
    @Override
    public MovieAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        int layoutItemId = R.layout.movie_grid_item;
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        View view = layoutInflater.inflate(layoutItemId, parent, false);
        return new MovieAdapterViewHolder(view);

    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified position.
     *
     * @param holder   ViewHolder which should be updated.
     * @param position position of item
     **/

    @Override
    public void onBindViewHolder(MovieAdapterViewHolder holder, int position) {
        Movies movies = mMovies.get(position);
        Uri posterUri = Uri.parse(movies.getMoviePoster());

        Picasso.with(context)
                .load(posterUri)
                .placeholder(R.drawable.ic_movie_white_48dp)
                .error(R.drawable.ic_error_outline_white_48dp)
                .into(holder.mMoviePoster);
    }

    /**
     * Method to return to number of items to display
     **/
    @Override
    public int getItemCount() {
        if (mMovies == null) return 0;
        return mMovies.size();
    }

    /**
     * Used to set movies data in a list within the Adapter
     *
     * @param movies list of movies
     **/
    public void addListMovies(List<Movies> movies) {
        mMovies = movies;
        notifyDataSetChanged();
    }

}

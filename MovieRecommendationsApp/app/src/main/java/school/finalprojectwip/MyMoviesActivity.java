package school.finalprojectwip;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by frank on 4/6/2016.
 */

public class MyMoviesActivity extends AppCompatActivity {
    //we use these variables to ensure that our program runs in the correct order, since we are using several async threads, depending on the contents of the movies list
    private int NUM_TASKS = 0;
    private int NUM_TASKS_COMPLETED = 0;

    private ArrayList<Movie> movieList;

    private RelativeLayout loadingLayout;

    //booleans used to toggle sorting
    boolean mRatingDesc = false;
    boolean uRatingDesc = false;
    boolean hasSeenDec = false;
    boolean titleDesc = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_movies);

        //getting loading layout reference
        loadingLayout = (RelativeLayout) findViewById(R.id.loadingUpdatesLayout);


        DatabaseManager db = new DatabaseManager(MyMoviesActivity.this, null, null, 1);
        movieList = db.getMoviesAsList();







        boolean checkedMetascoresToday = db.hasCheckedMetascoresToday();
        if (!checkedMetascoresToday) {//this if statement makes sure that we are only making API calls to check for updated release dates ONCE a day, reducing loading times and the amount of API calls required
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            //once we have our list, it is possible that a movie has been released since we first added it to our database, so before rendering our listview, we iterate through the list
            //and update our metacritic ratings
            for (int i = 0; i < movieList.size(); i++) {//first we find the number of movies we want to check for updates
                if (movieList.get(i).getMetaScore() == -1) {
                    NUM_TASKS++;
                }
            }
            for (int i = 0; i < movieList.size(); i++) {
                if (movieList.get(i).getMetaScore() == -1) {//if a movie was added to a database as "not released", call API and update metarating to latest rating
                    AsyncUpdateMetascore a = new AsyncUpdateMetascore(movieList.get(i));
                    a.execute();
                }
            }
            /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

            db.updateCheckedDateToToday();//updating our database with the last time we checked for updated metascores

        }

        if(NUM_TASKS == 0){//if we have no threads to run, we continue with the program
            continueProgram();
        }

        db.close();
    }

    public void continueProgram(){//this is called after all of our threads updating the metacritic ratings are finished running
        createAdapter();
        loadingLayout.setVisibility(View.GONE);
    }

    public void createAdapter(){//using the listener pattern to set up ListView only after async thread is completed
        final ListView movieScroll = (ListView) findViewById(R.id.myMoviesActivityScroll);
        final List<Movie> movies = movieList;

        final MovieScrollAdapter adapter = new MovieScrollAdapter(movieScroll.getContext(), movies);
        movieScroll.setAdapter(adapter);

        movieScroll.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DatabaseManager db = new DatabaseManager(MyMoviesActivity.this, null, null, 1);
                if (!db.isInDatabase(movies.get(position))) {//if not in database, add movie and set it to "want to see"
                    db.addMovie(movies.get(position));
                } else {//if it is in the database, we toggle the "want to see" status
                    db.toggleMovieSeen(movies.get(position));
                }
                db.close();
                adapter.notifyDataSetChanged();
            }
        });

        movieScroll.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                DatabaseManager db = new DatabaseManager(MyMoviesActivity.this, null, null, 1);
                if (db.isInDatabase(movies.get(position))) {//if in database
                    db.deleteMovie(movies.get(position));
                }
                db.close();
                movieList.remove(position);
                adapter.notifyDataSetChanged();
                return true;
            }
        });

    }


    private class MovieScrollAdapter extends ArrayAdapter<Movie> {//custom array adapter
        private Context context;
        private List<Movie> movies;

        public MovieScrollAdapter(Context context, List<Movie> movies){
            super(context, -1, movies);
            this.context = context;
            this.movies = movies;

            if(this.movies.isEmpty()){//if no results were returned after all processing, display a toast letting the user know
                Toast.makeText(getApplicationContext(), R.string.no_movies, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent){
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.
                        getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.movie_layout, parent, false);
            }

            TextView title = (TextView) convertView.findViewById(R.id.title);
            title.setText(movies.get(position).getTitle());

            TextView plot = (TextView) convertView.findViewById(R.id.plot);
            plot.setText(movies.get(position).getPlot());

            TextView genre = (TextView) convertView.findViewById(R.id.genre);
            genre.setText(movies.get(position).getGenre());


            TextView metaScore = (TextView) convertView.findViewById(R.id.metascore);

            if(movies.get(position).getMetaScore() == -1){//if the metaScore is set to -1, that means movie has not been rated, which by inference means it is not yet released
                metaScore.setText(R.string.movie_not_released);
                metaScore.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9.5f);//smaller text so it fits without breaking anything
                metaScore.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
            }
            else if (movies.get(position).getMetaScore() == -2){
                StringBuilder s = new StringBuilder();
                s.append("    ");
                s.append(getString(R.string.metarating_not_available));
                s.append("  ");
                metaScore.setText(s.toString());
                metaScore.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);

                //setting up a "highlighted" background to achieve metacritic square effect
                Spannable spanText = Spannable.Factory.getInstance().newSpannable(metaScore.getText());
                spanText.setSpan(new BackgroundColorSpan(ContextCompat.getColor(context, R.color.metaScore)), 3, 7, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                metaScore.setText(spanText);
                metaScore.setTextColor(ContextCompat.getColor(context, android.R.color.primary_text_dark));
            }
              else {
                StringBuilder s = new StringBuilder();
                s.append("    ");
                s.append(Integer.valueOf(movies.get(position).getMetaScore()));
                s.append(" ");
                metaScore.setText(s.toString());
                metaScore.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);

                //setting up a "highlighted" background to achieve metacritic square effect
                Spannable spanText = Spannable.Factory.getInstance().newSpannable(metaScore.getText());
                spanText.setSpan(new BackgroundColorSpan(ContextCompat.getColor(context, R.color.metaScore)), 3, 7, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                metaScore.setText(spanText);
                metaScore.setTextColor(ContextCompat.getColor(context, android.R.color.primary_text_dark));
            }

            ImageView image = (ImageView) convertView.findViewById(R.id.imageView);
            new ImageDownloadTask((ImageView)image).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, movies.get(position).getPosterURL());//because there are several images to load here, we let these threads run parallel

            title.setOnClickListener(new View.OnClickListener() {//setting up a simple onClickListener that will open a link leading to more info about the movie in question!
                @Override
                public void onClick(View v) {
                    Uri uri = Uri.parse(movies.get(position).getMovieURL());
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            });

            //////////TAKING CARE OF "MOVIE SEEN" IMAGEVIEW HERE, AND RATINGS
            final DatabaseManager db = new DatabaseManager(getApplicationContext(), null, null, 1);
            ImageView movieSeen = (ImageView) convertView.findViewById(R.id.movieSeen);

            //custom rating bar for usage with listview
            ImageView rated1= (ImageView) convertView.findViewById(R.id.rated1);
            ImageView rated2= (ImageView) convertView.findViewById(R.id.rated2);
            ImageView rated3= (ImageView) convertView.findViewById(R.id.rated3);
            ImageView rated4= (ImageView) convertView.findViewById(R.id.rated4);
            ImageView rated5= (ImageView) convertView.findViewById(R.id.rated5);

            rated1.setColorFilter(ContextCompat.getColor(context, android.R.color.darker_gray));
            rated2.setColorFilter(ContextCompat.getColor(context, android.R.color.darker_gray));
            rated3.setColorFilter(ContextCompat.getColor(context, android.R.color.darker_gray));
            rated4.setColorFilter(ContextCompat.getColor(context, android.R.color.darker_gray));
            rated5.setColorFilter(ContextCompat.getColor(context, android.R.color.darker_gray));
            ///////////////////////////////////////////////////////////////////////


            if (db.isInDatabase(movies.get(position))) {//if in database
                if(db.wantToSee(movies.get(position))){//if marked as "want to see"
                    movieSeen.setImageResource(R.drawable.ic_check_circle_black_24dp);
                    movieSeen.setColorFilter(ContextCompat.getColor(context, R.color.wantToSee));
                    rated1.setVisibility(View.GONE);
                    rated2.setVisibility(View.GONE);
                    rated3.setVisibility(View.GONE);
                    rated4.setVisibility(View.GONE);
                    rated5.setVisibility(View.GONE);
                } else {//if user has already seen movie
                    movieSeen.setImageResource(R.drawable.ic_check_circle_black_24dp);
                    movieSeen.setColorFilter(ContextCompat.getColor(context, R.color.metaScore));
                    rated1.setVisibility(View.VISIBLE);
                    rated2.setVisibility(View.VISIBLE);
                    rated3.setVisibility(View.VISIBLE);
                    rated4.setVisibility(View.VISIBLE);
                    rated5.setVisibility(View.VISIBLE);

                    //here we get the rating from the database and set the stars accordingly
                    int userRating = db.getUserRating(movies.get(position));
                    if (userRating >= 1){
                        rated1.setColorFilter(ContextCompat.getColor(context, R.color.wantToSee));
                    }
                    if (userRating >= 2){
                        rated2.setColorFilter(ContextCompat.getColor(context, R.color.wantToSee));
                    }
                    if (userRating >= 3){
                        rated3.setColorFilter(ContextCompat.getColor(context, R.color.wantToSee));
                    }
                    if (userRating >= 4){
                        rated4.setColorFilter(ContextCompat.getColor(context, R.color.wantToSee));
                    }
                    if (userRating >= 5){
                        rated5.setColorFilter(ContextCompat.getColor(context, R.color.wantToSee));
                    }

                }
            } else {
                movieSeen.setImageResource(R.drawable.ic_add_circle_black_24dp);
                movieSeen.setColorFilter(ContextCompat.getColor(context, android.R.color.darker_gray));
                rated1.setVisibility(View.GONE);
                rated2.setVisibility(View.GONE);
                rated3.setVisibility(View.GONE);
                rated4.setVisibility(View.GONE);
                rated5.setVisibility(View.GONE);
            }

            rated1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    db.setUserRating(movies.get(position), 1);
                    notifyDataSetChanged();
                }
            });
            rated2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    db.setUserRating(movies.get(position), 2);
                    notifyDataSetChanged();
                }
            });
            rated3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    db.setUserRating(movies.get(position), 3);
                    notifyDataSetChanged();
                }
            });
            rated4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    db.setUserRating(movies.get(position), 4);
                    notifyDataSetChanged();
                }
            });
            rated5.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    db.setUserRating(movies.get(position), 5);
                    notifyDataSetChanged();
                }
            });


            db.close();
            ////////////////////////////////////////////////////////////////////////




            //taking care of sorting our arraylist right here
            Button sortByTitle = (Button) findViewById(R.id.sortByTitle);
            Button sortByHasSeen = (Button) findViewById(R.id.sortBySeen);
            Button sortByMetaRating = (Button) findViewById(R.id.sortByMetaRating);
            Button sortByUserRating = (Button) findViewById(R.id.sortByRating);

            //sort by user rating
            sortByUserRating.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    uRatingDesc = !uRatingDesc;

                    final DatabaseManager db = new DatabaseManager(context, null, null, 1);

                    if (uRatingDesc) {
                        Collections.sort(movies, new Comparator<Movie>() {
                            @Override
                            public int compare(Movie lhs, Movie rhs) {
                                if (db.getUserRating(lhs) > db.getUserRating(rhs)) {
                                    return -1;
                                } else if (db.getUserRating(lhs) < db.getUserRating(rhs)) {
                                    return 1;
                                } else {
                                    return 0;
                                }
                            }
                        });
                    } else {
                        Collections.sort(movies, new Comparator<Movie>() {
                            @Override
                            public int compare(Movie lhs, Movie rhs) {
                                if (db.getUserRating(lhs) < db.getUserRating(rhs)) {
                                    return -1;
                                } else if (db.getUserRating(lhs) > db.getUserRating(rhs)) {
                                    return 1;
                                } else {
                                    return 0;
                                }
                            }
                        });
                    }

                    db.close();
                    notifyDataSetChanged();

                }
            });

            //sort by title
            sortByTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    titleDesc = !titleDesc;

                    if (titleDesc) {


                        Collections.sort(movies, new Comparator<Movie>() {
                            @Override
                            public int compare(Movie lhs, Movie rhs) {
                                return lhs.getTitle().compareToIgnoreCase(rhs.getTitle());
                            }
                        });

                    } else {

                        Collections.sort(movies, new Comparator<Movie>() {
                            @Override
                            public int compare(Movie lhs, Movie rhs) {
                                return -lhs.getTitle().compareToIgnoreCase(rhs.getTitle());
                            }
                        });

                    }
                    notifyDataSetChanged();
                }
            });

            //sort by whether user has seen the movie
            sortByHasSeen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hasSeenDec = !hasSeenDec;

                    final DatabaseManager db = new DatabaseManager(context, null, null, 1);

                    if (hasSeenDec) {
                        Collections.sort(movies, new Comparator<Movie>() {
                            @Override
                            public int compare(Movie lhs, Movie rhs) {
                                if (db.wantToSee(lhs) && !db.wantToSee(rhs)) {
                                    return -1;
                                } else if (!db.wantToSee(lhs) && db.wantToSee(rhs)) {
                                    return 1;
                                } else {
                                    return 0;
                                }
                            }
                        });
                    } else {
                        Collections.sort(movies, new Comparator<Movie>() {
                            @Override
                            public int compare(Movie lhs, Movie rhs) {
                                if (!db.wantToSee(lhs) && db.wantToSee(rhs)) {
                                    return -1;
                                } else if (db.wantToSee(lhs) && !db.wantToSee(rhs)) {
                                    return 1;
                                } else {
                                    return 0;
                                }
                            }
                        });
                    }
                    db.close();
                    notifyDataSetChanged();
                }
            });

            sortByMetaRating = (Button) findViewById(R.id.sortByMetaRating);
            sortByMetaRating.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mRatingDesc = !mRatingDesc;
                    if (mRatingDesc) {

                        Collections.sort(movies, new Comparator<Movie>() {
                            @Override
                            public int compare(Movie lhs, Movie rhs) {
                                if (lhs.getMetaScore() > rhs.getMetaScore()) {
                                    return -1;
                                } else if (lhs.getMetaScore() < rhs.getMetaScore()) {
                                    return 1;
                                } else {
                                    return 0;
                                }
                            }
                        });
                    } else {
                        Collections.sort(movies, new Comparator<Movie>() {
                            @Override
                            public int compare(Movie lhs, Movie rhs) {
                                if (lhs.getMetaScore() < rhs.getMetaScore()) {
                                    return -1;
                                } else if (lhs.getMetaScore() > rhs.getMetaScore()) {
                                    return 1;
                                } else {
                                    return 0;
                                }
                            }
                        });
                    }
                    notifyDataSetChanged();
                }
            });
            //sorting clicklisteners end here//////////////////////////

            return convertView;
        }
    }

    public class ImageDownloadTask extends AsyncTask<String, Void, Bitmap> {//handles the downloading of images
        ImageView bitmapImage;

        public ImageDownloadTask(ImageView bitmapImage){
            this.bitmapImage = bitmapImage;
        }

        @Override
        protected Bitmap doInBackground(String... urls){
            String URLDisplay = urls[0];
            Bitmap mIcon = null;

            try {
                InputStream in = new java.net.URL(URLDisplay).openStream();
                mIcon = BitmapFactory.decodeStream(in);
            } catch (Exception e){
                e.printStackTrace();
            }
            return mIcon;
        }

        @Override
        protected void onPostExecute(Bitmap downloadedImage) {
            bitmapImage.setImageBitmap(downloadedImage);
        }
    }


    //we will execute an instance of this class for every movie that is marked as "not yet released" to see if it has been released since the time of adding, and update metascore accordingly
    private class AsyncUpdateMetascore extends AsyncTask<Void, Void, JSONObject>{
        private Movie movie;

        public AsyncUpdateMetascore(Movie movie) {
            this.movie = movie;
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            //we use the API to find the movie
            ApiObject apiObject = new ApiObject();
            JSONObject json;
            json = apiObject.searchForSpecificMovie(movie.getTitle(), movie.getReleaseYear());
            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json){
            JSONParser parser = new JSONParser();

            int currentMetascore = parser.findCurrentMetascore(json);
            if (currentMetascore != -1){//if the returned result is not -1, we can surmise that the movie has been released, and therefore update our databse with the correct metacritic rating
                DatabaseManager db = new DatabaseManager(getApplicationContext(), null, null, 1);
                db.setMetascore(movie, currentMetascore);
                db.close();
            }

            NUM_TASKS_COMPLETED++;

            if(NUM_TASKS_COMPLETED == NUM_TASKS){
                continueProgram();
            }

        }

    }

}

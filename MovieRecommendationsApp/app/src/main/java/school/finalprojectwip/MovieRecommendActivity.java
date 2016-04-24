package school.finalprojectwip;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MovieRecommendActivity extends AppCompatActivity {

    public static final Integer NUM_MOVIES = 50;//max returned by API is 50

    private JSONParser parser;//this takes the JSON object from an APIObject and parses it
    private ArrayList<Movie> list;//this will be used to store a local copy of the parsed list of movies once the API call has been completed

    private CheckBox action;
    private CheckBox adventure;
    private CheckBox fantasy;
    private CheckBox drama;
    private CheckBox scifi;
    private CheckBox animation;
    private CheckBox comedy;
    private CheckBox thriller;
    private CheckBox romance;
    private CheckBox horror;
    private CheckBox mystery;
    private CheckBox history;
    private ArrayList<CheckBox> genres;

    private ImageButton search;

    private RelativeLayout loadingLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendations);

        //setting loading bar to invisible upon instantiation
        loadingLayout = (RelativeLayout) findViewById(R.id.loadingLayout);
        loadingLayout.setVisibility(View.GONE);


        //getting references for check boxes
        final ArrayList<CheckBox> checkBoxes = new ArrayList<>();
        action = (CheckBox) findViewById(R.id.action);                  checkBoxes.add(action);
        adventure = (CheckBox) findViewById(R.id.adventure);            checkBoxes.add(adventure);
        fantasy = (CheckBox) findViewById(R.id.fantasy);                checkBoxes.add(fantasy);
        drama = (CheckBox) findViewById(R.id.drama);                    checkBoxes.add(drama);
        scifi = (CheckBox) findViewById(R.id.scifi);                    checkBoxes.add(scifi);
        animation = (CheckBox) findViewById(R.id.animation);            checkBoxes.add(animation);
        comedy = (CheckBox) findViewById(R.id.comedy);                  checkBoxes.add(comedy);
        thriller = (CheckBox) findViewById(R.id.thriller);              checkBoxes.add(thriller);
        romance = (CheckBox) findViewById(R.id.romance);                checkBoxes.add(romance);
        horror = (CheckBox) findViewById(R.id.horror);                  checkBoxes.add(horror);
        mystery = (CheckBox) findViewById(R.id.mystery);                checkBoxes.add(mystery);
        history = (CheckBox) findViewById(R.id.history);                checkBoxes.add(history);


        //getting reference for select all
        ImageButton selectAll = (ImageButton) findViewById(R.id.selectAll);
        selectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (CheckBox box : checkBoxes){
                    box.setChecked(true);
                }
            }
        });

        //getting reference for clearAll
        ImageButton clearAll = (ImageButton) findViewById(R.id.clearAll);
        clearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (CheckBox box : checkBoxes){
                    box.setChecked(false);
                }
            }
        });

        //search button
        search = (ImageButton) findViewById(R.id.searchButton);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                disableSearch();//the disableSearch and enableSearch helper functions are called after each click + API call to eliminate button spamming

                //we populate our genre list on the search click, so that we can decide whether we want to expend resources to make the api call
                genres = new ArrayList<>();
                if (action.isChecked()) {
                    genres.add(action);
                }
                if (adventure.isChecked()) {
                    genres.add(adventure);
                }
                if (fantasy.isChecked()) {
                    genres.add(fantasy);
                }
                if (drama.isChecked()) {
                    genres.add(drama);
                }
                if (scifi.isChecked()) {
                    genres.add(scifi);
                }
                if (animation.isChecked()) {
                    genres.add(animation);
                }
                if (comedy.isChecked()) {
                    genres.add(comedy);
                }
                if (thriller.isChecked()) {
                    genres.add(thriller);
                }
                if (romance.isChecked()) {
                    genres.add(romance);
                }
                if (horror.isChecked()) {
                    genres.add(horror);
                }
                if (mystery.isChecked()) {
                    genres.add(mystery);
                }
                if (history.isChecked()) {
                    genres.add(history);
                }

                if (genres.isEmpty()) {
                    Toast.makeText(getApplicationContext(), R.string.no_genre, Toast.LENGTH_SHORT).show();
                    enableSearch();
                } else {
                    AsyncAPICall task = new AsyncAPICall();
                    task.execute();
                }
            }
        });
        //////////////////




    }

    public void enableSearch(){
        loadingLayout.setBackgroundColor(0);
        loadingLayout.setVisibility(View.GONE);
        search.setEnabled(true);
    }
    public void disableSearch() {
        //getting default activity background, so we can cover up the movie list with that color while we are loading
        TypedArray array = getTheme().obtainStyledAttributes(new int[]{
                android.R.attr.colorBackground,
                android.R.attr.textColorPrimary,
        });
        int backgroundColor = array.getColor(0, 0xFF00FF);
        array.recycle();
        //////////////////////////////////////////////////


        loadingLayout.setBackgroundColor(backgroundColor);

        loadingLayout.setVisibility(View.VISIBLE);
        search.setEnabled(false);
    }

    public void createAdapter(){//using the listener pattern to set up ListView only after async thread is completed
        final ListView movieScroll = (ListView) findViewById(R.id.movieScroll);
        final List<Movie> movies = list;

        final MovieScrollAdapter adapter = new MovieScrollAdapter(movieScroll.getContext(), movies);
        movieScroll.setAdapter(adapter);

        movieScroll.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DatabaseManager db = new DatabaseManager(MovieRecommendActivity.this, null, null, 1);
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
                DatabaseManager db = new DatabaseManager(MovieRecommendActivity.this, null, null, 1);
                if(db.isInDatabase(movies.get(position))){//if in database
                    db.deleteMovie(movies.get(position));
                }
                db.close();
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
                Toast.makeText(getApplicationContext(), R.string.no_matches, Toast.LENGTH_SHORT).show();
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
            } else {
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
            return convertView;
        }
    }

    public class AsyncAPICall extends AsyncTask<Void, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(Void... params) {
            ApiObject nowPlaying = new ApiObject();



            JSONObject json = nowPlaying.requestNowPlaying(parser, list, genres);


            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json){

            ArrayList<String> genreStrings = new ArrayList<>();
            for(int i = 0; i < genres.size(); i++){
                genreStrings.add(genres.get(i).getText().toString());
            }

            parser = new JSONParser();
            list = parser.parseResults(json, genreStrings);

            createAdapter();
            enableSearch();
        }
    }

}

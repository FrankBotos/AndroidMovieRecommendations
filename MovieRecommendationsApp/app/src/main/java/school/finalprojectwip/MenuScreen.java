package school.finalprojectwip;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.view.View;
import android.widget.Button;

/**
 * Created by frank on 3/30/2016.
 */
public class MenuScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Button recommend = (Button) findViewById(R.id.movieRecommendations);
        recommend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), MovieRecommendActivity.class);
                startActivity(i);
            }
        });

        Button myMovies = (Button) findViewById(R.id.moviesIveSeen);
        myMovies.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), MyMoviesActivity.class);
                startActivity(i);
            }
        });

        Button search = (Button) findViewById(R.id.searchForMovie);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), SearchActivity.class);
                startActivity(i);
            }
        });

        Button help = (Button) findViewById(R.id.help);
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), HelpActivity.class);
                startActivity(i);
            }
        });

    }

}

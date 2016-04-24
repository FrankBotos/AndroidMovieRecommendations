package school.finalprojectwip;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by frank on 3/29/2016.
 */

public class Movie {
    private String title;
    private int metaScore;
    private String genre;
    private String plot;
    private String posterURL;
    private String movieURL;
    private String releaseYear;

    Movie(String title, int metaScore, String genre, String plot, String posterURL, String movieURL, String releaseYear){
        this.title = title;
        this.metaScore = metaScore;
        this.genre = genre;
        this.plot = plot;
        this.posterURL = posterURL;
        this.movieURL = movieURL;
        this.releaseYear = releaseYear;
    }

    @Override
    public String toString(){
        return title + "\nGenre: " + genre + "\n Metascore: " + metaScore + "\n " + plot;
    }

    public String getTitle(){return title;}
    public void setTitle(String title){this.title = title;}
    public int getMetaScore(){return metaScore;}
    public String getGenre(){
        if (genre.contains("\"")) {
            genre = genre.replaceAll("\"", "");
            genre = genre.replaceAll(",", ", ");
        }
        return genre;
    }
    public String getPlot(){return plot;}
    public String getPosterURL() {return posterURL;}
    public String getMovieURL() {return movieURL;}
    public String getReleaseYear() {return releaseYear;}


}

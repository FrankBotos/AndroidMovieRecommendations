package school.finalprojectwip;

import android.widget.CheckBox;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by frank on 3/29/2016.
 */
public class JSONParser {


    public ArrayList<Movie> parseResults(JSONObject nowPlaying, ArrayList<String> genres){

        ArrayList<Movie> list = new ArrayList<Movie>();//creating a new array list of type Movie, which encapsulates details about a movie

        try{

            for (int i = 0; i < MovieRecommendActivity.NUM_MOVIES; i++) {//imdb returns 10 movies currently in theaters, so we need only 10 iterations
                String title = nowPlaying
                        .getJSONObject("data")
                        .getJSONArray("movies")
                        .getJSONObject(i)
                        .getString("title");


                int metaScore = -1;
                String ms = nowPlaying
                        .getJSONObject("data")
                        .getJSONArray("movies")
                        .getJSONObject(i)
                        .getString("metascore");
                if(!ms.isEmpty()){
                    metaScore = Integer.parseInt(ms);
                }


                String genre = nowPlaying
                        .getJSONObject("data")
                        .getJSONArray("movies")
                        .getJSONObject(i)
                        .getJSONArray("genres").toString();

                String plot = nowPlaying
                        .getJSONObject("data")
                        .getJSONArray("movies")
                        .getJSONObject(i)
                        .getString("simplePlot");

                String posterURL = nowPlaying
                        .getJSONObject("data")
                        .getJSONArray("movies")
                        .getJSONObject(i)
                        .getString("urlPoster");
                if (posterURL.isEmpty()){
                    posterURL = "http://i1053.photobucket.com/albums/s463/Frank_Botos/noposter_zpskhaiyoqa.jpg";//this is an image that ive hosted on an external website to use if a movie has no poster!
                }

                String movieURL = nowPlaying
                        .getJSONObject("data")
                        .getJSONArray("movies")
                        .getJSONObject(i)
                        .getString("urlIMDB");

                String releaseYear = nowPlaying
                        .getJSONObject("data")
                        .getJSONArray("movies")
                        .getJSONObject(i)
                        .getString("year");
                if (releaseYear.isEmpty()){
                    releaseYear = "1000";
                } else {
                    releaseYear = releaseYear.substring(0,4);
                }

                //code block filters out genres that are passed in
                boolean add = false;
                for (int j = 0; j < genres.size(); j++){
                    if(add){ break; }
                    else {
                        if(genre.contains(genres.get(j).replaceAll("\\s",""))){//some trailing spaces are used in the strings.xml file for formatting. The ReplaceAll removes them here for accurate parsing
                            add = true;
                        }
                    }
                }

                //using newly parsed data, add a movie to our arraylist of movies, before iterating again
                if (add){
                    list.add(new Movie(title, metaScore, genre, plot, posterURL, movieURL, releaseYear));
                }

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return list;

    }



    public ArrayList<Movie> parseSearchResults(JSONObject searchResults) {
        ArrayList<Movie> list = new ArrayList<>();//creating a new array list of type Movie, which encapsulates details about a movie

        try {


            JSONArray moviesArray = searchResults
                    .getJSONObject("data")
                    .getJSONArray("movies");
            int numResults = moviesArray.length();


            for (int i = 0; i < numResults; i++) {
                String title = searchResults
                        .getJSONObject("data")
                        .getJSONArray("movies")
                        .getJSONObject(i)
                        .getString("title");
                if (title.isEmpty()){
                    title = "No Title Available";
                }


                int metaScore = -1;
                String ms = searchResults
                        .getJSONObject("data")
                        .getJSONArray("movies")
                        .getJSONObject(i)
                        .getString("metascore");
                if(!ms.isEmpty()){
                    metaScore = Integer.parseInt(ms);
                }
                if(ms.isEmpty()){
                    String release = searchResults
                            .getJSONObject("data")
                            .getJSONArray("movies")
                            .getJSONObject(i)
                            .getString("releaseDate");

                    if (!release.isEmpty()) {

                        //we know that if there is a date on record, the format is YYYYMMDD, YYYMM, or YYYY so we run the substring method based on that pattern
                        String day = "";
                        String month = "";
                        String year = "";
                        if(release.length() == 8) {
                            day = release.substring(6);
                            month = release.substring(4, 6);
                            year = release.substring(0, 4);
                        } else if (release.length() == 6){
                            month = release.substring(4, 6);
                            year = release.substring(0, 4);
                            day = "01";
                        } else if (release.length() == 4) {
                            year = release.substring(0, 4);
                            month = "01";
                            day = "01";
                        }



                        Date todayDate = new Date();
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(todayDate);

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                        try {
                            Date releaseDate = sdf.parse(year + "-" + month + "-" + day);


                            if(!releaseDate.after(todayDate)){
                                metaScore = -2;
                            }



                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                }


                String genre = searchResults
                        .getJSONObject("data")
                        .getJSONArray("movies")
                        .getJSONObject(i)
                        .getJSONArray("genres").toString();
                if (genre.equals("[]")){
                    genre="[No Genres Tagged]";
                }

                String plot = searchResults
                        .getJSONObject("data")
                        .getJSONArray("movies")
                        .getJSONObject(i)
                        .getString("simplePlot");
                if (plot.isEmpty()){
                    plot = "This movie has no description!";
                }

                String posterURL = searchResults
                        .getJSONObject("data")
                        .getJSONArray("movies")
                        .getJSONObject(i)
                        .getString("urlPoster");
                if (posterURL.isEmpty()){
                    posterURL = "http://i1053.photobucket.com/albums/s463/Frank_Botos/noposter_zpskhaiyoqa.jpg";//this is an image that ive hosted on an external website to use if a movie has no poster!
                }

                String movieURL = searchResults
                        .getJSONObject("data")
                        .getJSONArray("movies")
                        .getJSONObject(i)
                        .getString("urlIMDB");

                String releaseYear = searchResults
                        .getJSONObject("data")
                        .getJSONArray("movies")
                        .getJSONObject(i)
                        .getString("year");
                if (releaseYear.isEmpty()){
                    releaseYear = "1000";
                } else {
                    releaseYear = releaseYear.substring(0,4);
                }


                //using newly parsed data, add a movie to our arraylist of movies, before iterating again
                list.add(new Movie(title, metaScore, genre, plot, posterURL, movieURL, releaseYear));

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return list;
    }


    public int findCurrentMetascore(JSONObject json){

        int metaScore = -1;
        try {
            String ms = json
                    .getJSONObject("data")
                    .getJSONArray("movies")
                    .getJSONObject(0)
                    .getString("metascore");
            if (!ms.isEmpty()) {
                metaScore = Integer.parseInt(ms);
            }
            if (ms.isEmpty()) {
                String release = json
                        .getJSONObject("data")
                        .getJSONArray("movies")
                        .getJSONObject(0)
                        .getString("releaseDate");

                if (!release.isEmpty()) {

                    //we know that if there is a date on record, the format is YYYYMMDD, YYYMM, or YYYY so we run the substring method based on that pattern
                    String day = "";
                    String month = "";
                    String year = "";
                    if (release.length() == 8) {
                        day = release.substring(6);
                        month = release.substring(4, 6);
                        year = release.substring(0, 4);
                    } else if (release.length() == 6) {
                        month = release.substring(4, 6);
                        year = release.substring(0, 4);
                        day = "01";
                    } else if (release.length() == 4) {
                        year = release.substring(0, 4);
                        month = "01";
                        day = "01";
                    }


                    Date todayDate = new Date();
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(todayDate);

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                    try {
                        Date releaseDate = sdf.parse(year + "-" + month + "-" + day);


                        if (!releaseDate.after(todayDate)) {
                            metaScore = -2;
                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        return metaScore;

    }


}

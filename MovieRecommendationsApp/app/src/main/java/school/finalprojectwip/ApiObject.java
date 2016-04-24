package school.finalprojectwip;

import android.content.Context;
import android.widget.CheckBox;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by frank on 3/29/2016.
 */

public class ApiObject {

    public JSONObject requestNowPlaying(JSONParser parser, ArrayList<Movie> list, ArrayList<CheckBox> genres){

        JSONObject nowPlaying = null;//initializing a null JSONObject

        try {
            URL url = new URL("http://www.myapifilms.com/imdb/search?count=" + MovieRecommendActivity.NUM_MOVIES.toString() + "&token=b9b962e2-29b8-41fa-9180-894a0457240f&format=json&searchFilter=moviemeter&order=asc&titleType=feature&genres=");//hardcoded API call, this will always be the same for this particular functionality
            HttpURLConnection connection;

            //setting up a GET request and connection to the URL
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            //reading the API reply to a string
            InputStream iStream = connection.getInputStream();
            BufferedReader bReader = new BufferedReader(new InputStreamReader(iStream));
            String temp;
            StringBuilder result = new StringBuilder();
            while ((temp = bReader.readLine()) != null){
                result.append(temp);
            }

            //populating JSON object with the data we retrieved from the API
            nowPlaying = (JSONObject) new JSONTokener(result.toString()).nextValue();

        } catch (IOException|JSONException e){
            e.printStackTrace();
        }

        return nowPlaying;//returning our JSONObject

    }

    public JSONObject searchForSpecificMovie(String title, String releaseyear){
        JSONObject result = null;

        title = title.replaceAll("\\s+", "+");
        title = title.replaceAll(":", "%3A");

        try {

            URL url = new URL(
                    "http://www.myapifilms.com/imdb/idIMDB?title=" + title + "&year=" + releaseyear + "&token=b9b962e2-29b8-41fa-9180-894a0457240f&format=json&language=en-us&aka=0&business=0&seasons=0&seasonYear=0&technical=0&filter=2&exactFilter=0&limit=1&forceYear=0&trailers=0&movieTrivia=0&awards=0&moviePhotos=0&movieVideos=0&actors=0&biography=0&uniqueName=0&filmography=0&bornAndDead=0&starSign=0&actorActress=0&actorTrivia=0&similarMovies=0&adultSearch=0&goofs=0&quotes=0&fullSize=0"
            );
            HttpURLConnection connection;

            //setting up a GET request and connection to the URL
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            //reading the API reply to a string
            InputStream iStream = connection.getInputStream();
            BufferedReader bReader = new BufferedReader(new InputStreamReader(iStream));
            String temp;
            StringBuilder res = new StringBuilder();
            while ((temp = bReader.readLine()) != null){
                res.append(temp);
            }

            //populating JSON object with the data we retrieved from the API
            result = (JSONObject) new JSONTokener(res.toString()).nextValue();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }


    public JSONObject searchMovies(String searchTerm){
        JSONObject searchResults = null;

        try {

            searchTerm = searchTerm.replaceAll("\\s+", "+");
            String searchURL = "http://www.myapifilms.com/imdb/idIMDB?title=" + searchTerm + "&token=b9b962e2-29b8-41fa-9180-894a0457240f&format=json&language=en-us&aka=0&business=0&seasons=0&seasonYear=0&technical=0&filter=3&exactFilter=0&limit=10&forceYear=0&trailers=0&movieTrivia=0&awards=0&moviePhotos=0&movieVideos=0&actors=0&biography=0&uniqueName=0&filmography=0&bornAndDead=0&starSign=0&actorActress=0&actorTrivia=0&similarMovies=0&adultSearch=0&goofs=0&quotes=0&fullSize=0";

            URL url = new URL(searchURL);//hardcoded API call, this will always be the same for this particular functionality
            HttpURLConnection connection;

            //setting up a GET request and connection to the URL
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            //reading the API reply to a string
            InputStream iStream = connection.getInputStream();
            BufferedReader bReader = new BufferedReader(new InputStreamReader(iStream));
            String temp;
            StringBuilder result = new StringBuilder();
            while ((temp = bReader.readLine()) != null){
                result.append(temp);
            }

            //populating JSON object with the data we retrieved from the API
            searchResults = (JSONObject) new JSONTokener(result.toString()).nextValue();

        } catch (IOException|JSONException e){
            e.printStackTrace();
        }


        return searchResults;
    }



}

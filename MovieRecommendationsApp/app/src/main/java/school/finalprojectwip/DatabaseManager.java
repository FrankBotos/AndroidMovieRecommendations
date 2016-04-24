package school.finalprojectwip;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.format.DateUtils;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by frank on 4/5/2016.
 */

public class DatabaseManager extends SQLiteOpenHelper {
    private String createQuery = "CREATE TABLE mymovies ( " +
            "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "title TEXT, " +
            "plot TEXT, " +
            "releaseyear TEXT, " +
            "posterurl TEXT, " +
            "movieurl TEXT, " +
            "metascore INTEGER, " +
            "genre TEXT, " +
            "wanttosee BOOLEAN, " +//this will be 0 or 1, 0 for false, 1 for true
            "userrating TEXT" +
            ");";

    private String createQuery2 = "CREATE TABLE datelastupdated ( " +
            "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "day TEXT, " +
            "month TEXT, " +
            "year TEXT" +
            ");";

    //we only ever want a single row in our datelastupdated table, so we insert a single entry and update it when needed
    private String createQuery3 = "INSERT INTO datelastupdated (day, month, year) VALUES ('placeholder', 'placeholder', 'placeholder');";

    public static final String DATABASE_NAME = "MoviesDB.db";

    public DatabaseManager(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(createQuery);
        db.execSQL(createQuery2);
        db.execSQL(createQuery3);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS mymovies");
        db.execSQL("DROP TABLE IF EXISTS datelastupdated");
        onCreate(db);
    }


    public void addMovie(Movie movie){
        ContentValues values = new ContentValues();
        values.put("title", movie.getTitle());
        values.put("plot", movie.getPlot());
        values.put("releaseyear", movie.getReleaseYear());
        values.put("posterurl", movie.getPosterURL());
        values.put("movieurl", movie.getMovieURL());
        values.put("metascore", movie.getMetaScore());
        values.put("genre", movie.getGenre());
        values.put("wanttosee", true);
        values.put("userrating", 0);
        SQLiteDatabase db = getWritableDatabase();
        db.insert("mymovies", null, values);
        db.close();
    }

    public void deleteMovie(Movie movie){
        String query = "DELETE FROM mymovies WHERE title = \"" + movie.getTitle() + "\" AND releaseyear = \"" + movie.getReleaseYear() + "\"";
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
        db.close();
    }

    public void toggleMovieSeen(Movie movie){
        String query = "SELECT * FROM mymovies WHERE title = \"" + movie.getTitle() + "\" AND releaseyear = \"" + movie.getReleaseYear() + "\"";
        SQLiteDatabase db = getWritableDatabase();

        Cursor c = db.rawQuery(query, null);
        String res;

        //move to first row in your results
        c.moveToFirst();
        res = c.getString(c.getColumnIndex("wanttosee"));
        c.close();

        int newValue = Integer.parseInt(res);

        if (newValue == 1){//swapping here
            newValue = 0;
        } else {
            newValue = 1;
        }

        String updateQuery = "UPDATE mymovies SET wanttosee = " + newValue + " WHERE title = \"" + movie.getTitle() + "\" AND releaseyear = \"" + movie.getReleaseYear() + "\"";

        if(movie.getMetaScore() != -1) {//metascore of -1 is our code for "not released", meaning the movie should not be marked as "has seen"
            db.execSQL(updateQuery);
        }

        db.close();

    }

    public boolean wantToSee(Movie movie){
        boolean wantToSee = false;

        String query = "SELECT wanttosee FROM mymovies WHERE title = \"" + movie.getTitle() + "\" AND releaseyear = \"" + movie.getReleaseYear() + "\"";
        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.rawQuery(query, null);
        String res;

        //move to first row in your results
        c.moveToFirst();
        res = c.getString(c.getColumnIndex("wanttosee"));
        c.close();

        int newValue = Integer.parseInt(res);

        if (newValue == 1){
            wantToSee = true;
        }



        return wantToSee;
    }

    public int getUserRating(Movie movie){
        int res = 0;
        String query = "SELECT userrating FROM mymovies WHERE title = \"" + movie.getTitle() + "\" AND releaseyear = \"" + movie.getReleaseYear() + "\"";
        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.rawQuery(query, null);

        c.moveToFirst();
        res = c.getInt(c.getColumnIndex("userrating"));
        c.close();
        db.close();

        return res;
    }

    public void setUserRating(Movie movie, int rating){
        SQLiteDatabase db = getWritableDatabase();
        String updateQuery = "UPDATE mymovies SET userrating = " + rating + " WHERE title = \"" + movie.getTitle() + "\" AND releaseyear = \"" + movie.getReleaseYear() + "\"";
        db.execSQL(updateQuery);
        db.close();
    }

    public void setMetascore(Movie movie, int metascore){
        SQLiteDatabase db = getWritableDatabase();
        String updateQuery = "UPDATE mymovies SET metascore = " + metascore + " WHERE title = \"" + movie.getTitle() + "\" AND releaseyear = \"" + movie.getReleaseYear() + "\"";
        db.execSQL(updateQuery);
        db.close();
    }

    public boolean isInDatabase(Movie movie){
        boolean inDatabase = false;

        String query = "SELECT * FROM mymovies WHERE title = \"" + movie.getTitle() + "\" AND releaseyear = \"" + movie.getReleaseYear() + "\"";
        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.rawQuery(query, null);

        if (c.getCount() == 1){
            inDatabase = true;
        }

        c.close();
        db.close();
        return inDatabase;
    }

    public boolean hasCheckedMetascoresToday(){//returns whether the date stored in table "datelastupdated" is equal to today's date
        boolean hasCheckedToday = false;

        String query = "SELECT * FROM datelastupdated WHERE _id = 1";
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(query, null);

        c.moveToFirst();
        String day = c.getString(c.getColumnIndex("day"));
        String month = c.getString(c.getColumnIndex("month"));
        String year = c.getString(c.getColumnIndex("year"));

        if(!day.equals("placeholder")){//if res is equal to anything other than the default value "placeholder," then we do the actual date check

            Date todayDate;
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            todayDate = cal.getTime();

            Date lastUpdated;
            Calendar cal2 = Calendar.getInstance();
            cal2.set(Calendar.HOUR_OF_DAY, 0);
            cal2.set(Calendar.MINUTE, 0);
            cal2.set(Calendar.SECOND, 0);
            cal2.set(Calendar.MILLISECOND, 0);

            cal2.set(Calendar.YEAR, Integer.parseInt(year));
            cal2.set(Calendar.MONTH, Integer.parseInt(month));
            cal2.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day));
            lastUpdated = cal.getTime();

            System.out.println("Today - > " + todayDate);
            System.out.println("LASTU - > " + lastUpdated);


            if(lastUpdated.before(todayDate)){
                hasCheckedToday = false;
            } else {
                hasCheckedToday = true;
            }


        }

        c.close();
        db.close();
        return hasCheckedToday;
    }

    public void updateCheckedDateToToday(){

        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        int d = cal.get(Calendar.DAY_OF_MONTH);
        int m = cal.get(Calendar.MONTH) + 1;
        int y = cal.get(Calendar.YEAR);

        String day = Integer.toString(d);
        String month = Integer.toString(m);
        String year = Integer.toString(y);

        String query = "UPDATE datelastupdated SET day = \"" + day + "\", month = \"" + month + "\", year = \"" + year + "\" WHERE _id = 1";
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);

    }

    public ArrayList<Movie> getMoviesAsList(){
        ArrayList<Movie> movies = new ArrayList<>();

        String query = "SELECT * FROM mymovies";
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.rawQuery(query, null);

        String title;
        String plot;
        String releaseyear;
        String posterurl;
        String movieurl;
        int metascore;
        String genre;
        boolean wantToSee;
        String userRating;

        while (c.moveToNext()) {
            if (c.getString(c.getColumnIndex("title")) != null) {
                title = c.getString(c.getColumnIndex("title"));
                plot = c.getString(c.getColumnIndex("plot"));
                releaseyear = c.getString(c.getColumnIndex("releaseyear"));
                posterurl = c.getString(c.getColumnIndex("posterurl"));
                movieurl = c.getString(c.getColumnIndex("movieurl"));
                metascore = c.getInt(c.getColumnIndex("metascore"));
                genre = c.getString(c.getColumnIndex("genre"));

                movies.add(new Movie(title, metascore, genre, plot, posterurl, movieurl, releaseyear));

            }
        }

        c.close();
        db.close();
        return movies;
    }

    public void reset(){
        String query = "DROP TABLE mymovies";
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
        db.execSQL(createQuery);
        db.close();
    }

    @Override
    public String toString() {
        String dbString = "";
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM mymovies";

        //Cursor points to location in results
        Cursor c = db.rawQuery(query, null);

        //move to first row in your results
        c.moveToFirst();


        //moves cursor through each row, and appends title to string
        while (c.moveToNext()) {
            if (c.getString(c.getColumnIndex("title")) != null) {
                dbString += c.getString(c.getColumnIndex("title"));
                dbString += " - " + (c.getString(c.getColumnIndex("plot")));
                dbString += "\n";
            }
        }

        db.close();
        dbString += "---------------------------------------\n\n";
        return dbString;

    }



}

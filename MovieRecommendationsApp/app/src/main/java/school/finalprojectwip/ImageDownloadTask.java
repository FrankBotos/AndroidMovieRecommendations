package school.finalprojectwip;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.InputStream;

/**
 * Created by frank on 4/7/2016.
 */
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

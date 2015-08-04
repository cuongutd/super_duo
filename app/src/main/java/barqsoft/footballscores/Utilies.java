package barqsoft.footballscores;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.GenericRequestBuilder;
import com.bumptech.glide.Glide;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.StreamEncoder;
import com.bumptech.glide.load.resource.file.FileToStreamDecoder;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.svgsample.app.SvgDecoder;
import com.bumptech.svgsample.app.SvgDrawableTranscoder;
import com.bumptech.svgsample.app.SvgSoftwareLayerSetter;
import com.caverock.androidsvg.SVG;
import com.larvalabs.svgandroid.SVGParser;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Created by yehya khaled on 3/3/2015.
 */
public class Utilies
{

    private static final String LOG_TAG = Utilies.class.getSimpleName();

    private static GenericRequestBuilder<Uri, InputStream, SVG, PictureDrawable> requestBuilder;

    public static String getScores(int home_goals,int awaygoals)
    {
        if(home_goals < 0 || awaygoals < 0)
        {
            return " - ";
        }
        else
        {
            return String.valueOf(home_goals) + " - " + String.valueOf(awaygoals);
        }
    }

    public static GenericRequestBuilder getRequestBuilder(Context context){

        if (requestBuilder == null)
            requestBuilder = Glide.with(context)
                    .using(Glide.buildStreamModelLoader(Uri.class, context), InputStream.class)
                    .from(Uri.class)
                    .as(SVG.class)
                    .transcode(new SvgDrawableTranscoder(), PictureDrawable.class)
                    .sourceEncoder(new StreamEncoder())
                    .cacheDecoder(new FileToStreamDecoder<SVG>(new SvgDecoder()))
                    .decoder(new SvgDecoder())
                    .placeholder(R.drawable.image_loading)
                    .error(R.drawable.image_error)
                    //.animate(android.R.anim.fade_in)
                    .listener(new SvgSoftwareLayerSetter<Uri>());

        return requestBuilder;
    }


    public static void downloadImageToView(String imgUrl, ImageView view, Context context) {

        Log.d(LOG_TAG, "imgUrl: "+ imgUrl);
        getRequestBuilder(context)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                        // SVG cannot be serialized so it's not worth to cache it
                .load(Uri.parse(imgUrl))
                .into(view);

    }

    public static Bitmap transformSVGImageToBitmap(String svgImgContent) {
        Bitmap bitmap = null;
        try {
            com.larvalabs.svgandroid.SVG svg = SVGParser.getSVGFromString(svgImgContent);
            PictureDrawable pictureDrawable = svg.createPictureDrawable();

            bitmap = Bitmap.createBitmap(pictureDrawable.getIntrinsicWidth(), pictureDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawPicture(pictureDrawable.getPicture());
        }catch(Exception e){
            Log.e(LOG_TAG, svgImgContent);
            e.printStackTrace();
        }
        return bitmap;
    }

    public static Drawable transformSVGImageToDrawable(String svgImgContent) {
        Drawable drawable = null;
        try {
            com.larvalabs.svgandroid.SVG svg = SVGParser.getSVGFromString(svgImgContent);
            drawable = svg.createPictureDrawable();
        }catch(Exception e){
            Log.e(LOG_TAG, svgImgContent);
            e.printStackTrace();
        }
        return drawable;
    }

}

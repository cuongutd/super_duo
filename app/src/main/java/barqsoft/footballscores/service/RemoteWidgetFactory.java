package barqsoft.footballscores.service;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

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

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.PagerFragment;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;
import barqsoft.footballscores.WidgetHolder;
import barqsoft.footballscores.scoresAdapter;

/**
 * Created by Cuong on 7/30/2015.
 */
public class RemoteWidgetFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context mContext;
    private int mAppWidgetId;
    private List<WidgetHolder> mWidgetItems = new ArrayList<WidgetHolder>();
    private static final String LOG_TAG = RemoteWidgetFactory.class.getSimpleName();

    private int current_fragment;



    public RemoteWidgetFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {

        SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
        for (int i = 0; i < PagerFragment.NUM_PAGES; i++) {

            Date fragmentdate = new Date(System.currentTimeMillis() + ((i - 2) * 86400000));
            //query from DB and get list of scores assigned to mWidgetItems
            String[] arg = {""};
            arg[0] = mformat.format(fragmentdate);
            Cursor cursor = mContext.getContentResolver().query(
                    DatabaseContract.scores_table.buildScoreWithDate(),
                    null, // leaving "columns" null just returns all the columns.
                    null, // cols for "where" clause
                    arg, // values for "where" clause
                    null  // sort order
            );

            //add query result to list
            if (cursor.moveToFirst()){
                current_fragment = i;
                do {
                    WidgetHolder holder = new WidgetHolder();
                    holder.home_name = (cursor.getString(scoresAdapter.COL_HOME));
                    holder.away_name = (cursor.getString(scoresAdapter.COL_AWAY));

                    String s = cursor.getString(scoresAdapter.COL_HOME_CREST);
                    if (s != null && !"".equals(s))
                        holder.home_crest=Utilies.transformSVGImageToBitmap(s);

                    s = cursor.getString(scoresAdapter.COL_AWAY_CREST);
                    if (s != null && !"".equals(s))
                        holder.away_crest=Utilies.transformSVGImageToBitmap(s);

                    holder.date = (cursor.getString(scoresAdapter.COL_MATCHTIME));
                    holder.score = (Utilies.getScores(cursor.getInt(scoresAdapter.COL_HOME_GOALS), cursor.getInt(scoresAdapter.COL_AWAY_GOALS)));
                    mWidgetItems.add(holder);

                }while (cursor.moveToNext());
                cursor.close();
                break; //load only 1 day having data
            }
            cursor.close();
        }

    }

    @Override
    public void onDataSetChanged() {
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return mWidgetItems.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {

        // Construct a remote views item based on the app widget item XML file,
        // and set the text based on the position.
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.scores_list_item);

        rv.setTextViewText(R.id.home_name, mWidgetItems.get(position).home_name);
        rv.setTextViewText(R.id.away_name, mWidgetItems.get(position).away_name);
        rv.setTextViewText(R.id.score_textview, mWidgetItems.get(position).score);
        rv.setTextViewText(R.id.data_textview, mWidgetItems.get(position).date);

        //TODO: set correct images
        //rv.setImageViewResource(R.id.home_crest, R.drawable.ic_launcher);
        //rv.setImageViewResource(R.id.away_crest, R.drawable.ic_launcher);
//        Utilies.getRequestBuilder(mContext).diskCacheStrategy(DiskCacheStrategy.SOURCE)
//                // SVG cannot be serialized so it's not worth to cache it
//                .load(Uri.parse(mWidgetItems.get(position).home_crest_url))
//                .into(homeBitmapTarget);
//
//        rv.setImageViewBitmap(R.id.home_crest, homeBitmap);
//
//        Utilies.getRequestBuilder(mContext).diskCacheStrategy(DiskCacheStrategy.SOURCE)
//                // SVG cannot be serialized so it's not worth to cache it
//                .load(Uri.parse(mWidgetItems.get(position).away_crest_url))
//                .into(awayBitmapTarget);
//
//        rv.setImageViewBitmap(R.id.away_crest, awayBitmap);

//not working: ex You must call this method on the main thread
//        try {
//            Glide.with(mContext)
//                    .using(Glide.buildStreamModelLoader(Uri.class, mContext), InputStream.class)
//                    .from(Uri.class)
//                    .as(SVG.class)
//                    .transcode(new SvgDrawableTranscoder(), PictureDrawable.class)
//                    .sourceEncoder(new StreamEncoder())
//                    .cacheDecoder(new FileToStreamDecoder<SVG>(new SvgDecoder()))
//                    .decoder(new SvgDecoder())
//                    .listener(new SvgSoftwareLayerSetter<Uri>())
//                    .load(Uri.parse(mWidgetItems.get(position).home_crest_url))
//                    .into((Target)new SimpleTarget<Bitmap>(40, 40) {
//                        @Override
//                        public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
//                            homeBitmap = resource;
//                        }
//
//                    });
//        } catch (Exception e) {
//            Log.e(LOG_TAG, e.getMessage());
//        }

//        if (homeBitmap != null)
//            rv.setImageViewBitmap(R.id.home_crest, homeBitmap);

        //TODO: get svg image from in
        //rv.setImageViewResource(R.id.home_crest, R.drawable.manchester_united);
        //rv.setImageViewResource(R.id.away_crest, R.drawable.manchester_united);

        Intent fillInIntent = new Intent();
        Bundle extras = new Bundle();
        extras.putInt("CURRENT_FRAGMENT", current_fragment);
        fillInIntent.putExtras(extras);

        rv.setOnClickFillInIntent(R.id.home_name, fillInIntent);
        rv.setOnClickFillInIntent(R.id.away_name, fillInIntent);
        rv.setOnClickFillInIntent(R.id.score_textview, fillInIntent);
        rv.setOnClickFillInIntent(R.id.data_textview, fillInIntent);
        rv.setOnClickFillInIntent(R.id.away_crest, fillInIntent);
        rv.setOnClickFillInIntent(R.id.home_crest, fillInIntent);
        rv.setOnClickFillInIntent(R.id.emptyTextView, fillInIntent);
        // Return the remote views object.
        return rv;
    }

//    Bitmap homeBitmap;
//
//    Target homeBitmapTarget = new SimpleTarget<Bitmap>() {
//        @Override
//        public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
//            homeBitmap = resource;
//        }
//
//        @Override
//        public void onLoadFailed(Exception e, Drawable errorDrawable) {
//            homeBitmap = null;
//        }
//
//    };
//
//    Bitmap awayBitmap;
//
//    Target awayBitmapTarget = new SimpleTarget<Bitmap>() {
//        @Override
//        public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
//            awayBitmap = resource;
//        }
//
//        @Override
//        public void onLoadFailed(Exception e, Drawable errorDrawable) {
//            awayBitmap = null;
//        }
//
//    };

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }


}
package barqsoft.footballscores.service;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import barqsoft.footballscores.DatabaseContract;
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

    public RemoteWidgetFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {

        //query from DB and get list of scores assigned to mWidgetItems
        Date fragmentdate = new Date(System.currentTimeMillis());
        SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");

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
            do {
                WidgetHolder holder = new WidgetHolder();
                holder.home_name = (cursor.getString(scoresAdapter.COL_HOME));
                holder.away_name = (cursor.getString(scoresAdapter.COL_AWAY));
                holder.date = (cursor.getString(scoresAdapter.COL_MATCHTIME));
                holder.score = (Utilies.getScores(cursor.getInt(scoresAdapter.COL_HOME_GOALS), cursor.getInt(scoresAdapter.COL_AWAY_GOALS)));
                mWidgetItems.add(holder);

            }while (cursor.moveToNext());
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
        rv.setImageViewResource(R.id.home_crest, Utilies.getTeamCrestByTeamName(mWidgetItems.get(position).home_name));
        rv.setImageViewResource(R.id.away_crest, Utilies.getTeamCrestByTeamName(mWidgetItems.get(position).away_name));

        Intent fillInIntent = new Intent();
        rv.setOnClickFillInIntent(R.id.home_name, fillInIntent);
        rv.setOnClickFillInIntent(R.id.away_name, fillInIntent);
        rv.setOnClickFillInIntent(R.id.score_textview, fillInIntent);
        rv.setOnClickFillInIntent(R.id.data_textview, fillInIntent);
        rv.setOnClickFillInIntent(R.id.away_crest, fillInIntent);
        rv.setOnClickFillInIntent(R.id.home_crest, fillInIntent);
        // Return the remote views object.
        return rv;
    }

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

//... include adapter-like methods here. See the StackView Widget sample.

}
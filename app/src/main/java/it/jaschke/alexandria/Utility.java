package it.jaschke.alexandria;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import it.jaschke.alexandria.services.BookService;

/**
 * Created by Cuong on 7/29/2015.
 */
public class Utility {

    @SuppressWarnings("ResourceType")
    public static
    @BookService.BookServiceStatus
    int getFetchBookResult(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        return pref.getInt(context.getString(R.string.fetch_book_result), BookService.BOOK_SERVICE_UNKNOWN);
    }

    public static boolean isNetworkAccessible(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }


    public static String getFetchBookResultMsg(Context context) {
        String msg = "";
        @BookService.BookServiceStatus int fetchResult = getFetchBookResult(context);
        if (fetchResult > 0) //something wrong
            switch (fetchResult) {
                case BookService.BOOK_SERVICE_SERVER_DOWN:
                    msg = context.getString(R.string.fetch_result_server_down);
                case BookService.BOOK_SERVICE_SERVER_INVALID:
                    msg = context.getString(R.string.fetch_result_server_invalid);
                default:
                    if (!isNetworkAccessible(context))
                        msg = context.getString(R.string.fetch_result_network_down);
                    else
                        msg = context.getString(R.string.fetch_result_unknown);
            }
        return msg;
    }

    public static void sendBroadcastMsg(Context context, int msgId) {
        Intent messageIntent = new Intent(MainActivity.MESSAGE_EVENT);
        messageIntent.putExtra(MainActivity.MESSAGE_KEY, context.getString(msgId));
        LocalBroadcastManager.getInstance(context).sendBroadcast(messageIntent);

    }

    public static void downloadImageToView(String imgUrl, ImageView view, Context context) {
        try {
            Picasso.with(context).load(imgUrl).into(view);
        } catch (Exception e) {
            Log.e("Error loading book img", e.getMessage());
        }
    }

}

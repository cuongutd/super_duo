package it.jaschke.alexandria;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.services.BookService;


public class BookDetail extends MyFragment implements LoaderManager.LoaderCallbacks<Cursor> {


    private static final String LOG_TAG = BookDetail.class.getSimpleName();

    public static final String EAN_KEY = "EAN";
    private final int LOADER_ID = 10;
    private View rootView;
    private String ean;
    private String mBookTitle;
    private String mBookSubTitle;
    private String mDesc;
    private String mAuthors;
    private String mImgUrl;
    private String mCategories;

    private ShareActionProvider shareActionProvider;

    public BookDetail(){
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //when rotating the book detail screen several times, exception occurred at shareActionProvider.setShareIntent(shareIntent);
        //onCreateOptionsMenu() is not called. the shareActionProvider is null. retain the fragment fixed the issue
        //setRetainInstance(true);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            ean = arguments.getString(BookDetail.EAN_KEY);
            getLoaderManager().restartLoader(LOADER_ID, null, this);
        }

        rootView = inflater.inflate(R.layout.fragment_full_book, container, false);
        rootView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, ean);
                bookIntent.setAction(BookService.DELETE_BOOK);
                getActivity().startService(bookIntent);
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        if(savedInstanceState!=null){
            mBookTitle = savedInstanceState.getString("mBookTitle");
            mBookSubTitle = savedInstanceState.getString("mBookSubTitle");
            mAuthors = savedInstanceState.getString("mAuthors");
            mDesc = savedInstanceState.getString("mDesc");
            mImgUrl = savedInstanceState.getString("mImgUrl");
            mCategories = savedInstanceState.getString("mCategories");
            populateViews();


        }


        return rootView;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.book_detail, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(ean)),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }

        mBookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        mBookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        mDesc = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.DESC));
        mAuthors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
        mImgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        mCategories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
        populateViews();

    }

    private void populateViews(){

        ((TextView) rootView.findViewById(R.id.fullBookTitle)).setText(mBookTitle);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text) + mBookTitle);
        if (shareActionProvider != null)
            shareActionProvider.setShareIntent(shareIntent);

        ((TextView) rootView.findViewById(R.id.fullBookSubTitle)).setText(mBookSubTitle);

        ((TextView) rootView.findViewById(R.id.fullBookDesc)).setText(mDesc);

        //some books returned with null authors, this if is to handle such cases
        if (mAuthors != null) {
            String[] authorsArr = mAuthors.split(",");
            ((TextView) rootView.findViewById(R.id.authors)).setLines(authorsArr.length);
            ((TextView) rootView.findViewById(R.id.authors)).setText(mAuthors.replace(",", "\n"));
        }
        if(Patterns.WEB_URL.matcher(mImgUrl).matches()){
            ImageView fullBookCover = (ImageView) rootView.findViewById(R.id.fullBookCover);
            Utility.downloadImageToView(mImgUrl, fullBookCover, getActivity());
            fullBookCover.setVisibility(View.VISIBLE);
        }

        ((TextView) rootView.findViewById(R.id.categories)).setText(mCategories);


    }


    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putString("mBookTitle", mBookTitle);
        outState.putString("mBookSubTitle", mBookSubTitle);
        outState.putString("mDesc", mDesc);
        outState.putString("mAuthors", mAuthors);
        outState.putString("mImgUrl", mImgUrl);
        outState.putString("mCategories", mCategories);

        super.onSaveInstanceState(outState);
    }
}
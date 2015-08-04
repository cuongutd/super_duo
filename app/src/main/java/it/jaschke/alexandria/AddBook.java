package it.jaschke.alexandria;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


import net.sourceforge.zbar.android.ScanBookActivity;

import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.services.BookService;


public class AddBook extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "INTENT_TO_SCAN_ACTIVITY";
    private EditText ean;
    private final int LOADER_ID = 1;
    private View rootView;
    private final String EAN_CONTENT="eanContent";
    private static final String SCAN_FORMAT = "scanFormat";
    private static final String SCAN_CONTENTS = "scanContents";

    private String mScanFormat = "Format:";
    private String mScanContents = "Contents:";

    private String mBookTitle;
    private String mBookSubTitle;
    private String mAuthors;
    private String mImgUrl;
    private String mCategories;





    public AddBook(){
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_add_book, container, false);
        ean = (EditText) rootView.findViewById(R.id.ean);

        ean.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //no need
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //no need
            }

            @Override
            public void afterTextChanged(Editable s) {
                String ean = s.toString();
                //catch isbn10 numbers
                if (ean.length() == 10 && !ean.startsWith("978")) {
                    ean = "978" + ean;
                }
                if (ean.length() < 13) {
                    clearFields();
                    return;
                }
                //Once we have an ISBN, start a book intent
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, ean);
                bookIntent.setAction(BookService.FETCH_BOOK);
                getActivity().startService(bookIntent);
                AddBook.this.restartLoader();
            }
        });

        rootView.findViewById(R.id.scan_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), ScanBookActivity.class);
                startActivityForResult(intent, 0);

//                IntentIntegrator integrator = new IntentIntegrator(AddBook.this);
//                integrator.addExtra("SCAN_WIDTH", 800);
//                integrator.addExtra("SCAN_HEIGHT", 200);
//                integrator.addExtra("RESULT_DISPLAY_DURATION_MS", 3000L);
//                integrator.addExtra("PROMPT_MESSAGE", "Scan a book ISBN");
//                integrator.initiateScan();//scan all types



            }
        });

        rootView.findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ean.setText("");
            }
        });

        rootView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, ean.getText().toString());
                bookIntent.setAction(BookService.DELETE_BOOK);
                getActivity().startService(bookIntent);
                ean.setText("");
            }
        });

        if(savedInstanceState!=null){
            ean.setText(savedInstanceState.getString(EAN_CONTENT));
            ean.setHint("");

            mBookTitle = savedInstanceState.getString("mBookTitle");
            mBookSubTitle = savedInstanceState.getString("mBookSubTitle");
            mAuthors = savedInstanceState.getString("mAuthors");
            mImgUrl = savedInstanceState.getString("mImgUrl");
            mCategories = savedInstanceState.getString("mCategories");
            populateViews();


        }

        return rootView;
    }

    /**called after scanning returned*/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (intent != null) {
            String contents = intent.getStringExtra(ScanBookActivity.ISBN_TAG);
            if (contents != null) {
                ean.setText(contents);
            }
        }

//        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
//        if (result != null && "EAN_13".equals(result.getFormatName())) {
//            ean.setText(result.getContents());
//        }

    }


    private void restartLoader(){
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(ean.getText().length()==0){
            return null;
        }
        String eanStr= ean.getText().toString();
        if(eanStr.length()==10 && !eanStr.startsWith("978")){
            eanStr="978"+eanStr;
        }
        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(eanStr)),
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
        mAuthors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
        mImgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        mCategories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
        populateViews();

        rootView.findViewById(R.id.save_button).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.delete_button).setVisibility(View.VISIBLE);
    }


    private void populateViews(){

        ((TextView) rootView.findViewById(R.id.bookTitle)).setText(mBookTitle);

        ((TextView) rootView.findViewById(R.id.bookSubTitle)).setText(mBookSubTitle);

        //some books returned with null authors, this if is to handle such cases
        if (mAuthors != null) {
            String[] authorsArr = mAuthors.split(",");
            ((TextView) rootView.findViewById(R.id.authors)).setLines(authorsArr.length);
            ((TextView) rootView.findViewById(R.id.authors)).setText(mAuthors.replace(",", "\n"));
        }
        if(Patterns.WEB_URL.matcher(mImgUrl).matches()){
            ImageView fullBookCover = (ImageView) rootView.findViewById(R.id.bookCover);
            Utility.downloadImageToView(mImgUrl, fullBookCover, getActivity());
            fullBookCover.setVisibility(View.VISIBLE);
        }

        ((TextView) rootView.findViewById(R.id.categories)).setText(mCategories);


    }





    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }

    private void clearFields(){
        if ("".equals(ean.getText().toString())) {
            ((TextView) rootView.findViewById(R.id.bookTitle)).setText("");
            ((TextView) rootView.findViewById(R.id.bookSubTitle)).setText("");
            ((TextView) rootView.findViewById(R.id.authors)).setText("");
            ((TextView) rootView.findViewById(R.id.categories)).setText("");
            rootView.findViewById(R.id.bookCover).setVisibility(View.INVISIBLE);
        }
        rootView.findViewById(R.id.save_button).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.delete_button).setVisibility(View.INVISIBLE);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.setTitle(R.string.scan);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putString("mBookTitle", mBookTitle);
        outState.putString("mBookSubTitle", mBookSubTitle);
        outState.putString("mAuthors", mAuthors);
        outState.putString("mImgUrl", mImgUrl);
        outState.putString("mCategories", mCategories);

        if(ean!=null) {
            outState.putString(EAN_CONTENT, ean.getText().toString());
        }

        super.onSaveInstanceState(outState);
    }

}

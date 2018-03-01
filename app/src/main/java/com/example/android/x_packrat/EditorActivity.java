package com.example.android.x_packrat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.app.TimePickerDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.android.x_packrat.data.BelongingsContract;
import com.example.android.x_packrat.utilities.XPackRatDateUtils;
import com.example.android.x_packrat.utilities.XPackRatImageUtils;

import java.io.FileNotFoundException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * The activity that launches when a user clicks on a recycler view list item to edit it
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.
        LoaderCallbacks<Cursor> {

    // Request code used for requests for a photo from the user's gallery
    private static final int SELECT_PHOTO = 100;

    // Identifier for belonging data loader
    private static final int ID_EDITOR_BELONGING_LOADER = 66;

    // Field where user enters the name of a belonging
    private TouchableEditText mNameEditText;

    // Field where a belonging's most recent usage date is displayed
    private TextView mDateText;

    // Field where a belonging's most recent usage time is displayed
    private TextView mTimeText;

    // Keeps track of new last used date and time values that the user sets for this belonging.
    // They are static because they need to be accessed inside of the static TimePickerFragment and
    // static DatePickerFragment classes.
    private static int sYear;
    private static int sMonth;
    private static int sDay;
    private static int sHour;
    private static int sMinute;

    // The database values for the last used date and time of the current belonging
    private int hourInDatabase;
    private int minuteInDatabase;
    private int yearInDb;
    private int monthInDb;
    private int dayInDb;

    // Field where image for belonging is displayed
    private ImageView mBelongingImage;

    // Stores the Uri for editing an existing belonging or adding a new belonging
    private Uri mBelongingEditUri;

    // Indicates whether or not the user has edited the name or image fields for the belonging
    private boolean mBelongingHasChanged = false;

    // Indicates whether or not the user has updated a belonging's last used date or time
    private boolean mBelongingDatetimeHasChanged = false;

    // Listens for any user touches on a View, implying that they are modifying
    // the view, and so we change the mBelongingHasChanged boolean to true.
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    break;
                case MotionEvent.ACTION_UP:
                    mBelongingHasChanged = true;
                    view.performClick();
                    break;
                default:
                    break;
            }
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Enables up navigation arrow in tool bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Gets references to all views in this activity
        mNameEditText = (TouchableEditText) findViewById(R.id.edit_belonging_name);
        mBelongingImage = (ImageView) findViewById(R.id.editor_belonging_image);
        mDateText = (TextView) findViewById(R.id.editor_date_text);
        mTimeText = (TextView) findViewById(R.id.editor_time_text);
        Button changeImageButton = (Button) findViewById(R.id.change_image_button);
        Button changeDateButton = (Button) findViewById(R.id.change_date_button);
        Button changeTimeButton = (Button) findViewById(R.id.change_time_button);

        Intent intent = getIntent();
        mBelongingEditUri = intent.getData();

        mNameEditText.setOnTouchListener(mTouchListener);

        // Checks whether the user is updating an existing belonging or adding a new belonging
        if (mBelongingEditUri != null) {
            // Updating a belonging

            // Changes the title in the action bar to reflect that the user is updating a belonging
            setTitle(getString(R.string.editor_activity_title_update_belonging));

            // Starts a loader to fetch the data for the existing belonging
            getLoaderManager().initLoader(ID_EDITOR_BELONGING_LOADER, null, this);
        } else {
            // Adding a belonging

            // Changes the title in the action bar to reflect that the user is adding a belonging
            setTitle(getString(R.string.editor_activity_title_add_belonging));

            // Invalidates the options menu, so the "Delete" menu option can be hidden since
            // it would not make sense to delete a belonging that does not exist yet
            invalidateOptionsMenu();
        }

        changeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBelongingHasChanged = true;
                openImageGallery();
            }
        });

        changeDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        changeTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog();
            }
        });
    }

    /**
     * Prompts user to select a photo from their gallery.
     */
    private void openImageGallery() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, SELECT_PHOTO);
    }

    /**
     * Processes the result of the user selecting an image for their belonging.
     * The photo is set to be associated with the belonging that is currently being edited.
     *
     * @param requestCode         The code that we defined for photo requests
     * @param resultCode          Indicates whether or not the request was resolved successfully
     * @param imageReturnedIntent The intent containing the user's selected image as a Uri
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = imageReturnedIntent.getData();
                    try {
                        Bitmap yourSelectedImage = XPackRatImageUtils.decodeUri(
                                this, selectedImage);

                        // Displays the selected image in EditorActivity
                        mBelongingImage.setImageBitmap(yourSelectedImage);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
        }
    }

    /**
     * Called to edit a menu before it is displayed in the action bar
     *
     * @param menu The options menu in which items are placed
     * @return True for the menu to be displayed
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // Hides the "Delete" menu item if the user is adding a new belonging
        if (mBelongingEditUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete_belonging);
            menuItem.setVisible(false);
        }
        return true;
    }

    /**
     * Inflates and sets up the menu for this Activity
     *
     * @param menu The options menu in which items are placed
     * @return True for the menu to be displayed
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_belonging, menu);
        return true;
    }

    /**
     * Callback invoked when a menu item was selected from this Activity's menu
     *
     * @param item The menu item that was selected by the user
     * @return True to indicate that menu click is handled here
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Checks which menu option the user has selected
        switch (item.getItemId()) {
            case R.id.action_save_belonging:
                saveBelonging();
                return true;
            case R.id.action_delete_belonging:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                // If the belonging hasn't changed, continue with navigating up to MainActivity
                if (!mBelongingHasChanged && !mBelongingDatetimeHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, sets up a dialog to warn the user.
                // Creates a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to MainActivity
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * Inserts a new belonging into the database or updates an existing belonging.
     * Called when the user clicks the "done" icon(check mark) in this activity's menu.
     */
    private void saveBelonging() {

        // Stores the name entered into the name input field
        String nameString = mNameEditText.getText().toString().trim();

        // Converts the user's selected image to bytes
        BitmapDrawable drawable = (BitmapDrawable) mBelongingImage.getDrawable();
        Bitmap belongingBitmap = drawable.getBitmap();
        byte[] imageData = XPackRatImageUtils.getBitmapAsByteArray(belongingBitmap);

        // Stores the date entered via the date picker dialog
        String date = mDateText.getText().toString().trim();

        // Stores the time entered via the time picker dialog
        String time = mTimeText.getText().toString().trim();

        // Avoids saving the belonging if the user has left the name field blank,
        // if the user has not selected a date or if the user has not set a time.
        if (TextUtils.isEmpty(nameString)) {
            Toast.makeText(this, getString(R.string.please_enter_name),
                    Toast.LENGTH_SHORT).show();
            return;
        } else if (TextUtils.isEmpty(date)) {
            Toast.makeText(this, getString(R.string.please_enter_date),
                    Toast.LENGTH_SHORT).show();
            return;
        } else if (TextUtils.isEmpty(time)) {
            Toast.makeText(this, getString(R.string.please_enter_time),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Gets a datetime in milliseconds using the user's set last used date and time
        Calendar calendar = Calendar.getInstance();
        calendar.set(sYear, sMonth, sDay,
                sHour, sMinute, 0);
        long dateTime = calendar.getTimeInMillis();

        // Checks if user edited the date or time fields to something different than what is stored
        // in the database
        if (sHour != hourInDatabase || sMinute != minuteInDatabase || sYear != yearInDb
                || sMonth != monthInDb || sDay != dayInDb) {
            mBelongingDatetimeHasChanged = true;
        }

        // The values to insert into the database
        ContentValues values = new ContentValues();
        values.put(BelongingsContract.BelongingEntry.COLUMN_BELONGING_NAME, nameString);
        values.put(BelongingsContract.BelongingEntry.COLUMN_BELONGING_IMAGE, imageData);
        values.put(BelongingsContract.BelongingEntry.COLUMN_LAST_USED_DATE, dateTime);

        // Determines if the user is trying to update an existing belonging or to insert a new one
        if (mBelongingEditUri == null) {
            // Adding a new belonging
            Uri newUri = getContentResolver().insert(
                    BelongingsContract.BelongingEntry.CONTENT_URI, values);

            // Shows a toast message depending on whether or not the insertion was successful
            if (newUri == null) {
                // Unsuccessful
                Toast.makeText(this, getString(R.string.belonging_add_fail),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Successful
                Toast.makeText(this, getString(R.string.belonging_add_succ),
                        Toast.LENGTH_SHORT).show();

                // Inserts an entry into the usage log table for the current set last used date and
                // time.
                List<String> uriSegments = newUri.getPathSegments();
                String newUriId = uriSegments.get(1);
                if (mBelongingDatetimeHasChanged) {
                    mBelongingDatetimeHasChanged = false;
                    insertUsageDate(newUriId, nameString, dateTime);
                }
            }
        } else {
            // Updating an existing belonging
            int numRowsUpdated = getContentResolver().update(mBelongingEditUri, values, null,
                    null);

            // Shows a toast message depending on whether or not the update was successful
            if (numRowsUpdated == 0) {
                // Unsuccessful
                Toast.makeText(this, getString(R.string.belonging_update_fail),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Successful
                Toast.makeText(this, getString(R.string.belonging_update_succ),
                        Toast.LENGTH_SHORT).show();

                // Inserts an entry in the usage log table for the current set last used date and
                // time of this belonging.
                if (mBelongingDatetimeHasChanged) {
                    mBelongingDatetimeHasChanged = false;
                    List<String> uriSegments = mBelongingEditUri.getPathSegments();
                    String belongingId = uriSegments.get(1);
                    insertUsageDate(belongingId, nameString, dateTime);
                }
            }
        }

        // Ends this activity and returns to the MainActivity
        finish();
    }

    /**
     * Performs the deletion of the belonging in the database
     */
    private void deleteBelonging() {

        // Number of rows deleted
        int numRowsDeleted = 0;

        // Only performs the delete if this is an existing belonging
        if (mBelongingEditUri != null) {
            // Calls the ContentResolver to delete the belonging at the given content URI
            numRowsDeleted = getContentResolver().delete(mBelongingEditUri, null,
                    null);
        }

        // Shows a toast message depending on whether or not the delete was successful
        if (numRowsDeleted == 0) {
            // Unsuccessful
            Toast.makeText(this, getString(R.string.editor_delete_belonging_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Successful
            Toast.makeText(this, getString(R.string.editor_delete_belonging_succeeded),
                    Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    /**
     * Shows the user an Alert Dialog that asks them to confirm the deletion of a belonging
     */
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);

        // Sets what the user sees as the option to confirm deletion
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the belonging
                deleteBelonging();
            }
        });

        // Sets what the user sees as the option to cancel deletion
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the belonging.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Creates an alert dialog from the above builder and displays the alert dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Shows the user an Alert Dialog that asks them to confirm discarding any changes made to a
     * belonging
     *
     * @param discardButtonClickListener Listener for the DISCARD button
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);

        // Sets what the user sees as the option to discard changes
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);

        // Sets what the user sees as the option to keep making changes
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the belonging.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Creates an alert dialog from the above builder and displays the alert dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Handles making sure the user is prompted with an alert dialog upon pressing their
     * device's back button while in this activity. This is to make sure that the user
     * wants to discard any changes made to this belonging and wants to exit this activity.
     */
    @Override
    public void onBackPressed() {
        // If the belonging hasn't changed, continue with handling back button press
        if (!mBelongingHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, sets up a dialog to warn the user.
        // Creates a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Shows unsaved changes dialog
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    /**
     * Called when a new Loader needs to be created
     *
     * @param id   The loader ID for which we need to create a loader
     * @param args Any arguments supplied by the caller
     * @return A new loader instance that is ready to start loading
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // The database table columns that we are interested in
        String[] projection = {
                BelongingsContract.BelongingEntry._ID,
                BelongingsContract.BelongingEntry.COLUMN_BELONGING_IMAGE,
                BelongingsContract.BelongingEntry.COLUMN_BELONGING_NAME,
                BelongingsContract.BelongingEntry.COLUMN_LAST_USED_DATE
        };

        // The row of the database table that we are interested in
        String selection = BelongingsContract.BelongingEntry._ID + "=?";
        String[] selectionArgs = new String[]{
                String.valueOf(ContentUris.parseId(mBelongingEditUri))};

        return new CursorLoader(this, BelongingsContract.BelongingEntry.CONTENT_URI,
                projection, selection, selectionArgs,
                null);
    }

    /**
     * Called when a loader has finished loading its data.
     * Displays the loaded data.
     *
     * @param loader The loader that has finished
     * @param data   The data generated by the loader
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        // Checks if there is data to show
        if (data.moveToFirst()) {
            // Fills the name field with the name of the belonging that was clicked
            mNameEditText.setText(data.getString(data.getColumnIndexOrThrow(
                    BelongingsContract.BelongingEntry.COLUMN_BELONGING_NAME)));

            // Fills the image field with the image of the belonging that was clicked
            byte[] imgByte = data.getBlob(data.getColumnIndexOrThrow(
                    BelongingsContract.BelongingEntry.COLUMN_BELONGING_IMAGE));
            Bitmap bitmap = BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length);
            mBelongingImage.setImageBitmap(bitmap);


            // Fetches datetime from cursor
            long dateTimeMilli = data.getLong(data.getColumnIndexOrThrow(
                    BelongingsContract.BelongingEntry.COLUMN_LAST_USED_DATE));

            // Creates calendar using fetched datetime and parses out date and time info.
            // Initializes date and time values for this belonging to those fetched from the
            // database. The values of these date and time vars will change if the user edits the
            // date or time fields of this activity.
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(dateTimeMilli);
            sYear = calendar.get(Calendar.YEAR);
            sMonth = calendar.get(Calendar.MONTH);
            sDay = calendar.get(Calendar.DAY_OF_MONTH);
            sHour = calendar.get(Calendar.HOUR_OF_DAY);
            sMinute = calendar.get(Calendar.MINUTE);

            // Stores date and time values that were fetched from the database for ths belonging.
            // After this initialization, these vars will remain unchanged while this activity is
            // running.
            yearInDb = sYear;
            monthInDb = sMonth;
            dayInDb = sDay;
            hourInDatabase = sHour;
            minuteInDatabase = sMinute;

            Locale locale = XPackRatDateUtils.getUserLocale(this);

            // Sets the date that the belonging was last used to be displayed in a text view
            mDateText.setText(XPackRatDateUtils.formatDate(locale, sYear, sMonth, sDay));

            // Sets the time that the belonging was last used to be displayed in a text view
            mTimeText.setText(XPackRatDateUtils.formatTime(locale, sMinute, sHour));
        }
    }

    /**
     * Called when a previously created loader is being reset, and thus making its data unavailable.
     * Clears all editor fields.
     *
     * @param loader The loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mBelongingImage.setImageBitmap(null);
        mDateText.setText("");
        mTimeText.setText("");
    }

    /**
     * Inner class that listens for when the user sets a time
     */
    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        // The activity that this fragment is attached to
        Activity mActivity;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mActivity = getActivity();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Uses the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        /**
         * Is responsible for displaying the user's selected time
         *
         * @param view      The time picker dialog
         * @param hourOfDay The initial hour set on the dialog
         * @param minute    The initial minute set on the dialog
         */
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

            int hour = hourOfDay % 12;

            Locale locale = XPackRatDateUtils.getUserLocale(mActivity);

            // Displays the user's selected time
            TextView timeTextView = (TextView) getActivity().findViewById(R.id.editor_time_text);
            timeTextView.setText(String.format(locale, "%2d:%02d %s", hour == 0 ? 12 : hour,
                    minute, hourOfDay < 12 ? "am" : "pm"));

            sHour = hourOfDay;

            sMinute = minute;
        }
    }

    /**
     * Displays the dialog that allows the user to pick a time
     */
    public void showTimePickerDialog() {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getFragmentManager(), "timePicker");
    }

    /**
     * Inner class that listens for when the user sets a date
     */
    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        // The activity that this fragment is attached to
        Activity mActivity;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mActivity = getActivity();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        /**
         * Is responsible for displaying the user's selected date
         *
         * @param view  The date picker dialog
         * @param year  The initial year set on the dialog
         * @param month The initial month set on the dialog
         * @param day   The initial day set on the dialog
         */
        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Gets the user's current locale. Uses deprecated code if device is using API < 24.
            Locale locale = XPackRatDateUtils.getUserLocale(mActivity);

            // Displays the user's selected date
            TextView dateTextView = (TextView) getActivity().findViewById(R.id.editor_date_text);
            dateTextView.setText(XPackRatDateUtils.formatDate(locale, year, month, day));

            sYear = year;
            sMonth = month;
            sDay = day;
        }
    }

    /**
     * Displays the dialog that allows the user to pick a date
     */
    public void showDatePickerDialog() {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    /**
     * Inserts an entry into the usage log table for the current belonging
     *
     * @param belongingId The database value for the belonging's id
     * @param nameString  The name of the current belonging
     * @param dateTime    The datetime to insert into the table
     */
    public void insertUsageDate(String belongingId, String nameString, long dateTime) {
        ContentValues dateVal = new ContentValues();
        dateVal.put(BelongingsContract.UsageLogEntry.COLUMN_USAGE_DATE, dateTime);
        dateVal.put(BelongingsContract.UsageLogEntry.COLUMN_BELONGING_ID, belongingId);

        Uri newUsageDateUri = getContentResolver().insert(
                BelongingsContract.UsageLogEntry.CONTENT_URI, dateVal);
    }
}

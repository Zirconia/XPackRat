package com.example.android.x_packrat;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.x_packrat.data.BelongingsContract;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class EditorActivity extends AppCompatActivity implements LoaderManager.
        LoaderCallbacks<Cursor>{

    // Result code used for requests for a photo from the gallery
    private static final int SELECT_PHOTO = 100;

    // Identifier for belonging data loader
    private static final int ID_EDITOR_BELONGING_LOADER = 66;

    // Field to enter the belonging name
    private EditText mNameEditText;

    // Field where image for belonging is entered
    private ImageView mBelongingImage;

    // Stores the Uri for editing an existing belonging or adding a new belonging
    private Uri mBelongingEditUri;

    // Indicates whether or not a field in this activity has been edited by the user
    private boolean mBelongingHasChanged = false;

    // OnTouchListener that listens for any user touches on a View, implying that they are modifying
    // the view, and we change the mBelongingHasChanged boolean to true.
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    //some code....
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
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mNameEditText = (EditText)findViewById(R.id.edit_belonging_name);
        mBelongingImage = (ImageView)findViewById(R.id.editor_belonging_image);
        Button changeImageButton = (Button)findViewById(R.id.change_image_button);

        Intent intent = getIntent();
        mBelongingEditUri = intent.getData();

        mNameEditText.setOnTouchListener(mTouchListener);

        // Changes the title in the action bar to reflect whether the user is updating an
        // existing belonging or adding a new belonging
        if(mBelongingEditUri != null) {
            setTitle(getString(R.string.editor_activity_title_update_belonging));

            // Starts a loader to fetch the data for the existing belonging
            getLoaderManager().initLoader(ID_EDITOR_BELONGING_LOADER, null, this);
        }
        else{
            setTitle(getString(R.string.editor_activity_title_add_belonging));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a belonging that hasn't been created yet.)
            invalidateOptionsMenu();
        }

        changeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBelongingHasChanged = true;
                openImageGallery();
            }
        });
    }

    /**
     * Called to edit a menu before it is displayed
     *
     * @param menu The options menu in which items are placed
     *
     * @return     True for the menu to be displayed
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
     * Prompts user to select a photo from their gallery.
     */
    private void openImageGallery(){
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, SELECT_PHOTO);
    }

    /**
     * Processes the result of the user selecting an image for their belonging.
     * The photo is set to be associated with the belonging that is currently being edited.
     *
     * @param requestCode          The code that we defined for photo requests
     * @param resultCode           Indicates whether or not the request was resolved successfully
     * @param imageReturnedIntent  The intent containing the user's selected image as a Uri
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch(requestCode) {
            case SELECT_PHOTO:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = imageReturnedIntent.getData();
                    try {
                        Bitmap yourSelectedImage = decodeUri(selectedImage);
                        mBelongingImage.setImageBitmap(yourSelectedImage);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
        }
    }

    /**
     * Processes the result of the user selecting an image for their belonging.
     * The photo is set to be associated with the belonging that is currently being edited.
     *
     * @param  selectedImage    The Uri representation of the user's selected image
     * @return                  Bitmap representation of the user's selected image
     */
    private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {

        // Decodes image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage),
                null, o);

        // The new size we want to scale to
        final int REQUIRED_SIZE = 140;

        // Finds the correct scale value. It should be a power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE
                    || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage),
                null, o2);
    }

    /**
     * Inflates and sets up the menu for this Activity
     *
     * @param menu The options menu in which items are placed
     *
     * @return     True for the menu to be displayed
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
     *
     * @return     True to indicate that menu click is handled here
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (item.getItemId()) {
            case R.id.action_save_belonging:
                saveBelonging();
                finish();
                return true;
            case R.id.action_delete_belonging:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                // If the belonging hasn't changed, continue with navigating up to MainActivity
                if (!mBelongingHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
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

        /*if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }


    /**
     * Inserts a new belonging into the database or updates an existing belonging.
     * Called when the user clicks the "done" icon(check mark) in this activity's menu.
     *
     */
    private void saveBelonging(){

        // Stores the name entered into the name input field
        String nameString = mNameEditText.getText().toString().trim();

        // Converts the user's selected image to bytes
        BitmapDrawable drawable = (BitmapDrawable) mBelongingImage.getDrawable();
        Bitmap belongingBitmap = drawable.getBitmap();
        byte[] imageData = getBitmapAsByteArray(belongingBitmap);

        // Avoids saving the belonging if the user has left the name field blank
        if(TextUtils.isEmpty(nameString))
        {
            return;
        }

        // The values to insert into the database
        ContentValues values = new ContentValues();
        values.put(BelongingsContract.BelongingEntry.COLUMN_BELONGING_NAME, nameString);
        values.put(BelongingsContract.BelongingEntry.COLUMN_BELONGING_IMAGE, imageData);

        // Determines if the user is trying to update an existing belonging or to insert a new one
        if (mBelongingEditUri == null) {
            Uri newUri = getContentResolver().insert(
                    BelongingsContract.BelongingEntry.CONTENT_URI, values);

            // Shows a toast message depending on whether or not the insertion was successful
            if (newUri == null) {
                // Unsuccessful
                Toast.makeText(this, getString(R.string.action_settings),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Successful
                Toast.makeText(this, getString(R.string.action_settings),
                        Toast.LENGTH_SHORT).show();
            }
        }
        else {
            int numRowsUpdated = getContentResolver().update(mBelongingEditUri, values, null,
                    null);

            // Shows a toast message depending on whether or not the update was successful
            if (numRowsUpdated == 0) {
                // Unsuccessful
                Toast.makeText(this, getString(R.string.action_settings),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Successful
                Toast.makeText(this, getString(R.string.action_settings),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Performs the deletion of the belonging in the database
     */
    private void deleteBelonging() {
        int numRowsDeleted = 0;

        // Only performs the delete if this is an existing belonging
        if (mBelongingEditUri != null) {
            // Calls the ContentResolver to delete the belonging at the given content URI
            numRowsDeleted = getContentResolver().delete(mBelongingEditUri,null,
                    null);
        }

        // Shows a toast message depending on whether or not the delete was successful
        if (numRowsDeleted == 0) {
            // Unsuccessful
            Toast.makeText(this, getString(R.string.editor_delete_belonging_failed),
                    Toast.LENGTH_SHORT).show();
        }
        else {
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

    @Override
    public void onBackPressed() {
        // If the belonging hasn't changed, continue with handling back button press
        if (!mBelongingHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    /**
     * Coverts a Bitmap to a byte array(byte[]). Used to convert images to a suitable format for
     * storage in the database as a Blob.
     *
     * @param bitmap    The image to convert to a byte array
     *
     * @return          The byte array representation of the supplied Bitmap image
     */
    public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
    }

    /**
     * Called when a new Loader needs to be created
     *
     * @param id       The loader ID for which we need to create a loader
     * @param args     Any arguments supplied by the caller
     * @return         A new loader instance that is ready to start loading
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                BelongingsContract.BelongingEntry._ID,
                BelongingsContract.BelongingEntry.COLUMN_BELONGING_IMAGE,
                BelongingsContract.BelongingEntry.COLUMN_BELONGING_NAME,
                BelongingsContract.BelongingEntry.COLUMN_LAST_USED_DATE
        };

        String selection = BelongingsContract.BelongingEntry._ID + "=?";
        String[] selectionArgs = new String[] {
                String.valueOf(ContentUris.parseId(mBelongingEditUri)) };

        return new CursorLoader(this, BelongingsContract.BelongingEntry.CONTENT_URI,
                projection, selection, selectionArgs,
                null);
    }

    /**
     * Called when a loader has finished loading its data.
     *
     * @param loader The loader that has finished
     * @param data   The data generated by the loader
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if(data.moveToFirst()) {
            // Fills the name field with the name of the belonging that was clicked
            mNameEditText.setText(data.getString(data.getColumnIndexOrThrow(
                    BelongingsContract.BelongingEntry.COLUMN_BELONGING_NAME)));

            // Fills the image field with the image of the belonging that was clicked
            byte[] imgByte = data.getBlob(data.getColumnIndexOrThrow(
                    BelongingsContract.BelongingEntry.COLUMN_BELONGING_IMAGE));
            Bitmap bitmap = BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length);
            mBelongingImage.setImageBitmap(bitmap);
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
    }
}

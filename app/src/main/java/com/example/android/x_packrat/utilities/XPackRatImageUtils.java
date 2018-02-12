package com.example.android.x_packrat.utilities;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;

public class XPackRatImageUtils {
    /**
     * Converts a Uri to a Bitmap image
     *
     * @param  selectedImage    The Uri representation of the user's selected image
     * @param  context          The context from which this method was called
     * @return                  Bitmap representation of the user's selected image
     */
    public static Bitmap decodeUri(Context context, Uri selectedImage)
            throws FileNotFoundException {

        // Decodes image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(context.getContentResolver().openInputStream(selectedImage),
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
        return BitmapFactory.decodeStream(context.getContentResolver().openInputStream(selectedImage),
                null, o2);
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
}

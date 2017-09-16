package KairosBackend;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Environment;
import android.util.Base64;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Daniel on 7/26/2017.
 */

public class KairosUtils {


    //format could be = Bitmap.CompressFormat.PNG

    /**
     *
     * @param image a bitmap image
     * @param format is the format a "jpg"? Use Bitmap.CompressFormat.JPEG, etc...
     * @return
     */
    public static String ConvertImageToBase64(Bitmap image,Bitmap.CompressFormat format) {

        Bitmap toSendImage = image.copy(image.getConfig(), true);
        toSendImage = Bitmap.createScaledBitmap(toSendImage, toSendImage.getWidth()/4, toSendImage.getHeight()/4, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        toSendImage.compress(format, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();

        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }



    /*** Code to Take photo and display in imageView ***/



    //SOURCE: https://stackoverflow.com/questions/6448856/android-camera-intent-how-to-get-full-sized-photo

    /**
     *
     * @param activity, should use "this"
     * @param format if png use "png", not ".png", etc...
     * @return
     * @throws Exception
     */
    public static File createImageFile(Activity activity, String format) throws Exception {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = format.toUpperCase()+"_" + timeStamp + "_";
        File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                "."+format,         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        return image;
    }


    public static Bitmap setPic(ImageView imageView, String photoPath) {
        // Get the dimensions of the View
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        if(targetH == 0 || targetW == 0) {
            System.out.println("TargetH: "+targetH+"   targetW: "+targetW);
            return null;
        }

        if(!new File(photoPath).exists()) {
            System.out.println("File: "+photoPath+" does not exist!");
            return null;
        }

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        int rotation = CheckOrientation(photoPath);
        BitmapFactory.decodeFile(photoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        Bitmap bitmap = BitmapFactory.decodeFile(photoPath);
        bitmap = rotateImage(bitmap, rotation);

        imageView.setImageBitmap(bitmap);

        return bitmap;
    }


    private static int CheckOrientation(String path){
        ExifInterface ei = null;
        try {
            ei = new ExifInterface(path);
        }catch(Exception e){
            e.printStackTrace();
            return 0;
        }
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        Bitmap rotatedBitmap = null;
        switch(orientation) {

            case ExifInterface.ORIENTATION_ROTATE_90:
                return 90;

            case ExifInterface.ORIENTATION_ROTATE_180:
                return 180;

            case ExifInterface.ORIENTATION_ROTATE_270:
                return 270;

            case ExifInterface.ORIENTATION_NORMAL:
                return 0;

            default:
                break;
        }

        return 0;
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

}

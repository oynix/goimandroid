package com.mixotc.imsdklib.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ImageUtils {
    public static final int SCALE_IMAGE_WIDTH = 640;
    public static final int SCALE_IMAGE_HEIGHT = 960;

    public ImageUtils() {
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
        return getRoundedCornerBitmap(bitmap, 6.0F);
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float corner) {
        Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(0x424242);
        canvas.drawRoundRect(rectF, corner, corner, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return newBitmap;
    }

    public static Bitmap getVideoThumbnail(String videoFile, int width, int height, int kind) {
        Bitmap newBitmap = ThumbnailUtils.createVideoThumbnail(videoFile, kind);
        if (newBitmap == null) {
            return null;
        }
        Logger.d("getVideoThumbnail", "video thumb width:" + newBitmap.getWidth());
        Logger.d("getVideoThumbnail", "video thumb height:" + newBitmap.getHeight());
        newBitmap = ThumbnailUtils.extractThumbnail(newBitmap, width, height, 2);
        return newBitmap;
    }

    public static Bitmap saveVideoThumb(File videoFile, String thumbPath, int width, int height, int kind) {
        Bitmap thumbnail = getVideoThumbnail(videoFile.getAbsolutePath(), width, height, kind);
        if (thumbnail == null) {
            Bitmap bm = Bitmap.createBitmap(140, 140, Config.ARGB_4444);
            bm.eraseColor(Color.GRAY);
            return  bm;
        }
        File thumbFile = new File(thumbPath);
        try {
            thumbFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(thumbFile);
            thumbnail.compress(CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return thumbnail;
    }

    public static Bitmap decodeScaleImage(String filePath, int width, int height) {
        Options options = getBitmapOptions(filePath);
        int sampleSize = calculateInSampleSize(options, width, height);
        Logger.d("img", "original wid" + options.outWidth + " original height:" + options.outHeight + " sample:" + sampleSize);
        options.inSampleSize = sampleSize;
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
        int degree = readPictureDegree(filePath);
        Bitmap newBitmap = null;
        if ((bitmap != null) && (degree != 0)) {
            newBitmap = rotatingImageView(degree, bitmap);
            bitmap.recycle();
            bitmap = null;
            return newBitmap;
        }
        return bitmap;
    }

    public static Bitmap decodeScaleImage(Context context, int resourceId, int width, int height) {
        Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), resourceId, options);
        int sampleSize = calculateInSampleSize(options, width, height);
        options.inSampleSize = sampleSize;
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
        return bitmap;
    }

    public static int calculateInSampleSize(Options options, int width, int height) {
        int oHeight = options.outHeight;
        int oWidth = options.outWidth;
        int sampleSize = 1;
        if ((oHeight > height) || (oWidth > width)) {
            int m = Math.round(oHeight / height);
            int n = Math.round(oWidth / width);
            sampleSize = m > n ? m : n;
        }
        return sampleSize;
    }

    public static Bitmap saveAvatar(File imageFile, String avatarPath, int size) {
        Bitmap bitmap = decodeScaleImage(imageFile.getAbsolutePath(), size, size);
        try {
            File thumbFile = new File(avatarPath);
            try {
                thumbFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(thumbFile);
            bitmap.compress(CompressFormat.JPEG, 60, fileOutputStream);
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static Bitmap saveImageThumb(File imageFile, String thumbPath, int width, int height) {
        Bitmap bitmap = decodeScaleImage(imageFile.getAbsolutePath(), width, height);
        try {
            File thumbFile = new File(thumbPath);
            try {
                thumbFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(thumbFile);
            bitmap.compress(CompressFormat.JPEG, 60, fileOutputStream);
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static Bitmap mergeImages(int width, int height, List<Bitmap> bitmaps) {
        Bitmap newBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawColor(Color.LTGRAY);
        Logger.d("img", "merge images to size:" + width + "*" + height + " with images:" + bitmaps.size());
        int column;
        if (bitmaps.size() <= 4) {
            column = 2;
        } else {
            column = 3;
        }
        int j = 0;
        int perImageWidth = (width - 4) / column;
        for (int m = 0; m < column; m++) {
            for (int n = 0; n < column; n++) {
                Bitmap bitmap = (Bitmap) bitmaps.get(j);
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, perImageWidth, perImageWidth, true);
                Bitmap roundedCornerBitmap = getRoundedCornerBitmap(scaledBitmap, 2.0F);
                scaledBitmap.recycle();
                canvas.drawBitmap(roundedCornerBitmap, n * perImageWidth + (n + 2), m * perImageWidth + (m + 2), null);
                roundedCornerBitmap.recycle();
                j++;
                if (j == bitmaps.size()) {
                    return newBitmap;
                }
            }
        }
        return newBitmap;
    }

    public static int readPictureDegree(String filePath) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(filePath);
            int orientation = exifInterface.getAttributeInt("Orientation", 1);
            switch (orientation) {
                case 6:
                    degree = 90;
                    break;
                case 3:
                    degree = 180;
                    break;
                case 8:
                    degree = 270;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    public static Bitmap rotatingImageView(int degree, Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return newBitmap;
    }

    public static Options getBitmapOptions(String filePath) {
        Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        return options;
    }
}

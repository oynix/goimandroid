package com.mixotc.imsdklib.utils;

import android.webkit.MimeTypeMap;

/**
 * Author   : xiaoyu
 * Date     : 2018/5/23 下午9:02
 * Version  : v1.0.0
 * Describe :
 */
public final class FileUtils {

    private FileUtils() {}

    public static String getMIMEType(String filePath) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(filePath);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        if (type == null) {
            type = "application/octet-stream";
        }
        return type;
    }

    public static String getFileExtension(String mime) {
        String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime);
        if (extension == null) {
            extension = "bin";
        }
        return extension;
    }
}

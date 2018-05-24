package com.mixotc.imsdklib.cloud;

import android.os.RemoteException;

import com.mixotc.imsdklib.RemoteConfig;
import com.mixotc.imsdklib.exception.ErrorType;
import com.mixotc.imsdklib.listener.RemoteCallBack;
import com.mixotc.imsdklib.utils.FileUtils;
import com.mixotc.imsdklib.utils.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GOIMCloudFileManager {

    public static final String AVATAR_UPLOAD_FIELD = "uploadimage";
    public static final String FILE_UPLOAD_FIELD = "uploadfile";

    private static GOIMCloudFileManager sInstance = new GOIMCloudFileManager();
    private OkHttpClient mHTTPClient;

    private GOIMCloudFileManager() {
        mHTTPClient = new OkHttpClient();
    }

    public static synchronized GOIMCloudFileManager getInstance() {
        return sInstance;
    }


    public void uploadFile(final String filepath, final RemoteCallBack callBack) {
        final ProgressListener progressListener = new ProgressListener() {
            @Override
            public void update(long bytesFinished, long contentLength, boolean done) {
                callbackOnProgress(callBack, (int) ((100 * bytesFinished) / contentLength), null);
            }
        };

        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder();
        multipartBodyBuilder.setType(MultipartBody.FORM);
        File file = new File(filepath);
        String mimeType = FileUtils.getMIMEType(filepath);
        MediaType mediaType = MediaType.parse(mimeType);
        multipartBodyBuilder.addFormDataPart(FILE_UPLOAD_FIELD, file.getName(), RequestBody.create(mediaType, file));

        RequestBody requestBody = new ProgressRequestBody(multipartBodyBuilder.build(), progressListener);
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(RemoteConfig.FILE_UPLOAD_URL).post(requestBody);

        final Request request = requestBuilder.build();
        mHTTPClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                callbackOnError(callBack, -1, e.getLocalizedMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (callBack != null) {
                    String result = response.body().string();
                    Logger.d("GOIMCloudFileManager", result);
                    if (result.contains("<html>")) {
                        if (result.contains("413 Request Entity Too Large")) {
                            callbackOnError(callBack, ErrorType.ERROR_EXCEPTION_FILE_SIZE_TOO_LARGE, result);
                        } else {
                            callbackOnError(callBack, -1, result);
                        }
                    } else if (result.contains("Error:Not support file")) {
                        callbackOnError(callBack, ErrorType.ERROR_EXCEPTION_FILE_NOT_SUPPORT, result);
                    } else {
                        List<String> list = new ArrayList<>();
                        list.add(result);
                        callbackOnSuccess(callBack, list);
                    }
                }
            }
        });
    }

    public void uploadAvatar(final String filepath, final RemoteCallBack callBack) {
        final ProgressListener progressListener = new ProgressListener() {
            @Override
            public void update(long bytesFinished, long contentLength, boolean done) {
                callbackOnProgress(callBack, (int) ((100 * bytesFinished) / contentLength), null);
            }
        };

        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder();
        multipartBodyBuilder.setType(MultipartBody.FORM);
        File file = new File(filepath);
        String mimeType = FileUtils.getMIMEType(filepath);
        MediaType mediaType = MediaType.parse(mimeType);
        multipartBodyBuilder.addFormDataPart(AVATAR_UPLOAD_FIELD, file.getName(), RequestBody.create(mediaType, file));

        RequestBody requestBody = new ProgressRequestBody(multipartBodyBuilder.build(), progressListener);
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(RemoteConfig.AVATAR_UPLOAD_URL).post(requestBody);

        final Request request = requestBuilder.build();
        mHTTPClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                callbackOnError(callBack, -1, e.getLocalizedMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (callBack != null) {
                    String result = response.body().string();
                    Logger.d("GOIMCloudFileManager", result);
                    if (result.contains("<html>")) {
                        if (result.contains("413 Request Entity Too Large")) {
                            callbackOnError(callBack, ErrorType.ERROR_EXCEPTION_FILE_SIZE_TOO_LARGE, result);
                        } else {
                            callbackOnError(callBack, -1, result);
                        }
                    } else if (result.contains("Error:Not support file")) {
                        callbackOnError(callBack, ErrorType.ERROR_EXCEPTION_FILE_NOT_SUPPORT, result);
                    } else {
                        List<String> list = new ArrayList<>();
                        list.add(result);
                        callbackOnSuccess(callBack, list);
                    }
                }
            }
        });
    }

    public void downloadFile(final String remoteUrl, final String localFile, final RemoteCallBack callBack) {
        final ProgressListener progressListener = new ProgressListener() {
            @Override
            public void update(long bytesFinished, long contentLength, boolean done) {
                callbackOnProgress(callBack, (int) ((100 * bytesFinished) / contentLength), null);
            }
        };
        OkHttpClient httpClient = mHTTPClient.newBuilder().addNetworkInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response originalResponse = chain.proceed(chain.request());
                return originalResponse.newBuilder().body(new ProgressResponseBody(originalResponse.body(), progressListener)).build();
            }
        }).build();

        Request request = new Request.Builder().url(remoteUrl).build();
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                callbackOnError(callBack, -1, e.getLocalizedMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream is = null;
                byte[] buf = new byte[2048];
                FileOutputStream fos = null;

                try {
                    is = response.body().byteStream();
                    File file = new File(localFile);
                    fos = new FileOutputStream(file);
                    int len = 0;
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                    }
                    fos.flush();
                    List<String> list = new ArrayList<>();
                    list.add(file.getAbsolutePath());
                    callbackOnSuccess(callBack, list);
                } catch (Exception e) {
                    callbackOnError(callBack, -1, e.getLocalizedMessage());
                } finally {
                    try {
                        if (is != null)
                            is.close();
                    } catch (IOException e) {
                    }
                    try {
                        if (fos != null)
                            fos.close();
                    } catch (IOException e) {
                    }
                }
            }
        });
    }

    private void callbackOnSuccess(RemoteCallBack callBack, List result) {
        if (callBack != null) {
            try {
                callBack.onSuccess(result);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void callbackOnProgress(RemoteCallBack callBack, int progress, String msg) {
        if (callBack != null) {
            try {
                callBack.onProgress(progress, msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void callbackOnError(RemoteCallBack callBack, int code, String reason) {
        if (callBack != null) {
            try {
                callBack.onError(code, reason);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

}

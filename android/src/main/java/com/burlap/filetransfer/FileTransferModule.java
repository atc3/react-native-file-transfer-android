package com.burlap.filetransfer;

import android.support.annotation.NonNull;
import android.util.Log;
import android.net.Uri;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReadableMap;

import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.File;

public class FileTransferModule extends ReactContextBaseJavaModule {

    private String TAG = "ImageUploadAndroid";
    private ReactApplicationContext reactContext;

    public FileTransferModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        // match up with the IOS name
        return "FileTransfer";
    }

    @ReactMethod
    public void upload(ReadableMap options, final Callback completeCallback) {
        final OkHttpClient client = new OkHttpClient();

        try {
            String fileKey = options.getString("fileKey");

            // file from uri
            String uri = options.getString("uri");
            File file = getFile(uri);

            if (!file.exists()) {
                Log.d(TAG, "FILE NOT FOUND");
                completeCallback.invoke("FILE NOT FOUND", null);
                return;
            }

            String url = options.getString("uploadUrl");
            String mimeType = options.getString("mimeType");
            String fileName = options.getString("fileName");
            ReadableMap headers = options.getMap("headers");
            ReadableMap data = options.getMap("data");

            Headers.Builder headerBuilder = createHeaders(headers);
            RequestBody requestBody = createRequestBody(fileKey, file, fileName, data, mimeType);
            requestBody = getCountingRequestBody(requestBody);

            // ----- create request -----
            Request request = new Request.Builder()
                    .headers(headerBuilder.build())
                    .url(url)
                    .post(requestBody)
                    .build();

            // ----- execute -----
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                completeCallback.invoke(response, null);
                return;
            }

            completeCallback.invoke(null, response.body().string());
        } catch(Exception e) {
            Log.d(TAG, e.toString());
            completeCallback.invoke(e.toString());
        }
    }

    @NonNull
    private File getFile(String uri) {
        Uri file_uri = Uri.parse(uri);
        return new File(file_uri.getPath());
    }

    @NonNull
    private RequestBody createRequestBody(
            String fileKey, File file, String fileName, ReadableMap data, String mimeType
    ) {
        MediaType mediaType = MediaType.parse(mimeType);

        // add file data
        MultipartBuilder bodyBuilder = new MultipartBuilder();
        bodyBuilder.type(MultipartBuilder.FORM)
                .addPart(
                        Headers.of("Content-Disposition",
                                "form-data; name=\"" + fileKey + "\"; " +
                                        "filename=\"" + fileName + "\""
                        ),
                        RequestBody.create(mediaType, file)
                )
                .addPart(
                        Headers.of("Content-Disposition",
                                "form-data; name=\"filename\""
                        ),
                        RequestBody.create(null, fileName)
                );

        // add extra data
        ReadableMapKeySetIterator dataIterator = data.keySetIterator();
        while (dataIterator.hasNextKey()) {
            String key = dataIterator.nextKey();
            String value = data.getString(key);
            ReadableType type = data.getType(key);
            bodyBuilder.addFormDataPart(key, value);
            Log.d(TAG, "key=" + key + ", type=" + type + ", value=" + value);
        }

        return bodyBuilder.build();
    }

    @NonNull
    private RequestBody getCountingRequestBody(RequestBody requestBody) {
        requestBody = new CountingRequestBody(requestBody, new CountingRequestBody.Listener() {
            @Override
            public void onRequestProgress(long bytesWritten, long contentLength) {
                Log.d(TAG, bytesWritten + "/" + contentLength);
                if (contentLength <= 0) {
                    sendProgressJSEvent(0.9);
                } else {
                    sendProgressJSEvent((double) bytesWritten / contentLength);
                }
            }
        });
        return requestBody;
    }

    @NonNull
    private Headers.Builder createHeaders(ReadableMap headers) {
        Headers.Builder headerBuilder = new Headers.Builder();
        ReadableMapKeySetIterator headerIterator = headers.keySetIterator();
        while (headerIterator.hasNextKey()) {
            String key = headerIterator.nextKey();
            String value = headers.getString(key);
            headerBuilder.add(key, value);
        }
        return headerBuilder;
    }

    private void sendProgressJSEvent(double progress) {
        WritableMap map = Arguments.createMap();
        map.putDouble("progress", progress);

        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("upload_progress", map);
    }
}

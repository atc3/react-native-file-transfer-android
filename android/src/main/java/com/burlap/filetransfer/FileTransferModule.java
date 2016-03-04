package com.burlap.filetransfer;

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
import java.io.IOException;

import okio.Buffer;


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

    private void sendProgressJSEvent(double progress) {
        WritableMap map = Arguments.createMap();
        map.putDouble("progress", progress);

        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("upload_progress", map);
    }

    @ReactMethod
    public void upload(ReadableMap options, Callback complete) {
        final OkHttpClient client = new OkHttpClient();
        final Callback completeCallback = complete;

        try {
            String fileKey = options.getString("fileKey");
            String uri = options.getString("uri");
            Uri file_uri = Uri.parse(uri);
            File file = new File(file_uri.getPath());

            if(file == null) {
                Log.d(TAG, "FILE NOT FOUND");
                completeCallback.invoke("FILE NOT FOUND", null);
                return;
            }

            String url = options.getString("uploadUrl");
            String mimeType = options.getString("mimeType");
            String fileName = options.getString("fileName");
            ReadableMap headers = options.getMap("headers");
            ReadableMap data = options.getMap("data");

            MediaType mediaType = MediaType.parse(mimeType);

            // build data
            MultipartBuilder bodyBuilder = new MultipartBuilder();
            bodyBuilder.type(MultipartBuilder.FORM)
                    .addPart(
                            Headers.of("Content-Disposition",
                                    "form-data; name=\"" + fileKey + "\"; filename=\"" + fileName + "\""
                            ),
                            RequestBody.create(mediaType, file)
                    )
                    .addPart(
                            Headers.of("Content-Disposition",
                                    "form-data; name=\"filename\""
                            ),
                            RequestBody.create(null, fileName)
                    );

            // build request body
            ReadableMapKeySetIterator dataIterator = data.keySetIterator();
            while(dataIterator.hasNextKey()) {
                String key = dataIterator.nextKey();
                String value = data.getString(key);
                ReadableType type = data.getType(key);
                bodyBuilder.addFormDataPart(key, value);
                Log.d(TAG, "key=" + key + ", type=" + type + ", value=" + value);
            }
            RequestBody requestBody = new CountingRequestBody(bodyBuilder.build(), new CountingRequestBody.Listener() {
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

            // build header
            Headers.Builder headerBuilder = new Headers.Builder();
            ReadableMapKeySetIterator headerIterator = headers.keySetIterator();
            while(headerIterator.hasNextKey()) {
                String key = headerIterator.nextKey();
                String value = headers.getString(key);
                ReadableType type = headers.getType(key);
                headerBuilder.add(key, value);
            }

            Request request = new Request.Builder()
                    .headers(headerBuilder.build())
                    .url(url)
                    .post(requestBody)
                    .build();

            // this will cause oom
            // Log.d(TAG, "request = " + bodyToString(request));

            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                Log.d(TAG, "Unexpected code" + response);
                completeCallback.invoke(response, null);
                return;
            }

            completeCallback.invoke(null, response.body().string());
        } catch(Exception e) {
            Log.d(TAG, e.toString());
        }
    }

    private static String bodyToString(final Request request){
        try {
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            copy.body().writeTo(buffer);
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "did not work";
        }
    }
}

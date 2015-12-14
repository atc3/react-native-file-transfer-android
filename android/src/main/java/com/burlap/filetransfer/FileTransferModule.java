package com.burlap.filetransfer;

import android.app.DownloadManager;
import android.content.Context;
import android.util.Log;
import android.net.Uri;

import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReadableMap;

import org.json.*;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.util.Map;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.HashMap;


public class FileTransferModule extends ReactContextBaseJavaModule {

  private final OkHttpClient client = new OkHttpClient();

  private static String siteUrl = "http://joinbevy.com";
  private static String apiUrl = "http://api.joinbevy.com";
  private static Integer port = 80;

  private String TAG = "ImageUploadAndroid";

  public FileTransferModule(ReactApplicationContext reactContext) {
    super(reactContext);
  }

  @Override
  public String getName() {
    // match up with the IOS name
    return "FileTransfer";
  }

  @ReactMethod
  public void upload(ReadableMap options, Callback complete) {

    final Callback completeCallback = complete;

    try {

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

        RequestBody requestBody = new MultipartBuilder()
                .type(MultipartBuilder.FORM)
                .addPart(
                        Headers.of("Content-Disposition",
                                "form-data; name=\"file\"; filename=\"" + fileName + "\""
                        ),
                        RequestBody.create(mediaType, file)
                )
                .addPart(
                        Headers.of("Content-Disposition",
                                "form-data; name=\"filename\""
                        ),
                        RequestBody.create(null, fileName)
                )
                .build();

        Request request = new Request.Builder()
                .header("Accept", "application/json")
                .url(url)
                .post(requestBody)
                .build();

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
}

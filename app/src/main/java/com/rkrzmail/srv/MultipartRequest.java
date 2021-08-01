package com.rkrzmail.srv;

import android.content.Context;
import android.util.Log;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;

public class MultipartRequest {

    public Context caller;
    public MultipartBuilder builder;
    private OkHttpClient client;

    public MultipartRequest(Context caller) {
        this.caller = caller;
        this.builder = new MultipartBuilder();
        this.builder.type(MultipartBuilder.FORM);
        this.client = new OkHttpClient();
    }

    public void addString(String name, String value) {
        this.builder.addFormDataPart(name, value);
    }

    public void addImageFile(String name, String filePath, String fileName) {
        this.builder.addFormDataPart(
                name,
                fileName,
                RequestBody.create(MediaType.parse("image/png"), new File(filePath)));
    }

    public void addImageFile(String name, String fileName, byte[] byteData) {
        this.builder.addFormDataPart(
                name,
                fileName,
                RequestBody.create(MediaType.parse("image/png"), byteData));
    }

    public void addTXTFile(String name, String filePath, String fileName) {
        this.builder.addFormDataPart(name, fileName, RequestBody.create(
                MediaType.parse("text/plain"), new File(filePath)));
    }

    public void addZipFile(String name, String filePath, String fileName) {
        this.builder.addFormDataPart(name, fileName, RequestBody.create(
                MediaType.parse("application/zip"), new File(filePath)));
    }

    public String execute(String url) {
        RequestBody requestBody = null;
        Request request = null;
        Response response = null;

        int code = 200;
        String strResponse = null;

        try {
            requestBody = this.builder.build();
            request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();
            response = client.newCall(request).execute();
           /* if (!response.isSuccessful())
                throw new IOException();*/

            code = response.networkResponse().code();
            strResponse = response.body().string();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            requestBody = null;
            request = null;
            response = null;
            builder = null;
            if (client != null)
                client = null;
            System.gc();
        }
        return strResponse;
    }
}

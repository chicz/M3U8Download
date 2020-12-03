package com.example.mediodownload.utils;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.util.concurrent.TimeUnit;

public class OkhttpUtil {

    private static long connectTimeout = 3*60*1000;
    private static long readTimeout = 3*60*1000;
    private static long writeTimeout = 3*60*1000;
    private static boolean retryOnConFail = false;
    private static int maxIdleConnections = 5;
    private static long keepAliveDuration = 3*60*1000;

    public static Request getRequest(String url){
        Request request = null;
        if(url!=null && !"".equals(url)){
            request = new Request.Builder().url(url).build();
        }
        return request;
    }

    public static OkHttpClient generatorClient(){
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
                .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
                .writeTimeout(writeTimeout, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(false)
                .retryOnConnectionFailure(retryOnConFail)
                .connectionPool(new ConnectionPool(maxIdleConnections, keepAliveDuration, TimeUnit.MILLISECONDS))
                .build();
        return httpClient;
    }
}

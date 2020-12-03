package com.example.mediodownload.utils;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileContentUtil {

    public static String createFileName(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS");
        return sdf.format(new Date());
    }

    //获取m3u8文件内容，按行读取
    public static List<String> getM3U8Content(String url){
        List<String> list = new ArrayList<>();
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        String line;
        Response response = null;

        Request request = OkhttpUtil.getRequest(url);
        OkHttpClient okHttpClient = OkhttpUtil.generatorClient();
        try{
            response = okHttpClient.newCall(request).execute();
            inputStream = response.body().byteStream();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
            while ((line=bufferedReader.readLine()) != null){
                if(line.indexOf("#") < 0){
                    list.add(line);
                }
            }
            response.body().close();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(bufferedReader!=null){
                    bufferedReader.close();
                }
                if(inputStream!=null){
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return list;
    }
}

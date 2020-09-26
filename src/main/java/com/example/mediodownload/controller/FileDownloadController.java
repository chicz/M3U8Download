package com.example.mediodownload.controller;

import com.example.mediodownload.model.RespModel;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
public class FileDownloadController {

    @RequestMapping(value = "/chicz/getFileByUrl", method = RequestMethod.POST)
    private RespModel fileDownload(HttpServletResponse response, @RequestParam("file_url") String url){
        RespModel respModel = new RespModel();
        if(url==null || "".equals(url)){
            respModel.setCode("01");
            respModel.setMsg("地址不正确");
            return respModel;
        }

        InputStream inputStream = null;
        OutputStream outputStream = null;
        Response response_temp = null;
        /* 设置文件ContentType类型，这样设置，会自动判断下载文件类型 */
        response.setContentType("multipart/form-data");
        /* 设置文件头：最后一个参数是设置下载文件名 */
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS");
        response.setHeader("Content-Disposition", "attachment;filename="+sdf.format(new Date())+".m3u8");
        //BufferedReader bufferedReader = null;
        int len;
        Request.Builder builder = new Request.Builder().url(url);
        OkHttpClient okHttpClient = DiliDiliDLController.generatorClient();
        try {
            response_temp = okHttpClient.newCall(builder.build()).execute();
            inputStream = response_temp.body().byteStream();
            outputStream = response.getOutputStream();
            byte[] bytes = new byte[1024];
            System.out.println("---------------------------------------------------");
            while ( (len=inputStream.read(bytes))!=-1){
                outputStream.write(bytes,0,len);
            }
            System.out.println("---------------------------------------------------");
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try {
                if(inputStream!=null){
                    inputStream.close();
                }
                if(outputStream!=null){
                    outputStream.flush();
                    outputStream.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return respModel;
    }

}

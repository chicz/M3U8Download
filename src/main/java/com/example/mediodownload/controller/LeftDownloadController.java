package com.example.mediodownload.controller;

import com.example.mediodownload.model.RespModel;
import com.example.mediodownload.utils.OkhttpUtil;
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
@RequestMapping(value = "/chicz")
public class LeftDownloadController {

    @RequestMapping(value = "/getFileByLeft", method = RequestMethod.POST)
    private void fileDownload(HttpServletResponse response, @RequestParam("file_url") String url){
        //RespModel respModel = new RespModel();
        if(url==null || "".equals(url)){
            /*respModel.setCode("01");
            respModel.setMsg("地址不能为空");
            return respModel;*/
            return;
        }

        InputStream inputStream = null;
        OutputStream outputStream = null;
        Response response_temp = null;

        response.reset();
        response.setContentType("application/x-download");
        /* 设置文件头：最后一个参数是设置下载文件名 */
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS");
        response.setHeader("Content-Disposition", "attachment;filename="+sdf.format(new Date()));
        //BufferedReader bufferedReader = null;
        int len;
        Request request = OkhttpUtil.getRequest(url);
        OkHttpClient okHttpClient = OkhttpUtil.generatorClient();
        try {
            response_temp = okHttpClient.newCall(request).execute();
            inputStream = response_temp.body().byteStream();
            outputStream = response.getOutputStream();
            byte[] bytes = new byte[1024];
            System.out.println("---------------------------------------------------");
            while ( (len=inputStream.read(bytes))!=-1){
                outputStream.write(bytes,0,len);
            }
            System.out.println("---------------------------------------------------");
            response_temp.body().close();
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
        //return respModel;
    }
}

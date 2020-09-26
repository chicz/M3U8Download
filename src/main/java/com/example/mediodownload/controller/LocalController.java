package com.example.mediodownload.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.*;


@RestController
public class LocalController {

    @RequestMapping(value = "/chicz/getLocalFile",method = RequestMethod.POST)
    private void getLocalFile(HttpServletResponse httpServletResponse, @RequestParam("local_url")String url){

        File file = new File(url);
        FileInputStream fileInputStream;
        OutputStream outputStream;
        try {
            /* 设置文件ContentType类型，这样设置，会自动判断下载文件类型 */
            httpServletResponse.setContentType("multipart/form-data");
            /* 设置文件头：最后一个参数是设置下载文件名 */
            httpServletResponse.setHeader("Content-Disposition", "attachment;filename="+url.substring(url.lastIndexOf("\\")));
            fileInputStream  = new FileInputStream(file);
            outputStream = httpServletResponse.getOutputStream();
            byte[] bytes = new byte[4096];
            int len;
            System.out.println("---------------------------------------------------");
            while ( (len=fileInputStream.read(bytes))!=-1){
                outputStream.write(bytes,0,len);
                outputStream.flush();
            }
            fileInputStream.close();
            outputStream.close();
            System.out.println("---------------------------------------------------");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

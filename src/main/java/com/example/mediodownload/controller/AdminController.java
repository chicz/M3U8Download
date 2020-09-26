package com.example.mediodownload.controller;

import com.example.mediodownload.model.MediaInfo;
import com.example.mediodownload.model.RespModel;
import okhttp3.*;
import org.apache.tomcat.util.security.MD5Encoder;
import org.springframework.stereotype.Controller;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping(value = "chicz")
public class AdminController {

    private static final int maxRetryCount = 5;
    private static final String video_temp = "D:\\video_menu";
    private static final String video_merge = "D:\\video_merge_menu";
    private static Integer completeCount = new Integer(0);
    private static boolean localComplete = false;

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public String index(){
        return "index";
    }

    @RequestMapping(value = "/allTest", method = RequestMethod.GET)
    @ResponseBody
    public RespModel allTest(){
        /*String str = "/20180919/EZPQV6c5/1000kb/hls/eP9C8Ym1204000.ts";
        String[] strs = str.split("/");
        System.out.println(strs[strs.length-1]);
        System.out.println(strs.length);
        System.out.println(str.lastIndexOf("/"));
        System.out.println(str.substring(str.lastIndexOf("/")<0?0:str.lastIndexOf("/")));
        String nextStr = "/eP9C8Ym1204000.ts";
        System.out.println(nextStr.lastIndexOf("/"));
        System.out.println(nextStr.substring(nextStr.lastIndexOf("/")<0?0:nextStr.lastIndexOf("/")+1));
        String nextStr1 = "eP9C8Ym1204000.ts";
        System.out.println(nextStr1.lastIndexOf("/"));
        System.out.println(nextStr1.substring(nextStr1.lastIndexOf("/")<0?0:nextStr1.lastIndexOf("/")+1));*/
        /*String str = "/eP9C8Ym1204000.ts";
        String str_temp = DigestUtils.md5DigestAsHex(str.getBytes());
        String str1 = "/eP9C8Ym1204001.ts";
        String str1_temp = DigestUtils.md5DigestAsHex(str1.getBytes());
        String str2 = "/eP9C8Ym1204002.ts";
        String str2_temp = DigestUtils.md5DigestAsHex(str2.getBytes());
        String str3 = "/eP9C8Ym1204003.ts";
        String str3_temp = DigestUtils.md5DigestAsHex(str3.getBytes());
        System.out.println("--0-0--: "+str_temp);
        System.out.println("--0-1--: "+Long.valueOf(str_temp.substring(0,10),16));
        int str_1 = (int) (Long.valueOf(str_temp.substring(0,10),16) % 5);
        System.out.println("--0-2--: "+str_1);

        System.out.println("--1-0--: "+str1_temp);
        System.out.println("--1-1--: "+Long.valueOf(str1_temp.substring(0,10),16));
        int str_2 = (int) (Long.valueOf(str1_temp.substring(0,10),16) % 5);
        System.out.println("--1-2--: "+str_2);

        System.out.println("--2-0--: "+str2_temp);
        System.out.println("--2-1--: "+Long.valueOf(str2_temp.substring(0,10),16));
        int str_3 = (int) (Long.valueOf(str2_temp.substring(0,10),16) % 5);
        System.out.println("--2-2--: "+str_3);

        System.out.println("--3-0--: "+str3_temp);
        System.out.println("--3-1--: "+Long.valueOf(str3_temp.substring(0,10),16));
        int str_4 = (int) (Long.valueOf(str3_temp.substring(0,10),16) % 5);
        System.out.println("--3-2--: "+str_4);*/
        completeCount=0;
        String url = "http://acfun.iqiyi-kuyun.com";
        String fileName = "/20180919/EZPQV6c5/1000kb/hls/eP9C8Ym1204010.ts";
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(60000, TimeUnit.MILLISECONDS)
                .readTimeout(60000, TimeUnit.MILLISECONDS)
                .writeTimeout(60000, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(false)
                .connectionPool(new ConnectionPool(5, 60, TimeUnit.SECONDS))
                .build();
        downloadSingle(url,fileName,1,1,httpClient,fileName.substring(fileName.lastIndexOf("/")));
        return new RespModel();
    }

    @RequestMapping(value = "/mergeTs", method = RequestMethod.GET)
    @ResponseBody
    public RespModel handMergeTs(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS");
        mergeTs(sdf.format(new Date())+".mp4");
        return new RespModel();
    }

    @RequestMapping(value = "/deleteFiles", method = RequestMethod.GET)
    @ResponseBody
    public RespModel handDelFiles(){
        deleteFiles();
        return new RespModel();
    }

    @RequestMapping(value = "/getMiddleProgress", method = RequestMethod.POST)
    @ResponseBody
    private String getMiddleProgress(@RequestParam("total")String total){
        int tt;
        try{
            tt = Integer.valueOf(total).intValue();
        }catch (Exception e){
            e.printStackTrace();
            return "100";
        }
        if(completeCount==tt && !localComplete){
            return "99";
        }
        return ""+ completeCount * 100 / tt;
    }

    @RequestMapping(value = "/download", method = RequestMethod.POST)
    @ResponseBody
    public RespModel download(@RequestPart("mediaFile") MultipartFile mediaFile,
                              @RequestParam("mediaUri") String mediaUri) throws IOException {
        completeCount = 0;
        localComplete=false;
        RespModel respModel = new RespModel();
        List<String> partList = new ArrayList<>();
        //合成文件名
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS");
        String lastFileName = sdf.format(new Date())+".mp4";
        if(!mediaFile.isEmpty() && mediaUri!=null && mediaUri!=""){
            Reader reader = null;
            reader = new InputStreamReader(mediaFile.getInputStream(),"utf-8");
            BufferedReader bufferReader = new BufferedReader(reader);
            String line;
            while ((line = bufferReader.readLine()) != null){
                if(line.indexOf("#")<0){
                    partList.add(line);
                }
            }
            System.out.println("listSize: "+partList.size());
            //System.out.println("listSize: "+System.getProperty("user.dir"));//项目根路径
            reader.close();
            //开始下载
            downloadByList(partList, mediaUri, lastFileName);
            //System.out.println(System.getProperty("user.dir")+"\\mediadownload");
        }else {
            respModel.setCode("01");
            respModel.setMsg("地址不正确");
            return respModel;
        }
        //do something
        //System.out.println("get INfo:"+mediaUri);
        respModel.setCode("00");
        respModel.setMsg(""+partList.size());
        respModel.setUrl(video_merge+"\\"+lastFileName);
        return respModel;
    }

    private void downloadByList(List<String> list, String url,String lastFileName){
        System.out.println("----准备下载url: "+url);
        System.out.println("----总下载数: "+list.size()+", 例: "+list.get(0));
        List<OkHttpClient> httpClients = new ArrayList<>();
        for (int i=0;i<5;i++){
            OkHttpClient httpClient = new OkHttpClient.Builder()
                    .connectTimeout(60000, TimeUnit.MILLISECONDS)
                    .readTimeout(60000, TimeUnit.MILLISECONDS)
                    .writeTimeout(60000, TimeUnit.MILLISECONDS)
                    .retryOnConnectionFailure(false)
                    .connectionPool(new ConnectionPool(5, 60, TimeUnit.SECONDS))
                    .build();
            httpClients.add(httpClient);
        }

        for(Iterator<String> iterator = list.iterator();iterator.hasNext(); ){
            String fileName = iterator.next();
            String fileName_md5 = DigestUtils.md5DigestAsHex(fileName.getBytes());
            int i = (int) (Long.valueOf(fileName_md5.substring(0,10),16) % 5);
            downloadSingle(url,fileName,1, list.size(),httpClients.get(i),lastFileName);
        }
    }

    private void downloadSingle(String url, String fileName, int currentCount, int listSize, OkHttpClient httpClient,String lastFileName){

        if(currentCount<=maxRetryCount){

            Request.Builder builder = new Request.Builder().url(url+"/"+fileName);
            httpClient.newCall(builder.build()).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    System.out.println("--------第"+currentCount+"次下载错误--------"+fileName);
                    //System.out.println("--------错误信息："+e.getMessage()+"-,-"+e.toString());
                    e.printStackTrace();
                    downloadSingle(url,fileName,currentCount+1,listSize, httpClient, lastFileName);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if(response.isSuccessful()){
                        InputStream inputStream = null;
                        FileOutputStream fileOutputStream = null;
                        /*
                        * FileOutputStream 里有个append,默认false，即覆盖文件，如果为true，则在文件末尾追加
                        * */
                        inputStream = response.body().byteStream();
                        //String path = System.getProperty("user.dir")+"\\video_menu";
                        String path = "D:"+"\\video_menu";
                        File menu = new File(path);
                        if(!menu.exists()){
                            menu.mkdir();
                        }
                        File file = new File(path,
                                fileName.substring(
                                        fileName.lastIndexOf("/")<0?0:fileName.lastIndexOf("/")+1));

                        try{
                            fileOutputStream = new FileOutputStream(file);
                            byte[] bytes = new byte[1024];
                            int len = 0;
                            //获取下载的文件的大小
                            long fileSize = response.body().contentLength();
                            long sum = 0;
                            //下载百分比
                            //int porSize = 0;
                            //(len = inputStream.read(bytes)) != -1
                            //System.out.print("----[ ");
                            while ((len = inputStream.read(bytes)) != -1){
                                //System.out.print(len+", ");
                                fileOutputStream.write(bytes,0,len);
                                fileOutputStream.flush();//不调用这个的话，视频容易乱码
                                sum += len;
                                //porSize = (int) ((sum * 1.0f / fileSize) * 100);
                            }
                            //System.out.println("]----");
                            //System.out.println("----sum----: "+sum);
                            inputStream.close();
                            fileOutputStream.close();
                            addCompleteCount(listSize,fileName,currentCount,lastFileName);
                        }catch (Exception e){

                            System.out.println("--------第"+currentCount+"次存储失败--------"+fileName);
                            e.printStackTrace();
                            //System.out.println("--------存储失败信息："+e.getMessage()+"-,-"+e.toString());
                            downloadSingle(url,fileName,currentCount+1,listSize,httpClient,lastFileName);
                        }finally {
                            try{
                                if(inputStream != null){
                                    inputStream.close();
                                }
                                if(fileOutputStream != null){
                                    fileOutputStream.flush();
                                    fileOutputStream.close();
                                }
                            }catch (IOException ex){
                                System.out.println("----关闭流失败----");
                                ex.printStackTrace();
                            }
                        }
                        //System.out.println("--------"+fileName+"----下载完成,总共完成个数："+AdminController.completeCount);
                    }else {
                        //System.out.println("--------"+fileName+"----下载失败："+response.toString());
                        System.out.println("--------第"+currentCount+"次下载失败--------"+fileName+": "+response.toString());
                        downloadSingle(url,fileName,currentCount+1,listSize,httpClient,lastFileName);
                    }
                }
            });
        }else{
            System.out.println("--------"+fileName+"----超过最大重试次数");
            addCompleteCount(listSize,fileName,currentCount,lastFileName);
        }

    }

    public synchronized void addCompleteCount(int listSize,String fileName,int completeTime,String lastFileName){
        completeCount++;
        System.out.println("--------"+fileName+"----第"+completeTime+"次下载完成,总共完成个数："+completeCount+"/"+listSize);
        if(completeCount==listSize){
            System.out.println("--------开始合并--------");
            mergeTs(lastFileName);
            System.out.println("--------合并完成--------");
            System.out.println("--------开始删除--------");
            deleteFiles();
            System.out.println("--------删除完成--------");
            localComplete=true;
        }
    }

    //合并下载好的ts文件
    private void mergeTs(String lastFileName){
        try{
            String path = video_temp;
            String merge_path = video_merge;
            File menu = new File(merge_path);
            if(!menu.exists()){
                menu.mkdir();
            }
            File[] files = new File(path).listFiles();

            Date date = new Date();
            //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS");
            File file = new File(merge_path, lastFileName);
            System.out.println(file.getAbsolutePath());
            if(file.exists()){
                file.delete();
            }else {
                file.createNewFile();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            byte[] bytes = new byte[4096];
            for(File f : files){
                FileInputStream fileInputStream = new FileInputStream(f);
                int len;
                while ((len=fileInputStream.read(bytes)) != -1){
                    fileOutputStream.write(bytes, 0, len);
                    fileOutputStream.flush();
                }
                fileInputStream.close();
            }
            fileOutputStream.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //删除ts文件片段
    private void deleteFiles(){
        String path = video_temp;
        File[] files = new File(path).listFiles();
        for(File f : files){
            //f.deleteOnExit();
            f.delete();
        }
    }

}

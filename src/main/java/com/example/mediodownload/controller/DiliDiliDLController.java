package com.example.mediodownload.controller;

import com.example.mediodownload.model.RespModel;
import okhttp3.*;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
public class DiliDiliDLController {

    private static final int maxRetryCountD = 5;
    private static Integer completeCount = new Integer(0);
    private static final String video_temp = "D:\\video_menu";
    private static final String video_merge = "D:\\video_merge_menu";
    private static final String SUCCESS = "success";
    private static final String ERROR = "error";
    private static boolean localComplete = false;

    @RequestMapping(value = "/chicz/getRightProgress", method = RequestMethod.POST)
    private String getRightProgress(@RequestParam("total")String total){
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

    @RequestMapping(value = "/chicz/download_no_file", method = RequestMethod.POST)
    private RespModel downloadWithNoFile(@RequestParam("mediaUri_no_file") String url){
        completeCount=0;
        localComplete=false;
        RespModel respModel = new RespModel();
        if(url==null||"".equals(url)){
            respModel.setCode("01");
            respModel.setMsg("地址不正确");
            return respModel;
        }
        String suffix = getValueFromUrl(url).get(0);
        String m3u8_path = url.substring(0,url.lastIndexOf("index."))+suffix;
        System.out.println(m3u8_path);
        String video_path = m3u8_path.substring(0, m3u8_path.lastIndexOf("index."));
        List<String> list = getValueFromUrl(m3u8_path);
        //合成文件名
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS");
        String lastFileName = sdf.format(new Date())+".mp4";
        downloadVideos(list, video_path, lastFileName);
        respModel.setCode("00");
        respModel.setMsg(""+list.size());
        respModel.setUrl(video_merge+"\\"+lastFileName);
        return respModel;
    }

    public static OkHttpClient generatorClient(){
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(60000, TimeUnit.MILLISECONDS)
                .readTimeout(60000, TimeUnit.MILLISECONDS)
                .writeTimeout(60000, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(false)
                .connectionPool(new ConnectionPool(5, 60, TimeUnit.SECONDS))
                .build();
        return httpClient;
    }

    public static List<String> getValueFromUrl(String url){
        List<String> list = new ArrayList<>();
        InputStream inputStream = null;
        Response response = null;
        BufferedReader bufferedReader = null;
        String line;
        Request.Builder builder = new Request.Builder().url(url);
        OkHttpClient okHttpClient = generatorClient();
        try {
            response = okHttpClient.newCall(builder.build()).execute();
            inputStream = response.body().byteStream();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            System.out.println("---------------------------------------------------");
            while ( (line=bufferedReader.readLine())!=null){
                System.out.println(line);
                if(line.indexOf("#")<0){
                    list.add(line);
                }
            }
            System.out.println("---------------------------------------------------");
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try {
                if(bufferedReader!=null){
                    bufferedReader.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return list;
    }

    private void downloadVideos(List<String> list, String url, String lastFileName){
        completeCount = 0;
        System.out.println("----准备下载url: "+url);
        System.out.println("----总下载数: "+list.size()+", 例: "+list.get(0));
        List<OkHttpClient> httpClients = new ArrayList<>();
        for (int i=0;i<5;i++){
            httpClients.add(generatorClient());
        }

        for(Iterator<String> iterator = list.iterator(); iterator.hasNext(); ){
            String fileName = iterator.next();
            String fileName_md5 = DigestUtils.md5DigestAsHex(fileName.getBytes());
            int i = (int) (Long.valueOf(fileName_md5.substring(0,10),16) % 5);
            downloadByUrl(url,fileName,1, list.size(),httpClients.get(i),lastFileName);
        }
    }

    private void downloadByUrl(String url, String fileName, int currentCount, int listSize,OkHttpClient httpClient,String lastFileName){

        if(currentCount<=maxRetryCountD){
            Request.Builder builder = new Request.Builder().url(url+"/"+fileName);
            httpClient.newCall(builder.build()).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    System.out.println("--------第"+currentCount+"次下载错误--------"+fileName);
                    e.printStackTrace();
                    downloadByUrl(url,fileName,currentCount+1, listSize,httpClient,lastFileName);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if(response.isSuccessful()){
                        InputStream inputStream = null;
                        FileOutputStream fileOutputStream = null;
                        inputStream = response.body().byteStream();
                        String path = video_temp;
                        File menu = new File(path);
                        if(!menu.exists()){
                            menu.mkdirs();
                        }
                        File file = new File(path,
                                fileName.substring(
                                        fileName.lastIndexOf("/")<0?0:fileName.lastIndexOf("/")+1));
                        if(file.exists()){
                            file.delete();
                        }else {
                            file.createNewFile();
                        }

                        try{
                            fileOutputStream = new FileOutputStream(file);
                            byte[] bytes = new byte[1024];
                            int len = 0;
                            while ((len = inputStream.read(bytes)) != -1){
                                fileOutputStream.write(bytes,0,len);
                                fileOutputStream.flush();//不调用这个的话，视频容易乱码
                            }
                            inputStream.close();
                            fileOutputStream.close();
                            addCompleteCount(listSize,fileName,currentCount,lastFileName);
                        }catch (Exception e){
                            System.out.println("--------第"+currentCount+"次存储失败--------"+fileName);
                            e.printStackTrace();
                            //System.out.println("--------存储失败信息："+e.getMessage()+"-,-"+e.toString());
                            downloadByUrl(url,fileName,currentCount+1,listSize,httpClient,lastFileName);
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
                                ex.printStackTrace();
                            }
                        }
                    }else {
                        System.out.println("--------第"+currentCount+"次下载失败--------"+fileName+": "+response.toString());
                        downloadByUrl(url,fileName,currentCount+1,listSize,httpClient,lastFileName);
                    }
                }
            });
        }else{
            System.out.println("--------"+fileName+"----超过最大重试次数");
            addCompleteCount(listSize,fileName,currentCount,lastFileName);
        }
    }

    private synchronized void addCompleteCount(int listSize,String fileName,int completeTime,String lastFileName){
        System.out.println("--------"+fileName+"----第"+completeTime+"次下载完成,总共完成个数："+ ++completeCount +"/"+listSize);
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
        String path = video_temp;
        String merge_path = video_merge;

        File menu = new File(merge_path);
        if(!menu.exists()){
            menu.mkdirs();
        }
        File file = new File(merge_path, lastFileName);
        System.out.println(file.getAbsolutePath());
        if(file.exists()){
            file.delete();
        }else {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        FileOutputStream fileOutputStream = null;
        FileInputStream fileInputStream = null;
        try {
            File[] files = new File(path).listFiles();
            fileOutputStream = new FileOutputStream(file);
            byte[] bytes = new byte[4096];
            for(File f : files){
                fileInputStream = new FileInputStream(f);
                int len;
                while ((len=fileInputStream.read(bytes)) != -1){
                    fileOutputStream.write(bytes, 0, len);
                    fileOutputStream.flush();
                }
                fileInputStream.close();
            }
            //System.out.println("----1----");
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try {
                //System.out.println("----2----");
                if(fileInputStream!=null){
                    //System.out.println("----3----");
                    fileInputStream.close();
                    //System.out.println("----4----");
                }
                //System.out.println("----5----");
                if(fileOutputStream!=null){
                    //System.out.println("----6----");
                    fileOutputStream.flush();
                    fileOutputStream.close();
                    //System.out.println("----7----");
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    //删除ts文件片段
    private void deleteFiles(){
        //System.out.println("----8----");
        String path = video_temp;
        File[] files = new File(path).listFiles();
        for(File f : files){
            //f.deleteOnExit();
            f.delete();
        }
        //System.out.println("----9----");
    }
}

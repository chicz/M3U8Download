package com.example.mediodownload.services;

import com.example.mediodownload.utils.FileContentUtil;
import com.example.mediodownload.utils.OkhttpUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RightDownloadTask implements Runnable{

    //参数初始化
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    //线程池最大容纳线程数
    private static final int maximumPoolSize = CPU_COUNT + 1;

    //m3u8视频片段临时存放
    public static final String video_temp = "D:\\chicz_m3u8\\temp_video_menu";
    public static final String video_merge = "D:\\chicz_m3u8\\merge_video_menu";
    private static final int maxRetryCount = 5;

    private int totalCount;//需要下载的数量
    private int completeCount;//完成下载的数量(包括成功&失败)
    private int successCount;
    private int failCount;
    private boolean isCancel = false;

    /*enum Status{
        DOWNLOADING,SUCCESS,ERROR
    }*/
    //
    private boolean isComplete = false;
    private String url;
    //最终文件名
    private String fileName;

    private OkHttpClient httpClient;

    public RightDownloadTask(String url,String fileName){
        this.url = url;
        this.fileName = fileName;
        //蛮写着
        this.totalCount = 0;
        this.successCount = 0;
        this.failCount = 0;
        this.completeCount = 0;
    }

    public String getUrl() {
        return url;
    }
    public String getFileName() {
        return fileName;
    }

    //获取下载进度
    public String getProgress(){
        String progress;
        if(totalCount<=0){
            return "0";
        }else if(isCancel || isComplete){
            return "100";
        }else if(completeCount==totalCount){
            return "99";
        }
        return "" + completeCount*100 / totalCount;
    }
    //取消下载
    //这个感觉不太好控制，实际效果待考究
    public void cancelDownload(){
        this.isCancel = true;
    }

    @Override
    public void run() {
        if(url==null || "".equals(url) || null==fileName || "".equals(fileName)){
            isComplete = true;
            return;
        }
        /*
        * 一共要获得两个***.m3u8文件
        * 第1个.m3u8文件是第2个.m3u8文件的路径
        * 第2个.m3u8文件里才是视频片段（.ts文件）地址
        * */
        List<String> first_m3u8_list = FileContentUtil.getM3U8Content(url);
        if(first_m3u8_list!=null && first_m3u8_list.size()>0){
            String suffix = first_m3u8_list.get(0);
            //请求协议http或https
            String requestProtocol = url.substring(0,url.indexOf("//")+2);
            //第2个m3u8文件地址
            String path = "";
            if(suffix.indexOf("/") == 0){
                try {
                    path = requestProtocol + new URL(url).getHost() + suffix;
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    isComplete = true;
                    return;
                }
            }else {
                path = url.substring(0, url.lastIndexOf("index.")) + suffix;
            }
            
            List<String> second_m3u8_list = FileContentUtil.getM3U8Content(path);
            //先取一条判断链接的前缀怎么取；
            String list0 = "";
            if(second_m3u8_list!=null && second_m3u8_list.size()>0){
                totalCount = second_m3u8_list.size();
                list0 = second_m3u8_list.get(0);
            }else {
                isComplete = true;
                return;
            }
            String path_prefix = "";
            if(list0.indexOf("/") == 0){
                try {
                    path_prefix = requestProtocol + new URL(url).getHost();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    isComplete = true;
                    return;
                }
            }else {
                path_prefix = path.substring(0, path.lastIndexOf("index."));
            }

            //-----------开始下载
            httpClient = OkhttpUtil.generatorClient();
            //临时ts文件保存位置
            String temp_file_path = video_temp + "/" + fileName;
            File menu = new File(temp_file_path);
            if(!menu.exists()){
                if(!menu.mkdirs()){
                    System.out.println("fileDownload()创建文件夹失败");
                }
            }
            //线程池
            System.out.println("maximumPoolSize="+maximumPoolSize);
            ExecutorService fixedThreadPool = Executors.newFixedThreadPool(maximumPoolSize);

            for(String tsName : second_m3u8_list){
                if(!isCancel){
                    Request request = OkhttpUtil.getRequest(path_prefix+tsName);
                    fixedThreadPool.execute(new Runnable() {
                        @Override
                        public void run() {
                            if(!isCancel){
                                downVideo(request,tsName,1,second_m3u8_list);
                            }
                        }
                    });
                }
            }
        }else {
            isComplete = true;
        }
    }

    //listOrder用于最后合并时对照，有些超过最大重试次数都没下载的
    private void downVideo(Request request, String tsName, int currentCount, List<String> listOrder){
        if(currentCount<=maxRetryCount){
            boolean isSuccess = fileDownload(request,tsName);
            if(isSuccess){
                successCount++;
                addCompleteCount(currentCount,tsName,listOrder);
            }else {
                System.out.println("--------第"+currentCount+"次下载失败--------"+tsName+": "+fileName);
                if(isCancel){
                    //downVideo(request,tsName,maxRetryCount+1,listOrder);
                }else {
                    downVideo(request,tsName,currentCount+1,listOrder);
                }

            }
        }else {
            System.out.println("--------"+tsName+"----超过最大重试次数: "+fileName);
            failCount++;
            addCompleteCount(currentCount,tsName,listOrder);
        }
    }

    private boolean fileDownload(Request request, String tsName){
        if(request==null || httpClient==null){
            return false;
        }
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        String temp_save_path = video_temp+"/"+fileName;

        File file = null;
        file = new File(temp_save_path+"/"+tsName.substring(tsName.lastIndexOf("/")+1));
        int len;
        byte[] bytes = new byte[1024];
        Response response = null;
        try {
            if(file.exists()){
                file.delete();
            }else {
                file.createNewFile();
            }
            fileOutputStream = new FileOutputStream(file);
            response = httpClient.newCall(request).execute();
            inputStream = response.body().byteStream();
            while ( (len=inputStream.read(bytes)) != -1){
                if(isCancel){
                    return false;
                }
                fileOutputStream.write(bytes,0, len);
                fileOutputStream.flush();
            }
            response.body().close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }finally {
            try{
                if(inputStream!=null){
                    inputStream.close();
                }
                if(fileOutputStream!=null){
                    fileOutputStream.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private synchronized void addCompleteCount(int completeTime,String tsName,List<String> listOrder){
        completeCount++;
        System.out.println("--------"+tsName+"----第"+completeTime+"次下载完成,总共完成个数："+ completeCount +"/"+listOrder.size());
        System.out.println("--------下载成功个数="+successCount+",失败个数="+failCount+","+fileName);
        if(completeCount==listOrder.size()){
            if(!isCancel){
                System.out.println("--------开始合并--------");
                if(!mergeTs(listOrder)){
                    System.out.println("合并文件失败");
                }
                System.out.println("--------合并完成--------");
            }
            System.out.println("--------开始删除--------");
            deleteFiles(1);
            System.out.println("--------删除完成--------");
            isComplete = true;
        }
    }

    public boolean mergeTs(List<String> listOrder){
        String merge_path = video_merge;
        File menu = new File(merge_path);
        if(!menu.exists()){
            if(!menu.mkdirs()){
                return false;
            }
        }
        File file = new File(merge_path, fileName+".mp4");
        if(file.exists()){
            file.delete();
        }else {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        FileOutputStream fileOutputStream = null;
        FileInputStream fileInputStream = null;
        try {
            File[] files = new File[listOrder.size()];
            for(int i=0;i<listOrder.size();i++){
                String tsName = listOrder.get(i);
                tsName = tsName.substring(tsName.lastIndexOf("/")+1);
                files[i] = new File(video_temp+"/"+fileName+"/"+tsName);
            }

            fileOutputStream = new FileOutputStream(file);
            byte[] bytes = new byte[4096];
            for(File f : files){
                if(!f.exists()){
                    continue;
                }
                //System.out.println("----合并fileName: "+f.getName());
                fileInputStream = new FileInputStream(f);
                int len;
                while ((len=fileInputStream.read(bytes)) != -1){
                    if(!isCancel){
                        fileOutputStream.write(bytes, 0, len);
                    }
                }
                fileInputStream.close();
            }
        }catch (IOException e){
            e.printStackTrace();
            return false;
        }finally {
            try {
                if(fileInputStream!=null){
                    fileInputStream.close();
                }
                if(fileOutputStream!=null){
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return true;
    }

    //只删除临时的ts文件
    public void deleteFiles(int currentCount){
        if(currentCount<=maxRetryCount){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    System.out.println(fileName+",执行第"+currentCount+"次删除");
                    String fileMenu = video_temp+"/"+fileName;
                    File menu = new File(fileMenu);
                    if(menu.exists()){
                        File[] files = menu.listFiles();
                        for(File f : files){
                            f.delete();
                        }
                        //删除文件夹
                        if(!menu.delete()){
                            try {
                                Thread.sleep(10*1000);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            deleteFiles(currentCount+1);
                        }else {
                            System.out.println("删除成功");
                        }
                    }else {
                        System.out.println("文件不存在，或者已经删除");
                    }
                }
            }).start();
        }
    }
}

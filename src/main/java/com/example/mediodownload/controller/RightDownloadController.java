package com.example.mediodownload.controller;

import com.example.mediodownload.model.RespModel;
import com.example.mediodownload.services.RightDownloadTask;
import com.example.mediodownload.utils.FileContentUtil;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/chicz")
public class RightDownloadController {

    Map<String, RightDownloadTask> tasks = new HashMap<>();

    @RequestMapping(value="/getTaskList", method = RequestMethod.GET)
    private Map<String,String> getTaskList(Model model){
        Map<String,String> map = new HashMap<>();
        for(String fileName : tasks.keySet()){
            map.put(fileName,tasks.get(fileName).getUrl());
        }
        return map;
    }

    @RequestMapping(value = "/getRightProgress", method = RequestMethod.POST)
    private String getRightProgress(@RequestParam("file_name") String fileName){
        RightDownloadTask task = tasks.get(fileName);
        return task==null?"100":task.getProgress();
    }

    @RequestMapping(value = "/getFileByRight", method = RequestMethod.POST)
    private RespModel getFileByRight(@RequestParam("video_url") String url){

        RespModel respModel = new RespModel();
        if(url==null||"".equals(url)){
            respModel.setCode("01");
            respModel.setMsg("地址不正确");
            return respModel;
        }
        String fileName = FileContentUtil.createFileName();
        try{
            RightDownloadTask task = new RightDownloadTask(url,fileName);
            tasks.put(fileName,task);
            new Thread(task).start();
        }catch (Exception e){
            e.printStackTrace();
            respModel.setCode("01");
            respModel.setMsg("下载错误");
            return respModel;
        }
        respModel.setMsg(fileName);
        respModel.setUrl(RightDownloadTask.video_merge+"/"+fileName+".mp4");
        return respModel;
    }

    @RequestMapping(value = "/cancelDown", method = RequestMethod.POST)
    private RespModel cancelDown(@RequestParam("file_name")String fileName){
        RespModel resp = new RespModel();
        resp.setCode("01");
        if(fileName!=null&&!"".equals(fileName)){
            RightDownloadTask task = tasks.get(fileName);
            if(task!=null){
                task.cancelDownload();
                task.deleteFiles(1);
                tasks.remove(fileName);
                resp.setCode("00");
            }
        }
        return resp;
    }

}

package com.example.mediodownload.controller;

import com.example.mediodownload.entity.VideoAddress;
import com.example.mediodownload.model.RespModel;
import com.example.mediodownload.repository.VideoAddrRepository;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    VideoAddrRepository repository;

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public String index(){
        return "index";
    }

    @RequestMapping(value = "/allTest", method = RequestMethod.GET)
    @ResponseBody
    public RespModel allTest(){

        return new RespModel();
    }

    @RequestMapping(value = "/getVideoAddr", method = RequestMethod.GET)
    @ResponseBody
    public List<VideoAddress> getAll(){
        List<VideoAddress> list = repository.getAllBy();
        if(list==null){
            list = new ArrayList<>();
        }
        return list;
    }
}

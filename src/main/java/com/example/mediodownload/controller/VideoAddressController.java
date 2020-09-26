package com.example.mediodownload.controller;

import com.example.mediodownload.entity.VideoAddress;
import com.example.mediodownload.repository.VideoAddrRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/app")
public class VideoAddressController {

    @Autowired
    VideoAddrRepository repository;

    @RequestMapping(value = "/getAll", method = RequestMethod.GET)
    public List<VideoAddress> getAll(){
        List<VideoAddress> list = repository.getAllBy();
        if(list==null){
            list = new ArrayList<>();
        }
        return list;
    }

}

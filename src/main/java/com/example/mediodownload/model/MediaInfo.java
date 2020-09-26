package com.example.mediodownload.model;

import org.springframework.web.multipart.MultipartFile;

public class MediaInfo {

    private String mediaUri;
    private MultipartFile mediaFile;

    public String getMediaUri() {
        return mediaUri;
    }

    public void setMediaUri(String mediaUri) {
        this.mediaUri = mediaUri;
    }

    public MultipartFile getMediaFile() {
        return mediaFile;
    }

    public void setMediaFile(MultipartFile mediaFile) {
        this.mediaFile = mediaFile;
    }
}

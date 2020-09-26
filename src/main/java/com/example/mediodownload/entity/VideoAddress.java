package com.example.mediodownload.entity;

import javax.persistence.*;

@Entity
@Table(name = "video_addr")
public class VideoAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    @Column(name = "name", nullable = false)
    String fileName;
    @Column(name = "episode")
    String fileEpisode;
    @Column(name = "path", nullable = false)
    String filePath;
    @Column(name = "type")
    String fileType;
    @Column(name = "pic")
    String filePic;
    @Column(name = "size")
    Long fileSize;
    @Column(name = "duration")
    Integer fileDuration;
    @Column(name = "describe")
    String fileDescribe;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileEpisode() {
        return fileEpisode;
    }

    public void setFileEpisode(String fileEpisode) {
        this.fileEpisode = fileEpisode;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePic() {
        return filePic;
    }

    public void setFilePic(String filePic) {
        this.filePic = filePic;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Integer getFileDuration() {
        return fileDuration;
    }

    public void setFileDuration(Integer fileDuration) {
        this.fileDuration = fileDuration;
    }

    public String getFileDescribe() {
        return fileDescribe;
    }

    public void setFileDescribe(String fileDescribe) {
        this.fileDescribe = fileDescribe;
    }
}

package com.example.mediodownload.repository;

import com.example.mediodownload.entity.VideoAddress;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoAddrRepository extends CrudRepository<VideoAddress,Integer> {
    List<VideoAddress> getAllBy();
    VideoAddress getById(Integer id);
    List<VideoAddress> getByFileType(String fileType);
    List<VideoAddress> getByFileName(String fileName);
}

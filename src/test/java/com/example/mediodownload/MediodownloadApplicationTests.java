package com.example.mediodownload;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

@SpringBootTest
class MediodownloadApplicationTests {

    private static final String video_temp = "D:\\video_menu";

    @Test
    void contextLoads() throws IOException {
        File first = new File(video_temp+"/first.txt");
        File second = new File(video_temp+"/second.txt");
        if(!second.exists()){
            try {
                second.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        BufferedOutputStream outputStream1 = null;
        outputStream1.flush();
        InputStream inputStream = new FileInputStream(first);
        byte[] bytes = new byte[3];
        FileOutputStream outputStream = new FileOutputStream(second);
        int len;
        while ( (len=inputStream.read(bytes)) != -1 ){
            System.out.println("--------: "+bytes.length);
            for(int i=0;i<bytes.length;i++){
                System.out.println("====: "+bytes[i]);
            }
            System.out.println("-------------------------len:"+len);
            outputStream.write(bytes,0,len);
            outputStream.flush();
        }
        inputStream.close();
        outputStream.close();

        Map map = new HashMap();
        map.put("123","123");
        map.size();
        List<String> list = new ArrayList<>();
        list.add("123");
        Hashtable hashtable = new Hashtable();
        
    }

}

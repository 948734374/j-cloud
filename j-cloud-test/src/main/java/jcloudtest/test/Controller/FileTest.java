package jcloudtest.test.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
public class FileTest {
    

    @GetMapping("/zipTest")
    public void ZipFile(HttpServletResponse response){

        String file1 = "/Users/zhangyichi/Documents/学习/butte-java-note-master.zip";
        String file2 = "/Users/zhangyichi/Documents/学习/butte-java-note-master1.zip";


        try {
            response.reset();

            response.setContentType("application/octet-stream; charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode("aaa.zip", "UTF-8"));
            FileInputStream fileInputStream1 = new FileInputStream(file1);
            FileInputStream fileInputStream2 = new FileInputStream(file2);

            ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream());

            zipOutputStream.putNextEntry(new ZipEntry("ceshi1"));

            int buf_size = 1024;
            byte[] buffer = new byte[buf_size];
            int len = 0;
            while(-1 != (len = fileInputStream1.read(buffer,0,buf_size))){
                zipOutputStream.write(buffer,0,len);
            }
            zipOutputStream.closeEntry();

            fileInputStream1.close();

            zipOutputStream.flush();

            zipOutputStream.putNextEntry(new ZipEntry("ceshi2"));

            buffer = new byte[buf_size];
            len = 0;

            while(-1 != (len = fileInputStream2.read(buffer,0,buf_size))){
                zipOutputStream.write(buffer,0,len);
            }
            zipOutputStream.closeEntry();
            fileInputStream2.close();

            zipOutputStream.close();
//            response.addHeader("Content-Length" , "" + );

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}

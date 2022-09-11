package jcloudtest.test.utils;

import jcloudtest.test.entity.HttpResult;
import lombok.val;
import org.apache.http.HttpStatus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class UrlUtils {

    public static HttpResult doPostWithCode(String urlStr ,String paramStr ){
        val httpResult = new HttpResult();
        try {
            URL url = new URL(urlStr);
            String result = "";
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept","application/json , text/javascript ,*/*; q=0.01");
            connection.setRequestProperty("Connection","Keep-Alive");
            connection.setRequestProperty("Content-Type","application/json; charset = UTF-8");
            byte[] paramStrArr = paramStr.getBytes("UTF-8");
            connection.setDoOutput(true);
            connection.setUseCaches(false);

            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(paramStrArr);
            outputStream.flush();
            outputStream.close();
            connection.connect();

            httpResult.setCode(connection.getResponseCode());

            if (HttpStatus.SC_OK == connection.getResponseCode()){

                InputStream intStream = connection.getInputStream();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                byte[] buffer = new byte[1024];

                int len;

                while (-1!=(len=intStream.read(buffer))){

                    byteArrayOutputStream.write(buffer,0,len);
                    byteArrayOutputStream.flush();

                }

                result = byteArrayOutputStream.toString("UTF-8");
                byteArrayOutputStream.close();

                httpResult.setResult(result);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return httpResult;
    }

}

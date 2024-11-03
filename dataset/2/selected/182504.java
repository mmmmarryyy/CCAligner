package com.openthinks.io;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class WebPageReader {

    public String readWebPage(String pageUrl) throws MalformedURLException {
        String currentLine = "";
        String content = "";
        InputStream urlStream;
        try {
            URL url = new URL(pageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            urlStream = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlStream, "UTF-8"));
            while ((currentLine = reader.readLine()) != null) {
                content += currentLine + " ";
            }
            return content;
        } catch (java.net.MalformedURLException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

package com.jalyoo.recon.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Spider {

    private String targetUrl;

    public Spider(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public StringBuffer downLoadPages() {
        URL url = null;
        HttpURLConnection httpConn = null;
        InputStream in = null;
        try {
            url = new URL(this.targetUrl);
            httpConn = (HttpURLConnection) url.openConnection();
            HttpURLConnection.setFollowRedirects(true);
            httpConn.setRequestMethod("GET");
            in = httpConn.getInputStream();
            InputStreamReader reader = new InputStreamReader(in, "UTF8");
            StringBuffer buffer = new StringBuffer();
            try {
                Reader inr = new BufferedReader(reader);
                int ch;
                while ((ch = inr.read()) > -1) {
                    buffer.append((char) ch);
                }
                inr.close();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            return buffer;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                httpConn.disconnect();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }
}

package com.baldwin.www.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * URLҳ�����ݶ�ȡ��
 */
public class URLReader {

    public URLReader() {
    }

    public static String callURL(String strURL) {
        try {
            URL url = new URL(strURL);
            BufferedReader receiver = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuffer msg = new StringBuffer();
            char[] data = new char[512];
            int n = 0;
            while ((n = receiver.read(data, 0, 512)) != -1) {
                msg.append(data, 0, n);
            }
            return msg.toString().trim();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static void main(String[] args) {
        String strContent = URLReader.callURL("http://www.sohu.com");
    }
}

package klobs;

import java.io.BufferedReader;
import java.io.*;
import java.io.InputStreamReader;
import java.io.BufferedWriter;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.*;

public class KReadHTTPFile {

    static String kuser;

    static String kpass;

    static class MyAuthenticator extends Authenticator {

        public PasswordAuthentication getPasswordAuthentication() {
            return (new PasswordAuthentication(kuser, kpass.toCharArray()));
        }
    }

    public static String syncronize2URL(String URL, String sessionFileName, String userDataFileName, String userName, String passWd) {
        kuser = userName;
        kpass = passWd;
        Authenticator.setDefault(new MyAuthenticator());
        try {
            URL url = new URL(URL + "?user=" + kuser + "&pw=" + MD5Sum.md5Sum(kpass));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "text/xml; charset=\"utf-8\"");
            conn.setDoOutput(true);
            try {
                DataInputStream fs = new DataInputStream(new FileInputStream(sessionFileName));
                BufferedOutputStream out = new BufferedOutputStream(conn.getOutputStream());
                chain(fs, out);
                out.close();
            } catch (java.io.IOException e) {
            }
            InputStream fis = conn.getInputStream();
            String retVal = "";
            BufferedWriter fos = null;
            boolean validData = false;
            try {
                String line;
                BufferedReader in = new BufferedReader(new InputStreamReader(fis));
                while ((line = in.readLine()) != null) {
                    retVal += line;
                    if (line.contains("<!-- validdata -->")) {
                        validData = true;
                    }
                }
                if (validData) {
                    fos = new BufferedWriter(new FileWriter(userDataFileName));
                    fos.write(retVal, 0, retVal.length());
                } else {
                    return retVal;
                }
            } catch (IOException e) {
                return KlobsApp.lang.getProperty("URLSaveText", "Couldn't save file to disk");
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return (conn.getResponseMessage());
            }
            conn.disconnect();
        } catch (java.net.MalformedURLException e) {
            return KlobsApp.lang.getProperty("URLErrorText", "Couldn't connect to Server- is the URL correct?");
        } catch (java.io.IOException e) {
            return KlobsApp.lang.getProperty("URLDownloadErrorText", "Couldn't upload data- please check login data (case sensitive)");
        }
        return "";
    }

    public static void chain(InputStream input, OutputStream out) throws IOException {
        byte[] buf = new byte[10000];
        int len = 0;
        while ((len = input.read(buf, 0, 1000)) > 0) {
            out.write(buf, 0, len);
        }
    }
}

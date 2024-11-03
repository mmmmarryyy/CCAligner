package com.haojii.notifier.easytv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import com.haojii.notifier.ObservableTask;

public class EasytvParserTask extends ObservableTask {

    private EasytvEntity easytvEntity;

    public EasytvParserTask(EasytvEntity easytvEntity) {
        this.easytvEntity = easytvEntity;
    }

    private void checkForUpdate() {
        try {
            URI uri = new URI(easytvEntity.url);
            HttpGet httpget = new HttpGet(uri);
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                entity = new BufferedHttpEntity(entity);
                InputStream instream = entity.getContent();
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
                    String line = reader.readLine();
                    boolean startSearching = false;
                    Pattern p = Pattern.compile("href\\s*=\\s*\"(.+?)\"\\s*>(.+?)</\\s*a\\s*>", Pattern.UNICODE_CASE);
                    Pattern p2 = Pattern.compile("(asf|avi|rm|rmvb|mp3|mp4|avi|wma|wmp|wmv|mov+3gp|mpg|mpeg|rar|zip){1}");
                    while (line != null) {
                        if (line.contains("<div id=\"download-list\">")) {
                            startSearching = true;
                        }
                        if (startSearching) {
                            if (line.contains("</div>")) startSearching = false;
                            Matcher m = p.matcher(line);
                            if (m.find() && p2.matcher(line).find()) {
                                String value = m.group(1);
                                String key = m.group(2);
                                EasytvItem item = new EasytvItem(key, value);
                                if (!easytvEntity.items.contains(item)) {
                                    easytvEntity.newItems.add(item);
                                }
                            }
                        }
                        line = reader.readLine();
                    }
                } catch (IOException ex) {
                    throw ex;
                } catch (RuntimeException ex) {
                    httpget.abort();
                    throw ex;
                } finally {
                    instream.close();
                }
                httpclient.getConnectionManager().shutdown();
                if (easytvEntity.newItems.size() > 0) {
                    this.setChanged();
                    this.notifyObservers(easytvEntity);
                }
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void doTask() {
        checkForUpdate();
    }
}

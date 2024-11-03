package util;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JProgressBar;

/**
 *
 * @author reza
 */
public class LoadPictureThread implements Runnable {

    Thread thisThread;

    ArrayList extExamples, components;

    JList list;

    JButton button;

    JFrame THIS;

    String URLx;

    JProgressBar PBar;

    boolean stopped;

    /** Creates a new instance of LoadPictureThread */
    public LoadPictureThread(ArrayList extExamples, JList list, JButton button, JFrame THIS, String URLx, JProgressBar PBar, ArrayList components) {
        this.THIS = THIS;
        this.list = list;
        this.extExamples = extExamples;
        this.button = button;
        this.URLx = URLx;
        this.PBar = PBar;
        this.components = components;
    }

    public void run() {
        String s = "";
        URL url = null;
        try {
            url = new URL(URLx);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible)");
            connection.setRequestProperty("Accept-Language", "en-us");
            connection.setRequestProperty("Accept", "*/*");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Cache-Control", "no-cache");
            connection.setDoInput(true);
            connection.setDoOutput(false);
            connection.connect();
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String str;
            while ((str = in.readLine()) != null) {
                s = s + str;
            }
            in.close();
            Pattern pattern = Pattern.compile("src=(.+?)width=", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher matcher = pattern.matcher(s);
            ArrayList urllist = new ArrayList();
            while (matcher.find()) {
                String stemp = matcher.group(1);
                if (stemp.contains(":")) {
                    urllist.add("http:" + stemp.substring(stemp.lastIndexOf(':') + 1));
                }
            }
            PBar.setMinimum(0);
            PBar.setMaximum(urllist.size());
            if (urllist.size() > 1) for (int j = 1; j < urllist.size(); j++) {
                if (stopped) return;
                URL url2 = new URL((String) urllist.get(j));
                java.awt.Image image = java.awt.Toolkit.getDefaultToolkit().createImage(url2);
                MediaTracker mt = new MediaTracker(THIS);
                mt.addImage(image, 1);
                if (stopped) {
                    mt = null;
                    return;
                }
                if (mt.waitForID(1, 30000)) {
                    if ((mt.getErrorsAny() != null) && (mt.getErrorsAny().length > 0)) for (int i = 0; i < mt.getErrorsAny().length; i++) {
                        System.out.println(mt.getErrorsAny()[i]);
                    } else {
                        int width = image.getWidth(null);
                        if (width > 290) width = 290;
                        Image img = image.getScaledInstance(width, -1, Image.SCALE_FAST);
                        mt.addImage(img, 2);
                        image.flush();
                        mt.waitForID(2);
                        if (stopped) {
                            mt = null;
                            return;
                        }
                        extExamples.add(new ImageIcon(img));
                        ((DefaultListModel) list.getModel()).addElement("xx");
                    }
                }
                PBar.setValue(PBar.getValue() + 1);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            button.setEnabled(true);
            this.stop();
        }
        button.setEnabled(true);
    }

    public void start() {
        thisThread = new Thread(this);
        thisThread.start();
    }

    public void stop() {
        stopped = true;
        thisThread = null;
    }
}

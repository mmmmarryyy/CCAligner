package edu.upmc.opi.caBIG.caTIES.map.java2d;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.MemoryImageSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.JFrame;
import edu.upmc.opi.caBIG.caTIES.common.CaTIES_Utils;

public class CaTIES_TopologicViewer extends Canvas {

    private static final long serialVersionUID = -5172611488507134681L;

    public static final String testUrl = "http://www.google.com/fusiontables/embedviz?viz=MAP&q=select+col4%2C+col5%2C+col6%2C+col7%2C+col11%2C+col8%2C+col9%2C+col10%2C+col13+from+1159987+&h=false&lat=46.55886030311719&lng=-95.80078125&z=4&t=2&l=col13";

    private Image image;

    public static void main(String[] args) {
        Canvas canvas = new CaTIES_TopologicViewer();
        JFrame frame = new JFrame("CaTIES_TopologicViewer");
        frame.add(canvas);
        frame.setSize(300, 200);
        CaTIES_Utils.centerComponent(frame);
        frame.setVisible(true);
    }

    public CaTIES_TopologicViewer() {
        int height = 512;
        int width = 512;
        final byte[] pixels = pullMapBytes(testUrl);
        ColorModel colorModel = generateColorModel();
        this.image = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(width, height, colorModel, pixels, 0, pixels.length));
    }

    public void draw(Graphics g, int x, int y) {
        g.drawImage(image, x, y, null);
    }

    private static ColorModel generateColorModel() {
        byte[] r = new byte[16];
        byte[] g = new byte[16];
        byte[] b = new byte[16];
        r[0] = 0;
        g[0] = 0;
        b[0] = 0;
        r[1] = 0;
        g[1] = 0;
        b[1] = (byte) 192;
        r[2] = 0;
        g[2] = 0;
        b[2] = (byte) 255;
        r[3] = 0;
        g[3] = (byte) 192;
        b[3] = 0;
        r[4] = 0;
        g[4] = (byte) 255;
        b[4] = 0;
        r[5] = 0;
        g[5] = (byte) 192;
        b[5] = (byte) 192;
        r[6] = 0;
        g[6] = (byte) 255;
        b[6] = (byte) 255;
        r[7] = (byte) 192;
        g[7] = 0;
        b[7] = 0;
        r[8] = (byte) 255;
        g[8] = 0;
        b[8] = 0;
        r[9] = (byte) 192;
        g[9] = 0;
        b[9] = (byte) 192;
        r[10] = (byte) 255;
        g[10] = 0;
        b[10] = (byte) 255;
        r[11] = (byte) 192;
        g[11] = (byte) 192;
        b[11] = 0;
        r[12] = (byte) 255;
        g[12] = (byte) 255;
        b[12] = 0;
        r[13] = (byte) 80;
        g[13] = (byte) 80;
        b[13] = (byte) 80;
        r[14] = (byte) 192;
        g[14] = (byte) 192;
        b[14] = (byte) 192;
        r[15] = (byte) 255;
        g[15] = (byte) 255;
        b[15] = (byte) 255;
        return new IndexColorModel(4, 16, r, g, b);
    }

    private byte[] pullMapBytes(String directoryLocation) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            URL url = new URL(directoryLocation);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            InputStream is = httpURLConnection.getInputStream();
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer.toByteArray();
    }
}

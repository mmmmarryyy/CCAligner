package br.com.manish.ahy.client;

import br.com.manish.ahy.client.exception.OopsException;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class HTTPUtil {

    public static HTTPContent getHTTPContent(String path) {
        return getHTTPContent(path, null);
    }

    public static HTTPContent getHTTPContent(String path, String parameters) {
        HTTPContent ret = new HTTPContent();
        try {
            final URL url = new URL(path);
            final URLConnection urlConn = url.openConnection();
            if (!(urlConn instanceof HttpURLConnection)) throw new OopsException("URL protocol must be HTTP.");
            final HttpURLConnection httpConn = (HttpURLConnection) urlConn;
            httpConn.setConnectTimeout(10000);
            httpConn.setReadTimeout(10000);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestProperty("User-agent", "Java/1.6.0_13");
            if (parameters != null) {
                httpConn.setRequestMethod("POST");
                httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
                httpConn.setRequestProperty("Content-Length", "" + Integer.toString(parameters.getBytes().length));
                httpConn.setRequestProperty("Content-Language", "en-US");
                httpConn.setUseCaches(false);
                httpConn.setDoInput(true);
                httpConn.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(httpConn.getOutputStream());
                wr.writeBytes(parameters);
                wr.flush();
                wr.close();
            }
            httpConn.connect();
            ret.setResponseHeader(httpConn.getHeaderFields());
            ret.setResponseCode(httpConn.getResponseCode());
            ret.setResponseURL(httpConn.getURL());
            final Integer length = httpConn.getContentLength();
            final String type = httpConn.getContentType();
            if (type != null) {
                final String[] parts = type.split(";");
                ret.setMimeType(parts[0].trim());
                for (int i = 1; i < parts.length && ret.getCharset() == null; i++) {
                    final String t = parts[i].trim();
                    final int index = t.toLowerCase().indexOf("charset=");
                    if (index != -1) {
                        ret.setCharset(t.substring(index + 8));
                    }
                }
            }
            final InputStream stream = httpConn.getErrorStream();
            if (stream != null) {
                ret.setSucess(false);
                ret.setContent(readStream(stream, length));
            } else if (httpConn.getContent() != null && httpConn.getContent() instanceof InputStream) {
                ret.setSucess(true);
                ret.setContent(readStream((InputStream) httpConn.getContent(), length));
            }
            httpConn.disconnect();
        } catch (Exception e) {
            throw new OopsException(e, "Some problem happened when trying to get the file: {0}", path);
        }
        return ret;
    }

    private static byte[] readStream(InputStream stream, Integer length) throws IOException {
        final int buflen = Math.max(1024, Math.max(length, stream.available()));
        byte[] buf = new byte[buflen];
        byte[] bytes = null;
        for (int nRead = stream.read(buf); nRead != -1; nRead = stream.read(buf)) {
            if (bytes == null) {
                bytes = buf;
                buf = new byte[buflen];
                continue;
            }
            final byte[] newBytes = new byte[bytes.length + nRead];
            System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
            System.arraycopy(buf, 0, newBytes, bytes.length, nRead);
            bytes = newBytes;
        }
        return bytes;
    }
}

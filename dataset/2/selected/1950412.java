package com.jspx.upload;

import com.jspx.core.environment.Environment;
import java.io.*;
import java.net.*;
import java.util.*;

/** 
 * A class to simplify HTTP applet-server communication.  It abstracts
 * the communication into messages, which can be either GET or POST.
 * <p>
 * It can be used like this:
 * <blockquote><pre>
 * URL url = new URL(getCodeBase(), "/upload/ServletName");
 * &nbsp;
 * HttpMessage msg = new HttpMessage(url);
 * &nbsp;
 * // Parameters may optionally be set using java.util.Properties
 * Properties props = new Properties();
 * props.put("name", "value");
 * &nbsp;
 * // Headers, cookies, and authorization may be set as well
 * msg.setHeader("Accept", "image/png");             // optional
 * msg.setCookie("JSESSIONID", "9585155923883872");  // optional
 * msg.setAuthorization("guest", "try2gueSS");       // optional
 * &nbsp;
 * InputStream in = msg.sendGetMessage(props);
 * </pre></blockquote>
 * <p>
 * This class is loosely modeled after the ServletMessage class written 
 * by Rod McChesney of JavaSoft.
 *
 * @author <b>Jason Hunter</b>, Copyright &#169; 1998
 * @version 1.3, 2000/10/24, fixed headers NPE bug
 * @version 1.2, 2000/10/15, changed upload object MIME type to
 *                           application/x-java-serialized-object
 * @version 1.1, 2000/06/11, added ability to set headers, cookies, 
                             and authorization
 * @version 1.0, 1998/09/18
 */
public class HttpMessage {

    URL servlet = null;

    Map<String, String> headers = null;

    /**
   * Constructs a new HttpMessage that can be used to communicate with the 
   * upload at the specified URL.
   *
   * @param servlet the server resource (typically a upload) with which
   * to communicate
   */
    public HttpMessage(URL servlet) {
        this.servlet = servlet;
    }

    /**
   * Performs a GET request to the upload, with no query string.
   *
   * @return an InputStream to read the response
   * @exception IOException if an I/O error occurs
   */
    public InputStream sendGetMessage() throws IOException {
        return sendGetMessage(null, null);
    }

    /**
   * Performs a GET request to the upload, building
   * a query string from the supplied properties list.
   *
   * @param args the properties list from which to build a query string
   * @return an InputStream to read the response
   * @exception IOException if an I/O error occurs
   */
    public InputStream sendGetMessage(Properties args, String encode) throws IOException {
        if (encode == null || "null".equalsIgnoreCase(encode)) encode = Environment.DefaultEncode;
        String argString = "";
        if (args != null) {
            argString = "?" + toEncodedString(args, encode);
        }
        URL url = new URL(servlet.toExternalForm() + argString);
        URLConnection con = url.openConnection();
        con.setUseCaches(false);
        sendHeaders(con);
        return con.getInputStream();
    }

    /**
   * Performs a POST request to the upload, with no query string.
   *
   * @return an InputStream to read the response
   * @exception IOException if an I/O error occurs
   */
    public InputStream sendPostMessage() throws IOException {
        return sendPostMessage(null);
    }

    /**
   * Performs a POST request to the upload, building
   * post data from the supplied properties list.
   *
   * @param args the properties list from which to build the post data
   * @return an InputStream to read the response
   * @exception IOException if an I/O error occurs
   */
    public InputStream sendPostMessage(Properties args, String encode) throws IOException {
        String argString = "";
        if (args != null) {
            argString = toEncodedString(args, encode);
        }
        URLConnection con = servlet.openConnection();
        con.setDoInput(true);
        con.setDoOutput(true);
        con.setUseCaches(false);
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        sendHeaders(con);
        DataOutputStream out = new DataOutputStream(con.getOutputStream());
        out.writeBytes(argString);
        out.flush();
        out.close();
        return con.getInputStream();
    }

    /**
   * Performs a POST request to the upload, uploading a serialized object.
   * <p>
   * The upload can receive the object in its <tt>doPost()</tt> method
   * like this:
   * <pre>
   *     ObjectInputStream objin =
   *       new ObjectInputStream(req.getInputStream());
   *     Object obj = objin.readObject();
   * </pre>
   * The type of the upload object can be determined through introspection.
   *
   * @param obj the serializable object to upload
   * @return an InputStream to read the response
   * @exception IOException if an I/O error occurs
   */
    public InputStream sendPostMessage(Serializable obj) throws IOException {
        URLConnection con = servlet.openConnection();
        con.setDoInput(true);
        con.setDoOutput(true);
        con.setUseCaches(false);
        con.setRequestProperty("Content-Type", "application/x-java-serialized-object");
        sendHeaders(con);
        ObjectOutputStream out = new ObjectOutputStream(con.getOutputStream());
        out.writeObject(obj);
        out.flush();
        out.close();
        return con.getInputStream();
    }

    /**
   * Sets a request header with the given name and value.  The header 
   * persists across multiple requests.  The caller is responsible for
   * ensuring there are no illegal characters in the name and value.
   *
   * @param name the header name
   * @param value the header value
   */
    public void setHeader(String name, String value) {
        if (headers == null) {
            headers = new Hashtable<String, String>();
        }
        headers.put(name, value);
    }

    private void sendHeaders(URLConnection con) {
        if (headers != null) {
            for (String name : headers.keySet()) {
                con.setRequestProperty(name, headers.get(name));
            }
        }
    }

    /**
   * Sets a request cookie with the given name and value.  The cookie 
   * persists across multiple requests.  The caller is responsible for
   * ensuring there are no illegal characters in the name and value.
   *
   * @param name the header name
   * @param value the header value
   */
    public void setCookie(String name, String value) {
        if (headers == null) {
            headers = new Hashtable<String, String>();
        }
        String existingCookies = headers.get("Cookie");
        if (existingCookies == null) {
            setHeader("Cookie", name + "=" + value);
        } else {
            setHeader("Cookie", existingCookies + "; " + name + "=" + value);
        }
    }

    /**
   * Sets the authorization information for the request (using BASIC
   * authentication via the HTTP Authorization header).  The authorization 
   * persists across multiple requests.
   *
   * @param name the user name
   * @param name the user password
   */
    public void setAuthorization(String name, String password) {
        String authorization = Base64Encoder.encode(name + ":" + password);
        setHeader("Authorization", "Basic " + authorization);
    }

    private String toEncodedString(Properties args, String encode) {
        StringBuffer buf = new StringBuffer();
        Enumeration names = args.propertyNames();
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            String value = args.getProperty(name);
            try {
                buf.append(URLEncoder.encode(name, encode)).append("=").append(URLEncoder.encode(value, encode));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if (names.hasMoreElements()) buf.append("&");
        }
        return buf.toString();
    }
}

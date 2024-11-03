package com.manning.sdmia.web.springmvc;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.springframework.osgi.test.AbstractConfigurableBundleCreatorTests;

/**
 * @author Thierry Templier
 */
public class SpringMvcTest extends AbstractConfigurableBundleCreatorTests {

    public void testWebapp() throws Exception {
        Thread.sleep(2000);
        assertTrue(getTextResponse("http://localhost:8080/springmvc/contacts.do").contains("Arnaud Cogoluegnes"));
    }

    @Override
    protected String[] getTestBundlesNames() {
        return new String[] { "org.springframework.osgi, spring-osgi-web, 2.0.0.M1", "org.springframework.osgi, spring-osgi-web-extender, 2.0.0.M1", "org.springframework, org.springframework.context.support, 3.0.0.RC1", "org.springframework, org.springframework.web, 3.0.0.RC1", "org.springframework, org.springframework.web.servlet, 3.0.0.RC1", "org.apache.log4j, com.springsource.org.apache.log4j, 1.2.15", "org.springframework.osgi, catalina.osgi, 5.5.23-SNAPSHOT", "org.springframework.osgi, catalina.start.osgi, 1.0-SNAPSHOT", "javax.el, com.springsource.javax.el, 1.0.0", "javax.servlet, com.springsource.javax.servlet, 2.5.0", "javax.servlet, com.springsource.javax.servlet.jsp, 2.1.0", "javax.servlet, com.springsource.javax.servlet.jsp.jstl, 1.1.2", "org.apache.commons, com.springsource.org.apache.commons.el, 1.0.0", "org.apache.taglibs, com.springsource.org.apache.taglibs.standard, 1.1.2", "org.aspectj, com.springsource.org.aspectj.runtime, 1.6.1", "org.aspectj, com.springsource.org.aspectj.weaver, 1.6.1", "org.springframework.osgi, jasper.osgi, 5.5.23-SNAPSHOT", "com.manning.sdmia.ch08, ch08-directory, 1.0-SNAPSHOT", "com.manning.sdmia.ch08, ch08-springmvc, 1.0-SNAPSHOT" };
    }

    private String getTextResponse(String address) throws Exception {
        URL url = new URL(address);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setUseCaches(false);
        BufferedReader in = null;
        try {
            con.connect();
            assertEquals(HttpURLConnection.HTTP_OK, con.getResponseCode());
            in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String inputLine = null;
            while ((inputLine = in.readLine()) != null) {
                builder.append(inputLine);
            }
            return builder.toString();
        } finally {
            if (in != null) {
                in.close();
            }
            con.disconnect();
        }
    }
}

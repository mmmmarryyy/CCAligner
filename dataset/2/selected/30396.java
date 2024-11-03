package org.myrpg.hicare.client;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.jar.*;
import org.myrpg.hicare.*;

/**
 * Kernel loader is a tool box for checking server's kernel version and download new version if neccessary.
 * It handles directory creation in HICARE_HOME/save directory.
 * It makes intensive use of WorldConfig to build directories and contact servers.
 * It also load HiCareKernel implementing class (HiCareSession), therefore builds appropriate URLClassLoader.
 * Enhancements : for multi-threading, check for running server kernel (which implies no kernel update for that server).
 */
public class KernelLoader {

    private Map kernelPool;

    public KernelLoader() {
        kernelPool = new HashMap();
    }

    public HiCareKernel loadKernel(HiCare hiCare, WorldConfig worldConf) {
        HiCareKernel kernel;
        URLClassLoader classLoader;
        File binDir;
        File localDir;
        URL binDirURL;
        URL localDirURL;
        URL distDirURL;
        String ejb;
        Properties props;
        kernel = null;
        if (kernelPool.containsKey(worldConf.getName())) kernel = (HiCareKernel) kernelPool.get(worldConf.getName()); else {
            try {
                binDir = new File(hiCare.getHome(), "bin");
                localDir = new File(hiCare.getHome(), "save");
                if (!localDir.exists()) localDir.mkdir();
                localDir = new File(localDir, worldConf.getName());
                if (!localDir.exists()) localDir.mkdir();
                binDirURL = binDir.toURL();
                localDirURL = localDir.toURL();
                hiCare.log("Deploy URL : " + localDirURL);
                distDirURL = new URL(worldConf.getDownload());
                hiCare.log("Distant download URL : " + distDirURL);
                URL url = new URL("jar:" + distDirURL + "hicare-version.jar!/");
                JarURLConnection jarConnection = (JarURLConnection) url.openConnection();
                Manifest manifest = jarConnection.getManifest();
                Attributes attr = manifest.getMainAttributes();
                hiCare.log("Kernel Version : " + attr.getValue("HiCare-Kernel-Version"));
                ejb = attr.getValue("EJB-URL");
                if (ejb != null) ejb.trim();
                downloadFile(distDirURL, localDir, "hicare-version.jar");
                downloadFile(distDirURL, localDir, "hicare-kernel.jar");
                downloadFile(distDirURL, localDir, "data.jar");
                downloadFile(distDirURL, localDir, "myrpg-atlas-client.jar");
                downloadFile(distDirURL, localDir, "jboss-j2ee.jar");
                downloadFile(distDirURL, localDir, "jnp-client.jar");
                downloadFile(distDirURL, localDir, "jboss-client.jar");
                downloadFile(distDirURL, localDir, "jbosssx-client.jar");
                downloadFile(distDirURL, localDir, "jaas.jar");
                classLoader = new URLClassLoader(new URL[] { new URL(localDirURL, "hicare-kernel.jar"), new URL(binDirURL, "hicare-interface.jar"), new URL(localDirURL, "myrpg-atlas-client.jar"), new URL(localDirURL, "jboss-j2ee.jar"), new URL(localDirURL, "jnp-client.jar"), new URL(localDirURL, "jboss-client.jar"), new URL(localDirURL, "jbosssx-client.jar"), new URL(localDirURL, "jaas.jar") });
                Class c = classLoader.loadClass("org.myrpg.hicare.kernel.HiCareSession");
                Object object = c.newInstance();
                kernel = (HiCareKernel) object;
                kernel.setClassLoader(classLoader);
                props = (Properties) System.getProperties().clone();
                props.setProperty("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
                props.setProperty("java.naming.provider.url", ejb);
                props.setProperty("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
                kernel.setParams("", "", props, new URL(localDirURL, "data.jar"));
            } catch (Throwable x) {
                hiCare.log("KernelLoader Error");
                ExceptionLogger.log(x, hiCare);
                kernel = null;
            }
            if (kernel != null) kernelPool.put(worldConf.getName(), kernel);
        }
        return kernel;
    }

    public void unloadKernel(HiCareKernel kernel) {
        Iterator it;
        Collection list;
        Object key;
        list = new ArrayList();
        it = kernelPool.keySet().iterator();
        while (it.hasNext()) {
            if (kernel == kernelPool.get(key = it.next())) list.add(key);
        }
        it = list.iterator();
        while (it.hasNext()) kernelPool.remove(it.next());
    }

    private static void downloadFile(URL fromDir, File toDir, String fileName) throws Throwable {
        URL source;
        URLConnection conn;
        File result;
        BufferedInputStream in;
        BufferedOutputStream out;
        int bSize;
        byte buffer[];
        int ccount;
        bSize = 256;
        buffer = new byte[bSize];
        source = new URL(fromDir, fileName);
        result = new File(toDir, fileName);
        if (result.exists()) result.delete();
        conn = source.openConnection();
        in = new BufferedInputStream(conn.getInputStream());
        out = new BufferedOutputStream(new FileOutputStream(result));
        while ((ccount = in.read(buffer, 0, bSize)) != -1) out.write(buffer, 0, ccount);
        in.close();
        out.close();
    }
}

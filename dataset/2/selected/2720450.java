package net.rsapollo.update;

import java.io.*;
import java.net.*;
import net.rsapollo.Main;

/**
 * <!-- This isnt meant to be read raw. Please use the documentation to
 * correctly read this --> Installs updates to the server, and overwrites
 * outdated base classes. <!-- Break --><br />
 * <hr width="80%" align="center" size="1" style="color: #FFFFFF;" />
 * <div align="center" style="font-weight: bold; font-size: 18px;">Notice</div>
 * This class is considered a base class, and therefore isnt setup to be
 * directly edited. So please, <b>Do not edit this file!</b> <!-- Break --><br />
 * <hr width="80%" align="center" size="1" style="color: #FFFFFF;" />
 * <br />
 * Apollo Server - RS Private Server<br />
 * Website: <a href="http://www.rsapollo.net/">http://www.rsapollo.net/</a><br />
 * Copyright (C) 2007 Kris <br />
 * <br />
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or any later version. <br />
 * <br />
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. <br />
 * <br />
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <a
 * href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.<br />
 * 
 * @author kris
 */
public class Installer implements Runnable {

    Download[] toDown;

    /**
	 * 
	 */
    public Installer() {
    }

    public void addDownload(double v, int s, String file) {
        int len = (toDown != null ? toDown.length : 0);
        Download[] tmp = new Download[len + 1];
        for (int i = 0; i < len; i++) tmp[i] = toDown[i];
        Download d = new Download(v, s, file);
        tmp[len] = d;
        toDown = tmp;
    }

    public void run() {
        Version v = Main.Updates.versions[0];
        for (int i = 0; i < Main.Updates.versions.length; i++) if ((Main.Updates.versions[i].version > v.version && Main.Updates.versions[i].stage >= v.stage) || (Main.Updates.versions[i].version >= v.version && Main.Updates.versions[i].stage > v.stage)) v = Main.Updates.versions[i];
        Object[][] tmpData = new Object[v.files.length][4];
        for (int i = 0; i < v.files.length; i++) {
            boolean update = true;
            try {
                if (Main.Updates.current.getFile(v.files[i].name).version >= v.files[i].version || (Main.Updates.current.getFile(v.files[i].name).version == v.files[i].version && Main.Updates.current.getFile(v.files[i].name).stage >= v.files[i].stage)) {
                    update = false;
                }
            } catch (NullPointerException e) {
            }
            if (update) addDownload(v.files[i].version, v.files[i].stage, v.files[i].name);
        }
        if (toDown == null) return;
        Main.Interface.Update.prgFiles.setMaximum(toDown.length);
        for (int i = 0; i < toDown.length; i++) {
            Main.Interface.Update.prgFiles.setValue(i);
            Main.Interface.Update.prgFiles.setString("Downloading " + toDown[i].file);
            String file = toDown[i].file;
            java.util.regex.Pattern p = java.util.regex.Pattern.compile("\\.");
            java.util.regex.Matcher m = p.matcher(file);
            file = m.replaceAll("\\/");
            java.io.File folder = new java.io.File("updates/" + file.substring(0, file.lastIndexOf("/")));
            folder.mkdirs();
            download("http://update.rsapollo.net/" + toDown[i].version + "/" + toDown[i].stage + "/" + file + ".java", "updates/" + file + ".java", toDown[i].file, toDown[i].version, toDown[i].stage);
        }
        Main.Interface.Update.prgFiles.setValue(Main.Interface.Update.prgFiles.getMaximum());
    }

    public static void download(String address, String localFileName, String rawClass, double newVer, int newStage) {
        OutputStream out = null;
        URLConnection conn = null;
        InputStream in = null;
        int totalBytes = 0;
        int dlBytes = 0;
        try {
            if (!Main.Updates.current.hasFile(rawClass)) {
                Main.Updates.current.addFile(newVer, newStage, rawClass);
            }
            Main.Updates.current.getFile(rawClass).downloading = true;
            Main.Updates.setImage(rawClass, "refresh.png");
            java.io.File folder = new java.io.File(localFileName);
            folder.createNewFile();
            URL url = new URL(address);
            out = new BufferedOutputStream(new FileOutputStream(localFileName));
            conn = url.openConnection();
            in = conn.getInputStream();
            totalBytes = conn.getContentLength();
            byte[] buffer = new byte[1024];
            int numRead;
            long numWritten = 0;
            double incr = java.lang.Math.floor(totalBytes / 1000);
            Main.Interface.Update.prgStatus.setMaximum(1000);
            Main.Interface.Update.prgStatus.setString("0.0%");
            while ((numRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, numRead);
                numWritten += numRead;
                dlBytes += numRead;
                int newVal = (dlBytes != totalBytes ? (int) java.lang.Math.floor(dlBytes / incr) : 1000);
                Main.Interface.Update.prgStatus.setValue(newVal);
                Main.Interface.Update.prgStatus.setString((newVal / 10) + "." + (newVal % 10) + "%");
            }
            Main.Updates.current.getFile(rawClass).downloading = false;
            Main.Updates.current.getFile(rawClass).version = newVer;
            Main.Updates.current.getFile(rawClass).stage = newStage;
            Main.Updates.setImage(rawClass, "updater.png");
            Main.Updates.updateTable();
        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
            } catch (IOException ioe) {
            }
        }
    }
}

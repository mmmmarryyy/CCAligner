package gov.sns.apps.wireanalysis;

import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * Read and parse a wirescan file, extract data and send out as HashMap.
 *
 * @author  cp3
 */
public class ParseWireFile {

    public ArrayList wiredata = new ArrayList();

    /** Creates new ParseWireFile */
    public ParseWireFile() {
    }

    public ArrayList parseFile(File newfile) throws IOException {
        String s;
        String firstName;
        String header;
        String name = null;
        Integer PVLoggerID = new Integer(0);
        String[] tokens;
        int nvalues = 0;
        double num1, num2, num3;
        boolean readfit = false;
        boolean readraw = false;
        boolean zerodata = false;
        boolean baddata = false;
        ArrayList fitparams = new ArrayList();
        ArrayList xraw = new ArrayList();
        ArrayList yraw = new ArrayList();
        ArrayList zraw = new ArrayList();
        ArrayList sraw = new ArrayList();
        URL url = newfile.toURL();
        InputStream is = url.openStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        while ((s = br.readLine()) != null) {
            tokens = s.split("\\s+");
            nvalues = tokens.length;
            firstName = tokens[0];
            if ((tokens[0]).length() == 0) {
                readraw = false;
                readfit = false;
                continue;
            }
            if ((nvalues == 4) && (!firstName.startsWith("---"))) {
                if ((Double.parseDouble(tokens[1]) == 0.) && (Double.parseDouble(tokens[2]) == 0.) && (Double.parseDouble(tokens[3]) == 0.)) {
                    zerodata = true;
                } else {
                    zerodata = false;
                }
                if (tokens[1].equals("NaN") || tokens[2].equals("NaN") || tokens[3].equals("NaN")) {
                    baddata = true;
                } else {
                    baddata = false;
                }
            }
            if (firstName.startsWith("start")) {
                header = s;
            }
            if (firstName.indexOf("WS") > 0) {
                if (name != null) {
                    dumpData(name, fitparams, sraw, yraw, zraw, xraw);
                }
                name = tokens[0];
                readraw = false;
                readfit = false;
                zerodata = false;
                baddata = false;
                fitparams.clear();
                xraw.clear();
                yraw.clear();
                zraw.clear();
                sraw.clear();
            }
            if (firstName.startsWith("Area")) ;
            if (firstName.startsWith("Ampl")) ;
            if (firstName.startsWith("Mean")) ;
            if (firstName.startsWith("Sigma")) {
                fitparams.add(new Double(Double.parseDouble(tokens[3])));
                fitparams.add(new Double(Double.parseDouble(tokens[1])));
                fitparams.add(new Double(Double.parseDouble(tokens[5])));
            }
            if (firstName.startsWith("Offset")) ;
            if (firstName.startsWith("Slope")) ;
            if ((firstName.equals("Position")) && ((tokens[2]).equals("Raw"))) {
                readraw = true;
                continue;
            }
            if ((firstName.equals("Position")) && ((tokens[2]).equals("Fit"))) {
                readfit = true;
                continue;
            }
            if (firstName.startsWith("---")) continue;
            if (readraw && (!zerodata) && (!baddata)) {
                sraw.add(new Double(Double.parseDouble(tokens[0]) / Math.sqrt(2.0)));
                yraw.add(new Double(Double.parseDouble(tokens[1])));
                zraw.add(new Double(Double.parseDouble(tokens[2])));
                xraw.add(new Double(Double.parseDouble(tokens[3])));
            }
            if (firstName.startsWith("PVLogger")) {
                try {
                    PVLoggerID = new Integer(Integer.parseInt(tokens[2]));
                } catch (NumberFormatException e) {
                }
            }
        }
        dumpData(name, fitparams, sraw, yraw, zraw, xraw);
        wiredata.add(PVLoggerID);
        return wiredata;
    }

    private void dumpData(String label, ArrayList fitparams, ArrayList sraw, ArrayList yraw, ArrayList zraw, ArrayList xraw) {
        HashMap data = new HashMap();
        data.put("name", label);
        data.put("fitparams", new ArrayList(fitparams));
        data.put("sdata", new ArrayList(sraw));
        data.put("xdata", new ArrayList(xraw));
        data.put("ydata", new ArrayList(yraw));
        data.put("zdata", new ArrayList(zraw));
        wiredata.add(data);
    }

    private void writeData() {
        Iterator itr = wiredata.iterator();
        while (itr.hasNext()) {
            HashMap map = (HashMap) itr.next();
            ArrayList fitlist = (ArrayList) map.get("fitparams");
            ArrayList slist = (ArrayList) map.get("sdata");
            ArrayList ylist = (ArrayList) map.get("ydata");
            ArrayList zlist = (ArrayList) map.get("zdata");
            ArrayList xlist = (ArrayList) map.get("xdata");
            int ssize = slist.size();
            int xsize = xlist.size();
            int ysize = ylist.size();
            int zsize = zlist.size();
            System.out.println("This is " + map.get("name"));
            System.out.println("With fit params " + fitlist.get(0) + " " + fitlist.get(1) + " " + fitlist.get(2));
            if ((ssize == xsize) && (xsize == ysize) && (ysize == zsize)) {
                for (int i = 0; i < ssize; i++) {
                    System.out.println(slist.get(i) + "  " + ylist.get(i) + "  " + zlist.get(i) + "  " + xlist.get(i));
                }
            } else {
                System.out.println("Oops, a problem with array sizing.");
                System.out.println(ssize + " " + xsize + " " + ysize + " " + zsize);
            }
        }
    }
}

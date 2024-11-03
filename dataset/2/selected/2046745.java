package ptolemy.plot;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.JApplet;
import javax.swing.SwingUtilities;

/**
 An Applet that can plot data from a URL.
 The URL should be specified using the dataurl applet parameter.
 The formatting commands are included in the file with the
 the data.
 If no URL is given, then a sample plot is generated.

 @author Edward A. Lee, Christopher Hylands, Contributor: Roger Robins
 @version $Id: PlotApplet.java,v 1.76 2005/07/30 05:30:56 cxh Exp $
 @since Ptolemy II 0.2
 @Pt.ProposedRating Yellow (cxh)
 @Pt.AcceptedRating Yellow (cxh)
 @see PlotBox
 @see Plot
 */
public class PlotApplet extends JApplet {

    /** Return a string describing this applet.
     *  @return A string describing the applet.
     */
    public String getAppletInfo() {
        return "PlotApplet " + PlotBox.PTPLOT_RELEASE + ": A data plotter.\n" + "By: Edward A. Lee and\n " + "Christopher Hylands\n" + "($Id: PlotApplet.java,v 1.76 2005/07/30 05:30:56 cxh Exp $)";
    }

    /** Return information about parameters.
     *  @return A array of arrays giving parameter names, the type,
     *   and the default value or description.
     */
    public String[][] getParameterInfo() {
        String[][] pinfo = { { "background", "hexcolor value", "background color" }, { "foreground", "hexcolor value", "foreground color" }, { "dataurl", "url", "the URL of the data to plot" }, { "height", "integer", "100" }, { "width", "integer", "100" } };
        return pinfo;
    }

    /** Initialize the applet.  Read the applet parameters.

     *  Subclasses that extend this method and call Swing UI methods
     *  should do so in the Swing Event thread by calling
     *  SwingUtilities.invokeAndWait().
     *  Note that some Plot methods will automatically run in the
     *  Swing Event thread, some will not.
     *  For details about SwingUtilities.invokeAndWait(), see
     *  <a href="http://java.sun.com/docs/books/tutorial/uiswing/components/applet.html#thread">The Sun Applet Tutorial</a>
     */
    public void init() {
        super.init();
        Runnable doActions = new Runnable() {

            public void run() {
                if (_plot == null) {
                    _plot = newPlot();
                }
                getContentPane().add(plot(), BorderLayout.NORTH);
                int width;
                int height;
                String widthspec = getParameter("width");
                if (widthspec != null) {
                    width = Integer.parseInt(widthspec);
                } else {
                    width = 400;
                }
                String heightspec = getParameter("height");
                if (heightspec != null) {
                    height = Integer.parseInt(heightspec);
                } else {
                    height = 400;
                }
                _setPlotSize(width, height);
                plot().setButtons(true);
                Color background = Color.white;
                String colorspec = getParameter("background");
                if (colorspec != null) {
                    background = PlotBox.getColorByName(colorspec);
                }
                setBackground(background);
                plot().setBackground(background);
                getContentPane().setBackground(background);
                Color foreground = Color.black;
                colorspec = getParameter("foreground");
                if (colorspec != null) {
                    foreground = PlotBox.getColorByName(colorspec);
                }
                setForeground(foreground);
                plot().setForeground(foreground);
                plot().setVisible(true);
                String dataurlspec = getParameter("dataurl");
                if (dataurlspec != null) {
                    try {
                        showStatus("Reading data");
                        URL dataurl = new URL(getDocumentBase(), dataurlspec);
                        InputStream in = dataurl.openStream();
                        _read(in);
                        showStatus("Done");
                    } catch (MalformedURLException e) {
                        System.err.println(e.toString());
                    } catch (FileNotFoundException e) {
                        System.err.println("PlotApplet: file not found: " + e);
                    } catch (IOException e) {
                        System.err.println("PlotApplet: error reading input file: " + e);
                    }
                }
            }
        };
        try {
            SwingUtilities.invokeAndWait(doActions);
        } catch (Exception ex) {
        }
    }

    /** Create a new Plot object for the applet.  Derived classes can
     *  redefine this method to return a different type of plot object.
     *  @return A new instance of PlotBox.
     */
    public PlotBox newPlot() {
        return new Plot();
    }

    /** Return the plot object to operate on.
     *  @return The plot object associated with this applet.
     */
    public PlotBox plot() {
        return _plot;
    }

    /** Set the plot object to operate on.
     *  @param plot The plot object to associate with this applet.
     */
    public void setPlot(PlotBox plot) {
        _plot = plot;
    }

    /** Read the specified stream.  Derived classes may override this
     *  to support other file formats.
     *  @param in The input stream.
     *  @exception IOException If the stream cannot be read.
     */
    protected void _read(InputStream in) throws IOException {
        plot().read(in);
    }

    /** Given the size of the applet, set the size of the plot.
     *  Derived classes may override this to allow room for other
     *  widgets below the plot.
     *  @param appletWidth The width of the applet.
     *  @param appletHeight The height of the applet.
     */
    protected void _setPlotSize(int appletWidth, int appletHeight) {
        plot().setSize(appletWidth, appletHeight);
    }

    private transient PlotBox _plot;
}

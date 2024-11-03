package org.xmlcml.cml.element;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.junit.Before;
import org.junit.Test;
import org.xmlcml.cml.base.CMLBuilder;
import org.xmlcml.cml.base.CMLElements;
import org.xmlcml.euclid.Util;

/**
 * test Peak and Spectrum
 * 
 * @author pmr
 * 
 */
public abstract class PeakSpectrumTest extends AbstractTest {

    protected String peakStructureFile1 = "peakStructure1" + XML_SUFF;

    protected String peakStructureFile1NoSchema = "peakStructure1-noSchema" + XML_SUFF;

    protected String peakStructureFile2 = "peakStructure2" + XML_SUFF;

    protected String peakStructureFile2Schema = "peakStructure2Schema" + XML_SUFF;

    protected String testfile = "spectrum";

    protected String testfile1 = "spectrum1.xml";

    protected String testfile2 = "spectrum2.xml";

    protected String testfile3 = "spectrum3.xml";

    protected String testfile4 = "spectrum4.xml";

    protected String testfile5 = "spectrum5.xml";

    protected String testCompoundFile1 = "spectrum_and_structure1.xml";

    /**
	 * setup.
	 * 
	 * @throws Exception
	 */
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    private URL makeSpectrumInputStreamContainer(int num) throws IOException {
        return Util.getResource(SIMPLE_RESOURCE + U_S + "spectrum" + num + XML_SUFF);
    }

    protected CMLSpectrum readSpectrum(int num) throws Exception {
        CMLSpectrum spectrum = null;
        URL spurl = makeSpectrumInputStreamContainer(num);
        InputStream in = spurl.openStream();
        spectrum = (CMLSpectrum) new CMLBuilder().build(in).getRootElement();
        in.close();
        return spectrum;
    }

    /** test */
    @Test
    public void testDummy1() {
        ;
    }

    /**
	 * gets the spectrum out of the peakStructure.xml test file
	 * 
	 * @return the spectrum
	 */
    CMLSpectrum getSpectrum() throws Exception {
        CMLSpectrum spectrum = null;
        InputStream in = Util.getInputStreamFromResource(SIMPLE_RESOURCE + U_S + peakStructureFile1);
        CMLCml cml = (CMLCml) new CMLBuilder().build(in).getRootElement();
        spectrum = (CMLSpectrum) cml.getChildCMLElements(CMLSpectrum.TAG).get(0);
        return spectrum;
    }

    /**
	 * gets peaks from peakStructure.xml example file.
	 * 
	 * @return the peaks
	 * @throws Exception
	 */
    CMLElements<CMLPeak> getPeaks() throws Exception {
        CMLSpectrum spectrum = getSpectrum();
        CMLPeakList peakList = spectrum.getPeakListElements().get(0);
        CMLElements<CMLPeak> peaks = peakList.getPeakElements();
        return peaks;
    }

    /**
	 * gets peak structures from peakStructure.xml example files.
	 * 
	 * @param num
	 *            only 1 works. gets peaksStructure[1] on Hb.
	 * @return the peakStructures
	 * @throws Exception
	 */
    CMLElements<CMLPeakStructure> getPeakStructures(int num) throws Exception {
        CMLPeak peak = getPeaks().get(num);
        return peak.getPeakStructureElements();
    }

    /**
	 * gets the molecule out of the peakStructure.xml test file
	 * 
	 * @return the spectrum
	 */
    CMLMolecule getMolecule() throws Exception {
        CMLMolecule molecule = null;
        InputStream in = Util.getInputStreamFromResource(SIMPLE_RESOURCE + U_S + peakStructureFile1);
        CMLCml cml = (CMLCml) new CMLBuilder().build(in).getRootElement();
        in.close();
        molecule = (CMLMolecule) cml.getChildCMLElements(CMLMolecule.TAG).get(0);
        return molecule;
    }
}

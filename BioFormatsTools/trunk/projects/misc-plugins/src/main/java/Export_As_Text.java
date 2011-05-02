//
// Export_As_Text.java
//

/*
Miscellaneous ImageJ plugins.

Copyright (c) 2010, UW-Madison LOCI
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the UW-Madison LOCI nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.plugin.filter.PlugInFilter;
import ij.plugin.frame.RoiManager;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

import java.io.FileWriter;
import java.io.IOException;
import java.util.prefs.*;

/**
 * TODO
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/java/browser/trunk/projects/misc-plugins/src/main/java/Export_As_Text.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/java/trunk/projects/misc-plugins/src/main/java/Export_As_Text.java">SVN</a></dd></dl>
 *
 * @author Aivar Grislis grislis at wisc.edu
 */
public class Export_As_Text implements PlugInFilter {
    private static final String FILE_KEY = "export_as_text_file";
    private String m_file;
    private ImagePlus imp;
    private Roi[] m_rois = {};

    @Override
    public int setup(String arg, ImagePlus imp) {
        this.imp = imp;
        return DOES_ALL;
    }

    @Override
    public void run(ImageProcessor ip) {
        boolean floatType = ip instanceof FloatProcessor;

        // get list of current ROIs
        RoiManager manager = RoiManager.getInstance();
        if (null != manager) {
            m_rois = manager.getRoisAsArray();
            //IJ.log("Got ROI Manager, count " + m_rois.length);
        }
        else IJ.log("No ROI Manager");

        //byte[] byteArray = (byte[]) ip.getPixels();

        if (showFileDialog(getFileFromPreferences())) {
            saveFileInPreferences(m_file);
        }
        else {
            return;
        }

        FileWriter fw = null;
        try {
            fw = new FileWriter(m_file);
        }
        catch (IOException e) {
            IJ.log("exception opening file " + m_file);
            IJ.handleException(e);
            return;
        }

        //StringBuffer sb = new StringBuffer();
//
        //for (byte b : byteArray)
        //{
        //    sb.append( b );
        //    sb.append("\t");
        //}

        try
        {
            //Write the header
            fw.write("x\ty\tROI\tIntensity\n");

            for(int x = 0; x < ip.getWidth(); x++)
            {
                for(int y = 0; y < ip.getHeight(); y++)
                {
                    fw.write( x  + "\t" + y + "\t" + lookUpRoi(x, y) + "\t" + getPixel(floatType, ip, x, y)  + "\n" );
                    //if (0 != lookUpRoi(x,y)) IJ.log(" x " + x + " y " + y + " roi " + lookUpRoi(x,y) + " intensity " + getPixel(floatType, ip, x, y));
                }
            }
            fw.close();
        }
        catch (IOException e)
        {
            IJ.log("exception writing file");
            IJ.handleException(e);
        }

        //IJ.log( sb.toString() );
        imp.updateAndDraw();
    }

    private String getFileFromPreferences() {
       Preferences prefs = Preferences.userNodeForPackage(this.getClass());
       return prefs.get(FILE_KEY, "");
    }

    private void saveFileInPreferences(String file) {
        Preferences prefs = Preferences.userNodeForPackage(this.getClass());
        prefs.put(FILE_KEY, file);
    }

    private boolean showFileDialog(String defaultFile) {
        //TODO shouldn't UI be in separate class?
        GenericDialog dialog = new GenericDialog("Export Image As Text");
        dialog.addStringField("Save As:", defaultFile, 24);
        dialog.showDialog();
        if (dialog.wasCanceled()) {
            return false;
        }

        m_file = dialog.getNextString();
        return true;
    }

    /**
     * Returns Roi number of a given pixel.
     *
     * @param x pixel x
     * @param y pixel y
     * @return which Roi number, 0 for none
     */
    private int lookUpRoi(int x, int y) {
        for (int i = 0; i < m_rois.length; ++i) {
            if (m_rois[i].contains(x, y)) {
                return i + 1;
            }
        }
        return 0;
    }

    /**
     * Returns pixel value.
     *
     * @param floatType whether ImageProcessor is a FloatProcessor
     * @param ip ImageProcessor
     * @param x pixel x
     * @param y pixel y
     * @return pixel value
     */
    private Object getPixel(boolean floatType, ImageProcessor ip, int x, int y) {
        Object returnValue = null;
        int pixel = ip.getPixel(x,y);
        if (floatType) {
            returnValue = Float.intBitsToFloat(pixel);
        }
        else {
            returnValue = pixel;
        }
        return returnValue;
    }

}

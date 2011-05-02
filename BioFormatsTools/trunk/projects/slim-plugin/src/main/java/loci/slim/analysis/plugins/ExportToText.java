/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.slim.analysis.plugins;

import ij.IJ;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.plugin.frame.RoiManager;

import java.io.FileWriter;
import java.io.IOException;
import java.util.prefs.*;

import loci.slim.analysis.ISLIMAnalyzer;
import loci.slim.analysis.SLIMAnalyzer;
import loci.slim.ui.IUserInterfacePanel.FitFunction;
import loci.slim.ui.IUserInterfacePanel.FitRegion;

import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.real.DoubleType;

/**
 * Exports to text for further analysis of SLIMPlugin results.
 *
 * @author Aivar Grislis
 */
@SLIMAnalyzer(name="Export to Text")
public class ExportToText implements ISLIMAnalyzer {
    private static final String FILE_KEY = "export_results_to_text";

    public void analyze(Image<DoubleType> image, FitRegion region, FitFunction function) {
        String fileName = showFileDialog(getFileFromPreferences());
        if (null != fileName) {
            saveFileInPreferences(fileName);
            export(fileName, image, region, function);
        }
    }

    public void export(String fileName, Image<DoubleType> image, FitRegion region, FitFunction function) {
        // get list of current ROIs
        boolean hasRois = false;
        Roi[] rois = {};
        RoiManager manager = RoiManager.getInstance();
        if (null != manager) {
            hasRois = true;
            rois = manager.getRoisAsArray();
        }

        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(fileName);
        } catch (IOException e) {
            IJ.log("exception opening file " + fileName);
            IJ.handleException(e);
        }

        if (null != fileWriter) {
            try
            {

                if (hasRois) {
                    fileWriter.write("x\ty\tROI\t");
                }
                fileWriter.write("HELLO WORLD");
                //Write the header
                fileWriter.write("x\ty\tROI\tIntensity\n");

               // for(int x = 0; x < imp.getWidth(); x++)
              //  {
               //     for(int y = 0; y < imp.getHeight(); y++)
              //      {
                      //  fw.write( x  + "\t" + y + "\t" + lookUpRoi(x, y) + "\t" + getPixel(floatType, ip, x, y)  + "\n" );
                //    }
             //   }
                fileWriter.close();
            }
            catch (IOException e)
            {
                System.out.println("IOEXCEPTION " + e);
                IJ.log("exception writing file");
                IJ.handleException(e);
            }
        }
    }


    private String getFileFromPreferences() {
       Preferences prefs = Preferences.userNodeForPackage(this.getClass());
       return prefs.get(FILE_KEY, "");
    }

    private void saveFileInPreferences(String file) {
        Preferences prefs = Preferences.userNodeForPackage(this.getClass());
        prefs.put(FILE_KEY, file);
    }

    private String showFileDialog(String defaultFile) {
        //TODO shouldn't UI be in separate class?
        GenericDialog dialog = new GenericDialog("Export Results to Text");
        dialog.addStringField("Save As:", defaultFile, 24);
        dialog.showDialog();
        if (dialog.wasCanceled()) {
            return null;
        }

        return dialog.getNextString();
    }

    /**
     * Returns Roi number of a given pixel.
     *
     * @param x pixel x
     * @param y pixel y
     * @return which Roi number, 0 for none
     */
    private int lookUpRoi(Roi rois[], int x, int y) {
        for (int i = 0; i < rois.length; ++i) {
            if (rois[i].contains(x, y)) {
                return i + 1;
            }
        }
        return 0;
    }
}

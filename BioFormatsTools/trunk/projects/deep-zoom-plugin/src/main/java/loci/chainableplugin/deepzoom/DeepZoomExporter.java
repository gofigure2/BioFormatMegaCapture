//
// DeepZoomExporter.java
//

/*
Deep Zoom export plugin that uses the chainable plugin framework.

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

package loci.chainableplugin.deepzoom;

/**
 *
 * @author Aivar Grislis
 */
import com.centerkey.utils.BareBonesBrowserLaunch;

import ij.ImagePlus;
import ij.io.FileSaver;
import ij.process.ImageProcessor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import loci.chainableplugin.AbstractPipelineProcessor;
import loci.plugin.ImageWrapper;
import loci.chainableplugin.IProcessor;

public class DeepZoomExporter implements IProcessor
{
    static final String LEVEL = DeepZoomExporter.class + "_LEVEL";
    static final String FILES_SUFFIX = "_files";
    static final String HTML_SUFFIX = ".html";
    static final String DZI_SUFFIX = ".dzi";
    static final String CRLF = "\r\n";
    static final String FORMAT = "png";
    static final int TILESIZE = 256;
    static final int OVERLAP = 1;
    boolean m_launch;
    boolean m_silverlight;
    String m_folder;
    String m_url;
    String m_name;
    String m_description;
    int m_width;
    int m_height;
    ScaleInHalfProcessor m_scaleInHalfProcessor;

    /**
     * Creates an object to export a particular image.
     *
     * @param launch whether or not to launch a browser window to view results
     * @param silverlight whether or not to use Microsoft Silverlight (tm) technology to view the image
     * @param folder output folder
     * @param url when launching a browser, url to use; otherwise uses file
     * @param name of HMTL file
     * @param description that goes in HTML page title
     * @param width of image window
     * @param height of image window
     */
    public DeepZoomExporter(boolean launch, boolean silverlight, String folder, String url, String name, String description, int width, int height)
    {
        m_launch = launch;
        m_silverlight = silverlight;
        m_folder = new File(folder).getAbsolutePath();
        m_url = url;
        m_name = name;
        m_description = description;
        m_width = width;
        m_height = height;
    }

    /**
     * Processes a given image.
     * 
     * @param image
     * @return
     */
    public int process(ImageWrapper image)
    {
        int xLevels = getLevels(image.getWidth(), 1);
        int yLevels = getLevels(image.getHeight(), 1);
        int levels = Math.max(xLevels, yLevels);
        image.getProperties().set(LEVEL, Integer.valueOf(levels));

        for (int level = 0; level <= levels; level++) {
            File newFolder = new File(m_folder + '/' + m_name + FILES_SUFFIX + '/' + level);
            System.out.println("Create folder " + level + ' ' + newFolder.mkdirs());
        }

        String xml = createDZI(m_folder, m_name, 256, 1, FORMAT, image.getWidth(), image.getHeight());

        createHTML(m_silverlight, m_folder, m_name, m_description, m_width, m_height, xml);

        LevelProcessor levelProcessor = new LevelProcessor();
        CutTilesProcessor cutTilesProcessor = new CutTilesProcessor(256, 256, 1);
        TileProcessor tileProcessor = new TileProcessor();
        m_scaleInHalfProcessor = new ScaleInHalfProcessor();

        // here the processors are chained together
        levelProcessor.chain(cutTilesProcessor);
        cutTilesProcessor.chain(tileProcessor);
        m_scaleInHalfProcessor.chain(levelProcessor);

        // run the processing chain with our input image
        int returnValue = levelProcessor.process(image);

        if (m_launch) {
            launchBrowser(m_folder, m_url, m_name);
        }

        return returnValue;
    }

    /**
     * Calculates how many levels of halving the image are necesary.
     *
     * @param srcWidth initial width
     * @param dstWidth required width
     * @return number of levels
     */
    private int getLevels(int srcWidth, int dstWidth) {
        int level = 1;
        while (srcWidth > dstWidth) {
            srcWidth /= 2;
            level++;
        }
        return level;
    }

    /**
     * Creates and writes out a HTML file.
     *
     * @param silverlight whether or not to use Microsoft Silverlight (tm) technology to view the image
     * @param folder folder to write to
     * @param name name of HTML file
     * @param description HTML title
     * @param width window width
     * @param height window height
     * @param xml XML incantation that describes the image
     */
    private void createHTML(boolean silverlight, String folder, String name, String description, int width, int height, String xml) {
        File file = new File(folder + '/' + name + HTML_SUFFIX);
        PrintStream printStream;
        try {
            printStream = new PrintStream(new FileOutputStream(file));
        }
        catch (FileNotFoundException e) {
            System.out.println("FILE NOT FOUND " + e);
            return;
        }
        
        if (silverlight) {
            createSilverlightHTML(printStream, name, description, width, height, xml);
        }
        else {
            createSeadragonHTML(printStream, name, description, width, height, xml);
        }
        printStream.close();
    }

    private void createSilverlightHTML(PrintStream printStream, String name, String description, int width, int height, String xml) {
       //TODO
    }

    /**
     * Writes HTML to use Seadragon technology.
     *
     * @param printStream
     * @param name
     * @param description
     * @param width
     * @param height
     * @param xml
     */
    private void createSeadragonHTML(PrintStream printStream, String name, String description, int width, int height, String xml) {
        printStream.print("<!DOCTYPE html>\r\n");
        printStream.print("<html>\r\n");
        printStream.print(" <head>\r\n");
        printStream.print("  <title>" + m_description + "</title>" + "\r\n");
        printStream.print("  <script type=\"text/javascript\" src=\"http://seadragon.com/ajax/0.8/seadragon-min.js\">\r\n");
        printStream.print("  </script>\r\n");
        printStream.print("  <script type=\"text/javascript\">\r\n");
        printStream.print("    var viewer = null;\r\n");
        printStream.print("    function init() {\r\n");
        printStream.print("     viewer = new Seadragon.Viewer(\"container\");\r\n");
        printStream.print("     viewer.openDzi(\"" + name + ".xml\",'" + xml + "');" + "\r\n");
        printStream.print("    }\r\n");
        printStream.print("    Seadragon.Utils.addEvent(window, \"load\", init);\r\n");
        printStream.print("  </script>\r\n");
        printStream.print("  <style type=\"text/css\">\r\n");
        printStream.print("    #container {\r\n");
        printStream.print("     width: " + width + "px;" + "\r\n");
        printStream.print("     height: " + height + "px;" + "\r\n");
        printStream.print("     background-color: black;\r\n");
        printStream.print("     border: 1px solid black;\r\n");
        printStream.print("     color: white;\r\n");
        printStream.print("    }\r\n");
        printStream.print("   </style>\r\n");
        printStream.print("  </head>\r\n");
        printStream.print("  <body bgcolor=\"#000000\">\r\n");
        printStream.print("   <div id=\"container\"></div>\r\n");
        printStream.print("  </body>\r\n");
        printStream.print("</html>\r\n");
    }

    /**
     * Writes XML incantation file describing the tiled image.  Returns the incantation to be embedded in the HTML also.
     *
     * @param folder
     * @param name
     * @param tileSize
     * @param overlap
     * @param format
     * @param width
     * @param height
     * @return XML
     */
    private String createDZI(String folder, String name, int tileSize, int overlap, String format, int width, int height)
    {
        String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?><Image TileSize=\"" + tileSize + "\""
                + " Overlap=\"" + overlap + "\"" + " Format=\"" + format + "\""
                + " ServerFormat=\"Default\"" + " xmlns=\"http://schemas.microsoft.com/deepzoom/\"" + ">"
                + " <Size Width=\"" + width + "\" Height=\"" + height + "\" />" + "</Image>";

        File file = new File(folder + '/' + name + ".xml");
        PrintStream printStream;
        try
        {
            printStream = new PrintStream(new FileOutputStream(file));
        }
        catch (FileNotFoundException e) {
            System.out.println("FILE NOT FOUND " + e);
            return null;
        }

        printStream.print(xml);
        printStream.close();

        return xml;
    }

    /**
     * Launches a browser window with the tiled image.
     *
     * @param folder file folder to use
     * @param url URL to use; may be empty string, then use file folder
     * @param name name of HTML file
     */
    private void launchBrowser(String folder, String url, String name) {
        String destUrl = url;
        if (null == url || "".equals(url)) {
            destUrl = "file:///" + folder + '/' + name + HTML_SUFFIX;
        }
        else {
            if (!url.endsWith(HTML_SUFFIX)) {
                if (!url.endsWith("/")) {
                    destUrl += '/';
                }
                destUrl += name + HTML_SUFFIX;
            }
        }
        BareBonesBrowserLaunch.openURL(destUrl);
    }

    /**
     * Inner class that chains from the CutTileProcessor.  Writes out the tiles
     * in appropriate folder with appropriate file name.
     *
     * Handles PNG and JPG.
     */
    private class TileProcessor implements IProcessor
    {
        private TileProcessor()
        {
        }

        public int process(ImageWrapper image)
        {
            int level = ((Integer)image.getProperties().get(DeepZoomExporter.LEVEL)).intValue();
            int xTile = ((Integer)image.getProperties().get(CutTilesProcessor.X)).intValue();
            int yTile = ((Integer)image.getProperties().get(CutTilesProcessor.Y)).intValue();

            String fileName = m_folder + '/' + m_name + FILES_SUFFIX + '/' + level + '/' + xTile + '_' + yTile + '.' + FORMAT;
            ImageProcessor imageProcessor = image.getImageProcessor();
            ImagePlus imagePlus = new ImagePlus("tile", imageProcessor);
            FileSaver fileSaver = new FileSaver(imagePlus);
            if (FORMAT.equals("png")) {
                fileSaver.saveAsPng(fileName);
            }
            else if (FORMAT.equals("jpg")) {
                fileSaver.saveAsJpeg(fileName);
            }
            else {
                System.out.println("UNKNOWN FORMAT " + FORMAT);
            }
            return 0;
        }
    }

    /**
     * Inner class at the top of the image chain.  Passes image on to CutTilesProcessor and also
     * ScaleInHalfProcessor.  Note that this assumes that the CutTilesProcessor handles the image
     * in a read-only fashion.
     *
     * When we get back from the inital chain to the CutTilesProcessor all of the tiles for this
     * image level will have been cut and written out.
     */
    private class LevelProcessor extends AbstractPipelineProcessor
    {
        private LevelProcessor()
        {
        }

        public int process(ImageWrapper image)
        {
            nextInChainProcess(image);

            int level = ((Integer)image.getProperties().get(DeepZoomExporter.LEVEL)).intValue();
            if (level > 0) {
                level--;
                image.getProperties().set(DeepZoomExporter.LEVEL, new Integer(level));

                m_scaleInHalfProcessor.process(image);
            }
            return 0;
        }
    }
}
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.multiinstanceplugin.deepzoom;

import ij.ImagePlus;
import ij.io.FileSaver;
import ij.process.ImageProcessor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import loci.multiinstanceplugin.AbstractPlugin;
import loci.multiinstanceplugin.ILinkedPlugin;
import loci.multiinstanceplugin.IPlugin;
import loci.multiinstanceplugin.LinkedPlugin;
import loci.plugin.ImageWrapper;
import loci.plugin.annotations.Img;
import loci.plugin.annotations.Input;
import loci.plugin.annotations.Output;

    /**
     * XInnerX class that chains from the CutTileProcessor.  Writes out the tiles
     * in appropriate folder with appropriate file name.
     *
     * Handles PNG and JPG.
     */
    @Input
    public class TileProcessor extends AbstractPlugin implements IPlugin
    {
        //TODO
        /*
         * Here is a simple kludge to get around a problem.
         *
         * Formerly TileProcessor was an inner class to DeepZoomExporter so it could see
         * the member variables m_folder and m_name and the static constants FILES_SUFFIX
         * and FORMAT.
         *
         * Unfortunately the Class.newInstance() method I am using to launch the plugins
         * within PluginLauncher won't instantiate an inner class (even though inner class
         * is public with public constructor).
         *
         * So I need some way of passing these settings to this tile processor.
         *
         * One way would be to implement TileProcessor as some new sort of ImageListener, not an
         * IPlugin.  Then it could go back to being an inner class, doesn't get instantiated by
         * PluginLauncher.
         *
         * Another approach is to feed LinkedPlugin settings.  These could be name/value pairs that
         * are subsequently available to new instantiations.  We do need such a scheme in general to
         * pass settings around.
         *
         * Lastly this approach just stuffs the settings into this class as static variables.
         */
        public static String s_folder = null;
        public static String s_name = null;
        public static String s_suffix = null;
        public static String s_format = null;

        public TileProcessor()
        {
        }

        public void process() {
            ImageWrapper image = get();

            int level = ((Integer)image.getProperties().get(DeepZoomExporter.LEVEL)).intValue();
            int xTile = ((Integer)image.getProperties().get(CutTilesProcessor.X)).intValue();
            int yTile = ((Integer)image.getProperties().get(CutTilesProcessor.Y)).intValue();

            //TODO OLD String fileName = m_folder + '/' + m_name + FILES_SUFFIX + '/' + level + '/' + xTile + '_' + yTile + '.' + FORMAT;
            String fileName = s_folder + '/' + s_name + s_suffix + '/' + level + '/' + xTile + '_' + yTile + '.' + s_format;
            ImageProcessor imageProcessor = image.getImageProcessor();
            ImagePlus imagePlus = new ImagePlus("tile", imageProcessor);
            FileSaver fileSaver = new FileSaver(imagePlus);
            //TODO OLD if (FORMAT.equals("png")) {
            if ("png".equals(s_format)) {
                fileSaver.saveAsPng(fileName);
            }
            //TODO OLD else if (FORMAT.equals("jpg")) {
            else if ("jpg".equals(s_format)) {
                fileSaver.saveAsJpeg(fileName);
            }
            else {
                //TODO OLD System.out.println("UNKNOWN FORMAT " + FORMAT);
                System.out.println("Unknown format " + s_format);
            }
        }
    }


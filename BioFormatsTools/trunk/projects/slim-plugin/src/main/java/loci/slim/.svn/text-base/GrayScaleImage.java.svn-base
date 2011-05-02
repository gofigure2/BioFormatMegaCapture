//
// GrayScaleImage.java
//

/*
SLIMPlugin for combined spectral-lifetime image analysis.

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

package loci.slim;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.ImageCanvas;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.awt.image.ColorModel;

import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.UnsignedShortType;

/**
 * The GrayScaleImage shows a grayscale representation of the input data.  It
 * also allows the user to look at all the channels and pick a channel for the
 * fit.
 *
 * @author Aivar Grislis
 */
public class GrayScaleImage<T extends RealType<T>> implements IGrayScaleImage {
    private int m_width;
    private int m_height;
    private ImageStack m_imageStack;
    private MyStackWindow m_stackWindow;
    private ISelectListener m_listener;
    private byte[] m_saveOutPixels[];

    public GrayScaleImage(String title, Image<T> image) {
        int dimensions[] = image.getDimensions();
        //for (int i = 0; i < dimensions.length; ++i) {
        //    System.out.println("dim[" + i + "] " + dimensions[i]);
        //}
        m_width = dimensions[0];
        m_height = dimensions[1];
        int bins = dimensions[2];
        int channels = 1;
        if (dimensions.length > 3) {
            channels = dimensions[3];
        }

        // building an image stack
        m_imageStack = new ImageStack(m_width, m_height);
        m_saveOutPixels = new byte[channels][];

        LocalizableByDimCursor cursor = image.createLocalizableByDimCursor();
        double[][] pixels = new double[m_width][m_height];
        byte[] outPixels = new byte[m_width * m_height];
        int[] position = (channels > 1) ? new int[4] : new int[3];

        for (int c = 0; c < channels; ++c) {
            if (channels > 1) {
                position[3] = c;
            }

            // sum photon counts
            double maxPixel = 0.0;
            for (int x = 0; x < m_width; ++x) {
                position[0] = x;
                for (int y = 0; y < m_height; ++y) {
                    position[1] = y;
                    pixels[x][y] = 0.0;
                    for (int b = 0; b < bins; ++b) {
                        position[2] = b;
                        cursor.setPosition(position);
                        pixels[x][y] += ((UnsignedShortType) cursor.getType()).getRealDouble();
                    }
                    if (pixels[x][y] > maxPixel) {
                        maxPixel = pixels[x][y];
                    }
                }
            }

            // convert to grayscale
            for (int x = 0; x < m_width; ++x) {
                for (int y = 0; y < m_height; ++y) {
                    outPixels[y * m_width + x] = (byte) (pixels[x][y] * 255 / maxPixel);
                }
            }
            //TODO random noise to ensure different channels do have different images:
            //java.util.Random randomizer = new java.util.Random();
            //for (int i = 0; i < 1000; ++i) {
            //    int noise = randomizer.nextInt(255);
            //    int x = randomizer.nextInt(m_width);
            //    int y = randomizer.nextInt(m_height);
            //    outPixels[y * m_width + x] = (byte) noise;
            //}

            // add a slice
           // m_imageStack.addSlice("" + c, true, outPixels); // stopped working 12/1/10
            m_imageStack.addSlice("" + c, outPixels);
            m_saveOutPixels[c] = outPixels;
        }
        ImagePlus imagePlus = new ImagePlus(title, m_imageStack);
        m_stackWindow = new MyStackWindow(imagePlus);
        m_stackWindow.setVisible(true);

        //System.out.println("Channel selector " + m_stackWindow.getChannelSelector());
        //System.out.println("Slice selector " + m_stackWindow.getSliceSelector());
        //System.out.println("Frame selector " + m_stackWindow.getFrameSelector());

        // hook up mouse listener
        ImageCanvas canvas = m_stackWindow.getCanvas();
        canvas.addMouseListener(
            new MouseListener() {
                public void mousePressed(MouseEvent e) {}
                public void mouseExited(MouseEvent e) {}
                public void mouseClicked(MouseEvent e) {}
                public void mouseEntered(MouseEvent e) {}

                public void mouseReleased(MouseEvent e) {
                    if (null != m_listener) {
                        m_listener.selected(getChannel(), e.getX(), e.getY());
                    }
                }
            }
        );
    }

    /**
     * Sets a listener for when the user clicks on the image.
     *
     * @param listener
     */
    public void setListener(ISelectListener listener) {
        m_listener = listener;
    }

    /**
     * Gets the channel slider selection.
     *
     * @return channel
     */
    public int getChannel(){
        // covert 1...n to 0...n-1
        return m_stackWindow.getSlice() - 1;
    }

    /**
     * Disables and enables channel selection, during and after a fit.
     *
     * @param enable
     */
    public void enable(boolean enable) {
        m_stackWindow.setEnabled(enable);
    }

    /**
     * Gets a grayscale pixel value, to test against a threshold.
     *
     * @param channel
     * @param x
     * @param y
     * @return unsigned byte expressed as an integer, 0...255
     */
    public int getPixel(int channel, int x, int y) {
        int returnValue = 0;
        //TODO this consistently results in "OutOfMemoryError: Java heap space"
        // getPixels calls getProcessor.
        // byte pixels[] = (byte [])m_imageStack.getPixels(channel + 1);
        byte pixels[] = m_saveOutPixels[channel];
        returnValue |= pixels[y * m_width + x] & 0xff;
        return returnValue;
    }
}

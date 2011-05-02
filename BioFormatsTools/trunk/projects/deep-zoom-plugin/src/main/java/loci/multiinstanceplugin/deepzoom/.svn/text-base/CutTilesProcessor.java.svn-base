//
// CutTilesProcessor.java
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
package loci.multiinstanceplugin.deepzoom;

import loci.chainableplugin.AbstractPipelineProcessor;
import loci.multiinstanceplugin.AbstractPlugin;
import loci.multiinstanceplugin.IPlugin;
import loci.plugin.ImageWrapper;
import loci.plugin.annotations.Input;
import loci.plugin.annotations.Output;

/**
 * A processor that takes an image and chops it up into tiles.
 *
 * @author Aivar Grislis
 */
@Input
@Output
public class CutTilesProcessor extends AbstractPlugin implements IPlugin
{
    public static final String X = CutTilesProcessor.class + "_X";
    public static final String Y = CutTilesProcessor.class + "_Y";
    private static final int DEFAULT_TILE_WIDTH = 256;
    private static final int DEFAULT_TILE_HEIGHT = 256;
    private static final int DEFAULT_OVERLAP = 1; //TODO was 0
    int m_tileWidth;
    int m_tileHeight;
    int m_overlap;

    /**
     * Default constructor.
     */
    public CutTilesProcessor()
    {
        m_tileWidth = DEFAULT_TILE_WIDTH;
        m_tileHeight = DEFAULT_TILE_HEIGHT;
        m_overlap = DEFAULT_OVERLAP;
    }

    /**
     * Constructor to specify width, height, and overlap of tiles.
     *
     * @param tileWidth
     * @param tileHeight
     * @param overlap
     */
    //TODO how do we specify these settings?
    //  note that constructor parameters are not a feature of existing IJ1 plugins
    public CutTilesProcessor(int tileWidth, int tileHeight, int overlap) {
        m_tileWidth = tileWidth;
        m_tileHeight = tileHeight;
        m_overlap = overlap;
    }

    public void process() {
        ImageWrapper image = get();

        String name = image.getName();
        int[] srcPixels = image.getPixels();
        int srcRowSize = image.getWidth();

        int yTileNo = 0;
        int remainingHeight = image.getHeight();

        while (remainingHeight > m_overlap)
        {
            int tileHeight = m_tileHeight + m_overlap;
            if (tileHeight > remainingHeight)
            {
                tileHeight = remainingHeight;
            }

            int ySrc = yTileNo * m_tileHeight;
            if (ySrc > 0)
            {
                ySrc -= m_overlap;
                tileHeight += m_overlap;
            }

            int xTileNo = 0;
            int remainingWidth = image.getWidth();

            while (remainingWidth > m_overlap)
            {
                int tileWidth = m_tileWidth + m_overlap;
                if (tileWidth > remainingWidth)
                {
                    tileWidth = remainingWidth;
                }

                int xSrc = xTileNo * m_tileWidth;
                if (xSrc > 0)
                {
                    xSrc -= m_overlap;
                    tileWidth += m_overlap;
                }

                ImageWrapper imageTile = new ImageWrapper(image, name + "_tile_" + xTileNo + '_' + yTileNo, tileWidth, tileHeight);

                imageTile.getProperties().set(X, new Integer(xTileNo));
                imageTile.getProperties().set(Y, new Integer(yTileNo));

                int srcIndex = xSrc + ySrc * srcRowSize;
                int[] dstPixels = imageTile.getPixels();
                copyPixels(tileWidth, tileHeight, srcIndex, srcPixels, srcRowSize, 0, dstPixels, tileWidth);

                // hand off the image tile
                put(imageTile);

                xTileNo++;
                remainingWidth -= m_tileWidth;
            }
            yTileNo++;
            remainingHeight -= m_tileHeight;
        }


    }

    /**
     * Does the image processing.
     *
     * @param image
     * @return status code
     */
    //TODO this is the old version; see above for latest
    public int processX(ImageWrapper image) {
        String name = image.getName();

        int[] srcPixels = image.getPixels();
        int srcRowSize = image.getWidth();

        int yTileNo = 0;
        int remainingHeight = image.getHeight();

        while (remainingHeight > m_overlap)
        {
            int tileHeight = m_tileHeight + m_overlap;
            if (tileHeight > remainingHeight)
            {
                tileHeight = remainingHeight;
            }

            int ySrc = yTileNo * m_tileHeight;
            if (ySrc > 0)
            {
                ySrc -= m_overlap;
                tileHeight += m_overlap;
            }

            int xTileNo = 0;
            int remainingWidth = image.getWidth();

            while (remainingWidth > m_overlap)
            {
                int tileWidth = m_tileWidth + m_overlap;
                if (tileWidth > remainingWidth)
                {
                    tileWidth = remainingWidth;
                }

                int xSrc = xTileNo * m_tileWidth;
                if (xSrc > 0)
                {
                    xSrc -= m_overlap;
                    tileWidth += m_overlap;
                }

                ImageWrapper imageTile = new ImageWrapper(image, name + "_tile_" + xTileNo + '_' + yTileNo, tileWidth, tileHeight);

                imageTile.getProperties().set(X, new Integer(xTileNo));
                imageTile.getProperties().set(Y, new Integer(yTileNo));

                int srcIndex = xSrc + ySrc * srcRowSize;
                int[] dstPixels = imageTile.getPixels();
                copyPixels(tileWidth, tileHeight, srcIndex, srcPixels, srcRowSize, 0, dstPixels, tileWidth);

                // hand off the image tile
         ////       nextInChainProcess(imageTile);
                xTileNo++;
                remainingWidth -= m_tileWidth;
            }
            yTileNo++;
            remainingHeight -= m_tileHeight;
        }
        return 0;
    }

    /**
     * Helper function, copies pixels from image to tile.
     *
     * @param width
     * @param height
     * @param srcIndex
     * @param srcPixels
     * @param srcRowSize
     * @param dstIndex
     * @param dstPixels
     * @param dstRowSize
     */
    void copyPixels(int width, int height, int srcIndex, int[] srcPixels, int srcRowSize, int dstIndex, int[] dstPixels, int dstRowSize)
    {
        for (int y = 0; y < height; y++) {
            int s = srcIndex;
            int d = dstIndex;
            for (int x = 0; x < width; x++) {
                dstPixels[(d++)] = srcPixels[(s++)];
            }
            srcIndex += srcRowSize;
            dstIndex += dstRowSize;
        }
    }
}
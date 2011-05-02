//
// BinNxN.java
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

package loci.slim.binning.plugins;

import loci.slim.binning.ISLIMBinner;

import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.cursor.LocalizableCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.outofbounds.OutOfBoundsStrategyMirrorFactory;
import mpicbg.imglib.type.numeric.RealType;

/**
 * Helper class, this is used by plugins to bin the fit input data.
 *
 * @author Aivar Grislis
 */
public class BinNxN<T extends RealType<T>> implements ISLIMBinner<T> {
    private int m_start;
    private int m_stop;

    /**
     * Constructor for a given size.
     *
     * @param size, should be odd, not enforced
     */
    public BinNxN(int size) {
        m_start = -size / 2;
        m_stop  =  size / 2;
    }

    /**
     * Does the binning.
     * 
     * @param sourceImage
     * @return binned image
     */
    public Image<T> bin(Image<T> sourceImage) {

        // create another image with same properties as incoming image
        Image<T> destImage = sourceImage.createNewImage();

        // create cursors for both images
        LocalizableByDimCursor<T> sourceCursor = sourceImage.createLocalizableByDimCursor(new OutOfBoundsStrategyMirrorFactory<T>());
        LocalizableCursor<T> destCursor = destImage.createLocalizableByDimCursor();

        double sum;
        int tmp[] = new int[sourceImage.getNumDimensions()];
        while (destCursor.hasNext()) {
            destCursor.fwd();
            destCursor.getPosition(tmp);
            sum = 0.0;
            for (int y = m_start; y <= m_stop; ++y) {
                for (int x = m_start; x <= m_stop; ++x) {
                    tmp[0] += x;
                    tmp[1] += y;
                    sourceCursor.setPosition(tmp);
                    sum += sourceCursor.getType().getRealDouble();
                }
            }
            destCursor.getType().setReal(sum);
        }
        return destImage;
    }
}

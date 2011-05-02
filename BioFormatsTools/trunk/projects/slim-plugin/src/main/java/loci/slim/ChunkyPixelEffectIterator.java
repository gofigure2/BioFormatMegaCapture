//
// ChunkyPixelEffectIterator.java
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

import java.lang.UnsupportedOperationException;
import java.util.Iterator;

/**
 * TODO
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/java/browser/trunk/projects/slim-plugin/src/main/java/loci/ChunkyPixelEffectIterator.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/java/trunk/projects/slim-plugin/src/main/java/loci/ChunkyPixelEffectIterator.java">SVN</a></dd></dl>
 *
 * @author Aivar Grislis grislis at wisc.edu
 */
public class ChunkyPixelEffectIterator implements Iterator {
    int m_width;
    int m_height;
    int m_index;
    int m_x;
    int m_y;
    IChunkyPixelTable m_table;
    ChunkyPixel m_chunkyPixel;
    
    public ChunkyPixelEffectIterator(IChunkyPixelTable table, int width, int height) {
        m_table = table;
        m_width = width;
        m_height = height;

        // initialize
        m_index = 0;
        m_x = 0;
        m_y = 0;

        // get first chunky pixel
        m_chunkyPixel = getNextChunkyPixel();
    }

    public boolean hasNext() {
        return m_chunkyPixel != null;
    }

    public ChunkyPixel next() {
        ChunkyPixel chunkyPixel = m_chunkyPixel;
        m_chunkyPixel = getNextChunkyPixel();
        return chunkyPixel;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    ChunkyPixel getNextChunkyPixel() {
        // get the relative chunky pixel from the table
        ChunkyPixel relChunkyPixel = m_table.getChunkyPixel(m_index);

        if (m_x + relChunkyPixel.getX() >= m_width) {
            // start next row
            m_x = 0;
            m_y += m_table.getHeight();

            if (m_y + relChunkyPixel.getY() >= m_height) {
                // use next table entry, are we done?
                if (++m_index >= m_table.size()) {
                    return null;
                }

                // start from the top
                m_y = 0;

                // update relative chunky pixel
                relChunkyPixel = m_table.getChunkyPixel(m_index);
            }
        }
        
        // convert relative to absolute
        int x = m_x + relChunkyPixel.getX();
        int y = m_y + relChunkyPixel.getY();
        ChunkyPixel absChunkyPixel = new ChunkyPixel(x, y,
                Math.min(relChunkyPixel.getWidth(), m_width - x),
                Math.min(relChunkyPixel.getHeight(), m_height - y));

        // set up for next call
        m_x += m_table.getWidth();

        return absChunkyPixel;
    }
}

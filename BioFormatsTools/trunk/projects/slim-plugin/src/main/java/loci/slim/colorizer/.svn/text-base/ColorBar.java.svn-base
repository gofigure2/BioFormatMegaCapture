//
// ColorBar.java
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

package loci.slim.colorizer;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

/**
 * Displays a color bar with the current colorization scheme.  Live,
 * reflects ongoing changes.
 *
 * @author Aivar Grislis grislis at wisc.edu
 */
public class ColorBar extends JPanel implements IColorizeRangeListener {
    final Object m_synchObject = new Object();
    int m_width;
    int m_height;
    IColorize m_colorize;
    double m_start;
    double m_stop;
    double m_min;
    double m_max;

    /**
     * Constructor
     *
     * @param width
     * @param height
     */
    public ColorBar(int width, int height, IColorize colorize) {
        super();
        
        m_width = width;
        m_height = height;
        m_colorize = colorize;
        
        setPreferredSize(new Dimension(width, height));

        m_start = m_stop = m_min = m_max = 0.0;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        synchronized (m_synchObject) {
            for (int i = 0; i < m_width; ++i) {
                g.setColor(m_colorize.colorize(m_start, m_stop, pixelToValue(i)));
                g.drawLine(i, 0, i, m_height-1);

            }
        }
    }

    /**
     * Called when any of these settings change.
     *
     * @param auto (ignored)
     * @param start
     * @param stop
     * @param min
     * @param max
     */
    public void setRange(boolean auto, double start, double stop, double min, double max) {
        boolean changed = false;
        synchronized (m_synchObject) {
            if (start != m_start) {
                m_start = start;
                changed = true;
            }
            if (stop != m_stop) {
                m_stop = stop;
                changed = true;
            }
            if (min != m_min) {
                m_min = min;
                changed = true;
            }
            if (max != m_max) {
                m_max = max;
                changed = true;
            }
        }
        if (changed) {
            repaint();
        }
    }

    private double pixelToValue(int x) {
        return (m_max * x) / (m_width - 1);
    }
}

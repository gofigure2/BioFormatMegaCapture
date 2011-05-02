//
// Histogram.java
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

/**
 * This class draws the lifetime histogram.  It also shows the start and
 * stop value for colorization.  User may drag start and stop bars to
 * affect colorization.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/java/browser/trunk/projects/slim-plugin/src/main/java/loci/colorizer/Histogram.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/java/trunk/projects/slim-plugin/src/main/java/loci/colorizer/Histogram.java">SVN</a></dd></dl>
 *
 * @author Aivar Grislis grislis at wisc.edu
 */
public class Histogram extends JPanel implements IColorizeRangeListener {
    static final int ONE_HEIGHT = 20;
    static final Object m_synchObject = new Object();
    private int m_width;
    private int m_height;
    private IColorizeRangeListener m_listener;
    int m_count[];
    private boolean m_auto;
    private double m_start;
    private double m_stop;
    private double m_min;
    private double m_max;

    /**
     * Constructor.
     *
     * @param width
     * @param height
     * @param listener called after start/stop bars are dragged.
     */
    public Histogram(int width, int height, IColorizeRangeListener listener) {
        super();
        m_width = width;
        m_height = height;
        m_listener = listener;

        setPreferredSize(new Dimension(width, height));

        m_auto = true;
        m_start = m_stop = m_min = m_max = 0.0;

        m_count = new int[width];
        for (int i = 0; i < width; ++i) {
            m_count[i] = 0;
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        synchronized (m_synchObject) {
            int max = 0;
            for (int i = 0; i < m_width; ++i) {
                if (m_count[i] > max) {
                    max = m_count[i];
                }
            }
            int height;
            for (int i = 0; i < m_width; ++i) {
                if (0 == m_count[i]) {
                    height = 0;
                }
                else if (1 == m_count[i]) {
                    height = ONE_HEIGHT;
                }
                else {
                    height = (int) ((m_height - ONE_HEIGHT) * Math.log(m_count[i]) / Math.log(max)) + ONE_HEIGHT;
                }
                if (height > m_height) {
                    height = m_height;
                }
                g.setColor(Color.WHITE);
                g.drawLine(i, 0, i, m_height - height);
                g.setColor(Color.DARK_GRAY);
                g.drawLine(i, m_height - height, i, m_height);
            }
            int x;
            g.setXORMode(Color.MAGENTA);
            x = valueToPixel(m_start);
            g.drawLine(x, 0, x, m_height - 1);
            x = valueToPixel(m_stop);
            g.drawLine(x, 0, x, m_height - 1);

        }
    }

    /*
        // catch drag events
        protected void processMouseMotionEvent(MouseEvent e, JXLayer<? extends V> l) {
            super.processMouseMotionEvent(e, l);
            if (e.getID() == MouseEvent.MOUSE_DRAGGED) {
                //Point point = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), l);
                if (m_draggingStartMarker || m_draggingStopMarker) {
                    double newProportion = getDraggedProportion(e);
                    if (m_draggingStartMarker) {
                        if (newProportion <= m_stopMarkerProportion) {
                            m_startMarkerProportion = newProportion;
                        }
                    }
                    else {
                        if (newProportion >= m_startMarkerProportion) {
                            m_stopMarkerProportion = newProportion;
                        }
                    }
                    // mark the ui as dirty and needed to be repainted
                    setDirty(true);
                }
            }
        }

        protected void processMouseEvent(MouseEvent e, JXLayer<? extends V> l) {
            super.processMouseEvent(e, l);
            if (e.getID() == MouseEvent.MOUSE_PRESSED) {
                int x = e.getX();
                int y = e.getY();
                if (y > m_y0 - CLOSE_ENOUGH && y < m_y1 + CLOSE_ENOUGH) { //4
                    if (Math.abs(x - m_xStart) < CLOSE_ENOUGH) {
                        // start dragging start line
                        m_draggingStartMarker = true;

                    }
                    else if (Math.abs(x - m_xStop) < CLOSE_ENOUGH) {
                        // start dragging stop line
                        m_draggingStopMarker = true;
                    }
                }
                System.out.println(" x " + e.getX() + " y " + e.getY());
            }
            if (e.getID() == MouseEvent.MOUSE_RELEASED) {
                m_draggingStartMarker = m_draggingStopMarker = false;
                SwingUtilities.invokeLater(
                        new Runnable() {
                            public void run() {
                                //System.out.println("RELEASED " + m_startMarkerProportion + " " + m_stopMarkerProportion);
                                m_listener.setStartStopProportion(m_startMarkerProportion, m_stopMarkerProportion);
                            }
                });*/

    /**
     * Keeps the histogram live as the fit progresses.
     *
     * @param lifetime
     * @param min
     * @param max
     */
    void updateData(double lifetime[], double min, double max) {
        //System.out.println("Histogram.updateData max is " + max);
        synchronized (m_synchObject) {

            // zero out counts
            for (int i = 0; i < m_width; ++i) {
                m_count[i] = 0;
            }

            for (int i = 0; i < lifetime.length; ++i) {
                if (lifetime[i] > 0.0) {
                    // find appropriate histogram count index
                    int n = (int) (m_width * lifetime[i] / max);
                    if (n >= m_width) {
                        n = m_width - 1;
                    }
                    // count this lifetime occurence
                    ++m_count[n];
                }
            }

            m_min = min;
            m_max = max;
            if (m_auto) {
                m_start = min;
                m_stop = max;
            }
        }
        repaint();
    }

    /**
     * Called when the UI changes any of these settings.  May also
     * be called as a consequence of self-induced changes.
     *
     * @param auto
     * @param start
     * @param stop
     */
    public void setRange(boolean auto, double start, double stop, double min, double max) {
        boolean redraw = false;
        synchronized (m_synchObject) {
            m_auto = auto;
            if (start != m_start) {
                m_start = start;
                redraw = true;
            }
            if (stop != m_stop) {
                m_stop = stop;
                redraw = true;
            }
            m_min = min;
            m_max = max;
        }
        if (redraw) {
            repaint();
        }
    }
    
    private int valueToPixel(double value) {
        return (int) (value * m_width / m_max);
       // return (int) ((m_width - 1) * value / m_max);
    }

    private double pixelToValue(int x) { //TODO from ColorBar
        return (m_max * x) / m_width; //TODO WAS: (m_width - 1);
    }

}

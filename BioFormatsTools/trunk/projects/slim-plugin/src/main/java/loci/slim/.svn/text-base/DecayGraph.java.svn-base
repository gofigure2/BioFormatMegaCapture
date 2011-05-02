//
// DecayGraph.java
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import loci.curvefitter.ICurveFitData;
import loci.slim.ui.IStartStopListener;

import org.jdesktop.jxlayer.JXLayer;
import org.jdesktop.jxlayer.plaf.AbstractLayerUI;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;

/**
 * TODO
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/java/browser/trunk/projects/slim-plugin/src/main/java/loci/DecayGraph.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/java/trunk/projects/slim-plugin/src/main/java/loci/DecayGraph.java">SVN</a></dd></dl>
 *
 * @author Aivar Grislis grislis at wisc.edu
 */
public class DecayGraph implements IStartStopProportionListener {
    static final int HORZ_TWEAK = 4;
    static final Color DECAY_COLOR = Color.BLUE;
    static final Color FITTED_COLOR = Color.MAGENTA;
    static final Color BACK_COLOR = Color.WHITE;
    static final Color START_COLOR = Color.BLUE.darker();
    static final Color STOP_COLOR = Color.RED.darker();
    static final Color BASE_COLOR = Color.GREEN.darker();
    JFrame m_frame;
    int m_start;
    int m_stop;
    int m_bins;
    StartStopDraggingUI<JComponent> m_startStopDraggingUI;
    IStartStopListener m_startStopListener;
    boolean m_headless = false;
    boolean m_logarithmic = true;
    XYPlot m_decaySubPlot;
    XYSeriesCollection m_decayDataset;
    XYSeriesCollection m_residualDataset;
    static ChartPanel m_panel;

    JFreeChart m_decayChart;
    JFreeChart m_residualsChart;

    /**
     * Creates a JFreeChart graph showing the decay curve.
     *
     * @param start time bin
     * @param stop time bin
     * @param bins number of bins
     * @param timeInc time increment per bin
     * @param data fitted data
     */
    DecayGraph(final String title, final int start, final int stop, final int bins, final double timeInc, ICurveFitData data) {
        m_start = start;
        m_stop = stop;
        m_bins = bins;

        // create the combined chart
        JFreeChart chart = createCombinedChart(start, stop, bins, timeInc, data); //TODO got ugly; rethink params, globals etc
        m_panel = new ChartPanel(chart, true, true, true, false, true);
        m_panel.setDomainZoomable(false);
        m_panel.setRangeZoomable(false);
        m_panel.setPreferredSize(new java.awt.Dimension(500, 270));
        
        // Add JXLayer to draw/drag start/stop bars
        JXLayer<JComponent> layer = new JXLayer<JComponent>(m_panel);
        m_startStopDraggingUI = new StartStopDraggingUI<JComponent>(m_panel, m_decaySubPlot, this);
        layer.setUI(m_startStopDraggingUI);

        // create a frame for the chart
        m_frame = new JFrame(title + " Fitted Decay Curve");
        m_frame.getContentPane().add(layer);
        m_frame.setSize(450, 450);
        m_frame.pack();

        // initialize the vertical bars that show start and stop time bins
        m_startStopDraggingUI.setStartStopValues(timeInc * start, timeInc * stop, timeInc * bins);
    }

    /**
     * Gets the chart JFrame
     *
     * @return JFrame
     */
    public JFrame getFrame() {
        return m_frame;
    }

    /**
     * Registers a single, external start/stop listener.
     * This receives new values of start and stop time bins.
     *
     * @param startStopListener
     */
    void setStartStopListener(IStartStopListener startStopListener) {
        m_startStopListener = startStopListener;
    }

    /**
     * Sets stop and start time bins, based on proportions 0.0..1.0.  This is called from
     * the UI layer that lets user drag the start and stop vertical bars.  Validates
     * and passes changes on to external listener.
     *
     * @param startProportion
     * @param stopProportion
     */
    public void setStartStopProportion(double startProportion, double stopProportion) {
        //System.out.println("getting notification of " + startProportion + " " + stopProportion);
        int start = (int) (startProportion * m_bins + 0.5);
        int stop = (int) (stopProportion * m_bins + 0.5);
        //System.out.println("start " + start + " stop " + stop);
        if (start != m_start || stop != m_stop) {
            // redraw UI on bin boundaries
         //TODO NO NO NO!!!     setStartStop(start, stop);
            if (null != m_startStopListener) {
                //System.out.println("NOTIFY LISTENER");
                m_startStopListener.setStartStop(start, stop);
            }
        }
    }

    /**
     * Creates the chart
     * 
     * @param start time bin
     * @param stop time bin
     * @param bins number of bins
     * @param timeInc time increment per bin
     * @param data fitted data
     * @return the chart
     */
    JFreeChart createCombinedChart(int start, int stop, int bins, double timeInc, ICurveFitData data) {

        // create chart data
        createDatasets(start, stop, bins, timeInc, data);

        // make a common horizontal axis for both sub-plots
        NumberAxis timeAxis = new NumberAxis("Time");
        timeAxis.setLabel("nanoseconds");
        timeAxis.setRange(0.0, (bins - 1) * timeInc);

        // make a vertically combined plot
        CombinedDomainXYPlot parent = new CombinedDomainXYPlot(timeAxis);

        // create decay sub-plot
        NumberAxis photonAxis;
        if (m_logarithmic) {
            photonAxis = new LogarithmicAxis("Photons");
        }
        else {
            photonAxis = new NumberAxis("Photons");
        }
       // photonAxis.setRange(0.0, 2000000.0);
        XYSplineRenderer decayRenderer = new XYSplineRenderer();
        decayRenderer.setSeriesShapesVisible(0, false);
        decayRenderer.setSeriesShapesVisible(1, false);
        decayRenderer.setSeriesLinesVisible(2, false);
        decayRenderer.setSeriesShape(2, new Ellipse2D.Float(2.0f, 2.0f, 2.0f, 2.0f)); // 1.5, 3.0 look ugly!

        decayRenderer.setSeriesPaint(0, Color.green);
        decayRenderer.setSeriesPaint(1, Color.red);
        decayRenderer.setSeriesPaint(2, Color.blue);

        m_decaySubPlot = new XYPlot(m_decayDataset, null, photonAxis, decayRenderer);
        m_decaySubPlot.setDomainCrosshairVisible(true);
        m_decaySubPlot.setRangeCrosshairVisible(true);

        // add decay sub-plot to parent
        parent.add(m_decaySubPlot, 4);

        // create residual sub-plot
        NumberAxis residualAxis = new NumberAxis("Residual");
        //TODO want to autorange it: residualAxis.setRange(-100.0, 100.0);
        XYSplineRenderer residualRenderer = new XYSplineRenderer();
        residualRenderer.setSeriesShapesVisible(0, false);
        residualRenderer.setSeriesPaint(0, Color.black);
        XYPlot residualSubPlot = new XYPlot(m_residualDataset, null, residualAxis, residualRenderer);
        residualSubPlot.setDomainCrosshairVisible(true);
        residualSubPlot.setRangeCrosshairVisible(true);
        residualSubPlot.setFixedLegendItems(null);

        // add residual sub-plot to parent
        parent.add(residualSubPlot, 1);

        // now make the top level JFreeChart
        JFreeChart chart = new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, parent, true);
        chart.removeLegend();

        return chart;
    }

    /**
     * Creates the data sets for the chart
     *
     * @param start time bin
     * @param stop time bin
     * @param bins number of time bins
     * @param timeInc time increment per time bin
     * @param data from the fit
     */
    private void createDatasets(int start, int stop, int bins, double timeInc, ICurveFitData data) {
        //TODO lamp function; comes from where?
        XYSeries series1 = new XYSeries("IRF");
   /*     series1.add(1.0, 1.0);
        series1.add(2.0, 4.0);
        series1.add(3.0, 3.0);
        series1.add(4.0, 5.0);
        series1.add(5.0, 5.0);
        series1.add(6.0, 7.0);
        series1.add(7.0, 7.0);
        series1.add(8.0, 8.0); */

        XYSeries series2 = new XYSeries("Fitted");
        XYSeries series3 = new XYSeries("Data");
        XYSeries series4 = new XYSeries("Residuals");


        double yData, yFitted;
        double xCurrent = 0;
        for (int i = 0; i < bins; ++i) {
            yData = data.getYCount()[i];
            // logarithmic plots can't handle <= 0.0
            series3.add(xCurrent, (yData > 0.0 ? yData : null));
            // are we in fitted region?
            if (start <= i && i <= stop) {
                // yes, show fitted curve and residuals
                yFitted = data.getYFitted()[i];
                // logarithmic plots can't handle <= 0.0
                if (yFitted > 0.0) {
                    series2.add(xCurrent, yFitted);
                    series4.add(xCurrent, yData - yFitted);
                }
                else {
                    series2.add(xCurrent, null);
                    series4.add(xCurrent, null);
                }
            }
            else {
                series2.add(xCurrent, null);
                series4.add(xCurrent, null);
            }
            xCurrent += timeInc;
        }

        m_decayDataset = new XYSeriesCollection();
        m_decayDataset.addSeries(series1);
        m_decayDataset.addSeries(series2);
        m_decayDataset.addSeries(series3);

        m_residualDataset = new XYSeriesCollection();
        m_residualDataset.addSeries(series4);
    }

    /**
     * UI which allows us to paint on top of the components, using JXLayer.
     *
     * @param <V> component
     */
    static class StartStopDraggingUI<V extends JComponent> extends AbstractLayerUI<V> {
        private static final int CLOSE_ENOUGH = 4; // pizels
        private ChartPanel m_panel;
        private XYPlot m_plot;
        private IStartStopProportionListener m_listener;
        boolean m_draggingStartMarker = false;
        boolean m_draggingStopMarker = false;
        private double m_startMarkerProportion = 0.25;
        private double m_stopMarkerProportion = 0.75;
        private int m_y0;
        private int m_y1;
        private int m_xStart;
        private int m_xStop;

        /**
         * Creates the UI.
         *
         * @param panel for the chart
         * @param plot within the chart
         * @param listener to be notified when user drags start/stop vertical bars
         */
        StartStopDraggingUI(ChartPanel panel, XYPlot plot, IStartStopProportionListener listener) {
            m_panel    = panel;
            m_plot     = plot;
            m_listener = listener;
        }

        void setStartStopValues(double startValue, double stopValue, double maxValue) {
            Rectangle2D area = getDataArea();
            double x = area.getX();
            double width = area.getWidth();
            if (0.1 > width) {
                m_startMarkerProportion = startValue / maxValue;
                m_stopMarkerProportion = stopValue / maxValue;
            }
            else {
                double minRepresentedValue = screenToValue((int) x);
                double maxRepresentedValue = screenToValue((int) (x + width));
                m_startMarkerProportion = (startValue - minRepresentedValue) / (maxRepresentedValue - minRepresentedValue);
                m_stopMarkerProportion = (stopValue - minRepresentedValue) / (maxRepresentedValue - minRepresentedValue);
            }
        }

        /**
         * Used to draw the start/stop vertical bars.
         *
         * Overrides 'paintLayer()', not 'paint()'.
         *
         * @param g2
         * @param l
         */
        @Override
        protected void paintLayer(Graphics2D g2, JXLayer<? extends V> l) {
            // this paints layer as is
            super.paintLayer(g2, l);

            // adjust to current size
            Rectangle2D area = getDataArea();
            double x = area.getX();
            m_y0 = (int) area.getY();
            m_y1 = (int) (area.getY() + area.getHeight());
            double width = area.getWidth();
            m_xStart = (int) Math.round(x + width * m_startMarkerProportion) + HORZ_TWEAK;
            m_xStop = (int) Math.round(x + width * m_stopMarkerProportion) + HORZ_TWEAK;

            // custom painting is here
            g2.setStroke(new BasicStroke(2f));
            g2.setXORMode(XORvalue(START_COLOR));
            g2.drawLine(m_xStart, m_y0, m_xStart, m_y1);
            g2.setXORMode(XORvalue(STOP_COLOR));
            g2.drawLine(m_xStop, m_y0, m_xStop, m_y1);
        }

        /**
         * Mouse listener, catches drag events
         *
         * @param e
         * @param l
         */
        protected void processMouseMotionEvent(MouseEvent e, JXLayer<? extends V> l) {
            super.processMouseMotionEvent(e, l);
            if (e.getID() == MouseEvent.MOUSE_DRAGGED) {
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

        private Color XORvalue(Color color) {
            int drawRGB = color.getRGB();
            int backRGB = BACK_COLOR.getRGB();
            return new Color(drawRGB ^ backRGB);
        }

        /**
         * Gets the currently dragged horizontal value as a proportion,
         * a value between 0.0 and 1.0.
         *
         * @param e
         * @return proportion
         */
        private double getDraggedProportion(MouseEvent e) {
            Rectangle2D dataArea = m_panel.getChartRenderingInfo().getPlotInfo().getDataArea();
            Rectangle2D area = getDataArea();
            double proportion = ((double) e.getX() - area.getX()) / area.getWidth();
            if (proportion < 0.0) {
                proportion = 0.0;
            }
            else if (proportion > 1.0) {
                proportion = 1.0;
            }
            return proportion;
        }

        /**
         * Mouse listener, catches mouse button events.
         * @param e
         * @param l
         */
        protected void processMouseEvent(MouseEvent e, JXLayer<? extends V> l) {
            super.processMouseEvent(e, l);
            if (e.getID() == MouseEvent.MOUSE_PRESSED) {
                int x = e.getX();
                int y = e.getY();
                if (y > m_y0 - CLOSE_ENOUGH && y < m_y1 + CLOSE_ENOUGH) {
                    if (Math.abs(x - m_xStart) < CLOSE_ENOUGH) {
                        // start dragging start line
                        m_draggingStartMarker = true;

                    }
                    else if (Math.abs(x - m_xStop) < CLOSE_ENOUGH) {
                        // start dragging stop line
                        m_draggingStopMarker = true;
                    }
                }
            }
            if (e.getID() == MouseEvent.MOUSE_RELEASED) {
                m_draggingStartMarker = m_draggingStopMarker = false;
                SwingUtilities.invokeLater(
                        new Runnable() {
                            public void run() {
                                m_listener.setStartStopProportion(m_startMarkerProportion, m_stopMarkerProportion);
                            }
                });
            }
        }

        /**
         * Gets the area of the chart panel.
         *
         * @return 2D rectangle area
         */
        private Rectangle2D getDataArea() {
            Rectangle2D dataArea = m_panel.getChartRenderingInfo().getPlotInfo().getDataArea();
            return dataArea;
        }

        /**
         * Converts screen x to chart x value.
         *
         * @param x
         * @return chart value
         */

        private double screenToValue(int x) {
            return m_plot.getDomainAxis().java2DToValue((double) x, getDataArea(), RectangleEdge.TOP);
        }
    }
}

/**
 * Used within DecayGraph, to get results from StartStopDraggingUI inner class.
 *
 * @author Aivar Grislis grislis at wisc.edu
 */
interface IStartStopProportionListener {
    public void setStartStopProportion(double startProportion, double stopProportion);
}

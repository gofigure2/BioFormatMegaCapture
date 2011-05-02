/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.slim.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
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
 *
 * @author Aivar Grislis
 */
public class ExcitationGraph implements IStartStopBaseProportionListener {
    static final int HORZ_TWEAK = 0; //TODO this was necessary for the fitted decay graph: 4;
    static final Color EXCITATION_COLOR = Color.BLACK;
    static final Color BACK_COLOR = Color.WHITE;
    static final Color START_COLOR = Color.BLUE.darker();
    static final Color STOP_COLOR = Color.RED.darker();
    static final Color BASE_COLOR = Color.GREEN.darker();
    JFrame m_frame;
    int m_start;
    int m_stop;
    float m_base;
    int m_bins;
    float m_count;
    StartStopBaseDraggingUI<JComponent> m_startStopBaseDraggingUI;
    IStartStopBaseListener m_startStopBaseListener;
    boolean m_headless = false;
    boolean m_logarithmic = false;
    XYPlot m_excitationPlot;
    XYSeriesCollection m_excitationDataset;
    XYSeriesCollection m_residualDataset;
    static ChartPanel m_panel;
    JXLayer<JComponent> m_layer;

    JFreeChart m_decayChart;
    JFreeChart m_residualsChart;

    /**
     * Creates a JFreeChart graph showing the excitation or instrument response
     * decay curve.
     *
     * @param start time bin
     * @param stop time bin
     * @param base count
     * @param bins number of bins
     * @param timeInc time increment per bin
     * @param values
     */
    ExcitationGraph(final int start, final int stop, final float base,
            final int bins, final double timeInc, float[] values) {
        m_start = start;
        m_stop = stop;
        m_base = base;
        m_bins = bins;
        m_count = 0.0f;
        // find maximum count
        for (float value : values) {
            if (value > m_count) {
                m_count = value;
            }
        }

        // create the chart
        JFreeChart chart = createChart(start, stop, bins, timeInc, values);
        ChartPanel chartPanel = new ChartPanel(chart, true, true, true, false, true);
        chartPanel.setDomainZoomable(false);
        chartPanel.setRangeZoomable(false);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));

        // Add JXLayer to draw/drag start/stop bars
        m_layer = new JXLayer<JComponent>(chartPanel);
        m_startStopBaseDraggingUI = new StartStopBaseDraggingUI<JComponent>(chartPanel, m_excitationPlot, this);
        m_layer.setUI(m_startStopBaseDraggingUI);

        // initialize the vertical bars that show start and stop time bins and
        // the horizontal bar with the base count.
        m_startStopBaseDraggingUI.setStartStopBaseValues(
                timeInc * start, timeInc * stop, timeInc * bins,
                base, m_count);
    }

    /**
     * Gets the chart JPanel
     *
     * @return JFrame
     */
    public JComponent getComponent() {
        return m_layer;
    }

    /**
     * Registers a single, external start/stop/base listener.
     * This receives new values of start and stop time bins and/or the base
     * count.
     *
     * @param startStopBaseListener
     */
    void setStartStopBaseListener(IStartStopBaseListener startStopBaseListener) {
        m_startStopBaseListener = startStopBaseListener;
    }

    /**
     * Sets stop and start time bins, based on proportions 0.0..1.0.  This is called from
     * the UI layer that lets user drag the start and stop vertical bars.  Validates
     * and passes changes on to external listener.
     *
     * @param startProportion
     * @param stopProportion
     */
    public void setStartStopBaseProportion(
            double startProportion, double stopProportion, double baseProportion) {
        int start = (int) (startProportion * m_bins + 0.5);
        int stop = (int) (stopProportion * m_bins + 0.5);
        int base = (int) (baseProportion * m_count + 0.5);
        //System.out.println("start " + start + " stop " + stop);
        if (start != m_start || stop != m_stop) {
            // redraw UI on bin boundaries
         //TODO NO NO NO!!!     setStartStop(start, stop);
            if (null != m_startStopBaseListener) {
                //System.out.println("NOTIFY LISTENER");
                m_startStopBaseListener.setStartStopBase(start, stop, base);
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
    JFreeChart createChart(int start, int stop, int bins, double timeInc, float[] values) {

        // create chart data
        createDataset(start, stop, bins, timeInc, values);

        // make a horizontal axis
        NumberAxis timeAxis = new NumberAxis("Time");
        timeAxis.setLabel("nanoseconds");
        timeAxis.setRange(0.0, (bins - 1) * timeInc);

        // make a vertical axis
        NumberAxis photonAxis;
        if (m_logarithmic) {
            photonAxis = new LogarithmicAxis("Photons");
        }
        else {
            photonAxis = new NumberAxis("Photons");
        }

        // make an excitation plot
        XYSplineRenderer excitationRenderer = new XYSplineRenderer();
        excitationRenderer.setSeriesShapesVisible(0, false);
        excitationRenderer.setSeriesPaint(0, EXCITATION_COLOR);

        m_excitationPlot = new XYPlot(m_excitationDataset, timeAxis, photonAxis, excitationRenderer);
        m_excitationPlot.setDomainCrosshairVisible(true);
        m_excitationPlot.setRangeCrosshairVisible(true);

        // now make the top level JFreeChart
        JFreeChart chart = new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, m_excitationPlot, true);
        chart.removeLegend();

        return chart;
    }

    /**
     * Creates the data set for the chart
     *
     * @param start time bin
     * @param stop time bin
     * @param bins number of time bins
     * @param timeInc time increment per time bin
     * @param data from the fit
     */
    private void createDataset(int start, int stop, int bins, double timeInc, float[] values) {
        XYSeries series = new XYSeries("Data");
        double yData, yFitted;
        double xCurrent = 0;
        for (int i = 0; i < bins; ++i) {
            yData = values[i];
            if (m_logarithmic) {
                // logarithmic plots can't handle <= 0.0
                series.add(xCurrent, (yData > 0.0 ? yData : null));
            }
            else {
                series.add(xCurrent, yData);
            }
            xCurrent += timeInc;
        }

        m_excitationDataset = new XYSeriesCollection();
        m_excitationDataset.addSeries(series);
    }

    /**
     * UI which allows us to paint on top of the components, using JXLayer.
     *
     * @param <V> component
     */
    static class StartStopBaseDraggingUI<V extends JComponent> extends AbstractLayerUI<V> {
        private static final int CLOSE_ENOUGH = 4; // pizels
        private ChartPanel m_panel;
        private XYPlot m_plot;
        private IStartStopBaseProportionListener m_listener;
        boolean m_draggingStartMarker = false;
        boolean m_draggingStopMarker = false;
        boolean m_draggingBaseMarker = false;
        private double m_startMarkerProportion = 0.25;
        private double m_stopMarkerProportion = 0.75;
        private double m_baseMarkerProportion = 0.25;
        private int m_x0;
        private int m_y0;
        private int m_x1;
        private int m_y1;
        private int m_xStart;
        private int m_xStop;
        private int m_yBase;

        /**
         * Creates the UI.
         *
         * @param panel for the chart
         * @param plot within the chart
         * @param listener to be notified when user drags start/stop/base bars
         */
        StartStopBaseDraggingUI(ChartPanel panel, XYPlot plot, IStartStopBaseProportionListener listener) {
            m_panel    = panel;
            m_plot     = plot;
            m_listener = listener;
        }

        void setStartStopBaseValues(double startValue, double stopValue, double maxHorzValue, double baseValue, double maxVertValue) {
            Rectangle2D area = getDataArea();
            double x = area.getX();
            double y = area.getY();
            double width = area.getWidth();
            double height = area.getHeight();

            if (0.1 > width) {
                m_startMarkerProportion = startValue / maxHorzValue;
                m_stopMarkerProportion = stopValue / maxHorzValue;
            }
            else {
                double minRepresentedValue = horzScreenToValue((int) x);
                double maxRepresentedValue = horzScreenToValue((int) (x + width));
                m_startMarkerProportion = (startValue - minRepresentedValue) / (maxRepresentedValue - minRepresentedValue);
                m_stopMarkerProportion = (stopValue - minRepresentedValue) / (maxRepresentedValue - minRepresentedValue);
            }

            if (0.1 > height) {
                m_baseMarkerProportion = baseValue / maxVertValue;
            }
            else {
                double minRepresentedValue = vertScreenToValue((int) y);
                double maxRepresentedValue = vertScreenToValue((int) (y + height));
                m_baseMarkerProportion = (baseValue - minRepresentedValue) / (maxRepresentedValue - minRepresentedValue);
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
            double y = area.getY();
            m_x0 = (int) area.getX();
            m_y0 = (int) area.getY();
            m_x1 = (int) (area.getX() + area.getWidth());
            m_y1 = (int) (area.getY() + area.getHeight());
            double width = area.getWidth();
            double height = area.getHeight();
            m_xStart = (int) Math.round(x + width * m_startMarkerProportion) + HORZ_TWEAK;
            m_xStop = (int) Math.round(x + width * m_stopMarkerProportion) + HORZ_TWEAK;
            m_yBase = (int) Math.round(y + height * (1 - m_baseMarkerProportion));

            // custom painting is here
            g2.setStroke(new BasicStroke(2f));
            g2.setXORMode(XORvalue(START_COLOR));
            g2.drawLine(m_xStart, m_y0, m_xStart, m_y1);
            g2.setXORMode(XORvalue(STOP_COLOR));
            g2.drawLine(m_xStop, m_y0, m_xStop, m_y1);
            g2.setXORMode(XORvalue(BASE_COLOR));
            g2.drawLine(m_x0, m_yBase, m_x1, m_yBase);
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
                    double newProportion = getHorzDraggedProportion(e);
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
                else if (m_draggingBaseMarker) {
                    m_baseMarkerProportion = getVertDraggedProportion(e);

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
        private double getHorzDraggedProportion(MouseEvent e) {
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
         * Gets the currently dragged vertical value as a proportion,
         * a value between 0.0 and 1.0.
         *
         * @param e
         * @return proportion
         */
        private double getVertDraggedProportion(MouseEvent e) {
            Rectangle2D dataArea = m_panel.getChartRenderingInfo().getPlotInfo().getDataArea();
            Rectangle2D area = getDataArea();
            //double proportion = ((double) e.getY() - area.getY()) / area.getHeight();
            double proportion = ((double) area.getY() + area.getHeight() - e.getY()) / area.getHeight();
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
                    else if (Math.abs(y - m_yBase) < CLOSE_ENOUGH) {
                        // start dragging base line
                        m_draggingBaseMarker = true;
                    }
                }
            }
            if (e.getID() == MouseEvent.MOUSE_RELEASED) {
                m_draggingStartMarker = m_draggingStopMarker = m_draggingBaseMarker = false;
                SwingUtilities.invokeLater(
                        new Runnable() {
                            public void run() {
                                m_listener.setStartStopBaseProportion(m_startMarkerProportion, m_stopMarkerProportion, m_baseMarkerProportion);
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
        private double horzScreenToValue(int x) {
            return m_plot.getDomainAxis().java2DToValue((double) x, getDataArea(), RectangleEdge.TOP);
        }

        /**
         * Converts screen y to chart y value.
         *
         * @param y
         * @return chart value
         */
        private double vertScreenToValue(int y) {
            return m_plot.getRangeAxis().java2DToValue((double) y, getDataArea(), RectangleEdge.LEFT);
        }
    }
}

/**
 * Used within ExcitationGraph, to get results from StartStopBaseDraggingUI
 * inner class.
 *
 * @author Aivar Grislis grislis at wisc.edu
 */
interface IStartStopBaseProportionListener {
    public void setStartStopBaseProportion(double startProportion, double stopProportion, double baseProportion);
}
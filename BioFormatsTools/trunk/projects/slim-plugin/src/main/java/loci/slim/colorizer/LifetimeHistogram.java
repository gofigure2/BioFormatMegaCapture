//
// LifetimeHistogram.java
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

import java.awt.Color;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * An experiment in using JFreeChart BarCharts for a histogram.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/java/browser/trunk/projects/slim-plugin/src/main/java/loci/colorizer/LifetimeHistogram.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/java/trunk/projects/slim-plugin/src/main/java/loci/colorizer/LifetimeHistogram.java">SVN</a></dd></dl>
 *
 * @author Aivar Grislis grislis at wisc.edu
 */
public class LifetimeHistogram {
    static final int HISTOGRAM_SIZE = 100;
    //HistogramDataset m_dataset;
    //JFreeChart m_histogram;
    ChartPanel m_chartPanel;
    DefaultCategoryDataset m_dataset;
    double m_max;

    public LifetimeHistogram() {
        m_dataset = initDataset();
//BarRenderer.setDefaultShadowsVisible(false);
        boolean legend = false;
        boolean tooltips = false;
        boolean url = false;
        JFreeChart chart = ChartFactory.createBarChart
                ("Histogram","Lifetime", "Count", m_dataset,
                 PlotOrientation.VERTICAL, legend, tooltips, url);
        LogarithmicAxis logScale = new LogarithmicAxis("Percentage (%)");

            logScale.setStrictValuesFlag(false);
            logScale.setRange(0.00, 100.0);
            logScale.setTickLabelsVisible(true);

            chart.getCategoryPlot().setRangeAxis(logScale);



        chart.setBackgroundPaint(Color.yellow);
        chart.getTitle().setPaint(Color.blue);
        CategoryPlot plot = chart.getCategoryPlot();
        //plot.setRangeGridlinePaint(Color.red);
        plot.setRangeGridlinePaint(Color.black);
        plot.getDomainAxis().setVisible(false);
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setDrawBarOutline(false);
        renderer.setShadowVisible(false);
        m_chartPanel = new ChartPanel(chart);
        m_chartPanel.setPreferredSize(new java.awt.Dimension(800, 300));


    /*    double lifetime[] = new double[0];
        m_dataset = new HistogramDataset();
        m_dataset.setType(HistogramType.RELATIVE_FREQUENCY);
        m_dataset.addSeries("Histogram", lifetime, HISTOGRAM_SIZE);
        String plotTitle = "Histogram";
        String xaxis = "number";
        String yaxis = "value";
        PlotOrientation orientation = PlotOrientation.VERTICAL;
        boolean show = false;
        boolean toolTips = false;
        boolean urls = false;
        m_histogram = ChartFactory.createHistogram( plotTitle, xaxis, yaxis,
                m_dataset, orientation, show, toolTips, urls);
        m_chartPanel = new ChartPanel(m_histogram, true, true, true, false, true);
        m_chartPanel.setDomainZoomable(false);
        m_chartPanel.setRangeZoomable(false);
        m_chartPanel.setPreferredSize(new java.awt.Dimension(256, 256));*/
    }

    JPanel getPanel() {
        return m_chartPanel;
    }

    private DefaultCategoryDataset initDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < HISTOGRAM_SIZE; ++i) {
            dataset.addValue(0, "0", "" + i);
        }
        return dataset;
    }

    public void updateData(double lifetime[], double max) {
        // create and zero out histogram counts
        int count[] = new int[HISTOGRAM_SIZE];
        for (int i = 0; i < count.length; ++i) {
            count[i] = 0;
        }

        for (int i = 0; i < lifetime.length; ++i) {
            if (lifetime[i] > 0.0) {
                // find appropriate histogram count index
                int n = (int) (HISTOGRAM_SIZE * lifetime[i] / max);
                if (n >= HISTOGRAM_SIZE) {
                    n = HISTOGRAM_SIZE - 1;
                }
                // count this lifetime occurence
                ++count[n];
            }
        }
        //TODO for now; accentuate the counts
        for (int i = 0; i < HISTOGRAM_SIZE; ++i) {
            m_dataset.setValue(count[i], "0", "" + i);
        }
    }
}

//
// UserInterfacePanel.java
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

package loci.slim.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.border.Border;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import ij.gui.GenericDialog;

import loci.slim.Excitation;
import loci.slim.ExcitationFileHandler;
import loci.slim.ui.IUserInterfacePanel.FitAlgorithm;
import loci.slim.ui.IUserInterfacePanel.FitFunction;
import loci.slim.ui.IUserInterfacePanel.FitRegion;
import loci.slim.analysis.SLIMAnalysis;
import loci.slim.binning.SLIMBinning;

/**
 * TODO
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/java/browser/trunk/projects/slim-plugin/src/main/java/loci/slim/ui/UserInterfacePanel.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/java/trunk/projects/slim-plugin/src/main/java/loci/slim/ui/UserInterfacePanel.java">SVN</a></dd></dl>
 *
 * @author Aivar Grislis grislis at wisc.edu
 */

public class UserInterfacePanel implements IUserInterfacePanel {
    // Unicode special characters
    private static final Character CHI    = '\u03c7';
    private static final Character SQUARE = '\u00b2';
    private static final Character TAU    = '\u03c4';
    private static final Character LAMBDA = '\u03bb';
    private static final Character SIGMA  = '\u03c3';
    private static final Character SUB_1  = '\u2081';
    private static final Character SUB_2  = '\u2082';
    private static final Character SUB_3  = '\u2083';
    
    private static final String SUM_REGION = "Sum All";
    private static final String ROIS_REGION = "Sum Each ROI";
    private static final String PIXEL_REGION = "Single Pixel";
    private static final String ALL_REGION = "Each Pixel";
    
    private static final String GRAY_RLD_ALGORITHM = "Gray NR RLD";
    private static final String GRAY_LMA_ALGORITHM = "Gray NR LMA";
    private static final String JAOLHO_LMA_ALGORITHM = "Jaolho LMA";
    private static final String SLIM_CURVE_RLD_ALGORITHM = "SLIMCurve RLD";
    private static final String SLIM_CURVE_LMA_ALGORITHM = "SLIMCurve LMA";
    private static final String SLIM_CURVE_RLD_LMA_ALGORITHM = "SLIMCurve RLD+LMA";

    private static final String SINGLE_EXPONENTIAL = "Single Exponential";
    private static final String DOUBLE_EXPONENTIAL = "Double Exponential";
    private static final String TRIPLE_EXPONENTIAL = "Triple Exponential";
    private static final String STRETCHED_EXPONENTIAL = "Stretched Exponential";

    private static final String EXCITATION_NONE = "None";
    private static final String EXCITATION_FILE = "File";
    private static final String EXCITATION_CREATE = "Use current X Y";
    
    private static final String DO_FIT = "Do Fit";
    private static final String CANCEL_FIT = "Cancel Fit";

    private static final Border EMPTY_BORDER = BorderFactory.createEmptyBorder(10, 10, 10, 10);
    private static final Border ETCHED_BORDER = BorderFactory.createEtchedBorder();

    private static final String REGION_ITEMS[] = { SUM_REGION, ROIS_REGION, PIXEL_REGION, ALL_REGION };
    private static final String ALGORITHM_ITEMS[] = { JAOLHO_LMA_ALGORITHM, GRAY_RLD_ALGORITHM, GRAY_LMA_ALGORITHM, SLIM_CURVE_RLD_ALGORITHM, SLIM_CURVE_LMA_ALGORITHM, SLIM_CURVE_RLD_LMA_ALGORITHM };
    private static final String FUNCTION_ITEMS[] = { SINGLE_EXPONENTIAL, DOUBLE_EXPONENTIAL, TRIPLE_EXPONENTIAL, STRETCHED_EXPONENTIAL };

    private static final String EXCITATION_ITEMS[] = { EXCITATION_NONE, EXCITATION_FILE, EXCITATION_CREATE };
    
    public IUserInterfacePanelListener m_listener;

    private ExcitationPanel m_excitationPanel;

    int m_fittedParameterCount = 0;

    // UI panel
    JPanel m_COMPONENT;
    JFrame m_frame;
    JPanel m_cardPanel;

    JComboBox m_regionComboBox;
    JComboBox m_algorithmComboBox;
    JComboBox m_functionComboBox;
    JComboBox m_analysisComboBox;
    JCheckBox m_fitAllChannels;

    // fit settings
    JTextField m_xField;
    JTextField m_yField;
    JTextField m_startField;
    JTextField m_stopField;
    JTextField m_thresholdField;
    JComboBox m_binningComboBox;
    JComboBox m_excitationComboBox;

    // parameter panel
    JPanel m_paramPanel;
    int m_paramPanelIndex;

    // single exponent fit
    JTextField m_aParam1;
    JCheckBox m_aFix1;
    JTextField m_tParam1;
    JCheckBox m_tFix1;
    JTextField m_zParam1;
    JCheckBox m_zFix1;
    JTextField m_chiSqParam1;
    JCheckBox m_startParam1;

    // double exponent fit
    JTextField m_a1Param2;
    JCheckBox m_a1Fix2;
    JTextField m_a2Param2;
    JCheckBox m_a2Fix2;
    JTextField m_t1Param2;
    JCheckBox m_t1Fix2;
    JTextField m_t2Param2;
    JCheckBox m_t2Fix2;
    JTextField m_zParam2;
    JCheckBox m_zFix2;
    JTextField m_chiSqParam2;
    JCheckBox m_startParam2;

    // triple exponent fit
    JTextField m_a1Param3;
    JCheckBox m_a1Fix3;
    JTextField m_a2Param3;
    JCheckBox m_a2Fix3;
    JTextField m_a3Param3;
    JCheckBox m_a3Fix3;
    JTextField m_t1Param3;
    JCheckBox m_t1Fix3;
    JTextField m_t2Param3;
    JCheckBox m_t2Fix3;
    JTextField m_t3Param3;
    JCheckBox m_t3Fix3;
    JTextField m_zParam3;
    JCheckBox m_zFix3;
    JTextField m_chiSqParam3;
    JCheckBox m_startParam3;

    // stretched exonent fit
    JTextField m_aParam4;
    JCheckBox m_aFix4;
    JTextField m_tParam4;
    JCheckBox m_tFix4;
    JTextField m_hParam4;
    JCheckBox m_hFix4;
    JTextField m_zParam4;
    JCheckBox m_zFix4;
    JTextField m_chiSqParam4;
    JCheckBox m_startParam4;

    JButton m_quitButton;
    JButton m_fitButton;

    public UserInterfacePanel(boolean showTau, String[] analysisChoices, String[] binningChoices) {
        String lifetimeLabel = "" + (showTau ? TAU : LAMBDA);

        m_frame = new JFrame("SLIM Plugin");

        // create outer panel
        JPanel outerPanel = new JPanel();
        outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.Y_AXIS));

        // create inner panel
        JPanel innerPanel = new JPanel();
        innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.X_AXIS));

        JPanel fitPanel = createFitPanel(analysisChoices);
        fitPanel.setBorder(border("Fit"));
        innerPanel.add(fitPanel);

        JPanel controlPanel = createControlPanel(binningChoices);
        controlPanel.setBorder(border("Control"));
        innerPanel.add(controlPanel);

        // Create cards and the panel that contains the cards
        m_cardPanel = new JPanel(new CardLayout());
        m_cardPanel.add(createSingleExponentialPanel(lifetimeLabel), SINGLE_EXPONENTIAL);
        m_cardPanel.add(createDoubleExponentialPanel(lifetimeLabel), DOUBLE_EXPONENTIAL);
        m_cardPanel.add(createTripleExponentialPanel(lifetimeLabel), TRIPLE_EXPONENTIAL);
        m_cardPanel.add(createStretchedExponentialPanel(lifetimeLabel), STRETCHED_EXPONENTIAL);
        m_cardPanel.setBorder(border("Params"));
        innerPanel.add(m_cardPanel);

        outerPanel.add(innerPanel);

        //Lay out the buttons from left to right.
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPanel.add(Box.createHorizontalGlue());
        m_quitButton = new JButton("Quit");
        m_quitButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    m_listener.quit();
                }
            }
        );
        buttonPanel.add(m_quitButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        m_fitButton = new JButton("Do Fit");
        m_fitButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String text = (String)e.getActionCommand();
                    if (text.equals("Do Fit")){
                        enableAll(false);
                        setFitButtonState(false);
                        if (null != m_listener) {
                            m_listener.doFit();
                        }
                    }
                    else{
                        setFitButtonState(true);
                        if (null != m_listener) {
                            m_listener.cancelFit();
                        }
                    }
                }
            }
        );
        buttonPanel.add(m_fitButton);

        outerPanel.add(buttonPanel);
        m_frame.getContentPane().add(outerPanel);

        m_frame.pack();
        final Dimension preferred = m_frame.getPreferredSize();
        m_frame.setMinimumSize(preferred);
        m_frame.addComponentListener(
                new ComponentAdapter() {
                    @Override
                    public void componentResized(ComponentEvent e) {
                        // allow horizontal but not vertical resize
                        int width = m_frame.getWidth();
                        if (width < (int) preferred.getWidth()) {
                            width = (int) preferred.getWidth();
                        }
                        m_frame.setSize(width, (int) preferred.getHeight());
                    }

        });
    }

    public JFrame getFrame() {
        return m_frame;
    }
    
    public void setListener(IUserInterfacePanelListener listener) {
        m_listener = listener;
    }

    public void reset() {
        enableAll(true);
        setFitButtonState(true);
    }

    private JPanel createFitPanel(String[] analysisChoices) {
        JPanel fitPanel = new JPanel();
        fitPanel.setBorder(new EmptyBorder(0, 0, 8, 8));
        fitPanel.setLayout(new SpringLayout());

        JLabel regionLabel = new JLabel("Region");
        regionLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        fitPanel.add(regionLabel);
        m_regionComboBox = new JComboBox(REGION_ITEMS);
     m_regionComboBox.setSelectedItem(ALL_REGION); // for demo
        fitPanel.add(m_regionComboBox);

        JLabel algorithmLabel = new JLabel("Algorithm");
        algorithmLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        fitPanel.add(algorithmLabel);
        m_algorithmComboBox = new JComboBox(ALGORITHM_ITEMS);
     m_algorithmComboBox.setSelectedItem(SLIM_CURVE_RLD_LMA_ALGORITHM);
        fitPanel.add(m_algorithmComboBox);

        JLabel functionLabel = new JLabel("Function");
        functionLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        fitPanel.add(functionLabel);
        m_functionComboBox = new JComboBox(FUNCTION_ITEMS);
        m_functionComboBox.addItemListener(
            new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    CardLayout cl = (CardLayout)(m_cardPanel.getLayout());
                    cl.show(m_cardPanel, (String)e.getItem());
                    reconcileStartParam();
                }
            }
        );
        fitPanel.add(m_functionComboBox);

        JLabel analysisLabel = new JLabel("Analysis");
        analysisLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        fitPanel.add(analysisLabel);
        m_analysisComboBox = new JComboBox(analysisChoices);
        fitPanel.add(m_analysisComboBox);

        // rows, cols, initX, initY, xPad, yPad
        SpringUtilities.makeCompactGrid(fitPanel, 4, 2, 4, 4, 4, 4);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add("North", fitPanel);

        m_fitAllChannels = new JCheckBox("Fit all channels");
        m_fitAllChannels.setSelected(true);

        panel.add("South", m_fitAllChannels);
        return panel;
    }

    float[] m_values = null;

    /*
     * Creates a panel that has some settings that control the fit.
     */
    private JPanel createControlPanel(String[] binningChoices) {
        JPanel controlPanel = new JPanel();
        controlPanel.setBorder(new EmptyBorder(0, 0, 8, 8));
        controlPanel.setLayout(new SpringLayout());

        JLabel xLabel = new JLabel("X");
        xLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        controlPanel.add(xLabel);
        m_xField = new JTextField(9);
        controlPanel.add(m_xField);

        JLabel yLabel = new JLabel("Y");
        yLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        controlPanel.add(yLabel);
        m_yField = new JTextField(9);
        controlPanel.add(m_yField);

        JLabel startLabel = new JLabel("Start");
        startLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        controlPanel.add(startLabel);
        m_startField = new JTextField(9);
        controlPanel.add(m_startField);

        JLabel stopLabel = new JLabel("Stop");
        stopLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        controlPanel.add(stopLabel);
        m_stopField = new JTextField(9);
        controlPanel.add(m_stopField);

        JLabel thresholdLabel = new JLabel("Threshold");
        thresholdLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        controlPanel.add(thresholdLabel);
        m_thresholdField = new JTextField(9);
        controlPanel.add(m_thresholdField);

        JLabel binningLabel = new JLabel("Bin");
        binningLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        controlPanel.add(binningLabel);
        m_binningComboBox = new JComboBox(binningChoices);
        controlPanel.add(m_binningComboBox);

        JLabel excitationLabel = new JLabel("Excitation");
        excitationLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        controlPanel.add(excitationLabel);
        m_excitationComboBox = new JComboBox(EXCITATION_ITEMS);
        m_excitationComboBox.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Excitation excitation = null;

                    String selectedItem = (String) m_excitationComboBox.getSelectedItem();
                    System.out.println("selected " + selectedItem);
                    if (EXCITATION_NONE.equals(selectedItem)) {
                        if (null != m_excitationPanel) {
                            m_excitationPanel.quit();
                            m_excitationPanel = null;
                        }
                    }
                    else if (EXCITATION_FILE.equals(selectedItem)) {
                        String fileName = getFileName("Load Excitation File", "");
                        if (null != fileName) {
                            excitation = ExcitationFileHandler.getInstance().loadExcitation(fileName);
                        }
                        if (null != excitation) {
                            m_values = excitation.getValues();
                        }
                    }
                    else if (EXCITATION_CREATE.equals(selectedItem)) {
                        String fileName = getFileName("Save Excitation File", "");
                        if (null != fileName) {
                            if (null != m_values) {
                                excitation = ExcitationFileHandler.getInstance().createExcitation(fileName, m_values);
                            }
                        }
                    }

                    if (null == excitation) {
                        m_excitationComboBox.setSelectedItem(EXCITATION_NONE);
                    }
                    else {
                        m_excitationComboBox.setSelectedItem(EXCITATION_FILE);
                        m_excitationPanel = new ExcitationPanel(excitation);
                    }
                }
            }
        );
        controlPanel.add(m_excitationComboBox);

        // rows, cols, initX, initY, xPad, yPad
        SpringUtilities.makeCompactGrid(controlPanel, 7, 2, 4, 4, 4, 4);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add("North", controlPanel);

        return panel;
    }

    /*
     * Creates panel for the single exponential version of the fit parameters.
     */
    private JPanel createSingleExponentialPanel(String lifetimeLabel) {
        JPanel expPanel = new JPanel();
        expPanel.setBorder(new EmptyBorder(0, 0, 8, 8));
        expPanel.setLayout(new SpringLayout());

        JLabel aLabel1 = new JLabel("A");
        aLabel1.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(aLabel1);
        m_aParam1 = new JTextField(9);
        //m_a1Param1.setEditable(false);
        expPanel.add(m_aParam1);
        m_aFix1 = new JCheckBox("Fix");
        //m_a1Fix1.addItemListener(this);
        expPanel.add(m_aFix1);

        JLabel t1Label1 = new JLabel(lifetimeLabel);
        t1Label1.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(t1Label1);
        m_tParam1 = new JTextField(9);
        //m_t1Param1.setEditable(false);
        expPanel.add(m_tParam1);
        m_tFix1 = new JCheckBox("Fix");
        //m_t1Fix1.addItemListener(this);
        expPanel.add(m_tFix1);

        JLabel zLabel1 = new JLabel("Z");
        zLabel1.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(zLabel1);
        m_zParam1 = new JTextField(9);
        //m_zParam1.setEditable(false);
        expPanel.add(m_zParam1);
        m_zFix1 = new JCheckBox("Fix");
        //m_zFix1.addItemListener(this);
        expPanel.add(m_zFix1);

        JLabel chiSqLabel1 = new JLabel("" + CHI + SQUARE);
        chiSqLabel1.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(chiSqLabel1);
        m_chiSqParam1 = new JTextField(9);
        m_chiSqParam1.setEditable(false);
        expPanel.add(m_chiSqParam1);
        JLabel nullLabel1 = new JLabel("");
        expPanel.add(nullLabel1);

        // SLIMPlotter look & feel:
        //Color fixColor = m_a1Param1.getBackground();
        //Color floatColor = a1Label1.getBackground();
        //m_a1Param1.setBackground(floatColor);
        //m_t1Param1.setBackground(floatColor);
        //m_zParam1.setBackground(floatColor);
        //m_chiSqParam1.setBackground(floatColor);

        // rows, cols, initX, initY, xPad, yPad
        SpringUtilities.makeCompactGrid(expPanel, 4, 3, 4, 4, 4, 4);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add("North", expPanel);

        m_startParam1 = new JCheckBox("Use as starting parameters for fit");
        m_startParam1.setSelected(true);
        m_startParam1.setEnabled(false);

        panel.add("South", m_startParam1);
        return panel;
    }

    /*
     * Creates panel for the double exponential version of the fit parameters.
     */
    private JPanel createDoubleExponentialPanel(String lifetimeLabel) {
        JPanel expPanel = new JPanel();
        expPanel.setBorder(new EmptyBorder(0, 0, 8, 8));
        expPanel.setLayout(new SpringLayout());

        JLabel a1Label2 = new JLabel("A" + SUB_1);
        a1Label2.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(a1Label2);
        m_a1Param2 = new JTextField(9);
        //m_a1Param2.setEditable(false);
        expPanel.add(m_a1Param2);
        m_a1Fix2 = new JCheckBox("Fix");
        //m_a1Fix2.addItemListener(this);
        expPanel.add(m_a1Fix2);

        JLabel t1Label2 = new JLabel(lifetimeLabel + SUB_1);
        t1Label2.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(t1Label2);
        m_t1Param2 = new JTextField(9);
        //m_t1Param2.setEditable(false);
        expPanel.add(m_t1Param2);
        m_t1Fix2 = new JCheckBox("Fix");
        //m_t1Fix2.addItemListener(this);
        expPanel.add(m_t1Fix2);

        JLabel a2Label2 = new JLabel("A" + SUB_2);
        a2Label2.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(a2Label2);
        m_a2Param2 = new JTextField(9);
        //m_a2Param2.setEditable(false);
        expPanel.add(m_a2Param2);
        m_a2Fix2 = new JCheckBox("Fix");
        //m_a2Fix2.addItemListener(this);
        expPanel.add(m_a2Fix2);

        JLabel t2Label2 = new JLabel(lifetimeLabel + SUB_2);
        t2Label2.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(t2Label2);
        m_t2Param2 = new JTextField(9);
        //m_t2Param2.setEditable(false);
        expPanel.add(m_t2Param2);
        m_t2Fix2 = new JCheckBox("Fix");
        //m_t2Fix2.addItemListener(this);
        expPanel.add(m_t2Fix2);

        JLabel zLabel2 = new JLabel("Z");
        zLabel2.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(zLabel2);
        m_zParam2 = new JTextField(9);
        //m_zParam2.setEditable(false);
        expPanel.add(m_zParam2);
        m_zFix2 = new JCheckBox("Fix");
        //m_zFix2.addItemListener(this);
        expPanel.add(m_zFix2);

        JLabel chiSqLabel2 = new JLabel("" + CHI + SQUARE);
        chiSqLabel2.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(chiSqLabel2);
        m_chiSqParam2 = new JTextField(9);
        //m_chiSqParam2.setEditable(false);
        expPanel.add(m_chiSqParam2);
        JLabel nullLabel2 = new JLabel("");
        expPanel.add(nullLabel2);

        // From SLIMPlotter
        //Color fixColor = m_a1Param2.getBackground();
        //Color floatColor = a1Label2.getBackground();
        //m_a1Param2.setBackground(floatColor);
        //m_t1Param2.setBackground(floatColor);
        //m_a2Param2.setBackground(floatColor);
        //m_t2Param2.setBackground(floatColor);
        //m_zParam2.setBackground(floatColor);
        //m_chiSqParam2.setBackground(floatColor);

        // rows, cols, initX, initY, xPad, yPad
        SpringUtilities.makeCompactGrid(expPanel, 6, 3, 4, 4, 4, 4);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add("North", expPanel);

        m_startParam2 = new JCheckBox("Use as starting parameters for fit");
        m_startParam2.setSelected(true);
        m_startParam2.setEnabled(false);
        panel.add("South", m_startParam2);
        return panel;
    }

    /*
     * Creates panel for the triple exponential version of the fit parameters.
     */
    private JPanel createTripleExponentialPanel(String lifetimeLabel) {
        JPanel expPanel = new JPanel();
        expPanel.setBorder(new EmptyBorder(0, 0, 8, 8));
        expPanel.setLayout(new SpringLayout());

        JLabel a1Label3 = new JLabel("A" + SUB_1);
        a1Label3.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(a1Label3);
        m_a1Param3 = new JTextField(9);
        //m_a1Param3.setEditable(false);
        expPanel.add(m_a1Param3);
        m_a1Fix3 = new JCheckBox("Fix");
        //m_a1Fix3.addItemListener(this);
        expPanel.add(m_a1Fix3);

        JLabel t1Label3 = new JLabel(lifetimeLabel + SUB_1);
        t1Label3.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(t1Label3);
        m_t1Param3 = new JTextField(9);
        //m_t1Param3.setEditable(false);
        expPanel.add(m_t1Param3);
        m_t1Fix3 = new JCheckBox("Fix");
        //m_t1Fix3.addItemListener(this);
        expPanel.add(m_t1Fix3);

        JLabel a2Label3 = new JLabel("A" + SUB_2);
        a2Label3.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(a2Label3);
        m_a2Param3 = new JTextField(9);
        //m_a2Param3.setEditable(false);
        expPanel.add(m_a2Param3);
        m_a2Fix3 = new JCheckBox("Fix");
        //m_a2Fix3.addItemListener(this);
        expPanel.add(m_a2Fix3);

        JLabel t2Label3 = new JLabel(lifetimeLabel + SUB_2);
        t2Label3.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(t2Label3);
        m_t2Param3 = new JTextField(9);
        //m_t2Param3.setEditable(false);
        expPanel.add(m_t2Param3);
        m_t2Fix3 = new JCheckBox("Fix");
        //m_t2Fix3.addItemListener(this);
        expPanel.add(m_t2Fix3);

        JLabel a3Label3 = new JLabel("A" + SUB_3);
        a3Label3.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(a3Label3);
        m_a3Param3 = new JTextField(9);
        //m_a3Param3.setEditable(false);
        expPanel.add(m_a3Param3);
        m_a3Fix3 = new JCheckBox("Fix");
        //m_a3Fix3.addItemListener(this);
        expPanel.add(m_a3Fix3);

        JLabel t3Label3 = new JLabel(lifetimeLabel + SUB_3);
        t3Label3.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(t3Label3);
        m_t3Param3 = new JTextField(9);
        //m_t3Param3.setEditable(false);
        expPanel.add(m_t3Param3);
        m_t3Fix3 = new JCheckBox("Fix");
        //m_t3Fix3.addItemListener(this);
        expPanel.add(m_t3Fix3);

        JLabel zLabel3 = new JLabel("Z");
        zLabel3.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(zLabel3);
        m_zParam3 = new JTextField(9);
        //m_zParam3.setEditable(false);
        expPanel.add(m_zParam3);
        m_zFix3 = new JCheckBox("Fix");
        //m_zFix3.addItemListener(this);
        expPanel.add(m_zFix3);


        JLabel chiSqLabel3 = new JLabel("" + CHI + SQUARE);
        chiSqLabel3.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(chiSqLabel3);
        m_chiSqParam3 = new JTextField(9);
        //m_chiSqParam3.setEditable(false);
        expPanel.add(m_chiSqParam3);
        JLabel nullLabel3 = new JLabel("");
        expPanel.add(nullLabel3);

        // SLIMPlotter look & feel:
        //Color fixColor = m_a1Param3.getBackground();
        //Color floatColor = a1Label3.getBackground();
        //m_a1Param3.setBackground(floatColor);
        //m_t1Param3.setBackground(floatColor);
        //m_a2Param3.setBackground(floatColor);
        //m_t2Param3.setBackground(floatColor);
        //m_a3Param3.setBackground(floatColor);
        //m_t3Param3.setBackground(floatColor);
        //m_zParam3.setBackground(floatColor);
        //m_chiSqParam3.setBackground(floatColor);

        // rows, cols, initX, initY, xPad, yPad
        SpringUtilities.makeCompactGrid(expPanel, 8, 3, 4, 4, 4, 4);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add("North", expPanel);

        m_startParam3 = new JCheckBox("Use as starting parameters for fit");
        m_startParam3.setSelected(true);
        m_startParam3.setEnabled(false);
        panel.add("South", m_startParam3);
        return panel;
    }

    /*
     * Creates panel for the stretched exponential version of the fit parameters.
     */
    private JPanel createStretchedExponentialPanel(String lifetimeLabel) {
        JPanel expPanel = new JPanel();
        expPanel.setBorder(new EmptyBorder(0, 0, 8, 8));
        expPanel.setLayout(new SpringLayout());

        JLabel a1Label4 = new JLabel("A");
        a1Label4.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(a1Label4);
        m_aParam4 = new JTextField(9);
        //m_a1Param1.setEditable(false);
        expPanel.add(m_aParam4);
        m_aFix4 = new JCheckBox("Fix");
        //m_a1Fix1.addItemListener(this);
        expPanel.add(m_aFix4);

        JLabel tLabel4 = new JLabel(lifetimeLabel);
        tLabel4.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(tLabel4);
        m_tParam4 = new JTextField(9);
        //m_t1Param1.setEditable(false);
        expPanel.add(m_tParam4);
        m_tFix4 = new JCheckBox("Fix");
        //m_t1Fix1.addItemListener(this);
        expPanel.add(m_tFix4);

        JLabel hLabel4 = new JLabel("H");
        hLabel4.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(hLabel4);
        m_hParam4 = new JTextField(9);
        //m_hParam4.setEditable(false);
        expPanel.add(m_hParam4);
        m_hFix4 = new JCheckBox("Fix");
        //m_hFix4.addItemListener(this);
        expPanel.add(m_hFix4);

        JLabel zLabel1 = new JLabel("Z");
        zLabel1.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(zLabel1);
        m_zParam4 = new JTextField(9);
        //m_zParam1.setEditable(false);
        expPanel.add(m_zParam4);
        m_zFix4 = new JCheckBox("Fix");
        //m_zFix1.addItemListener(this);
        expPanel.add(m_zFix4);

        JLabel chiSqLabel4 = new JLabel("" + CHI + SQUARE);
        chiSqLabel4.setHorizontalAlignment(SwingConstants.RIGHT);
        expPanel.add(chiSqLabel4);
        m_chiSqParam4 = new JTextField(9);
        m_chiSqParam4.setEditable(false);
        expPanel.add(m_chiSqParam4);
        JLabel nullLabel1 = new JLabel("");
        expPanel.add(nullLabel1);

        // SLIMPlotter look & feel:
        //Color fixColor = m_a1Param1.getBackground();
        //Color floatColor = a1Label1.getBackground();
        //m_a1Param1.setBackground(floatColor);
        //m_t1Param1.setBackground(floatColor);
        //m_zParam1.setBackground(floatColor);
        //m_chiSqParam1.setBackground(floatColor);

        // rows, cols, initX, initY, xPad, yPad
        SpringUtilities.makeCompactGrid(expPanel, 5, 3, 4, 4, 4, 4);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add("North", expPanel);

        m_startParam4 = new JCheckBox("Use as starting parameters for fit");
        m_startParam4.setSelected(true);
        m_startParam4.setEnabled(false);

        panel.add("South", m_startParam4);
        return panel;
    }

    private Border border(String title) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(ETCHED_BORDER, title),
                EMPTY_BORDER);
    }
    
    private void setFitButtonState(boolean on) {
        m_fitButton.setText(on ? DO_FIT : CANCEL_FIT);
    }
    
    private boolean getFitButtonState() {
        return m_fitButton.getText().equals(DO_FIT);
    }

    /*
     * Disables and enables UI during and after a fit.
     *
     * @param enable
     */
    private void enableAll(boolean enable) {
        // fit algorithm settings
        m_regionComboBox.setEnabled(enable);
        m_algorithmComboBox.setEnabled(enable);
        m_functionComboBox.setEnabled(enable);
        m_analysisComboBox.setEnabled(enable);
        m_fitAllChannels.setEnabled(enable);

        // fit control settings
        m_xField.setEditable(enable);
        m_yField.setEditable(enable);
        m_startField.setEditable(enable);
        m_stopField.setEditable(enable);
        m_thresholdField.setEditable(enable);
        m_binningComboBox.setEnabled(enable);

        // single exponent fit
        m_aParam1.setEditable(enable);
        m_aFix1.setEnabled(enable);
        m_tParam1.setEditable(enable);
        m_tFix1.setEnabled(enable);
        m_zParam1.setEditable(enable);
        m_zFix1.setEnabled(enable);

        // double exponent fit
        m_a1Param2.setEditable(enable);
        m_a1Fix2.setEnabled(enable);
        m_a2Param2.setEditable(enable);
        m_a2Fix2.setEnabled(enable);
        m_t1Param2.setEditable(enable);
        m_t1Fix2.setEnabled(enable);
        m_t2Param2.setEditable(enable);
        m_t2Fix2.setEnabled(enable);
        m_zParam2.setEditable(enable);
        m_zFix2.setEnabled(enable);

        // triple exponent fit
        m_a1Param3.setEditable(enable);
        m_a1Fix3.setEnabled(enable);
        m_a2Param3.setEditable(enable);
        m_a2Fix3.setEnabled(enable);
        m_a3Param3.setEditable(enable);
        m_a3Fix3.setEnabled(enable);
        m_t1Param3.setEditable(enable);
        m_t1Fix3.setEnabled(enable);
        m_t2Param3.setEditable(enable);
        m_t2Fix3.setEnabled(enable);
        m_t3Param3.setEditable(enable);
        m_t3Fix3.setEnabled(enable);
        m_zParam3.setEditable(enable);
        m_zFix3.setEnabled(enable);

        // stretched exonent fit
        m_aParam4.setEditable(enable);
        m_aFix4.setEnabled(enable);
        m_tParam4.setEditable(enable);
        m_tFix4.setEnabled(enable);
        m_hParam4.setEditable(enable);
        m_hFix4.setEnabled(enable);
        m_zParam4.setEditable(enable);
        m_zFix4.setEnabled(enable);

        if (enable) {
            reconcileStartParam();
        }
    }

    public FitRegion getRegion() {
        FitRegion region = null;
        String selected = (String) m_regionComboBox.getSelectedItem();
        if (selected.equals(SUM_REGION)) {
            region = FitRegion.SUMMED;
        }
        else if (selected.equals(ROIS_REGION)) {
            region = FitRegion.ROI;
        }
        else if (selected.equals(PIXEL_REGION)) {
            region = FitRegion.POINT;
        }
        else if (selected.equals(ALL_REGION)) {
            region = FitRegion.EACH;
        }
        return region;
    }

    public FitAlgorithm getAlgorithm() {
        FitAlgorithm algorithm = null;
        String selected = (String) m_algorithmComboBox.getSelectedItem();
        if (selected.equals(JAOLHO_LMA_ALGORITHM)) {
            algorithm = FitAlgorithm.JAOLHO;
        }
        else if (selected.equals(GRAY_RLD_ALGORITHM)) {
            algorithm = FitAlgorithm.BARBER2_RLD;
        }
        else if (selected.equals(GRAY_LMA_ALGORITHM)) {
            algorithm = FitAlgorithm.BARBER2_LMA;
        }
        else if (selected.equals(SLIM_CURVE_RLD_ALGORITHM)) {
            algorithm = FitAlgorithm.SLIMCURVE_RLD;
        }
        else if (selected.equals(SLIM_CURVE_LMA_ALGORITHM)) {
            algorithm = FitAlgorithm.SLIMCURVE_LMA;
        }
        else if (selected.equals(SLIM_CURVE_RLD_LMA_ALGORITHM)) {
            algorithm = FitAlgorithm.SLIMCURVE_RLD_LMA;
        }
        return algorithm;
    }

    public FitFunction getFunction() {
        FitFunction function = null;
        String selected = (String) m_functionComboBox.getSelectedItem();
        if (selected.equals(SINGLE_EXPONENTIAL)) {
            function = FitFunction.SINGLE_EXPONENTIAL;
        }
        else if (selected.equals(DOUBLE_EXPONENTIAL)) {
            function = FitFunction.DOUBLE_EXPONENTIAL;
        }
        else if (selected.equals(TRIPLE_EXPONENTIAL)) {
            function = FitFunction.TRIPLE_EXPONENTIAL;
        }
        else if (selected.equals(STRETCHED_EXPONENTIAL)) {
            function = FitFunction.STRETCHED_EXPONENTIAL;
        }
        return function;
    }

    public String getAnalysis() {
        String selected = (String) m_analysisComboBox.getSelectedItem();
        return selected;
    }

    public boolean getFitAllChannels() {
        return m_fitAllChannels.isSelected();
    }

    public int getX() {
        return parseInt(m_xField);
    }

    public void setX(int x) {
        m_xField.setText("" + x);
    }

    public int getY() {
        return parseInt(m_yField);
    }

    public void setY(int y) {
        m_yField.setText("" + y);
    }

    public int getStart() {
        return parseInt(m_startField);
    }

    public void setStart(int start) {
        m_startField.setText("" + start);
    }

    public int getStop() {
        return parseInt(m_stopField);
    }

    public void setStop(int stop) {
        m_stopField.setText("" + stop);
    }

    public int getThreshold() {
        return parseInt(m_thresholdField);
    }

    public void setThreshold(int threshold) {
        m_thresholdField.setText("" + threshold);
    }

    public String getBinning() {
        String selected = (String) m_binningComboBox.getSelectedItem();
        return selected;
    }

    public int getParameterCount() {
        int count = 0;
        String function = (String) m_functionComboBox.getSelectedItem();
        if (function.equals(SINGLE_EXPONENTIAL)) {
            count = 4;
        }
        else if (function.equals(DOUBLE_EXPONENTIAL)) {
            count = 6;
        }
        else if (function.equals(TRIPLE_EXPONENTIAL)) {
            count = 8;
        }
        else if (function.equals(STRETCHED_EXPONENTIAL)) {
            count = 5;
        }
        return count;
    }

    public void setFittedParameterCount(int count) {
        m_fittedParameterCount = count;
    }

    public double[] getParameters() {
        double parameters[] = null;
        String function = (String) m_functionComboBox.getSelectedItem();
        if (function.equals(SINGLE_EXPONENTIAL)) {
            parameters = new double[4];
            parameters[2] = Double.valueOf(m_aParam1.getText());
            parameters[3] = Double.valueOf(m_tParam1.getText());
            parameters[1] = Double.valueOf(m_zParam1.getText());
        }
        else if (function.equals(DOUBLE_EXPONENTIAL)) {
            parameters = new double[6];
            parameters[2] = Double.valueOf(m_a1Param2.getText());
            parameters[3] = Double.valueOf(m_t1Param2.getText());
            parameters[4] = Double.valueOf(m_a2Param2.getText());
            parameters[5] = Double.valueOf(m_t2Param2.getText());
            parameters[1] = Double.valueOf(m_zParam2.getText());
        }
        else if (function.equals(TRIPLE_EXPONENTIAL)) {
            parameters = new double[8];
            parameters[2] = Double.valueOf(m_a1Param3.getText());
            parameters[3] = Double.valueOf(m_t1Param3.getText());
            parameters[4] = Double.valueOf(m_a2Param3.getText());
            parameters[5] = Double.valueOf(m_t2Param3.getText());
            parameters[6] = Double.valueOf(m_a3Param3.getText());
            parameters[7] = Double.valueOf(m_t3Param3.getText());
            parameters[1] = Double.valueOf(m_zParam3.getText());
        }
        else if (function.equals(STRETCHED_EXPONENTIAL)) {
            parameters = new double[5];
            parameters[2] = Double.valueOf(m_aParam4.getText());
            parameters[3] = Double.valueOf(m_tParam4.getText());
            parameters[4] = Double.valueOf(m_hParam4.getText());
            parameters[1] = Double.valueOf(m_zParam4.getText());
        }
        parameters[0] = 0.0;
        return parameters;
    }

    public void setParameters(double params[]) {
        String function = (String) m_functionComboBox.getSelectedItem();
        if (function.equals(SINGLE_EXPONENTIAL)) {
            m_aParam1.setText    ("" + (float) params[2]);
            m_tParam1.setText    ("" + (float) params[3]);
            m_zParam1.setText    ("" + (float) params[1]);
            m_chiSqParam1.setText("" + (float) params[0]);
        }
        else if (function.equals(DOUBLE_EXPONENTIAL)) {
            m_a1Param2.setText   ("" + (float) params[2]);
            m_t1Param2.setText   ("" + (float) params[3]);
            m_a2Param2.setText   ("" + (float) params[4]);
            m_t2Param2.setText   ("" + (float) params[5]);
            m_zParam2.setText    ("" + (float) params[1]);
            m_chiSqParam2.setText("" + (float) params[0]);
        }
        else if (function.equals(TRIPLE_EXPONENTIAL)) {
            m_a1Param3.setText   ("" + (float) params[2]);
            m_t1Param3.setText   ("" + (float) params[3]);
            m_a2Param3.setText   ("" + (float) params[4]);
            m_t2Param3.setText   ("" + (float) params[5]);
            m_a3Param3.setText   ("" + (float) params[6]);
            m_t3Param3.setText   ("" + (float) params[7]);
            m_zParam3.setText    ("" + (float) params[1]);
            m_chiSqParam3.setText("" + (float) params[0]);
        }
        else if (function.equals(STRETCHED_EXPONENTIAL)) {
            m_aParam4.setText    ("" + (float) params[2]);
            m_tParam4.setText    ("" + (float) params[3]);
            m_hParam4.setText    ("" + (float) params[4]);
            m_zParam4.setText    ("" + (float) params[1]);
            m_chiSqParam4.setText("" + (float) params[0]);
        }
    }

    public void setFunctionParameters(int function, double params[]) {
        switch (function) {
            case 0:
                m_aParam1.setText    ("" + (float) params[2]);
                m_tParam1.setText    ("" + (float) params[3]);
                m_zParam1.setText    ("" + (float) params[1]);
                m_chiSqParam1.setText("" + (float) params[0]);
                break;
            case 1:
                m_a1Param2.setText   ("" + (float) params[2]);
                m_t1Param2.setText   ("" + (float) params[3]);
                m_a2Param2.setText   ("" + (float) params[4]);
                m_t2Param2.setText   ("" + (float) params[5]);
                m_zParam2.setText    ("" + (float) params[1]);
                m_chiSqParam2.setText("" + (float) params[0]);
                break;
            case 2:
                m_a1Param3.setText   ("" + (float) params[2]);
                m_t1Param3.setText   ("" + (float) params[3]);
                m_a2Param3.setText   ("" + (float) params[4]);
                m_t2Param3.setText   ("" + (float) params[5]);
                m_a3Param3.setText   ("" + (float) params[6]);
                m_t3Param3.setText   ("" + (float) params[7]);
                m_zParam3.setText    ("" + (float) params[1]);
                m_chiSqParam3.setText("" + (float) params[0]);
                break;
            case 3:
                m_aParam4.setText    ("" + (float) params[0]);
                m_tParam4.setText    ("" + (float) params[1]);
                m_hParam4.setText    ("" + (float) params[2]);
                m_zParam4.setText    ("" + (float) params[1]);
                m_chiSqParam4.setText("" + (float) params[0]);
                break;
        }
    }

    public boolean[] getFree() {
        boolean free[] = null;
        String function = (String) m_functionComboBox.getSelectedItem();
        if (function.equals(SINGLE_EXPONENTIAL)) {
            free = new boolean[3];
            free[0] = !m_aFix1.isSelected();
            free[1] = !m_tFix1.isSelected();
            free[2] = !m_zFix1.isSelected();
        }
        else if (function.equals(DOUBLE_EXPONENTIAL)) {
            free = new boolean[5];
            free[0] = !m_a1Fix2.isSelected();
            free[1] = !m_t1Fix2.isSelected();
            free[2] = !m_a2Fix2.isSelected();
            free[3] = !m_t2Fix2.isSelected();
            free[4] = !m_zFix2.isSelected();
        }
        else if (function.equals(TRIPLE_EXPONENTIAL)) {
            free = new boolean[7];
            free[0] = !m_a1Fix3.isSelected();
            free[1] = !m_t1Fix3.isSelected();
            free[2] = !m_a2Fix3.isSelected();
            free[3] = !m_t2Fix3.isSelected();
            free[4] = !m_a3Fix3.isSelected();
            free[5] = !m_t3Fix3.isSelected();
            free[6] = !m_zFix3.isSelected();

        }
        else if (function.equals(STRETCHED_EXPONENTIAL)) {
            free = new boolean[4];
            free[0] = !m_aFix4.isSelected();
            free[1] = !m_tFix4.isSelected();
            free[2] = !m_hFix4.isSelected();
            free[3] = !m_zFix4.isSelected();
        }
        return free;
    }

    public boolean refineFit() {
        JCheckBox checkBox = null;
        String function = (String) m_functionComboBox.getSelectedItem();
        if (function.equals(SINGLE_EXPONENTIAL)) {
            checkBox = m_startParam1;
        }
        else if (function.equals(DOUBLE_EXPONENTIAL)) {
            checkBox = m_startParam2;
        }
        else if (function.equals(TRIPLE_EXPONENTIAL)) {
            checkBox = m_startParam3;
        }
        else if (function.equals(STRETCHED_EXPONENTIAL)) {
            checkBox = m_startParam4; //TODO use an array of checkboxes, etc.
        }
        return !checkBox.isSelected();
    }

    private int parseInt(JTextField field) {
        int value = 0;
        try {
            value = Integer.parseInt(field.getText());
        }
        catch (NumberFormatException e) {
            System.out.println("Error parsing " + field.getName());
        }
        return value;
    }

    /**
     * This decides whether the existing parameters could be used as the
     * initial values for another fit.
     */
    private void reconcileStartParam() {
        // parameter counts happen to be unique for each fit function
        boolean enable = (m_fittedParameterCount == getParameterCount());
        m_startParam1.setEnabled(enable);
        m_startParam2.setEnabled(enable);
        m_startParam3.setEnabled(enable);
        m_startParam4.setEnabled(enable);
    }
    
    private String getFileName(String title, String defaultFileName) {
        GenericDialog dialog = new GenericDialog(title);
        //TODO works with GenericDialogPlus, dialog.addFileField("File:", defaultFile, 24);
        dialog.addStringField("File", defaultFileName);
        dialog.showDialog();
        if (dialog.wasCanceled()) {
            return null;
        }

        return dialog.getNextString();
    }
}

//
// InputPanel.java
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;

/**
 * The input panel allows the user to select/deselect automatic colorizer
 * range selection.  When automatic mode is off it allows the user to enter
 * minimum and maximum values.
 *
 * @author Aivar Grislis grislis at wisc.edu
 */
public class InputPanel extends JPanel implements IColorizeRangeListener {
    JCheckBox m_autoCheckBox;
    JTextField m_startTextField;
    JTextField m_stopTextField;
    boolean m_auto;
    double m_start;
    double m_stop;
    double m_min;
    double m_max;
    IColorizeRangeListener m_listener;

    /**
     * Constructor.  Passed in an initial state and a state change
     * listener.
     *
     * @param auto
     * @param start
     * @param stop
     */
    InputPanel(IColorizeRangeListener listener) {
        super();
        m_listener = listener;

        m_auto = true;
        m_start = m_stop = m_min = m_max = 0.0;

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        m_autoCheckBox = new JCheckBox("Auto", m_auto);
        m_autoCheckBox.addItemListener(
            new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    m_auto = m_autoCheckBox.isSelected();
                    if (m_auto) {
                        m_start = m_min;
                        m_startTextField.setText("" + m_start);

                        m_stop = m_max;
                        m_stopTextField.setText("" + m_stop);
                    }
                    enableAppropriately();
                    m_listener.setRange(m_auto, m_start, m_stop, m_min, m_max);
                }
            }
        );
        add(m_autoCheckBox);

        m_startTextField = new JTextField();
        m_startTextField.setText("" + m_start);
        m_startTextField.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    m_start = Double.parseDouble(m_startTextField.getText());
                    m_listener.setRange(m_auto, m_start, m_stop, m_min, m_max);
                }
            }
        );
        add(m_startTextField);

        m_stopTextField = new JTextField();
        m_startTextField.setText("" + m_stop);
        m_stopTextField.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    m_stop = Double.parseDouble(m_stopTextField.getText());
                    m_listener.setRange(m_auto, m_start, m_stop, m_min, m_max);
                }
            }
        );
        add(m_stopTextField);

        enableAppropriately();
    }

    /**
     * IColorizeRangeListener method.  Gets external changes to settings.
     *
     * @param auto
     * @param start
     * @param stop
     * @param min
     * @param max
     */
    public void setRange(boolean auto, double start, double stop, double min, double max) {
        if (auto != m_auto) {
            m_auto = auto;
            m_autoCheckBox.setSelected(auto);
            enableAppropriately();
        }
        
        if (start != m_start) {
            m_start = start;
            m_startTextField.setText("" + start);
        }

        if (stop != m_stop) {
            m_stop = stop;
            m_stopTextField.setText("" + stop);
        }
        m_min = min;
        m_max = max;
    }

    /**
     * Enable/disable start/stop text fields.
     */
    private void enableAppropriately() {
        m_startTextField.setEnabled(!m_auto);
        m_stopTextField.setEnabled(!m_auto);
    }
}

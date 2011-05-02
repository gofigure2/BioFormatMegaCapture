//
// MyStackWindow.java
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

import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.StackWindow;

import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

/**
 * A kludge because StackWindow has issues.
 *
 * @author Aivar Grislis
 */
public class MyStackWindow extends StackWindow {
    private int m_slice = 1;

    public MyStackWindow(ImagePlus imp) {
        super(imp);
        if (null != sliceSelector) {
            sliceSelector.addAdjustmentListener(
                new AdjustmentListener() {
                    public void adjustmentValueChanged(AdjustmentEvent e) {
                       if (e.getValue() != m_slice) {
                           //System.out.println("Show slice " + e.getValue());
                           m_slice = e.getValue();
                           //TODO this does affect the scrollbar, but not the ImagePlus!
                           showSlice(m_slice);
                       }
                    }
                }
            );

        }
    }

    public MyStackWindow(ImagePlus imp, ImageCanvas ic) {
	 super(imp, ic);
    }

    public int getSlice() {
        //TODO this approach did not work; StackWindow doesn't keep slice up to date.
        //return slice;
        return m_slice;
    }
}

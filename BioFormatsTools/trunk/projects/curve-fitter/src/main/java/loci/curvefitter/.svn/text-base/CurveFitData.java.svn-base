//
// CurveFitData.java
//

/*
Curve Fitter library for fitting exponential decay curves.

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

package loci.curvefitter;

/**
 * TODO
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/java/browser/trunk/projects/curve-fitter/src/main/java/loci/curvefitter/CurveFitData.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/java/trunk/projects/curve-fitter/src/main/java/loci/curvefitter/CurveFitData.java">SVN</a></dd></dl>
 *
 * @author Aivar Grislis grislis at wisc.edu
 */
public class CurveFitData implements ICurveFitData {
    int m_channel;
    int m_x;
    int m_y;
    double[] m_params;
    boolean[] m_free;
    double[] m_yCount;
    double[] m_sig;
    double[] m_yFitted;
    double m_chiSquare;
    Object m_userData;

    @Override
    public int getChannel() {
        return m_channel;
    }

    @Override
    public void setChannel(int channel) {
        m_channel = channel;
    }

    @Override
    public int getX() {
        return m_x;
    }

    @Override
    public void setX(int x) {
        m_x = x;
    }

    @Override
    public int getY() {
        return m_y;
    }

    @Override
    public void setY(int y) {
        m_y = y;
    }

    @Override
    public double[] getParams() {
        return m_params;
    }

    @Override
    public void setParams(double[] params) {
        m_params = params;
    }

    public boolean[] getFree() {
        return m_free;
    }

    public void setFree(boolean[] free) {
        m_free = free;
    }

    @Override
    public double[] getYCount() {
        return m_yCount;
    }

    @Override
    public void setYCount(double yCount[]) {
        m_yCount = yCount;
    }

    @Override
    public double[] getSig() {
        return m_sig;
    }

    @Override
    public void setSig(double sig[]) {
        m_sig = sig;
    }

    @Override
    public double[] getYFitted() {
        return m_yFitted;
    }

    @Override
    public void setYFitted(double yFitted[]) {
        m_yFitted = yFitted;
    }

    public double getChiSquare() {
        return m_chiSquare;
    }

    public void setChiSquare(double chiSquare) {
        m_chiSquare = chiSquare;
    }
}

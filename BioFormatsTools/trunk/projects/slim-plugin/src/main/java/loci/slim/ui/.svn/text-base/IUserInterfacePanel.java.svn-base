//
// IUserInterfacePanel.java
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

import javax.swing.JFrame;

/**
 * Interface to the User Interface Panel
 *
 * @author Aivar Grislis
 */
public interface IUserInterfacePanel {
    public static enum FitRegion {
        SUMMED, ROI, POINT, EACH
    }

    public static enum FitAlgorithm { //TODO not really algorithm, usu. LMA
       JAOLHO, /*AKUTAN,*/ BARBER_RLD, BARBER_LMA, MARKWARDT, BARBER2_RLD, BARBER2_LMA, SLIMCURVE_RLD, SLIMCURVE_LMA, SLIMCURVE_RLD_LMA
    }

    public static enum FitFunction {
        SINGLE_EXPONENTIAL, DOUBLE_EXPONENTIAL, TRIPLE_EXPONENTIAL, STRETCHED_EXPONENTIAL
    }


    /**
     * Gets the UI JFrame.
     *
     * @return JFrame
     */
    public JFrame getFrame();

    /**
     * Sets the listener
     */
    public void setListener(IUserInterfacePanelListener listener);

    /**
     * Resets the UI after a fit.
     */
    public void reset();

    /**
     * Gets region the fit applies to.
     *
     * @return fit region
     */
    public FitRegion getRegion();

    /**
     * Gets implementation & algorithm for the fit.
     *
     * @return fit algorithm.
     */
    public FitAlgorithm getAlgorithm();

    /**
     * Gets function to be fitted.
     *
     * @return fit function
     */
    public FitFunction getFunction();

    /**
     * Gets analysis plugin name.
     *
     * @return analysis plugin name
     */
    public String getAnalysis();

    /**
     * Gets whether or not to fit all channels.
     *
     * @return fit all channels
     */
    public boolean getFitAllChannels();

    /**
     * Gets starting bin of fit.
     *
     * @return start
     */
    public int getStart();

    /**
     * Sets starting bin of fit.
     *
     * @param bin
     */
    public void setStart(int bin);

    /**
     * Gets stopping bin of fit (inclusive).
     *
     * @return stop
     */
    public int getStop();

    /**
     * Sets stopping bin of fit.
     *
     * @param bin
     */
    public void setStop(int bin);

    /**
     * Gets photon count threshold to fit a pixel.
     *
     * @return threshold
     */
    public int getThreshold();

    /**
     * Sets photon count threshold to fit a pixel.
     *
     * @param threshold
     */
    public void setThreshold(int threshold);

    /**
     * Gets binning plugin name.
     *
     * @return binning plugin name
     */
    public String getBinning();

    /**
     * Gets pixel x.
     *
     * @return x
     */
    public int getX();

    /**
     * Sets pixel x.
     *
     * @param x
     */
    public void setX(int x);

    /**
     * Gets pixel y.
     *
     * @return y
     */
    public int getY();

    /**
     * Sets pixel y.
     *
     * @param y
     */
    public void setY(int y);

    /**
     * Gets number of fit parameters
     *
     * @return count
     */
    public int getParameterCount();

    /**
     * Sets number of fitted parameters after a successful fit.
     *
     * @param components
     */
    public void setFittedParameterCount(int count);

    /**
     * Gets the parameters of the fit
     *
     * @return fit parameters
     */
    public double[] getParameters();

    /**
     * Sets the parameters of the fit
     *
     * @param parameters
     */
    public void setParameters(double parameters[]);

    /**
     * Sets the parameters of the fit by function.
     *
     * @param function index
     * @param parameters
     */
    public void setFunctionParameters(int function, double parameters[]);

    /**
     * Gets which parameters aren't fixed.
     *
     * @return free parameters
     */
    public boolean[] getFree();

    /**
     * Gets whether to start next fit with results of last fit.
     *
     * @return
     */
    public boolean refineFit();
}

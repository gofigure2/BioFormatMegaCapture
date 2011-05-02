//
// JaolhoCurveFitter.java
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

import jaolho.data.lma.LMA;
import jaolho.data.lma.LMAFunction;

/**
 * Curve fitter that uses the Jaolho curve fitting package.
 *
 * @author Aivar Grislis
 */
public class JaolhoCurveFitter extends AbstractCurveFitter {

  @Override
  public int fitData(ICurveFitData[] dataArray, int start, int stop) {
    int goodPixels = 0;
    int badPixels = 0;
    double[][] lmaData;
    LMAFunction function;
    LMA lma;

    int length = stop - start + 1;
    lmaData = new double[2][length];
    double x_value = start * m_xInc;
    for (int i = 0; i < length; ++i) {
      lmaData[0][i] = x_value;
      x_value += m_xInc;
    }

    if (ICurveFitter.FitFunction.STRETCHED_EXPONENTIAL.equals(getFitFunction())) {
      System.out.println("Stretched exponentials not supported in Jaolho at this time.");
      return 0;
    }
    function = new ExpFunction(getNumberComponents());

    for (ICurveFitData data: dataArray) {
      double yData[] = data.getYCount();
      for (int i = 0; i < length; ++i) {
        lmaData[1][i] = yData[start + i];
      }

      double inParams[] = data.getParams();
      double params[] = new double[inParams.length - 1];
      switch (getNumberComponents()) {
        case 1:
          params[0] = inParams[2]; // A1
          params[1] = inParams[3]; // T1
          params[2] = inParams[1]; // C
          break;
        case 2:
          params[0] = inParams[2]; // A1
          params[1] = inParams[3]; // T1
          params[2] = inParams[4]; // A2
          params[3] = inParams[5]; // T2
          params[4] = inParams[1]; // C
          break;
        case 3:
          params[0] = inParams[2]; // A1
          params[1] = inParams[3]; // T1
          params[2] = inParams[4]; // A2
          params[3] = inParams[5]; // T2
          params[4] = inParams[6]; // A3
          params[5] = inParams[7]; // T3
          params[6] = inParams[1]; // C
          break;
      }
      lma = new LMA(function, params, lmaData);

      try {
        lma.fit();
        ++goodPixels;
      }
      catch (Exception e) {
        ++badPixels;
        System.out.println("exception " + e);
      }
      for (int i = 0; i < length; ++i) {
        data.getYFitted()[start + i] = function.getY(lmaData[0][i], lma.parameters);
      }
      double outParams[] = data.getParams();
      switch (getNumberComponents()) {
        case 1:
          outParams[0] = lma.chi2;
          outParams[1] = params[2]; // C
          outParams[2] = params[0]; // A1
          outParams[3] = params[1]; // T1
          break;
        case 2:
          outParams[0] = lma.chi2;
          outParams[1] = params[4]; // C
          outParams[2] = params[0]; // A1
          outParams[3] = params[1]; // T1
          outParams[4] = params[2]; // A2
          outParams[5] = params[3]; // T2
          break;
        case 3:
          outParams[0] = lma.chi2;
          outParams[1] = params[6]; // C
          outParams[2] = params[0]; // A1
          outParams[3] = params[1]; // T1
          outParams[4] = params[2]; // A2
          outParams[5] = params[3]; // T2
          outParams[6] = params[4]; // A3
          outParams[7] = params[5]; // T3
          break;
      }
    }
    //TODO error return deserves more thought
    return 0;
  }

  /**
   * A summed exponential function of the form:
   * y(t) = a1*e^(-b1*t) + ... + an*e^(-bn*t) + c.
   * <p>
   * From loci.slim.fit.LMCurveFitter by Curtis Rueden.
   */
  public static class ExpFunction extends LMAFunction {

    /** Number of exponentials to fit. */
    private int numExp = 1;

    /** Constructs a function with the given number of summed exponentials. */
    public ExpFunction(int num) { numExp = num; }

    @Override
    public double getY(double x, double[] a) {
      double sum = 0;
      //System.out.println("numExp is " + numExp);
      //System.out.println("length of a is " + a.length); TODO length of a is # timeBins! s/b # params
      for (int e=0; e<numExp; e++) {
        double aTerm = a[2 * e];
        double bTerm = a[2 * e + 1];
        sum += aTerm * Math.exp(-x / bTerm);
      }
      double cTerm = a[2 * numExp];
      sum += cTerm;
      return sum;
    }

    @Override
    public double getPartialDerivate(double x, double[] a, int parameterIndex) {
      if (parameterIndex == 2 * numExp) return 1; // c term
      int e = parameterIndex / 2;
      int off = parameterIndex % 2;
      double aTerm = a[2 * e];
      double bTerm = a[2 * e + 1];
      switch (off) {
        case 0:
          return Math.exp(-x / bTerm); // a term
        case 1:
          return -aTerm * x * Math.exp(-x / bTerm); // b term
      }
      throw new RuntimeException("No such parameter index: " +
        parameterIndex);
    }
  }

}

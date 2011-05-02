//
// AkutanCurveFitter.java
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

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;

import org.akutan.optimization.LMObjective;
import org.akutan.optimization.LMSolver;

/**
 * TODO
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/java/browser/trunk/projects/curve-fitter/src/main/java/loci/curvefitter/AkutanCurveFitter.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/java/trunk/projects/curve-fitter/src/main/java/loci/curvefitter/AkutanCurveFitter.java">SVN</a></dd></dl>
 *
 * @author Aivar Grislis grislis at wisc.edu
 */
public class AkutanCurveFitter extends AbstractCurveFitter {

    @Override
    public int fitData(ICurveFitData[] dataArray, int start, int stop) {
        int length = stop - start + 1;
        DoubleMatrix1D x = new DenseDoubleMatrix1D(length);
        DoubleMatrix1D y = new DenseDoubleMatrix1D(length);
        DoubleMatrix1D yFitted = new DenseDoubleMatrix1D(length);
        LMSolver lmSolver = new LMSolver();
        LMObjective lmObjective = new MyObjective(x, y, yFitted);

        // initialize x array (same for all data)
        double x_value = start * m_xInc;
        for (int i = 0; i < length; ++i) {
            x.set(i, x_value);
            x_value += m_xInc;
        }

        for (ICurveFitData data: dataArray) {
            // initialize y array
            double yCount[] = data.getYCount();
            for (int i = 0; i < length; ++i) {
                y.set(i, yCount[start + i]);
            }

            // find the solution
            DoubleMatrix1D solution = lmSolver.solve(lmObjective, x);

            // return params and yFitted
            double params[] = data.getParams();
            int paramsLength = params.length;
            for (int i = 0; i <  paramsLength; ++i) {
                params[i] = solution.get(i);
            }
            double yFittedX[] = data.getYFitted(); //TODO better name; was 'yFitted', collided with 'yFitted', formerly 'm_yFitted'
            for (int i = 0; i < length; ++i) {
                yFittedX[start + i] = yFitted.get(i);
            }
        }
        return 0;
    }
    
   /**
    * Inner class for LM optimization.
    */
    //TODO the LMObjective class could be passed in to the constructor, for different
    // flavors of curve fit criteria
    static class MyObjective implements LMObjective {
        int m_countDown = 50;
        boolean m_firstTime = true;
        boolean m_skipJacobean = false;
        DoubleMatrix1D m_x;
        DoubleMatrix1D m_y;
        DoubleMatrix1D m_yFitted;
        DoubleMatrix2D m_jacobean;
        int seq = 0;

        /**
         * Constructor
         *
         * @param x
         * @param y
         * @param yFitted
         */
        MyObjective(DoubleMatrix1D x, DoubleMatrix1D y, DoubleMatrix1D yFitted) {
            m_x = x;
            m_y = y;
            m_yFitted = yFitted;
            m_jacobean = new DenseDoubleMatrix2D(y.size(), 100); // specified rows, cols
        }

        /**
         * Computes the value we want to minimize for parameters a.<p>
         * Also computes 'm_jacobean', used by 'gradient()' and 'hessian()' methods.
         *
         * @param a parameters of the fit
         */
        @Override
        public double value(DoubleMatrix1D a) {

            if (0 > --m_countDown) {
              //  System.exit(0);
            }

            double A      = a.get(0);
            double lambda = a.get(1);
            double b      = a.get(2);

            if (0.0 == A && !m_firstTime) {
                m_skipJacobean = true;
            }
            else {
                m_skipJacobean = false;
            }

            double chiSq = 0.0;

            for (int i = 0; i < m_y.size(); ++i) {
                double tmpX = m_x.get(i);
                double e = Math.exp(-lambda * tmpX);
                double yFitted = A * e + b;
                m_yFitted.set(i, yFitted);
                chiSq += (m_y.get(i) - yFitted)*(m_y.get(i) - yFitted);

                if (!m_skipJacobean) {
                    m_jacobean.set(i, 0, e);
                    m_jacobean.set(i, 1, -A * tmpX * e);
                    m_jacobean.set(i, 2, 1.0);
                }
            }
            System.out.println("A " + A + " lambda " + lambda + " b " + b + " chiSq is " + chiSq + " " + ++seq);

            m_firstTime = false;
            
            return chiSq;
        }

        /**
         * Computes the gradient of the function for parameters a.<p>
         * Must be called after 'value()'.
         *
         * @param a
         * @return gradient
         */
        @Override
        public DoubleMatrix1D gradient(DoubleMatrix1D a) {
            DoubleMatrix1D g = new DenseDoubleMatrix1D(a.size());
            double betaSum;
            for (int j = 0; j < a.size(); ++j) {
                betaSum = 0.0;
                for (int k = 0; k < m_y.size(); ++k) {
                    //System.out.println("k " + k + " j " + j);
                    //System.out.println("jacobean " + m_jacobean.toString());
                    betaSum += (m_y.get(k) - m_yFitted.get(k)) * m_jacobean.get(k, j); //TODO s/b weighted by 1.0/(sigma[k]*sigma[k])
                }
                g.set(j, betaSum);
            }
            System.out.println("gradient " + seq);
            return g;
        }

        /**
         * Computes an exact or approximated Hessian of the function at a.<p>
         * Must be called after 'value()'.
         *
         * @param a
         * @return hessian
         */
        @Override
        public DoubleMatrix2D hessian(DoubleMatrix1D a) {
            DoubleMatrix2D H = new DenseDoubleMatrix2D(a.size(), a.size());
            for (int j = 0; j < a.size(); ++j) {
                for (int i = 0; i <= j; ++i) {
                    double dotProduct = 0.0;
                    for (int k = 0; k < m_y.size(); ++k) {
                        dotProduct += m_jacobean.get(k, i) * m_jacobean.get(k, j); //TODO s/b weighted by 1.0/(sigma[k]*sigma[k])
                    }
                    H.set(i, j, dotProduct);
                    if (i != j) {
                        H.set(j, i, dotProduct);
                    }
                }
            }
            System.out.println("hessian " + seq);
            return H;
        }
    }
    /*
			for (j = 0; j < nParams; ++j) {                                       // for all columns
				betaSum = 0.0;
				for (i = 0; i <= j; ++i) {                                        // row loop, need only consider lower triangle
					dotProduct = 0.0;
					for (k = 0; k < nData; ++k) {                                 // for all data
						weight = 1.0 / (sigma[k] * sigma[k]);                       // NOTE inefficiency here!  c/b computed one time only!
						dotProduct += J[k][i] * J[k][j] * weight;                 // get dot product of row i of J^T and col j of J
						if (0 == i) {                                             // once per column for every data index
							yDiff = y[k] - yCalc[k];                              // compute difference between data and model
							betaSum += yDiff * J[k][j] * weight;                 // accumulate approximate gradient
							if (0 == j) {                                         // once per data index
								chiSquare += (yDiff * yDiff) * weight;          // calculate chi square
							}
						}
					}
					alpha[i][j] = dotProduct;
					if (i != j) {
						alpha[j][i] = dotProduct;                                 // matrix is symmetrical
					}
				}
				beta[j] = betaSum;

     */
}

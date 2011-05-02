//
// MarkwardtCurveFitter.java
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

import com.sun.jna.Library;
import com.sun.jna.Native;

/**
 * TODO
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/java/browser/trunk/projects/curve-fitter/src/main/java/loci/curvefitter/MarkwardtCurveFitter.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/java/trunk/projects/curve-fitter/src/main/java/loci/curvefitter/MarkwardtCurveFitter.java">SVN</a></dd></dl>
 *
 * @author Aivar Grislis grislis at wisc.edu
 */
public class MarkwardtCurveFitter extends AbstractCurveFitter {
  int m_algType;

  public interface CLibrary extends Library {
    public int markwardt_fit(double x_incr, double y[], int fit_start, int fit_end,
      double param[], int param_free[], int n_param);
  }

  @Override
  public int fitData(ICurveFitData[] dataArray, int start, int stop) {
    CLibrary lib = (CLibrary) Native.loadLibrary("Markwardt", CLibrary.class);

    for (ICurveFitData data: dataArray) {
      double y[] = data.getYCount();
      int nParams = data.getParams().length;
      double params[] = data.getParams();
      int paramFree[] = new int[nParams];
      for (int i = 0; i < nParams; ++i) {
        paramFree[i] = 1;
      }

      int returnValue = lib.markwardt_fit(m_xInc, y, start, stop, params, paramFree, nParams);
    }

    //TODO error return deserves more thought
    return 0;
  }
}

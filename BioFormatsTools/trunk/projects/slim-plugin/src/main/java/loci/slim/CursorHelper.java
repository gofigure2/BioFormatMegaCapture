/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.slim;

import loci.curvefitter.CurveFitData;
import loci.curvefitter.ICurveFitData;
import loci.curvefitter.SLIMCurveFitter;

/**
 *
 * Based on TRI2 TRCursors.c.  Comments in quotes are from that source file.
 *
 * @author aivar
 */
public class CursorHelper {
    private static final int ATTEMPTS = 10;

    public static float[] estimateExcitationCursors(float[] excitation) {
        float baseline;
        int startp = 0;
        int endp = 0;
        float[] diffed = new float[excitation.length];
        int steepp;

        // "Estimate prompt baseline; very rough and ready"
        int index = findMax(excitation);
        float maxval = excitation[index];

        if (index > excitation.length * 3 /4) { // "integer arithmetic"
            baseline = 0.0f;
        }
        else {
            baseline = 0.0f;
            int index2 = (index + excitation.length) / 2;
            for (int i = index2; i < excitation.length; ++i) {
                baseline += excitation[i];
            }
            baseline /= (excitation.length - index2);
        }

        // "Where does the prompt first drop to (peak amplitude - baseline) / 10?
        // This could be silly if the baseline is silly; caveat emptor!"
        for (int i = index; i > 0; --i) {
            if ((excitation[i] - baseline) < 0.1 * (maxval - baseline)) {
                startp = i; // "First estimate"
                break;
            }
        }

        // "And first drop away again?"
        for (int i = index; i < excitation.length - 1; ++i) {
            if ((excitation[i] - baseline) < 0.1 * (maxval - baseline)) {
                endp = i;
                break;
            }
        }

        // "Differentiate"
        for (int i = 0; i < index; ++i) {
            diffed[i] = excitation[i + 1] - excitation[i];
        }

        // "Find the steepest rise"
        steepp = (int) findMax(diffed, index);
        if (steepp < startp) {
            startp = steepp;
        }

        // "One more sanity check"
        if (endp == startp) {
            if (endp == excitation.length) {
                --startp;
            }
            else {
                ++endp;
            }
        }

        float[] returnValue = new float[3];
        returnValue[0] = startp;
        returnValue[1] = endp;
        returnValue[2] = baseline;
        return returnValue;
    }

    public static float[] estimateCursors(float[] prompt, double[] decay) {
        float[] returnValue = new float[5];
        float baseline;
        int startp = 0;
        int startt = 0;
        int endp = 0;
        int endt = 0;
        float[] diffed = new float[prompt.length];
        int steepp;
        int steept;

        // "Estimate prompt baseline; very rough and ready"
        int index = findMax(prompt);
        float maxval = prompt[index];

        if (index > prompt.length * 3 /4) { // "integer arithmetic"
            baseline = 0.0f;
        }
        else {
            baseline = 0.0f;
            int index2 = (index + prompt.length) / 2;
            for (int i = index2; i < prompt.length; ++i) {
                baseline += prompt[i];
            }
            baseline /= (prompt.length - index2);
        }

        // "Where does the prompt first drop to (peak amplitude - baseline) / 10?
        // This could be silly if the baseline is silly; caveat emptor!"
        for (int i = index; i > 0; --i) {
            if ((prompt[i] - baseline) < 0.1 * (maxval - baseline)) {
                startp = i; // "First estimate"
                break;
            }
        }

        // "And first drop away again?"
        for (int i = index; i < prompt.length - 1; ++i) {
            if ((prompt[i] - baseline) < 0.1 * (maxval - baseline)) {
                endp = i;
                break;
            }
        }

        // "Differentiate"
        for (int i = 0; i < index; ++i) {
            diffed[i] = prompt[i + 1] - prompt[i];
        }

        // "Find the steepest rise"
        steepp = (int) findMax(diffed, index);
        if (steepp < startp) {
            startp = steepp;
        }

        // "One more sanity check"
        if (endp == startp) {
            if (endp == prompt.length) {
                --startp;
            }
            else {
                ++endp;
            }
        }

        // "Now do the same for the transient decay"
        index = findMax(decay);

        // "Differentiate"
        double[] diffedd = new double[decay.length];
        for (int i = 0; i < index; ++i) {
            diffedd[i] = decay[i + 1] - decay[i];
        }

        // "Find the steepest rise"
        steept = (int) findMax(diffedd, index);

        // "Make steep - start the same for both prompt and transient"
        startt = steept - (steepp - startp);
        if (startt < 0) {
            startt = 0;
        }

        // save estimates
        returnValue[0] = startp;
        returnValue[1] = endp;
        returnValue[2] = baseline;
        returnValue[3] = startt;
        returnValue[4] = endt; //TODO ITS JUST ZERO HERE!!

        // "Now we've got estimates we can do some Marquardt fitting to fine-tune
        // the estimates"

        int gTransStartIndex = startt - ATTEMPTS;
        if (gTransStartIndex < 0) {
            gTransStartIndex = 0;
        }
        int gTransEndIndex = 9 * decay.length / 10; // "90% of transient"
        if (gTransEndIndex <= gTransStartIndex + 2 * ATTEMPTS) { // "oops"
            return returnValue;
        }

        double[] param = new double[3];
        boolean[] free = new boolean[] { true, true, true };
        double[] yFitted = new double[decay.length];
        double[] chiSqTable = new double[2 * ATTEMPTS + 1];
        for (int i = 0; i < 2 * ATTEMPTS + 1; ++i, ++gTransStartIndex) {
            int fit_start = 0; // gTransFitStartIndex - gTransStartIndex;
            int fit_end = gTransEndIndex - gTransStartIndex;

            //int gTransFitStartIndex = gTransStartIndex;

            CurveFitData curveFitData = new CurveFitData();
            curveFitData.setParams(param);
            curveFitData.setFree(free);
            curveFitData.setYCount(decay);
            curveFitData.setSig(null);
            curveFitData.setYFitted(yFitted);
            CurveFitData[] data = new CurveFitData[] { curveFitData };

            SLIMCurveFitter curveFitter = new SLIMCurveFitter(SLIMCurveFitter.AlgorithmType.RLD_LMA);
            int ret = 0; //curveFitter.fitData(data, fitStart, fitStop);

            if (ret >= 0) {
                //System.out.println("for start " + fitStart + " stop " + fitStop + " chiSq is " + data[0].getChiSquare());
                chiSqTable[i] = data[0].getChiSquare();
            }
            else {
                chiSqTable[i] = 1e10f; // "silly value"
            }
        }

        // "Find the minimum chisq in this range"
        index = findMin(chiSqTable, 2 * ATTEMPTS + 1);
        if (chiSqTable[index] > 9e9f) {
            return null; //TODO do estimate resets/frees???
        }

        returnValue[0] = startp;
        returnValue[1] = endp;
        returnValue[2] = baseline;
        returnValue[3] = startt;
        returnValue[4] = endt; //TODO it's still zero!!
        return returnValue;
    }

    private static int findMax(float[] values) {
        return findMax(values, values.length);
    }

    private static int findMax(float[] values, int endIndex) {
        int index = 0;
        float max = 0.0f;
        for (int i = 0; i < endIndex; ++i) {
            if (values[i] > max) {
                max = values[i];
                index = i;
            }
        }
        return index;
    }

    private static int findMin(float[] values, int endIndex) {
        int index = 0;
        float min = Float.MAX_VALUE;
        for (int i = 0; i < endIndex; ++i) {
            if (values[i] < min) {
                min = values[i];
                index = i;
            }
        }
        return index;
    }

    private static int findMax(double[] values) {
        return findMax(values, values.length);
    }

    private static int findMax(double[] values, int endIndex) {
        int index = 0;
        double max = 0.0f;
        for (int i = 0; i < endIndex; ++i) {
            if (values[i] > max) {
                max = values[i];
                index = i;
            }
        }
        return index;
    }

    private static int findMin(double[] values, int endIndex) {
        int index = 0;
        double min = Double.MAX_VALUE;
        for (int i = 0; i < endIndex; ++i) {
            if (values[i] < min) {
                min = values[i];
                index = i;
            }
        }
        return index;
    }
}

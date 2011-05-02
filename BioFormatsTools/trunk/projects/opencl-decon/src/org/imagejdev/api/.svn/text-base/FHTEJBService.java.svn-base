/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.imagejdev.api;


public interface FHTEJBService {

    /**
     * Returns a hello string...
     * @return
     */
     public String getTestMessage();

     public float[] getSobel( int width, int height, float[] imageData );

     /**
     * Profile network throughput...
     * @return
     */
     public float[][] getProfileData(float[][] data);

     /**
     * Performs a single 3D FHT on the provided data
     **/
     public float[][] fht( int width, int height, int depth, float[][] data, boolean inverse );

     /**
     * Performs iterative 3d deconvolution with the provided data
     **/
     public float[][] getIterative3DDecon( float[][] dataAin, float[][] dataYin, boolean normalize, int bw, int bh, int bd, int kd, int kw, int kh, double filterX, double filterY, double filterZ, boolean dB, boolean wiener, boolean antiRing, int nIter,  float gamma, boolean detectDivergence, double changeThreshPercent);
}

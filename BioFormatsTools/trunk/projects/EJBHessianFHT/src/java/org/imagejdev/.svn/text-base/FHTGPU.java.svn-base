/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.imagejdev;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;

/**
 *
 * @author rick
 */

@Stateless
public class FHTGPU  {

    public float[][] fht(int width, int height, int depth, float[][] data, boolean inverse) throws Exception {
        try {
            //call the constructor
            ImageDeckFHT imageDeckFHT = new ImageDeckFHT(width, height, depth);
            imageDeckFHT.run(data, inverse);
            
        } catch (IOException ex) {
            Logger.getLogger(FHTGPU.class.getName()).log(Level.SEVERE, null, ex);
        }
    return data;
    }

    
}

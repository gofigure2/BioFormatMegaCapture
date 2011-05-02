//
// MultiColorColorize.java
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

import java.awt.Color;

/**
 * Abstract base class for colorizers that interpolate between colors.
 *
 * @author Aivar Grislis
 */
public abstract class MultiColorColorize implements IColorize {

    /**
     * Given starting and ending colors and a value between 0.0 and 1.0,
     * interpolates the color.
     *
     * @param start
     * @param stop
     * @param value
     * @return interpolated color
     */
    public abstract Color colorize(double start, double stop, double value);

    /**
     * Returns a color bar or ramp of given number of pixels.
     *
     * @param pixels
     * @return
     */
    public Color[] bar(int pixels) {
        Color returnColors[] = new Color[pixels];
        for (int i = 0; i < pixels; ++i) {
            returnColors[i] = colorize(0, pixels, i);
        }
        return returnColors;
    }

    /**
     * Helper routine that interpolates between two colors.
     *
     * @param start
     * @param end
     * @param blend
     * @return interpolated color
     */
    Color interpolateColor(Color start, Color end, double blend) {
        int startRed   = start.getRed();
        int startGreen = start.getGreen();
        int startBlue  = start.getBlue();
        int endRed   = end.getRed();
        int endGreen = end.getGreen();
        int endBlue  = end.getBlue();
        int red   = interpolateColorComponent(startRed, endRed, blend);
        int green = interpolateColorComponent(startGreen, endGreen, blend);
        int blue  = interpolateColorComponent(startBlue, endBlue, blend);
        Color returnColor = Color.BLACK;
        try {
            returnColor = new Color(red, green, blue);
        }
        catch (Exception e) {
            System.out.println("Exception " + e + " " + red + " " + green + " " + blue);
        }
        return returnColor;
    }

    /**
     * Helper routine that interpolates one channel of color.
     *
     * @param start
     * @param end
     * @param blend
     * @return interpolated channel value
     */
    private int interpolateColorComponent(int start, int end, double blend) {
        return (int)(blend * (end - start) + start);
    }
}

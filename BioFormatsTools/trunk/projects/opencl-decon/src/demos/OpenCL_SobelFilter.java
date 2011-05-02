package demos;

import java.io.IOException;

import org.imagejdev.api.StreamToString;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

/**
 * Demonstrates running of OpenCL from ImageJ as a Plugin
 * 
 * @author rick
 * 
 */
public class OpenCL_SobelFilter implements PlugInFilter {

	private ImagePlus imagePlus;  // image plus object needed for processing
    private String argumentString;  // input arguments 
    private String openCLString;
    
	@Override
	public void run( ImageProcessor imageProcessor ) 
	{
		// get a backing float array from the ImageProcessor
		float[] imageFloatData = (float[]) imageProcessor.convertToFloat().getPixels();
		
		// run the OpenCL Filter Example
		SobelFilterExample sobelFilterExample = null;
		try {
			sobelFilterExample = new SobelFilterExample().init( imageProcessor.getWidth(), imageProcessor.getHeight(), false, openCLString );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		sobelFilterExample.run( imageFloatData );
	}

	@Override
	public int setup( String argumentString, ImagePlus imagePlus ) 
	{	
		// get the OpenCL code string
		try {
			this.openCLString = StreamToString.getString( SobelFilterExample.class.getResourceAsStream("sobel.cl"), true );
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.argumentString = argumentString;
		this.imagePlus = imagePlus;
		if (imagePlus == null) {
			IJ.noImage();
			return DONE;
		}
		
		//return the types of images supported
		return DOES_ALL;
	}

}

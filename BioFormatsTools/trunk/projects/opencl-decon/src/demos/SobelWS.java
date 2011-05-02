package demos;

/* Paste into the Script Editor, or save as Duplicate_and_Scale.java into the plugins folder */
import java.awt.Image;
import java.awt.image.PixelGrabber;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.imagejdev.api.FHTEJBService;

import com.caucho.hessian.client.HessianProxyFactory;

import ij.plugin.PlugIn;
import ij.IJ;
import ij.ImagePlus;
 
/** Duplicate and scale the current image. */
public class SobelWS implements PlugIn {
 
	/** Ask for parameters and then execute.*/
	public void run(String arg) 
	{
		// 1 - Obtain the currently active image:
		ImagePlus imp = IJ.getImage();
		if (null == imp) return;
 
		// 4 - Execute!
		float[] results = null;
		try {
			String url = "http://ws.cbios.co:8080/EJBHessianFHT/FHTEJBService";

			// Class loader switch
			ClassLoader bundleClassLoader = this.getClass().getClassLoader();
			ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
		
			// Set class loader
			Thread.currentThread().setContextClassLoader(bundleClassLoader);

			// invoke my code
			HessianProxyFactory factory = new HessianProxyFactory();
			FHTEJBService fhtejbservice = (FHTEJBService) factory.create(FHTEJBService.class, url);
	
			// convert to awt image
			Image image = imp.getImage();
			int testWidth = imp.getWidth();
			int testHeight = imp.getHeight();
			
			//grab the pixels
			int[] inputImage = new int[testWidth*testHeight];
			PixelGrabber pixelGrabber = new PixelGrabber( image, 0, 0, testWidth, testHeight, inputImage, 0, testWidth );
			try {
				pixelGrabber.grabPixels();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			//Convert Image to float
			float[] testImage = new float[testWidth*testHeight];
			for( int i = 0; i < testWidth; i++ )
				for( int j = 0; j < testHeight; j++ )
					testImage[ i*testHeight+j ] = getAvg( inputImage[ i*testHeight+j ]);
			results = fhtejbservice.getSobel( imp.getWidth(), imp.getHeight(), testImage );

			
			// switch classloader back
			Thread.currentThread().setContextClassLoader(originalClassLoader);
		} catch (MalformedURLException ex) {
			Logger.getLogger(Iterative3DDeconWS.class.getName()).log(
					Level.SEVERE, null, ex);
		}
 
		// 5 - If all went well, show the image:
		ij.process.FloatProcessor  floatProcessor = new ij.process.FloatProcessor( imp.getWidth(), imp.getHeight() );
		// set the pixels
		floatProcessor.setPixels( results );	
		ImagePlus scaled = new ImagePlus( "Sobel", floatProcessor );
			scaled.show();
	}
	
	
	public static float getAvg( int c ) {
		int r = (c&0xff0000) >> 16;
		int g = (c&0xff00) >> 8;
			int b = c&0xff;
			return (r + g + b)/3;
	}

}
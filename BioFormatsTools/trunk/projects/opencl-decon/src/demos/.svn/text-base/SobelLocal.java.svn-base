package demos;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;

import java.awt.Image;
import java.awt.image.PixelGrabber;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice.Type;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLMemory.Mem;
import com.jogamp.opencl.CLPlatform;
import com.jogamp.opencl.CLProgram;

public class SobelLocal implements PlugIn {
	
	
	public void run(String arg) 
	{
		// this is used to get detailed program level debug statements to the console
		boolean DEBUG = true;
		
		// **CHANGE THESE - Platform specific locations per getting started document at:
		// https://docs.google.com/document/d/1y_psquVas0bALfpO8GwvPGFrZ64V_fZl9Sz6CfJ4D2k/edit?hl=en
		if (DEBUG) { System.out.println( "Loading the OpenCL native libraries"); }
		System.load("/Applications/Fiji.app/plugins/libJOCL-apple-x86_64.dylib");
		System.load("/Applications/Fiji.app/plugins/libjocl.dylib");
		System.load("/Applications/Fiji.app/plugins/libgluegen-rt.dylib");
		
		
		if (DEBUG) { System.out.println( "Obtain the currently active image" ); }
		ImagePlus imp = IJ.getImage();
		if (null == imp)
			return;

		if (DEBUG) { System.out.println( "Convert to an awt.Image image"); }
		Image image = imp.getImage();
		int imageWidth = imp.getWidth();
		int imageHeight = imp.getHeight();

		if (DEBUG) { System.out.println( "Grab the pixels"); }
		int[] inputImage = new int[imageWidth * imageHeight];
		PixelGrabber pixelGrabber = new PixelGrabber(image, 0, 0, imageWidth,
				imageHeight, inputImage, 0, imageWidth);
		try {
			pixelGrabber.grabPixels();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if (DEBUG) { System.out.println( "Convert Image to float"); }
		float[] testImage = new float[imageWidth * imageHeight];
		for (int i = 0; i < imageWidth; i++)
			for (int j = 0; j < imageHeight; j++)
				testImage[i * imageHeight + j] = getAvg(inputImage[i* imageHeight + j]);

		if (DEBUG) { System.out.println( "Getting the OpenCL code"); } 
		String openCLCodeString = null;
		try {
			openCLCodeString = org.imagejdev.api.StreamToString.getString(
					SobelLocal.class.getResourceAsStream("sobel.cl"), false);
		} catch (Exception e1) {
			IJ.handleException(e1);
		}

		
		CLProgram program = null;
		CLContext context;
		CLKernel kernel;
		CLCommandQueue queue;

		FloatBuffer data;
		CLBuffer<FloatBuffer> clFloatBufferDataCopy;
		CLBuffer<FloatBuffer> clFloatBufferData;

		if (DEBUG) { System.out.println( "Creating an OpenCL context from GPU"); }
		context = CLContext.create(Type.GPU);

		if (DEBUG) { System.out.println( "Create the OpenCL program"); }
		program = context.createProgram(openCLCodeString);

		if (DEBUG) { System.out.println( "The OpenCL source is " + program.getSource()) ; }
		program.build();

		// Display java.libary.path -Djava.library.path=""
		if (DEBUG) {
			System.out.println(" Java.library.path is "
					+ System.getProperty("java.library.path"));
		}

		if (DEBUG) {
			CLPlatform[] platforms = CLPlatform.listCLPlatforms();
			if (DEBUG) {
				for (CLPlatform clPlatform : platforms) {
					System.out.println("Discovered " + clPlatform.getName());
				}
			}
		}

		if (DEBUG) { System.out.println("Creating the OpenCL kernel"); }
		kernel = program.createCLKernel("sobel");

		if (DEBUG) { System.out.println("Creating the OpenCL command queue"); }
		queue = context.getMaxFlopsDevice().createCommandQueue();

		data = ByteBuffer.allocateDirect(imageHeight * imageWidth * 4)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();

		if (DEBUG) { System.out.println("Creating device buffers for the data.");}
		clFloatBufferData = context.createBuffer(data, Mem.READ_WRITE);
		clFloatBufferDataCopy = context.createFloatBuffer(imageHeight* imageWidth, Mem.READ_ONLY);

		if (DEBUG) { System.out.println("Making the OpenCL kernel assignments"); }
		kernel.setArg(0, clFloatBufferDataCopy);
		kernel.setArg(1, clFloatBufferData);
		kernel.setArg(2, imageWidth);
		kernel.setArg(3, imageHeight);

		if( DEBUG ) System.out.println( "Adding the input data to the objects buffer");
		for (int i = 0; i < imageHeight * imageWidth; i++) {
			data.put(i, testImage[i]);
		}

		if (DEBUG) { System.out.println("Writing the OpenCL input data to the device"); }
		queue.putWriteBuffer(clFloatBufferData, false);

		if (DEBUG) { System.out.println( "Making a copy of the input data buffer"); }
		queue.putCopyBuffer(clFloatBufferData, clFloatBufferDataCopy);

		if (DEBUG) { System.out.println("Enqueing the OpenCL kernel"); }
		queue.put2DRangeKernel(kernel, 0, 0, imageWidth, imageHeight, 0, 0);

		if (DEBUG) { System.out.println("Waiting for the OpenCL kernel to finish"); }
		queue.finish();

		if (DEBUG) { System.out.println("Reading back data results from the device"); }
		queue.putReadBuffer(clFloatBufferData, true);
		
		if( DEBUG ) System.out.println("Copying data from the NIO buffer to the JVM float[]");
		for (int i = 0; i < imageHeight * imageWidth; i++) {
			testImage[i] = data.get(i);
		}

		if (DEBUG)  System.out.println("Releasing the OpenCL context");
		context.release();
		
		if (DEBUG)  System.out.println("Creating a new Image from the results");
		ij.process.FloatProcessor floatProcessor = new ij.process.FloatProcessor(imp.getWidth(), imp.getHeight() );
		floatProcessor.setPixels( testImage );	
		ImagePlus scaled = new ImagePlus( "Sobel", floatProcessor );
	
		if (DEBUG)  System.out.println("Showing the image");
		scaled.show();
		
	}

	public static float getAvg(int c) {
		int r = (c & 0xff0000) >> 16;
		int g = (c & 0xff00) >> 8;
		int b = c & 0xff;
		return (r + g + b) / 3;
	}
}

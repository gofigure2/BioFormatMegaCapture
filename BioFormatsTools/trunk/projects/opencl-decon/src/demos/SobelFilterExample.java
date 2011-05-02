package demos;

import java.awt.Image;
import java.awt.image.PixelGrabber;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.imageio.ImageIO;

import org.imagejdev.api.StreamToString;

import com.jogamp.common.nio.Buffers;
import com.jogamp.common.nio.PointerBuffer;
import com.jogamp.opencl.CL;
import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice.Type;
import com.jogamp.opencl.CLException;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLMemory.Mem;
import com.jogamp.opencl.CLPlatform;
import com.jogamp.opencl.CLProgram;

/**
 * This example will fetch an image from the web and use the installed OpenCL libraries on your host machine to process the image. 
 * The class will display the processed image.
 * @author rick
 *
 */
public class SobelFilterExample {
	static boolean DEBUG = true;
	static boolean COMPARERESULTS = true;
	static boolean DISPLAYIMAGE = true;
	static CLProgram program = null;
	static CLPlatform platform = null;
	static CLContext context;
	static CLKernel kernel;
	static CLCommandQueue queue;
	static int maxComputeUnits;
	static FloatBuffer data;
	static CLBuffer<FloatBuffer> clFloatBufferDataCopy;
	static CLBuffer<FloatBuffer> clFloatBufferData;
	static int imageWidth;
	static int imageHeight;
	static int globalWorkSize;
	static int localWorkSize;


	public void release()
	{
		context.release();
	}
	
	
	public SobelFilterExample ()
	{;}
	
    private final void checkError(String msg, int ret) {
        if(ret != org.jocl.CL.CL_SUCCESS)  //to CLs org.jocl and com.jogamp.opencl
            throw CLException.newException( ret, msg );
    }

	
	public synchronized SobelFilterExample init( int w, int h, boolean printDebugStatements, final String openCLCodeString ) throws IOException
	{
		//Create a context from GPU
		context = CLContext.create( Type.GPU );
	
		// create the program
		program = context.createProgram( openCLCodeString );
		
		//System.out.println( "Gotz srzs ?" +  program.getSource());
		program.build();
	    
		// Display java.libary.path -Djava.library.path=""
		if( DEBUG )  System.out.println(" Java.library.path is " + System.getProperty("java.library.path"));
		
		imageHeight = h;
		imageWidth = w;
		
		
		final boolean DEBUG = printDebugStatements;
			
		if( DEBUG )  {
			CLPlatform[] platforms = CLPlatform.listCLPlatforms();
			if( DEBUG ) for(CLPlatform clPlatform : platforms)
				System.out.println("Discovered " + clPlatform.getName() );
		}
				
		if( DEBUG )  System.out.println( "assign the kernel" );
		kernel = program.createCLKernel("sobel");

		if( DEBUG )  System.out.println( "get a command queue" );
		queue = context.getMaxFlopsDevice().createCommandQueue( );

		if( DEBUG )  System.out.println( "find the max compute unites");
		maxComputeUnits = queue.getDevice().getMaxComputeUnits();

		data = ByteBuffer.allocateDirect( imageHeight*imageWidth*4 ).order(ByteOrder.nativeOrder()).asFloatBuffer();

		if( DEBUG )  System.out.println( "Create device buffers for the image data and the convolve kernel.");
		clFloatBufferData = context.createBuffer( data, Mem.READ_WRITE );
		clFloatBufferDataCopy = context.createFloatBuffer( imageHeight*imageWidth, Mem.READ_ONLY );

		if( DEBUG )  System.out.println( "Making kernel assignments");
		kernel.setArg( 0, clFloatBufferDataCopy );
		kernel.setArg( 1, clFloatBufferData );
		kernel.setArg( 2, imageWidth );
		kernel.setArg( 3, imageHeight );

		return this;
	}

	private void sleepInSeconds( int seconds ) {
		try {
			Thread.sleep( seconds * 1000 );
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public synchronized long run( float[] in )
	{
		//check length
		assert( in.length == imageHeight*imageWidth );

		//if( DEBUG )  System.out.println( "add the input data to the objects buffer ");
		for(int i = 0; i < imageHeight*imageWidth; i++)
			data.put(i, in[i] );

		long executionTime = run( );

		//if( DEBUG )  System.out.println("Copy data to the float[]...");
		for(int i = 0; i < imageHeight*imageWidth; i++)
			in[i] = data.get( i );

		return executionTime;
	}

	private synchronized long run( )
	{		
		if( DEBUG ) { System.out.println("Writing the input data set and the convolveKernel to the device...");}
		queue.putWriteBuffer( clFloatBufferData, false );

		//make a copy of the data buffer
		queue.putCopyBuffer( clFloatBufferData, clFloatBufferDataCopy );

		if( DEBUG ) System.out.println("Starting the timer...");
		long time = System.currentTimeMillis();

		if( DEBUG ) { System.out.println("Enqueing the kernel w...");}
		queue.put2DRangeKernel(kernel, 0, 0, imageWidth, imageHeight, 0, 0);

		if( DEBUG ) { System.out.println("Waiting for CLFinish to return...");}
		queue.finish();

		if( DEBUG ) System.out.println("Stopping the java timer...");
		time = System.currentTimeMillis() - time;

		if( DEBUG )  System.out.println("Reading back data from the device...");
		queue.putReadBuffer( clFloatBufferData, true );

		return time;
	}

	public static void main(String[] args)
	{
		// print out java.library.path
		if (DEBUG) System.out.println( System.getProperty("java.library.path"));
		
		// get an image 
		if( DEBUG )  System.out.println("Retrieving test image...  ");
		Image image = null; 
		try {
			URL url = new URL("http://www.newscenter.philips.com/pwc_nc/main/shared/assets/newscenter/2009_pressreleases/GlyGenix/ultrasound_mediated_gene_delivery_hi-res.jpg");
			image = ImageIO.read(url); 
		} catch (IOException e) { } 

		int testWidth = image.getWidth(null);
		int testHeight = image.getHeight(null);
		
	
		// get the OpenCL code
		String openCLCodeString = null;
		try {
			openCLCodeString = StreamToString.getString( SobelFilterExample.class.getResourceAsStream("sobel.cl"), false );
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//if ( DEBUG ) System.out.println( openCLCodeString );
		
		final int numVariables = 4;
		final int numIterations = 1;
		long[] times = new long[numVariables];

		//run test
		runTest( image, numIterations, times, openCLCodeString, testWidth, testHeight );

		System.out.println("The average OpenCL set up time is " + times[0]/numIterations);
		System.out.println("The average OpenCL IO transfer time is " + times[1]/numIterations);
		System.out.println("The average OpenCL execution time is " + times[2]/numIterations);
		System.out.println("The average Java execution time is " + times[3]/numIterations);
	}


	public static float getAvg( int c ) {
		int r = (c&0xff0000) >> 16;
		int g = (c&0xff00) >> 8;
			int b = c&0xff;
			return (r + g + b)/3;
	}


	public static synchronized void runTest( Image image, int numIterations, long[] totalTime, final String openCLCodeString, int testWidth, int testHeight )
	{

		for(int iteration = 0; iteration < numIterations; iteration++)
		{
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

			//Clone the input data
			float[] testImageCopy = testImage.clone();
			
			try {
				System.out.println("Starting iteration... " + iteration );

				//Start the performance timer
				long time = System.currentTimeMillis();

				//setup OpenCL
				SobelFilterExample generateSobelFilterData = new SobelFilterExample().init( testWidth, testHeight, true, openCLCodeString );

				//Stop the performance timer
				time = System.currentTimeMillis() - time;
				System.out.println("It took...  " + time + " milliseconds to set up OpenCL.");

				totalTime[0] += time;

				//Start the performance timer
				time = System.currentTimeMillis();

				long executionTime = generateSobelFilterData.run( testImage );

				//Stop the performance timer
				time = System.currentTimeMillis() - time;
				System.out.println("It took...  " + executionTime + " milliseconds to execute sobel filter in OpenCL for image size " + testHeight + " x " + testWidth + " and " + time + " mSec total time." );

				totalTime[1] += time;
				totalTime[2] += executionTime;

				//Start the performance timer
				time = System.currentTimeMillis();

				filter3x3( testImageCopy, testWidth, testHeight );

				//Stop the performance timer
				time = System.currentTimeMillis() - time;
				totalTime[3] += time;
				System.out.println("It took...  " + time + " milliseconds to execute sobel filter in Java for image size " + testHeight + " x " + testWidth );

				//Compare the results
				if (COMPARERESULTS)
				{
					int success = 0, failed = 0;
					for( int i = 0; i < testWidth; i++ )
						for( int j = 0; j < testHeight; j++ )
						{
							int index = i*testHeight+j;
							float diff = testImage[ index ] - testImageCopy[ index ];

							if ( Math.abs(diff) < 0.0001 )
							{
								success++;
							}	else 
							{ 
								failed++; 
								//if (failed < 5000*5)
								System.out.println("Reference value " + testImageCopy[ index ] + " not equal " + testImage[ index ] + " for index " + index );
							}
						}
					System.out.println("The test passed " + success + " values and failed " + failed + " of " + testWidth*testHeight + " values.");
				}

				//free the OpenCL resources and wait 10 seconds...
				generateSobelFilterData.release();
				generateSobelFilterData = null;
				System.gc();
				Thread.sleep(10000);  //sleep for 10 seconds

			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}





	static void filter3x3(float[] pixels, int width, int height ) {
		//create a copy
		float[] pixelsCopy = pixels.clone();

		float p0, p1, p2, p3, p5, p6, p7, p8 = 0;


		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) 
			{
				int offset = y * width + x;

				if( x < 1 || y < 1 || x > width - 2 || y > height - 2 )
				{
					pixels[offset] = 0; 
				}
				else
				{
					p0 = pixelsCopy[offset - width - 1] ;
					p1 = pixelsCopy[offset - width] ;
					p2 = pixelsCopy[offset - width + 1] ;
					p3 = pixelsCopy[offset - 1];
					p5 = pixelsCopy[offset + 1];
					p6 = pixelsCopy[offset + width - 1] ;
					p7 = pixelsCopy[offset + width] ;
					p8 = pixelsCopy[offset + width + 1] ;

					double sum1 = p0 + 2*p1 + p2 - p6 - 2*p7 - p8;  //GY
					double sum2 = p0 + 2*p3 + p6 - p2 - 2*p5 - p8;  //GX

					pixels[offset] = (float) Math.sqrt(  sum1*sum1 + sum2*sum2 );
				}
			}
		}
	}

}

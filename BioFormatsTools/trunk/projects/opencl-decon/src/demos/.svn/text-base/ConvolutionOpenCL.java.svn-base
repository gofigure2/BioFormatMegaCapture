package demos;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLProgram;
import com.jogamp.opencl.CLDevice.Type;
import com.jogamp.opencl.CLMemory.Mem;

import mpicbg.imglib.algorithm.fft.FourierConvolution;
import mpicbg.imglib.container.DirectAccessContainer;
import mpicbg.imglib.container.array.ArrayContainerFactory;
import mpicbg.imglib.container.basictypecontainer.FloatAccess;
import mpicbg.imglib.container.basictypecontainer.array.FloatArray;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.image.ImageFactory;
import mpicbg.imglib.image.display.imagej.ImageJFunctions;
import mpicbg.imglib.io.ImageOpener;
import mpicbg.imglib.type.numeric.real.FloatType;
import ij.ImageJ;

public class ConvolutionOpenCL 
{

	public static float[] getConv( final float[] img, final int w, final int h, final int d, 
			final float[] kernel, final int wk, final int hk, final int dk )
	{
		float[] results = new float[w*h*d];
		final Type deviceType = Type.CPU;
		final boolean DEBUG = true;
		
		if( DEBUG )  System.out.println( "Create a float buffer for the 3D Image, the 3D kernel, and the output" );
		FloatBuffer imageFloatBuffer = ByteBuffer.allocateDirect( w * h * d * 4 ).order(ByteOrder.nativeOrder()).asFloatBuffer();
		FloatBuffer kernelFloatBuffer = ByteBuffer.allocateDirect( wk * hk * dk * 4 ).order(ByteOrder.nativeOrder()).asFloatBuffer();
		FloatBuffer resultFloatBuffer = ByteBuffer.allocateDirect( w * h * d * 4 ).order(ByteOrder.nativeOrder()).asFloatBuffer();
			
		if( DEBUG )  System.out.println( "Put the image and kernel float[]s into the NIO backed buffer" );
		for( int i = 0; i < w*h*d; i++)
			imageFloatBuffer.put( i, img[i] );
		for( int i = 0; i < wk*hk*dk; i++)
			kernelFloatBuffer.put( i, kernel[i] );
		
		// Set the the OpenCL device
		CLContext context = null;
		CLProgram convProgram = null;
        try {
        	if( DEBUG )  System.out.println( "Pick the openCL Device" );
        	context = CLContext.create( deviceType );
        	
        	if( DEBUG )  System.out.println( "Create the program from the OpenCL file" );
        	convProgram = context.createProgram( FHT3DExample.class.getResourceAsStream( "conv.cl" ) ).build();
        
        } catch ( IOException e ) 
        {
        	e.printStackTrace();
        }

        if( DEBUG )  System.out.println( "Create read only memory on the device for the image" );
		CLBuffer<FloatBuffer> clImageBuffer = context.createBuffer( imageFloatBuffer, Mem.READ_ONLY );
		
		if( DEBUG )  System.out.println( "Create read only memory on the device for the kernel" );
		CLBuffer<FloatBuffer> clKernelBuffer = context.createBuffer( kernelFloatBuffer, Mem.READ_ONLY );
		
		if( DEBUG )  System.out.println( "Create read / write memory on the device for the output results" );
		CLBuffer<FloatBuffer> clResultBuffer = context.createBuffer( resultFloatBuffer, Mem.READ_WRITE );
		
		if( DEBUG )  System.out.println( "Creating the kernel" );
		CLKernel kernelConv3D = convProgram.createCLKernel("conv");
		
		// set the kernel args
		kernelConv3D.setArg( 0, clImageBuffer );
		kernelConv3D.setArg( 1, clKernelBuffer );
		kernelConv3D.setArg( 2, clResultBuffer );
		kernelConv3D.setArg( 3, w );
		kernelConv3D.setArg( 4, h );
		kernelConv3D.setArg( 5, d );
		kernelConv3D.setArg( 6, wk );
		kernelConv3D.setArg( 7, hk );
		kernelConv3D.setArg( 8, dk );
		
		if( DEBUG )  System.out.println( "Getting a command queue" );
		CLCommandQueue queue = context.getDevices()[0].createCommandQueue();
		
		if( DEBUG )  System.out.println( "Using device " + queue.getDevice().getName() );
		
		if( DEBUG ) { System.out.println("Writing the input image and kernel data set to the device...");}
		queue.putWriteBuffer( clImageBuffer, false );
		queue.putWriteBuffer( clKernelBuffer, false );

		if( DEBUG ) { System.out.println("Enqueing the kernel...");}
		queue.put2DRangeKernel( kernelConv3D, 0, 0, 2, 2, 0, 0 );

		if( DEBUG ) { System.out.println("Waiting for CLFinish to return...");}
		//queue.finish();

		if( DEBUG )  System.out.println("Reading back results from the device...");
		queue.putReadBuffer( clResultBuffer, true );
		
		if ( DEBUG ) System.out.println("Copying the results back to the results float[]...");
		for( int i = 0; i < w*h*d; i++ )
			results[i] = resultFloatBuffer.get( i );
		
		//release the queue, image, and kernel memory resources used in the native layers
		imageFloatBuffer = null;
		kernelFloatBuffer = null;
		resultFloatBuffer = null;
		queue.release();
		
		// return the results
		return results;
	}
	
	public static void main( String[] args )
	{
		new ImageJ();
		
		Image<FloatType> kernel = FourierConvolution.createGaussianKernel( new ArrayContainerFactory(), 10.7f, 3 );
		Image<FloatType> kernel2 = FourierConvolution.createGaussianKernel( new ArrayContainerFactory(), 11.7f, 3 );
		ImageJFunctions.copyToImagePlus( kernel ).show();
		ImageJFunctions.copyToImagePlus( kernel2 ).show();
		
		ImageFactory<FloatType> factory = new ImageFactory<FloatType>( new FloatType(), new ArrayContainerFactory() );
		Image<FloatType> img = null;
		
		try {
			img = new ImageOpener().openImage( "HisYFP-SPIM/spim_TL18_angle0.lsm", factory );
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//ImageJFunctions.show( img );
		
        final float[] imageF = ((FloatArray) ( (DirectAccessContainer<FloatType, FloatAccess>) img.getContainer() ).update( null )).getCurrentStorageArray();
        final float[] kernelF = ((FloatArray) ( (DirectAccessContainer<FloatType, FloatAccess>) kernel.getContainer() ).update( null )).getCurrentStorageArray();
        
        final float[] results = conv( imageF, img.getDimension(0), img.getDimension(1), img.getDimension(2), kernelF, kernel.getDimension(0), kernel.getDimension(1), kernel.getDimension(2)); 
        //float[] results = ConvolutionOpenCL.getConv(imageF, img.getDimension(0), img.getDimension(1), img.getDimension(2), kernelF, kernel.getDimension(0), kernel.getDimension(1), kernel.getDimension(2));
        
        // display results
        System.exit(0);
        
	}

	private static float[] conv( float[] imageF, int w, int h,
			int d, float[] kernelF, int kw, int kh,
			int kd ) 
	{
		System.out.println("w*h*d" + (w*h*d*4)  );
		System.out.println("w" + (w)  );
		System.out.println("h" + (h)  );
		System.out.println("d" + (d)  );
		
		// results array
		float[] results = new float[ w*h*d ];
		
		// boundary
		int boundaryW = (kw-1)/2;
		int boundaryH = (kh-1)/2;
		int boundaryD = (kd-1)/2;
			
		// simulate 2d execution
		for( int oclW = 0; oclW < w; oclW++)
		{
			System.out.println((oclW/w)*100 + " % complete.");
			for( int oclH = 0; oclH < h; oclH++)
			{
				// Start kernel logic
				for (int oclD = 0; oclD < d; oclD++) {

					// sum the values
					results[oclW * h * d + oclH * d + oclD] = getSum( imageF,
							kernelF, boundaryW, boundaryH, boundaryD, oclW,
							oclH, oclD, kw, kh, kd, w, h, d );

				}// End kernel logic
			}
		}
		
		return results;
	}

	private static float getSum(float[] imageF, float[] kernelF, int boundaryW,
			int boundaryH, int boundaryD, int oclW, int oclH, int oclD, int kw,
			int kh, int kd,  int w, int h, int d ) {
		
		float sum = 0f;
		
		// Sum the inputs over the dimensions of the kernel
		for( int kernW = 0; kernW < kw; kernW++ )
		{
			for( int kernH = 0; kernH < kh; kernH++ )
			{
				for( int kernD = 0; kernD < kd; kernD++ )
				{
					sum += kernelF[ kernW*kh*kd + kernH*kd + kernD ] * getImageValue( imageF, w, h, d, oclW, oclH, oclD, boundaryW, boundaryH, boundaryD, kernW, kernH, kernD );
				} 
			}
		}
		
		return sum;
	}

	// given an input, find the output
	private static float getImageValue( float[] imageF, int w, int h, int d,
			int oclW, int oclH, int oclD, int boundaryW, int boundaryH,
			int boundaryD, int kernW, int kernH, int kernD ) 
	{
		
		// indices used for boundary conditions
		int xIndex = 0;
		int yIndex = 0;
		int zIndex = 0;

		// if output X index is inside left kernel boundary
		if ( oclW < boundaryW )
		{
			xIndex = ( boundaryW - oclW );
		} else if ( w - boundaryW < oclW )  // if output X index is inside right kernel boundary
		{
			xIndex = w - ( boundaryW - oclW );
		} else { xIndex = oclW; }
		

		// if output Y index is inside left kernel boundary
		if ( oclH < boundaryH )
		{
			yIndex = ( boundaryH - oclH );
		} else if ( h - boundaryH < oclH )  // if output Y index is inside right kernel boundary
		{
			yIndex = h - ( boundaryH - oclH );
		} else { yIndex = oclH; }
		

		// if output Z index is inside left kernel boundary
		if ( oclD < boundaryD )
		{
			zIndex = ( boundaryD - oclD );
		} else if ( d - boundaryD < oclD )  // if output Z index is inside right kernel boundary
		{
			zIndex = d - ( boundaryD - oclD );
		} else { zIndex = oclD; }
		
		return imageF[ xIndex*h*d + yIndex*d + zIndex ];
	}
}

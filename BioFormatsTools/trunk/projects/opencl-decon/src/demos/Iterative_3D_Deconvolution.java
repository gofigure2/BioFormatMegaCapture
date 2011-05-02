package demos;


import java.io.IOException;


import ij.*;
import ij.gui.*;
import ij.process.*;


public class Iterative_3D_Deconvolution
{	
	static boolean GENERATEREFERENCE = false;
	static boolean IJSHOWSTATUS = false;
	static boolean TESTSMALL = false;
	static boolean TEST = false;
	static String path = "/home/loci/workspace/OpenCLIterative3DDeconvolution/";
	static boolean USEOPENCL;
	private static FHT3DExample fastHartleyTransform3D;
	private static long[] times;

	public synchronized static void main(String[] args) throws IOException
	{
		USEOPENCL = false;
		times = new long[5];

		if( USEOPENCL )
		{
			times = new long[5];

			//run test
			run( 1, times );

			System.out.println("The average OpenCL set up time is " + times[0]);  //916, 921, 912, 912, 921, 919, 921, 914, 915, 912
			System.out.println("The average OpenCL IO transfer and compute time is " + times[1]/times[3]);
			System.out.println("The average OpenCL execution time is " + times[2]/times[3]);
			System.out.println("FHT called " + times[3] + " times.");
		} else {

			run( 1, times );
			System.out.println("The average Java execution time is " + times[4]/times[3]);
			System.out.println("FHT called " + times[3] + " times.");
		}
	}

	public synchronized static void run(int numIterations, long[] times){
		
		int nIter = 1;
		float gamma = 0; //Wiener filter gamma
		double filterX = 1;
		double filterY = 1;
		double filterZ = 1;
		boolean normalize = false;
		boolean logMean = false;
		boolean showIteration = false;
		boolean antiRing = true;
		double changeThreshPercent = 0.01;
		boolean dB = false;
		boolean detectDivergence = true;
		String titleOut = "Deconvolved";
		boolean wiener = (gamma > 0.0001);
		ImageJ ij = new ImageJ();

		IJ.run("Image Sequence...", "open=" + path + "sourcedata/PSF/PSF20000.tif number=256 starting=1 increment=1 scale=100 file=[] or=[] sort");
		IJ.run("Image Sequence...", "open=" + path + "sourcedata/Bars-G10-P15/Bars-G10-P15-2000.tif number=128 starting=1 increment=1 scale=100 file=[] or=[] sort");


		int[] wList = WindowManager.getIDList();
		if (wList == null){
			IJ.noImage();
			return;
		}
		String[] titles = new String[wList.length];
		for (int i = 0; i < wList.length; i++)
		{
			ImagePlus imp = WindowManager.getImage( wList[i] );
			if (imp != null)
				titles[i] = imp.getTitle();
			else
				titles[i] = "";
		}

		String titleImage = Prefs.get("iterativedeconvolve3d.titleImage", titles[0]);
		int imageChoice = 0;
		for (int i = 0; i < wList.length; i++)
		{
			if(titleImage.equals(titles[i]))
			{
				imageChoice = i;
				break;
			}
		}

		String titlePSF = Prefs.get("iterativedeconvolve3d.titlePSF", titles[0]);
		int psfChoice = 0;
		for (int i = 0; i < wList.length; i++)
		{
			if(titlePSF.equals(titles[i]))
			{
				psfChoice = i;
				break;
			}
		}

		GenericDialog gd = new GenericDialog("Deconvlove 3D (DAMAS3)", IJ.getInstance());
		gd.addChoice("Image",titles,titles[imageChoice]);
		gd.addChoice("Point Spread Function (Kernel)",titles,titles[psfChoice]);
		ImagePlus impY = WindowManager.getImage(wList[gd.getNextChoiceIndex()]);
		ImagePlus impA = WindowManager.getImage(wList[gd.getNextChoiceIndex()]);


		ImageStack stackA = impA.getStack();
		int kw = stackA.getWidth();
		int kh = stackA.getHeight();
		int kd = stackA.getSize();

		ImageStack stackY = impY.getStack();
		int bw = stackY.getWidth();
		int bh = stackY.getHeight();
		int bd = stackY.getSize();

		int bwE = expandedSize(bw);
		int bhE = expandedSize(bh);
		int bdE = (bd == 1) ? 1 : expandedSize(bd);
		int kwE = expandedSize(kw);
		int khE = expandedSize(kh);
		int kdE = (kd == 1) ? 1 : expandedSize(kd);

		//w and h will always be at least 4.  d can be 1 as a special case.
		int w = (int) Math.max( bwE,kwE );
		int h = (int) Math.max( bhE,khE );
		int d = (int) Math.max(bdE,kdE);
		int kOff = (d - bd + 1)/2;
		int jOff = (h - bh + 1)/2;
		int iOff = (w - bw + 1)/2;	
		java.awt.image.ColorModel cmY = stackY.getProcessor(1).getColorModel();
		double ic = w/(filterX + 0.000001);
		double jc = h/(filterY + 0.000001);
		double kc = d/(filterZ + 0.000001);
		float oldPercentChange = Float.MAX_VALUE;

		ImageProcessor ipY = impY.getProcessor();
		ImageProcessor ipA = impA.getProcessor();
		if(((ipY instanceof ColorProcessor)||(ipA instanceof ColorProcessor))){
			IJ.showMessage("RGB images are not currently supported.");
			return;
		}


		float[][] dataYin = new float[bd][];
		if(ipY instanceof FloatProcessor){
			for (int i = 0; i < bd; i++){
				dataYin[i] = (float[])stackY.getProcessor(i+1).getPixels();
			}
		}else{
			for (int i = 0; i < bd; i++){
				dataYin[i] = (float[])stackY.getProcessor(i+1).convertToFloat().getPixels();
			}
		}

		if(GENERATEREFERENCE) { HelperUtils.debugWriteFloatArray( path, dataYin, "dataYin" ); }
		if(TEST) { HelperUtils.compareResults( path, dataYin, "dataYin_float_128_65536"); }
		if(TESTSMALL) { HelperUtils.compareResults( path, dataYin, "dataYin_float_256_4096"); }


		float[][] dataAin = new float[kd][];
		if(ipA instanceof FloatProcessor){
			for (int i = 0; i < kd; i++){
				dataAin[i] = (float[])stackA.getProcessor(i+1).getPixels();
			}
		}else{
			for (int i = 0; i < kd; i++){
				dataAin[i] = (float[])stackA.getProcessor(i+1).convertToFloat().getPixels();
			}
		}

		if(GENERATEREFERENCE) { HelperUtils.debugWriteFloatArray( path, dataAin, "dataAin" ); }
		if(TEST) { HelperUtils.compareResults( path, dataAin, "dataAin_float_256_65536"); }
		if(TESTSMALL) { HelperUtils.compareResults( path, dataAin, "dataAin_float_256_4096"); }


		//Start the timer
		long timer = System.currentTimeMillis();

		double minA = 0;
		double minY = 0;
		if(dB){
			minA = unDB(dataAin);
			minY = unDB(dataYin);
		}

		float[] aw = null,ah = null,ad = null;

		float scalePSF = 1;
		//if(normalize){
		float sum = 0;
		for (int k = 0; k < kd; k++){
			for (int ind = 0; ind < kh*kw; ind++){
				sum += dataAin[k][ind];
			}
		}
		if((sum != 0)&&normalize)scalePSF = scalePSF / sum;
		//}

		//Compute Gaussian filter weights
		float[] gi = new float[w];
		for(int i = 0; i < w; i++){
			int iShifted = i;
			if(iShifted > w/2) iShifted = w - iShifted;
			gi[i] = (float)Math.exp(-(iShifted/ic)*(iShifted/ic));
		}


		float[] gj = new float[h];
		for(int j = 0; j < h; j++){
			int jShifted = j;
			if(jShifted > h/2)jShifted = h - jShifted;
			gj[j] = (float)Math.exp(-(jShifted/jc)*(jShifted/jc));
		}
		float[] gk = new float[d];
		for(int k = 0; k < d; k++){
			int kShifted = k;
			if(kShifted > d/2) kShifted = d - kShifted;
			gk[k] = (float)Math.exp(-(kShifted/kc)*(kShifted/kc));
		}

		if(GENERATEREFERENCE) { HelperUtils.debugWriteFloatArray( path, gi, "gi"); }
		if(GENERATEREFERENCE) { HelperUtils.debugWriteFloatArray( path, gj, "gj"); }
		if(GENERATEREFERENCE) { HelperUtils.debugWriteFloatArray( path, gk, "gk"); }
		if(TEST) { HelperUtils.compareResults( path, gi, "gi_float_512"); }
		if(TESTSMALL) { HelperUtils.compareResults( path, gi, "gi_float_128"); }
		if(TEST) { HelperUtils.compareResults( path, gj, "gj_float_512"); }
		if(TESTSMALL) { HelperUtils.compareResults( path, gj, "gj_float_128"); }
		if(TEST) { HelperUtils.compareResults( path, gk, "gk_float_512"); }
		if(TESTSMALL) { HelperUtils.compareResults( path, gk, "gk_float_512"); }

		IJ.showStatus("Creating expanded arrays");

		float[][] dataY = new float[d][w*h];
		copyDataMirror( bw, bh, bd, dataYin, w, h, d, dataY);
		if(GENERATEREFERENCE) { HelperUtils.debugWriteFloatArray( path, dataY, "dataY"); }
		if(TEST) { HelperUtils.compareResults( path, dataY, "dataY_float_512_262144"); }
		if(TESTSMALL) { HelperUtils.compareResults( path, dataY, "dataY_float_512_16384"); }

		float[][] dataA = new float[d][w*h];
		copyDataMask(kw,kh,kd,dataAin,w,h,d,dataA);
		if(GENERATEREFERENCE) { HelperUtils.debugWriteFloatArray( path, dataA, "dataAcopyMask"); }
		if(TEST) { HelperUtils.compareResults( path, dataA, "dataAcopyMask_float_512_262144"); }
		if(TESTSMALL) { HelperUtils.compareResults( path, dataA, "dataAcopyMask_float_512_16384"); }

		IJ.showStatus("Swapping quadrants of the PSF");
		swapQuadrants(w,h,d,dataA);
		if(GENERATEREFERENCE) { HelperUtils.debugWriteFloatArray( path, dataA, "dataAswapQuadrants"); }
		if(TEST) { HelperUtils.compareResults( path, dataA, "dataAswapQuadrants_float_512_262144"); }
		if(TESTSMALL) { HelperUtils.compareResults( path, dataA, "dataAswapQuadrants_float_512_16384"); }

		IJ.showStatus("Transforming PSF");
		FHT3D(dataA,w,h,d,false);
		if (GENERATEREFERENCE) { HelperUtils.debugWriteFloatArray( path, dataA, "dataAfht1"); }
		if(TEST) { HelperUtils.compareResults( path, dataA, "dataAfht1_float_512_262144"); }
		if(TESTSMALL) { HelperUtils.compareResults( path, dataA, "dataAfht1_float_512_16384"); }
		
		float[][] dataX = new float[d][w*h];
		float[][] AX = new float[d][w*h];


		if(antiRing){
			//Anti-ringing step.
			IJ.showStatus("Performing anti-ringing step.");
			copyData(w,h,d,dataY,dataX);
			if(GENERATEREFERENCE) { HelperUtils.debugWriteFloatArray( path,  dataX, "dataXantiRingCopyData" ); }
			if(TEST) { HelperUtils.compareResults( path, dataX, "dataXantiRingCopyData_float_512_262144"); }
			if(TESTSMALL) { HelperUtils.compareResults( path, dataX, "dataXantiRingCopyData_float_512_16384"); }
			
			//compute on second GPU device
			FHT3D(dataX,w,h,d,false);
			if(GENERATEREFERENCE) { HelperUtils.debugWriteFloatArray( path,  dataX, "dataXantiRingFHT3D" ); }
			if(TEST) { HelperUtils.compareResults( path, dataX, "dataXantiRingFHT3D_float_512_262144"); }
			if(TESTSMALL) { HelperUtils.compareResults( path, dataX, "dataXantiRingFHT3D_float_512_16384"); }

			convolveFD(w,h,d,dataA,dataX,AX);
			if(GENERATEREFERENCE) { HelperUtils.debugWriteFloatArray( path,  AX, "AXantiRingConvolve" ); }
			if(TEST) { HelperUtils.compareResults( path, AX, "AXantiRingConvolve_float_512_262144"); }
			if(TESTSMALL) { HelperUtils.compareResults( path, AX, "AXantiRingConvolve_float_512_16384"); }

			FHT3D(AX,w,h,d,true);
			if(GENERATEREFERENCE) { HelperUtils.debugWriteFloatArray( path,  AX, "AXantiRingFHT3D" ); }
			if(TEST) { HelperUtils.compareResults( path, AX, "AXantiRingFHT3D_float_512_262144"); }			
			if(TESTSMALL) { HelperUtils.compareResults( path, AX, "AXantiRingFHT3D_float_512_16384"); }	

			copyDataAverage(bw,bh,bd,w,h,d,sum,dataY,AX,dataY);
			if(GENERATEREFERENCE) { HelperUtils.debugWriteFloatArray( path,  dataY, "dataYantiRingCopyAverage" ); }
			if(TEST) { HelperUtils.compareResults( path, dataY, "dataYantiRingCopyAverage_float_512_262144"); }
			if(TESTSMALL) { HelperUtils.compareResults( path, dataY, "dataYantiRingCopyAverage_float_512_16384"); }

		}

		//Optional premultiplication step
		if(wiener){
			IJ.showStatus("Finding largest spectral element");
			double magMax = findMagMax(w,h,d,dataA);
			if(GENERATEREFERENCE) { HelperUtils.debugWriteDouble( path, magMax, "magMaxWiener"); }

			IJ.showStatus("Transforming blurred image");
			FHT3D(dataY,w,h,d,false);
			if(GENERATEREFERENCE) { HelperUtils.debugWriteFloatArray( path,  dataY, "dataYWienerFHT3D" ); }

			IJ.showStatus("Premultiplying PSF and blured image");
			//Use dataX storage temporarily for FD PSF (could be more efficient)
			//Use AX storage temporarily to store FD Y (could be more efficient)
			copyData(w,h,d,dataA,dataX);
			if(GENERATEREFERENCE) { HelperUtils.debugWriteFloatArray( path,  dataX, "dataXWienerCopyData" ); }

			deconvolveFD(gamma,magMax,w,h,d,dataX,dataX,dataA);
			if(GENERATEREFERENCE) { HelperUtils.debugWriteFloatArray( path,  dataA, "dataAWienerDeconvolveFD"); }
			copyData(w,h,d,dataY,AX);
			if(GENERATEREFERENCE) { HelperUtils.debugWriteFloatArray( path,  AX, "AXWienerCopyData"); }
			deconvolveFD(gamma,magMax,w,h,d,AX,dataX,dataY);
			if(GENERATEREFERENCE) { HelperUtils.debugWriteFloatArray( path,  dataY, "dataYWienerDeconvolveFD"); }
			IJ.showStatus("Inverse transforming blurred image");
			FHT3D(dataY,w,h,d,true);
			if(GENERATEREFERENCE) { HelperUtils.debugWriteFloatArray( path,  dataY, "dataYWienerFHT3D2"); }
		}

		//Finished with optional premultiplication step

		int wh = w*h;

		//Convert PSF back to the spatial domain in order to
		//compute aSum after the premultiplication step
		FHT3D(dataA,w,h,d,true);
		if(GENERATEREFERENCE) { HelperUtils.debugWriteFloatArray( path,  dataA, "dataAFHT3DPSF"); }
		if(TEST) { HelperUtils.compareResults( path, dataA, "dataAFHT3DPSF_float_512_262144"); }
		if(TESTSMALL){ HelperUtils.compareResults( path, dataA, "dataAFHT3DPSF_float_512_16384"); }

		float aSum = 0;
		for(int k = 0; k < d; k++){
			for (int ind = 0; ind < wh; ind++){
				aSum += (float)Math.abs(dataA[k][ind]);
			}
		}
		if(GENERATEREFERENCE) { HelperUtils.debugWriteFloat( path,  aSum, "aSumABS_float"); }
		if(TEST) { HelperUtils.compareResults( path, aSum, "aSumABS_float_float"); }
		if(TESTSMALL) { HelperUtils.compareResults( path, aSum, "aSumABS_float_float"); }

		//Apply scale factors
		if(scalePSF != 1){
			IJ.showStatus("Normalizing");
			for (int k = 0; k < d; k++){
				for (int ind = 0; ind < h*w; ind++){
					dataY[k][ind] = dataY[k][ind] / scalePSF;
				}
			}
			if(GENERATEREFERENCE) { HelperUtils.debugWriteFloatArray( path,  dataY, "dataYApplyScaleFactor"); }

		}
		FHT3D(dataA,w,h,d,false);
		if(GENERATEREFERENCE) { HelperUtils.debugWriteFloatArray( path,  dataA, "dataAFHT3DPostScaleFactor"); }
		if(TEST) { HelperUtils.compareResults( path, dataA, "dataAFHT3DPostScaleFactor_float_512_262144"); }
		if(TESTSMALL) { HelperUtils.compareResults( path, dataA, "dataAFHT3DPostScaleFactor_float_512_16384"); }

		copyData(w,h,d,dataY,dataX);
		if(GENERATEREFERENCE) { HelperUtils.debugWriteFloatArray( path,  dataX, "dataXPostSFCopyData"); }
		if(TEST) { HelperUtils.compareResults( path, dataX, "dataXPostSFCopyData_float_512_262144"); }
		if(TESTSMALL) { HelperUtils.compareResults( path, dataX, "dataXPostSFCopyData_float_512_16384"); }

		ImageStack stackOutTemp = null;
		ImagePlus impOutTemp = null;
		if(showIteration){
			stackOutTemp = new ImageStack(w,h);
			for (int k = 0; k < d; k++){
				ImageProcessor ip = new FloatProcessor(w,h);
				float[] px = (float[])ip.getPixels();
				for (int j = 0; j < h; j++){
					for (int i = 0; i < w; i++){
						px[i + w*j] = dataX[k][i + w*j];
					}
				}
				ip.setMinAndMax(0,0);
				ip.setColorModel(cmY);
				ip.setRoi(iOff,jOff,bw,bh);
				stackOutTemp.addSlice(null,ip);
			}
			impOutTemp = new ImagePlus(titleOut+"Temp",stackOutTemp);
			impOutTemp.setSlice(d/2 + 1);
			impOutTemp.show();
			ImageProcessor ip2 = impOutTemp.getProcessor();
			ip2.setMinAndMax(0,0);
			ip2.setColorModel(cmY);
			impOutTemp.updateAndDraw();
			impOutTemp.setRoi(iOff,jOff,bw,bh);
		}

		int iter;
		long innerTimer = System.currentTimeMillis();
		for (iter = 0; iter < nIter; iter++)
		{
			IJ.showProgress((float)iter/nIter);
			IJ.showStatus("Starting iteration "+(iter+1)+" of "+nIter);
			FHT3D(dataX,w,h,d,false);
			if(GENERATEREFERENCE) { HelperUtils.debugWriteFloatArray( path,  dataX, "dataXiter" + iter + "FHT3Dfalse"); }
			if(TEST) { HelperUtils.compareResults( path, dataX, "dataXiter0FHT3Dfalse_float_512_262144"); }
			if(TESTSMALL) { HelperUtils.compareResults( path, dataX, "dataXiter0FHT3Dfalse_float_512_16384"); }

			//Gaussian filter
			for(int k = 0; k < d; k++){
				for (int j = 0; j < h; j++){
					for (int i = 0; i < w; i++){
						dataX[k][i + w*j] = dataX[k][i + w*j]*gk[k]*gj[j]*gi[i];
					}
				}
			}
			if(GENERATEREFERENCE) { HelperUtils.debugWriteFloatArray( path,  dataX, "dataXiter" + iter + "GaussianFilter"); }
			if(TEST) { HelperUtils.compareResults( path, dataX, "dataXiter0GaussianFilter_float_512_262144"); }
			if(TESTSMALL) { HelperUtils.compareResults( path, dataX, "dataXiter0GaussianFilter_float_512_16384"); }

			convolveFD(w,h,d,dataA,dataX,AX);
			if(GENERATEREFERENCE) { HelperUtils.debugWriteFloatArray( path,  AX, "AXiter" + iter + "ConvolveFD"); }
			if(TEST) { HelperUtils.compareResults( path, AX, "AXiter0ConvolveFD_float_512_262144"); }
			if(TESTSMALL) { HelperUtils.compareResults( path, AX, "AXiter0ConvolveFD_float_512_16384"); }

			FHT3D(AX,w,h,d,true);
			if(GENERATEREFERENCE) { HelperUtils.debugWriteFloatArray( path,  AX, "AXiter" + iter + "FHT3Dtrue"); }
			if(TEST) { HelperUtils.compareResults( path, AX, "AXiter0FHT3Dtrue_float_512_262144"); }
			if(TESTSMALL) { HelperUtils.compareResults( path, AX, "AXiter0FHT3Dtrue_float_512_16384"); }

			//compute on second GPU device
			FHT3D(dataX,w,h,d,true);
			if(GENERATEREFERENCE) { HelperUtils.debugWriteFloatArray( path,  dataX, "dataXiter" + iter + "FHT3Dtrue"); }
			if(TEST) { HelperUtils.compareResults( path, dataX, "dataXiter0FHT3Dtrue_float_512_262144"); }
			if(TESTSMALL) { HelperUtils.compareResults( path, dataX, "dataXiter0FHT3Dtrue_float_512_16384"); }

			float meanDelta = 0;
			float delta;
			for(int k = 0; k < d; k++){
				for (int ind = 0; ind < wh; ind++){
					delta = (dataY[k][ind] - AX[k][ind]/aSum);
					dataX[k][ind] += delta;
					if(dataX[k][ind] < 0){
						dataX[k][ind] = 0;
					}else{
						meanDelta += Math.abs(delta);
					}
				}
			}
			if(GENERATEREFERENCE) { HelperUtils.debugWriteFloat( path, meanDelta, "meanDeltaiter" + iter + "MeanDelta"); }
			if(GENERATEREFERENCE) { HelperUtils.debugWriteFloatArray( path,  dataX, "dataXiter" + iter + "meanDeltaPostConvolve"); }
			if(TEST) { HelperUtils.compareResults( path, meanDelta, "meanDeltaiter0MeanDelta_float"); }
			if(TESTSMALL){ HelperUtils.compareResults( path, meanDelta, "meanDeltaiter0MeanDelta_float"); }
			if(TEST) { HelperUtils.compareResults( path, dataX, "dataXiter0meanDeltaPostConvolve_float_512_262144"); }
			if(TESTSMALL){ HelperUtils.compareResults( path, dataX, "dataXiter0meanDeltaPostConvolve_float_512_16384"); }

			if(showIteration){
				for (int k = 0; k < d; k++){
					ImageProcessor ip = stackOutTemp.getProcessor(k+1);
					float[] px = (float[])ip.getPixels();
					for (int j = 0; j < h; j++){
						for (int i = 0; i < w; i++){
							px[i + w*j] = dataX[k][i + w*j];
						}
					}
				}
				ImageProcessor ip1 = impOutTemp.getProcessor();
				ip1.setMinAndMax(0,0);
				ip1.setColorModel(cmY);
				impOutTemp.updateAndDraw();
				impOutTemp.setRoi(iOff,jOff,bw,bh);
			}
			//Energy sum to track convergence
			float sumPixels = 0;
			for (int k = 0; k < bd; k++){
				for (int j = 0; j < bh; j++){
					for (int i = 0; i < bw; i++){
						sumPixels = sumPixels + dataX[k+kOff][i + iOff + w*(j+jOff)];
					}
				}
			}
			if(GENERATEREFERENCE) { HelperUtils.debugWriteFloat( path, sumPixels, "sumPixelsTrack"); }
			if(TEST) { HelperUtils.compareResults( path, sumPixels, "sumPixelsTrack_float"); }
			if(TESTSMALL) { HelperUtils.compareResults( path, sumPixels, "sumPixelsTrack_float"); }

			float percentChange = 100*meanDelta/sumPixels;
			if(logMean)IJ.write(Float.toString(percentChange));
			if((oldPercentChange - percentChange) < changeThreshPercent){
				if(logMean)IJ.write("Automatically terminated after "+iter+" iterations.");
				break;
			}
			if((oldPercentChange < percentChange)&&detectDivergence){
				if(logMean)IJ.write("Automatically terminated due to divergence "+iter+" iterations.");
				break;
			}
			oldPercentChange = percentChange;
			IJ.showStatus(iter+" iterations complete.");
		}

		innerTimer=System.currentTimeMillis() - innerTimer;
		System.out.println("inner loop took " + (double)(innerTimer/1000) + " seconds for " + nIter + " iterations." );

		IJ.showStatus("Final filter.");
		FHT3D(dataX,w,h,d,false);
		if(GENERATEREFERENCE) { HelperUtils.debugWriteFloatArray( path,  dataX, "dataXFinalFHT3dfalse"); }
		if(TEST) { HelperUtils.compareResults( path, dataX, "dataXFinalFHT3dfalse_float_512_262144"); }
		if(TESTSMALL) { HelperUtils.compareResults( path, dataX, "dataXFinalFHT3dfalse_float_512_16384"); }

		//Gaussian filter and scaling
		for(int k = 0; k < d; k++){
			for (int j = 0; j < h; j++){
				for (int i = 0; i < w; i++){
					dataX[k][i + w*j] = dataX[k][i + w*j]*gk[k]*gj[j]*gi[i]/aSum;
				}
			}
		}
		if(GENERATEREFERENCE) { HelperUtils.debugWriteFloatArray( path,  dataX, "dataXFinalFilterScale" ); }
		if(TEST) { HelperUtils.compareResults( path, dataX, "dataXFinalFilterScale_float_512_262144"); }
		if(TESTSMALL) { HelperUtils.compareResults( path, dataX, "dataXFinalFilterScale_float_512_16384"); }

		FHT3D(dataX,w,h,d,true);

		if(GENERATEREFERENCE) { HelperUtils.debugWriteFloatArray( path,  dataX, "dataXFinalFHT3dtrue"); }
		if(TEST) { HelperUtils.compareResults( path, dataX, "dataXFinalFHT3dtrue_float_512_262144"); }	
		if(TESTSMALL) { HelperUtils.compareResults( path, dataX, "dataXFinalFHT3dtrue_float_512_16384"); }

		IJ.showStatus("Done");

		if(dB){
			toDB(dataAin, minA);
			toDB(dataYin, minY);
			toDB(dataX, -90);
		}

		timer =  System.currentTimeMillis() - timer;

		//Crop the output to the size of Yin
		if(showIteration)impOutTemp.hide();
		ImageStack stackOut = new ImageStack(bw,bh);
		for (int k = 0; k < bd; k++){
			ImageProcessor ip = new FloatProcessor(bw,bh);
			float[] px = (float[])ip.getPixels();
			for (int j = 0; j < bh; j++){
				for (int i = 0; i < bw; i++){
					px[i + bw*j] = dataX[k+kOff][i + iOff + w*(j+jOff)];
				}
			}
			stackOut.addSlice(null,ip);
		}
		ImagePlus impOut = new ImagePlus(titleOut+"_"+iter,stackOut);
		impOut.setSlice(bd/2+1);
		ImageProcessor ip = impOut.getProcessor();
		ip.setMinAndMax(0,0);
		ip.setColorModel(cmY);
		impOut.show();

		System.out.println("The computation took: "+( double ) ( timer/1000 )+" [Sec]");
		 
		fastHartleyTransform3D.release();
		fastHartleyTransform3D = null;


	}//run



	static void copyDataMask(int w, int h, int d,float[][] data,int wE,int hE,int dE,float[][] dataE){
		int kOff = (dE - d + 1)/2;
		int jOff = (hE - h + 1)/2;
		int iOff = (wE - w + 1)/2;
		for(int k = 0; k < d; k++){
			for (int j = 0; j < h; j++){
				for (int i = 0; i < w; i++){
					dataE[k+kOff][i+iOff + wE*(j+jOff)] = data[k][i + w*j];
				}
			}
		}
	}
	static void copyDataAverage(int w, int h, int d,int wE,int hE,int dE,float sum,
			float[][] dataIn,float[][] dataOut,float[][] result){
		int kOff = (dE - d + 1)/2;
		int jOff = (hE - h + 1)/2;
		int iOff = (wE - w + 1)/2;
		int iIn,jIn,kIn,iOut,jOut,kOut;
		float alphaI,alphaJ,alphaK;
		float a;
		for(int k = -kOff; k < dE-kOff; k++){
			kOut = k + kOff;
			if(k < 0){
				alphaK = -k/((float)kOff);
			}else if(k > (d-1)){
				alphaK = (k - d)/((float)kOff);
			}else{
				alphaK = 0;
			}
			for (int j = -jOff; j < hE-jOff; j++){
				jOut = j + jOff;
				if(j < 0){
					alphaJ = -j/((float)jOff);
				}else if(j > (h-1)){
					alphaJ = (j - h)/((float)jOff);
				}else{
					alphaJ = 0;
				}
				for (int i = -iOff; i < wE-iOff; i++){
					iOut = i + iOff;
					if(i < 0){
						alphaI = -i/((float)iOff);
					}else if(i > (w-1)){
						alphaI = (i - w)/((float)iOff);
					}else{
						alphaI = 0;
					}
					a = alphaK;
					if(alphaJ > a) a = alphaJ;
					if(alphaI > a) a = alphaI;
					result[kOut][iOut + wE*jOut] = (1-a)*dataIn[kOut][iOut + wE*jOut] +
					a*dataOut[kOut][iOut + wE*jOut]/sum;
				}
			}
		}
	}
	static void copyData(int w, int h, int d,float[][] data, float[][] data2){
		int wh = w*h;
		for(int k = 0; k < d; k++){
			for (int ind = 0; ind < wh; ind++){
				data2[k][ind] = data[k][ind];
			}
		}
	}
	static void copyDataMirror(int w, int h, int d,float[][] data,int wE,int hE,int dE,float[][] dataE){
		int kOff = (dE - d + 1)/2;
		int jOff = (hE - h + 1)/2;
		int iOff = (wE - w + 1)/2;
		int iIn,jIn,kIn,iOut,jOut,kOut;
		for(int k = -kOff; k < dE-kOff; k++){
			kOut = k + kOff;
			kIn = mirror(k,d);
			for (int j = -jOff; j < hE-jOff; j++){
				jOut = j + jOff;
				jIn = mirror(j,h);
				for (int i = -iOff; i < wE-iOff; i++){
					iOut = i + iOff;
					iIn = mirror(i,w);
					dataE[kOut][iOut + wE*jOut] = data[kIn][iIn + w*jIn];
				}
			}
		}
	}
	static int mirror(int i, int n){
		int ip = mod(i,2*n);
		if(ip < n){
			return ip;
		}else{
			return n - (ip % n) - 1;
		}
	}
	//A version of mod that is periodic for postive and negative i
	static int mod(int i, int n){
		return ((i % n) + n) % n;
	}
	static int expandedSize(int maxN){
		//Expand this to a power of 2 that is at least 1.5* as large, to avoid wrap effects
		//Start with 4 to avoid apparent normalization problems with n = 2
		int iN=4;
		if(maxN > 1){
			while(iN<1.5 * maxN) iN *= 2;
		}
		return iN;
	}
	static double unDB(float[][] x){
		double result = Float.MAX_VALUE;
		int n = x.length;
		for (int i = 0; i < n; i++){
			double ri = unDB(x[i]);
			if(ri < result) result = ri;
		}
		return result;
	}
	static double unDB(float[] x){
		double SCALE = 10/Math.log(10);
		int n = x.length;
		double result = Float.MAX_VALUE;
		for (int i = 0; i < n; i++){
			if(x[i] < result) result = x[i];
			x[i] = (float)Math.exp(x[i]/SCALE);
		}
		return result;
	}
	static void toDB(float[][] x, double minDB){
		double SCALE = 10/Math.log(10);
		int n = x.length;
		for (int i = 0; i < n; i++){
			toDB(x[i], minDB);
		}
	}
	static void toDB(float[] x, double minDB){
		double SCALE = 10/Math.log(10);
		double minVal = Math.exp(minDB/SCALE);
		int n = x.length;
		for (int i = 0; i < n; i++){
			if(x[i] > minVal)
				x[i] = (float)(SCALE*Math.log(x[i]));
			else
				x[i] = (float)minDB;
		}
	}
	static void swapQuadrants(int w,int h,int d,float[][] x){
		int k1P,k2P,k3P;
		float temp;
		int wHalf = w/2;
		int hHalf = h/2;
		int dHalf = d/2;
		//Shift by half of the grid, less one pixel, in each direction
		for(int k3 = 0; k3 < dHalf; k3++){
			k3P = k3 + dHalf;
			for (int k2 = 0; k2 < h; k2++){
				for (int k1 = 0; k1 < w; k1++){
					temp = x[k3][k1 + w*k2];
					x[k3][k1 + w*k2] = x[k3P][k1 + w*k2];
					x[k3P][k1 + w*k2] = temp;
				}
			}
		}
		for(int k2 = 0; k2 < hHalf; k2++){
			k2P = k2 + hHalf;
			for (int k3 = 0; k3 < d; k3++){
				for (int k1 = 0; k1 < w; k1++){
					temp = x[k3][k1 + w*k2];
					x[k3][k1 + w*k2] = x[k3][k1 + w*k2P];
					x[k3][k1 + w*k2P] = temp;
				}
			}
		}
		for(int k1 = 0; k1 < wHalf; k1++){
			k1P = k1 + wHalf;
			for (int k2 = 0; k2 < h; k2++){
				for (int k3 = 0; k3 < d; k3++){
					temp = x[k3][k1 + w*k2];
					x[k3][k1 + w*k2] = x[k3][k1P + w*k2];
					x[k3][k1P + w*k2] = temp;
				}
			}
		}
	}
	static void convolveFD(int w,int h,int d,float[][] h1,float[][] h2, float[][] result){
		int k1C,k2C,k3C;
		double h2e,h2o;
		for(int k3 = 0; k3 < d; k3++){
			k3C = (d - k3) % d;
			for (int k2 = 0; k2 < h; k2++){
				k2C = (h - k2) % h;
				for (int k1 = 0; k1 < w; k1++){
					k1C = (w - k1) % w;
					h2e = (h2[k3][k1 + w*k2] + h2[k3C][k1C + w*k2C])/2;
					h2o = (h2[k3][k1 + w*k2] - h2[k3C][k1C + w*k2C])/2;
					result[k3][k1 + w*k2] = (float)(h1[k3][k1 + w*k2]*h2e + h1[k3C][k1C + w*k2C]*h2o);
				}
			}
		}
	}
	static void deconvolveFD(float gamma, double magMax, int w,int h,int d,float[][] h1,float[][] h2, float[][] result){
		int k1C,k2C,k3C;
		double mag,h2e,h2o;
		double gammaScaled = gamma*magMax;
		for(int k3 = 0; k3 < d; k3++){
			k3C = (d - k3) % d;
			for (int k2 = 0; k2 < h; k2++){
				k2C = (h - k2) % h;
				for (int k1 = 0; k1 < w; k1++){
					k1C = (w - k1) % w;
					h2e = (h2[k3][k1 + w*k2] + h2[k3C][k1C + w*k2C])/2;
					h2o = (h2[k3][k1 + w*k2] - h2[k3C][k1C + w*k2C])/2;
					mag =h2[k3][k1 + w*k2]*h2[k3][k1 + w*k2] + h2[k3C][k1C + w*k2C]*h2[k3C][k1C + w*k2C];
					double tmp = h1[k3][k1 + w*k2]*h2e - h1[k3C][k1C + w*k2C]*h2o;
					result[k3][k1 + w*k2] = (float)(tmp/(mag+gammaScaled));
				}
			}
		}
	}
	static double findMagMax(int w,int h,int d,float[][] h2){
		int k1C,k2C,k3C;
		double magMax = 0;
		double mag;
		for(int k3 = 0; k3 < d; k3++){
			k3C = (d - k3) % d;
			for (int k2 = 0; k2 < h; k2++){
				k2C = (h - k2) % h;
				for (int k1 = 0; k1 < w; k1++){
					k1C = (w - k1) % w;
					mag =h2[k3][k1 + w*k2]*h2[k3][k1 + w*k2] + h2[k3C][k1C + w*k2C]*h2[k3C][k1C + w*k2C];
					if(mag > magMax) magMax = mag;
				}
			}
		}
		return magMax;
	}
	public static boolean powerOf2Size(int w) {
		int i=2;
		while(i<w) i *= 2;
		return i==w;
	}


	public synchronized static void FHT3D(float[][] data,int w, int h, int d, boolean inverse) 
	{
		if (USEOPENCL)
		{
			if ( fastHartleyTransform3D == null )
			{	
				long time = System.currentTimeMillis();
				fastHartleyTransform3D =  new FHT3DExample( w, h, d );
				time = System.currentTimeMillis() - time;
				times[0] += time;
			}

			long time = System.currentTimeMillis();
			long executionTime = fastHartleyTransform3D.run( data, inverse );
			time = System.currentTimeMillis() - time;
			times[1] += time;
			times[2] += executionTime;
			times[3] ++;
			//FastHartleyTransform3D.run(data, w, h, d, inverse);
		} else {
			long time = System.currentTimeMillis();

			float[] sw = new float[w/4];
			float[] cw = new float[w/4];
			float[] sh = new float[h/4];
			float[] ch = new float[h/4];
			makeSinCosTables(w,sw,cw);
			makeSinCosTables(h,sh,ch);
			for (int i = 0; i < d; i++){
				rc2DFHT(data[i], w, h, sw, cw, sh, ch);
			}
			float[] u = new float[d];
			if(powerOf2Size(d)){
				float[] s = new float[d/4];
				float[] c = new float[d/4];
				makeSinCosTables(d,s,c);
				for(int k2 = 0; k2 < h; k2++){
					for(int k1 = 0; k1 < w; k1++){
						int ind = k1 + k2*w;
						for(int k3 = 0; k3 < d; k3++){
							u[k3] = data[k3][ind];
						}
						dfht3(u, 0, d, s, c);
						for(int k3 = 0; k3 < d; k3++){
							data[k3][ind] = u[k3];
						}
					}
				}
			}else{
				float[] cas = hartleyCoefs(d);
				float[] work = new float[d];
				for(int k2 = 0; k2 < h; k2++){
					for(int k1 = 0; k1 < w; k1++){
						int ind = k1 + k2*w;
						for(int k3 = 0; k3 < d; k3++){
							u[k3] = data[k3][ind];
						}
						slowHT(u,cas,d,work);
						for(int k3 = 0; k3 < d; k3++){
							data[k3][ind] = u[k3];
						}
					}
				}
			}
			//Convert to actual Hartley transform
			float A,B,C,D,E,F,G,H;
			int k1C,k2C,k3C;
			for(int k3 = 0; k3 <= d/2; k3++){
				k3C = (d - k3) % d;
				for(int k2 = 0; k2 <= h/2; k2++){
					k2C = (h - k2) % h;
					for (int k1 = 0; k1 <= w/2; k1++){
						k1C = (w - k1) % w;
						A = data[k3][k1 + w*k2C];
						B = data[k3][k1C + w*k2];
						C = data[k3C][k1 + w*k2];
						D = data[k3C][k1C + w*k2C];
						E = data[k3C][k1 + w*k2C];
						F = data[k3C][k1C + w*k2];
						G = data[k3][k1 + w*k2];
						H = data[k3][k1C + w*k2C];
						data[k3][k1 + w*k2] = (A+B+C-D)/2;
						data[k3C][k1 + w*k2] = (E+F+G-H)/2;
						data[k3][k1 + w*k2C] = (G+H+E-F)/2;
						data[k3C][k1 + w*k2C] = (C+D+A-B)/2;
						data[k3][k1C + w*k2] = (H+G+F-E)/2;
						data[k3C][k1C + w*k2] = (D+C+B-A)/2;
						data[k3][k1C + w*k2C] = (B+A+D-C)/2;
						data[k3C][k1C + w*k2C] = (F+E+H-G)/2;
					}
				}
			}
			if(inverse){
				//float norm = (float)Math.sqrt(d*h*w);
				float norm = d*h*w;
				for(int k3 = 0; k3 < d; k3++){
					for(int k2 = 0; k2 < h; k2++){
						for (int k1 = 0; k1 < w; k1++){
							data[k3][k1 + w*k2] = data[k3][k1 + w*k2] / norm;
						}
					}
				}
			}
			time = System.currentTimeMillis() - time;
			times[3] ++;
			times[4] += time;
		}
	}
	static float[] hartleyCoefs(int max){
		float[] cas = new float[max*max];
		int ind = 0;
		for(int n = 0; n < max; n++){
			for (int k = 0; k < max; k++){
				double arg = (2*Math.PI*k*n)/max;
				cas[ind++] = (float)(Math.cos(arg) + Math.sin(arg));
			}
		}
		return cas;
	}
	static void slowHT(float[] u, float[] cas, int max, float[] work){
		int ind = 0;
		for(int k = 0; k < max; k++){
			float sum = 0;
			for(int n = 0; n < max; n++){
				sum += u[n]*cas[ind++];
			}
			work[k] = sum;
		}
		for (int k = 0; k < max; k++){
			u[k] = work[k];
		}
	}
	static void makeSinCosTables(int maxN, float[] s, float[] c) {
		int n = maxN/4;
		double theta = 0.0;
		double dTheta = 2.0 * Math.PI/maxN;
		for (int i=0; i<n; i++) {
			c[i] = (float)Math.cos(theta);
			s[i] = (float)Math.sin(theta);
			theta += dTheta;
		}
	}
	/** Row-column Fast Hartley Transform */
	static void rc2DFHT(float[] x, int w, int h, float[] sw, float[] cw, float[] sh, float[] ch) {
		for (int row=0; row<h; row++)
			dfht3(x, row*w, w, sw, cw);
		float[] temp = new float[h];
		for(int col = 0; col < w; col++){
			for (int row = 0; row < h; row++){
				temp[row] = x[col + w*row];
			}
			dfht3(temp, 0, h, sh, ch);
			for (int row = 0; row < h; row++){
				x[col + w*row] = temp[row];
			}
		}
	}
	/* An optimized real FHT */
	static void dfht3 (float[] x, int base, int maxN, float[] s, float[] c) {
		int i, stage, gpNum, gpIndex, gpSize, numGps, Nlog2;
		int bfNum, numBfs;
		int Ad0, Ad1, Ad2, Ad3, Ad4, CSAd;
		float rt1, rt2, rt3, rt4;

		Nlog2 = log2(maxN);
		BitRevRArr(x, base, Nlog2, maxN);	//bitReverse the input array
		gpSize = 2;     //first & second stages - do radix 4 butterflies once thru
		numGps = maxN / 4;
		for (gpNum=0; gpNum<numGps; gpNum++)  {
			Ad1 = gpNum * 4;
			Ad2 = Ad1 + 1;
			Ad3 = Ad1 + gpSize;
			Ad4 = Ad2 + gpSize;
			rt1 = x[base+Ad1] + x[base+Ad2];   // a + b
			rt2 = x[base+Ad1] - x[base+Ad2];   // a - b
			rt3 = x[base+Ad3] + x[base+Ad4];   // c + d
			rt4 = x[base+Ad3] - x[base+Ad4];   // c - d
			x[base+Ad1] = rt1 + rt3;      // a + b + (c + d)
			x[base+Ad2] = rt2 + rt4;      // a - b + (c - d)
			x[base+Ad3] = rt1 - rt3;      // a + b - (c + d)
			x[base+Ad4] = rt2 - rt4;      // a - b - (c - d)
		}
		if (Nlog2 > 2) {
			// third + stages computed here
			gpSize = 4;
			numBfs = 2;
			numGps = numGps / 2;
			//IJ.write("FFT: dfht3 "+Nlog2+" "+numGps+" "+numBfs);
			for (stage=2; stage<Nlog2; stage++) {
				for (gpNum=0; gpNum<numGps; gpNum++) {
					Ad0 = gpNum * gpSize * 2;
					Ad1 = Ad0;     // 1st butterfly is different from others - no mults needed
					Ad2 = Ad1 + gpSize;
					Ad3 = Ad1 + gpSize / 2;
					Ad4 = Ad3 + gpSize;
					rt1 = x[base+Ad1];
					x[base+Ad1] = x[base+Ad1] + x[base+Ad2];
					x[base+Ad2] = rt1 - x[base+Ad2];
					rt1 = x[base+Ad3];
					x[base+Ad3] = x[base+Ad3] + x[base+Ad4];
					x[base+Ad4] = rt1 - x[base+Ad4];
					for (bfNum=1; bfNum<numBfs; bfNum++) {
						// subsequent BF's dealt with together
						Ad1 = bfNum + Ad0;
						Ad2 = Ad1 + gpSize;
						Ad3 = gpSize - bfNum + Ad0;
						Ad4 = Ad3 + gpSize;

						CSAd = bfNum * numGps;
						rt1 = x[base+Ad2] * c[CSAd] + x[base+Ad4] * s[CSAd];
						rt2 = x[base+Ad4] * c[CSAd] - x[base+Ad2] * s[CSAd];

						x[base+Ad2] = x[base+Ad1] - rt1;
						x[base+Ad1] = x[base+Ad1] + rt1;
						x[base+Ad4] = x[base+Ad3] + rt2;
						x[base+Ad3] = x[base+Ad3] - rt2;

					} /* end bfNum loop */
				} /* end gpNum loop */
				gpSize *= 2;
				numBfs *= 2;
				numGps = numGps / 2;
			} /* end for all stages */
		} /* end if Nlog2 > 2 */
	}
	static int log2 (int x) 
	{
		int count = 15;
		while (!btst(x, count))
			count--;
		return count;
	}
	private static boolean btst (int  x, int bit) {
		//int mask = 1;
		return ((x & (1<<bit)) != 0);
	}
	static void BitRevRArr (float[] x, int base, int bitlen, int maxN) {
		int    l;
		float[] tempArr = new float[maxN];
		for (int i=0; i<maxN; i++)  {
			l = BitRevX (i, bitlen);  //i=1, l=32767, bitlen=15
			tempArr[i] = x[base+l];
		}
		for (int i=0; i<maxN; i++)
			x[base+i] = tempArr[i];
	}
	private static int BitRevX (int  x, int bitlen) {
		int  temp = 0;
		for (int i=0; i<=bitlen; i++)
			if ((x & (1<<i)) !=0)
				temp  |= (1<<(bitlen-i-1));
		return temp & 0x0000ffff;
	}
	private int bset (int x, int bit) {
		x |= (1<<bit);
		return x;
	}
	static public void showAbout(){
		IJ.showMessage( "About Iterative Decon 3D ...",
				"Iterative convolution and positive deconvolution\n" +
		"in 3D");
	}

}
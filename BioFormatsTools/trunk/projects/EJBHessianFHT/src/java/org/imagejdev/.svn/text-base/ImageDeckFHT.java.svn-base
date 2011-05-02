/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.imagejdev;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice.Type;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLMemory.Mem;
import com.jogamp.opencl.CLProgram;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 *
 * @author rick
 */
public class ImageDeckFHT {

    private CLContext context;
    private CLBuffer<FloatBuffer> clFloatBufferDATA;
    private CLBuffer<FloatBuffer> clFloatBufferS;
    private CLBuffer<FloatBuffer> clFloatBufferC;
    private CLBuffer<FloatBuffer> clFloatBufferSW;
    private CLBuffer<FloatBuffer> clFloatBufferCW;
    private CLBuffer<FloatBuffer> clFloatBufferSH;
    private CLBuffer<FloatBuffer> clFloatBufferCH;
    private CLProgram programFHT3D;
    private CLKernel kernelsw;
    private CLKernel kernelsh;
    private CLKernel kernels;
    private CLKernel kernelf;
    private CLCommandQueue queue;
    private int globalWorkSize = 1;
    private int localWorkSize = 1;
    private int maxComputeUnits = 0;
    private FloatBuffer dataA;
    private int w = 0;
    private int h = 0;
    private int d = 0;
    private int inverse = 0;

    //used to release contents
     protected void release() {
        dataA = null;
        queue.release();
    }

     /**
      * This object only supports power of 2 size images with w, d, and h < 1000
      * Constructor allows reuse based on image deck size:
      * Creates the sin and cos tables, allocates arrays,
      * 
      * @param w - width of image
      * @param h - height of image
      * @param d - depth of image
      */
     public ImageDeckFHT(int w, int h, int d) throws Exception
    {
        //Check declared limitations
        assert (w == h);
        assert (h == d);
        assert (powerOf2Size(d));
        assert (d <= 1024);

        //set the dimensions for reuse
        this.w = w;
        this.h = h;
        this.d = d;

        //Get the GPU
        this.context = CLContext.create(Type.GPU).getContext();

        //Create the program context
        this.programFHT3D = context.createProgram(ImageDeckFHT.class.getResourceAsStream("fht.cl")).build();
        
        //create the kernels
        this.kernelsw = programFHT3D.createCLKernel("fhtsw");
        this.kernelsh = programFHT3D.createCLKernel("fhtsh");
        this.kernels = programFHT3D.createCLKernel("fhts");
        this.kernelf = programFHT3D.createCLKernel("fhtf");

       //use the fasted device
        this.queue = context.getMaxFlopsDevice().createCommandQueue(CLCommandQueue.Mode.PROFILING_MODE);

       //determine the number of compute units to divide the problem space over
        maxComputeUnits = queue.getDevice().getMaxComputeUnits();

        //allocate the arrays for the server
        this.dataA = ByteBuffer.allocateDirect(d * w * h * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        FloatBuffer sw = ByteBuffer.allocateDirect(w).order(ByteOrder.nativeOrder()).asFloatBuffer();
        FloatBuffer cw = ByteBuffer.allocateDirect(w).order(ByteOrder.nativeOrder()).asFloatBuffer();
        FloatBuffer sh = ByteBuffer.allocateDirect(h).order(ByteOrder.nativeOrder()).asFloatBuffer();
        FloatBuffer ch = ByteBuffer.allocateDirect(h).order(ByteOrder.nativeOrder()).asFloatBuffer();
        FloatBuffer s = ByteBuffer.allocateDirect(d).order(ByteOrder.nativeOrder()).asFloatBuffer();
        FloatBuffer c = ByteBuffer.allocateDirect(d).order(ByteOrder.nativeOrder()).asFloatBuffer();

        //generate the sin/cos tables
        makeSinCosTables(d, s, c);
        makeSinCosTables(w, sw, cw);
        makeSinCosTables(h, sh, ch);

       //allocate the buffers needed on the GPU
        clFloatBufferC = context.createBuffer(c, Mem.READ_ONLY);
        clFloatBufferS = context.createBuffer(s, Mem.READ_ONLY);
        clFloatBufferCW = context.createBuffer(cw, Mem.READ_ONLY);
        clFloatBufferSW = context.createBuffer(sw, Mem.READ_ONLY);
        clFloatBufferCH = context.createBuffer(ch, Mem.READ_ONLY);
        clFloatBufferSH = context.createBuffer(sh, Mem.READ_ONLY);
        clFloatBufferDATA = context.createFloatBuffer(d * w * h, Mem.READ_WRITE);

       //bind the OpenCL progam with the arguments
        kernels.setArg(0, clFloatBufferDATA);
        kernelsw.setArg(0, clFloatBufferDATA);
        kernelsh.setArg(0, clFloatBufferDATA);
        kernelf.setArg(0, clFloatBufferDATA);
        kernels.setArg(1, clFloatBufferS);
        kernels.setArg(2, clFloatBufferC);
        kernelsw.setArg(1, clFloatBufferSW);
        kernelsw.setArg(2, clFloatBufferCW);
        kernelsh.setArg(1, clFloatBufferSH);
        kernelsh.setArg(2, clFloatBufferCH);
        kernelsw.setArg(3, w);
        kernelsw.setArg(4, h);
        kernelsw.setArg(5, d);
        kernels.setArg(3, w);
        kernels.setArg(4, h);
        kernels.setArg(5, d);
        kernelsh.setArg(3, w);
        kernelsh.setArg(4, h);
        kernelsh.setArg(5, d);
        kernelf.setArg(1, w);
        kernelf.setArg(2, h);
        kernelf.setArg(3, d);
        kernels.setArg(6, maxComputeUnits);
        kernelsw.setArg(6, maxComputeUnits);
        kernelsh.setArg(6, maxComputeUnits);
        kernelf.setArg(4, maxComputeUnits);

        //allocate the shared memory
        kernelsh.setNullArg(7, h * 4);
        kernelsh.setNullArg(8, h);
        kernelsh.setNullArg(9, h);
        kernelsw.setNullArg(7, w * 4);
        kernelsw.setNullArg(8, w);
        kernelsw.setNullArg(9, w);
        kernels.setNullArg(7, d * 4);
        kernels.setNullArg(8, d);
        kernels.setNullArg(9, d);

        //copy the static buffers to the allocated GPU memory
        queue.putWriteBuffer(clFloatBufferC, false);
        queue.putWriteBuffer(clFloatBufferS, false);
        queue.putWriteBuffer(clFloatBufferCW, false);
        queue.putWriteBuffer(clFloatBufferSW, false);
        queue.putWriteBuffer(clFloatBufferCH, false);
        queue.putWriteBuffer(clFloatBufferSH, false);

     } //end constructor


      /**
       * Determines the FHT of the image deck

       * @param data - image plane by N images in deck
       * @param inverse - if inverse FHT stage needs to be computed
       */
     public synchronized void run(float[][] data, boolean inverse) {
      //copy data into the FloatBuffer
       for (int i = 0; i < d; i++) {
            for (int j = 0; j < w * h; j++) {
                dataA.put(i * w * h + j, data[i][j]);
            }
        }

        //Copy to GPU and executeKernelComputation
        run(dataA, inverse);

        //Copy results back to 2d array
        for (int i = 0; i < d; i++) {
            for (int j = 0; j < w * h; j++) {
                data[i][j] = dataA.get(i * w * h + j);
            }
        }
    }

      /**
       * Copy data to device and executeKernelComputation FHT kernels on image deck
       * @param data - Serialized 2D float array to be computed
       *     Serialized Format: starts in upper left of top image
       *     For each image plane,
       *        width row 1, row 2, ...  Repeats for each image plane.

       * @param inverse - true if inverse step is needed
       */
    public synchronized void run(FloatBuffer data, boolean inverse) {

        //bind cl buffer to the FloatBuffer
        clFloatBufferDATA.use(data);

        //update status of inverse flag
       this.inverse = (inverse == true) ? 1 : 0;

       //copy the bound FloatBuffer from host memory to the device
       queue.putWriteBuffer(clFloatBufferDATA, false);

       //now that all data is on the device and all parameters are
       //bound to the kernels, start the GPU computation
        executeKernelComputation();

        //copy the computed results from the GPU to the host memory
        queue.putReadBuffer(clFloatBufferDATA, true);
    }


    private synchronized void executeKernelComputation() {

        //update the kernl parameter (this changes every time)
        kernelf.setArg(5, inverse);

        //determine variables used by the FHT kernels
        globalWorkSize = w * maxComputeUnits;
        localWorkSize = w;

        //put the data on the card
        queue.putWriteBuffer(clFloatBufferDATA, false);

        //run the first compute stage
        queue.put1DRangeKernel(kernelsw, 0, globalWorkSize, localWorkSize);

        //run the second compute stage
        queue.put1DRangeKernel(kernelsh, 0, globalWorkSize, localWorkSize);

       //run the third compute stage
        queue.put1DRangeKernel(kernels, 0, globalWorkSize, localWorkSize);

        //run the fourth compute stage
        queue.put1DRangeKernel(kernelf, 0, globalWorkSize, localWorkSize);

        //wait for stages to complete in order
        queue.finish();

    }


     /**
      * Generates the sin/cos tables
      * @param maxN
      * @param s - Sin storage object
      * @param c - Cos storage object
      */
     public static void makeSinCosTables(int maxN, FloatBuffer s, FloatBuffer c) {
        int n = maxN / 4;
        double theta = 0.0;
        double dTheta = 2.0 * Math.PI / maxN;
        for (int i = 0; i < n; i++) {
            c.put(i, (float) Math.cos(theta));
            s.put(i, (float) Math.sin(theta));
            theta += dTheta;
        }
    }


     /**
      * Determines if the parameter is of a power of 2
      * @param w - integer to be evaluated
      * @return
      */
         public static boolean powerOf2Size(int w) {
        {
            int i = 2;
            while (i < w) {
                i = i * 2;
            }
            return i == w;
        }
    }

    //         private String kernelFileAsString = "";
}

package demos;
// Credit to Michael Bien (http://michael-bien.com/)

import com.jogamp.opencl.CL;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLException;
import com.jogamp.opencl.CLPlatform;
import com.jogamp.opencl.util.CLUtil;
import java.nio.ByteBuffer;
import com.jogamp.common.nio.Buffers;
import com.jogamp.common.nio.PointerBuffer;
import java.nio.IntBuffer;
import org.junit.BeforeClass;
import org.junit.Test;

import static java.lang.System.*;

/**
 * This test was authored by Michael Bien to help identify a platform specific bug
 * identified only on OSX (specifically 10.6)
 * @author Michael Bien
 *
 */
public class ProgramTest {

    @BeforeClass
    public synchronized static void setUpClass() throws Exception {
        out.println("OS: " + System.getProperty("os.name"));
        out.println("ARCH: " + System.getProperty("os.arch"));
        out.println("VM: " + System.getProperty("java.vm.name"));
        out.println("lib path: " + System.getProperty("java.library.path"));
    }
    
    private final static String programSource =""
//             + " #pragma OPENCL EXTENSION cl_khr_fp64: enable   \n"
             +"   __kernel void sobel( __global float* input, __global float* output, int width,  int height ) {     \n"
             +"       int x = get_global_id(0);   \n"
             +"       int y = get_global_id(1);   \n"
             +"       int offset = y * width + x;   \n"
             +"                                                               \n"
             +"         float p0, p1, p2, p3, p5, p6, p7, p8 = 0;            \n"
             +"                                                               \n"
             +"                                                               \n"
             +"              if( x < 1 || y < 1 || x > width - 2 || y > height - 2 )   \n"
             +"              {   \n"
             +"                 output[offset] = 0;   \n"
             +"              }   \n"
             +"              else   \n"
             +"              {   \n"
             +"                  p0 = input[offset - width - 1] ;   \n"
             +"                  p1 = input[offset - width] ;   \n"
             +"                  p2 = input[offset - width + 1] ;   \n"
             +"                  p3 = input[offset - 1] ;   \n"
             +"                  p5 = input[offset + 1] ;   \n"
             +"                  p6 = input[offset + width - 1] ;   \n" 
             +"                  p7 = input[offset + width] ;   \n"
             +"                  p8 = input[offset + width + 1] ;   \n" 
             +"   \n"
             +"                  float sum1 = p0 + 2*p1 + p2 - p6 - 2*p7 - p8;  //GY   \n"
             +"                  float sum2 = p0 + 2*p3 + p6 - p2 - 2*p5 - p8;  //GX   \n"
             +"   \n"
             +"                  output[offset] = sqrt(  sum1*sum1 + sum2*sum2 );   \n"
             +"              }   \n"
             +"      }  "; 

    @Test
    public synchronized void buildProgramTest() {
        
        CLContext context = CLContext.create();
        
        try {
            System.out.println(context);
            System.out.println(context.getPlatform().getVersion());

            long contextID = context.ID;
            CL cl = CLPlatform.getLowLevelCLInterface();

            PointerBuffer buffer = (PointerBuffer) PointerBuffer.allocateDirect(1).put(programSource.length());
            String[] srcArray = new String[]{programSource};

            IntBuffer uploadStatus = Buffers.newDirectIntBuffer(1);
            final long programID = cl.clCreateProgramWithSource(contextID, 1, srcArray, buffer, uploadStatus);
            checkError("on clCreateProgramWithSource", uploadStatus.get(0));

            // Build the program
            int buildStatus = cl.clBuildProgram(programID, 0, null, null, null);

            System.out.println("please ignore "+srcArray+ "" + buffer); // please ignore, just a artificial reference lock 
            
            System.out.println("src: " + getProgramInfoString(cl, programID, CL.CL_PROGRAM_SOURCE));
            
            checkError("on clBuildProgram", buildStatus);
        } finally {
            context.release();
            System.out.println("-> success");
        }
    }
    
    private synchronized String getProgramInfoString(CL cl, long program, int flag) {

        PointerBuffer size = PointerBuffer.allocateDirect(1);

        int ret = cl.clGetProgramInfo(program, flag, 0, null, size);
        checkError("on clGetProgramInfo", ret);

        ByteBuffer buffer = Buffers.newDirectByteBuffer((int)size.get(0));

        ret = cl.clGetProgramInfo(program, flag, buffer.capacity(), buffer, null);
        checkError("on clGetProgramInfo", ret);

        return CLUtil.clString2JavaString(buffer, (int)size.get(0));
    }

    private synchronized void checkError(String msg, int ret) {
        if(ret != CL.CL_SUCCESS)
            throw CLException.newException(ret, msg);
    }
}
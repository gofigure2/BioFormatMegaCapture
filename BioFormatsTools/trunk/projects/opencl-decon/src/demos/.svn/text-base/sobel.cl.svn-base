//#pragma OPENCL EXTENSION cl_khr_fp64: enable
__kernel void sobel( __global float* input,
	__global float* output,
     int width,
     int height )
{  
    int x = get_global_id(0);
    int y = get_global_id(1);
    int offset = y * width + x;
    
    float p0, p1, p2, p3, p5, p6, p7, p8 = 0;
    
    
	if( x < 1 || y < 1 || x > width - 2 || y > height - 2 )
	{
	  output[offset] = 0; 
	}
	else
	{
	    p0 = input[offset - width - 1] ;
	    p1 = input[offset - width] ;
	    p2 = input[offset - width + 1] ;
	    p3 = input[offset - 1] ;
	    p5 = input[offset + 1] ;
	    p6 = input[offset + width - 1] ;
	    p7 = input[offset + width] ;
	    p8 = input[offset + width + 1] ;
	
		// you can use double here
	    float sum1 = p0 + 2*p1 + p2 - p6 - 2*p7 - p8;  //GY
	    float sum2 = p0 + 2*p3 + p6 - p2 - 2*p5 - p8;  //GX
	   
	    output[offset] = sqrt(  sum1*sum1 + sum2*sum2 );
	}
} 

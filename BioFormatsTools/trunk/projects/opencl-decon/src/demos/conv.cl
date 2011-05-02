__kernel void conv( 
	__global float* image,
	__global float* kernelMatrix,
	__global float* result,
     int imageWidth,
     int imageHeight,
     int imageDepth,
     int kernelWidth,
     int kernelHeight,
     int kernelDepth )
{  		
	// get the thread id from the 2D coordinates
    int oclW = get_global_id(0);
    int oclH = get_global_id(1);
    
	// boundary
	int boundaryW = (kernelWidth-1)/2;
	int boundaryH = (kernelHeight-1)/2;
	int boundaryD = (kernelDepth-1)/2;
    
    int oclD = 0;
  	for ( oclD = 0; oclD < imageDepth; oclD++) 
  	{
		// sum the values
		result[oclW * imageHeight * imageDepth + oclH * imageDepth + oclD] = 0;

   	}
} 



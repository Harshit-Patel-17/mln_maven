package iitd.data_analytics.mln.gpu;

import static jcuda.driver.JCudaDriver.*;
import jcuda.*;
import jcuda.driver.*;

class GpuUtil
{
  public GpuUtil()
  {
    // Load the ptx file.
    /*module = new CUmodule();
    cuModuleLoad(module, GpuConfig.ptxBase + "utilCudaKernels.ptx");*/
  }

  public void parallelInit(CUdeviceptr d_A, long size, int val, int maxThreads, int gpuNo)
  {
    // Obtain a function pointer to the kernel function
    CUfunction function = new CUfunction();
    cuModuleGetFunction(function, GpuConfig.utilCudaKernels[gpuNo], "initKernel");

    Pointer kernelParameters = Pointer.to(
      Pointer.to(d_A),
      Pointer.to(new long[]{size}),
      Pointer.to(new int[]{val})
    );

    int blockSizeX = Math.min(maxThreads, (int)size);
    int gridSizeX = ((int)size + blockSizeX - 1) / blockSizeX;
    /*System.out.println("Grid size: " + gridSizeX + ", Block size: " + blockSizeX + " :: parallelInit");*/

    cuLaunchKernel(function,
      gridSizeX, 1, 1,
      blockSizeX, 1, 1,
      0, null,
      kernelParameters, null
    );
    cuCtxSynchronize();
  }

  public int parallelSum(CUdeviceptr d_A, long size, int maxThreads, int gpuNo)
  {
    // Obtain a function pointer to the kernel function
    CUfunction function = new CUfunction();
    cuModuleGetFunction(function, GpuConfig.utilCudaKernels[gpuNo], "sumKernel");

    if(size == 0) return 0;

    while(size != 1)
    {
      Pointer kernelParameters = Pointer.to(
        Pointer.to(d_A),
        Pointer.to(new long[]{size})
      );

      int blockSizeX = Math.min(maxThreads, (int)size);
      int gridSizeX = ((int)size + blockSizeX - 1) / blockSizeX;
      /*System.out.println("Grid size: " + gridSizeX + ", Block size: " + blockSizeX + " :: parallelSum");*/

      cuLaunchKernel(function,
        gridSizeX, 1, 1,
        blockSizeX, 1, 1,
        maxThreads * Sizeof.INT, null,
        kernelParameters, null
      );
      cuCtxSynchronize();

      regularCompact(d_A, size, maxThreads, maxThreads, gpuNo);
      size = gridSizeX;
    }

    int[] ans = new int[1];
    assert cuMemcpyDtoH(Pointer.to(ans), d_A, Sizeof.INT) == CUresult.CUDA_SUCCESS;

    return ans[0];
  }

  public void regularCompact(CUdeviceptr d_A, long size, int intervalSize, int maxThreads, int gpuNo)
  {
    // Obtain a function pointer to the kernel function
    CUfunction function = new CUfunction();
    cuModuleGetFunction(function, GpuConfig.utilCudaKernels[gpuNo], "regularCompactKernel");

    long newSize = (size - 1) / intervalSize + 1;

    CUdeviceptr d_B = new CUdeviceptr();
    assert cuMemAlloc(d_B, newSize * Sizeof.INT) == CUresult.CUDA_SUCCESS;

    Pointer kernelParameters = Pointer.to(
      Pointer.to(d_A),
      Pointer.to(d_B),
      Pointer.to(new long[]{newSize}),
      Pointer.to(new long[]{intervalSize})
    );

    int blockSizeX = Math.min(maxThreads, (int)newSize);
    int gridSizeX = ((int)newSize + blockSizeX - 1) / blockSizeX;
    /*System.out.println("Grid size: " + gridSizeX + ", Block size: " + blockSizeX + " :: regularCompact");*/

    cuLaunchKernel(function,
      gridSizeX, 1, 1,
      blockSizeX, 1, 1,
      0, null,
      kernelParameters, null
    );
    cuCtxSynchronize();

    assert cuMemcpyDtoD(d_A, d_B, newSize * Sizeof.INT) == CUresult.CUDA_SUCCESS;

    cuMemFree(d_B);
  }
}
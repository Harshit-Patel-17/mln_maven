extern "C"
__global__ void initDbIndexKernel(int totalVars, int totalPreds, int *d_varDomainSizes,
                                  int *d_predBaseIdx, int *d_predVarMat, int *d_dbIndex,
                                  long totalGroundings)
{
  long idx = blockIdx.x * blockDim.x + threadIdx.x;

  if(idx < totalGroundings)
    {
      long baseDbIndex = idx * totalPreds;
      for(int i = 0; i < totalPreds; i++)
	  d_dbIndex[baseDbIndex + i] = d_predBaseIdx[i];

      long n = idx;
      for(int i = totalVars-1; i >= 0; i--)
	{
	  int domainSize = d_varDomainSizes[i];
	  long temp = n / domainSize;
	  int val = n - temp * domainSize;
	  n = temp;

	  int basePredVarMatIndex = i * totalPreds;
	  for(int j = 0; j < totalPreds; j++)
	      d_dbIndex[baseDbIndex + j] += d_predVarMat[basePredVarMatIndex + j] * val;
	}
    }
}

extern "C"
__global__ void evalClauseKernel(int *d_satArray, int **d_interpretation, int *dbIndex,
                                 int *d_predicates, int *d_valTrue, int totalPreds, long totalGroundings)
{
  long idx = blockIdx.x * blockDim.x + threadIdx.x;

  if(idx < totalGroundings && d_satArray[idx] == 1)
    {
      long baseDbIndex = idx * totalPreds;
      int sat = 0;
      for(int i = 0; i < totalPreds; i++)
	{
	  int predId = d_predicates[i];
	  long interpretationIdx = dbIndex[baseDbIndex + i];
	  sat = max(sat, d_interpretation[predId][interpretationIdx] == d_valTrue[i]);
	}

      d_satArray[idx] = sat;
    }
}

extern "C"
__global__ void evalClauseWithoutDbKernel(int totalVars, int totalPreds, int *d_varDomainSizes,
                                  int *d_predicates, int *d_negated, int *d_predBaseIdx, int *d_valTrue, int *d_predVarMat, 
								  int *d_satArray, int **d_interpretation, long totalGroundings, long offset, int *d_mem)
{
  long idx = blockIdx.x * blockDim.x + threadIdx.x;

  if(idx < totalGroundings && d_satArray[idx] == 1)
    {
		int memBase = idx * totalVars;
		long n = idx + offset;
		for(int i = totalVars-1; i >= 0; i--)
		{
		  int domainSize = d_varDomainSizes[i];
		  long temp = n / domainSize;
		  int val = n - temp * domainSize;
		  n = temp;
		  d_mem[memBase + i] = val;
		}

		int sat = 0;
		for(int i = 0; i < totalPreds; i++)
		{
			int predId = d_predicates[i];
			int negated = d_negated[i];
			int dbIndex = d_predBaseIdx[i];
			for(int j = 0; j < totalVars; j++)
				dbIndex += d_mem[memBase + j] * d_predVarMat[j * totalPreds + i];
			if(negated == 0)
				sat = max(sat, d_interpretation[predId][dbIndex] == d_valTrue[i]);
			else
				sat = max(sat, d_interpretation[predId][dbIndex] != d_valTrue[i]);
		}
		d_satArray[idx] = sat;
    }
}

extern "C"
__global__ void evalCNFKernel(int totalVars, int totalClauses, int *totalPredsInClause, int *d_varDomainSizes,
                              int **d_predicates, int **d_negated, int **d_predBaseIdx, int **d_valTrue,
                              int **d_predVarMat, int *d_satArray, int **d_interpretation, long totalGroundings,
                              long offset, int *d_mem)
{
  long idx = blockIdx.x * blockDim.x + threadIdx.x;

  if(idx < totalGroundings)
    {
		int memBase = idx * totalVars;
		long n = idx + offset;
		for(int i = totalVars-1; i >= 0; i--)
		{
		  int domainSize = d_varDomainSizes[i];
		  long temp = n / domainSize;
		  int val = n - temp * domainSize;
		  n = temp;
		  d_mem[memBase + i] = val;
		}

		int sat = 1;
		for(int c = 0; c < totalClauses; c++) {
		  if(sat == 0)
		    break;
		  int clauseSat = 0;
		  int totalPredicates = totalPredsInClause[c];
		  for(int i = 0; i < totalPredicates; i++)
		  {
			  int predId = d_predicates[c][i];
			  int negated = d_negated[c][i];
			  int dbIndex = d_predBaseIdx[c][i];
			  for(int j = 0; j < totalVars; j++)
				  dbIndex += d_mem[memBase + j] * d_predVarMat[c][j * totalPredicates + i];
			  if(negated == 0)
			    clauseSat = max(clauseSat, d_interpretation[predId][dbIndex] == d_valTrue[c][i]);
			  else
			    clauseSat = max(clauseSat, d_interpretation[predId][dbIndex] != d_valTrue[c][i]);
		  }
		  sat = min(sat, clauseSat);
		}
		d_satArray[idx] = sat;
    }
}

/*extern "C"
__global__ void evalClauseWithoutDbKernel(int totalVars, int totalPreds, int *d_varDomainSizes,
                                  int *d_predicates, int *d_predBaseIdx, int *d_valTrue, int *d_predVarMat, 
								  int *d_satArray, int **d_interpretation, long totalGroundings)
{
  int dbIndex[5];

  long idx = blockIdx.x * blockDim.x + threadIdx.x;

  if(idx < totalGroundings && d_satArray[idx] == 1)
    {
		//long baseDbIndex = idx * totalPreds;
		for(int i = 0; i < totalPreds; i++)
			dbIndex[i] = d_predBaseIdx[i];
			//d_dbIndex[baseDbIndex + i] = d_predBaseIdx[i];

		long n = idx;
		for(int i = totalVars-1; i >= 0; i--)
		{
		  int domainSize = d_varDomainSizes[i];
		  long temp = n / domainSize;
		  int val = n - temp * domainSize;
		  n = temp;

		  int basePredVarMatIndex = i * totalPreds;
		  for(int j = 0; j < totalPreds; j++)
			dbIndex[j] += d_predVarMat[basePredVarMatIndex + j] * val;
			  //d_dbIndex[baseDbIndex + j] += d_predVarMat[basePredVarMatIndex + j] * val;
		}

		int sat = 0;
		for(int i = 0; i < totalPreds; i++)
		{
			int predId = d_predicates[i];
			long interpretationIdx = dbIndex[i];
			sat = max(sat, d_interpretation[predId][interpretationIdx] == d_valTrue[i]);
		}
		d_satArray[idx] = sat;
    }
}*/

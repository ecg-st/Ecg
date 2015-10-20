#include "head_ecg.h"
//checked by Huo
void filter(double const *src,int len,double *dst,double *pb,int len_b,double *pa,int len_a)
{
	if(src == NULL || dst == NULL || pb == NULL || pa == NULL){
		//puts("filter pointer is invalid!\n");
		//exit(0);
		return;
	}
	double sum=0;
	double sum1=0;
	for(int i=0;i<len;i++)
	{
		if(i<len_b)
		{
			for(int j=0;j<=i;j++)
			{
				sum += pb[j]*src[i-j];
			}
		}
		else
		{
			for(int j=0;j<len_b;j++)
			{
				sum += pb[j]*src[i-j];
			}
		}

		if(len_a==1||i==0)
			sum1 = 0;
		else if(i<len_a)
			{
				for(int j=1;j<=i;j++)
				{
					sum1+=pa[j]*dst[i-j];
				}
			}
		else 
			{
				for(int j=1;j<len_a;j++)
				{
					sum1+=pa[j]*dst[i-j];
				}
			}
		dst[i]=sum-sum1;	
		sum=0;
		sum1=0;
	}
}
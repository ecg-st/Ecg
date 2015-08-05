#include "head_ecg.h"

//checked by Huo
void lpfilter(double const *src,double *dst,int len)
{	
		if(src == NULL || dst == NULL){
		//puts("lpfilter pointer is invalid!\n");
		//exit(0);
			return;
		}
		double sum = 0;
		double b[4]={0.00000382,    0.00001145,    0.00001145,    0.00000382};
		double a[4]={1.0000,   -2.9368,    2.8757,   -0.9388};
		for(int i=3;i<len;i++)
		{
			for(int j=0;j<4;j++)
			{
				if(j==0)
					sum+=b[0]*src[i];
				else
					sum+=b[j]*src[i-j]-a[j]*dst[i-j];
			}
			dst[i]=sum;
			sum=0;
		}
}
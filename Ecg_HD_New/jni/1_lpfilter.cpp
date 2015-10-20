#include "head_ecg.h"

//checked by Huo
void lpfilter_1(double const *src,double *dst,int len)
{	
		if(src == NULL || dst == NULL){
			//puts("1_lpfilter pointer is invalid!\n");
			//exit(0);
			return;
		}
		double a = 0.0126, b = 0.9874;
		dst[0] = a * src[0];
		for(int i=1;i<len;i++)
		{
			dst[i]=a*src[i]+b*dst[i-1];
		}
		
}
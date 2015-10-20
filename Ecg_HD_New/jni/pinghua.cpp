#include "head_ecg.h"

//checked by Huo
void pinghua(double const *src,double *dst,int len)
{	
		if(src == NULL || dst == NULL){
		//puts("pinghua pointer is invalid!\n");
		//exit(0);
			return;
		}
		double *data=(double *)calloc(len+100,sizeof(double));
		for(int i=0;i<50;i++)
		{
			data[i]=src[49-i];
			data[len+50+i]=src[len-i-1];
		}
		for(int i=0;i<len;i++)
		{
			data[i+50]=src[i];
		}
			//Æ½»¬´¦Àí
		double sum=0;
		for(int i=0;i<len;i++)
		{
			for(int count=0;count<101;count++)
			{
				sum+=data[i+count];
			}
			sum=sum/100;
			dst[i]=sum;
			sum=0;
		}
}
//int PositivePeakDetect(double *src,int len)
//int knee_detect(double *src,int len)
//#include <stdlib.h>
//#include <stdio.h>
#include "my_array_operation.h"
#include "head_ecg.h"
//checked by Huo
void P_detect(double const *src,int len,int *Pstart_pos,int *Ppeak_pos,int *Pend_pos,int sample_rate)
{
	if(src == NULL || Pstart_pos == NULL || Ppeak_pos == NULL || Pend_pos == NULL){
		//puts("P_detect pointer is invalid!\n");
		//exit(0);
		return;
	}
	*Ppeak_pos = positive_peak_detect(src,len,1000);
	if (*Ppeak_pos == 0){
		*Pstart_pos = 0;
		*Pend_pos = 0;
		return;
	}
	*Pstart_pos = knee_detect(src,*Ppeak_pos);
	int dend = *Ppeak_pos+0.1*sample_rate;
	if(dend > len)
	{
		dend = len;
	}
	
	*Pend_pos = knee_detect((double *)src+*Ppeak_pos,dend-*Ppeak_pos)+*Ppeak_pos;
	
	if((*Pend_pos-*Pstart_pos) < 0.04*sample_rate)
	{
		*Pstart_pos=0;
		*Ppeak_pos=0;
		*Pend_pos=0;
	}
}
#if 0
void main()
{
	double *data=(double *)calloc(200,sizeof(double));
	for(int i=0;i<50;i++)
	{
		data[i]=0;
		data[199-i]=0;
	}
	for(int i=50;i<100;i++)
	{
		data[i]=i-49;
		data[i+50]=101-i;
	}
	int start_pos=0,end_pos=0,peak_pos=0;
	int *p_start_pos=&start_pos,*p_peak_pos=&peak_pos,*p_end_pos=&end_pos;
	P_detect(data,200,p_start_pos,p_peak_pos,p_end_pos,1000);
	printf("%d\n",start_pos);
	printf("%d\n",peak_pos);
	printf("%d\n",end_pos);
	free(data);
	system("PAUSE");
}
#endif
#include "head_ecg.h"

//返回的如果是负数 说明T波倒置
//checked by Huo
int T_detect(double const *src,int len,int *Tpeak_pos,int *Tend_pos,int sample_rate)
{
	int isReverse = 1; //1为T波未倒置 -1为T波倒置
	if(src == NULL || Tpeak_pos == NULL || Tend_pos == NULL){
		//puts("T_detect pointer is invalid!\n");
		//exit(0);
		return 1;
	}
	int w1=0.16*sample_rate;
	if(w1>len)		w1=len;

	//Tpeak detection
	int w=0.03*sample_rate;
	double *dst=(double *)calloc(len,sizeof(double));
	double *dst_left=(double *)calloc(len,sizeof(double));
	wing_function(src,dst,len,w,dst_left);
	int peak_pos=0;
	peak_pos=find_max_double_array(dst, len, "first" );
	free(dst);
	double *tmp=(double *)calloc(11,sizeof(double));
	for(int i=0;i<11;i++)
	{
		*(tmp+i)=src[peak_pos-5+i];
	}
	if (dst_left[peak_pos] > 0)
		peak_pos=peak_pos-6+find_max_double_array(tmp, 11, "first" );
	else {
		peak_pos=peak_pos-6+find_min_double_array(tmp, 11, "first" );
		isReverse = -1;
	}

	free(tmp);
	*Tpeak_pos=peak_pos;

	//Tend detection
	double *b1=(double *)calloc(w1,sizeof(double));
	for(int i=0;i<w1;i++)
	{
		b1[i]=isReverse;
	}
	b1[0]=(1-w1)*isReverse;
	double a=1,*pa=&a;
	double *indicator=(double *)calloc(len,sizeof(double));
	double *indi=(double *)calloc(len-w1+1,sizeof(double));
	double sum=0;
	filter(src,len,indicator,b1,w1,pa,1);
	
	for(int i=0;i<len-w1+1;i++)
	{
		*(indi+i)=*(indicator+w1-1+i);
	}
	int tend=find_max_double_array(indi, len-w1+1, "first" );
	*Tend_pos=tend+w1-1;
	free(indicator);
	free(indi);
	free(b1);
	free(dst_left);

	return isReverse;
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
	int peak_pos=0,end_pos=0;
	int *p_peak_pos=&peak_pos,*p_end_pos=&end_pos;
	T_detect(data,200,p_peak_pos,p_end_pos,200/0.3);
	free(data);
	printf("%d\n",peak_pos);
	printf("%d\n",end_pos);
	system("PAUSE");
}
#endif
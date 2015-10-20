#include "head_ecg.h"
//checked by Huo
void wing_function(double const *src,double *dst,int len,int w,double *dst_left)
{
	
	double *data_left=(double *)calloc(len,sizeof(double));
	double *data_right=(double *)calloc(len,sizeof(double));
	
	double *wing_right=(double *)calloc(len,sizeof(double));
	for(int i=0;i<w;i++)
	{
		data_left[i]=2*src[0]-src[w-i];
	}
	for(int i=0;i<len-w;i++)
	{
		data_left[i+w]=src[i];
	}
	for(int i=0;i<len-w;i++)
	{
		data_right[i]=src[i+w];
	}
	for(int i=0;i<w;i++)
	{
		data_right[len-w+i]=2*src[len-1]-src[len-2-i];
	}
	void double_array_minus_double_array(double *src1,double *src2,int len,double *dst);
	void double_array_multiply_double_array(double *src1,double *src2,int len,double *dst);
	double_array_minus_double_array((double*)src,data_left,len,dst_left);
	double_array_minus_double_array((double*)src,data_right,len,wing_right);
	double_array_multiply_double_array(dst_left,wing_right,len,dst);
	free(data_left);
	free(data_right);
	free(wing_right);
}
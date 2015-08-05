#include "head_ecg.h"
//checked by Huo
///////////////////////////////////////////////////////////
//sum_back_forward对每个点前后win个点进行求和，
//返回的数组长度比原来的数组短2*win个点
///////////////////////////////////////////////////////////
void sum_back_forward(double const *src,	//待处理的数组
					  int len,				//待处理的数组长度
					  double *dst,			//存放结果的数组
					  int win)				//处理时前后的点数
{
	int i = 0, j = 0;
	for( i = 0; i < len - 2 * win; ++i ){
		dst[i] = 0;
		for( j = 0; j < win * 2 + 1; ++j){
			dst[i] += src[i + j]; 
		}
	} 
}

int positive_peak_detect(double const * src, int len, int sample_rate)
{
	int seg_len = len;				//待检测数据长度，起始值为原数据长
	int dstart = 0;					//待检测数据起点，起始值为原数据起点
	int L = sample_rate * 0.02;		//计算数据段求和时左右的宽度
	if ((len - L * 2) <= sample_rate * 0.01) return 0;
		
	void *sumbf = calloc( len - L * 2, sizeof(double) );
	int peak_pos;
	while( L > 0 ){
		sum_back_forward( &(src[dstart]), seg_len, (double *)sumbf, L );
		peak_pos = find_max_double_array( (double *)sumbf, seg_len - 2 * L, "first" );
		peak_pos = dstart + peak_pos + L;
		seg_len /= 2;
		dstart = peak_pos - seg_len/2;
		if ( dstart < 0 )
			dstart = 0;
		if ( dstart + seg_len > len)
			seg_len = len - dstart;
		L /= 2;
	}
	free(sumbf);
	return peak_pos;
}

#if 0
int main()
{
	double d4sum[8] = {1, 2, 3, 4, 5, 6, 7, 8};
	double d4sumdst[4];
	sum_back_forward(d4sum, 8, d4sumdst, 2);
	print_array('d', 8, d4sum);
	print_array('d', 4, d4sumdst);


	//测试positive_peak_detect
	double d4peak[100];
	int real_peak = 80;
	for( int i = 0; i <= real_peak; ++i )
		d4peak[i] = 0 + i * 0.7;
	for( int i = real_peak; i< 100; ++i)
		d4peak[i] = d4peak[real_peak] - (i - real_peak) * 0.6;
	int peak_pos = positive_peak_detect(d4peak, 100, 1000);
	printf("%d", peak_pos);
	getchar();
}


#endif
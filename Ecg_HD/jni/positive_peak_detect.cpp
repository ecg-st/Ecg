#include "head_ecg.h"
//checked by Huo
///////////////////////////////////////////////////////////
//sum_back_forward��ÿ����ǰ��win���������ͣ�
//���ص����鳤�ȱ�ԭ���������2*win����
///////////////////////////////////////////////////////////
void sum_back_forward(double const *src,	//�����������
					  int len,				//����������鳤��
					  double *dst,			//��Ž��������
					  int win)				//����ʱǰ��ĵ���
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
	int seg_len = len;				//��������ݳ��ȣ���ʼֵΪԭ���ݳ�
	int dstart = 0;					//�����������㣬��ʼֵΪԭ�������
	int L = sample_rate * 0.02;		//�������ݶ����ʱ���ҵĿ��
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


	//����positive_peak_detect
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
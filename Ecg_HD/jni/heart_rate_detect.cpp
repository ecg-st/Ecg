#include "head_ecg.h"
#define fname  "1"
//checked by Huo
int* heart_rate_detect(double* dp, int len, int sample_rate)
{
	//除错
	if(dp == NULL){
		//puts("heart_rate_detect: pointer invalid!\n");
		return NULL;
	}
	//求二阶导数
	double *deriv1 = (double *)calloc(len, sizeof(double));
	deriv_double_array(dp, deriv1, len);
/*	double *deriv2 = (double *)calloc(len, sizeof(double));
	deriv_double_array(deriv1, deriv2, len);
	free(deriv1);

	//求绝对值
	double *deriv_abs = (double *)calloc(len, sizeof(double));
	abs_double_array(deriv2, len, deriv_abs);
	free(deriv2);
*/

	//用平方法替代绝对值和二阶导数
	double *deriv_sqr = (double *)calloc(len, sizeof(double));
	for (int count = 0; count < len; ++count ){
		deriv_sqr[count] = deriv1[count]*deriv1[count];
	}

	//求低通	
	double *lp1 = (double *)calloc(len, sizeof(double));
	double *lp2= (double *)calloc(len, sizeof(double));
	double *lp= (double *)calloc(len, sizeof(double));
	lpfilter_1(deriv_sqr, lp2, len);
//	lpfilter(deriv_sqr, lp1, len);
	pinghua(lp2, lp1, len);
	free(lp2);
	free(deriv_sqr);
//for test:
//	FILE *foutp = fopen(fname"_hr.dat", "wb"); 
//	fwrite(lp1, sizeof(double), len, foutp);
	
	double *lp_deriv= (double *)calloc(len, sizeof(double));
/*	deriv_double_array(lp1, lp_deriv, len);
	for (int i = 0; i<len; ++i){
		lp_deriv[i] = lp_deriv[i] * lp_deriv[i];
	}
	double mean_deriv = mean_double_array(lp_deriv, len);
*/

	double *left= (double *)calloc(len, sizeof(double));
	wing_function(lp1,lp_deriv,len,20, left);
	free(left);
	double sum_mean_deriv = 0;
	int mean_deriv_cnt = 0;
	for (int i = 0; i< len; ++i){
		if(lp_deriv[i] > 0){
			sum_mean_deriv += lp_deriv[i];
			mean_deriv_cnt ++;
		}
	}
	double mean_deriv = sum_mean_deriv / mean_deriv_cnt;
	

/*	//求低通后将不可能为峰值的区域置零
	double mean = mean_double_array(lp1, len);
	int set0_cnt = 0;
	for (int count = 20; count < len-20; ++count){
		if ( (lp1[count] > mean ) 
			&& ((lp1[count] - lp1[count-1]) >= 0) 
			&& ((lp1[count] - lp1[count+1]) >= 0)
			&& ((lp1[count] - lp1[count-10]) >= 0) 
			&& ((lp1[count] - lp1[count+10]) >= 0)
			&& ((lp1[count] - lp1[count-20]) >= 0) 
			&& ((lp1[count] - lp1[count+20]) >= 0) ){
				lp[count] = lp1[count];
				set0_cnt ++;
		}
	}

	//求出阈值
//	for(int i = 0; i< 10000; i++)printf("%f ", lp1[i]);
	double sum1 = 0, sum_cnt = 0;
	for (int count = 20; count < len-20; ++count){
		if ( lp[count] > mean ) {
			sum1 += lp[count]; 
			sum_cnt++;
		}
	}
	mean = sum1/sum_cnt;
*/
	//通过阈值筛选数列
//	for(int i = 0; i< 10000; i++)printf("%f ", lp1[i]);
	double mean = mean_double_array(lp1, len);
	for (int count = 0; count < len; ++count){
		if ( (lp1[count] > mean) 
			&&(lp_deriv[count] > mean_deriv)
			&&(lp1[count] - lp1[count-1]>0)
			&&(lp1[count] - lp1[count+1]>0) )
			lp[count] = 1;
	}
	lp[0] = 0; 
	lp[len-1] = 0;

	//顶峰原则求出顶点
	int *pos = (int *)calloc(len/300, sizeof(int));
	int num_pos = find_val_in_double_array(lp, len, 1, "all", &(pos[1]));
	if(num_pos <= 1){
		//puts("heart_rate: too few period\n");
		//getchar();
		free(lp);
		free(lp1);
		free(pos);
		free(lp_deriv);
		free(deriv1);
		return NULL;
	}
	pos[0] = num_pos;
	free(lp);
	free(lp1);
	free(lp_deriv);
	free(deriv1);

	/*//求顶点导数平均值
	int sum_inter = 0, num = 0;
	double mean_inter = 0;
	for(int count = 1; count < num_pos-1; count++ ){
		if (pos[count+1] - pos[count] >30){
		sum_inter += pos[count+1] - pos[count];
		num ++;
		}
	}
	for(int i = 0; i< num_pos; i++)printf("%d ", pos[i]);

	mean_inter = (double)sum_inter / (double)(num);
//	free(pos);
	//求心律
	double heart_rate =  60 * double(sample_rate) / double(mean_inter);
	
	*/

	//返回
	return pos;
}
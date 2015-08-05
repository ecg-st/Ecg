#include "head_ecg.h"

//checked by Huo
NODE * R_detect ( double const * dp, int len , int sample_rate)
{
	
	/////////////////////////////////////////////////////////////
	//  Step1: compute the threshold
	/////////////////////////////////////////////////////////////
	int win_wing = sample_rate * 0.015;
	double *wing = (double *)calloc(len, sizeof(double));
	double *wing_left = (double *)calloc(len, sizeof(double));
	wing_function(dp, wing, len, win_wing, wing_left);
	
	//计算threshold
	double sum_wing_good = 0;		int num_wing_good = 0;
	double threshold = 0;
	double *wing_good = (double *) calloc(len, sizeof(double));
	for( int count = 0; count < len; ++count){
		if ( (wing[count] > 0)&&(wing_left[count] > 0) ){
			sum_wing_good += wing[count];
			num_wing_good++ ;
			wing_good[count] = wing[count];
		}else{
			wing_good[count] = 0;
		}
	}
	threshold = sum_wing_good / num_wing_good;
	if(threshold < 100) {
		//puts("R_detect:threshold too small!\n");
		free(wing);
		free(wing_left);
		free(wing_good);
		return NULL;
	}
	//加强threshold
	double sum_wing_better = 0;		
	int num_wing_better = 0;
	double *wing_better = (double *) calloc(len, sizeof(double));
	for( int count = 0; count < len; ++count ){
		if ( (wing_good[count])>threshold ){
			sum_wing_better += wing_good[count];
			num_wing_better++;
			wing_better[count] = wing_good[count];
		}else{
			wing_better[count] = 0;
		}
	}
	threshold = sum_wing_better / num_wing_better;
	
//	double threshold_high = threshold * 0.8;
	double threshold_low = threshold * 0.2; //previous:0.3, modified by Huo

	//加强wing_better
	for ( int count = 0; count < len; ++count ){
		if ( wing_better[count] < threshold )
			wing_better[count] = 0;
	}

	/////////////////////////////////////////////////////////////
	//  Step2: Rpeak detection
	/////////////////////////////////////////////////////////////
	double *wing1 = (double *)calloc(len, sizeof(double));
	double *wing5 = (double *)calloc(len, sizeof(double));
	wing_function(dp, wing1, len, 1, wing_left);
	wing_function(dp, wing5, len, 5, wing_left);
	for( int count = 0; count < len; ++count ){
		if( (wing1[count] <= 0) || (wing5[count] <= 0) )
			wing_better[count] = 0;
	}
	

	int *final = (int *)calloc(len, sizeof(int));
	double_array_cmp_value(wing_better, len, final, 0);
//test:
//	print_array('i', 200, final);

	//检测位置并存放于数组R_pos中
	int *R_pos = (int *)calloc(len, sizeof(int));
	int num_R = find_val_in_int_array(final, len, 1, "all", R_pos);
//for test:
//	print_array('i', num_R, R_pos);
	if( num_R <= 1) {
		free(final);
		free(wing);
		free(wing1);
		free(wing5);
		free(wing_left);
		free(wing_good);
		free(wing_better);
		free(R_pos);
		return NULL;
	}
	/////////////////////////////////////////////////////////////
	//  Step3: 生成链表 & 异常重检
	/////////////////////////////////////////////////////////////
	NODE *start = (NODE *)calloc(1, sizeof(NODE));
	NODE *previous = start;
	NODE *current = NULL;
	int leaveout_flag = 0;
	int pos_reexam;
	for(int count = 1; count < num_R; ++count){
		////////////////////////////////////////////////////////////////////////
		//判断是否存在漏检的可能，如果RR间期大于1.4s，则回检
		//该段程序有待改进，回检机制不完善
		////////////////////////////////////////////////////////////////////////
		if ( (R_pos[count] - R_pos[count-1]) > sample_rate * 1.4 ){
			int dstart_reexam = R_pos[count-1] + sample_rate * 0.3; //previous:0.5, modified by Huo
			int len_reexam = sample_rate;
			for(int count  = 0; count < len_reexam; ++count){
				if( (wing1[count + dstart_reexam] <= 0) || 
					(wing5[count + dstart_reexam] <= 0 || 
					(wing_good[count + dstart_reexam]) <= threshold_low) )
					wing_good[count + dstart_reexam] = 0;				
			}
			double_array_cmp_value( &(wing_good[dstart_reexam]), len_reexam, &(final[dstart_reexam]), 0);
			pos_reexam = find_val_in_int_array(&(final[dstart_reexam]), len_reexam, 1, "first", final);
			
//for test
//			printf("dstart_reexam: %d; pos_reexam: %d \n", dstart_reexam, pos_reexam);
			if ( pos_reexam > 0 ){
				pos_reexam += dstart_reexam;
				leaveout_flag = 1;
				current = (NODE *)calloc(1, sizeof(NODE));
				current->next = NULL;
				current->content.Rpeak_pos = pos_reexam;
//for test  
//				printf("R_pos:%d	", current->content.Rpeak_pos);
				current->content.RR_interval = pos_reexam - R_pos[count-1];
				previous->next = current;
				previous = current;
			}
		}
		
		////////////////////////////////////////////////////////////////////////
		//判断是否存在一个心动周期检测到多个R波，若RR间期小于0.2个周期则舍弃
		//该处同样有待改进
		////////////////////////////////////////////////////////////////////////
		if ( R_pos[count] - previous->content.Rpeak_pos < 0.1 * sample_rate )
			continue;

		current = (NODE *)calloc(1, sizeof(NODE));
		current->next = NULL;
		current->content.Rpeak_pos = R_pos[count];
//for test  
//		printf("R_pos:%d	", current->content.Rpeak_pos);
		current->content.RR_interval = (leaveout_flag == 1) ? R_pos[count] - pos_reexam : R_pos[count] - R_pos[count-1];
		previous->next = current;
		previous = current;
	}


	/////////////////////////////////////////////////////////////
	//  Step4:
	/////////////////////////////////////////////////////////////


	free(final);
	free(wing);
	free(wing1);
	free(wing5);
	free(wing_left);
	free(wing_good);
	free(wing_better);
	free(R_pos);
	return start;
}
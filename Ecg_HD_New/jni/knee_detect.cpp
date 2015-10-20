#include "head_ecg.h"


#define EPSILON 0.0001
//checked by Huo
/*
曲线的拐点检测，先局域法定范围，再微调，返回拐点位置
*/
int knee_detect( double const * data_p, int len )
{
	int knee_pos;
	//compute the slope of the straight line
	double slope = (data_p[len-1] - data_p[0])/(len-1);

	//////////////////////////////////////////////////////////////////
	//compute the dalta between straight line and curve.
	//////////////////////////////////////////////////////////////////
	//allocate memory for delta and abs_delta
	void *delta = calloc( len, sizeof(double) );
	void *abs_delta = calloc( len, sizeof(double) );
	if( delta == NULL ){
		//puts("knee_detect error: out of memory!");
		free(delta);
		free(abs_delta);
		return 0;
	}
	int count = 0;
	for ( count = 0; count < len; ++count )
		((double *)delta)[count] = slope * count + data_p[0] - data_p[count];
	
	//////////////////////////////////////////////////////////////////
	//find the approximate knee_pos
	//////////////////////////////////////////////////////////////////
	//find the max delta position
	abs_double_array( (double *)delta, len, (double *)abs_delta );
	int max_delta_pos = find_max_double_array((double *)abs_delta, len, "first");
	//release memory of abs_delta
	free(abs_delta);
	
	//////////////////////////////////////////////////////////////////
	//search the knee_pos around the max_delta_pos
	//////////////////////////////////////////////////////////////////
	int seg_len, start = max_delta_pos;
	//////////////////////////////////////
	//决定应该向哪个方向搜索多少个数据
	///////////////////////////////////////
	if( slope * ((double *)delta)[max_delta_pos] < 0 ){
		//确定拐点存在的数据范围
		//如果数据不够长（10个数），则有多长取多长
		if ( start + 10 >= len ) {
			seg_len = len - start - 1;
		}
		else {	//足够长则取10个数
			seg_len = 10;
		}
		///////////////////////////////////////////////////
		//在所选取出来的搜索范围内进行拐点搜索
		///////////////////////////////////////////////////
		if(seg_len < 0){
			//puts("FUNCTION knee_detect ERROR: seg_len negtive!");
			free(delta);
			return 0;
		}
		double *delta2, *del_mul;
		int *sign;
		void * d2 = calloc( seg_len, sizeof(double) ); 
		delta2 = (double *)d2;
		void * dm = calloc( seg_len, sizeof(double) ); 
		del_mul = (double *)dm;
		void * sgn = calloc( seg_len, sizeof(double) ); 
		sign = (int *)sgn;
		//在所选定数据内寻找拐点
		for( count = 0; count < seg_len; ++count ){
			//计算二阶导数
			delta2[count] = data_p[start + count + 1]  + data_p[start + count -1] - 
				2 * data_p[start + count]; 
			//计算前后一阶导数是否异号
			del_mul[count] = ( data_p[start + count + 1] - data_p[start + count] )
				* ( data_p[start + count] - data_p[start + count - 1] );
		}
		double_array_cmp_value(del_mul, seg_len, sign, EPSILON);
		//一阶导数异号处理
		if ( ( knee_pos =  find_val_in_int_array( sign, seg_len, -1, "first" , NULL)) != NO_SUCH_VALUE ){
			if( knee_pos < 0 || knee_pos >= seg_len ){
				//puts("FUNCTION knee_detect ERROR: 11111111");
				free(d2);
				free(dm);
				free(sgn);
				free(delta);
				return 0;
			}
			knee_pos += start;
			free(d2);
			free(dm);
			free(sgn);
			free(delta);
			return knee_pos;
		} 
		else{	//一阶导数同号处理
			abs_double_array(delta2, seg_len, delta2);
			knee_pos = find_max_double_array(delta2, seg_len, "first");
			knee_pos += start;
			free(d2);
			free(dm);
			free(sgn);
			free(delta);
			return knee_pos;
		}
	} // 以上是需要向后搜寻拐点的情况，
	//下面else里面的部分是要向前搜寻拐点的情况
	else{
		if ( max_delta_pos - 10 < 1 ){
			start = 1;
			seg_len = max_delta_pos + 1;
		}
		else{
			start = max_delta_pos - 9;
			seg_len = 10;
		}
		///////////////////////////////////////////////////
		//在所选取出来的搜索范围内进行拐点搜索
		///////////////////////////////////////////////////
		if(seg_len < 0){
			//puts("FUNCTION knee_detect ERROR: seg_len negtive!");
			free(delta);
			return 0;
		}
		double *delta2, *del_mul;
		int *sign;
		void * d2 = calloc( seg_len, sizeof(double) ); 
		delta2 = (double *)d2;
		void * dm = calloc( seg_len, sizeof(double) ); 
		del_mul = (double *)dm;
		void * sgn = calloc( seg_len, sizeof(double) ); 
		sign = (int *)sgn;
		//在所选定数据内寻找拐点
		for( count = 0; count < seg_len; ++count ){
			//计算二阶导数
			delta2[count] = data_p[start + count + 1]  + data_p[start + count -1] - 
				2 * data_p[start + count]; 
			//计算前后一阶导数是否异号
			del_mul[count] = ( data_p[start + count + 1] - data_p[start + count] )
				* ( data_p[start + count] - data_p[start + count - 1] );
		}
		double_array_cmp_value(del_mul, seg_len, sign, EPSILON);
		//一阶导数异号处理
		if ( ( knee_pos =  find_val_in_int_array( sign, seg_len, -1, "last" , NULL)) != NO_SUCH_VALUE ){
			if( knee_pos < 0 || knee_pos >= seg_len ){
				//puts("FUNCTION knee_detect ERROR: 11111111");
				free(d2);
				free(dm);
				free(sgn);
				free(delta);
				return 0;
			}
			knee_pos += start;
			free(d2);
			free(dm);
			free(sgn);
			free(delta);
			return knee_pos;
		} 
		else{	//一阶导数同号处理
			abs_double_array(delta2, seg_len, delta2);
			knee_pos = find_max_double_array(delta2, seg_len, "last");
			knee_pos += start;
			free(d2);
			free(dm);
			free(sgn);
			free(delta);
			return knee_pos;
		}
	}
}
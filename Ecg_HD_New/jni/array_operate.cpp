#include "my_array_operation.h"
//checked by Huo
/////////////////////////////////////////////////////////////////////////
//对double数组求导数
/////////////////////////////////////////////////////////////////////////
void deriv_double_array(double *src, double *rst, int len)
{
	if ( src == NULL || rst == NULL ){
		//puts("pointer invalid.");
		//exit(0);
		return;
	}
	rst[0] = src[1] - src[0];
	int count = 1;
	for (count = 1; count < len; ++count ){
		rst[count] = src[count] - src[count - 1];
	}
}





/////////////////////////////////////////////////////////////////////////
//对double数组求平均
/////////////////////////////////////////////////////////////////////////
double mean_double_array(double *dp, int len)
{
	if ( dp == NULL ){
		//puts("pointer invalid.");
		//exit(0);
		return 0;
	}
	int count = 0;
	double sum = 0;
	for(count = 0; count < len; ++count){
		sum += dp[count];
	}
	double mean = sum/len;
	return mean;
}
/////////////////////////////////////////////////////////////////////////
//对double数组求和
/////////////////////////////////////////////////////////////////////////
double sum_double_array(double *dp, int len)
{
	if ( dp == NULL ){
		//puts("pointer invalid.");
		//exit(0);
		return 0;
	}
	int count = 0;
	double sum = 0;
	for(count = 0; count < len; ++count){
		sum += dp[count];
	}
	return sum;
}
//////////////////////////////////////////////////////////////////////
//对int数组元素逐项取绝对值
//////////////////////////////////////////////////////////////////////
void abs_int_array(int *src,		//待处理的数组
				int len,			//待处理的数组长度
				int *dst = NULL)	//结果存放数组
{
	if ( dst == NULL )dst = src;
	if ( src == NULL || dst == NULL ){
		//puts("pointer is invalid!\n");
		//exit(0);
		return;
	}
	int count = 0;
	for( count = 0; count < len; ++count ){
		dst[count] = (src[count] >= 0) ? src[count]: -src[count];
	}
}


//////////////////////////////////////////////////////////////////////
//对double数组元素逐项取绝对值
//////////////////////////////////////////////////////////////////////
void abs_double_array( double *src,			//待处理的数组
					int len,				//待处理的数组长度
					double *dst = NULL )	//结果存放数组
{
	if ( dst == NULL ) dst = src;
	if ( src == NULL || dst == NULL ){
		//puts("pointer is invalid!\n");
		//exit(0);
		return;
	}
	int count = 0;
	for( count = 0; count < len; ++count ){
		dst[count] = (src[count] >= 0) ? src[count] : -src[count];
	}
}


//////////////////////////////////////////////////////////////////////
//实现int数组src与value的逐项比较：大于为1，等于为0，小于为-1
//////////////////////////////////////////////////////////////////////
void int_array_cmp_value(int *src,		//待处理的数组
						int len,		//待处理的数组长度
						int *dst,		//结果存放数组
						double value)	//带比较的值
{
	if(src == NULL || dst == NULL){
		//puts("pointer is invalid!\n");
		//exit(0);
		return;
	}
	int count = 0;
	for( count = 0; count < len; ++count ){
		dst[count] = (src[count] > value) ? 1 : ( (src[count] < value) ? -1 : 0 );
	}
}

//////////////////////////////////////////////////////////////////////
//实现double数组src与value的逐项比较：大于为1，等于为0，小于为-1
//////////////////////////////////////////////////////////////////////
void double_array_cmp_value(double const *src,	//待处理的数组
							int len,			//待处理的数组长度
							int *dst,			//结果存放数组
							double value)		//待比较的值
{
	if(src == NULL || dst == NULL){
		//puts("pointer is invalid!\n");
		//exit(0);
		return;
	}
	int count = 0;
	for( count = 0; count < len; ++count ){
		dst[count] = (src[count] > value) ? 1 : ( (src[count] < value) ? -1 : 0 );
	}
}

//////////////////////////////////////
//打印数组
//////////////////////////////////////
void print_array( char type,		//数组类型
				 int len,			//数组长度
				 void const *p)		//数组起始地址
{
	if(p == NULL){
		//puts("pointer is invalid!\n");
		//exit(0);
	}
	int count = 0;
	switch( type ){
		case 'i':
			for(count = 0; count < len; ++count )
				printf("%d ", ((int *)p)[count]);
			printf("\n");
			break;

		case 'd':
			for(count = 0; count < len; ++count)
				printf("%f ", ((double *)p)[count]);
			printf("\n");
			break;

		case 'f':
			for(count = 0; count < len; ++count )
				printf("%f ", ((double *)p)[count]);
			printf("\n");
			break;

		case 'c':
			for(count = 0; count < len; ++count )
				printf("%c", ((char *)p)[count]);
			printf("\n");
			break;

		default:
			printf("wrong type!\n");
	}
}

/////////////////////////////////////////////////////////////
//实现int数组之间的逐项相与，非零为真，返回int
/////////////////////////////////////////////////////////////
void int_array_and_int_array (int *src1,
							 int *src2,
							int len, 		
							int *dst = NULL)
{
	if ( dst == NULL ) dst = src1;
	if ( src1 == NULL || dst == NULL || src2 == NULL ){
		//puts(" Warning: pointers invalid! ");
		//exit(0);
		return;
	}
	int count = 0;
	for(count = 0; count < len; ++count ){
		dst[count] = ( src1[count] != 0 ) && ( src2[count] != 0 );
	}
}

/////////////////////////////////////////////////////////////
//实现double数组之间的逐项相与，非零为真，返回int
/////////////////////////////////////////////////////////////
void double_array_and_double_array (double *src1,
							 double *src2,
							int len, 		
							double *dst = NULL)
{
	if ( dst == NULL ) dst = src1;
	if ( src1 == NULL || dst == NULL || src2 == NULL ){
		//puts(" Warning: pointers invalid! ");
		//exit(0);
		return;
	}
	int count = 0;
	for(count = 0; count < len; ++count ){
		dst[count] = ( src1[count] != 0 ) && ( src2[count] != 0 );
	}
}




/////////////////////////////////////////////////////////////
//实现int数组逐项相或，非零为真
/////////////////////////////////////////////////////////////
void int_array_or_int_array (int *src1,
							 int *src2,
							int len, 		
							int *dst = NULL)
{
	if ( dst == NULL ) dst = src1;
	if ( src1 == NULL || dst == NULL || src2 == NULL ){
		//puts(" Warning: pointers invalid! ");
		//exit(0);
		return;
	}
	int count = 0;
	for(count = 0; count < len; ++count ){
		dst[count] = ( src1[count] != 0 ) || ( src2[count] != 0 );
	}
}


/////////////////////////////////////////////////////////////////
//在double数组中找出最大值，返回最大值
/////////////////////////////////////////////////////////////////
double max_double_array( double const *p, int len )
{
	if ( p == NULL ){
		//puts(" Warning: pointer invalid! ");
		//exit(0);
		return 0;
	}
	double max = *p;
	int count = 0;
	for ( count = 0; count < len; ++count ){
		if ( *p++ > max ) max = *(p-1);
	}
	return max;
}


/////////////////////////////////////////////////////////////////
//在double数组中找出最小值，返回最小值
/////////////////////////////////////////////////////////////////
double min_double_array( double const *p, int len )
{
	if ( p == NULL ){
		//puts(" Warning: pointer invalid! ");
		//exit(0);
		return 0;
	}
	double min = *p;
	int count = 0;
	for ( count = 0; count < len; ++count ){
		if ( *p++ < min ) min = *(p-1);
	}
	return min;
}


/////////////////////////////////////////////////////////////////
//在double数组中找出最大值，返回其中一个的位置："first" or "last"
/////////////////////////////////////////////////////////////////
int find_max_double_array( double const *p, 
						  int len, 
						  char *num = "first" )
{
	if ( p == NULL ){
		//puts(" Warning: pointer invalid! ");
		//exit(0);
		return 0;
	}
	double max = *p;
	int count = 0;
	int max_pos = 0;
	switch(*num){
		case 'f':
			for ( count = 0; count < len; ++count ){
				if ( *p++ > max ) {
					max = *(p-1);
					max_pos = count;
				}
			}
			break;
		case 'l':
			for ( count = 0; count < len; ++count ){
				if ( *p++ >= max ) {
					max = *(p-1);
					max_pos = count;
				}
			}
			break;
		default:
			//puts("invalid num input!");
			//exit(0);
			max_pos = 0;
	}
	return max_pos;
}



/////////////////////////////////////////////////////////////////
//在double数组中找出最小值，返回其中一个的位置："first" or "last"
/////////////////////////////////////////////////////////////////
int find_min_double_array( double const *p, 
						  int len, 
						  char *num = "first" )
{
	if ( p == NULL ){
		//puts(" Warning: pointer invalid! ");
		//exit(0);
		return 0;
	}
	double min = *p;
	int count = 0;
	int min_pos = 0;
	switch(*num){
		case 'f':
			for ( count = 0; count < len; ++count ){
				if ( *p++ < min ) {
					min = *(p-1);
					min_pos = count;
				}
			}
			break;
		case 'l':
			for ( count = 0; count < len; ++count ){
				if ( *p++ <= min ) {
					min = *(p-1);
					min_pos = count;
				}
			}
			break;
		default:
			//puts("invalid num input!");
			//exit(0);
			min_pos = 0;
	}
	return min_pos;
}




//////////////////////////////////////////////////////////////////////////////
//在double数组中找出某定值，并返回所要求的位置："first" ， "last" or "all"
//如果是first或last，结果直接返回，
//如果找不到，则返回 NO_SUCH_VALUE，可以用返回值小于0来检验是否找到
//如果是all，结果存放在传入的结果数组的指针指向的位置，并返回查找到的数量，
//找不到则返回0
//////////////////////////////////////////////////////////////////////////////
int find_val_in_double_array(double *p,				//待搜索数组
							 int len,				//数组的长度
							 double val,			//待搜索值
							 char *type = "first",	//搜索的位置
							 int *pos = NULL )		//如果type为all，则需要传入
{
	if ( p == NULL ){
		//puts(" FUNCTION find_val_in_double_array ERROR: invalid source pointer!");
		//exit(0);
		return NO_SUCH_VALUE;
	}
	int count = 0, result = NO_SUCH_VALUE;
	switch ( *type ){
		case 'f':
			for ( count = 0; count < len; ++count ){
				if ( p[count] == val ) {
					return count;
				}
			}
			return NO_SUCH_VALUE;
		case 'l':
			for ( count = 0; count < len; ++count ){
				if ( p[count] == val ) {
					result = count;
				}
			}
			return result;
		case 'a':
			if( pos == NULL ){
				//puts("FUNCTION find_val_in_double_array ERROR: invalid pos pointer!");
				//exit(0);
				return NO_SUCH_VALUE;
			}
			for ( count = 0, result = 0; count < len; ++count ){
				if ( p[count] == val ) {
					*pos++ = count;
					result++;
				}
			}
			return result;
		default:
			//puts("FUNCTION find_val_in_double_array ERROR: invalid type!");
			//exit(0);
			return NO_SUCH_VALUE;
	}
}


//////////////////////////////////////////////////////////////////////////////
//在int数组中找出某定值，并返回所要求的位置："first" ， "last" or "all"
//如果是first或last，结果直接返回，
//如果找不到，则返回 NO_SUCH_VALUE，可以用返回值小于0来检验是否找到
//如果是all，结果存放在传入的结果数组的指针指向的位置，并返回查找到的数量，
//找不到则返回0
//////////////////////////////////////////////////////////////////////////////
int find_val_in_int_array(int *p,				//待搜索数组
							 int len,				//数组的长度
							 int val,			//待搜索值
							 char *type = "first",	//搜索的位置
							 int *pos = NULL)		//如果type为all，则需要传入
{
	if ( p == NULL ){
		//puts(" FUNCTION find_val_in_int_array ERROR: invalid source pointer!");
		//exit(0);
		return NO_SUCH_VALUE;
	}
	int count = 0, result = NO_SUCH_VALUE;
	switch ( *type ){
		case 'f':
			for ( count = 0; count < len; ++count ){
				if ( p[count] == val ) {
					return count;
				}
			}
			return NO_SUCH_VALUE;
		case 'l':
			for ( count = 0; count < len; ++count ){
				if ( p[count] == val ) {
					result = count;
				}
			}
			return result;
		case 'a':
			if( pos == NULL ){
				//puts("FUNCTION find_val_in_int_array ERROR: invalid pos pointer!");
				//exit(0);
				return NO_SUCH_VALUE;
			}
			for ( count = 0, result = 0; count < len; ++count ){
				if ( p[count] == val ) {
					*pos++ = count;
					result++;
				}
			}
			return result;
		default:
			//puts("FUNCTION find_val_in_int_array ERROR: invalid type!");
			//exit(0);
			return NO_SUCH_VALUE;
	}
}


////////////////////////////////////////////////////////
//double数组 + value
////////////////////////////////////////////////////////
void double_array_plus_val (double *src, 
							int len, 
							double val,						
							double *dst = NULL)
{
	if ( dst == NULL ) dst = src;
	if ( src == NULL || dst == NULL ){
		//puts(" Warning: pointers invalid! ");
		//exit(0);
		return;
	}
	int count = 0;
	for(count = 0; count < len; ++count ){
		*dst++ = *src++ + val;
	}
}


////////////////////////////////////////////////////////
//double数组 - value
////////////////////////////////////////////////////////
void double_array_minus_val (double *src, 
							int len, 
							double val,						
							double *dst = NULL)
{
	if ( dst == NULL ) dst = src;
	if ( src == NULL || dst == NULL ){
		//puts(" Warning: pointers invalid! ");
		//exit(0);
		return;
	}
	int count = 0;
	for(count = 0; count < len; ++count ){
		*dst++ = *src++ - val;
	}
}

////////////////////////////////////////////////////////
//double数组 * value
////////////////////////////////////////////////////////
void double_array_mutiply_val (double *src, 
							int len, 
							double val,						
							double *dst = NULL)
{
	if ( dst == NULL ) dst = src;
	if ( src == NULL || dst == NULL ){
		//puts(" Warning: pointers invalid! ");
		//exit(0);
		return;
	}
	int count = 0;
	for(count = 0; count < len; ++count ){
		*dst++ = *src++ * val;
	}
}

////////////////////////////////////////////////////////
//double数组 / value
////////////////////////////////////////////////////////
void double_array_divide_val (double *src, 
							int len, 
							double val,						
							double *dst = NULL)
{
	if ( dst == NULL ) dst = src;
	if ( src == NULL || dst == NULL || val == 0){
		//puts(" Warning: pointers invalid! ");
		//exit(0);
		return;
	}
	int count = 0;
	for(count = 0; count < len; ++count ){
		*dst++ = *src++ / val;
	}
}


////////////////////////////////////////////////////////
//double数组src1 + double数组src2
////////////////////////////////////////////////////////
void double_array_plus_double_array (double *src1,
							 double *src2,
							int len, 		
							double *dst = NULL)
{
	if ( dst == NULL ) dst = src1;
	if ( src1 == NULL || dst == NULL || src2 == NULL ){
		//puts(" Warning: pointers invalid! ");
		//exit(0);
		return;
	}
	int count = 0;
	for(count = 0; count < len; ++count ){
		*dst++ = *src1++ + *src2++;
	}
}


////////////////////////////////////////////////////////
//double数组src1 - double数组src2
////////////////////////////////////////////////////////
void double_array_minus_double_array (double *src1,
							 double *src2,
							int len, 				
							double *dst = NULL)
{
	if ( dst == NULL ) dst = src1;
	if ( src1 == NULL || dst == NULL || src2 == NULL ){
		//puts(" Warning: pointers invalid! ");
		//exit(0);
		return;
	}
	int count = 0;
	for(count = 0; count < len; ++count ){
		*dst++ = *src1++ - *src2++;
	}
}


////////////////////////////////////////////////////////
//double数组src1 .* double数组src2
////////////////////////////////////////////////////////
void double_array_multiply_double_array (double *src1,
							 double *src2,
							int len, 				
							double *dst = NULL)
{
	if ( dst == NULL ) dst = src1;
	if ( src1 == NULL || dst == NULL || src2 == NULL ){
		//puts(" Warning: pointers invalid! ");
		//exit(0);
		return;
	}
	int count = 0;
	for(count = 0; count < len; ++count ){
		*dst++ = *src1++ * *src2++;
	}
}


////////////////////////////////////////////////////////
//double数组src1 .* int数组src2
////////////////////////////////////////////////////////
void double_array_multiply_int_array (double *src1,
							 int *src2,
							int len, 				
							double *dst = NULL)
{
	if ( dst == NULL ) dst = src1;
	if ( src1 == NULL || dst == NULL || src2 == NULL ){
		//puts(" Warning: pointers invalid! ");
		//exit(0);
		return;
	}
	int count = 0;
	for(count = 0; count < len; ++count ){
		*dst++ = *src1++ * *src2++;
	}
}


////////////////////////////////////////////////////////
//double数组src1 ./ double数组src2
////////////////////////////////////////////////////////
void double_array_divide_double_array (double *src1,
							 double *src2,
							int len, 				
							double *dst = NULL)
{
	if ( dst == NULL ) dst = src1;
	if ( src1 == NULL || dst == NULL || src2 == NULL ){
		//puts(" Warning: pointers invalid! ");
		//exit(0);
		return;
	}
	int count = 0;
	for(count = 0; count < len; ++count ){
		*dst++ = *src1++ / *src2++;
	}
}


////////////////////////////////////////////////////////
//int数组 + value
////////////////////////////////////////////////////////
void int_array_plus_val (int *src, 
							int len, 
							int val,						
							int *dst = NULL)
{
	if ( dst == NULL ) dst = src;
	if ( src == NULL || dst == NULL ){
		//puts(" Warning: pointers invalid! ");
		//exit(0);
		return;
	}
	int count = 0;
	for(count = 0; count < len; ++count ){
		*dst++ = *src++ + val;
	}
}


////////////////////////////////////////////////////////
//int数组 - value
////////////////////////////////////////////////////////
void int_array_minus_val (int *src, 
							int len, 
							int val,						
							int *dst = NULL)
{
	if ( dst == NULL ) dst = src;
	if ( src == NULL || dst == NULL ){
		//puts(" Warning: pointers invalid! ");
		//exit(0);
		return;
	}
	int count = 0;
	for(count = 0; count < len; ++count ){
		*dst++ = *src++ - val;
	}
}

////////////////////////////////////////////////////////
//int数组 * value
////////////////////////////////////////////////////////
void int_array_mutiply_val (int *src, 
							int len, 
							int val,						
							int *dst = NULL)
{
	if ( dst == NULL ) dst = src;
	if ( src == NULL || dst == NULL ){
		//puts(" Warning: pointers invalid! ");
		//exit(0);
		return;
	}
	int count = 0;
	for(count = 0; count < len; ++count ){
		*dst++ = *src++ * val;
	}
}

////////////////////////////////////////////////////////
//int数组 / value
////////////////////////////////////////////////////////
void int_array_divide_val (int *src, 
							int len, 
							int val,						
							int *dst = NULL)
{
	if ( dst == NULL ) dst = src;
	if ( src == NULL || dst == NULL ){
		//puts(" Warning: pointers invalid! ");
		//exit(0);
		return;
	}
	int count = 0;
	for(count = 0; count < len; ++count ){
		*dst++ = *src++ / val;
	}
}


////////////////////////////////////////////////////////
//int数组src1 + int数组src2
////////////////////////////////////////////////////////
void int_array_plus_int_array (int *src1,
							 int *src2,
							int len, 		
							int *dst = NULL)
{
	if ( dst == NULL ) dst = src1;
	if ( src1 == NULL || dst == NULL || src2 == NULL ){
		//puts(" Warning: pointers invalid! ");
		//exit(0);
		return;
	}
	int count = 0;
	for(count = 0; count < len; ++count ){
		*dst++ = *src1++ + *src2++;
	}
}


////////////////////////////////////////////////////////
//int数组src1 - int数组src2
////////////////////////////////////////////////////////
void int_array_minus_int_array (int *src1,
							 int *src2,
							int len, 				
							int *dst = NULL)
{
	if ( dst == NULL ) dst = src1;
	if ( src1 == NULL || dst == NULL || src2 == NULL ){
		//puts(" Warning: pointers invalid! ");
		//exit(0);
		return;
	}
	int count = 0;
	for(count = 0; count < len; ++count ){
		*dst++ = *src1++ - *src2++;
	}
}


#if 0

void main()
{
	double a[5] = {1, 1, 3, 5, 5};
	int b[5];
	double c[5], d[5];
	double max, min;
	double_array_cmp_value(a, 5, b, 3);
	print_array('d', 5, a);
	print_array('i', 5, b);
	max = max_double_array(a, 5);
	min = min_double_array(a, 5);
	int max_pos = find_max_double_array(a, 5, "last");
	int min_pos = find_min_double_array(a, 5, "first");
	printf("%d: %f \n%d: %f\n", max_pos, max, min_pos, min);

	double_array_divide_val(a, 5, 2);
	double_array_minus_val(a, 5, 1, c);
	print_array('d', 5, a);
	print_array('d', 5, c);

	double_array_minus_double_array(a, c, 5, d);
	print_array('d', 5, d);
	double_array_divide_double_array(a, c, 5);
	print_array('d', 5, a);
	double_array_multiply_int_array(a, b, 5);
	print_array('d', 5, a);

	double d4abs[5] = {1, -1, 0, 4, -0.5};
	abs_double_array(d4abs, 5);
	print_array('d', 5, d4abs);


	double_array_cmp_value(d4abs, 5, b, 1);
	int pos_first = find_val_in_int_array(b, 5, 1, "first");
	int pos_last = find_val_in_int_array(b, 5, 1, "last");
	int pos_array[5];
	int num_all = find_val_in_int_array(b, 5, 1, "all", pos_array);
	print_array('d', 5, d4abs);
	print_array('i', 5, b);
	printf("first: %d\nlast: %d\nall: %d\n	", pos_first, pos_last, num_all);
	print_array('i', num_all, pos_array);

	int ddd[5] = {1, -9, 0, 0, -1};
	int aaa[5] = {0, -1, 0, -1, -1};
	int and[5], or[5];
	print_array('i', 5, ddd);
	print_array('i', 5, aaa);
	int_array_and_int_array(ddd, aaa, 5, and);
	int_array_or_int_array(ddd, aaa, 5, or);
	
	print_array('i', 5, and);
	print_array('i', 5, or);

	double d4mean[5] = {1,2,3,4,5};
	double mean = mean_double_array(d4mean, 5);
	double sum = sum_double_array(d4mean, 5);
	printf("mean: %f\n", mean);
	printf("sum: %f\n", sum);



	getchar();
}


#endif
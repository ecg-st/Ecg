#include "my_array_operation.h"
//checked by Huo
/////////////////////////////////////////////////////////////////////////
//��double��������
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
//��double������ƽ��
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
//��double�������
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
//��int����Ԫ������ȡ����ֵ
//////////////////////////////////////////////////////////////////////
void abs_int_array(int *src,		//�����������
				int len,			//����������鳤��
				int *dst = NULL)	//����������
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
//��double����Ԫ������ȡ����ֵ
//////////////////////////////////////////////////////////////////////
void abs_double_array( double *src,			//�����������
					int len,				//����������鳤��
					double *dst = NULL )	//����������
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
//ʵ��int����src��value������Ƚϣ�����Ϊ1������Ϊ0��С��Ϊ-1
//////////////////////////////////////////////////////////////////////
void int_array_cmp_value(int *src,		//�����������
						int len,		//����������鳤��
						int *dst,		//����������
						double value)	//���Ƚϵ�ֵ
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
//ʵ��double����src��value������Ƚϣ�����Ϊ1������Ϊ0��С��Ϊ-1
//////////////////////////////////////////////////////////////////////
void double_array_cmp_value(double const *src,	//�����������
							int len,			//����������鳤��
							int *dst,			//����������
							double value)		//���Ƚϵ�ֵ
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
//��ӡ����
//////////////////////////////////////
void print_array( char type,		//��������
				 int len,			//���鳤��
				 void const *p)		//������ʼ��ַ
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
//ʵ��int����֮����������룬����Ϊ�棬����int
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
//ʵ��double����֮����������룬����Ϊ�棬����int
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
//ʵ��int����������򣬷���Ϊ��
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
//��double�������ҳ����ֵ���������ֵ
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
//��double�������ҳ���Сֵ��������Сֵ
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
//��double�������ҳ����ֵ����������һ����λ�ã�"first" or "last"
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
//��double�������ҳ���Сֵ����������һ����λ�ã�"first" or "last"
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
//��double�������ҳ�ĳ��ֵ����������Ҫ���λ�ã�"first" �� "last" or "all"
//�����first��last�����ֱ�ӷ��أ�
//����Ҳ������򷵻� NO_SUCH_VALUE�������÷���ֵС��0�������Ƿ��ҵ�
//�����all���������ڴ���Ľ�������ָ��ָ���λ�ã������ز��ҵ���������
//�Ҳ����򷵻�0
//////////////////////////////////////////////////////////////////////////////
int find_val_in_double_array(double *p,				//����������
							 int len,				//����ĳ���
							 double val,			//������ֵ
							 char *type = "first",	//������λ��
							 int *pos = NULL )		//���typeΪall������Ҫ����
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
//��int�������ҳ�ĳ��ֵ����������Ҫ���λ�ã�"first" �� "last" or "all"
//�����first��last�����ֱ�ӷ��أ�
//����Ҳ������򷵻� NO_SUCH_VALUE�������÷���ֵС��0�������Ƿ��ҵ�
//�����all���������ڴ���Ľ�������ָ��ָ���λ�ã������ز��ҵ���������
//�Ҳ����򷵻�0
//////////////////////////////////////////////////////////////////////////////
int find_val_in_int_array(int *p,				//����������
							 int len,				//����ĳ���
							 int val,			//������ֵ
							 char *type = "first",	//������λ��
							 int *pos = NULL)		//���typeΪall������Ҫ����
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
//double���� + value
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
//double���� - value
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
//double���� * value
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
//double���� / value
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
//double����src1 + double����src2
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
//double����src1 - double����src2
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
//double����src1 .* double����src2
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
//double����src1 .* int����src2
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
//double����src1 ./ double����src2
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
//int���� + value
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
//int���� - value
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
//int���� * value
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
//int���� / value
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
//int����src1 + int����src2
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
//int����src1 - int����src2
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
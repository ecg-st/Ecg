#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <stdarg.h>
#include "my_array_operation.h"
#define WRONG 1
#define RIGHT 0
//单独测量心律
int *heart_rate_detect(double *dp, int len, int sample_rate);

//生成一个心动周期的波形，len为数组长度，len不得小于100
void generate_ecg( double *dst, int len );

//正向波峰位置检测
int positive_peak_detect(double const * src, int len, int sample_rate);


/*
每个心动周期的参数（有R波视为一个心动周期）
*/
typedef struct {
	int Rpeak_pos;		//Rpeak位置
	int Pstart_pos;		//Pstart位置
	int Ppeak_pos;		//Ppeak位置
	int Pend_pos;		//Pend位置
	int Pperiod;		//P波长度
	int Qstart_pos;		//Qstart位置
	int Qpeak_pos;		//Qpeak位置
	int	Qend_pos;		//Qend位置
	int Qperiod;		//Q波长度
	int Speak_pos;		//Speak位置
	int Send_pos;		//Send位置
	int Tpeak_pos;		//Tpeak位置
	int Tend_pos;		//Tend位置
	int QRSperiod;		//QRS长度
	double Tpeak_value;	//Tpeak幅值
	double Ppeak_value;	//Ppeak幅值
	double Rpeak_value;	//Rpeak幅值
	double Qpeak_value;	//Qpeak幅值
	double Speak_value;	//Speak幅值
	double ST_height;	//ST高度
	int NO_P_WAVE, P_BIPEAK, P_AM_NORMAL, P_TIME_NORMAL;		//P波状态
	int NO_T_WAVE, T_INVERSE, T_BIPEAK, T_AM_NORMAL ;			//T波状态
	int NO_Q_WAVE, Q_AM_NORMAL , Q_TIME_NORMAL;					//Q波状态
	int NO_R_WAVE, R_AM_NORMAL ;								//R波状态
	int NO_S_WAVE, S_AM_NORMAL ;								//S波状态
	int ST_NORMAL;												//ST高度状态
	int QRS_NORMAL;												//QRS状态
	int RR_NORMAL;												//RR间期状态
	int RR_interval;				//RR间期
	int PP_interval;				//PP间期
	int PP_peak_interval;			//PP峰值间期
	int PR_interval;				//PR间期
	int RR_interval_pre_mean;		//以前RR间期平均值，用来判断基准电压
	int	QT_interval;				//QT间期
	int heart_rate;					//心率
	double reference_voltage;		//基准电压
} PERIOD_PARAMETERS;

typedef struct NODE_TAG {
	struct NODE_TAG *next;
	PERIOD_PARAMETERS content;
} NODE;

/*
曲线的拐点检测，先局域法定范围，再微调
*/
int knee_detect( double const *data_p, int len );

/*
用局域法检测QS拐点位置，并做简单的判别
*/
int QS_detect( double const *dp,		//待检测数据起始指针
					 int len,			//待检测数据长度
					 int sample_rate,	//待测数据的采样率
					 int *Qstart_pos,	//Qstart位置
					 int *Qpeak_pos,	//Qpeak位置
					 int *Speak_pos,	//Speak位置
					 int *Send_pos );	//Send位置


int T_detect(double const *src,
			  int len,
			  int *Tpeak_pos,
			  int *Tend_pos,
			  int sample_rate);

void P_detect(double  const *src,
			  int len,
			  int *Pstart_pos,
			  int *Ppeak_pos,
			  int *Pend_pos,
			  int sample_rate);


/*
检测各个波形的关键点，包括PQST波
*/
int keypoints_detect(  double const *dp,			//待检测数据起始指针
						int len,					//待检测数据长度
						int R_pos,					//待检测的心动周期中R波的位置
						int seg_start,				//当前数据段相对于全部数据的起始位置
						int sample_rate,			//采样率
						PERIOD_PARAMETERS *result );	//存放结果的结构体

NODE * R_detect ( double const * dp, int len , int sample_rate);

void moving_detect ( double const * dp, NODE *start , int sample_rate);

NODE * ecg_detect(double *dp, int len, int sample_rate);
//int * ecg_detect(double *dp, int len, int sample_rate);

void para_judgement(double const *data,NODE *NODE_start,int sample_rate);

void wing_function(double const *src,double *dst,int len,int w, double *dst_left);

void filter(double const *src,int len,double *dst,double *pb,int len_b,double *pa,int len_a);

void my_filter(double const *src,double *dst,int len);

void lpfilter(double const *src,double *dst,int len);

void pinghua(double const *src,double *dst,int len);

void lpfilter_1(double const *src,double *dst,int len);

void calculateQrpstPara(NODE *pECGInfo);

int* ParseECGInfo(NODE *pECGInfo, int *pHRinfo);

int* get_ecg_para(double *dp, int len, int sample_rate);
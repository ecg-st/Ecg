#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <stdarg.h>
#include "my_array_operation.h"
#define WRONG 1
#define RIGHT 0
//������������
int *heart_rate_detect(double *dp, int len, int sample_rate);

//����һ���Ķ����ڵĲ��Σ�lenΪ���鳤�ȣ�len����С��100
void generate_ecg( double *dst, int len );

//���򲨷�λ�ü��
int positive_peak_detect(double const * src, int len, int sample_rate);


/*
ÿ���Ķ����ڵĲ�������R����Ϊһ���Ķ����ڣ�
*/
typedef struct {
	int Rpeak_pos;		//Rpeakλ��
	int Pstart_pos;		//Pstartλ��
	int Ppeak_pos;		//Ppeakλ��
	int Pend_pos;		//Pendλ��
	int Pperiod;		//P������
	int Qstart_pos;		//Qstartλ��
	int Qpeak_pos;		//Qpeakλ��
	int	Qend_pos;		//Qendλ��
	int Qperiod;		//Q������
	int Speak_pos;		//Speakλ��
	int Send_pos;		//Sendλ��
	int Tpeak_pos;		//Tpeakλ��
	int Tend_pos;		//Tendλ��
	int QRSperiod;		//QRS����
	double Tpeak_value;	//Tpeak��ֵ
	double Ppeak_value;	//Ppeak��ֵ
	double Rpeak_value;	//Rpeak��ֵ
	double Qpeak_value;	//Qpeak��ֵ
	double Speak_value;	//Speak��ֵ
	double ST_height;	//ST�߶�
	int NO_P_WAVE, P_BIPEAK, P_AM_NORMAL, P_TIME_NORMAL;		//P��״̬
	int NO_T_WAVE, T_INVERSE, T_BIPEAK, T_AM_NORMAL ;			//T��״̬
	int NO_Q_WAVE, Q_AM_NORMAL , Q_TIME_NORMAL;					//Q��״̬
	int NO_R_WAVE, R_AM_NORMAL ;								//R��״̬
	int NO_S_WAVE, S_AM_NORMAL ;								//S��״̬
	int ST_NORMAL;												//ST�߶�״̬
	int QRS_NORMAL;												//QRS״̬
	int RR_NORMAL;												//RR����״̬
	int RR_interval;				//RR����
	int PP_interval;				//PP����
	int PP_peak_interval;			//PP��ֵ����
	int PR_interval;				//PR����
	int RR_interval_pre_mean;		//��ǰRR����ƽ��ֵ�������жϻ�׼��ѹ
	int	QT_interval;				//QT����
	int heart_rate;					//����
	double reference_voltage;		//��׼��ѹ
} PERIOD_PARAMETERS;

typedef struct NODE_TAG {
	struct NODE_TAG *next;
	PERIOD_PARAMETERS content;
} NODE;

/*
���ߵĹյ��⣬�Ⱦ��򷨶���Χ����΢��
*/
int knee_detect( double const *data_p, int len );

/*
�þ��򷨼��QS�յ�λ�ã������򵥵��б�
*/
int QS_detect( double const *dp,		//�����������ʼָ��
					 int len,			//��������ݳ���
					 int sample_rate,	//�������ݵĲ�����
					 int *Qstart_pos,	//Qstartλ��
					 int *Qpeak_pos,	//Qpeakλ��
					 int *Speak_pos,	//Speakλ��
					 int *Send_pos );	//Sendλ��


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
���������εĹؼ��㣬����PQST��
*/
int keypoints_detect(  double const *dp,			//�����������ʼָ��
						int len,					//��������ݳ���
						int R_pos,					//�������Ķ�������R����λ��
						int seg_start,				//��ǰ���ݶ������ȫ�����ݵ���ʼλ��
						int sample_rate,			//������
						PERIOD_PARAMETERS *result );	//��Ž���Ľṹ��

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
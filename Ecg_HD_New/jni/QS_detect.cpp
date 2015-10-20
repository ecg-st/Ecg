#include "head_ecg.h"

#define TSPACE_Q		0.08;		//单位s
#define TSPACE_QSTART	0.1;		//单位s
#define TSPACE_S		0.1;		//单位s
#define TSPACE_SEND		0.12;		//单位s
//checked by Huo
/*
用局域法检测QS拐点位置，并做简单的判别
*/
int QS_detect( double const *dp,		//待检测数据起始指针
					 int len,			//待检测数据长度
					 int sample_rate,	//待测数据的采样率
					 int *Qstart_pos,	//Qstart位置
					 int *Qpeak_pos,	//Qpeak位置
					 int *Speak_pos,	//Speak位置
					 int *Send_pos )	//Send位置
{
	/*
	int Tspace4Q		= sample_rate * TSPACE_Q;
	int Tspace4Qstart	= sample_rate * TSPACE_QSTART;
	int Tspace4S		= sample_rate * TSPACE_S;
	int Tspace4Send		= sample_rate * TSPACE_SEND;
	*/
	///////////////////////////////
	//R点检测
	///////////////////////////////
	int R_pos = find_max_double_array( dp, len, "first" );
//debug
//	puts("R detect finished");

	///////////////////////////////
	//Q波检测
	///////////////////////////////
	//接下来要做的事：用dstart来提高程序稳定性。
	//
	//
	/*
	int dstart = R_pos - Tspace4Q;
	if( dstart < 0 )dstart = 0;
	int seg_len = R_pos - dstart;
	*/
	int dstart = 0;
	int seg_len = R_pos + 1;
	int Qpeak = knee_detect( &(dp[dstart]), 
							seg_len );
	int Qstart = knee_detect( &(dp[dstart]), 
							Qpeak - dstart + 1 );
	*Qpeak_pos = Qpeak + dstart;
	*Qstart_pos = Qstart + dstart;
//debug:
//	puts("Q detect finished");


	///////////////////////////////
	//S波检测
	///////////////////////////////
	/*
	if( dstart + Tspace4S > len )
		seg_len = len - dstart; 
	else 
		seg_len = Tspace4S;
	*/
	dstart = R_pos;
	seg_len = len - R_pos;
	int Speak = knee_detect( &(dp[dstart]), 
							seg_len) + dstart;
	dstart = Speak;
	seg_len = len - Speak;
	int Send = knee_detect( &(dp[dstart]), 
							seg_len) + dstart;
	*Speak_pos = Speak;
	*Send_pos = Send;

	
//debug:
//	puts("S detect finished");

	return 0;
}

#if 0

int main()
{
	const int len = 500;
	double ecg[len];
	generate_ecg(ecg, len);
	int dstart = len * 0.30;
	int seg_len = len * 0.46 - dstart;
	int Qs = 0, Qp = 0, Sp = 0, Se = 0;
	QS_detect(&(ecg[dstart]), seg_len, len, &Qs, &Qp, &Sp, &Se);
	printf("  Qstart: %d\n  Qpeak: %d\n  Speak: %d\n  Send: %d\n", Qs, Qp, Sp, Se);
	getchar();

}




#endif
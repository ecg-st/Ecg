#include "head_ecg.h"

#define TSPACE_Q		0.08;		//��λs
#define TSPACE_QSTART	0.1;		//��λs
#define TSPACE_S		0.1;		//��λs
#define TSPACE_SEND		0.12;		//��λs
//checked by Huo
/*
�þ��򷨼��QS�յ�λ�ã������򵥵��б�
*/
int QS_detect( double const *dp,		//�����������ʼָ��
					 int len,			//��������ݳ���
					 int sample_rate,	//�������ݵĲ�����
					 int *Qstart_pos,	//Qstartλ��
					 int *Qpeak_pos,	//Qpeakλ��
					 int *Speak_pos,	//Speakλ��
					 int *Send_pos )	//Sendλ��
{
	/*
	int Tspace4Q		= sample_rate * TSPACE_Q;
	int Tspace4Qstart	= sample_rate * TSPACE_QSTART;
	int Tspace4S		= sample_rate * TSPACE_S;
	int Tspace4Send		= sample_rate * TSPACE_SEND;
	*/
	///////////////////////////////
	//R����
	///////////////////////////////
	int R_pos = find_max_double_array( dp, len, "first" );
//debug
//	puts("R detect finished");

	///////////////////////////////
	//Q�����
	///////////////////////////////
	//������Ҫ�����£���dstart����߳����ȶ��ԡ�
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
	//S�����
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
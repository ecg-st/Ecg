#include "head_ecg.h"
//checked by Huo
/////////////////////////////////////////////////////////////////////////////////
//���������εĹؼ��㣬����PQST��
/////////////////////////////////////////////////////////////////////////////////
int keypoints_detect(  double const *dp,			//�����������ʼָ��
						int len,					//��������ݳ���
						int R_pos,					//������Ķ�������R����λ��
						int seg_start,				//��ǰ���ݶ������ȫ�����ݵ���ʼλ��
						int sample_rate,			//������
						PERIOD_PARAMETERS *result )	//��Ž���Ľṹ��
{
	if(dp == NULL || result == NULL){
		//puts("FUNCTION keypoints_detect ERROR: pointers invalid!");
		return 0;
	}
	////////////////////////////////////////////////////////////////////
	//���QS���ؼ���
	////////////////////////////////////////////////////////////////////
	//QS��������λ��
	int *Qstart_pos = &(result->Qstart_pos);
	int *Qpeak_pos = &(result->Qpeak_pos);
	int *Speak_pos = &(result->Speak_pos);
	int *Send_pos = &(result->Send_pos);

	//QS�����������ͳ���
	int dstart_QS = R_pos - sample_rate * 0.11;
	int len_QS = sample_rate * 0.23;
	
	//QS���
	QS_detect( &(dp[dstart_QS]),	//�����������ʼָ��
					len_QS,				//��������ݳ���
					sample_rate,		//�������ݵĲ�����
					Qstart_pos,			//Qstartλ��
					Qpeak_pos,			//Qpeakλ��
					Speak_pos,			//Speakλ��
					Send_pos );			//Sendλ��
	*Qstart_pos += (dstart_QS );
	*Qpeak_pos += (dstart_QS );
	*Send_pos += (dstart_QS );
	*Speak_pos += (dstart_QS );
	
	//puts("******************QS detection finished.********************");

	////////////////////////////////////////////////////////////////////
	//���T���ؼ���
	////////////////////////////////////////////////////////////////////
	//T���ؼ�����λ��
	int *Tend_pos = &(result->Tend_pos);
	int *Tpeak_pos = &(result->Tpeak_pos);

	//T������������ͳ���
	int dstart_T = result->Send_pos;
	int len_T = ( (dstart_T + sample_rate * 0.45) > len ) ? len - dstart_T : sample_rate * 0.45;
	if(len_T>sample_rate * 0.1)
	//T�����
		T_detect( &(dp[dstart_T]),
				  len_T,
				  Tpeak_pos,
				  Tend_pos,
				  sample_rate);
	else{
		*Tend_pos = 0;
		*Tpeak_pos = 0;
	}
	*Tend_pos += (dstart_T );
	*Tpeak_pos += (dstart_T );

	
	//puts("******************T detection finished.********************");

	////////////////////////////////////////////////////////////////////
	//���P���ؼ���
	////////////////////////////////////////////////////////////////////
	//P���ؼ�����λ��
	int *Pstart_pos = &(result->Pstart_pos);
	int *Ppeak_pos = &(result->Ppeak_pos);
	int *Pend_pos = &(result->Pend_pos);
	
	//P������������ͳ���
	int dstart_P = ( (R_pos - sample_rate * 0.4) < 0 )? 0 : R_pos - sample_rate * 0.4;
	int len_P = result->Qstart_pos - dstart_P + 1;
	
	//puts("******************before P detect.********************");
	if(len_P>0.05*sample_rate)
		//P�����
		P_detect(&(dp[dstart_P]),
					len_P, 
					Pstart_pos, 
					Ppeak_pos, 
					Pend_pos, 
					sample_rate);
	else{
		*Pstart_pos =0;
		*Ppeak_pos =0;
		*Pend_pos =0;
	}
	*Pstart_pos += (dstart_P );
	*Ppeak_pos += (dstart_P );
	*Pend_pos += (dstart_P );

	//puts("******************P detection finished.********************");

	*Pstart_pos += seg_start;
	*Ppeak_pos += seg_start;
	*Pend_pos += seg_start;
	*Qstart_pos += seg_start;
	*Qpeak_pos += seg_start;
	*Speak_pos += seg_start;
	*Send_pos += seg_start;
	*Tpeak_pos += seg_start;
	*Tend_pos += seg_start;
	
	return 1;
}
#include "head_ecg.h"
//checked by Huo
/////////////////////////////////////////////////////////////////////////////////
//检测各个波形的关键点，包括PQST波
/////////////////////////////////////////////////////////////////////////////////
int keypoints_detect(  double const *dp,			//待检测数据起始指针
						int len,					//待检测数据长度
						int R_pos,					//待检测心动周期中R波的位置
						int seg_start,				//当前数据段相对于全部数据的起始位置
						int sample_rate,			//采样率
						PERIOD_PARAMETERS *result )	//存放结果的结构体
{
	if(dp == NULL || result == NULL){
		//puts("FUNCTION keypoints_detect ERROR: pointers invalid!");
		return 0;
	}
	////////////////////////////////////////////////////////////////////
	//检测QS波关键点
	////////////////////////////////////////////////////////////////////
	//QS检测结果存放位置
	int *Qstart_pos = &(result->Qstart_pos);
	int *Qpeak_pos = &(result->Qpeak_pos);
	int *Speak_pos = &(result->Speak_pos);
	int *Send_pos = &(result->Send_pos);

	//QS检测的数据起点和长度
	int dstart_QS = R_pos - sample_rate * 0.11;
	int len_QS = sample_rate * 0.23;
	
	//QS检测
	QS_detect( &(dp[dstart_QS]),	//待检测数据起始指针
					len_QS,				//待检测数据长度
					sample_rate,		//待测数据的采样率
					Qstart_pos,			//Qstart位置
					Qpeak_pos,			//Qpeak位置
					Speak_pos,			//Speak位置
					Send_pos );			//Send位置
	*Qstart_pos += (dstart_QS );
	*Qpeak_pos += (dstart_QS );
	*Send_pos += (dstart_QS );
	*Speak_pos += (dstart_QS );
	
	//puts("******************QS detection finished.********************");

	////////////////////////////////////////////////////////////////////
	//检测T波关键点
	////////////////////////////////////////////////////////////////////
	//T波关键点存放位置
	int *Tend_pos = &(result->Tend_pos);
	int *Tpeak_pos = &(result->Tpeak_pos);

	//T波检测数据起点和长度
	int dstart_T = result->Send_pos;
	int len_T = ( (dstart_T + sample_rate * 0.45) > len ) ? len - dstart_T : sample_rate * 0.45;
	if(len_T>sample_rate * 0.1)
	//T波检测
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
	//检测P波关键点
	////////////////////////////////////////////////////////////////////
	//P波关键点存放位置
	int *Pstart_pos = &(result->Pstart_pos);
	int *Ppeak_pos = &(result->Ppeak_pos);
	int *Pend_pos = &(result->Pend_pos);
	
	//P波检测数据起点和长度
	int dstart_P = ( (R_pos - sample_rate * 0.4) < 0 )? 0 : R_pos - sample_rate * 0.4;
	int len_P = result->Qstart_pos - dstart_P + 1;
	
	//puts("******************before P detect.********************");
	if(len_P>0.05*sample_rate)
		//P波检测
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
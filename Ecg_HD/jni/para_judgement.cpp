#include "head_ecg.h"
#define NOT_SURE 0
//checked by Huo
void para_judgement(double const *data,NODE *NODE_start,int sample_rate)
{
	NODE *current = NULL;
	NODE *previous = NODE_start;
	int n=1;		//用来表征第几个心电周期
	while(previous -> next != NULL)
	{
		current = previous -> next;
		//*********************//
		//*******各种判断******//
		//*********************//

		//RR_interval
		if(previous == NODE_start)
		{
			current -> content.RR_interval = 0;
			current -> content.RR_interval_pre_mean = 0;
		}
		else
		{
			current -> content.RR_interval = current -> content.Rpeak_pos - previous -> content.Rpeak_pos;
			current -> content.RR_interval_pre_mean = (current -> content.RR_interval_pre_mean * n + current -> content.RR_interval) / (n+1);

		}
		if(previous == NODE_start)
			current -> content.heart_rate = 0;
		else
			current -> content.heart_rate = (double)sample_rate / current -> content.RR_interval * 60;
//for test
//		printf("heart_rate: %f  \n", current->content.heart_rate);
		if((current -> content.heart_rate >= 120 || current -> content.heart_rate <= 50)&&previous != NODE_start)
		{
			//puts("Heart_rate ERROR!\n");
			current -> content.RR_NORMAL = WRONG;
		}
		//基线电压
		int start_pos = 0,end_pos = current -> content.Pstart_pos;
		double sum = 0;
		if(previous == NODE_start)
			start_pos = 0;
		else if(current -> content.RR_interval > 1.5 * previous -> content.RR_interval_pre_mean)
			start_pos = end_pos - sample_rate * 0.02 > 0 ? end_pos - sample_rate * 0.02 : 0; //有时end_pos = 0, start_pos = -20， 下面的data[i]会变成data[-20]，内存访问出错，modified by Huo
		else 
			start_pos = previous -> content.Tend_pos + 0.05 * sample_rate;

		for(int i=start_pos;i<end_pos;i++)
		{
			sum += data[i];
		}
		if((end_pos - start_pos + 1) != 0)
			current -> content.reference_voltage = sum / (end_pos - start_pos + 1);
		//T波状态
		current -> content.Tpeak_value = data[current -> content.Tpeak_pos] - current -> content.reference_voltage;
		current -> content.Rpeak_value = data[current -> content.Rpeak_pos] - current -> content.reference_voltage;
		if(current -> content.Tpeak_value < 0)
		{

			current -> content.T_INVERSE = WRONG;
			//puts("T inverse!\n");
		}
		else if((current -> content.Tpeak_value < 0.1 * current -> content.Rpeak_value)&&previous != NODE_start)
		{
			current -> content.T_AM_NORMAL = WRONG;
			//puts("T amplitude ERROR!\n");
		}
		//P波状态
		current -> content.Pperiod = current -> content.Pend_pos - current -> content.Pstart_pos;
		current -> content.Ppeak_value = data[current -> content.Ppeak_pos] - current -> content.reference_voltage;
		if(current -> content.Pperiod / sample_rate > 0.11)
		{
			current -> content.P_TIME_NORMAL = WRONG;
			//puts("P period ERROR\n");
		}
		if((current -> content.Ppeak_value < 0.01 * current -> content.Rpeak_value || current -> content.Pperiod  < 0.05 * sample_rate)&&previous != NODE_start)
		{
			current -> content.NO_P_WAVE =WRONG;
			//puts("No P wave!\n");
		}
		//Q波状态
		
		
		if((data[current -> content.Qpeak_pos]>data[current -> content.Qstart_pos])&&previous != NODE_start)
		{
			current -> content.NO_Q_WAVE =WRONG;
			//puts("No Q wave!\n");
		}
		int k=current -> content.Qpeak_pos;
		if(current -> content.NO_Q_WAVE == WRONG)
			current -> content.Qperiod = 0;
		else
			while(data[k]<data[current -> content.Qstart_pos])
			{
				k++;
			}
		current -> content.Qperiod = k - current -> content.Qstart_pos;	

		if(current -> content.NO_Q_WAVE != WRONG)
			current -> content.Qpeak_value = current -> content.reference_voltage - data[current -> content.Qpeak_pos];
		else
			current -> content.Qpeak_value = current -> content.reference_voltage - data[current -> content.Qstart_pos];
		
		if((current -> content.Qpeak_value > 0.35 * current -> content.Rpeak_value)&&previous != NODE_start)
		{
			current -> content.Q_AM_NORMAL = WRONG;
			//puts("Q amplitude ERROR\n");
		}
		//S波状态
		current -> content.Speak_value = current -> content.reference_voltage - data[current -> content.Speak_pos];
		if((current -> content.Speak_value < 0.05 * current -> content.Rpeak_value)&&previous != NODE_start)
		{
			current -> content.NO_S_WAVE =WRONG;
			//puts("No S wave!\n");
		}
		int J;
		if(current -> content.NO_S_WAVE == WRONG)
			J = current -> content.Speak_pos + 0.06 * sample_rate;
		else
			J = current -> content.Speak_pos + 0.08 * sample_rate;
		current -> content.ST_height = data[J] - current -> content.reference_voltage;
		if((current -> content.ST_height * 2 / 1000 >  200)&&previous != NODE_start)
		{
			current -> content.ST_NORMAL = WRONG;
			//puts("ST heignt ERROR!\n");
		}
		//R波状态
		current -> content.Rpeak_value = data[current -> content.Rpeak_pos] - current -> content.reference_voltage;
		//QRS波状态
		current -> content.QRSperiod = current -> content.Send_pos - current -> content.Qstart_pos;
		if(current -> content.QRSperiod > 0.2 * sample_rate)
		{
			current -> content.QRS_NORMAL = WRONG;
			//puts("QRS period ERROR!\n");
		}
		if(previous == NODE_start)
		{
			//current -> content.PP_interval = 0;
			//current -> content.PP_peak_interval = 0;
		}
		else
		{
			current -> content.PP_interval = current -> content.Pstart_pos - previous -> content.Pstart_pos;
			current -> content.PP_peak_interval = current -> content.Ppeak_pos - previous -> content.Ppeak_pos;
		}
		current -> content.PR_interval = current -> content.Qstart_pos - current -> content.Pstart_pos;
		current -> content.QT_interval = current -> content.Tend_pos - current -> content.Qstart_pos;
		//**********************//
		previous = current;
		n++;
	}
}
#if 0
void main()
{
	const int len = 100;
	double ecg[400];
	generate_ecg(ecg, len);
	generate_ecg(&ecg[100], len);
	generate_ecg(&ecg[200], len);
	generate_ecg(&ecg[300], len);
	NODE n1,n2,n3,n4,tmp;
	NODE *NODE1=&n1,*NODE2=&n2,*NODE3=&n3,*NODE4=&n4;
	NODE *start=&tmp;
	start -> next = NODE1;
	NODE1 -> next = NODE2;
	NODE2 -> next = NODE3;
	NODE3 -> next = NODE4;
	NODE4 -> next = NULL;
	NODE1 -> content.Rpeak_pos = 0.38 * len;
	NODE1 -> content.Pstart_pos = 0.07 * len;
	NODE1 -> content.Ppeak_pos = 0.14 * len;
	NODE1 -> content.Pend_pos = 0.22 * len;
	NODE1 -> content.Qstart_pos = 0.31 * len;
	NODE1 -> content.Qpeak_pos = 0.33 * len;
	NODE1 -> content.Qend_pos = 0.35 * len;
	NODE1 -> content.Speak_pos = 0.43 * len;
	NODE1 -> content.Send_pos = 0.45 * len;
	//NODE1 -> content.Tstart_pos = 0.50 * len;
	NODE1 -> content.Tpeak_pos = 0.71 * len;
	NODE1 -> content.Tend_pos = 0.78 * len;

	
	NODE2 -> content.Rpeak_pos = 0.38 * len+100;
	NODE2 -> content.Pstart_pos = 0.07 * len+100;
	NODE2 -> content.Ppeak_pos = 0.14 * len+100;
	NODE2 -> content.Pend_pos = 0.22 * len+100;
	NODE2 -> content.Qstart_pos = 0.31 * len+100;
	NODE2 -> content.Qpeak_pos = 0.33 * len+100;
	NODE2 -> content.Qend_pos = 0.35 * len+100;
	NODE2 -> content.Speak_pos = 0.43 * len+100;
	NODE2 -> content.Send_pos = 0.45 * len+100;
	//NODE1 -> content.Tstart_pos = 0.50 * len;
	NODE2 -> content.Tpeak_pos = 0.71 * len+100;
	NODE2 -> content.Tend_pos = 0.78 * len+100;

	NODE3 -> content.Rpeak_pos = 0.38 * len+200;
	NODE3 -> content.Pstart_pos = 0.07 * len+200;
	NODE3 -> content.Ppeak_pos = 0.14 * len+200;
	NODE3 -> content.Pend_pos = 0.22 * len+200;
	NODE3 -> content.Qstart_pos = 0.31 * len+200;
	NODE3 -> content.Qpeak_pos = 0.33 * len+200;
	NODE3 -> content.Qend_pos = 0.35 * len+200;
	NODE3 -> content.Speak_pos = 0.43 * len+200;
	NODE3 -> content.Send_pos = 0.45 * len+200;
	//NODE1 -> content.Tstart_pos = 0.50 * len;
	NODE3 -> content.Tpeak_pos = 0.71 * len+200;
	NODE3 -> content.Tend_pos = 0.78 * len+200;

	NODE4 -> content.Rpeak_pos = 0.38 * len+300;
	NODE4 -> content.Pstart_pos = 0.07 * len+300;
	NODE4 -> content.Ppeak_pos = 0.14 * len+300;
	NODE4 -> content.Pend_pos = 0.22 * len+300;
	NODE4 -> content.Qstart_pos = 0.31 * len+300;
	NODE4 -> content.Qpeak_pos = 0.33 * len+300;
	NODE4 -> content.Qend_pos = 0.35 * len+300;
	NODE4 -> content.Speak_pos = 0.43 * len+300;
	NODE4 -> content.Send_pos = 0.45 * len+300;
	//NODE1 -> content.Tstart_pos = 0.50 * len;
	NODE4 -> content.Tpeak_pos = 0.71 * len+300;
	NODE4 -> content.Tend_pos = 0.78 * len+300;

	para_judgement(ecg,400,start,100);
	printf("%d\n",NODE1 -> content.Pperiod);
	printf("%d\n",NODE1 -> content.Qperiod);
	printf("%d\n",NODE1 -> content.QRSperiod);
	printf("%f\n",NODE1 -> content.Ppeak_value);
	printf("%f\n",-NODE1 -> content.Qpeak_value);
	printf("%f\n",NODE1 -> content.Rpeak_value);
	printf("%f\n",-NODE1 -> content.Speak_value);
	printf("%f\n",NODE1 -> content.Tpeak_value);
	printf("%f\n",NODE1 -> content.ST_height);
	printf("%d\n",NODE1 -> content.RR_interval);
	printf("%d\n",NODE1 -> content.heart_rate);
	printf("%f\n",NODE1 -> content.reference_voltage);

	printf("%d\n",NODE2 -> content.Pperiod);
	printf("%d\n",NODE2 -> content.Qperiod);
	printf("%d\n",NODE2 -> content.QRSperiod);
	printf("%f\n",NODE2 -> content.Ppeak_value);
	printf("%f\n",-NODE2 -> content.Qpeak_value);
	printf("%f\n",NODE2 -> content.Rpeak_value);
	printf("%f\n",-NODE2 -> content.Speak_value);
	printf("%f\n",NODE2 -> content.Tpeak_value);
	printf("%f\n",NODE2 -> content.ST_height);
	printf("%d\n",NODE2 -> content.RR_interval);
	printf("%d\n",NODE2 -> content.heart_rate);
	printf("%f\n",NODE2 -> content.reference_voltage);

	printf("%d\n",NODE3 -> content.Pperiod);
	printf("%d\n",NODE3 -> content.Qperiod);
	printf("%d\n",NODE3 -> content.QRSperiod);
	printf("%f\n",NODE3 -> content.Ppeak_value);
	printf("%f\n",-NODE3 -> content.Qpeak_value);
	printf("%f\n",NODE3 -> content.Rpeak_value);
	printf("%f\n",-NODE3 -> content.Speak_value);
	printf("%f\n",NODE3 -> content.Tpeak_value);
	printf("%f\n",NODE3 -> content.ST_height);
	printf("%d\n",NODE3 -> content.RR_interval);
	printf("%d\n",NODE3 -> content.heart_rate);
	printf("%f\n",NODE3 -> content.reference_voltage);

	printf("%d\n",NODE4 -> content.Pperiod);
	printf("%d\n",NODE4 -> content.Qperiod);
	printf("%d\n",NODE4 -> content.QRSperiod);
	printf("%f\n",NODE4 -> content.Ppeak_value);
	printf("%f\n",-NODE4 -> content.Qpeak_value);
	printf("%f\n",NODE4 -> content.Rpeak_value);
	printf("%f\n",-NODE4 -> content.Speak_value);
	printf("%f\n",NODE4 -> content.Tpeak_value);
	printf("%f\n",NODE4 -> content.ST_height);
	printf("%d\n",NODE4 -> content.RR_interval);
	printf("%d\n",NODE4 -> content.heart_rate);
	printf("%f\n",NODE4 -> content.reference_voltage);
	system("PAUSE");


}
#endif
#include "head_ecg.h"
#include <math.h>
//for windows
#define BYTE unsigned char
//#define QPRST_PARA_LENGTH 14
#define QPRST_PARA_LENGTH 100

static int m_clockseg =0;
static int m_shrinkfreq = 0;

double m_Tpeak = 0;
double m_Tperiod = 0;
double m_QTinterval = 0;
double m_STseg = 0;
double m_Ppeak = 0;
double m_Pperiod = 0;
double m_PPinterval = 0;
double m_PPinterval2 = 0;
double m_PRinterval = 0;
double m_QRS = 0;
double m_QRSabnormal = 0;
double m_Rpeak = 0;
double m_Qpeak = 0;
double m_Qperiod = 0;
bool m_bTinversed = false;
int m_HeartRate = 0;
int m_curRR = 0;
int m_lastRR = 0;
int AbnormalList[14]; //add the unknown abnormal in Abnormallist[13], by Huo
int thirdPara[QPRST_PARA_LENGTH];

int* get_ecg_para(double *dp, int len, int sample_rate) {
	NODE * qrst = ecg_detect(dp, len, sample_rate);
//	int* pHRinfo = heart_rate_detect(dp, len, sample_rate); //modified by Huo
	int* pHRinfo = NULL;
	return ParseECGInfo(qrst, pHRinfo);
}

int* ParseECGInfo(NODE *pECGInfo, int *pHRinfo) {

	NODE *head = pECGInfo;
	NODE *current = head;
	NODE *prev = head;
    m_clockseg++;
	int *ret = (int *)calloc(QPRST_PARA_LENGTH+1, sizeof(int));
	//pass build
	int m_meanHeartRate = 0;
	double m_meanPPinterval = 0;
	double m_meanPperiod = 0;
	double m_meanPPinterval2 = 0;
	double m_meanPRinterval = 0;
	double m_meanQperiod = 0;
	int m_nHeartRate = 0;
	int m_nQ = 0;
	int m_nQRS = 0;
	int m_nP = 0;
	double m_meanQpeak = 0;
	int m_nQp = 0;
	double m_meanQRS = 0;
	double m_meanRpeak = 0;
	int m_nR = 0;
	double m_meanTpeak = 0;
	double m_meanQTinterval = 0;
	int m_nT = 0;
	double m_meanSTseg = 0;
	int m_nST = 0;;
    //End pass build


	int n_T = 0; //��⵽T������
	int n_ST = 0; //��⵽ST�θ���
	int n_P = 0; //��⵽P������
	int n_QRS = 0; //��⵽QRS��Ⱥ����
	int n_R = 0; //��⵽R������
//	int n_R2 = 0;//����2��⵽R�����������ȶ���
	int n_Q = 0; //��⵽Q������
	int n_Tinverse = 0; //��⵽T�����ô���
	int meanRR = 0;
	int validcount = 0;
	int i, j;

	double QRSperiod[100];  //��ű��μ�⵽������QRS���
	double RR_interval[100]; //��ű��μ�⵽������RR����
	bool T_inverse[100]; //���T�����õ��ж�

	for (i=0; i<100; i++)
		T_inverse[i] = false;
//	double RR_interval2[100];//��ŷ���2(��heart_rate_detect()����)��⵽��RR����

	for(i = 0; i < 14; i++) {
	    AbnormalList[i] = 0;
	}

	m_Tpeak = 0;
	m_Tperiod = 0;
	m_QTinterval = 0;

	m_STseg = 0;
	m_Ppeak = 0;
	m_Pperiod = 0;
	m_PPinterval = 0;
	m_PPinterval2 = 0;
	m_PRinterval = 0;
	m_QRS = 0;
	m_QRSabnormal = 0;
	m_Rpeak = 0;
	m_Qpeak = 0;
	m_Qperiod = 0;
	m_bTinversed = false;
	m_HeartRate = 0;
	
	if (pECGInfo != NULL) //modified by Huo
		calculateQrpstPara(pECGInfo);
	else
	{	//���ʧ�ܣ�ֱ�ӷ���
		for(j = 0; j < QPRST_PARA_LENGTH; j++)
			thirdPara[j] = 0;
			ret[j] = 0;
			return ret;
	}
	
	//Copy the qprst wave information
	for(j = 0; j < QPRST_PARA_LENGTH; j++) {
//		LOGV("thirdPara[%d] = %d", j, thirdPara[j]);
	    ret[j] = thirdPara[j];
	}
	

	//�����������ȡ�����нڵ���Ϣ���ж�����ʧ�����Բ��β���ȡƽ��, ���ͷ��ڴ�
	if(current != NULL)
	{
		current = current->next;
		while(current!=NULL && current->next!=NULL)
		{
			prev = current;
			if(current->content.NO_P_WAVE != WRONG && current->content.Pperiod>0 && current->content.PP_peak_interval>0 && current->content.PR_interval>0)
			{
				m_Pperiod += current->content.Pperiod; 
				m_PPinterval += current->content.PP_interval;
				m_PPinterval2 += current->content.PP_peak_interval;
				m_PRinterval += current->content.PR_interval;
				n_P++;
			}
			if(current->content.NO_Q_WAVE != WRONG)
			{
				m_Qpeak += current->content.Qpeak_value;
				m_Qperiod += current->content.Qperiod;
				n_Q++;
			}
			if(current->content.NO_R_WAVE != WRONG && current->content.QRSperiod>1 && current->content.RR_interval>1)
			{
				m_QRS += current->content.QRSperiod * 0.8;
				QRSperiod[n_QRS] = current->content.QRSperiod * 0.8;  //QRS�������
				n_QRS++;
				m_Rpeak += current->content.Rpeak_value;
				//			m_HeartRate +=current->content.heart_rate;
				RR_interval[n_R] = current->content.RR_interval;
				//			meanRR += current->content.RR_interval;
				if (current->content.T_INVERSE == WRONG)
				{
					T_inverse[n_R] = true;
					n_Tinverse++;
				}
				n_R++;
			}
			if(current->content.NO_T_WAVE != WRONG && current->content.QT_interval>0)
			{
				m_Tpeak += current->content.Tpeak_value;
				m_QTinterval += current->content.QT_interval;
				n_T++;
				m_STseg += current->content.ST_height;
				n_ST++;
			}


			current = current->next;	
			free(prev);
		}
		free(current);

	}
		
	//ͳ�Ʊ���ƽ������
	if (n_R > 1)
	{
		//����2���Ĳ��仯С��10%��Ϊ���ϻ�׼����,ͳ��ƽ������ʱ�Է��ϻ�׼���ɵ��Ĳ�Ϊ׼
		for (i=0; i<n_R-1; i++)
		{
			if (abs(RR_interval[i+1] - RR_interval[i]) < 0.1 * RR_interval[i])
			{
				meanRR = meanRR + RR_interval[i] + RR_interval[i+1];
				validcount += 2;
			}
		}
		if (validcount == 0)
		{
			meanRR = 0;
			for (i=0; i<n_R; i++)
				meanRR += RR_interval[i];	
			meanRR /= n_R; 
		}
		else
			meanRR /= validcount; 

		m_HeartRate = 60000 / meanRR;
		m_meanHeartRate += m_HeartRate;
		m_nHeartRate++;
		//��ret[15]��ʼ��¼��RR����ֵ,by Huo
		ret[15] = n_R;
		for (i=0; i<n_R; i++)
			ret[i+16] = (int)(RR_interval[i]); 
	}
	else
	{
		ret[15] = 0;
		m_HeartRate = 0;
		AbnormalList[13] = 1; //��⵽R�����٣���ʾδ֪�쳣
	}
	//�뷽��1����RR���ڽ��бȽϣ������ܴ��򷽷�1����������д���������һ�ɲ��ټ��㣨��0��
//	bool bTestOK = fabs(double(n_R2 - n_R)) <= n_R2 * 0.5; //xiaofei 0.3
		
//	LOGV("n-R2 = %d, n-R = %d\n", n_R2, n_R);
	if (n_R > 1) //modified by Huo
	{
//		LOGV("bTestOK = true");
		//�Ը��Ķ����ڽ��ȡƽ��������Ϊ��ѹ����Ϊ���ν��
		if (n_P>0 && m_Pperiod>0 && m_Pperiod<10000)
		{
			m_Pperiod /= n_P;
			m_PPinterval /= n_P;
			m_PPinterval2 /= n_P;
			m_PRinterval /= n_P;

			m_meanPperiod += m_Pperiod;
			m_meanPPinterval += m_PPinterval;
			m_meanPPinterval2 += m_PPinterval2;
			m_meanPRinterval += m_PRinterval;
			m_nP++;
		}
		else
		{
			m_Pperiod = 0;
			m_PPinterval = 0;
			m_PPinterval2 = 0;
			m_PRinterval = 0;
		}

		if (n_Q>0 && m_Qperiod>0 && m_Qperiod<10000)
		{
			m_Qperiod /= n_Q;
			m_meanQperiod += m_Qperiod;
			m_nQ++;
		}
		else
			m_Qperiod = 0;

		if (n_Q>0 && fabs(m_Qpeak) < 100000)
		{
			m_Qpeak =  m_Qpeak / n_Q * 2 / 1000;
			m_meanQpeak += m_Qpeak;
			m_nQp++;
		}
		else
			m_Qpeak = 0;


		if (n_R>0 && m_QRS>0 && m_QRS<10000)
		{
			m_QRS /= n_R;
			m_meanQRS += m_QRS;
			m_nQRS++;		
		}
		else
		{
			m_QRS = 0;
		}

		if (n_R>0 && fabs(m_Rpeak)<100000)
		{
			m_Rpeak = m_Rpeak / n_R * 2 / 1000; //ת��Ϊ��ѹ��mVֵ
			m_meanRpeak += m_Rpeak;
			m_nR++;
		}
		else
			m_Rpeak = 0;

		if (n_T>0 && fabs(m_Tpeak)<100000)
		{
			m_Tpeak = m_Tpeak / n_T * 2 / 1000;	//ת��Ϊ��ѹ��mVֵ
			m_QTinterval /= n_T;
			m_meanTpeak += m_Tpeak;
			m_meanQTinterval += m_QTinterval;
			m_nT++;
		}
		else
		{
			m_Tpeak = 0;
			m_QTinterval = 0;
		}

		if (n_T>0 && fabs(m_STseg)<100000)
		{
			m_STseg = m_STseg / n_T * 2 / 1000;	//ת��Ϊ��ѹ��mVֵ
			m_meanSTseg += m_STseg;
			m_nST++;
		}
		else
			m_STseg = 0;

		if (n_T * 0.5 < n_Tinverse) //����50%���ϵ�T�����ã���ʾT������
			m_bTinversed = true;
	}
	else
	{
		m_Pperiod = 0;
		m_PPinterval = 0;
		m_PPinterval2 = 0;
		m_PRinterval = 0;
		m_Qperiod = 0;
		m_Qpeak = 0;
		m_QRS = 0;
		m_Rpeak = 0;
		m_Tpeak = 0;
		m_QTinterval = 0;
		m_STseg = 0;

	}
	//����ʧ������Ϣ�����
	// ���ǰ���ж�
	for (i=0; i<14; i++) 
		AbnormalList[i] = 0;
	m_curRR = meanRR;	//���±���ƽ��RR����
	if (m_lastRR == 0)
		m_lastRR = m_curRR;    //��1������û��ǰ���ƽ����������׼�����Ա��μ����ƽ��������Ϊ��׼
	//1.�Ķ�����(�Ķ�������n_R�ĸ���Ҫ��һ����ֻҪĬ�ϵ�n_R>1�������ó���)
	if (m_curRR > 1180)
		AbnormalList[0] = 1;
	bool pb = false; //��¼�ϴ��Ƿ��⵽premature beat
	if (n_R>3)
	{
		//2.�Ķ�����
		if (m_lastRR < 520)
			AbnormalList[1] = 1;
		//12.QRS����쳣
		if (m_QRS > 120)
		{
			AbnormalList[11] = 1;
			m_QRSabnormal++;
		}
		//3,4,5,6,7
		BYTE statemachine1 = 0;
		BYTE statemachine2 = 0;
		BYTE statemachine3 = 0;
		BYTE statemachine4 = 0;

		//m_RRinterval.SetSize(n_R);
		for (i=0; i<n_R; i++)
		{	//3.���ͣ��
			if (i>0 && RR_interval[i] > 1900 && !pb) //�ϴ�Ϊ�粫����������ڲ��ж�
				AbnormalList[2] = 1;
			else
				pb = false;
			//4.©��
			if (i>0 && RR_interval[i] > 1.98 * m_lastRR && !pb) //�ϴ�Ϊ�粫����������ڲ��ж�
				AbnormalList[3] = 1;
			else
				pb = false;
			//6.�����粫
			if ((RR_interval[i] < 0.9 * m_lastRR) && ((QRSperiod[i]>140) || T_inverse[i])) //����T���������
			{
				AbnormalList[5] = 1;
				AbnormalList[11] = 0; //�������粫�Ͳ������г�QRS����쳣
				pb = true;
			}
			else if (i<n_R-1 && RR_interval[i] < 0.8 * m_lastRR && RR_interval[i+1]>1.15*m_lastRR)//5.�����粫(���ϴ�����϶���ж�)
			{
				AbnormalList[4] = 1;
				pb = true;
			}

			//7.���٣������������粫��
			switch(statemachine3)
			{
			case 0:
					if ((RR_interval[i] < 0.8 * m_lastRR) && ((QRSperiod[i]>120) || T_inverse[i])) 
						statemachine3 = 1;
					break;
				case 1:
					if ((RR_interval[i] < 0.8 * m_lastRR) && ((QRSperiod[i]>120) || T_inverse[i])) 
						statemachine3 = 2;
					else
						statemachine3 = 0;
					break;
				case 2:
					if ((RR_interval[i] < 0.8 * m_lastRR) && ((QRSperiod[i]>120) || T_inverse[i])) 
						AbnormalList[6] = 1;
					else
						statemachine3 = 0;
			}
			//8.RonT
			if (RR_interval[i] < 0.375 * log10(10*RR_interval[i] + 70))
				AbnormalList[7] = 1;
			//9.�����˶�
			switch(statemachine1)
			{
			case 0:
				if (RR_interval[i]<300)
					statemachine1 = 1;
				break;
			case 1:
				if (RR_interval[i]<300)
					statemachine1 = 2;
				else
					statemachine1 = 0;
				break;
			case 2:
				if (RR_interval[i]<300)
					statemachine1 = 3;
				else
					statemachine1 = 0;
				break;
			case 3:
				if (RR_interval[i]<300)
					statemachine1 = 4;
				else
					statemachine1 = 0;
				break;
			case 4:
				if (RR_interval[i]<300)
					AbnormalList[8] = 1;
				else
					statemachine1 = 0;
			}
			//12.QRS����쳣(��Ϊƽ��ֵ�жϣ��Ƶ�����,by Huo)
			//if (QRSperiod[i] > 120)
			//{
			//	AbnormalList[11] = 1;
			//	m_QRSabnormal++;
			//}
			//13.QRS��������
			switch(statemachine4)
			{
			case 0:
				if ((QRSperiod[i] > 120) && (RR_interval[i]<600) )
					statemachine4 = 1;
				break;
			case 1:
				if ((QRSperiod[i] > 120) && (RR_interval[i]<600) )
					statemachine4 = 2;
				else
					statemachine4 = 0;
				break;
			case 2:
				if ((QRSperiod[i] > 120) && (RR_interval[i]<600) )
					AbnormalList[12] = 1;
				else
					statemachine4 = 0;
			}

				//���浱ǰRR���ڣ��Ա�����ɢ��ͼ
				//m_RRinterval[i] = RR_interval[i];
		} //end of for

		//10.��������������
		for (i=1; i<n_R; i++)
		{
			if ((RR_interval[i-1] < 0.9 * m_lastRR) && (fabs(RR_interval[i-1] + RR_interval[i] - 2 * m_lastRR) < m_lastRR * 0.1)) //�ж������ǽ�����ȣ�0.1Ϊ�ݶ��Ľ������������
				m_shrinkfreq++;
			//11.������ɲ���(RR�������>0.16s)
				if ( fabs(RR_interval[i] - RR_interval[i-1]) > 160  && !(AbnormalList[2] || AbnormalList[3] || AbnormalList[4] || AbnormalList[5])) //�и����粫©��ʱ������ʾ������ɲ���
					AbnormalList[10] = 1;
		}
		if (m_shrinkfreq>10)
			AbnormalList[9] = 1;
		//�ۼ���1���ӣ�����
		if (m_clockseg >= 6)//10����һ�Σ��ܹ�6��Ϊ1����
		{
			m_shrinkfreq = 0;
			m_clockseg = 0;
		}	

		m_lastRR = m_curRR;	//����RR������Ч����������Ϊ�¶εļ����׼

	}//end of if(n_R>3)


	int abnormalValue = 0;
    for(i = 0; i < 14; i++) {
//		LOGV("AbnormalList[%d] = %d ", i, AbnormalList[i]);
	    abnormalValue |= AbnormalList[i] << i ;

	}
	//Test log.
	//LOGV("abnormalValue = %x ", abnormalValue);
 //   LOGV("m_HeartRate = %d ", m_HeartRate);
	//LOGV("m_curRR = %d ", m_curRR);
	//LOGV("m_lastRR = %d ", m_lastRR);
	//LOGV("m_Tpeak = %f ", m_Tpeak);
	//LOGV("m_Pperiod = %f ", m_Pperiod);
	//LOGV("m_QRS = %f ", m_QRS);
	//LOGV("m_QRSabnormal = %f ", m_QRSabnormal);
	//LOGV("m_Qperiod = %f ", m_Qperiod);
	//LOGV("m_QRS = %f ", m_QRS);
	//LOGV("m_STseg = %f ", m_STseg);
	//LOGV("m_Ppeak = %f ", m_Ppeak);
	//LOGV("m_QTinterval = %f ", m_QTinterval);


    ret[QPRST_PARA_LENGTH] = abnormalValue;
	
	return ret;
}


void calculateQrpstPara(NODE *pECGInfo) {
	NODE *NODE1 = pECGInfo;
	int m_valid_n = 0, m_point = 0;
	int n_QRS = 0;
	int n_ST = 0;
	int n_P = 0;
	int i;
	for(i = 0; i < QPRST_PARA_LENGTH; i++) {
		thirdPara[i] = 0;
	}

	while(NODE1 != NULL) {
		//LOGV("-----%d point ---", m_point++);
		//LOGV("Pperiod = %d\n",NODE1 -> content.Pperiod);
		//LOGV("Qperiod = %d\n",NODE1 -> content.Qperiod);
		//LOGV("QRSperiod = %d\n",NODE1 -> content.QRSperiod);
		//LOGV("Ppeak_value = %f\n",NODE1 -> content.Ppeak_value);
		//LOGV("Qpeak_value = %f\n",NODE1 -> content.Qpeak_value);
		//LOGV("Rpeak_value = %f\n",NODE1 -> content.Rpeak_value);
		//LOGV("Speak_value = %f\n",NODE1 -> content.Speak_value);
		//LOGV("Tpeak_value = %f\n",NODE1 -> content.Tpeak_value);
		//LOGV("ST_height = %f\n",NODE1 -> content.ST_height);
		//LOGV("RR_interval = %d\n",NODE1 -> content.RR_interval);
		//LOGV("heart_rate = %d\n",NODE1 -> content.heart_rate);
		//LOGV("RR_interval_pre_mean = %d\n",NODE1 -> content.RR_interval_pre_mean);
		//LOGV("reference_voltage = %f\n",NODE1 -> content.reference_voltage);

		if(NODE1->content.NO_R_WAVE != WRONG && NODE1->content.QRSperiod>1 && NODE1->content.RR_interval>1){
			thirdPara[2] += NODE1 -> content.QRSperiod * 0.8; //��ʾ������ΪQRS���:
			n_QRS++;
		}
		if(NODE1->content.NO_T_WAVE != WRONG && NODE1->content.QT_interval>0)
		{
			thirdPara[8] += NODE1 -> content.ST_height * 1000;//��ʾ������Ϊ:ST�θ߶�
			thirdPara[13] += NODE1 -> content.QT_interval; //��ʾ������Ϊ:QT����:
			n_ST++;		

		}
		if(NODE1->content.NO_P_WAVE != WRONG && NODE1->content.PR_interval>0)
		{
			thirdPara[11] += NODE1 -> content.PR_interval; //��ʾ������ΪPR����:
			n_P++;
		}

		if(NODE1 -> content.Pperiod > 0 &&  NODE1 -> content.Qperiod > 0 && NODE1 -> content.QRSperiod > 0) {
			thirdPara[0] += NODE1 -> content.Pperiod;
			thirdPara[1] += NODE1 -> content.Qperiod;
			
			thirdPara[3] += NODE1 -> content.Ppeak_value * 1000;
			thirdPara[4] += NODE1 -> content.Qpeak_value * 1000;
			thirdPara[5] += NODE1 -> content.Rpeak_value * 1000;
			thirdPara[6] += NODE1 -> content.Speak_value * 1000;
			thirdPara[7] += NODE1 -> content.Tpeak_value * 1000;
			
			thirdPara[9] += NODE1 -> content.RR_interval;
			thirdPara[10] += NODE1 -> content.heart_rate;

			thirdPara[12] += NODE1 -> content.reference_voltage;

			m_valid_n++;
		}
		NODE1 = NODE1->next;
	}
	if (n_QRS>0)
		thirdPara[2] /= n_QRS;
	else
		thirdPara[2] = 0;
	if (n_ST>0)
	{
		thirdPara[8] /= n_ST;
		thirdPara[13] /= n_ST;
	} 
	else
	{
		thirdPara[8] = 0;
		thirdPara[13] = 0;
	}
	if (n_P>0)
		thirdPara[11] /= n_P;
	else
		thirdPara[11] = 0;


	if (m_valid_n > 1) //modified by Huo
	{
		thirdPara[0] /= m_valid_n;
		thirdPara[1] /= m_valid_n;
		
		thirdPara[3] /= m_valid_n;
		thirdPara[4] /= m_valid_n;
		thirdPara[5] /= m_valid_n;
		thirdPara[6] /= m_valid_n;
		thirdPara[7] /= m_valid_n;
//		thirdPara[8] /= m_valid_n;
		thirdPara[9] /= m_valid_n - 1;
		thirdPara[10] /= m_valid_n - 1;
//		thirdPara[11] /= m_valid_n - 1;
		thirdPara[12] /= m_valid_n;
//		thirdPara[13] = 375 * log10(thirdPara[9] / 100.0 + 0.07);
	}
	else
	{
		for(i = 0; i < QPRST_PARA_LENGTH; i++) 
			thirdPara[i] = 0;
	}
       
}
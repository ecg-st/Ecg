#include "head_ecg.h"

#include <android/log.h>
#define TAG "hello"
#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, TAG, __VA_ARGS__)
//checked by Huo
//int * ecg_detect(double *dp, int len, int sample_rate)
NODE * ecg_detect(double *dp, int len, int sample_rate)
{
//	extern int segment_number;
//	printf("\n********************\nNo. %d segment R_detect.\n********************\n", segment_number);
LOGV("ecg_detect 1");
	NODE *start = R_detect(dp, len, sample_rate);
LOGV("ecg_detect 2");
	if (start == NULL ) return NULL;
	else if (start->next == NULL) {
		free(start);
		return NULL;
	}
	LOGV("ecg_detect 3");
//	printf("\n********************\nNo. %d segment moving_detect.\n********************\n", segment_number);
	moving_detect(dp, start, sample_rate);
	LOGV("ecg_detect 4");
//	printf("\n********************\nNo. %d segment para_judgement.\n********************\n", segment_number);
	para_judgement(dp, start, sample_rate);
LOGV("ecg_detect 5");
//	puts("this place should be replaced by writing the datas to the ram");


	return start;
	//return thirdPara;
}

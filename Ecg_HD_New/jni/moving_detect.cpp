#include "head_ecg.h"
//checked by Huo
void moving_detect ( double const * dp, NODE *start , int sample_rate)
{
	NODE *previous = start;
	NODE *current = previous->next;
	NODE *nextnode = NULL;
	int dstart; 
	int seg_len;
	if(current == NULL)
		//exit(0);
		return;
	while( current->next != NULL ){
		nextnode = current->next;
		if( current->content.RR_interval < sample_rate * 0.2 || nextnode->content.RR_interval < sample_rate * 0.2)
			return;
		//计算窗口起点
		if ( current->content.RR_interval < sample_rate * 0.9 )
			dstart = current->content.Rpeak_pos - current->content.RR_interval * 0.5;
		else
			dstart = current->content.Rpeak_pos - sample_rate * 0.4;
		
		//计算窗长
		if ( nextnode->content.RR_interval < sample_rate * 0.9 )
			seg_len = current->content.Rpeak_pos + nextnode->content.RR_interval * 0.6 - dstart;
		else
			seg_len = current->content.Rpeak_pos + sample_rate * 0.5 - dstart;
		
		int R_pos = current->content.Rpeak_pos - dstart + 1;
		//关键点检测
		keypoints_detect(&(dp[dstart]), seg_len, R_pos, dstart, sample_rate, &(current->content));

		previous = current;		
		current = previous->next;
	}

}
package com.nju.ecg.wave.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import com.nju.ecg.R;
import com.nju.ecg.wave.EcgWelcomeScreen;

public class WidgetProviderBig extends AppWidgetProvider {
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		//创建一个Intent对象
        Intent intent = new Intent(context, EcgWelcomeScreen.class);
        //这里是Broadcast、Activity、Receiver等
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        //通过RemoteViews添加单击事件
        RemoteViews remoteViews  = new RemoteViews(context.getPackageName(), R.layout.appwidget_provider_big);                  
        remoteViews.setOnClickPendingIntent(R.id.launch_img, pendingIntent);
        //更新Appwidget
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);        
	}
}

package com.nju.ecg.wave;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.nju.ecg.R;
import com.nju.ecg.wave.DemoModeResultListener.ModeAction;

public class DemoWaveDataAdapter extends BaseAdapter
{
    private String[] demoNameStrs;
    /**
     * 布局加载器
     */
    private LayoutInflater mInflater;
    
    private DemoModeResultListener mListener;
    
    public DemoWaveDataAdapter(Context context, DemoModeResultListener listener)
    {
        mInflater = LayoutInflater.from(context);
        mListener = listener;
        demoNameStrs = context.getResources().getStringArray(R.array.demo_data_name);
    }
    
    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        ViewHolder mHolder;
        if (convertView == null)
        {
            mHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.demo_wave_data_list_item,
                null);
            mHolder.waveDescTxt = (TextView)convertView.findViewById(R.id.data_desc_txt);
            mHolder.showBtn = (Button)convertView.findViewById(R.id.show_demo_btn);
            mHolder.viewDetailBtn = (Button)convertView.findViewById(R.id.view_detail_btn);
            convertView.setTag(mHolder);
        }
        else
        {
            mHolder = (ViewHolder) convertView.getTag();
        }
        mHolder.waveDescTxt.setText(demoNameStrs[position]);
        mHolder.showBtn.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mListener.onResult(position, ModeAction.ACTION_SHOW);
            }
        });
        
        mHolder.viewDetailBtn.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mListener.onResult(position, ModeAction.ACTION_DETAIl);
            }
        });
        return convertView;
    }
    
    @Override
    public int getCount()
    {
        return demoNameStrs.length;
    }

    @Override
    public Object getItem(int position)
    {
        return demoNameStrs[position];
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    static class ViewHolder
    {
        TextView waveDescTxt;
        Button showBtn;
        Button viewDetailBtn;
    }
}

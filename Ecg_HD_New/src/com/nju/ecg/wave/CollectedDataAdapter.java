package com.nju.ecg.wave;

import java.util.List;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import com.nju.ecg.R;
import com.nju.ecg.model.WaveData;
import com.nju.ecg.wave.CollectedDataResultListener.Action;

/**
 * 显示历史采集数据
 * @author zhuhf
 * @version [ME MTVClient_Handset V100R001C04SPC002, 2012-9-16]
 */
public class CollectedDataAdapter extends BaseAdapter
{
    /** 布局加载器*/
    private LayoutInflater mInflater;
    /** 数据列表*/
    private List<WaveData> mDataList;
    /** 回调接口*/
    private CollectedDataResultListener mListener;
    
    public CollectedDataAdapter(Context context, List<WaveData> dataList, CollectedDataResultListener listener)
    {
        mInflater = LayoutInflater.from(context);
        mDataList = dataList;
        mListener = listener;
    }
    
    @Override
    public int getCount()
    {
        return mDataList.size();
    }

    @Override
    public WaveData getItem(int position)
    {
        return mDataList.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        ViewHolder mHolder;
        if (convertView == null)
        {
            mHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.collected_data_list_item,
                null);
            mHolder.collectTimeTxt = (TextView) convertView
                .findViewById(R.id.time_txt);
            mHolder.replayBtn = (Button) convertView
                .findViewById(R.id.replay_btn);
            mHolder.diagnoseBtn = (Button) convertView
                .findViewById(R.id.diagnose_btn);
            mHolder.dotGraphBtn = (Button) convertView
                .findViewById(R.id.dotGraph_btn);
            mHolder.deleteBtn = (Button) convertView
                .findViewById(R.id.delete_btn);
            convertView.setTag(mHolder);
        }
        else
        {
            mHolder = (ViewHolder) convertView.getTag();
        }
        if (mDataList.get(position).isCustom())
        {
            String filePath = mDataList.get(position).getFilePath();
            mHolder.collectTimeTxt.setText(filePath.substring(filePath.lastIndexOf("/") + 1));
//            mHolder.diagnoseBtn.setVisibility(View.GONE);
//            mHolder.dotGraphBtn.setVisibility(View.GONE);
        }
        else
        {
            mHolder.collectTimeTxt.setText(mDataList.get(position).getCollectFormatedTime());
//            mHolder.diagnoseBtn.setVisibility(View.VISIBLE);
//            mHolder.dotGraphBtn.setVisibility(View.VISIBLE);
        }
        mHolder.replayBtn.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mListener.onResult(position, getItem(position), Action.ACTION_REPLAY);
            }
        });
        
        mHolder.diagnoseBtn.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mListener.onResult(position, getItem(position), Action.ACTION_DIAGNOSE);
            }
        });
        
        mHolder.dotGraphBtn.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mListener.onResult(position, getItem(position), Action.ACTION_DOT_GRAPH);
            }
        });
        
        mHolder.deleteBtn.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mListener.onResult(position ,getItem(position), Action.ACTION_DELETE);
            }
        });
        return convertView;
    }

    static class ViewHolder
    {
        TextView collectTimeTxt;
        Button replayBtn;
        Button diagnoseBtn;
        Button dotGraphBtn;
        Button deleteBtn;
    }
}

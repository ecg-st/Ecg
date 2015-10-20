package com.nju.ecg.wave;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.nju.ecg.R;
import com.nju.ecg.basic.BasicActivity;

public class FileListActivity extends BasicActivity implements OnItemClickListener
{
    private List<String> items = null;
    private List<String> paths = null;
    private final String rootpath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/EcgApp/DataDir";
    private TextView pathTxt;
    private ListView fileList;
    private FileListAdapter fileListAdapter;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_list);
        pathTxt = (TextView) findViewById(R.id.path_txt);
        fileList = (ListView) findViewById(R.id.file_list);
        fileList.setOnItemClickListener(this);
        getFileDir(rootpath);
    }

    private void getFileDir(String filepath)
    {
        pathTxt.setText(filepath);
        items = new ArrayList<String>();
        paths = new ArrayList<String>();
        File f = new File(filepath);
        File[] files = f.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				if (file.isDirectory())
				{
					return true;
				}
				else if (file.isFile() && file.getName().endsWith(".raw"))
				{
					return true;
				}
				return false;
			}
		});
        if (!filepath.equals(rootpath))
        {
            items.add("Back to" + rootpath);
            paths.add(rootpath);
            items.add("Back to ../");
            paths.add(f.getParent());
        }

        if (files != null)
        {
            for (int i = 0; i < files.length; i++)
            {
                items.add(files[i].getName());
                paths.add(files[i].getPath());
            }
        }
        if (fileListAdapter == null)
        {
            fileListAdapter = new FileListAdapter(items);
            fileList.setAdapter(fileListAdapter);
            fileListAdapter.notifyDataSetChanged();
        }
        else
        {
            fileListAdapter.setDataSource(items);
            fileListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        File file = new File(paths.get(position));
        if (file.isDirectory())
        {
            getFileDir(paths.get(position));
        }
        else
        {
            Intent intent = new Intent();
            intent.putExtra("FilePath", file.getAbsolutePath());
            setResult(RESULT_OK, intent);
            finish();
        }
    }
    
    private class FileListAdapter extends BaseAdapter
    {
        private List<String> items;
        private LayoutInflater inflater;
        public FileListAdapter(List<String> items)
        {
            this.items = items;
            inflater = LayoutInflater.from(FileListActivity.this);
        }
        public void setDataSource(List<String> items)
        {
            this.items = items;
        }
        @Override
        public int getCount()
        {
            return items.size();
        }

        @Override
        public String getItem(int position)
        {
            return items.get(position);
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            ViewHolder holder = null;
            if (convertView == null)
            {
                convertView = inflater.inflate(R.layout.file_list_item, null);
                holder = new ViewHolder();
                holder.nameTxt = (TextView)convertView.findViewById(R.id.name_txt);
                convertView.setTag(holder);
            }
            else
            {
                holder = (ViewHolder)convertView.getTag();
            }
            holder.nameTxt.setText(items.get(position));
            return convertView;
        }
        
        private class ViewHolder
        {
            private TextView nameTxt;
        }
    }

}

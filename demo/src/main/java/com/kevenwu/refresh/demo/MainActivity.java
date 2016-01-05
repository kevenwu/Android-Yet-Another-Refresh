package com.kevenwu.refresh.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.kevenwu.refresh.lib.RefreshLayout;


/**
 * Created by keven on 16/1/4.
 */
public class MainActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RefreshLayout refreshLayout = (RefreshLayout)findViewById(R.id.refresh_layout);
        View headerView = LayoutInflater.from(this).inflate(R.layout.list_header, null);
        refreshLayout.setHeaderView(headerView, (ImageView)headerView.findViewById(R.id.fake), R.drawable.bg);

        String[] data = new String[20];
        for (int i = 0; i < data.length; i++) {
            data[i] = "测试item " + i;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter(this, R.layout.list_item,
                data);

        ListView listView = (ListView)findViewById(R.id.list_view);
        listView.setAdapter(adapter);
    }
}

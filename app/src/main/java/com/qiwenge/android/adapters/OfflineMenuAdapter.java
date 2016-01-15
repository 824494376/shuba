package com.qiwenge.android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.qiwenge.android.R;
import com.qiwenge.android.adapters.base.MyBaseAdapter;
import com.qiwenge.android.entity.OfflineMenuItem;

import java.util.List;

public class OfflineMenuAdapter extends MyBaseAdapter<OfflineMenuItem> {

    private ViewHolder viewHolder;

    public OfflineMenuAdapter(Context context, List<OfflineMenuItem> data) {
        this.data = data;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_offline, null);
            viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.item_tv_title);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        OfflineMenuItem model = data.get(position);
        if (model != null) {
            viewHolder.tvTitle.setText(model.title);
        }
        return convertView;
    }

    public class ViewHolder {
        public TextView tvTitle;
    }

}

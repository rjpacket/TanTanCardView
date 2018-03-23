package com.rjp.tantancardview.tantan;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.rjp.tantancardview.R;


/**
 * author : Gimpo create on 2018/3/22 18:33
 * email  : jimbo922@163.com
 */

public class TTView extends TanTanBaseView<String> {
    public TTView(Context context) {
        super(context);
    }

    public TTView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View getView(String item, View convertView) {
        if(convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_tan_tan_view, null);
        }
        TextView tvNum = (TextView) convertView.findViewById(R.id.num);
        tvNum.setText(item);
        return convertView;
    }
}

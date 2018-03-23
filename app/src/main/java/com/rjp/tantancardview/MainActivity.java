package com.rjp.tantancardview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.rjp.tantancardview.tantan.TTView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TTView tanTanView = (TTView) findViewById(R.id.tan_tan_view);
        ArrayList<String> datas = new ArrayList<>();
        datas.add("再");
        datas.add("往");
        datas.add("后");
        datas.add("面");
        datas.add("翻");
        datas.add("几");
        datas.add("下");
        datas.add("你");
        datas.add("会");
        datas.add("有");
        datas.add("惊");
        datas.add("喜");
        datas.add("我");
        datas.add("爱");
        datas.add("你");
        tanTanView.addData(datas);
    }
}

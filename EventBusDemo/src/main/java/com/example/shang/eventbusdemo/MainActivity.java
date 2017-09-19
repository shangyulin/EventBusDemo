package com.example.shang.eventbusdemo;

import android.app.usage.UsageEvents;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EventBus.getDefault().register(this);
    }

    @Subscribe(ThreadMode.MAIN)
    public void messgae(Friend friend){
        System.out.println("我收到消息了");
        System.out.println(friend.getName());
    }

    public void jump(View view){
        startActivity(new Intent(MainActivity.this, SecondActivity.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unRegister(this);
    }
}

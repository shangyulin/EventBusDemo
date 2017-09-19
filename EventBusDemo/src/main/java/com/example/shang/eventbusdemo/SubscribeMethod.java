package com.example.shang.eventbusdemo;

import java.lang.reflect.Method;

/**
 * Created by Shang on 2017/7/11.
 */
public class SubscribeMethod {

    private Method Method;// 方法名
    private ThreadMode threadMode;// 操作线程
    private Class<?> eventType;// 参数类型

    public SubscribeMethod(Method method, ThreadMode threadMode, Class<?> eventType) {
        Method = method;
        this.threadMode = threadMode;
        this.eventType = eventType;
    }

    public Method getMethod() {
        return Method;
    }

    public void setMethod(Method method) {
        Method = method;
    }

    public Class<?> getEventType() {
        return eventType;
    }

    public void setEventType(Class<?> eventType) {
        this.eventType = eventType;
    }

    public ThreadMode getThreadMode() {
        return threadMode;
    }

    public void setThreadMode(ThreadMode threadMode) {
        this.threadMode = threadMode;
    }
}

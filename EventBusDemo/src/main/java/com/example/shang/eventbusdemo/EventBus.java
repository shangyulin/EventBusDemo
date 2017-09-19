package com.example.shang.eventbusdemo;

import android.os.Handler;
import android.os.Looper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Shang on 2017/7/11.
 */
public class EventBus {

    private static EventBus instance;

    private Handler handler;

    private ExecutorService executorService;

    private Map<Object, List<SubscribeMethod>> cacheMap;

    private EventBus() {
        cacheMap = new HashMap<Object, List<SubscribeMethod>>();
        handler = new Handler();
        executorService = Executors.newCachedThreadPool();
    }

    public static EventBus getDefault() {
        if (instance == null) {
            synchronized (EventBus.class) {
                if (instance == null) {
                    instance = new EventBus();
                }
            }
        }
        return instance;
    }

    public void register(Object activity) {
        // 判断该类是否注册过, 如果注册过就不处理了
        List<SubscribeMethod> list = cacheMap.get(activity);
        if (list == null) {
            // 找到当前类及其父类的所有注册方法
            List<SubscribeMethod> methods = findSubscribeMethod(activity);
            cacheMap.put(activity, methods);
        }
    }

    /**
     * @param obj 调用post方法时的参数类型
     */
    public void post(final Object obj) {
        // 拿到所有含有注解方法--类的集合
        Set<Object> set = cacheMap.keySet();
        Iterator<Object> iterator = set.iterator();
        // 遍历这些类
        while (iterator.hasNext()) {
            // 拿到当前类
            final Object activity = iterator.next();
            // 当前类中所有注解方法
            List<SubscribeMethod> list = cacheMap.get(activity);
            for (final SubscribeMethod method : list) {
                // 如果参数类型相等
                if (method.getEventType().isAssignableFrom(obj.getClass())) {
                    ThreadMode mode = method.getThreadMode();
                    switch (mode) {
                        case POSTING:
                            invoke(activity, method, obj);
                            break;
                        case MAIN:
                            // 如果当前实在主线程的话
                            if (Looper.myLooper() == Looper.getMainLooper()) {
                                invoke(activity, method, obj);
                            } else {
                                // 如果当前线程不是主线程，则切换回主线程
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        invoke(activity, method, obj);
                                    }
                                });
                            }
                            break;
                        case BACKGROUND:
                            if (Looper.getMainLooper() != Looper.myLooper()){
                                invoke(activity, method, obj);
                            }else{
                                // 由线程池切换至子线程
                                executorService.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        invoke(activity, method, obj);
                                    }
                                });
                            }
                            break;
                    }
                }
            }
        }
    }

    /**
     * @param activity 当前类
     * @param method   注解方法
     * @param obj      参数类型
     */
    private void invoke(Object activity, SubscribeMethod method, Object obj) {
        try {
            method.getMethod().invoke(activity, obj);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private List<SubscribeMethod> findSubscribeMethod(Object activity) {
        // 保存当前类的注解方法
        List<SubscribeMethod> list = new CopyOnWriteArrayList<SubscribeMethod>();
        // 当前需要注册的类
        Class<?> clazz = activity.getClass();
        // 当前类中的方法
        Method[] methods = clazz.getDeclaredMethods();
        while (clazz != null) {
            // 获取当前全类名
            String name = clazz.getName();
            // 系统类不做循环
            if (name.startsWith("java") || name.startsWith("javax") || name.startsWith("android")) {
                break;
            }
            // 遍历所有方法
            for (Method method : methods) {
                // 找到当前类中包括注解的方法
                Subscribe subscribe = method.getAnnotation(Subscribe.class);
                // 将没有注解的方法过滤掉
                if (subscribe == null) {
                    continue;
                }
                // 在具有注解的方法中获取只有一个参数的方法
                Class<?>[] parameterTypes = method.getParameterTypes();
                // 判断参数类型长度
                if (parameterTypes.length != 1) {
                    throw new RuntimeException("EventBus method must have one parameters");
                }
                /**
                 * 封装注解方法
                 */
                // 获取参数类型
                Class<?> parameterType = parameterTypes[0];
                // 线程
                ThreadMode value = subscribe.value();
                SubscribeMethod subscribeMethod = new SubscribeMethod(method, value, parameterType);
                list.add(subscribeMethod);
            }
            // 当前类的父类
            clazz = clazz.getSuperclass();
        }
        return list;
    }

    public void unRegister(Object obj){
        cacheMap.remove(obj);
    }
}

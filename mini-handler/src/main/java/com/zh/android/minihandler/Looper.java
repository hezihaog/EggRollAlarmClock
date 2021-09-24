package com.zh.android.minihandler;

public class Looper {
    private static final ThreadLocal<Looper> sThreadLocal = new ThreadLocal<>();
    /**
     * 消息队列
     */
    final MessageQueue mMessageQueue;

    private Looper() {
        //一个线程，只有一个Looper，一个Looper也只有一个消息队列
        mMessageQueue = new MessageQueue();
    }

    /**
     * 把当前线程和Looper进行绑定
     */
    public static void prepare() {
        Looper looper = sThreadLocal.get();
        if (looper != null) {
            throw new RuntimeException("一个线程只能绑定一个Looper，请确保prepare方法在一个线程中只调用一次");
        }
        sThreadLocal.set(new Looper());
    }

    /**
     * 获取当前线程的Looper
     */
    public static Looper myLooper() {
        return sThreadLocal.get();
    }

    /**
     * 开始循环从队列中取出消息
     */
    public static void loop() {
        //获取当前线程的轮询器
        Looper looper = myLooper();
        MessageQueue queue = looper.mMessageQueue;
        while (!queue.isQuit) {
            Message message = queue.next();
            try {
                message.target.dispatchMessage(message);
            } finally {
                //回收Message对象
                message.recycleUnchecked();
            }
        }
    }

    /**
     * 安全退出，会等所有事件都执行完，再关闭
     */
    public void quitSafely() {
        mMessageQueue.quit();
    }
}
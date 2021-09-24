package com.zh.android.minihandler;

import android.util.Log;

public class MiniHandler {
    /**
     * 消息队列
     */
    private final MessageQueue mMessageQueue;
    /**
     * 事件回调，可以选择构造时传入一个Callback，就可以不需要复写handleMessage()方法进行处理
     */
    private Callback mCallback;

    public MiniHandler() {
        this(Looper.myLooper());
    }

    public MiniHandler(Callback callback) {
        Looper looper = Looper.myLooper();
        //Looper没有绑定
        if (looper == null) {
            throw new RuntimeException(
                    "Can't create handler inside thread " + Thread.currentThread()
                            + " that has not called Looper.prepare()");
        }
        mMessageQueue = looper.mMessageQueue;
        mCallback = callback;
    }

    public MiniHandler(Looper looper) {
        //Looper没有绑定
        if (looper == null) {
            throw new RuntimeException(
                    "Can't create handler inside thread " + Thread.currentThread()
                            + " that has not called Looper.prepare()");
        }
        mMessageQueue = looper.mMessageQueue;
    }

    public interface Callback {
        /**
         * @param msg A {@link android.os.Message Message} object
         * @return True if no further handling is desired
         */
        boolean handleMessage(Message msg);
    }

    /**
     * 移除队列中指定Handler的未执行的回调和消息
     */
    public final void removeCallbacksAndMessages() {
        mMessageQueue.removeCallbacksAndMessages(this);
    }

    /**
     * 发送消息到消息队列中
     */
    public boolean sendMessage(Message message) {
        return sendMessageDelayed(message, 0);
    }

    /**
     * 在主线程执行一个Runnable
     */
    public final boolean post(Runnable task) {
        return sendMessageDelayed(getPostMessage(task), 0);
    }

    /**
     * 延迟一段时间后，在主线程执行一个Runnable
     *
     * @param delayMillis 延时时间，毫秒值
     */
    public final boolean postDelayed(Runnable task, long delayMillis) {
        return sendMessageDelayed(getPostMessage(task), delayMillis);
    }

    /**
     * 获取一个带Runnable任务的Message
     */
    private static Message getPostMessage(Runnable task) {
        Message m = Message.obtain();
        m.callback = task;
        return m;
    }

    /**
     * 发送延时消息
     *
     * @param message     消息
     * @param delayMillis 延时时间
     */
    public final boolean sendMessageDelayed(Message message, long delayMillis) {
        if (delayMillis < 0) {
            delayMillis = 0;
        }
        return sendMessageAtTime(message, System.currentTimeMillis() + delayMillis);
    }

    public boolean sendMessageAtTime(Message message, long uptimeMillis) {
        MessageQueue queue = this.mMessageQueue;
        if (queue == null) {
            RuntimeException e = new RuntimeException(
                    this + " sendMessageAtTime() called with no mQueue");
            Log.w("Looper", e.getMessage(), e);
            return false;
        }
        return enqueueMessage(queue, message, uptimeMillis);
    }

    private boolean enqueueMessage(MessageQueue queue, Message message, long uptimeMillis) {
        message.target = this;
        return queue.enqueueMessage(message, uptimeMillis);
    }

    /**
     * 分发消息给Handler进行处理
     */
    void dispatchMessage(Message message) {
        //如果是post()或postDelayed()发出的任务，则执行这个任务
        if (message.callback != null) {
            handleCallback(message);
        } else {
            //如果在Handler构造时传入了Callback，则回调这个Callback
            if (mCallback != null) {
                if (mCallback.handleMessage(message)) {
                    return;
                }
            }
            //都没有，则调用handleMessage()，Handler的子类可以复写该方法进行事件处理
            handleMessage(message);
        }
    }

    /**
     * 执行Message绑定的任务
     */
    private static void handleCallback(Message message) {
        message.callback.run();
    }

    /**
     * 子类重写该方法进行处理消息
     */
    public void handleMessage(Message message) {
    }
}
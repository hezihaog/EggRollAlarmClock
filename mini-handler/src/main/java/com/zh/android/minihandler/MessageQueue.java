package com.zh.android.minihandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;

public class MessageQueue {
    /**
     * 是否退出
     */
    volatile boolean isQuit;
    /**
     * 消息队列
     */
    private final BlockingQueue<Message> mMessageQueue = new DelayQueue<>();

    /**
     * 消息入队
     */
    public boolean enqueueMessage(Message message, long uptimeMillis) {
        try {
            message.workTimeMillis = uptimeMillis;
            mMessageQueue.put(message);
            return true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 拿出一条消息
     */
    public Message next() {
        try {
            //如果队列中没有消息，就会阻塞在这里
            return mMessageQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 移除队列中指定Handler的未执行的回调和消息
     */
    public final void removeCallbacksAndMessages(MiniHandler handler) {
        if (handler == null) {
            return;
        }
        List<Message> targetMessages = new ArrayList<>();
        for (Message message : mMessageQueue) {
            if (message.target == handler) {
                targetMessages.add(message);
            }
        }
        mMessageQueue.removeAll(targetMessages);
    }

    public void quit() {
        mMessageQueue.clear();
        isQuit = true;
    }
}
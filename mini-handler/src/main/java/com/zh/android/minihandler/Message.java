package com.zh.android.minihandler;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * 消息事件实体，同时
 */
public class Message implements Delayed {
    /**
     * 使用中的标志位
     */
    static final int FLAG_IN_USE = 1;

    int flags;
    /**
     * 为了形成消息链表
     */
    Message next;
    /**
     * 对象锁
     */
    public static final Object sPoolSync = new Object();
    /**
     * 消息链表的头节点
     */
    private static Message sPool;
    /**
     * 当前链表中数据的个数
     */
    private static int sPoolSize = 0;
    /**
     * 总共可使用的消息链表大小
     */
    private static final int MAX_POOL_SIZE = 50;
    /**
     * 消息的标识
     */
    public long what;
    /**
     * 消息的附件
     */
    public Object obj;
    /**
     * 消息的处理器
     */
    public MiniHandler target;
    /**
     * 要执行的任务，可为null
     */
    public Runnable callback;
    /**
     * 指定的执行时间，毫秒值
     */
    public long workTimeMillis;

    /**
     * 回收Message
     */
    void recycleUnchecked() {
        //把标记设置为使用中
        flags = FLAG_IN_USE;
        //清理所有字段
        what = 0;
        obj = null;
        target = null;
        callback = null;
        workTimeMillis = 0;
        synchronized (sPoolSync) {
            if (sPoolSize < MAX_POOL_SIZE) {
                next = sPool;
                sPool = this;
                sPoolSize++;
            }
        }
    }

    /**
     * 创建一个Message对象
     */
    public static Message obtain() {
        synchronized (sPoolSync) {
            //判断头节点是否null
            if (sPool != null) {
                //取出头节点
                Message m = sPool;
                //将头节点的下一个作为最新的头节点
                sPool = m.next;
                //设置需要返回的消息的next为空
                m.next = null;
                //清除使用中的标志位
                m.flags = 0;
                sPoolSize--;
                return m;
            }
        }
        //如果消息链表为空 创建新的message
        return new Message();
    }

    /**
     * 创建一个Message对象，并绑定处理它的Handler
     */
    public static Message obtain(MiniHandler handler) {
        return obtain(handler, 0);
    }

    /**
     * 创建一个Message对象，并绑定处理它的Handler、消息标识what
     */
    public static Message obtain(MiniHandler handler, long what) {
        return obtain(handler, what, null);
    }

    /**
     * 创建一个Message对象，绑定消息标识what
     */
    public static Message obtain(long what) {
        return obtain(what, null);
    }

    /**
     * 创建一个Message对象，绑定消息标识what、消息附件obj
     */
    public static Message obtain(long what, Object obj) {
        return obtain(null, what, obj);
    }

    /**
     * 创建一个Message对象，并绑定处理它的Handler、消息标识what、消息附件obj
     */
    public static Message obtain(MiniHandler handler, long what, Object obj) {
        Message message = obtain();
        message.target = handler;
        message.what = what;
        message.obj = obj;
        return message;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(workTimeMillis
                - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        return (int) (this.getDelay(TimeUnit.MILLISECONDS) - o.getDelay(TimeUnit.MILLISECONDS));
    }
}
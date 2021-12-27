package com.zy.environment.utils;

import org.greenrobot.eventbus.EventBus;

public class EventBusUtils {

    private static final  EventBus eventBus = EventBus.getDefault();
    private EventBusUtils() {
    }

    /**
     * 注册 EventBus
     * @param subscriber 订阅者
     */
    public static void register(final Object subscriber) {

        if (!eventBus.isRegistered(subscriber)) {
            eventBus.register(subscriber);
        }
    }

    /**
     * 解绑 EventBus
     * @param subscriber 订阅者
     */
    public static void unregister(final Object subscriber) {
        if (eventBus.isRegistered(subscriber)) {
            eventBus.unregister(subscriber);
        }
    }

    // =========
    // = Event =
    // =========

    /**
     * 发送事件消息
     * @param event Event
     */
    public static void post(final Object event) {
        eventBus.post(event);
    }

    /**
     * 取消事件传送
     * @param event Event
     */
    public static void cancelEventDelivery(final Object event) {
        eventBus.cancelEventDelivery(event);
    }

    // =

    /**
     * 发送粘性事件消息
     * @param event Event
     */
    public static void postSticky(final Object event) {
       eventBus.postSticky(event);
    }

    /**
     * 移除指定的粘性订阅事件
     * @param eventType Event Type
     * @param <T>       泛型
     */
    public static <T> void removeStickyEvent(final Class<T> eventType) {
        T stickyEvent = EventBus.getDefault().getStickyEvent(eventType);
        if (stickyEvent != null) {
            eventBus.removeStickyEvent(stickyEvent);
        }
    }

    /**
     * 移除所有的粘性订阅事件
     */
    public static void removeAllStickyEvents() {
        eventBus.removeAllStickyEvents();
    }

}

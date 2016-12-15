package com.tenth.space.imservice.manager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;

import com.tenth.space.imservice.event.LoginEvent;
import com.tenth.space.imservice.event.ReconnectEvent;
import com.tenth.space.imservice.event.SocketEvent;
import com.tenth.space.utils.Logger;
import com.tenth.space.utils.NetworkUtil;

import de.greenrobot.event.EventBus;
/**
 * @modify yingmu
 * @Date 2014-12-28
 *
 * [修改重点]:
 *   重试的触发点是：1.网络链接的异常 2.请求消息服务器 3链接消息服务器
 */
public class IMReconnectManager extends IMManager {
    private Logger logger = Logger.getLogger(IMReconnectManager.class);

	private static IMReconnectManager inst = new IMReconnectManager();
    public static IMReconnectManager instance() {
       return inst;
    }

    /**重连所处的状态*/
    private volatile ReconnectEvent status = ReconnectEvent.NONE;

    private final int INIT_RECONNECT_INTERVAL_SECONDS = 3;
    private int reconnectInterval = INIT_RECONNECT_INTERVAL_SECONDS;
    private final int MAX_RECONNECT_INTERVAL_SECONDS = 60;

    private final int HANDLER_CHECK_NETWORK = 1;


    private volatile boolean isAlarmTrigger = false;

    /**
     * imService 服务建立的时候
     * 初始化所有的manager 调用onStartIMManager会调用下面的方法
     * eventBus 注册可以放在这里
     */
    @Override
    public void doOnStart() {
    }

    public void onNormalLoginOk(){
        onLocalLoginOk();
        status = ReconnectEvent.SUCCESS;
    }

    public void onLocalLoginOk(){
       logger.i("reconnect#LoginEvent onLocalLoginOk");

       if(!EventBus.getDefault().isRegistered(inst)){
           EventBus.getDefault().register(inst);
       }

       IntentFilter intentFilter = new IntentFilter();
       intentFilter.addAction(ACTION_RECONNECT);
       intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
       logger.i("reconnect#register actions");
       ctx.registerReceiver(imReceiver, intentFilter);
    }

    /** 网络链接成功*/
    public void onLocalNetOk(){
      logger.i("reconnect#onLoginSuccess 网络链接上来,无需其他操作");
      status = ReconnectEvent.SUCCESS;
    }


    @Override
    public void reset() {
        logger.i("reconnect#reset begin");
        try {
            EventBus.getDefault().unregister(inst);
            ctx.unregisterReceiver(imReceiver);
            status = ReconnectEvent.NONE;
            isAlarmTrigger = false;
            logger.i("reconnect#reset stop");
        }catch (Exception e){
            logger.e("reconnect#reset error:%s",e.getCause());
        }
    }

    /**
     * 网络socket状态监听
     * 并未登陆，请求链接消息服务器失败
     * @param socketEvent
     */
    public void onEventMainThread(SocketEvent socketEvent){
        logger.i("reconnect#SocketEvent event:%s",socketEvent.name());

        //MSG_SERVER_DISCONNECTED 没有必要重连吧。。

        switch (socketEvent){
            case MSG_SERVER_DISCONNECTED:
            case REQ_MSG_SERVER_ADDRS_FAILED:
            case CONNECT_MSG_SERVER_FAILED:{
                tryReconnect();
            }break;
        }
    }

    /**
     *
     * @param event
     */
    public void onEventMainThread(LoginEvent event){
        logger.i("reconnect#LoginEvent event: %s",event.name());
        switch (event){
            case LOGIN_INNER_FAILED:
                    tryReconnect();
                break;

            case LOCAL_LOGIN_MSG_SERVICE:
                resetReconnectTime();
                break;
        }
    }

    /**EventBus 没有找到延迟发送的*/
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case HANDLER_CHECK_NETWORK:{
                      if(!NetworkUtil.isNetWorkAvalible(ctx)){
                          logger.w("reconnect#handleMessage#网络依旧不可用");
                          EventBus.getDefault().postSticky(ReconnectEvent.DISABLE);
                      }
                }break;
            }
        }
    };

    private boolean isReconnecting(){
        SocketEvent socketEvent =   IMSocketManager.instance().getSocketStatus();
        LoginEvent loginEvent = IMLoginManager.instance().getLoginStatus();

        if(socketEvent.equals(SocketEvent.CONNECTING_MSG_SERVER)
                || socketEvent.equals(SocketEvent.REQING_MSG_SERVER_ADDRS)
                || loginEvent.equals(LoginEvent.LOGINING)){
            return true;
        }
        return false;
    }

    /**
     * 可能是网络环境切换
     * 可能是数据包异常
     * 启动快速重连机制
     */
    private  void tryReconnect(){
        /**检测网络状态*/
        ConnectivityManager nw = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = nw.getActiveNetworkInfo();

        if(netinfo == null){
            // 延迟检测
            logger.w("reconnect#netinfo 为空延迟检测");
            status = ReconnectEvent.DISABLE;
            handler.sendEmptyMessageDelayed(HANDLER_CHECK_NETWORK, 2000);
            return ;
        }

        synchronized(IMReconnectManager.this) {
            // 网络状态可用 当前状态处于重连的过程中 可能会死循环嘛
            if (netinfo.isAvailable()) {
                /**网络显示可用*/
                /**判断是否有必要重练*/
                if (status == ReconnectEvent.NONE
                        || !IMLoginManager.instance().isEverLogined()
                        || IMLoginManager.instance().isKickout()
                        || IMSocketManager.instance().isSocketConnect()
                        ) {
                    logger.i("reconnect#无需启动重连程序");
                    return;
                }
                if (isReconnecting()) {
                    /**升级时间，部署下一次的重练*/
                    logger.i("reconnect#正在重连中..");
                    incrementReconnectInterval();
                    scheduleReconnect(reconnectInterval);
                    logger.i("reconnect#tryReconnect#下次重练时间间隔:%d", reconnectInterval);
                    return;
                }

                /**确保之前的链接已经关闭*/
                IMSocketManager.instance().disconnectMsgServer();

                if (isAlarmTrigger) {
                    isAlarmTrigger = false;
                    logger.i("reconnect#定时器触发重连。。。");
                    handleReconnectServer();
                } else {
                    logger.i("reconnect#正常重连，非定时器");
                    IMSocketManager.instance().reconnectMsg();
                }
            } else {
                //通知上层UI修改
                logger.i("reconnect#网络不可用!! 通知上层");
                status = ReconnectEvent.DISABLE;
                EventBus.getDefault().postSticky(ReconnectEvent.DISABLE);
            }
        }
    }

    /**
     * 部署下次请求的时间
     * 为什么用这种方式，而不是handler?
     * @param seconds
     */
	private void scheduleReconnect(int seconds) {
		logger.i("reconnect#scheduleReconnect after %d seconds", seconds);
		Intent intent = new Intent(ACTION_RECONNECT);
		PendingIntent pi = PendingIntent.getBroadcast(ctx, 0, intent,PendingIntent.FLAG_CANCEL_CURRENT);
		if (pi == null) {
			logger.e("reconnect#pi is null");
			return;
		}
		AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + seconds
				* 1000, pi);
	}

    /**
     * 在RECONNECT 的时候，时间加成处理
     */
    private void incrementReconnectInterval() {
		if (reconnectInterval >= MAX_RECONNECT_INTERVAL_SECONDS) {
			reconnectInterval = MAX_RECONNECT_INTERVAL_SECONDS;
		} else {
			reconnectInterval = reconnectInterval * 2;
		}
	}

    /**
     * 重新设定重连时间
     */
    private void resetReconnectTime() {
        logger.i("reconnect#resetReconnectTime");
        reconnectInterval = INIT_RECONNECT_INTERVAL_SECONDS;
    }




    /**--------------------boradcast-广播相关-----------------------------*/
    private final String  ACTION_RECONNECT = "com.mogujie.tt.imlib.action.reconnect";
    private BroadcastReceiver imReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                logger.i("reconnect#im#receive action:%s", action);
                onAction(action, intent);
        }
    };

    /**
     * 【重要】这个地方作为重连判断的唯一标准
     *  飞行模式: 触发
     *  切换网络状态： 触发
     *  没有网络 ： 触发
     * */
    public void onAction(String action, Intent intent) {
        logger.i("reconnect#onAction action:%s", action);
        if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            logger.i("reconnect#onAction#网络状态发生变化!!");
            tryReconnect();
        } else if (action.equals(ACTION_RECONNECT)) {
            isAlarmTrigger = true;
            tryReconnect();
        }
    }

    /**
     * 设定的调度开始执行
     * todo 其他reconnect需要获取锁嘛
     */
    private void handleReconnectServer() {
        logger.i("reconnect#handleReconnectServer#定时任务触发");
            PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "teamtalk_reconnecting_wakelock");
            wl.acquire();
            try {
                if(!IMLoginManager.instance().isEverLogined() || IMLoginManager.instance().isKickout()){
                    logger.i("reconnect#isEverLogined return!!!");
                    return;
                }
                logger.i("reconnect#login#reConnect.");
                if(reconnectInterval > 24){
                    // 重新请求msg地址
                    IMLoginManager.instance().relogin();
                }else{
                    IMSocketManager.instance().reconnectMsg();
                }

                logger.i("reconnect#trigger event reconnecting");
            } finally {
                wl.release();
            }
    }
}



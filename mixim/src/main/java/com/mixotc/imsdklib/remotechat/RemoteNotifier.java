package com.mixotc.imsdklib.remotechat;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;

import com.mixotc.imsdklib.R;
import com.mixotc.imsdklib.message.GOIMMessage;
import com.mixotc.imsdklib.utils.AppUtils;
import com.mixotc.imsdklib.utils.Logger;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RemoteNotifier {

    private static final String TAG = RemoteNotifier.class.getSimpleName();

    private static Ringtone ringtone = null;
    private static int notifyID = 341;
    private NotificationManager notificationManager = null;
    private int notificationMsgNum = 0;
    private int notificationNotifyNum = 0;
    private Context mContext;
    private String packageName;
    private long lastNotifiyTime;
    private static RemoteNotifier instance;
    private AudioManager audioManager;
    private Vibrator vibrator;
    private ExecutorService newMsgQueue = Executors.newSingleThreadExecutor();

    private RemoteNotifier(Context paramContext) {
        mContext = paramContext;
        if (notificationManager == null) {
            notificationManager = ((NotificationManager) paramContext.getSystemService(Context.NOTIFICATION_SERVICE));
        }
        packageName = mContext.getApplicationInfo().packageName;
        audioManager = ((AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE));
        vibrator = ((Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE));
    }

    public static RemoteNotifier getInstance(Context paramContext) {
        if (instance == null) {
            synchronized (RemoteNotifier.class) {
                if (instance == null) {
                    instance = new RemoteNotifier(paramContext);
                }
            }
        }
        return instance;
    }

    public void stop() {
        if (ringtone != null) {
            ringtone.stop();
            ringtone = null;
        }
    }

    void resetNotificationCount() {
        notificationMsgNum = 0;
        notificationNotifyNum = 0;
    }

    void notifyChatMsg(final GOIMMessage message) {
        newMsgQueue.submit(new Runnable() {
            public void run() {
                String str = null;
                List notificationDisabled = RemoteChatManager.getInstance().getChatOptions().getGroupsOfNotificationDisabled();
                if ((RemoteChatManager.getInstance().getChatOptions().isShowNotificationInBackgroud())
                        && (!AppUtils.isAppRunningForeground(mContext))) {
                    Logger.d("notify", "chat app is not running, sending notification");
                    if ((notificationDisabled == null) || (!notificationDisabled.contains(str))) {
                        RemoteNotifier.this.sendNotification(message);
                        notifyOnNewMsg();
                    }
                    return;
                }
                sendBroadcast(message);
                if (((notificationDisabled == null) || (!notificationDisabled.contains(str)))) {
                    notifyOnNewMsg();
                }
            }
        });
    }

    void notifySystemMsg() {
        newMsgQueue.submit(new Runnable() {
            public void run() {
                Intent intent = new Intent(RemoteChatManager.getInstance().getNewSystemMessageBroadcastAction());
                Logger.d("notify", "send new system message broadcast");
                mContext.sendOrderedBroadcast(intent, null);
            }
        });
    }

    // send message to local
    void sendBroadcast(GOIMMessage message) {
        Intent intent = new Intent(RemoteChatManager.getInstance().getNewMessageBroadcastAction());
        intent.putExtra("msgid", message.getMsgId());
        Logger.d("notify", "send new message broadcast for msg:" + message.getMsgId());
        mContext.sendOrderedBroadcast(intent, null);
    }

    private void sendNotification(GOIMMessage message) {
        String str1 = message.getContact().getNick();
        try {
            String ticker = str1 + " ";
            switch (message.getType()) {
                case FILE:
                    ticker = ticker + mContext.getString(R.string.notification_new_file);
                    notificationMsgNum++;
                    break;
                case TXT:
                    Object isNotify = message.getAttribute(GOIMMessage.IS_NOTIFY_KEY);
                    if (isNotify != null && (Boolean) isNotify) {
                        ticker = mContext.getString(R.string.notification_new_notify);
                        notificationNotifyNum++;
                    } else {
                        ticker = ticker + mContext.getString(R.string.notification_new_text);
                        notificationMsgNum++;
                    }
                    break;
                case LOCATION:
                    ticker = ticker + mContext.getString(R.string.notification_new_location);
                    notificationMsgNum++;
                    break;
                case IMAGE:
                    ticker = ticker + mContext.getString(R.string.notification_new_image);
                    notificationMsgNum++;
                    break;
                case VIDEO:
                    ticker = ticker + mContext.getString(R.string.notification_new_video);
                    notificationMsgNum++;
                    break;
                case VOICE:
                    ticker = ticker + mContext.getString(R.string.notification_new_voice);
                    notificationMsgNum++;
                    break;
                case PACKET:
                    ticker = ticker + mContext.getString(R.string.notification_new_packet);
                    notificationNotifyNum++;
                    break;
                case TRANSFER:
                    ticker = ticker + mContext.getString(R.string.notification_new_transfer);
                    notificationNotifyNum++;
                    break;
                case SECURETRANS:
                    ticker = ticker + mContext.getString(R.string.notification_new_securetrans);
                    notificationNotifyNum++;
                    break;
            }
            CharSequence title = mContext.getPackageManager().getApplicationLabel(mContext.getApplicationInfo());
            NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext).setSmallIcon(R.drawable.ic_notification).setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_notification)).setWhen(System.currentTimeMillis()).setAutoCancel(true);
            Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(packageName);
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, notifyID, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            String content = "";
            if (notificationMsgNum > 0) {
                content += mContext.getString(R.string.notification_new_message_format_string, notificationMsgNum);
            }
            if (notificationMsgNum > 0 && notificationNotifyNum > 0) {
                content += mContext.getString(R.string.notification_and_string);
            }
            if (notificationNotifyNum > 0) {
                content += mContext.getString(R.string.notification_new_notify_format_string, notificationNotifyNum);
            }
            builder.setContentTitle(title);
            builder.setTicker(ticker);
            builder.setContentText(content);
            builder.setContentIntent(pendingIntent);
            Notification notification = builder.build();
            try {
                notificationManager.cancel(notifyID);
            } catch (Exception e) {
                e.printStackTrace();
            }
            notificationManager.notify(notifyID, notification);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 产生震动和声音消息
    private void notifyOnNewMsg() {
        if ((!RemoteChatManager.getInstance().getChatOptions().getNotificationEnable())
                || (!RemoteChatManager.getInstance().getChatOptions().getNotifyBySoundAndVibrate())) {
            return;
        }
        try {
            if (System.currentTimeMillis() - lastNotifiyTime < 1000L) {
                return;
            }
            lastNotifiyTime = System.currentTimeMillis();
            if (audioManager.getRingerMode() == 0) {
                Logger.e(TAG, "in silent mode now");
                return;
            }
            if (RemoteChatManager.getInstance().getChatOptions().getNoticedByVibrate()) {
                long[] vib = new long[]{0, 180L, 80L, 120L};
                vibrator.vibrate(vib, -1);
            }
            if (RemoteChatManager.getInstance().getChatOptions().getNoticedBySound()) {
                if (ringtone == null) {
                    Uri uri = null;
                    if (RemoteChatManager.getInstance().getChatOptions().getNotifyRingUri() == null) {
                        uri = RingtoneManager.getDefaultUri(2);
                    } else {
                        uri = RemoteChatManager.getInstance().getChatOptions().getNotifyRingUri();
                    }
                    ringtone = RingtoneManager.getRingtone(mContext, uri);
                    if (ringtone == null) {
                        Logger.d("notify", "cant find ringtone at:" + uri.getPath());
                        return;
                    }
                }
                if (!ringtone.isPlaying()) {
                    ringtone.play();
                    String manufacturer = Build.MANUFACTURER;
                    if ((manufacturer != null) && (manufacturer.toLowerCase().contains("samsung"))) {
                        Thread thread = new Thread() {
                            public void run() {
                                try {
                                    Thread.sleep(3000L);
                                    if (RemoteNotifier.ringtone.isPlaying()) {
                                        RemoteNotifier.ringtone.stop();
                                    }
                                } catch (Exception ignore) {
                                    ignore.printStackTrace();
                                }
                            }
                        };
                        thread.run();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void cancelNotificaton() {
        if (notificationManager != null) {
            notificationManager.cancel(notifyID);
        }
    }
}

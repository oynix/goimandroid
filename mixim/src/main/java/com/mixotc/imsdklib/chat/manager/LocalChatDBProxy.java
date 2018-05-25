package com.mixotc.imsdklib.chat.manager;

import android.os.RemoteException;

import com.mixotc.imsdklib.AdminManager;
import com.mixotc.imsdklib.RemoteServiceBinder;
import com.mixotc.imsdklib.chat.GOIMConversation;
import com.mixotc.imsdklib.message.GOIMMessage;
import com.mixotc.imsdklib.message.GOIMSystemMessage;
import com.mixotc.imsdklib.utils.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class LocalChatDBProxy {
    private static String TAG = LocalChatDBProxy.class.getSimpleName();

    private static final class LazyHolder {
        private static final LocalChatDBProxy INSTANCE = new LocalChatDBProxy();
    }

    private LocalChatDBProxy() {
    }

    public static synchronized LocalChatDBProxy getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void saveMsgToDB(GOIMMessage message) {
        RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
        if (binder != null) {
            try {
                binder.saveMsgToDB(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean updateMessageBody(GOIMMessage message) {
        RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
        if (binder != null) {
            try {
                return binder.updateMessageBody(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public void deleteMsg(String msgId) {
        RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
        if (binder != null) {
            try {
                binder.deleteMsg(msgId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public GOIMMessage loadMsg(String msgId) {
        GOIMMessage message = null;
        RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
        if (binder != null) {
            try {
                message = binder.loadMsg(msgId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return message;
    }

    public void deleteConversionMsgs(long groupId) {
        RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
        if (binder !=  null) {
            try {
                binder.deleteConversionMsgs(groupId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateMsgListen(String msgId, boolean listened) {
        RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
        if (binder != null) {
            try {
                binder.updateMsgListen(msgId, listened);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateMsgStatus(String msgId, int status) {
        RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
        if (binder != null) {
            try {
                binder.updateMsg(msgId, status);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    // 为避免aidl传输数据过大，这里只加载conversation不加载message
    public Hashtable<Long, GOIMConversation> loadAllConversationsWithoutMessage(int count) {
        Logger.d(TAG, "load all conversations :" + count);
        Hashtable<Long, GOIMConversation> hashtable = new Hashtable<>();
        RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
        if (binder != null) {
            try {
                String sConversations = binder.loadAllConversationsWithoutMessage(count);
                JSONArray jConversations = new JSONArray(sConversations);
                for (int i = 0; i < jConversations.length(); i++) {
                    JSONObject jConversation = jConversations.optJSONObject(i);
                    if (jConversation != null) {
                        long groupId = jConversation.optLong("groupId");
                        String name = jConversation.optString("name");
                        long msgCount = jConversation.optLong("msgCount");
                        boolean isOnTop = jConversation.optBoolean("isOnTop");
                        boolean isSingle = jConversation.optBoolean("isSingle");
                        long lastMsgTime = jConversation.optLong("lastMsgTime");
                        String lastMsgText = jConversation.optString("lastMsgText");
//                        String messages = jConversation.optString("messages");
//                        byte[] bytes = Base64.decode(messages.getBytes(), Base64.DEFAULT);
//                        Parcel parcel = Parcel.obtain();
//                        parcel.unmarshall(bytes, 0, bytes.length);
//                        parcel.setDataPosition(0);
//                        List<GOIMMessage> messageList = new ArrayList<>();
//                        parcel.readTypedList(messageList, GOIMMessage.CREATOR);
                        GOIMConversation conversation = new GOIMConversation(groupId, name, null, msgCount, isOnTop, isSingle, msgCount <= 0);
                        conversation.setLastMsgTime(lastMsgTime);
                        conversation.setLastMsgText(lastMsgText);
//                        parcel.recycle();
                        // 与被对方删除的conversation,无法获取对方的id，可通过遍历消息，如果有非登录人发送的消息则认为是对方
                        // 如果消息列表为空，或者全部为登录的用户自己发送的消息，则直接将该条conversation删除
                        // 行不通，因为被删除的用户本地没有存储其信息
                        Logger.w(TAG, "load " + conversation.isSingle() + "<-single" +conversation.getGroupId() + ", message count:" + conversation.getAllMsgCount() + "transfer from RemoteDB:" + conversation.getName() + (conversation.getGroup() == null));
//                        if (conversation.getGroup() == null && conversation.getGroupId() > 0 && conversation.isSingle()) {
//
//                        } else {
//                        }
                        conversation.loadMoreMsgFromDB(null, 20);
                        hashtable.put(conversation.getGroupId(), conversation);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return hashtable;
    }

    public void deleteConversation(long groupId) {
        RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
        if (binder != null) {
            try {
                binder.deleteConversation(groupId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveConversation(GOIMConversation conversation) {
        RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
        if (binder != null) {
            try {
                binder.saveConversation(conversation);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateConversationName(long groupId, String newName) {
        RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
        if (binder != null) {
            try {
                binder.updateConversationName(groupId, newName);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void setConversationOnTop(long groupId, boolean isOnTop) {
        RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
        if (binder != null) {
            try {
                binder.setConversationOnTop(groupId, isOnTop);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateMessagesGroupId(long oldGroupId, long newGroupId) {
        RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
        if (binder != null) {
            try {
                binder.updateMessagesGroupId(oldGroupId, newGroupId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void clearUnread(long groupId) {
        RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
        if (binder != null) {
            try {
                binder.clearUnread(groupId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveUnreadCount(long groupId, int count) {
        RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
        if (binder != null) {
            try {
                binder.saveUnreadCount(groupId, count);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public int getUnreadCount(long groupId) {
        RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
        if (binder != null) {
            try {
                return binder.getUnreadCount(groupId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public long getMsgCount(long groupId) {
        RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
        if (binder != null) {
            try {
                return binder.getMsgCount(groupId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return 0L;
    }

    public List<GOIMMessage> loadMessageById(long groupId, String startMsgId, int count) {
        Logger.d(TAG, "load message by last id :" + groupId + ", start: " + startMsgId + ", count:" + count);
        List<GOIMMessage> messages = new ArrayList<>();
        RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
        if (binder != null) {
            try {
                return binder.findMsgs(groupId, startMsgId, count);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return messages;
    }

    public GOIMMessage findTransferMsg(long groupId, long transId) {
        GOIMMessage message = null;
        RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
        if (binder != null) {
            try {
                message = binder.findTransferMsg(groupId, transId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return message;
    }

    public synchronized void addSystemMsg(GOIMSystemMessage systemMessage) {
        RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
        if (binder != null) {
            try {
                binder.addSystemMsg(systemMessage);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public List<GOIMSystemMessage> getSystemMsgs() {
        ArrayList<GOIMSystemMessage> msgs = new ArrayList<>();
        RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
        if (binder != null) {
            try {
                msgs.addAll(binder.getSystemMsgs());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return msgs;
    }

    public GOIMSystemMessage getLastSystemMsg() {
        GOIMSystemMessage msg = null;
        RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
        if (binder != null) {
            try {
                msg = binder.getLastSystemMsg();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return msg;
    }

    public int getUnreadSystemMsgs() {
        RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
        if (binder != null) {
            try {
                return binder.getUnreadSystemMsgs();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public void clearUnreadSystemMsgs() {
        RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
        if (binder != null) {
            try {
                binder.clearUnreadSystemMsgs();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void clearAllSystemMsgs() {
        RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
        if (binder != null) {
            try {
                binder.clearAllSystemMsgs();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}

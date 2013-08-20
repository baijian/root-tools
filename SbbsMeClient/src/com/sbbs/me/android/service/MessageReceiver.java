package com.sbbs.me.android.service;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.rarnu.utils.DeviceUtilsLite;
import com.rarnu.utils.NotificationUtils;
import com.sbbs.me.android.R;
import com.sbbs.me.android.api.SbbsMeAPI;
import com.sbbs.me.android.api.SbbsMePrivateMessage;
import com.sbbs.me.android.api.SbbsMeUpdate;
import com.sbbs.me.android.consts.Actions;
import com.sbbs.me.android.database.PrivateMessageUtils;
import com.sbbs.me.android.utils.Config;

public class MessageReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(final Context context, final Intent intent) {
		String action = intent.getAction();
		if (action.equals(Actions.ACTION_CHECK_MESSAGE)) {
			doCheckMessage(context);
		} else if (action.equals(Actions.ACTION_CHECK_UPDATE)) {
			doCheckUpdate(context);
		}
	}

	private void doCheckMessage(final Context context) {
		final Handler hMessage = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == 1) {
					NotificationUtils.cancelNotication(context,
							Actions.ACTION_NOTIFY_MESSAGE);
					NotificationUtils.showNotification(context,
							Actions.ACTION_NOTIFY_MESSAGE, R.drawable.logo48,
							R.string.notify_message_title,
							R.string.notify_message_desc,
							Actions.ACTION_NOTIFY_MESSAGE_CLICK, null, true);
				}
				super.handleMessage(msg);
			}
		};
		new Thread(new Runnable() {

			@Override
			public void run() {
				if (!SbbsMeAPI.isLogin()) {
					try {
						String uid = Config.getUserId(context);
						if (!uid.equals("")) {
							String accType = getAccountType(context);
							SbbsMeAPI.login(uid, Config.getUserName(context),
									accType, Config.getAvatarUrl(context));
							Log.e("MessageReceiver", "loged-in");
						}

					} catch (Exception e) {
						Log.e("MessageReceiver",
								"login error: " + e.getMessage());
					}
				}
				if (SbbsMeAPI.isLogin()) {
					List<SbbsMePrivateMessage> list = SbbsMeAPI.queryMessage(
							PrivateMessageUtils.getLastMessageId(context), 1, 1);
					if (list != null && list.size() != 0) {
						Message msg = new Message();
						msg.what = 1;
						msg.obj = list;
						hMessage.sendMessage(msg);
					}
				}
			}
		}).start();
	}

	private void doCheckUpdate(final Context context) {
		final Handler hUpdate = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == 1) {
					SbbsMeUpdate update = (SbbsMeUpdate) msg.obj;
					NotificationUtils.cancelNotication(context,
							Actions.ACTION_NOTIFY_UPDATE);
					NotificationUtils.showNotification(context,
							Actions.ACTION_NOTIFY_UPDATE, R.drawable.logo48,
							R.string.notify_update_title,
							R.string.notify_update_desc,
							Actions.ACTION_NOTIFY_UPDATE_CLICK, update, true);
				}
				super.handleMessage(msg);
			};
		};

		new Thread(new Runnable() {

			@Override
			public void run() {
				SbbsMeUpdate update = SbbsMeAPI.checkUpdate(DeviceUtilsLite
						.getAppVersionCode(context));
				if (update != null && update.needUpdate) {
					Message msg = new Message();
					msg.what = 1;
					msg.obj = update;
					hUpdate.sendMessage(msg);
				}
			}
		}).start();
	}

	private String getAccountType(final Context context) {
		String accType = "";
		int acc = Config.getAccountType(context);
		switch (acc) {
		case 0:
			accType = "google";
			break;
		case 1:
			accType = "github";
			break;
		case 2:
			accType = "weibo";
			break;
		}
		return accType;
	}
}

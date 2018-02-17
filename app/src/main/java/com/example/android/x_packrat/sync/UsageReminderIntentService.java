package com.example.android.x_packrat.sync;


import android.app.IntentService;
import android.content.Intent;

/**
 * Contains a method that calls the appropriate methods to handle user interactions with
 * notifications(bringing app to foreground on press, dismissing notification when "dismiss" is
 * clicked.
 */
public class UsageReminderIntentService extends IntentService {
    public UsageReminderIntentService() {
        super("UsageReminderIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        ReminderTasks.executeTask(this, action);
    }
}

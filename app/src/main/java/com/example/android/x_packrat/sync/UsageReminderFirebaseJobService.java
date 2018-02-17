package com.example.android.x_packrat.sync;


import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.firebase.jobdispatcher.RetryStrategy;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Contains methods to handle the very start of the process for executing a job in a background
 * thread. Contains methods to handle a jobs completion or interruption.
 */
public class UsageReminderFirebaseJobService extends JobService {
    private AsyncTask mBackgroundTask;

    /**
     * Entry point to execute a job called by the Job Dispatcher.
     *
     * @param jobParameters The parameters supplied for the job
     * @return Whether or not there is more work remaining
     */
    @Override
    public boolean onStartJob(final JobParameters jobParameters) {

        Log.d("JOB", "job started!");
        mBackgroundTask = new AsyncTask() {

            /*
             * Contains the work to do on a background thread
             */
            @Override
            protected Object doInBackground(Object[] params) {
                Context context = UsageReminderFirebaseJobService.this;
                ReminderTasks.executeTask(context, ReminderTasks.ACTION_USAGE_REMINDER);
                return null;
            }

            /*
             * Called when the async task is finished and thus the job is finished
             */
            @Override
            protected void onPostExecute(Object o) {
                Log.d("JOB", "job finished!");

                // Informs JobManager that the job is finished
                jobFinished(jobParameters, false);
            }
        };

        mBackgroundTask.execute();
        return true;
    }

    /**
     * Called when the scheduling engine has decided to interrupt the execution of a running job,
     * most likely because the runtime constraints associated with the job are no longer satisfied.
     *
     * @param jobParameters The parameters supplied for the job
     * @return Whether or not the job should be retried
     */
    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        if (mBackgroundTask != null) mBackgroundTask.cancel(true);
        return true;
    }
}

package dar.games.music.capstonekote.utils;

import android.os.AsyncTask;

import java.lang.ref.WeakReference;

/*
A fake progress bar required by the project specifications.
 */
public class ProgressBarAsyncTask extends AsyncTask<Void, Void, Void> {


    private final WeakReference<OnPbFinishedListener> mWeakRefCallback;

    public ProgressBarAsyncTask(OnPbFinishedListener callback) {
        this.mWeakRefCallback = new WeakReference<>(callback);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        final OnPbFinishedListener callback = mWeakRefCallback.get();
        if (callback != null)
            callback.onPbFinished();
    }
}


package kumar.karan.scancard.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MainFragment extends Fragment {
    protected ArrayList<String> listFileName2;
    protected   ArrayList<String> listFileSize2;
    protected long avgFileSize;

    HashMap<String,Long> hashMap = new HashMap<String,Long>();
    HashMap<String,Long> sortedHashMap = new HashMap<String,Long>();
    List<File> dir = new ArrayList<File>();
    List<File> files = new ArrayList<File>();
    protected   ArrayList<String> listFileName = new ArrayList<>();
    protected   ArrayList<String> listFileSize = new ArrayList<>();

    public static final String ACTION_MyIntentService = "apps.khara.sdscan.RESPONSE";
    public static final String ACTION_MyUpdate = "apps.khara.sdscan.UPDATE";
    public static final String EXTRA_KEY_UPDATE = "EXTRA_UPDATE";

    protected static long fileCount=0;
    protected static long totalFileSize=0;
    protected DummyTask dummyTask;
     /*....................................................*/


    private static final String TAG = MainFragment.class.getSimpleName();
    private static final boolean DEBUG = true; // Set this to false to disable logs.
    public static interface TaskCallbacks {
        void onProgressUpdate();
    }
    public TaskCallbacks mCallbacks;
    private boolean mRunning;

    @Override
    public void onAttach(Context context) {
        if (DEBUG) Log.i(TAG, "onAttach(Activity)");
        super.onAttach(context);
        if (!(context instanceof TaskCallbacks)) {
            throw new IllegalStateException("Activity must implement the TaskCallbacks interface.");
        }

        // Hold a reference to the parent Activity so we can report back the task's current progress and results.
        mCallbacks = (TaskCallbacks) context;
    }

    /**
     * This method is called once when the Fragment is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (DEBUG) Log.i(TAG, "onCreate(Bundle)");
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }
    @Override
    public void onDestroy() {
        if (DEBUG) Log.i(TAG, "onDestroy()");
        super.onDestroy();
        cancel();
    }

    /*****************************/
    /***** TASK FRAGMENT API *****/
    /*****************************/

    /**
     * Start the background task.
     */
    public void start() {
        if (!mRunning) {
            dummyTask = new DummyTask();
            dummyTask.execute();
            mRunning = true;
        }
    }

    /**
     * Cancel the background task.
     */
    public void cancel() {
        if (mRunning) {
            dummyTask.cancel(false);
            dummyTask = null;
            mRunning = false;
        }
    }

    /**
     * Returns the current state of the background task.
     */
    public boolean isRunning() {
        return mRunning;
    }

    class DummyTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected void onPreExecute() {
            //
        }

        /**
         * Note that we do NOT call the callback object's methods directly from the
         * background thread, as this could result in a race condition.
         */
        @Override
        protected Void doInBackground(Void... ignore) {
            listFileSize2 = new ArrayList<>();
            listFileName2 = new ArrayList<>();

            File rootPath = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
            Log.e("TAG", "root: " + rootPath.toString());
            List<File> rootDirs = new ArrayList<File>();
            for (File f : rootPath.listFiles()) {
                if (f.isDirectory()) {
                    rootDirs.add(f);
                }
            }

            for (File f : rootDirs) { // find in root...
                scan(f);
            }

            avgFileSize = totalFileSize/fileCount;
            Log.e("TAG","avg file size: "+avgFileSize);

            sortedHashMap = sortByComparator(hashMap);
            Log.e("TAG", String.valueOf(sortedHashMap));
            Log.e("TAG", "files size: " + files.size());
            Log.e("TAG", "dir size: " + dir.size());

            /****  sending broadcast of arraylist data. We will use broadcast receiver in MainActivity to receive   *****/
            Intent intent2 = new Intent();
            intent2.setAction(ACTION_MyIntentService);
            intent2.addCategory(Intent.CATEGORY_DEFAULT);

            intent2.putStringArrayListExtra("file_name", listFileName);
            intent2.putStringArrayListExtra("file_size", listFileSize);
            intent2.putExtra("avg_file_size",avgFileSize);
            getActivity().sendBroadcast(intent2);

            return null;
        }

        public void scan(File path) {
            for (File f : path.listFiles()) {
                if (f.isFile()) {
                    files.add(f);
                    hashMap.put(f.getName(), f.length());
                    fileCount = fileCount+1;
                    totalFileSize = totalFileSize + f.length();

                    try {
                        Intent intentUpdate = new Intent();
                        intentUpdate.setAction(ACTION_MyUpdate);
                        intentUpdate.addCategory(Intent.CATEGORY_DEFAULT);
                        intentUpdate.putExtra(EXTRA_KEY_UPDATE, f.getName());
                        getActivity().sendBroadcast(intentUpdate);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                } else {
                    dir.add(f);
                    scan(f);
                }
            }
        }

        private HashMap<String, Long> sortByComparator(Map<String, Long> hashMap) {
            List<Map.Entry<String, Long>> list =
                    new LinkedList<Map.Entry<String, Long>>(hashMap.entrySet());

            Collections.sort(list, new Comparator<Map.Entry<String, Long>>() {
                public int compare(Map.Entry<String, Long> o1,
                                   Map.Entry<String, Long> o2) {
                    return (o2.getValue()).compareTo(o1.getValue());
                }
            });

            HashMap<String, Long> sortedMap = new HashMap<String, Long>();

            for (Iterator<Map.Entry<String, Long>> it = list.iterator(); it.hasNext(); ) {
                Map.Entry<String, Long> entry = it.next();
                sortedMap.put(entry.getKey(), entry.getValue());
                listFileName.add(entry.getKey());
                listFileSize.add("Size: " + String.valueOf(entry.getValue() / 1024) + " kb");
            }

            return sortedMap;
        }

        @Override
        protected void onProgressUpdate(Integer... percent) {
            // Proxy the call to the Activity.
        mCallbacks.onProgressUpdate();
        }

        @Override
        protected void onCancelled() {
        }

        @Override
        protected void onPostExecute(Void ignore) {
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (DEBUG) Log.i(TAG, "onActivityCreated(Bundle)");
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        if (DEBUG) Log.i(TAG, "onStart()");
        super.onStart();
    }

    @Override
    public void onResume() {
        if (DEBUG) Log.i(TAG, "onResume()");
        super.onResume();
    }

    @Override
    public void onPause() {
        if (DEBUG) Log.i(TAG, "onPause()");
        super.onPause();
    }

    @Override
    public void onStop() {
        if (DEBUG) Log.i(TAG, "onStop()");
        super.onStop();
    }

}

package kumar.karan.scancard;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import kumar.karan.scancard.fragments.MainFragment;

public class MainActivity extends AppCompatActivity implements MainFragment.TaskCallbacks
{
    protected Button mBtnStartScan;
    protected ListView mListScannedFiles;
    protected TextView mTextGetList;
    protected TextView mTextAvg;
    protected TextView mTextFrequency;
    private ProgressDialog progressDialog;
    protected   ArrayList<String> listFileSize2 = new ArrayList<>();
    protected ArrayList<String> listFileName2 = new ArrayList<>();


//    private MyBroadcastReceiver myBroadcastReceiver;
//    private MyBroadcastReceiver_Update myBroadcastReceiver_Update;
    protected MyBroadcastReceiver myBroadcastReceiver = new MyBroadcastReceiver();
    protected MyBroadcastReceiver_Update myBroadcastReceiver_Update = new MyBroadcastReceiver_Update();

    ProgressDialog ringProgressDialog;
    protected static int counterBtn=0;
    protected String update;
    protected long avgFileSize;

    /*.....................................................*/
    protected MainFragment mMainFragment;
    private static final String TAG_TASK_FRAGMENT = "task_fragment";
     /*....................................................*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextGetList = (TextView) findViewById(R.id.textGetList);
        mTextAvg = (TextView) findViewById(R.id.textAverage);
        mTextFrequency = (TextView) findViewById(R.id.textFrequency);

        mBtnStartScan = (Button) findViewById(R.id.btnStartScan);
        if (counterBtn == 0) {
            mBtnStartScan.setText("START SD CARD SCAN");
        }

        mListScannedFiles = (ListView) findViewById(R.id.listScannedFiles);

        mBtnStartScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mMainFragment.isRunning()) {
                    mMainFragment.cancel();
                } else {
                    mMainFragment.start();
                }

                if(mBtnStartScan.getText() == "RESCAN SD CARD"){
                    mMainFragment.start();
                }

                mListScannedFiles.setVisibility(View.VISIBLE);
                ringProgressDialog = ProgressDialog.show(MainActivity.this,"Scanning SD card..","===");

                //register BroadcastReceiver ................//
                IntentFilter intentFilter = new IntentFilter(MainFragment.ACTION_MyIntentService);
                intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
                registerReceiver(myBroadcastReceiver, intentFilter);

                IntentFilter intentFilter_update = new IntentFilter(MainFragment.ACTION_MyUpdate);
                intentFilter_update.addCategory(Intent.CATEGORY_DEFAULT);
                registerReceiver(myBroadcastReceiver_Update, intentFilter_update);
                //..............................................//

            }
        });

        FragmentManager fm = getSupportFragmentManager();
        mMainFragment = (MainFragment) fm.findFragmentByTag(TAG_TASK_FRAGMENT);

        // If the Fragment is non-null, then it is being retained over a configuration change.
        if (mMainFragment == null) {
            mMainFragment = new MainFragment();
            fm.beginTransaction().add(mMainFragment, TAG_TASK_FRAGMENT).commit();
        }

        // Restore saved state.
        if (savedInstanceState != null) {
            mListScannedFiles.setVisibility(View.VISIBLE);
            ringProgressDialog = ProgressDialog.show(MainActivity.this,"Scanning SD card,,,,","===");

            //register BroadcastReceiver ................//
            IntentFilter intentFilter = new IntentFilter(MainFragment.ACTION_MyIntentService);
            intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
            registerReceiver(myBroadcastReceiver, intentFilter);

            IntentFilter intentFilter_update = new IntentFilter(MainFragment.ACTION_MyUpdate);
            intentFilter_update.addCategory(Intent.CATEGORY_DEFAULT);
            registerReceiver(myBroadcastReceiver_Update, intentFilter_update);
            //..............................................//


            ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_2, android.R.id.text1, listFileName2) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                    TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                    text1.setText(listFileName2.get(position));
                    text2.setText(listFileSize2.get(position));
                    return view;
                }
                @Override
                public int getCount() {
                    return 10;
                }
            };
            mListScannedFiles.setAdapter(adapter2);
//
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.e("TAG", "===***** file name ===: " + listFileName2.toString());
        outState.putStringArrayList("bundleFileName",listFileName2);
        outState.putStringArrayList("bundleFileSize",listFileSize2);
    }

    @Override
    public void onProgressUpdate() {

    }

    public class MyBroadcastReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent) {

            listFileName2 = intent.getStringArrayListExtra("file_name");
            listFileSize2 = intent.getStringArrayListExtra("file_size");
            avgFileSize = intent.getLongExtra("avg_file_size", 0);
            mTextAvg.setText("Average File Size: "+String.valueOf(avgFileSize/1024) + "kb");
            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Average file size");
            builder.setMessage(String.valueOf(avgFileSize/1024) + "kb");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });

            builder.show();

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_2, android.R.id.text1, listFileName2) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                    TextView text2 = (TextView) view.findViewById(android.R.id.text2);
                    text1.setText(listFileName2.get(position));
                    text2.setText(listFileSize2.get(position));
                    return view;
                }
                @Override
                public int getCount() {
                    return 10;
                }
            };

            counterBtn++;
            mBtnStartScan.setText("RESCAN SD CARD");
            mBtnStartScan.setHeight(30);

            mTextGetList.setVisibility(View.GONE);
            mListScannedFiles.setAdapter(adapter);
            ringProgressDialog.dismiss();
        }
    }

    public class MyBroadcastReceiver_Update extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            update =  intent.getStringExtra(MainFragment.EXTRA_KEY_UPDATE);
            ringProgressDialog.setMessage(update);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //un-register BroadcastReceiver
        unregisterReceiver(myBroadcastReceiver);
        unregisterReceiver(myBroadcastReceiver_Update);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}



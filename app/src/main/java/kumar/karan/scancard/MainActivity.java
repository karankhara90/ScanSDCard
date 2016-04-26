package kumar.karan.scancard;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

public class MainActivity extends AppCompatActivity {
    protected Button mBtnStartScan;
    protected ListView mListScannedFiles;
    protected TextView mTextGetList;
    protected TextView mTextStartScan;
    protected TextView mTextAvg;
    protected TextView mTextFrequency;
    private ProgressDialog progressDialog;
    protected ArrayList<String> listFileName2;
    protected   ArrayList<String> listFileSize2;


    private MyBroadcastReceiver myBroadcastReceiver;
    private MyBroadcastReceiver_Update myBroadcastReceiver_Update;
    ProgressDialog ringProgressDialog;
    protected static int counterBtn=0;
    protected String update;
    protected Intent msgIntent;
    protected long avgFileSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mTextGetList = (TextView)findViewById(R.id.textGetList);
//        mTextStartScan = (TextView)findViewById(R.id.textStartScan);
        mTextAvg = (TextView)findViewById(R.id.textAverage);
        mTextFrequency = (TextView)findViewById(R.id.textFrequency);

        mBtnStartScan=(Button)findViewById(R.id.btnStartScan);
        if(counterBtn ==0){
            mBtnStartScan.setText("START SD CARD SCAN");
        }

        mListScannedFiles = (ListView)findViewById(R.id.listScannedFiles);

        mBtnStartScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListScannedFiles.setVisibility(View.VISIBLE);
                listFileSize2 = new ArrayList<>();
                listFileName2 = new ArrayList<>();

                ringProgressDialog = new ProgressDialog(MainActivity.this);
                ringProgressDialog.setTitle("Scanning SD card");

                msgIntent = new Intent(MainActivity.this, MyService.class);
                startService(msgIntent);

                myBroadcastReceiver = new MyBroadcastReceiver();
                myBroadcastReceiver_Update = new MyBroadcastReceiver_Update();

                ringProgressDialog.show();
                ringProgressDialog.setCancelable(true);



                //register BroadcastReceiver
                IntentFilter intentFilter = new IntentFilter(MyService.ACTION_MyIntentService);
                intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
                registerReceiver(myBroadcastReceiver, intentFilter);

                IntentFilter intentFilter_update = new IntentFilter(MyService.ACTION_MyUpdate);
                intentFilter_update.addCategory(Intent.CATEGORY_DEFAULT);
                registerReceiver(myBroadcastReceiver_Update, intentFilter_update);

            }


        });
    }





    public class MyBroadcastReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent) {

            listFileName2 = intent.getStringArrayListExtra("file_name");
            listFileSize2 = intent.getStringArrayListExtra("file_size");
            avgFileSize = intent.getLongExtra("avg_file_size", 0);

            Log.e("TAG", "=== file name ===: " + listFileName2.toString());
            Log.e("TAG", "=== file size ===: " + listFileSize2.toString());
            Log.e("TAG", "==== avg file size ====: " + String.valueOf(avgFileSize));
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
            update =  intent.getStringExtra(MyService.EXTRA_KEY_UPDATE);
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

package kumar.karan.scancard;


import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class MyService extends IntentService {
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
    protected static long avgFileSize=0;


    public MyService() {
        super("apps.khara.sdscan.MyService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {


        Toast.makeText(this, "MyCustomService Started", Toast.LENGTH_SHORT).show();
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
        sendBroadcast(intent2);
        /**************************************************************/

    }


    public void scan(File path) {
//        int i=0;
        for (File f : path.listFiles()) {
            if (f.isFile()) {
                files.add(f);
                hashMap.put(f.getName(), f.length());
                fileCount = fileCount+1;
                totalFileSize = totalFileSize + f.length();

                try {
//                    Thread.sleep(1000);

                    Intent intentUpdate = new Intent();
                    intentUpdate.setAction(ACTION_MyUpdate);
                    intentUpdate.addCategory(Intent.CATEGORY_DEFAULT);
                    intentUpdate.putExtra(EXTRA_KEY_UPDATE, f.getName());
                    sendBroadcast(intentUpdate);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            } else {
                dir.add(f);
                scan(f);
            }
        }
//        Log.e("TAG", "......,,,,,,.......");


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

            //send update

        }

        return sortedMap;
    }


}


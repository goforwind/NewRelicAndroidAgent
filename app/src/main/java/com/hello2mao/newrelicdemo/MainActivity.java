package com.hello2mao.newrelicdemo;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.newrelic.agent.android.NewRelic;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private static final String KEY = "AA422db870c3bfa52d93f856953eb395882368be7a";
    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE };
    /**
     * Checks if the app has permission to write to device storage
     * If the app does not has permission then the user will be prompted to
     * grant permissions
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
        // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NewRelic.withApplicationToken(KEY)
                .withLoggingEnabled(true)
                .withLogLevel(5)
                .start(this.getApplication());

        findViewById(R.id.hmma_test).setOnClickListener(this);
        verifyStoragePermissions(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.hmma_test:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        OutputStream out = null;
                        InputStream is = null;
                        try {
                            URL url = new URL("https://hmma.baidu.com/ap.gif");
                            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                            // 发送POST请求必须设置如下两行
                            conn.setDoOutput(true);
                            conn.setUseCaches(false);
                            conn.setRequestMethod("POST");
                            conn.setRequestProperty("Content-Type","gzip");
                            conn.connect();
                            conn.setConnectTimeout(10000);
                            out =conn.getOutputStream();
                            File file = new File("/sdcard/app.gif");
                            DataInputStream in = new DataInputStream(new FileInputStream(file));
                            int bytes = 0;
                            byte[] buffer = new byte[1024];
                            while ((bytes = in.read(buffer)) != -1) {
                                out.write(buffer, 0, bytes);
                            }
                            in.close();
                            out.flush();
                            if (conn.getResponseCode() == 200) {
                                Log.d("1sss", "200");
//                                is = conn.getInputStream();
//                                Log.d("1sss",getResultString(is, "UTF-8"));
                            } else {
                                Log.d("1sss", "!200");
                            }

                        } catch (Exception e) {
                            System.out.println("发送文件出现异常！" + e);
                            e.printStackTrace();
                        } finally {
                            try {
                                out.close();

//                                is.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
                break;
            default:
                break;
        }
    }

    private static String getResultString(InputStream inputStream, String encode) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int len = 0;
        String result = "";
        if (inputStream != null) {
            try {
                while ((len = inputStream.read(data)) != -1) {
                    outputStream.write(data, 0, len);
                }
                result = new String(outputStream.toByteArray(), encode);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}

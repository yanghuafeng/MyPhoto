package com.example.myphoto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import me.iwf.photopicker.PhotoPickerActivity;
import me.iwf.photopicker.utils.PhotoPickerIntent;

public class MainActivity extends AppCompatActivity {
    private WebView webView;
    private TextView textView;
    private String path = Environment.getExternalStorageDirectory()+"/myphoto";
    private String pngurl = "file:///"+path+"/index.html";
    private String jpgurl = "file:///"+path+"/index.html";
    private static final String[] authBaseArr = {//申请权限类型
            Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final int authBaseRequestCode = 1;
    private ArrayList<String> photos;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = (WebView)findViewById(R.id.web_view);
        textView = (TextView)findViewById(R.id.text);
        webView.setWebViewClient(new WebViewClient());
        initNavi();
        sharedPreferences = getApplicationContext().getSharedPreferences("myphoto",MODE_PRIVATE);
        if(sharedPreferences.getBoolean("FIRST_START", true)) {
            try {
                addText("正在解压文件。。。");
                Utils.unZipAssets(this, "package.zip", path, true);
                sharedPreferences.edit().putBoolean("FIRST_START", false).apply();
                addText("解压完成，右上角选择图片");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            addText("右上角选择图片");
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menu.add(1,1,1,"选择图片");
        menu.add(1,2,2,"显示图片");
        menu.add(1,3,3,"显示日志");
        menu.add(1,4,4,"清空日志");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id){
            case 1:
                PhotoPickerIntent intent2 = new PhotoPickerIntent(MainActivity.this);
                intent2.setPhotoCount(12);
                startActivityForResult(intent2, authBaseRequestCode);
                break;
            case 2:
                textView.setVisibility(View.GONE);
                webView.loadUrl(jpgurl);
                webView.setVisibility(View.VISIBLE);
                break;
            case 3:
                textView.setVisibility(View.VISIBLE);
                webView.setVisibility(View.GONE);
                break;
            case 4:
                textView.setText("");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == authBaseRequestCode) {
            if (data != null) {
                photos = data.getStringArrayListExtra(PhotoPickerActivity.KEY_SELECTED_PHOTOS);
                if (photos != null && photos.size() == 12) {

                    addText("选择图片完成");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            savePhoto(photos);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    textView.setVisibility(View.GONE);
                                    webView.loadUrl(jpgurl);

                                }
                            });
                        }
                    }).start();
                }else{
                    Toast.makeText(this,"请选择12张",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void savePhoto(ArrayList<String> photos){
        int i=0;
        for(String mpath:photos){
            i++;
            Bitmap bitmap = Utils.getImage(mpath,i<=6);
            Utils.savePhoto(bitmap,path+"/img/",+i+".jpg");
            final  String name = "保存文件："+path+"/img/"+i+".jpg";
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addText(name);
                }
            });

        }
    }

    private boolean hasBasePhoneAuth() {
        PackageManager pm = getPackageManager();
        for (String auth : authBaseArr) {
            if (pm.checkPermission(auth, getPackageName()) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void initNavi() {
        // 申请权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!hasBasePhoneAuth()) {
                this.requestPermissions(authBaseArr, authBaseRequestCode);
            }else{
                if(sharedPreferences.getBoolean("FIRST_START", true)) {
                    try {
                        addText("正在解压文件。。。");
                        Utils.unZipAssets(this, "package.zip", path, true);
                        sharedPreferences.edit().putBoolean("FIRST_START", false).apply();
                        addText("解压完成，右上角选择图片");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else {
                    addText("右上角选择图片");
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(sharedPreferences.getBoolean("FIRST_START", true)) {
            try {
                addText("正在解压文件。。。");
                Utils.unZipAssets(this, "package.zip", path, true);
                sharedPreferences.edit().putBoolean("FIRST_START", false).apply();
                addText("解压完成，右上角选择图片");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            addText("右上角选择图片");
        }
    }

    private void addText(String content) {
        textView.append(content);
        textView.append("\n");
        int offset = textView.getLineCount() * textView.getLineHeight();
        if (offset > textView.getHeight()) {
            textView.scrollTo(0, offset - textView.getHeight());
        }
    }
}

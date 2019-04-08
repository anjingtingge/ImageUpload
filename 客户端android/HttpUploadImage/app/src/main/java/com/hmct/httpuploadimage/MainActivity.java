package com.hmct.httpuploadimage;



import net.tsz.afinal.FinalBitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    ProgressDialog dialog=null;
    String result="";

    Button btn=null;
    TextView tv=null;
    ImageView iv=null;

    FinalBitmap fb=null;

    @SuppressLint("HandlerLeak")
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            dialog.dismiss();
            switch (msg.what) {
                case 1:
                    Toast.makeText(getApplicationContext(), "图片上传成功", Toast.LENGTH_SHORT).show();
                    tv.setText("上传成功,图片路径："+Const.DOWNLOAD_URL+result);
                    fb.display(iv, Const.DOWNLOAD_URL+result);
                    break;
                case 2:
                    Toast.makeText(getApplicationContext(), "图片上传失败", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dialog=new ProgressDialog(this);
        dialog.setTitle("请稍后...");

        fb=FinalBitmap.create(this);

        btn=(Button)findViewById(R.id.btn);
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent openAlbumIntent = new Intent(Intent.ACTION_GET_CONTENT);
                openAlbumIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(openAlbumIntent, 1);
            }
        });

        tv=(TextView)findViewById(R.id.tv);
        iv=(ImageView)findViewById(R.id.iv);

    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        Uri uri = null;
        switch (requestCode) {
            case 1:// 相册
                if (data == null) {
                    return;
                }
                uri = data.getData();
                String[] proj = { MediaStore.Images.Media.DATA };
                Cursor cursor = managedQuery(uri, proj, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                final String path = cursor.getString(column_index);// 图片在的路径

                Log.e("jj", "path:"+path);
                dialog.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        result = ServerUtils.formUpload(Const.UPLOAD_URL, path);
                        Log.e("jj", "result:"+result);
                        if(!TextUtils.isEmpty(result)){
                            handler.sendEmptyMessage(1);
                        }else{
                            handler.sendEmptyMessage(2);
                        }
                    }
                }).start();
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


}

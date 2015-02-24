package com.example.codescan.app;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity {
    private final static int SCANNIN_GREQUEST_CODE = 1;
    private final static String OUTPUT_FOLDER = "CodeScan";

    private TextView mCodeView;
    private TextView mCountView;
    private Button mSubTract;
    private ImageView mImageView;

    private Map<String, Integer> map = new HashMap<String, Integer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        map = ConvertStringToMap(ReadFromFile(GetFileName()));

        mCodeView = (TextView) findViewById(R.id.result);
        mCountView = (TextView) findViewById(R.id.count);

        mImageView = (ImageView) findViewById(R.id.qrcode_bitmap);

        Button mButton = (Button) findViewById(R.id.button1);
        mButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setClass(MainActivity.this, MipcaActivityCapture.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intent, SCANNIN_GREQUEST_CODE);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SCANNIN_GREQUEST_CODE:
                if(resultCode == RESULT_OK){
                    Bundle bundle = data.getExtras();
                    final String code = bundle.getString("result");

                    mCodeView.setText("条码：" + code);
                    SaveCode(code, GetCodeCount(code) + 1);
                    mCountView.setText("数量：" + GetCodeCount(code));

                    mImageView.setImageBitmap((Bitmap) data.getParcelableExtra("bitmap"));

                    Button mSubTract = (Button) findViewById(R.id.subtract);
                    mSubTract.setVisibility(View.VISIBLE);

                    mSubTract.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            SaveCode(code, GetCodeCount(code) - 1 );
                            mCountView.setText("数量：" + GetCodeCount(code));

                        }
                    });

                }
                break;
        }
    }

    private String GetOutputDir() {
        String outputDir = "";
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {

            File f = Environment.getExternalStorageDirectory();//获取SD卡目录
            outputDir = f.getPath() + "/" + OUTPUT_FOLDER + "/";
            File mDir = new File(outputDir);

            if (!mDir.exists()) {
                mDir.mkdirs();
            }
        }

        return outputDir;
    }

    private String GetFileName()
    {
        Date date = new Date();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String fileName = format.format(date);

        return GetOutputDir() + fileName + ".txt";
    }

    private int GetCodeCount(String code){
        if(map.containsKey(code)){
            return map.get(code);
        }

        return 0;
    }

    private void SaveCode(String code, int count)
    {
        if (count < 0) count = 0;
        map.put(code, count);

        try {
            SaveToFile(GetFileName(), ConvertMapToString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String ConvertMapToString()
    {
        StringBuilder sb = new StringBuilder();

        for (String key : map.keySet()) {

            sb.append(key + "," + map.get(key) + "\r\n");

        }
        return sb.toString();
    }

    private void SaveToFile(String fileName, String content) throws FileNotFoundException {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {

            FileOutputStream fos = new FileOutputStream(fileName);

            try {
                fos.write(content.getBytes());
                fos.close();
                Toast.makeText(MainActivity.this, "条码已写入文件",
                        Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "写入文件失败",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String ReadFromFile(String fileName) {
        String content = "";
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            File f = new File(fileName);
            if (f.exists()) {
                try {
                    FileInputStream inputStream = new FileInputStream(fileName);
                    byte[] b = new byte[inputStream.available()];
                    inputStream.read(b);
                    //Toast.makeText(MainActivity.this, "读取文件成功", Toast.LENGTH_LONG).show();
                    content = new String(b);
                } catch (Exception e) {
                    //Toast.makeText(MainActivity.this, "读取失败", Toast.LENGTH_SHORT).show();
                }
            }
        }

        return content;
    }

    private Map<String, Integer> ConvertStringToMap(String content){

        Map<String, Integer> map = new HashMap<String, Integer>();
        Matcher m = Pattern.compile("(.*[^,]),(\\d+)")
                        .matcher(content);

        while(m.find()) {
                map.put(m.group(1),Integer.parseInt(m.group(2)));
        }

        return map;
    }

}

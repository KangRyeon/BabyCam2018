package com.example.babycam;

import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button connect_btn;                 // ip 받아오는 버튼
    Button show_btn;                    // 아이가 울고있어요 버튼
    Button clear_btn;                    // 아이가 울고있어요 지우는 버튼

    EditText ip_edit;               // ip 에디트
    TextView show_text;             // 서버에서온거 보여주는 에디트
    // 소켓통신에 필요한것
    private String html = "";
    private Handler mHandler;

    private Socket socket;

    private BufferedReader networkReader;
    private PrintWriter networkWriter;

    private DataOutputStream dos;
    private DataInputStream dis;

    private String ip = "xxx.xxx.xx.xx";            // IP 번호
    private int port = 9999;                          // port 번호

    Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 웹캠 보여주기
        WebView webView = (WebView)findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient());
        webView.setBackgroundColor(255);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        //영상을 폭을 꽉 차게 하기 위해 직접 html태그로 작성함.
        webView.loadData("<html><head><style type='text/css'>body{margin:auto auto;text-align:center;} img{width:100%25;} div{overflow: hidden;} </style></head><body><div><img src='http://192.168.25.15:8090/stream/video.mjpeg'/></div></body></html>" ,"text/html",  "UTF-8");
        //webView.loadUrl("http://192.168.25.15:8090/stream/video.jpeg");



        // 버튼
        connect_btn = (Button)findViewById(R.id.connect_btn);
        connect_btn.setOnClickListener(this);

        show_btn = (Button)findViewById(R.id.show_btn);
        show_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                RelativeLayout rel = (RelativeLayout)View.inflate(
                        MainActivity.this, R.layout.newmessage, null);

                //TextView text_view = (TextView)findViewById(R.id.print_view);
                //System.out.println(text_view.getText());
                LinearLayout linear = (LinearLayout)findViewById(R.id.linear2);
                if (linear.getChildCount() % 2 == 0)
                    rel.setBackgroundColor(Color.GRAY);
                else
                    rel.setBackgroundColor(Color.LTGRAY);
                linear.addView(rel);
            }
        });

        clear_btn = (Button)findViewById(R.id.clear_btn);
        clear_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                LinearLayout linear = (LinearLayout)findViewById(R.id.linear2);
                linear.removeAllViews();
            }
        });

        ip_edit = (EditText)findViewById(R.id.ip_edit);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.connect_btn:     // ip 받아오는 버튼
                connect();
            case R.id.show_btn:
        }
    }

    // 로그인 정보 db에 넣어주고 연결시켜야 함.
    void connect(){
        mHandler = new Handler();
        Log.w("connect","연결 하는중");
        // 받아오는거
        Thread checkUpdate = new Thread() {
            public void run() {
                // ip받기
                String newip = String.valueOf(ip_edit.getText());

                // 서버 접속
                try {
                    socket = new Socket(newip, port);
                    Log.w("서버 접속됨", "서버 접속됨");
                } catch (IOException e1) {
                    Log.w("서버접속못함", "서버접속못함");
                    e1.printStackTrace();
                }

                Log.w("edit 넘어가야 할 값 : ","안드로이드에서 서버로 연결요청");

                // Buffered가 잘못된듯.
                try {
                    dos = new DataOutputStream(socket.getOutputStream());   // output에 보낼꺼 넣음
                    dis = new DataInputStream(socket.getInputStream());     // input에 받을꺼 넣어짐
                    dos.writeUTF("안드로이드에서 서버로 연결요청");

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.w("버퍼", "버퍼생성 잘못됨");
                }
                Log.w("버퍼","버퍼생성 잘됨");

                while(true) {
                    // 서버에서 받아옴
                    try {
                        String line = "";
                        int line2;
                        while (true) {
                            //line = (String) dis.readUTF();
                            line2 = (int) dis.read();
                            //Log.w("서버에서 받아온 값 ", "" + line);
                            //Log.w("서버에서 받아온 값 ", "" + line2);

                            if(line2 == 1) {
                                Log.w("------서버에서 받아온 값 ", "" + line2);
                                dos.writeUTF("하나 받았습니다. : " + line2);
                                dos.flush();
                                new Thread(new Runnable()
                                {
                                    @Override
                                    public void run() {
                                        while (!Thread.interrupted())
                                            try {
                                                Thread.sleep(1000);
                                                // start actions in UI thread
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        show_btn.performClick();            // 클릭
                                                        long[] pattern = {100,300,100,700,300,2000}; // 패턴지정
                                                        vibrator.vibrate(pattern, -1); // 패턴으로 진동울림.
                                                    }
                                                });
                                                break;
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                    }
                                }).start();
                            }
                            if(line2 == 99) {
                                Log.w("------서버에서 받아온 값 ", "" + line2);
                                socket.close();
                                break;
                            }
                            /*
                            Intent intent = new Intent(Membership.this, Membership3.class); // 인텐트 생성 여따하면안됨
                            startActivity(intent);
                            finish();
                            */

                        }
                    } catch (Exception e) {

                    }
                }

            }
        };
        // 소켓 접속 시도, 버퍼생성
        checkUpdate.start();
    }
}
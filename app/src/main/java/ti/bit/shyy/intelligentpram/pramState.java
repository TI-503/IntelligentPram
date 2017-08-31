package ti.bit.shyy.intelligentpram;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.opengl.EGLConfig;
import android.opengl.GLU;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.ScaleAnimation;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.opengl.GLSurfaceView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by ShinoharaYuyoru on 2017/06/18.
 */

public class pramState extends AppCompatActivity {
    public Button Button_startMonitor;
    public Button Button_returnMain1;
    public Button Button_goFront;
    public Button Button_goRear;
    public Button Button_goLeft;
    public Button Button_goRight;
    public Button Button_goClockwise;
    public Button Button_goAntiClockwise;
    public Button Button_stopMove;
    public Button Button_listenSound;
    public Button Button_stopListenSound;

    public MediaPlayer mpDangerous;
    public MediaPlayer mpError;

    Thread sendMoveSubThread;

    private LinearLayout alcohol;
    private LinearLayout meter;
    public float staratemp;
    public float temp;  // The fake Temperature used to draw
    private float temperatureF;
    private float AccelerateZ;

    static public TextView TextV_State;
    public TextView TextV_tempState;
    public WebView webView;
    private TCP_GetState TCPst;
    private TCP_sendMove TCPsM;

    private boolean MonitorFlag = false;

//    private GLSurfaceView  mView;
//    private final float pi=(float)Math.acos(0.0)*2;
//    private double alpha[]={pi/3,pi/3,pi/3};
//    float[] a_x={0f,0f,0f,2f,0f,0f};
//    float[] a_y={0f,0f,0f,0f,2f,0f};
//    //float[] a_z={0f,0f,0f,0f,0f,2f};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pramstate);

        // Thermometer
        meter = ((LinearLayout) findViewById(R.id.meter));
        alcohol = ((LinearLayout) findViewById(R.id.alcohol));

        //Video
        webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);

        // MediaPlayer
        mpDangerous = MediaPlayer.create(this, R.raw.dangerouserror);
        mpError = MediaPlayer.create(this, R.raw.criticalstop);

        // Start monitor
        TextV_State = (TextView) findViewById(R.id.stateText);
        TextV_tempState = (TextView)findViewById(R.id.tempStateText);
        Button_startMonitor = (Button) findViewById(R.id.startMonitorButton);
        Button_startMonitor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View videoV) {
                if(MonitorFlag == false){
                    MonitorFlag = true;
                    Button_startMonitor.setText("停止视频、传感器监听");
                    // WebView
                    webView.loadUrl("http://192.168.10.1:8080/?action=stream");

                    // State
                    TCPst = new TCP_GetState();
                    TCPst.execute();
                }
                else{
                    MonitorFlag = false;

                    Button_startMonitor.setText("开始视频、传感器监听");
                    TextV_State.setTextColor(Color.parseColor("#ffff8800"));
                    TextV_State.setText("传感器等待监听");
                    TextV_tempState.setTextColor(Color.parseColor("#ffff8800"));
                    TextV_tempState.setText("温度等待监听");
                    webView.loadUrl("about:blank");

                    TCPst.cancel(true);
                }
            }
        });

//        //mView用于显示3D图像，当需要显示时同时去掉此部分注释和MyRenderer类的注释,同时去掉activity_main.xml文件中的一段GLSurfaceView的注释
//        mView=(GLSurfaceView)findViewById(R.id.glview_1);
//        mView.setRenderer(new MyRenderer());
//        mView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
//        mView = new GLSurfaceView(this);

        // Return Main1
        Button_returnMain1 = (Button) findViewById(R.id.returnMainButton1);
        Button_returnMain1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View MainActivity1V) {
                if(TCPst != null && TCPst.getStatus() == AsyncTask.Status.RUNNING)
                {
                    TCPst.cancel(true);
                }

                mpDangerous.release();
                mpError.release();

                finish();
            }
        });

        // Go Front
        Button_goFront = (Button) findViewById(R.id.goFrontButton);
        Button_goFront.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View goFrontV) {
                sendMoveSubThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        TCPsM = new TCP_sendMove();
                        TCPsM.goFront();
                    }
                });
                sendMoveSubThread.start();
            }
        });

        // Go Rear
        Button_goRear = (Button) findViewById(R.id.goRearButton);
        Button_goRear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View goRearV) {
                sendMoveSubThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        TCPsM = new TCP_sendMove();
                        TCPsM.goRear();
                    }
                });
                sendMoveSubThread.start();
            }
        });

        // Go Left
        Button_goLeft = (Button) findViewById(R.id.goLeftButton);
        Button_goLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View goLeftV) {
                sendMoveSubThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        TCPsM = new TCP_sendMove();
                        TCPsM.goLeft();
                    }
                });
                sendMoveSubThread.start();
            }
        });

        // Go Right
        Button_goRight = (Button) findViewById(R.id.goRightButton);
        Button_goRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View goRightV) {
                sendMoveSubThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        TCPsM = new TCP_sendMove();
                        TCPsM.goRight();
                    }
                });
                sendMoveSubThread.start();
            }
        });

        // Go Clockwise
        Button_goClockwise = (Button) findViewById(R.id.goClockwiseButton);
        Button_goClockwise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View goClockwiseV) {
                sendMoveSubThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        TCPsM = new TCP_sendMove();
                        TCPsM.goClockwise();
                    }
                });
                sendMoveSubThread.start();
            }
        });

        // Go AntiClockwise
        Button_goAntiClockwise = (Button) findViewById(R.id.goAntiClockwiseButton);
        Button_goAntiClockwise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View goAntiClockwiseV) {
                sendMoveSubThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        TCPsM = new TCP_sendMove();
                        TCPsM.goAntiClockwise();
                    }
                });
                sendMoveSubThread.start();
            }
        });

        // Stop Move
        Button_stopMove = (Button) findViewById(R.id.stopMoveButton);
        Button_stopMove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View stopMoveV) {
                sendMoveSubThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        TCPsM = new TCP_sendMove();
                        TCPsM.stopMove();
                    }
                });
                sendMoveSubThread.start();
            }
        });

        // Listen Sound
        // In this state, other buttons except stopLisntenSound Button should be unable.
        Button_listenSound = (Button) findViewById(R.id.listenSoundButton);
        Button_listenSound.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View listenSoundV)
            {
                sendMoveSubThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        TCPsM = new TCP_sendMove();
                        TCPsM.listenSound();
                    }
                });

                Button_goFront.setEnabled(false);
                Button_goRear.setEnabled(false);
                Button_goLeft.setEnabled(false);
                Button_goRight.setEnabled(false);
                Button_goClockwise.setEnabled(false);
                Button_goAntiClockwise.setEnabled(false);
                Button_stopMove.setEnabled(false);
                Button_listenSound.setEnabled(false);
                Button_stopListenSound.setEnabled(true);

                sendMoveSubThread.start();
            }
        });

        // Stop Listen Sound
        // This state is setEnable(false) in default.
        Button_stopListenSound = (Button) findViewById(R.id.stopListenSoundButton);
        Button_stopListenSound.setEnabled(false);
        Button_stopListenSound.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View stopListenSoundV)
            {
                sendMoveSubThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        TCPsM = new TCP_sendMove();
                        TCPsM.stopListenSound();
                    }
                });

                Button_goFront.setEnabled(true);
                Button_goRear.setEnabled(true);
                Button_goLeft.setEnabled(true);
                Button_goRight.setEnabled(true);
                Button_goClockwise.setEnabled(true);
                Button_goAntiClockwise.setEnabled(true);
                Button_stopMove.setEnabled(true);
                Button_listenSound.setEnabled(true);
                Button_stopListenSound.setEnabled(false);

                sendMoveSubThread.start();
            }
        });
    }

    public class TCP_GetState extends AsyncTask<Void, String, Void> {
        @Override
        protected void onPreExecute()
        {
            TextV_State.setTextColor(Color.BLUE);
            TextV_State.setText("等待数据...");
            TextV_tempState.setTextColor(Color.BLUE);
            TextV_tempState.setText("等待数据...");
        }

        @Override
        protected Void doInBackground(Void... param) {

            try {
                //创建TCPServer
//                ServerSocket serverSocket = new ServerSocket(5001);
//                Socket TCP_Socket = serverSocket.accept();
//                while (true) {
//                    InputStream inputStream = TCP_Socket.getInputStream();
//                    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
//                    String StateData;
//                    StateData = br.readLine();
//                    Log.d("TCP", StateData);
//
//                    publishProgress(StateData);

                //创建TCP Client
                Socket accTmp_Socket = new Socket();
                accTmp_Socket.connect(new InetSocketAddress("192.168.10.208", 5001));

                //循环接收温度加速度字符串
                while(MonitorFlag) {
//                    BufferedInputStream BIS = new BufferedInputStream(accTmp_Socket.getInputStream());
//                    DataInputStream DIS = new DataInputStream(BIS);
//
//                    byte[] inputBytes = new byte[1024];
//                    DIS.read(inputBytes);
//                    String inputString = new String(inputBytes);
//                    Log.d("TCP Client", inputString);
//
//                    publishProgress(inputString);
                    InputStream inputStream = accTmp_Socket.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                    String StateData;
                    StateData = br.readLine();
                    Log.d("TCP", StateData);

                    publishProgress(StateData);
                }

                accTmp_Socket.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onProgressUpdate(String... Data) {
            String Whole = Data[0];
            String Tempra = Whole.substring(Whole.indexOf("Temp is ") + 8, Whole.indexOf(" F "));
            String AccX = Whole.substring(Whole.indexOf("AccX=") + 5, Whole.indexOf(",AccY"));
            String AccY = Whole.substring(Whole.indexOf("AccY=") + 5, Whole.indexOf(",AccZ"));
            String AccZ = Whole.substring(Whole.indexOf("AccZ=") + 5);

            //TextV_State.setText("The Temparature is " + Tempra + "\n" + "The acceleration in X, Y and Z is" + AccX + "\n" + AccY + "\n" + AccZ);

            // Draw the Thermometer
            float TEMPFLT = Float.parseFloat(Tempra);
            setTemperatureF(TEMPFLT);
            mUpdateUi();

//            // Draw the OpenGL Graphics
//            // Use the Acc and Calculate
//            double Vx=Double.parseDouble(AccX);//将数值字符串转换为实数，电压值
//            double Vy=Double.parseDouble(AccY);
//            double Vz=Double.parseDouble(AccZ);
//            Vx=(Vx-0.73)/0.28;//计算三个方向的加速度值
//            Vy=(Vy-0.77)/0.28;
//            Vz=(Vz-0.788)/0.28;
//
//            Vx=Vx<1?Vx:0.9999999;//加速度阈值处理
//            Vx=Vx>-1?Vx:-0.9999999;
//
//            Vy=Vy<1?Vy:0.9999999;
//            Vy=Vy>-1?Vy:-0.9999999;
//
//            Vz=Vz<1?Vz:0.9999999;
//            Vz=Vz>-1?Vz:-0.9999999;
//            alpha[0]=Math.acos(Vx);//计算角度
//            alpha[1]=Math.acos(Vy);
//            alpha[2]=Math.acos(Vz);
//
//            double temp_cos2,temp_cos1,temp_sin1;//临时变量，用于计算向量
//            temp_cos2=Math.cos(alpha[1]);
//            temp_cos1=Math.cos(alpha[0]);
//            temp_sin1=Math.sin(alpha[0]);
//
//            a_x[3]=0f;//计算芯片的x方向在世界坐标系中的坐标
//            a_x[4]=(float)temp_sin1;
//            a_x[5]=(float)temp_cos1;
//
//            double temp_a_yx;//计算芯片的y方向在世界坐标系中的坐标
//            a_y[5]=(float)temp_cos2;
//            a_y[4]=(float)(-temp_cos2*temp_cos1/temp_sin1);
//            temp_a_yx=1-temp_cos2*temp_cos2-a_y[4]*a_y[4];
//            temp_a_yx=temp_a_yx>0?temp_a_yx:0;
//            a_y[3]=(float)Math.sqrt(temp_a_yx);
//
//            Log.i("ACC","alpha x:"+alpha[0]*180/pi+"\nalpha y:"+alpha[1]*180/pi+"\n" +
//                    "alpha z:"+alpha[2]*180/pi+"\n"+"1:x=("+a_x[3]+","+a_x[4]+","+a_x[5]+") y=("+a_y[3]+","+a_y[4]+","+a_y[5]+")\n");
//
//            double temp_len=a_y[3]*a_y[3]+a_y[4]*a_y[4]+a_y[5]*a_y[5];
//            temp_len=Math.sqrt(temp_len);
//
//            a_y[3]=(float)(a_y[3]/temp_len);//y坐标向量单位化
//            a_y[4]=(float)(a_y[4]/temp_len);
//            a_y[5]=(float)(a_y[5]/temp_len);
//
//            Log.i("ACC","2:x=("+a_x[3]+","+a_x[4]+","+a_x[5]+") y=("+a_y[3]+","+a_y[4]+","+a_y[5]+")\n");

            // checkDangerous
            float ZFLT = Float.parseFloat(AccZ);
            setAccelerateZ(ZFLT);
            checkDangerous();
        }

        @Override
        protected void onCancelled() {

        }
    }

    /*
    * Main
    * Update the Thermometer
    */
    private void mUpdateUi() {
        ScaleAnimation localScaleAnimation1 = new ScaleAnimation(1.0F, 1.0F, this.staratemp, this.temp, 1, 0.5F, 1,
                1.0F);
        localScaleAnimation1.setDuration(1000L);
        localScaleAnimation1.setFillEnabled(true);
        localScaleAnimation1.setFillAfter(true);
        this.alcohol.startAnimation(localScaleAnimation1);
        this.staratemp = this.temp;

        ScaleAnimation localScaleAnimation2 = new ScaleAnimation(1.0F, 1.0F, 1.0F, 1.0F, 1, 0.5F, 1, 0.5F);
        localScaleAnimation2.setDuration(10L);
        localScaleAnimation2.setFillEnabled(true);
        localScaleAnimation2.setFillAfter(true);
        this.meter.startAnimation(localScaleAnimation2);

        // 把刻度表看出总共700份，如何计算缩放比例。从-20°到50°。
        // 例如，现在温度是30°的话，应该占（30+20）*10=500份 其中20是0到-20°所占有的份
        this.temp = (float) ((20.0F + getTemperatureC()) * 10) / (70.0F * 10);

        // Dangerous Check
        float tempC = getTemperatureC();
        if(tempC > 30)
        {
            // HOT: tempC > 30
            TextV_tempState.setTextColor(Color.RED);
            TextV_tempState.setText("温 度 过 热！");
        }
        else
        {
            if(tempC >= 20)
            {
                // FIT: 20 <= tempC <= 30
                TextV_tempState.setTextColor(Color.GREEN);
                TextV_tempState.setText("温 度 适 中");
            }
            else
            {
                // COLD: tempC < 20
                TextV_tempState.setTextColor(Color.BLUE);
                TextV_tempState.setText("温 度 过 冷！");
            }
        }
    }

    /*
    * Tool
    * Save one number after the dot
    */
    public float getFloatOne(float tempFloat) {
        return (float) (Math.round(tempFloat * 10)) / 10;
    }

    /*
    * Tool
    * Get the F temperature in TCP thread
    */
    public void setTemperatureF(float temperatureF) {
        this.temperatureF = temperatureF;
    }

    /*
    * Tool
    * Get the F temperature
    */
    public float getTemperatureF() {
        return getFloatOne(temperatureF);
    }

    /*
    * Tool
    * Get the C temperatur
    */
    public float getTemperatureC() {
        float temperatureC = (temperatureF - 32)/(9.0F/5.0F); // (temperatureC * 9 / 5) + 32;
        return getFloatOne(temperatureC);
    }

    /*
    * Set Accelerate Z
    */
    public void setAccelerateZ(float ACCZ)
    {
        this.AccelerateZ = ACCZ;
    }

    /*
    * Check AccZ
    */
    private void checkDangerous()
    {
        // Safe Z > 55
        // Dangerous Z [45, 55]
        // DIE Z < 45
        if(AccelerateZ <= -60)
        {
            TextV_State.setTextColor(Color.GREEN);
            TextV_State.setText("安 全");
        }
        else
        {
            if(AccelerateZ <= -45)
            {
                TextV_State.setTextColor(Color.rgb(255,97,0));
                TextV_State.setText("危 险！");
                mpDangerous.start();
            }
            else
            {
                if(AccelerateZ > -45)
                {
                    TextV_State.setTextColor(Color.RED);
                    TextV_State.setText("即 将 倾 覆！！！");
                    mpError.start();
                }
            }
        }
    }

//    //以下部分为OpenGL，显示3D图像
//    class MyRenderer implements GLSurfaceView.Renderer
//    {
//        public void onSurfaceCreated(GL10 gl, javax.microedition.khronos.egl.EGLConfig config)
//        {
//            // Set the background frame color
////            GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
////            GLES20.glVertexAttribPointer();
//            gl.glClearColor(0, 0, 0, 1);
//            //启用顶点缓冲区
//            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
//            //  GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
//            //  GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
//        }
//
//        public void onDrawFrame(GL10 gl)
//        {
//            gl.glClear(GL10.GL_COLOR_BUFFER_BIT);//清除颜色缓冲区
//
//            //模型视图矩阵
//            gl.glMatrixMode(gl.GL_MODELVIEW);
//            gl.glLoadIdentity();
//
//            GLU.gluLookAt(gl, 5,5, 5, 0, 0, 0, 0,0,1);
//
//            // 画三角形
//            // 绘制数组
//            // 三角形坐标
//            float[] coords = {
//                    1f,0f,1f,
//                    -1f,0f,1f,
//                    1f,0f,-1f,
//                    -1f,0f,-1f,
//            };
//            float[] axio_x={0f,0f,0f,2f,0f,0f};
//            float[] axio_y={0f,0f,0f,0f,2f,0f};
//            float[] axio_z={0f,0f,0f,0f,0f,2f};
//
//            //分配字节缓冲区控件,存放顶点坐标数据
//            float chip[]=new float[12];
//
//            chip[0]=a_x[3]+a_y[3];
//            chip[1]=a_x[4]+a_y[4];
//            chip[2]=a_x[5]+a_y[5];
//
//            chip[3]=a_x[3]-a_y[3];
//            chip[4]=a_x[4]-a_y[4];
//            chip[5]=a_x[5]-a_y[5];
//
//            chip[6]=a_y[3]-a_x[3];
//            chip[7]=a_y[4]-a_x[4];
//            chip[8]=a_y[5]-a_x[5];
//
//            chip[9]=-chip[0];
//            chip[10]=-chip[1];
//            chip[11]=-chip[2];
//
//            ByteBuffer ibb = ByteBuffer.allocateDirect(coords.length * 4);
//            ibb.order(ByteOrder.nativeOrder());//设置顺序(本地顺序)
//            FloatBuffer fbb = ibb.asFloatBuffer();//放置顶点坐标数组
//            fbb.put(coords);
//            ibb.position(0);//定位指针的位置,从该位置开始读取顶点数据
//            gl.glColor4f(0.5f, 0.5f, 0.5f, 1f);//设置绘图时的颜色
//            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, ibb);
//            gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);//绘制四边形
//
//            ibb = ByteBuffer.allocateDirect(chip.length * 4);
//            ibb.order(ByteOrder.nativeOrder());//设置顺序(本地顺序)
//            fbb = ibb.asFloatBuffer();//放置顶点坐标数组
//            fbb.put(chip);
//            ibb.position(0);//定位指针的位置,从该位置开始读取顶点数据
//            gl.glColor4f(0.5f, 0.5f, 0.5f, 1f);//设置绘图时的颜色
//            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, ibb);
//            gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);//绘制四边形
//
//            //分配字节缓冲区控件,存放顶点坐标数据
//            ByteBuffer ibbx = ByteBuffer.allocateDirect(axio_x.length * 4);
//            ibbx.order(ByteOrder.nativeOrder());//设置顺序(本地顺序)
//            FloatBuffer fbbx = ibbx.asFloatBuffer();//放置顶点坐标数组
//            fbbx.put(axio_x);
//            ibbx.position(0);//定位指针的位置,从该位置开始读取顶点数据
//            gl.glColor4f(1f, 0.5f, 0.5f, 1f);//设置绘图时的颜色
//            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, ibbx);
//            gl.glDrawArrays(GL10.GL_LINES, 0, 2);//绘制x轴
//
//            //分配字节缓冲区控件,存放顶点坐标数据
//            ByteBuffer ibby = ByteBuffer.allocateDirect(axio_y.length * 4);
//            ibby.order(ByteOrder.nativeOrder());//设置顺序(本地顺序)
//            FloatBuffer fbby = ibby.asFloatBuffer();//放置顶点坐标数组
//            fbby.put(axio_y);
//            ibby.position(0);//定位指针的位置,从该位置开始读取顶点数据
//            gl.glColor4f(0.5f,1f,  0.5f, 1f);//设置绘图时的颜色
//            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, ibby);
//            gl.glDrawArrays(GL10.GL_LINES, 0, 2);//绘制y轴
//
//            //分配字节缓冲区控件,存放顶点坐标数据
//            ByteBuffer ibbz = ByteBuffer.allocateDirect(axio_z.length * 4);
//            ibbz.order(ByteOrder.nativeOrder());//设置顺序(本地顺序)
//            FloatBuffer fbbz = ibbz.asFloatBuffer();//放置顶点坐标数组
//            fbbz.put(axio_z);
//            ibbz.position(0);//定位指针的位置,从该位置开始读取顶点数据
//            gl.glColor4f( 0.5f, 0.5f,1f, 1f);//设置绘图时的颜色
//            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, ibbz);
//            gl.glDrawArrays(GL10.GL_LINES, 0, 2);//绘制z轴
//
//            //分配字节缓冲区控件,存放顶点坐标数据
//            ByteBuffer ibbx1 = ByteBuffer.allocateDirect(a_x.length * 4);
//            ibbx1.order(ByteOrder.nativeOrder());//设置顺序(本地顺序)
//            FloatBuffer fbbx1 = ibbx1.asFloatBuffer();//放置顶点坐标数组
//            fbbx1.put(a_x);
//            ibbx1.position(0);//定位指针的位置,从该位置开始读取顶点数据
//            gl.glColor4f(1f, 0.6f, 0.6f, 1f);//设置绘图时的颜色
//            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, ibbx1);
//            gl.glDrawArrays(GL10.GL_LINES, 0, 2);//绘制x轴
//
//            //分配字节缓冲区控件,存放顶点坐标数据
//            ByteBuffer ibby1 = ByteBuffer.allocateDirect(a_y.length * 4);
//            ibby1.order(ByteOrder.nativeOrder());//设置顺序(本地顺序)
//            FloatBuffer fbby1 = ibby1.asFloatBuffer();//放置顶点坐标数组
//            fbby1.put(a_y);
//            ibby1.position(0);//定位指针的位置,从该位置开始读取顶点数据
//            gl.glColor4f(0.6f,1f,  0.6f, 1f);//设置绘图时的颜色
//            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, ibby1);
//            gl.glDrawArrays(GL10.GL_LINES, 0, 2);//绘制y轴
//        }
//
//        public void onSurfaceChanged(GL10 gl, int width, int height)
//        {
//            gl.glViewport(0, 0, width, height);
//
//            //矩阵模式,投影矩阵,openGL基于状态机
//            gl.glMatrixMode(GL10.GL_PROJECTION);
//            //加载单位矩阵
//            gl.glLoadIdentity();
//            //平截头体(最后一个f表示他是浮点数的类型)
//            // gl.glFrustumf(-0.5f, 0.5f, -0.5f, 0.5f, 2f, 10);
//            gl.glOrthof(-2f, 2f, -2f, 2f, 2f, 10);
//        }
//    }
}

class TCP_sendMove
{
    public void goFront()
    {
        try
        {
            Socket goFront_Socket = new Socket();
            goFront_Socket.connect(new InetSocketAddress("192.168.10.135", 5001));

            DataOutputStream DOS = new DataOutputStream(goFront_Socket.getOutputStream());
            String goFrontStr = "qj";
            DOS.write(goFrontStr.getBytes());
            DOS.flush();
            DOS.close();

            goFront_Socket.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void goRear()
    {
        try
        {
            Socket goRear_Socket = new Socket();
            goRear_Socket.connect(new InetSocketAddress("192.168.10.135", 5001));

            DataOutputStream DOS = new DataOutputStream(goRear_Socket.getOutputStream());
            String goRearStr = "ht";
            DOS.write(goRearStr.getBytes());
            DOS.flush();
            DOS.close();

            goRear_Socket.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void goLeft()
    {
        try
        {
            Socket goLeft_Socket = new Socket();
            goLeft_Socket.connect(new InetSocketAddress("192.168.10.135", 5001));

            DataOutputStream DOS = new DataOutputStream(goLeft_Socket.getOutputStream());
            String goLeftStr = "left";
            DOS.write(goLeftStr.getBytes());
            DOS.flush();
            DOS.close();

            goLeft_Socket.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void goRight()
    {
        try
        {
            Socket goRight_Socket = new Socket();
            goRight_Socket.connect(new InetSocketAddress("192.168.10.135", 5001));

            DataOutputStream DOS = new DataOutputStream(goRight_Socket.getOutputStream());
            String goRightStr = "right";
            DOS.write(goRightStr.getBytes());
            DOS.flush();
            DOS.close();

            goRight_Socket.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void goClockwise()
    {
        try
        {
            Socket goClockwise_Socket = new Socket();
            goClockwise_Socket.connect(new InetSocketAddress("192.168.10.135", 5001));

            DataOutputStream DOS = new DataOutputStream(goClockwise_Socket.getOutputStream());
            String goClockwiseStr = "ssz";
            DOS.write(goClockwiseStr.getBytes());
            DOS.flush();
            DOS.close();

            goClockwise_Socket.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void goAntiClockwise()
    {
        try
        {
            Socket goAntiClockwise_Socket = new Socket();
            goAntiClockwise_Socket.connect(new InetSocketAddress("192.168.10.135", 5001));

            DataOutputStream DOS = new DataOutputStream(goAntiClockwise_Socket.getOutputStream());
            String goAntiClockwiseStr = "nsz";
            DOS.write(goAntiClockwiseStr.getBytes());
            DOS.flush();
            DOS.close();

            goAntiClockwise_Socket.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void stopMove()
    {
        try
        {
            Socket stopMove_Socket = new Socket();
            stopMove_Socket.connect(new InetSocketAddress("192.168.10.135", 5001));

            DataOutputStream DOS = new DataOutputStream(stopMove_Socket.getOutputStream());
            String stopMoveStr = "tz";
            DOS.write(stopMoveStr.getBytes());
            DOS.flush();
            DOS.close();

            stopMove_Socket.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    // Listen Sound
    public void listenSound()
    {
        try
        {
            Socket listenSound_Socket = new Socket();
            listenSound_Socket.connect(new InetSocketAddress("192.168.10.135", 5001));

            DataOutputStream DOS = new DataOutputStream(listenSound_Socket.getOutputStream());
            String listenSoundStr = "dw";
            DOS.write(listenSoundStr.getBytes());
            DOS.flush();
            DOS.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    // Stop Listen Sound
    public void stopListenSound()
    {
        try
        {
            Socket stopListenSound_Socket = new Socket();
            stopListenSound_Socket.connect(new InetSocketAddress("192.168.10.135", 5001));

            DataOutputStream DOS = new DataOutputStream(stopListenSound_Socket.getOutputStream());
            String stopListenSoundStr = "bdw";
            DOS.write(stopListenSoundStr.getBytes());
            DOS.flush();
            DOS.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
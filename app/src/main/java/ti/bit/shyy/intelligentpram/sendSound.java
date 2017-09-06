package ti.bit.shyy.intelligentpram;

import android.content.Context;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import android.content.res.Resources;

/**
 * Created by ShinoharaYuyoru on 2017/06/18.
 */

public class sendSound extends AppCompatActivity
{
    public Button Button_startSend;
    public Button Button_returnMain2;
//    public Button Button_volumeUp;
//    public Button Button_volumeDown;
    public TextView TextV_sendSound;
    private UDP_SendSound UDPst;
    public boolean checkStop = false;

    Thread musicThreadTemp;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sendsound);

        checkStop = false;
        TextV_sendSound = (TextView)findViewById(R.id.sendSoundText);

        // Start Send Sound
        Button_startSend = (Button)findViewById(R.id.startSendButton);
        Button_startSend.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View pramStateV)
            {
                sendSound.this.TextV_sendSound.setText("正在发送音乐到婴儿车中...");

                 musicThreadTemp = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        UDPst = new UDP_SendSound();
                        UDPst.run(Button_startSend.getContext());
                    }
                });
                musicThreadTemp.start();
                Button_startSend.setEnabled(false);
            }
        });

//        Button_volumeUp = (Button) findViewById(R.id.volumeUpButton);
//        Button_volumeUp.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                try
//                {
//                    Socket goFront_Socket = new Socket();
//                    goFront_Socket.connect(new InetSocketAddress("192.168.10.135", 5001));      // NEED CHANGE
//
//                    DataOutputStream DOS = new DataOutputStream(goFront_Socket.getOutputStream());
//                    String goFrontStr = "volume+";
//                    DOS.write(goFrontStr.getBytes());
//                    DOS.flush();
//                    DOS.close();
//
//                    goFront_Socket.close();
//                }
//                catch(Exception e)
//                {
//                    e.printStackTrace();
//                }
//            }
//        });

//        Button_volumeDown = (Button) findViewById(R.id.volumeDownButton);
//        Button_volumeDown.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                try
//                {
//                    Socket goFront_Socket = new Socket();
//                    goFront_Socket.connect(new InetSocketAddress("192.168.10.135", 5001));      // NEED CHANGE
//
//                    DataOutputStream DOS = new DataOutputStream(goFront_Socket.getOutputStream());
//                    String goFrontStr = "volume-";
//                    DOS.write(goFrontStr.getBytes());
//                    DOS.flush();
//                    DOS.close();
//
//                    goFront_Socket.close();
//                }
//                catch(Exception e)
//                {
//                    e.printStackTrace();
//                }
//            }
//        });

        // Return Main2
        Button_returnMain2 = (Button)findViewById(R.id.returnMainButton2);
        Button_returnMain2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View pramStateV)
            {
                checkStop = true;
                try
                {
                    musicThreadTemp.wait();
                    musicThreadTemp.interrupt();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                finish();
            }
        });
    }

    class UDP_SendSound extends Thread
    {
        public void run(Context context)
        {
            try
            {
                Resources resources = context.getResources();
                InputStream soundIS = resources.openRawResource(R.raw.xiyangyang);

                DatagramSocket DS = new DatagramSocket();

                byte[] buffer = new byte[1024];  // The music sounds slowly, but now don't know the resolusion
                int len;
                DatagramPacket DP = null;

                while((len = soundIS.read(buffer))!=-1 && !checkStop)
                {
                    DP = new DatagramPacket(buffer, len, InetAddress.getByName("192.168.10.222"), 5050);
                    DS.send(DP);

                    SystemClock.sleep(14);  // This waiting time needs fitting
                }

                DS.close();
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}


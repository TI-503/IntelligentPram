package ti.bit.shyy.intelligentpram;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.content.Intent;

public class MainActivity extends AppCompatActivity
{
    public Button Button_pramState;
    public Button Button_sendSound;
    public Button Button_exitApp;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Jump to pramState
        Button_pramState = (Button)findViewById(R.id.pramStateButton);
        Button_pramState.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View pramStateV)
            {
                Intent intentpramStateV = new Intent(getApplicationContext(), pramState.class);
                startActivity(intentpramStateV);

                onStop();
            }
        });

        // Jump to sendSound
        Button_sendSound = (Button)findViewById(R.id.sendSoundButton);
        Button_sendSound.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View sendSoundV)
            {
                Intent intentsendSoundV = new Intent(getApplicationContext(), sendSound.class);
                startActivity(intentsendSoundV);

                onStop();
            }
        });

        // Exit App1
        Button_exitApp = (Button)findViewById(R.id.exitButton);
        Button_exitApp.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View MainActivity)
            {
                finish();
            }
        });
    }
}
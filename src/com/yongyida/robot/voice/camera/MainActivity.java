package com.yongyida.robot.voice.camera;

import com.yongyida.robot.voice.camera.R;
import com.yongyida.robot.voice.camera.autophoto.CameraActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends Activity {

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

/*		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(500);
					finish();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();*/
        Intent intent=new Intent(this, CameraActivity.class);
        startActivity(intent);
        finish();
	}

}

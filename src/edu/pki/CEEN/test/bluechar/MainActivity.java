package edu.pki.CEEN.test.bluechar;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	protected static final String TAG = "BluMote";
	private Button LeftDwnBtn;
	private Button LeftUpBtn;
	private TextView cStatus;
	private BluetoothAdapter mAdapter;
	private BluetoothSocket mSocket;
	private BluetoothDevice mDevice;
	private OutputStream outStream;
	private String macAddr = "20:13:01:22:12:07";
	private Button RightUpBtn;
	private Button RightDwnBtn;
	private Button hornBtn;
	private byte dataSet[];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		dataSet = new byte[5];

		LeftDwnBtn = (Button) findViewById(R.id.LeftDownBtn);
		LeftUpBtn = (Button) findViewById(R.id.LeftUpBtn);
		RightUpBtn = (Button) findViewById(R.id.RightUpBtn);
		RightDwnBtn = (Button) findViewById(R.id.RightDwnBtn);
		hornBtn = (Button) findViewById(R.id.hornBtn);

		cStatus = (TextView) findViewById(R.id.ConnectionStatus);
		mAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mAdapter == null) {
			Toast.makeText(getApplicationContext(), "Bluetooth not available.",
					Toast.LENGTH_LONG).show();
		}
		if (!mAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, 1);
		}

		LeftDwnBtn.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					dataSet[3] = (byte) 0xFF;
					return true;
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					dataSet[3] = 0x00;
					return true;
				}
				return false;
			}
		});

		hornBtn.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					dataSet[0] |= (byte) 0x01;
					return true;
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					dataSet[0] &= ~(0x01);
					return true;
				}
				return false;
			}
		});

		LeftUpBtn.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					dataSet[3] = (byte) 0x7F;
					return true;
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					dataSet[3] = 0x00;
					return true;
				}
				return false;
			}
		});

		RightUpBtn.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					dataSet[1] = (byte) 0x7F;
					return true;
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					dataSet[1] = 0x00;
					return true;
				}
				return false;
			}
		});

		RightDwnBtn.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					dataSet[1] = (byte) 0xFF;
					return true;
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					dataSet[1] = 0x00;
					return true;
				}
				return false;
			}
		});
	}

	protected void onResume() {
		super.onResume();
		Runnable btControl;
		mDevice = mAdapter.getRemoteDevice(macAddr);
		mAdapter.cancelDiscovery();
		try {
			mSocket = mDevice.createInsecureRfcommSocketToServiceRecord(UUID
					.fromString("00001101-0000-1000-8000-00805F9B34FB"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			mSocket.connect();
			outStream = mSocket.getOutputStream();
			cStatus.setText("Connected");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		btControl = new Runnable(){
			@Override
			public void run()
			{
				byte prevData[] = new byte[5];
				while(true)
				{
					if(prevData != dataSet)
					{
						try {
							outStream.write(dataSet);
							Thread.sleep(10);
						} catch (IOException e) {
							Log.d(TAG, "ERROR within BTControl Thread.");
							Log.d(TAG, "Exception: " + e.toString());
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							Log.d(TAG, "BT Sleep interrupted, ignoring.");
						}
						prevData = dataSet;
					}
				}
			}
		};
		new Thread(btControl).start();
	}

	protected void onPause() {
		super.onPause();
		if (outStream != null) {
			try {
				outStream.flush();
				mSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}

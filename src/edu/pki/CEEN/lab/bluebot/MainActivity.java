package edu.pki.CEEN.lab.bluebot;

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
import edu.pki.CEEN.lab.bluebot.R;

public class MainActivity extends Activity {

	protected static final String TAG = "BluBoT";
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
	private Thread btControl;
	private boolean killBtControl = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Runnable btControlRunnable;

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
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					dataSet[3] = 0x00;
				}
				return false;
			}
		});

		hornBtn.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					dataSet[0] |= (byte) 0x01;
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					dataSet[0] &= ~(0x01);
				}
				return false;
			}
		});

		LeftUpBtn.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					dataSet[3] = (byte) 0x7F;
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					dataSet[3] = 0x00;
				}
				return false;
			}
		});

		RightUpBtn.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					dataSet[1] = (byte) 0x7F;
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					dataSet[1] = 0x00;
				}
				return false;
			}
		});

		RightDwnBtn.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					dataSet[1] = (byte) 0xFF;
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					dataSet[1] = 0x00;
				}
				return false;
			}
		});
		
		// Populate Runnable for thread creation.
		btControlRunnable = new Runnable(){
			@Override
			public void run()
			{
				boolean newData = false;
				byte prevData[] = new byte[5];
				while(true)
				{
					if(killBtControl == false)
					{
						for(int i = 0;i < dataSet.length;i++)
							if(dataSet[i] != prevData[i])
								newData = true;
						if(newData)
						{
							Log.d(TAG, "New data... trying to transmit.");
							try {
								if(outStream != null)
								{
									outStream.write(dataSet);
								}
								Thread.sleep(10);
							} catch (IOException e) {
								Log.d(TAG, "ERROR within BTControl Thread.");
								Log.d(TAG, "Exception: " + e.toString());
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								Log.d(TAG, "BT Sleep interrupted, ignoring.");
							} catch (Exception e)
							{
								Log.e(TAG, "Generic exception caught...");
								e.printStackTrace();
							}
							for(int i = 0;i<dataSet.length;i++)
								prevData[i] = dataSet[i];
							newData = false;
						}
					} else {
						Log.i(TAG, "Killing BTControl Thread.");
						return;			//Kills the thread...
					}
				}
			}
		};
		
		//Create thread, but do not start it here. 
		//Will prevent multiple threads from being 
		//created and wasted resources.
		btControl = new Thread(btControlRunnable ,"BTControl");
		
		//Set buttons clickable, not done when onTouchListener is used.
		RightDwnBtn.setClickable(true);
		LeftDwnBtn.setClickable(true);
		RightUpBtn.setClickable(true);
		LeftUpBtn.setClickable(true);
		hornBtn.setClickable(true);
	}

	protected void onResume() {
		super.onResume();
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
			//Do nothing, just assume the bot isn't available at this time.
		}
		//Start previously created thread.
		try{
			btControl.start();
		} catch (Exception e) {}
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
			
			//	Stop the thread from wasting battery and keeping resources we don't need.
			killBtControl = true;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
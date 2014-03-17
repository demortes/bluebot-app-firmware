package edu.pki.CEEN.lab.bluebot;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import com.MobileAnarchy.Android.Widgets.Joystick.JoystickMovedListener;
import com.MobileAnarchy.Android.Widgets.Joystick.JoystickView;

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

	protected static final String TAG = "BLUBoT";
	protected static final boolean DEBUG = false;
	private TextView cStatus;
	private BluetoothAdapter mAdapter;
	private BluetoothSocket mSocket;
	private BluetoothDevice mDevice;
	private OutputStream outStream;
	private String macAddr = "20:13:01:22:12:07";
	private Button xBtn, l1Btn, l2Btn, r1Btn, r2Btn, circleBtn, squareBtn,
			triangleBtn;
	private byte dataSet[];
	private Thread btControl;
	private boolean killBtControl = false;
	private InputStream inStream;
	JoystickView joyStick;
	TextView xView;
	TextView yView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.joystick);

		Runnable btControlRunnable;

		dataSet = new byte[6];
		dataSet[0] = (byte) 0xCA;

		xBtn = (Button) findViewById(R.id.xBtn);
		l1Btn = (Button) findViewById(R.id.L1Btn);
		l2Btn = (Button) findViewById(R.id.L2Btn);
		r1Btn = (Button) findViewById(R.id.r1Btn);
		r2Btn = (Button) findViewById(R.id.R2Btn);
		squareBtn = (Button) findViewById(R.id.squareBtn);
		circleBtn = (Button) findViewById(R.id.circleBtn);
		triangleBtn = (Button) findViewById(R.id.triangleBtn);

		joyStick = (JoystickView) findViewById(R.id.joystickView);
		xView = (TextView) findViewById(R.id.TextViewX);
		yView = (TextView) findViewById(R.id.TextViewY);

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

		// TODO: Joy stick setup

		xBtn.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					dataSet[1] |= (byte) (1 << 6);
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					dataSet[1] &= ~(1 << 6);
				}
				if (DEBUG)
					Log.d(TAG, String.format("Button Byte: 0x%02x", dataSet[1]));
				return false;
			}
		});

		circleBtn.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					dataSet[1] |= (byte) (1 << 5);
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					dataSet[1] &= ~(1 << 5);
				}
				if (DEBUG)
					Log.d(TAG, String.format("Button Byte: 0x%02x", dataSet[1]));
				return false;
			}
		});

		squareBtn.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					dataSet[1] |= (byte) (1 << 7);
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					dataSet[1] &= ~(1 << 7);
				}
				if (DEBUG)
					Log.d(TAG, String.format("Button Byte: 0x%02x", dataSet[1]));
				return false;
			}
		});

		triangleBtn.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					dataSet[1] |= (byte) (1 << 4);
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					dataSet[1] &= ~(1 << 4);
				}
				if (DEBUG)
					Log.d(TAG, String.format("Button Byte: 0x%02x", dataSet[1]));
				return false;
			}
		});
		r1Btn.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					dataSet[1] |= (byte) (1 << 3);
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					dataSet[1] &= ~(1 << 3);
				}
				if (DEBUG)
					Log.d(TAG, String.format("Button Byte: 0x%02x", dataSet[1]));
				return false;
			}
		});

		l1Btn.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					dataSet[1] |= (byte) (1 << 2);
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					dataSet[1] &= ~(1 << 2);
				}
				if (DEBUG)
					Log.d(TAG, String.format("Button Byte: 0x%02x", dataSet[1]));
				return false;
			}
		});
		r2Btn.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					dataSet[1] |= (byte) (1 << 1);
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					dataSet[1] &= ~(1 << 1);
				}
				if (DEBUG)
					Log.d(TAG, String.format("Button Byte: 0x%02x", dataSet[1]));
				return false;
			}
		});
		l2Btn.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					dataSet[1] |= (byte) (1 << 0);
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					dataSet[1] &= ~(1 << 0);
				}
				if (DEBUG)
					Log.d(TAG, String.format("Button Byte: 0x%02x", dataSet[1]));
				return false;
			}
		});

		joyStick.setOnJoystickMovedListener(new JoystickMovedListener() {

			@Override
			public void OnMoved(int x, int y) {

				dataSet[2] = dataSet[4] = (byte) (-12.7 * y);
				dataSet[3] = dataSet[5] = (byte) (12.7 * x);
				xView.setText(String.format("%d", x));
				yView.setText(String.format("%d", y));
			}

			@Override
			public void OnReleased() {
				dataSet[2] = dataSet[3] = dataSet[4] = dataSet[5] = (byte) 0x00;
			}

			@Override
			public void OnReturnedToCenter() {
				dataSet[2] = dataSet[3] = dataSet[4] = dataSet[5] = (byte) 0x00;
			}
		});

		// Populate Runnable for thread creation.
		btControlRunnable = new Runnable() {
			@Override
			public void run() {
				boolean newData = false; // Assume first run it's true.
				byte prevData[] = new byte[6];
				while (true) {
					if (killBtControl == false) {
						for (int i = 0; i < dataSet.length; i++)
							if (dataSet[i] != prevData[i])
								newData = true;
						if (newData)
							if (newData) {
								if (DEBUG) {
									Log.d(TAG,
											"New data... trying to transmit.");
									Log.d(TAG,
											String.format(
													"0x%02x 0x%02x 0x%02x 0x%02x 0x%02x 0x%02x",
													dataSet[0], dataSet[1],
													dataSet[2], dataSet[3],
													dataSet[4], dataSet[5]));
								}
								try {
									if (outStream != null) {
										int tmpCnt = 0;
										for (int j = 0; j < dataSet.length; j++) {
											outStream
													.write(prevData[j] = dataSet[j]);
											while (inStream.read() != 0xAC) {
												tmpCnt++;
												if (tmpCnt >= 200) {
													Log.e(TAG,
															"Timeout of BT communication occured. No ACK");
													break;
												}
											}
											tmpCnt = 0;
										}
									}
									Thread.sleep(10);
								} catch (IOException e) {
									Log.d(TAG, "ERROR within BTControl Thread.");
									Log.d(TAG, "Exception: " + e.toString());
								} catch (Exception e) {
									Log.e(TAG, "Generic exception caught...");
									e.printStackTrace();
								}
								newData = false;
							}
					} else {
						Log.i(TAG, "Killing BTControl Thread.");
						return; // Kills the thread...
					}
				}
			}
		};
		// Create thread, but do not start it here.
		// Will prevent multiple threads from being
		// created and wasted resources.
		btControl = new Thread(btControlRunnable, "BTControl");

		// Set buttons clickable, not done when onTouchListener is used.
		xBtn.setClickable(true);
		r1Btn.setClickable(true);
		r2Btn.setClickable(true);
		l1Btn.setClickable(true);
		l2Btn.setClickable(true);
		squareBtn.setClickable(true);
		circleBtn.setClickable(true);
		triangleBtn.setClickable(true);
	}

	protected void onResume() {
		super.onResume();
		if (mSocket == null) {
			mDevice = mAdapter.getRemoteDevice(macAddr);
			mAdapter.cancelDiscovery();
			try {
				mSocket = mDevice
						.createInsecureRfcommSocketToServiceRecord(UUID
								.fromString("00001101-0000-1000-8000-00805F9B34FB"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			mSocket.connect();
			outStream = mSocket.getOutputStream();
			inStream = mSocket.getInputStream();
			cStatus.setText("Connected");
		} catch (Exception e) {
			// Do nothing, just assume the bot isn't available at this time.
		}
		// Start previously created thread.
		try {
			btControl.start();
		} catch (Exception e) {
		}

		if (joyStick.getUserCoordinateSystem() != JoystickView.COORDINATE_CARTESIAN)
			joyStick.setUserCoordinateSystem(JoystickView.COORDINATE_CARTESIAN);
	}

	protected void onPause() {
		super.onPause();
		if (outStream != null) {
			try {
				outStream = null;
				inStream = null;
				mSocket.close();
				cStatus.setText("Disconnected");
			} catch (IOException e) {
				e.printStackTrace();
			}

			// Stop the thread from wasting battery and keeping resources we
			// don't need.
			killBtControl = true;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void resetJoyData() {
		dataSet[2] = 0;
		dataSet[3] = 0;
		dataSet[4] = 0;
		dataSet[5] = 0;

	}
}

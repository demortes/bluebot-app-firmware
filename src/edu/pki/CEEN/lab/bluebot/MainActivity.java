/*
 * Copyright (C) 2014 The Board of Regents of the University of Nebraska.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import edu.pki.CEEN.lab.bluebot.R;

public class MainActivity extends Activity {

	protected static final String TAG = "MainActivity";
	protected static final boolean DEBUG = true;
	private TextView cStatus;
	private BluetoothAdapter mAdapter;
	private BluetoothSocket mSocket;
	private BluetoothDevice mDevice;
	private OutputStream outStream;
	private String macAddr = null;
	private Button xBtn, l1Btn, l2Btn, r1Btn, r2Btn, circleBtn, squareBtn,
			triangleBtn;
	private byte dataSet[];
	private Thread btControl;
	private boolean killBtControl = false;
	private InputStream inStream;
	private JoystickView joyStickL;
	private JoystickView joyStickR;

	Runnable btControlRunnable;
	private btChange broadcastRec;

	private class btChange extends BroadcastReceiver {
		MainActivity main = null;

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
				// Device has disconnected
				main.setConnectionString("Disconnected");
				Toast.makeText(main, "Robot was disconnected!",
						Toast.LENGTH_LONG).show();
			}
		}

		public void setMain(MainActivity main) {
			this.main = main;
		}

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// ignore orientation/keyboard change
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.joystick);

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

		joyStickL = (JoystickView) findViewById(R.id.JoystickViewL);
		joyStickR = (JoystickView) findViewById(R.id.joystickViewR);
		joyStickL.setMovementRange(127);
		joyStickR.setMovementRange(127);

		cStatus = (TextView) findViewById(R.id.ConnectionStatus);
		mAdapter = BluetoothAdapter.getDefaultAdapter();

		cStatus.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (macAddr != null && cStatus.getText() == "Disconnected") {
					mSocket = null;
					connectBT();
				}
			}

		});
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

		joyStickL.setOnJoystickMovedListener(new JoystickMovedListener() {

			@Override
			public void OnMoved(int x, int y) {

				dataSet[4] = (byte) -y;
				dataSet[5] = (byte) x;
			}

			@Override
			public void OnReleased() {
				dataSet[5] = dataSet[4] = (byte) 0x00;
			}

			@Override
			public void OnReturnedToCenter() {
				dataSet[5] = dataSet[4] = (byte) 0x00;
			}
		});

		joyStickR.setOnJoystickMovedListener(new JoystickMovedListener() {

			@Override
			public void OnMoved(int x, int y) {

				dataSet[2] = (byte) -y;
				dataSet[3] = (byte) x;
			}

			@Override
			public void OnReleased() {
				dataSet[3] = dataSet[2] = (byte) 0x00;
			}

			@Override
			public void OnReturnedToCenter() {
				dataSet[3] = dataSet[2] = (byte) 0x00;
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
									if (outStream != null && inStream != null) {
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
		cStatus.setClickable(true);
	}

	protected void onResume() {
		super.onResume();
		killBtControl = false;
		if (mAdapter == null) {
			Toast.makeText(getApplicationContext(), "Bluetooth not available.",
					Toast.LENGTH_LONG).show();
		}
		if (!mAdapter.isEnabled() || mAdapter.isDiscovering()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, 1);
		}
		if (macAddr == null) {
			Intent deviceIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(deviceIntent, 1);
		} else
			connectBT();

		if (joyStickL.getUserCoordinateSystem() != JoystickView.COORDINATE_CARTESIAN)
			joyStickL
					.setUserCoordinateSystem(JoystickView.COORDINATE_CARTESIAN);
	}

	protected void onPause() {
		super.onPause();
		if (outStream != null) {
			try {
				if (mSocket != null) {
					mSocket.close();
					mSocket = null;
				}
			} catch (IOException e) {
			}
			cStatus.setText("Disconnected");
			unregisterReceiver(broadcastRec);
		}
		// Stop the thread from wasting battery and keeping resources we
		// don't need.
		killBtControl = true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void setConnectionString(String text) {
		cStatus.setText(text);
	}

	public void resetJoyData() {
		dataSet[2] = 0;
		dataSet[3] = 0;
		dataSet[4] = 0;
		dataSet[5] = 0;

	}

	protected void onActivityResult(int request, int result, Intent data) {
		if (request == 1) {
			if (result == RESULT_OK) {
				macAddr = data.getStringExtra("device_address");
				connectBT();
			}
		}
	}

	private void connectBT() {
		if (broadcastRec == null) {
			broadcastRec = new btChange();
			broadcastRec.setMain(this);
		}

		if (mSocket == null && macAddr != null) {
			if (mSocket != null) {
				try {
					mSocket.close();
					mSocket = null;
				} catch (Exception e) {

				}
			}
			mDevice = mAdapter.getRemoteDevice(macAddr);
			mAdapter.cancelDiscovery();
			if (DEBUG)
				Log.d(TAG, "Attempting to connect to MAC: " + macAddr);
			try {
				mSocket = mDevice
						.createInsecureRfcommSocketToServiceRecord(UUID
								.fromString("00001101-0000-1000-8000-00805F9B34FB"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		try {
			mSocket.connect();
			outStream = mSocket.getOutputStream();
			inStream = mSocket.getInputStream();
			cStatus.setText("Connected");
			if(!btControl.isAlive())
				btControl.start();
		} catch (IOException e) {
			// Connection failed at this point, close the socket and move
			// on.
			try {
				if (mSocket != null) {
					mSocket.close();
					mSocket = null;
				}
				cStatus.setText("Disconnected");
			} catch (IOException e1) {
			}
		}
		registerReceiver(broadcastRec, new IntentFilter(
				BluetoothDevice.ACTION_ACL_DISCONNECTED));
	}
	}
}

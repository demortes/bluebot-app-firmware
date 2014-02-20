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
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private Button dwnBtn;
	private Button upBtn;
	private TextView cStatus;
	private BluetoothAdapter mAdapter;
	private BluetoothSocket mSocket;
	private BluetoothDevice mDevice;
	private OutputStream outStream;
	private String macAddr = "20:13:01:22:12:07";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		dwnBtn = (Button) findViewById(R.id.DwnBtn);
		upBtn = (Button) findViewById(R.id.UpBtn);
		cStatus = (TextView) findViewById(R.id.ConnectionStatus);
		mAdapter = BluetoothAdapter.getDefaultAdapter();
		if(mAdapter == null)
		{
			Toast.makeText(getApplicationContext(), "Bluetooth not available.", Toast.LENGTH_LONG).show();
		}
		if(!mAdapter.isEnabled())
		{
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    startActivityForResult(enableBtIntent, 1);
		}
		
		dwnBtn.setOnClickListener(
				new View.OnClickListener(){
					public void onClick(View tmp)				
					{
						try {
							outStream.write('D');
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
		);
		
		upBtn.setOnClickListener(
				new View.OnClickListener()
				{
					public void onClick(View tmp)
					{
						try {
							outStream.write('U');
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
	}
	
	protected void onResume()
	{
		super.onResume();
		mDevice = mAdapter.getRemoteDevice(macAddr);
		mAdapter.cancelDiscovery();
		try{
			mSocket = mDevice.createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try{
			mSocket.connect();
			outStream = mSocket.getOutputStream();
			cStatus.setText("Connected");
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	protected void onPause()
	{
		super.onPause();
		if(outStream != null)
		{
			try{
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

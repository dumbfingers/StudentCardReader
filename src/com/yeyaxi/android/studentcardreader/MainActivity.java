package com.yeyaxi.android.studentcardreader;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends Activity {

	private static final String TAG = MainActivity.class.getSimpleName();
	private NfcAdapter mAdapter;
	private IntentFilter[] intentFiltersArray;
	private String[][] techListsArray;
	private PendingIntent pendingIntent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		pendingIntent = PendingIntent.getActivity(
			    this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		mAdapter = NfcAdapter.getDefaultAdapter(this);
		
		IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        
        try {
            ndef.addDataType("*/*");
        } catch (MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }
        intentFiltersArray = new IntentFilter[] {ndef, };

        // Setup a tech list for all NfcF tags
        techListsArray = new String[][] { new String[] { MifareClassic.class.getName() } };
        
//        Intent intent = getIntent();
	}
	
	@Override
	public void onNewIntent(Intent intent) {
//	    Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
	    //do something with tagFromIntent
		getCardInfo(intent);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mAdapter.disableForegroundDispatch(this);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void getCardInfo(Intent intent) {
		if (intent.getAction().equals(NfcAdapter.ACTION_TECH_DISCOVERED)) {
			Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			MifareClassic mifareClassic = MifareClassic.get(tagFromIntent);
			byte[] data;
			
			try {
				mifareClassic.connect();
				boolean auth = false;
				String cardData = null;
				int secCount = mifareClassic.getSectorCount();
				int bCount = 0;
				int bIndex = 0;
				for (int j = 0; j < secCount; j ++) {
					auth = mifareClassic.authenticateSectorWithKeyA(j, MifareClassic.KEY_DEFAULT);
					if (auth) {
						bCount = mifareClassic.getBlockCountInSector(j);
						bIndex = 0;
						for (int i = 0; i < bCount; i ++) {
							bIndex = mifareClassic.sectorToBlock(j);
							data = mifareClassic.readBlock(bIndex);
							cardData = getHexString(data);
							Log.i(TAG, cardData);
	                        bIndex++;
						}
					}
				}
			} catch (Exception exception) {
//				Log.e(TAG, "Error: " + exception.getLocalizedMessage());
				exception.printStackTrace();
			}
		}
	}
	
	private static String getHexString(byte[] bytes) {
		char[] hexArray = "0123456789ABCDEF".toCharArray();
	    char[] hexChars = new char[bytes.length * 2];
	    int v;
	    for ( int j = 0; j < bytes.length; j++ ) {
	        v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
}

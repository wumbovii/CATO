package com.cato;


import android.app.Activity;
import android.app.AlertDialog;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.ViewGroup;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.media.MediaRecorder;
import android.media.MediaPlayer;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.alexd.jsonrpc.JSONRPCClient;
import org.alexd.jsonrpc.JSONRPCException;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;


/*
 * Class is its own tab that is has several capabilities:
 * Record/Stop Audio
 * Send sound bite to server for analysis
 */
public class RecordActivity extends Activity {
	
    private static final String LOG_TAG = "AudioRecordTest";
  	private static String mFileName = null;  	
  	private static boolean mFileRecordedFlag = false;
  	
  	private RecordButton mRecordButton = null;
  	private MediaRecorder mRecorder = null;
  	
  	private SendButton mSendButton = null;
  	
  	static double lon, lat;
  	
  	/* Recording methods */
  	private void onRecord(boolean start) {
  		if(start) {
  			startRecording();
  		} else {
  			stopRecording();
  		}
  	}
  	
  	private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mRecorder.setMaxDuration(10000);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
        mFileRecordedFlag = true;
    }
    
  	class RecordButton extends Button {
        boolean mStartRecording = true;

		OnClickListener clicker = new OnClickListener() {
			public void onClick(View v) {
				onRecord(mStartRecording);
				if (mStartRecording) {
					setText("Stop recording");
				} else {
					setText("Start recording");
				}
				mStartRecording = !mStartRecording;
			}
		};

		public RecordButton(Context ctx) {
			super(ctx);
			setText("Start recording");
			setOnClickListener(clicker);
		}
    }
  	

  	
  	/* Send methods */
  	private AlertDialog.Builder audio_status = null;
 	private void onSend() {
 		audio_status = new AlertDialog.Builder(this);
 		SendAudioTask sender = new SendAudioTask();
 		sender.execute();
  	}

 	
  	private class SendAudioTask extends AsyncTask<URL, Integer, String> {
		@Override
		protected String doInBackground(URL... params) {
			HttpClient httpclient = new DefaultHttpClient();
		    HttpPost httppost = new HttpPost("http://glowing-mountain-5425.herokuapp.com/search/tag");
		    
		    String result = "";
		    
		    try {
		        // Add your data
		        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		        nameValuePairs.add(new BasicNameValuePair("name", "Dmitry"));
		        //nameValuePairs.add(new BasicNameValuePair("stringdata", "AndDev is Cool!"));
		        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

		        // Execute HTTP Post Request
		        HttpResponse response = httpclient.execute(httppost);
		        
		        StatusLine statusLine = response.getStatusLine();
		        if(statusLine.getStatusCode() == HttpStatus.SC_OK){
		            ByteArrayOutputStream out = new ByteArrayOutputStream();
		            response.getEntity().writeTo(out);
		            out.close();
		            result += out.toString();
		            
		        } else{
		            
		            response.getEntity().getContent().close();
		            throw new IOException(statusLine.getReasonPhrase());
		        }
		        
		        System.out.println(result);
		        
		    } catch (ClientProtocolException e) {
		        e.printStackTrace();
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
 			//JSONRPCClient client = JSONRPCClient.create("http://:5000");
			//JSONRPCClient client = JSONRPCClient.create("http://glowing-mountain-5425.herokuapp.com/search/");
			//client.setConnectionTimeout(2000);
 			//client.setSoTimeout(2000);
 			
 			
// 			try {
// 				client.callJSONObject("tag");
// 				//client.call("tag");
//				//result = client.callString("tag");
// 			}
// 			catch (JSONRPCException e) {
// 				e.printStackTrace();
//				result = "Cannot connect to server...";
// 			}
//			
			return result;
		}
  		
		protected void onPostExecute(String result) {
			  audio_status.setMessage(result)
				.setCancelable(false)
				.setTitle("Tagging complete!")
				.setPositiveButton("Done",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.dismiss();
							}
						});
			  final AlertDialog alert = audio_status.create();
			  alert.show(); 		
		}
  	}
  	
	private void sendAudio() {
		
	}


	private void sendNoFile() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Please record a sound bite")
				.setCancelable(false)
				.setTitle("No File Recorded")
				.setPositiveButton("Done",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.dismiss();
							}
						});
		final AlertDialog alert = builder.create();
		alert.show();
  	}
  	
  	class SendButton extends Button {
        
		OnClickListener clicker = new OnClickListener() {
			public void onClick(View v) {
				onSend();
			}
		};

		public SendButton(Context ctx) {
			super(ctx);
			setText("Send Sound Bite");
			setOnClickListener(clicker);
		}
    }
  	
  	
  	/* Constructor and onCreate */
  	public RecordActivity() {
  		
  		mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
  		mFileName = mFileName + "/recordfile.mp4";
  	}

  	private class GPS {
  		private LocationManager lm;
  		private LocationListener locationListener;
  		
  		public GPS(){
  			lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
  			locationListener = new MyLocationListener();
  			lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 600000, 2000,
  					locationListener);
  		}
  		
  	}
  	
  	private class MyLocationListener implements LocationListener {
		
		public void onLocationChanged(Location loc) {
			if (loc != null) {
				RecordActivity.lat = loc.getLatitude();
				RecordActivity.lon = loc.getLongitude();
			}
		}

		public void onProviderDisabled(String provider) {
			Toast.makeText( getApplicationContext(), "Gps Disabled",
					Toast.LENGTH_SHORT ).show();
		}

		public void onProviderEnabled(String provider) {
			Toast.makeText( getApplicationContext(), "Gps Enabled",
					Toast.LENGTH_SHORT).show();
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			//pass
		}
	}
  		
    public void onCreate(Bundle savedInstanceState) {    	
    	super.onCreate(savedInstanceState);    
    	//GPS
    	
    	LinearLayout ll = new LinearLayout(this);
        mRecordButton = new RecordButton(this);
        mSendButton = new SendButton(this);
        ll.addView(mRecordButton,
            new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0));
        setContentView(ll);
        ll.addView(mSendButton,
                new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    0));
            setContentView(ll);
            

    
    }
}
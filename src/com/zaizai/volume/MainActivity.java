package com.zaizai.volume;


import java.util.Queue;

import java.util.concurrent.LinkedBlockingQueue;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends Activity {

 

	  private static final String TAG = "AudioRecord";
	  static final int SAMPLE_RATE_IN_HZ = 8000;
	  static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
	          AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT);
	  AudioRecord mAudioRecord;
	  boolean isGetVoiceRun;
	  Object mLock;
	

	private TextView textView;
	private Handler handler = new MyHandler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		String s="";
		textView=(TextView)findViewById(R.id.textView);
		mLock = new Object();//1
		getNoiseLevel();//2
		
	
		
 }        
	     
		
		
	
	@Override
	protected void onDestroy () 
	{
		super.onDestroy(); 
        mAudioRecord.stop();
       // mAudioRecord.release();
       // mAudioRecord = null;
  
      
	
	}
	
	
	
	int i=0;
	Queue<String> queue = new LinkedBlockingQueue<String>();
	Bundle receiveBundle;
	  class MyHandler extends Handler{
		  	
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if(msg.what==1)
				{
					receiveBundle= msg.getData();
					Double dtemp=receiveBundle.getDouble("volume");
					String s=Double.toString(dtemp);
					if(dtemp==0.0)
					{
						return;
					}
					
					s+='\n';
					queue.offer(s);
					i++;
					String sall="";
	                //textView.setText(s);
					for(String x :queue)
					{
						sall+=x;
					}
					textView.setText(sall);
	                if(i==35)
	                {
	                	queue.poll();
	                	i--;
	                }
				}
				
				

				}
               
		}
	    
	 
	
	 
	  public void getNoiseLevel() {
	    if (isGetVoiceRun) {
	      Log.e(TAG, "还在录着呢");
	      return;
	    }
	    mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
	        SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_DEFAULT,
	        AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE);
	    if (mAudioRecord == null) {
	      Log.e("sound", "mAudioRecord初始化失败");
	    }
	    isGetVoiceRun = true;
	 
	    

	    new Thread(new Runnable() {
	      @Override
	      public void run() {
	    	  
	    	 Bundle data = new Bundle(); //线程通信，传递数据
	    	  
	    	  
	        mAudioRecord.startRecording();
	        short[] buffer = new short[BUFFER_SIZE];
	        while (isGetVoiceRun) {
	          //r是实际读取的数据长度，一般而言r会小于buffersize
	          int r = mAudioRecord.read(buffer, 0, BUFFER_SIZE);
	          long v = 0;
	          // 将 buffer 内容取出，进行平方和运算
	          for (int i = 0; i < buffer.length; i++) {
	            v += buffer[i] * buffer[i];
	          }
	          // 平方和除以数据总长度，得到音量大小。
	          double mean = v / (double) r;
	          double volume = 10 * Math.log10(mean);
	         
	        	Message msg = handler.obtainMessage();//线程通信，传递数据
	    		msg.what=1;
	    		data.putDouble("volume", volume);; 
	    		msg.setData(data); 
	    		handler.sendMessage(msg);
	    		
	    		

	          
	        
	         // Log.d(TAG, "分贝值:" + volume);
	          
	    		
	          // 大概一秒十次
	          synchronized (mLock) {
	            try {
	              mLock.wait(100);
	            } catch (InterruptedException e) {
	              e.printStackTrace();
	            }
	          }
	        }
	        mAudioRecord.stop();
	        mAudioRecord.release();
	        mAudioRecord = null;
	      }
	    }).start();
	  }
	
	
	}

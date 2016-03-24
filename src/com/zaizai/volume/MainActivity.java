package com.zaizai.volume;


import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

 

	  private static final String TAG = "AudioRecord";
	  static final int SAMPLE_RATE_IN_HZ = 8000;
	  static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
	          AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT);
	  AudioRecord mAudioRecord;
	  boolean isGetVoiceRun;
	  Object mLock;
	

	private CheckBox checkBoxAlarm;
	private TextView textView;
	private Handler handler = new MyHandler();
	
	private MediaPlayer mp;
	boolean delayComplete=false;
	boolean alarmChecked=false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		String s="";
		textView=(TextView)findViewById(R.id.textView);
		checkBoxAlarm=(CheckBox)findViewById(R.id.checkBoxAlarm);
		mLock = new Object();//1
		getNoiseLevel();//2
		
		
checkBoxAlarm.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean checked) {
                // TODO Auto-generated method stub
            	if(checked)
            	{
            		alarmChecked=true;
            	}else
            	{
            		alarmChecked=false;
            	}
            }
        });
		

	
		mp = MediaPlayer.create(this,R.raw.apdisconnect); 

		Timer timer = new Timer();//启动延时
        timer.schedule(new TimerTask() {
            public void run() {
            	delayComplete=true;//设定指定事件
            }
        }, 500);// 设定指定的时间time,此处为500毫秒
        
		
		/* 当MediaPlayer.OnCompletionLister会运行的Listener */
		mp.setOnCompletionListener(  
		          new MediaPlayer.OnCompletionListener()   
		        {   
		          // @Override   
		          public void onCompletion(MediaPlayer arg0)   
		          {   
		           
		        	  //播放完毕后延时1秒
		        	  
		           	  Timer timer = new Timer();
		              timer.schedule(new TimerTask() {
		                  public void run() {
		                  	delayComplete=true;//设定指定事件
		                  }
		              }, 500);// 设定指定的时间time,此处为500毫秒
		     
		              
		              
		        	  
		          }   
		        });   
	
		
 }        
	     
		
		
	
	@Override
	protected void onDestroy () 
	{
		super.onDestroy(); 
        mAudioRecord.stop();
       // mAudioRecord.release();
       // mAudioRecord = null;
        if(mp !=null)  
        {  
         //mp.stop();  //音乐停止播放
         mp.release();//释放资源
        }  
      
	
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
				else if(msg.what==2)
				{
					
					try {       //音乐开始播放
					     if(mp != null)  
					     {  
					      mp.stop();  
					     }      
					     mp.prepare();  
					     mp.start();  
					     delayComplete=false;//设定延时未到，避免因声音短路造成重复播放
					    		 
					    } catch (Exception e) {  
					   
					     e.printStackTrace();  //播放发生异常
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
	          if(alarmChecked&&  delayComplete && volume>50 )
	          {
	        	  Message msg = handler.obtainMessage();//线程通信，传递数据
	  	    		msg.what=2;
	  	    		handler.sendMessage(msg);
	        	  
	          }
	
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

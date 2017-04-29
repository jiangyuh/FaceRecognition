package com.facpp.picturedetect;

import java.io.ByteArrayOutputStream;

import android.app.Activity;
import android.content.Intent;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.*;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.provider.MediaStore.Images.ImageColumns;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facepp.error.FaceppParseException;
import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;
import com.facepp.result.FaceppResult;

import org.json.*;

/**
 * A simple demo, get a picture form your phone<br />
 * Use the facepp api to detect<br />
 * Find all face on the picture, and mark them out.
 * @author moon5ckq
 */
public class MainActivity extends Activity {

	final private static String TAG = "MainActivity";
	final private int PICTURE_CHOOSE = 1;
	
	private ImageView imageView = null;
	private Bitmap img = null;
	private Button buttonDetect = null;
	private Button buttonExhibit = null;
	private TextView textView = null;
	Db db= new Db(this);
	private String fruit=null;
	private double sad=0;
	
	public enum Person 

	{person_0,person_1, person_2,person_3,person_4,
		person_5,person_6,person_7;	 
	public static Person getpersonType(String person){
		 return valueOf(person.toLowerCase());
		 }
	} 	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Button button = (Button)this.findViewById(R.id.button1);
        button.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				//get a picture form your phone
				Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
		        photoPickerIntent.setType("image/*");
		        startActivityForResult(photoPickerIntent, PICTURE_CHOOSE);
			}
		});
        
        textView = (TextView)this.findViewById(R.id.textView1);
        
		buttonDetect = (Button) this.findViewById(R.id.button2);
        buttonDetect.setVisibility(View.INVISIBLE);
        buttonDetect.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				
				textView.setText("Waiting ...");
				
				FaceppDetect faceppDetect = new FaceppDetect();
				faceppDetect.setDetectCallback(new DetectCallback() {
					
					public void detectResult(FaceppResult rst) {
						//Log.v(TAG, rst.toString());
						
						//use the red paint
						Paint paint = new Paint();
						paint.setColor(Color.RED);
						paint.setStrokeWidth(Math.max(img.getWidth(), img.getHeight()) / 100f);

						//create a new canvas
						Bitmap bitmap = Bitmap.createBitmap(img.getWidth(), img.getHeight(), img.getConfig());
						Canvas canvas = new Canvas(bitmap);
						canvas.drawBitmap(img, new Matrix(), null);
						
						
						try {
							//find out all faces
							final int count = rst.get("face").getCount();
							for (int i = 0; i < count; ++i) {
								float x, y, w, h;
								//get the center point
								x = (float)rst.get("face").get(i).get("position").get("center").get("x").toDouble().doubleValue();
								y = (float)rst.get("face").get(i).get("position").get("center").get("y").toDouble().doubleValue();

								//get face size
								w = (float)rst.get("face").get(i).get("position").get("width").toDouble().doubleValue();
								h = (float)rst.get("face").get(i).get("position").get("height").toDouble().doubleValue();
								
								//change percent value to the real size
								x = x / 100 * img.getWidth();
								w = w / 100 * img.getWidth() * 0.7f;
								y = y / 100 * img.getHeight();
								h = h / 100 * img.getHeight() * 0.7f;

								//draw the box to mark it out
								canvas.drawLine(x - w, y - h, x - w, y + h, paint);
								canvas.drawLine(x - w, y - h, x + w, y - h, paint);
								canvas.drawLine(x + w, y + h, x - w, y + h, paint);
								canvas.drawLine(x + w, y + h, x + w, y - h, paint);
							}
							
							//save new image
							img = bitmap;

							MainActivity.this.runOnUiThread(new Runnable() {
								
								public void run() {
									//show the image
									imageView.setImageBitmap(img);
									textView.setText("Finished.");
								}
							});
							
						} catch (FaceppParseException e) {
							e.printStackTrace();
							
							MainActivity.this.runOnUiThread(new Runnable() {
								public void run() {
									textView.setText("Error.");
								}
							});
						}
						
					}
				});
				faceppDetect.detect(img);
			}
		});
        
        imageView = (ImageView)this.findViewById(R.id.imageView1);
        imageView.setImageBitmap(img);
        
        buttonExhibit = (Button) this.findViewById(R.id.button3);
        buttonExhibit.setVisibility(View.INVISIBLE);
        
        buttonExhibit.setOnClickListener(new OnClickListener() 
        {
        	public void onClick(View arg0) {
        		Intent intent = new Intent();
	            intent.setClass(MainActivity.this, SecondActivity.class);            
	            
	            ByteArrayOutputStream stream = new ByteArrayOutputStream();
	    		float scale = Math.min(1, Math.min(600f / img.getWidth(), 600f / img.getHeight()));
	    		Matrix matrix = new Matrix();
	    		matrix.postScale(scale, scale);
	    		Bitmap imgSmall = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, false);	    		
	    		imgSmall.compress(Bitmap.CompressFormat.JPEG, 100, stream);
	    		byte[] array = stream.toByteArray();
	    		
	    		byte[] bit=null;
	            bit=Endback();
	            String word=null;
	            word=Textback();
			    intent.putExtra("bitmap", bit);			    
			    intent.putExtra("picture", array);
			    intent.putExtra("statement", word);
			    startActivity(intent);
			    
        		
		}
	});
        
        //this is for database
       
        SQLiteDatabase dbWrite= db.getWritableDatabase();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        
        ContentValues values = new ContentValues();
       
        Bitmap bmp =BitmapFactory.decodeResource(this.getBaseContext().getResources(),R.drawable.angelababy);
        bmp.compress(Bitmap.CompressFormat.PNG, 100, os);    
        values.put("pic_img", os.toByteArray());  
        values.put("pic_name","person_0");   
        dbWrite.insert("pic",null, values);
        
        values = new ContentValues(); 
        os = new ByteArrayOutputStream();
        bmp =BitmapFactory.decodeResource(this.getBaseContext().getResources(),R.drawable.krystal);  
        bmp.compress(Bitmap.CompressFormat.PNG, 100, os);    
        values.put("pic_img", os.toByteArray());  
        values.put("pic_name","person_1");   
        dbWrite.insert("pic",null, values);
        
        values = new ContentValues();
        os = new ByteArrayOutputStream();
        bmp =BitmapFactory.decodeResource(this.getBaseContext().getResources(),R.drawable.taeyeong);  
        bmp.compress(Bitmap.CompressFormat.PNG, 100, os);    
        values.put("pic_img", os.toByteArray());  
        values.put("pic_name","person_2");   
        dbWrite.insert("pic",null, values);
        
        values = new ContentValues();
        os = new ByteArrayOutputStream();
        bmp =BitmapFactory.decodeResource(this.getBaseContext().getResources(),R.drawable.jessica);  
        bmp.compress(Bitmap.CompressFormat.PNG, 100, os);    
        values.put("pic_img", os.toByteArray());  
        values.put("pic_name","person_3");   
        dbWrite.insert("pic",null, values);
        
        values = new ContentValues(); 
        os = new ByteArrayOutputStream();
        bmp =BitmapFactory.decodeResource(this.getBaseContext().getResources(),R.drawable.liuyifei);  
        bmp.compress(Bitmap.CompressFormat.PNG, 100, os);    
        values.put("pic_img", os.toByteArray());  
        values.put("pic_name","person_4");   
        dbWrite.insert("pic",null, values);
        
        values = new ContentValues();
        os = new ByteArrayOutputStream();
        bmp =BitmapFactory.decodeResource(this.getBaseContext().getResources(),R.drawable.liyifeng);  
        bmp.compress(Bitmap.CompressFormat.PNG, 100, os);    
        values.put("pic_img", os.toByteArray());  
        values.put("pic_name","person_5");   
        dbWrite.insert("pic",null, values);
        
        values = new ContentValues();
        os = new ByteArrayOutputStream();
        bmp =BitmapFactory.decodeResource(this.getBaseContext().getResources(),R.drawable.huangxiaoming);  
        bmp.compress(Bitmap.CompressFormat.PNG, 100, os);    
        values.put("pic_img", os.toByteArray());  
        values.put("pic_name","person_6");   
        dbWrite.insert("pic",null, values);
        
        values = new ContentValues();
        os = new ByteArrayOutputStream();
        bmp =BitmapFactory.decodeResource(this.getBaseContext().getResources(),R.drawable.wuyifan);  
        bmp.compress(Bitmap.CompressFormat.PNG, 100, os);    
        values.put("pic_img", os.toByteArray());  
        values.put("pic_name","person_7");   
        dbWrite.insert("pic",null, values);
        
        dbWrite.close();
   

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	super.onActivityResult(requestCode, resultCode, intent);
    	
    	//the image picker callback
    	if (requestCode == PICTURE_CHOOSE) {
    		if (intent != null) {
    			//The Android api ~~~ 
    			//Log.d(TAG, "idButSelPic Photopicker: " + intent.getDataString());
    			Cursor cursor = getContentResolver().query(intent.getData(), null, null, null, null);
    			cursor.moveToFirst();
    			int idx = cursor.getColumnIndex(ImageColumns.DATA);
    			String fileSrc = cursor.getString(idx); 
    			//Log.d(TAG, "Picture:" + fileSrc);
    			
    			//just read size
    			Options options = new Options();
    			options.inJustDecodeBounds = true;
    			img = BitmapFactory.decodeFile(fileSrc, options);

    			//scale size to read
    			options.inSampleSize = Math.max(1, (int)Math.ceil(Math.max((double)options.outWidth / 1024f, (double)options.outHeight / 1024f)));
    			options.inJustDecodeBounds = false;
    			img = BitmapFactory.decodeFile(fileSrc, options);
    			textView.setText("Clik Detect. ==>");
    			
    			
    			imageView.setImageBitmap(img);
    			buttonDetect.setVisibility(View.VISIBLE);
    			buttonExhibit.setVisibility(View.VISIBLE);
    			
    		}
    		else {
    			Log.d(TAG, "idButSelPic Photopicker canceled");
    		}
    	}
    }

    private class FaceppDetect {
    	DetectCallback callback = null;
 
    	public void setDetectCallback(DetectCallback detectCallback) { 
    		callback = detectCallback;
    	}

    	public  void detect(final Bitmap image) {
    		
    		
    		
    		new Thread(new Runnable() {
				
				public void run() {
					HttpRequests httpRequests = new HttpRequests("e7ed54669400b6e0757f16cc10e6ba2c", "0O3kdSVLSY-cPA1NfaH2ecs6w6Anap6U");
		    		//Log.v(TAG, "image size : " + img.getWidth() + " " + img.getHeight());
		    		
		    		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		    		float scale = Math.min(1, Math.min(600f / img.getWidth(), 600f / img.getHeight()));
		    		Matrix matrix = new Matrix();
		    		matrix.postScale(scale, scale);

		    		Bitmap imgSmall = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, false);
		    		//Log.v(TAG, "imgSmall size : " + imgSmall.getWidth() + " " + imgSmall.getHeight());
		    		
		    		imgSmall.compress(Bitmap.CompressFormat.JPEG, 100, stream);
		    		byte[] array = stream.toByteArray();
		    		
		    		try {
		    			//detect
						FaceppResult result = httpRequests.detectionDetect(new PostParameters().setImg(array).setAttribute("gender,age"));
						
      				//recognition/train
						FaceppResult syncRet = null; 
						
						syncRet = httpRequests.trainIdentify(new PostParameters().setGroupName("apptest"));
						FaceppResult cons= httpRequests.getSessionSync(syncRet.get("session_id").toString());
					    
					  //recognition/recognize
						FaceppResult a=httpRequests.recognitionIdentify(new PostParameters().setGroupName("apptest").setImg(array));
						System.out.println(a);
						
						 FaceppResult b=a.get("face").get(0).get("candidate").get(0).get("person_name");
						 FaceppResult g=a.get("face").get(0).get("candidate").get(0).get("confidence");
					    fruit=b.toString();     
				        sad=g.toDouble();
						//finished , then call the callback function
						if (callback != null) {
							callback.detectResult(result);
						}
					} catch (FaceppParseException e) {
						e.printStackTrace();
						MainActivity.this.runOnUiThread(new Runnable() {
							public void run() {
								textView.setText("Network error.");
							}
						});
					}
					
				}
			}).start();
			
			
    	}
    }

    interface DetectCallback {
    	void detectResult(FaceppResult rst);
	}
  
    public byte[] Endback()
    {
    	
		byte[] in=null;
    	
	  SQLiteDatabase dbRead=db.getReadableDatabase();
	  System.out.println(fruit);
   	
	    Cursor c=dbRead.query("pic", new String[]{"id","pic_name","pic_img"}, "pic_name=?", new String[]{fruit}, null, null, null);
	    while(c.moveToNext())
	        {
		       String id=c.getString(c.getColumnIndex("id"));
		       String name=c.getString(c.getColumnIndex("pic_name"));
		       in=c.getBlob(c.getColumnIndex("pic_img"));
	    }
		return in;   
    	
    }
   
    public String Textback()
    {
    	System.out.println(fruit);
    	String text=null;
    	int x=0;  
    	
    	switch(Person.getpersonType(fruit))
    	{
    	case person_0: text=sad+"% look like Angelababy";
    		break;
    	case person_1: text=sad+"% look like Krystal";
		break;
    	case person_2: text=sad+"% look like Taeyeong";
		break;
    	case person_3: text=sad+"% look like Jessica";
		break;
    	case person_4: text=sad+"% look like Liuyifei";
		break;
    	case person_5: text=sad+"% look like Liyifeng";
		break;
    	case person_6: text=sad+"% look like Huangxiaoming";
		break;
    	case person_7: text=sad+"% look like Wuyifan";
		break;
    	}
    	return text;
    	
    }
   

}

      
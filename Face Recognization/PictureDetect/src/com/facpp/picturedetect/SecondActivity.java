package com.facpp.picturedetect;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class SecondActivity extends Activity {
	
	private ImageView imageView1 = null;
	private ImageView imageView2 = null;
	private TextView textView = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_second);
	imageView1 = (ImageView)this.findViewById(R.id.imageViewshow);
	imageView2 = (ImageView)this.findViewById(R.id.imageViewexhibit);
	textView = (TextView)this.findViewById(R.id.textView1);
	Intent intent=getIntent();  
	if(intent !=null)  
	{  
	      byte [] bis=intent.getByteArrayExtra("bitmap");
	      Bitmap bitmap=BitmapFactory.decodeByteArray(bis, 0, bis.length);  
	      imageView1.setImageBitmap(bitmap); 
	      
	      byte [] bs=intent.getByteArrayExtra("picture");
	      Bitmap bmp=BitmapFactory.decodeByteArray(bs, 0, bs.length);
	      imageView2.setImageBitmap(bmp);
	      
	      String state=intent.getStringExtra("statement");
	      textView.setText(state);
	      
	 }  

	}

}

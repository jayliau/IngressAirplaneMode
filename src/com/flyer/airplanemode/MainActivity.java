package com.flyer.airplanemode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.OutputStream;
import java.lang.reflect.Field;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {

	EditText mNick;
	EditText mAcc;
	EditText mLat;
	EditText mLon;
	
	ListView mListView;
	ArrayAdapter<String> mAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mNick = (EditText) this.findViewById(R.id.editNick);
		mAcc = (EditText) this.findViewById(R.id.editAcc);
		mLat = (EditText) this.findViewById(R.id.editLat);
		mLon = (EditText) this.findViewById(R.id.editLon);
	
		mListView = (ListView) this.findViewById(R.id.listView1);
		Intent i = this.getIntent();
		if( i != null) {
			parseIntent(i);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void Click(View v) {
		if(v.getId() == R.id.button1) {
			String result = mLat.getText().toString()+","+mLon.getText().toString()+","+mAcc.getText().toString();
	        Settings.System.putString(MainActivity.this.getContentResolver(),
	                "FakeGPS", result);
	        
	        String newInput = mNick.getText().toString()+","+result;
	        Log.e("Timmy", newInput);
	        for(int a = 0 ; a < mAdapter.getCount() ; a++) {
	        	if(mAdapter.getItem(a).contentEquals(newInput)){
	        		return;
	        	}
	        }
	        mAdapter.insert(newInput, 0);
	        //mAdapter.add(newInput);
	        mAdapter.notifyDataSetChanged();
	        this.finish();
		}
		else if(v.getId() == R.id.button2) {
	        Settings.System.putString(MainActivity.this.getContentResolver(),
	                "FakeGPS", "9999.0,9999.0,0.0");
			mLat.setText("");
			mLon.setText("");
			mAcc.setText("2000");
			mNick.setText("");
	        Log.e("Timmy", "reset fake gps");
		}
		else if(v.getId() == R.id.button3) {
			mAdapter.clear();
		}
	}

	@Override
	protected void onPause() {
		
		try {
			File cache = new File(this.getDir("histroy", 0), "history.txt");
			Log.e("Timmy"," write to file "+cache.getAbsolutePath());
			FileWriter out = new FileWriter(cache);
			for(int a = 0 ; a < mAdapter.getCount(); a++) {
				out.write(mAdapter.getItem(a)+"\n");
			}
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		
		mAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
		mListView.setAdapter(mAdapter);
        
         //ListView��onClick
		mListView.setOnItemClickListener(new OnItemClickListener(){
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					String result[] = mAdapter.getItem(arg2).split(",");
					mNick.setText(result[0]);
					mLat.setText(result[1]);
					mLon.setText(result[2]);
					mAcc.setText(result[3]);
				}
         });
		mListView.setOnItemLongClickListener(new OnItemLongClickListener(){

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				mAdapter.remove(mAdapter.getItem(arg2));
				mAdapter.notifyDataSetChanged();
				return true;
			}
			
		});

		File cache = new File(this.getDir("histroy", 0), "history.txt");
		if (cache.exists()) {
			Log.e("Timmy",cache.getAbsoluteFile()+" exist");
			try {
				BufferedReader buffreader = new BufferedReader(new FileReader(
						cache));
				String line;
				while ((line = buffreader.readLine()) != null) {
					mAdapter.add(line);
				}
				buffreader.close();
			} catch (Exception e) {

			}
			mAdapter.notifyDataSetChanged();
			cache.delete();
		} else {
			Log.e("Timmy","no file : "+cache.getAbsoluteFile());
		}
		super.onResume();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if(intent != null) {
			parseIntent(intent);
		}
	}
	
	private void parseIntent(Intent i) {
		if(i.getAction().contains("SEND")) {
			Bundle b = i.getExtras();
			String nick = b.getString("android.intent.extra.SUBJECT");
			nick.replaceAll(",", "-");
			mNick.setText(nick);
			String ll[] = b.getString("android.intent.extra.TEXT").split("pll=")[1].split(",");
			mLat.setText(ll[0]);
			mLon.setText(ll[1]);
		}
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if (item.getItemId() == R.id.action_select) {
			FilenameFilter filter = new FilenameFilter() {

				@Override
				public boolean accept(File dir, String filename) {
					if(filename.contains(".portal")){
						return true;
					}
					return false;
				}
				
			};
			final File[] files = this.getFilesDir().listFiles(filter);
			final File[] files_d = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).listFiles(filter);
			String[] list = new String[files.length+files_d.length];
			for( int a = 0 ; a < files.length ; a++) {
				list[a] = files[a].getAbsolutePath();
			}
			for( int a = 0 ; a < files_d.length ; a++) {
				list[a] = files_d[a].getAbsolutePath();
			}
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        builder.setTitle("Make your selection");
	        builder.setItems(list, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int item) {
	            	File cache = (item >= files.length ? files_d[item-files.length] : files[item]);
	        		if (cache.exists()) {
	        			try {
	        				BufferedReader buffreader = new BufferedReader(new FileReader(cache));
	        				OutputStream out = MainActivity.this.openFileOutput("default.portal", Context.MODE_WORLD_READABLE);
	        				String line;
	        				while ((line = buffreader.readLine()) != null) {
	        					out.write((line+"\n").getBytes());
	        				}
	        				buffreader.close();
	        				out.close();

						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} 			
	        		}
	            }
	        });
	        AlertDialog alert = builder.create();
	        alert.show();
		} else if (item.getItemId() == R.id.action_load) {
			FilenameFilter filter = new FilenameFilter() {

				@Override
				public boolean accept(File dir, String filename) {
					if(filename.contains(".portal")){
						return true;
					}
					return false;
				}
				
			};
			final File[] files = this.getFilesDir().listFiles(filter);
			final File[] files_d = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).listFiles(filter);
			String[] list = new String[files.length+files_d.length];
			for( int a = 0 ; a < files.length ; a++) {
				list[a] = files[a].getAbsolutePath();
			}
			for( int a = 0 ; a < files_d.length ; a++) {
				list[a] = files_d[a].getAbsolutePath();
			}
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        builder.setTitle("Make your selection");
	        builder.setItems(list, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int item) {
	            	File cache = (item >= files.length ? files_d[item-files.length] : files[item]);
	        		if (cache.exists()) {
	        			Log.e("Timmy",cache.getAbsoluteFile()+" exist");
	        			try {
							BufferedReader buffreader = new BufferedReader(
									new FileReader(cache));
	        				String line;
	        				while ((line = buffreader.readLine()) != null) {
	        					String l = line.substring(line.indexOf(",")+1);
	        					String ll[] = l.split("--pll=");
	        					mAdapter.add(ll[0]+","+ll[1]);
	        				}
	        				buffreader.close();
	        			} catch (Exception e) {

	        			}
	        			mAdapter.notifyDataSetChanged();
	        			//[SJ] Not delete file.
	        			//cache.delete();
	        		} else {
	        			Log.e("Timmy","no file : "+cache.getAbsoluteFile());
	        		}
	            }
	        });
	        
	        AlertDialog alert = builder.create();
	        alert.show();
		} else if (item.getItemId() == R.id.action_export) {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

			// set prompts.xml to alertdialog builder
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View layout = (View) inflater.inflate(R.layout.input_dialog, null);
			alertDialogBuilder.setView(layout);

			final EditText userInput = (EditText) layout.findViewById(R.id.editTextDialogUserInput);			
			((TextView)layout.findViewById(R.id.textView1)).setText("input file name");
			// set dialog message
			alertDialogBuilder
					.setCancelable(false)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									String fn = userInput.getText().toString();
									try {
										OutputStream out = MainActivity.this.openFileOutput(fn+".portal", Context.MODE_WORLD_READABLE);
										for (int a = 0; a < mAdapter.getCount(); a++) {
											String result[] = mAdapter.getItem(a).split(",");
											String line = (a+1)+","+result[0]+"--pll="+result[1]+","+result[2]+"\n";
											out.write(line.getBytes());
										}
										out.close();
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									dialog.cancel();
								}
							});

			// create alert dialog
			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();

		}
		return super.onMenuItemSelected(featureId, item);
	}
}

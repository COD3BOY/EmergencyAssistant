package com.snr9.EmergencyAssistant;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import fortyonepost.com.pwop.R;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.widget.Toast;

public class TakePicture extends Activity implements SurfaceHolder.Callback {
	// a variable to store a reference to the Image View at the main.xml file
	private ImageView iv_image;
	// a variable to store a reference to the Surface View at the main.xml file
	private SurfaceView sv;

	// a bitmap to display the captured image
	private Bitmap bmp;

	// Camera variables
	// a surface holder
	private SurfaceHolder sHolder;
	// a variable to control the camera
	private Camera mCamera;
	// the camera parameters
	private Parameters parameters;

	int flag = 0;

	String locstr;

	class GPSBasedLocationListener implements Runnable {

		Double lat = 0.0, lon = 0.0;

		@Override
		public void run() {

			final LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			final LocationListener mlocListener = new MyLocationListener();

			TakePicture.this.runOnUiThread(new Runnable() {
				public void run() {
					mlocManager.requestLocationUpdates(
							LocationManager.GPS_PROVIDER, 0, 0, mlocListener);
				}
			});

		}

		public class MyLocationListener implements LocationListener

		{
			
			public void onLocationChanged(Location loc)

			{
				flag = 1;

				lat = loc.getLatitude();
				lon = loc.getLongitude();

				Geocoder geocoder = new Geocoder(getApplicationContext(),
						Locale.getDefault());
				try {
					List<Address> listAddresses = geocoder.getFromLocation(
							loc.getLatitude(), loc.getLongitude(), 1);
					if (null != listAddresses && listAddresses.size() > 0) {

						Address tes = listAddresses.get(0);

						locstr = tes.getAddressLine(0) + " : "
								+ tes.getAddressLine(1) + " , "
								+ tes.getAddressLine(2);

						TakePicture.this.runOnUiThread(new Runnable() {
							public void run() {
								Toast.makeText(TakePicture.this,
										"LOCATION : " + locstr,
										Toast.LENGTH_LONG).show();
							}
						});

						

					}
				} catch (IOException e) {
					Context context = getApplicationContext();
					CharSequence text = "GEOcoder";
					int duration = Toast.LENGTH_SHORT;

					Toast toast = Toast.makeText(context, text, duration);
					toast.show();
				}

			}

			public void onProviderDisabled(String provider)

			{

				TakePicture.this.runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(getApplicationContext(), "Gps Disabled",
								Toast.LENGTH_SHORT).show();
					}
				});

			}

			public void onProviderEnabled(String provider)

			{
				TakePicture.this.runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(getApplicationContext(),

						"Gps Enabled",

						Toast.LENGTH_SHORT).show();
					}
				});

			}

			public void onStatusChanged(String provider, int status,
					Bundle extras)

			{

			}

		}/* End of Class MyLocationListener */

	}

	class ContinuousPics implements Runnable {

		int num;
		SurfaceHolder surfHolder;

		class Email implements Runnable {
			
			@SuppressLint({ "NewApi", "NewApi", "NewApi", "NewApi" })
			private void sendSMS(String phoneNumber, String message) {
				Log.v("phoneNumber", phoneNumber);
				Log.v("MEssage", message);
				PendingIntent pi = PendingIntent.getActivity(TakePicture.this,
						0, new Intent(TakePicture.this, Object.class), 0);
				SmsManager sms = SmsManager.getDefault();
				sms.sendTextMessage(phoneNumber, null, message, pi, null);
			}


			@SuppressLint({ "NewApi", "NewApi" })
			@Override
			public void run() {
				Mail m = new Mail();

				if (flag == 0) {
					m.setBody("NO Location!");

					// Toast.makeText(TakePicture.this,
					// "No update from Location manager yet!",Toast.LENGTH_LONG).show();

				}

				else {
					m.setBody(locstr);
				}

				

				try {

					File sdDir = Environment
							.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

					String photoFile[] = new String[4];

					for (int i = 0; i < 4; i++) {
						photoFile[i] = "Picture_" + i + ".jpg";
						String filename = sdDir + File.separator
								+ "EmergencyAssistant" + File.separator
								+ photoFile[i];

						Log.i("File path : ", filename);

						m.addAttachment(filename);
					}

					if (m.send()) {

						TakePicture.this.runOnUiThread(new Runnable() {
							public void run() {
								Toast.makeText(TakePicture.this,
										"Email was sent successfully.",
										Toast.LENGTH_LONG).show();
								
						
							}
						});

					} else {
						TakePicture.this.runOnUiThread(new Runnable() {
							public void run() {
								Toast.makeText(TakePicture.this,
										"Email was not sent.",
										Toast.LENGTH_LONG).show();
							}
						});

					}
				} catch (Exception e) {
					
					Log.e("MailApp", "Could not send email", e);
				}

			//	sendSMS("9746733813", "HELP! I am here : " + locstr);
				
			}

		}

		public ContinuousPics(int numberOfTimes, SurfaceHolder hold) {

			this.num = numberOfTimes;
			this.surfHolder = hold;
		}

		@Override
		public void run() {
			// sets what code should be executed after the picture is taken
			Camera.PictureCallback mCall = new Camera.PictureCallback() {
				@TargetApi(8)
				@Override
				public void onPictureTaken(byte[] data, Camera camera) {
					// decode the data obtained by the camera into a Bitmap
					bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
					// set the iv_image
					iv_image.setImageBitmap(bmp);

					// /////////////////////////////////////////////////////////////////////////////
					/* Save to SD card */
					// ////////////////////////////////////////////////////////////////////////////

					File sdDir = Environment
							.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

					File pictureFileDir = new File(sdDir, "EmergencyAssistant");

					if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {

						Log.d("DEBUG", "Can't create directory to save image.");
						Toast.makeText(getApplicationContext(),
								"Can't create directory to save image.",
								Toast.LENGTH_LONG).show();
						return;

					}

					String photoFile = "Picture_" + num + ".jpg";

					String filename = pictureFileDir.getPath() + File.separator
							+ photoFile;

					File pictureFile = new File(filename);

					try {

						FileOutputStream fos = new FileOutputStream(pictureFile);
						bmp.compress(Bitmap.CompressFormat.JPEG, 10, fos);

						// fos.write(data);
						fos.close();
						Toast.makeText(getApplicationContext(),
								"New Image saved:" + photoFile,
								Toast.LENGTH_LONG).show();

						/* Restart the thread for the specified number of times */
						if (num > 0) {
							mCamera.release();
							mCamera = Camera.open();

							try {
								mCamera.setPreviewDisplay(surfHolder);
								// get camera parameters
								parameters = mCamera.getParameters();

								// set camera parameters
								mCamera.setParameters(parameters);
								mCamera.startPreview();
							} catch (Exception e) {
								Log.e("ERROR", "Exception in loop#");
							}

							Thread continuousPics = new Thread(
									new ContinuousPics(num - 1, surfHolder));
							continuousPics.start();
						} else {
							Toast.makeText(getApplicationContext(), "Email!",
									Toast.LENGTH_LONG).show();

							Thread email = new Thread(new Email());
							email.start();

							Toast.makeText(getApplicationContext(),
									"Email sent!", Toast.LENGTH_LONG).show();
						}

					} catch (Exception error) {
						Log.d("DEBUG", "File" + filename + "not saved: "
								+ error.getMessage());
						Toast.makeText(getApplicationContext(),
								"Image could not be saved.", Toast.LENGTH_LONG)
								.show();
					}

					/////////////////////////////////////////////////////////////////////////////

				}
			};

			mCamera.takePicture(null, null, mCall);

		}

	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// get the Image View at the main.xml file
		iv_image = (ImageView) findViewById(R.id.imageView);

		// get the Surface View at the main.xml file
		sv = (SurfaceView) findViewById(R.id.surfaceView);

		// Get a surface
		sHolder = sv.getHolder();

		// add the callback interface methods defined below as the Surface View
		// callbacks
		sHolder.addCallback(this);

		// tells Android that this surface will have its data constantly
		// replaced
		sHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, acquire the camera and tell it where
		// to draw the preview.
		mCamera = Camera.open();
		try {
			mCamera.setPreviewDisplay(holder);
			// get camera parameters
			parameters = mCamera.getParameters();

			// set camera parameters
			mCamera.setParameters(parameters);
			mCamera.startPreview();

			Thread continuousPics = new Thread(new ContinuousPics(4, holder));
			continuousPics.start();

			Thread location = new Thread(new GPSBasedLocationListener());
			location.start();

		} catch (IOException exception) {
			mCamera.release();
			mCamera = null;
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// stop the preview
		mCamera.stopPreview();
		// release the camera
		mCamera.release();
		// unbind the camera from this object
		mCamera = null;
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub

	}
	
	@Override
	protected void onPause() {
		
		System.exit(0);
	/*	Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()]);
		
		for(int i=0;i<threadArray.length;i++)
		{
			threadArray[i].destroy();
		}*/
		super.onPause();
	}
	@Override
	protected void onStop() {
		
		System.exit(0);
	/*	Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()]);
		
		for(int i=0;i<threadArray.length;i++)
		{
			threadArray[i].destroy();
		}*/
		
		super.onStop();
	}
}
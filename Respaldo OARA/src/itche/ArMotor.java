package mx.gob.itche;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ZoomControls;

import com.metaio.sdk.ARViewActivity;
import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.jni.AnnotatedGeometriesGroupCallback;
import com.metaio.sdk.jni.EGEOMETRY_FOCUS_STATE;
import com.metaio.sdk.jni.EPLAYBACK_STATUS;
import com.metaio.sdk.jni.IAnnotatedGeometriesGroup;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.sdk.jni.IRadar;
import com.metaio.sdk.jni.LLACoordinate;
import com.metaio.sdk.jni.MovieTextureStatus;
import com.metaio.sdk.jni.Rotation;
import com.metaio.sdk.jni.SensorValues;
import com.metaio.sdk.jni.TrackingValuesVector;
import com.metaio.sdk.jni.Vector3d;
import com.metaio.tools.SystemInfo;
import com.metaio.tools.io.AssetsManager;

public class ArMotor extends ARViewActivity {
	
    private IAnnotatedGeometriesGroup mAnnotatedGeometriesGroup;
	
	private MyAnnotatedGeometriesGroupCallback mAnnotatedGeometriesGroupCallback;
	
	//Ruta DOWNLOADS de SD donde lee los archivos DESCARGADOS ya sea objetos, xml, videos
	File externalStorage = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
	
	// Geometries Object
	private IGeometry IGeoObject;
	private IGeometry mSailboat;
	
	String mTrackingConfigFile="";
	
	// Geometries Video
	private IGeometry IGeoVideo;	
	private MetaioSDKCallbackHandler mCallbackHandler;
	
	
	// Geometries GPS 	 
	private IGeometry IGeoGps;	
	private IRadar mRadar;
    String modelGps="";
    
    ProgressDialog progressDialog;
	int progreso;
		int id=0;
		
 
	GetDataAsync mTask;
		
	
	//Zoom Controls
	ZoomControls zoom;
	float objectSize = 80.0f;
	
    //MEDIA LAUNCHER 
	String MEDIA_LAUNCHER="";

    //Nombre del Archivo de Tracking, Objetos y videos
	
	String mediaToExecute="";
	//final String NOMBRE_OBJETO2 = "UH60.zip";
	
	String xmlTrackingFile ="";
	//final String NOMBRE_VIDEO ="moments.3g2";
	
	String latitudID="";
	String longitudID="";
	
    final String File ="";
    
    Bundle dataStatusBusiness;
	
       
    ProgressBar pb;
    Dialog dialog;
    int downloadedSize = 0;
    int totalSize = 0;
    TextView cur_val;
    
    String download_file_path = "http://advertisingchannel.com.mx/oara/";
   
  //mediaLauncher  mediaToExecute  xmlTrackingFile  marker
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		IGeoObject = null;	
		IGeoGps = null;	
		
		
		
		dataStatusBusiness = getIntent().getExtras();
		
		MEDIA_LAUNCHER = dataStatusBusiness.getString("mediaLauncher");
		mediaToExecute = dataStatusBusiness.getString("mediaToExecute");
		xmlTrackingFile = dataStatusBusiness.getString("xmlTrackingFile");
		latitudID = dataStatusBusiness.getString("latitudID");
		longitudID = dataStatusBusiness.getString("longitudID");
		
		//Toast.makeText(this, "Launcher: "+ MEDIA_LAUNCHER+ "    "+"File: " + mediaToExecute , Toast.LENGTH_LONG).show();
			
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	 public boolean onOptionsItemSelected(MenuItem item) {
	        switch (item.getItemId()) {
	        case R.id.stopService:
	        stopService(new Intent(ArMotor.this, ServiceGps.class));
	        return true;
	        case R.id.resumeService:
		        startService(new Intent(ArMotor.this, ServiceGps.class));
		        return true;
	        
	        
	        default:
	        return super.onOptionsItemSelected(item);
	        }
	    }
	
	
	//------------------------------------------------------------------------------
	@Override
	protected int getGUILayout() {
		// TODO Auto-generated method stub
		return R.layout.ar_view; 
	}
	
	
	  //------------------------------------------------------------------------------
		@Override
		protected void loadContents() {
			
			if(MEDIA_LAUNCHER.equalsIgnoreCase("Object")){
				objectLauncher();			
			}else if (MEDIA_LAUNCHER.equalsIgnoreCase("Video")){			
				videoLauncher();
			}else if (MEDIA_LAUNCHER.equalsIgnoreCase("GpsObject")){
				gpsLauncher();
				//mTask = new GetDataAsync();
				//mTask.execute(0);
				
				
			}
			  //objectLauncher();			           
			  //gpsLauncher();
			//videoLauncher();
		
		}
		
		//------------------------------------------------------------------------------
		
	public void objectLauncher(){
			
		      				
				String modelPath ="";		
				
				
				try
				{
					// Load desired tracking data for planar marker tracking
					
					//final String mTrackingConfigFile;
					//mTrackingConfigFile = AssetsManager.getAssetPath("Recursos/sergiocunmarker.xml");
					
					File fileTrackingXml = new File(externalStorage.getAbsolutePath()+"/"+xmlTrackingFile);
					if(fileTrackingXml.exists()){      
					//Do somehting
						
						mTrackingConfigFile = externalStorage.getAbsolutePath()+"/"+xmlTrackingFile;
						
					}	
					else {
						//DESPUES LO  LEE DESDE LA SD 
					
					downloadFile(xmlTrackingFile);
					mTrackingConfigFile = externalStorage.getAbsolutePath()+"/"+xmlTrackingFile;
					
					}
					
					//mTrackingConfigFile = externalStorage.getAbsolutePath()+"/itchmarker.xml";
			
					boolean result = metaioSDK.setTrackingConfiguration(mTrackingConfigFile); 
					MetaioDebug.log("Tracking data loaded: " + result); 
										
					// Load all the geometries.
					File file2 = new File(externalStorage.getAbsolutePath()+"/"+mediaToExecute);
					
					if(file2.exists()){      
								//Do somehting
									
									modelPath = externalStorage.getAbsolutePath()+"/"+ mediaToExecute;
									
								}	
								else {
									//DESPUES LO  LEE DESDE LA SD 
								
								downloadFile(mediaToExecute);
								modelPath = externalStorage.getAbsolutePath()+"/"+ mediaToExecute;
								
								}							
								
								if (modelPath != null) 
								{
									IGeoObject = metaioSDK.createGeometry(modelPath);
									if (IGeoObject != null) 
									{
										// Set geometry properties
										IGeoObject.setScale(new Vector3d(80.0f, 80.0f, 80.0f));
										IGeoObject.setVisible(true);
										MetaioDebug.log("Loaded geometry "+modelPath);
									}
									else
										MetaioDebug.log(Log.ERROR, "Error loading geometry: "+modelPath);
								}								
							
							}       
							catch (Exception e)
							{
								e.printStackTrace();
					}			
		
	}
					

	
	//------------------------------------------------------------------------------
	
	public void videoLauncher()
    {
		String moviePath="";
		
		try
		{
			// Load desired tracking data for planar marker tracking
			
			//final String mTrackingConfigFile;
			//mTrackingConfigFile = AssetsManager.getAssetPath("Recursos/sergiocunmarker.xml");
			
			File fileTrackingXml = new File(externalStorage.getAbsolutePath()+"/"+xmlTrackingFile);
			if(fileTrackingXml.exists()){      
			//Do somehting
				
				mTrackingConfigFile = externalStorage.getAbsolutePath()+"/"+xmlTrackingFile;
				
			}	
			else {
				//DESPUES LO  LEE DESDE LA SD 
			
			downloadFile(xmlTrackingFile);
			mTrackingConfigFile = externalStorage.getAbsolutePath()+"/"+xmlTrackingFile;
			
			}
				
			boolean result = metaioSDK.setTrackingConfiguration(mTrackingConfigFile); 
			MetaioDebug.log("Tracking data loaded: " + result); 
			
			
			File file2 = new File(externalStorage.getAbsolutePath()+"/"+mediaToExecute);
			if(file2.exists()){      
			//Do somehting
				
				//cargando video
				moviePath = externalStorage.getAbsolutePath()+"/"+ mediaToExecute;
				
			}	
			else {
				//DESPUES LO  LEE DESDE LA SD 
			
			downloadFile(mediaToExecute);
			//cargando video
			moviePath = externalStorage.getAbsolutePath()+"/"+ mediaToExecute;
			
			}
			
		    
			MetaioDebug.log(Log.ERROR, "movie loaded: " + moviePath);
		    
		    
			if (moviePath != null)
			{
				IGeoVideo = metaioSDK.createGeometryFromMovie(moviePath, false);
				
				if (IGeoVideo != null)
				{
					//MetaioDebug.log(Log.ERROR, "movie created");
					IGeoVideo.setScale(6.0f);
					//mMoviePlane.setRotation(new Rotation(0f, 0f, (float)-Math.PI/2));
					MetaioDebug.log("Loaded geometry "+moviePath);
				}
				else {
					MetaioDebug.log(Log.ERROR, "Error loading geometry: "+moviePath);
				}
			}		
			
			//start displaying the model
			setActiveModel();
		}
		
					catch (Exception e)
					{
						e.printStackTrace();	
	                }
}
		
    //------------------------------------------------------------------------------
		
		
		public void gpsLauncher()
		{

			// Set GPS tracking configuration
			boolean result = metaioSDK.setTrackingConfiguration("GPS", false);
			MetaioDebug.log("Tracking data loaded: " + result);
			
			mAnnotatedGeometriesGroup = metaioSDK.createAnnotatedGeometriesGroup();
			mAnnotatedGeometriesGroupCallback = new MyAnnotatedGeometriesGroupCallback();
			mAnnotatedGeometriesGroup.registerCallback(mAnnotatedGeometriesGroupCallback);

			// Clamp geometries' Z position to range [5000;200000] no matter how close or far they are away.
			// This influences minimum and maximum scaling of the geometries (easier for development).
			metaioSDK.setLLAObjectRenderingLimits(5, 200);

			// Set render frustum accordingly
			metaioSDK.setRendererClippingPlaneLimits(10, 220000);

			// let's create LLA objects for known cities    18.51904776254762, -88.3028295636177 Chetumal
						
			double latitud = Double.parseDouble(latitudID);
			double longitud =  Double.parseDouble(longitudID);
			
			LLACoordinate coordenadasGps = new LLACoordinate(latitud, longitud, 0, 0);
			
			// Load some POIs. Each of them has the same shape at its geoposition. We pass a string
			// (const char*) to IAnnotatedGeometriesGroup::addGeometry so that we can use it as POI title
			// in the callback, in order to create an annotation image with the title on it.
			
			IGeoGps = createPOIGeometry(coordenadasGps);
			mAnnotatedGeometriesGroup.addGeometry(IGeoGps, "Empresa");			

			String cubeReference = AssetsManager.getAssetPath(getApplicationContext(), "Recursos/cube.obj");
			//String metaioManModel = AssetsManager.getAssetPath(getApplicationContext(), "Recursos/metaioman.md2");
			
			
			if (cubeReference != null)
			{
				IGeoGps = metaioSDK.createGeometry(cubeReference);
				if (IGeoGps != null)
				{
					IGeoGps.setTranslationLLA(coordenadasGps);
					IGeoGps.setLLALimitsEnabled(true);
					IGeoGps.setScale(1);
				}
				else
				{
					MetaioDebug.log(Log.ERROR, "Error loading geometry: " + cubeReference);
				}
			}

			
			
			// create radar
			mRadar = metaioSDK.createRadar();
			//La siguiente linea es la imagen est�tica de la brujula en la esquina superior izquierda
			mRadar.setBackgroundTexture(AssetsManager.getAssetPath(getApplicationContext(),"Recursos/mac.png"));
			//La siguiente linea es el indicador del Sensor del Giroscopio
			mRadar.setObjectsDefaultTexture(AssetsManager.getAssetPath(getApplicationContext(), "Recursos/circleyellow.png"));
			mRadar.setRelativeToScreen(IGeometry.ANCHOR_TL);

			// add geometries to the radar
			mRadar.add(IGeoGps);
			
		}
		
	
	//------------------------------------------------------------------------------

	
	public void zoomIn(View v){
		
		//Toast.makeText(this, "Zoom IN", Toast.LENGTH_LONG).show();
		objectSize +=20.0f;
		IGeoObject.setScale(new Vector3d(objectSize, objectSize, objectSize));
		IGeoObject.setVisible(true);
	}
	
public void zoomOut(View v){
		
		//Toast.makeText(this, "Zoom IN", Toast.LENGTH_LONG).show();
		objectSize -=20.0f;
		IGeoObject.setScale(new Vector3d(objectSize, objectSize, objectSize));
		IGeoObject.setVisible(true);
	}
	
	
	public void onObjectButtonClick(View v)
	{
		IGeoObject.setVisible(true);
		mSailboat.setVisible(false);
		IGeoVideo.setVisible(false);
	}
	
	/**
	 * activates the sailboat model and deactivates the Metaio man model
	 * @param v
	 */
	public void onBoatButtonClick(View v)
	{
		IGeoObject.setVisible(false);
		mSailboat.setVisible(true);
		
		IGeoVideo.setVisible(false);
	}
	public void onVideoButtonClick(View v)
	{
		IGeoObject.setVisible(false);
		mSailboat.setVisible(false);
		
		
		
		IGeoVideo.setVisible(true);
	}
	
	
	
	/**
	 * changes the tracking configuration to 'ID marker tracking'
	 * @param v
	 */
	@SuppressWarnings("deprecation")
	public void onIdButtonClick(View v)
	{
		mTrackingConfigFile = AssetsManager.getAssetPath("Recursos/TrackingData_Marker.xml");
		MetaioDebug.log("Tracking Config path = "+mTrackingConfigFile);
		
		boolean result = metaioSDK.setTrackingConfiguration(mTrackingConfigFile); 
		MetaioDebug.log("Id Marker tracking data loaded: " + result); 
		IGeoObject.setScale(new Vector3d(1.0f, 1.0f, 1.0f));
		mSailboat.setScale(new Vector3d(3.0f, 3.0f, 3.0f));
	}
	
	/**
	 * changes the tracking configuration to 'picture marker tracking' 
	 * @param v
	 */
	@SuppressWarnings("deprecation")
	public void onPictureButtonClick(View v)
	{
		mTrackingConfigFile = AssetsManager.getAssetPath("Recursos/TrackingData_PictureMarker.xml");
		MetaioDebug.log("Tracking Config path = "+mTrackingConfigFile);
		
		boolean result = metaioSDK.setTrackingConfiguration(mTrackingConfigFile); 
		
		MetaioDebug.log("Picture Marker tracking data loaded: " + result); 
		IGeoObject.setScale(new Vector3d(6.0f, 6.0f, 6.0f));
		mSailboat.setScale(new Vector3d(14.0f, 14.0f, 14.0f));
	}
	
	/**
	 * changes the tracking configuration to 'markerless tracking'
	 * @param v
	 */
	@SuppressWarnings("deprecation")
	public void onMarkerlessButtonClick(View v)
	{
		mTrackingConfigFile = AssetsManager.getAssetPath("Recursos/TrackingData_MarkerlessFast.xml");
		MetaioDebug.log("Tracking Config path = "+mTrackingConfigFile);
	
		boolean result = metaioSDK.setTrackingConfiguration(mTrackingConfigFile); 
		MetaioDebug.log("Markerless tracking data loaded: " + result); 
		
		IGeoObject.setScale(new Vector3d(4.0f, 4.0f, 4.0f));
		mSailboat.setScale(new Vector3d(12.0f, 12.0f, 12.0f));
	}	
	
	
	//------------------------------------------------------------------------------
	@Override
	protected void onGeometryTouched(final IGeometry geometry) {
		// TODO Auto-generated method stub
		MetaioDebug.log("Geometry selected: "+geometry);
        
		
		
		if (geometry.equals(IGeoVideo))
		{
			final MovieTextureStatus status = IGeoVideo.getMovieTextureStatus();
			if (status.getPlaybackStatus() == EPLAYBACK_STATUS.EPLAYBACK_STATUS_PLAYING)
				IGeoVideo.pauseMovieTexture();
			else
				IGeoVideo.startMovieTexture(true);
		}
		
		/*
		mSurfaceView.queueEvent(new Runnable()
		{

			@Override
			public void run()
			{
				mRadar.setObjectsDefaultTexture(AssetsManager.getAssetPath(getApplicationContext(), "TutorialLocationBasedAR/Assets/yellow.png"));
				mRadar.setObjectTexture(geometry, AssetsManager.getAssetPath(getApplicationContext(), "TutorialLocationBasedAR/Assets/red.png"));
				mAnnotatedGeometriesGroup.setSelectedGeometry(geometry);
			}
		});
		
		*/

	}
	
	
	//------------------------------------------------------------------------------

	//Metodo para Descargar un archivo de Servidor
	 
	public boolean downloadFile(String objectNameSaved){
		
		
        
        try {
            URL url = new URL(download_file_path+objectNameSaved);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
 
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);
 
            //<span id="IL_AD3" class="IL_AD">connect</span>
            urlConnection.connect();
 
            //set the path where we want to <span id="IL_AD8" class="IL_AD">save</span> the file            
            File SDCardRoot = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            //create a new file, to save the downloaded file 
            File file = new File(SDCardRoot,objectNameSaved);
  
            FileOutputStream fileOutput = new FileOutputStream(file);
 
            //Stream used for reading the data from the internet
            InputStream inputStream = urlConnection.getInputStream();
 
            //this is the total size of the file which we are downloading
            totalSize = urlConnection.getContentLength();
            
            
             
            //create a buffer...
            byte[] buffer = new byte[1024];
            int bufferLength = 0;
 
            while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
                fileOutput.write(buffer, 0, bufferLength);
                downloadedSize += bufferLength;
                //publishing the progress
                progreso=(int)(downloadedSize*100/totalSize);
                onProgressUpdate();     			
            }
            //close the output stream when complete //
            fileOutput.close();
            
         
        } catch (final MalformedURLException e) {
            showError("Error : MalformedURLException " + e);        
            e.printStackTrace();
        } catch (final IOException e) {
            showError("Error : IOException " + e);          
            e.printStackTrace();
        }
        catch (final Exception e) {
            showError("Error : Please check your internet connection " + e);
        }  
        
        return true;
    }
     
    void showError(final String err){
        
    }
    protected Dialog onCreateDialog(int id) {
		progressDialog = new ProgressDialog(this);
	       
		if (id==0){
		progressDialog.setProgressStyle(
		ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setIcon(R.drawable.itche);
		progressDialog.setTitle("Descargando . . . " );		
		progressDialog.setMessage("Espere un momento…");
		progressDialog.setIndeterminate(false);
		progressDialog.setMax(100);        
		progressDialog.setCancelable(true);
          }
		else if (id==1){
		progressDialog.setProgressStyle(
		ProgressDialog.STYLE_SPINNER);
		progressDialog.setIcon(R.drawable.itche);
		progressDialog.setTitle("Descargando . . . " );

		  }
		return progressDialog;
        }
    
    protected void onProgressUpdate(Void...progress) {
		progressDialog.setProgress(progreso) ;
		if(progreso==100)removeDialog(id);
		//se puede usar lo siguiente para ocultar el diálogo
		// como alternativa:
		// if(progreso==100)progressDialog . hide();
		
		
		}

    
    
  //------------------------------------------------------------------------------
    @Override
	protected void onDestroy()
	{
		// Break circular reference of Java objects
		if (mAnnotatedGeometriesGroup != null)
		{
			mAnnotatedGeometriesGroup.registerCallback(null);
		}
		
		if (mAnnotatedGeometriesGroupCallback != null)
		{
			mAnnotatedGeometriesGroupCallback.delete();
			mAnnotatedGeometriesGroupCallback = null;
		}

		super.onDestroy();
	}
	
    
  //------------------------------------------------------------------------------
	
    @Override
	public void onDrawFrame()
	{
		if (metaioSDK != null && mSensors != null)
		{
			SensorValues sensorValues = mSensors.getSensorValues();

			float heading = 0.0f;
			if (sensorValues.hasAttitude())
			{
				float m[] = new float[9];
				sensorValues.getAttitude().getRotationMatrix(m);

				Vector3d v = new Vector3d(m[6], m[7], m[8]);
				v = v.normalize();

				heading = (float)(-Math.atan2(v.getY(), v.getX()) - Math.PI/2.0);
			}

			IGeometry geos[] = new IGeometry[] {IGeoGps};
			Rotation rot = new Rotation((float)(Math.PI/2.0), 0.0f, -heading);
			for (IGeometry geo : geos)
			{
				if (geo != null)
				{
					geo.setRotation(rot);
				}
			}
		}

		super.onDrawFrame();
	}
	
	
	
	//------------------------------------------------------------------------------
	
  //ABAJO 

  	private IGeometry createPOIGeometry(LLACoordinate lla)
  	{
  		
  		File file2 = new File(externalStorage.getAbsolutePath()+"/"+mediaToExecute);
		if(file2.exists()){      
		//Do somehting
			
			modelGps = externalStorage.getAbsolutePath()+"/"+ mediaToExecute;
			
		}	
		else {
			
			/*
			//DESPUES LO  LEE DESDE LA SD 
			runOnUiThread(new Runnable() 
    		{
    			@Override
    			public void run() 
    			{
    				//id=0;
    		    	showDialog(id);
    			}
    		});
		downloadFile(mediaToExecute);
		*/
		/*
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		//gpsLauncher();
		//loadContents();
		
		
		modelGps = externalStorage.getAbsolutePath()+"/"+ mediaToExecute;
		
		}
		
  		
  		//String path = AssetsManager.getAssetPath(getApplicationContext(), "Recursos/metaioman.md2");
  		//String path = AssetsManager.getAssetPath(getApplicationContext(), "Recursos/cube.obj");
  		if (modelGps != null)
  		{
  			IGeometry geo = metaioSDK.createGeometry(modelGps);
  			geo.setTranslationLLA(lla);
  			geo.setLLALimitsEnabled(true);
  			geo.setScale(500);
  			return geo;
  		}
  		else
  		{
  			MetaioDebug.log(Log.ERROR, "Missing files for POI geometry");
  			return null;
  		}
  	}
	
	//------------------------------------------------------------------------------
  	final class MyAnnotatedGeometriesGroupCallback extends AnnotatedGeometriesGroupCallback
	{

		@Override
		public IGeometry loadUpdatedAnnotation(IGeometry geometry, Object userData,
				IGeometry existingAnnotation)
		{
			if (userData == null)
			{
				return null;
			}

			if (existingAnnotation != null)
			{
				// We don't update the annotation if e.g. distance has changed
				return existingAnnotation;
			}

			String title = (String)userData; // as passed to addGeometry
			String texturePath = getAnnotationImageForTitle(title);

			return metaioSDK.createGeometryFromImage(texturePath, true, false);
		}
		
		@Override
		public void onFocusStateChanged(IGeometry geometry, Object userData,
				EGEOMETRY_FOCUS_STATE oldState, EGEOMETRY_FOCUS_STATE newState) 
		{
			MetaioDebug.log("onFocusStateChanged for "+(String)userData+", "+oldState+"->"+newState);
		}
	}
	
	//------------------------------------------------------------------------------
	private String getAnnotationImageForTitle(String title)
	{
		Bitmap billboard = null;

		try
		{
			final String texturepath = getCacheDir() + "/" + title + ".png";
			Paint mPaint = new Paint();

			// Load background image and make a mutable copy
			
			float dpi = SystemInfo.getDisplayDensity(getApplicationContext());
			int scale = dpi > 240 ? 2 : 1;
			String filepath = AssetsManager.getAssetPath(getApplicationContext(), "TutorialLocationBasedAR/Assets/POI_bg" + (scale == 2 ? "@2x" : "") + ".png");
			Bitmap mBackgroundImage = BitmapFactory.decodeFile(filepath);

			billboard = mBackgroundImage.copy(Bitmap.Config.ARGB_8888, true);

			Canvas c = new Canvas(billboard);

			mPaint.setColor(Color.WHITE);
			mPaint.setTextSize(24);
			mPaint.setTypeface(Typeface.DEFAULT);
			mPaint.setTextAlign( Paint.Align.CENTER );

			float y = 40 * scale;
			float x = 30 * scale;

			// Draw POI name
			if (title.length() > 0)
			{
				String n = title.trim();

				final int maxWidth = 160 * scale;

				int i = mPaint.breakText(n, true, maxWidth, null);

				int xPos = (c.getWidth() / 2);
				int yPos = (int) ((c.getHeight() / 2) - ((mPaint.descent() + mPaint.ascent()) / 2)) ; 
				c.drawText(n.substring(0, i), xPos, yPos, mPaint);
				
				// Draw second line if valid
				if (i < n.length())
				{
					 n = n.substring(i);
					 y += 20 * scale;
					 i = mPaint.breakText(n, true, maxWidth, null);

					 if (i < n.length())
					 {
							i = mPaint.breakText(n, true, maxWidth - 20*scale, null);
							c.drawText(n.substring(0, i) + "...", x, y, mPaint);
					 }
					 else
					 {
							c.drawText(n.substring(0, i), x, y, mPaint);
					 }
				}
			}

			// Write texture file
			try
			{
				FileOutputStream out = new FileOutputStream(texturepath);
				billboard.compress(Bitmap.CompressFormat.PNG, 90, out);
				MetaioDebug.log("Texture file is saved to "+texturepath);
				return texturepath;
			}
			catch (Exception e)
			{
				MetaioDebug.log("Failed to save texture file");
				e.printStackTrace();
			}
		}
		catch (Exception e)
		{
			MetaioDebug.log("Error creating annotation texture: " + e.getMessage());
			MetaioDebug.printStackTrace(Log.DEBUG, e);
			return null;
		}
		finally
		{
			if (billboard != null)
			{
				billboard.recycle();
				billboard = null;
			}
		}

		return null;
	}
	
	//------------------------------------------------------------------------------
	
	private void setActiveModel()
	{	
			//Si hubierse otro video, se pausa el modelvideo y se inicia el otro
			//modelVideo.stopMovieTexture();
			IGeoVideo.setVisible(true);
		   // IGeoVideo.stopMovieTexture();
			IGeoVideo.startMovieTexture(true);

		// Start or pause movie according to tracking state
		mCallbackHandler.onTrackingEvent(metaioSDK.getTrackingValues());
	}
	
	//------------------------------------------------------------------------------
	@Override
	protected IMetaioSDKCallback getMetaioSDKCallbackHandler() {
		// TODO Auto-generated method stub
		return null;
	}
	
	//------------------------------------------------------------------------------
	final private class MetaioSDKCallbackHandler extends IMetaioSDKCallback 
	{
		@Override
		public void onSDKReady() 
		{
			// show GUI after SDK is ready
			runOnUiThread(new Runnable() 
			{
				@Override
				public void run() 
				{
					mGUIView.setVisibility(View.VISIBLE);
				}
			});
		}
		
	//------------------------------------------------------------------------------
	
	@Override
	public void onTrackingEvent(TrackingValuesVector trackingValues)
	{
		super.onTrackingEvent(trackingValues);

		// We only have one COS, so there can only ever be one TrackingValues structure passed.
		// Play movie if the movie button was selected and we're currently tracking.
		if (trackingValues.isEmpty() || !trackingValues.get(0).isTrackingState())
		{
			if (IGeoVideo != null )
				IGeoVideo.startMovieTexture(true);
			
			
		}
	}
	
	
	}

	  private class GetDataAsync extends AsyncTask<Integer, Integer, Boolean>
	    {
	        @Override
	        protected Boolean doInBackground(Integer... params) 
	        {
	        	gpsLauncher(); 
	            return true;
	        }    
	    } 
	 
	  /*
	  class DownloadAsyncTask extends AsyncTask<Void,Void,Void>{
			protected Void doInBackground(Void...argO){
				try {
		            URL url = new URL(download_file_path+mediaToExecute);
		            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
		 
		            urlConnection.setRequestMethod("GET");
		            urlConnection.setDoOutput(true);
		 
		            //<span id="IL_AD3" class="IL_AD">connect</span>
		            urlConnection.connect();
		 
		            //set the path where we want to <span id="IL_AD8" class="IL_AD">save</span> the file            
		            File SDCardRoot = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		            //create a new file, to save the downloaded file 
		            File file = new File(SDCardRoot,mediaToExecute);
		  
		            FileOutputStream fileOutput = new FileOutputStream(file);
		 
		            //Stream used for reading the data from the internet
		            InputStream inputStream = urlConnection.getInputStream();
		 
		            //this is the total size of the file which we are downloading
		            totalSize = urlConnection.getContentLength();
		            		            
		            //create a buffer...
		            byte[] buffer = new byte[1024];
		            int bufferLength = 0;
		 
		            while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
		                fileOutput.write(buffer, 0, bufferLength);
		                downloadedSize += bufferLength;
		                 
		                //publishing the progress
		                progreso=(int)(downloadedSize*100/totalSize);
		    			publishProgress();     			
		    			
		            }
		            
		            //close the output stream when complete //
		            fileOutput.close();
		                   
		            //removeDialog(1);
		        } catch (final MalformedURLException e) {
		            showError("Error : MalformedURLException " + e);        
		            e.printStackTrace();
		        } catch (final IOException e) {
		            showError("Error : IOException " + e);          
		            e.printStackTrace();
		        }
		        catch (final Exception e) {
		            showError("Error : Please check your internet connection " + e);
		        }     
				
				
		        
				return null;			
		  }
			protected void onProgressUpdate(Void...progress) {
				progressDialog.setProgress(progreso) ;
				if(progreso==100)removeDialog(id);
				//se puede usar lo siguiente para ocultar el diálogo
				// como alternativa:
				// if(progreso==100)progressDialog . hide();
				}
			
			protected void onPostExecute(String unused) {
	            //dismiss the dialog after the file was downloaded
	            dismissDialog(id);
	            
	        }
			
			
		}
		
	*/
}
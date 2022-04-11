package mx.gob.itche;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;



public class ServiceGps extends Service implements LocationListener {
	MediaPlayer reproductor;
	
	 private LocationManager manejador;
	 private String proveedor;
	 Criteria criteria;
	 Location localizacion;
	 
	 Double latitude;
	 Double longitude;
	   
	 String latiString="";
	 String longString="";
	  
	 //String to compare in Database
	 String latiToBD="";
	 String longToBD="";   
	 
	 GetDataAsync mTask;
	 
	 String companyName="";


	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
    public void onCreate() {
          //Toast.makeText(this,"Servicio creado", Toast.LENGTH_SHORT).show();
         reproductor = MediaPlayer.create(this, R.raw.audio);
         
         
         manejador = (LocationManager) getSystemService(LOCATION_SERVICE);
 		 Criteria criteria = new Criteria();
 	     proveedor = manejador.getBestProvider(criteria, true);
 	     localizacion = manejador.getLastKnownLocation(proveedor);  
 	     manejador.requestLocationUpdates(proveedor, 1000, 1, this);
 	     
 	    
          
           
    }

    @Override
    public int onStartCommand(Intent intenc, int flags, int idArranque) {
          //Toast.makeText(this,"Servicio arrancado "+ idArranque,Toast.LENGTH_SHORT).show();
          //reproductor.start();
          
          
         
          return START_STICKY;
    }

    @Override
    public void onDestroy() {
          Toast.makeText(this,"Servicio detenido", 
                                                                             Toast.LENGTH_SHORT).show();
          reproductor.stop();
    }
    
    public void showNotification(String companyName){

		// define sound URI, the sound to be played when there's a notification
		Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		
		// intent triggered, you can add other intent for other actions
		Intent intent = new Intent(ServiceGps.this, MainActivity.class);
		PendingIntent pIntent = PendingIntent.getActivity(ServiceGps.this, 0, intent, 0);
		
		// this is it, we'll build the notification!
		// in the addAction method, if you don't want any icon, just set the first param to 0
		Notification mNotification = new Notification.Builder(this)
			
			.setContentTitle("Punto Gps: "+companyName)
			.setContentText("lat: "+latiToBD +"long: "+longToBD)
			.setSmallIcon(R.drawable.ic_orange)
			.setContentIntent(pIntent)
			.setSound(soundUri)
			 
			.addAction(R.drawable.ic_blue, "View", pIntent)
			.addAction(0, "Remind", pIntent)
			
			.build();
		
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		// If you want to hide the notification after it was selected, do the code below
		// myNotification.flags |= Notification.FLAG_AUTO_CANCEL;
		
		notificationManager.notify(0, mNotification);
	}
	
	public void cancelNotification(int notificationId){
		
		if (Context.NOTIFICATION_SERVICE!=null) {
            String ns = Context.NOTIFICATION_SERVICE;
            NotificationManager nMgr = (NotificationManager) getApplicationContext().getSystemService(ns);
            nMgr.cancel(notificationId);
        }
	}

	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		//reproductor.start();
		 localizacion = manejador.getLastKnownLocation(proveedor);  
		 latitude = localizacion.getLatitude();
	     longitude = localizacion.getLongitude();
	     
	 	    latiString = latitude.toString();
		     longString = longitude.toString();
		     
	        if ( latiString.charAt(0)=='-'){
		    	 latiToBD= ""+latiString.charAt(0)+latiString.charAt(1)+latiString.charAt(2)+latiString.charAt(3)+latiString.charAt(4)+latiString.charAt(5)+
		    			 latiString.charAt(6)+latiString.charAt(7);    	 
		    	 
		     }else latiToBD= ""+latiString.charAt(0)+latiString.charAt(1)+latiString.charAt(2)+latiString.charAt(3)+latiString.charAt(4)+latiString.charAt(5)+
	   			 latiString.charAt(6);
	        
	        
	        
	        /**------Longitud to String database */	 
		     
		     if ( longString.charAt(0)=='-'){
		    	 
		    	 longToBD= ""+longString.charAt(0)+longString.charAt(1)+longString.charAt(2)+longString.charAt(3)+longString.charAt(4)+longString.charAt(5)+
		    			 longString.charAt(6)+longString.charAt(7);
		    	 
		    	  	 
		     }else longToBD= ""+longString.charAt(0)+longString.charAt(1)+longString.charAt(2)+longString.charAt(3)+longString.charAt(4)+longString.charAt(5)+
	   			 longString.charAt(6);
		     
		         
	         
	       //Launch de Asyntask 
	       
	         mTask = new GetDataAsync();       
		     mTask.execute(0);
		     
		
	}
	
	

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
		
	}
	
	
	  private class GetDataAsync extends AsyncTask<Integer, Integer, Boolean>
	    {
	        @Override
	        protected Boolean doInBackground(Integer... params) 
	        {
	        	getDataNewPoint();    
	            return true;
	        }    
	    } 
	
	
	public void getDataNewPoint(){
    	String result = "";
    	InputStream isr = null;
    	try{
            HttpClient httpclient = new DefaultHttpClient();
            //HttpPost httppost = new HttpPost("http://192.168.10.162/oara/PHP/statusBusiness.php"); //YOUR PHP SCRIPT ADDRESS 
            //HttpPost httppost = new HttpPost("http://castell.net84.net/statusBusiness.php");
            HttpPost httppost = new HttpPost("http://advertisingchannel.com.mx/oara/statusBusiness.php");
            //A�adir Parametros, en este caso el numero de Control
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            //params.add(new BasicNameValuePair("companyName", companyName));
            params.add(new BasicNameValuePair("latitudID", latiToBD));
            params.add(new BasicNameValuePair("longitudID", longToBD));
            httppost.setEntity(new UrlEncodedFormEntity(params));
            
            
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            isr = entity.getContent();
    }
    catch(Exception e){
            Log.e("log_tag", "Error in http connection "+e.toString());
            
    }
    //convert response to string
    try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(isr,"iso-8859-1"),8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
            }
            isr.close();
     
            result=sb.toString();
    }
    catch(Exception e){
            Log.e("log_tag", "Error  converting result "+e.toString());
    }
     
  //parse json data
    try {   	
    		
    	 JSONArray jArray = new JSONArray(result);
    	 JSONObject jdata = jArray.getJSONObject(0);    	
    	
    	 String comparingEmpty = jdata.getString("mediaLauncher"); 
    	
   		if(comparingEmpty.equalsIgnoreCase("null")){
   			
   			
				  // Toast.makeText(ServiceGps.this, "No existe datos con esa lat y long", 10000).show();
					 
   	
   			//Toast.makeText(this, "No existe registro", Toast.LENGTH_LONG).show();
       		Log.e("Oara. ServiceGPS", "No existe registro de esta latitud y longitud en la base de datos"); 	   	
      	} else {     	
     
      		Log.e("ServiceGPS", "Registro Encontrado"); 
        		 companyName=jdata.getString("companyName");
        		 showNotification(companyName);       		
        	}	  
     	   
     	   
    }catch (Exception e) {
	// TODO: handle exception
	   Log.e("log_tag_SERVICE_GPS", "Error Parsing Data "+e.toString());
   }  
	
 }

}

package com.project.toko_online.activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.project.toko_online.Config;
import com.project.toko_online.R;
import com.project.toko_online.utilities.DBHelper;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class ActivityCheckout extends AppCompatActivity {
	
	Button btnSend;
	static Button btnDate;
	static Button btnTime;
	EditText edtName, edtName2, edtPhone, edtOrderList, edtComment, edtAlamat, edtEmail, edtKota, edtProvinsi;
	ScrollView sclDetail;
	ProgressBar prgLoading;
	TextView txtAlert;
	Spinner spinner;
	

	public static DBHelper dbhelper;
	ArrayList<ArrayList<Object>> data;
	

	String Name, Name2, Date, Time, Phone, Date_n_Time, Alamat, Email, Kota, Provinsi;
	String OrderList = "";
	String Comment = "";


	private static int mYear;
	private static int mMonth;
	private static int mDay;
	private static int mHour;
	private static int mMinute;
	

	public static double Tax;
	public static String Currency;

	public static final String TIME_DIALOG_ID = "timePicker";
	public static final String DATE_DIALOG_ID = "datePicker";


	DecimalFormat formatData = new DecimalFormat("#.##");

	String Result;
	String TaxCurrencyAPI;
	int IOConnect = 0;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

		final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		final android.support.v7.app.ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setTitle(R.string.title_checkout);
		}

        edtName = (EditText) findViewById(R.id.edtName);
        edtName2 = (EditText) findViewById(R.id.edtName2);
        edtEmail = (EditText) findViewById(R.id.edtEmail);
        btnDate = (Button) findViewById(R.id.btnDate);
        btnTime = (Button) findViewById(R.id.btnTime);
        edtPhone = (EditText) findViewById(R.id.edtPhone);
        edtOrderList = (EditText) findViewById(R.id.edtOrderList);
        edtComment = (EditText) findViewById(R.id.edtComment);
        btnSend = (Button) findViewById(R.id.btnSend);
        sclDetail = (ScrollView) findViewById(R.id.sclDetail);
        prgLoading = (ProgressBar) findViewById(R.id.prgLoading);
        txtAlert = (TextView) findViewById(R.id.txtAlert);       
        edtAlamat = (EditText) findViewById(R.id.edtAlamat);
        edtKota = (EditText) findViewById(R.id.edtKota);
        edtProvinsi = (EditText) findViewById(R.id.edtProvinsi);
        
        Spinner spinner = (Spinner) findViewById(R.id.spinner1);
	     ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
	             R.array.shipping_array, android.R.layout.simple_spinner_item);
	     adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	     spinner.setAdapter(adapter);
	     
	     spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
	    	 
				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					
					switch(arg2) {
					
						case 0 :
							edtName2.setText(R.string.shipping_list_1);
							break;
						case 1 :
							edtName2.setText(R.string.shipping_list_2);
							break;
						case 2 :
							edtName2.setText(R.string.shipping_list_3);
							break;
						default :
							edtName2.setText(R.string.shipping_list_4);
							break;
					}				
				}
				
				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub
					
				}
			});

		TaxCurrencyAPI = Config.ADMIN_PANEL_URL + "/api/get-tax-and-currency.php" + "?accesskey="+Config.AccessKey;
        
        dbhelper = new DBHelper(this);
		try{
			dbhelper.openDataBase();
		}catch(SQLException sqle){
			throw sqle;
		}
		
        new getTaxCurrency().execute();
        
        btnDate.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				DialogFragment newFragment = new DatePickerFragment();
			    newFragment.show(getSupportFragmentManager(), DATE_DIALOG_ID);
			}
		});
        
        btnTime.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// show time picker dialog
				DialogFragment newFragment = new TimePickerFragment();
			    newFragment.show(getSupportFragmentManager(), TIME_DIALOG_ID);
			}
		});

        btnSend.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
				Name = edtName.getText().toString();
				Alamat = edtAlamat.getText().toString();
				Kota = edtKota.getText().toString();
				Provinsi = edtProvinsi.getText().toString();
				Email = edtEmail.getText().toString();
				Name2 = edtName2.getText().toString();
				Date = btnDate.getText().toString();
				Time = btnTime.getText().toString();
				Phone = edtPhone.getText().toString();
				Comment = edtComment.getText().toString();
				Date_n_Time = Date+" "+Time;
				if(Name.equalsIgnoreCase("") || Name2.equalsIgnoreCase("") || Email.equalsIgnoreCase("") || Alamat.equalsIgnoreCase("") || Kota.equalsIgnoreCase("") || Provinsi.equalsIgnoreCase("") ||
						Date.equalsIgnoreCase(getString(R.string.checkout_set_date)) ||
						Time.equalsIgnoreCase(getString(R.string.checkout_set_time)) ||
						Phone.equalsIgnoreCase("")){
					Toast.makeText(ActivityCheckout.this, R.string.form_alert, Toast.LENGTH_SHORT).show();
				}else if((data.size() == 0)){
					Toast.makeText(ActivityCheckout.this, R.string.order_alert, Toast.LENGTH_SHORT).show();
				}else{
					new sendData().execute();
				}
			}
		});
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			
		case android.R.id.home:
        	this.finish();
			return true;
			
		default:
			return super.onOptionsItemSelected(item);
		}
	}
    
    public static class DatePickerFragment extends DialogFragment
    implements DatePickerDialog.OnDateSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);
			
			return new DatePickerDialog(getActivity(), this, year, month, day);
		}
		
		public void onDateSet(DatePicker view, int year, int month, int day) {
			mYear = year;
			mMonth = month;
			mDay = day;
			
			btnDate.setText(new StringBuilder()
    		.append(mYear).append("-")
    		.append(mMonth + 1).append("-")
    		.append(mDay).append(" "));
		}
    }
    
    public static class TimePickerFragment extends DialogFragment
    implements TimePickerDialog.OnTimeSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final Calendar c = Calendar.getInstance();
	        int hour = c.get(Calendar.HOUR_OF_DAY);
	        int minute = c.get(Calendar.MINUTE);
			
			return new TimePickerDialog(getActivity(), this, hour, minute,
	                DateFormat.is24HourFormat(getActivity()));
		}
		
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			mHour = hourOfDay;
			mMinute = minute;
			
			btnTime.setText(new StringBuilder()
            .append(pad(mHour)).append(":")
            .append(pad(mMinute)).append(":")
            .append("00")); 	
		}
    }

    public class getTaxCurrency extends AsyncTask<Void, Void, Void>{
    	
    	getTaxCurrency(){
	 		if(!prgLoading.isShown()){
	 			prgLoading.setVisibility(View.VISIBLE);
				txtAlert.setVisibility(View.GONE);
	 		}
	 	}
    	
		@Override
		protected Void doInBackground(Void... arg0) {
			// TODO Auto-generated method stub
			parseJSONDataTax();
			return null;
		}
    	
		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
 			prgLoading.setVisibility(View.GONE);
			if(IOConnect == 0){
				new getDataTask().execute();
			}else{
				txtAlert.setVisibility(View.VISIBLE);
			}
		}
    }
    
	public void parseJSONDataTax(){
	
		try {
	        HttpClient client = new DefaultHttpClient();
	        HttpConnectionParams.setConnectionTimeout(client.getParams(), 15000);
			HttpConnectionParams.setSoTimeout(client.getParams(), 15000);
	        HttpUriRequest request = new HttpGet(TaxCurrencyAPI);
			HttpResponse response = client.execute(request);
			InputStream atomInputStream = response.getEntity().getContent();
	
			
			BufferedReader in = new BufferedReader(new InputStreamReader(atomInputStream));
		        
	        String line;
	        String str = "";
	        while ((line = in.readLine()) != null){
	        	str += line;
	        }
	    
			JSONObject json = new JSONObject(str);
			JSONArray data = json.getJSONArray("data"); // this is the "items: [ ] part
				
				
			JSONObject object_tax = data.getJSONObject(0); 
			JSONObject tax = object_tax.getJSONObject("tax_n_currency");
			    
			Tax = Double.parseDouble(tax.getString("Value"));
				   
			JSONObject object_currency = data.getJSONObject(1); 
			JSONObject currency = object_currency.getJSONObject("tax_n_currency");
				    
			Currency = currency.getString("Value");
					
		} catch (MalformedURLException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		} catch (IOException e) {
		    // TODO Auto-generated catch block
			IOConnect = 1;
		    e.printStackTrace();
		} catch (JSONException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}	
	}
	
    public class getDataTask extends AsyncTask<Void, Void, Void>{
    	
    	
		@Override
		protected Void doInBackground(Void... arg0) {
			// TODO Auto-generated method stub
			getDataFromDatabase();
			return null;
		}
    	
		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			prgLoading.setVisibility(View.GONE);
			sclDetail.setVisibility(View.VISIBLE);
			
		}
    }
    
    public class sendData extends AsyncTask<Void, Void, Void> {
		ProgressDialog dialog;
		
		@Override
		 protected void onPreExecute() {
		  // TODO Auto-generated method stub
			 dialog= ProgressDialog.show(ActivityCheckout.this, "", 
	                 getString(R.string.sending_alert), true);
		  	
		 }

		 @Override
		 protected Void doInBackground(Void... params) {
		  // TODO Auto-generated method stub
			 // send data to server and store result to variable
			 Result = getRequest(Name, Alamat, Kota, Provinsi, Email, Name2, Date_n_Time, Phone, OrderList, Comment);
		  return null;
		 }

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			dialog.dismiss();
			resultAlert(Result);
			
			
		}
	}

    public void resultAlert(String HasilProses){
		if(HasilProses.trim().equalsIgnoreCase("OK")){
			Toast.makeText(ActivityCheckout.this, R.string.ok_alert, Toast.LENGTH_SHORT).show();
			Intent i = new Intent(ActivityCheckout.this, ActivityConfirmMessage.class);
			startActivity(i);
			finish();
		}else if(HasilProses.trim().equalsIgnoreCase("Failed")){
			Toast.makeText(ActivityCheckout.this, R.string.failed_alert, Toast.LENGTH_SHORT).show();
		}else{
			Log.d("HasilProses", HasilProses);
		}
	}
	
	public String getRequest(String name, String alamat, String kota, String provinsi, String email, String name2, String date_n_time, String phone, String orderlist, String comment){
		String result = "";
		
        HttpClient client = new DefaultHttpClient();
        HttpPost request = new HttpPost(Config.ADMIN_PANEL_URL + "/api/add-reservation.php");
        
        try{
        	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(6);
        	nameValuePairs.add(new BasicNameValuePair("name", name));
        	nameValuePairs.add(new BasicNameValuePair("alamat", alamat));
        	nameValuePairs.add(new BasicNameValuePair("kota", kota));
        	nameValuePairs.add(new BasicNameValuePair("provinsi", provinsi));
        	nameValuePairs.add(new BasicNameValuePair("email", email));
            nameValuePairs.add(new BasicNameValuePair("name2", name2));
            nameValuePairs.add(new BasicNameValuePair("date_n_time", date_n_time));
            nameValuePairs.add(new BasicNameValuePair("phone", phone));
            nameValuePairs.add(new BasicNameValuePair("order_list", orderlist));
            nameValuePairs.add(new BasicNameValuePair("comment", comment));
            request.setEntity(new UrlEncodedFormEntity(nameValuePairs,HTTP.UTF_8));
        	HttpResponse response = client.execute(request);
            result = request(response);
        }catch(Exception ex){
        	result = "Unable to connect.";
        }
        return result;
     }

	public static String request(HttpResponse response){
	    String result = "";
	    try{
	        InputStream in = response.getEntity().getContent();
	        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
	        StringBuilder str = new StringBuilder();
	        String line = null;
	        while((line = reader.readLine()) != null){
	            str.append(line + "\n");
	        }
	        in.close();
	        result = str.toString();
	    }catch(Exception ex){
	        result = "Error";
	    }
	    return result;
	}

    public void getDataFromDatabase(){
    	
    	data = dbhelper.getAllData();

    	double Order_price = 0;
    	double Total_price = 0;
    	double tax = 0;
    	
    	for(int i=0;i<data.size();i++){
    		ArrayList<Object> row = data.get(i);
    		
    		String Menu_name = row.get(1).toString();
    		String Quantity = row.get(2).toString();
    		double Sub_total_price = Double.parseDouble(formatData.format(Double.parseDouble(row.get(3).toString())));
    		Order_price += Sub_total_price;
    		
    		OrderList += (Quantity+" "+Menu_name+" "+Sub_total_price+" "+Currency+",\n");
    	}
    	
    	if(OrderList.equalsIgnoreCase("")){
    		OrderList += getString(R.string.no_order_menu);
    	}
    	
    	tax = Double.parseDouble(formatData.format(Order_price *(Tax /100)));
    	Total_price = Double.parseDouble(formatData.format(Order_price - tax));
    	OrderList += "\nOrder: "+Order_price+" "+Currency+
    			"\nTax: "+Tax+"%: "+tax+" "+Currency+
    			"\nTotal: "+Total_price+" "+Currency;
    	edtOrderList.setText(OrderList);
    }
    
    private static String pad(int c) {
        if (c >= 10){
             return String.valueOf(c);
        }else{
        	return "0" + String.valueOf(c);
        }
    }

    @Override
    public void onBackPressed() {
    	// TODO Auto-generated method stub
    	super.onBackPressed();
    	dbhelper.close();
    	finish();
    }
    
    @Override
	public void onConfigurationChanged(final Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
	}
}

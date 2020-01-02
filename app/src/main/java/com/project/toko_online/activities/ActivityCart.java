package com.project.toko_online.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.project.toko_online.Config;
import com.project.toko_online.R;
import com.project.toko_online.adapters.AdapterCart;
import com.project.toko_online.utilities.DBHelper;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
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

public class ActivityCart extends AppCompatActivity {


    ListView listOrder;
    ProgressBar prgLoading;
    TextView txtTotalLabel, txtTotal, txtAlert;
    RelativeLayout lytOrder;

    DBHelper dbhelper;
    AdapterCart mola;



    public static double Tax;
    public static String Currency;


    ArrayList<ArrayList<Object>> data;
    public static ArrayList<Integer> Menu_ID = new ArrayList<Integer>();
    public static ArrayList<String> Menu_name = new ArrayList<String>();
    public static ArrayList<Integer> Quantity = new ArrayList<Integer>();
    public static ArrayList<Double> Sub_total_price = new ArrayList<Double>();

    double Total_price;
    final int CLEAR_ALL_ORDER = 0;
    final int CLEAR_ONE_ORDER = 1;
    int FLAG;
    int ID;
    String TaxCurrencyAPI;
    int IOConnect = 0;


    DecimalFormat formatData = new DecimalFormat("#.##");


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_cart);
        }

        FloatingActionButton clear = (FloatingActionButton) findViewById(R.id.fabClear);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showClearDialog(CLEAR_ALL_ORDER, 1111);
            }
        });

        FloatingActionButton checkout = (FloatingActionButton) findViewById(R.id.fabCheckout);
        checkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbhelper.close();
                Intent i = new Intent(ActivityCart.this, ActivityCheckout.class);
                startActivity(i);
            }
        });


        prgLoading = (ProgressBar) findViewById(R.id.prgLoading);
        listOrder = (ListView) findViewById(R.id.listOrder);
        txtTotalLabel = (TextView) findViewById(R.id.txtTotalLabel);
        txtTotal = (TextView) findViewById(R.id.txtTotal);
        txtAlert = (TextView) findViewById(R.id.txtAlert);

        lytOrder = (RelativeLayout) findViewById(R.id.lytOrder);


        TaxCurrencyAPI = Config.ADMIN_PANEL_URL + "/api/get-tax-and-currency.php" + "?accesskey=" + Config.AccessKey;

        mola = new AdapterCart(this);
        dbhelper = new DBHelper(this);


        try {
            dbhelper.openDataBase();
        } catch (SQLException sqle) {
            throw sqle;
        }


        new getTaxCurrency().execute();


        listOrder.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                    long arg3) {
                // show confirmation dialog
                showClearDialog(CLEAR_ONE_ORDER, Menu_ID.get(position));
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_cart, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:

                this.finish();
                return true;

            case R.id.checkout:
                dbhelper.close();
                Intent i = new Intent(ActivityCart.this, ActivityCheckout.class);
                startActivity(i);
                return true;

            case R.id.clear:
                showClearDialog(CLEAR_ALL_ORDER, 1111);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    void showClearDialog(int flag, int id) {
        FLAG = flag;
        ID = id;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirm);
        switch (FLAG) {
            case 0:
                builder.setMessage(getString(R.string.clear_all_order));
                break;
            case 1:
                builder.setMessage(getString(R.string.clear_one_order));
                break;
        }
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                switch (FLAG) {
                    case 0:

                        dbhelper.deleteAllData();
                        listOrder.invalidateViews();
                        clearData();
                        new getDataTask().execute();
                        break;
                    case 1:

                        dbhelper.deleteData(ID);
                        listOrder.invalidateViews();
                        clearData();
                        new getDataTask().execute();
                        break;
                }

            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub

                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();

    }


    public class getTaxCurrency extends AsyncTask<Void, Void, Void> {

        getTaxCurrency() {
            if (!prgLoading.isShown()) {
                prgLoading.setVisibility(View.VISIBLE);
                txtAlert.setVisibility(View.GONE);
            }
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // TODO Auto-generated method stub
            // parse json data from server in background
            parseJSONDataTax();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub

            prgLoading.setVisibility(View.GONE);

            if (IOConnect == 0) {
                new getDataTask().execute();
            } else {
                txtAlert.setVisibility(View.VISIBLE);
                txtAlert.setText(R.string.alert);
            }

        }
    }


    public void parseJSONDataTax() {

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
            while ((line = in.readLine()) != null) {
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


    void clearData() {
        Menu_ID.clear();
        Menu_name.clear();
        Quantity.clear();
        Sub_total_price.clear();
    }


    public class getDataTask extends AsyncTask<Void, Void, Void> {


        getDataTask() {
            if (!prgLoading.isShown()) {
                prgLoading.setVisibility(View.VISIBLE);
                lytOrder.setVisibility(View.GONE);
                txtAlert.setVisibility(View.GONE);
            }
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // TODO Auto-generated method stub

            getDataFromDatabase();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub

            txtTotal.setText(Total_price + " " + Currency);
            txtTotalLabel.setText(getString(R.string.total_order) + " (Tax " + Tax + "%)");
            prgLoading.setVisibility(View.GONE);

            if (Menu_ID.size() > 0) {
                lytOrder.setVisibility(View.VISIBLE);
                listOrder.setAdapter(mola);
            } else {
                txtAlert.setVisibility(View.VISIBLE);
            }

        }
    }


    public void getDataFromDatabase() {

        Total_price = 0;
        clearData();
        data = dbhelper.getAllData();


        for (int i = 0; i < data.size(); i++) {
            ArrayList<Object> row = data.get(i);

            Menu_ID.add(Integer.parseInt(row.get(0).toString()));
            Menu_name.add(row.get(1).toString());
            Quantity.add(Integer.parseInt(row.get(2).toString()));
            Sub_total_price.add(Double.parseDouble(formatData.format(Double.parseDouble(row.get(3).toString()))));
            Total_price += Sub_total_price.get(i);
        }


        Total_price -= (Total_price * (Tax / 100));
        Total_price = Double.parseDouble(formatData.format(Total_price));
    }


    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
        dbhelper.close();
        finish();
    }


    @Override
    public void onConfigurationChanged(final Configuration newConfig) {

        super.onConfigurationChanged(newConfig);
    }


}

package com.anilduyguc.barcodescannerproject;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.anilduyguc.barcodescannerproject.data.TempData;
import com.anilduyguc.barcodescannerproject.view.About;
import com.anilduyguc.barcodescannerproject.view.SellerInfo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity{
    private static final String API_KEY="3g4tl8awaycfo0s5yrzrktb2ea65q3";
    private AppCompatButton scanButton;
    private AppCompatButton aboutButton;
    private AppCompatButton productListButton;
    private FirebaseFirestore firestore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scanButton = findViewById(R.id.scanButton);
        aboutButton = findViewById(R.id.aboutButton);
        productListButton = findViewById(R.id.productListButton);
        scanButton.setOnClickListener(v -> {
            scanCode();
        });
        aboutButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, About.class)));
        productListButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SellerInfo.class);
            startActivity(intent);
        });

    }

    private void scanCode() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(CaptureActivity.class);
        integrator.setOrientationLocked(false);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Scanning Code");
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode,data);
        if(result != null){
            if(result.getContents() != null){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Result:"+result.getContents());
                Toast.makeText(this, "Barcode No:" + result.getContents(), Toast.LENGTH_SHORT).show();

                RequestQueue queue = Volley.newRequestQueue(this);
                Log.d("Barcode", result.getContents());
                String rapidAPIUrl = "https://barcodes1.p.rapidapi.com/?query="+result.getContents(); // renews in 1 month 18 request left
                //String barcodeLookUpUrl = "https://api.barcodelookup.com/v3/products?barcode="+result.getContents()+"&key=g4wulvk4xbeos6bqh2nhd7gukebhec"; // Expires in 2 weeks 47 request left
                String barcodeLookUpUrl = "https://api.barcodelookup.com/v3/products?barcode="+result.getContents()+"&key="+API_KEY; // Expires in 2 weeks 47 request left
                this.fetchApiFromBarcodeAPI(barcodeLookUpUrl, queue);
                //this.fetchApiRapidAPI(rapidAPIUrl, queue);
                //this.getFromString();

            } else {
                Toast.makeText(this, "No Results", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void getFromString() {
        try {
            JSONObject body = new JSONObject(this.getJsonOfBarcodeLookUp());
            JSONArray products = body.getJSONArray("products");
            //Log.d("Products", String.valueOf(products));
            JSONObject main = products.getJSONObject(0);
            String barcodeNumber = main.getString("barcode_number");
            String title = main.getString("title");
            String category = main.getString("category");
            String description = main.getString("description");
            String images = main.getJSONArray("images").getString(0);
            JSONArray contributors = main.getJSONArray("contributors");
            String author = "";
            String publisher = "";
            for(int i = 0; i<contributors.length(); i++){
                JSONObject jsonObject = contributors.getJSONObject(i);
                if(jsonObject.getString("role").equals("author")){
                    author = jsonObject.getString("name");
                }
                if(jsonObject.getString("role").equals("publisher")){
                    publisher = jsonObject.getString("name");
                }
            }
            JSONArray stores = main.getJSONArray("stores");
            ArrayList<TempData> product = new ArrayList<>();
            for(int i =0; i< stores.length(); i++){
                JSONObject jsonObject = stores.getJSONObject(i);
                String sellerName = jsonObject.getString("name");
                String price = jsonObject.getString("price");
                String imageUrl = jsonObject.getString("link");
                product.add(new TempData(sellerName, Double.parseDouble(price), imageUrl));
            }

            Log.d("Barcode NO: ", barcodeNumber);
            Log.d("Title: ", title);
            Log.d("Category NO: ", category);
            Log.d("Description NO: ", description);
            Log.d("Images NO: ", images);
            Log.d("Author NO: ", author);
            Log.d("Publisher NO: ", publisher);
            for(int i =0; i< product.size(); i++){
                Log.d("Index of: "+ i + " -SellerName", product.get(i).getSellerName());
                Log.d("Index of: "+ i + " -Price", String.valueOf(product.get(i).getPrice()));
                Log.d("Index of: "+ i + " -ImageURl", product.get(i).getImageUrl());
            }
            this.addToFirebase(barcodeNumber, title, category, description, images, author, publisher, product);
            //Log.d("Main", String.valueOf(main));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void fetchApiRapidAPI(String rapidAPIUrl, RequestQueue queue) {
        // TO DO ---> change json Objects
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, rapidAPIUrl, null, body -> {
            Log.d("Response:", body.toString());
            JSONArray products = null;
            try {
                products = body.getJSONArray("products");
                JSONObject main = products.getJSONObject(0);
                String barcodeNumber = main.getString("barcode_number");
                String title = main.getString("title");
                String category = main.getString("category");
                String description = main.getString("description");
                String images = main.getJSONArray("images").getString(0);
                JSONArray contributors = main.getJSONArray("contributors");
                String author = "";
                String publisher = "";
                for(int i = 0; i<contributors.length(); i++){
                    JSONObject jsonObject = contributors.getJSONObject(i);
                    if(jsonObject.getString("role").equals("author")){
                        author = jsonObject.getString("name");
                    }
                    if(jsonObject.getString("role").equals("publisher")){
                        publisher = jsonObject.getString("name");
                    }
                }
                JSONArray stores = main.getJSONArray("stores");
                ArrayList<TempData> product = new ArrayList<>();
                for(int i =0; i< stores.length(); i++){
                    JSONObject jsonObject = stores.getJSONObject(i);
                    String sellerName = jsonObject.getString("name");
                    String price = jsonObject.getString("price");
                    String imageUrl = jsonObject.getString("link");
                    product.add(new TempData(sellerName, Double.parseDouble(price), imageUrl));
                }

                Log.d("Barcode NO: ", barcodeNumber);
                Log.d("Title: ", title);
                Log.d("Category NO: ", category);
                Log.d("Description NO: ", description);
                Log.d("Images NO: ", images);
                Log.d("Author NO: ", author);
                Log.d("Publisher NO: ", publisher);
                for(int i =0; i< product.size(); i++){
                    Log.d("Index of: "+ i + " -SellerName", product.get(i).getSellerName());
                    Log.d("Index of: "+ i + " -Price", String.valueOf(product.get(i).getPrice()));
                    Log.d("Index of: "+ i + " -ImageURl", product.get(i).getImageUrl());
                }
                addToFirebase(barcodeNumber, title, category, description, images, author, publisher, product);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //Log.d("Products", String.valueOf(products));
        }, error -> error.printStackTrace()){
            @Override
            public Map getHeaders() throws AuthFailureError {
                HashMap params = new HashMap();
                params.put("X-RapidAPI-Key", "68b180a35bmshc55bed4cf39be30p17c4bdjsn0521a128e399");
                params.put("X-RapidAPI-Host", "barcodes1.p.rapidapi.com");
                return params;
            }
        };
        queue.add(jsonObjectRequest);
    }

    private void fetchApiFromBarcodeAPI(String url, RequestQueue queue) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject body) {
                Log.d("Response:", body.toString());
                JSONArray products = null;
                try {
                    products = body.getJSONArray("products");
                    JSONObject main = products.getJSONObject(0);
                    String barcodeNumber = main.getString("barcode_number");
                    String title = main.getString("title");
                    String category = main.getString("category");
                    String description = main.getString("description");
                    String images = main.getJSONArray("images").getString(0);
                    JSONArray contributors = main.getJSONArray("contributors");
                    String author = "";
                    String publisher = "";
                    for(int i = 0; i<contributors.length(); i++){
                        JSONObject jsonObject = contributors.getJSONObject(i);
                        if(jsonObject.getString("role").equals("author")){
                            author = jsonObject.getString("name");
                        }
                        if(jsonObject.getString("role").equals("publisher")){
                            publisher = jsonObject.getString("name");
                        }
                    }
                    JSONArray stores = main.getJSONArray("stores");
                    ArrayList<TempData> product = new ArrayList<>();
                    for(int i =0; i< stores.length(); i++){
                        JSONObject jsonObject = stores.getJSONObject(i);
                        String sellerName = jsonObject.getString("name");
                        String price = jsonObject.getString("price");
                        String imageUrl = jsonObject.getString("link");
                        product.add(new TempData(sellerName, Double.parseDouble(price), imageUrl));
                    }

                    Log.d("Barcode NO: ", barcodeNumber);
                    Log.d("Title: ", title);
                    Log.d("Category NO: ", category);
                    Log.d("Description NO: ", description);
                    Log.d("Images NO: ", images);
                    Log.d("Author NO: ", author);
                    Log.d("Publisher NO: ", publisher);
                    for(int i =0; i< product.size(); i++){
                        Log.d("Index of: "+ i + " -SellerName", product.get(i).getSellerName());
                        Log.d("Index of: "+ i + " -Price", String.valueOf(product.get(i).getPrice()));
                        Log.d("Index of: "+ i + " -ImageURl", product.get(i).getImageUrl());
                    }
                    addToFirebase(barcodeNumber, title, category, description, images, author, publisher, product);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //Log.d("Products", String.valueOf(products));
            }
        }, error -> error.printStackTrace());
        queue.add(jsonObjectRequest);
    }

    private void addToFirebase(String barcodeNumber, String title, String category, String description, String images, String author, String publisher, ArrayList<TempData> product) {
        firestore = FirebaseFirestore.getInstance();
        Map<String, Object> products = new HashMap<>();
        for(int i=0;i<product.size(); i++){
            products.put("seller", product.get(i).getSellerName());
            products.put("price", product.get(i).getPrice());
            products.put("url", product.get(i).getImageUrl());
            products.put("isbnNo", barcodeNumber);
            products.put("title", title);
            products.put("category", category);
            products.put("imageUrl", images);
            products.put("description", description);
            products.put("author", author);
            products.put("publisher", publisher);

            firestore.collection("products").add(products)
                    .addOnSuccessListener(documentReference -> Toast.makeText(MainActivity.this, "Saved successfully", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Failure", Toast.LENGTH_SHORT).show());
        }
    }

    private String getJsonOfBarcodeLookUp(){
        return "{\n" +
                "    \"products\": [\n" +
                "        {\n" +
                "            \"barcode_number\": \"9781911223139\",\n" +
                "            \"barcode_formats\": \"ISBN-10 1911223135, ISBN-13 9781911223139\",\n" +
                "            \"mpn\": \"9781911223139\",\n" +
                "            \"model\": \"\",\n" +
                "            \"asin\": \"\",\n" +
                "            \"title\": \"Best Murder in Show\",\n" +
                "            \"category\": \"Media > Books\",\n" +
                "            \"manufacturer\": \"Debbie Young\",\n" +
                "            \"brand\": \"Debbie Young\",\n" +
                "            \"contributors\": [\n" +
                "                {\n" +
                "                    \"role\": \"author\",\n" +
                "                    \"name\": \"Debbie Young\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"role\": \"publisher\",\n" +
                "                    \"name\": \"Hawkesbury Press\"\n" +
                "                }\n" +
                "            ],\n" +
                "            \"age_group\": \"adult\",\n" +
                "            \"ingredients\": \"\",\n" +
                "            \"nutrition_facts\": \"\",\n" +
                "            \"energy_efficiency_class\": \"\",\n" +
                "            \"color\": \"\",\n" +
                "            \"gender\": \"\",\n" +
                "            \"material\": \"\",\n" +
                "            \"pattern\": \"\",\n" +
                "            \"format\": \"\",\n" +
                "            \"multipack\": \"\",\n" +
                "            \"size\": \"0.49 x 8 x 0.53\",\n" +
                "            \"length\": \"\",\n" +
                "            \"width\": \"\",\n" +
                "            \"height\": \"\",\n" +
                "            \"weight\": \"\",\n" +
                "            \"release_date\": \"\",\n" +
                "            \"description\": \"When Sophie Sayers inherits a cottage in a sleepy English Cotswold village, she's hoping for a quieter life than the one she's running away from. What she gets instead is a dead body on a carnival float, and an extraordinary assortment of suspects. Is the enigmatic bookseller Hector Munro all he seems? And what about the over-friendly neighbour who brings her jars of honey? Not to mention the eccentric village shopkeeper, show committee, writers' group and drama club, all suspiciously keen to welcome her to their midst. For fans of cosy (cozy) mysteries everywhere, Best Murder in Show will make you laugh out loud at the idiosyncrasies of English country life, and rack your brains to discover the murderer before Sophie can. | Best Murder in Show Paperback | Indigo Chapters.\",\n" +
                "            \"features\": [],\n" +
                "            \"images\": [\n" +
                "                \"https://images.barcodelookup.com/8559/85595691-1.jpg\"\n" +
                "            ],\n" +
                "            \"last_update\": \"2022-08-30 18:48:04\",\n" +
                "            \"stores\": [\n" +
                "                {\n" +
                "                    \"name\": \"AbeBooks\",\n" +
                "                    \"country\": \"US\",\n" +
                "                    \"currency\": \"USD\",\n" +
                "                    \"currency_symbol\": \"$\",\n" +
                "                    \"price\": \"4.49\",\n" +
                "                    \"sale_price\": \"4.49\",\n" +
                "                    \"tax\": [],\n" +
                "                    \"link\": \"https://www.abebooks.com/Best-Murder-Show-Sophie-Sayers-Village/31119532161/bd\",\n" +
                "                    \"item_group_id\": \"\",\n" +
                "                    \"availability\": \"\",\n" +
                "                    \"condition\": \"used\",\n" +
                "                    \"shipping\": [],\n" +
                "                    \"last_update\": \"2022-08-09 08:57:49\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"name\": \"BetterWorld.com - New, Used, Rare Books & Textbooks\",\n" +
                "                    \"country\": \"US\",\n" +
                "                    \"currency\": \"USD\",\n" +
                "                    \"currency_symbol\": \"$\",\n" +
                "                    \"price\": \"6.98\",\n" +
                "                    \"sale_price\": \"\",\n" +
                "                    \"tax\": [],\n" +
                "                    \"link\": \"https://www.BetterWorldBooks.com/product/detail/Best-Murder-in-Show-9781911223139?utm_source=CJ_feed\",\n" +
                "                    \"item_group_id\": \"\",\n" +
                "                    \"availability\": \"\",\n" +
                "                    \"condition\": \"\",\n" +
                "                    \"shipping\": [],\n" +
                "                    \"last_update\": \"2021-06-22 04:55:55\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"name\": \"ThriftBooks.com\",\n" +
                "                    \"country\": \"US\",\n" +
                "                    \"currency\": \"USD\",\n" +
                "                    \"currency_symbol\": \"$\",\n" +
                "                    \"price\": \"7.68\",\n" +
                "                    \"sale_price\": \"\",\n" +
                "                    \"tax\": [],\n" +
                "                    \"link\": \"https://www.thriftbooks.com/w/best-murder-in-show_debbie--young/18427996/#isbn=1911223135\",\n" +
                "                    \"item_group_id\": \"\",\n" +
                "                    \"availability\": \"\",\n" +
                "                    \"condition\": \"\",\n" +
                "                    \"shipping\": [],\n" +
                "                    \"last_update\": \"2021-06-22 03:01:17\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"name\": \"OnBuy.com\",\n" +
                "                    \"country\": \"GB\",\n" +
                "                    \"currency\": \"GBP\",\n" +
                "                    \"currency_symbol\": \"£\",\n" +
                "                    \"price\": \"6.92\",\n" +
                "                    \"sale_price\": \"\",\n" +
                "                    \"tax\": [],\n" +
                "                    \"link\": \"https://www.onbuy.com/gb/best-murder-in-show-a-sophie-sayers-village-mystery-volume-1-sophie-sayers-village-mysteries~c8053~p4195805/?exta=cjunct&stat=eyJpcCI6IjYuOTIiLCJkcCI6MCwibGlkIjoiMTg5NjUyMjAiLCJzIjoiMyIsInQiOjE1ODQ4Nj\",\n" +
                "                    \"item_group_id\": \"\",\n" +
                "                    \"availability\": \"\",\n" +
                "                    \"condition\": \"\",\n" +
                "                    \"shipping\": [],\n" +
                "                    \"last_update\": \"2021-06-22 04:00:03\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"name\": \"OnBuy.com UK\",\n" +
                "                    \"country\": \"GB\",\n" +
                "                    \"currency\": \"GBP\",\n" +
                "                    \"currency_symbol\": \"£\",\n" +
                "                    \"price\": \"8.16\",\n" +
                "                    \"sale_price\": \"\",\n" +
                "                    \"tax\": [],\n" +
                "                    \"link\": \"https://www.onbuy.com/gb/best-murder-in-show-a-sophie-sayers-village-mystery-volume-1-sophie-sayers-village-mysteries~c8053~p4195805/?exta=cjunct&stat=eyJpcCI6IjguMTYiLCJkcCI6MCwibGlkIjoiMTg5NjUyMjAiLCJzIjoiMyIsInQiOjE\",\n" +
                "                    \"item_group_id\": \"\",\n" +
                "                    \"availability\": \"\",\n" +
                "                    \"condition\": \"\",\n" +
                "                    \"shipping\": [],\n" +
                "                    \"last_update\": \"2021-06-22 02:35:54\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"name\": \"Barnes & Noble\",\n" +
                "                    \"country\": \"US\",\n" +
                "                    \"currency\": \"USD\",\n" +
                "                    \"currency_symbol\": \"$\",\n" +
                "                    \"price\": \"9.99\",\n" +
                "                    \"sale_price\": \"\",\n" +
                "                    \"tax\": [],\n" +
                "                    \"link\": \"https://www.barnesandnoble.com/w/best-murder-in-show-debbie-young/1126164478?ean=9781911223139\",\n" +
                "                    \"item_group_id\": \"\",\n" +
                "                    \"availability\": \"\",\n" +
                "                    \"condition\": \"\",\n" +
                "                    \"shipping\": [],\n" +
                "                    \"last_update\": \"2021-06-22 03:50:04\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"name\": \"Target\",\n" +
                "                    \"country\": \"US\",\n" +
                "                    \"currency\": \"USD\",\n" +
                "                    \"currency_symbol\": \"$\",\n" +
                "                    \"price\": \"9.99\",\n" +
                "                    \"sale_price\": \"\",\n" +
                "                    \"tax\": [],\n" +
                "                    \"link\": \"https://www.target.com/p/best-murder-in-show-sophie-sayers-village-mysteries-by-debbie-young-paperback/-/A-79286392&intsrc=CATF_1444\",\n" +
                "                    \"item_group_id\": \"\",\n" +
                "                    \"availability\": \"\",\n" +
                "                    \"condition\": \"new\",\n" +
                "                    \"shipping\": [],\n" +
                "                    \"last_update\": \"2021-11-02 16:58:04\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"name\": \"Wordery (US)\",\n" +
                "                    \"country\": \"GB\",\n" +
                "                    \"currency\": \"GBP\",\n" +
                "                    \"currency_symbol\": \"£\",\n" +
                "                    \"price\": \"11.65\",\n" +
                "                    \"sale_price\": \"\",\n" +
                "                    \"tax\": [],\n" +
                "                    \"link\": \"https://wordery.com/best-murder-in-show-debbie-young-9781911223139\",\n" +
                "                    \"item_group_id\": \"\",\n" +
                "                    \"availability\": \"in stock\",\n" +
                "                    \"condition\": \"new\",\n" +
                "                    \"shipping\": [],\n" +
                "                    \"last_update\": \"2022-08-30 18:48:04\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"name\": \"Indigo Books & Music\",\n" +
                "                    \"country\": \"CA\",\n" +
                "                    \"currency\": \"CAD\",\n" +
                "                    \"currency_symbol\": \"$\",\n" +
                "                    \"price\": \"12.99\",\n" +
                "                    \"sale_price\": \"11.16\",\n" +
                "                    \"tax\": [],\n" +
                "                    \"link\": \"https://www.chapters.indigo.ca/en-ca/books/product/9781911223139-item.html\",\n" +
                "                    \"item_group_id\": \"\",\n" +
                "                    \"availability\": \"\",\n" +
                "                    \"condition\": \"new\",\n" +
                "                    \"shipping\": [],\n" +
                "                    \"last_update\": \"2022-08-12 19:54:27\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"name\": \"averdo DE\",\n" +
                "                    \"country\": \"EU\",\n" +
                "                    \"currency\": \"EUR\",\n" +
                "                    \"currency_symbol\": \"€\",\n" +
                "                    \"price\": \"28.29\",\n" +
                "                    \"sale_price\": \"\",\n" +
                "                    \"tax\": [],\n" +
                "                    \"link\": \"https://shop.averdo.com/product/81090991\",\n" +
                "                    \"item_group_id\": \"\",\n" +
                "                    \"availability\": \"in stock\",\n" +
                "                    \"condition\": \"new\",\n" +
                "                    \"shipping\": [],\n" +
                "                    \"last_update\": \"2022-08-30 21:43:56\"\n" +
                "                }\n" +
                "            ],\n" +
                "            \"reviews\": []\n" +
                "        }\n" +
                "    ]\n" +
                "}\n";
    }

    private String getJsonOfRapidAPI(){
        return "{\n" +
                "    \"product\": {\n" +
                "        \"artist\": null,\n" +
                "        \"attributes\": {\n" +
                "            \"mpn\": \"9781911223139\",\n" +
                "            \"size\": \"0.49 x 8 x 0.53\",\n" +
                "            \"age_group\": \"adult\"\n" +
                "        },\n" +
                "        \"author\": \"Debbie Young\",\n" +
                "        \"barcode_formats\": {\n" +
                "            \"isbn_10\": \"1911223135\",\n" +
                "            \"isbn_13\": \"9781911223139\"\n" +
                "        },\n" +
                "        \"brand\": null,\n" +
                "        \"category\": [\n" +
                "            \"Media\",\n" +
                "            \"Books\"\n" +
                "        ],\n" +
                "        \"description\": \"When Sophie Sayers inherits a cottage in a sleepy English Cotswold village, she's hoping for a quieter life than the one she's running away from. What she gets instead is a dead body on a carnival float, and an extraordinary assortment of suspects. Is the enigmatic bookseller Hector Munro all he seems? And what about the over-friendly neighbour who brings her jars of honey? Not to mention the eccentric village shopkeeper, show committee, writers' group and drama club, all suspiciously keen to welcome her to their midst. For fans of cosy (cozy) mysteries everywhere, Best Murder in Show will make you laugh out loud at the idiosyncrasies of English country life, and rack your brains to discover the murderer before Sophie can. | Best Murder in Show Paperback | Indigo Chapters.\",\n" +
                "        \"features\": [],\n" +
                "        \"images\": [\n" +
                "            \"https://images.barcodelookup.com/8559/85595691-1.jpg\"\n" +
                "        ],\n" +
                "        \"ingredients\": null,\n" +
                "        \"manufacturer\": null,\n" +
                "        \"online_stores\": [\n" +
                "            {\n" +
                "                \"name\": \"AbeBooks\",\n" +
                "                \"price\": \"$4.49\",\n" +
                "                \"url\": \"https://www.abebooks.com/Best-Murder-Show-Sophie-Sayers-Village/31119532161/bd\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"name\": \"BetterWorld.com - New, Used, Rare Books & Textbooks\",\n" +
                "                \"price\": \"$6.98\",\n" +
                "                \"url\": \"https://www.BetterWorldBooks.com/product/detail/Best-Murder-in-Show-9781911223139?utm_source=CJ_feed\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"name\": \"ThriftBooks.com\",\n" +
                "                \"price\": \"$7.68\",\n" +
                "                \"url\": \"https://www.thriftbooks.com/w/best-murder-in-show_debbie--young/18427996/#isbn=1911223135\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"name\": \"OnBuy.com\",\n" +
                "                \"price\": \"£6.92\",\n" +
                "                \"url\": \"https://www.onbuy.com/gb/best-murder-in-show-a-sophie-sayers-village-mystery-volume-1-sophie-sayers-village-mysteries~c8053~p4195805/?exta=cjunct&stat=eyJpcCI6IjYuOTIiLCJkcCI6MCwibGlkIjoiMTg5NjUyMjAiLCJzIjoiMyIsInQiOjE1ODQ4Nj\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"name\": \"OnBuy.com UK\",\n" +
                "                \"price\": \"£8.16\",\n" +
                "                \"url\": \"https://www.onbuy.com/gb/best-murder-in-show-a-sophie-sayers-village-mystery-volume-1-sophie-sayers-village-mysteries~c8053~p4195805/?exta=cjunct\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"name\": \"Barnes & Noble\",\n" +
                "                \"price\": \"$9.99\",\n" +
                "                \"url\": \"https://www.barnesandnoble.com/w/best-murder-in-show-debbie-young/1126164478?ean=9781911223139\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"title\": \"Best Murder in Show\"\n" +
                "    }\n" +
                "}\n";
    }

}
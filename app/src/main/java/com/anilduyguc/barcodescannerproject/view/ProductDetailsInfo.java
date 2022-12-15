package com.anilduyguc.barcodescannerproject.view;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.anilduyguc.barcodescannerproject.MainActivity;
import com.anilduyguc.barcodescannerproject.R;
import com.squareup.picasso.Picasso;

public class ProductDetailsInfo extends AppCompatActivity {
    private TextView titleTextView, sellerTextView, authorTextView, descriptionTextView,  urlTextView, priceTextView, isbnNoTextView;
    private Button backButton, buyButton;
    private ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details_info);
        backButton = findViewById(R.id.backButtonB);
        buyButton = findViewById(R.id.buyButtonB);
        titleTextView = findViewById(R.id.titleTV);
        sellerTextView = findViewById(R.id.sellerTV);
        authorTextView = findViewById(R.id.authorTV);
        descriptionTextView = findViewById(R.id.descriptionTV);
        urlTextView = findViewById(R.id.urlTV);
        priceTextView = findViewById(R.id.priceTV);
        isbnNoTextView = findViewById(R.id.isbnNoTV);
        imageView = findViewById(R.id.imageView2);
        Intent intent = getIntent();

        String title = intent.getStringExtra("title");
        String seller = intent.getStringExtra("seller");
        String author = intent.getStringExtra("author");
        String description = intent.getStringExtra("description");
        String url = intent.getStringExtra("url");
        String price = intent.getStringExtra("price");
        String isbnNo = intent.getStringExtra("isbnNo");
        String imageUrl = intent.getStringExtra("imageUrl");
        String category = intent.getStringExtra("category");

        titleTextView.setText(title);
        sellerTextView.setText("Seller: " +seller);
        authorTextView.setText("Author: " + author);
        descriptionTextView.setText("\t\t\t" +description);
        urlTextView.setText("Category: " + category);
        priceTextView.setText("$ " +price);
        isbnNoTextView.setText("ISBN No: " + isbnNo);
        Picasso.get().load(imageUrl).into(imageView);

        backButton.setOnClickListener(v -> {
            Log.d("In here", "Button Clicked");
            startActivity(new Intent(ProductDetailsInfo.this, SellerInfo.class));
        });
        buyButton.setOnClickListener(v -> {
            Uri uri = Uri.parse(url); // missing 'http://' will cause crashed
            Intent intentUrl = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intentUrl);
        });

    }
}
package com.anilduyguc.barcodescannerproject.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.anilduyguc.barcodescannerproject.MainActivity;
import com.anilduyguc.barcodescannerproject.R;
import com.anilduyguc.barcodescannerproject.adapter.SellerAdapter;
import com.anilduyguc.barcodescannerproject.data.SellerData;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SellerInfo extends AppCompatActivity {
    private FirebaseFirestore firestore;
    private RecyclerView recyclerView;
    private SellerAdapter adapter;
    private List<SellerData> modelList;
    private AppCompatButton backButton;
    private CollectionReference books;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_info);
        modelList = new ArrayList<>();
        firestore = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recylerView);
        backButton = findViewById(R.id.backButton2);


        backButton.setOnClickListener(v -> {
            startActivity(new Intent(SellerInfo.this, MainActivity.class));

        });                                                                         
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Log.d("Checkpoint", "Checkpoint");
        this.readData((list, applicationContext) -> {
            adapter = new SellerAdapter(applicationContext, list);
            recyclerView.setAdapter(adapter);
        }, this);
    }

    private void readData(FireStoreCallBack fireStoreCallBack, Context applicationContext){
        books = firestore.collection("products");
        books.orderBy("title", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (document.contains("title") && document.contains("price") && document.contains("url") && document.contains("seller")) {
                                String title = Objects.requireNonNull(document.getData().get("title")).toString();
                                String seller = Objects.requireNonNull(document.getData().get("seller")).toString();
                                Double price = (Double) document.getData().get("price");
                                String url = Objects.requireNonNull(document.getData().get("url")).toString();
                                String imageUrl = Objects.requireNonNull(document.getData().get("imageUrl")).toString();
                                String description = Objects.requireNonNull(document.getData().get("description")).toString();
                                String author = Objects.requireNonNull(document.getData().get("author")).toString();
                                String isbnNo = Objects.requireNonNull(document.getData().get("isbnNo")).toString();
                                String category = Objects.requireNonNull(document.getData().get("category")).toString();
                                String publisher = Objects.requireNonNull(document.getData().get("publisher")).toString();
                                modelList.add(new SellerData(title, price, url, seller, imageUrl, description, author, isbnNo, category, publisher));
//                                Log.d("Title", document.getId() + " => " + title);
//                                Log.d("Seller", document.getId() + " => " + seller);
//                                Log.d("Price", document.getId() + " => " + price);
//                                Log.d("Url", document.getId() + " => " + url);
                            }
                        }
                        fireStoreCallBack.onBallBack(modelList, applicationContext);
                    } else {
                        Log.w("TAG", "Error getting documents.", task.getException());
                    }
                });
    }
    private interface FireStoreCallBack{
        void onBallBack(List<SellerData> list, Context applicationContext);
    }
}
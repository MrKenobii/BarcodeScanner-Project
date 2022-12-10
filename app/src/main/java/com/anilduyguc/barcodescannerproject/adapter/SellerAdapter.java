package com.anilduyguc.barcodescannerproject.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;

import androidx.recyclerview.widget.RecyclerView;

import com.anilduyguc.barcodescannerproject.MainActivity;
import com.anilduyguc.barcodescannerproject.R;
import com.anilduyguc.barcodescannerproject.data.SellerData;
import com.anilduyguc.barcodescannerproject.view.ProductDetailsInfo;
import com.squareup.picasso.Picasso;

import android.content.Intent;



import android.widget.Filter;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;


import java.util.ArrayList;
import java.util.List;


public class SellerAdapter extends RecyclerView.Adapter<SellerAdapter.MyHolder> implements Filterable {
    Context context;
    List<SellerData> modelList ;
    List<SellerData> modelListFilter ;

    public SellerAdapter(Context context, List<SellerData> modelList) {
        this.context = context;
        this.modelList = modelList;
        this.modelListFilter = modelList ;
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.seller_list_item , parent , false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(MyHolder holder, int position) {

        String title = modelListFilter.get(position).getTitle();
        String url  = modelListFilter.get(position).getUrl();
        double price = modelListFilter.get(position).getPrice();
        String image = modelListFilter.get(position).getImageUrl();


        holder.title.setText(title);
        holder.price.setText("$ " + price);
        holder.url.setText(url);
        Picasso.get().load(image).into(holder.imageView);



    }

    @Override
    public int getItemCount() {
        return modelListFilter.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String character = constraint.toString();
                if (character.isEmpty()){
                    modelListFilter = modelList ;
                }else {
                    List<SellerData> filterList = new ArrayList<>();
                    for (SellerData row: modelList){
                        if (row.getTitle().toLowerCase().contains(character.toLowerCase())){
                            filterList.add(row);
                        }
                    }

                    modelListFilter = filterList ;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = modelListFilter;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                modelListFilter = (ArrayList<SellerData>) results.values ;
                notifyDataSetChanged();
            }
        };
    }


    class MyHolder extends RecyclerView.ViewHolder  implements  View.OnClickListener{

        TextView url, title, price ;
        ImageView imageView;
        public MyHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.productImageView);
            url = itemView.findViewById(R.id.url);
            title = itemView.findViewById(R.id.title);
            price = itemView.findViewById(R.id.price);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            Toast.makeText(context, "position"+position, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(context , ProductDetailsInfo.class);
            intent.putExtra("title" , modelListFilter.get(position).getTitle());
            intent.putExtra("price" , String.valueOf(modelListFilter.get(position).getPrice()));
            intent.putExtra("author" , modelListFilter.get(position).getAuthor());
            intent.putExtra("description" , modelListFilter.get(position).getDescription());
            intent.putExtra("seller" , modelListFilter.get(position).getName());
            intent.putExtra("imageUrl" , modelListFilter.get(position).getImageUrl());
            intent.putExtra("url" , modelListFilter.get(position).getUrl());
            intent.putExtra("isbnNo" , modelListFilter.get(position).getIsbnNo());
            context.startActivity(intent);

        }
    }
}

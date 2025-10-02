package com.example.onlyfanshop.adapter;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlyfanshop.R;
import com.example.onlyfanshop.databinding.ViewholderCartBinding;
import com.example.onlyfanshop.model.CartItemDTO;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewholder> {
    private List<CartItemDTO> cartItems;

    public CartAdapter(List<CartItemDTO> cartItems) {
        this.cartItems = cartItems;
    }


    @NonNull
    @Override
    public CartViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderCartBinding binding = ViewholderCartBinding.inflate(
                LayoutInflater.from(parent.getContext()),parent,false);
        //View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_cart,parent,false);
        return new CartViewholder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewholder holder, int position) {

    }

    @Override
    public int getItemCount() {
        int count=0;
        if (cartItems!=null){
            count = cartItems.size();
        }
        return count;
    }

    public static class CartViewholder extends RecyclerView.ViewHolder {
        ViewholderCartBinding binding;
        public CartViewholder(ViewholderCartBinding binding ) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

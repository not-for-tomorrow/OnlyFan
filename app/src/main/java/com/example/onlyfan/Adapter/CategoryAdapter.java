package com.example.onlyfan.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlyfan.Domain.CategoryModel;
import com.example.onlyfan.R;
import com.example.onlyfan.databinding.ViewholderCategoryBinding;

import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder>{
    private ArrayList<CategoryModel> item;
    private Context context;
    private int selectedPosition=-1;
    private int lastSelectedPosition=-1;

    public CategoryAdapter(ArrayList<CategoryModel> item) {
        this.item = item;
    }

    @NonNull
    @Override
    public CategoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ViewholderCategoryBinding binding = ViewholderCategoryBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
            public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.binding.titleTxt.setText(item.get(position).getTitle());
        holder.binding.titleTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lastSelectedPosition = selectedPosition;
                selectedPosition = position;
                notifyItemChanged(lastSelectedPosition);
                notifyItemChanged(selectedPosition);
            }
        });
        if (selectedPosition == position) {
            holder.binding.titleTxt.setBackgroundResource(R.drawable.green_bg);
            holder.binding.titleTxt.setTextColor(context.getResources().getColor(R.color.white));
        } else {
            holder.binding.titleTxt.setBackgroundResource(R.drawable.stroke_bg);
            holder.binding.titleTxt.setTextColor(context.getResources().getColor(R.color.black));
        }
    }
        @Override
    public int getItemCount() {
        return item.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ViewholderCategoryBinding binding;
        public ViewHolder(ViewholderCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }
}

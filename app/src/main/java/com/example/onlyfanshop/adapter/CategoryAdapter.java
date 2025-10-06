package com.example.onlyfanshop.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlyfanshop.R;
import com.example.onlyfanshop.databinding.ViewholderCategoryBinding;
import com.example.onlyfanshop.model.CategoryModel;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private final ArrayList<CategoryModel> originalList = new ArrayList<>();
    private final ArrayList<CategoryModel> displayList = new ArrayList<>();
    private ArrayList<CategoryModel> item;

    private Context context;
    private int selectedPosition = -1;
    private int lastSelectedPosition = -1;

    private String highlightQuery = "";
    @ColorInt
    private int highlightColor = Color.parseColor("#FFB300");

    public interface OnCategoryClick {
        void onClick(CategoryModel model, int position);
    }
    private OnCategoryClick onCategoryClick;

    public enum SortMode {
        DEFAULT, NAME_ASC, NAME_DESC, LENGTH_ASC, LENGTH_DESC, ID_ASC, ID_DESC
    }
    private SortMode currentSort = SortMode.DEFAULT;

    public CategoryAdapter(ArrayList<CategoryModel> item) {
        this.item = item;
        if (item != null) {
            originalList.addAll(item);
            displayList.addAll(item);
        }
        setHasStableIds(true);
    }

    public CategoryAdapter(ArrayList<CategoryModel> item, OnCategoryClick listener) {
        this(item);
        this.onCategoryClick = listener;
    }

    public void setOnCategoryClickListener(OnCategoryClick l) { this.onCategoryClick = l; }

    public void updateData(List<CategoryModel> newData) {
        originalList.clear();
        if (newData != null) originalList.addAll(newData);
        applyFilterAndSort(highlightQuery, currentSort, false);
    }

    public void setHighlightQuery(String query) {
        this.highlightQuery = query == null ? "" : query.trim();
        notifyDataSetChanged();
    }

    public void applyFilterAndSort(String query, SortMode mode) {
        applyFilterAndSort(query, mode, true);
    }

    private void applyFilterAndSort(String query, SortMode mode, boolean userAction) {
        String q = query == null ? "" : query.trim();
        this.highlightQuery = q;
        this.currentSort = (mode == null ? SortMode.DEFAULT : mode);

        displayList.clear();
        if (q.isEmpty()) {
            displayList.addAll(originalList);
        } else {
            String normQ = normalize(q);
            for (CategoryModel c : originalList) {
                String title = c.getTitle() == null ? "" : c.getTitle();
                if (normalize(title).contains(normQ)) {
                    displayList.add(c);
                }
            }
        }

        sortInternal(displayList, currentSort);

        if (userAction) {
            selectedPosition = -1;
            lastSelectedPosition = -1;
        }

        item = new ArrayList<>(displayList);
        notifyDataSetChanged();
    }

    private void sortInternal(List<CategoryModel> list, SortMode mode) {
        switch (mode) {
            case NAME_ASC:
                Collections.sort(list, Comparator.comparing(c -> safe(c.getTitle())));
                break;
            case NAME_DESC:
                Collections.sort(list, (a, b) -> safe(b.getTitle()).compareTo(safe(a.getTitle())));
                break;
            case LENGTH_ASC:
                Collections.sort(list, Comparator.comparingInt(a -> safe(a.getTitle()).length()));
                break;
            case LENGTH_DESC:
                Collections.sort(list, (a, b) ->
                        Integer.compare(safe(b.getTitle()).length(), safe(a.getTitle()).length()));
                break;
            case ID_ASC:
                Collections.sort(list, Comparator.comparingInt(this::safeId));
                break;
            case ID_DESC:
                Collections.sort(list, (a, b) -> Integer.compare(safeId(b), safeId(a)));
                break;
            case DEFAULT:
            default:
                break;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ViewholderCategoryBinding binding = ViewholderCategoryBinding.inflate(
                LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position < 0 || position >= displayList.size()) return;
        CategoryModel model = displayList.get(position);

        String title = model.getTitle() == null ? "" : model.getTitle();
        if (highlightQuery.isEmpty()) {
            holder.binding.titleTxt.setText(title);
        } else {
            holder.binding.titleTxt.setText(buildHighlighted(title, highlightQuery));
        }

        holder.binding.titleTxt.setOnClickListener(view -> {
            lastSelectedPosition = selectedPosition;

            int adapterPos = holder.getAdapterPosition();
            if (adapterPos == RecyclerView.NO_POSITION) return;

            selectedPosition = adapterPos;
            if (lastSelectedPosition != -1) notifyItemChanged(lastSelectedPosition);
            notifyItemChanged(selectedPosition);

            if (onCategoryClick != null &&
                    selectedPosition >= 0 &&
                    selectedPosition < displayList.size()) {
                onCategoryClick.onClick(displayList.get(selectedPosition), selectedPosition);
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
        return displayList.size();
    }

    @Override
    public long getItemId(int position) {
        if (position < 0 || position >= displayList.size()) return position;
        CategoryModel c = displayList.get(position);
        try {
            return c.getId();
        } catch (Exception e) {
            String t = c.getTitle();
            return t != null ? t.hashCode() : position;
        }
    }

    private CharSequence buildHighlighted(String original, String query) {
        try {
            String normOriginal = normalize(original);
            String normQuery = normalize(query);
            int start = normOriginal.indexOf(normQuery);
            if (start < 0) return original;
            SpannableString span = new SpannableString(original);
            span.setSpan(
                    new ForegroundColorSpan(highlightColor),
                    start,
                    Math.min(start + query.length(), original.length()),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            return span;
        } catch (Exception e) {
            return original;
        }
    }

    private String normalize(String s) {
        if (s == null) return "";
        return Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase();
    }

    private String safe(String s) { return s == null ? "" : s; }

    private int safeId(CategoryModel c) {
        try {
            return c.getId();
        } catch (Exception ignore) {
            return 0;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ViewholderCategoryBinding binding;
        public ViewHolder(ViewholderCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
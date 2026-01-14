package com.ensias.fundlytest.adapters;

import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ensias.fundlytest.FundlyApplication;
import com.ensias.fundlytest.R;
import com.ensias.fundlytest.models.Category;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private static final String TAG = "CategoryAdapter";

    private List<Category> categories;
    private CategoryListener listener;

    public interface CategoryListener {
        void onDelete(Category category);
        void onEdit(Category category);
    }

    public CategoryAdapter(List<Category> categories, CategoryListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    public void updateCategories(List<Category> newCategories) {
        this.categories = newCategories;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    private boolean isDefaultCategory(Category c) {
        String uid = c.getUserId();
        return uid == null || FundlyApplication.DEFAULT_USER_ID.equals(uid);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categories.get(position);

        boolean isDefault = isDefaultCategory(category);

        Log.d(TAG, "Binding category: " + category.getName()
                + " userId=" + category.getUserId()
                + " isDefault=" + isDefault
                + " isCustomFlag=" + category.isCustom());

        holder.categoryName.setText(category.getName());

        // Label + buttons only for USER categories
        if (!isDefault) {
            holder.customizedLabel.setVisibility(View.VISIBLE);
            holder.customizedLabel.setText("(Customized)");

            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnEdit.setVisibility(View.VISIBLE);

            holder.btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(category);
            });

            holder.btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEdit(category);
            });

        } else {
            holder.customizedLabel.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);
            holder.btnEdit.setVisibility(View.GONE);
            holder.btnDelete.setOnClickListener(null);
            holder.btnEdit.setOnClickListener(null);
        }

        // Icon
        try {
            int iconResId = holder.itemView.getContext().getResources()
                    .getIdentifier(category.getIconName(), "drawable",
                            holder.itemView.getContext().getPackageName());

            if (iconResId != 0) {
                holder.categoryIcon.setImageResource(iconResId);
            } else {
                holder.categoryIcon.setImageResource(android.R.drawable.ic_menu_add);
            }
            holder.categoryIcon.setColorFilter(0xFFFFFFFF);
        } catch (Exception e) {
            holder.categoryIcon.setImageResource(android.R.drawable.ic_menu_add);
            holder.categoryIcon.setColorFilter(0xFFFFFFFF);
        }

        // Background color
        View iconBackground = holder.itemView.findViewById(R.id.iconBackground);
        if (iconBackground != null && iconBackground.getBackground() instanceof GradientDrawable) {
            ((GradientDrawable) iconBackground.getBackground()).setColor(category.getColor());
        }
    }

    @Override
    public int getItemCount() {
        return categories != null ? categories.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName;
        TextView customizedLabel;
        ImageView categoryIcon;
        ImageButton btnDelete;
        ImageButton btnEdit;

        ViewHolder(View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.categoryName);
            customizedLabel = itemView.findViewById(R.id.customizedLabel);
            categoryIcon = itemView.findViewById(R.id.categoryIcon);
            btnDelete = itemView.findViewById(R.id.btnDeleteCategory);
            btnEdit = itemView.findViewById(R.id.btnEditCategory);
        }
    }
}

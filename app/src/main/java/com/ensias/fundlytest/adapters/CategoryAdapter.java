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
        Log.d(TAG, "Adapter created with " + (categories != null ? categories.size() : 0) + " categories");
    }

    public void updateCategories(List<Category> newCategories) {
        this.categories = newCategories;
        notifyDataSetChanged();
        Log.d(TAG, "Categories updated: " + (newCategories != null ? newCategories.size() : 0));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            Category category = categories.get(position);

            Log.d(TAG, "Binding category: " + category.getName() + " (custom: " + category.isCustom() + ")");

            // Set category name
            holder.categoryName.setText(category.getName());

            // Show "Customized" label only for custom categories
            if (category.isCustom()) {
                holder.customizedLabel.setVisibility(View.VISIBLE);
                holder.customizedLabel.setText("(Customized)");
            } else {
                holder.customizedLabel.setVisibility(View.GONE);
            }

            // Set icon if available
            try {
                int iconResId = holder.itemView.getContext().getResources()
                        .getIdentifier(category.getIconName(), "drawable",
                                holder.itemView.getContext().getPackageName());

                if (iconResId != 0) {
                    holder.categoryIcon.setImageResource(iconResId);
                } else {
                    Log.w(TAG, "Icon not found: " + category.getIconName());
                    holder.categoryIcon.setImageResource(android.R.drawable.ic_menu_add);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error setting icon", e);
                holder.categoryIcon.setImageResource(android.R.drawable.ic_menu_add);
            }

            // Set category color on the background view
            View iconBackground = holder.itemView.findViewById(R.id.iconBackground);
            if (iconBackground != null) {
                GradientDrawable drawable = (GradientDrawable) iconBackground.getBackground();
                if (drawable != null) {
                    drawable.setColor(category.getColor());
                }
            }

            // Set delete button visibility (only for custom categories)
            if (category.isCustom()) {
                holder.btnDelete.setVisibility(View.VISIBLE);
                holder.btnDelete.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onDelete(category);
                    }
                });
            } else {
                holder.btnDelete.setVisibility(View.GONE);
            }

            // Set edit button (only for custom categories)
            if (category.isCustom()) {
                holder.btnEdit.setVisibility(View.VISIBLE);
                holder.btnEdit.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onEdit(category);
                    }
                });
            } else {
                holder.btnEdit.setVisibility(View.GONE);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error binding view at position " + position, e);
        }
    }

    @Override
    public int getItemCount() {
        int count = categories == null ? 0 : categories.size();
        Log.d(TAG, "getItemCount: " + count);
        return count;
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

            // Log if any views are null
            if (categoryName == null) Log.e(TAG, "categoryName is null in ViewHolder");
            if (customizedLabel == null) Log.e(TAG, "customizedLabel is null in ViewHolder");
            if (categoryIcon == null) Log.e(TAG, "categoryIcon is null in ViewHolder");
            if (btnDelete == null) Log.e(TAG, "btnDelete is null in ViewHolder");
            if (btnEdit == null) Log.e(TAG, "btnEdit is null in ViewHolder");
        }
    }
}
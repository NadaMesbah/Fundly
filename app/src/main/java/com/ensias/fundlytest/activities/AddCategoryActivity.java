package com.ensias.fundlytest.activities;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ensias.fundlytest.FundlyApplication;
import com.ensias.fundlytest.R;
import com.ensias.fundlytest.adapters.CategoryAdapter;
import com.ensias.fundlytest.database.DataManager;
import com.ensias.fundlytest.models.Category;
import com.ensias.fundlytest.utils.SessionManager;
import com.google.android.material.tabs.TabLayout;

import java.util.*;

import yuku.ambilwarna.AmbilWarnaDialog;

public class AddCategoryActivity extends AppCompatActivity {

    private DataManager dataManager;
    private SessionManager sessionManager;
    private String currentUserId;

    private CategoryAdapter adapter;
    private final List<Category> displayedCategories = new ArrayList<>();
    private String currentType = "expense";
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_category);

        sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getUserId();

        if (currentUserId == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dataManager = new DataManager();

        setupViews();
        loadCategories();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dataManager != null) dataManager.close();
    }

    private boolean isDefaultCategory(Category c) {
        String uid = c.getUserId();
        return uid == null || FundlyApplication.DEFAULT_USER_ID.equals(uid);
    }

    private void setupViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        ImageButton btnRefresh = findViewById(R.id.btnRefresh);
        if (btnRefresh != null) btnRefresh.setOnClickListener(v -> refreshCategories());

        Button btnAddCategory = findViewById(R.id.btnAddCategory);
        if (btnAddCategory != null) btnAddCategory.setOnClickListener(v -> showAddCategoryDialog());

        RecyclerView recyclerView = findViewById(R.id.categoryRecyclerView);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new CategoryAdapter(displayedCategories, new CategoryAdapter.CategoryListener() {
                @Override public void onDelete(Category category) { showDeleteConfirmation(category); }
                @Override public void onEdit(Category category) { showEditCategoryDialog(category); }
            });
            recyclerView.setAdapter(adapter);
        }

        tabLayout = findViewById(R.id.tabLayout);
        if (tabLayout != null) {
            tabLayout.removeAllTabs();
            tabLayout.addTab(tabLayout.newTab().setText("Expense"), true);
            tabLayout.addTab(tabLayout.newTab().setText("Income"));

            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    currentType = tab.getPosition() == 0 ? "expense" : "income";
                    loadCategories();
                }
                @Override public void onTabUnselected(TabLayout.Tab tab) {}
                @Override public void onTabReselected(TabLayout.Tab tab) {}
            });
        }
    }

    private void loadCategories() {
        displayedCategories.clear();
        displayedCategories.addAll(dataManager.getCategoriesByType(currentUserId, currentType));
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    private void refreshCategories() {
        loadCategories();
        Toast.makeText(this, "Categories refreshed", Toast.LENGTH_SHORT).show();
    }

    private void showDeleteConfirmation(Category category) {
        if (isDefaultCategory(category)) {
            Toast.makeText(this, "Default categories cannot be deleted", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Category")
                .setMessage("Are you sure you want to delete " + category.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> deleteCategory(category))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_category, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        EditText etCategoryName = dialogView.findViewById(R.id.etCategoryName);
        GridLayout iconGrid = dialogView.findViewById(R.id.iconGrid);
        GridLayout colorGrid = dialogView.findViewById(R.id.colorGrid);
        Button btnMoreColors = dialogView.findViewById(R.id.btnMoreColors);

        final String[] selectedIcon = {"ic_restaurant"};
        final int[] selectedColor = {withAlpha(Color.parseColor("#2196F3"), 0.85f)}; // soft

        setupIconGrid(iconGrid, selectedIcon);
        setupColorGrid(colorGrid, selectedColor);

        if (btnMoreColors != null) {
            btnMoreColors.setOnClickListener(v -> openColorPicker(selectedColor, colorGrid));
        }

        Button btnCancel = dialogView.findViewById(R.id.btnCancelCategory);
        if (btnCancel != null) btnCancel.setOnClickListener(v -> dialog.dismiss());

        Button btnSave = dialogView.findViewById(R.id.btnSaveCategory);
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                String name = etCategoryName.getText().toString().trim();
                if (name.isEmpty()) {
                    etCategoryName.setError("Please enter category name");
                    return;
                }

                dataManager.addCategory(
                        UUID.randomUUID().toString(),
                        currentUserId,
                        name,
                        currentType,
                        selectedIcon[0],
                        selectedColor[0],
                        true
                );

                dialog.dismiss();
                loadCategories();
                Toast.makeText(this, "Category added", Toast.LENGTH_SHORT).show();
            });
        }

        dialog.show();
    }

    private void showEditCategoryDialog(Category categoryToEdit) {
        if (isDefaultCategory(categoryToEdit)) {
            Toast.makeText(this, "Default categories cannot be edited", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!currentUserId.equals(categoryToEdit.getUserId())) {
            Toast.makeText(this, "Unauthorized access", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_category, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        if (dialogTitle != null) dialogTitle.setText("Edit Category");

        EditText etCategoryName = dialogView.findViewById(R.id.etCategoryName);
        if (etCategoryName != null) etCategoryName.setText(categoryToEdit.getName());

        GridLayout iconGrid = dialogView.findViewById(R.id.iconGrid);
        GridLayout colorGrid = dialogView.findViewById(R.id.colorGrid);
        Button btnMoreColors = dialogView.findViewById(R.id.btnMoreColors);

        final String[] selectedIcon = {categoryToEdit.getIconName()};
        final int[] selectedColor = {categoryToEdit.getColor()};

        setupIconGrid(iconGrid, selectedIcon);
        setupColorGrid(colorGrid, selectedColor);

        if (btnMoreColors != null) {
            btnMoreColors.setOnClickListener(v -> openColorPicker(selectedColor, colorGrid));
        }

        Button btnCancel = dialogView.findViewById(R.id.btnCancelCategory);
        if (btnCancel != null) btnCancel.setOnClickListener(v -> dialog.dismiss());

        Button btnSave = dialogView.findViewById(R.id.btnSaveCategory);
        if (btnSave != null) {
            btnSave.setText("Update");
            btnSave.setOnClickListener(v -> {
                String name = etCategoryName.getText().toString().trim();
                if (name.isEmpty()) {
                    etCategoryName.setError("Please enter category name");
                    return;
                }

                dataManager.updateCategory(categoryToEdit.getId(), name, selectedIcon[0], selectedColor[0]);

                dialog.dismiss();
                loadCategories();
                Toast.makeText(this, "Category updated", Toast.LENGTH_SHORT).show();
            });
        }

        dialog.show();
    }

    private void openColorPicker(final int[] selectedColor, GridLayout colorGrid) {
        int initialColor = selectedColor[0];

        new AmbilWarnaDialog(
                this,
                initialColor,
                new AmbilWarnaDialog.OnAmbilWarnaListener() {
                    @Override
                    public void onCancel(AmbilWarnaDialog dialog) {
                        // nothing
                    }

                    @Override
                    public void onOk(AmbilWarnaDialog dialog, int color) {
                        // soft / modern
                        selectedColor[0] = withAlpha(color, 0.85f);

                        // remove selection stroke from grid (since now custom color)
                        clearColorGridSelection(colorGrid);

                        Toast.makeText(AddCategoryActivity.this, "Custom color selected", Toast.LENGTH_SHORT).show();
                    }
                }
        ).show();
    }

    private void clearColorGridSelection(GridLayout colorGrid) {
        if (colorGrid == null) return;
        for (int i = 0; i < colorGrid.getChildCount(); i++) {
            View child = colorGrid.getChildAt(i);
            if (child.getBackground() instanceof GradientDrawable) {
                ((GradientDrawable) child.getBackground()).setStroke(4, Color.TRANSPARENT);
            }
        }
    }

    private void deleteCategory(Category category) {
        if (isDefaultCategory(category)) {
            Toast.makeText(this, "Default categories cannot be deleted", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!currentUserId.equals(category.getUserId())) {
            Toast.makeText(this, "Unauthorized access", Toast.LENGTH_SHORT).show();
            return;
        }

        dataManager.deleteCategory(category.getId());
        loadCategories();
        Toast.makeText(this, "Category deleted", Toast.LENGTH_SHORT).show();
    }

    // --- Icon grid unchanged ---
    private void setupIconGrid(GridLayout iconGrid, final String[] selectedIcon) {
        if (iconGrid == null) return;
        iconGrid.removeAllViews();

        String[] icons = {
                "ic_restaurant", "ic_directions_car", "ic_shopping_cart",
                "ic_health", "ic_movie", "ic_school",
                "ic_home", "ic_phone_android", "ic_work",
                "ic_computer", "ic_trending_up", "ic_card_giftcard",
                "ic_attach_money"
        };

        float density = getResources().getDisplayMetrics().density;
        int sizePx = (int) (56 * density);

        for (String icon : icons) {
            ImageButton iconBtn = new ImageButton(this);

            int resId = getResources().getIdentifier(icon, "drawable", getPackageName());
            if (resId == 0) resId = android.R.drawable.ic_menu_add;
            iconBtn.setImageResource(resId);

            iconBtn.setBackgroundResource(R.drawable.button_secondary);
            iconBtn.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = sizePx;
            params.height = sizePx;
            params.setMargins(8, 8, 8, 8);
            iconBtn.setLayoutParams(params);
            iconBtn.setPadding(10, 10, 10, 10);

            if (icon.equals(selectedIcon[0])) {
                iconBtn.setBackgroundResource(R.drawable.button_primary);
            }

            iconBtn.setOnClickListener(v -> {
                for (int i = 0; i < iconGrid.getChildCount(); i++) {
                    iconGrid.getChildAt(i).setBackgroundResource(R.drawable.button_secondary);
                }
                iconBtn.setBackgroundResource(R.drawable.button_primary);
                selectedIcon[0] = icon;
            });

            iconGrid.addView(iconBtn);
        }
    }

    // --- Color grid updated: more colors + alpha style ---
    private void setupColorGrid(GridLayout colorGrid, final int[] selectedColor) {
        if (colorGrid == null) return;
        colorGrid.removeAllViews();

        int[] colors = {
                Color.parseColor("#4CAF50"),
                Color.parseColor("#2196F3"),
                Color.parseColor("#FF9800"),
                Color.parseColor("#9C27B0"),
                Color.parseColor("#E91E63"),
                Color.parseColor("#00BCD4"),
                Color.parseColor("#795548"),
                Color.parseColor("#607D8B"),
                Color.parseColor("#667EEA"),
                Color.parseColor("#FF6B9D"),
                Color.parseColor("#4ECDC4"),
                Color.parseColor("#FFC107")
        };

        float density = getResources().getDisplayMetrics().density;
        int sizePx = (int) (40 * density);

        for (int baseColor : colors) {
            int color = withAlpha(baseColor, 0.85f);

            ImageButton colorBtn = new ImageButton(this);

            GradientDrawable circle = new GradientDrawable();
            circle.setShape(GradientDrawable.OVAL);
            circle.setColor(color);

            boolean isSelected = color == selectedColor[0];
            circle.setStroke(4, isSelected ? Color.WHITE : Color.TRANSPARENT);

            colorBtn.setBackground(circle);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = sizePx;
            params.height = sizePx;
            params.setMargins(10, 10, 10, 10);
            colorBtn.setLayoutParams(params);

            colorBtn.setOnClickListener(v -> {
                // clear strokes
                for (int i = 0; i < colorGrid.getChildCount(); i++) {
                    View child = colorGrid.getChildAt(i);
                    if (child.getBackground() instanceof GradientDrawable) {
                        ((GradientDrawable) child.getBackground()).setStroke(4, Color.TRANSPARENT);
                    }
                }

                // set this stroke
                if (colorBtn.getBackground() instanceof GradientDrawable) {
                    ((GradientDrawable) colorBtn.getBackground()).setStroke(4, Color.WHITE);
                }

                selectedColor[0] = color;
            });

            colorGrid.addView(colorBtn);
        }
    }

    // Utils: alpha
    private static int withAlpha(int color, float alpha) {
        int a = Math.round(255 * alpha);
        return (color & 0x00FFFFFF) | (a << 24);
    }
}

package com.ensias.fundlytest.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ensias.fundlytest.R;
import com.ensias.fundlytest.database.DataManager;
import com.ensias.fundlytest.models.Category;
import com.ensias.fundlytest.models.Transaction;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private List<Transaction> transactions;
    private OnTransactionClickListener listener;
    private DataManager dataManager;
    private DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());

    public interface OnTransactionClickListener {
        void onTransactionClick(Transaction transaction);
    }

    public TransactionAdapter(List<Transaction> transactions, OnTransactionClickListener listener) {
        this.transactions = transactions;
        this.listener = listener;
        this.dataManager = new DataManager();
    }

    public void updateTransactions(List<Transaction> newTransactions) {
        this.transactions = newTransactions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.transaction_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        Category category = dataManager.getCategoryById(transaction.getCategoryId());

        // Set transaction title (category name)
        if (category != null) {
            holder.transactionTitle.setText(category.getName());
        } else {
            holder.transactionTitle.setText("Transaction");
        }

        // Set amount with type indicator
        String amountText = decimalFormat.format(transaction.getAmount()) + " Dhs";
        holder.transactionAmount.setText(amountText);

        // Set icon if available
        if (transaction.getIconName() != null) {
            try {
                int resId = holder.itemView.getContext().getResources()
                        .getIdentifier(transaction.getIconName(), "drawable",
                                holder.itemView.getContext().getPackageName());
                if (resId != 0) {
                    holder.iconImage.setImageResource(resId);
                }
            } catch (Exception e) {
                // Use default icon
                holder.iconImage.setImageResource(R.drawable.ic_attach_money);
            }
        }

        // Set color
        if (transaction.getColor() != 0) {
            holder.iconBackground.setBackgroundColor(transaction.getColor());
        }

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTransactionClick(transaction);
            }
        });
    }

    @Override
    public int getItemCount() {
        return transactions != null ? transactions.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView transactionTitle;
        TextView transactionAmount;
        View iconBackground;
        ImageView iconImage;

        ViewHolder(View itemView) {
            super(itemView);
            transactionTitle = itemView.findViewById(R.id.transactionTitle);
            transactionAmount = itemView.findViewById(R.id.transactionAmount);
            iconBackground = itemView.findViewById(R.id.iconBackground);
            iconImage = itemView.findViewById(R.id.iconImage);
        }
    }

    public void closeDataManager() {
        if (dataManager != null) {
            dataManager.close();
        }
    }
}
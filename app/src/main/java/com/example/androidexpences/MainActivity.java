package com.example.androidexpences;

import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Spinner transactionSpinner, budgetSpinner;
    TextView budgetAmountView, budgetNameView, dollarSignText;
    EditText transactionAmountEditText, transactionCommentEditText;
    Button changeBudgetBtn, addTransactionBtn;
    ListView historyListView;
    Switch historySwitch;
    int currentBudgetIndex;

    private ArrayList<Budget> budgetList = new ArrayList<>();
    private ArrayAdapter<CharSequence> historyAdapter;

    private static final  String CHANNEL_ID = "default_channel";
    private static final String CHANNEL_NAME = "Main Notification Channel";
    private static int ID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createNotificationChannel(this);

        budgetAmountView = findViewById(R.id.budgetAmountView);
        budgetNameView = findViewById(R.id.budgetNameView);
        dollarSignText = findViewById(R.id.dollarSignText);
        transactionAmountEditText = findViewById(R.id.transactionAmountEditText);
        transactionCommentEditText = findViewById(R.id.transactionCommentEditText);
        changeBudgetBtn = findViewById(R.id.changeBudgetBtn);
        transactionSpinner = findViewById(R.id.transactionSpinner);
        addTransactionBtn = findViewById(R.id.addTransactionBtn);
        historyListView = findViewById(R.id.historyListView);
        historySwitch = findViewById(R.id.historySwitch);

        budgetList.add(new Budget("Default", 1000));
        budgetAmountView.setText(String.valueOf(budgetList.get(0).getAmount()));
        budgetNameView.setText("Budget name: "+budgetList.get(0).getName());
        updateBudgetAmountColor(budgetList.get(0).getAmount());

        historyAdapter = new ArrayAdapter<>(this, R.layout.transaction_list_item, new ArrayList<>());
        historyListView.setAdapter(historyAdapter);
        updateTransactionHistory(0);

        historyListView.setVisibility(View.VISIBLE);

        historySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            historyListView.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        ArrayAdapter<CharSequence> tranSpinAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.spinner_transaction_options_array,
                android.R.layout.simple_spinner_item
        );
        tranSpinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        transactionSpinner.setAdapter(tranSpinAdapter);

        changeBudgetBtn.setOnClickListener(v -> {
            showChangeBudgetDialog();
        });

        addTransactionBtn.setOnClickListener(v->{
            try {
                double amount = Double.parseDouble(transactionAmountEditText.getText().toString());
                String comment = transactionCommentEditText.getText().toString();
                int type = transactionSpinner.getSelectedItemPosition();

                currentBudgetIndex = budgetSpinner != null ? budgetSpinner.getSelectedItemPosition() : 0;
                Budget currentBudget = budgetList.get(currentBudgetIndex);

                Transaction transaction = new Transaction(amount, comment, type);
                currentBudget.addTransaction(transaction);

                if (type == 1) {
                    currentBudget.setAmount(currentBudget.getAmount() - amount);
                } else {
                    currentBudget.setAmount(currentBudget.getAmount() + amount);
                }
                budgetAmountView.setText(String.valueOf(currentBudget.getAmount()));
                updateBudgetAmountColor(currentBudget.getAmount());

                updateTransactionHistory(currentBudgetIndex);

                transactionAmountEditText.setText("");
                transactionCommentEditText.setText("");
            } catch (NumberFormatException e) {
                transactionAmountEditText.setError("Please enter a valid number");
            }
        });



    }

    private void updateTransactionHistory(int budgetIndex) {
        Budget budget = budgetList.get(budgetIndex);
        historyAdapter.clear();
        ArrayList<Transaction> transactions = budget.getTransactions();
        for (int i = transactions.size() - 1; i >= 0; i--) {
            Transaction transaction = transactions.get(i);
            String typeStr = transaction.getType() == 0 ? "Income" : "Expense";
            String color = transaction.getType() == 0 ? "#4CAF50" : "#F44336";
            String displayText = "<font color='" + color + "'>" + typeStr + ": " + transaction.getAmount() + "</font>" +
                    "<br>Comment: " + transaction.getComment() + "<br>Date: " + transaction.getDate();
            historyAdapter.add(Html.fromHtml(displayText, Html.FROM_HTML_MODE_LEGACY));
        }
        historyAdapter.notifyDataSetChanged();
    }

    private void showChangeBudgetDialog() {
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.choose_budget_dialog);

        Button btnCreateBudget = dialog.findViewById(R.id.btnCreateBudget);
        Button btnSelect = dialog.findViewById(R.id.btnSelect);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        budgetSpinner = dialog.findViewById(R.id.budgetSpinner);

        ArrayList<String> spinner_budget_options_array = new ArrayList<>();
        for (Budget budget : budgetList) {
            spinner_budget_options_array.add(budget.toString());
        }

        ArrayAdapter<String> budgetSpinAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                spinner_budget_options_array
        );

        budgetSpinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        budgetSpinner.setAdapter(budgetSpinAdapter);

        btnCreateBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateBudgetDialog();
                dialog.dismiss();
            }
        });

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedPosition = budgetSpinner.getSelectedItemPosition();
                budgetAmountView.setText(String.valueOf(budgetList.get(selectedPosition).getAmount()));
                budgetNameView.setText("Budget name: "+budgetList.get(selectedPosition).getName());
                updateBudgetAmountColor(budgetList.get(selectedPosition).getAmount());
                updateTransactionHistory(selectedPosition);
                dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showCreateBudgetDialog() {
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.create_budget_dialog);

        Button btnSave = dialog.findViewById(R.id.btnSave);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        EditText budgetName = dialog.findViewById(R.id.budgetName);
        EditText budgetAmount = dialog.findViewById(R.id.budgetAmount);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = budgetName.getText().toString();
                int amount = Integer.parseInt(budgetAmount.getText().toString());
                budgetList.add(new Budget(name, amount));
                currentBudgetIndex = budgetList.size()-1;
                budgetAmountView.setText(String.valueOf(budgetList.get(currentBudgetIndex).getAmount()));
                budgetNameView.setText("Budget name: "+budgetList.get(currentBudgetIndex).getName());
                updateBudgetAmountColor(budgetList.get(currentBudgetIndex).getAmount());
                updateTransactionHistory(currentBudgetIndex);
                sendNotification(ID, MainActivity.this);
                ID++;
                dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
    private void createNotificationChannel(Context context){
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Channel for budget notifications");
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static void sendNotification(int NOTIFICATION_ID, AppCompatActivity activity) {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU){
            if(ContextCompat.checkSelfPermission(activity, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
                return;
            }
        }

        NotificationManager notificationManager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(activity, CHANNEL_ID)
                .setContentTitle("Budget App")
                .setContentText("Congratulations! New budget is created.")
                .setSmallIcon(R.drawable.ic_notification)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void updateBudgetAmountColor(double amount) {
        if (amount < 0) {
            budgetAmountView.setTextColor(getResources().getColor(android.R.color.holo_red_light));
            dollarSignText.setTextColor(getResources().getColor(android.R.color.holo_red_light));
        } else {
            budgetAmountView.setTextColor(getResources().getColor(android.R.color.black));
            dollarSignText.setTextColor(getResources().getColor(android.R.color.black));
        }
    }
}
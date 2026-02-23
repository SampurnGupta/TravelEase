package com.example.travelease;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class PackingChecklistActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "packing_checklist_prefs";
    private static final String KEY_LIST = "checklist_items";

    private EditText etItem;
    private Button btnAdd;
    private RecyclerView rvChecklist;
    private ChecklistAdapter adapter;
    private ArrayList<ChecklistItem> checklist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.packing_checklist);

        etItem = findViewById(R.id.et_item);
        btnAdd = findViewById(R.id.btn_add);
        rvChecklist = findViewById(R.id.rv_checklist);

        checklist = loadChecklist();
        adapter = new ChecklistAdapter(checklist, this::saveChecklist);
        rvChecklist.setLayoutManager(new LinearLayoutManager(this));
        rvChecklist.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> addItem());
        etItem.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addItem();
                return true;
            }
            return false;
        });
    }

    private void addItem() {
        String text = etItem.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            Toast.makeText(this, "Enter an item", Toast.LENGTH_SHORT).show();
            return;
        }
        checklist.add(new ChecklistItem(text, false));
        adapter.notifyItemInserted(checklist.size() - 1);
        etItem.setText("");
        saveChecklist();
    }

    private void saveChecklist() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        JSONArray array = new JSONArray();
        for (ChecklistItem item : checklist) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("text", item.text);
                obj.put("checked", item.checked);
                array.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        prefs.edit().putString(KEY_LIST, array.toString()).apply();
    }

    private ArrayList<ChecklistItem> loadChecklist() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_LIST, null);
        ArrayList<ChecklistItem> list = new ArrayList<>();
        if (json != null) {
            try {
                JSONArray array = new JSONArray(json);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    String text = obj.getString("text");
                    boolean checked = obj.getBoolean("checked");
                    list.add(new ChecklistItem(text, checked));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    // --- ChecklistItem as static inner class ---
    public static class ChecklistItem {
        public String text;
        public boolean checked;

        public ChecklistItem(String text, boolean checked) {
            this.text = text;
            this.checked = checked;
        }
    }

    // --- ChecklistAdapter as inner class ---
    public class ChecklistAdapter extends RecyclerView.Adapter<ChecklistAdapter.ViewHolder> {
        private final ArrayList<ChecklistItem> items;
        private final Runnable onListChanged;

        public ChecklistAdapter(ArrayList<ChecklistItem> items, Runnable onListChanged) {
            this.items = items;
            this.onListChanged = onListChanged;
        }

        @NonNull
        @Override
        public ChecklistAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.checklist_item, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ChecklistAdapter.ViewHolder holder, int position) {
            ChecklistItem item = items.get(position);
            holder.tvItem.setText(item.text);
            holder.cbChecked.setChecked(item.checked);

            holder.cbChecked.setOnCheckedChangeListener(null); // Prevent unwanted triggers
            holder.cbChecked.setChecked(item.checked);
            holder.cbChecked.setOnCheckedChangeListener((buttonView, isChecked) -> {
                item.checked = isChecked;
                onListChanged.run();
            });

            holder.btnDelete.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    items.remove(pos);
                    notifyItemRemoved(pos);
                    onListChanged.run();
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            CheckBox cbChecked;
            TextView tvItem;
            ImageButton btnDelete;

            ViewHolder(View itemView) {
                super(itemView);
                cbChecked = itemView.findViewById(R.id.cb_checked);
                tvItem = itemView.findViewById(R.id.tv_item);
                btnDelete = itemView.findViewById(R.id.btn_delete);
            }
        }
    }
}

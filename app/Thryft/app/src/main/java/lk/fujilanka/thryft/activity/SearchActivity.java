package lk.fujilanka.thryft.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import lk.fujilanka.thryft.R;
import lk.fujilanka.thryft.adapter.ProductGridAdapter;
import lk.fujilanka.thryft.adapter.RecentSearchAdapter;
import lk.fujilanka.thryft.model.Product;

public class SearchActivity extends AppCompatActivity {

    private EditText etSearch;
    private ImageView ivClear;

    private RecyclerView rvResults;

    private LinearLayout llRecent;
    private LinearLayout llEmpty;

    private TextView tvResultsCount;
    private TextView tvSearchQuery;

    private ProgressBar progressBar;

    private FirebaseFirestore db;

    private RecyclerView rvRecent;
    private RecentSearchAdapter recentAdapter;
    private List<String> recentSearches = new ArrayList<>();

    private ProductGridAdapter adapter;

    private List<Product> allProducts = new ArrayList<>();
    private List<Product> filteredProducts = new ArrayList<>();

    private final String FILE_NAME = "search_history.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        db = FirebaseFirestore.getInstance();

        initViews();

        loadProducts();

        openKeyboard();

        setupSearch();

        TextView tvClear = findViewById(R.id.tv_clear_recent);

        tvClear.setOnClickListener(v -> {

            deleteFile("search_history.txt");

            recentSearches.clear();

            recentAdapter.notifyDataSetChanged();
        });
    }

    private void initViews(){

        etSearch = findViewById(R.id.et_search);
        ivClear = findViewById(R.id.iv_clear);

        rvResults = findViewById(R.id.rv_search_results);

        llRecent = findViewById(R.id.ll_recent_searches);
        llEmpty = findViewById(R.id.ll_empty_search);

        tvResultsCount = findViewById(R.id.tv_results_count);
        tvSearchQuery = findViewById(R.id.tv_search_query);

        progressBar = findViewById(R.id.progress_bar);

        adapter = new ProductGridAdapter(filteredProducts, product -> {

            Intent intent = new Intent(SearchActivity.this, MainActivity.class);
            intent.putExtra("openProduct", product.getProductId());
            startActivity(intent);

        });

        rvResults.setLayoutManager(new GridLayoutManager(this,2));
        rvResults.setAdapter(adapter);

        rvRecent = findViewById(R.id.rv_recent_searches);

        recentSearches = getSearchHistory();

        recentAdapter = new RecentSearchAdapter(recentSearches, new RecentSearchAdapter.OnSearchClick() {
            @Override
            public void onClick(String query) {

                etSearch.setText(query);
                searchProducts(query);

            }

            @Override
            public void onDelete(String query) {

                recentSearches.remove(query);
                recentAdapter.notifyDataSetChanged();

            }
        });

        rvRecent.setAdapter(recentAdapter);
    }

    private void openKeyboard(){

        etSearch.requestFocus();

        etSearch.postDelayed(() -> {

            InputMethodManager imm =
                    (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

            if(imm != null){
                imm.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT);
            }

        },200);

    }

    private void loadProducts(){

        progressBar.setVisibility(View.VISIBLE);

        db.collection("products")
                .whereEqualTo("status", true)
                .get()
                .addOnSuccessListener(snapshot -> {

                    progressBar.setVisibility(View.GONE);

                    allProducts.clear();

                    for(DocumentSnapshot doc : snapshot.getDocuments()){

                        Product product = doc.toObject(Product.class);

                        if(product != null){
                            allProducts.add(product);
                        }

                    }

                });
    }

    private void setupSearch(){

        etSearch.setOnEditorActionListener((v, actionId, event) -> {

            if(actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER){

                String query = etSearch.getText().toString().trim();

                if(!TextUtils.isEmpty(query)){

                    saveSearch(query);

                    searchProducts(query);

                }

                return true;
            }

            return false;

        });

        ivClear.setOnClickListener(v -> {

            etSearch.setText("");

            rvResults.setVisibility(View.GONE);
            llRecent.setVisibility(View.VISIBLE);
            llEmpty.setVisibility(View.GONE);

        });

    }

    private void searchProducts(String query){

        filteredProducts.clear();

        for(Product product : allProducts){

            if(product.getTitle() != null &&
                    product.getTitle().toLowerCase().contains(query.toLowerCase())){

                filteredProducts.add(product);

            }

        }

        adapter.notifyDataSetChanged();

        llRecent.setVisibility(View.GONE);

        if(filteredProducts.isEmpty()){

            rvResults.setVisibility(View.GONE);
            llEmpty.setVisibility(View.VISIBLE);

            tvSearchQuery.setText(query);

        }else{

            llEmpty.setVisibility(View.GONE);
            rvResults.setVisibility(View.VISIBLE);

            tvResultsCount.setText("Found " + filteredProducts.size() + " products");

        }

        saveSearch(query);

        recentSearches.clear();
        recentSearches.addAll(getSearchHistory());
        recentAdapter.notifyDataSetChanged();

    }

    private void saveSearch(String query) {

        try {

            FileOutputStream fos = openFileOutput(FILE_NAME, MODE_APPEND);

            fos.write((query + "\n").getBytes());

            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private List<String> getSearchHistory() {

        List<String> history = new ArrayList<>();

        try {

            FileInputStream fis = openFileInput(FILE_NAME);

            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));

            String line;

            while ((line = reader.readLine()) != null) {
                history.add(line);
            }

            reader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return history;

    }

}
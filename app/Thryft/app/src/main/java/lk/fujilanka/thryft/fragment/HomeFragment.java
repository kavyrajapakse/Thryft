package lk.fujilanka.thryft.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.List;

import lk.fujilanka.thryft.R;
import lk.fujilanka.thryft.adapter.CategoryAdapter;
import lk.fujilanka.thryft.adapter.ListingAdapter;
import lk.fujilanka.thryft.databinding.FragmentHomeBinding;
import lk.fujilanka.thryft.model.Category;
import lk.fujilanka.thryft.model.Product;




public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private CategoryAdapter adapter;
    private ListingAdapter newArrivalsAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int spanCount;

        if (getResources().getConfiguration().orientation ==
                android.content.res.Configuration.ORIENTATION_LANDSCAPE) {

            spanCount = 5;

        } else {

            spanCount = 3;

        }

        binding.rvCategories.setLayoutManager(new GridLayoutManager(getContext(), spanCount));

        FirebaseFirestore db = FirebaseFirestore.getInstance();

//        Category c1 = new Category("cat1", "Dresses", "https://images.unsplash.com/photo-1520975922284-9e0ce82759c2");
//        Category c2 = new Category("cat2", "Tops & Blouses", "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab");
//        Category c3 = new Category("cat3", "Shirts & Tshirts", "https://images.unsplash.com/photo-1503342217505-b0a15ec3261c");
//        Category c4 = new Category("cat4", "Shorts & Jeans", "https://images.unsplash.com/photo-1541099649105-f69ad21f3246");
//        Category c5 = new Category("cat5", "Skirts", "https://images.unsplash.com/photo-1490481651871-ab68de25d43d");
//        Category c6 = new Category("cat6", "Shoes", "https://images.unsplash.com/photo-1519741497674-611481863552");
//        Category c7 = new Category("cat7", "Bags", "https://images.unsplash.com/photo-1591561954557-26941169b49e");
//
//
//        List<Category> cats = List.of(c1,c2,c3,c4,c5,c6,c7);
//
//        WriteBatch batch = db.batch();
//
//        for (Category c : cats){
//            DocumentReference ref = db.collection("categories").document();
//            batch.set(ref, c);
//        }
//
//        batch.commit();


        db.collection("categories").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        QuerySnapshot result = task.getResult();
                        //List<Category> categories = result.toObjects(Category.class);

                        List<Category> categories = task.getResult().toObjects(Category.class);
                        adapter = new CategoryAdapter(categories, category -> {

                            Bundle bundle = new Bundle();
                            bundle.putString("categoryId", category.getCategoryId());

                            ListingFragment fragment = new ListingFragment();
                            fragment.setArguments(bundle);

                            getParentFragmentManager().beginTransaction()
                                    .replace(R.id.fragmentContainer, fragment)
                                    .addToBackStack(null)
                                    .commit();

                        });
                        binding.rvCategories.setAdapter(adapter);
                    }
                });

        // New Arrivals RecyclerView
        // --------------------------
        binding.rvNewArrivals.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
        );

        db.collection("products")
                .whereEqualTo("status", true) // only available products// or use "timestamp" if you have a createdAt field
                .limit(10)                    // latest 10 products
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Product> products = task.getResult().toObjects(Product.class);

                        newArrivalsAdapter = new ListingAdapter(products, product -> {
                            // Open ProductDetailsFragment
                            Bundle bundle = new Bundle();
                            bundle.putString("productId", product.getProductId());

                            ProductDetailsFragment fragment = new ProductDetailsFragment();
                            fragment.setArguments(bundle);

                            getParentFragmentManager().beginTransaction()
                                    .replace(R.id.fragmentContainer, fragment)
                                    .addToBackStack(null)
                                    .commit();
                        });

                        binding.rvNewArrivals.setAdapter(newArrivalsAdapter);

                    } else {
                        Toast.makeText(getContext(), "Failed to load new arrivals", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    public void refreshProducts(){

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("products")
                .whereEqualTo("status", true)
                .limit(10)
                .get()
                .addOnCompleteListener(task -> {

                    if(task.isSuccessful()){

                        List<Product> products = task.getResult().toObjects(Product.class);

                        newArrivalsAdapter = new ListingAdapter(products, product -> {

                            Bundle bundle = new Bundle();
                            bundle.putString("productId", product.getProductId());

                            ProductDetailsFragment fragment = new ProductDetailsFragment();
                            fragment.setArguments(bundle);

                            getParentFragmentManager().beginTransaction()
                                    .replace(R.id.fragmentContainer, fragment)
                                    .addToBackStack(null)
                                    .commit();
                        });

                        binding.rvNewArrivals.setAdapter(newArrivalsAdapter);
                    }
                });
    }
}
package lk.fujilanka.thryft.model;

import com.google.firebase.firestore.Exclude;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    @Getter(onMethod_ = {@Exclude})
    @Setter(onMethod_ = {@Exclude})
    private String documentId;

    private String productId;
    private String title;
    private String description;
    private double price;
    private String categoryId;
    private List<String> images; // urls of product images
    private boolean status; // available or sold
    private String size; // S, M, L etc.
    private String condition; // New, Like New, Used
    private String color; // Red, Blue, Black etc.
}
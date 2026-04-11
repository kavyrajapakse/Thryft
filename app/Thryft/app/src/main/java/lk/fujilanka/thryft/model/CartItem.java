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
public class CartItem {

    @Getter(onMethod_ = {@Exclude})
    @Setter(onMethod_ = {@Exclude})
    private String documentId;

    private String productId;

    public CartItem(String productId) {
        this.productId = productId;
    }

}
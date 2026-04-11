package lk.fujilanka.thryft.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationModel {

    private String id;        // Firestore document ID
    private String title;
    private String message;
    private long timestamp;
    private boolean read;

}
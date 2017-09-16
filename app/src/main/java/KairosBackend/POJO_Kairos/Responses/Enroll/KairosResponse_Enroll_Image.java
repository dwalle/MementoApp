package KairosBackend.POJO_Kairos.Responses.Enroll;

/**
 * Created by Daniel on 7/26/2017.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class KairosResponse_Enroll_Image {

    @SerializedName("attributes")
    @Expose
    private KairosResponse_Enroll_Attributes attributes;
    @SerializedName("transaction")
    @Expose
    private KairosResponse_Enroll_Transaction transaction;

    public KairosResponse_Enroll_Attributes getAttributes() {
        return attributes;
    }

    public void setAttributes(KairosResponse_Enroll_Attributes attributes) {
        this.attributes = attributes;
    }

    public KairosResponse_Enroll_Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(KairosResponse_Enroll_Transaction transaction) {
        this.transaction = transaction;
    }

}
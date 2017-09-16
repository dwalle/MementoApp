package KairosBackend.POJO_Kairos.Responses.Recognize;

/**
 * Created by Daniel on 7/27/2017.
 */


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class KairosResponse_Recognize_Image {

    @SerializedName("transaction")
    @Expose
    private KairosResponse_Recognize_Transaction transaction;
    @SerializedName("candidates")
    @Expose
    private List<KairosResponse_Recognize_Candidate> candidates = null;

    public KairosResponse_Recognize_Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(KairosResponse_Recognize_Transaction transaction) {
        this.transaction = transaction;
    }

    public List<KairosResponse_Recognize_Candidate> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<KairosResponse_Recognize_Candidate> candidates) {
        this.candidates = candidates;
    }

}
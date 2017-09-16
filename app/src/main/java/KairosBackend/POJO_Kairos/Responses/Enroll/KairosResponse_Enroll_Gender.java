package KairosBackend.POJO_Kairos.Responses.Enroll;

/**
 * Created by Daniel on 7/26/2017.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class KairosResponse_Enroll_Gender {

    @SerializedName("femaleConfidence")
    @Expose
    private Double femaleConfidence;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("maleConfidence")
    @Expose
    private Double maleConfidence;

    public Double getFemaleConfidence() {
        return femaleConfidence;
    }

    public void setFemaleConfidence(Double femaleConfidence) {
        this.femaleConfidence = femaleConfidence;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getMaleConfidence() {
        return maleConfidence;
    }

    public void setMaleConfidence(Double maleConfidence) {
        this.maleConfidence = maleConfidence;
    }

}
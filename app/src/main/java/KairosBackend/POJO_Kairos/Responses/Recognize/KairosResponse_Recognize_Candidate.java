package KairosBackend.POJO_Kairos.Responses.Recognize;

/**
 * Created by Daniel on 7/27/2017.
 */
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class KairosResponse_Recognize_Candidate {

    @SerializedName("subject_id")
    @Expose
    private String subjectId;
    @SerializedName("face_id")
    @Expose
    private String faceId;
    @SerializedName("confidence")
    @Expose
    private Double confidence;
    @SerializedName("enrollment_timestamp")
    @Expose
    private String enrollmentTimestamp;

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getFaceId() {
        return faceId;
    }

    public void setFaceId(String faceId) {
        this.faceId = faceId;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public String getEnrollmentTimestamp() {
        return enrollmentTimestamp;
    }

    public void setEnrollmentTimestamp(String enrollmentTimestamp) {
        this.enrollmentTimestamp = enrollmentTimestamp;
    }

}
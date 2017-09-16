package KairosBackend.POJO_Kairos.Responses.Recognize;

/**
 * Created by Daniel on 7/27/2017.
 */


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class KairosResponse_Recognize_Transaction {

    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("topLeftX")
    @Expose
    private Integer topLeftX;
    @SerializedName("topLeftY")
    @Expose
    private Integer topLeftY;
    @SerializedName("gallery_name")
    @Expose
    private String galleryName;
    @SerializedName("subject_id")
    @Expose
    private String subjectId;
    @SerializedName("confidence")
    @Expose
    private Double confidence;
    @SerializedName("eyeDistance")
    @Expose
    private Integer eyeDistance;
    @SerializedName("height")
    @Expose
    private Integer height;
    @SerializedName("width")
    @Expose
    private Integer width;
    @SerializedName("face_id")
    @Expose
    private Integer faceId;
    @SerializedName("quality")
    @Expose
    private Double quality;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getTopLeftX() {
        return topLeftX;
    }

    public void setTopLeftX(Integer topLeftX) {
        this.topLeftX = topLeftX;
    }

    public Integer getTopLeftY() {
        return topLeftY;
    }

    public void setTopLeftY(Integer topLeftY) {
        this.topLeftY = topLeftY;
    }

    public String getGalleryName() {
        return galleryName;
    }

    public void setGalleryName(String galleryName) {
        this.galleryName = galleryName;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public Integer getEyeDistance() {
        return eyeDistance;
    }

    public void setEyeDistance(Integer eyeDistance) {
        this.eyeDistance = eyeDistance;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getFaceId() {
        return faceId;
    }

    public void setFaceId(Integer faceId) {
        this.faceId = faceId;
    }

    public Double getQuality() {
        return quality;
    }

    public void setQuality(Double quality) {
        this.quality = quality;
    }

}
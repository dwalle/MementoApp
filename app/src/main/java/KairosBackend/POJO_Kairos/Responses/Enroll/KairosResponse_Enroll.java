package KairosBackend.POJO_Kairos.Responses.Enroll;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class KairosResponse_Enroll {

    @SerializedName("face_id")
    @Expose
    private String faceId;
    @SerializedName("images")
    @Expose
    private List<KairosResponse_Enroll_Image> images = null;
    @SerializedName("uploaded_image_url")
    @Expose
    private String uploadedImageUrl;

    public String getFaceId() {
        return faceId;
    }

    public void setFaceId(String faceId) {
        this.faceId = faceId;
    }

    public List<KairosResponse_Enroll_Image> getImages() {
        return images;
    }

    public void setImages(List<KairosResponse_Enroll_Image> images) {
        this.images = images;
    }

    public String getUploadedImageUrl() {
        return uploadedImageUrl;
    }

    public void setUploadedImageUrl(String uploadedImageUrl) {
        this.uploadedImageUrl = uploadedImageUrl;
    }

}
package KairosBackend.POJO_Kairos.Responses.Recognize;

/**
 * Created by Daniel on 7/27/2017.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class KairosResponse_Recognize {

    @SerializedName("images")
    @Expose
    private List<KairosResponse_Recognize_Image> images = null;
    @SerializedName("uploaded_image_url")
    @Expose
    private String uploadedImageUrl;

    public List<KairosResponse_Recognize_Image> getImages() {
        return images;
    }

    public void setImages(List<KairosResponse_Recognize_Image> images) {
        this.images = images;
    }

    public String getUploadedImageUrl() {
        return uploadedImageUrl;
    }

    public void setUploadedImageUrl(String uploadedImageUrl) {
        this.uploadedImageUrl = uploadedImageUrl;
    }

}
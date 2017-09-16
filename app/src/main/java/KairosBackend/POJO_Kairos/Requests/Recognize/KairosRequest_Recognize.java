package KairosBackend.POJO_Kairos.Requests.Recognize;

/**
 * Created by Daniel on 7/27/2017.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class KairosRequest_Recognize {

    @SerializedName("image")
    @Expose
    private String image;
    @SerializedName("gallery_name")
    @Expose
    private String galleryName;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getGalleryName() {
        return galleryName;
    }

    public void setGalleryName(String galleryName) {
        this.galleryName = galleryName;
    }

}
package KairosBackend.POJO_Kairos.Requests.GalleryView;

/**
 * Created by Daniel on 7/26/2017.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class KairosRequest_GalleryView {

    @SerializedName("gallery_name")
    @Expose
    private String galleryName;

    public String getGalleryName() {
        return galleryName;
    }

    public void setGalleryName(String galleryName) {
        this.galleryName = galleryName;
    }

}

package KairosBackend.POJO_Kairos.Responses.GalleryListAll;

/**
 * Created by Daniel on 7/26/2017.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class KairosResponse_GalleryListAll {

    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("gallery_ids")
    @Expose
    private List<String> galleryIds = null;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getGalleryIds() {
        return galleryIds;
    }

    public void setGalleryIds(List<String> galleryIds) {
        this.galleryIds = galleryIds;
    }

}
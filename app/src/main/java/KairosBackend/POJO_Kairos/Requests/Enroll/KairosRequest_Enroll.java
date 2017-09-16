package KairosBackend.POJO_Kairos.Requests.Enroll;

/**
 * Created by Daniel on 7/26/2017.
 */


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class KairosRequest_Enroll {

    @SerializedName("image")
    @Expose
    private String image;
    @SerializedName("subject_id")
    @Expose
    private String subjectId;
    @SerializedName("gallery_name")
    @Expose
    private String galleryName;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getGalleryName() {
        return galleryName;
    }

    public void setGalleryName(String galleryName) {
        this.galleryName = galleryName;
    }

}

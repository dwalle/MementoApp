package KairosBackend.POJO_Kairos.Requests.GalleryRemoveSubject;

/**
 * Created by Daniel on 8/2/2017.
 */



import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class KairosRequest_GalleryRemoveSubject {

    @SerializedName("gallery_name")
    @Expose
    private String galleryName;
    @SerializedName("subject_id")
    @Expose
    private String subjectId;

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

}
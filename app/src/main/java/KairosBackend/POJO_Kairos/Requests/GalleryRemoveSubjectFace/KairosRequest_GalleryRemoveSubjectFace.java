package KairosBackend.POJO_Kairos.Requests.GalleryRemoveSubjectFace;

/**
 * Created by Daniel on 8/2/2017.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class KairosRequest_GalleryRemoveSubjectFace {

    @SerializedName("gallery_name")
    @Expose
    private String galleryName;
    @SerializedName("subject_id")
    @Expose
    private String subjectId;
    @SerializedName("face_id")
    @Expose
    private String faceId;

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

    public String getFaceId() {
        return faceId;
    }

    public void setFaceId(String faceId) {
        this.faceId = faceId;
    }

}
package KairosBackend.POJO_Kairos.Responses.GalleryView;

/**
 * Created by Daniel on 7/26/2017.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class KairosResponse_GalleryView {

    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("subject_ids")
    @Expose
    private List<String> subjectIds = null;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getSubjectIds() {
        return subjectIds;
    }

    public void setSubjectIds(List<String> subjectIds) {
        this.subjectIds = subjectIds;
    }

}
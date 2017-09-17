package KairosBackend;

import KairosBackend.POJO_Kairos.Requests.Enroll.KairosRequest_Enroll;
import KairosBackend.POJO_Kairos.Requests.GalleryRemove.KairosRequest_GalleryRemove;
import KairosBackend.POJO_Kairos.Requests.GalleryRemoveSubject.KairosRequest_GalleryRemoveSubject;
import KairosBackend.POJO_Kairos.Requests.GalleryRemoveSubjectFace.KairosRequest_GalleryRemoveSubjectFace;
import KairosBackend.POJO_Kairos.Requests.GalleryView.KairosRequest_GalleryView;
import KairosBackend.POJO_Kairos.Requests.Recognize.KairosRequest_Recognize;
import KairosBackend.POJO_Kairos.Responses.Enroll.KairosResponse_Enroll;
import KairosBackend.POJO_Kairos.Responses.GalleryListAll.KairosResponse_GalleryListAll;
import KairosBackend.POJO_Kairos.Responses.GalleryRemove.KairosResponse_GalleryRemove;
import KairosBackend.POJO_Kairos.Responses.GalleryRemoveSubject.KairosResponse_GalleryRemoveSubject;
import KairosBackend.POJO_Kairos.Responses.GalleryRemoveSubjectFace.KairosResponse_GalleryRemoveSubjectFace;
import KairosBackend.POJO_Kairos.Responses.GalleryView.KairosResponse_GalleryView;
import KairosBackend.POJO_Kairos.Responses.Recognize.KairosResponse_Recognize;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created by Daniel on 7/24/2017.
 */

public interface KairosClient {

    static final String BASE_URL = "https://api.kairos.com/";
    static String app_id = "{ENTER KAIROS APP ID HERE}";
    static String app_key = "{ENTER KAIROS APP KEY HERE}";

    @Headers({
            "app_id: "+app_id,
            "app_key: "+app_key
    })
    @POST("gallery/list_all")
    Call<KairosResponse_GalleryListAll> postGalleryListAll();

    @Headers({
            "app_id: "+app_id,
            "app_key: "+app_key
    })
    @POST("gallery/view")
    Call<KairosResponse_GalleryView> postGalleryView(@Body KairosRequest_GalleryView gView);

    @Headers({
            "app_id: "+app_id,
            "app_key: "+app_key
    })
    @POST("enroll")
    Call<KairosResponse_Enroll> postEnroll(@Body KairosRequest_Enroll gView);


    @Headers({
            "app_id: "+app_id,
            "app_key: "+app_key
    })
    @Multipart
    @POST("enroll")
    Call<KairosResponse_Enroll> postEnroll_Multipart(@Part MultipartBody.Part image,
                                                     @Part("subject_id") RequestBody subject_id,
                                                     @Part("gallery_name") RequestBody gallery_name);

    @Headers({
            "app_id: "+app_id,
            "app_key: "+app_key
    })
    @POST("recognize")
    Call<KairosResponse_Recognize> postRecognize(@Body KairosRequest_Recognize rec);

    @Headers({
            "app_id: "+app_id,
            "app_key: "+app_key
    })
    @Multipart
    @POST("recognize")
    Call<KairosResponse_Recognize> postRecognize_Multipart(@Part MultipartBody.Part image,
                                                           @Part("gallery_name") RequestBody gallery_name);

    @Headers({
            "app_id: "+app_id,
            "app_key: "+app_key
    })
    @POST("gallery/remove")
    Call<KairosResponse_GalleryRemove> postGalleryRemove(@Body KairosRequest_GalleryRemove req);

    @Headers({
            "app_id: "+app_id,
            "app_key: "+app_key
    })
    @POST("gallery/remove_subject")
    Call<KairosResponse_GalleryRemoveSubject> postGalleryRemoveSubject(@Body KairosRequest_GalleryRemoveSubject req);

    @Headers({
            "app_id: "+app_id,
            "app_key: "+app_key
    })
    @POST("gallery/remove_subject")
    Call<KairosResponse_GalleryRemoveSubjectFace> postGalleryRemoveSubjectFace(@Body KairosRequest_GalleryRemoveSubjectFace req);

}

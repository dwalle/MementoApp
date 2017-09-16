package FirebaseBackend.POJO;

/**
 * Created by Daniel on 8/1/2017.
 */

public class Person_Kairos_Extra {
    private String kairos_faceid;
    private String image_url;



    private String firebase_uid;

    public Person_Kairos_Extra(){

    }

    public String getKairos_faceid() {
        return kairos_faceid;
    }

    public void setKairos_faceid(String kairos_faceid) {
        this.kairos_faceid = kairos_faceid;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }
    public String getFirebase_uid() {
        return firebase_uid;
    }

    public void setFirebase_uid(String firebase_uid) {
        this.firebase_uid = firebase_uid;
    }

}

package FirebaseBackend.POJO;
import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by Daniel on 8/1/2017.
 */

@IgnoreExtraProperties
public class Person_Memento {


    private String name;
    private String image_url;
    private String audio_url;
    private String description;

    public Person_Memento(){

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }
    public String getAudio_url() {
        return audio_url;
    }

    public void setAudio_url(String audio_url) {
        this.audio_url = audio_url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }



}

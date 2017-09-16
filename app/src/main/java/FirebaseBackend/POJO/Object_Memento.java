package FirebaseBackend.POJO;

import java.util.ArrayList;

/**
 * Created by Daniel on 8/10/2017.
 */

public class Object_Memento {

    private String name;
    private String image_url;
    private String audio_url;
    private String description;
    private ArrayList<String> labelsList;

    public Object_Memento(){

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

    public void setDescription(String _description) {
        this.description = _description;
    }

    public ArrayList<String> getLabelsList() {
        return labelsList;
    }

    public void setLabelsList(ArrayList<String> labelsList) {
        this.labelsList = labelsList;
    }
}

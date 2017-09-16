package CloudVisionBackend;

/**
 * Created by Daniel on 8/10/2017.
 */

public class UICheckBoxModel {


    String name;
    int value; /* 0 -&gt; checkbox disable, 1 -&gt; checkbox enable */

    public UICheckBoxModel(String name, int value){
        this.name = name;
        this.value = value;
    }
    public String getName(){
        return this.name;
    }
    public int getValue(){
        return this.value;
    }
    public void setValue(int value) {
        this.value = value;
    }
}

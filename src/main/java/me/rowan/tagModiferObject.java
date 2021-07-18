package me.rowan;

public class tagModiferObject {

    public String tagName;
    public float value;


    public tagModiferObject(String tagName, float value) {
        this.tagName = tagName;
        this.value = value;
    }

    public String printTag() {

        return this.tagName + " " + this.value;
    }
}

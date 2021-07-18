package me.rowan;

public class OutputTagsJson {

        private String tagName;
        private float baseValue;

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public float getBaseValue() {
        return baseValue;
    }

    public void setBaseValue(float baseValue) {
        this.baseValue = baseValue;
    }

    public OutputTagsJson(String tagName, float baseValue) {
        this.tagName = tagName;
        this.baseValue = baseValue;
    }

    public void printJson() {

        System.out.println("The tag name is " + this.tagName);

        System.out.println("The tag base score is " + this.baseValue);

    }
}

package me.rowan;


import java.util.List;

public class OutputRelicJson {
    private List<tagModiferObject> tags;
    private String relicName;

    public List<tagModiferObject> getTags() {
        return tags;
    }

    public void setTags(List<tagModiferObject> tags) {
        this.tags = tags;
    }

    public String getRelicName() {
        return relicName;
    }

    public void setRelicName(String relicName) {
        this.relicName = relicName;
    }

    public OutputRelicJson(List<tagModiferObject> tags, String relicName) {
        this.tags = tags;
        this.relicName = relicName;
    }

    public void printRelicJsonObject() {
        System.out.println("The relic name is " + this.relicName);

        System.out.println("The tags are ");

       tagModiferObject[] arrTags =  this.tags.toArray(new tagModiferObject[0]);

        for (tagModiferObject tag: arrTags
             ) {
            System.out.println( tag.printTag());
        }
    }
}

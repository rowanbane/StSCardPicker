package me.rowan;

import java.util.List;

public class OutputCardJson {
    private String cardName;
    private float baseScore;
    private List<String> tagsProvided;
    private List<String> tagsAffected;
    private float[] actModifer;
    private List<tagModiferObject> synergyModifer;
    private float oneOfRule;
    private float oneOfModifier;

    public OutputCardJson(String cardName, float baseScore, List<String> tagsProvided, List<String> tagsAffected, float[] actModifer, List<tagModiferObject> synergyModifer, float oneOfRule, float oneOfModifier) {
        this.cardName = cardName;
        this.baseScore = baseScore;
        this.tagsProvided = tagsProvided;
        this.tagsAffected = tagsAffected;
        this.actModifer = actModifer;
        this.synergyModifer = synergyModifer;
        this.oneOfRule = oneOfRule;
        this.oneOfModifier = oneOfModifier;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public float getBaseScore() {
        return baseScore;
    }

    public void setBaseScore(float baseScore) {
        this.baseScore = baseScore;
    }

    public List<String> getTagsProvided() {
        return tagsProvided;
    }

    public void setTagsProvided(List<String> tagsProvided) {
        this.tagsProvided = tagsProvided;
    }

    public List<String> getTagsAffected() {
        return tagsAffected;
    }

    public void setTagsAffected(List<String> tagsAffected) {
        this.tagsAffected = tagsAffected;
    }

    public float[] getActModifer() {
        return actModifer;
    }

    public void setActModifer(float[] actModifer) {
        this.actModifer = actModifer;
    }

    public List<tagModiferObject> getSynergyModifer() {
        return synergyModifer;
    }

    public void setSynergyModifer(List<tagModiferObject> synergyModifer) {
        this.synergyModifer = synergyModifer;
    }

    public float getOneOfRule() {
        return oneOfRule;
    }

    public void setOneOfRule(float oneOfRule) {
        this.oneOfRule = oneOfRule;
    }

    public float getOneOfModifier() {
        return oneOfModifier;
    }

    public void setOneOfModifier(float oneOfModifier) {
        this.oneOfModifier = oneOfModifier;
    }

    public void printJsonObject() {
        //Name
        System.out.println("The card name is " + this.cardName);


        //Base Score
        System.out.println("The card base score is " + this.baseScore);


        //Tags Prov
        System.out.println("The tags provided are ");

        String[] arrTagsProv = this.tagsProvided.toArray(new String[0]);

        for (String tag : arrTagsProv
        ) {
            System.out.println(tag);
        }


        //Tags Aff
        System.out.println("The tags affected are ");

        String[] arrTagsAff = this.tagsAffected.toArray(new String[0]);

        for (String tag : arrTagsAff
        ) {
            System.out.println(tag);
        }

        //act Mods
        System.out.println("The act mods are ");

        for (float mod : actModifer
        ) {
            System.out.println(mod);
        }

        //Synergy mods
        System.out.println("The synergy mods are ");

        tagModiferObject[] arrSynergyMods =  this.synergyModifer.toArray(new tagModiferObject[0]);

        for (tagModiferObject tag: arrSynergyMods
        ) {
            System.out.println( tag.printTag());
        }

        //One of Rule
        System.out.println("The One of Rule is " + this.oneOfRule);

        //One of Rule Mod
        System.out.println("The One of Rule Mod is " + this.oneOfModifier);

    }
}

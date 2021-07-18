package me.rowan;

import basemod.BaseMod;
import basemod.interfaces.PostBattleSubscriber;
import basemod.interfaces.PostDungeonInitializeSubscriber;
import basemod.interfaces.PostExhaustSubscriber;


import basemod.interfaces.PostInitializeSubscriber;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.screens.CardRewardScreen;
import javassist.CannotCompileException;
import javassist.CtBehavior;

import java.util.*;

@SpireInitializer
public class TestMod implements PostExhaustSubscriber,
        PostBattleSubscriber, PostDungeonInitializeSubscriber, PostInitializeSubscriber {

    private int count;

    private int totalCount;

    public static HashMap<String, OutputCardJson> cardJsonHashMap = new HashMap<>();

    public static HashMap<String, OutputRelicJson> relicJsonHashMap = new HashMap<>();

    public static HashMap<String, OutputTagsJson> tagJsonHashMap = new HashMap<>();

    public static HashMap<String, Float> cardRatingMap = new HashMap<>();

    public static HashMap<String, Float> tagListAffecting = new HashMap<>();

    public static HashMap<String, Float> tagsPresent = new HashMap<>();

    public static List<String> deckCards = new ArrayList<String>();


    /**
     * Method comment
     */
    private void resetCounts() {
        totalCount = 0;
        count = 0;
    }

    public TestMod() {
        BaseMod.subscribe(this);
        resetCounts();
    }

    public static void initialize() {
        System.out.println("MY MOD IS CALLED YAAAAA");
        new TestMod();
    }

    @Override
    public void receivePostBattle(AbstractRoom abstractRoom) {
        System.out.println(count + " cards were exhausted this battle, " + totalCount + " cards have been exhausted so far this act.");
        count = 0;


    }


    @Override
    public void receivePostDungeonInitialize() {


        resetCounts();
    }

    @Override
    public void receivePostExhaust(AbstractCard abstractCard) {
        count++;
        totalCount++;
        System.out.println("Counts incremented");

    }

    @Override
    public void receivePostInitialize() {

        String assetPath = "me.rowanResources/";


        //Deserialize our JSON to have as lists in the code to reference
        //Cards
        try {
            Gson gson = new Gson();
            String json = Gdx.files.internal(assetPath + "cardJson.json").readString();
            Arrays.stream(gson.fromJson(json, OutputCardJson[].class)).forEach(card -> {
                cardJsonHashMap.put(card.getCardName(), card);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

        //Relics
        try {
            Gson gson = new Gson();
            String json = Gdx.files.internal(assetPath + "relicJson.json").readString();
            Arrays.stream(gson.fromJson(json, OutputRelicJson[].class)).forEach(relic -> {
                relicJsonHashMap.put(relic.getRelicName(), relic);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

        //Tags
        try {
            Gson gson = new Gson();
            String json = Gdx.files.internal(assetPath + "tagJson.json").readString();
            Arrays.stream(gson.fromJson(json, OutputTagsJson[].class)).forEach(tag -> {
                tagJsonHashMap.put(tag.getTagName(), tag);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @SpirePatch2(clz = CardRewardScreen.class, method = "open")
    public static class GenerateRatings {

        @SpirePostfixPatch
        public static void generateCardRewardRatings(ArrayList<AbstractCard> cards) {


            //Clears everything
            cardRatingMap.clear();
            tagListAffecting.clear();
            tagsPresent.clear();
            deckCards.clear();

            //Populate deck and relic data
            populatePlayerRunData();

            for (AbstractCard card : cards
            ) {

                cardRatingMap.put(card.name, calculateCardValue(formatCardName(card.name)));

            }

        }

    }


    @SpirePatch2(clz = CardRewardScreen.class, method = "renderCardReward")
    public static class RenderPrediction {


        @SpireInsertPatch(locator = Locator.class)
        public static void patch(CardRewardScreen __instance, SpriteBatch sb) {

            for (AbstractCard c : __instance.rewardGroup) {
                FontHelper.renderSmartText(sb,
                        FontHelper.topPanelAmountFont,
                        cardRatingMap.get(c.name).toString(),
                        c.hb.x,
                        c.hb.y,
                        Color.WHITE);
            }
        }
    }


    //Locator class to aid in finding the render point - From Slay AI
    private static class Locator extends SpireInsertLocator {
        public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
            Matcher finalMatcher = new Matcher.MethodCallMatcher(AbstractCard.class, "render");
            return LineFinder.findInOrder(ctMethodToPatch, new ArrayList<>(), finalMatcher);
        }
    }

    public static String formatCardName(String cardName) {
        return cardName.replace("+", "");
    }

    public static void populatePlayerRunData() {
        for (AbstractCard card : AbstractDungeon.player.masterDeck.group
        ) {

            if (!cardJsonHashMap.containsKey(card.name)) {
                continue;
            }

            //formats the name
            String formattedName = formatCardName(card.name);

            OutputCardJson cardEval = cardJsonHashMap.get(formattedName);

            //adds the cards edited name to the deck list
            deckCards.add(formattedName);


            //checks if the tags provided is preset then increments the count
            for (String tag : cardEval.getTagsProvided()
            ) {

                tagsPresent.computeIfPresent(tag, (k, v) -> v + 1);

                tagsPresent.computeIfAbsent(tag, (k) -> tagsPresent.put(k, 1f));

            }

            //Constructs the taglist affecting
            for (String tag : cardEval.getTagsProvided()
            ) {
                //If a tag doesn't exist in the map it adds it and the base value
                tagListAffecting.computeIfAbsent(tag, (k) -> tagListAffecting.put(k, tagJsonHashMap.get(tag).getBaseValue()));
            }

        }

        //Adds the player relic tags to the recognised pool and adds any modifiers to it all
        for (AbstractRelic relic : AbstractDungeon.player.relics
        ) {
            for (tagModiferObject relicTag : relicJsonHashMap.get(relic.name).getTags()) {
                //If a tag doesn't exist in the map it adds it and the base value
                tagListAffecting.computeIfPresent(relicTag.tagName, (k, v) -> v + relicTag.value);
                tagListAffecting.computeIfAbsent(relicTag.tagName, (k) -> tagListAffecting.put(k, tagJsonHashMap.get(relicTag.tagName).getBaseValue() + relicTag.value));
            }
        }
    }

    public static float calculateCardValue(String cardName) {

        OutputCardJson card = cardJsonHashMap.get(cardName);

        //Obtain base value
        float cardScore = card.getBaseScore();

        //obtain act modifier
        float actModifier = card.getActModifer()[AbstractDungeon.actNum];

        //Scale it by the card synergies present
        List<tagModiferObject> cardSynergies = card.getSynergyModifer();
        if (actModifier < 0) {

            float amountToDeduct = 0;
            for (tagModiferObject tag : cardSynergies
            ) {
                if (tagsPresent.containsKey(tag.tagName)) {
                    amountToDeduct = amountToDeduct + (tag.value * actModifier * tagsPresent.get(tag.tagName));
                }
            }

            actModifier = actModifier - amountToDeduct;
            if (actModifier> 0){
                actModifier = 0;
            }
        }
        cardScore = cardScore + actModifier;

        //Apply any tag bonuses
        for (String tagName: card.getTagsAffected()
             ) {
            if (tagListAffecting.containsKey(tagName))
            {
                cardScore = cardScore + tagListAffecting.get(tagName);
            }
        }




        return cardScore;
    }
}


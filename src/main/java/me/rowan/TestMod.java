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
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.screens.CardRewardScreen;
import com.megacrit.cardcrawl.shop.ShopScreen;
import javassist.CannotCompileException;
import javassist.CtBehavior;

import java.util.*;

@SpireInitializer
public class TestMod implements PostInitializeSubscriber {


    //A Hashmap that contains the card evaluation data
    public static HashMap<String, OutputCardJson> cardJsonHashMap = new HashMap<>();

    //A Hashmap that contains the relic evaluation data
    public static HashMap<String, OutputRelicJson> relicJsonHashMap = new HashMap<>();

    //A Hashmap that contains the tag evaluation data
    public static HashMap<String, OutputTagsJson> tagJsonHashMap = new HashMap<>();

    //A Hashmap that contains the card ratings for every card on the screen
    public static HashMap<String, Float> cardRatingMap = new HashMap<>();

    //A Hashmap that contains tag lists affecting each card in the deck
    public static HashMap<String, Float> tagListAffecting = new HashMap<>();

    //A Hashmap that contains tag lists present each card in the deck
    public static HashMap<String, Float> tagsPresent = new HashMap<>();

    //A list that a count of every card in the deck
    public static List<String> deckCards = new ArrayList<String>();


    public TestMod() {
        BaseMod.subscribe(this);
    }

    public static void initialize() {
        //Subscribes this mod
        new TestMod();
    }


    //Deserializes the evaluation json into objects in code for easy searching and typing of attributes
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

    //A patch to clear any rating data then generate new ratings
    @SpirePatch2(clz = CardRewardScreen.class, method = "open")
    public static class GenerateRatings {

        @SpirePostfixPatch
        public static void generateCardRewardRatings(ArrayList<AbstractCard> cards) {

            //If in combat then skip
            if (CardCrawlGame.isInARun() && AbstractDungeon.currMapNode != null && AbstractDungeon.getCurrRoom() != null && AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT)
            {
                return;
            }


            //Clears everything
            cardRatingMap.clear();
            tagListAffecting.clear();
            tagsPresent.clear();
            deckCards.clear();

            //Populate deck and relic data
            populatePlayerRunData();

            //Adds the cards rating to the map for each render cycle
            for (AbstractCard card : cards
            ) {
                cardRatingMap.put(card.name, calculateCardValue(formatCardName(card.name)));
            }
        }
    }


    //A patch to render card ratings on the reward screen
    @SpirePatch2(clz = CardRewardScreen.class, method = "renderCardReward")
    public static class RenderPrediction {


        @SpireInsertPatch(locator = Locator.class)
        public static void patch(CardRewardScreen __instance, SpriteBatch sb) {

            //If in combat then skip
            if (CardCrawlGame.isInARun() && AbstractDungeon.currMapNode != null && AbstractDungeon.getCurrRoom() != null && AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT)
            {
                return;
            }

            for (AbstractCard c : __instance.rewardGroup) {
                FontHelper.renderSmartText(sb,
                        FontHelper.topPanelAmountFont,
                        "score: " + cardRatingMap.get(c.name).toString(),
                        c.hb.x,
                        c.hb.y,
                        Color.WHITE);
            }
        }
    }


    //Locator class to aid in finding the render point
    private static class Locator extends SpireInsertLocator {
        public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
            Matcher finalMatcher = new Matcher.MethodCallMatcher(AbstractCard.class, "render");
            return LineFinder.findInOrder(ctMethodToPatch, new ArrayList<>(), finalMatcher);
        }
    }

    public static String formatCardName(String cardName) {
        //Removes the + from the card name to allow searching of the card
        return cardName.replace("+", "");
    }

    public static void populatePlayerRunData() {

        //Goes through the deck to populate evaluation base
        for (AbstractCard card : AbstractDungeon.player.masterDeck.group
        ) {

            //Continues if the card isn't contained
            if (!cardJsonHashMap.containsKey(card.name)) {
                continue;
            }

            //formats the name
            String formattedName = formatCardName(card.name);

            //Obtains the deserialized JSON evaluation object to calculate the score
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

        //Prints out the card name to show it's been recognised
        System.out.println(cardName);

        //Obtains the deserialized JSON evaluation object to calculate the score
        OutputCardJson card = cardJsonHashMap.get(cardName);

        //Obtain base value
        float cardScore = card.getBaseScore();


        //obtain act modifier
        float actModifier = card.getActModifer()[AbstractDungeon.actNum - 1];

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

            //Calculate the actmodifer after synergy modifers
            actModifier = actModifier - amountToDeduct;

            //If the act modifier is positive it will set to zero
            if (actModifier > 0) {
                actModifier = 0;
            }
        }
        cardScore = cardScore + actModifier;

        //Apply any tag bonuses
        for (String tagName : card.getTagsAffected()
        ) {
            if (tagListAffecting.containsKey(tagName)) {
                cardScore = cardScore + tagListAffecting.get(tagName);

            }
        }


        //Apply One of rule
        if (deckCards.contains(cardName)) {
            if (card.getOneOfRule() > AbstractDungeon.player.masterDeck.size()) {
                cardScore = cardScore * (1 - card.getOneOfModifier());
            }
        }

        return cardScore;
    }

    //A Patch to allow the evaluation of shop cards
    @SpirePatch2(clz = ShopScreen.class, method = "init")
    public static class InitCardHook {
        @SpirePostfixPatch
        public static void patch(ShopScreen __instance, ArrayList<AbstractCard> coloredCards, ArrayList<AbstractCard> colorlessCards) {

            //Clears everything
            cardRatingMap.clear();
            tagListAffecting.clear();
            tagsPresent.clear();
            deckCards.clear();

            //Populate deck and relic data
            populatePlayerRunData();

            //Populate the coloured card list for the shop
            for (AbstractCard card : coloredCards
            ) {
                cardRatingMap.put(card.name, calculateCardValue(formatCardName(card.name)));
            }
            //Populate the colourless card list for the shop
            for (AbstractCard card : colorlessCards
            ) {
                cardRatingMap.put(card.name, calculateCardValue(formatCardName(card.name)));
            }


        }
    }

    //A patch to render the evaluation of shop cards
    @SpirePatch2(clz = ShopScreen.class, method = "renderCardsAndPrices")
    public static class RenderShopCardEvaluations {
        @SpirePostfixPatch
        public static void patch(ShopScreen __instance, SpriteBatch sb) {
            for (AbstractCard c : __instance.coloredCards) {
                renderGridSelectPrediction(sb, c);
            }
            for (AbstractCard c : __instance.colorlessCards) {
                renderGridSelectPrediction(sb, c);
            }
        }

        //Function for the rendering of shop cards
        private static void renderGridSelectPrediction(SpriteBatch sb, AbstractCard card) {

            String cardRating = "score: " + cardRatingMap.get(card.name).toString();

            sb.setColor(Color.WHITE);
            FontHelper.renderSmartText(sb,
                    FontHelper.cardDescFont_N,
                    cardRating,
                    card.hb.cX - FontHelper.getSmartWidth(FontHelper.cardDescFont_N, cardRating, Float.MAX_VALUE, FontHelper.cardDescFont_N.getSpaceWidth()) * 0.5f,
                    card.hb.y + (12f * Settings.scale),
                    Color.WHITE);
        }
    }
}


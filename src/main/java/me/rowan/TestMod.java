package me.rowan;

import basemod.BaseMod;
import basemod.interfaces.PostBattleSubscriber;
import basemod.interfaces.PostDungeonInitializeSubscriber;
import basemod.interfaces.PostExhaustSubscriber;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.screens.CardRewardScreen;
import javassist.CannotCompileException;
import javassist.CtBehavior;

import java.util.ArrayList;
import java.util.Map;

@SpireInitializer
public class TestMod implements PostExhaustSubscriber,
        PostBattleSubscriber, PostDungeonInitializeSubscriber {

    private int count;

    private int totalCount;

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

    @SpirePatch(clz = CardRewardScreen.class, method = "renderCardReward")
    public static class RenderPrediction {


        @SpireInsertPatch(locator = Locator.class)
        public static void patch(CardRewardScreen __instance, SpriteBatch sb) {

            for (AbstractCard c : __instance.rewardGroup) {
                FontHelper.renderSmartText(sb,
                        FontHelper.topPanelAmountFont,
                        c.name,
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

}


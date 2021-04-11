package me.rowan;

import basemod.BaseMod;
import basemod.interfaces.PostBattleSubscriber;
import basemod.interfaces.PostDungeonInitializeSubscriber;
import basemod.interfaces.PostExhaustSubscriber;


import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.rooms.AbstractRoom;

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
}

package Main;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.randoms.RandomEvent;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.methods.interactive.Players;

import java.awt.*;
import java.lang.reflect.Array;

@ScriptManifest(category = Category.FISHING,name="Nino's Minnows Script LITE",author = "Nino",version = 1.0)
public class MainClass extends AbstractScript{

    @Override
    public void onStart() {
        log("Welcome To Ninoo's Minnows script lite");
    }


    @Override
    public int onLoop() {
        NPC fishingSpot = getNpcs().closest("Fishing spot");

        if(getPlayers().myPlayer().isInteracting(fishingSpot) == true){ //if already fishing then do nothing

        }
        else{   // else find closest spot
            sleep(Calculations.random(1200,2000));  //Slow it down and added variance
            fishingSpot.interactForceLeft("Small Net");
        }

        return 1000;

    }

    @Override
    public void onExit() {

    }

    @Override
    public void onPaint(Graphics g) {

    }
}
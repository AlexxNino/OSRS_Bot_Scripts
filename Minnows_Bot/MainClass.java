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

@ScriptManifest(category = Category.FISHING,name="Minnows Script",author = "Nino",version = 1.0)
public class MainClass extends AbstractScript{

    public static int count = 0;
    public static int TIME_TO_LOG = 2500;  /*   3600 = ~1hr , 1800 = 30min if on 1 sec interval*/
    public static int LOGOUT_TIME = Calculations.random(600000,900000);
    private final Point STATS_WIDGET = new Point(577, 186); // Stats menu
    private final Point INVENTORY_WIDGET = new Point(643, 185); // Inventory menu
    private Zen z;
    private int START_X;
    private int START_Y;


    @Override
    public void onStart() {
        // Construct the Zen helper class and attach it to this script
        z = new Zen(this);
        // Wait until the user is logged in before building the GUI as it depends on character data
        while (!getClient().isLoggedIn())
            z.setStatus("#Logged out");
        // Setup GUI
        z.gui.addStringInput(0, "Starting Coords (x,y)", "location", "Leave blank to use current location", "");
        if (!z.showGUI())
            return;
        // Add starting items to don't-drop list if the option is selected
        z.saveStartingItems();

        // Set up antiban
        if (!z.gui.getString("chatflags").equals(""))
            z.ban.setChatFlags(z.gui.getString("chatflags").split(","));

        for(String s : z.gui.getString("dontdrop").split(","))
            z.dontDrop(s);

        if(z.gui.getStrings("location") == null) {
            START_X = getLocalPlayer().getX();
            START_Y = getLocalPlayer().getY();
        } else {
            START_X = Integer.parseInt(z.gui.getStrings("location")[0]);
            START_Y = Integer.parseInt(z.gui.getStrings("location")[1]);
        }
    }



    public void sleeper() {
        log("Entering Sleeper");
        getTabs().logout(); /* Logs out */
        getRandomManager().disableSolver(RandomEvent.LOGIN);
        sleep(LOGOUT_TIME); /* Log out for x time */
        getRandomManager().enableSolver(RandomEvent.LOGIN); /* AutoMatic Login */
    }

    /*"A flying fish jumps up and eats some of your minnows!"*/

    @Override
    public int onLoop() {
        NPC fishingSpot = getNpcs().closest("Fishing spot");
        /*Small Net Fishing Spot */
        /*if (getPlayers().myPlayer().isAnimating() == false)*/
        if(z.getAntiBan().doRandom())
            log("Script-specific random flag triggered");
        if(getPlayers().myPlayer().isInteracting(fishingSpot) == true){

        }
        else{
            /* log("Looking for a new fishing spot..."); */
            sleep(Calculations.random(1200,2000));
            fishingSpot.interactForceLeft("Small Net");
        }
        count++;
        if (count == TIME_TO_LOG){
            sleeper();
        }

        return z.getAntiBan().antiBan();
    }

    @Override
    public void onExit() {
    }

    @Override
    public void onPaint(Graphics g) {
        g.drawString("Anti-Ban Status: " + (z.getAntiBan().getStatus().equals("") ? "Inactive" : z.getAntiBan().getStatus()), 10, 100);
        g.drawString("", 10, 100);
    }
}

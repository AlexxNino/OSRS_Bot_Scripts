package Main;
/* Best times? CT 11pm - 7am */
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.randoms.RandomEvent;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.methods.interactive.Players;
import java.awt.*;

@ScriptManifest(category = Category.FISHING,name="Minnows Script",author = "Nino",version = 1.0)
public class MainClass extends AbstractScript{

    public static int count = 0;
    public static int TIME_TO_LOG = 3600;  /*   3600 = 1hr , 1800 = 30min */
    public static int LOGOUT_TIME = Calculations.random(600000,900000);
    @Override
    public void onStart() {
        log("Welcome");
    }
    public void DO_RANDOM_SHIT(){
        /* 
        ==Things to do==
        -Examine random entities (objects, npcs, ground and inventory items etc)
        -Check a random stat (you can tell it which stats to check with antiban.setStatsToCheck(Skill.ATTACK, Skill.DEFENCE);)
        -Move the mouse to a random location (and sometimes click the left or right button)
        -Walk to a random location nearby
        -Chop a random tree nearby
        -Click on a random entity (object, npc, item etc.)
        -Go AFK for a while (turns off autologin and random solvers temporarily)
        -Open your inventory
        -Open your stats menu
        -Open your magic menu
        -Move the mouse off-screen for a while
        */
        
    }



    public void sleeper() {
        getTabs().logout(); /* Logs out */
        getRandomManager().disableSolver(RandomEvent.LOGIN);
        sleep(LOGOUT_TIME); /* Log out for x time */
        getRandomManager().enableSolver(RandomEvent.LOGIN); /* AutoMatic Login */
    }


    @Override
    public int onLoop() {
        NPC fishingSpot = getNpcs().closest("Fishing spot");
        /*Small Net Fishing Spot */
        /*if (getPlayers().myPlayer().isAnimating() == false)*/
        if(getPlayers().myPlayer().isInteracting(fishingSpot) == true){
            log("Fishing...");
        }
        else{
            log("Looking for a new fishing spot...");
            fishingSpot.interactForceLeft("Small Net");
        }
        count++;
        if (count == TIME_TO_LOG){
            sleeper();
        }

        return Calculations.random(1800, 2200);
    }

    @Override
    public void onExit() {
    }

    @Override
    public void onPaint(Graphics graphics) {
        super.onPaint(graphics);
    }
}

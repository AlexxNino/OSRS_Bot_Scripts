import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.listener.MessageListener;
import org.dreambot.api.utilities.Timer;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.widgets.message.Message;

import java.awt.*;

@ScriptManifest(category = Category.FISHING,name="Nino's Minnows Script LITE",author = "Nino",version = 1.01)
public class MainClass extends AbstractScript implements MessageListener {
    private long start;
    public NPC fishingSpot;
    @Override
    public void onStart() {
        fishingSpot = getNpcs().closest("Fishing spot");
        log("Welcome To Ninoo's Minnows script lite");
        start = System.currentTimeMillis();
        getSkillTracker().start(true);
    }

    @Override
    public int onLoop() {
        if(getPlayers().myPlayer().isInteracting(fishingSpot) == true ){

        }
        else{
            fishingSpot.interactForceLeft("Small Net");
        }
        return 1000;
    }
    @Override
    public void onPaint(Graphics g) {
        g.drawString("Runtime: " + Timer.formatTime(System.currentTimeMillis() - start), 15, 45);
        g.drawString("Fishing XP gained (per hour): " + getSkillTracker().getGainedExperience(Skill.FISHING) + " (" + getSkillTracker().getGainedExperiencePerHour(Skill.FISHING) + ")", 15, 60);

    }
    @Override
    public void onGameMessage(Message message) {
        if (message.getMessage().contains("flying fish")){
            NPC currentSpot = getNpcs().closest("Fishing spot");
            for(int i=0;i<5;i++){
                fishingSpot = getNpcs().all("Fishing spot").get(i);
                if (currentSpot != fishingSpot){
                    fishingSpot.interactForceLeft("Small Net");
                    log("sleep starting");
                    sleep(Calculations.random(2000,3000));
                    log("inside the if..breaking");
                    break;
                }
            }

        }
    }

    @Override
    public void onPlayerMessage(Message message) {

    }

    @Override
    public void onTradeMessage(Message message) {

    }

    @Override
    public void onPrivateInMessage(Message message) {

    }

    @Override
    public void onPrivateOutMessage(Message message) {

    }
}
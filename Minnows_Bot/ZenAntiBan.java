package Main;

import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.randoms.RandomEvent;
import org.dreambot.api.wrappers.interactive.Entity;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.wrappers.widgets.Menu;
import org.dreambot.api.wrappers.widgets.message.Message;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import static org.dreambot.api.methods.MethodProvider.log; /*Added log to investigate error*/

/**
 * This class holds all of my anti-ban ideas.
 *
 * @author Zenarchist
 */
public class ZenAntiBan {
    public int MIN_WAIT_NO_ACTION = 50; // This is the minimum time to wait if no action was taken
    public int MAX_WAIT_NO_ACTION = 100; // This is the maximum time to wait if no action was taken
    private Zen z; // Script helper
    private String STATUS = ""; // Current anti-ban status
    private Skill[] STATS_TO_CHECK = { Skill.HITPOINTS }; // This is used for determining which stats to randomly check
    public int MIN_WAIT_BETWEEN_EVENTS = 10; // In seconds
    private long LAST_EVENT = 0L; // Last time an antiban event was triggered
    private long LAST_IDLE; // Last time we idled for a while
    private boolean DO_RANDOM = false; // This is a generic flag for randomly doing something early in a script for anti-patterning
    // Chatbot variables
    private String CHATBOT_STATE = "x"; // This is used for continuing conversations (not that it matters - chatbot is dumb as hell)
    private long LAST_CHATBOT_REPLY = 0L; // Last time the chatbot replied
    private int CHATBOT_REPLIES = 0; // How many times the chatbot has replied to player messages
    private final String VALID_CHARACTERS = "abcdefghijklmnopqrstuvwxyz"; // Valid characters for chatbot
    private long LAST_PERSONAL_REPLY = 0L; // Last time we replied to a 'bot' flagged message or a message addressed to us
    private String[] CHAT_FLAGS = {};
    private String CHATBOT_KEY = "xxx";

    // Constructs a new Anti-Ban class with the given script-helper
    public ZenAntiBan(Zen zen) {
        this.z = zen;
        // Set last idle to now to avoid idling early into the script
        LAST_IDLE = System.currentTimeMillis();
    }

    // Sets the custom chat flags to scan for
    public void setChatFlags(String[] chatFlags) {
        this.CHAT_FLAGS = chatFlags;
    }

    // Returns the wait time for when the antiban system does nothing
    private int doNothing() {
        return z.rh(MIN_WAIT_NO_ACTION, MAX_WAIT_NO_ACTION);
    }

    // Sets the stats to check during random antiban events
    public void setStatsToCheck(Skill... skills) {
        STATS_TO_CHECK = skills;
    }

    // Returns the sleep time after performing an anti-ban check
    public int antiBan() {

        setStatus("");
        if(z.gui.getInt("antibanrate") == 0 || System.currentTimeMillis() - LAST_EVENT <= z.r(MIN_WAIT_BETWEEN_EVENTS * 1000, MIN_WAIT_BETWEEN_EVENTS * 2000))
            return doNothing();

        // If we have moved the mouse outside of the screen, wait a moment before performing the ban action
        if(z.s.getMouse().getX() == -1 && z.s.getMouse().getY() == -1)
            z.sleep(1000, 2000);

        // Calculate overall random anti-ban intervention rate (%)
        int rp = z.r(0, 100);
        if(rp < z.gui.getInt("antibanrate")) {
            // Calculate event-specific activation rate (%)
            rp = z.r(0, 100);
            // Calculate event ID
            int event = z.r(0, 14);
            // Handle specified event
            switch(event) {
                case 0: { // Examine random entity

                    if(rp < 25) { // 25% chance
                        log("Examine random ent: 25% chance");
                        int r = z.r(1, 3);
                        Entity e = z.s.getGameObjects().closest(o -> o != null && !o.getName().equals("null") && z.r(1, 2) != 1);
                        if (e == null || r == 2) {
                            e = z.s.getNpcs().closest(n -> n != null && !n.getName().equals("null"));
                            if (e == null || r == 3) {
                                e = z.s.getGroundItems().closest(i -> i != null && !i.getName().equals("null"));
                                if (e == null)
                                    return doNothing();
                            }
                        }

                        setStatus("Examining entity (" + e.getName() + ")");
                        z.s.getMouse().move(e);

                        if(z.r(0, 100) < 99) { // 99% chance of clicking examine
                            // Open right-click menu and find Examine option
                            Menu menu = new Menu(z.s.getClient());
                            z.rh(1, 100);
                            menu.open();
                            z.s.sleep(z.rh(250, 1000));
                            if (menu.contains("Examine"))
                                menu.clickAction("Examine", e);
                            else
                            if(menu.contains("Cancel"))
                                menu.clickAction("Cancel");
                        }

                        LAST_EVENT = System.currentTimeMillis();
                        return z.rh(250, 3000);
                    }
                }
                case 1: { // Check random stat

                    if(rp < 10) { // 10% chance
                        log("Check random stat: 10% chance");
                        if (z.s.getTabs().getOpen() != Tab.STATS)
                            z.openStats();
                        int x = z.r(0, 25);
                        int y = z.r(0, 15);
                        int skill = -1;
                        long t = System.currentTimeMillis();
                        while (skill == -1 && System.currentTimeMillis() - t <= 500) {
                            int r = z.r(0, Skill.values().length - 1);
                            for (Skill s : STATS_TO_CHECK) {
                                if (s.getName().equals(Skill.values()[r].getName()))
                                    skill = r;
                            }
                        }

                        setStatus("Checking EXP (" + Skill.values()[skill].getName() + ")");
                        Point p = z.STAT_WIDGET[skill];
                        p.setLocation(p.getX() + x, p.getY() + y);
                        z.s.getMouse().move(p);

                        LAST_EVENT = System.currentTimeMillis();
                        return z.rh(2000, 5000);
                    }
                }
                case 2: { // Type something random

                    if(rp < 1) { // 1% chance
                        log("Type Something random");
                        String s = "";
                        int r = z.r(1, 2);
                        for(int i = 0; i < r; i++)
                            s += VALID_CHARACTERS.charAt(z.r(0, VALID_CHARACTERS.length()));
                        setStatus("Typing random keys (" + s + ")");
                        /*z.s.getKeyboard().type(s); Temp disable, don't like this*/

                        LAST_EVENT = System.currentTimeMillis();
                        return z.rh(500, 3000);
                    }
                }
                case 3: { // Move mouse to random location (and sometimes click)

                    if(rp < 10) { // 10% chance
                        log("move mouse to random Location: 10% chance");
                        int r = z.r(0, 100);
                        int x = z.r(0, 760);
                        int y = z.r(0,500);
                        setStatus("Moving mouse (" + x + "," + y + ")");
                        z.s.getMouse().move(new Point(x, y));
                        if(r < 5) // 10% chance of right-clicking
                            z.s.getMouse().click(true);
                        else
                        if(r < 5) // 5% chance of left-clicking
                            z.s.getMouse().click();

                        LAST_EVENT = System.currentTimeMillis();
                        return z.rh(500, 3000);
                    }
                }
                case 4: { // Walk to random location

                    if(rp < 0) { // 0% chance
                        log("walk to random location: 0% chance");
                        int x = z.s.getLocalPlayer().getX() - 15;
                        int y = z.s.getLocalPlayer().getY() - 15;
                        int x2 = z.r(0, 30);
                        int y2 = z.r(0, 30);
                        Area a = new Area(x, y, x2, y2);
                        Tile t = a.getRandomTile();
                        setStatus("Walking to random tile (" + t.getX() + "," + t.getY() + ")");
                        z.s.getWalking().walk(t);
                        LAST_EVENT = System.currentTimeMillis();
                        return z.rh(500, 3000);
                    }
                }
                case 5: { // Chop random tree

                    if(rp < 0) { // 1% chance , changed to 0 i dont like it
                        log("chop random tree");
                        GameObject obj = z.s.getGameObjects().closest(o -> o != null && !o.getName().equals("null") && z.r(1, 2) != 1 && o.hasAction("Chop down") && z.s.getLocalPlayer().distance(o) < 5);
                        if(obj == null)
                            return doNothing();

                        setStatus("Chopping random tree (" + obj.getName() + ")");
                        if(z.r(1, 2) != 1)
                            obj.interact("Chop down");
                        else
                            obj.interactForceLeft("Chop down");

                        LAST_EVENT = System.currentTimeMillis();
                        return z.rh(500, 3000);
                    }
                }
                case 6: { // Click random entity

                    if(rp < 1) { // 1% chance
                        log("Click random entity");
                        Entity e = z.s.getGameObjects().closest(o -> o != null && !o.getName().equals("null")  && z.r(1, 2) != 1 && z.s.getLocalPlayer().distance(o) < 5);
                        int r = z.r(1, 3);
                        if(e == null || r == 2) {
                            e = z.s.getNpcs().closest(n -> n != null && !n.getName().equals("null"));
                            if(e == null || r == 3) {
                                e = z.s.getGroundItems().closest(i -> i != null && !i.getName().equals("null"));
                                if(e == null)
                                    return doNothing();
                            }
                        }

                        if(e instanceof NPC && z.s.getCombat().isInMultiCombat())
                            break;

                        setStatus("Clicking random entity (" + e.getName() + ")");
                        z.s.getMouse().move(e);
                        z.s.sleep(z.rh(0, 50));
                        if(z.r(0, 100) < 25)
                            z.s.getMouse().click(true);
                        else
                            z.s.getMouse().click();

                        LAST_EVENT = System.currentTimeMillis();
                        return z.rh(500, 3000);
                    }
                }
                case 7: { // Just idle for a while

                    if(rp < 5) { // 5% chance
                        log("Just idle");
                        if (System.currentTimeMillis() - LAST_IDLE >= 300000) { // Only allow idling to occur every 5+ minutes
                            int minsRunning = z.getMinsRunning();
                            int idle = z.r(minsRunning < 60 ? 20000 : 60000, 120000 + (minsRunning * 1200));
                            setStatus("Idling for " + (idle / 1000) + " seconds");
                            if (z.r(0, 100) < 99)
                                z.s.getMouse().moveMouseOutsideScreen();
                            // Disable dismiss & autologin solvers temporarily
                            z.s.getRandomManager().disableSolver(RandomEvent.LOGIN);
                            z.s.getRandomManager().disableSolver(RandomEvent.DISMISS);
                            // Sleep for the calculated time
                            z.s.sleep(idle);
                            // Enable dismiss & autologin solvers and resume script as normal
                            z.s.getRandomManager().enableSolver(RandomEvent.LOGIN);
                            z.s.getRandomManager().enableSolver(RandomEvent.DISMISS);
                            LAST_IDLE = System.currentTimeMillis();
                            return 1;
                        }
                    }
                }
                case 8: { // Open inventory or stats

                    if(rp < 25) { // 25% chance
                        log("open inventory");
                        if(z.s.getTabs().getOpen() != Tab.INVENTORY && z.s.getInventory().getEmptySlots() > 0)
                            setStatus("Opening inventory");
                        if (z.openInventory()) {
                            LAST_EVENT = System.currentTimeMillis();
                            z.sleep(50, 100);
                            z.s.getMouse().moveMouseOutsideScreen();
                            return z.rh(500, 1000);
                        }
                    } else
                    if(rp > 90) { // 10% chance
                        log("open stats");
                        if(z.s.getTabs().getOpen() != Tab.STATS)
                            setStatus("Opening stats");
                        if(z.openStats()) {
                            LAST_EVENT = System.currentTimeMillis();
                            z.sleep(50, 100);
                            z.s.getMouse().moveMouseOutsideScreen();
                            return z.rh(500, 1000);
                        }
                    }
                }
                case 9: { // Open combat menu (only if we are training melee stats)

                    boolean meleeStats = false;
                    for(Skill s : STATS_TO_CHECK)
                        if(s.equals(Skill.ATTACK) || s.equals(Skill.STRENGTH) || s.equals(Skill.DEFENCE))
                            meleeStats = true;

                    if(!meleeStats)
                        break;

                    if(rp < 5) { // 5% chance
                        if (z.s.getTabs().getOpen() != Tab.COMBAT)
                            setStatus("Opening combat menu");
                        z.openCombat();
                        z.sleep(50, 100);
                        z.s.getMouse().moveMouseOutsideScreen();
                        LAST_EVENT = System.currentTimeMillis();
                        return z.rh(500, 1000);
                    }
                }
                case 10: { // Moving mouse off-screen for a moment

                    if(rp < 50) { // 50% chance
                        log("move mouse off-screen");
                        if(z.s.getMouse().getX() == -1 && z.s.getMouse().getY() == -1)
                            return doNothing();

                        setStatus("Moving mouse off-screen");
                        z.s.getMouse().moveMouseOutsideScreen();
                        LAST_EVENT = System.currentTimeMillis();
                        return z.rh(5000, 8000);
                    }
                }
                case 11: { // Open magic menu

                    if(rp < 1) { // 1% chance
                        log("Open Magic menu: 1% chance");
                        if(z.s.getTabs().getOpen() != Tab.MAGIC) {
                            setStatus("Opening magic menu");
                            z.openMagic();
                            z.sleep(50, 100);
                            z.s.getMouse().moveMouseOutsideScreen();
                        }
                    }
                }
                case 12: { // Examine random inventory item

                    if(rp < 1) { // 1% chance
                        log("Examine random inventory item: 1% chance");
                        if (z.openInventory())
                            z.sleep(10, 250);
                        for(Item i : z.s.getInventory().all(it -> it != null)) {
                            if(i != null && z.r(1, 3) == 2) {
                                setStatus("Examining item (" + i.getName() + ")");
                                z.s.getMouse().move(i.getDestination());
                                z.s.sleep(z.rh(0, 50));
                                // Open right-click menu and find Examine option
                                Menu menu = new Menu(z.s.getClient());
                                menu.open();
                                z.s.sleep(z.rh(250, 1000));
                                if (menu.contains("Examine"))
                                    menu.clickAction("Examine");
                                else
                                if(menu.contains("Cancel"))
                                    menu.clickAction("Cancel");

                                break;
                            }
                        }
                    }
                }
                case 13: { // Move camera randomly

                    if(rp < 30) { // 30% chance
                        log("Move Camera Random: 30% chance");
                        print("Moving camera");
                        Area a = new Area(z.getX() - 10, z.getY() - 10, z.getX() + 10, z.getY() + 10);
                        z.s.getCamera().rotateToTile(a.getRandomTile());
                        return doNothing();
                    }
                }
                case 14: { // Do early flag
                    if(rp < 5) { // 5% chance
                        DO_RANDOM = true;
                        return doNothing();
                    }
                }
                default:
                    return doNothing();
            }

        }

        return doNothing();
    }

    // Returns whether or not the DO_RANDOM flag has been triggerd
    public boolean doRandom() {
        if(DO_RANDOM) {
            DO_RANDOM = false;
            return true;
        }

        return false;
    }

    // Get antiban status
    public String getStatus() {
        return STATUS;
    }

    // Print to the console if debug is enabled
    private void print(Object o) {
        if(z.gui.getBoolean("debug"))
            z.print("[AntiBan] " + o.toString());
    }

    // Returns the chatbot reply count
    public int getChatBotReplyCount() {
        return CHATBOT_REPLIES;
    }

    // Returns a chat reply from Cleverbot
    public String getChatbotReply(String phrase) {
        // If message contains spam, ignore it
        if(containsSpam(phrase))
            return null;

        // If message does not contain any letters, ignore it
        boolean containsLetter = false;
        for(char c : VALID_CHARACTERS.toCharArray()) {
            if(phrase.contains("" + c))
                containsLetter = true;
        }

        if(!containsLetter)
            return null;

        String returner = null;
        phrase = phrase.toLowerCase().trim().replaceAll("[^\\w\\s]", "").replaceAll(z.s.getLocalPlayer().getName().toLowerCase(), "");
        phrase = phrase.replaceAll("/", "");

        try {
            // Fetch URL result for chatbot API call
            URL url = new URL("https://www.cleverbot.com/getreply?key=" + (z.gui.getString("chatkey").equals("") ? CHATBOT_KEY : z.gui.getString("chatkey")) + "&input=" + phrase.toLowerCase().trim() + "&cs=" + CHATBOT_STATE + "&callback=ProcessReply");
            URLConnection urlConnection = url.openConnection();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line = bufferedReader.readLine();
            String T1 = "\"output\":";
            String T2 = "\",\"";
            // Scrape relevant data from JSON output
            String reply = line.substring(line.indexOf(T1) + T1.length());
            reply = reply.substring(0, reply.indexOf(T2)).replaceAll("\"", "").replaceAll("\\.", "");
            String cleverbotState = line.substring(20, line.indexOf("\",\""));
            // Remove any special characters and inappropriate words from the bot reply
            reply = reply.toLowerCase().replaceAll("cleverbot", "");
            reply = reply.toLowerCase().replaceAll("robot", "");
            reply = reply.toLowerCase().replaceAll("name", "deal");
            reply = reply.toLowerCase().replaceAll("how old", "");
            reply = reply.toLowerCase().replaceAll("age", "deal");
            reply = reply.replaceAll("[^\\w\\s]","");

            // Randomly mix up certain punctuation
            if(z.r(1, 5) == 3)
                reply = reply.replaceAll("'", "");
            if (reply.equalsIgnoreCase("what?") && z.r(1, 6) == 2)
                reply = reply + "!";

            // If reply does not contain any letters at all, then ignore it
            boolean letterFound = false;
            for(char c : VALID_CHARACTERS.toCharArray()) {
                if(reply.contains("" + c))
                    letterFound = true;
            }

            if(!letterFound)
                return null;

            returner = reply;
            CHATBOT_STATE = cleverbotState;
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
            print("ERROR: Failed to get reply from chatbot!");
        }

        return returner;
    }

    // Checks to see if the given message should be replied to
    public boolean checkPlayerMessage(Message message) {
        // Ignore spam
        if(!z.gui.getBoolean("chatbot") || containsSpam(message.getMessage()))
            return false;

        long now = System.currentTimeMillis();

        // If script has just started, do not reply to chat
        if(now - z.getStartTime() <= 60000)
            return false;

        String text = message.getMessage().toLowerCase().trim();
        String user = message.getUsername();
        Player speaker = null;

        // Check local players to find if the Speaker exists as a Player entity
        for(Player p : z.s.getPlayers().all()) {
            if(p != null && p.getName().equalsIgnoreCase(user))
                speaker = p;
        }

        // If the speaker player is null, or the speaker is us, or the speaker is far away, then do not reply
        if(speaker == null || z.s.getLocalPlayer().equals(speaker) || z.s.getLocalPlayer().distance(speaker) > 15)
            return false;

        int chatLimitMins = z.gui.getInt("chatbotrate") * 1000 * 60;
        // If we have replied within the last X minutes and this chat does not contain our player name or the word bot, don't reply
        if(now - LAST_CHATBOT_REPLY <= z.r(chatLimitMins, chatLimitMins * 2) && !containsPlayerName(text) && !containsBotWord(text)) {
            //print("We cannot reply for 5-10 more minutes");
            return false;
        }

        // If this chat DOES contain the word bot or our player name, and we have not replied to a similar message in the last 20-45 seconds, then reply.
        if(containsPlayerName(text) || containsBotWord(text)) {
            if(now - LAST_PERSONAL_REPLY <= z.r(20000, 45000)) {
                return false;
            }

            LAST_PERSONAL_REPLY = now;
        }

        // If 'only reply to username/chatflags' is selected, then do not handle the message unless it contains our name or a chat flag
        if(z.gui.getBoolean("chatbotusername") && (!containsPlayerName(text) && !containsBotWord(text)))
            return false;

        // Get reply from chatbot and type it
        String chatbot = getChatbotReply(message.getMessage());
        if(chatbot != null && !chatbot.equals("")) {
            LAST_CHATBOT_REPLY = System.currentTimeMillis();
            log("[ChatBot] Reply: " + chatbot + ", Phrase: " + message.getMessage() + ", From: " + message.getUsername());
            z.s.sleep(z.rh(3000, 5000));
            z.s.getKeyboard().type(chatbot, true, true);
            CHATBOT_REPLIES++;
            return true;
        }

        // No reply was given, return false
        return false;
    }

    // Returns true if any of these botting-related words are found in the given sentence
    public boolean containsBotWord(String phrase) {
        return phrase.contains("bot") || phrase.contains("macro") || phrase.contains("script") || phrase.contains("auto");
    }

    // Returns true if any of these spam-related wrods are found in the given sentence
    public boolean containsSpam(String phrase) {
        String spam = phrase.replaceAll(" ", "").toLowerCase().trim();
        // If message is selling OSRS gp or advertising a clan, ignore it
        if (spam.contains("sell") || spam.contains("usd") || spam.contains("gp") || spam.contains("fee") || spam.contains("free") || spam.contains("join") || phrase.equals("") || phrase.length() <= 1)
            return true;

        return false;
    }

    // Returns true if the player's name is found in the given string
    public boolean containsPlayerName(String phrase) {
        // Check if phrase straight-up contains our name
        boolean containsName = phrase.contains(z.s.getLocalPlayer().getName());

        // Check if this chat straight-up contains our username
        if(containsName)
            return true;

        // Check for spam
        if(containsSpam(phrase))
            return false;

        // Cycle through user-defined chat flags
        if(CHAT_FLAGS.length > 0) {
            for (String s : CHAT_FLAGS) {
                if (!s.equals("") && phrase.toLowerCase().contains(s.toLowerCase()))
                    return true;
            }
        }

        // Removes all numbers from our player's name
        String name = z.s.getLocalPlayer().getName().replaceAll("[^A-Za-z]", "");
        String[] nameWords = splitCamelCase(name);// Splits our name up by capital letters (for nicknames/shorthand - eg. ZenArchist becomes Zen,Archist)
        String[] spaceWords = z.s.getLocalPlayer().getName().toLowerCase().split(" "); // Splits our name up by spaces (eg. Zen Archist becomes Zen,Archist)

        // Cycle through camelcase name words
        if(nameWords != null && nameWords.length > 1) {
            for (String s : nameWords) {
                if (s.equals("") || s.length() <= 2)
                    continue;

                if (phrase.toLowerCase().contains(s.toLowerCase()))
                    containsName = true;
            }
        }

        // Cycle through spaced name words
        if(spaceWords != null && spaceWords.length > 1) {
            for(String s : spaceWords) {
                if(s.equals("") || s.length() <= 2)
                    continue;

                if(phrase.toLowerCase().contains(s.toLowerCase()))
                    containsName = true;
            }
        }

        // If we have found a chat flag, let the user know in the debug console
        if(containsName)
            print("[ChatBot] Flag: " + phrase + " = " + containsName);

        return containsName;
    }

    // Converts the given string into split words (ie. BitcoinMan becomes Bitcoin Man)
    private static String[] splitCamelCase(String s) {
        String split = s.replaceAll(
                String.format("%s|%s|%s",
                        "(?<=[A-Z])(?=[A-Z][a-z])",
                        "(?<=[^A-Z])(?=[A-Z])",
                        "(?<=[A-Za-z])(?=[^A-Za-z])"
                ),
                ","
        );

        return split.split(",");
    }

    // Allows an external class to set the anti-ban status
    public void setStatus(String status) {
        STATUS = status;
        if(!status.equals(""))
            print(status);
    }
}
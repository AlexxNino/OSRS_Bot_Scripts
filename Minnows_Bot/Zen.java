
package Main;

import org.dreambot.api.data.GameState;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.filter.Filter;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.randoms.RandomEvent;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.wrappers.widgets.Menu;
import org.dreambot.core.Instance;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

// This is a script-helper class designed to make generic actions easier to code across many scripts.
public class Zen {
    public AbstractScript s; // This holds the reference to the main script
    public ZenAntiBan ban; // This holds the reference to the anti-ban system
    public ZenGUI gui; // This holds the reference to the script settings GUI
    public final int GUI_X = 10; // GUI start X for script paint()
    public final int GUI_Y = 70; // GUI start Y for script paint()
    private final int IMAGE_X = 471; // Image X coordinate for paint()
    private final int IMAGE_Y = 433; // Image Y coordinate for paint()
    public String STATUS = "#Loading"; // Current script status (# before text = text color red)
    public long START_TIME; // Time script was started (used for stopping script after max duration)
    private long LAG_START_TIME; // This is used to calculating progressive lag multiplier
    private HashMap<Skill, Integer> START_LEVELS = new HashMap<>(); // Starting levels (used for calculating individual levels gained)
    private HashMap<Skill, Integer> START_EXP = new HashMap<>(); // Starting exps (used for calculating individual EXP gained)
    private int STARTING_TOTAL_EXP = 0; // Used for calculating total exp gained
    private int STARTING_TOTAL_LEVELS; // Used for calculating total levels gained
    private int HOPS_PAST_5_MINUTES = 0; // This is how many times we've world-hopped in the past 5 minutes
    private long LAST_WORLD_HOP = 0L; // This is used to throttle world hopping to prevent the bot flipping out if all the worlds are full of players
    private int MOUSE_SPEED; // This is the mouse speed (which is set randomly at start of script to help with anti-patterning)
    private int MAX_RUNTIME_MINUTES = -1; // This is the maximum amount of time the script should run for (used for calculating progressive lag multiplier + max duration)
    private boolean CHANGED_MOUSE_LAG = false; // This is set to true when we reach our initial max lag multiplier. Slows the mouse speed slightly
    private boolean CHANGED_MOUSE_LAG2 = false; // This is set to true when we reach our second max lag multiplier. Slows the mouse speed slightly
    private long LAST_CPU_CHECK = 0L; // This is used to throttle CPU load checks to avoid unnecessary memory usage in monitoring the memory usgage which would be fucking dumb
    private double LAST_CPU_LOAD = -1; // % of CPU load - used to store the last CPU load value in between checks for display purposes
    private int WORLD_HOPS = 0; // Total number of times we have world-hopped
    private Image icon = null; // Icon image to draw to game UI
    private String EXP_TIL_LVL = null; // Last EXP til Level string (store for UI display)
    private int RUN_THRESHOLD; // The threshold that our energy must reach in order to turn run on
    public long PAUSE_TIME = 0L; // This is the accumulated time that the script has been paused for
    // Item IDs
    public static int ITEM_COINS = 995; // Coins ID
    public static int ITEM_BONES = 526; // Bones ID
    // Setting IDs
    public int COMBAT_STYLE_SETTING = 43; // The widget ID for the combat style menu
    // Various inventory clicking patterns
    public int CLICK_ORDER1[] = {0, 1, 2, 3, 7, 6, 5, 4, 8, 9, 10, 11, 15, 14, 13, 12, 16, 17, 18, 19, 23, 22, 21, 20, 24, 25, 26, 27};
    public int CLICK_ORDER2[] = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27};
    public int CLICK_ORDER3[] = {0, 4, 1, 5, 2, 6, 3, 7, 11, 15, 10, 14, 9, 13, 8, 12, 16, 20, 17, 21, 18, 22, 19, 23, 27, 26, 25, 24};
    public int CLICK_ORDER4[] = {0, 4, 8, 12, 16, 20, 24, 25, 21, 17, 13, 9, 5, 1, 2, 6, 10, 14, 18, 22, 26, 3, 7, 11, 15, 19, 23, 27};
    public int CLICK_ORDER5[] = {0, 1, 4, 5, 8, 9, 12, 13, 16, 17, 20, 21, 24, 25, 2, 3, 6, 7, 10, 11, 14, 15, 18, 19, 22, 23, 26, 27};
    // F2P world list
    public int[] F2P_WORLDS = {1, 8, 16, 26, 35, 82, 83, 84, 93, 94, 117, 118, 124};
    private ArrayList<Integer> RECENT_WORLD_HOPS = new ArrayList<>();
    // Items that we should not drop
    public ArrayList<String> DONT_DROP = new ArrayList<>();
    public ArrayList<String> DONT_DROP_NAMES = new ArrayList<>();
    // Grand Exchange variables
    private static final String BASE_GE_URL = "http://services.runescape.com/m=itemdb_oldschool/api/catalogue/detail.json?item="; // Website for GE
    private static final int MILLION_GP = 1000000; // Used for converting 1m to 1000000
    private static final int THOUSAND_GP = 1000; // Used for converting 1k to 1000
    // Widget Coordinates
    public final Point STATS_WIDGET = new Point(577, 186); // Stats menu
    public final Point INVENTORY_WIDGET = new Point(643, 185); // Inventory menu
    public final Point LOGOUT_WIDGET = new Point(635, 475); // Logout menu
    public final Point LOGOUT_BUTTON_WIDGET = new Point(638, 432); // Logout button
    public final Point COMBAT_WIDGET = new Point(543, 186); // Combat style menu
    public final Point MAGIC_WIDGET = new Point(742, 186); // Magic menu
    public final Point ATTACK_STYLE_WIDGET = new Point(602, 273); // Attack combat mode
    public final Point STRENGTH_STYLE_WIDGET = new Point(688, 272); // Strength combat mode
    public final Point STRENGTH_OR_DEFENCE_STYLE_WIDGET = new Point(603, 326); // This can be defence or strength depending on weapon (combat mode)
    public final Point DEFENCE_STYLE_WIDGET = new Point(687, 326); // This is only Defence combat mode
    public final Point DEATH_CLOSE = new Point(262, 315); // This is the 'Never show this again' coordinates for the death screen
    public final Point DIALOGUE_CONTINUE = new Point(290, 445); // Middle of continue dialogue button
    public final Point OPTIONS_WIDGET = new Point(665, 472); // Options widget location
    public final Point CONTROLS_WIDGET = new Point(700, 215); // Controls widget location
    public final Point SHIFT_DROP_WIDGET = new Point(592, 300); // Shift drop control widget location
    public final Point COOKING_WIDGET = new Point(260, 430); // Cooking widget location
    public final int[] COOKING_WIDGET_CHILD = { 270, 5 };
    // EXP Popup variables
    private boolean showExpPopup = false; // If true, the EXP popup draws to the screen
    private long expPopupStart = 0L; // This is used to determine when the last EXP gain happened
    private int xpGained = 0; // This tracks the EXP gain to display
    private int lastExpPopupTotal = 0; // This tracks our last total exp so that we can detect when our new total exp changes
    private double expY = 160; // This is the Y coordinate of the EXP popup (by decrementing, we can make the popup float to the top of the screen)
    // Stat widget coordinates (in order of DB API listing for Skill array, for sake of convenience)
    public final Point[] STAT_WIDGET = {
            new Point(550, 210), // Attack
            new Point(550, 270), // Defence
            new Point(550, 240), // Strength
            new Point(612, 210), // Hits
            new Point(550, 304), // Ranged
            new Point(550, 336), // Prayer
            new Point(350, 370), // Magic
            new Point(367, 304), // Cooking
            new Point(676, 368), // Woodcut
            new Point(613, 369), // Fletching
            new Point(677, 273), // Fishing
            new Point(676, 336), // Firemaking
            new Point(614, 337), // Crafting
            new Point(677, 240), // Smithing
            new Point(677, 209), // Mining
            new Point(613, 271), // Herblore
            new Point(614, 240), // Agility
            new Point(614, 304), // Thieving
            new Point(614, 401), // Slayer
            new Point(676, 400), // Farming
            new Point(550, 400), // Runecrafting
            new Point(613, 432), // Hunter
            new Point(550, 432), // Construction
    };

    public Zen(AbstractScript script) {
        this(script, true);
    }

    // Creates the script-helper class Zen
    public Zen(AbstractScript script, boolean showGui) {
        LAG_START_TIME = System.currentTimeMillis();
        this.s = script;
        //this.MAX_RUNTIME_MINUTES = this.MAX_RUNTIME_MINUTES + r(0, 30);
        this.RUN_THRESHOLD = r(50, 99);
        this.STARTING_TOTAL_LEVELS = s.getSkills().getTotalLevel();
        Skill[] skills = Skill.values();
        for (Skill skill : skills) {
            START_LEVELS.put(skill, s.getSkills().getRealLevel(skill));
            START_EXP.put(skill, s.getSkills().getExperience(skill));
            STARTING_TOTAL_EXP += s.getSkills().getExperience(skill);
        }
        s.getMouse().getMouseSettings().setWordsPerMinute(rd(50.0, 100.0));
        this.MOUSE_SPEED = r(5, 6);
        s.getMouse().getMouseSettings().setSpeed(MOUSE_SPEED);
        s.log("[" + getScriptName() + "] Started.");
        // Setup anti-ban
        ban = new ZenAntiBan(this);

        if(showGui) {
            gui = new ZenGUI(this);
            gui.addPanel(s.getManifest().name(), "Script settings");
            gui.addPanel("Anti-Ban", "Anti-Ban settings");
            gui.addPanel("World Hopper", "World hop settings");
            setupUI();
        }
    }

    // Adds generic panel settings to script GUI
    private void setupUI() {
        // Add generic script settings
        gui.addBooleanInput(0, "Show EXP Popups", "exppopup", true);
        gui.addBooleanInput(0, "Keep Starting Items", "items", false);
        gui.addBooleanInput(0, "Shift Drop", "shift", "Use shift-drop?", true);
        gui.addStringInput(0, "Don't Drop List", "dontdrop", "Separate with commas - (eg. bronze sword,rune,hammer)", "");
        gui.addIntegerInput(1, "Max Runtime (mins)", "maxruntime", -1, -1, 9999);
        gui.addBooleanInput(1, "Debug", "debug", false);
        gui.addBooleanInput(1, "Use ChatBot", "chatbot", true);
        gui.addBooleanInput(1, "Only Reply to Username/Flags", "chatbotusername", true);
        gui.addStringInput(1, "Chat Flags (Separate by ,)", "chatflags", "");
        gui.addStringInput(1, "CleverBot API Key", "chatkey", "If you have purchasead a CleverBot API Key, enter it here", "");
        gui.addSliderInput(1, "ChatBot Limit (mins)", "chatbotrate", 20, 5, 100, 25, 5);
        gui.addSliderInput(1, "Anti-Ban Activity", "antibanrate", 50, 0, 100, 25, 5);
        gui.addBooleanInput(2, "World Hop", "worldhop", "Change worlds if another player is detected?", false);
        gui.addBooleanInput(2, "F2P", "f2p", "Hop to Free worlds?", true);
        gui.addBooleanInput(2, "P2P", "p2p", "Hop to Members worlds?", false);
        gui.addIntegerInput(2, "Player Distance", "pdistance", "Hop worlds if a player gets within X tiles of you", 6, 0, 50);
        gui.setTooltip("items", "Do not drop any items that start out in your inventory?");
        gui.setTooltip("maxruntime", "This stops the script after the specified time (-1 to disable)");
        gui.setTooltip("debug", "Print verbose script information to console?");
        gui.setTooltip("chatbot", "Use automatic chatbot replies?");
        gui.setTooltip("chatflags", "The chatbot will prioritize replying to any keywords you put here (optional)");
        gui.setTooltip("antibanrate", "This affects how frequently the anti-ban system performs actions (0 = disabled)");
        gui.setTooltip("chatbotrate", "This affects how frequently the ChatBot can reply (5 = no more than every 5 minutes)");
        gui.setTooltip("chatbotusername", "If selected, the ChatBot will only reply when your username or a given chat flag is mentioned");
    }

    // Shows the GUI and waits until input has been received
    public boolean showGUI() {
        boolean run = gui.show();

        // Setup main script variables
        if(run) {
            setMaxRuntime(gui.getInt("maxruntime"));
        }

        return run;
    }

    // Add any items that exist in our inventory to the Don't Drop list
    public void saveStartingItems() {
        if(gui.getBoolean("items")) {
            for(Item i : s.getInventory().all()) {
                if (i != null)
                    dontDrop(i.getID());
            }
        }
    }

    // Sets the start time for the script (called by GUI when it is closed)
    public void setStartTime() {
        this.START_TIME = System.currentTimeMillis();
    }

    // Adds the given item to the don't drop list
    public void dontDrop(int id) {
        if(!DONT_DROP.contains("" + id))
            DONT_DROP.add("" + id);
    }

    // Adds the given item to the don't drop list
    public void dontDrop(String name) {
        if(!DONT_DROP_NAMES.contains(name))
            DONT_DROP_NAMES.add(name);
    }

    // World hops if necessary
    public int checkForWorldHop() {
        if(!gui.getBoolean("worldhop"))
            return -1;

        // If there are players in the vicinity, hop worlds
        if(getBoolean("worldhop") && s.getPlayers().all().size() > 1) {
            for(Player p : s.getPlayers().all()) {
                if (!p.equals(s.getLocalPlayer()) && p.distance(s.getLocalPlayer()) < gui.getInt("pdistance") && !p.isMoving()) {
                    //print("Player detected too close - " + p.distance(getLocalPlayer()));
                    if(hopWorlds()) // TODO: Only do this if player lingers for longer than a few seconds?
                        return rh(3000, 6000);
                    break;
                }
            }
        }

        return -1;
    }

    // Returns the anti-ban system
    public ZenAntiBan getAntiBan() {
        return ban;
    }

    // Returns getBoolean from the GUI
    public boolean getBoolean(String key) {
        return gui.getBoolean(key);
    }

    // Returns getBoolean from the GUI
    public int getInt(String key) {
        return gui.getInt(key);
    }

    // Returns getBoolean from the GUI
    public String getString(String key) {
        return gui.getString(key);
    }

    // Set the max runtime of the script
    public void setMaxRuntime(int mins) {
        this.MAX_RUNTIME_MINUTES = mins;
    }

    // Returns the script this helper class is attached to
    public AbstractScript getScript() {
        return s;
    }

    // Continues the dialogue. If mouse is close to button, 75% of the time it will click it. Otherwise it will spacebar
    public boolean continueDialogue() {
        if(s.getDialogues().canContinue()) {
            if(getMouseDistance(DIALOGUE_CONTINUE) <= 50 && r(0, 100) < 75)
                s.getDialogues().clickContinue();
            else {
                if (r(1, 5) == 2)
                    s.getDialogues().continueDialogue();
                else
                    s.getDialogues().clickContinue();
            }

            sleep(10, 250);
            return true;
        }

        return false;
    }

    // Return how far the mouse is from a given point
    public double getMouseDistance(Point p) {
        Point mouse = s.getMouse().getPosition();
        return Math.sqrt(Math.pow(mouse.getX() - p.getX(), 2) + Math.pow(mouse.getY() - p.getY(), 2));
    }

    // Returns the Skill array index for the given Skill
    public int getSkillIndex(Skill skill) {
        Skill[] skills = Skill.values();
        for(int i = 0; i < skills.length; i++) {
            Skill s = skills[i];
            if(s.getName().equals(skill.getName()))
                return i;
        }

        return -1;
    }

    // Returns how many minutes the script has been running
    public int getMinsRunning() {
        return (int) (((System.currentTimeMillis() - START_TIME - PAUSE_TIME) / 1000) / 60);
    }

    // Returns the preset mouse speed
    public int getMouseSpeed() {
        return MOUSE_SPEED;
    }

    // Gets a random double
    public static double rd(double min, double max) {
        return (Math.random() * ((max - min) + 1)) + min;
    }

    // Get levels gained for given skill
    public int getLevelsGained(Skill skill) {
        return s.getSkills().getRealLevel(skill) - START_LEVELS.get(skill);
    }

    // Returns the current 'real' level for the given skill
    public int getLevel(Skill skill) { return s.getSkills().getRealLevel(skill); }

    // Get EXP gained for given skill
    public int getExpGained(Skill skill) {
        return s.getSkills().getExperience(skill) - START_EXP.get(skill);
    }

    // Returns total EXP gained
    public int getExpGained() {
        return getTotalExp() - STARTING_TOTAL_EXP;
    }

    // Returns total EXP gained
    public int getTotalExp() {
        Skill[] skills = Skill.values();
        int exp = 0;
        for (Skill skill : skills)
            exp += s.getSkills().getExperience(skill);

        return exp;
    }

    // Get total levels gained
    public int getLevelsGained() {
        return s.getSkills().getTotalLevel() - STARTING_TOTAL_LEVELS;
    }

    // Sets the current displayed status and prints it to the debug console
    public void setStatus(String status) {
        if (!status.equals(STATUS)) {
            STATUS = status;
            ban.setStatus("");
            debug(status);
        }
    }

    // Returns the max runtime for this script
    public int getMaxRuntime() {
        return MAX_RUNTIME_MINUTES;
    }

    // Prints only if debug is enabled
    public void debug(Object o) {
        if(gui.finished() && getBoolean("debug"))
            print(o);
    }

    // Returns the lag multiplier (increases as script runs longer)
    public double getLagMultiplier() {
        double minutesRunning = (double) ((System.currentTimeMillis() - LAG_START_TIME) / 60000);
        double percent = (minutesRunning / (double)(MAX_RUNTIME_MINUTES == -1 ? 480 : MAX_RUNTIME_MINUTES));

        if(percent >= 0.5) {
            // Slow the mouse down a bit
            if(!CHANGED_MOUSE_LAG) {
                MOUSE_SPEED = r(3, 4);
                s.getMouse().getMouseSettings().setSpeed(MOUSE_SPEED);
                CHANGED_MOUSE_LAG = true;
            }
        }

        if(percent >= 0.8) {
            // Slow the mouse down a bit more
            if(!CHANGED_MOUSE_LAG2) {
                MOUSE_SPEED = r(2, 3);
                s.getMouse().getMouseSettings().setSpeed(MOUSE_SPEED);
                CHANGED_MOUSE_LAG2 = true;
            }
        }

        if(percent > 1.0)
            percent = 1.0D;

        return percent;
    }

    // Returns true if we should break a loop to free the mouse
    public boolean freeMouse() {
        return s.isPaused() || s.getRandomManager().isSolving() || Instance.getInstance().isMouseInputEnabled();
    }

    // Walks to the given area in a human-like manner
    public boolean walk(Area area) {
        return walk(area, true);
    }

    // Walks to the given area in a human-like manner
    public boolean walk(Area area, boolean walkOnScreen) {
        Tile t = area.getRandomTile();
        long lastIdle = System.currentTimeMillis();
        // Use a loop to make using this method in scripts easier
        while(!area.contains(s.getLocalPlayer())) {
            if(freeMouse() || !clientLoaded() || s.getLocalPlayer().getZ() != t.getZ() || s.getLocalPlayer().distance(t) >= 10000)
                return false;

            STATUS = "Walking (" + t.getX() + "," + t.getY() + ") - " + (int)(s.getLocalPlayer().distance(t));
            boolean walkedOnScreen = false;
            if(!s.getMap().canReach(t))
                t = area.getRandomTile();
            if(t == null || s.getWalking() == null)
                continue;

            try {
                if (s.getLocalPlayer().distance(t) <= 10 && s.getMap().canReach(t) && !s.getLocalPlayer().isMoving() && walkOnScreen) {
                    s.getWalking().walkOnScreen(t);
                    walkedOnScreen = true;
                } else if (s.getMap().canReach(t) && walkOnScreen)
                    s.getWalking().walkExact(t);
                else
                    s.getWalking().walk(t);
            } catch(Exception e) {
                s.getWalking().walk(t);
            }

            int r = r(1, 10 - getLagMultiplier() >= 0.25 ? 3 : 0);
            // Randomly idle after clicking the next tile (becomes more likely to happen the longer the script runs)
            if((s.getWalking().isRunEnabled() ? r == 1 : r <= 3) && System.currentTimeMillis() - lastIdle >= 15000 && !walkedOnScreen) {
                int wait = rh(5000, 15000);
                setStatus("#Idling for " + (wait / 1000) + " secs");
                s.getMouse().moveMouseOutsideScreen();
                s.sleep(wait);
                lastIdle = System.currentTimeMillis();
                continue;
            }

            sleep(250, 1000);
            s.sleepUntil(() -> s.getWalking().shouldWalk(r(3, 6)), rh(10000, 15000));
        }

        return true;
    }

    // Walks to the given Area
    public boolean walk(Area area, Filter<GameObject> filter) {
        Tile t = area.getRandomTile();
        // Use a loop to make using this method in scripts easier
        while(!area.contains(s.getLocalPlayer())) {
            if(freeMouse() || !clientLoaded() || s.getLocalPlayer().getZ() != t.getZ() || s.getLocalPlayer().distance(t) >= 10000)
                return false;

            GameObject o = s.getGameObjects().closest(filter);
            if(o != null && s.getLocalPlayer().distance(o) <= 15 && s.getMap().canReach(o))
                return true;

            STATUS = "Walking (" + t.getX() + "," + t.getY() + ") - " + (int)(s.getLocalPlayer().distance(t));
            if(!s.getMap().canReach(t))
                t = area.getRandomTile();
            if(t == null || s.getWalking() == null)
                continue;
            if(s.getLocalPlayer().distance(t) <= 10 && s.getMap().canReach(t) && !s.getLocalPlayer().isMoving())
                s.getWalking().walkOnScreen(t);
            else
            if(s.getMap().canReach(t))
                s.getWalking().walkExact(t);
            else
                s.getWalking().walk(t);

            // Randomly idle after clicking the next tile
            if(r(1, 10) == 1) {
                int wait = rh(5000, 15000);
                setStatus("#Idling for " + (wait / 1000) + " secs");
                s.getMouse().moveMouseOutsideScreen();
                s.sleep(wait);
                continue;
            }

            sleep(1000, 2000);
            s.sleepUntil(() -> s.getWalking().shouldWalk(r(3, 6)), rh(10000, 15000));
        }

        return true;
    }

    // Chooses any of the given answers if they pop up in the dialogue menu
    public boolean answer(String... answers) {
        if(s.getDialogues().getOptions() != null) {
            int reply = getOption(answers);
            if (reply != -1) {
                if (r(1, 3) == 2)
                    s.getDialogues().clickOption(reply);
                else
                    s.getDialogues().chooseOption(reply);

                sleep(250, 500);
                return true;
            } else {
                // If we have been caught by a continuable dialogue, then continue it.
                if (continueDialogue()) {
                    sleep(2500, 3000);
                    return true;
                }

                print("Error - did not know how to continue NPC dialogue!");
            }
        }

        return false;
    }

    // Returns the current script status
    public String getStatus() {
        return STATUS;
    }

    // Returns our player's X coordinate
    public int getX() {
        return s.getLocalPlayer().getX();
    }

    // Returns our player's Y coordinate
    public int getY() {
        return s.getLocalPlayer().getY();
    }

    // This method opens the inventory
    public boolean openInventory() {
        if (s.getTabs().getOpen() != Tab.INVENTORY) {
            // Sometimes use hot keys, sometimes use mouse
            if (Calculations.random(1, 3) == 2)
                s.getTabs().open(Tab.INVENTORY);
            else {
                int x = (int) INVENTORY_WIDGET.getX() + r(0, 10);
                int y = (int) INVENTORY_WIDGET.getY() + r(0, 10);
                s.getMouse().move(new Point(x, y));
                s.getMouse().click();
            }

            sleep(50, 250);
        }

        return s.getTabs().getOpen() == Tab.INVENTORY;
    }

    // This method opens the options menu
    public boolean openOptions() {
        if (s.getTabs().getOpen() != Tab.OPTIONS) {
            // Sometimes use hot keys, sometimes use mouse
            if (Calculations.random(1, 3) == 2)
                s.getTabs().open(Tab.OPTIONS);
            else {
                int x = (int) OPTIONS_WIDGET.getX() + r(0, 10);
                int y = (int) OPTIONS_WIDGET.getY() + r(0, 10);
                s.getMouse().move(new Point(x, y));
                s.getMouse().click();
            }

            sleep(50, 250);
        }

        return s.getTabs().getOpen() == Tab.OPTIONS;
    }

    // This method opens the magic menu
    public boolean openMagic() {
        if (s.getTabs().getOpen() != Tab.MAGIC) {
            // Sometimes use hot keys, sometimes use mouse
            if (Calculations.random(1, 3) == 2)
                s.getTabs().open(Tab.MAGIC);
            else {
                int x = (int) MAGIC_WIDGET.getX() + r(0, 10);
                int y = (int) MAGIC_WIDGET.getY() + r(0, 10);
                s.getMouse().move(new Point(x, y));
                s.getMouse().click();
            }

            sleep(50, 250);
        }

        return s.getTabs().getOpen() == Tab.MAGIC;
    }

    // Gets the given answer for the given set of replies if the NPC Dialogue is open
    public int getOption(String... replies) {
        String[] options = s.getDialogues().getOptions();
        if (options == null)
            return -1;

        int reply = -1;
        for (int i = 0; i < options.length; i++) {
            String s = options[i].toLowerCase();
            for (String replyStr : replies) {
                if (s.contains(replyStr)) {
                    return i + 1;
                }
            }
        }

        return -1;
    }

    // This method opens the stats menu
    public boolean openStats() {
        if (s.getTabs().getOpen() != Tab.STATS) {
            // Sometimes use hot keys, sometimes use mouse
            if (Calculations.random(1, 3) == 2)
                s.getSkills().open();
            else {
                int x = (int) STATS_WIDGET.getX() + r(0, 10);
                int y = (int) STATS_WIDGET.getY() + r(0, 10);
                s.getMouse().move(new Point(x, y));
                sleep(0, 50);
                s.getMouse().click();
            }

            sleep(50, 250);
        }

        return s.getTabs().getOpen() == Tab.STATS;
    }

    // Opens the  combat menu then waits for a second
    public boolean openCombat() {
        if (s.getTabs().getOpen() != Tab.COMBAT) {
            // Sometimes use hot keys, sometimes use mouse
            if (Calculations.random(1, 3) == 2)
                s.getTabs().open(Tab.COMBAT);
            else {
                int x = (int) COMBAT_WIDGET.getX() + Calculations.random(0, 10);
                int y = (int) COMBAT_WIDGET.getY() + Calculations.random(0, 10);
                s.getMouse().move(new Point(x, y));
                sleep(0, 50);
                s.getMouse().click();
            }

            sleep(50, 250);
        }

        return s.getTabs().getOpen() == Tab.COMBAT;
    }

    // Opens the logout menu
    public boolean openLogout() {
        if (s.getTabs().getOpen() != Tab.LOGOUT) {
            if(r(1, 3) == 2) {
                s.getTabs().open(Tab.LOGOUT);
            } else {
                int x = (int) LOGOUT_WIDGET.getX() + Calculations.random(0, 10);
                int y = (int) LOGOUT_WIDGET.getY() + Calculations.random(0, 10);
                s.getMouse().move(new Point(x, y));
                sleep(0, 50);
                s.getMouse().click();
            }

            sleep(50, 250);
        } else
            return true;

        return s.getTabs().getOpen() == Tab.LOGOUT;
    }

    // Logs out of the game
    public void logout() {
        // Disable autologin otherwise this method is pointless
        s.getRandomManager().disableSolver(RandomEvent.LOGIN);

        // If we fail to open the logout menu, just afk til logout.
        if(!openLogout())
            return;

        int x = (int) LOGOUT_BUTTON_WIDGET.getX() + r(0, 25);
        int y = (int) LOGOUT_BUTTON_WIDGET.getY() + r(0, 5);
        s.getMouse().move(new Point(x, y));
        sleep(0, 50);
        s.getMouse().click();
    }

    public boolean interactItems(String action) {
        boolean interacted = false;
        if(action.equals("Drop") && gui.getBoolean("shift") && !quickDropActive())
            turnOnShiftDrop();

        if(!s.getTabs().isOpen(Tab.INVENTORY))
            openInventory();

        // If quick drop is active, hold shift
        if(quickDropActive() && action.equals("Drop"))
            pressShift();

        // Randomize click order for anti-patterning
        int[] CLICK_ORDER = getClickOrder();
        Inventory inventory = s.getInventory();
        for (int i = 0; i < CLICK_ORDER.length; i++) {
            Item item = inventory.getItemInSlot(CLICK_ORDER[i]);
            if (item != null) {
                if(action.equals("Drop") && itemsMatchID(item.getID(), DONT_DROP))
                    continue;
                if(action.equals("Drop") && itemsMatch(item.getName(), DONT_DROP_NAMES))
                    continue;
                if(action.equals("Drop") && nameContains(item.getName(), gui.getString("dontdrop").split(",")))
                    continue;
                if(!action.equals("Drop") && !item.hasAction(action))
                    continue;

                if(quickDropActive() && action.equals("Drop")) {
                    s.getMouse().click(item.getDestination());
                } else
                    s.getInventory().slotInteract(CLICK_ORDER[i], r(0, 100) < 1 && action.equals("Drop") ? "Examine" : action);

                interacted = true;
                final int slot = CLICK_ORDER[i];
                if (i < CLICK_ORDER.length - 1) {
                    // Declare inner loop, cycle through items and find the next item that has our specified Action
                    INNER_LOOP:
                    for(int j = i + 1; j < CLICK_ORDER.length; j++) {
                        Item nextItem = inventory.getItemInSlot(CLICK_ORDER[j]);
                        // If the item is found with the given Action, then move the mouse over it in anticipation of the next click (like a human would)
                        if (nextItem != null) {
                            if(action.equals("Drop") && itemsMatchID(nextItem.getID(), DONT_DROP))
                                continue INNER_LOOP;
                            if(action.equals("Drop") && itemsMatch(nextItem.getName(), DONT_DROP_NAMES))
                                continue INNER_LOOP;
                            if(action.equals("Drop") && nameContains(nextItem.getName(), gui.getString("dontdrop").split(",")))
                                continue INNER_LOOP;
                            if(!action.equals("Drop") && !nextItem.hasAction(action))
                                continue INNER_LOOP;
                            // Disable mouse input while doing this action in a loop
                            Instance.getInstance().setMouseInputEnabled(false);
                            s.getMouse().move(nextItem.getDestination());
                            s.sleepUntil(() -> s.getInventory().getItemInSlot(slot) == null, r(2000, 3000));
                            // If we found a valid item to hover over, break this inner loop and resume the main outer loop.
                            break INNER_LOOP;
                        }
                    }
                }

                s.sleep(r(1, 10));

                // If we are paused or solving a random event, stop this loop
                if(freeMouse())
                    break;
            }
        }

        // If quick drop is active, release shift
        if(quickDropActive() && action.equals("Drop"))
            releaseShift();

        return interacted;
    }

    // Interacts with items in a randomized pattern (and randomly Examines sometimes)
    public boolean interactItemsOnly(String action, int... only) {
        if(action.equals("Drop") && gui.getBoolean("shift") && !quickDropActive())
            turnOnShiftDrop();

        // If quick drop is active, hold shift
        if(quickDropActive() && action.equals("Drop")) {
            pressShift();
        }

        if (!s.getTabs().isOpen(Tab.INVENTORY))
            openInventory();

        boolean interacted = false;
        // Randomize click order for anti-patterning
        int[] CLICK_ORDER = getClickOrder();
        Inventory inventory = s.getInventory();
        for (int i = 0; i < CLICK_ORDER.length; i++) {
            Item item = inventory.getItemInSlot(CLICK_ORDER[i]);
            if (item != null) {
                if(!itemsMatch(item.getID(), only))
                    continue;
                if(action.equals("Drop") && itemsMatchID(item.getID(), DONT_DROP))
                    continue;
                if(action.equals("Drop") && itemsMatch(item.getName(), DONT_DROP_NAMES))
                    continue;
                if(!action.equals("Drop") && !item.hasAction(action))
                    continue;

                if(quickDropActive() && action.equals("Drop")) {
                    s.getMouse().click(item.getDestination());
                } else
                    s.getInventory().slotInteract(CLICK_ORDER[i], r(0, 100) < 1 && action.equals("Drop") ? "Examine" : action);

                interacted = true;
                final int slot = CLICK_ORDER[i];
                if (i < CLICK_ORDER.length - 1) {
                    // Declare inner loop, cycle through items and find the next item that has our specified Action
                    INNER_LOOP:
                    for(int j = i + 1; j < CLICK_ORDER.length; j++) {
                        Item nextItem = inventory.getItemInSlot(CLICK_ORDER[j]);
                        // If the item is found with the given Action, then move the mouse over it in anticipation of the next click (like a human would)
                        if (nextItem != null) {
                            if(!itemsMatch(nextItem.getID(), only))
                                continue INNER_LOOP;
                            if(action.equals("Drop") && itemsMatchID(nextItem.getID(), DONT_DROP))
                                continue INNER_LOOP;
                            if(action.equals("Drop") && itemsMatch(nextItem.getName(), DONT_DROP_NAMES))
                                continue INNER_LOOP;
                            if(!action.equals("Drop") && !nextItem.hasAction(action))
                                continue INNER_LOOP;
                            // Disable mouse input while doing this action in a loop
                            Instance.getInstance().setMouseInputEnabled(false);
                            s.getMouse().move(nextItem.getDestination());
                            s.sleepUntil(() -> s.getInventory().getItemInSlot(slot) == null, r(2000, 3000));
                            // If we found a valid item to hover over, break this inner loop and resume the main outer loop.
                            break INNER_LOOP;
                        }
                    }
                }

                s.sleep(r(1, 10));

                // If we are paused or solving a random event, stop this loop
                if(freeMouse())
                    break;
            }
        }

        // If quick drop is active, release shift
        if(quickDropActive() && action.equals("Drop")) {
            releaseShift();
        }

        return interacted;
    }

    // Interacts with items in a randomized pattern (and randomly Examines sometimes)
    public boolean interactItemsExcept(String action, int... except) {
        if(action.equals("Drop") && gui.getBoolean("shift") && !quickDropActive())
            turnOnShiftDrop();

        // If quick drop is active, hold shift
        if(quickDropActive() && action.equals("Drop")) {
            pressShift();
        }

        if (!s.getTabs().isOpen(Tab.INVENTORY))
            openInventory();

        boolean interacted = false;
        // Randomize click order for anti-patterning
        int[] CLICK_ORDER = getClickOrder();
        Inventory inventory = s.getInventory();
        for (int i = 0; i < CLICK_ORDER.length; i++) {
            Item item = inventory.getItemInSlot(CLICK_ORDER[i]);
            if (item != null) {
                if(itemsMatch(item.getID(), except))
                    continue;
                if(action.equals("Drop") && itemsMatchID(item.getID(), DONT_DROP))
                    continue;
                if(action.equals("Drop") && itemsMatch(item.getName(), DONT_DROP_NAMES))
                    continue;
                if(action.equals("Drop") && nameContains(item.getName(), gui.getString("dontdrop").split(",")))
                    continue;
                if(!action.equals("Drop") && !item.hasAction(action))
                    continue;

                if(quickDropActive() && action.equals("Drop")) {
                    s.getMouse().click(item.getDestination());
                } else
                    s.getInventory().slotInteract(CLICK_ORDER[i], r(0, 100) < 1 && action.equals("Drop") ? "Examine" : action);

                interacted = true;
                final int slot = CLICK_ORDER[i];
                if (i < CLICK_ORDER.length - 1) {
                    // Declare inner loop, cycle through items and find the next item that has our specified Action
                    INNER_LOOP:
                    for(int j = i + 1; j < CLICK_ORDER.length; j++) {
                        Item nextItem = inventory.getItemInSlot(CLICK_ORDER[j]);
                        // If the item is found with the given Action, then move the mouse over it in anticipation of the next click (like a human would)
                        if (nextItem != null) {
                            if(itemsMatch(item.getID(), except))
                                continue;
                            if(action.equals("Drop") && itemsMatchID(nextItem.getID(), DONT_DROP))
                                continue INNER_LOOP;
                            if(action.equals("Drop") && itemsMatch(nextItem.getName(), DONT_DROP_NAMES))
                                continue INNER_LOOP;
                            if(action.equals("Drop") && nameContains(nextItem.getName(), gui.getString("dontdrop").split(",")))
                                continue INNER_LOOP;
                            if(!action.equals("Drop") && !nextItem.hasAction(action))
                                continue INNER_LOOP;
                            // Disable mouse input while doing this action in a loop
                            Instance.getInstance().setMouseInputEnabled(false);
                            s.getMouse().move(nextItem.getDestination());
                            s.sleepUntil(() -> s.getInventory().getItemInSlot(slot) == null, r(2000, 3000));
                            // If we found a valid item to hover over, break this inner loop and resume the main outer loop.
                            break INNER_LOOP;
                        }
                    }
                }

                s.sleep(r(1, 10));

                // If we are paused or solving a random event, stop this loop
                if(freeMouse())
                    break;
            }
        }

        // If quick drop is active, release shift
        if(quickDropActive() && action.equals("Drop")) {
            releaseShift();
        }

        return interacted;
    }

    public void pressShift() {
        Instance instance = s.getClient().getInstance();
        Canvas canvas = instance.getCanvas();
        instance.setKeyboardInputEnabled(true);
        canvas.dispatchEvent(new KeyEvent(canvas, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_SHIFT, KeyEvent.CHAR_UNDEFINED));
    }

    public void releaseShift() {
        Instance instance = s.getClient().getInstance();
        Canvas canvas = instance.getCanvas();
        canvas.dispatchEvent(new KeyEvent(canvas, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_SHIFT, KeyEvent.CHAR_UNDEFINED));
        instance.setKeyboardInputEnabled(false);
    }

    // Returns a random inventory slot clicking order
    public int[] getClickOrder() {
        // Randomize click order for anti-patterning
        int random = Calculations.random(1, 5);
        int[] CLICK_ORDER = new int[CLICK_ORDER1.length];
        switch (random) {
            case 1:
                CLICK_ORDER = CLICK_ORDER1;
                break;
            case 2:
                CLICK_ORDER = CLICK_ORDER2;
                break;
            case 3:
                CLICK_ORDER = CLICK_ORDER3;
                break;
            case 4:
                CLICK_ORDER = CLICK_ORDER4;
                break;
            case 5:
                CLICK_ORDER = CLICK_ORDER5;
                break;
        }

        // Randomly flip the array order sometimes to reverse the pattern
        if (Calculations.random(1, 3) == 2) {
            int[] flippedArray = new int[CLICK_ORDER.length];
            int x = 0;
            for (int i = flippedArray.length - 1; i > 0; i--)
                flippedArray[i] = CLICK_ORDER[x++];
            CLICK_ORDER = flippedArray;
        }

        return CLICK_ORDER;
    }

    // Returns whether or not our inventory contains one of the given IDs
    public boolean hasOneOfTheseItems(int... ids) {
        for(Item i : s.getInventory().all()) {
            for(int id : ids) {
                if (i != null && i.getID() == id)
                    return true;
            }
        }

        return false;
    }

    // Returns whether or not the given item matches any of the given ids
    public boolean itemsMatch(int id, int... ids) {
        if(id == -1)
            return false;

        for (int i : ids) {
            if(i == -1)
                return false;
            if (id == i)
                return true;
        }

        return false;
    }

    // Returns whether or not the given item matches any of the given ids
    public boolean itemsMatchID(int id, ArrayList<String> ids) {
        for (String  s : ids) {
            int i = Integer.parseInt(s);
            if (id == i)
                return true;
        }

        return false;
    }

    // Returns whether or not the given item matches any of the given ids (ArrayList version)
    public boolean itemsMatch(int id, ArrayList<Integer> ids) {
        for (int i : ids) {
            if (id == i)
                return true;
        }

        return false;
    }

    // Returns whether or not the given item matches any of the given ids (ArrayList version)
    public boolean itemsMatch(String name, ArrayList<String> names) {
        for (String s : names) {
            if (!s.equals("") && name.toLowerCase().contains(s.toLowerCase()))
                return true;
        }

        return false;
    }

    // Returns whether or not the given string matches any of the given names
    public boolean nameMatches(String name, String... compare) {
        for(String s : compare) {
            if(s.equalsIgnoreCase(name))
                return true;
        }

        return false;
    }

    // Returns whether or not the given string contains any of the given names
    public boolean nameContains(String name, String... compare) {
        for(String s : compare) {
            if(!s.equals("") && s.toLowerCase().contains(name.toLowerCase()))
                return true;
        }

        return false;
    }

    // Returns a random item slot for the given id
    public int getRandomSlot(int id) {
        int slot = -1;
        if (s.getInventory().count(id) == 0)
            return slot;

        long start = System.currentTimeMillis();
        while (true) {
            if (System.currentTimeMillis() - start >= 500) {
                print("ERROR: getRandomSlot(" + id + ") timed out!");
                break;
            }
            int randomSlot = r(0, 27);
            Item i = s.getInventory().getItemInSlot(randomSlot);
            if (i != null && i.getID() == id)
                return randomSlot;
        }

        return slot;
    }

    // Returns a random item slot for the given ids
    public int getRandomSlot(int... ids) {
        int slot = -1;
        long start = System.currentTimeMillis();
        while (true) {
            if (System.currentTimeMillis() - start >= 500) {
                String idstr = "";
                for (int id : ids)
                    idstr = idstr + id + ",";
                print("ERROR: getRandomSlot(" + idstr + ") timed out!");
                break;
            }

            int randomSlot = r(0, 27);
            Item i = s.getInventory().getItemInSlot(randomSlot);
            for (int id : ids) {
                if (i != null && i.getID() == id)
                    return randomSlot;
            }
        }

        return slot;
    }
    // Returns a random item slot that is not on our don't drop list
    public int getRandomJunkSlot() {
        int slot = -1;
        long start = System.currentTimeMillis();
        while (true) {
            if (System.currentTimeMillis() - start >= 500) {
                print("ERROR: getRandomJunkSlot() timed out!");
                break;
            }

            int randomSlot = r(0, 27);
            Item i = s.getInventory().getItemInSlot(randomSlot);
            if(itemsMatchID(i.getID(), DONT_DROP))
                continue;
            if(itemsMatch(i.getName(), DONT_DROP_NAMES))
                continue;
            if(nameContains(i.getName(), gui.getString("dontdrop").split(",")))
                continue;

            return randomSlot;
        }

        return slot;
    }

    // Returns a random item slot for the given id
    public int getRandomSlotAction(String action) {
        int slot = -1;

        long start = System.currentTimeMillis();
        while (true) {
            if (System.currentTimeMillis() - start >= 500) {
                //print("ERROR: getRandomSlot(" + id + ") timed out!");
                break;
            }
            int randomSlot = r(0, 27);
            Item i = s.getInventory().getItemInSlot(randomSlot);
            if (i != null && i.hasAction(action))
                return randomSlot;
        }

        return slot;
    }

    // Returns the inventory count of the given item ids
    public int countInventory(int... ids) {
        int count = 0;
        for (Item i : s.getInventory().all()) {
            for (int id : ids) {
                if (i != null && i.getID() == id)
                    count++;
            }
        }

        return count;
    }

    // Returns how many items in the player's inventory contain the given word
    public int countInventory(String phrase) {
        int count = 0;
        for (Item i : s.getInventory().all()) {
            if (i != null && i.getName().toLowerCase().contains(phrase.toLowerCase()))
                count++;
        }

        return count;
    }

    // Returns true or false if the given item phrase is found
    public boolean hasItem(String phrase) {
        return countInventory(phrase) > 0;
    }

    // Returns true if the player has the given item with the given action
    public boolean hasItemWithAction(String action) {
        for(Item i : s.getInventory().all()) {
            if(i != null && i.hasAction(action))
                return true;
        }

        return false;
    }

    // Returns true or false if the given item IDs are found

    // This method turns Run on if our energy is above or equal to the given threshold
    public boolean turnOnRun() {
        if (!s.getWalking().isRunEnabled() && s.getWalking().getRunEnergy() >= r(RUN_THRESHOLD, 100)) {
            setStatus("#Turning run on");
            s.getWalking().toggleRun();
            RUN_THRESHOLD = r(5, 99);
            return true;
        }

        return false;
    }

    // Turns on shift drop if it is not already on
    public boolean turnOnShiftDrop() {
        if(openOptions()) {
            s.getMouse().move(new Point((int)CONTROLS_WIDGET.getX() + r(0, 20), (int)CONTROLS_WIDGET.getY() + r(0, 25)));
            sleep(0, 10);
            s.getMouse().click();
            sleep(30, 150);
            s.getMouse().move(new Point((int)SHIFT_DROP_WIDGET.getX() + r(0, 20), (int)SHIFT_DROP_WIDGET.getY() + r(0, 25)));
            sleep(0, 10);
            s.getMouse().click();
            sleep(250, 500);
            return quickDropActive();
        }

        return false;
    }

    // Returns whether or not shift drop is active
    public boolean quickDropActive() {
        return ((s.getPlayerSettings().getConfig(1055) >> 17) & 0x1) == 1 && gui.getBoolean("shift");
    }

    // This method sets the fighting style to whatever mode is desired
    public boolean setCombatStyle(int combatStyle) {
        int selectedStyle = s.getPlayerSettings().getConfig(COMBAT_STYLE_SETTING);
        // Check if we need to change our combat style
        if (selectedStyle != combatStyle) {
            if (s.getTabs().getOpen() != Tab.COMBAT)
                openCombat();

            Point styleButton = null;
            if (combatStyle == 0) { // Attack
                styleButton = ATTACK_STYLE_WIDGET;
            } else if (combatStyle == 1) { // Strength
                styleButton = STRENGTH_STYLE_WIDGET;
            } else if (combatStyle == 3) { // Defence
                if (s.getEquipment().isSlotEmpty(EquipmentSlot.WEAPON.getSlot()))
                    styleButton = STRENGTH_OR_DEFENCE_STYLE_WIDGET;
                else
                    styleButton = DEFENCE_STYLE_WIDGET;
            }

            int x = (int) styleButton.getX() + r(0, 10);
            int y = (int) styleButton.getY() + r(0, 10);
            s.getMouse().move(new Point(x, y));
            s.getMouse().click();
            sleep(250, 550);
        }

        return s.getPlayerSettings().getConfig(COMBAT_STYLE_SETTING) == combatStyle;
    }

    // Returns a random number
    public int r(int x, int y) {
        return Calculations.random(x, y+1);
    }

    // Returns a random number with the human lag element added to the minimum wait time
    public int rh(int x, int y) {
        //return r(x + getHumanLag(), y + RAND + getHumanLag());
        return r(x + (int)(x*getLagMultiplier()), y + (int)(y*getLagMultiplier()));
    }

    // Gets the item ID for the given name
    public int getItemID(String name) {
        for(Item item : s.getInventory().all(i -> i != null && i.getName().toLowerCase().contains(name.toLowerCase())))
            return item.getID();

        return -1;
    }

    // Opens the shop with the default general store names (shop keeper, shop assistant)
    public boolean openShop() {
        return openShop("shop keeper", "shop assistant");
    }

    // Opens the shop with any of the given NPC names
    public boolean openShop(String... names) {
        for (NPC n : s.getNpcs().all(n -> n != null && n.hasAction("Trade") && s.getMap().canReach(n))) {
            for (String name : names) {
                if (n.getName().toLowerCase().contains(name)) {
                    // Open shop
                    n.interact("Trade");
                    sleep(250, 1000);
                    s.sleepUntil(() -> s.getShop().isOpen(), rh(5000, 10000));
                    return true;
                }
            }
        }

        return false;
    }

    // Returns a random F2P world
    public int getRandomWorldF2P() {
        int world = s.getWorlds().getRandomWorld(w -> w != null && !w.isMembers() && !w.isDeadmanMode() && !w.isHighRisk() && !w.isPVP() && s.getSkills().getTotalLevel() >= w.getMinimumLevel()).getID();
        while(RECENT_WORLD_HOPS.contains(world))
            world = s.getWorlds().getRandomWorld(w -> w != null && !w.isMembers() && !w.isDeadmanMode() && !w.isHighRisk() && !w.isPVP() && s.getSkills().getTotalLevel() >= w.getMinimumLevel()).getID();

        return world;
    }

    // Returns a random P2P world
    public int getRandomWorldP2P() {
        int world = s.getWorlds().getRandomWorld(w -> w != null && w.isMembers() && !w.isDeadmanMode() && !w.isHighRisk() && !w.isPVP() && s.getSkills().getTotalLevel() >= w.getMinimumLevel()).getID();
        while(RECENT_WORLD_HOPS.contains(world))
            world = s.getWorlds().getRandomWorld(w -> w != null && w.isMembers() && !w.isDeadmanMode() && !w.isHighRisk() && !w.isPVP() && s.getSkills().getTotalLevel() >= w.getMinimumLevel()).getID();

        return world;
    }

    // Returns a random world
    public int getRandomWorld() {
        int world = s.getWorlds().getRandomWorld(w -> w != null && !w.isHighRisk() && !w.isDeadmanMode() && s.getSkills().getTotalLevel() >= w.getMinimumLevel() && !w.isPVP()).getID();
        while(RECENT_WORLD_HOPS.contains(world))
            world = s.getWorlds().getRandomWorld(w -> w != null && !w.isHighRisk() && !w.isDeadmanMode() && s.getSkills().getTotalLevel() >= w.getMinimumLevel() && !w.isPVP()).getID();

        return world;
    }

    // Hops to a random F2P world
    public boolean hopWorlds() {
        long now = System.currentTimeMillis();
        if(now - LAST_WORLD_HOP <= 300000 && HOPS_PAST_5_MINUTES >= 10) {
            print("Hopped too many times - waiting for a few minutes");
            logout();
            s.getRandomManager().disableSolver(RandomEvent.LOGIN);
            sleep(60000, 120000);
            s.getRandomManager().enableSolver(RandomEvent.LOGIN);
            HOPS_PAST_5_MINUTES = 0;
            return false;
        }

        WORLD_HOPS++;
        boolean f2p = gui.getBoolean("f2p");
        boolean p2p = gui.getBoolean("p2p");
        int world;
        if(f2p && !p2p)
            world = getRandomWorldF2P();
        else
        if(!f2p && p2p)
            world = getRandomWorldP2P();
        else
            world = getRandomWorld();

        int oldWorld = s.getClient().getCurrentWorld();
        if(s.getWorldHopper().hopWorld(world)) {
            sleep(0, 50);
            s.getMouse().moveMouseOutsideScreen();
            sleep(4000, 5000);
            s.sleepUntil(() -> s.getLocalPlayer().exists() && s.getClient().isLoggedIn() && s.getClient().getCurrentWorld() != oldWorld, r(5000, 10000));
            HOPS_PAST_5_MINUTES++;
            RECENT_WORLD_HOPS.add(world);
            if(now - LAST_WORLD_HOP >= 300000) {
                LAST_WORLD_HOP = System.currentTimeMillis();
                HOPS_PAST_5_MINUTES = 0;
                RECENT_WORLD_HOPS.clear();
            }
            return true;
        } else
            return false;
    }

    // Takes the given ground item, custom method to eliminate bug where bot will continually open the menu even after taking the item
    public boolean takeGroundItem(GroundItem i) {
        // Move mouse over the item
        s.getMouse().move(i);
        Menu menu = new Menu(s.getClient());

        // 75% of the time, just left-click the item
        if(r(0, 100) < 75 && menu.getDefaultAction().equals("Take")) {
            s.getMouse().click();
            s.sleepUntil(() -> !i.exists() || s.getLocalPlayer().distance(i) <= 1, r(1000, 2000));
            return true;
        }

        // Otherwise open right-click menu and find Take option for bones
        s.sleep(r(1, 100));
        menu.open();
        if(menu.contains("Take", i)) {
            menu.clickAction("Take", i);
            s.sleepUntil(() -> !i.exists() || s.getLocalPlayer().distance(i) <= 1, r(3000, 6000));
            return true;
        } else
            menu.close();

        return false;
    }

    // This returns whether or not we have exceeded our max runtime
    public boolean maxRuntime() {
        return MAX_RUNTIME_MINUTES != -1 && getMinsRunning() > MAX_RUNTIME_MINUTES;
    }

    // If client isn't loaded, we aren't logged in, we just hopped worlds or the user has not closed the GUI, wait.
    public void waitTilReady() {
        long t = System.currentTimeMillis();
        while(!clientLoaded()) {
            sleep(3000, 6000);
            if(gui.finished() && System.currentTimeMillis() - t >= 30000) {
                print("Error: waitTilReady() timed out!");
                break;
            }
        }
    }

    // This sleeps for the given time in MS between the given min and max (using the RandomHuman modifier)
    public void sleep(int min, int max) {
        s.sleep(rh(min, max));
    }

    // Returns whether or not the client is loaded and logged in
    public boolean clientLoaded() {
        return s.getLocalPlayer() != null && s.getClient().isLoggedIn() && System.currentTimeMillis() - LAST_WORLD_HOP >= 5000 && s.getClient().getGameState() == GameState.LOGGED_IN && gui.finished();
    }

    // Prints the given Object to the console
    public void print(Object o) {
        String tag = o.toString().contains("[") ? "" : "[" + getScriptName() + "] ";
        s.log(tag + o.toString());
    }

    // Returns the start time for this script
    public long getStartTime() {
        return START_TIME;
    }

    // Returns the script title
    public String getScriptName() {
        return s.getManifest().name();
    }

    // Displays an error message to the user
    public void error(String msg) {
        print("Error: " + msg);
    }

    // Returns how many times we have hopped worlds
    public int getWorldHops() {
        return WORLD_HOPS;
    }

    // Returns the script's current CPU load every 5 seconds (to avoid unnecessary extra load)
    public double getCPU() {
        if(System.currentTimeMillis() - LAST_CPU_CHECK <= 5000)
            return LAST_CPU_LOAD;

        try {
            LAST_CPU_CHECK = System.currentTimeMillis();
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName name = ObjectName.getInstance("java.lang:type=OperatingSystem");
            AttributeList list = mbs.getAttributes(name, new String[]{"ProcessCpuLoad"});

            if (list.isEmpty())
                return -1;

            Attribute att = (Attribute) list.get(0);
            Double value = (Double) att.getValue();

            // Usually takes a couple of seconds before we get real values
            if (value == -1.0)
                return -1;

            // Returns a percentage value with 1 decimal point precision
            return LAST_CPU_LOAD = ((int) (value * 1000) / 10.0);
        } catch(Exception e) {
            return -1;
        }
    }

    // Returns the current runtime in a String format
    public String getRuntime() {
        long runtime = System.currentTimeMillis() - START_TIME - PAUSE_TIME;
        return String.format("%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes(runtime),
                TimeUnit.MILLISECONDS.toSeconds(runtime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(runtime))
        );
    }

    // Draws script title + generic feedback info to the screen about anti-ban & script performance
    public int drawInfo(Graphics g) {
        int x = GUI_X;
        int y = GUI_Y;
        // Paint information to the screen
        drawTitle(g, x, y += 14);
        drawString(g, "Runtime: ", s.isPaused() ? "#Paused" : (START_TIME == 0L ? "#Not started yet" : getRuntime()), x, y += 14);
        drawString(g, "Status: ", ban.getStatus().equals("") ? getStatus() : "#" + ban.getStatus(), x, y += 14);
        drawString(g, "Levels Gained: ", getLevelsGained(), x, y += 14);
        drawString(g, "EXP Gained: ", format(getExpGained()) + "xp", x, y += 14);
        double hrsRunning = (double)(getMinsRunning() <= 0 ? 1 : getMinsRunning()) / 60D;
        if(hrsRunning >= 1)
            drawString(g, "EXP / Hour: ", format((int)((double)getExpGained() / hrsRunning)) + "xp", x, y += 14);

        int a = s.getLocalPlayer().getAnimation();
        // TODO: ADD MORE SKILLS
        if(s.getLocalPlayer().isInCombat())
            a = -420;

        // Determine which skill to track
        switch(a) {
            case 879: // Woodcut
                EXP_TIL_LVL = format(s.getSkills().getExperienceToLevel(Skill.WOODCUTTING));
                break;
            case 733: // Firemaking
                EXP_TIL_LVL = format(s.getSkills().getExperienceToLevel(Skill.FIREMAKING));
                break;
            case 897: // Cooking
            case 896:
                EXP_TIL_LVL = format(s.getSkills().getExperienceToLevel(Skill.COOKING));
                break;
            case 621: // Fishing
            case 619:
                EXP_TIL_LVL = format(s.getSkills().getExperienceToLevel(Skill.FISHING));
                break;
            case -420: // In combat
                int selectedStyle = s.getPlayerSettings().getConfig(COMBAT_STYLE_SETTING);
                if(selectedStyle == 0)
                    EXP_TIL_LVL = format(s.getSkills().getExperienceToLevel(Skill.ATTACK));
                else
                if(selectedStyle == 1)
                    EXP_TIL_LVL = format(s.getSkills().getExperienceToLevel(Skill.STRENGTH));
                else
                    EXP_TIL_LVL = format(s.getSkills().getExperienceToLevel(Skill.DEFENCE));
        }

        if(EXP_TIL_LVL != null)
            drawString(g, "EXP > Level: ", EXP_TIL_LVL + "xp", x, y += 14);
        int inventoryPercent = (int)(((double)s.getInventory().fullSlotCount() / 28D) * 100D);
        if(s.getInventory().fullSlotCount() == 27)
            inventoryPercent = 99;
        drawString(g, "Inventory: ", s.getInventory().fullSlotCount() + " (" + inventoryPercent + "%)", x, y += 14);
        if(gui.getBoolean("worldhop")) {
            drawString(g, "World Hops: ", WORLD_HOPS, x, y += 14);
            drawString(g, "World: ", s.getClient().getCurrentWorld(), x, y += 14);
            drawString(g, "Nearby Players: ", (s.getPlayers().all().size() > 1 ? "#" : "") + (s.getPlayers().all().size() - 1), x, y += 14);
        }
        if(gui.getBoolean("chatbot"))
            drawString(g, "Chatbot Replies: ", ban.getChatBotReplyCount(), x, y += 14);

        // Draw icon
        if(icon == null)
            icon = gui.getScaledImage(gui.getIcon(), 25, 25);
        else
        if(!s.getDialogues().inDialogue())
            g.drawImage(icon, IMAGE_X, IMAGE_Y, null);

        // Display EXP popups if they are enabled
        if(gui.getBoolean("exppopup")) {
            if(lastExpPopupTotal == 0)
                lastExpPopupTotal = getTotalExp();

            // Show EXP Popup
            if (showExpPopup) {
                // If it has been less than 3 seconds, display the popup
                if (System.currentTimeMillis() - expPopupStart <= 3000 && xpGained != 0) {
                    String gained = "+" + xpGained + " EXP";
                    drawString(g, "", "@" + gained, 270, (int) expY);
                    expY -= 0.5;
                } else {
                    // Otherwise reset the popup
                    expY = 160;
                    showExpPopup = false;
                }
            }

            // New exp gain detected
            if (getTotalExp() != lastExpPopupTotal) {
                showExpPopup = true;
                expPopupStart = System.currentTimeMillis();
                xpGained = getTotalExp() - lastExpPopupTotal;
                lastExpPopupTotal = getTotalExp();
                expY = 160;
            }
        }

        return y;
    }

    // Draws debug info to the screen (separate method to allow for tacking this on to the end of script info)
    public int drawDebug(Graphics g, int x, int y) {
        if(getBoolean("debug")) {
            drawString(g, "Mouse Speed: ", getMouseSpeed(), x, y += 14);
            drawString(g, "Lag Multiplier: ", formatDecimal(getLagMultiplier()) + "x", x, y += 14);
            drawString(g, "CPU: ", (getCPU() > 80 ? "#" : "") + formatDecimal(getCPU()) + "%", x, y += 14);
        }

        return y;
    }

    // Draws the given 2 variables to the screen with different colors
    public void drawString(Graphics g, Object title, Object value, int x, int y) {
        g.setColor(Color.black);
        g.drawString(title.toString() + value.toString().replaceAll("#", "").replaceAll("@", ""), x + 1, y + 1);
        g.setColor(Color.white);
        g.drawString(title.toString(), x, y);
        Color c = Color.cyan;
        if(value.toString().startsWith("#"))
            c = Color.red;
        else
        if(value.toString().startsWith("@"))
            c = Color.green;
        g.setColor(c);
        g.drawString(value.toString().replaceAll("#", "").replaceAll("@", ""), x + g.getFontMetrics().stringWidth(title.toString()), y);
        g.setColor(Color.white);
    }

    // Draws the script title
    public void drawTitle(Graphics g, int x, int y) {
        g.setColor(Color.black);
        g.setFont(new Font(g.getFont().getName(), Font.BOLD, g.getFont().getSize()));
        drawString(g, getScriptName() + " - v" + s.getManifest().version(), "", x, y);
        g.setFont(new Font(g.getFont().getName(), Font.PLAIN, g.getFont().getSize()));
    }

    // Adds the given pause time (ms)
    public void addPauseTime(long ms) {
        PAUSE_TIME += ms;
    }

    // Formats the given percentage number input
    public String formatDecimal(Object o) {
        return new DecimalFormat("#.##").format(o);
    }

    // FOrmats the given number input
    public String format(Object o) {
        return NumberFormat.getInstance().format(o);
    }

    // Returns the current REPORT string
    public String getReport() {
        return "Runtime: " + getRuntime() + ", Lvls Gained: " + getLevelsGained() + ", Exp Gained: " + getExpGained() + "xp, Chatbot Replies: " + ban.getChatBotReplyCount() + ", Lag: " + new DecimalFormat("#.##").format(getLagMultiplier()) + "x, ";
    }

    // Gets the current price for the given item ID
    public int getPrice(final int id) {
        try (
                final BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(BASE_GE_URL + id).openStream()))) {
            final String raw = reader.readLine().replace(",", "").replace("\"", "").split("price:")[1].split("}")[0];
            return raw.endsWith("m") || raw.endsWith("k") ? (int) (Double.parseDouble(raw.substring(0, raw.length() - 1)) * (raw.endsWith("m") ? MILLION_GP : THOUSAND_GP)) : Integer.parseInt(raw);
        } catch (final Exception e) {
            e.printStackTrace();
        }

        return -1;
    }
}
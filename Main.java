package org.rickydactyl;

import com.google.common.eventbus.Subscribe;
import org.powbot.api.Area;
import org.powbot.api.Condition;
import org.powbot.api.Tile;
import org.powbot.api.event.RenderEvent;
import org.powbot.api.rt4.*;
import org.powbot.api.rt4.stream.locatable.interactive.NpcStream;
import org.powbot.api.rt4.walking.model.Skill;
import org.powbot.api.script.*;
import org.powbot.api.script.paint.Paint;
import org.powbot.api.script.paint.PaintBuilder;
import org.powbot.api.script.paint.TrackSkillOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

@ScriptConfiguration(name = "Skills to LevelUP", description = "Skill to train?: (Example: attack,strength,prayer)", defaultValue = "attack,strength,prayer",optionType = OptionType.STRING)
@ScriptConfiguration(name = "Loot Table", description = "Items to loot?: (Example: feather,bones)", defaultValue = "feather,bones",optionType = OptionType.STRING)
@ScriptConfiguration(name = "AutoEquip", description = "Automatically upgrade weapon tier? (Weapon must be in inventory)", optionType = OptionType.BOOLEAN)
@ScriptConfiguration(name = "AutoGrabBScim", description = "Collect bronze scimitar when no weapon found?",optionType = OptionType.BOOLEAN)
@ScriptManifest(name="rF2PMelee", description="Automatically trains melee stats on F2P monsters", author = "RickyDactyl", category = ScriptCategory.Combat)
public class Main extends AbstractScript {

    public static void main(String[] args) {
//        new ScriptUploader().uploadAndStart("rF2PMelee", "melee", "192.168.1.86:5555", true, false);
    }

    @Override
    public void onStart() {
        levelPrayer = false;
        lootTable = new ArrayList<>();
        lootTiles = new ArrayList<>();
        nextLootSize = 2 + new Random().nextInt(3);
        nextBoneSize = 2 + new Random().nextInt(3);
        skills = getOption("Skills to LevelUP").toString().split(",");
        for(String skill : skills) {
            if(skill.equalsIgnoreCase("prayer")) {
                levelPrayer = true;
                break;
            }
        }
        String tempLootTable = getOption("Loot Table");
        String[] tempList = tempLootTable != null && tempLootTable.split(",").length > 0 ? tempLootTable.split(",") : null;
        if(tempList != null) {
            for(String item : tempList) {
                lootTable.add(item.toLowerCase());
            }
        }
        shouldGrabScim = getOption("AutoGrabBScim");
        shouldEquipHighest = getOption("AutoEquip");
        attackLevel = Skills.level(Skill.Attack);
        currentSkill = getLowestSkill();
        currentGoal = currentSkill.realLevel() + (1+new Random().nextInt(4));
        local = Players.local();
        currentMonster = new Monster(getTargetMonsterType());
        area_scimitar = new Area(new Tile(2963, 3216, 1), new Tile(2970, 3209, 1));
        Paint paint = PaintBuilder.newBuilder()
                .y(350)
                .x(400)
                .addString("Current Goal:", new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        return currentSkill.name().toUpperCase() + " to " + currentGoal;
                    }
                })
                .addString("Current Monster:", new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        return currentMonster.getName().toUpperCase();
                    }
                })
                .trackSkill(Skill.Hitpoints, TrackSkillOption.Exp,TrackSkillOption.LevelProgressBar)
                .trackSkill(currentSkill,TrackSkillOption.Exp,TrackSkillOption.LevelProgressBar)
                .build();
        addPaint(paint);
    }

//    private void hopWorld(boolean members) {
//        List<World.Specialty> WORLD_SPECIALITY_FILTER = Arrays.asList(World.Specialty.BOUNTY_HUNTER, World.Specialty.TARGET_WORLD,
//                World.Specialty.FRESH_START, World.Specialty.HIGH_RISK, World.Specialty.BETA, World.Specialty.DEAD_MAN,
//                World.Specialty.LEAGUE, World.Specialty.PVP_ARENA, World.Specialty.SKILL_REQUIREMENT,
//                World.Specialty.SPEEDRUNNING, World.Specialty.TWISTED_LEAGUE, World.Specialty.PVP);
//        List<World> newWorlds = Worlds.get(world -> world.getType().equals(members ? World.Type.MEMBERS : World.Type.FREE)  && world.getNumber() != Worlds.current().getNumber() && !WORLD_SPECIALITY_FILTER.contains(world.specialty()));
//        World newWorld = newWorlds.get(new Random().nextInt(newWorlds.size()-1));
//        if(newWorld.hop()) {
//            Condition.wait(() -> Worlds.current() == newWorld, 200, 50);
//            if(Worlds.current() == newWorld) {
//            }
//        }
//    }

    private Skill getSkill(String string) {
        for(Skill skill : Skill.values()) {
            if(skill.getSkillName().equalsIgnoreCase(string)) {
                return skill;
            }
        }
        return null;
    }

    private Skill getLowestSkill() {
        List<String> skills = Arrays.asList(this.skills);
        Skill lowest = null;
        for(String s : skills) {
            Skill skill = getSkill(s);
            if(!skill.equals(Skill.Prayer)) {
                if(lowest == null || Skills.realLevel(skill) < Skills.realLevel(lowest)) {
                    lowest = skill;
                }
            }
        }
        return lowest;
    }

    private Combat.Style requiredCombatStyle() {
        switch(currentSkill) {
            case Strength: {
                return Combat.Style.AGGRESSIVE;
            }
            case Defence: {
                return Combat.Style.DEFENSIVE;
            }
            default : {
                return Combat.Style.ACCURATE;
            }
        }
    }

    private String getBestAcquiredWeapon() {
        String[] tierNames = {"Bronze","Iron","Steel","Black","Mithril","Adamant","Rune"};
        String weaponType = " scimitar";
        int highest = -1, current = 0;
        for(String tier : tierNames) {
            String weaponName = tier + weaponType;
            if(getWeaponLevel(weaponName) > attackLevel) {
                break;
            }
            if(Equipment.stream().name(weaponName).isNotEmpty()) {
                highest = current;
            } else if(Inventory.stream().name(weaponName).isNotEmpty()) {
                highest = current;
            }
            current++;
        }
        if(highest != -1) {
            return tierNames[highest] + weaponType;
        }
        return null;
    }

    private boolean sortSettings() {
        if(Camera.getZoom() > 11) {
            Camera.moveZoomSlider(1+new Random().nextInt(9));
            return true;
        } else if(Combat.autoRetaliate() == false) {
            Combat.autoRetaliate(true);
            return true;
        }
        return false;
    }

    @Override
    public void poll() {
        attackLevel = Skills.level(Skill.Attack);
        Combat.Style requiredStyle = requiredCombatStyle();
        if(sortSettings()) {
            return;
        } else if(currentSkill.realLevel() >= currentGoal) {
            currentSkill = getLowestSkill();
            currentGoal = currentSkill.realLevel() + (1+new Random().nextInt(4));
            String monster = getTargetMonsterType();
            if(!currentMonster.getName().equals(monster)) {
                currentMonster = new Monster(monster);
            }
            //Check boolean -> automatically grab the scimitar and checks if current exists
        } else if(shouldGrabScim && !Equipment.itemAt(Equipment.Slot.MAIN_HAND).valid()){
            String scimitar = "Bronze scimitar";
            if(Equipment.stream().name(scimitar).isEmpty()) {
                Item inv_scimitar = Inventory.stream().name(scimitar).first();
                if(inv_scimitar.valid() && inv_scimitar.interact("Wield")) {
                    Condition.wait(() -> !inv_scimitar.valid(), 100, 12);
                } else if(area_scimitar.contains(local)) {
                    GroundItem groundItem = GroundItems.stream().name(scimitar).first();
                    if(groundItem.valid() && groundItem.interact("Take")) {
                        Condition.wait(() -> !groundItem.valid(), 200, 50);
                    } else {
                        Condition.wait(() -> GroundItems.stream().name(scimitar).isNotEmpty(), 200, 50);
                    }
                } else {
                    Movement.walkTo(area_scimitar.getRandomTile());
                }
                return;
            }
            //Check boolean -> automatically equips highest detected scimitar in inventory
        } else if(shouldEquipHighest && Equipment.stream().name(getBestAcquiredWeapon()).isEmpty()) {
            Item item = Inventory.stream().name(getBestAcquiredWeapon()).first();
            if(item.valid() && item.interact("Wield")) {
                Condition.wait(() -> !item.valid(), 100, 12);
            }
        } else if(Combat.style() != requiredStyle) {
            Combat.style(requiredStyle);
            //If we need to loot more items make sure the inventory has space
        } else if(Inventory.isFull()) {
            lootTiles.clear();
            if(Bank.open()) {
                Condition.wait(() -> Bank.opened(), 100, 50);
                if(Bank.depositInventory()) {
                    Condition.wait(() -> Inventory.isEmpty(), 100, 50);
                }
            } else {
                Movement.moveToBank();
            }
            return;
        }

        NpcStream stream = Npcs.stream();
        List<Npc> NPC_LIST = stream.reachable().name(currentMonster.getName()).nearest().list();
        //Not in area run to assigned location
        if(NPC_LIST.isEmpty() && !currentMonster.getArea().contains(local)) {
            Movement.walkTo(currentMonster.getArea().getRandomTile());
             //Someone is interacting with us handle it
        } else if(levelPrayer && Inventory.stream().name("Bones","Big bones").list().size() > nextBoneSize) {
            for(Item item : Inventory.stream().name("Bones","Big bones")) {
                if(item.interact("Bury")) {
                    Condition.wait(() -> local.animation() != -1, 101, 12);
                    Condition.wait(() -> local.animation() == -1, 101, 12);
                }
            }
            nextBoneSize = 2 + new Random().nextInt(3);
        } else if(stream.interactingWithMe().isNotEmpty()) {
            Npc interacter = stream.interactingWithMe().nearest().first();
            if(!local.interacting().valid()) {
                //Not attacking them attack back
                if(interacter.interact("Attack")) {
                    Condition.wait(() -> !interacter.valid() || !interacter.inCombat() || local.inCombat(), 200, 50);
                }
            } else {
                Condition.wait(() -> (!interacter.inMotion() && !local.inMotion()) || interacter.healthPercent() == 0 || local.healthPercent() == 0, 100, 50);
                Tile interacterTile = interacter.tile();
                interacterTile = interacter.name().equalsIgnoreCase("Cow") ? new Tile(interacterTile.getX()-1, interacterTile.getY()-1, 0) :  interacterTile;
                if(interacter.valid() && !lootTiles.contains(interacterTile)) {
                    lootTiles.add(interacterTile);
                }
                //Wait till end of combat
                Condition.wait(() -> local.healthPercent() == 0 || !local.interacting().valid() || !interacter.valid() || interacter.healthPercent() == 0, 100, 300);
            }
        } else if(!lootTable.isEmpty() && lootTiles.size() >= nextLootSize) {
            boolean stopSearch = false;
            int x = 0;
            List<GroundItem> groundItems = GroundItems.stream().isEmpty() ? null : GroundItems.stream().list();
            ArrayList<String> alreadyLooted = new ArrayList<>();
            for(Tile tile : lootTiles) {
                if(!stopSearch && tile.valid()) {
                    if(groundItems != null) {
                        for(GroundItem item : groundItems) {
                            if(Inventory.isFull()) {
                                stopSearch = true;
                                break;
                            } else if(item.valid() && !alreadyLooted.contains(item.name()) && item.tile().equals(tile) && lootTable.contains(item.name().toLowerCase()) && item.interact("Take")) {
                                Condition.wait(() -> !item.valid(), 100, 70);
                                if(!item.valid()) {
                                    alreadyLooted.add(item.name());
                                }
                            }
                        }
                    }
                }
                alreadyLooted = new ArrayList<>();
            }
            lootTiles.clear();
            nextLootSize = 2 + new Random().nextInt(3);
        } else if(!local.interacting().valid() && !local.inCombat()) {
            if(!NPC_LIST.isEmpty()) {
                for(Npc npc : NPC_LIST) {
                    if(npc.valid() && !npc.inCombat()) {
                        if(npc.inViewport() && npc.interact("Attack")) {;
                            Condition.wait(() -> local.inMotion() , 600, 15);
                            if(local.inMotion()) {
                                Condition.wait(() -> !local.inMotion(), 600, 15);
                            }
                            Condition.wait(() -> !npc.valid(), 601, 5);
                            break;
                        } else {
                            Camera.turnTo(npc);
                        }
                    }
                }
            }
        }
    }
    @Subscribe
    public void onRender(RenderEvent r){

    }

    private String getTargetMonsterType() {
        if(attackLevel > 20) {
            return "Cow";
        } else if(attackLevel > 10) {
            return "Goblin";
        } else {
            return "Chicken";
        }
    }



    private int getWeaponLevel(String item) {
        String itemGrade = item.split(" ")[0];
        switch (itemGrade.toUpperCase()) {
            case "STEEL" : {
                return 5;
            }
            case "BLACK" : {
                return 10;
            }
            case "MITHRIL" : {
                return 20;
            }
            case "ADAMANT" : {
                return 30;
            }
            case "RUNE" : {
                return 40;
            }
            default : {
                return 1;
            }
        }
    }

    private Monster currentMonster;

    private int attackLevel, currentGoal, nextLootSize, nextBoneSize;
    private static Player local;

    String[] skills;
    boolean shouldGrabScim, shouldEquipHighest, levelPrayer;

    List<String> lootTable;
    private ArrayList<Tile> lootTiles;

    private Skill currentSkill;
    private Area area_scimitar;

}
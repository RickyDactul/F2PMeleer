package org.rickydactyl;

import org.powbot.api.Area;
import org.powbot.api.Tile;
import org.powbot.api.rt4.Skills;
import org.powbot.api.rt4.walking.model.Skill;

import java.util.ArrayList;
import java.util.Random;

public class Monster  {

    private String name;
    private Integer level, foodLow, foodHigh;
    private Boolean aggresive;
    private String[] lootables, junkItems;
    private Area area;

    public Monster(String name) {
        ArrayList<Tile> locations = new ArrayList<>();
        int defenceLevel = Skills.level(Skill.Defence);
        setName(name);
        switch(name) {
            case "Goblin" : {
                setFoodLow(4);
                setFoodHigh(6);
                if(defenceLevel >= 20) {
                    setFoodLow(1);
                    setFoodHigh(1);
                } else if(defenceLevel >= 10) {
                    setFoodLow(2);
                    setFoodHigh(3);
                }
                setAggresive(false);
                level = 2;
                setLootables(new String[] {"Bones","Goblin mail","Chef's hat","Bronze sq shield","Beer", "Brass necklace", "Hammer","Iron arrow","Mithril arrow"});
                setJunkItems(new String[] {"Jug","Earth rune","Water rune","Body rune","Bronze bolts"});
                area = getRandomMonsterArea(new Area[]{
                        new Area(new Tile(2987, 3217), new Tile(3007, 3201)),
                        new Area(new Tile(3136, 3265), new Tile(3152, 3256)),
                        new Area(new Tile(3139, 3234), new Tile(3148, 3222)),
                        new Area(new Tile(3182, 3255), new Tile(3206, 3242)),
                        new Area(new Tile(3248, 3253), new Tile(3263, 3225)),
                        new Area(new Tile(3160, 3286), new Tile(3191, 3281))
                });
                break;
            }
            case "Chicken" : {
                setFoodLow(1);
                setFoodHigh(1);
                setAggresive(false);
                level = 1;
                setLootables(new String[] {"Feather","Raw chicken"});
                setJunkItems(new String[] {});
                area = getRandomMonsterArea(new Area[]{
                                new Area(new Tile(3184, 3279, 0), new Tile(3196, 3276, 0)),
                                //new Area(new Tile(3171, 3301, 0), new Tile(3183, 3290, 0)),
                                //new Area(new Tile(3014, 3294, 0), new Tile(3019, 3282, 0)),
                                //new Area(new Tile(3026, 3289, 0), new Tile(3037, 3282, 0))
                        }
                );
                break;
            }
            case "Seagull" : {
                setAggresive(false);
                level = 2;
                setLootables(new String[]{"Bones","Iron arrow","Mithril arrow"});
                setJunkItems(new String[]{});
                locations.add(new Tile(3028,3236,0)); //Port Sarim Dock
                area = getRandomMonsterArea(new Area[] {
                        new Area(new Tile(3026, 3240, 0), new Tile(3029, 3202, 0))
                        }
                );
                break;
            }
//            case "Giant frog" : {
//                setAggresive(false);
//                level = 13;
//                setLootables(new String[]{"Big bones","Iron arrow","Mithril arrow"});
//                setJunkItems(new String[]{"Jug"});
//                setFoodLow(8);
//                setFoodHigh(16);
//                area = getRandomMonsterArea(new WorldArea[]{
//                        new WorldArea(3193, 3172, 12, 21, 0)
//                });
//                break;
//            }
            case "Cow" : {
                setFoodLow(3);
                setFoodHigh(5);
                if(defenceLevel >= 20) {
                    setFoodLow(1);
                    setFoodHigh(1);
                } else if(defenceLevel >= 10) {
                    setFoodLow(2);
                    setFoodHigh(4);
                }
                setAggresive(false);
                level = 2;
                setLootables(new String[]{"Cowhide","Raw beef","Bones","Iron arrow","Mithril arrow"});
                area = getRandomMonsterArea(new Area[]{
                        new Area(new Tile(3022, 3312, 0), new Tile(3041, 3299, 0)),
//                        new Area(new Tile(3154, 3342, 0), new Tile(3197, 3318, 0)),
//                        new Area(new Tile(3195, 3301, 0), new Tile(3209, 3284, 0))
                });
                break;
            }
        }
    }

    private Area getRandomMonsterArea(Area[] areas) {
        if(areas.length > 1) {
            return areas[0 + new Random().nextInt(areas.length)];
        }
        return areas[0];
    }

    public String[] getLootables() {
        return lootables;
    }

    public void setLootables(String[] lootables) {
        this.lootables = lootables;
    }

    public String[] getJunkItems() {
        return junkItems;
    }

    public void setJunkItems(String[] junkItems) {
        this.junkItems = junkItems;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLevel() {
        return level;
    }

    public Integer getFoodLow() {
        return foodLow;
    }

    public void setFoodLow(Integer foodLow) {
        this.foodLow = foodLow;
    }

    public Integer getFoodHigh() {
        return foodHigh;
    }

    public void setFoodHigh(Integer foodHigh) {
        this.foodHigh = foodHigh;
    }

    public Boolean getAggresive() {
        return aggresive;
    }

    public void setAggresive(Boolean aggresive) {
        this.aggresive = aggresive;
    }

    public static long getTimeDifferenceInSeconds(long startTime, long endTime) {
        return (endTime - startTime) / 1000;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }
}
package top.chancelethay.bingo.lib.platform;

import top.chancelethay.bingo.BingoReloaded;
import top.chancelethay.bingo.lib.platform.item.ItemType;
import top.chancelethay.bingo.util.StatisticsKeyConverter;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public record StatisticHandle(@NotNull StatisticType statistic, @Nullable EntityType entityType, @Nullable ItemType itemType)
{
    private static final Set<EntityType> VALID_ENTITIES_FOR_STATISTICS = BingoReloaded.runtime().getValidEntityTypesForStatistics();

    public StatisticHandle(StatisticType stat)
    {
        this(stat, null, null);
    }

    public StatisticHandle(StatisticType stat, @Nullable EntityType entityType)
    {
        this(stat, entityType, null);
    }

    public StatisticHandle(StatisticType stat, @Nullable ItemType itemType)
    {
        this(stat, null, itemType);
    }

    public StatisticHandle(Statistic stat)
    {
        this(new StatisticType(stat), null, null);
    }

    public StatisticHandle(Statistic stat, @NotNull org.bukkit.entity.EntityType entityType)
    {
        this(new StatisticType(stat), new EntityType(entityType), null);
    }

    public StatisticHandle(Statistic stat, @NotNull Material itemType)
    {
        this(new StatisticType(stat), null, ItemType.of(itemType));
    }

    public static StatisticHandle create(StatisticType type, @Nullable ItemType item, @Nullable EntityType entity) {
        return new StatisticHandle(type, entity, item);
    }

    public static StatisticHandle create(Statistic stat, @Nullable org.bukkit.entity.EntityType entity, @Nullable Material itemType) {
        if (entity == null && itemType != null) {
            return new StatisticHandle(new StatisticType(stat), null, ItemType.of(itemType));
        } else if (entity != null && itemType == null) {
            return new StatisticHandle(new StatisticType(stat), new EntityType(entity), null);
        } else if (entity == null && itemType == null) {
            return new StatisticHandle(new StatisticType(stat), null, null);
        } else {
            return new StatisticHandle(new StatisticType(stat), new EntityType(entity), ItemType.of(itemType));
        }
    }

    public static Set<EntityType> getValidEntityTypes() {
        return VALID_ENTITIES_FOR_STATISTICS;
    }

    public boolean isEntityValid() {
        return VALID_ENTITIES_FOR_STATISTICS.contains(entityType());
    }

    public StatisticType statisticType() {
        return statistic;
    }

    public boolean hasItemType() {
        return itemType() != null;
    }

    public boolean hasEntity() {
        return entityType() != null;
    }

    public boolean isSubStatistic() {
        return statistic.handle().isSubstatistic();
    }

    public String translationKey() {
        return StatisticsKeyConverter.getMinecraftTranslationKey(statistic.handle());
    }

    /**
     * @return True if this statistic is processed by the PlayerStatisticIncrementEvent
     */
    public boolean getsUpdatedAutomatically()
    {
        if (statistic.getCategory() == StatisticType.StatisticCategory.TRAVEL)
            return false;

        return switch (statistic.handle())
        {
            case PLAY_ONE_MINUTE,
                    SNEAK_TIME,
                    TOTAL_WORLD_TIME,
                    TIME_SINCE_REST,
                    TIME_SINCE_DEATH -> false;
            default -> true;
        };
    }

    public static String createDescription(Statistic stat)
    {
        return switch (stat)
        {
            default -> "";
        };
    }

    public @NotNull ItemType icon()
    {
        return switch (statistic.handle())
        {
            case DAMAGE_DEALT -> ItemType.of(Material.DIAMOND_SWORD);
            case DAMAGE_TAKEN -> ItemType.of(Material.IRON_CHESTPLATE);
            case DEATHS -> ItemType.of(Material.SKELETON_SKULL);
            case MOB_KILLS -> ItemType.of(Material.CREEPER_HEAD);
            case PLAYER_KILLS -> ItemType.of(Material.PLAYER_HEAD);
            case FISH_CAUGHT -> ItemType.of(Material.TROPICAL_FISH);
            case ANIMALS_BRED -> ItemType.of(Material.WHEAT);
            case LEAVE_GAME -> ItemType.of(Material.BARRIER);
            case JUMP -> ItemType.of(Material.RABBIT_FOOT);
            case DROP_COUNT, HOPPER_INSPECTED -> ItemType.of(Material.HOPPER);
            case PLAY_ONE_MINUTE -> ItemType.of(Material.CLOCK);
            case TOTAL_WORLD_TIME -> ItemType.of(Material.FILLED_MAP);
            case WALK_ONE_CM -> ItemType.of(Material.LEATHER_BOOTS);
            case WALK_ON_WATER_ONE_CM -> ItemType.of(Material.ICE);
            case FALL_ONE_CM -> ItemType.of(Material.LAVA_BUCKET);
            case SNEAK_TIME -> ItemType.of(Material.SCULK_SHRIEKER);
            case CLIMB_ONE_CM -> ItemType.of(Material.EMERALD_ORE);
            case FLY_ONE_CM -> ItemType.of(Material.COMMAND_BLOCK);
            case WALK_UNDER_WATER_ONE_CM -> ItemType.of(Material.GOLDEN_BOOTS);
            case MINECART_ONE_CM -> ItemType.of(Material.MINECART);
            case BOAT_ONE_CM -> ItemType.of(Material.OAK_BOAT);
            case PIG_ONE_CM -> ItemType.of(Material.CARROT_ON_A_STICK);
            case HORSE_ONE_CM -> ItemType.of(Material.SADDLE);
            case SPRINT_ONE_CM -> ItemType.of(Material.FEATHER);
            case CROUCH_ONE_CM -> ItemType.of(Material.SCULK_SENSOR);
            case AVIATE_ONE_CM -> ItemType.of(Material.ELYTRA);
            case TIME_SINCE_DEATH -> ItemType.of(Material.RECOVERY_COMPASS);
            case TALKED_TO_VILLAGER -> ItemType.of(Material.POPPY);
            case TRADED_WITH_VILLAGER -> ItemType.of(Material.EMERALD);
            case CAKE_SLICES_EATEN -> ItemType.of(Material.CAKE);
            case CAULDRON_FILLED -> ItemType.of(Material.CAULDRON);
            case CAULDRON_USED -> ItemType.of(Material.WATER_BUCKET);
            case ARMOR_CLEANED -> ItemType.of(Material.LEATHER_CHESTPLATE);
            case BANNER_CLEANED -> ItemType.of(Material.WHITE_BANNER);
            case BREWINGSTAND_INTERACTION -> ItemType.of(Material.BREWING_STAND);
            case BEACON_INTERACTION -> ItemType.of(Material.BEACON);
            case DROPPER_INSPECTED -> ItemType.of(Material.DROPPER);
            case DISPENSER_INSPECTED -> ItemType.of(Material.DISPENSER);
            case NOTEBLOCK_PLAYED, NOTEBLOCK_TUNED -> ItemType.of(Material.NOTE_BLOCK);
            case FLOWER_POTTED -> ItemType.of(Material.FLOWER_POT);
            case TRAPPED_CHEST_TRIGGERED -> ItemType.of(Material.TRAPPED_CHEST);
            case ENDERCHEST_OPENED -> ItemType.of(Material.ENDER_CHEST);
            case ITEM_ENCHANTED -> ItemType.of(Material.ENCHANTING_TABLE);
            case RECORD_PLAYED -> ItemType.of(Material.MUSIC_DISC_CAT);
            case FURNACE_INTERACTION -> ItemType.of(Material.FURNACE);
            case CRAFTING_TABLE_INTERACTION -> ItemType.of(Material.CRAFTING_TABLE);
            case CHEST_OPENED -> ItemType.of(Material.CHEST);
            case SLEEP_IN_BED -> ItemType.of(Material.RED_BED);
            case SHULKER_BOX_OPENED -> ItemType.of(Material.SHULKER_BOX);
            case TIME_SINCE_REST -> ItemType.of(Material.YELLOW_BED);
            case SWIM_ONE_CM -> ItemType.of(Material.BUBBLE_CORAL);
            case DAMAGE_DEALT_ABSORBED -> ItemType.of(Material.DAMAGED_ANVIL);
            case DAMAGE_DEALT_RESISTED -> ItemType.of(Material.NETHERITE_SWORD);
            case DAMAGE_BLOCKED_BY_SHIELD -> ItemType.of(Material.SHIELD);
            case DAMAGE_ABSORBED -> ItemType.of(Material.SPONGE);
            case DAMAGE_RESISTED -> ItemType.of(Material.DIAMOND_CHESTPLATE);
            case CLEAN_SHULKER_BOX -> ItemType.of(Material.SHULKER_SHELL);
            case OPEN_BARREL -> ItemType.of(Material.BARREL);
            case INTERACT_WITH_BLAST_FURNACE -> ItemType.of(Material.BLAST_FURNACE);
            case INTERACT_WITH_SMOKER -> ItemType.of(Material.SMOKER);
            case INTERACT_WITH_LECTERN -> ItemType.of(Material.LECTERN);
            case INTERACT_WITH_CAMPFIRE -> ItemType.of(Material.CAMPFIRE);
            case INTERACT_WITH_CARTOGRAPHY_TABLE -> ItemType.of(Material.CARTOGRAPHY_TABLE);
            case INTERACT_WITH_LOOM -> ItemType.of(Material.LOOM);
            case INTERACT_WITH_STONECUTTER -> ItemType.of(Material.STONECUTTER);
            case BELL_RING -> ItemType.of(Material.BELL);
            case RAID_TRIGGER -> ItemType.of(Material.CROSSBOW);
            case RAID_WIN -> ItemType.of(Material.TOTEM_OF_UNDYING);
            case INTERACT_WITH_ANVIL -> ItemType.of(Material.ANVIL);
            case INTERACT_WITH_GRINDSTONE -> ItemType.of(Material.GRINDSTONE);
            case TARGET_HIT -> ItemType.of(Material.TARGET);
            case INTERACT_WITH_SMITHING_TABLE -> ItemType.of(Material.SMITHING_TABLE);
            case STRIDER_ONE_CM -> ItemType.of(Material.WARPED_FUNGUS_ON_A_STICK);
            case HAPPY_GHAST_ONE_CM -> ItemType.of(Material.DRIED_GHAST);
			case NAUTILUS_ONE_CM -> ItemType.of(Material.GOLDEN_NAUTILUS_ARMOR);
            case DROP,
                    PICKUP,
                    MINE_BLOCK,
                    USE_ITEM,
                    BREAK_ITEM,
                    CRAFT_ITEM,
                    KILL_ENTITY,
                    ENTITY_KILLED_BY -> rootStatIcon(statistic.handle());
        };
    }

    private ItemType rootStatIcon(Statistic statistic)
    {
        if (statistic.getType() == Statistic.Type.ITEM || statistic.getType() == Statistic.Type.BLOCK) {
            return itemType();
        }
        else if (entityType() != null &&
                statistic.getType() == Statistic.Type.ENTITY)
        {
            return ItemType.of("minecraft:" + entityType().key().value() + "_spawn_egg");
        }

        return ItemType.of(Material.GLOBE_BANNER_PATTERN);
    }
}

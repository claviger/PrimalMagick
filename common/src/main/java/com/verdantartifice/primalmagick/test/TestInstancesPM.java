package com.verdantartifice.primalmagick.test;

import com.verdantartifice.primalmagick.common.util.ResourceUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;

import java.util.function.Consumer;

public class TestInstancesPM {
    public static final ResourceKey<GameTestInstance> CANARY = createInstanceKey("canary");

    // Attunement buff tests
    public static final ResourceKey<GameTestInstance> MINOR_ATTUNEMENT_DISCOUNT_EARTH = createInstanceKey("minor_attunement_discount_earth");
    public static final ResourceKey<GameTestInstance> MINOR_ATTUNEMENT_DISCOUNT_SEA = createInstanceKey("minor_attunement_discount_sea");
    public static final ResourceKey<GameTestInstance> MINOR_ATTUNEMENT_DISCOUNT_SKY = createInstanceKey("minor_attunement_discount_sky");
    public static final ResourceKey<GameTestInstance> MINOR_ATTUNEMENT_DISCOUNT_SUN = createInstanceKey("minor_attunement_discount_sun");
    public static final ResourceKey<GameTestInstance> MINOR_ATTUNEMENT_DISCOUNT_MOON = createInstanceKey("minor_attunement_discount_moon");
    public static final ResourceKey<GameTestInstance> MINOR_ATTUNEMENT_DISCOUNT_BLOOD = createInstanceKey("minor_attunement_discount_blood");
    public static final ResourceKey<GameTestInstance> MINOR_ATTUNEMENT_DISCOUNT_INFERNAL = createInstanceKey("minor_attunement_discount_infernal");
    public static final ResourceKey<GameTestInstance> MINOR_ATTUNEMENT_DISCOUNT_VOID = createInstanceKey("minor_attunement_discount_void");
    public static final ResourceKey<GameTestInstance> MINOR_ATTUNEMENT_DISCOUNT_HALLOWED = createInstanceKey("minor_attunement_discount_hallowed");
    public static final ResourceKey<GameTestInstance> LESSER_EARTH_ATTUNEMENT_BUFF = createInstanceKey("lesser_earth_attunement_buff");
    public static final ResourceKey<GameTestInstance> GREATER_EARTH_ATTUNEMENT_BUFF = createInstanceKey("greater_earth_attunement_buff");
    public static final ResourceKey<GameTestInstance> LESSER_SEA_ATTUNEMENT_BUFF = createInstanceKey("lesser_sea_attunement_buff");
    public static final ResourceKey<GameTestInstance> GREATER_SEA_ATTUNEMENT_BUFF = createInstanceKey("greater_sea_attunement_buff");
    public static final ResourceKey<GameTestInstance> LESSER_SKY_ATTUNEMENT_BUFF1 = createInstanceKey("lesser_sky_attunement_buff1");
    public static final ResourceKey<GameTestInstance> LESSER_SKY_ATTUNEMENT_BUFF2 = createInstanceKey("lesser_sky_attunement_buff2");
    public static final ResourceKey<GameTestInstance> GREATER_SKY_ATTUNEMENT_BUFF = createInstanceKey("greater_sky_attunement_buff");
    public static final ResourceKey<GameTestInstance> LESSER_SUN_ATTUNEMENT_DAY_BUFF = createInstanceKey("lesser_sun_attunement_day_buff");
    public static final ResourceKey<GameTestInstance> LESSER_SUN_ATTUNEMENT_NIGHT_BUFF = createInstanceKey("lesser_sun_attunement_night_buff");
    public static final ResourceKey<GameTestInstance> LESSER_MOON_ATTUNEMENT_BUFF = createInstanceKey("lesser_moon_attunement_buff");
    public static final ResourceKey<GameTestInstance> GREATER_MOON_ATTUNEMENT_BUFF = createInstanceKey("greater_moon_attunement_buff");
    public static final ResourceKey<GameTestInstance> LESSER_BLOOD_ATTUNEMENT_BUFF = createInstanceKey("lesser_blood_attunement_buff");
    public static final ResourceKey<GameTestInstance> GREATER_BLOOD_ATTUNEMENT_BUFF = createInstanceKey("greater_blood_attunement_buff");
    public static final ResourceKey<GameTestInstance> GREATER_INFERNAL_ATTUNEMENT_BUFF_IN_FIRE = createInstanceKey("greater_infernal_attunement_buff_in_fire");
    public static final ResourceKey<GameTestInstance> GREATER_INFERNAL_ATTUNEMENT_BUFF_ON_FIRE = createInstanceKey("greater_infernal_attunement_buff_on_fire");
    public static final ResourceKey<GameTestInstance> GREATER_INFERNAL_ATTUNEMENT_BUFF_LAVA = createInstanceKey("greater_infernal_attunement_buff_lava");
    public static final ResourceKey<GameTestInstance> GREATER_INFERNAL_ATTUNEMENT_BUFF_HOT_FLOOR = createInstanceKey("greater_infernal_attunement_buff_hot_floor");
    public static final ResourceKey<GameTestInstance> GREATER_INFERNAL_ATTUNEMENT_BUFF_INFERNAL_SORCERY = createInstanceKey("greater_infernal_attunement_buff_infernal_sorcery");
    public static final ResourceKey<GameTestInstance> LESSER_VOID_ATTUNEMENT_BUFF = createInstanceKey("lesser_void_attunement_buff");
    public static final ResourceKey<GameTestInstance> GREATER_VOID_ATTUNEMENT_BUFF = createInstanceKey("greater_void_attunement_buff");
    public static final ResourceKey<GameTestInstance> LESSER_HALLOWED_ATTUNEMENT_BUFF = createInstanceKey("lesser_allowed_attunement_buff");
    public static final ResourceKey<GameTestInstance> GREATER_HALLOWED_ATTUNEMENT_BUFF = createInstanceKey("greater_allowed_attunement_buff");

    // Item handler tests
    public static final ResourceKey<GameTestInstance> ITEM_HANDLER_NULL_DIRECTION_RESEARCH_TABLE = createInstanceKey("item_handler_null_direction_research_table");
    public static final ResourceKey<GameTestInstance> ITEM_HANDLER_NULL_DIRECTION_WAND_CHARGER = createInstanceKey("item_handler_null_direction_wand_charger");
    public static final ResourceKey<GameTestInstance> ITEM_HANDLER_NULL_DIRECTION_CALCINATOR_BASIC = createInstanceKey("item_handler_null_direction_calculator_basic");

    // Item handler capability tests
    public static final ResourceKey<GameTestInstance> ITEM_HANDLER_CAPABILITY_REJECTS_INVALID_ITEM_FOR_ALTAR_OUTPUT = createInstanceKey("item_handler_capability_rejects_invalid_item_for_altar_output");
    public static final ResourceKey<GameTestInstance> ITEM_HANDLER_CAPABILITY_ENFORCES_SLOT_LIMIT_ON_OFFERING_PEDESTAL = createInstanceKey("item_handler_capability_enforces_slot_limit_on_offering_pedestal");
    public static final ResourceKey<GameTestInstance> ITEM_HANDLER_CAPABILITY_ACCEPTS_VALID_ITEM = createInstanceKey("item_handler_capability_accepts_valid_item");

    // Player knowledge tests
    public static final ResourceKey<GameTestInstance> ADD_AND_CHECK_RESEARCH = createInstanceKey("add_and_check_research");
    public static final ResourceKey<GameTestInstance> CANNOT_ADD_DUPLICATE_RESEARCH = createInstanceKey("cannot_add_duplicate_research");
    public static final ResourceKey<GameTestInstance> REMOVE_RESEARCH = createInstanceKey("remove_research");
    public static final ResourceKey<GameTestInstance> GET_RESEARCH_SET = createInstanceKey("get_research_set");
    public static final ResourceKey<GameTestInstance> GET_SET_RESEARCH_STAGE = createInstanceKey("get_set_research_stage");
    public static final ResourceKey<GameTestInstance> GET_SET_RESEARCH_FLAG = createInstanceKey("get_set_research_flag");
    public static final ResourceKey<GameTestInstance> REMOVE_RESEARCH_FLAG = createInstanceKey("remove_research_flag");
    public static final ResourceKey<GameTestInstance> GET_RESEARCH_FLAGS = createInstanceKey("get_research_flags");
    public static final ResourceKey<GameTestInstance> GET_RESEARCH_STATUS = createInstanceKey("get_research_status");
    public static final ResourceKey<GameTestInstance> IS_RESEARCH_COMPLETE = createInstanceKey("is_research_complete");
    public static final ResourceKey<GameTestInstance> GET_SET_KNOWLEDGE_RAW = createInstanceKey("get_set_knowledge_raw");
    public static final ResourceKey<GameTestInstance> GET_KNOWLEDGE_LEVELS = createInstanceKey("get_knowledge_levels");
    public static final ResourceKey<GameTestInstance> GET_SET_ACTIVE_RESEARCH_PROJECT = createInstanceKey("get_set_active_research_project");
    public static final ResourceKey<GameTestInstance> GET_SET_LAST_RESEARCH_TOPIC = createInstanceKey("get_set_last_research_topic");
    public static final ResourceKey<GameTestInstance> GET_SET_RESEARCH_TOPIC_HISTORY = createInstanceKey("get_set_research_topic_history");
    public static final ResourceKey<GameTestInstance> KNOWLEDGE_SERIALIZATION = createInstanceKey("knowledge_serialization");
    public static final ResourceKey<GameTestInstance> ADD_AND_CHECK_RESEARCH_POST_SERIALIZATION = createInstanceKey("add_and_check_research_post_serialization");

    // Arcane workbench tests
    public static final ResourceKey<GameTestInstance> ARCANE_WORKBENCH_CRAFT_WORKS = createInstanceKey("arcane_workbench_craft_works");

    // Calcinator tests
    public static final ResourceKey<GameTestInstance> CALCINATOR_WORKS_WITH_PLAYER_PRESENT = createInstanceKey("calcinator_works_with_player_present");
    public static final ResourceKey<GameTestInstance> CALCINATOR_WORKS_WITHOUT_PLAYER_PRESENT = createInstanceKey("calcinator_works_without_player_present");
    public static final ResourceKey<GameTestInstance> CALCINATOR_LAVA_BUCKET_FUEL_LEAVES_EMPTY_BUCKET = createInstanceKey("calcinator_lava_bucket_fuel_leaves_empty_bucket");

    // Crafting requirement tests
    public static final ResourceKey<GameTestInstance> CRAFTING_REQUIREMENT_ARCANE_RECIPE = createInstanceKey("crafting_requirement_arcane_recipe");
    public static final ResourceKey<GameTestInstance> CRAFTING_REQUIREMENT_RITUAL_RECIPE = createInstanceKey("crafting_requirement_ritual_recipe");

    // Repair tests
    public static final ResourceKey<GameTestInstance> EARTHSHATTER_HAMMER_CANNOT_BE_REPAIRED = createInstanceKey("earthshatter_hammer_cannot_be_repaired");

    // Earthshatter hammer tests
    public static final ResourceKey<GameTestInstance> EARTHSHATTER_HAMMER_RECIPE_RETURNS_HAMMER = createInstanceKey("earthshatter_hammer_recipe_returns_hammer");
    public static final ResourceKey<GameTestInstance> EARTHSHATTER_HAMMER_HAS_NO_DURABILITY = createInstanceKey("earthshatter_hammer_has_no_durability");
    public static final ResourceKey<GameTestInstance> EARTHSHATTER_HAMMER_REMAINDER_KEEPS_COMPONENTS = createInstanceKey("earthshatter_hammer_remainder_keeps_components");
    public static final ResourceKey<GameTestInstance> EARTHSHATTER_HAMMER_REMAINDER_COUNT_IS_ONE_FOR_STACKED_INPUT = createInstanceKey("earthshatter_hammer_remainder_count_is_one_for_stacked_input");

    // Runecarving tests
    public static final ResourceKey<GameTestInstance> RUNECARVING_CRAFT_WORKS = createInstanceKey("runecarving_craft_works");

    // Wand inscription tests
    public static final ResourceKey<GameTestInstance> WAND_INSCRIPTION_INSCRIBES_SCROLL_INTO_WAND = createInstanceKey("wand_inscription_inscribes_scroll_into_wand");
    public static final ResourceKey<GameTestInstance> WAND_INSCRIPTION_INSCRIBES_SCROLL_INTO_SPELLTOME = createInstanceKey("wand_inscription_inscribes_scroll_into_spelltome");
    public static final ResourceKey<GameTestInstance> WAND_INSCRIPTION_REJECTS_FULL_CONTAINER = createInstanceKey("wand_inscription_rejects_full_container");
    public static final ResourceKey<GameTestInstance> WAND_INSCRIPTION_CLEARS_SPELLS_WITHOUT_SCROLL = createInstanceKey("wand_inscription_clears_spells_without_scroll");
    public static final ResourceKey<GameTestInstance> WAND_INSCRIPTION_EMPTY_CASTER_WITHOUT_SCROLL_GIVES_NOTHING = createInstanceKey("wand_inscription_empty_caster_without_scroll_gives_nothing");
    public static final ResourceKey<GameTestInstance> WAND_INSCRIPTION_APPENDS_SPELL_TO_PARTIALLY_FILLED_SPELLTOME = createInstanceKey("wand_inscription_appends_spell_to_partially_filled_spelltome");
    public static final ResourceKey<GameTestInstance> WAND_INSCRIPTION_ALL_RECIPE_KEYS_RESOLVE = createInstanceKey("wand_inscription_all_recipe_keys_resolve");

    // Runescribing result tests
    public static final ResourceKey<GameTestInstance> RUNESCRIBING_CREDIT_ONLY_APPLIED_ENCHANTMENTS = createInstanceKey("runescribing_credit_only_applied_enchantments");
    public static final ResourceKey<GameTestInstance> RUNESCRIBING_CREDIT_ALL_APPLIED_ENCHANTMENTS = createInstanceKey("runescribing_credit_all_applied_enchantments");
    public static final ResourceKey<GameTestInstance> RUNESCRIBING_CREDIT_SKIPS_ENCHANTMENT_BLOCKED_BY_EXISTING = createInstanceKey("runescribing_credit_skips_enchantment_blocked_by_existing");
    public static final ResourceKey<GameTestInstance> RUNESCRIBING_CREDIT_SKIPS_PREEXISTING_ENCHANTMENT_MATCHING_RUNES = createInstanceKey("runescribing_credit_skips_preexisting_enchantment_matching_runes");

    // Caster enchantability tests
    public static final ResourceKey<GameTestInstance> CASTER_ENCHANTABLE_MUNDANE_WAND = createInstanceKey("caster_enchantable_mundane_wand");
    public static final ResourceKey<GameTestInstance> CASTER_ENCHANTABLE_MODULAR_WAND = createInstanceKey("caster_enchantable_modular_wand");
    public static final ResourceKey<GameTestInstance> CASTER_ENCHANTABLE_MODULAR_STAFF = createInstanceKey("caster_enchantable_modular_staff");

    // Ritual enchantment tests
    public static final ResourceKey<GameTestInstance> ENCHANTMENT_ESSENCE_THIEF1 = createInstanceKey("enchantment_essence_thief1");
    public static final ResourceKey<GameTestInstance> ENCHANTMENT_ESSENCE_THIEF2 = createInstanceKey("enchantment_essence_thief2");
    public static final ResourceKey<GameTestInstance> ENCHANTMENT_ESSENCE_THIEF3 = createInstanceKey("enchantment_essence_thief3");
    public static final ResourceKey<GameTestInstance> ENCHANTMENT_ESSENCE_THIEF4 = createInstanceKey("enchantment_essence_thief4");

    // Rune manager tests
    public static final ResourceKey<GameTestInstance> RUNE_ENCHANTMENT_RESOLVES_SHARPNESS = createInstanceKey("rune_enchantment_resolves_sharpness");
    public static final ResourceKey<GameTestInstance> RUNE_ENCHANTMENT_RESOLVES_PROTECTION = createInstanceKey("rune_enchantment_resolves_protection");
    public static final ResourceKey<GameTestInstance> RUNE_ENCHANTMENT_RESOLVES_FIRE_PROTECTION = createInstanceKey("rune_enchantment_resolves_fire_protection");
    public static final ResourceKey<GameTestInstance> RUNE_ENCHANTMENT_RESOLVES_SMITE = createInstanceKey("rune_enchantment_resolves_smite");
    public static final ResourceKey<GameTestInstance> RUNE_ENCHANTMENT_RESOLVES_FROST_WALKER = createInstanceKey("rune_enchantment_resolves_frost_walker");
    public static final ResourceKey<GameTestInstance> RUNE_ENCHANTMENT_RESOLVES_INFINITY = createInstanceKey("rune_enchantment_resolves_infinity");
    public static final ResourceKey<GameTestInstance> RUNE_ENCHANTMENT_RESOLVES_MENDING = createInstanceKey("rune_enchantment_resolves_mending");
    public static final ResourceKey<GameTestInstance> RUNE_ENCHANTMENT_RESOLVES_LIFESTEAL = createInstanceKey("rune_enchantment_resolves_lifesteal");
    public static final ResourceKey<GameTestInstance> RUNE_ENCHANTMENT_RESOLVES_MANA_EFFICIENCY = createInstanceKey("rune_enchantment_resolves_mana_efficiency");
    public static final ResourceKey<GameTestInstance> RUNE_ENCHANTMENT_RESOLVES_AEGIS = createInstanceKey("rune_enchantment_resolves_aegis");
    public static final ResourceKey<GameTestInstance> RUNE_ENCHANTMENT_RESOLVES_LUCKY_STRIKE = createInstanceKey("rune_enchantment_resolves_lucky_strike");
    public static final ResourceKey<GameTestInstance> RUNE_ENCHANTMENT_REQUIRES_RESEARCH = createInstanceKey("rune_enchantment_requires_research");
    public static final ResourceKey<GameTestInstance> RUNE_ENCHANTMENT_REQUIRES_ENCHANTABLE_STACK = createInstanceKey("rune_enchantment_requires_enchantable_stack");
    public static final ResourceKey<GameTestInstance> RUNE_ENCHANTMENT_POWER_RUNE_ONE_RUNE_GIVES_LEVEL_2 = createInstanceKey("rune_enchantment_power_rune_one_rune_gives_level_2");
    public static final ResourceKey<GameTestInstance> RUNE_ENCHANTMENT_POWER_RUNE_TWO_RUNES_GIVES_LEVEL_3 = createInstanceKey("rune_enchantment_power_rune_two_runes_gives_level_3");
    public static final ResourceKey<GameTestInstance> RUNE_ENCHANTMENT_POWER_RUNE_THREE_RUNES_GIVES_LEVEL_4 = createInstanceKey("rune_enchantment_power_rune_three_runes_gives_level_4");
    public static final ResourceKey<GameTestInstance> RUNE_ENCHANTMENT_POWER_RUNE_CAPPED_AT_MAX_LEVEL = createInstanceKey("rune_enchantment_power_rune_capped_at_max_level");
    public static final ResourceKey<GameTestInstance> RUNE_ENCHANTMENT_COMPETING_UNFILTERED = createInstanceKey("rune_enchantment_competing_unfiltered");
    public static final ResourceKey<GameTestInstance> RUNE_ENCHANTMENT_COMPETING_FILTERED = createInstanceKey("rune_enchantment_competing_filtered");
    public static final ResourceKey<GameTestInstance> RUNE_ENCHANTMENT_TIE_BROKEN_BY_ID = createInstanceKey("rune_enchantment_tie_broken_by_id");
    public static final ResourceKey<GameTestInstance> RUNE_ENCHANTMENT_EMPTY_INPUTS = createInstanceKey("rune_enchantment_empty_inputs");
    public static final ResourceKey<GameTestInstance> RUNE_LIMITS_INSIGHT = createInstanceKey("rune_limits_insight");
    public static final ResourceKey<GameTestInstance> RUNE_LIMITS_POWER = createInstanceKey("rune_limits_power");
    public static final ResourceKey<GameTestInstance> RUNE_LIMITS_UNLIMITED_GRACE = createInstanceKey("rune_limits_unlimited_grace");
    public static final ResourceKey<GameTestInstance> RUNE_LIMITS_UNLIMITED_PROJECT = createInstanceKey("rune_limits_unlimited_project");
    public static final ResourceKey<GameTestInstance> RUNE_STACK_SET_AND_CLEAR = createInstanceKey("rune_stack_set_and_clear");
    public static final ResourceKey<GameTestInstance> RUNE_MERGE_ENCHANTMENTS_TAKES_STRONGER = createInstanceKey("rune_merge_enchantments_takes_stronger");
    public static final ResourceKey<GameTestInstance> RUNE_MERGE_ENCHANTMENTS_SKIPS_INCOMPATIBLE_WITH_ORIGINAL = createInstanceKey("rune_merge_enchantments_skips_incompatible_with_original");
    public static final ResourceKey<GameTestInstance> RUNE_MERGE_ENCHANTMENTS_ADDS_MUTUALLY_INCOMPATIBLE_ADDITIONS = createInstanceKey("rune_merge_enchantments_adds_mutually_incompatible_additions");
    public static final ResourceKey<GameTestInstance> RUNE_MERGE_ENCHANTMENTS_ADDS_COMPATIBLE = createInstanceKey("rune_merge_enchantments_adds_compatible");
    public static final ResourceKey<GameTestInstance> RUNE_DEFINITION_LOOKUP = createInstanceKey("rune_definition_lookup");
    public static final ResourceKey<GameTestInstance> RUNE_IS_KNOWN_VERB = createInstanceKey("rune_is_known_verb");
    public static final ResourceKey<GameTestInstance> RUNE_IS_KNOWN_NOUN = createInstanceKey("rune_is_known_noun");
    public static final ResourceKey<GameTestInstance> RUNE_IS_KNOWN_SOURCE = createInstanceKey("rune_is_known_source");
    public static final ResourceKey<GameTestInstance> RUNE_IS_KNOWN_FULL_KEY = createInstanceKey("rune_is_known_full_key");

    // Loot modifier tests
    public static final ResourceKey<GameTestInstance> LOOT_ADD_ITEM_ADDS_FIXED_ROLLS = createInstanceKey("loot_add_item_adds_fixed_rolls");
    public static final ResourceKey<GameTestInstance> LOOT_ADD_ITEM_ZERO_ROLLS_ADDS_NOTHING = createInstanceKey("loot_add_item_zero_rolls_adds_nothing");
    public static final ResourceKey<GameTestInstance> LOOT_REPLACE_ITEM_REPLACES_ALL_ENTRIES = createInstanceKey("loot_replace_item_replaces_all_entries");
    public static final ResourceKey<GameTestInstance> LOOT_BLOODY_FLESH_DROPS_FOR_TAGGED_ENTITY_VILLAGER = createInstanceKey("loot_bloody_flesh_drops_for_tagged_entity_villager");
    public static final ResourceKey<GameTestInstance> LOOT_BLOODY_FLESH_DROPS_FOR_TAGGED_ENTITY_WITCH = createInstanceKey("loot_bloody_flesh_drops_for_tagged_entity_witch");
    public static final ResourceKey<GameTestInstance> LOOT_BLOODY_FLESH_SKIPS_UNTAGGED_ENTITY = createInstanceKey("loot_bloody_flesh_skips_untagged_entity");
    public static final ResourceKey<GameTestInstance> LOOT_BLOOD_NOTES_DROPS_FOR_TAGGED_ENTITY_EVOKER = createInstanceKey("loot_blood_notes_drops_for_tagged_entity_evoker");
    public static final ResourceKey<GameTestInstance> LOOT_BLOOD_NOTES_DROPS_FOR_TAGGED_ENTITY_WITCH = createInstanceKey("loot_blood_notes_drops_for_tagged_entity_witch");
    public static final ResourceKey<GameTestInstance> LOOT_BLOOD_NOTES_SKIPS_UNTAGGED_ENTITY = createInstanceKey("loot_blood_notes_skips_untagged_entity");
    public static final ResourceKey<GameTestInstance> LOOT_BONUS_NUGGET_IRON_CHANCE_1 = createInstanceKey("loot_bonus_nugget_iron_chance_1");
    public static final ResourceKey<GameTestInstance> LOOT_BONUS_NUGGET_QUARTZ_CHANCE_1 = createInstanceKey("loot_bonus_nugget_quartz_chance_1");
    public static final ResourceKey<GameTestInstance> LOOT_BONUS_NUGGET_IRON_CHANCE_0 = createInstanceKey("loot_bonus_nugget_iron_chance_0");
    public static final ResourceKey<GameTestInstance> LOOT_BONUS_NUGGET_REQUIRES_LUCKY_STRIKE = createInstanceKey("loot_bonus_nugget_requires_lucky_strike");
    public static final ResourceKey<GameTestInstance> LOOT_BONUS_NUGGET_SKIPS_UNTAGGED_BLOCK = createInstanceKey("loot_bonus_nugget_skips_untagged_block");
    public static final ResourceKey<GameTestInstance> LOOT_BOUNTY_FARMING_CHANCE_1 = createInstanceKey("loot_bounty_farming_chance_1");
    public static final ResourceKey<GameTestInstance> LOOT_BOUNTY_FARMING_CHANCE_0 = createInstanceKey("loot_bounty_farming_chance_0");
    public static final ResourceKey<GameTestInstance> LOOT_BOUNTY_FISHING_CHANCE_1 = createInstanceKey("loot_bounty_fishing_chance_1");
    public static final ResourceKey<GameTestInstance> LOOT_BOUNTY_FISHING_CHANCE_0 = createInstanceKey("loot_bounty_fishing_chance_0");
    public static final ResourceKey<GameTestInstance> LOOT_GUILLOTINE_DROPS_HEAD_FOR_TAGGED_ENTITY_ZOMBIE = createInstanceKey("loot_guillotine_drops_head_for_tagged_entity_zombie");
    public static final ResourceKey<GameTestInstance> LOOT_GUILLOTINE_DROPS_HEAD_FOR_TAGGED_ENTITY_DROWNED = createInstanceKey("loot_guillotine_drops_head_for_tagged_entity_drowned");
    public static final ResourceKey<GameTestInstance> LOOT_GUILLOTINE_DROPS_HEAD_FOR_TAGGED_ENTITY_SKELETON = createInstanceKey("loot_guillotine_drops_head_for_tagged_entity_skeleton");
    public static final ResourceKey<GameTestInstance> LOOT_GUILLOTINE_DROPS_HEAD_FOR_TAGGED_ENTITY_CREEPER = createInstanceKey("loot_guillotine_drops_head_for_tagged_entity_creeper");
    public static final ResourceKey<GameTestInstance> LOOT_GUILLOTINE_DROPS_HEAD_SCALES_WITH_LEVEL = createInstanceKey("loot_guillotine_drops_head_scales_with_level");
    public static final ResourceKey<GameTestInstance> LOOT_GUILLOTINE_SKIPS_UNTAGGED_ENTITY = createInstanceKey("loot_guillotine_skips_untagged_entity");
    public static final ResourceKey<GameTestInstance> LOOT_GUILLOTINE_REQUIRES_ENCHANTMENT = createInstanceKey("loot_guillotine_requires_enchantment");
    public static final ResourceKey<GameTestInstance> LOOT_GUILLOTINE_SKIPS_EXISTING_HEAD = createInstanceKey("loot_guillotine_skips_existing_head");
    public static final ResourceKey<GameTestInstance> LOOT_RELIC_FRAGMENTS_DROPS_FOR_TAGGED_ENTITY_EVOKER = createInstanceKey("loot_relic_fragments_drops_for_tagged_entity_evoker");
    public static final ResourceKey<GameTestInstance> LOOT_RELIC_FRAGMENTS_DROPS_FOR_TAGGED_ENTITY_ZOMBIE = createInstanceKey("loot_relic_fragments_drops_for_tagged_entity_zombie");
    public static final ResourceKey<GameTestInstance> LOOT_RELIC_FRAGMENTS_SKIPS_UNTAGGED_ENTITY = createInstanceKey("loot_relic_fragments_skips_untagged_entity");
    public static final ResourceKey<GameTestInstance> LOOT_RELIC_FRAGMENTS_COUNT_WITHIN_RANGE = createInstanceKey("loot_relic_fragments_count_within_range");
    public static final ResourceKey<GameTestInstance> LOOT_ESSENCE_THIEF_REQUIRES_ENCHANTMENT = createInstanceKey("loot_essence_thief_requires_enchantment");

    // FTUX tests
    public static final ResourceKey<GameTestInstance> FONT_DISCOVERY_EARTH = createInstanceKey("font_discovery_earth");
    public static final ResourceKey<GameTestInstance> FONT_DISCOVERY_SEA = createInstanceKey("font_discovery_sea");
    public static final ResourceKey<GameTestInstance> FONT_DISCOVERY_SKY = createInstanceKey("font_discovery_sky");
    public static final ResourceKey<GameTestInstance> FONT_DISCOVERY_SUN = createInstanceKey("font_discovery_sun");
    public static final ResourceKey<GameTestInstance> FONT_DISCOVERY_MOON = createInstanceKey("font_discovery_moon");
    public static final ResourceKey<GameTestInstance> SLEEP_AFTER_SHRINE_GRANTS_DREAM = createInstanceKey("sleep_after_shrine_grants_dream");
    public static final ResourceKey<GameTestInstance> MUNDANE_WAND_CRAFTING_EARTH = createInstanceKey("mundane_wand_crafting_earth");
    public static final ResourceKey<GameTestInstance> MUNDANE_WAND_CRAFTING_SEA = createInstanceKey("mundane_wand_crafting_sea");
    public static final ResourceKey<GameTestInstance> MUNDANE_WAND_CRAFTING_SKY = createInstanceKey("mundane_wand_crafting_sky");
    public static final ResourceKey<GameTestInstance> MUNDANE_WAND_CRAFTING_SUN = createInstanceKey("mundane_wand_crafting_sun");
    public static final ResourceKey<GameTestInstance> MUNDANE_WAND_CRAFTING_MOON = createInstanceKey("mundane_wand_crafting_moon");
    public static final ResourceKey<GameTestInstance> TRANSFORM_ABORT_GIVES_HINT = createInstanceKey("transform_abort_gives_hint");
    public static final ResourceKey<GameTestInstance> TRANSFORM_WITHOUT_DREAM_DOES_NOTHING = createInstanceKey("transform_without_dream_does_nothing");
    public static final ResourceKey<GameTestInstance> TRANSFORM_GRIMOIRE = createInstanceKey("transform_grimoire");

    // Beeswax item tests
    public static final ResourceKey<GameTestInstance> APPLY_BEESWAX_DIRECTLY_CLEAN = createInstanceKey("apply_beeswax_directly_clean");
    public static final ResourceKey<GameTestInstance> APPLY_BEESWAX_DIRECTLY_EXPOSED = createInstanceKey("apply_beeswax_directly_exposed");
    public static final ResourceKey<GameTestInstance> APPLY_BEESWAX_DIRECTLY_WEATHERED = createInstanceKey("apply_beeswax_directly_weathered");
    public static final ResourceKey<GameTestInstance> APPLY_BEESWAX_DIRECTLY_OXIDIZED = createInstanceKey("apply_beeswax_directly_oxidized");
    public static final ResourceKey<GameTestInstance> APPLY_BEESWAX_VIA_CRAFTING_CLEAN = createInstanceKey("apply_beeswax_via_crafting_clean");
    public static final ResourceKey<GameTestInstance> APPLY_BEESWAX_VIA_CRAFTING_EXPOSED = createInstanceKey("apply_beeswax_via_crafting_exposed");
    public static final ResourceKey<GameTestInstance> APPLY_BEESWAX_VIA_CRAFTING_WEATHERED = createInstanceKey("apply_beeswax_via_crafting_weathered");
    public static final ResourceKey<GameTestInstance> APPLY_BEESWAX_VIA_CRAFTING_OXIDIZED = createInstanceKey("apply_beeswax_via_crafting_oxidized");

    // Dispenser item tests
    public static final ResourceKey<GameTestInstance> MANA_ARROWS_FIRED_FROM_DISPENSER_EARTH = createInstanceKey("mana_arrows_fired_from_dispenser_earth");
    public static final ResourceKey<GameTestInstance> MANA_ARROWS_FIRED_FROM_DISPENSER_SEA = createInstanceKey("mana_arrows_fired_from_dispenser_sea");
    public static final ResourceKey<GameTestInstance> MANA_ARROWS_FIRED_FROM_DISPENSER_SKY = createInstanceKey("mana_arrows_fired_from_dispenser_sky");
    public static final ResourceKey<GameTestInstance> MANA_ARROWS_FIRED_FROM_DISPENSER_SUN = createInstanceKey("mana_arrows_fired_from_dispenser_sun");
    public static final ResourceKey<GameTestInstance> MANA_ARROWS_FIRED_FROM_DISPENSER_MOON = createInstanceKey("mana_arrows_fired_from_dispenser_moon");
    public static final ResourceKey<GameTestInstance> MANA_ARROWS_FIRED_FROM_DISPENSER_BLOOD = createInstanceKey("mana_arrows_fired_from_dispenser_blood");
    public static final ResourceKey<GameTestInstance> MANA_ARROWS_FIRED_FROM_DISPENSER_INFERNAL = createInstanceKey("mana_arrows_fired_from_dispenser_infernal");
    public static final ResourceKey<GameTestInstance> MANA_ARROWS_FIRED_FROM_DISPENSER_VOID = createInstanceKey("mana_arrows_fired_from_dispenser_void");
    public static final ResourceKey<GameTestInstance> MANA_ARROWS_FIRED_FROM_DISPENSER_HALLOWED = createInstanceKey("mana_arrows_fired_from_dispenser_hallowed");

    // Essence tests
    public static final ResourceKey<GameTestInstance> ESSENCE_ITEM_LOOKUP_DUST = createInstanceKey("essence_item_lookup_dust");
    public static final ResourceKey<GameTestInstance> ESSENCE_ITEM_LOOKUP_SHARD = createInstanceKey("essence_item_lookup_shard");
    public static final ResourceKey<GameTestInstance> ESSENCE_ITEM_LOOKUP_CRYSTAL = createInstanceKey("essence_item_lookup_crystal");
    public static final ResourceKey<GameTestInstance> ESSENCE_ITEM_LOOKUP_CLUSTER = createInstanceKey("essence_item_lookup_cluster");
    public static final ResourceKey<GameTestInstance> ESSENCE_STACK_LOOKUP = createInstanceKey("essence_stack_lookup");
    public static final ResourceKey<GameTestInstance> ESSENCE_ALL_ESSENCES_COUNT = createInstanceKey("essence_all_essences_count");
    public static final ResourceKey<GameTestInstance> ESSENCE_TYPE_MANA_EQUIVALENT = createInstanceKey("essence_type_mana_equivalent");
    public static final ResourceKey<GameTestInstance> ESSENCE_TYPE_AFFINITY = createInstanceKey("essence_type_affinity");
    public static final ResourceKey<GameTestInstance> ESSENCE_TYPE_UPGRADE_CHAIN = createInstanceKey("essence_type_upgrade_chain");
    public static final ResourceKey<GameTestInstance> ESSENCE_TYPE_UPGRADE_MEDIUM = createInstanceKey("essence_type_upgrade_medium");
    public static final ResourceKey<GameTestInstance> ESSENCE_TYPE_UPGRADE_RESEARCH = createInstanceKey("essence_type_upgrade_research");
    public static final ResourceKey<GameTestInstance> ESSENCE_TYPE_DISCOVERY_DUST = createInstanceKey("essence_type_discovery_dust");
    public static final ResourceKey<GameTestInstance> ESSENCE_TYPE_DISCOVERY_SHARD = createInstanceKey("essence_type_discovery_shard");
    public static final ResourceKey<GameTestInstance> ESSENCE_TYPE_DISCOVERY_CRYSTAL = createInstanceKey("essence_type_discovery_crystal");
    public static final ResourceKey<GameTestInstance> ESSENCE_TYPE_DISCOVERY_CLUSTER = createInstanceKey("essence_type_discovery_cluster");
    public static final ResourceKey<GameTestInstance> SOURCE_DISCOVERY_BLOOD = createInstanceKey("source_discovery_blood");
    public static final ResourceKey<GameTestInstance> SOURCE_DISCOVERY_INFERNAL = createInstanceKey("source_discovery_infernal");
    public static final ResourceKey<GameTestInstance> SOURCE_DISCOVERY_VOID = createInstanceKey("source_discovery_void");
    public static final ResourceKey<GameTestInstance> SOURCE_DISCOVERY_HALLOWED = createInstanceKey("source_discovery_hallowed");
    public static final ResourceKey<GameTestInstance> SOURCE_DISCOVERY_PRIMAL_SOURCES_ALWAYS_DISCOVERED = createInstanceKey("source_discovery_primal_sources_always_discovered");

    // Concoction tests
    public static final ResourceKey<GameTestInstance> CONCOCTION_TYPE_ROUND_TRIP_WATER = createInstanceKey("concoction_type_round_trip_water");
    public static final ResourceKey<GameTestInstance> CONCOCTION_TYPE_ROUND_TRIP_TINCTURE = createInstanceKey("concoction_type_round_trip_tincture");
    public static final ResourceKey<GameTestInstance> CONCOCTION_TYPE_ROUND_TRIP_PHILTER = createInstanceKey("concoction_type_round_trip_philter");
    public static final ResourceKey<GameTestInstance> CONCOCTION_TYPE_ROUND_TRIP_ELIXIR = createInstanceKey("concoction_type_round_trip_elixir");
    public static final ResourceKey<GameTestInstance> CONCOCTION_TYPE_ROUND_TRIP_BOMB = createInstanceKey("concoction_type_round_trip_bomb");
    public static final ResourceKey<GameTestInstance> CONCOCTION_DOSES_ROUND_TRIP = createInstanceKey("concoction_doses_round_trip");
    public static final ResourceKey<GameTestInstance> CONCOCTION_DOSES_NEGATIVE_STORED_AS_IS = createInstanceKey("concoction_doses_negative_stored_as_is");
    public static final ResourceKey<GameTestInstance> CONCOCTION_BOMB_FUSE_ROUND_TRIP_IMPACT = createInstanceKey("concoction_bomb_fuse_round_trip_impact");
    public static final ResourceKey<GameTestInstance> CONCOCTION_BOMB_FUSE_ROUND_TRIP_SHORT = createInstanceKey("concoction_bomb_fuse_round_trip_short");
    public static final ResourceKey<GameTestInstance> CONCOCTION_BOMB_FUSE_ROUND_TRIP_MEDIUM = createInstanceKey("concoction_bomb_fuse_round_trip_medium");
    public static final ResourceKey<GameTestInstance> CONCOCTION_BOMB_FUSE_ROUND_TRIP_LONG = createInstanceKey("concoction_bomb_fuse_round_trip_long");
    public static final ResourceKey<GameTestInstance> CONCOCTION_BOMB_DEFAULT_FUSE = createInstanceKey("concoction_bomb_default_fuse");
    public static final ResourceKey<GameTestInstance> CONCOCTION_IS_BOMB = createInstanceKey("concoction_is_bomb");
    public static final ResourceKey<GameTestInstance> CONCOCTION_HAS_BENEFICIAL_EFFECT = createInstanceKey("concoction_has_beneficial_effect");
    public static final ResourceKey<GameTestInstance> CONCOCTION_DEFAULTS_ON_NON_CONCOCTION = createInstanceKey("concoction_defaults_on_non_concoction");

    // Wand mana tests
    public static final ResourceKey<GameTestInstance> WAND_CAN_GET_AND_ADD_MANA = createInstanceKey("wand_can_get_and_add_mana");
    public static final ResourceKey<GameTestInstance> WAND_CAN_GET_AND_ADD_REAL_MANA = createInstanceKey("wand_can_get_and_add_real_mana");
    public static final ResourceKey<GameTestInstance> WAND_MANA_CHANGE_DOES_NOT_MUTATE_STACK_COPIES = createInstanceKey("wand_mana_change_does_not_mutate_stack_copies");
    public static final ResourceKey<GameTestInstance> WAND_CANNOT_ADD_TOO_MUCH_MANA = createInstanceKey("wand_cannot_add_too_much_mana");
    public static final ResourceKey<GameTestInstance> WAND_CAN_GET_ALL_MANA = createInstanceKey("wand_can_get_all_mana");
    public static final ResourceKey<GameTestInstance> WAND_CAN_CONSUME_MANA = createInstanceKey("wand_can_consume_mana");
    public static final ResourceKey<GameTestInstance> WAND_CANNOT_CONSUME_MORE_MANA_THAN_IT_HAS = createInstanceKey("wand_cannot_consume_more_mana_than_it_has");
    public static final ResourceKey<GameTestInstance> WAND_CAN_CONSUME_MULTIPLE_TYPES_OF_MANA = createInstanceKey("wand_can_consume_multiple_types_of_mana");
    public static final ResourceKey<GameTestInstance> WAND_CANNOT_CONSUME_MORE_MANA_THAN_IT_HAS_WITH_MULTIPLE_TYPES = createInstanceKey("wand_cannot_consume_more_mana_than_it_has_with_multiple_types");
    public static final ResourceKey<GameTestInstance> WAND_CAN_REMOVE_MANA_RAW = createInstanceKey("wand_can_remove_mana_raw");
    public static final ResourceKey<GameTestInstance> WAND_CANNOT_REMOVE_MORE_RAW_MANA_THAN_IT_HAS = createInstanceKey("wand_cannot_remove_more_raw_mana_than_it_has");
    public static final ResourceKey<GameTestInstance> WAND_CONTAINS_MANA = createInstanceKey("wand_contains_mana");
    public static final ResourceKey<GameTestInstance> WAND_CONTAINS_MANA_LIST = createInstanceKey("wand_contains_mana_list");
    public static final ResourceKey<GameTestInstance> WAND_CONTAINS_MANA_RAW = createInstanceKey("wand_contains_mana_raw");
    public static final ResourceKey<GameTestInstance> WAND_CAN_GET_AND_ADD_MANA_SEA = createInstanceKey("wand_can_get_and_add_mana_sea");
    public static final ResourceKey<GameTestInstance> WAND_CAN_GET_AND_ADD_MANA_SKY = createInstanceKey("wand_can_get_and_add_mana_sky");
    public static final ResourceKey<GameTestInstance> WAND_CAN_GET_AND_ADD_MANA_SUN = createInstanceKey("wand_can_get_and_add_mana_sun");
    public static final ResourceKey<GameTestInstance> WAND_CAN_GET_AND_ADD_MANA_MOON = createInstanceKey("wand_can_get_and_add_mana_moon");
    public static final ResourceKey<GameTestInstance> WAND_CAN_GET_AND_ADD_MANA_BLOOD = createInstanceKey("wand_can_get_and_add_mana_blood");
    public static final ResourceKey<GameTestInstance> WAND_CAN_GET_AND_ADD_MANA_INFERNAL = createInstanceKey("wand_can_get_and_add_mana_infernal");
    public static final ResourceKey<GameTestInstance> WAND_CAN_GET_AND_ADD_MANA_VOID = createInstanceKey("wand_can_get_and_add_mana_void");
    public static final ResourceKey<GameTestInstance> WAND_CAN_GET_AND_ADD_MANA_HALLOWED = createInstanceKey("wand_can_get_and_add_mana_hallowed");
    public static final ResourceKey<GameTestInstance> WAND_CANNOT_ADD_TOO_MUCH_MANA_SEA = createInstanceKey("wand_cannot_add_too_much_mana_sea");
    public static final ResourceKey<GameTestInstance> WAND_CANNOT_ADD_TOO_MUCH_MANA_SKY = createInstanceKey("wand_cannot_add_too_much_mana_sky");
    public static final ResourceKey<GameTestInstance> WAND_CANNOT_ADD_TOO_MUCH_MANA_SUN = createInstanceKey("wand_cannot_add_too_much_mana_sun");
    public static final ResourceKey<GameTestInstance> WAND_CANNOT_ADD_TOO_MUCH_MANA_MOON = createInstanceKey("wand_cannot_add_too_much_mana_moon");
    public static final ResourceKey<GameTestInstance> WAND_CANNOT_ADD_TOO_MUCH_MANA_BLOOD = createInstanceKey("wand_cannot_add_too_much_mana_blood");
    public static final ResourceKey<GameTestInstance> WAND_CANNOT_ADD_TOO_MUCH_MANA_INFERNAL = createInstanceKey("wand_cannot_add_too_much_mana_infernal");
    public static final ResourceKey<GameTestInstance> WAND_CANNOT_ADD_TOO_MUCH_MANA_VOID = createInstanceKey("wand_cannot_add_too_much_mana_void");
    public static final ResourceKey<GameTestInstance> WAND_CANNOT_ADD_TOO_MUCH_MANA_HALLOWED = createInstanceKey("wand_cannot_add_too_much_mana_hallowed");
    public static final ResourceKey<GameTestInstance> WAND_CAN_CONSUME_MANA_SEA = createInstanceKey("wand_can_consume_mana_sea");
    public static final ResourceKey<GameTestInstance> WAND_CAN_CONSUME_MANA_SKY = createInstanceKey("wand_can_consume_mana_sky");
    public static final ResourceKey<GameTestInstance> WAND_CAN_CONSUME_MANA_SUN = createInstanceKey("wand_can_consume_mana_sun");
    public static final ResourceKey<GameTestInstance> WAND_CAN_CONSUME_MANA_MOON = createInstanceKey("wand_can_consume_mana_moon");
    public static final ResourceKey<GameTestInstance> WAND_CAN_CONSUME_MANA_BLOOD = createInstanceKey("wand_can_consume_mana_blood");
    public static final ResourceKey<GameTestInstance> WAND_CAN_CONSUME_MANA_INFERNAL = createInstanceKey("wand_can_consume_mana_infernal");
    public static final ResourceKey<GameTestInstance> WAND_CAN_CONSUME_MANA_VOID = createInstanceKey("wand_can_consume_mana_void");
    public static final ResourceKey<GameTestInstance> WAND_CAN_CONSUME_MANA_HALLOWED = createInstanceKey("wand_can_consume_mana_hallowed");
    public static final ResourceKey<GameTestInstance> WAND_CANNOT_CONSUME_MORE_MANA_THAN_IT_HAS_SEA = createInstanceKey("wand_cannot_consume_more_mana_than_it_has_sea");
    public static final ResourceKey<GameTestInstance> WAND_CANNOT_CONSUME_MORE_MANA_THAN_IT_HAS_SKY = createInstanceKey("wand_cannot_consume_more_mana_than_it_has_sky");
    public static final ResourceKey<GameTestInstance> WAND_CANNOT_CONSUME_MORE_MANA_THAN_IT_HAS_SUN = createInstanceKey("wand_cannot_consume_more_mana_than_it_has_sun");
    public static final ResourceKey<GameTestInstance> WAND_CANNOT_CONSUME_MORE_MANA_THAN_IT_HAS_MOON = createInstanceKey("wand_cannot_consume_more_mana_than_it_has_moon");
    public static final ResourceKey<GameTestInstance> WAND_CANNOT_CONSUME_MORE_MANA_THAN_IT_HAS_BLOOD = createInstanceKey("wand_cannot_consume_more_mana_than_it_has_blood");
    public static final ResourceKey<GameTestInstance> WAND_CANNOT_CONSUME_MORE_MANA_THAN_IT_HAS_INFERNAL = createInstanceKey("wand_cannot_consume_more_mana_than_it_has_infernal");
    public static final ResourceKey<GameTestInstance> WAND_CANNOT_CONSUME_MORE_MANA_THAN_IT_HAS_VOID = createInstanceKey("wand_cannot_consume_more_mana_than_it_has_void");
    public static final ResourceKey<GameTestInstance> WAND_CANNOT_CONSUME_MORE_MANA_THAN_IT_HAS_HALLOWED = createInstanceKey("wand_cannot_consume_more_mana_than_it_has_hallowed");
    public static final ResourceKey<GameTestInstance> WAND_CAN_GET_AND_ADD_MANA_MUNDANE_WAND = createInstanceKey("wand_can_get_and_add_mana_mundane_wand");
    public static final ResourceKey<GameTestInstance> WAND_CAN_GET_AND_ADD_MANA_MODULAR_STAFF = createInstanceKey("wand_can_get_and_add_mana_modular_staff");
    public static final ResourceKey<GameTestInstance> WAND_CAN_CONSUME_MANA_MUNDANE_WAND = createInstanceKey("wand_can_consume_mana_mundane_wand");
    public static final ResourceKey<GameTestInstance> WAND_CAN_CONSUME_MANA_MODULAR_STAFF = createInstanceKey("wand_can_consume_mana_modular_staff");

    // Warding module tests
    public static final ResourceKey<GameTestInstance> WARD_REGENERATION_DOES_NOT_MUTATE_STACK_COPIES = createInstanceKey("ward_regeneration_does_not_mutate_stack_copies");

    // Wand component tests
    public static final ResourceKey<GameTestInstance> WAND_GEM_SETS_MAX_MANA_APPRENTICE = createInstanceKey("wand_gem_sets_max_mana_apprentice");
    public static final ResourceKey<GameTestInstance> WAND_GEM_SETS_MAX_MANA_ADEPT = createInstanceKey("wand_gem_sets_max_mana_adept");
    public static final ResourceKey<GameTestInstance> WAND_GEM_SETS_MAX_MANA_WIZARD = createInstanceKey("wand_gem_sets_max_mana_wizard");
    public static final ResourceKey<GameTestInstance> WAND_GEM_SETS_MAX_MANA_ARCHMAGE = createInstanceKey("wand_gem_sets_max_mana_archmage");
    public static final ResourceKey<GameTestInstance> WAND_GEM_CREATIVE_HAS_INFINITE_MANA = createInstanceKey("wand_gem_creative_has_infinite_mana");
    public static final ResourceKey<GameTestInstance> WAND_CAP_SETS_BASE_COST_MODIFIER_IRON = createInstanceKey("wand_cap_sets_base_cost_modifier_iron");
    public static final ResourceKey<GameTestInstance> WAND_CAP_SETS_BASE_COST_MODIFIER_GOLD = createInstanceKey("wand_cap_sets_base_cost_modifier_gold");
    public static final ResourceKey<GameTestInstance> WAND_CAP_SETS_BASE_COST_MODIFIER_PRIMALITE = createInstanceKey("wand_cap_sets_base_cost_modifier_primalite");
    public static final ResourceKey<GameTestInstance> WAND_CAP_SETS_BASE_COST_MODIFIER_HEXIUM = createInstanceKey("wand_cap_sets_base_cost_modifier_hexium");
    public static final ResourceKey<GameTestInstance> WAND_CAP_SETS_BASE_COST_MODIFIER_HALLOWSTEEL = createInstanceKey("wand_cap_sets_base_cost_modifier_hallowsteel");
    public static final ResourceKey<GameTestInstance> WAND_CAP_SETS_SIPHON_AMOUNT_IRON = createInstanceKey("wand_cap_sets_siphon_amount_iron");
    public static final ResourceKey<GameTestInstance> WAND_CAP_SETS_SIPHON_AMOUNT_GOLD = createInstanceKey("wand_cap_sets_siphon_amount_gold");
    public static final ResourceKey<GameTestInstance> WAND_CAP_SETS_SIPHON_AMOUNT_PRIMALITE = createInstanceKey("wand_cap_sets_siphon_amount_primalite");
    public static final ResourceKey<GameTestInstance> WAND_CAP_SETS_SIPHON_AMOUNT_HEXIUM = createInstanceKey("wand_cap_sets_siphon_amount_hexium");
    public static final ResourceKey<GameTestInstance> WAND_CAP_SETS_SIPHON_AMOUNT_HALLOWSTEEL = createInstanceKey("wand_cap_sets_siphon_amount_hallowsteel");
    public static final ResourceKey<GameTestInstance> WAND_CORE_SPELL_SLOTS_HEARTWOOD = createInstanceKey("wand_core_spell_slots_heartwood");
    public static final ResourceKey<GameTestInstance> WAND_CORE_SPELL_SLOTS_OBSIDIAN = createInstanceKey("wand_core_spell_slots_obsidian");
    public static final ResourceKey<GameTestInstance> WAND_CORE_SPELL_SLOTS_BONE = createInstanceKey("wand_core_spell_slots_bone");
    public static final ResourceKey<GameTestInstance> WAND_CORE_SPELL_SLOTS_PRIMAL = createInstanceKey("wand_core_spell_slots_primal");
    public static final ResourceKey<GameTestInstance> WAND_CORE_SPELL_SLOTS_DARK_PRIMAL = createInstanceKey("wand_core_spell_slots_dark_primal");
    public static final ResourceKey<GameTestInstance> WAND_CORE_SPELL_SLOTS_PURE_PRIMAL = createInstanceKey("wand_core_spell_slots_pure_primal");
    public static final ResourceKey<GameTestInstance> WAND_CORE_BONUS_SLOT_ACCEPTS_MATCHING_SPELL_OBSIDIAN = createInstanceKey("wand_core_bonus_slot_accepts_matching_spell_obsidian");
    public static final ResourceKey<GameTestInstance> WAND_CORE_BONUS_SLOT_ACCEPTS_MATCHING_SPELL_BONE = createInstanceKey("wand_core_bonus_slot_accepts_matching_spell_bone");
    public static final ResourceKey<GameTestInstance> WAND_CORE_ALIGNED_SOURCES_HEARTWOOD = createInstanceKey("wand_core_aligned_sources_heartwood");
    public static final ResourceKey<GameTestInstance> WAND_CORE_ALIGNED_SOURCES_BONE = createInstanceKey("wand_core_aligned_sources_bone");
    public static final ResourceKey<GameTestInstance> WAND_CORE_ALIGNED_SOURCES_DARK_PRIMAL = createInstanceKey("wand_core_aligned_sources_dark_primal");
    public static final ResourceKey<GameTestInstance> STAFF_GEM_SETS_MAX_MANA_WIZARD = createInstanceKey("staff_gem_sets_max_mana_wizard");
    public static final ResourceKey<GameTestInstance> STAFF_CAP_SETS_BASE_COST_MODIFIER_HEXIUM = createInstanceKey("staff_cap_sets_base_cost_modifier_hexium");
    public static final ResourceKey<GameTestInstance> STAFF_CAP_SETS_SIPHON_AMOUNT_HEXIUM = createInstanceKey("staff_cap_sets_siphon_amount_hexium");
    public static final ResourceKey<GameTestInstance> STAFF_CORE_SPELL_SLOTS_PRIMAL = createInstanceKey("staff_core_spell_slots_primal");

    // Research key tests
    public static final ResourceKey<GameTestInstance> RESEARCH_KEY_DISCIPLINE = createInstanceKey("research_key_discipline");
    public static final ResourceKey<GameTestInstance> RESEARCH_KEY_ENTRY = createInstanceKey("research_key_entry");
    public static final ResourceKey<GameTestInstance> RESEARCH_KEY_STAGE = createInstanceKey("research_key_stage");
    public static final ResourceKey<GameTestInstance> RESEARCH_KEY_ITEM_SCAN = createInstanceKey("research_key_item_scan");
    public static final ResourceKey<GameTestInstance> RESEARCH_KEY_ENTITY_SCAN = createInstanceKey("research_key_entity_scan");
    public static final ResourceKey<GameTestInstance> RESEARCH_KEY_STACK_CRAFTED = createInstanceKey("research_key_stack_crafted");
    public static final ResourceKey<GameTestInstance> RESEARCH_KEY_TAG_CRAFTED = createInstanceKey("research_key_tag_crafted");
    public static final ResourceKey<GameTestInstance> RESEARCH_KEY_RUNE_ENCHANTMENT = createInstanceKey("research_key_rune_enchantment");
    public static final ResourceKey<GameTestInstance> RESEARCH_KEY_RUNE_ENCHANTMENT_PARTIAL = createInstanceKey("research_key_rune_enchantment_partial");

    // Research requirement tests
    public static final ResourceKey<GameTestInstance> RESEARCH_REQUIREMENT_RESEARCH = createInstanceKey("research_requirement_research");
    public static final ResourceKey<GameTestInstance> RESEARCH_REQUIREMENT_KNOWLEDGE = createInstanceKey("research_requirement_knowledge");
    public static final ResourceKey<GameTestInstance> RESEARCH_REQUIREMENT_ITEM_STACK = createInstanceKey("research_requirement_item_stack");
    public static final ResourceKey<GameTestInstance> RESEARCH_REQUIREMENT_ITEM_TAG = createInstanceKey("research_requirement_item_tag");
    public static final ResourceKey<GameTestInstance> RESEARCH_REQUIREMENT_STAT = createInstanceKey("research_requirement_stat");
    public static final ResourceKey<GameTestInstance> RESEARCH_REQUIREMENT_EXPERTISE = createInstanceKey("research_requirement_expertise");
    public static final ResourceKey<GameTestInstance> RESEARCH_REQUIREMENT_VANILLA_ITEM_USED_STAT = createInstanceKey("research_requirement_vanilla_item_used_stat");
    public static final ResourceKey<GameTestInstance> RESEARCH_REQUIREMENT_VANILLA_CUSTOM_STAT = createInstanceKey("research_requirement_vanilla_custom_stat");
    public static final ResourceKey<GameTestInstance> RESEARCH_REQUIREMENT_AND = createInstanceKey("research_requirement_and");
    public static final ResourceKey<GameTestInstance> RESEARCH_REQUIREMENT_OR = createInstanceKey("research_requirement_or");
    public static final ResourceKey<GameTestInstance> RESEARCH_REQUIREMENT_QUORUM = createInstanceKey("research_requirement_quorum");

    // Research system tests
    public static final ResourceKey<GameTestInstance> RESEARCH_GRANT_WORKS = createInstanceKey("research_grant_works");

    // Spell tests
    public static final ResourceKey<GameTestInstance> DAMAGE_SPELLS_WORK_EARTH = createInstanceKey("damage_spells_work_earth");
    public static final ResourceKey<GameTestInstance> DAMAGE_SPELLS_WORK_SEA = createInstanceKey("damage_spells_work_sea");
    public static final ResourceKey<GameTestInstance> DAMAGE_SPELLS_WORK_SKY = createInstanceKey("damage_spells_work_sky");
    public static final ResourceKey<GameTestInstance> DAMAGE_SPELLS_WORK_SUN = createInstanceKey("damage_spells_work_sun");
    public static final ResourceKey<GameTestInstance> DAMAGE_SPELLS_WORK_MOON = createInstanceKey("damage_spells_work_moon");
    public static final ResourceKey<GameTestInstance> DAMAGE_SPELLS_WORK_BLOOD = createInstanceKey("damage_spells_work_blood");
    public static final ResourceKey<GameTestInstance> DAMAGE_SPELLS_WORK_INFERNAL = createInstanceKey("damage_spells_work_infernal");
    public static final ResourceKey<GameTestInstance> DAMAGE_SPELLS_WORK_VOID = createInstanceKey("damage_spells_work_void");
    public static final ResourceKey<GameTestInstance> DAMAGE_SPELLS_WORK_HALLOWED = createInstanceKey("damage_spells_work_hallowed");
    public static final ResourceKey<GameTestInstance> SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_EARTH_DAMAGE = createInstanceKey("spell_mana_cost_isolated_to_payload_source_earth_damage");
    public static final ResourceKey<GameTestInstance> SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_FROST_DAMAGE = createInstanceKey("spell_mana_cost_isolated_to_payload_source_frost_damage");
    public static final ResourceKey<GameTestInstance> SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_LIGHTNING_DAMAGE = createInstanceKey("spell_mana_cost_isolated_to_payload_source_lightning_damage");
    public static final ResourceKey<GameTestInstance> SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_SOLAR_DAMAGE = createInstanceKey("spell_mana_cost_isolated_to_payload_source_solar_damage");
    public static final ResourceKey<GameTestInstance> SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_LUNAR_DAMAGE = createInstanceKey("spell_mana_cost_isolated_to_payload_source_lunar_damage");
    public static final ResourceKey<GameTestInstance> SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_BLOOD_DAMAGE = createInstanceKey("spell_mana_cost_isolated_to_payload_source_blood_damage");
    public static final ResourceKey<GameTestInstance> SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_FLAME_DAMAGE = createInstanceKey("spell_mana_cost_isolated_to_payload_source_flame_damage");
    public static final ResourceKey<GameTestInstance> SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_VOID_DAMAGE = createInstanceKey("spell_mana_cost_isolated_to_payload_source_void_damage");
    public static final ResourceKey<GameTestInstance> SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_HOLY_DAMAGE = createInstanceKey("spell_mana_cost_isolated_to_payload_source_holy_damage");
    public static final ResourceKey<GameTestInstance> SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_BREAK = createInstanceKey("spell_mana_cost_isolated_to_payload_source_break");
    public static final ResourceKey<GameTestInstance> SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_CONJURE_STONE = createInstanceKey("spell_mana_cost_isolated_to_payload_source_conjure_stone");
    public static final ResourceKey<GameTestInstance> SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_CONJURE_WATER = createInstanceKey("spell_mana_cost_isolated_to_payload_source_conjure_water");
    public static final ResourceKey<GameTestInstance> SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_SHEAR = createInstanceKey("spell_mana_cost_isolated_to_payload_source_shear");
    public static final ResourceKey<GameTestInstance> SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_FLIGHT = createInstanceKey("spell_mana_cost_isolated_to_payload_source_flight");
    public static final ResourceKey<GameTestInstance> SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_HEALING = createInstanceKey("spell_mana_cost_isolated_to_payload_source_healing");
    public static final ResourceKey<GameTestInstance> SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_CONJURE_LIGHT = createInstanceKey("spell_mana_cost_isolated_to_payload_source_conjure_light");
    public static final ResourceKey<GameTestInstance> SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_POLYMORPH_WOLF = createInstanceKey("spell_mana_cost_isolated_to_payload_source_polymorph_wolf");
    public static final ResourceKey<GameTestInstance> SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_POLYMORPH_SHEEP = createInstanceKey("spell_mana_cost_isolated_to_payload_source_polymorph_sheep");
    public static final ResourceKey<GameTestInstance> SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_CONJURE_ANIMAL = createInstanceKey("spell_mana_cost_isolated_to_payload_source_conjure_animal");
    public static final ResourceKey<GameTestInstance> SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_CONJURE_LAVA = createInstanceKey("spell_mana_cost_isolated_to_payload_source_conjure_lava");
    public static final ResourceKey<GameTestInstance> SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_DRAIN_SOUL = createInstanceKey("spell_mana_cost_isolated_to_payload_source_drain_soul");
    public static final ResourceKey<GameTestInstance> SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_TELEPORT = createInstanceKey("spell_mana_cost_isolated_to_payload_source_teleport");
    public static final ResourceKey<GameTestInstance> SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_CONSECRATE = createInstanceKey("spell_mana_cost_isolated_to_payload_source_consecrate");
    public static final ResourceKey<GameTestInstance> SPELL_MANA_COST_EARTH_DAMAGE_POWER_1 = createInstanceKey("spell_mana_cost_earth_damage_power_1");
    public static final ResourceKey<GameTestInstance> SPELL_MANA_COST_EARTH_DAMAGE_POWER_5 = createInstanceKey("spell_mana_cost_earth_damage_power_5");
    public static final ResourceKey<GameTestInstance> SPELL_MANA_COST_FLAME_DAMAGE_POWER_3_DURATION_2 = createInstanceKey("spell_mana_cost_flame_damage_power_3_duration_2");
    public static final ResourceKey<GameTestInstance> SPELL_MANA_COST_BREAK_POWER_2_SILK_TOUCH = createInstanceKey("spell_mana_cost_break_power_2_silk_touch");
    public static final ResourceKey<GameTestInstance> SPELL_MANA_COST_VEHICLE_SELF = createInstanceKey("spell_mana_cost_vehicle_self");
    public static final ResourceKey<GameTestInstance> SPELL_MANA_COST_VEHICLE_BOLT = createInstanceKey("spell_mana_cost_vehicle_bolt");
    public static final ResourceKey<GameTestInstance> SPELL_MANA_COST_VEHICLE_PROJECTILE = createInstanceKey("spell_mana_cost_vehicle_projectile");
    public static final ResourceKey<GameTestInstance> SPELL_MANA_COST_MOD_AMPLIFY = createInstanceKey("spell_mana_cost_mod_amplify");
    public static final ResourceKey<GameTestInstance> SPELL_MANA_COST_MOD_BURST = createInstanceKey("spell_mana_cost_mod_burst");
    public static final ResourceKey<GameTestInstance> SPELL_MANA_COST_MOD_FORK = createInstanceKey("spell_mana_cost_mod_fork");
    public static final ResourceKey<GameTestInstance> SPELL_MANA_COST_MOD_MINE = createInstanceKey("spell_mana_cost_mod_mine");
    public static final ResourceKey<GameTestInstance> SPELL_MANA_COST_MOD_QUICKEN = createInstanceKey("spell_mana_cost_mod_quicken");
    public static final ResourceKey<GameTestInstance> SPELL_MANA_COST_MOD_ORDER_INDEPENDENT = createInstanceKey("spell_mana_cost_mod_order_independent");
    public static final ResourceKey<GameTestInstance> SPELL_COOLDOWN_BASE = createInstanceKey("spell_cooldown_base");
    public static final ResourceKey<GameTestInstance> SPELL_COOLDOWN_QUICKEN_HASTE_1 = createInstanceKey("spell_cooldown_quicken_haste_1");
    public static final ResourceKey<GameTestInstance> SPELL_COOLDOWN_QUICKEN_HASTE_2 = createInstanceKey("spell_cooldown_quicken_haste_2");
    public static final ResourceKey<GameTestInstance> SPELL_COOLDOWN_QUICKEN_HASTE_3 = createInstanceKey("spell_cooldown_quicken_haste_3");
    public static final ResourceKey<GameTestInstance> SPELL_COOLDOWN_QUICKEN_HASTE_4 = createInstanceKey("spell_cooldown_quicken_haste_4");
    public static final ResourceKey<GameTestInstance> SPELL_COOLDOWN_QUICKEN_HASTE_5 = createInstanceKey("spell_cooldown_quicken_haste_5");
    public static final ResourceKey<GameTestInstance> SPELL_COOLDOWN_QUICKEN_HASTE_6 = createInstanceKey("spell_cooldown_quicken_haste_6");
    public static final ResourceKey<GameTestInstance> SPELL_COOLDOWN_QUICKEN_HASTE_7 = createInstanceKey("spell_cooldown_quicken_haste_7");
    public static final ResourceKey<GameTestInstance> SPELL_PACKAGE_NBT_ROUNDTRIP = createInstanceKey("spell_package_nbt_roundtrip");
    public static final ResourceKey<GameTestInstance> SPELL_ACTIVE_MOD_COUNT_0 = createInstanceKey("spell_active_mod_count_0");
    public static final ResourceKey<GameTestInstance> SPELL_ACTIVE_MOD_COUNT_1 = createInstanceKey("spell_active_mod_count_1");
    public static final ResourceKey<GameTestInstance> SPELL_ACTIVE_MOD_COUNT_2 = createInstanceKey("spell_active_mod_count_2");
    public static final ResourceKey<GameTestInstance> SPELL_ACTIVE_MOD_COUNT_IGNORES_EMPTY_MOD = createInstanceKey("spell_active_mod_count_ignores_empty_mod");
    public static final ResourceKey<GameTestInstance> SPELL_IS_VALID_COMPLETE = createInstanceKey("spell_is_valid_complete");
    public static final ResourceKey<GameTestInstance> SPELL_IS_INVALID_EMPTY_NAME = createInstanceKey("spell_is_invalid_empty_name");
    public static final ResourceKey<GameTestInstance> SPELL_IS_INVALID_EMPTY_VEHICLE = createInstanceKey("spell_is_invalid_empty_vehicle");
    public static final ResourceKey<GameTestInstance> SPELL_IS_INVALID_EMPTY_PAYLOAD = createInstanceKey("spell_is_invalid_empty_payload");
    public static final ResourceKey<GameTestInstance> SPELL_IS_INVALID_NULL_NAME = createInstanceKey("spell_is_invalid_null_name");
    public static final ResourceKey<GameTestInstance> SPELL_IS_INVALID_NULL_VEHICLE = createInstanceKey("spell_is_invalid_null_vehicle");
    public static final ResourceKey<GameTestInstance> SPELL_IS_INVALID_NULL_PAYLOAD = createInstanceKey("spell_is_invalid_null_payload");

    // Auto charger tests
    public static final ResourceKey<GameTestInstance> AUTO_CHARGER_OUTPUT_ALLOWS_CHARGEABLE_ITEMS = createInstanceKey("auto_charger_output_allows_chargeable_items");
    public static final ResourceKey<GameTestInstance> AUTO_CHARGER_OUTPUT_DOES_NOT_ALLOW_UNCHARGEABLE_ITEMS = createInstanceKey("auto_charger_output_does_not_allow_unchargeable_items");
    public static final ResourceKey<GameTestInstance> AUTO_CHARGER_CAN_HAVE_CHARGEABLE_ITEMS_INSERTED = createInstanceKey("auto_charger_can_have_chargeable_items_inserted");
    public static final ResourceKey<GameTestInstance> AUTO_CHARGER_CANNOT_HAVE_UNCHARGEABLE_ITEMS_INSERTED = createInstanceKey("auto_charger_cannot_have_unchargeable_items_inserted");
    public static final ResourceKey<GameTestInstance> AUTO_CHARGER_CAN_HAVE_CHARGEABLE_ITEMS_REMOVED = createInstanceKey("auto_charger_can_have_chargeable_items_removed");
    public static final ResourceKey<GameTestInstance> AUTO_CHARGER_SIPHONS_INTO_CHARGEABLE_ITEMS = createInstanceKey("auto_charger_siphons_into_chargeable_items");
    public static final ResourceKey<GameTestInstance> AUTO_CHARGER_SIPHON_DOES_NOT_MUTATE_STACK_COPIES = createInstanceKey("auto_charger_siphon_does_not_mutate_stack_copies");
    public static final ResourceKey<GameTestInstance> AUTO_CHARGER_SIPHON_MARKS_BLOCK_ENTITY_CHANGED = createInstanceKey("auto_charger_siphon_marks_block_entity_changed");
    public static final ResourceKey<GameTestInstance> AUTO_CHARGER_OUTPUT_ALLOWS_CHARGEABLE_ITEMS_MODULAR_WAND = createInstanceKey("auto_charger_output_allows_chargeable_items_modular_wand");
    public static final ResourceKey<GameTestInstance> AUTO_CHARGER_OUTPUT_ALLOWS_CHARGEABLE_ITEMS_MODULAR_STAFF = createInstanceKey("auto_charger_output_allows_chargeable_items_modular_staff");
    public static final ResourceKey<GameTestInstance> AUTO_CHARGER_OUTPUT_ALLOWS_CHARGEABLE_ITEMS_WARDED_PRIMALITE_CHEST = createInstanceKey("auto_charger_output_allows_chargeable_items_warded_primalite_chest");
    public static final ResourceKey<GameTestInstance> AUTO_CHARGER_OUTPUT_DOES_NOT_ALLOW_ESSENCE = createInstanceKey("auto_charger_output_does_not_allow_essence");

    // Mana battery tests
    public static final ResourceKey<GameTestInstance> MANA_BATTERY_CAN_HAVE_ITS_MENU_OPENED = createInstanceKey("mana_battery_can_have_its_menu_opened");
    public static final ResourceKey<GameTestInstance> MANA_BATTERY_OUTPUT_ALLOWS_CHARGEABLE_ITEMS = createInstanceKey("mana_battery_output_allows_chargeable_items");
    public static final ResourceKey<GameTestInstance> MANA_BATTERY_OUTPUT_DOES_NOT_ALLOW_UNCHARGEABLE_ITEMS = createInstanceKey("mana_battery_output_does_not_allow_unchargeable_items");
    public static final ResourceKey<GameTestInstance> MANA_BATTERY_INPUT_ALLOWS_ESSENCE = createInstanceKey("mana_battery_input_allows_essence");
    public static final ResourceKey<GameTestInstance> MANA_BATTERY_INPUT_ALLOWS_WANDS = createInstanceKey("mana_battery_input_allows_wands");
    public static final ResourceKey<GameTestInstance> MANA_BATTERY_SIPHONS_FROM_NEARBY_FONTS = createInstanceKey("mana_battery_siphons_from_nearby_fonts");
    public static final ResourceKey<GameTestInstance> MANA_BATTERY_OUTPUT_ALLOWS_CHARGEABLE_ITEMS_MODULAR_WAND = createInstanceKey("mana_battery_output_allows_chargeable_items_modular_wand");
    public static final ResourceKey<GameTestInstance> MANA_BATTERY_OUTPUT_ALLOWS_CHARGEABLE_ITEMS_MODULAR_STAFF = createInstanceKey("mana_battery_output_allows_chargeable_items_modular_staff");
    public static final ResourceKey<GameTestInstance> MANA_BATTERY_OUTPUT_ALLOWS_CHARGEABLE_ITEMS_WARDED_PRIMALITE_CHEST = createInstanceKey("mana_battery_output_allows_chargeable_items_warded_primalite_chest");
    public static final ResourceKey<GameTestInstance> MANA_BATTERY_OUTPUT_DOES_NOT_ALLOW_ESSENCE = createInstanceKey("mana_battery_output_does_not_allow_essence");
    public static final ResourceKey<GameTestInstance> MANA_BATTERY_INPUT_ALLOWS_ESSENCE_DUST = createInstanceKey("mana_battery_input_allows_essence_dust");
    public static final ResourceKey<GameTestInstance> MANA_BATTERY_INPUT_ALLOWS_ESSENCE_CRYSTAL = createInstanceKey("mana_battery_input_allows_essence_crystal");
    public static final ResourceKey<GameTestInstance> MANA_BATTERY_INPUT_ALLOWS_ESSENCE_CLUSTER = createInstanceKey("mana_battery_input_allows_essence_cluster");
    public static final ResourceKey<GameTestInstance> MANA_BATTERY_INPUT_ALLOWS_WANDS_MODULAR_WAND = createInstanceKey("mana_battery_input_allows_wands_modular_wand");
    public static final ResourceKey<GameTestInstance> MANA_BATTERY_INPUT_ALLOWS_WANDS_MODULAR_STAFF = createInstanceKey("mana_battery_input_allows_wands_modular_staff");
    public static final ResourceKey<GameTestInstance> MANA_BATTERY_SIPHONS_FROM_NEARBY_FONTS_MANA_SINGULARITY = createInstanceKey("mana_battery_siphons_from_nearby_fonts_mana_singularity");
    public static final ResourceKey<GameTestInstance> MANA_BATTERY_DOES_NOT_SIPHON_FROM_NEARBY_FONTS_MANA_SINGULARITY_CREATIVE = createInstanceKey("mana_battery_does_not_siphon_from_nearby_fonts_mana_singularity_creative");
    public static final ResourceKey<GameTestInstance> MANA_BATTERY_OUTPUT_DOES_NOT_MUTATE_STACK_COPIES = createInstanceKey("mana_battery_output_does_not_mutate_stack_copies");
    public static final ResourceKey<GameTestInstance> MANA_BATTERY_ITEM_COMPONENTS_DO_NOT_ALIAS_TILE = createInstanceKey("mana_battery_item_components_do_not_alias_tile");

    // Mana font tests
    public static final ResourceKey<GameTestInstance> MANA_FONT_SIPHONED_BY_WAND = createInstanceKey("mana_font_siphoned_by_wand");
    public static final ResourceKey<GameTestInstance> MANA_FONT_RECHARGES = createInstanceKey("mana_font_recharges");
    public static final ResourceKey<GameTestInstance> MANA_FONT_SIPHONED_BY_WAND_ANCIENT_SEA = createInstanceKey("mana_font_siphoned_by_wand_ancient_sea");
    public static final ResourceKey<GameTestInstance> MANA_FONT_SIPHONED_BY_WAND_ANCIENT_SKY = createInstanceKey("mana_font_siphoned_by_wand_ancient_sky");
    public static final ResourceKey<GameTestInstance> MANA_FONT_SIPHONED_BY_WAND_ANCIENT_SUN = createInstanceKey("mana_font_siphoned_by_wand_ancient_sun");
    public static final ResourceKey<GameTestInstance> MANA_FONT_SIPHONED_BY_WAND_ANCIENT_MOON = createInstanceKey("mana_font_siphoned_by_wand_ancient_moon");
    public static final ResourceKey<GameTestInstance> MANA_FONT_SIPHONED_BY_WAND_ARTIFICIAL_EARTH = createInstanceKey("mana_font_siphoned_by_wand_artificial_earth");
    public static final ResourceKey<GameTestInstance> MANA_FONT_SIPHONED_BY_WAND_ARTIFICIAL_SEA = createInstanceKey("mana_font_siphoned_by_wand_artificial_sea");
    public static final ResourceKey<GameTestInstance> MANA_FONT_SIPHONED_BY_WAND_ARTIFICIAL_SKY = createInstanceKey("mana_font_siphoned_by_wand_artificial_sky");
    public static final ResourceKey<GameTestInstance> MANA_FONT_SIPHONED_BY_WAND_ARTIFICIAL_SUN = createInstanceKey("mana_font_siphoned_by_wand_artificial_sun");
    public static final ResourceKey<GameTestInstance> MANA_FONT_SIPHONED_BY_WAND_ARTIFICIAL_MOON = createInstanceKey("mana_font_siphoned_by_wand_artificial_moon");
    public static final ResourceKey<GameTestInstance> MANA_FONT_SIPHONED_BY_WAND_ARTIFICIAL_BLOOD = createInstanceKey("mana_font_siphoned_by_wand_artificial_blood");
    public static final ResourceKey<GameTestInstance> MANA_FONT_SIPHONED_BY_WAND_ARTIFICIAL_INFERNAL = createInstanceKey("mana_font_siphoned_by_wand_artificial_infernal");
    public static final ResourceKey<GameTestInstance> MANA_FONT_SIPHONED_BY_WAND_ARTIFICIAL_VOID = createInstanceKey("mana_font_siphoned_by_wand_artificial_void");
    public static final ResourceKey<GameTestInstance> MANA_FONT_SIPHONED_BY_WAND_ARTIFICIAL_HALLOWED = createInstanceKey("mana_font_siphoned_by_wand_artificial_hallowed");
    public static final ResourceKey<GameTestInstance> MANA_FONT_SIPHONED_BY_WAND_FORBIDDEN_EARTH = createInstanceKey("mana_font_siphoned_by_wand_forbidden_earth");
    public static final ResourceKey<GameTestInstance> MANA_FONT_SIPHONED_BY_WAND_FORBIDDEN_SEA = createInstanceKey("mana_font_siphoned_by_wand_forbidden_sea");
    public static final ResourceKey<GameTestInstance> MANA_FONT_SIPHONED_BY_WAND_FORBIDDEN_SKY = createInstanceKey("mana_font_siphoned_by_wand_forbidden_sky");
    public static final ResourceKey<GameTestInstance> MANA_FONT_SIPHONED_BY_WAND_FORBIDDEN_SUN = createInstanceKey("mana_font_siphoned_by_wand_forbidden_sun");
    public static final ResourceKey<GameTestInstance> MANA_FONT_SIPHONED_BY_WAND_FORBIDDEN_MOON = createInstanceKey("mana_font_siphoned_by_wand_forbidden_moon");
    public static final ResourceKey<GameTestInstance> MANA_FONT_SIPHONED_BY_WAND_FORBIDDEN_BLOOD = createInstanceKey("mana_font_siphoned_by_wand_forbidden_blood");
    public static final ResourceKey<GameTestInstance> MANA_FONT_SIPHONED_BY_WAND_FORBIDDEN_INFERNAL = createInstanceKey("mana_font_siphoned_by_wand_forbidden_infernal");
    public static final ResourceKey<GameTestInstance> MANA_FONT_SIPHONED_BY_WAND_FORBIDDEN_VOID = createInstanceKey("mana_font_siphoned_by_wand_forbidden_void");
    public static final ResourceKey<GameTestInstance> MANA_FONT_SIPHONED_BY_WAND_FORBIDDEN_HALLOWED = createInstanceKey("mana_font_siphoned_by_wand_forbidden_hallowed");
    public static final ResourceKey<GameTestInstance> MANA_FONT_SIPHONED_BY_WAND_HEAVENLY_EARTH = createInstanceKey("mana_font_siphoned_by_wand_heavenly_earth");
    public static final ResourceKey<GameTestInstance> MANA_FONT_SIPHONED_BY_WAND_HEAVENLY_SEA = createInstanceKey("mana_font_siphoned_by_wand_heavenly_sea");
    public static final ResourceKey<GameTestInstance> MANA_FONT_SIPHONED_BY_WAND_HEAVENLY_SKY = createInstanceKey("mana_font_siphoned_by_wand_heavenly_sky");
    public static final ResourceKey<GameTestInstance> MANA_FONT_SIPHONED_BY_WAND_HEAVENLY_SUN = createInstanceKey("mana_font_siphoned_by_wand_heavenly_sun");
    public static final ResourceKey<GameTestInstance> MANA_FONT_SIPHONED_BY_WAND_HEAVENLY_MOON = createInstanceKey("mana_font_siphoned_by_wand_heavenly_moon");
    public static final ResourceKey<GameTestInstance> MANA_FONT_SIPHONED_BY_WAND_HEAVENLY_BLOOD = createInstanceKey("mana_font_siphoned_by_wand_heavenly_blood");
    public static final ResourceKey<GameTestInstance> MANA_FONT_SIPHONED_BY_WAND_HEAVENLY_INFERNAL = createInstanceKey("mana_font_siphoned_by_wand_heavenly_infernal");
    public static final ResourceKey<GameTestInstance> MANA_FONT_SIPHONED_BY_WAND_HEAVENLY_VOID = createInstanceKey("mana_font_siphoned_by_wand_heavenly_void");
    public static final ResourceKey<GameTestInstance> MANA_FONT_SIPHONED_BY_WAND_HEAVENLY_HALLOWED = createInstanceKey("mana_font_siphoned_by_wand_heavenly_hallowed");
    public static final ResourceKey<GameTestInstance> MANA_FONT_SIPHONED_BY_MODULAR_WAND = createInstanceKey("mana_font_siphoned_by_modular_wand");
    public static final ResourceKey<GameTestInstance> MANA_FONT_SIPHONED_BY_MODULAR_STAFF = createInstanceKey("mana_font_siphoned_by_modular_staff");
    public static final ResourceKey<GameTestInstance> MANA_FONT_RECHARGES_ARTIFICIAL = createInstanceKey("mana_font_recharges_artificial");
    public static final ResourceKey<GameTestInstance> MANA_FONT_RECHARGES_FORBIDDEN = createInstanceKey("mana_font_recharges_forbidden");
    public static final ResourceKey<GameTestInstance> MANA_FONT_RECHARGES_HEAVENLY = createInstanceKey("mana_font_recharges_heavenly");

    // Wand charger tests
    public static final ResourceKey<GameTestInstance> WAND_CHARGER_CAN_HAVE_ITS_MENU_OPENED = createInstanceKey("wand_charger_can_have_its_menu_opened");
    public static final ResourceKey<GameTestInstance> WAND_CHARGER_OUTPUT_ALLOWS_CHARGEABLE_ITEMS = createInstanceKey("wand_charger_output_allows_chargeable_items");
    public static final ResourceKey<GameTestInstance> WAND_CHARGER_OUTPUT_DOES_NOT_ALLOW_UNCHARGEABLE_ITEMS = createInstanceKey("wand_charger_output_does_not_allow_unchargeable_items");
    public static final ResourceKey<GameTestInstance> WAND_CHARGER_OUTPUT_DOES_NOT_ALLOW_ESSENCE = createInstanceKey("wand_charger_output_does_not_allow_essence");
    public static final ResourceKey<GameTestInstance> WAND_CHARGER_INPUT_ALLOWS_ESSENCE = createInstanceKey("wand_charger_input_allows_essence");
    public static final ResourceKey<GameTestInstance> WAND_CHARGER_INPUT_DOES_NOT_ALLOW_NON_ESSENCE = createInstanceKey("wand_charger_input_does_not_allow_non_essence");
    public static final ResourceKey<GameTestInstance> WAND_CHARGER_CAN_CHARGE_WITH_RIGHT_ITEMS = createInstanceKey("wand_charger_can_charge_with_right_items");
    public static final ResourceKey<GameTestInstance> WAND_CHARGER_DO_CHARGE_WITH_RIGHT_ITEMS = createInstanceKey("wand_charger_do_charge_with_right_items");
    public static final ResourceKey<GameTestInstance> WAND_CHARGER_CHARGE_DOES_NOT_MUTATE_STACK_COPIES = createInstanceKey("wand_charger_charge_does_not_mutate_stack_copies");
    public static final ResourceKey<GameTestInstance> WAND_CHARGER_OUTPUT_ALLOWS_CHARGEABLE_ITEMS_MODULAR_WAND = createInstanceKey("wand_charger_output_allows_chargeable_items_modular_wand");
    public static final ResourceKey<GameTestInstance> WAND_CHARGER_OUTPUT_ALLOWS_CHARGEABLE_ITEMS_MODULAR_STAFF = createInstanceKey("wand_charger_output_allows_chargeable_items_modular_staff");
    public static final ResourceKey<GameTestInstance> WAND_CHARGER_OUTPUT_ALLOWS_CHARGEABLE_ITEMS_WARDED_PRIMALITE_CHEST = createInstanceKey("wand_charger_output_allows_chargeable_items_warded_primalite_chest");
    public static final ResourceKey<GameTestInstance> WAND_CHARGER_INPUT_ALLOWS_ESSENCE_DUST = createInstanceKey("wand_charger_input_allows_essence_dust");
    public static final ResourceKey<GameTestInstance> WAND_CHARGER_INPUT_ALLOWS_ESSENCE_CRYSTAL = createInstanceKey("wand_charger_input_allows_essence_crystal");
    public static final ResourceKey<GameTestInstance> WAND_CHARGER_INPUT_ALLOWS_ESSENCE_CLUSTER = createInstanceKey("wand_charger_input_allows_essence_cluster");
    public static final ResourceKey<GameTestInstance> WAND_CHARGER_DO_CHARGE_WITH_RIGHT_ITEMS_SHARD = createInstanceKey("wand_charger_do_charge_with_right_items_shard");
    public static final ResourceKey<GameTestInstance> WAND_CHARGER_DO_CHARGE_WITH_RIGHT_ITEMS_CRYSTAL = createInstanceKey("wand_charger_do_charge_with_right_items_crystal");
    public static final ResourceKey<GameTestInstance> WAND_CHARGER_DO_CHARGE_WITH_RIGHT_ITEMS_CLUSTER = createInstanceKey("wand_charger_do_charge_with_right_items_cluster");

    // Ritual altar tests
    public static final ResourceKey<GameTestInstance> RITUAL_ALTAR_ACTIVE_RECIPE_ROUND_TRIPS = createInstanceKey("ritual_altar_active_recipe_round_trips");
    public static final ResourceKey<GameTestInstance> RITUAL_ALTAR_REPLACE_ITEM_ON_EMPTY_SLOT = createInstanceKey("ritual_altar_replace_item_on_empty_slot");
    public static final ResourceKey<GameTestInstance> RITUAL_ALTAR_REPLACE_ITEM_WITH_EMPTY_STACK = createInstanceKey("ritual_altar_replace_item_with_empty_stack");
    public static final ResourceKey<GameTestInstance> RITUAL_ALTAR_FINISH_CRAFT_WITH_EMPTY_SLOT_DOES_NOT_THROW = createInstanceKey("ritual_altar_finish_craft_with_empty_slot_does_not_throw");

    // Infernal furnace tests
    public static final ResourceKey<GameTestInstance> INFERNAL_FURNACE_LAVA_BUCKET_FUEL_LEAVES_EMPTY_BUCKET = createInstanceKey("infernal_furnace_lava_bucket_fuel_leaves_empty_bucket");

    public static void bootstrap(BootstrapContext<GameTestInstance> context) {
        registerFunction(context, CANARY, TestFunctionsPM.CANARY.getKey());
        registerAttunementBuffTests(context);
        registerItemHandlerTests(context);
        registerItemHandlerCapabilityTests(context);
        registerPlayerKnowledgeTests(context);
        registerArcaneWorkbenchTests(context);
        registerCalcinatorTests(context);
        registerCraftingRequirementTests(context);
        registerRepairTests(context);
        registerEarthshatterHammerTests(context);
        registerRunecarvingTests(context);
        registerWandInscriptionTests(context);
        registerRunescribingResultTests(context);
        registerCasterEnchantabilityTests(context);
        registerRitualEnchantmentTests(context);
        registerRuneManagerTests(context);
        registerLootModifierTests(context);
        registerFtuxTests(context);
        registerBeeswaxItemTests(context);
        registerDispenserItemTests(context);
        registerEssenceTests(context);
        registerConcoctionTests(context);
        registerWandManaTests(context);
        registerWandComponentTests(context);
        registerWardingModuleTests(context);
        registerResearchKeyTests(context);
        registerResearchRequirementTests(context);
        registerResearchTests(context);
        registerSpellTests(context);
        registerAutoChargerTests(context);
        registerManaBatteryTests(context);
        registerManaFontTests(context);
        registerWandChargerTests(context);
        registerRitualAltarTests(context);
        registerInfernalFurnaceTests(context);
    }

    public static void registerWandChargerTests(BootstrapContext<GameTestInstance> context) {
        registerFunction(context, WAND_CHARGER_CAN_HAVE_ITS_MENU_OPENED, TestFunctionsPM.WAND_CHARGER_CAN_HAVE_ITS_MENU_OPENED.getKey());
        registerFunction(context, WAND_CHARGER_OUTPUT_ALLOWS_CHARGEABLE_ITEMS, TestFunctionsPM.WAND_CHARGER_OUTPUT_ALLOWS_CHARGEABLE_ITEMS.getKey());
        registerFunction(context, WAND_CHARGER_OUTPUT_DOES_NOT_ALLOW_UNCHARGEABLE_ITEMS, TestFunctionsPM.WAND_CHARGER_OUTPUT_DOES_NOT_ALLOW_UNCHARGEABLE_ITEMS.getKey());
        registerFunction(context, WAND_CHARGER_OUTPUT_DOES_NOT_ALLOW_ESSENCE, TestFunctionsPM.WAND_CHARGER_OUTPUT_DOES_NOT_ALLOW_ESSENCE.getKey());
        registerFunction(context, WAND_CHARGER_INPUT_ALLOWS_ESSENCE, TestFunctionsPM.WAND_CHARGER_INPUT_ALLOWS_ESSENCE.getKey());
        registerFunction(context, WAND_CHARGER_INPUT_DOES_NOT_ALLOW_NON_ESSENCE, TestFunctionsPM.WAND_CHARGER_INPUT_DOES_NOT_ALLOW_NON_ESSENCE.getKey());
        registerFunction(context, WAND_CHARGER_CAN_CHARGE_WITH_RIGHT_ITEMS, TestFunctionsPM.WAND_CHARGER_CAN_CHARGE_WITH_RIGHT_ITEMS.getKey());
        registerFunction(context, WAND_CHARGER_DO_CHARGE_WITH_RIGHT_ITEMS, TestFunctionsPM.WAND_CHARGER_DO_CHARGE_WITH_RIGHT_ITEMS.getKey());
        registerFunction(context, WAND_CHARGER_CHARGE_DOES_NOT_MUTATE_STACK_COPIES, TestFunctionsPM.WAND_CHARGER_CHARGE_DOES_NOT_MUTATE_STACK_COPIES.getKey());
        registerFunction(context, WAND_CHARGER_OUTPUT_ALLOWS_CHARGEABLE_ITEMS_MODULAR_WAND, TestFunctionsPM.WAND_CHARGER_OUTPUT_ALLOWS_CHARGEABLE_ITEMS_MODULAR_WAND.getKey());
        registerFunction(context, WAND_CHARGER_OUTPUT_ALLOWS_CHARGEABLE_ITEMS_MODULAR_STAFF, TestFunctionsPM.WAND_CHARGER_OUTPUT_ALLOWS_CHARGEABLE_ITEMS_MODULAR_STAFF.getKey());
        registerFunction(context, WAND_CHARGER_OUTPUT_ALLOWS_CHARGEABLE_ITEMS_WARDED_PRIMALITE_CHEST, TestFunctionsPM.WAND_CHARGER_OUTPUT_ALLOWS_CHARGEABLE_ITEMS_WARDED_PRIMALITE_CHEST.getKey());
        registerFunction(context, WAND_CHARGER_INPUT_ALLOWS_ESSENCE_DUST, TestFunctionsPM.WAND_CHARGER_INPUT_ALLOWS_ESSENCE_DUST.getKey());
        registerFunction(context, WAND_CHARGER_INPUT_ALLOWS_ESSENCE_CRYSTAL, TestFunctionsPM.WAND_CHARGER_INPUT_ALLOWS_ESSENCE_CRYSTAL.getKey());
        registerFunction(context, WAND_CHARGER_INPUT_ALLOWS_ESSENCE_CLUSTER, TestFunctionsPM.WAND_CHARGER_INPUT_ALLOWS_ESSENCE_CLUSTER.getKey());
        registerFunction(context, WAND_CHARGER_DO_CHARGE_WITH_RIGHT_ITEMS_SHARD, TestFunctionsPM.WAND_CHARGER_DO_CHARGE_WITH_RIGHT_ITEMS_SHARD.getKey());
        registerFunction(context, WAND_CHARGER_DO_CHARGE_WITH_RIGHT_ITEMS_CRYSTAL, TestFunctionsPM.WAND_CHARGER_DO_CHARGE_WITH_RIGHT_ITEMS_CRYSTAL.getKey());
        registerFunction(context, WAND_CHARGER_DO_CHARGE_WITH_RIGHT_ITEMS_CLUSTER, TestFunctionsPM.WAND_CHARGER_DO_CHARGE_WITH_RIGHT_ITEMS_CLUSTER.getKey());
    }

    public static void registerRitualAltarTests(BootstrapContext<GameTestInstance> context) {
        registerFunction(context, RITUAL_ALTAR_ACTIVE_RECIPE_ROUND_TRIPS, TestFunctionsPM.RITUAL_ALTAR_ACTIVE_RECIPE_ROUND_TRIPS.getKey());
        registerFunction(context, RITUAL_ALTAR_REPLACE_ITEM_ON_EMPTY_SLOT, TestFunctionsPM.RITUAL_ALTAR_REPLACE_ITEM_ON_EMPTY_SLOT.getKey());
        registerFunction(context, RITUAL_ALTAR_REPLACE_ITEM_WITH_EMPTY_STACK, TestFunctionsPM.RITUAL_ALTAR_REPLACE_ITEM_WITH_EMPTY_STACK.getKey());
        registerFunction(context, RITUAL_ALTAR_FINISH_CRAFT_WITH_EMPTY_SLOT_DOES_NOT_THROW, TestFunctionsPM.RITUAL_ALTAR_FINISH_CRAFT_WITH_EMPTY_SLOT_DOES_NOT_THROW.getKey());
    }

    public static void registerInfernalFurnaceTests(BootstrapContext<GameTestInstance> context) {
        registerFunction(context, INFERNAL_FURNACE_LAVA_BUCKET_FUEL_LEAVES_EMPTY_BUCKET, TestFunctionsPM.INFERNAL_FURNACE_LAVA_BUCKET_FUEL_LEAVES_EMPTY_BUCKET.getKey());
    }

    public static void registerManaFontTests(BootstrapContext<GameTestInstance> context) {
        registerFunction(context, MANA_FONT_SIPHONED_BY_WAND, TestFunctionsPM.MANA_FONT_SIPHONED_BY_WAND.getKey());
        registerFunction(context, MANA_FONT_RECHARGES, TestFunctionsPM.MANA_FONT_RECHARGES.getKey());
        registerFunction(context, MANA_FONT_SIPHONED_BY_WAND_ANCIENT_SEA, TestFunctionsPM.MANA_FONT_SIPHONED_BY_WAND_ANCIENT_SEA.getKey());
        registerFunction(context, MANA_FONT_SIPHONED_BY_WAND_ANCIENT_SKY, TestFunctionsPM.MANA_FONT_SIPHONED_BY_WAND_ANCIENT_SKY.getKey());
        registerFunction(context, MANA_FONT_SIPHONED_BY_WAND_ANCIENT_SUN, TestFunctionsPM.MANA_FONT_SIPHONED_BY_WAND_ANCIENT_SUN.getKey());
        registerFunction(context, MANA_FONT_SIPHONED_BY_WAND_ANCIENT_MOON, TestFunctionsPM.MANA_FONT_SIPHONED_BY_WAND_ANCIENT_MOON.getKey());
        registerFunction(context, MANA_FONT_SIPHONED_BY_WAND_ARTIFICIAL_EARTH, TestFunctionsPM.MANA_FONT_SIPHONED_BY_WAND_ARTIFICIAL_EARTH.getKey());
        registerFunction(context, MANA_FONT_SIPHONED_BY_WAND_ARTIFICIAL_SEA, TestFunctionsPM.MANA_FONT_SIPHONED_BY_WAND_ARTIFICIAL_SEA.getKey());
        registerFunction(context, MANA_FONT_SIPHONED_BY_WAND_ARTIFICIAL_SKY, TestFunctionsPM.MANA_FONT_SIPHONED_BY_WAND_ARTIFICIAL_SKY.getKey());
        registerFunction(context, MANA_FONT_SIPHONED_BY_WAND_ARTIFICIAL_SUN, TestFunctionsPM.MANA_FONT_SIPHONED_BY_WAND_ARTIFICIAL_SUN.getKey());
        registerFunction(context, MANA_FONT_SIPHONED_BY_WAND_ARTIFICIAL_MOON, TestFunctionsPM.MANA_FONT_SIPHONED_BY_WAND_ARTIFICIAL_MOON.getKey());
        registerFunction(context, MANA_FONT_SIPHONED_BY_WAND_ARTIFICIAL_BLOOD, TestFunctionsPM.MANA_FONT_SIPHONED_BY_WAND_ARTIFICIAL_BLOOD.getKey());
        registerFunction(context, MANA_FONT_SIPHONED_BY_WAND_ARTIFICIAL_INFERNAL, TestFunctionsPM.MANA_FONT_SIPHONED_BY_WAND_ARTIFICIAL_INFERNAL.getKey());
        registerFunction(context, MANA_FONT_SIPHONED_BY_WAND_ARTIFICIAL_VOID, TestFunctionsPM.MANA_FONT_SIPHONED_BY_WAND_ARTIFICIAL_VOID.getKey());
        registerFunction(context, MANA_FONT_SIPHONED_BY_WAND_ARTIFICIAL_HALLOWED, TestFunctionsPM.MANA_FONT_SIPHONED_BY_WAND_ARTIFICIAL_HALLOWED.getKey());
        registerFunction(context, MANA_FONT_SIPHONED_BY_WAND_FORBIDDEN_EARTH, TestFunctionsPM.MANA_FONT_SIPHONED_BY_WAND_FORBIDDEN_EARTH.getKey());
        registerFunction(context, MANA_FONT_SIPHONED_BY_WAND_FORBIDDEN_SEA, TestFunctionsPM.MANA_FONT_SIPHONED_BY_WAND_FORBIDDEN_SEA.getKey());
        registerFunction(context, MANA_FONT_SIPHONED_BY_WAND_FORBIDDEN_SKY, TestFunctionsPM.MANA_FONT_SIPHONED_BY_WAND_FORBIDDEN_SKY.getKey());
        registerFunction(context, MANA_FONT_SIPHONED_BY_WAND_FORBIDDEN_SUN, TestFunctionsPM.MANA_FONT_SIPHONED_BY_WAND_FORBIDDEN_SUN.getKey());
        registerFunction(context, MANA_FONT_SIPHONED_BY_WAND_FORBIDDEN_MOON, TestFunctionsPM.MANA_FONT_SIPHONED_BY_WAND_FORBIDDEN_MOON.getKey());
        registerFunction(context, MANA_FONT_SIPHONED_BY_WAND_FORBIDDEN_BLOOD, TestFunctionsPM.MANA_FONT_SIPHONED_BY_WAND_FORBIDDEN_BLOOD.getKey());
        registerFunction(context, MANA_FONT_SIPHONED_BY_WAND_FORBIDDEN_INFERNAL, TestFunctionsPM.MANA_FONT_SIPHONED_BY_WAND_FORBIDDEN_INFERNAL.getKey());
        registerFunction(context, MANA_FONT_SIPHONED_BY_WAND_FORBIDDEN_VOID, TestFunctionsPM.MANA_FONT_SIPHONED_BY_WAND_FORBIDDEN_VOID.getKey());
        registerFunction(context, MANA_FONT_SIPHONED_BY_WAND_FORBIDDEN_HALLOWED, TestFunctionsPM.MANA_FONT_SIPHONED_BY_WAND_FORBIDDEN_HALLOWED.getKey());
        registerFunction(context, MANA_FONT_SIPHONED_BY_WAND_HEAVENLY_EARTH, TestFunctionsPM.MANA_FONT_SIPHONED_BY_WAND_HEAVENLY_EARTH.getKey());
        registerFunction(context, MANA_FONT_SIPHONED_BY_WAND_HEAVENLY_SEA, TestFunctionsPM.MANA_FONT_SIPHONED_BY_WAND_HEAVENLY_SEA.getKey());
        registerFunction(context, MANA_FONT_SIPHONED_BY_WAND_HEAVENLY_SKY, TestFunctionsPM.MANA_FONT_SIPHONED_BY_WAND_HEAVENLY_SKY.getKey());
        registerFunction(context, MANA_FONT_SIPHONED_BY_WAND_HEAVENLY_SUN, TestFunctionsPM.MANA_FONT_SIPHONED_BY_WAND_HEAVENLY_SUN.getKey());
        registerFunction(context, MANA_FONT_SIPHONED_BY_WAND_HEAVENLY_MOON, TestFunctionsPM.MANA_FONT_SIPHONED_BY_WAND_HEAVENLY_MOON.getKey());
        registerFunction(context, MANA_FONT_SIPHONED_BY_WAND_HEAVENLY_BLOOD, TestFunctionsPM.MANA_FONT_SIPHONED_BY_WAND_HEAVENLY_BLOOD.getKey());
        registerFunction(context, MANA_FONT_SIPHONED_BY_WAND_HEAVENLY_INFERNAL, TestFunctionsPM.MANA_FONT_SIPHONED_BY_WAND_HEAVENLY_INFERNAL.getKey());
        registerFunction(context, MANA_FONT_SIPHONED_BY_WAND_HEAVENLY_VOID, TestFunctionsPM.MANA_FONT_SIPHONED_BY_WAND_HEAVENLY_VOID.getKey());
        registerFunction(context, MANA_FONT_SIPHONED_BY_WAND_HEAVENLY_HALLOWED, TestFunctionsPM.MANA_FONT_SIPHONED_BY_WAND_HEAVENLY_HALLOWED.getKey());
        registerFunction(context, MANA_FONT_SIPHONED_BY_MODULAR_WAND, TestFunctionsPM.MANA_FONT_SIPHONED_BY_MODULAR_WAND.getKey());
        registerFunction(context, MANA_FONT_SIPHONED_BY_MODULAR_STAFF, TestFunctionsPM.MANA_FONT_SIPHONED_BY_MODULAR_STAFF.getKey());
        registerFunction(context, MANA_FONT_RECHARGES_ARTIFICIAL, TestFunctionsPM.MANA_FONT_RECHARGES_ARTIFICIAL.getKey());
        registerFunction(context, MANA_FONT_RECHARGES_FORBIDDEN, TestFunctionsPM.MANA_FONT_RECHARGES_FORBIDDEN.getKey());
        registerFunction(context, MANA_FONT_RECHARGES_HEAVENLY, TestFunctionsPM.MANA_FONT_RECHARGES_HEAVENLY.getKey());
    }

    public static void registerManaBatteryTests(BootstrapContext<GameTestInstance> context) {
        registerFunction(context, MANA_BATTERY_CAN_HAVE_ITS_MENU_OPENED, TestFunctionsPM.MANA_BATTERY_CAN_HAVE_ITS_MENU_OPENED.getKey());
        registerFunction(context, MANA_BATTERY_OUTPUT_ALLOWS_CHARGEABLE_ITEMS, TestFunctionsPM.MANA_BATTERY_OUTPUT_ALLOWS_CHARGEABLE_ITEMS.getKey());
        registerFunction(context, MANA_BATTERY_OUTPUT_DOES_NOT_ALLOW_UNCHARGEABLE_ITEMS, TestFunctionsPM.MANA_BATTERY_OUTPUT_DOES_NOT_ALLOW_UNCHARGEABLE_ITEMS.getKey());
        registerFunction(context, MANA_BATTERY_INPUT_ALLOWS_ESSENCE, TestFunctionsPM.MANA_BATTERY_INPUT_ALLOWS_ESSENCE.getKey());
        registerFunction(context, MANA_BATTERY_INPUT_ALLOWS_WANDS, TestFunctionsPM.MANA_BATTERY_INPUT_ALLOWS_WANDS.getKey());
        registerFunction(context, MANA_BATTERY_SIPHONS_FROM_NEARBY_FONTS, TestFunctionsPM.MANA_BATTERY_SIPHONS_FROM_NEARBY_FONTS.getKey());
        registerFunction(context, MANA_BATTERY_OUTPUT_ALLOWS_CHARGEABLE_ITEMS_MODULAR_WAND, TestFunctionsPM.MANA_BATTERY_OUTPUT_ALLOWS_CHARGEABLE_ITEMS_MODULAR_WAND.getKey());
        registerFunction(context, MANA_BATTERY_OUTPUT_ALLOWS_CHARGEABLE_ITEMS_MODULAR_STAFF, TestFunctionsPM.MANA_BATTERY_OUTPUT_ALLOWS_CHARGEABLE_ITEMS_MODULAR_STAFF.getKey());
        registerFunction(context, MANA_BATTERY_OUTPUT_ALLOWS_CHARGEABLE_ITEMS_WARDED_PRIMALITE_CHEST, TestFunctionsPM.MANA_BATTERY_OUTPUT_ALLOWS_CHARGEABLE_ITEMS_WARDED_PRIMALITE_CHEST.getKey());
        registerFunction(context, MANA_BATTERY_OUTPUT_DOES_NOT_ALLOW_ESSENCE, TestFunctionsPM.MANA_BATTERY_OUTPUT_DOES_NOT_ALLOW_ESSENCE.getKey());
        registerFunction(context, MANA_BATTERY_INPUT_ALLOWS_ESSENCE_DUST, TestFunctionsPM.MANA_BATTERY_INPUT_ALLOWS_ESSENCE_DUST.getKey());
        registerFunction(context, MANA_BATTERY_INPUT_ALLOWS_ESSENCE_CRYSTAL, TestFunctionsPM.MANA_BATTERY_INPUT_ALLOWS_ESSENCE_CRYSTAL.getKey());
        registerFunction(context, MANA_BATTERY_INPUT_ALLOWS_ESSENCE_CLUSTER, TestFunctionsPM.MANA_BATTERY_INPUT_ALLOWS_ESSENCE_CLUSTER.getKey());
        registerFunction(context, MANA_BATTERY_INPUT_ALLOWS_WANDS_MODULAR_WAND, TestFunctionsPM.MANA_BATTERY_INPUT_ALLOWS_WANDS_MODULAR_WAND.getKey());
        registerFunction(context, MANA_BATTERY_INPUT_ALLOWS_WANDS_MODULAR_STAFF, TestFunctionsPM.MANA_BATTERY_INPUT_ALLOWS_WANDS_MODULAR_STAFF.getKey());
        registerFunction(context, MANA_BATTERY_SIPHONS_FROM_NEARBY_FONTS_MANA_SINGULARITY, TestFunctionsPM.MANA_BATTERY_SIPHONS_FROM_NEARBY_FONTS_MANA_SINGULARITY.getKey());
        registerFunction(context, MANA_BATTERY_DOES_NOT_SIPHON_FROM_NEARBY_FONTS_MANA_SINGULARITY_CREATIVE, TestFunctionsPM.MANA_BATTERY_DOES_NOT_SIPHON_FROM_NEARBY_FONTS_MANA_SINGULARITY_CREATIVE.getKey());
        registerFunction(context, MANA_BATTERY_OUTPUT_DOES_NOT_MUTATE_STACK_COPIES, TestFunctionsPM.MANA_BATTERY_OUTPUT_DOES_NOT_MUTATE_STACK_COPIES.getKey());
        registerFunction(context, MANA_BATTERY_ITEM_COMPONENTS_DO_NOT_ALIAS_TILE, TestFunctionsPM.MANA_BATTERY_ITEM_COMPONENTS_DO_NOT_ALIAS_TILE.getKey());
    }

    public static void registerAutoChargerTests(BootstrapContext<GameTestInstance> context) {
        registerFunction(context, AUTO_CHARGER_OUTPUT_ALLOWS_CHARGEABLE_ITEMS, TestFunctionsPM.AUTO_CHARGER_OUTPUT_ALLOWS_CHARGEABLE_ITEMS.getKey());
        registerFunction(context, AUTO_CHARGER_OUTPUT_DOES_NOT_ALLOW_UNCHARGEABLE_ITEMS, TestFunctionsPM.AUTO_CHARGER_OUTPUT_DOES_NOT_ALLOW_UNCHARGEABLE_ITEMS.getKey());
        registerFunction(context, AUTO_CHARGER_CAN_HAVE_CHARGEABLE_ITEMS_INSERTED, TestFunctionsPM.AUTO_CHARGER_CAN_HAVE_CHARGEABLE_ITEMS_INSERTED.getKey());
        registerFunction(context, AUTO_CHARGER_CANNOT_HAVE_UNCHARGEABLE_ITEMS_INSERTED, TestFunctionsPM.AUTO_CHARGER_CANNOT_HAVE_UNCHARGEABLE_ITEMS_INSERTED.getKey());
        registerFunction(context, AUTO_CHARGER_CAN_HAVE_CHARGEABLE_ITEMS_REMOVED, TestFunctionsPM.AUTO_CHARGER_CAN_HAVE_CHARGEABLE_ITEMS_REMOVED.getKey());
        registerFunction(context, AUTO_CHARGER_SIPHONS_INTO_CHARGEABLE_ITEMS, TestFunctionsPM.AUTO_CHARGER_SIPHONS_INTO_CHARGEABLE_ITEMS.getKey(), ResourceUtils.loc("test/floor5x5x5"));
        registerFunction(context, AUTO_CHARGER_SIPHON_DOES_NOT_MUTATE_STACK_COPIES, TestFunctionsPM.AUTO_CHARGER_SIPHON_DOES_NOT_MUTATE_STACK_COPIES.getKey(), ResourceUtils.loc("test/floor5x5x5"));
        registerFunction(context, AUTO_CHARGER_SIPHON_MARKS_BLOCK_ENTITY_CHANGED, TestFunctionsPM.AUTO_CHARGER_SIPHON_MARKS_BLOCK_ENTITY_CHANGED.getKey(), ResourceUtils.loc("test/floor5x5x5"));
        registerFunction(context, AUTO_CHARGER_OUTPUT_ALLOWS_CHARGEABLE_ITEMS_MODULAR_WAND, TestFunctionsPM.AUTO_CHARGER_OUTPUT_ALLOWS_CHARGEABLE_ITEMS_MODULAR_WAND.getKey());
        registerFunction(context, AUTO_CHARGER_OUTPUT_ALLOWS_CHARGEABLE_ITEMS_MODULAR_STAFF, TestFunctionsPM.AUTO_CHARGER_OUTPUT_ALLOWS_CHARGEABLE_ITEMS_MODULAR_STAFF.getKey());
        registerFunction(context, AUTO_CHARGER_OUTPUT_ALLOWS_CHARGEABLE_ITEMS_WARDED_PRIMALITE_CHEST, TestFunctionsPM.AUTO_CHARGER_OUTPUT_ALLOWS_CHARGEABLE_ITEMS_WARDED_PRIMALITE_CHEST.getKey());
        registerFunction(context, AUTO_CHARGER_OUTPUT_DOES_NOT_ALLOW_ESSENCE, TestFunctionsPM.AUTO_CHARGER_OUTPUT_DOES_NOT_ALLOW_ESSENCE.getKey());
    }

    public static void registerSpellTests(BootstrapContext<GameTestInstance> context) {
        registerFunction(context, DAMAGE_SPELLS_WORK_EARTH, TestFunctionsPM.DAMAGE_SPELLS_WORK_EARTH.getKey());
        registerFunction(context, DAMAGE_SPELLS_WORK_SEA, TestFunctionsPM.DAMAGE_SPELLS_WORK_SEA.getKey());
        registerFunction(context, DAMAGE_SPELLS_WORK_SKY, TestFunctionsPM.DAMAGE_SPELLS_WORK_SKY.getKey());
        registerFunction(context, DAMAGE_SPELLS_WORK_SUN, TestFunctionsPM.DAMAGE_SPELLS_WORK_SUN.getKey());
        registerFunction(context, DAMAGE_SPELLS_WORK_MOON, TestFunctionsPM.DAMAGE_SPELLS_WORK_MOON.getKey());
        registerFunction(context, DAMAGE_SPELLS_WORK_BLOOD, TestFunctionsPM.DAMAGE_SPELLS_WORK_BLOOD.getKey());
        registerFunction(context, DAMAGE_SPELLS_WORK_INFERNAL, TestFunctionsPM.DAMAGE_SPELLS_WORK_INFERNAL.getKey());
        registerFunction(context, DAMAGE_SPELLS_WORK_VOID, TestFunctionsPM.DAMAGE_SPELLS_WORK_VOID.getKey());
        registerFunction(context, DAMAGE_SPELLS_WORK_HALLOWED, TestFunctionsPM.DAMAGE_SPELLS_WORK_HALLOWED.getKey());
        registerFunction(context, SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_EARTH_DAMAGE, TestFunctionsPM.SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_EARTH_DAMAGE.getKey());
        registerFunction(context, SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_FROST_DAMAGE, TestFunctionsPM.SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_FROST_DAMAGE.getKey());
        registerFunction(context, SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_LIGHTNING_DAMAGE, TestFunctionsPM.SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_LIGHTNING_DAMAGE.getKey());
        registerFunction(context, SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_SOLAR_DAMAGE, TestFunctionsPM.SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_SOLAR_DAMAGE.getKey());
        registerFunction(context, SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_LUNAR_DAMAGE, TestFunctionsPM.SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_LUNAR_DAMAGE.getKey());
        registerFunction(context, SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_BLOOD_DAMAGE, TestFunctionsPM.SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_BLOOD_DAMAGE.getKey());
        registerFunction(context, SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_FLAME_DAMAGE, TestFunctionsPM.SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_FLAME_DAMAGE.getKey());
        registerFunction(context, SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_VOID_DAMAGE, TestFunctionsPM.SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_VOID_DAMAGE.getKey());
        registerFunction(context, SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_HOLY_DAMAGE, TestFunctionsPM.SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_HOLY_DAMAGE.getKey());
        registerFunction(context, SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_BREAK, TestFunctionsPM.SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_BREAK.getKey());
        registerFunction(context, SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_CONJURE_STONE, TestFunctionsPM.SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_CONJURE_STONE.getKey());
        registerFunction(context, SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_CONJURE_WATER, TestFunctionsPM.SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_CONJURE_WATER.getKey());
        registerFunction(context, SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_SHEAR, TestFunctionsPM.SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_SHEAR.getKey());
        registerFunction(context, SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_FLIGHT, TestFunctionsPM.SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_FLIGHT.getKey());
        registerFunction(context, SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_HEALING, TestFunctionsPM.SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_HEALING.getKey());
        registerFunction(context, SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_CONJURE_LIGHT, TestFunctionsPM.SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_CONJURE_LIGHT.getKey());
        registerFunction(context, SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_POLYMORPH_WOLF, TestFunctionsPM.SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_POLYMORPH_WOLF.getKey());
        registerFunction(context, SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_POLYMORPH_SHEEP, TestFunctionsPM.SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_POLYMORPH_SHEEP.getKey());
        registerFunction(context, SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_CONJURE_ANIMAL, TestFunctionsPM.SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_CONJURE_ANIMAL.getKey());
        registerFunction(context, SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_CONJURE_LAVA, TestFunctionsPM.SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_CONJURE_LAVA.getKey());
        registerFunction(context, SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_DRAIN_SOUL, TestFunctionsPM.SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_DRAIN_SOUL.getKey());
        registerFunction(context, SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_TELEPORT, TestFunctionsPM.SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_TELEPORT.getKey());
        registerFunction(context, SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_CONSECRATE, TestFunctionsPM.SPELL_MANA_COST_ISOLATED_TO_PAYLOAD_SOURCE_CONSECRATE.getKey());
        registerFunction(context, SPELL_MANA_COST_EARTH_DAMAGE_POWER_1, TestFunctionsPM.SPELL_MANA_COST_EARTH_DAMAGE_POWER_1.getKey());
        registerFunction(context, SPELL_MANA_COST_EARTH_DAMAGE_POWER_5, TestFunctionsPM.SPELL_MANA_COST_EARTH_DAMAGE_POWER_5.getKey());
        registerFunction(context, SPELL_MANA_COST_FLAME_DAMAGE_POWER_3_DURATION_2, TestFunctionsPM.SPELL_MANA_COST_FLAME_DAMAGE_POWER_3_DURATION_2.getKey());
        registerFunction(context, SPELL_MANA_COST_BREAK_POWER_2_SILK_TOUCH, TestFunctionsPM.SPELL_MANA_COST_BREAK_POWER_2_SILK_TOUCH.getKey());
        registerFunction(context, SPELL_MANA_COST_VEHICLE_SELF, TestFunctionsPM.SPELL_MANA_COST_VEHICLE_SELF.getKey());
        registerFunction(context, SPELL_MANA_COST_VEHICLE_BOLT, TestFunctionsPM.SPELL_MANA_COST_VEHICLE_BOLT.getKey());
        registerFunction(context, SPELL_MANA_COST_VEHICLE_PROJECTILE, TestFunctionsPM.SPELL_MANA_COST_VEHICLE_PROJECTILE.getKey());
        registerFunction(context, SPELL_MANA_COST_MOD_AMPLIFY, TestFunctionsPM.SPELL_MANA_COST_MOD_AMPLIFY.getKey());
        registerFunction(context, SPELL_MANA_COST_MOD_BURST, TestFunctionsPM.SPELL_MANA_COST_MOD_BURST.getKey());
        registerFunction(context, SPELL_MANA_COST_MOD_FORK, TestFunctionsPM.SPELL_MANA_COST_MOD_FORK.getKey());
        registerFunction(context, SPELL_MANA_COST_MOD_MINE, TestFunctionsPM.SPELL_MANA_COST_MOD_MINE.getKey());
        registerFunction(context, SPELL_MANA_COST_MOD_QUICKEN, TestFunctionsPM.SPELL_MANA_COST_MOD_QUICKEN.getKey());
        registerFunction(context, SPELL_MANA_COST_MOD_ORDER_INDEPENDENT, TestFunctionsPM.SPELL_MANA_COST_MOD_ORDER_INDEPENDENT.getKey());
        registerFunction(context, SPELL_COOLDOWN_BASE, TestFunctionsPM.SPELL_COOLDOWN_BASE.getKey());
        registerFunction(context, SPELL_COOLDOWN_QUICKEN_HASTE_1, TestFunctionsPM.SPELL_COOLDOWN_QUICKEN_HASTE_1.getKey());
        registerFunction(context, SPELL_COOLDOWN_QUICKEN_HASTE_2, TestFunctionsPM.SPELL_COOLDOWN_QUICKEN_HASTE_2.getKey());
        registerFunction(context, SPELL_COOLDOWN_QUICKEN_HASTE_3, TestFunctionsPM.SPELL_COOLDOWN_QUICKEN_HASTE_3.getKey());
        registerFunction(context, SPELL_COOLDOWN_QUICKEN_HASTE_4, TestFunctionsPM.SPELL_COOLDOWN_QUICKEN_HASTE_4.getKey());
        registerFunction(context, SPELL_COOLDOWN_QUICKEN_HASTE_5, TestFunctionsPM.SPELL_COOLDOWN_QUICKEN_HASTE_5.getKey());
        registerFunction(context, SPELL_COOLDOWN_QUICKEN_HASTE_6, TestFunctionsPM.SPELL_COOLDOWN_QUICKEN_HASTE_6.getKey());
        registerFunction(context, SPELL_COOLDOWN_QUICKEN_HASTE_7, TestFunctionsPM.SPELL_COOLDOWN_QUICKEN_HASTE_7.getKey());
        registerFunction(context, SPELL_PACKAGE_NBT_ROUNDTRIP, TestFunctionsPM.SPELL_PACKAGE_NBT_ROUNDTRIP.getKey());
        registerFunction(context, SPELL_ACTIVE_MOD_COUNT_0, TestFunctionsPM.SPELL_ACTIVE_MOD_COUNT_0.getKey());
        registerFunction(context, SPELL_ACTIVE_MOD_COUNT_1, TestFunctionsPM.SPELL_ACTIVE_MOD_COUNT_1.getKey());
        registerFunction(context, SPELL_ACTIVE_MOD_COUNT_2, TestFunctionsPM.SPELL_ACTIVE_MOD_COUNT_2.getKey());
        registerFunction(context, SPELL_ACTIVE_MOD_COUNT_IGNORES_EMPTY_MOD, TestFunctionsPM.SPELL_ACTIVE_MOD_COUNT_IGNORES_EMPTY_MOD.getKey());
        registerFunction(context, SPELL_IS_VALID_COMPLETE, TestFunctionsPM.SPELL_IS_VALID_COMPLETE.getKey());
        registerFunction(context, SPELL_IS_INVALID_EMPTY_NAME, TestFunctionsPM.SPELL_IS_INVALID_EMPTY_NAME.getKey());
        registerFunction(context, SPELL_IS_INVALID_EMPTY_VEHICLE, TestFunctionsPM.SPELL_IS_INVALID_EMPTY_VEHICLE.getKey());
        registerFunction(context, SPELL_IS_INVALID_EMPTY_PAYLOAD, TestFunctionsPM.SPELL_IS_INVALID_EMPTY_PAYLOAD.getKey());
        registerFunction(context, SPELL_IS_INVALID_NULL_NAME, TestFunctionsPM.SPELL_IS_INVALID_NULL_NAME.getKey());
        registerFunction(context, SPELL_IS_INVALID_NULL_VEHICLE, TestFunctionsPM.SPELL_IS_INVALID_NULL_VEHICLE.getKey());
        registerFunction(context, SPELL_IS_INVALID_NULL_PAYLOAD, TestFunctionsPM.SPELL_IS_INVALID_NULL_PAYLOAD.getKey());
    }

    public static void registerResearchTests(BootstrapContext<GameTestInstance> context) {
        registerFunction(context, RESEARCH_GRANT_WORKS, TestFunctionsPM.RESEARCH_GRANT_WORKS.getKey());
    }

    public static void registerResearchRequirementTests(BootstrapContext<GameTestInstance> context) {
        registerFunction(context, RESEARCH_REQUIREMENT_RESEARCH, TestFunctionsPM.RESEARCH_REQUIREMENT_RESEARCH.getKey());
        registerFunction(context, RESEARCH_REQUIREMENT_KNOWLEDGE, TestFunctionsPM.RESEARCH_REQUIREMENT_KNOWLEDGE.getKey());
        registerFunction(context, RESEARCH_REQUIREMENT_ITEM_STACK, TestFunctionsPM.RESEARCH_REQUIREMENT_ITEM_STACK.getKey());
        registerFunction(context, RESEARCH_REQUIREMENT_ITEM_TAG, TestFunctionsPM.RESEARCH_REQUIREMENT_ITEM_TAG.getKey());
        registerFunction(context, RESEARCH_REQUIREMENT_STAT, TestFunctionsPM.RESEARCH_REQUIREMENT_STAT.getKey());
        registerFunction(context, RESEARCH_REQUIREMENT_EXPERTISE, TestFunctionsPM.RESEARCH_REQUIREMENT_EXPERTISE.getKey());
        registerFunction(context, RESEARCH_REQUIREMENT_VANILLA_ITEM_USED_STAT, TestFunctionsPM.RESEARCH_REQUIREMENT_VANILLA_ITEM_USED_STAT.getKey(), ResourceUtils.loc("test/floor5x5x5"));
        registerFunction(context, RESEARCH_REQUIREMENT_VANILLA_CUSTOM_STAT, TestFunctionsPM.RESEARCH_REQUIREMENT_VANILLA_CUSTOM_STAT.getKey(), ResourceUtils.loc("test/floor5x5x5"));
        registerFunction(context, RESEARCH_REQUIREMENT_AND, TestFunctionsPM.RESEARCH_REQUIREMENT_AND.getKey());
        registerFunction(context, RESEARCH_REQUIREMENT_OR, TestFunctionsPM.RESEARCH_REQUIREMENT_OR.getKey());
        registerFunction(context, RESEARCH_REQUIREMENT_QUORUM, TestFunctionsPM.RESEARCH_REQUIREMENT_QUORUM.getKey());
    }

    public static void registerResearchKeyTests(BootstrapContext<GameTestInstance> context) {
        registerFunction(context, RESEARCH_KEY_DISCIPLINE, TestFunctionsPM.RESEARCH_KEY_DISCIPLINE.getKey());
        registerFunction(context, RESEARCH_KEY_ENTRY, TestFunctionsPM.RESEARCH_KEY_ENTRY.getKey());
        registerFunction(context, RESEARCH_KEY_STAGE, TestFunctionsPM.RESEARCH_KEY_STAGE.getKey());
        registerFunction(context, RESEARCH_KEY_ITEM_SCAN, TestFunctionsPM.RESEARCH_KEY_ITEM_SCAN.getKey());
        registerFunction(context, RESEARCH_KEY_ENTITY_SCAN, TestFunctionsPM.RESEARCH_KEY_ENTITY_SCAN.getKey());
        registerFunction(context, RESEARCH_KEY_STACK_CRAFTED, TestFunctionsPM.RESEARCH_KEY_STACK_CRAFTED.getKey());
        registerFunction(context, RESEARCH_KEY_TAG_CRAFTED, TestFunctionsPM.RESEARCH_KEY_TAG_CRAFTED.getKey());
        registerFunction(context, RESEARCH_KEY_RUNE_ENCHANTMENT, TestFunctionsPM.RESEARCH_KEY_RUNE_ENCHANTMENT.getKey());
        registerFunction(context, RESEARCH_KEY_RUNE_ENCHANTMENT_PARTIAL, TestFunctionsPM.RESEARCH_KEY_RUNE_ENCHANTMENT_PARTIAL.getKey());
    }

    public static void registerWardingModuleTests(BootstrapContext<GameTestInstance> context) {
        registerFunction(context, WARD_REGENERATION_DOES_NOT_MUTATE_STACK_COPIES, TestFunctionsPM.WARD_REGENERATION_DOES_NOT_MUTATE_STACK_COPIES.getKey());
    }

    public static void registerWandComponentTests(BootstrapContext<GameTestInstance> context) {
        registerFunction(context, WAND_GEM_SETS_MAX_MANA_APPRENTICE, TestFunctionsPM.WAND_GEM_SETS_MAX_MANA_APPRENTICE.getKey());
        registerFunction(context, WAND_GEM_SETS_MAX_MANA_ADEPT, TestFunctionsPM.WAND_GEM_SETS_MAX_MANA_ADEPT.getKey());
        registerFunction(context, WAND_GEM_SETS_MAX_MANA_WIZARD, TestFunctionsPM.WAND_GEM_SETS_MAX_MANA_WIZARD.getKey());
        registerFunction(context, WAND_GEM_SETS_MAX_MANA_ARCHMAGE, TestFunctionsPM.WAND_GEM_SETS_MAX_MANA_ARCHMAGE.getKey());
        registerFunction(context, WAND_GEM_CREATIVE_HAS_INFINITE_MANA, TestFunctionsPM.WAND_GEM_CREATIVE_HAS_INFINITE_MANA.getKey());
        registerFunction(context, WAND_CAP_SETS_BASE_COST_MODIFIER_IRON, TestFunctionsPM.WAND_CAP_SETS_BASE_COST_MODIFIER_IRON.getKey());
        registerFunction(context, WAND_CAP_SETS_BASE_COST_MODIFIER_GOLD, TestFunctionsPM.WAND_CAP_SETS_BASE_COST_MODIFIER_GOLD.getKey());
        registerFunction(context, WAND_CAP_SETS_BASE_COST_MODIFIER_PRIMALITE, TestFunctionsPM.WAND_CAP_SETS_BASE_COST_MODIFIER_PRIMALITE.getKey());
        registerFunction(context, WAND_CAP_SETS_BASE_COST_MODIFIER_HEXIUM, TestFunctionsPM.WAND_CAP_SETS_BASE_COST_MODIFIER_HEXIUM.getKey());
        registerFunction(context, WAND_CAP_SETS_BASE_COST_MODIFIER_HALLOWSTEEL, TestFunctionsPM.WAND_CAP_SETS_BASE_COST_MODIFIER_HALLOWSTEEL.getKey());
        registerFunction(context, WAND_CAP_SETS_SIPHON_AMOUNT_IRON, TestFunctionsPM.WAND_CAP_SETS_SIPHON_AMOUNT_IRON.getKey());
        registerFunction(context, WAND_CAP_SETS_SIPHON_AMOUNT_GOLD, TestFunctionsPM.WAND_CAP_SETS_SIPHON_AMOUNT_GOLD.getKey());
        registerFunction(context, WAND_CAP_SETS_SIPHON_AMOUNT_PRIMALITE, TestFunctionsPM.WAND_CAP_SETS_SIPHON_AMOUNT_PRIMALITE.getKey());
        registerFunction(context, WAND_CAP_SETS_SIPHON_AMOUNT_HEXIUM, TestFunctionsPM.WAND_CAP_SETS_SIPHON_AMOUNT_HEXIUM.getKey());
        registerFunction(context, WAND_CAP_SETS_SIPHON_AMOUNT_HALLOWSTEEL, TestFunctionsPM.WAND_CAP_SETS_SIPHON_AMOUNT_HALLOWSTEEL.getKey());
        registerFunction(context, WAND_CORE_SPELL_SLOTS_HEARTWOOD, TestFunctionsPM.WAND_CORE_SPELL_SLOTS_HEARTWOOD.getKey());
        registerFunction(context, WAND_CORE_SPELL_SLOTS_OBSIDIAN, TestFunctionsPM.WAND_CORE_SPELL_SLOTS_OBSIDIAN.getKey());
        registerFunction(context, WAND_CORE_SPELL_SLOTS_BONE, TestFunctionsPM.WAND_CORE_SPELL_SLOTS_BONE.getKey());
        registerFunction(context, WAND_CORE_SPELL_SLOTS_PRIMAL, TestFunctionsPM.WAND_CORE_SPELL_SLOTS_PRIMAL.getKey());
        registerFunction(context, WAND_CORE_SPELL_SLOTS_DARK_PRIMAL, TestFunctionsPM.WAND_CORE_SPELL_SLOTS_DARK_PRIMAL.getKey());
        registerFunction(context, WAND_CORE_SPELL_SLOTS_PURE_PRIMAL, TestFunctionsPM.WAND_CORE_SPELL_SLOTS_PURE_PRIMAL.getKey());
        registerFunction(context, WAND_CORE_BONUS_SLOT_ACCEPTS_MATCHING_SPELL_OBSIDIAN, TestFunctionsPM.WAND_CORE_BONUS_SLOT_ACCEPTS_MATCHING_SPELL_OBSIDIAN.getKey());
        registerFunction(context, WAND_CORE_BONUS_SLOT_ACCEPTS_MATCHING_SPELL_BONE, TestFunctionsPM.WAND_CORE_BONUS_SLOT_ACCEPTS_MATCHING_SPELL_BONE.getKey());
        registerFunction(context, WAND_CORE_ALIGNED_SOURCES_HEARTWOOD, TestFunctionsPM.WAND_CORE_ALIGNED_SOURCES_HEARTWOOD.getKey());
        registerFunction(context, WAND_CORE_ALIGNED_SOURCES_BONE, TestFunctionsPM.WAND_CORE_ALIGNED_SOURCES_BONE.getKey());
        registerFunction(context, WAND_CORE_ALIGNED_SOURCES_DARK_PRIMAL, TestFunctionsPM.WAND_CORE_ALIGNED_SOURCES_DARK_PRIMAL.getKey());
        registerFunction(context, STAFF_GEM_SETS_MAX_MANA_WIZARD, TestFunctionsPM.STAFF_GEM_SETS_MAX_MANA_WIZARD.getKey());
        registerFunction(context, STAFF_CAP_SETS_BASE_COST_MODIFIER_HEXIUM, TestFunctionsPM.STAFF_CAP_SETS_BASE_COST_MODIFIER_HEXIUM.getKey());
        registerFunction(context, STAFF_CAP_SETS_SIPHON_AMOUNT_HEXIUM, TestFunctionsPM.STAFF_CAP_SETS_SIPHON_AMOUNT_HEXIUM.getKey());
        registerFunction(context, STAFF_CORE_SPELL_SLOTS_PRIMAL, TestFunctionsPM.STAFF_CORE_SPELL_SLOTS_PRIMAL.getKey());
    }

    public static void registerWandManaTests(BootstrapContext<GameTestInstance> context) {
        registerFunction(context, WAND_CAN_GET_AND_ADD_MANA, TestFunctionsPM.WAND_CAN_GET_AND_ADD_MANA.getKey());
        registerFunction(context, WAND_CAN_GET_AND_ADD_REAL_MANA, TestFunctionsPM.WAND_CAN_GET_AND_ADD_REAL_MANA.getKey());
        registerFunction(context, WAND_MANA_CHANGE_DOES_NOT_MUTATE_STACK_COPIES, TestFunctionsPM.WAND_MANA_CHANGE_DOES_NOT_MUTATE_STACK_COPIES.getKey());
        registerFunction(context, WAND_CANNOT_ADD_TOO_MUCH_MANA, TestFunctionsPM.WAND_CANNOT_ADD_TOO_MUCH_MANA.getKey());
        registerFunction(context, WAND_CAN_GET_ALL_MANA, TestFunctionsPM.WAND_CAN_GET_ALL_MANA.getKey());
        registerFunction(context, WAND_CAN_CONSUME_MANA, TestFunctionsPM.WAND_CAN_CONSUME_MANA.getKey());
        registerFunction(context, WAND_CANNOT_CONSUME_MORE_MANA_THAN_IT_HAS, TestFunctionsPM.WAND_CANNOT_CONSUME_MORE_MANA_THAN_IT_HAS.getKey());
        registerFunction(context, WAND_CAN_CONSUME_MULTIPLE_TYPES_OF_MANA, TestFunctionsPM.WAND_CAN_CONSUME_MULTIPLE_TYPES_OF_MANA.getKey());
        registerFunction(context, WAND_CANNOT_CONSUME_MORE_MANA_THAN_IT_HAS_WITH_MULTIPLE_TYPES, TestFunctionsPM.WAND_CANNOT_CONSUME_MORE_MANA_THAN_IT_HAS_WITH_MULTIPLE_TYPES.getKey());
        registerFunction(context, WAND_CAN_REMOVE_MANA_RAW, TestFunctionsPM.WAND_CAN_REMOVE_MANA_RAW.getKey());
        registerFunction(context, WAND_CANNOT_REMOVE_MORE_RAW_MANA_THAN_IT_HAS, TestFunctionsPM.WAND_CANNOT_REMOVE_MORE_RAW_MANA_THAN_IT_HAS.getKey());
        registerFunction(context, WAND_CONTAINS_MANA, TestFunctionsPM.WAND_CONTAINS_MANA.getKey());
        registerFunction(context, WAND_CONTAINS_MANA_LIST, TestFunctionsPM.WAND_CONTAINS_MANA_LIST.getKey());
        registerFunction(context, WAND_CONTAINS_MANA_RAW, TestFunctionsPM.WAND_CONTAINS_MANA_RAW.getKey());
        registerFunction(context, WAND_CAN_GET_AND_ADD_MANA_SEA, TestFunctionsPM.WAND_CAN_GET_AND_ADD_MANA_SEA.getKey());
        registerFunction(context, WAND_CAN_GET_AND_ADD_MANA_SKY, TestFunctionsPM.WAND_CAN_GET_AND_ADD_MANA_SKY.getKey());
        registerFunction(context, WAND_CAN_GET_AND_ADD_MANA_SUN, TestFunctionsPM.WAND_CAN_GET_AND_ADD_MANA_SUN.getKey());
        registerFunction(context, WAND_CAN_GET_AND_ADD_MANA_MOON, TestFunctionsPM.WAND_CAN_GET_AND_ADD_MANA_MOON.getKey());
        registerFunction(context, WAND_CAN_GET_AND_ADD_MANA_BLOOD, TestFunctionsPM.WAND_CAN_GET_AND_ADD_MANA_BLOOD.getKey());
        registerFunction(context, WAND_CAN_GET_AND_ADD_MANA_INFERNAL, TestFunctionsPM.WAND_CAN_GET_AND_ADD_MANA_INFERNAL.getKey());
        registerFunction(context, WAND_CAN_GET_AND_ADD_MANA_VOID, TestFunctionsPM.WAND_CAN_GET_AND_ADD_MANA_VOID.getKey());
        registerFunction(context, WAND_CAN_GET_AND_ADD_MANA_HALLOWED, TestFunctionsPM.WAND_CAN_GET_AND_ADD_MANA_HALLOWED.getKey());
        registerFunction(context, WAND_CANNOT_ADD_TOO_MUCH_MANA_SEA, TestFunctionsPM.WAND_CANNOT_ADD_TOO_MUCH_MANA_SEA.getKey());
        registerFunction(context, WAND_CANNOT_ADD_TOO_MUCH_MANA_SKY, TestFunctionsPM.WAND_CANNOT_ADD_TOO_MUCH_MANA_SKY.getKey());
        registerFunction(context, WAND_CANNOT_ADD_TOO_MUCH_MANA_SUN, TestFunctionsPM.WAND_CANNOT_ADD_TOO_MUCH_MANA_SUN.getKey());
        registerFunction(context, WAND_CANNOT_ADD_TOO_MUCH_MANA_MOON, TestFunctionsPM.WAND_CANNOT_ADD_TOO_MUCH_MANA_MOON.getKey());
        registerFunction(context, WAND_CANNOT_ADD_TOO_MUCH_MANA_BLOOD, TestFunctionsPM.WAND_CANNOT_ADD_TOO_MUCH_MANA_BLOOD.getKey());
        registerFunction(context, WAND_CANNOT_ADD_TOO_MUCH_MANA_INFERNAL, TestFunctionsPM.WAND_CANNOT_ADD_TOO_MUCH_MANA_INFERNAL.getKey());
        registerFunction(context, WAND_CANNOT_ADD_TOO_MUCH_MANA_VOID, TestFunctionsPM.WAND_CANNOT_ADD_TOO_MUCH_MANA_VOID.getKey());
        registerFunction(context, WAND_CANNOT_ADD_TOO_MUCH_MANA_HALLOWED, TestFunctionsPM.WAND_CANNOT_ADD_TOO_MUCH_MANA_HALLOWED.getKey());
        registerFunction(context, WAND_CAN_CONSUME_MANA_SEA, TestFunctionsPM.WAND_CAN_CONSUME_MANA_SEA.getKey());
        registerFunction(context, WAND_CAN_CONSUME_MANA_SKY, TestFunctionsPM.WAND_CAN_CONSUME_MANA_SKY.getKey());
        registerFunction(context, WAND_CAN_CONSUME_MANA_SUN, TestFunctionsPM.WAND_CAN_CONSUME_MANA_SUN.getKey());
        registerFunction(context, WAND_CAN_CONSUME_MANA_MOON, TestFunctionsPM.WAND_CAN_CONSUME_MANA_MOON.getKey());
        registerFunction(context, WAND_CAN_CONSUME_MANA_BLOOD, TestFunctionsPM.WAND_CAN_CONSUME_MANA_BLOOD.getKey());
        registerFunction(context, WAND_CAN_CONSUME_MANA_INFERNAL, TestFunctionsPM.WAND_CAN_CONSUME_MANA_INFERNAL.getKey());
        registerFunction(context, WAND_CAN_CONSUME_MANA_VOID, TestFunctionsPM.WAND_CAN_CONSUME_MANA_VOID.getKey());
        registerFunction(context, WAND_CAN_CONSUME_MANA_HALLOWED, TestFunctionsPM.WAND_CAN_CONSUME_MANA_HALLOWED.getKey());
        registerFunction(context, WAND_CANNOT_CONSUME_MORE_MANA_THAN_IT_HAS_SEA, TestFunctionsPM.WAND_CANNOT_CONSUME_MORE_MANA_THAN_IT_HAS_SEA.getKey());
        registerFunction(context, WAND_CANNOT_CONSUME_MORE_MANA_THAN_IT_HAS_SKY, TestFunctionsPM.WAND_CANNOT_CONSUME_MORE_MANA_THAN_IT_HAS_SKY.getKey());
        registerFunction(context, WAND_CANNOT_CONSUME_MORE_MANA_THAN_IT_HAS_SUN, TestFunctionsPM.WAND_CANNOT_CONSUME_MORE_MANA_THAN_IT_HAS_SUN.getKey());
        registerFunction(context, WAND_CANNOT_CONSUME_MORE_MANA_THAN_IT_HAS_MOON, TestFunctionsPM.WAND_CANNOT_CONSUME_MORE_MANA_THAN_IT_HAS_MOON.getKey());
        registerFunction(context, WAND_CANNOT_CONSUME_MORE_MANA_THAN_IT_HAS_BLOOD, TestFunctionsPM.WAND_CANNOT_CONSUME_MORE_MANA_THAN_IT_HAS_BLOOD.getKey());
        registerFunction(context, WAND_CANNOT_CONSUME_MORE_MANA_THAN_IT_HAS_INFERNAL, TestFunctionsPM.WAND_CANNOT_CONSUME_MORE_MANA_THAN_IT_HAS_INFERNAL.getKey());
        registerFunction(context, WAND_CANNOT_CONSUME_MORE_MANA_THAN_IT_HAS_VOID, TestFunctionsPM.WAND_CANNOT_CONSUME_MORE_MANA_THAN_IT_HAS_VOID.getKey());
        registerFunction(context, WAND_CANNOT_CONSUME_MORE_MANA_THAN_IT_HAS_HALLOWED, TestFunctionsPM.WAND_CANNOT_CONSUME_MORE_MANA_THAN_IT_HAS_HALLOWED.getKey());
        registerFunction(context, WAND_CAN_GET_AND_ADD_MANA_MUNDANE_WAND, TestFunctionsPM.WAND_CAN_GET_AND_ADD_MANA_MUNDANE_WAND.getKey());
        registerFunction(context, WAND_CAN_GET_AND_ADD_MANA_MODULAR_STAFF, TestFunctionsPM.WAND_CAN_GET_AND_ADD_MANA_MODULAR_STAFF.getKey());
        registerFunction(context, WAND_CAN_CONSUME_MANA_MUNDANE_WAND, TestFunctionsPM.WAND_CAN_CONSUME_MANA_MUNDANE_WAND.getKey());
        registerFunction(context, WAND_CAN_CONSUME_MANA_MODULAR_STAFF, TestFunctionsPM.WAND_CAN_CONSUME_MANA_MODULAR_STAFF.getKey());
    }

    public static void registerConcoctionTests(BootstrapContext<GameTestInstance> context) {
        registerFunction(context, CONCOCTION_TYPE_ROUND_TRIP_WATER, TestFunctionsPM.CONCOCTION_TYPE_ROUND_TRIP_WATER.getKey());
        registerFunction(context, CONCOCTION_TYPE_ROUND_TRIP_TINCTURE, TestFunctionsPM.CONCOCTION_TYPE_ROUND_TRIP_TINCTURE.getKey());
        registerFunction(context, CONCOCTION_TYPE_ROUND_TRIP_PHILTER, TestFunctionsPM.CONCOCTION_TYPE_ROUND_TRIP_PHILTER.getKey());
        registerFunction(context, CONCOCTION_TYPE_ROUND_TRIP_ELIXIR, TestFunctionsPM.CONCOCTION_TYPE_ROUND_TRIP_ELIXIR.getKey());
        registerFunction(context, CONCOCTION_TYPE_ROUND_TRIP_BOMB, TestFunctionsPM.CONCOCTION_TYPE_ROUND_TRIP_BOMB.getKey());
        registerFunction(context, CONCOCTION_DOSES_ROUND_TRIP, TestFunctionsPM.CONCOCTION_DOSES_ROUND_TRIP.getKey());
        registerFunction(context, CONCOCTION_DOSES_NEGATIVE_STORED_AS_IS, TestFunctionsPM.CONCOCTION_DOSES_NEGATIVE_STORED_AS_IS.getKey());
        registerFunction(context, CONCOCTION_BOMB_FUSE_ROUND_TRIP_IMPACT, TestFunctionsPM.CONCOCTION_BOMB_FUSE_ROUND_TRIP_IMPACT.getKey());
        registerFunction(context, CONCOCTION_BOMB_FUSE_ROUND_TRIP_SHORT, TestFunctionsPM.CONCOCTION_BOMB_FUSE_ROUND_TRIP_SHORT.getKey());
        registerFunction(context, CONCOCTION_BOMB_FUSE_ROUND_TRIP_MEDIUM, TestFunctionsPM.CONCOCTION_BOMB_FUSE_ROUND_TRIP_MEDIUM.getKey());
        registerFunction(context, CONCOCTION_BOMB_FUSE_ROUND_TRIP_LONG, TestFunctionsPM.CONCOCTION_BOMB_FUSE_ROUND_TRIP_LONG.getKey());
        registerFunction(context, CONCOCTION_BOMB_DEFAULT_FUSE, TestFunctionsPM.CONCOCTION_BOMB_DEFAULT_FUSE.getKey());
        registerFunction(context, CONCOCTION_IS_BOMB, TestFunctionsPM.CONCOCTION_IS_BOMB.getKey());
        registerFunction(context, CONCOCTION_HAS_BENEFICIAL_EFFECT, TestFunctionsPM.CONCOCTION_HAS_BENEFICIAL_EFFECT.getKey());
        registerFunction(context, CONCOCTION_DEFAULTS_ON_NON_CONCOCTION, TestFunctionsPM.CONCOCTION_DEFAULTS_ON_NON_CONCOCTION.getKey());
    }

    public static void registerEssenceTests(BootstrapContext<GameTestInstance> context) {
        registerFunction(context, ESSENCE_ITEM_LOOKUP_DUST, TestFunctionsPM.ESSENCE_ITEM_LOOKUP_DUST.getKey());
        registerFunction(context, ESSENCE_ITEM_LOOKUP_SHARD, TestFunctionsPM.ESSENCE_ITEM_LOOKUP_SHARD.getKey());
        registerFunction(context, ESSENCE_ITEM_LOOKUP_CRYSTAL, TestFunctionsPM.ESSENCE_ITEM_LOOKUP_CRYSTAL.getKey());
        registerFunction(context, ESSENCE_ITEM_LOOKUP_CLUSTER, TestFunctionsPM.ESSENCE_ITEM_LOOKUP_CLUSTER.getKey());
        registerFunction(context, ESSENCE_STACK_LOOKUP, TestFunctionsPM.ESSENCE_STACK_LOOKUP.getKey());
        registerFunction(context, ESSENCE_ALL_ESSENCES_COUNT, TestFunctionsPM.ESSENCE_ALL_ESSENCES_COUNT.getKey());
        registerFunction(context, ESSENCE_TYPE_MANA_EQUIVALENT, TestFunctionsPM.ESSENCE_TYPE_MANA_EQUIVALENT.getKey());
        registerFunction(context, ESSENCE_TYPE_AFFINITY, TestFunctionsPM.ESSENCE_TYPE_AFFINITY.getKey());
        registerFunction(context, ESSENCE_TYPE_UPGRADE_CHAIN, TestFunctionsPM.ESSENCE_TYPE_UPGRADE_CHAIN.getKey());
        registerFunction(context, ESSENCE_TYPE_UPGRADE_MEDIUM, TestFunctionsPM.ESSENCE_TYPE_UPGRADE_MEDIUM.getKey());
        registerFunction(context, ESSENCE_TYPE_UPGRADE_RESEARCH, TestFunctionsPM.ESSENCE_TYPE_UPGRADE_RESEARCH.getKey());
        registerFunction(context, ESSENCE_TYPE_DISCOVERY_DUST, TestFunctionsPM.ESSENCE_TYPE_DISCOVERY_DUST.getKey());
        registerFunction(context, ESSENCE_TYPE_DISCOVERY_SHARD, TestFunctionsPM.ESSENCE_TYPE_DISCOVERY_SHARD.getKey());
        registerFunction(context, ESSENCE_TYPE_DISCOVERY_CRYSTAL, TestFunctionsPM.ESSENCE_TYPE_DISCOVERY_CRYSTAL.getKey());
        registerFunction(context, ESSENCE_TYPE_DISCOVERY_CLUSTER, TestFunctionsPM.ESSENCE_TYPE_DISCOVERY_CLUSTER.getKey());
        registerFunction(context, SOURCE_DISCOVERY_BLOOD, TestFunctionsPM.SOURCE_DISCOVERY_BLOOD.getKey());
        registerFunction(context, SOURCE_DISCOVERY_INFERNAL, TestFunctionsPM.SOURCE_DISCOVERY_INFERNAL.getKey());
        registerFunction(context, SOURCE_DISCOVERY_VOID, TestFunctionsPM.SOURCE_DISCOVERY_VOID.getKey());
        registerFunction(context, SOURCE_DISCOVERY_HALLOWED, TestFunctionsPM.SOURCE_DISCOVERY_HALLOWED.getKey());
        registerFunction(context, SOURCE_DISCOVERY_PRIMAL_SOURCES_ALWAYS_DISCOVERED, TestFunctionsPM.SOURCE_DISCOVERY_PRIMAL_SOURCES_ALWAYS_DISCOVERED.getKey());
    }

    public static void registerDispenserItemTests(BootstrapContext<GameTestInstance> context) {
        registerFunction(context, MANA_ARROWS_FIRED_FROM_DISPENSER_EARTH, TestFunctionsPM.MANA_ARROWS_FIRED_FROM_DISPENSER_EARTH.getKey());
        registerFunction(context, MANA_ARROWS_FIRED_FROM_DISPENSER_SEA, TestFunctionsPM.MANA_ARROWS_FIRED_FROM_DISPENSER_SEA.getKey());
        registerFunction(context, MANA_ARROWS_FIRED_FROM_DISPENSER_SKY, TestFunctionsPM.MANA_ARROWS_FIRED_FROM_DISPENSER_SKY.getKey());
        registerFunction(context, MANA_ARROWS_FIRED_FROM_DISPENSER_SUN, TestFunctionsPM.MANA_ARROWS_FIRED_FROM_DISPENSER_SUN.getKey());
        registerFunction(context, MANA_ARROWS_FIRED_FROM_DISPENSER_MOON, TestFunctionsPM.MANA_ARROWS_FIRED_FROM_DISPENSER_MOON.getKey());
        registerFunction(context, MANA_ARROWS_FIRED_FROM_DISPENSER_BLOOD, TestFunctionsPM.MANA_ARROWS_FIRED_FROM_DISPENSER_BLOOD.getKey());
        registerFunction(context, MANA_ARROWS_FIRED_FROM_DISPENSER_INFERNAL, TestFunctionsPM.MANA_ARROWS_FIRED_FROM_DISPENSER_INFERNAL.getKey());
        registerFunction(context, MANA_ARROWS_FIRED_FROM_DISPENSER_VOID, TestFunctionsPM.MANA_ARROWS_FIRED_FROM_DISPENSER_VOID.getKey());
        registerFunction(context, MANA_ARROWS_FIRED_FROM_DISPENSER_HALLOWED, TestFunctionsPM.MANA_ARROWS_FIRED_FROM_DISPENSER_HALLOWED.getKey());
    }

    public static void registerBeeswaxItemTests(BootstrapContext<GameTestInstance> context) {
        registerFunction(context, APPLY_BEESWAX_DIRECTLY_CLEAN, TestFunctionsPM.APPLY_BEESWAX_DIRECTLY_CLEAN.getKey());
        registerFunction(context, APPLY_BEESWAX_DIRECTLY_EXPOSED, TestFunctionsPM.APPLY_BEESWAX_DIRECTLY_EXPOSED.getKey());
        registerFunction(context, APPLY_BEESWAX_DIRECTLY_WEATHERED, TestFunctionsPM.APPLY_BEESWAX_DIRECTLY_WEATHERED.getKey());
        registerFunction(context, APPLY_BEESWAX_DIRECTLY_OXIDIZED, TestFunctionsPM.APPLY_BEESWAX_DIRECTLY_OXIDIZED.getKey());
        registerFunction(context, APPLY_BEESWAX_VIA_CRAFTING_CLEAN, TestFunctionsPM.APPLY_BEESWAX_VIA_CRAFTING_CLEAN.getKey());
        registerFunction(context, APPLY_BEESWAX_VIA_CRAFTING_EXPOSED, TestFunctionsPM.APPLY_BEESWAX_VIA_CRAFTING_EXPOSED.getKey());
        registerFunction(context, APPLY_BEESWAX_VIA_CRAFTING_WEATHERED, TestFunctionsPM.APPLY_BEESWAX_VIA_CRAFTING_WEATHERED.getKey());
        registerFunction(context, APPLY_BEESWAX_VIA_CRAFTING_OXIDIZED, TestFunctionsPM.APPLY_BEESWAX_VIA_CRAFTING_OXIDIZED.getKey());
    }

    public static void registerFtuxTests(BootstrapContext<GameTestInstance> context) {
        registerFunction(context, FONT_DISCOVERY_EARTH, TestFunctionsPM.FONT_DISCOVERY_EARTH.getKey());
        registerFunction(context, FONT_DISCOVERY_SEA, TestFunctionsPM.FONT_DISCOVERY_SEA.getKey());
        registerFunction(context, FONT_DISCOVERY_SKY, TestFunctionsPM.FONT_DISCOVERY_SKY.getKey());
        registerFunction(context, FONT_DISCOVERY_SUN, TestFunctionsPM.FONT_DISCOVERY_SUN.getKey());
        registerFunction(context, FONT_DISCOVERY_MOON, TestFunctionsPM.FONT_DISCOVERY_MOON.getKey());
        registerFunction(context, SLEEP_AFTER_SHRINE_GRANTS_DREAM, TestFunctionsPM.SLEEP_AFTER_SHRINE_GRANTS_DREAM.getKey(), TestEnvironmentsPM.NIGHTTIME_ENV, ResourceUtils.loc("test/floor5x5x5"));
        registerFunction(context, MUNDANE_WAND_CRAFTING_EARTH, TestFunctionsPM.MUNDANE_WAND_CRAFTING_EARTH.getKey());
        registerFunction(context, MUNDANE_WAND_CRAFTING_SEA, TestFunctionsPM.MUNDANE_WAND_CRAFTING_SEA.getKey());
        registerFunction(context, MUNDANE_WAND_CRAFTING_SKY, TestFunctionsPM.MUNDANE_WAND_CRAFTING_SKY.getKey());
        registerFunction(context, MUNDANE_WAND_CRAFTING_SUN, TestFunctionsPM.MUNDANE_WAND_CRAFTING_SUN.getKey());
        registerFunction(context, MUNDANE_WAND_CRAFTING_MOON, TestFunctionsPM.MUNDANE_WAND_CRAFTING_MOON.getKey());
        registerFunction(context, TRANSFORM_ABORT_GIVES_HINT, TestFunctionsPM.TRANSFORM_ABORT_GIVES_HINT.getKey());
        registerFunction(context, TRANSFORM_WITHOUT_DREAM_DOES_NOTHING, TestFunctionsPM.TRANSFORM_WITHOUT_DREAM_DOES_NOTHING.getKey());
        registerFunction(context, TRANSFORM_GRIMOIRE, TestFunctionsPM.TRANSFORM_GRIMOIRE.getKey());
    }

    public static void registerLootModifierTests(BootstrapContext<GameTestInstance> context) {
        registerFunction(context, LOOT_ADD_ITEM_ADDS_FIXED_ROLLS, TestFunctionsPM.LOOT_ADD_ITEM_ADDS_FIXED_ROLLS.getKey());
        registerFunction(context, LOOT_ADD_ITEM_ZERO_ROLLS_ADDS_NOTHING, TestFunctionsPM.LOOT_ADD_ITEM_ZERO_ROLLS_ADDS_NOTHING.getKey());
        registerFunction(context, LOOT_REPLACE_ITEM_REPLACES_ALL_ENTRIES, TestFunctionsPM.LOOT_REPLACE_ITEM_REPLACES_ALL_ENTRIES.getKey());
        registerFunction(context, LOOT_BLOODY_FLESH_DROPS_FOR_TAGGED_ENTITY_VILLAGER, TestFunctionsPM.LOOT_BLOODY_FLESH_DROPS_FOR_TAGGED_ENTITY_VILLAGER.getKey());
        registerFunction(context, LOOT_BLOODY_FLESH_DROPS_FOR_TAGGED_ENTITY_WITCH, TestFunctionsPM.LOOT_BLOODY_FLESH_DROPS_FOR_TAGGED_ENTITY_WITCH.getKey());
        registerFunction(context, LOOT_BLOODY_FLESH_SKIPS_UNTAGGED_ENTITY, TestFunctionsPM.LOOT_BLOODY_FLESH_SKIPS_UNTAGGED_ENTITY.getKey());
        registerFunction(context, LOOT_BLOOD_NOTES_DROPS_FOR_TAGGED_ENTITY_EVOKER, TestFunctionsPM.LOOT_BLOOD_NOTES_DROPS_FOR_TAGGED_ENTITY_EVOKER.getKey());
        registerFunction(context, LOOT_BLOOD_NOTES_DROPS_FOR_TAGGED_ENTITY_WITCH, TestFunctionsPM.LOOT_BLOOD_NOTES_DROPS_FOR_TAGGED_ENTITY_WITCH.getKey());
        registerFunction(context, LOOT_BLOOD_NOTES_SKIPS_UNTAGGED_ENTITY, TestFunctionsPM.LOOT_BLOOD_NOTES_SKIPS_UNTAGGED_ENTITY.getKey());
        registerFunction(context, LOOT_BONUS_NUGGET_IRON_CHANCE_1, TestFunctionsPM.LOOT_BONUS_NUGGET_IRON_CHANCE_1.getKey());
        registerFunction(context, LOOT_BONUS_NUGGET_QUARTZ_CHANCE_1, TestFunctionsPM.LOOT_BONUS_NUGGET_QUARTZ_CHANCE_1.getKey());
        registerFunction(context, LOOT_BONUS_NUGGET_IRON_CHANCE_0, TestFunctionsPM.LOOT_BONUS_NUGGET_IRON_CHANCE_0.getKey());
        registerFunction(context, LOOT_BONUS_NUGGET_REQUIRES_LUCKY_STRIKE, TestFunctionsPM.LOOT_BONUS_NUGGET_REQUIRES_LUCKY_STRIKE.getKey());
        registerFunction(context, LOOT_BONUS_NUGGET_SKIPS_UNTAGGED_BLOCK, TestFunctionsPM.LOOT_BONUS_NUGGET_SKIPS_UNTAGGED_BLOCK.getKey());
        registerFunction(context, LOOT_BOUNTY_FARMING_CHANCE_1, TestFunctionsPM.LOOT_BOUNTY_FARMING_CHANCE_1.getKey());
        registerFunction(context, LOOT_BOUNTY_FARMING_CHANCE_0, TestFunctionsPM.LOOT_BOUNTY_FARMING_CHANCE_0.getKey());
        registerFunction(context, LOOT_BOUNTY_FISHING_CHANCE_1, TestFunctionsPM.LOOT_BOUNTY_FISHING_CHANCE_1.getKey());
        registerFunction(context, LOOT_BOUNTY_FISHING_CHANCE_0, TestFunctionsPM.LOOT_BOUNTY_FISHING_CHANCE_0.getKey());
        registerFunction(context, LOOT_GUILLOTINE_DROPS_HEAD_FOR_TAGGED_ENTITY_ZOMBIE, TestFunctionsPM.LOOT_GUILLOTINE_DROPS_HEAD_FOR_TAGGED_ENTITY_ZOMBIE.getKey());
        registerFunction(context, LOOT_GUILLOTINE_DROPS_HEAD_FOR_TAGGED_ENTITY_DROWNED, TestFunctionsPM.LOOT_GUILLOTINE_DROPS_HEAD_FOR_TAGGED_ENTITY_DROWNED.getKey());
        registerFunction(context, LOOT_GUILLOTINE_DROPS_HEAD_FOR_TAGGED_ENTITY_SKELETON, TestFunctionsPM.LOOT_GUILLOTINE_DROPS_HEAD_FOR_TAGGED_ENTITY_SKELETON.getKey());
        registerFunction(context, LOOT_GUILLOTINE_DROPS_HEAD_FOR_TAGGED_ENTITY_CREEPER, TestFunctionsPM.LOOT_GUILLOTINE_DROPS_HEAD_FOR_TAGGED_ENTITY_CREEPER.getKey());
        registerFunction(context, LOOT_GUILLOTINE_DROPS_HEAD_SCALES_WITH_LEVEL, TestFunctionsPM.LOOT_GUILLOTINE_DROPS_HEAD_SCALES_WITH_LEVEL.getKey());
        registerFunction(context, LOOT_GUILLOTINE_SKIPS_UNTAGGED_ENTITY, TestFunctionsPM.LOOT_GUILLOTINE_SKIPS_UNTAGGED_ENTITY.getKey());
        registerFunction(context, LOOT_GUILLOTINE_REQUIRES_ENCHANTMENT, TestFunctionsPM.LOOT_GUILLOTINE_REQUIRES_ENCHANTMENT.getKey());
        registerFunction(context, LOOT_GUILLOTINE_SKIPS_EXISTING_HEAD, TestFunctionsPM.LOOT_GUILLOTINE_SKIPS_EXISTING_HEAD.getKey());
        registerFunction(context, LOOT_RELIC_FRAGMENTS_DROPS_FOR_TAGGED_ENTITY_EVOKER, TestFunctionsPM.LOOT_RELIC_FRAGMENTS_DROPS_FOR_TAGGED_ENTITY_EVOKER.getKey());
        registerFunction(context, LOOT_RELIC_FRAGMENTS_DROPS_FOR_TAGGED_ENTITY_ZOMBIE, TestFunctionsPM.LOOT_RELIC_FRAGMENTS_DROPS_FOR_TAGGED_ENTITY_ZOMBIE.getKey());
        registerFunction(context, LOOT_RELIC_FRAGMENTS_SKIPS_UNTAGGED_ENTITY, TestFunctionsPM.LOOT_RELIC_FRAGMENTS_SKIPS_UNTAGGED_ENTITY.getKey());
        registerFunction(context, LOOT_RELIC_FRAGMENTS_COUNT_WITHIN_RANGE, TestFunctionsPM.LOOT_RELIC_FRAGMENTS_COUNT_WITHIN_RANGE.getKey());
        registerFunction(context, LOOT_ESSENCE_THIEF_REQUIRES_ENCHANTMENT, TestFunctionsPM.LOOT_ESSENCE_THIEF_REQUIRES_ENCHANTMENT.getKey());
    }

    public static void registerRuneManagerTests(BootstrapContext<GameTestInstance> context) {
        registerFunction(context, RUNE_ENCHANTMENT_RESOLVES_SHARPNESS, TestFunctionsPM.RUNE_ENCHANTMENT_RESOLVES_SHARPNESS.getKey());
        registerFunction(context, RUNE_ENCHANTMENT_RESOLVES_PROTECTION, TestFunctionsPM.RUNE_ENCHANTMENT_RESOLVES_PROTECTION.getKey());
        registerFunction(context, RUNE_ENCHANTMENT_RESOLVES_FIRE_PROTECTION, TestFunctionsPM.RUNE_ENCHANTMENT_RESOLVES_FIRE_PROTECTION.getKey());
        registerFunction(context, RUNE_ENCHANTMENT_RESOLVES_SMITE, TestFunctionsPM.RUNE_ENCHANTMENT_RESOLVES_SMITE.getKey());
        registerFunction(context, RUNE_ENCHANTMENT_RESOLVES_FROST_WALKER, TestFunctionsPM.RUNE_ENCHANTMENT_RESOLVES_FROST_WALKER.getKey());
        registerFunction(context, RUNE_ENCHANTMENT_RESOLVES_INFINITY, TestFunctionsPM.RUNE_ENCHANTMENT_RESOLVES_INFINITY.getKey());
        registerFunction(context, RUNE_ENCHANTMENT_RESOLVES_MENDING, TestFunctionsPM.RUNE_ENCHANTMENT_RESOLVES_MENDING.getKey());
        registerFunction(context, RUNE_ENCHANTMENT_RESOLVES_LIFESTEAL, TestFunctionsPM.RUNE_ENCHANTMENT_RESOLVES_LIFESTEAL.getKey());
        registerFunction(context, RUNE_ENCHANTMENT_RESOLVES_MANA_EFFICIENCY, TestFunctionsPM.RUNE_ENCHANTMENT_RESOLVES_MANA_EFFICIENCY.getKey());
        registerFunction(context, RUNE_ENCHANTMENT_RESOLVES_AEGIS, TestFunctionsPM.RUNE_ENCHANTMENT_RESOLVES_AEGIS.getKey());
        registerFunction(context, RUNE_ENCHANTMENT_RESOLVES_LUCKY_STRIKE, TestFunctionsPM.RUNE_ENCHANTMENT_RESOLVES_LUCKY_STRIKE.getKey());
        registerFunction(context, RUNE_ENCHANTMENT_REQUIRES_RESEARCH, TestFunctionsPM.RUNE_ENCHANTMENT_REQUIRES_RESEARCH.getKey());
        registerFunction(context, RUNE_ENCHANTMENT_REQUIRES_ENCHANTABLE_STACK, TestFunctionsPM.RUNE_ENCHANTMENT_REQUIRES_ENCHANTABLE_STACK.getKey());
        registerFunction(context, RUNE_ENCHANTMENT_POWER_RUNE_ONE_RUNE_GIVES_LEVEL_2, TestFunctionsPM.RUNE_ENCHANTMENT_POWER_RUNE_ONE_RUNE_GIVES_LEVEL_2.getKey());
        registerFunction(context, RUNE_ENCHANTMENT_POWER_RUNE_TWO_RUNES_GIVES_LEVEL_3, TestFunctionsPM.RUNE_ENCHANTMENT_POWER_RUNE_TWO_RUNES_GIVES_LEVEL_3.getKey());
        registerFunction(context, RUNE_ENCHANTMENT_POWER_RUNE_THREE_RUNES_GIVES_LEVEL_4, TestFunctionsPM.RUNE_ENCHANTMENT_POWER_RUNE_THREE_RUNES_GIVES_LEVEL_4.getKey());
        registerFunction(context, RUNE_ENCHANTMENT_POWER_RUNE_CAPPED_AT_MAX_LEVEL, TestFunctionsPM.RUNE_ENCHANTMENT_POWER_RUNE_CAPPED_AT_MAX_LEVEL.getKey());
        registerFunction(context, RUNE_ENCHANTMENT_COMPETING_UNFILTERED, TestFunctionsPM.RUNE_ENCHANTMENT_COMPETING_UNFILTERED.getKey());
        registerFunction(context, RUNE_ENCHANTMENT_COMPETING_FILTERED, TestFunctionsPM.RUNE_ENCHANTMENT_COMPETING_FILTERED.getKey());
        registerFunction(context, RUNE_ENCHANTMENT_TIE_BROKEN_BY_ID, TestFunctionsPM.RUNE_ENCHANTMENT_TIE_BROKEN_BY_ID.getKey());
        registerFunction(context, RUNE_ENCHANTMENT_EMPTY_INPUTS, TestFunctionsPM.RUNE_ENCHANTMENT_EMPTY_INPUTS.getKey());
        registerFunction(context, RUNE_LIMITS_INSIGHT, TestFunctionsPM.RUNE_LIMITS_INSIGHT.getKey());
        registerFunction(context, RUNE_LIMITS_POWER, TestFunctionsPM.RUNE_LIMITS_POWER.getKey());
        registerFunction(context, RUNE_LIMITS_UNLIMITED_GRACE, TestFunctionsPM.RUNE_LIMITS_UNLIMITED_GRACE.getKey());
        registerFunction(context, RUNE_LIMITS_UNLIMITED_PROJECT, TestFunctionsPM.RUNE_LIMITS_UNLIMITED_PROJECT.getKey());
        registerFunction(context, RUNE_STACK_SET_AND_CLEAR, TestFunctionsPM.RUNE_STACK_SET_AND_CLEAR.getKey());
        registerFunction(context, RUNE_MERGE_ENCHANTMENTS_TAKES_STRONGER, TestFunctionsPM.RUNE_MERGE_ENCHANTMENTS_TAKES_STRONGER.getKey());
        registerFunction(context, RUNE_MERGE_ENCHANTMENTS_SKIPS_INCOMPATIBLE_WITH_ORIGINAL, TestFunctionsPM.RUNE_MERGE_ENCHANTMENTS_SKIPS_INCOMPATIBLE_WITH_ORIGINAL.getKey());
        registerFunction(context, RUNE_MERGE_ENCHANTMENTS_ADDS_MUTUALLY_INCOMPATIBLE_ADDITIONS, TestFunctionsPM.RUNE_MERGE_ENCHANTMENTS_ADDS_MUTUALLY_INCOMPATIBLE_ADDITIONS.getKey());
        registerFunction(context, RUNE_MERGE_ENCHANTMENTS_ADDS_COMPATIBLE, TestFunctionsPM.RUNE_MERGE_ENCHANTMENTS_ADDS_COMPATIBLE.getKey());
        registerFunction(context, RUNE_DEFINITION_LOOKUP, TestFunctionsPM.RUNE_DEFINITION_LOOKUP.getKey());
        registerFunction(context, RUNE_IS_KNOWN_VERB, TestFunctionsPM.RUNE_IS_KNOWN_VERB.getKey());
        registerFunction(context, RUNE_IS_KNOWN_NOUN, TestFunctionsPM.RUNE_IS_KNOWN_NOUN.getKey());
        registerFunction(context, RUNE_IS_KNOWN_SOURCE, TestFunctionsPM.RUNE_IS_KNOWN_SOURCE.getKey());
        registerFunction(context, RUNE_IS_KNOWN_FULL_KEY, TestFunctionsPM.RUNE_IS_KNOWN_FULL_KEY.getKey());
    }

    public static void registerRitualEnchantmentTests(BootstrapContext<GameTestInstance> context) {
        registerFunction(context, ENCHANTMENT_ESSENCE_THIEF1, TestFunctionsPM.ENCHANTMENT_ESSENCE_THIEF1.getKey());
        registerFunction(context, ENCHANTMENT_ESSENCE_THIEF2, TestFunctionsPM.ENCHANTMENT_ESSENCE_THIEF2.getKey());
        registerFunction(context, ENCHANTMENT_ESSENCE_THIEF3, TestFunctionsPM.ENCHANTMENT_ESSENCE_THIEF3.getKey());
        registerFunction(context, ENCHANTMENT_ESSENCE_THIEF4, TestFunctionsPM.ENCHANTMENT_ESSENCE_THIEF4.getKey());
    }

    public static void registerCasterEnchantabilityTests(BootstrapContext<GameTestInstance> context) {
        registerFunction(context, CASTER_ENCHANTABLE_MUNDANE_WAND, TestFunctionsPM.CASTER_ENCHANTABLE_MUNDANE_WAND.getKey());
        registerFunction(context, CASTER_ENCHANTABLE_MODULAR_WAND, TestFunctionsPM.CASTER_ENCHANTABLE_MODULAR_WAND.getKey());
        registerFunction(context, CASTER_ENCHANTABLE_MODULAR_STAFF, TestFunctionsPM.CASTER_ENCHANTABLE_MODULAR_STAFF.getKey());
    }

    private static void registerRunecarvingTests(BootstrapContext<GameTestInstance> context) {
        registerFunction(context, RUNECARVING_CRAFT_WORKS, TestFunctionsPM.RUNECARVING_CRAFT_WORKS.getKey());
    }

    private static void registerWandInscriptionTests(BootstrapContext<GameTestInstance> context) {
        registerFunction(context, WAND_INSCRIPTION_INSCRIBES_SCROLL_INTO_WAND, TestFunctionsPM.WAND_INSCRIPTION_INSCRIBES_SCROLL_INTO_WAND.getKey());
        registerFunction(context, WAND_INSCRIPTION_INSCRIBES_SCROLL_INTO_SPELLTOME, TestFunctionsPM.WAND_INSCRIPTION_INSCRIBES_SCROLL_INTO_SPELLTOME.getKey());
        registerFunction(context, WAND_INSCRIPTION_REJECTS_FULL_CONTAINER, TestFunctionsPM.WAND_INSCRIPTION_REJECTS_FULL_CONTAINER.getKey());
        registerFunction(context, WAND_INSCRIPTION_CLEARS_SPELLS_WITHOUT_SCROLL, TestFunctionsPM.WAND_INSCRIPTION_CLEARS_SPELLS_WITHOUT_SCROLL.getKey());
        registerFunction(context, WAND_INSCRIPTION_EMPTY_CASTER_WITHOUT_SCROLL_GIVES_NOTHING, TestFunctionsPM.WAND_INSCRIPTION_EMPTY_CASTER_WITHOUT_SCROLL_GIVES_NOTHING.getKey());
        registerFunction(context, WAND_INSCRIPTION_APPENDS_SPELL_TO_PARTIALLY_FILLED_SPELLTOME, TestFunctionsPM.WAND_INSCRIPTION_APPENDS_SPELL_TO_PARTIALLY_FILLED_SPELLTOME.getKey());
        registerFunction(context, WAND_INSCRIPTION_ALL_RECIPE_KEYS_RESOLVE, TestFunctionsPM.WAND_INSCRIPTION_ALL_RECIPE_KEYS_RESOLVE.getKey());
    }

    private static void registerRunescribingResultTests(BootstrapContext<GameTestInstance> context) {
        registerFunction(context, RUNESCRIBING_CREDIT_ONLY_APPLIED_ENCHANTMENTS, TestFunctionsPM.RUNESCRIBING_CREDIT_ONLY_APPLIED_ENCHANTMENTS.getKey());
        registerFunction(context, RUNESCRIBING_CREDIT_ALL_APPLIED_ENCHANTMENTS, TestFunctionsPM.RUNESCRIBING_CREDIT_ALL_APPLIED_ENCHANTMENTS.getKey());
        registerFunction(context, RUNESCRIBING_CREDIT_SKIPS_ENCHANTMENT_BLOCKED_BY_EXISTING, TestFunctionsPM.RUNESCRIBING_CREDIT_SKIPS_ENCHANTMENT_BLOCKED_BY_EXISTING.getKey());
        registerFunction(context, RUNESCRIBING_CREDIT_SKIPS_PREEXISTING_ENCHANTMENT_MATCHING_RUNES, TestFunctionsPM.RUNESCRIBING_CREDIT_SKIPS_PREEXISTING_ENCHANTMENT_MATCHING_RUNES.getKey());
    }

    private static void registerRepairTests(BootstrapContext<GameTestInstance> context) {
        registerFunction(context, EARTHSHATTER_HAMMER_CANNOT_BE_REPAIRED, TestFunctionsPM.EARTHSHATTER_HAMMER_CANNOT_BE_REPAIRED.getKey());
    }

    private static void registerEarthshatterHammerTests(BootstrapContext<GameTestInstance> context) {
        registerFunction(context, EARTHSHATTER_HAMMER_RECIPE_RETURNS_HAMMER, TestFunctionsPM.EARTHSHATTER_HAMMER_RECIPE_RETURNS_HAMMER.getKey());
        registerFunction(context, EARTHSHATTER_HAMMER_HAS_NO_DURABILITY, TestFunctionsPM.EARTHSHATTER_HAMMER_HAS_NO_DURABILITY.getKey());
        registerFunction(context, EARTHSHATTER_HAMMER_REMAINDER_KEEPS_COMPONENTS, TestFunctionsPM.EARTHSHATTER_HAMMER_REMAINDER_KEEPS_COMPONENTS.getKey());
        registerFunction(context, EARTHSHATTER_HAMMER_REMAINDER_COUNT_IS_ONE_FOR_STACKED_INPUT, TestFunctionsPM.EARTHSHATTER_HAMMER_REMAINDER_COUNT_IS_ONE_FOR_STACKED_INPUT.getKey());
    }

    private static void registerCraftingRequirementTests(BootstrapContext<GameTestInstance> context) {
        registerFunction(context, CRAFTING_REQUIREMENT_ARCANE_RECIPE, TestFunctionsPM.CRAFTING_REQUIREMENT_ARCANE_RECIPE.getKey());
        registerFunction(context, CRAFTING_REQUIREMENT_RITUAL_RECIPE, TestFunctionsPM.CRAFTING_REQUIREMENT_RITUAL_RECIPE.getKey());
    }

    private static void registerCalcinatorTests(BootstrapContext<GameTestInstance> context) {
        registerFunction(context, CALCINATOR_WORKS_WITH_PLAYER_PRESENT, TestFunctionsPM.CALCINATOR_WORKS_WITH_PLAYER_PRESENT.getKey());
        registerFunction(context, CALCINATOR_WORKS_WITHOUT_PLAYER_PRESENT, TestFunctionsPM.CALCINATOR_WORKS_WITHOUT_PLAYER_PRESENT.getKey());
        registerFunction(context, CALCINATOR_LAVA_BUCKET_FUEL_LEAVES_EMPTY_BUCKET, TestFunctionsPM.CALCINATOR_LAVA_BUCKET_FUEL_LEAVES_EMPTY_BUCKET.getKey());
    }

    private static void registerArcaneWorkbenchTests(BootstrapContext<GameTestInstance> context) {
        registerFunction(context, ARCANE_WORKBENCH_CRAFT_WORKS, TestFunctionsPM.ARCANE_WORKBENCH_CRAFT_WORKS.getKey());
    }

    private static void registerPlayerKnowledgeTests(BootstrapContext<GameTestInstance> context) {
        registerFunction(context, ADD_AND_CHECK_RESEARCH, TestFunctionsPM.ADD_AND_CHECK_RESEARCH.getKey());
        registerFunction(context, CANNOT_ADD_DUPLICATE_RESEARCH, TestFunctionsPM.CANNOT_ADD_DUPLICATE_RESEARCH.getKey());
        registerFunction(context, REMOVE_RESEARCH, TestFunctionsPM.REMOVE_RESEARCH.getKey());
        registerFunction(context, GET_RESEARCH_SET, TestFunctionsPM.GET_RESEARCH_SET.getKey());
        registerFunction(context, GET_SET_RESEARCH_STAGE, TestFunctionsPM.GET_SET_RESEARCH_STAGE.getKey());
        registerFunction(context, GET_SET_RESEARCH_FLAG, TestFunctionsPM.GET_SET_RESEARCH_FLAG.getKey());
        registerFunction(context, REMOVE_RESEARCH_FLAG, TestFunctionsPM.REMOVE_RESEARCH_FLAG.getKey());
        registerFunction(context, GET_RESEARCH_FLAGS, TestFunctionsPM.GET_RESEARCH_FLAGS.getKey());
        registerFunction(context, GET_RESEARCH_STATUS, TestFunctionsPM.GET_RESEARCH_STATUS.getKey());
        registerFunction(context, IS_RESEARCH_COMPLETE, TestFunctionsPM.IS_RESEARCH_COMPLETE.getKey());
        registerFunction(context, GET_SET_KNOWLEDGE_RAW, TestFunctionsPM.GET_SET_KNOWLEDGE_RAW.getKey());
        registerFunction(context, GET_KNOWLEDGE_LEVELS, TestFunctionsPM.GET_KNOWLEDGE_LEVELS.getKey());
        registerFunction(context, GET_SET_ACTIVE_RESEARCH_PROJECT, TestFunctionsPM.GET_SET_ACTIVE_RESEARCH_PROJECT.getKey());
        registerFunction(context, GET_SET_LAST_RESEARCH_TOPIC, TestFunctionsPM.GET_SET_LAST_RESEARCH_TOPIC.getKey());
        registerFunction(context, GET_SET_RESEARCH_TOPIC_HISTORY, TestFunctionsPM.GET_SET_RESEARCH_TOPIC_HISTORY.getKey());
        registerFunction(context, KNOWLEDGE_SERIALIZATION, TestFunctionsPM.KNOWLEDGE_SERIALIZATION.getKey());
        registerFunction(context, ADD_AND_CHECK_RESEARCH_POST_SERIALIZATION, TestFunctionsPM.ADD_AND_CHECK_RESEARCH_POST_SERIALIZATION.getKey());
    }

    private static void registerItemHandlerTests(BootstrapContext<GameTestInstance> context) {
        registerFunction(context, ITEM_HANDLER_NULL_DIRECTION_RESEARCH_TABLE, TestFunctionsPM.ITEM_HANDLER_NULL_DIRECTION_RESEARCH_TABLE.getKey());
        registerFunction(context, ITEM_HANDLER_NULL_DIRECTION_WAND_CHARGER, TestFunctionsPM.ITEM_HANDLER_NULL_DIRECTION_WAND_CHARGER.getKey());
        registerFunction(context, ITEM_HANDLER_NULL_DIRECTION_CALCINATOR_BASIC, TestFunctionsPM.ITEM_HANDLER_NULL_DIRECTION_CALCINATOR_BASIC.getKey());
    }

    private static void registerItemHandlerCapabilityTests(BootstrapContext<GameTestInstance> context) {
        registerFunction(context, ITEM_HANDLER_CAPABILITY_REJECTS_INVALID_ITEM_FOR_ALTAR_OUTPUT, TestFunctionsPM.ITEM_HANDLER_CAPABILITY_REJECTS_INVALID_ITEM_FOR_ALTAR_OUTPUT.getKey());
        registerFunction(context, ITEM_HANDLER_CAPABILITY_ENFORCES_SLOT_LIMIT_ON_OFFERING_PEDESTAL, TestFunctionsPM.ITEM_HANDLER_CAPABILITY_ENFORCES_SLOT_LIMIT_ON_OFFERING_PEDESTAL.getKey());
        registerFunction(context, ITEM_HANDLER_CAPABILITY_ACCEPTS_VALID_ITEM, TestFunctionsPM.ITEM_HANDLER_CAPABILITY_ACCEPTS_VALID_ITEM.getKey());
    }

    private static void registerAttunementBuffTests(BootstrapContext<GameTestInstance> context) {
        registerFunction(context, MINOR_ATTUNEMENT_DISCOUNT_EARTH, TestFunctionsPM.MINOR_ATTUNEMENT_DISCOUNT_EARTH.getKey());
        registerFunction(context, MINOR_ATTUNEMENT_DISCOUNT_SEA, TestFunctionsPM.MINOR_ATTUNEMENT_DISCOUNT_SEA.getKey());
        registerFunction(context, MINOR_ATTUNEMENT_DISCOUNT_SKY, TestFunctionsPM.MINOR_ATTUNEMENT_DISCOUNT_SKY.getKey());
        registerFunction(context, MINOR_ATTUNEMENT_DISCOUNT_SUN, TestFunctionsPM.MINOR_ATTUNEMENT_DISCOUNT_SUN.getKey());
        registerFunction(context, MINOR_ATTUNEMENT_DISCOUNT_MOON, TestFunctionsPM.MINOR_ATTUNEMENT_DISCOUNT_MOON.getKey());
        registerFunction(context, MINOR_ATTUNEMENT_DISCOUNT_BLOOD, TestFunctionsPM.MINOR_ATTUNEMENT_DISCOUNT_BLOOD.getKey());
        registerFunction(context, MINOR_ATTUNEMENT_DISCOUNT_INFERNAL, TestFunctionsPM.MINOR_ATTUNEMENT_DISCOUNT_INFERNAL.getKey());
        registerFunction(context, MINOR_ATTUNEMENT_DISCOUNT_VOID, TestFunctionsPM.MINOR_ATTUNEMENT_DISCOUNT_VOID.getKey());
        registerFunction(context, MINOR_ATTUNEMENT_DISCOUNT_HALLOWED, TestFunctionsPM.MINOR_ATTUNEMENT_DISCOUNT_HALLOWED.getKey());
        registerFunction(context, LESSER_EARTH_ATTUNEMENT_BUFF, TestFunctionsPM.LESSER_EARTH_ATTUNEMENT_BUFF.getKey());
        registerFunction(context, GREATER_EARTH_ATTUNEMENT_BUFF, TestFunctionsPM.GREATER_EARTH_ATTUNEMENT_BUFF.getKey());
        registerFunction(context, LESSER_SEA_ATTUNEMENT_BUFF, TestFunctionsPM.LESSER_SEA_ATTUNEMENT_BUFF.getKey());
        registerFunction(context, GREATER_SEA_ATTUNEMENT_BUFF, TestFunctionsPM.GREATER_SEA_ATTUNEMENT_BUFF.getKey());
        registerFunction(context, LESSER_SKY_ATTUNEMENT_BUFF1, TestFunctionsPM.LESSER_SKY_ATTUNEMENT_BUFF1.getKey());
        registerFunction(context, LESSER_SKY_ATTUNEMENT_BUFF2, TestFunctionsPM.LESSER_SKY_ATTUNEMENT_BUFF2.getKey());
        registerFunction(context, GREATER_SKY_ATTUNEMENT_BUFF, TestFunctionsPM.GREATER_SKY_ATTUNEMENT_BUFF.getKey());
        registerFunction(context, LESSER_SUN_ATTUNEMENT_DAY_BUFF, TestFunctionsPM.LESSER_SUN_ATTUNEMENT_DAY_BUFF.getKey(), TestEnvironmentsPM.DAYTIME_ENV);
        registerFunction(context, LESSER_SUN_ATTUNEMENT_NIGHT_BUFF, TestFunctionsPM.LESSER_SUN_ATTUNEMENT_NIGHT_BUFF.getKey(), TestEnvironmentsPM.NIGHTTIME_ENV);
        registerFunction(context, LESSER_MOON_ATTUNEMENT_BUFF, TestFunctionsPM.LESSER_MOON_ATTUNEMENT_BUFF.getKey());
        registerFunction(context, GREATER_MOON_ATTUNEMENT_BUFF, TestFunctionsPM.GREATER_MOON_ATTUNEMENT_BUFF.getKey());
        registerFunction(context, LESSER_BLOOD_ATTUNEMENT_BUFF, TestFunctionsPM.LESSER_BLOOD_ATTUNEMENT_BUFF.getKey());
        registerFunction(context, GREATER_BLOOD_ATTUNEMENT_BUFF, TestFunctionsPM.GREATER_BLOOD_ATTUNEMENT_BUFF.getKey());
        registerFunction(context, GREATER_INFERNAL_ATTUNEMENT_BUFF_IN_FIRE, TestFunctionsPM.GREATER_INFERNAL_ATTUNEMENT_BUFF_IN_FIRE.getKey());
        registerFunction(context, GREATER_INFERNAL_ATTUNEMENT_BUFF_ON_FIRE, TestFunctionsPM.GREATER_INFERNAL_ATTUNEMENT_BUFF_ON_FIRE.getKey());
        registerFunction(context, GREATER_INFERNAL_ATTUNEMENT_BUFF_LAVA, TestFunctionsPM.GREATER_INFERNAL_ATTUNEMENT_BUFF_LAVA.getKey());
        registerFunction(context, GREATER_INFERNAL_ATTUNEMENT_BUFF_HOT_FLOOR, TestFunctionsPM.GREATER_INFERNAL_ATTUNEMENT_BUFF_HOT_FLOOR.getKey());
        registerFunction(context, GREATER_INFERNAL_ATTUNEMENT_BUFF_INFERNAL_SORCERY, TestFunctionsPM.GREATER_INFERNAL_ATTUNEMENT_BUFF_INFERNAL_SORCERY.getKey());
        registerFunction(context, LESSER_VOID_ATTUNEMENT_BUFF, TestFunctionsPM.LESSER_VOID_ATTUNEMENT_BUFF.getKey());
        registerFunction(context, GREATER_VOID_ATTUNEMENT_BUFF, TestFunctionsPM.GREATER_VOID_ATTUNEMENT_BUFF.getKey());
        registerFunction(context, LESSER_HALLOWED_ATTUNEMENT_BUFF, TestFunctionsPM.LESSER_HALLOWED_ATTUNEMENT_BUFF.getKey());
        registerFunction(context, GREATER_HALLOWED_ATTUNEMENT_BUFF, TestFunctionsPM.GREATER_HALLOWED_ATTUNEMENT_BUFF.getKey());
    }

    private static ResourceKey<GameTestInstance> createInstanceKey(String name) {
        return ResourceKey.create(Registries.TEST_INSTANCE, ResourceUtils.loc(name));
    }

    private static Holder.Reference<GameTestInstance> registerFunction(BootstrapContext<GameTestInstance> context,
                                                                       ResourceKey<GameTestInstance> instanceKey,
                                                                       ResourceKey<Consumer<GameTestHelper>> funcKey) {
        return registerFunction(context, instanceKey, funcKey, TestEnvironmentsPM.DEFAULT, TestUtils.DEFAULT_TEMPLATE);
    }

    private static Holder.Reference<GameTestInstance> registerFunction(BootstrapContext<GameTestInstance> context,
                                                                       ResourceKey<GameTestInstance> instanceKey,
                                                                       ResourceKey<Consumer<GameTestHelper>> funcKey,
                                                                       ResourceKey<TestEnvironmentDefinition<?>> envKey) {
        return registerFunction(context, instanceKey, funcKey, envKey, TestUtils.DEFAULT_TEMPLATE);
    }


    private static Holder.Reference<GameTestInstance> registerFunction(BootstrapContext<GameTestInstance> context,
                                                                       ResourceKey<GameTestInstance> instanceKey,
                                                                       ResourceKey<Consumer<GameTestHelper>> funcKey,
                                                                       Identifier templateLoc) {
        return registerFunction(context, instanceKey, funcKey, TestEnvironmentsPM.DEFAULT, templateLoc);
    }

    private static Holder.Reference<GameTestInstance> registerFunction(BootstrapContext<GameTestInstance> context,
                                                                       ResourceKey<GameTestInstance> instanceKey,
                                                                       ResourceKey<Consumer<GameTestHelper>> funcKey,
                                                                       ResourceKey<TestEnvironmentDefinition<?>> envKey,
                                                                       Identifier templateLoc) {
        HolderGetter<TestEnvironmentDefinition<?>> envs = context.lookup(Registries.TEST_ENVIRONMENT);
        return registerFunction(context, instanceKey, funcKey, TestDataBuilder.withEnvironment(envKey, envs).template(templateLoc).build());
    }

    private static Holder.Reference<GameTestInstance> registerFunction(BootstrapContext<GameTestInstance> context,
                                                                       ResourceKey<GameTestInstance> instanceKey,
                                                                       ResourceKey<Consumer<GameTestHelper>> funcKey,
                                                                       TestData<Holder<TestEnvironmentDefinition<?>>> testData) {
        return context.register(instanceKey, new FunctionGameTestInstance(funcKey, testData));
    }
}

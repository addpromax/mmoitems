package net.Indyuce.mmoitems.stat.type;

import com.google.gson.*;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackCategory;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackProvider;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.util.message.FriendlyFeedbackPalette_MMOItems;
import net.Indyuce.mmoitems.stat.data.*;
import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.data.type.UpgradeInfo;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * The primordial problem is removing Gem Stones.
 * <p></p>
 * To achieve this, we must know which stat is from the item originally, vs which stats were given to it from each gem stone.
 * We must also account for weapon upgrades and such.
 * <p></p>
 * This class will store the different sources of each stat UPON being modified.
 */
@SuppressWarnings({"unused", "unchecked", "SpellCheckingInspection"})
public class StatHistory<S extends StatData> {

    /*
     * Which stat is this the history of?
     */
    @NotNull ItemStat itemStat;

    /**
     * Which stat is this the history of?
     */
    @NotNull public ItemStat getItemStat() { return itemStat; }

    /*
     * What MMOItem is this StatHistory linked to?
     */
    @NotNull MMOItem parent;

    /**
     * What MMOItem is this StatHistory linked to?
     */
    @NotNull public MMOItem getMMOItem() { return parent; }

    /*
     * The first value ever recorded of this stat, in this item.
     * Presumably from when it was first generated.
     */
    @NotNull S originalData;

    /**
     * The first value ever recorded of this stat, in this item.
     * Presumably from when it was first generated.
     */
    @NotNull public S getOriginalData() { return originalData; }

    /*
     * The final modifier being provided by each gemstone.
     * GemStones may have scaled with upgrades, that will be accounted for.
     */
    @NotNull public HashMap<UUID, S> perGemstoneData = new HashMap<>();

    /**
     * The final modifier being provided by each gemstone.
     * GemStones may have scaled with upgrades, that will be accounted for.
     */
    @NotNull public S getGemstoneData(UUID of) { return perGemstoneData.get(of); }

    /**
     * All the Stat Datas provided by GemStones
     */
    @NotNull public ArrayList<UUID> getAllGemstones() { return new ArrayList<>(perGemstoneData.keySet()); }

    /**
     * The final modifier being provided by each gemstone.
     * GemStones may have scaled with upgrades, that will be accounted for.
     * <p></p>
     * Basically, supposing this stands for a double data like <i>Attack Damage</i>:
     * <p>originally <code>+5</code>, now at level 2, with <code>+0.25</code> per level</p>
     * The value of this stat data will be <b><code>+5.5</code></b>
     */
    public void registerGemstoneData(@NotNull UUID of, @NotNull S data) { perGemstoneData.put(of, data); }

    /*
     * Modifiers of unknown origin.
     * Presumably put here by external plugins I guess.
     */
    @NotNull ArrayList<S> perExternalData = new ArrayList<>();

    /**
     * Modifiers of unknown origin.
     * Presumably put here by external plugins I guess.
     * <p></p>
     * This returns the actual list, so modifying it will modify the 'external data'
     * <p></p>
     * <i>External Data</i> is just a fancy name for '<i>GemStones with no UUID</i>'
     * <p>They act as gem stones, adding together to produce the total of the item, but cannot be removed natively, since there is no way to tell them from each other.</p>
     * Well, I guess whatever plugin is putting them here may remove them by editing the list directly with <code>StatHistory.getExternalData()</code>
     */
    @NotNull public ArrayList<S> getExternalData() { return perExternalData; }

    /**
     * Modifiers of unknown origin.
     * Presumably put here by external plugins I guess.
     * <p></p>
     * <i>External Data</i> is just a fancy name for '<i>GemStones with no UUID</i>'
     * <p>They act as gem stones, adding together to produce the total of the item, but cannot be removed, since there is no way to tell them from each other.</p>
     * Well, I guess whatever plugin is putting them here may remove them by editing the list directly with <code>StatHistory.getExternalData()</code>
     */
    public void registerExternalData(@NotNull S data) { perExternalData.add(data); }

    /**
     * Gets the stat history of this item. <b>The stat must be <code>Mergeable</code></b>
     * <p></p>
     * If the item has no stat history, it will be created anew and appended; the current stat values will become the 'Original' ones,
     * and will be forever unchangeable.
     * <p></p>
     * <b>Make sure the item has the stat present</b>
     */
    @NotNull public static StatHistory<StatData> From(@NotNull MMOItem ofItem, @NotNull ItemStat ofStat) {

        // Get history :B
        StatHistory<StatData> hist = ofItem.getStatHistory(ofStat);

        // Found? Thats it
        if (hist != null) {
            //UPGRD//MMOItems.Log("Found Stat History of \u00a76" + ofStat.getNBTPath() + "\u00a77 in this \u00a7c" + ofItem.getType().getName() + " " + ofItem.getId());
            return hist; }
        //UPGRD//MMOItems.Log("\u00a7aCreated Hisotry of \u00a76" + ofStat.getNBTPath() + "\u00a7a of this \u00a7c" + ofItem.getType().getName() + " " + ofItem.getId());

        // That is Mergeable right...
        Validate.isTrue(ofStat.getClearStatData() instanceof Mergeable, "Non-Mergeable stat data wont have a Stat History; they cannot be modified dynamically in the first place.");

        // Get original data
        StatData original = ofItem.getData(ofStat);
        if (original == null) {
            original = ofStat.getClearStatData();
            ofItem.setData(ofStat, original);
            //UPGRD//MMOItems.Log("\u00a7e   +\u00a77 Item didnt have this stat, original set as blanc.");
        }
        else {
            original = ((Mergeable) original).cloneData();
            //UPGRD//MMOItems.Log("\u00a7a   +\u00a77 Found original data");
        }

        // Create new
        hist = new StatHistory<>(ofItem, ofStat, original);

        // Append to the item
        ofItem.setStatHistory(ofStat, hist);

        // Thats it
        return hist;
    }

    /**
     * Simplemost constructor, shall never be actually called outside this class.
     * <p></p>
     * Use <code>StatHistory.From()</code> to get the stat history associated to an item.
     */
    StatHistory(@NotNull MMOItem ofItem, @NotNull ItemStat ofStat, @NotNull S ogData) { itemStat = ofStat; originalData = ogData; parent = ofItem; }

    /**
     * Checks the item and makes sure that the UUIDs
     * attributed to gemstones link to existing gem
     * stones. Removes them if no such gemstone exists.
     */
    public void PurgeGemstones() {

        // Which will get purged...
        ArrayList<UUID> extraneous = new ArrayList<>();
        GemSocketsData data = (GemSocketsData) getMMOItem().getData(ItemStats.GEM_SOCKETS);
        if (data == null) { data = new GemSocketsData(new ArrayList<>()); }

        // For each UUID
        for (UUID gem : perGemstoneData.keySet()) {

            // Check Gemstones
            boolean success = false;
            for (GemstoneData indiv : data.getGemstones()) {

                // Not null
                if (indiv != null) {

                    // Equal in UUID
                    if (gem.equals(indiv.getHistoricUUID())) {

                        success = true;
                        break;
                    }
                }
            }

            // No success?
            if (!success) {

                // No gemstone matched
                extraneous.add(gem);
            }
        }

        // Unregister
        for (UUID ext : extraneous) {
            //UPGRD//MMOItems.Log("\u00a76 ||\u00a77 Purged Stone: \u00a7e" + ext.toString());

            // Remove
            perGemstoneData.remove(ext);
        }
    }

    /**
     * This recalculates final value of the stats of the item.
     * <p></p>
     * This will not apply the changes, it will just give you the final
     * <code>StatData</code> that shall be applied (used when upgrading).
     */
    @NotNull public S Recalculate() { return Recalculate(true); }
    /**
     * This recalculates final value of the stats of the item.
     * <p></p>
     * This will not apply the changes, it will just give you the final
     * <code>StatData</code> that shall be applied (used when upgrading).
     * @param withPurge Check if the gemstones UUIDs are valid.
     *                  Leave <code>true</code> unless you know
     *                  what you're doing.
     */
    @NotNull public S Recalculate(boolean withPurge) {
        if (withPurge) { PurgeGemstones(); }

        // If its upgradeable and not level ZERO, it must apply upgrades
        if ((getMMOItem().getUpgradeLevel() != 0)  &&
            (getItemStat() instanceof Upgradable) &&
            (getMMOItem().hasUpgradeTemplate())) {


            // Recalculate upgrading
            return Recalculate_AsUpgradeable();
        }

        // Merge Normally
        return Recalculate_ThroughClone();
    }

    /**
     * This recalculates values accounting only for gemstones and external data.
     * <p></p>
     * In case someone was wondered the contribution of upgrading the item, just
     * substract it from {@link #Recalculate()}
     */
    @NotNull public S Recalculate_Unupgraded() { return Recalculate_Unupgraded(true); }

    /**
     * This recalculates values accounting only for gemstones and external data.
     * <p></p>
     * In case someone was wondered the contribution of upgrading the item, just
     * substract it from {@link #Recalculate()}
     * @param withPurge Check if the gemstones UUIDs are valid.
     *                  Leave <code>true</code> unless you know
     *                  what you're doing.
     */
    @NotNull public S Recalculate_Unupgraded(boolean withPurge) {
        if (withPurge) { PurgeGemstones(); }

        // Merge Normally
        return Recalculate_ThroughClone();
    }

    /**
     * This recalculates final value of the stats of the item.
     * <p></p>
     * That is, it (in this order):
     * <p>1: Starts out with a fresh (empty) data
     * </p>2: Sums the original values
     * <p>3: Scales to current Upgrade Level
     * </p>4: Sums Gem Stone Data (which should be scaled accordingly [Upgrades are entirely merged into their data])
     * <p>5: Sums external data (modifiers that are not linked to an ID, I suppose by external plugins).
     */
    private S Recalculate_AsUpgradeable() {
        //UPGRD//MMOItems.Log("\u00a76|||\u00a77 Calculating \u00a7f" + getItemStat().getNBTPath() + "\u00a77 as Upgradeable");

        // Get Upgrade Info?
        UpgradeInfo inf = getMMOItem().getUpgradeTemplate().getUpgradeInfo(getItemStat());

        // No Upgrade Information? Looks like you're calculating as a normal merge stat
        if (inf == null) { return Recalculate_ThroughClone(); }

        // Clone original
        StatData ogCloned = ((Mergeable) originalData).cloneData();

        // Level up
        int lvl = getMMOItem().getUpgradeLevel();
        //UPGRD//MMOItems.Log("\u00a76 ||\u00a77 Item Level: \u00a7e" + lvl);
        //UPGRD//MMOItems. Log("\u00a76  >\u00a77 Original Base: \u00a7e" + ((DoubleData) ogCloned).getValue());
        S ret = (S) ((Upgradable) getItemStat()).apply(ogCloned, inf, lvl);
        //UPGRD//MMOItems. Log("\u00a76  >\u00a77 Leveled Base: \u00a7e" + ((DoubleData) ret).getValue());

        // Add up gemstones
        for (UUID d : perGemstoneData.keySet()) {

            // Identify insertion level (When was the gemstone put into the item?
            int level = 0;

            // Whats this gemstone's upgrade level?
            for (GemstoneData gData : getMMOItem().getGemStones()) {

                // Find that one of matching UUID
                if (gData.getHistoricUUID().equals(d)) {

                    if (gData.isScaling()) {

                        // Ok
                        level = gData.getLevel();

                    } else {

                        // No scaling
                        level = lvl;
                    }
                }
            }

            // Calculate level difference
            int gLevel = lvl - level;
            //UPGRD//MMOItems.Log("\u00a76 |\u00a7b|\u00a76>\u00a77 Gemstone Level: \u00a7e" + gLevel + "\u00a77 (Put at \u00a7b" + level + "\u00a77)");

            //UPGRD//MMOItems. Log("\u00a76  \u00a7b|>\u00a77 Gemstone Base: \u00a7e" + ((DoubleData) getGemstoneData(d)).getValue());
            // Apply upgrades
            StatData gRet = ((Upgradable) getItemStat()).apply(((Mergeable) getGemstoneData(d)).cloneData(), inf, gLevel);
            //UPGRD//MMOItems. Log("\u00a76  \u00a7b|>\u00a77 Leveled Base: \u00a7e" + ((DoubleData) gRet).getValue());

            // Merge
            ((Mergeable) ret).merge(gRet);
        }

        // Add up externals
        for (S d : perExternalData) {

            //UPGRD//MMOItems. Log("\u00a76  >\u00a7c> \u00a77 Extraneous Base: \u00a7e" + ((DoubleData) d).getValue());
            // Just merge ig
            ((Mergeable) ret).merge(((Mergeable) d).cloneData());
        }

        // Return result
        //UPGRD//MMOItems. Log("\u00a76:::\u00a77 Result: \u00a7e" + ((DoubleData) ret).getValue());
        return ret;
    }

    /**
     * This recalculates final value of the stats of the item.
     * <p></p>
     * That is, it (in this order):
     * <p>1: Starts out with a fresh (empty) data
     * </p>2: Sums the original values
     * </p>3: Sums Gem Stone Data (which should be scaled accordingly [Upgrades are entirely merged into their data])
     * <p>4: Sums external data (modifiers that are not linked to an ID, I suppose by external plugins).
     */
    private S Recalculate_ThroughClone() {
        //UPGRD//MMOItems.Log("\u00a73|||\u00a77 Calculating \u00a7f" + getItemStat().getNBTPath() + "\u00a77 as Clonium");

        // Just clone bro
        S ret = (S) ((Mergeable) getOriginalData()).cloneData();
        //UPGRD//MMOItems. Log("\u00a73  > \u00a77 Original Base: \u00a7e" + ((DoubleData) ret).getValue());

        // Add up gemstones
        for (S d : perGemstoneData.values()) {
            //UPGRD//MMOItems. Log("\u00a73  >\u00a7b> \u00a77 Gemstone Base: \u00a7e" + ((DoubleData) d).getValue());
            ((Mergeable) ret).merge(d);
        }

        // Add up externals
        for (S d : perExternalData) {
            //UPGRD//MMOItems. Log("\u00a73  >\u00a7c> \u00a77 Extraneous Base: \u00a7e" + ((DoubleData) d).getValue());
            ((Mergeable) ret).merge(d);
        }

        // Return result
        //UPGRD//MMOItems. Log("\u00a73:::\u00a77 Result: \u00a7b" + ((DoubleData) ret).getValue());
        return ret;
    }

    /**
     * To store onto the NBT of the item.
     * <p></p>
     * I've heard its not very optimized, but honestly that just means that
     * instead of running in 0.0001s it runs in 0.0002s idk.
     * <p></p>
     * Still don't abuse calls to this. Try to do so only when necessary
     */
    @NotNull public JsonObject toJson() {
        JsonObject object = new JsonObject();

        // To know the stat it was
        object.addProperty(enc_Stat, getItemStat().getId());

        // Original data
        object.add(enc_OGS, ItemTag.compressTags(getItemStat().getAppliedNBT(getOriginalData())));

        // Kompress Arrays
        JsonArray gemz = new JsonArray();

        // Compress I suppose
        for (UUID gem : perGemstoneData.keySet()) {

            // As Json Object
            JsonObject yes = new JsonObject();

            // Compress tags
            JsonArray yesCompressed = ItemTag.compressTags(getItemStat().getAppliedNBT(getGemstoneData(gem)));

            // Put
            yes.add(gem.toString(), yesCompressed);

            // Actually Include
            gemz.add(yes);
        }

        // Include
        object.add(enc_GSS, gemz);

        // Kompress Arrays
        JsonArray externals = new JsonArray();

        // Compress I suppose
        for (StatData ex : perExternalData) {

            // Put
            externals.add(ItemTag.compressTags(getItemStat().getAppliedNBT(ex)));
        }

        // Include
        object.add(enc_EXS, externals);

        return object;
    }

    /**
     * To store onto the NBT of the item.
     * <p></p>
     * I've heard its not very optimized, but honestly that just means that
     * instead of running in 0.0001s it runs in 0.0002s idk.
     * <p></p>
     * Still don't abuse calls to this. Try to do so only when necessary
     */
    @NotNull public String toNBTString() {

        // Just convert to string :thinking:
        return toJson().toString();
    }

    /**
     * To read from NBT data. This undoes {@link #toJson()} basically.
     * <p></p>
     * @param iSource The MMOItem you are trying to read the NBT of
     */
    @Nullable public static StatHistory<StatData> fromJson(@NotNull MMOItem iSource, @NotNull JsonObject json) {

        // Get the stat we're searching for
        JsonElement statEncode;
        JsonElement ogStatsEncode;
        JsonElement gemsEncode = null;
        JsonElement extEncode = null;

        // It has stat information right?
        if (!json.has(enc_Stat)) { return null; } else { statEncode = json.get(enc_Stat); }
        if (!json.has(enc_OGS)) { return null; } else { ogStatsEncode = json.get(enc_OGS); }
        if (json.has(enc_GSS)) { gemsEncode = json.get(enc_GSS); }
        if (json.has(enc_EXS)) { extEncode = json.get(enc_EXS); }

        // It is a primitive right
        if (!statEncode.isJsonPrimitive()) { return null; }
        if (!ogStatsEncode.isJsonArray()) { return null; }
        if (gemsEncode != null && !gemsEncode.isJsonArray()) { return null; }
        if (extEncode != null && !extEncode.isJsonArray()) { return null; }

        // Get string
        String statInternalName = statEncode.getAsJsonPrimitive().getAsString();

        // Get stat
        ItemStat stat = MMOItems.plugin.getStats().get(statInternalName);

        // Nope
        if (stat == null) { return null; }

        // Decompress tags
        ArrayList<ItemTag> ogDecoded = ItemTag.decompressTags(ogStatsEncode.getAsJsonArray());

        // To know the stat it was
        StatData sData = stat.getLoadedNBT(ogDecoded);

        // Validate non null
        if (sData == null) { return null; }

        // Can now generate stat history
        StatHistory<StatData> sHistory = new StatHistory<>(iSource, stat, sData);

        //region Getting Gem Stone History
        if (gemsEncode != null) {

            // Decompress gems
            for (JsonElement elmnt : gemsEncode.getAsJsonArray()) {

                // Must be an object
                if (elmnt.isJsonObject()) {

                    // Get as Object
                    JsonObject element = elmnt.getAsJsonObject();

                    // Get map
                    Set<Map.Entry<String, JsonElement>> contained = element.entrySet();

                    // There should be exacly one but anyway;
                    for (Map.Entry<String, JsonElement> entry : contained) {

                        // Get path (Gemstone UUID)
                        String gemUUID = entry.getKey();

                        // Attempt to parse gemuuid
                        UUID actualUUID = GemstoneData.UUIDFromString(gemUUID);

                        // Get Stat compressed tag
                        JsonElement compressedTags = entry.getValue();

                        // Succeed?
                        if (compressedTags.isJsonArray() && actualUUID != null) {

                            // Continue...
                            ArrayList<ItemTag> tags = ItemTag.decompressTags(compressedTags.getAsJsonArray());

                            // Generate data
                            StatData gemData = stat.getLoadedNBT(tags);

                            // Validated?
                            if (gemData != null) {

                                // Add
                                sHistory.registerGemstoneData(actualUUID, gemData);
                            }
                        }
                    }
                }
            }
        }
        //endregion

        //region External Stat History
        if (extEncode != null) {

            // Decompress gems
            for (JsonElement elmnt : extEncode.getAsJsonArray()) {

                // Must be an array (compressed tags)
                if (elmnt.isJsonArray()) {

                    // Continue...
                    ArrayList<ItemTag> tags = ItemTag.decompressTags(elmnt.getAsJsonArray());

                    // Generate data
                    StatData exData = stat.getLoadedNBT(tags);

                    // Validated?
                    if (exData != null) {

                        // Add
                        sHistory.registerExternalData(exData);
                    }
                }
            }
        }
        //endregion

        return sHistory;
    }

    /**
     * To read from NBT data. This reverses {@link #toNBTString()} basically.
     * <p></p>
     * Will be null if some error happens
     */
    @Nullable public static StatHistory<StatData> fromNBTString(@NotNull MMOItem iSource, @NotNull String codedJson) {

        // Attempt
        try {

            // Make JSON Parser
            JsonParser pJSON = new JsonParser();

            // Parse as array
            JsonObject oJSON = pJSON.parse(codedJson).getAsJsonObject();

            // Bake
            return fromJson(iSource, oJSON);

        } catch (Throwable e) {

            // Feedbacc
            FriendlyFeedbackProvider ffp = new FriendlyFeedbackProvider(FriendlyFeedbackPalette_MMOItems.get());
            ffp.ActivatePrefix(true, "Stat History");
            ffp.Log(FriendlyFeedbackCategory.ERROR, "Could not get stat history: $f{0}$b at $f{1}", e.getMessage(), e.getStackTrace()[0].toString());
            ffp.SendTo(FriendlyFeedbackCategory.ERROR, MMOItems.getConsole());
            return null;
        }
    }

    /**
     * Get all gemstone and extraneous data from this other, while
     * keeping the current ones as well as <u>these</u> original bases.
     * <p></p>
     * Fails if the stats are not the same one.
     */
    public void Assimilate(@NotNull StatHistory<StatData> other) {

        // Stat must be the same
        if (other.getItemStat().getNBTPath().equals(getItemStat().getNBTPath())) {
           //UPDT//MMOItems.Log("    \u00a72>\u00a76> \u00a77History Stat Matches");

           //UPDT//MMOItems.Log("     \u00a76:\u00a72: \u00a77Original Gemstones \u00a7f" + perGemstoneData.size());
           //UPDT//MMOItems.Log("     \u00a76:\u00a72: \u00a77Original Externals \u00a7f" + perExternalData.size());

            // Register gemstones
            for (UUID exUID : other.perGemstoneData.keySet()) { registerGemstoneData(exUID, (S) other.getGemstoneData(exUID)); }

            // Register externals
            for (StatData ex : other.perExternalData) { registerExternalData((S) ex); }

           //UPDT//MMOItems.Log("     \u00a76:\u00a72: \u00a77Final Gemstones \u00a7f" + perGemstoneData.size());
           //UPDT//MMOItems.Log("     \u00a76:\u00a72: \u00a77Final Externals \u00a7f" + perExternalData.size());
        }
    }

    static final String enc_Stat = "Stat";
    static final String enc_OGS = "OGStory";
    static final String enc_GSS = "Gemstory";
    static final String enc_EXS = "Exstory";
}
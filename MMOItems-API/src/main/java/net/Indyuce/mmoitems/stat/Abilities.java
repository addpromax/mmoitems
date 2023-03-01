package net.Indyuce.mmoitems.stat;

import com.google.gson.*;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.SupportedNBTTagValues;
import io.lumine.mythic.lib.api.util.AltChar;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackCategory;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackProvider;
import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.util.DamageTypeRestrictionSettings;
import net.Indyuce.mmoitems.api.util.message.FFPMMOItems;
import net.Indyuce.mmoitems.util.MMOUtils;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.api.util.NumericStatFormula;
import net.Indyuce.mmoitems.api.util.NumericStatFormula.FormulaSaveOption;
import net.Indyuce.mmoitems.gui.edition.AbilityListEdition;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.skill.RegisteredSkill;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.Indyuce.mmoitems.stat.data.AbilityListData;
import net.Indyuce.mmoitems.stat.data.random.RandomAbilityData;
import net.Indyuce.mmoitems.stat.data.random.RandomAbilityListData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Abilities extends ItemStat<RandomAbilityListData, AbilityListData> {
	public Abilities() {
		super("ABILITY", Material.BLAZE_POWDER, "Item Abilities",
				new String[] { "Make your item cast amazing abilities", "to kill monsters or buff yourself." }, new String[] { "!block", "all" });
	}

	@Override
	public RandomAbilityListData whenInitialized(Object object) {
		Validate.isTrue(object instanceof ConfigurationSection, "Must specify a valid config section");
		ConfigurationSection config = (ConfigurationSection) object;
		RandomAbilityListData list = new RandomAbilityListData();

		for (String key : config.getKeys(false))
			list.add(new RandomAbilityData(config.getConfigurationSection(key)));

		return list;
	}

	@Override
	public void whenApplied(@NotNull ItemStackBuilder item, @NotNull AbilityListData data) {

		//Modify Lore
		List<String> abilityLore = new ArrayList<>();
		boolean splitter = !MMOItems.plugin.getLanguage().abilitySplitter.equals("");
		DamageTypeRestrictionSettings settings = MMOItems.plugin.getLanguage().damageTypeRestrictionSettings;

		String modifierFormat = ItemStat.translate("ability-modifier"), abilityFormat = ItemStat.translate("ability-format");

		data.getAbilities().forEach(ability -> {

			// Replace name of trigger, as well as name of ability
			String triggerDisplayName = settings.getTriggerDisplayName(ability.getTrigger(), ability.getModifier(SkillHandler.SKMOD_DAMAGE_TYPE));
			abilityLore.add(abilityFormat.replace("{trigger}", triggerDisplayName).replace("{ability}", ability.getAbility().getName()));

			for (String modifier : ability.getModifiers()) {

				// Damage Type Modifier does not display in lore
				if (modifier.equals(SkillHandler.SKMOD_DAMAGE_TYPE)) { continue; }

				item.getLore().registerPlaceholder("ability_" + ability.getAbility().getHandler().getId().toLowerCase() + "_" + modifier,
						MythicLib.plugin.getMMOConfig().decimal.format(ability.getModifier(modifier)));
				abilityLore.add(modifierFormat.replace("{modifier}", settings.damageColors(ability.getAbility().getModifierName(modifier))).replace("{value}",
						MythicLib.plugin.getMMOConfig().decimal.format(ability.getModifier(modifier))));
			}

			if (splitter)
				abilityLore.add(MMOItems.plugin.getLanguage().abilitySplitter);
		});

		if (splitter && abilityLore.size() > 0)
			abilityLore.remove(abilityLore.size() - 1);

		// Modify tags
		item.getLore().insert("abilities", abilityLore);
		item.addItemTag(getAppliedNBT(data));
	}

	@NotNull
	@Override
	public ArrayList<ItemTag> getAppliedNBT(@NotNull AbilityListData data) {

		// Make Array
		ArrayList<ItemTag> ret = new ArrayList<>();

		// Convert to JSON
		JsonArray jsonArray = new JsonArray();
		for (AbilityData ab : data.getAbilities()) { jsonArray.add(ab.toJson()); }

		// Put
		ret.add(new ItemTag(getNBTPath(), jsonArray.toString()));

		return ret;
	}

	@Override
	public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
		new AbilityListEdition(inv.getPlayer(), inv.getEdited()).open(inv.getPage());
	}

	@Override
	public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
		String configKey = (String) info[0];
		String edited = (String) info[1];

		String format = message.toUpperCase().replace("-", "_").replace(" ", "_").replaceAll("[^A-Z0-9_]", "");

		if (edited.equals("ability")) {
			Validate.isTrue(MMOItems.plugin.getSkills().hasSkill(format),
					"format is not a valid ability! You may check the ability list using /mi list ability.");
			RegisteredSkill ability = MMOItems.plugin.getSkills().getSkill(format);

			inv.getEditedSection().set("ability." + configKey, null);
			inv.getEditedSection().set("ability." + configKey + ".type", format);
			inv.registerTemplateEdition();
			inv.getPlayer().sendMessage(
					MMOItems.plugin.getPrefix() + "Successfully set the ability to " + ChatColor.GOLD + ability.getName() + ChatColor.GRAY + ".");
			return;
		}

		if (edited.equals("mode")) {

			TriggerType castMode = TriggerType.valueOf(format);

			inv.getEditedSection().set("ability." + configKey + ".mode", castMode.name());
			inv.registerTemplateEdition();
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Successfully set the trigger to " + ChatColor.GOLD + castMode.getName()
					+ ChatColor.GRAY + ".");
			return;
		}
		String number = message;

		// If we are editing the damage types and the provided value is not a number already
		if (SkillHandler.SKMOD_DAMAGE_TYPE.equals(edited) && !SilentNumbers.IntTryParse(number)) {

			// Might come in handy....
			FriendlyFeedbackProvider ffp = new FriendlyFeedbackProvider(FFPMMOItems.get());
			ffp.activatePrefix(true, "Edition");
			boolean failure = false;

			// Or Mode
			boolean orMode = message.startsWith("OR ");
			if (orMode) { message = message.substring("OR ".length()); }

			// Build arrays
			ArrayList<DamageType> whitelisted = new ArrayList<>();
			ArrayList<DamageType> blacklisted = new ArrayList<>();

			// Split by spaces
			String[] typesSplit = message.split(" ");
			for (String ty : typesSplit) {

				// Crop blacklist
				boolean blacklist = false;
				String observed = ty.toUpperCase().replace("-", "_").replace(" ", "_");
				if (observed.startsWith("!")) { observed = observed.substring(1); blacklist = true; }

				// Identify Damage Type
				try {

					// Un-parse
					DamageType damageType = DamageType.valueOf(observed);

					// Add to the lists
					if (blacklist) { blacklisted.add(damageType); } else { whitelisted.add(damageType); }

					// Mention
				} catch (IllegalArgumentException ignored) {

					// no
					failure = true;

					ffp.log(FriendlyFeedbackCategory.ERROR, "Unknown damage type '$r{0}$b' in '$u{1}$b'. ", observed, ty);
				}
			}

			// Cancel
			if (failure) {

				// Errors
				ffp.sendAllTo(inv.getPlayer());
				throw new IllegalArgumentException("$bInvalid input! Please specify damage types to require or blacklist, for example: '$eMAGIC WEAPON !SKILL$b' or '$eOR PHYSICAL PROJECTILE MINION !MAGIC$b'. ");
			}

			// Bake number
			number = String.valueOf(DamageType.encodeDamageTypeMatch(whitelisted, blacklisted, orMode));
		}

		new NumericStatFormula(number).fillConfigurationSection(inv.getEditedSection(), "ability." + configKey + "." + edited,
				FormulaSaveOption.NONE);
		inv.registerTemplateEdition();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.GOLD + MMOUtils.caseOnWords(edited.replace("-", " ")) + ChatColor.GRAY
				+ " successfully added.");
	}

	@Override
	public void whenDisplayed(List<String> lore, Optional<RandomAbilityListData> statData) {
		lore.add(ChatColor.GRAY + "Current Abilities: " + ChatColor.GOLD
				+ (statData.isPresent() ? ((RandomAbilityListData) statData.get()).getAbilities().size() : 0));
		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to edit the item abilities.");
	}

	@NotNull
	@Override
	public AbilityListData getClearStatData() {
		return new AbilityListData();
	}

	@Override
	public void whenLoaded(@NotNull ReadMMOItem mmoitem) {

		// Get tags
		ArrayList<ItemTag> relevantTags = new ArrayList<>();

		if (mmoitem.getNBT().hasTag(getNBTPath()))
			relevantTags.add(ItemTag.getTagAtPath(getNBTPath(), mmoitem.getNBT(), SupportedNBTTagValues.STRING));

		AbilityListData data = (AbilityListData) getLoadedNBT(relevantTags);

		// Valid?
		if (data != null) {

			// Set
			mmoitem.setData(this, data);
		}
	}

	@Nullable
	@Override
	public AbilityListData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {

		// Get Json Array thing
		ItemTag jsonCompact = ItemTag.getTagAtPath(getNBTPath(), storedTags);

		// Found?
		if (jsonCompact != null) {

			// Attempt to parse
			try {

				// New ablity list data
				AbilityListData list = new AbilityListData();
				JsonArray ar = new JsonParser().parse((String) jsonCompact.getValue()).getAsJsonArray();

				// Convert every object into an ability
				for (JsonElement e : ar) {

					// For every object
					if (e.isJsonObject()) {

						// Get as object
						JsonObject obj = e.getAsJsonObject();

						// Add to abilit list
						list.add(new AbilityData(obj));
					}
				}

				// Well that mus thave worked aye?
				return list;

			} catch (JsonSyntaxException|IllegalStateException exception) {
				/*
				 * OLD ITEM WHICH MUST BE UPDATED.
				 */
			}
		}

		// Did not work out
		return null;
	}
}

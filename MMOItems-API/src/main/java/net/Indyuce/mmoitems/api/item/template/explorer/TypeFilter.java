package net.Indyuce.mmoitems.api.item.template.explorer;

import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.item.type.MMOItemType;

import java.util.function.Predicate;

/**
 * Filters items with a specific type
 */
public class TypeFilter implements Predicate<MMOItemTemplate> {
	private final MMOItemType type;

	public TypeFilter(MMOItemType type) {
		this.type = type;
	}

	@Override
	public boolean test(MMOItemTemplate template) {
		return template.getType().equals(type);
	}
}

package com.demigodsrpg.chitchat.tag;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

public class NameTag extends PlayerTag {
    @Override
    public String getName() {
        return "name";
    }

    @Override
    public int getPriority() {
        return 999;
    }

    @Override
    public TextComponent getComponentFor(Player tagSource) {
        TextComponent ret = new TextComponent("");
        for (BaseComponent component : TextComponent.fromLegacyText(tagSource.getDisplayName())) {
            ret.addExtra(component);
        }
        return ret;
    }
}

package br.net.fabiozumbi12.translationapi.database;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public interface LangDB {

    String getText(String key);

    void setText(String key, String translation);

    String getCustom(String category, String key);

    void setCustom(String category, String key, String translation);

    String getItemName(Material material);

    void setItemName(Material material, String translation);

    String getEntityName(EntityType entityType);

    void setEntityName(EntityType entityType, String translation);

    void save();

    void closeConn();
}

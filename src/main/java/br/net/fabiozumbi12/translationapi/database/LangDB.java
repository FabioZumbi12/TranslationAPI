package br.net.fabiozumbi12.translationapi.database;

import org.bukkit.Material;

public interface LangDB {

    String get(String language, String key);
    void set(String language, String key, String translation);

    String getCustom(String category, String language, String key);
    void setCustom(String category, String language, String key, String translation);

    String getItemName(String language, Material material);
    void setItemName(String language, Material material, String translation);

    void save();
    void load();
}

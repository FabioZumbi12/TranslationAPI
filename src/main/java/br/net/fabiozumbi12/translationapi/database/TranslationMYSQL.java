package br.net.fabiozumbi12.translationapi.database;

import org.bukkit.Material;

public class TranslationMYSQL implements LangDB {

    @Override
    public String get(String language, String key) {
        return null;
    }

    @Override
    public void set(String language, String key, String translation) {

    }

    @Override
    public String getCustom(String category, String language, String key) {
        return null;
    }

    @Override
    public void setCustom(String category, String language, String key, String translation) {

    }

    @Override
    public String getItemName(String language, Material material) {
        return null;
    }

    @Override
    public void setItemName(String language, Material material, String translation) {

    }

    @Override
    public void save() {

    }

    @Override
    public void load() {

    }
}

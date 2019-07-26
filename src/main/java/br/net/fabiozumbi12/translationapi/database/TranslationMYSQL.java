package br.net.fabiozumbi12.translationapi.database;

import br.net.fabiozumbi12.translationapi.TranslationAPI;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public class TranslationMYSQL implements LangDB {

    public TranslationMYSQL(TranslationAPI plugin) {

    }

    @Override
    public String getText(String key) {
        return null;
    }

    @Override
    public void setText(String key, String translation) {

    }

    @Override
    public String getCustom(String category, String key) {
        return null;
    }

    @Override
    public void setCustom(String category, String key, String translation) {

    }

    @Override
    public String getItemName(Material material) {
        return null;
    }

    @Override
    public void setItemName(Material material, String translation) {

    }

    @Override
    public String getEntityName(EntityType entityType) {
        return null;
    }

    @Override
    public void setEntityName(EntityType entityType, String translation) {

    }

    @Override
    public void save() {

    }
}

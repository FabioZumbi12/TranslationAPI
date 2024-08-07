package br.net.fabiozumbi12.translationapi.database;

import br.net.fabiozumbi12.translationapi.TranslationAPI;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.io.IOException;

public class TranslationFile implements LangDB {

    private final YamlConfiguration langFile = new YamlConfiguration();
    private File langFileExt;

    public TranslationFile(TranslationAPI plugin) {
        try {
            langFileExt = new File(plugin.getDataFolder(), plugin.getSysLang() + ".yml");
            if (!langFileExt.exists())
                langFileExt.createNewFile();

            langFile.load(langFileExt);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getText(String key) {
        return langFile.getString("text." + fixConfigPath(key));
    }

    @Override
    public void setText(String key, String translation) {
        langFile.set("text." + fixConfigPath(key), translation);
        save();
    }

    @Override
    public String getCustom(String category, String key) {
        return langFile.getString("custom." + category + "." + fixConfigPath(key));
    }

    @Override
    public void setCustom(String category, String key, String translation) {
        langFile.set("custom." + category + "." + fixConfigPath(key), translation);
        save();
    }

    @Override
    public String getItemName(Material material) {
        return langFile.getString("material." + material.name());
    }

    @Override
    public void setItemName(Material material, String translation) {
        langFile.set("material." + material.name(), translation);
        save();
    }

    @Override
    public String getEntityName(EntityType entityType) {
        return langFile.getString("entity." + entityType.name());
    }

    @Override
    public void setEntityName(EntityType entityType, String translation) {
        langFile.set("entity." + entityType.name(), translation);
        save();
    }

    @Override
    public String getCustomType(String typeName) {
        return langFile.getString("customType." + typeName);
    }

    @Override
    public void setCustomType(String typeName, String translation) {
        langFile.set("customType." + typeName, translation);
        save();
    }

    @Override
    public void save() {
        try {
            langFile.save(langFileExt);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void closeConn() {

    }

    private String fixConfigPath(String text) {
        while (text.endsWith(".")) {
            text = text.substring(0, text.lastIndexOf("."));
        }
        return text;
    }
}

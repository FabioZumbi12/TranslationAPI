package br.net.fabiozumbi12.translationapi;

import com.google.common.html.HtmlEscapers;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TranslationCore {
    private final TranslationAPI plugin;

    public TranslationCore(TranslationAPI plugin) {
        this.plugin = plugin;
    }

    String translate(String msg, String languageFrom, String languageTo) {
        if (msg.length() <= 1)
            return msg;

        try {
            URL url = new URL(String.format(new String(Base64.getDecoder().decode("aHR0cHM6Ly90cmFuc2xhdGUuZ29vZ2xlLmNvbS50ci9tP2hsPWVuJnNsPSVzJnRsPSVzJmllPVVURi04JnByZXY9X20mcT0lcw==")), languageFrom, languageTo, URLEncoder.encode(msg, "UTF-8")));
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));

            String line;
            StringBuilder str = new StringBuilder();
            while ((line = br.readLine()) != null) {
                str.append(line);
            }

            br.close();
            con.disconnect();

            final Pattern pattern = Pattern.compile("<div dir=\"ltr\" class=\"t0\">(.+?)</div>", Pattern.DOTALL);
            final Matcher matcher = pattern.matcher(str);
            if (matcher.find()) {
                msg = HtmlEscapers.htmlEscaper().escape(matcher.group(1));
            }
        } catch (IOException ex) {
            plugin.getLogger().warning("Error on getText language from google: " + ex.getLocalizedMessage());
        }
        return msg;
    }


    public String capitalizeText(String name) {
        String[] split = name.toUpperCase().replace("_", " ").split(" ");
        StringBuilder finalName = new StringBuilder();
        for (String nm : split) {
            finalName.append(nm.toUpperCase(), 0, 1).append(nm.toLowerCase().substring(1)).append(" ");
        }
        return finalName.delete(finalName.length() - 1, finalName.length()).toString();
    }

    /**
     * Translation for custom Texts or Messages like a plugin or specific messages
     * @param category The plugin name or category
     * @param text The text to translate
     * @param from Translate from (e.g. "en-us")
     * @param save Save translation on Database
     * @return Translated text
     */
    public String translateCustomText(String category, String text, String from, boolean save) {
        return translateCustomText(category, text, from, plugin.getSysLang(), save);
    }

    /**
     * Translation for custom Texts or Messages like a plugin or specific messages
     * @param category The plugin name or category
     * @param text The text to translate
     * @param from Translate from (e.g. "en-us")
     * @param to Translate to (e.g. "pt-br")
     * @param save Save translation on Database
     * @return Translated text
     */
    public String translateCustomText(String category, String text, String from, String to, boolean save) {
        if (from.equals(to))
            return text;

        String temp = plugin.langDB.getCustom(category, text);

        if (temp == null) {
            if (!plugin.getConfig().getBoolean("online-translation"))
                return text;

            temp = translate(text, from, to);
            if (!temp.isEmpty() && !text.equals(temp) && save) {
                plugin.langDB.setCustom(category, text, temp);
            }
        }

        if (!temp.isEmpty()) {
            text = temp;
        }
        return text;
    }

    /**
     * Translate some text
     * @param text The text to translate
     * @param from Translate from (e.g. "en-us")
     * @param save Save translation on Database
     * @return Translated text
     */
    public String translateText(String text, String from, boolean save) {
        return translateText(text, from, plugin.getSysLang(), save);
    }

    /**
     * Translate some text
     * @param text The text to translate
     * @param from Translate from (e.g. "en-us")
     * @param to Translate to (e.g. "pt-br")
     * @param save Save translation on Database
     * @return Translated text
     */
    public String translateText(String text, String from, String to, boolean save) {
        if (from.equals(to))
            return text;

        String temp = plugin.langDB.getText(text);

        if (temp == null) {
            if (!plugin.getConfig().getBoolean("online-translation"))
                return text;

            temp = translate(text, from, to);
            if (!temp.isEmpty() && !text.equals(temp) && save) {
                plugin.langDB.setText(text, temp);
            }
        }

        if (!temp.isEmpty()) {
            text = temp;
        }
        return text;
    }

    /**
     * Translate material name
     * @param material The Material
     * @param from Translate from (e.g. "en-us")
     * @param save Save translation on Database
     * @return Translated text
     */
    public String translateItem(Material material, String from, boolean save) {
        return translateItem(material, from, plugin.getSysLang(), save);
    }

    /**
     * Translate material name
     * @param material The Material
     * @param from Translate from (e.g. "en-us")
     * @param to Translate to (e.g. "pt-br")
     * @param save Save translation on Database
     * @return Translated text
     */
    public String translateItem(Material material, String from, String to, boolean save) {
        String text = capitalizeText(material.name());

        if (from.equals(to))
            return text;

        String temp = plugin.langDB.getItemName(material);

        if (temp == null) {
            if (!plugin.getConfig().getBoolean("online-translation"))
                return text;

            temp = translate(text, from, to);
            if (!temp.isEmpty() && !text.equals(temp) && save) {
                plugin.langDB.setItemName(material, capitalizeText(temp));
            }
        }

        if (!temp.isEmpty()) {
            text = temp;
        }
        return text;
    }

    /**
     * Translate entity types
     * @param entityType The entity type
     * @param from Translate from (e.g. "en-us")
     * @param save Save translation on Database
     * @return Translated text
     */
    public String translateEntity(EntityType entityType, String from, boolean save) {
        return translateEntity(entityType, from, plugin.getSysLang(), save);
    }

    /**
     * Translate entity types
     * @param entityType The entity type
     * @param from Translate from (e.g. "en-us")
     * @param to Translate to (e.g. "pt-br")
     * @param save Save translation on Database
     * @return Translated text
     */
    public String translateEntity(EntityType entityType, String from, String to, boolean save) {
        String text = capitalizeText(entityType.name());

        if (from.equals(to))
            return text;

        String temp = plugin.langDB.getEntityName(entityType);

        if (temp == null) {
            if (!plugin.getConfig().getBoolean("online-translation"))
                return text;

            temp = translate(text, from, to);
            if (!temp.isEmpty() && !text.equals(temp) && save) {
                plugin.langDB.setEntityName(entityType, capitalizeText(temp));
            }
        }

        if (!temp.isEmpty()) {
            text = temp;
        }
        return text;
    }

    /**
     * Translate some custom type, normally separated with _ (underline)
     * @param typeName The custom type name
     * @param from Translate from (e.g. "en-us")
     * @param save Save translation on Database
     * @return Translated text
     */
    public String translateCustomType(String typeName, String from, boolean save) {
        return translateCustomType(typeName, from, plugin.getSysLang(), save);
    }

    /**
     * Translate some custom type, normally separated with _ (underline)
     * @param typeName The custom type name
     * @param from Translate from (e.g. "en-us")
     * @param to Translate to (e.g. "pt-br")
     * @param save Save translation on Database
     * @return Translated text
     */
    public String translateCustomType(String typeName, String from, String to, boolean save) {
        String text = capitalizeText(typeName);

        if (from.equals(to))
            return text;

        String temp = plugin.langDB.getCustomType(typeName);

        if (temp == null) {
            if (!plugin.getConfig().getBoolean("online-translation"))
                return text;

            temp = translate(text, from, to);
            if (!temp.isEmpty() && !text.equals(temp) && save) {
                plugin.langDB.setCustomType(typeName, capitalizeText(temp));
            }
        }

        if (!temp.isEmpty()) {
            text = temp;
        }
        return text;
    }
}

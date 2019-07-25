package br.net.fabiozumbi12.translationapi;

import br.net.fabiozumbi12.translationapi.database.LangDB;
import br.net.fabiozumbi12.translationapi.database.TranslationFile;
import br.net.fabiozumbi12.translationapi.database.TranslationMYSQL;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TranslationAPI extends JavaPlugin {
    private LangDB langDB;
    private String sysLanguage;

    @Override
    public void onEnable() {
        sysLanguage = (Locale.getDefault().getLanguage()+"-"+Locale.getDefault().getCountry()).toLowerCase();

        // Init config
        getConfig().set("system-language", getConfig().getString("system-language", sysLanguage));
        getConfig().set("database", getConfig().getString("database", "file"));
        saveConfig();

        // Init vars
        reload();

        getLogger().info("TranslationAPI Enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("TranslationAPI Disabled");
    }

    private void reload() {
        reloadConfig();

        String database = getConfig().getString("database");
        if (database.equalsIgnoreCase("mysql")) {
            langDB = new TranslationMYSQL();
        } else {
            langDB = new TranslationFile();
        }
        getLogger().info(translate("Using " + database + " as database", "en-us", sysLanguage));
    }

    public String translate(String msg, String languageFrom, String languageTo) {
        try {
            URL url = new URL(String.format("https://translate.google.com.tr/m?hl=en&sl=%s&tl=%s&ie=UTF-8&prev=_m&q=%s", languageFrom, languageTo, URLEncoder.encode(msg, "UTF-8")));
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));

            String line;
            StringBuilder str = new StringBuilder();
            while ((line = br.readLine()) != null) {
                str.append(line);
            }

            br.close();
            final Pattern pattern = Pattern.compile("<div dir=\"ltr\" class=\"t0\">(.+?)</div>", Pattern.DOTALL);
            final Matcher matcher = pattern.matcher(str);
            if (matcher.find()){
                msg = matcher.group(1);
            }
        } catch (IOException ex) {
            getLogger().warning("Error on get language from google: " + ex.getLocalizedMessage());
        }
        return msg;
    }
}

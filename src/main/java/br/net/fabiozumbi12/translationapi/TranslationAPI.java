package br.net.fabiozumbi12.translationapi;

import br.net.fabiozumbi12.translationapi.database.LangDB;
import br.net.fabiozumbi12.translationapi.database.TranslationFile;
import br.net.fabiozumbi12.translationapi.database.TranslationMYSQL;
import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class TranslationAPI extends JavaPlugin implements CommandExecutor, TabCompleter {
    private static TranslationAPI plugin;
    private LangDB langDB;
    private String sysLanguage;
    private boolean wait = false;

    public static TranslationAPI getAPI() {
        return plugin;
    }

    public String getSysLang() {
        return this.sysLanguage;
    }

    @Override
    public void onEnable() {
        plugin = this;

        // Init commands
        getCommand("translationapi").setExecutor(this);

        // Init vars
        reload(false);

        getLogger().info("TranslationAPI Enabled");
    }

    @Override
    public void onDisable() {
        langDB.save();
        getLogger().info("TranslationAPI Disabled");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> tab = new ArrayList<>();
        if (args.length == 1) {
            if (args[0].isEmpty()) {
                tab.addAll(Arrays.asList("reload", "translate-items", "translate-entities"));
            } else {
                tab.addAll(Stream.of("reload", "translate-items", "translate-entities").filter(t -> t.startsWith(args[0])).collect(Collectors.toList()));
            }
        }
        return tab;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&5[&7TranslationAPI&5]&a " + translateCustomText("TranslationAPI", "Plugin made by FabioZumbi12", "en-us", sysLanguage, true)));
            return true;
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                reload(true);
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&5[&7TranslationAPI&5]&a " + translateCustomText("TranslationAPI", "Configuration reloaded with success!", "en-us", sysLanguage, true)));
                return true;
            }

            if (args[0].equalsIgnoreCase("translate-items")) {
                if (wait) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&5[&7TranslationAPI&5]&a " + translateCustomText("TranslationAPI", "Another action is running, try again in a few moments!", "en-us", sysLanguage, true)));
                    return true;
                }

                wait = true;
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&5[&7TranslationAPI&5]&a " + translateCustomText("TranslationAPI", "The translation of all Material Names has been started...", "en-us", sysLanguage, true)));
                Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                    final int[] total = {0};
                    Arrays.stream(Material.values()).forEach(m -> {
                        String temp = langDB.getItemName(m);
                        if (temp != null) {
                            return;
                        }

                        translateItem(m, "en-us", sysLanguage, true);
                        total[0]++;
                    });

                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&5[&7TranslationAPI&5]&a " + translateCustomText("TranslationAPI", "Translated a total of " + total[0] + " item names!", "en-us", sysLanguage, true)));
                    wait = false;
                });
                return true;
            }

            if (args[0].equalsIgnoreCase("translate-entities")) {
                if (wait) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&5[&7TranslationAPI&5]&a " + translateCustomText("TranslationAPI", "Another action is running, try again in a few moments!", "en-us", sysLanguage, true)));
                    return true;
                }

                wait = true;
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&5[&7TranslationAPI&5]&a " + translateCustomText("TranslationAPI", "The translation of all Entity Names has been started...", "en-us", sysLanguage, true)));
                Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                    final int[] total = {0};
                    Arrays.stream(EntityType.values()).forEach(e -> {
                        String temp = langDB.getEntityName(e);
                        if (temp != null) {
                            return;
                        }

                        translateEntity(e, "en-us", sysLanguage, true);
                        total[0]++;
                    });

                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&5[&7TranslationAPI&5]&a " + translateCustomText("TranslationAPI", "Translated a total of " + total[0] + " entity names!", "en-us", sysLanguage, true)));
                    wait = false;
                });
                return true;
            }
        }
        return false;
    }

    private void reload(boolean reload) {
        // Get system language
        String sysLang = (System.getProperty("user.language") + "-" + System.getProperty("user.country")).toLowerCase();
        sysLanguage = sysLang.equals("-") ? "en-us" : sysLang;

        // Init config
        if (reload) reloadConfig();

        getConfig().set("system-language", getConfig().getString("system-language", sysLanguage));
        getConfig().set("database", getConfig().getString("database", "file"));

        getConfig().set("mysql.host", getConfig().getString("mysql.host", "localhost"));
        getConfig().set("mysql.port", getConfig().getString("mysql.port", "3306"));
        getConfig().set("mysql.database", getConfig().getString("mysql.database", "TranslationApi"));
        getConfig().set("mysql.username", getConfig().getString("mysql.username", "root"));
        getConfig().set("mysql.password", getConfig().getString("mysql.password", "1234"));
        getConfig().set("mysql.prefix", getConfig().getString("mysql.prefix", "langApi_"));

        saveConfig();

        Bukkit.getScheduler().cancelTasks(this);
        wait = false;

        sysLanguage = getConfig().getString("system-language");
        String database = getConfig().getString("database");

        if (langDB != null) {
            langDB.closeConn();
        }

        if (database.equalsIgnoreCase("mysql")) {
            langDB = new TranslationMYSQL(this);
        } else {
            langDB = new TranslationFile(this);
        }
        getLogger().info(translateCustomText("TranslationAPI", "Using \"" + database + "\" as database", "en-us", sysLanguage, true));
    }

    public String capitalizeText(String name) {
        String[] split = name.replace("_", " ").split(" ");
        StringBuilder finalName = new StringBuilder();
        for (String nm : split) {
            finalName.append(nm.toUpperCase(), 0, 1).append(nm.toLowerCase().substring(1)).append(" ");
        }
        return finalName.delete(finalName.length() - 1, finalName.length()).toString();
    }

    public String translate(String msg, String languageFrom, String languageTo) {
        if (msg.length() <= 1)
            return msg;

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
            con.disconnect();

            final Pattern pattern = Pattern.compile("<div dir=\"ltr\" class=\"t0\">(.+?)</div>", Pattern.DOTALL);
            final Matcher matcher = pattern.matcher(str);
            if (matcher.find()) {
                msg = StringEscapeUtils.unescapeHtml(matcher.group(1));
            }
        } catch (IOException ex) {
            getLogger().warning("Error on getText language from google: " + ex.getLocalizedMessage());
        }
        return msg;
    }

    /*
    Translation for custom Texts or Messages like a plugin category or specific messages
     */
    public String translateCustomText(String category, String text, String from) {
        return translateCustomText(category, text, from, getSysLang(), false);
    }

    public String translateCustomText(String category, String text, String from, String to) {
        return translateCustomText(category, text, from, to, false);
    }

    public String translateCustomText(String category, String text, String from, String to, boolean save) {
        if (from.equals(to))
            return text;

        String temp = langDB.getCustom(category, text);

        if (temp == null) {
            temp = translate(text, from, to);
            if (!temp.isEmpty() && !text.equals(temp) && save) {
                langDB.setCustom(category, text, temp);
            }
        }

        if (!temp.isEmpty()) {
            text = temp;
        }
        return text;
    }

    /*
    Translation for Text or Messages
     */
    public String translateText(String text, String from, boolean save) {
        return translateText(text, from, getSysLang(), save);
    }

    public String translateText(String text, String from, String to, boolean save) {
        if (from.equals(to))
            return text;

        String temp = langDB.getText(text);

        if (temp == null) {
            temp = translate(text, from, to);
            if (!temp.isEmpty() && !text.equals(temp) && save) {
                langDB.setText(text, temp);
            }
        }

        if (!temp.isEmpty()) {
            text = temp;
        }
        return text;
    }

    /*
    Translation for Item types
     */
    public String translateItem(Material material, String from, boolean save) {
        return translateItem(material, from, getSysLang(), save);
    }

    public String translateItem(Material material, String from, String to, boolean save) {
        String text = capitalizeText(material.name());

        if (from.equals(to))
            return text;

        String temp = langDB.getItemName(material);

        if (temp == null) {
            temp = translate(text, from, to);
            if (!temp.isEmpty() && !text.equals(temp) && save) {
                langDB.setItemName(material, capitalizeText(temp));
            }
        }

        if (!temp.isEmpty()) {
            text = temp;
        }
        return text;
    }


    /*
    Translation for Item types
     */
    public String translateEntity(EntityType entityType, String from, boolean save) {
        return translateEntity(entityType, from, getSysLang(), save);
    }

    public String translateEntity(EntityType entityType, String from, String to, boolean save) {
        String text = capitalizeText(entityType.name());

        if (from.equals(to))
            return text;

        String temp = langDB.getEntityName(entityType);

        if (temp == null) {
            temp = translate(text, from, to);
            if (!temp.isEmpty() && !text.equals(temp) && save) {
                langDB.setEntityName(entityType, capitalizeText(temp));
            }
        }

        if (!temp.isEmpty()) {
            text = temp;
        }
        return text;
    }
}

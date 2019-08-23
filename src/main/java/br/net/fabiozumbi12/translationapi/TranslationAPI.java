package br.net.fabiozumbi12.translationapi;

import br.net.fabiozumbi12.translationapi.database.LangDB;
import br.net.fabiozumbi12.translationapi.database.TranslationFile;
import br.net.fabiozumbi12.translationapi.database.TranslationMYSQL;
import br.net.fabiozumbi12.translationapi.metrics.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class TranslationAPI extends JavaPlugin implements CommandExecutor, TabCompleter {
    LangDB langDB;
    private String sysLanguage;
    private boolean wait = false;

    private static TranslationCore transCore;
    public static TranslationCore getAPI() {
        return transCore;
    }

    public String getSysLang() {
        return this.sysLanguage;
    }

    @Override
    public void onEnable() {
        // Init commands
        getCommand("translationapi").setExecutor(this);

        // Init vars
        reload(false);

        getLogger().info("TranslationAPI Enabled");

        // Metrics
        try {
            Metrics metrics = new Metrics(this);
            metrics.addCustomChart(new Metrics.SimplePie("translating_to", () -> sysLanguage));
            if (metrics.isEnabled())
                getLogger().info("Metrics enabled! See our stats here: https://bstats.org/plugin/bukkit/TranslationAPI");
        } catch (Exception ex) {
            getLogger().info("Metrics not enabled due errors: " + ex.getLocalizedMessage());
        }
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
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&5[&7TranslationAPI&5]&a " + transCore.translateCustomText("TranslationAPI", "Plugin made by FabioZumbi12", "en-us", sysLanguage, true)));
            return true;
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                reload(true);
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&5[&7TranslationAPI&5]&a " + transCore.translateCustomText("TranslationAPI", "Configuration reloaded with success!", "en-us", sysLanguage, true)));
                return true;
            }

            if (args[0].equalsIgnoreCase("translate-items")) {
                if (wait) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&5[&7TranslationAPI&5]&a " + transCore.translateCustomText("TranslationAPI", "Another action is running, try again in a few moments!", "en-us", sysLanguage, true)));
                    return true;
                }

                wait = true;
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&5[&7TranslationAPI&5]&a " + transCore.translateCustomText("TranslationAPI", "The translation of all Material Names has been started...", "en-us", sysLanguage, true)));
                Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                    final int[] total = {0};
                    Arrays.stream(Material.values()).forEach(m -> {
                        String temp = langDB.getItemName(m);
                        if (temp != null) {
                            return;
                        }

                        transCore.translateItem(m, "en-us", sysLanguage, true);
                        total[0]++;
                    });

                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&5[&7TranslationAPI&5]&a " + transCore.translateCustomText("TranslationAPI", "Translated a total of " + total[0] + " item names!", "en-us", sysLanguage, true)));
                    wait = false;
                });
                return true;
            }

            if (args[0].equalsIgnoreCase("translate-entities")) {
                if (wait) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&5[&7TranslationAPI&5]&a " + transCore.translateCustomText("TranslationAPI", "Another action is running, try again in a few moments!", "en-us", sysLanguage, true)));
                    return true;
                }

                wait = true;
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&5[&7TranslationAPI&5]&a " + transCore.translateCustomText("TranslationAPI", "The translation of all Entity Names has been started...", "en-us", sysLanguage, true)));
                Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                    final int[] total = {0};
                    Arrays.stream(EntityType.values()).forEach(e -> {
                        String temp = langDB.getEntityName(e);
                        if (temp != null) {
                            return;
                        }

                        transCore.translateEntity(e, "en-us", sysLanguage, true);
                        total[0]++;
                    });

                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&5[&7TranslationAPI&5]&a " + transCore.translateCustomText("TranslationAPI", "Translated a total of " + total[0] + " entity names!", "en-us", sysLanguage, true)));
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
        if (!new File(getDataFolder(), sysLanguage + ".yml").exists() && getResource("presets/" + sysLanguage + ".yml") != null){
            saveResource("presets/" + sysLanguage + ".yml", false);
            try {
                Files.copy(new File(getDataFolder(), "presets" + File.separator + sysLanguage + ".yml").toPath(), new File(getDataFolder(), sysLanguage + ".yml").toPath());
            } catch (IOException ignored) {}
        }

        String database = getConfig().getString("database");

        if (langDB != null) {
            langDB.closeConn();
        }

        if (database.equalsIgnoreCase("mysql")) {
            langDB = new TranslationMYSQL(this);
        } else {
            langDB = new TranslationFile(this);
        }

        // Init core
        transCore = new TranslationCore(this);

        getLogger().info(transCore.translateCustomText("TranslationAPI", "Using \"" + database + "\" as database", "en-us", sysLanguage, true));
    }
}

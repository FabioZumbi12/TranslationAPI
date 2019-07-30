package br.net.fabiozumbi12.translationapi.database;

import br.net.fabiozumbi12.translationapi.TranslationAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.*;
import java.util.HashMap;

public class TranslationMYSQL implements LangDB {

    private Connection connection;
    private String prefix;
    private TranslationAPI plugin;
    private HashMap<String, String> cache = new HashMap<>();

    public TranslationMYSQL(TranslationAPI plugin) {
        this.plugin = plugin;
        try {
            this.prefix = plugin.getConfig().getString("mysql.prefix");

            openConnection(plugin.getConfig().getString("mysql.host"),
                    plugin.getConfig().getString("mysql.port"),
                    plugin.getConfig().getString("mysql.database"),
                    plugin.getConfig().getString("mysql.username"),
                    plugin.getConfig().getString("mysql.password"));

            createTables();

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void addCache(String key, String value) {
        if (!cache.containsKey(key)) {
            cache.put(key, value);
            Bukkit.getScheduler().runTaskLater(plugin, ()->{
                cache.remove(key);
            }, 80);
        }
    }

    private void createTables() {
        try {
            DatabaseMetaData meta = connection.getMetaData();

            // Items Table
            ResultSet rs = meta.getTables(null, null, prefix + "items", null);
            if (!rs.next()) {
                PreparedStatement ps = connection.prepareStatement("CREATE TABLE `" + prefix + "items` " +
                        "(`en-us` varchar(100) PRIMARY KEY NOT NULL, `"+plugin.getSysLang()+"` varchar(100)) CHARACTER SET utf8 COLLATE utf8_general_ci");
                ps.executeUpdate();
                ps.close();
            }
            rs.close();
            rs = meta.getColumns(null, null, prefix + "items", plugin.getSysLang());
            if (!rs.next()) {
                PreparedStatement st = connection.prepareStatement("ALTER TABLE `" + prefix + "items" + "` ADD `"+plugin.getSysLang()+"` varchar(100)");
                st.executeUpdate();
            }
            rs.close();

            // Entity Table
            rs = meta.getTables(null, null, prefix + "entities", null);
            if (!rs.next()) {
                PreparedStatement ps = connection.prepareStatement("CREATE TABLE `" + prefix + "entities` " +
                        "(`en-us` varchar(100) PRIMARY KEY NOT NULL, `"+plugin.getSysLang()+"` varchar(100)) CHARACTER SET utf8 COLLATE utf8_general_ci");
                ps.executeUpdate();
                ps.close();
            }
            rs.close();
            rs = meta.getColumns(null, null, prefix + "entities", plugin.getSysLang());
            if (!rs.next()) {
                PreparedStatement st = connection.prepareStatement("ALTER TABLE `" + prefix + "entities" + "` ADD `"+plugin.getSysLang()+"` varchar(100)");
                st.executeUpdate();
            }
            rs.close();

            // Text Table
            rs = meta.getTables(null, null, prefix + "text", null);
            if (!rs.next()) {
                PreparedStatement ps = connection.prepareStatement("CREATE TABLE `" + prefix + "text` " +
                        "(`en-us` LONGTEXT PRIMARY KEY NOT NULL, `"+plugin.getSysLang()+"` LONGTEXT) CHARACTER SET utf8 COLLATE utf8_general_ci");
                ps.executeUpdate();
                ps.close();
            }
            rs.close();
            rs = meta.getColumns(null, null, prefix + "text", plugin.getSysLang());
            if (!rs.next()) {
                PreparedStatement st = connection.prepareStatement("ALTER TABLE `" + prefix + "text" + "` ADD `"+plugin.getSysLang()+"` LONGTEXT");
                st.executeUpdate();
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createCustomTable(String table) {
        try {
            DatabaseMetaData meta = connection.getMetaData();

            ResultSet rs = meta.getTables(null, null, prefix + "custom_" + table, null);
            if (!rs.next()) {
                PreparedStatement ps = connection.prepareStatement("CREATE TABLE `" + prefix + "custom_" + table + "` " +
                        "(`en-us` LONGTEXT PRIMARY KEY NOT NULL, `"+plugin.getSysLang()+"` LONGTEXT) CHARACTER SET utf8 COLLATE utf8_general_ci");
                ps.executeUpdate();
                ps.close();
            }
            rs.close();
            rs = meta.getColumns(null, null, prefix + "custom_" + table, plugin.getSysLang());
            if (!rs.next()) {
                PreparedStatement st = connection.prepareStatement("ALTER TABLE `" + prefix + "custom_" + table + "` ADD `"+plugin.getSysLang()+"` LONGTEXT");
                st.executeUpdate();
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void openConnection(String host, String port, String database, String username, String password) throws SQLException, ClassNotFoundException {
        if (connection != null && !connection.isClosed()) {
            return;
        }
        synchronized (this) {
            if (connection != null && !connection.isClosed()) {
                return;
            }
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + host+ ":" + port + "/" + database, username, password);
        }
    }

    @Override
    public String getText(String key) {
        if (cache.containsKey("text_"+key)) {
            return cache.get("text_"+key);
        }

        try {
            PreparedStatement ps = connection.prepareStatement("SELECT `" + plugin.getSysLang() + "` from `" + prefix + "text` where `en-us` = ?");
            ps.setString(1, key);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String value = rs.getString(plugin.getSysLang());
                rs.close();
                addCache("text_"+key, value);
                return value;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setText(String key, String translation) {
        BukkitRunnable r = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    PreparedStatement ps = connection.prepareStatement("INSERT INTO `" + prefix + "text` (?,?) VALUES (?,?)");
                    ps.setString(1, "en-us");
                    ps.setString(2, plugin.getSysLang());
                    ps.setString(3, key);
                    ps.setString(4, translation);
                    ps.executeUpdate();
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        };
        r.runTaskAsynchronously(plugin);
    }

    @Override
    public String getCustom(String category, String key) {
        if (cache.containsKey("custom_"+category+key)) {
            return cache.get("custom_"+category+key);
        }

        try {
            PreparedStatement ps = connection.prepareStatement("SELECT `" + plugin.getSysLang() + "` from `" + prefix + "custom_" + category + "` where `en-us` = ?");
            ps.setString(1, key);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String value = rs.getString(plugin.getSysLang());
                rs.close();
                addCache("custom_"+category+key, value);
                return value;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setCustom(String category, String key, String translation) {
        BukkitRunnable r = new BukkitRunnable() {
            @Override
            public void run() {
                createCustomTable(category);
                try {
                    PreparedStatement ps = connection.prepareStatement("INSERT INTO `" + prefix + "custom_" + category + "` (?,?) VALUES (?,?)");
                    ps.setString(1, "en-us");
                    ps.setString(2, plugin.getSysLang());
                    ps.setString(3, key);
                    ps.setString(4, translation);
                    ps.executeUpdate();
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        };
        r.runTaskAsynchronously(plugin);
    }

    @Override
    public String getItemName(Material material) {
        if (cache.containsKey("item_"+material.name())) {
            return cache.get("item_"+material.name());
        }

        try {
            PreparedStatement ps = connection.prepareStatement("SELECT `" + plugin.getSysLang() + "` from `" + prefix + "items` where `en-us` = ?");
            ps.setString(1, material.name());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String value = rs.getString(plugin.getSysLang());
                rs.close();
                addCache("item_"+material.name(), value);
                return value;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setItemName(Material material, String translation) {
        BukkitRunnable r = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    PreparedStatement ps = connection.prepareStatement("INSERT INTO `" + prefix + "items` (?,?) VALUES (?,?)");
                    ps.setString(1, "en-us");
                    ps.setString(2, plugin.getSysLang());
                    ps.setString(3, material.name());
                    ps.setString(4, translation);
                    ps.executeUpdate();
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        };
        r.runTaskAsynchronously(plugin);
    }

    @Override
    public String getEntityName(EntityType entityType) {
        if (cache.containsKey("entity_"+entityType.name())) {
            return cache.get("entity_"+entityType.name());
        }

        try {
            PreparedStatement ps = connection.prepareStatement("SELECT `" + plugin.getSysLang() + "` from `" + prefix + "entities` where `en-us` = ?");
            ps.setString(1, entityType.name());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String value = rs.getString(plugin.getSysLang());
                rs.close();
                addCache("entity_"+entityType.name(), value);
                return value;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setEntityName(EntityType entityType, String translation) {
        BukkitRunnable r = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    PreparedStatement ps = connection.prepareStatement("INSERT INTO `" + prefix + "entities` (?,?) VALUES (?,?)");
                    ps.setString(1, "en-us");
                    ps.setString(2, plugin.getSysLang());
                    ps.setString(3, entityType.name());
                    ps.setString(4, translation);
                    ps.executeUpdate();
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        };
        r.runTaskAsynchronously(plugin);
    }

    @Override
    public void save() {

    }

    @Override
    public void closeConn() {
        try {
            if (connection != null && !connection.isClosed())
                connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

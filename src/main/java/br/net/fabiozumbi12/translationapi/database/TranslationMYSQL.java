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
                        "(`en-us` varchar(256) PRIMARY KEY NOT NULL, `"+plugin.getSysLang()+"` varchar(256)) CHARACTER SET utf8 COLLATE utf8_general_ci");
                ps.executeUpdate();
                ps.close();
            }
            rs.close();
            rs = meta.getColumns(null, null, prefix + "text", plugin.getSysLang());
            if (!rs.next()) {
                PreparedStatement st = connection.prepareStatement("ALTER TABLE `" + prefix + "text" + "` ADD `"+plugin.getSysLang()+"` varchar(256)");
                st.executeUpdate();
            }
            rs.close();

            // Custom Type
            rs = meta.getTables(null, null, prefix + "customType", null);
            if (!rs.next()) {
                PreparedStatement ps = connection.prepareStatement("CREATE TABLE `" + prefix + "customType` " +
                        "(`en-us` varchar(100) PRIMARY KEY NOT NULL, `"+plugin.getSysLang()+"` varchar(100)) CHARACTER SET utf8 COLLATE utf8_general_ci");
                ps.executeUpdate();
                ps.close();
            }
            rs.close();
            rs = meta.getColumns(null, null, prefix + "customType", plugin.getSysLang());
            if (!rs.next()) {
                PreparedStatement st = connection.prepareStatement("ALTER TABLE `" + prefix + "customType" + "` ADD `"+plugin.getSysLang()+"` varchar(100)");
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
                        "(`en-us` varchar(256) PRIMARY KEY NOT NULL, `"+plugin.getSysLang()+"` varchar(256)) CHARACTER SET utf8 COLLATE utf8_general_ci");
                ps.executeUpdate();
                ps.close();
            }
            rs.close();
            rs = meta.getColumns(null, null, prefix + "custom_" + table, plugin.getSysLang());
            if (!rs.next()) {
                PreparedStatement st = connection.prepareStatement("ALTER TABLE `" + prefix + "custom_" + table + "` ADD `"+plugin.getSysLang()+"` varchar(256)");
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
        addCache("text_"+key, translation);
        BukkitRunnable r = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    PreparedStatement ps = connection.prepareStatement("INSERT INTO `" + prefix + "text` (`en-us`,`"+plugin.getSysLang()+"`) VALUES (?,?)");
                    ps.setString(1, key);
                    ps.setString(2, translation);
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
            createCustomTable(category);
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
        addCache("custom_"+category+key, translation);
        BukkitRunnable r = new BukkitRunnable() {
            @Override
            public void run() {
                createCustomTable(category);
                try {
                    PreparedStatement ps = connection.prepareStatement("INSERT INTO `" + prefix + "custom_" + category + "` (`en-us`,`"+plugin.getSysLang()+"`) VALUES (?,?)");
                    ps.setString(1, key);
                    ps.setString(2, translation);
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
        addCache("item_"+material.name(), translation);
        BukkitRunnable r = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    PreparedStatement ps = connection.prepareStatement("INSERT INTO `" + prefix + "items` (`en-us`,`"+plugin.getSysLang()+"`) VALUES (?,?)");
                    ps.setString(1, material.name());
                    ps.setString(2, translation);
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
        addCache("entity_"+entityType.name(), translation);
        BukkitRunnable r = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    PreparedStatement ps = connection.prepareStatement("INSERT INTO `" + prefix + "entities` (`en-us`,`"+plugin.getSysLang()+"`) VALUES (?,?)");
                    ps.setString(1, entityType.name());
                    ps.setString(2, translation);
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
    public String getCustomType(String key) {
        if (cache.containsKey("customType_"+key)) {
            return cache.get("customType_"+key);
        }

        try {
            PreparedStatement ps = connection.prepareStatement("SELECT `" + plugin.getSysLang() + "` from `" + prefix + "customType` where `en-us` = ?");
            ps.setString(1, key);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String value = rs.getString(plugin.getSysLang());
                rs.close();
                addCache("customType_"+key, value);
                return value;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setCustomType(String key, String translation) {
        addCache("customType_"+key, translation);
        BukkitRunnable r = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    PreparedStatement ps = connection.prepareStatement("INSERT INTO `" + prefix + "customType` (`en-us`,`"+plugin.getSysLang()+"`) VALUES (?,?)");
                    ps.setString(1, key);
                    ps.setString(2, translation);
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
    public void save() {}

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

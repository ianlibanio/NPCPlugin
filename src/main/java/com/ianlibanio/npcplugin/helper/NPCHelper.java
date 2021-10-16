package com.ianlibanio.npcplugin.helper;

import com.ianlibanio.npcplugin.npc.NPCBase;
import com.ianlibanio.npcplugin.npc.impl.NPC_1_12_R1;
import com.ianlibanio.npcplugin.npc.impl.NPC_1_15_R1;
import com.ianlibanio.npcplugin.npc.impl.NPC_1_17_R1;
import com.ianlibanio.npcplugin.npc.impl.NPC_1_8_R3;
import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.Optional;

public abstract class NPCHelper extends JavaPlugin {

    public abstract void load();

    public abstract void enable();

    public abstract void disable();

    @Override
    public void onLoad() {
        load();
    }

    @Override
    public void onEnable() {
        enable();
    }

    @Override
    public void onDisable() {
        disable();
    }

    @SneakyThrows
    public NPCBase getBaseVersion() {
        val packageName = this.getServer().getClass().getPackage().getName();
        val version = packageName.substring(packageName.lastIndexOf('.') + 1);

        NPCBase npcBase;

        switch (version) {
            case "v1_8_R3":
                npcBase = new NPC_1_8_R3();
                break;
            case "v1_12_R1":
                npcBase = new NPC_1_12_R1();
                break;
            case "v1_15_R1":
                npcBase = new NPC_1_15_R1();
                break;
            case "v1_17_R1":
                npcBase = new NPC_1_17_R1();
                break;
            default:
                throw new Exception("The version " + version + " is not supported.");
        }

        return npcBase;
    }

    public void listener(Listener... listeners) {
        for (Listener listener : listeners) {
            Bukkit.getPluginManager().registerEvents(listener, this);
        }
    }

    public <T> T getService(Class<T> service) {
        Objects.requireNonNull(service, "clazz");

        return Optional
                .ofNullable(Bukkit.getServicesManager().getRegistration(service))
                .map(RegisteredServiceProvider::getProvider)
                .orElseThrow(() -> new IllegalStateException("No registration present for service '" + service.getName() + "'"));
    }

    public <T> T provideService(Class<T> clazz, T instance, ServicePriority priority) {
        Objects.requireNonNull(clazz, "clazz");
        Objects.requireNonNull(instance, "instance");
        Objects.requireNonNull(priority, "priority");

        Bukkit.getServicesManager().register(clazz, instance, this, priority);

        return instance;
    }

    public <T> T provideService(Class<T> clazz, T instance) {
        provideService(clazz, instance, ServicePriority.Normal);
        return instance;
    }

    @SneakyThrows
    protected HikariDataSource getDataSourceFromConfig() {
        final FileConfiguration fileConfiguration = getConfig();
        final HikariDataSource dataSource = new HikariDataSource();

        String host = fileConfiguration.getString("mysql.host", "localhost");
        String database = fileConfiguration.getString("mysql.database", "npc");
        int port = fileConfiguration.getInt("mysql.port", 3306);
        String username = fileConfiguration.getString("mysql.username", "root");
        String password = fileConfiguration.getString("mysql.password", "");

        dataSource.setMaximumPoolSize(20);
        dataSource.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        dataSource.addDataSourceProperty("useSSL", "false");
        dataSource.addDataSourceProperty("autoReconnect", "true");
        dataSource.addDataSourceProperty("cachePrepStmts", "true");
        dataSource.addDataSourceProperty("prepStmtCacheSize", "250");
        dataSource.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        dataSource.addDataSourceProperty("useServerPrepStmts", "true");

        return dataSource;
    }


}
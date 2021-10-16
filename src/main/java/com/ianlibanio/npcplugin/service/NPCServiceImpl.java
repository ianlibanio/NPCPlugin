package com.ianlibanio.npcplugin.service;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.ianlibanio.npcplugin.NPCPlugin;
import com.ianlibanio.npcplugin.data.frame.Frame;
import com.ianlibanio.npcplugin.data.npc.NPC;
import com.ianlibanio.npcplugin.utils.location.LocationSerializer;
import lombok.SneakyThrows;
import lombok.val;
import org.bukkit.Location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class NPCServiceImpl implements NPCService {

    private final NPCPlugin instance;

    @SneakyThrows
    public NPCServiceImpl(NPCPlugin instance) {
        this.instance = instance;

        Connection connection = instance.getHikariDataSource().getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `npcs` " +
                "(`name` VARCHAR(64) NOT NULL," +
                " `skin` VARCHAR(64) NOT NULL," +
                " `uuid` VARCHAR(128) NOT NULL," +
                " `location` VARCHAR(512) NOT NULL," +
                " `frames` MEDIUMTEXT NOT NULL)");

        preparedStatement.executeUpdate();
        connection.close();
    }

    public CompletableFuture<List<NPC>> load() {
        return CompletableFuture.supplyAsync(() -> {
            List<NPC> npcList = Lists.newArrayList();

            try (Connection connection = instance.getHikariDataSource().getConnection()) {
                try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `npcs`;")) {
                    ResultSet resultSet = preparedStatement.executeQuery();

                    while (resultSet.next()) {
                        String name = resultSet.getString("name");
                        String skin = resultSet.getString("skin");
                        String uuid = resultSet.getString("uuid");

                        String serializedLocation = resultSet.getString("location");
                        Location location = LocationSerializer.deserialize(serializedLocation);

                        Gson gson = instance.getGson();

                        val type = new TypeToken<List<Frame>>() {
                        }.getType();

                        List<Frame> frames = gson.fromJson(resultSet.getString("frames"), type);

                        NPC npc = new NPC(name, skin);

                        npc.setUuid(UUID.fromString(uuid));
                        npc.setLocation(location);

                        if (!frames.isEmpty()) {
                            npc.setFrameList(frames);
                        }

                        npcList.add(npc);
                    }
                }
            } catch (SQLException exception) {
                exception.printStackTrace();
            }

            return npcList;
        });
    }

    @Override
    public void save(NPC... npcList) {
        try (Connection connection = instance.getHikariDataSource().getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO `npcs` (`name`, `skin`, `uuid`, `location`, `frames`) VALUES (?,?,?,?,?)")) {
                for (NPC npc : npcList) {
                    try {
                        preparedStatement.setString(1, npc.getDisplayName());
                        preparedStatement.setString(2, npc.getSkinName());
                        preparedStatement.setString(3, npc.getUuid().toString());

                        preparedStatement.setString(4, LocationSerializer.serialize(npc.getLocation()));
                        preparedStatement.setString(5, instance.getGson().toJson(npc.getFrameList()));

                        preparedStatement.addBatch();
                        preparedStatement.clearParameters();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

                preparedStatement.executeBatch();
                preparedStatement.clearBatch();
                connection.close();
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    @SneakyThrows
    public void delete(String name) {
        Connection connection = instance.getHikariDataSource().getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM `npcs` WHERE `name` =  ?");

        preparedStatement.setString(1, name);

        preparedStatement.executeUpdate();
        connection.close();
    }

}

package com.ianlibanio.npcplugin.utils.skin;

import com.ianlibanio.npcplugin.NPCPlugin;
import com.ianlibanio.npcplugin.utils.skin.response.profile.ProfileResponse;
import com.ianlibanio.npcplugin.utils.skin.response.profile.PropertyResponse;
import com.ianlibanio.npcplugin.utils.skin.response.uuid.UUIDResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public class SkinFetcher {

    public Optional<String[]> fetchSkin(String name) {
        final String finalName = name.trim().toUpperCase();

        if (Arrays.stream(DefaultSkins.values()).anyMatch(skin -> skin.name().equals(finalName))) {
            return Optional.of(new String[]{DefaultSkins.valueOf(finalName).value, DefaultSkins.valueOf(finalName).signature});
        }

        final String uuid = getUUID(name).orElse("");
        if (uuid.isEmpty()) return Optional.empty();

        final String output = readURL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
        final ProfileResponse response = NPCPlugin.getInstance().getGson().fromJson(output, ProfileResponse.class);

        if (response.getProperties() != null) {
            final PropertyResponse propertyResponse = response.getProperties()[0];

            return Optional.of(new String[]{propertyResponse.getValue(), propertyResponse.getSignature()});
        }

        return Optional.empty();
    }

    @SneakyThrows
    public Optional<String> getUUID(String name) {
        final String output = readURL("https://api.mojang.com/users/profiles/minecraft/" + name);

        if (output.isEmpty()) return Optional.empty();

        final UUIDResponse response = NPCPlugin.getInstance().getGson().fromJson(output, UUIDResponse.class);

        if (response.getError() != null) return Optional.empty();

        return Optional.of(response.getId());
    }

    @SneakyThrows
    private String readURL(String url) {
        final HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "NPCPlugin");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.setDoOutput(true);

        final InputStream inputStream = connection.getInputStream();

        return new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining());
    }

    @RequiredArgsConstructor
    public enum DefaultSkins {
        STEVE("ewogICJ0aW1lc3RhbXAiIDogMTYwNTUyODUwNjExMSwKICAicHJvZmlsZUlkIiA6ICI0YWEyYzA2M2I5NzY0YzIwYTg1OTY0MThmOWE2Y2YwNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJBem9yIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzc0OTYxYjFlYTA4MjZkNzFjYjcwOTBmZGMxMDVjODE1ZTRlYzUxZjU3YjA1NmFhNWE2NDE2MjFlYmY4MGU4MDQiCiAgICB9CiAgfQp9", "pOwFSfG79W+KEJfJNiKajlQSwUCIzknvtHhZydopzf+SFKIn0K2+ZOTmJBun5NK4JN/G0S7DNmiN7iK20Blx78+f+n+ilFT138ADK4skbrDLkcDk2h5+ZgGhYuSq07SiQFax27YNL+7JlLIUDpdhKl8caB2vb5iYbjTEpLFc/g/pjci8+Pb1JTZ6U98liJZ8q3+IIu5+Seh6JhLKRUh15fhE2eKKzf4Li48LX0SOydufwYdoZ+qxWAUTwdMHJfwh9sABmqG7RiCLk4MRO12kWpRuymkvbhfd7KYVU8LS9rNBlPnwguBg9lEX36V6ojptGsG5KWpJKXL5R9qG7A0jT8acNycWrS8ARwA2pUF6BA8eaWG1aDzONAU9ScRXSsDF0IShvkJObRirmlbwLY8mIV/kOODPRURiw1q8WtCoZJrxBGzFgfTlce4VJ4vkYdjzTHNd/XRwMCh7Ekl510oZ3WbJO0Gw5Ryn4Hghka6e+GkR9EHeUzubKbIQjjm8cHu7jHEtJd2jwvlSh8G+nkFUrQuEHldRd+XgTh51TgfCy9gU80MrKxZRj9Itw0cLpLr/T3MwWqemixRg1lt4OA3aZKHYIPTanwW/CDpi7ptK2J+BFkIpYS5VLLqdcPQIFyLSQ2+yBdG9/0yUu4pfGXBMC/Kn0RgFICRRGNE6+G1jfRo="),
        ALEX("ewogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJpZCIgOiAiMWRjODQ3ZGViZTg2NDBhOGEzODExODkwZTk0ZTdmNmIiLAogICAgICAidHlwZSIgOiAiU0tJTiIsCiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmI5YWIzNDgzZjgxMDZlY2M5ZTc2YmQ0N2M3MTMxMmIwZjE2YTU4Nzg0ZDYwNjg2NGYzYjNlOWNiMWZkN2I2YyIsCiAgICAgICJwcm9maWxlSWQiIDogIjc3MjdkMzU2NjlmOTQxNTE4MDIzZDYyYzY4MTc1OTE4IiwKICAgICAgInRleHR1cmVJZCIgOiAiZmI5YWIzNDgzZjgxMDZlY2M5ZTc2YmQ0N2M3MTMxMmIwZjE2YTU4Nzg0ZDYwNjg2NGYzYjNlOWNiMWZkN2I2YyIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfSwKICAic2tpbiIgOiB7CiAgICAiaWQiIDogIjFkYzg0N2RlYmU4NjQwYThhMzgxMTg5MGU5NGU3ZjZiIiwKICAgICJ0eXBlIiA6ICJTS0lOIiwKICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmI5YWIzNDgzZjgxMDZlY2M5ZTc2YmQ0N2M3MTMxMmIwZjE2YTU4Nzg0ZDYwNjg2NGYzYjNlOWNiMWZkN2I2YyIsCiAgICAicHJvZmlsZUlkIiA6ICI3NzI3ZDM1NjY5Zjk0MTUxODAyM2Q2MmM2ODE3NTkxOCIsCiAgICAidGV4dHVyZUlkIiA6ICJmYjlhYjM0ODNmODEwNmVjYzllNzZiZDQ3YzcxMzEyYjBmMTZhNTg3ODRkNjA2ODY0ZjNiM2U5Y2IxZmQ3YjZjIiwKICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgIH0KICB9LAogICJjYXBlIiA6IG51bGwKfQ==", "Bl/hfaMcGIDwYEl1fqSiPxj2zTGrTMJomqEODvB97VbJ8cs7kfLZIC1bRaCzlFHo5BL0bChL8aQRs/DJGkxmfOC5PfQXubxA4/PHgnNq6cqPZvUcC4hjWdTSKAbZKzHDGiH8aQtuEHVpHeb9T+cutsS0i2zEagWeYVquhFFtctSZEh5+JWxQOba+eh7xtwmzlaXfUDYguzHSOSV4q+hGzSU6osxO/ddiy4PhmFX1MZo237Wp1jE5Fjq+HN4J/cpm/gbtGQBfCuTE7NP3B+PKCXAMicQbQRZy+jaJ+ysK8DJP/EulxyERiSLO9h8eYF5kP5BT5Czhm9FoAwqQlpTXkJSllcdAFqiEZaRNYgJqdmRea4AeyCLPz83XApTvnHyodss1lQpJiEJuyntpUy1/xYNv+EdrNvwCnUPS/3/+jA/VKjAiR9ebKTVZL8A5GHR4mKp7uaaL1DouQa2VOJmQHKo3++v6HGsz1Xk6J7n/8qVUp3oS79WqLxlZoZPBIuQ90xt8Yqhxv6e9FXD4egHsabVj5TO/bZE6pEUaVTrKv49ciE0RqjZHxR5P13hFsnMJTXnT5rzAVCkJOvjaPfZ70WiLJL3X4OOt1TrGK0CoBKQt7yLbU5Eap6P+SLusHrZx+oU4Xspimb79splBxOsbhvb+olbRrJhmxIcrhVIqHDY=");

        private final String value;
        private final String signature;
    }

}

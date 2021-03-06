package eu.chromacube.hub.test;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import eu.chromacube.hub.Hub;
import net.minecraft.server.v1_13_R1.MinecraftEncryption;
import net.minecraft.server.v1_13_R1.MinecraftServer;
import net.minecraft.server.v1_13_R1.PacketLoginInEncryptionBegin;
import org.bukkit.craftbukkit.v1_13_R1.CraftServer;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;


public class test extends PacketAdapter {
    private Hub plugin;
    public test(Hub plugin) {
        super(plugin, PacketType.Login.Client.ENCRYPTION_BEGIN);
        this.plugin = plugin;
    }

    public String getServerId(PacketLoginInEncryptionBegin packet, MinecraftServer server) {
        // Decryption copied from "net.minecraft.server.v1_10_R1.LoginListener.a(PacketLoginInEncryptionBegin)"
        SecretKey loginKey = packet.a(server.G().getPrivate());
        return new BigInteger(MinecraftEncryption.a("", server.G().getPublic(), loginKey)).toString(16);
    }
    public void checkSession(String name, String serverId) {
        try {
            URL url= new URL("https://sessionserver.mojang.com/session/minecraft/hasJoined?username=" + name + "&serverId=" + serverId);
            plugin.getLogger().info("Starting HTTP GET request to \"https://sessionserver.mojang.com/session/minecraft/hasJoined?username=" + name + "&serverId=" + serverId + "\"");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream streamIn = connection.getInputStream();
                byte[] buffer = new byte[streamIn.available()];
                streamIn.read(buffer);
                streamIn.close();
                plugin.getLogger().info("Received data: " + new String(buffer, "UTF-8"));
            } else {
                plugin.getLogger().info("Error: HTTP/" + Integer.toString(connection.getResponseCode()) + ", " + connection.getResponseMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

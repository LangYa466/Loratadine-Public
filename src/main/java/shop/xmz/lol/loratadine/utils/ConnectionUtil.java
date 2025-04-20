package shop.xmz.lol.loratadine.utils;

import net.minecraft.client.multiplayer.ServerData;
import shop.xmz.lol.loratadine.utils.wrapper.Wrapper;

import java.util.Objects;

public class ConnectionUtil implements Wrapper {
    public static String getRemoteIp() {
        if (mc.player == null || mc.level == null) return null;

        String serverIp = "SinglePlayer";

        if (mc.level.isClientSide) {
            final ServerData serverData = mc.getCurrentServer();
            if (serverData != null) {
                serverIp = serverData.ip;
            }
        }

        return serverIp;
    }
}

package net.shortninja.staffplus.staff.broadcast;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.shortninja.staffplus.IocContainer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import static net.shortninja.staffplus.common.Constants.BUNGEE_CORD_CHANNEL;

public class BungeeBroadcastListener implements PluginMessageListener {

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals(BUNGEE_CORD_CHANNEL)) {
            return;
        }
        try {
            ByteArrayDataInput in = ByteStreams.newDataInput(message);
            String subchannel = in.readUTF();
            if (subchannel.equals("StaffPlusPlusBroadcast")) {
                short len = in.readShort();
                byte[] msgbytes = new byte[len];
                in.readFully(msgbytes);

                DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(msgbytes));
                String broadcastMessage = msgin.readUTF();
                IocContainer.getBroadcastService().handleBungeeBroadcast(broadcastMessage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

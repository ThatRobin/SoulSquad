package io.github.thatrobin.soul_squad.entity;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;

public class FakeClientConnection extends ClientConnection
{
    public FakeClientConnection(NetworkSide p)
    {
        super(p);
        // compat with adventure-platform-fabric. This does NOT trigger other vanilla handlers for establishing a channel
        // also makes #isOpen return true, allowing enderpearls to teleport fake players
        //((ClientConnectionInterface)this).setChannel(new EmbeddedChannel());
    }

    @Override
    public void disableAutoRead()
    {
    }

    @Override
    public void handleDisconnection()
    {
    }
}

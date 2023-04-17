package io.github.thatrobin.hivemind.entity;

import io.netty.channel.Channel;

public interface ClientConnectionInterface {
    void setChannel(Channel channel);
}

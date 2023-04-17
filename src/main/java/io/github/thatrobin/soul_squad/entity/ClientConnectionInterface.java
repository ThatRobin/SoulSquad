package io.github.thatrobin.soul_squad.entity;

import io.netty.channel.Channel;

public interface ClientConnectionInterface {
    void setChannel(Channel channel);
}

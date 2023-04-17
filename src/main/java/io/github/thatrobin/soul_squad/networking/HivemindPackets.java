package io.github.thatrobin.hivemind.networking;

import io.github.thatrobin.hivemind.Hivemind;
import net.minecraft.util.Identifier;

public class HivemindPackets {
    public static final Identifier UPDATE_SLOT = Hivemind.identifier("update_slot");
    public static final Identifier SWAP_BODIES = Hivemind.identifier("swap_bodies");
    public static final Identifier UPDATE_UUIDS = Hivemind.identifier("update_uuids");
    public static final Identifier UPDATE_MAP = Hivemind.identifier("update_map");
    public static final Identifier UPDATE_MAP_2 = Hivemind.identifier("update_map_2");
}

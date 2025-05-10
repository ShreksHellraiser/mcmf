package com.github.shrekshellraiser.network;

import net.minecraft.network.FriendlyByteBuf;

public interface ScreenBufferHandler {
    void screenBufferUpdate(FriendlyByteBuf buffer);
}

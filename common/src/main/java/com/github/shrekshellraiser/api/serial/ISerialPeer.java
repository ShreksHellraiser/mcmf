package com.github.shrekshellraiser.api.serial;

import net.minecraft.core.BlockPos;

public interface ISerialPeer {
    void attach(ISerialPeer device);
    void detach(ISerialPeer device);

    void write(char ch); // Assumes STDIO type
    void writePeer(char ch);

    void setConflicting(boolean b);

    ISerialPeer getPeer();
    BlockPos getPos();

}

package top.chancelethay.bingo.gameloop;

import org.jetbrains.annotations.Nullable;

public interface SessionMember
{
    @Nullable
    BingoSession getSession();

    void setup();
}

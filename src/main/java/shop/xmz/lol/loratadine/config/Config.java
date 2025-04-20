package shop.xmz.lol.loratadine.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.File;

@Getter
@AllArgsConstructor
public abstract class Config {
    protected final File file;

    public abstract void read() throws Throwable;

    public abstract boolean write() throws Throwable;
}

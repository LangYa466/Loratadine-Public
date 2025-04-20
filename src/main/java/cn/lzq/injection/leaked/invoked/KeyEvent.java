package cn.lzq.injection.leaked.invoked;

import lombok.AllArgsConstructor;
import lombok.Getter;
import shop.xmz.lol.loratadine.event.impl.Event;

@Getter
@AllArgsConstructor
public class KeyEvent implements Event {
    private int keyCode;
}

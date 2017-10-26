package tk.zhangh.netty.ch7;

import org.msgpack.MessagePack;
import org.msgpack.template.Templates;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZhangHao on 2017/10/26.
 */
public class MsgPackDemo {
    public static void main(String[] args) throws IOException {
        ArrayList<String> src = new ArrayList<>();
        src.add("hello");
        src.add("world");
        src.add("msg");
        src.add("test");

        MessagePack messagePack = new MessagePack();
        byte[] raw = messagePack.write(src);

        List<String> dst = messagePack.read(raw, Templates.tList(Templates.TString));

        for (String s : dst) {
            System.out.println(s);
        }
    }
}

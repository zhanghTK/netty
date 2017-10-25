package tk.zhangh.netty.demo.pojo;

import lombok.AllArgsConstructor;

import java.util.Date;

/**
 * Created by ZhangHao on 2017/10/25.
 */
@AllArgsConstructor
public class UnixTime {
    private final long value;

    public UnixTime() {
        this(System.currentTimeMillis() / 1000L + 2208988800L);
    }

    public long value() {
        return value;
    }

    @Override
    public String toString() {
        return new Date((value() - 2208988800L) * 1000L).toString();
    }
}

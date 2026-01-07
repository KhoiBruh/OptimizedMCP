package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import javax.crypto.Cipher;
import javax.crypto.ShortBufferException;

public class NettyEncryptionTranslator {
    private final Cipher cipher;
    private byte[] field_150505_b = new byte[0];
    private byte[] field_150506_c = new byte[0];

    protected NettyEncryptionTranslator(Cipher cipherIn) {
        cipher = cipherIn;
    }

    private byte[] func_150502_a(ByteBuf buf) {
        int i = buf.readableBytes();

        if (field_150505_b.length < i) {
            field_150505_b = new byte[i];
        }

        buf.readBytes(field_150505_b, 0, i);
        return field_150505_b;
    }

    protected ByteBuf decipher(ChannelHandlerContext ctx, ByteBuf buffer) throws ShortBufferException {
        int i = buffer.readableBytes();
        byte[] abyte = func_150502_a(buffer);
        ByteBuf bytebuf = ctx.alloc().heapBuffer(cipher.getOutputSize(i));
        bytebuf.writerIndex(cipher.update(abyte, 0, i, bytebuf.array(), bytebuf.arrayOffset()));
        return bytebuf;
    }

    protected void cipher(ByteBuf in, ByteBuf out) throws ShortBufferException {
        int i = in.readableBytes();
        byte[] abyte = func_150502_a(in);
        int j = cipher.getOutputSize(i);

        if (field_150506_c.length < j) {
            field_150506_c = new byte[j];
        }

        out.writeBytes(field_150506_c, 0, cipher.update(abyte, 0, i, field_150506_c));
    }
}

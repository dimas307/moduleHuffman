package com.module;

import com.module.Assert;

import java.io.*;

public class BinaryFile {

    private boolean inputFile;
    private RandomAccessFile file;
    private byte buffer;
    private int buf_length;
    private int total_bits;
    private int bitsleft;
    private int bitsread;

    public BinaryFile(String filename, char readOrWrite) {
        buffer = (byte) 0;
        int buf_length = 0;
        total_bits = 0;
        bitsleft = 0;
        bitsread = 0;
        total_bits = 0;
        buffer = 0;
        bitsread = 0;
        try {
            if (readOrWrite == 'w' || readOrWrite == 'W') {
                inputFile = false;
                file = new RandomAccessFile(filename, "rw");
                file.writeInt(0); /* header -- # of bits in the file */
            } else if (readOrWrite == 'r' || readOrWrite == 'R') {
                inputFile = true;
                file = new RandomAccessFile(filename, "r");
                total_bits = file.readInt();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }

    public boolean EndOfFile() {
        Assert.notFalse(inputFile, "EndOfFile only relevant for input files");
        return bitsread == total_bits;
    }

    public boolean EndOfPaddedFile(int padding) {
        Assert.notFalse(inputFile, "EndOfFile only relevant for input files");
        return bitsread == total_bits - padding;
    }

    public char readChar() {
        int charbuf = 0;
        int revcharbuf = 0;
        int i;

        Assert.notFalse(inputFile, "Can only read from input files");

        for (i = 0; i < 8; i++) {
            charbuf = charbuf << 1;
            if (readBit()) {
                charbuf += 1;
            }
        }
        for (i = 0; i < 8; i++) {
            revcharbuf = revcharbuf << 1;
            revcharbuf += charbuf % 2;
            charbuf = charbuf >> 1;
        }
        return (char) revcharbuf;
    }

    public void writeChar(char c) {
        Assert.notFalse(!inputFile, "Can only write to output files");

        int i;
        int charbuf = (int) c;
        for (i = 0; i < 8; i++) {
            writeBit(charbuf % 2 > 0);
            charbuf = charbuf >> 1;
        }
    }

    public void writeBit(boolean bit) {
        byte bit_;
        Assert.notFalse(!inputFile, "Can't write to an input file");
        total_bits++;

        if (bit)
            bit_ = 1;
        else
            bit_ = 0;
        buffer |= (bit_ << (7 - buf_length++));
        try {
            if (buf_length == 8) {
                file.writeByte(buffer);
                buf_length = 0;
                buffer = 0;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }

    public boolean readBit() {

        Assert.notFalse(inputFile, "Can't read from an output file");
        Assert.notFalse(bitsread < total_bits, "Read past end of file");
        try {
            if (bitsleft == 0) {
                buffer = file.readByte();
                bitsleft = 8;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
        bitsread++;
        return (((buffer >> --bitsleft) & 0x01) > 0);
    }

    public void close() {
        try {
            if (!inputFile) {
                if (buf_length != 0) {
                    while (buf_length < 8) {
                        buffer |= (0 << (7 - buf_length++));
                    }
                    file.writeByte(buffer);
                }
                file.seek(0);
                file.writeInt(total_bits);
            }
            file.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }

    }


}
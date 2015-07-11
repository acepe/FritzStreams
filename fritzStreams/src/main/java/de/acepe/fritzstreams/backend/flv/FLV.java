/**
 * JToothpaste - Copyright (C) 2007 Matthias Schuhmann
 * <p/>
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package de.acepe.fritzstreams.backend.flv;

import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

public class FLV {

    private static final String TAG = "FLV";

    private String inFile;
    private String outFile;

    private boolean video = false;
    private boolean audio = false;
    private byte version;
    private String signature;
    private int offset;

    boolean debug = false;

    public FLV() {
    }

    public FLV(String inFile, String outFile) {
        this.inFile = inFile;
        this.outFile = outFile;
    }

    public void convert() {
        try {
            RandomAccessFile mp3file = new RandomAccessFile(outFile, "rw");
            ReadableByteChannel channel = new FileInputStream(inFile).getChannel();

            // Header:
            /*
             * Signature byte[3] �FLV� Always �FLV� Version uint8 �\x01� (1) Currently 1 for known FLV files Flags uint8
             * bitmask �\x05� (5, audio+video) Bitmask: 4 is audio, 1 is video Offset uint32_be �\x00\x00\x00\x09� (9)
             * Total size of header (always 9 for known FLV files)
             */

            ByteData byteData = read(channel, 9);
            ByteBuffer buf = byteData.getByteBuffer();
            signature = new String(getBytes(buf, 0, 3)); // alway FLV
            version = getBytes(buf, 4, 1)[0]; // 1.0
            byte audio_video = getBytes(buf, 5, 1)[0]; // audio and/or video
            if ((audio_video & 4) == 4)
                audio = true; // 4 is audio
            if ((audio_video & 1) == 1)
                video = true; // 1 is video
            byte[] bOffset = getBytes(buf, 6, 4);
            offset = getInt(bOffset); // always 9
            // log.debug(toString());

            // Tags
            read(channel, 3);
            readTags(channel, mp3file);

            channel.close();
            mp3file.close();
        } catch (IOException e) {
            Log.e(TAG, "Exception during FLV->MP3 conversion", e);
        }
    }

    private void readTags(ReadableByteChannel channel, RandomAccessFile outFile) throws IOException {
        ByteBuffer buf;
        FLVTag tag;

        ByteData byteData = read(channel, 1);
        while (byteData != null && !byteData.isEof()) {
            byteData = read(channel, 11);
            buf = byteData.getByteBuffer();

            byte type = getBytes(buf, 0, 1)[0];
            int bodyLength = getInt(getBytes(buf, 1, 3));
            int timestamp = getInt(getBytes(buf, 4, 3));
            byte timestampExt = getBytes(buf, 7, 1)[0];
            int streamId = getInt(getBytes(buf, 8, 3));

            timestamp |= timestampExt << 24;

            switch (type) {
                case FLVTag.TYPE_AUDIO: {
                    tag = new FLVTagAudio(type, bodyLength, timestamp, timestampExt, streamId);
                    tag.read(channel);
                    outFile.write(tag.getBody());
                    break;
                }
                default: {
                    byteData = read(channel, bodyLength);
                }
            }
            // Read 4 bytes
            read(channel, 4);
        }
    }

    public ByteData read(ReadableByteChannel channel, int bytes) throws IOException {
        if (bytes == 0) {
            return null;
        }
        ByteBuffer buf = ByteBuffer.allocateDirect(bytes);
        buf.rewind();
        int eof = channel.read(buf);
        buf.rewind();

        if (debug) {
            byte[] b = getBytes(buf);
            for (int i = 0; i < b.length; i++) {
                Log.d(TAG, (i + 1) + ": " + b[i]);
            }
            buf.rewind();
        }

        return new ByteData(buf, (eof == -1));
    }

    public byte[] getBytes(ByteBuffer buf) {
        return getBytes(buf, 0, buf.capacity());
    }

    public byte[] getBytes(ByteBuffer buf, int from, int len) {
        byte[] bytes = new byte[len];
        buf.get(bytes, 0, bytes.length);
        return bytes;
    }

    public static int getInt(byte[] data) {

        int number = 0;
        for (int i = 0; i < data.length; ++i) {
            byte b = data[(data.length - 1) - i];
            int bitsToShift = i << 3;
            int add = (b & 0xff) << bitsToShift;
            number = number | add;
        }
        return number;
    }

    public static long arr2long(byte[] arr, int start) {
        int i;
        int len = 4;
        int cnt = 0;
        byte[] tmp = new byte[len];
        for (i = start; i < (start + len); i++) {
            tmp[cnt] = arr[i];
            cnt++;
        }
        long accum = 0;
        i = 0;
        for (int shiftBy = 0; shiftBy < 32; shiftBy += 8) {
            accum |= ((long) (tmp[i] & 0xff)) << shiftBy;
            i++;
        }
        return accum;
    }

    public String toString() {
        return "FLVInformation - Signature:"
                + signature
                + " Version:"
                + version
                + " hasAudio:"
                + audio
                + " hasVideo:"
                + video
                + " Offset:"
                + offset;
    }

}

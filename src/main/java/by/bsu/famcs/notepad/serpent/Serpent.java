package by.bsu.famcs.notepad.serpent;

import edu.rit.util.Packing;
import org.apache.commons.lang3.ArrayUtils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Serpent {

    private int[] preKeys = new int[140];

    private int blockSize = 16;

    public void expandToMaxKeySize(byte[] k) {
        if (k.length != 256) {
            byte[] key = new byte[256];
            System.arraycopy(k, 0, key, 0, k.length);
            key[k.length] = (byte) 0x80;
            for (int i = k.length + 1; i < 256; i++) {
                key[i] = (byte) 0x00;
            }
        }
    }

    public String encrypt(String input) {
        byte[] fileData = input.getBytes(UTF_8);
        byte b = "\0".getBytes(UTF_8)[0];
        List<Byte> tmp = new ArrayList<>();
        for (byte i : fileData)
            tmp.add(i);
        if (fileData.length % 16 != 0) {
            int closest = fileData.length;
            while (closest % 16 != 0) {
                closest += 1;
            }
            int diff = closest - fileData.length;
            for (int i = 0; i < diff; i++) {
                tmp.add(b);
            }
            Byte[] bytes = tmp.toArray(new Byte[0]);
            fileData = ArrayUtils.toPrimitive(bytes);
        }

        byte[] iv = new byte[16];

        StringBuilder str = new StringBuilder();

        Packing.unpackIntLittleEndian(Integer.parseInt("1234"), iv, 0);
        encrypt(iv);

        for (int i = 0; i < fileData.length; i += 16) {
            byte[] block = new byte[]{
                    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
            };
            for (int n = 0; n < 16 && i + n < fileData.length; n++) {
                block[n] = (byte) (fileData[i + n] ^ iv[n]);
            }
            encrypt(block);
            iv = block;

            StringBuffer result = new StringBuffer();
            for (byte bl : block) {
                result.append(String.format("%02x", bl));
            }
            str.append(result.toString());
        }

        return str.toString();
    }

    public String decrypt(String input) {
        byte[] fileData = hexStringToByteArray(input);

        byte b = "\0".getBytes(UTF_8)[0];
        List<Byte> tmp = new ArrayList<>();
        for (byte i : fileData)
            tmp.add(i);
        if (fileData.length % 16 != 0) {
            int closest = fileData.length;
            while (closest % 16 != 0) {
                closest += 1;
            }
            int diff = closest - fileData.length;
            for (int i = 0; i < diff; i++) {
                tmp.add(b);
            }
            Byte[] bytes = tmp.toArray(new Byte[0]);
            fileData = ArrayUtils.toPrimitive(bytes);
        }

        byte[] iv = new byte[16];

        StringBuilder str = new StringBuilder();

        Packing.unpackIntLittleEndian(Integer.parseInt("1234"), iv, 0);
        encrypt(iv);

        for (int i = 0; i < fileData.length; i += 16) {
            byte[] block = new byte[]{
                    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
            };
            for (int n = 0; n < 16 && n < fileData.length; n++) {
                block[n] = fileData[i + n];
            }
            byte[] savedForIV = Arrays.copyOf(block, 16);
            decrypt(block);
            for (int n = 0; n < 16; n++) {
                block[n] = (byte) (block[n] ^ iv[n]);
            }
            iv = savedForIV;

            String s = new String(block, UTF_8);
            str.append(s);
        }

        return str.toString().trim();
    }

    public void setKey(byte[] k) {
        byte[] key = k;

        setPreKeys(key);
    }

    private void setPreKeys(byte[] key) {
        for (int i = 0; i < 8; i++)
            preKeys[i] = Packing.packIntBigEndian(new byte[]{key[4 * i], key[4 * i + 1], key[4 * i + 2], key[4 * i + 3]}, 0);

        for (int i = 8; i < preKeys.length; i++) {
            byte[] prnt;
            int phi = 0x9e3779b9;
            int tmp = preKeys[i - 8] ^ preKeys[i - 5] ^ preKeys[i - 3] ^ preKeys[i - 1] ^
                    (i - 8) ^ phi;
            preKeys[i] = (tmp << 11) | (tmp >>> (21));
            prnt = new byte[4];
            Packing.unpackIntBigEndian(preKeys[i], prnt, 0);
        }
    }

    private void encrypt(byte[] text) {
        byte[] data = initPermutation(text);
        data = new byte[]{
                data[12], data[13], data[14], data[15],
                data[8], data[9], data[10], data[11],
                data[4], data[5], data[6], data[7],
                data[0], data[1], data[2], data[3],
        };
        byte[] roundKey;
        for (int i = 0; i < 32; i++) {
            roundKey = getRoundKey(i);
            for (int n = 0; n < 16; n++) {
                data[n] = (byte) (data[n] ^ roundKey[n]);
            }
            data = sBox(data, i, SerpentTables.sBox);

            if (i == 31) {

                roundKey = getRoundKey(32);
                for (int n = 0; n < 16; n++) {
                    data[n] = (byte) (data[n] ^ roundKey[n]);
                }
            } else {
                data = linearTransformation(data, false);
            }
        }
        permute(data, text);
    }

    private void decrypt(byte[] text) {
        byte[] temp = new byte[]{
                text[3], text[2], text[1], text[0],
                text[7], text[6], text[5], text[4],
                text[11], text[10], text[9], text[8],
                text[15], text[14], text[13], text[12],
        };
        byte[] data = initPermutation(temp);
        byte[] roundKey = getRoundKey(32);
        for (int n = 0; n < 16; n++) {
            data[n] = (byte) (data[n] ^ roundKey[n]);
        }
        for (int i = 31; i >= 0; i--) {
            if (i != 31) {
                data = linearTransformation(data, true);
            }
            data = sBox(data, i, SerpentTables.invSBox);
            roundKey = getRoundKey(i);
            for (int n = 0; n < 16; n++) {
                data[n] = (byte) (data[n] ^ roundKey[n]);
            }
        }
        permute(data, text);
    }

    private void permute(byte[] data, byte[] text) {
        data = finalPermutation(data);
        text[0] = data[3];
        text[1] = data[2];
        text[2] = data[1];
        text[3] = data[0];
        text[4] = data[7];
        text[5] = data[6];
        text[6] = data[5];
        text[7] = data[4];
        text[8] = data[11];
        text[9] = data[10];
        text[10] = data[9];
        text[11] = data[8];
        text[12] = data[15];
        text[13] = data[14];
        text[14] = data[13];
        text[15] = data[12];
    }

    private byte[] initPermutation(byte[] data) {
        byte[] output = new byte[16];
        for (int i = 0; i < 128; i++) {
            int bit = (data[(SerpentTables.ip[i]) / 8] >>> ((SerpentTables.ip[i]) % 8)) & 0x01;
            if ((bit & 0x01) == 1)
                output[15 - (i / 8)] |= 1 << (i % 8);
            else
                output[15 - (i / 8)] &= ~(1 << (i % 8));
        }
        return output;
    }

    private byte[] finalPermutation(byte[] data) {
        byte[] output = new byte[16];
        for (int i = 0; i < 128; i++) {
            int bit = (data[15 - SerpentTables.fp[i] / 8] >>> (SerpentTables.fp[i] % 8)) & 0x01;
            if ((bit & 0x01) == 1)
                output[(i / 8)] |= 1 << (i % 8);
            else
                output[(i / 8)] &= ~(1 << (i % 8));
        }
        return output;
    }

    private byte[] sBox(byte[] data, int round, byte[][] sBox) {
        byte[] toUse = sBox[round % 8];
        byte[] output = new byte[blockSize];
        for (int i = 0; i < blockSize; i++) {
            int curr = data[i] & 0xFF;
            byte low4 = (byte) (curr >>> 4);
            byte high4 = (byte) (curr & 0x0F);
            output[i] = (byte) ((toUse[low4] << 4) ^ (toUse[high4]));
        }
        return output;
    }

    private byte[] linearTransformation(byte[] data, boolean isInv) {
        data = finalPermutation(data);
        byte[] output;
        ByteBuffer buffer = ByteBuffer.wrap(data);
        int[] x = new int[4];
        x[0] = buffer.getInt();
        x[1] = buffer.getInt();
        x[2] = buffer.getInt();
        x[3] = buffer.getInt();

        if (isInv)
            invLinearTransform(x);
        else
            linearTransform(x);

        buffer.clear();
        buffer.putInt(x[0]);
        buffer.putInt(x[1]);
        buffer.putInt(x[2]);
        buffer.putInt(x[3]);

        output = buffer.array();
        output = initPermutation(output);

        return output;
    }

    private void linearTransform(int[] x) {
        x[0] = ((x[0] << 13) | (x[0] >>> (32 - 13)));
        x[2] = ((x[2] << 3) | (x[2] >>> (32 - 3)));
        x[1] = x[1] ^ x[0] ^ x[2];
        x[3] = x[3] ^ x[2] ^ (x[0] << 3);
        x[1] = (x[1] << 1) | (x[1] >>> (32 - 1));
        x[3] = (x[3] << 7) | (x[3] >>> (32 - 7));
        x[0] = x[0] ^ x[1] ^ x[3];
        x[2] = x[2] ^ x[3] ^ (x[1] << 7);
        x[0] = (x[0] << 5) | (x[0] >>> (32 - 5));
        x[2] = (x[2] << 22) | (x[2] >>> (32 - 22));
    }

    private void invLinearTransform(int[] x) {
        x[2] = (x[2] >>> 22) | (x[2] << (32 - 22));
        x[0] = (x[0] >>> 5) | (x[0] << (32 - 5));
        x[2] = x[2] ^ x[3] ^ (x[1] << 7);
        x[0] = x[0] ^ x[1] ^ x[3];
        x[3] = (x[3] >>> 7) | (x[3] << (32 - 7));
        x[1] = (x[1] >>> 1) | (x[1] << (32 - 1));
        x[3] = x[3] ^ x[2] ^ (x[0] << 3);
        x[1] = x[1] ^ x[0] ^ x[2];
        x[2] = (x[2] >>> 3) | (x[2] << (32 - 3));
        x[0] = (x[0] >>> 13) | (x[0] << (32 - 13));
    }

    private byte[] getRoundKey(int round) {
        int k0 = preKeys[4 * round + 8];
        int k1 = preKeys[4 * round + 9];
        int k2 = preKeys[4 * round + 10];
        int k3 = preKeys[4 * round + 11];
        int box = (((3 - round) % 8) + 8) % 8;
        byte[] in = new byte[16];
        for (int j = 0; j < 32; j += 2) {
            in[j / 2] = (byte) (((k0 >>> j) & 0x01) |
                    ((k1 >>> j) & 0x01) << 1 |
                    ((k2 >>> j) & 0x01) << 2 |
                    ((k3 >>> j) & 0x01) << 3 |
                    ((k0 >>> j + 1) & 0x01) << 4 |
                    ((k1 >>> j + 1) & 0x01) << 5 |
                    ((k2 >>> j + 1) & 0x01) << 6 |
                    ((k3 >>> j + 1) & 0x01) << 7);
        }
        byte[] out = sBox(in, box, SerpentTables.sBox);
        byte[] key = new byte[16];
        for (int i = 3; i >= 0; i--) {
            for (int j = 0; j < 4; j++) {
                key[3 - i] |= (out[i * 4 + j] & 0x01) << (j * 2) | ((out[i * 4 + j] >>> 4) & 0x01) << (j * 2 + 1);
                key[7 - i] |= ((out[i * 4 + j] >>> 1) & 0x01) << (j * 2) | ((out[i * 4 + j] >>> 5) & 0x01) << (j * 2 + 1);
                key[11 - i] |= ((out[i * 4 + j] >>> 2) & 0x01) << (j * 2) | ((out[i * 4 + j] >>> 6) & 0x01) << (j * 2 + 1);
                key[15 - i] |= ((out[i * 4 + j] >>> 3) & 0x01) << (j * 2) | ((out[i * 4 + j] >>> 7) & 0x01) << (j * 2 + 1);
            }
        }
        return initPermutation(key);
    }

    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}

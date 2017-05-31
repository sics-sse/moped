/*
 * Copyright (c) 2012-2013 Wolf480pl (wolf480@interia.pl)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package service;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

public class PHPass {
    private static String itoa64 = "./0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private int iterationCountLog2;
    private SecureRandom randomGen;

    public PHPass(int iterationCountLog2) {
        if (iterationCountLog2 < 4 || iterationCountLog2 > 31) {
            iterationCountLog2 = 8;
        }
        this.iterationCountLog2 = iterationCountLog2;
        this.randomGen = new SecureRandom();
    }

    private String encode64(byte[] src, int count) {
        int i, value;
        String output = "";
        i = 0;

        if (src.length < count) {
            byte[] t = new byte[count];
            System.arraycopy(src, 0, t, 0, src.length);
            Arrays.fill(t, src.length, count - 1, (byte) 0);
            src = t;
        }

        do {
            value = src[i] + (src[i] < 0 ? 256 : 0);
            ++i;
            output += itoa64.charAt(value & 63);
            if (i < count) {
                value |= (src[i] + (src[i] < 0 ? 256 : 0)) << 8;
            }
            output += itoa64.charAt((value >> 6) & 63);
            if (i++ >= count) {
                break;
            }
            if (i < count) {
                value |= (src[i] + (src[i] < 0 ? 256 : 0)) << 16;
            }
            output += itoa64.charAt((value >> 12) & 63);
            if (i++ >= count) {
                break;
            }
            output += itoa64.charAt((value >> 18) & 63);
        } while (i < count);
        return output;
    }

    private String cryptPrivate(String password, String setting) {
        String output = "*0";
        if (((setting.length() < 2) ? setting : setting.substring(0, 2)).equalsIgnoreCase(output)) {
            output = "*1";
        }
        String id = (setting.length() < 3) ? setting : setting.substring(0, 3);
        if (!(id.equals("$P$") || id.equals("$H$"))) {
            return output;
        }
        int countLog2 = itoa64.indexOf(setting.charAt(3));
        if (countLog2 < 7 || countLog2 > 30) {
            return output;
        }
        int count = 1 << countLog2;
        String salt = setting.substring(4, 4 + 8);
        if (salt.length() != 8) {
            return output;
        }
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return output;
        }
        byte[] pass = stringToUtf8(password);
        byte[] hash = md.digest(stringToUtf8(salt + password));
        do {
            byte[] t = new byte[hash.length + pass.length];
            System.arraycopy(hash, 0, t, 0, hash.length);
            System.arraycopy(pass, 0, t, hash.length, pass.length);
            hash = md.digest(t);
        } while (--count > 0);
        output = setting.substring(0, 12);
        output += encode64(hash, 16);
        return output;
    }

    private String gensaltPrivate(byte[] input) {
        String output = "$P$";
        output += itoa64.charAt(Math.min(this.iterationCountLog2 + 5, 30));
        output += encode64(input, 6);
        return output;
    }

    private byte[] stringToUtf8(String string) {
        try {
            return string.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new UnsupportedOperationException("This system doesn't support UTF-8!", e);
        }
    }

    public String HashPassword(String password) {
        byte random[] = new byte[6];
        this.randomGen.nextBytes(random);
        // Unportable hashes (Blowfish, EXT_DES) could be added here, but I won't do this.
        String hash = cryptPrivate(password, gensaltPrivate(stringToUtf8(new String(random))));
        if (hash.length() == 34) {
            return hash;
        }
        return "*";
    }

    public boolean CheckPassword(String password, String storedHash) {
        String hash = cryptPrivate(password, storedHash);
        MessageDigest md = null;
        if (hash.startsWith("*")) {	// If not phpass, try some algorythms from unix crypt()
            if (storedHash.startsWith("$6$")) {
                try {
                    md = MessageDigest.getInstance("SHA-512");
                } catch (NoSuchAlgorithmException e) {
                    md = null;
                }
            }
            if (md == null && storedHash.startsWith("$5$")) {
                try {
                    md = MessageDigest.getInstance("SHA-256");
                } catch (NoSuchAlgorithmException e) {
                    md = null;
                }
            }
            if (md == null && storedHash.startsWith("$2")) {
                return BCrypt.checkpw(password, storedHash);
            }
            if (md == null && storedHash.startsWith("$1$")) {
                try {
                    md = MessageDigest.getInstance("MD5");
                } catch (NoSuchAlgorithmException e) {
                    md = null;
                }
            }
            // STD_DES and EXT_DES not supported yet.
            if (md != null) {
                hash = new String(md.digest(password.getBytes()));
            }
        }
        return hash.equals(storedHash);
    }

}

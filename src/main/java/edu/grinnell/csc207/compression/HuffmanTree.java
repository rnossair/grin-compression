package edu.grinnell.csc207.compression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * A HuffmanTree derives a space-efficient coding of a collection of byte
 * values.
 *
 * The huffman tree encodes values in the range 0--255 which would normally
 * take 8 bits. However, we also need to encode a special EOF character to
 * denote the end of a .grin file. Thus, we need 9 bits to store each
 * byte value. This is fine for file writing (modulo the need to write in
 * byte chunks to the file), but Java does not have a 9-bit data type.
 * Instead, we use the next larger primitive integral type, short, to store
 * our byte values.
 */
public class HuffmanTree {

    Node root;
    HashMap<Short, String> charTable;

    private class Node implements Comparable<Node> {
        short c;
        Integer val;
        Node left;
        Node right;
        boolean leaf;

        public Node(Integer val, Node left, Node right) {
            this.val = val;
            c = 0;
            leaf = false;
            this.left = left;
            this.right = right;
        }

        public Node(short c, Integer val) {
            this.c = c;
            this.val = val;
            left = null;
            right = null;
            leaf = true;
        }

        @Override
        public int compareTo(Node other) {
            return val.compareTo(other.val);
        }
    }

    /**
     * Constructs a new HuffmanTree from a frequency map.
     * 
     * @param freqs a map from 9-bit values to frequencies.
     */
    public HuffmanTree(Map<Short, Integer> freqs) {
        charTable = new HashMap<Short, String>();
        PriorityQueue<Node> pQueue = new PriorityQueue<Node>();
        short eof = (short) 256;
        freqs.put(eof, 1);
        for (Short key : freqs.keySet()) {
            pQueue.add(new Node(key, freqs.get(key)));
        }
        while (pQueue.size() > 1) {
            Node left = pQueue.poll();
            Node right = pQueue.poll();
            pQueue.add(new Node(left.val + right.val, left, right));
        }
        root = pQueue.poll();
    }

    /**
     * Helps build a huffman tree from a serialized tree
     * 
     * @param in
     * @return
     */

    public Node deserialize(BitInputStream in) {
        if (in.readBit() == 0) {
            return new Node((short) in.readBits(9), 1);
        }
        Node left = deserialize(in);
        Node right = deserialize(in);
        return new Node(0, left, right);
    }

    /**
     * Constructs a new HuffmanTree from the given file.
     * 
     * @param in the input file (as a BitInputStream)
     */
    public HuffmanTree(BitInputStream in) {
        charTable = new HashMap<Short, String>();
        root = deserialize(in);
        in.readBits(9);
    }

    public void bctHelper(Node rootNode, String str) {
        if (rootNode.leaf) {
            charTable.put(rootNode.c, str);
        } else {
            bctHelper(rootNode.left, str + '0');
            bctHelper(rootNode.right, str + '1');
        }
    }

    /**
     * Builds a character table that associates each character to its huffman code
     */

    public void buildCharTable() {
        bctHelper(root, "");
    }

    public void serializeHelper(Node rootNode, BitOutputStream out) {
        if (rootNode.leaf) {
            out.writeBit(0);
            out.writeBits(rootNode.c, 9);
        } else {
            out.writeBit(1);
            serializeHelper(rootNode.left, out);
            serializeHelper(rootNode.right, out);
        }
    }

    /**
     * Writes this HuffmanTree to the given file as a stream of bits in a
     * serialized format.
     * 
     * @param out the output file as a BitOutputStream
     */
    public void serialize(BitOutputStream out) {
        serializeHelper(root, out);
        out.writeBits(257, 9); // we are using 257 as an "end of serial tree character"
    }

    /**
     * Encodes the file given as a stream of bits into a compressed format
     * using this Huffman tree. The encoded values are written, bit-by-bit
     * to the given BitOuputStream.
     * 
     * @param in  the file to compress.
     * @param out the file to write the compressed output to.
     */
    public void encode(BitInputStream in, BitOutputStream out) {

        // magic number
        out.writeBits(1846, 32);

        // output the serialized tree
        serialize(out);

        buildCharTable(); // building the char codes from huffman tree

        // writing payload
        short currByte = (short) in.readBits(8);
        while (currByte != -1) {
            String code = charTable.get((Short) currByte);
            for (char c : code.toCharArray()) {
                if (c == '0') {
                    out.writeBit(0);
                } else {
                    out.writeBit(1);
                }
            }
            currByte = (short) in.readBits(8);
        }
        // adding the EOF char
        for (char c : charTable.get((short) 256).toCharArray()) {
            if (c == '0') {
                out.writeBit(0);
            } else {
                out.writeBit(1);
            }
        }
    }

    /**
     * Decodes a stream of huffman codes from a file given as a stream of
     * bits into their uncompressed form, saving the results to the given
     * output stream. Note that the EOF character is not written to out
     * because it is not a valid 8-bit chunk (it is 9 bits).
     * 
     * @param in  the file to decompress.
     * @param out the file to write the decompressed output to.
     */
    public void decode(BitInputStream in, BitOutputStream out) {

        short currBit = (short) in.readBit();
        Node currNode = root;
        buildCharTable();
        // System.out.println("hi");
        // System.out.println("Does the tree contain EOF? " +
        // charTable.containsKey((short) 256));
        // navigating the tree for each bit
        while (currBit != -1) {
            // if(dump){
            // currBit = (short) in.readBit();
            // }
            if (currBit == 0) {
                currNode = currNode.left;
            } else {
                currNode = currNode.right;
            }
            // outputing characters to file (except eof)
            if (currNode.leaf) {
                if (currNode.c != 256) {
                    // System.out.println((char) currNode.c);
                    out.writeBits(currNode.c, 8);
                    currNode = root;
                } else {
                    // System.out.println("I CAN SEEEEEEEEEEEEE");

                    return;
                }
            }
            currBit = (short) in.readBit();
        }
    }
}

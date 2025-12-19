package edu.grinnell.csc207.compression;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import org.w3c.dom.Node;

/**
 * The driver for the Grin compression program.
 */
public class Grin {
    /**
     * Decodes the .grin file denoted by infile and writes the output to the
     * .grin file denoted by outfile.
     * 
     * @param infile  the file to decode
     * @param outfile the file to ouptut to
     * @throws IOException 
     */
    public static void decode(String infile, String outfile) throws IOException {
        BitInputStream in = new BitInputStream(infile);
        BitOutputStream out = new BitOutputStream(outfile);
        int magicNum = in.readBits(32);
        if (magicNum != 1846) {
            throw new IllegalArgumentException("Not a valid .grin file!!");
        }
        HuffmanTree hTree = new HuffmanTree(in);
        hTree.decode(in, out);
        out.close();
    }

    /**
     * Creates a mapping from 8-bit sequences to number-of-occurrences of
     * those sequences in the given file. To do this, read the file using a
     * BitInputStream, consuming 8 bits at a time.
     * 
     * @param file the file to read
     * @return a freqency map for the given file
     * @throws IOException 
     */
    public static Map<Short, Integer> createFrequencyMap(String file) throws IOException {
        // PriorityQueue<Node> pQueue = new PriorityQueue<Node>();
        HashMap<Short, Integer> shortMap = new HashMap<Short, Integer>();
        BitInputStream in = new BitInputStream(file);
        short eof = (short) 256;
        shortMap.put(eof, 1);
        short currByte = 0;
        while (currByte != -1) {
            currByte = (short) in.readBits(8);
            if (currByte == -1) {
                break;
            }
            if (shortMap.containsKey(currByte)) { // increment frequency if map already contains char
                shortMap.put(currByte, shortMap.get(currByte) + 1);
            } else {
                shortMap.put(currByte, 1);
            }

        }
        in.close();
        return shortMap;
    }

    /**
     * Encodes the given file denoted by infile and writes the output to the
     * .grin file denoted by outfile.
     * 
     * @param infile  the file to encode.
     * @param outfile the file to write the output to.
     * @throws IOException 
     */
    public static void encode(String infile, String outfile) throws IOException {
        Map<Short, Integer> freqMap = createFrequencyMap(infile);
        HuffmanTree hTree = new HuffmanTree(freqMap);
        BitInputStream in = new BitInputStream(infile);
        BitOutputStream out = new BitOutputStream(outfile);
        hTree.encode(in, out);
        in.close();
        out.close();
    }

    /**
     * The entry point to the program.
     * 
     * @param args the command-line arguments.
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        // TODO: fill me in!
        System.out.println("Usage: java Grin <encode|decode> <infile> <outfile>");
        if(args.length == 0){
            throw new IllegalArgumentException("No arguments provided to command!");
        }
        switch(args[0]){
            case "encode":
                encode(args[1], args[2]);
                break;
            case "decode":
                decode(args[1], args[2]);
                break;
            default:
                throw new IllegalArgumentException("Not a valid operation!");
        }
    }
}

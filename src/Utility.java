package src;
import java.io.*;
import java.util.*;


public class Utility {
    private static int maxDepth = 200;
    private static int percentage = 0;

    Utility() {
    }

    public Utility(int maxDepth, int similarityPercent) {
        this.maxDepth = maxDepth;
        this.percentage = similarityPercent;
    }

    // Below is encoding for larger images(~4000x4000)
    // public static String encode(ArrayList<simpleNode> leafs) throws IOException {
    // ByteArrayOutputStream baos = new ByteArrayOutputStream();
    // for (simpleNode leaf : leafs) {
    // // Write x, y, width, and height as 12-bit integers
    // baos.write((byte) ((leaf.x >> 4) & 0xFF));
    // baos.write((byte) ((leaf.x << 4) & 0xF0 | (leaf.y >> 8) & 0x0F));
    // baos.write((byte) (leaf.y & 0xFF));
    // baos.write((byte) ((leaf.width >> 4) & 0xFF));
    // baos.write((byte) ((leaf.width << 4) & 0xF0 | (leaf.height >> 8) & 0x0F));
    // baos.write((byte) (leaf.height & 0xFF));
    // // Write color as a 24-bit integer
    // baos.write((byte) ((leaf.color >> 16) & 0xFF));
    // baos.write((byte) ((leaf.color >> 8) & 0xFF));
    // baos.write((byte) (leaf.color & 0xFF));
    // }
    // return Base64.getEncoder().encodeToString(baos.toByteArray());
    // }

    /**
     * Encodes an ArrayList of simpleNodes into a Base64-encoded string.
     * 
     * @param leafs the ArrayList of simpleNodes to encode
     * @return the Base64-encoded string
     * @throws IOException if an I/O error occurs
     */
    public static String encode(ArrayList<simpleNode> leafs) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (simpleNode leaf : leafs) {
            // Write x, y, width, and height as 10-bit integers
            baos.write((byte) ((leaf.x >> 2) & 0xFF));
            baos.write((byte) ((leaf.x << 6) & 0xC0 | (leaf.y >> 4) & 0x3F));
            baos.write((byte) ((leaf.y << 4) & 0xF0 | (leaf.width >> 6) & 0x0F));
            baos.write((byte) ((leaf.width << 2) & 0xFC | (leaf.height >> 8) & 0x03));
            baos.write((byte) (leaf.height & 0xFF));
            // Write color as a 24-bit integer
            baos.write((byte) ((leaf.color >> 16) & 0xFF));
            baos.write((byte) ((leaf.color >> 8) & 0xFF));
            baos.write((byte) (leaf.color & 0xFF));
        }
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    // Below is encoding for larger images(~4000x4000)
    // public static ArrayList<simpleNode> decode(String encoded) throws IOException
    // {
    // ArrayList<simpleNode> leafs = new ArrayList<>();
    // byte[] data = Base64.getDecoder().decode(encoded);
    // for (int i = 0; i + 8 <= data.length; i += 9) {
    // // Read x, y, width, and height as 12-bit integers
    // int x = ((data[i] & 0xFF) << 4) | ((data[i + 1] >> 4) & 0x0F);
    // int y = ((data[i + 1] & 0x0F) << 8) | (data[i + 2] & 0xFF);
    // int width = ((data[i + 3] & 0xFF) << 4) | ((data[i + 4] >> 4) & 0x0F);
    // int height = ((data[i + 4] & 0x0F) << 8) | (data[i + 5] & 0xFF);
    // // Read color as a 24-bit integer
    // int color = ((data[i + 6] & 0xFF) << 16) | ((data[i + 7] & 0xFF) << 8) |
    // (data[i + 8] & 0xFF);
    // leafs.add(new simpleNode(x, y, width, height, color));
    // }
    // return leafs;
    // }
    /**
     * Decodes a Base64-encoded string into an ArrayList of simpleNodes.
     * Each simpleNode represents a rectangular region of the image with a single
     * color.
     * The encoded string contains information about the x and y coordinates, width,
     * height, and color of each region.
     * The x, y, width, and height values are read as 10-bit integers, while the
     * color value is read as a 24-bit integer.
     * The decoded simpleNodes are added to an ArrayList and returned.
     *
     * @param encoded the Base64-encoded string to decode
     * @return an ArrayList of simpleNodes representing the image regions
     * @throws IOException if there is an error decoding the string
     */
    public static ArrayList<simpleNode> decode(String encoded) throws IOException {
        ArrayList<simpleNode> leafs = new ArrayList<>();
        byte[] data = Base64.getDecoder().decode(encoded);
        for (int i = 0; i + 7 <= data.length; i += 8) {
            // Read x, y, width, and height as 10-bit integers
            int x = ((data[i] & 0xFF) << 2) | ((data[i + 1] >> 6) & 0x03);
            int y = ((data[i + 1] & 0x3F) << 4) | ((data[i + 2] >> 4) & 0x0F);
            int width = ((data[i + 2] & 0x0F) << 6) | ((data[i + 3] >> 2) & 0x3F);
            int height = ((data[i + 3] & 0x03) << 8) | (data[i + 4] & 0xFF);
            // Read color as a 24-bit integer
            int color = ((data[i + 5] & 0xFF) << 16) | ((data[i + 6] & 0xFF) << 8) | (data[i + 7] & 0xFF);
            leafs.add(new simpleNode(x, y, width, height, color));
        }
        return leafs;
    }

    /**
     * Compresses an image represented by a 3D array of pixels using a quad tree and
     * writes the compressed data to a file.
     * 
     * @param pixels         the 3D array of pixels representing the image
     * @param outputFileName the name of the file to write the compressed data to
     * @throws IOException if there is an error writing to the output file
     */
    public void Compress(int[][][] pixels, String outputFileName) throws IOException {
        // create the quad tree with the pixels, maxDepth, and similarityPercent
        QuadTree quadtree = new QuadTree(pixels, maxDepth, percentage);

        // By calling the mergeSimilarNodes method multiple times on the root node, the
        // loop is effectively merging similar nodes at different levels of the
        // quadtree. This can help to further reduce the number of nodes in the quadtree
        // and simplify the structure of the tree.
        //seems to reduce the size of the compressed file by a bit
        Node rootNode = quadtree.getRoot();
        for (int i = 0; i < 10; i++) {
            quadtree.mergeSimilarNodes(rootNode);
        }

        // For default quad tree with just the pixels, uncomment the line below
        // QuadTree quadtree = new QuadTree(pixels);

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outputFileName))) {
            oos.writeObject(encode(quadtree.getLeafs()));
        } catch (IOException e) {
            throw new IOException("Error writing to the output file");
        }
    }

    /**
     * This method reads a compressed image file and returns the decompressed image
     * as a 3D integer array.
     * The input file must be in the form of a serialized String object containing
     * the compressed image data.
     * The method first decodes the compressed data into an ArrayList of simpleNode
     * objects, which represent the
     * rectangular regions of the image with the same color. It then calculates the
     * width and height of the image
     * from the leaf nodes, and converts the leaf nodes into a 3D integer array
     * representing the decompressed image.
     * 
     * @param inputFileName the name of the input file containing the compressed
     *                      image data
     * @return a 3D integer array representing the decompressed image
     * @throws IOException            if there is an error reading the input file or
     *                                if the input file contains invalid data
     * @throws ClassNotFoundException if the class of the serialized object cannot
     *                                be found
     */
    public int[][][] Decompress(String inputFileName) throws IOException, ClassNotFoundException {

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(inputFileName))) {
            Object object = ois.readObject();

            if (object instanceof String) {
                String csv = (String) object;
                ArrayList<simpleNode> leafs = decode(csv);

                // calculate height and width of image from leafs
                int width = 0;
                int height = 0;
                for (simpleNode leaf : leafs) {
                    if (leaf.x + leaf.width > width) {
                        width = leaf.x + leaf.width;
                    }
                    if (leaf.y + leaf.height > height) {
                        height = leaf.y + leaf.height;
                    }
                }

                // convert leafs into int[][][] image
                int[][][] image = new int[width][height][3];
                for (simpleNode leaf : leafs) {
                    for (int i = leaf.x; i < leaf.x + leaf.width; i++) {
                        for (int j = leaf.y; j < leaf.y + leaf.height; j++) {
                            image[i][j][0] = (leaf.color >> 16) & 0xff;
                            image[i][j][1] = (leaf.color >> 8) & 0xff;
                            image[i][j][2] = leaf.color & 0xff;
                        }
                    }
                }
                return image;
            } else {
                throw new IOException("Invalid object type in the input file");
            }
        }
    }
}
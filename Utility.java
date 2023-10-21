import java.io.*;
import java.util.*;

/**
 * The Utility class contains a QuadTree class that builds a quad tree from an
 * array of pixels.
 * The QuadTree class divides the image into smaller nodes, each with a color
 * that is the average
 * color of the pixels it covers. The class also includes methods to encode and
 * decode the leaf
 * nodes of the quad tree into a string format for storage or transmission.
 */
public class Utility {

    /**
     * This class represents a node in a quadtree used for image compression. It
     * contains the coordinates of the top left corner of the node, the dimensions
     * of the node, the average color of the node, and an array of the four children
     * of the node. It also has methods to check if the node is a leaf node and to
     * set the color of the node to the average color of the pixels it covers.
     */
    private static class Node {
        // x and y are the coordinates of the top left corner of the node
        int x;
        int y;
        // width and height are the dimensions of the node
        int width;
        int height;
        // color is the average color of the node
        int color;
        // children is an array of the four children of the node
        Node[] children;

        public Node(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        // checks if the node is a leaf node
        public boolean isLeaf() {
            return children == null;
        }

        // setColor sets the color of the node to the average color of the pixels it
        // covers
        public void setColor(int[][][] pixels) {
            int r = 0;
            int g = 0;
            int b = 0;
            int count = 0;
            for (int i = x; i < x + width; i++) {
                for (int j = y; j < y + height; j++) {
                    r += (pixels[i][j][0] & 0xff);
                    g += (pixels[i][j][1] & 0xff);
                    b += (pixels[i][j][2] & 0xff);
                    count++;
                }
            }
            if (count == 0) {
                return;
            }
            r /= count;
            g /= count;
            b /= count;
            color = (r << 16) | (g << 8) | b;
        }
    }

    /**
     * A utility class that contains a static nested class for creating simple
     * nodes.
     */
    public static class simpleNode {
        int x;
        int y;
        int width;
        int height;
        int color;

        /**
         * Constructs a simple node with the specified x and y coordinates, width,
         * height, and color.
         * 
         * @param x      the x coordinate of the node
         * @param y      the y coordinate of the node
         * @param width  the width of the node
         * @param height the height of the node
         * @param color  the color of the node
         */
        public simpleNode(int x, int y, int width, int height, int color) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.color = color;
        }
    }

    /**
     * Encodes the given list of simpleNodes into a string representation.
     * Each simpleNode is represented as a comma-separated string of its x, y,
     * width, height, and color values,
     * with each value converted to a hexadecimal string.
     * The string representation of each simpleNode is separated by a newline
     * character.
     *
     * @param leafs the list of simpleNodes to encode
     * @return the string representation of the encoded simpleNodes
     */
    public String encode(ArrayList<simpleNode> leafs) {
        StringBuilder sb = new StringBuilder();
        for (simpleNode leaf : leafs) {
            sb.append(Integer.toHexString(leaf.x)).append(",").append(Integer.toHexString(leaf.y)).append(",")
                    .append(Integer.toHexString(leaf.width)).append(",")
                    .append(Integer.toHexString(leaf.height)).append(",").append(Integer.toHexString(leaf.color))
                    .append("\n");
        }
        return sb.toString();
    }

    /**
     * Decodes a CSV string into an ArrayList of simpleNode objects.
     * Each line of the CSV string represents a simpleNode object, with
     * comma-separated values
     * for x, y, width, height, and color in hexadecimal format.
     * 
     * @param csv the CSV string to decode
     * @return an ArrayList of simpleNode objects
     */
    public ArrayList<simpleNode> decode(String csv) {
        ArrayList<simpleNode> leafs = new ArrayList<>();
        Scanner scanner = new Scanner(csv);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] values = line.split(",");
            int x = Integer.parseInt(values[0], 16);
            int y = Integer.parseInt(values[1], 16);
            int width = Integer.parseInt(values[2], 16);
            int height = Integer.parseInt(values[3], 16);
            int color = Integer.parseInt(values[4], 16);
            simpleNode leaf = new simpleNode(x, y, width, height, color);
            leafs.add(leaf);
        }
        scanner.close();
        return leafs;
    }

    /**
     * This class represents a QuadTree data structure that can be built from a 3D
     * array of pixels.
     * The QuadTree is built recursively and can be divided into four children.
     * The maximum depth of the tree can be set and the tree will not go deeper than
     * this.
     * The tree can be built with a default max depth of 200 (probably lossless).
     * The tree can also be built with a percentage value that determines if a node
     * is a leaf or not.
     * If the color of the pixel is not within PERCENTAGE% of the average color of
     * the node, then the node is not a leaf.
     * The class provides methods to get the root of the tree and to get the leaf
     * nodes of the tree.
     */
    public class QuadTree {

        // the maximum depth of the quad tree, the tree will not go deeper than this,
        // edit this to change the depth
        private static int MAX_DEPTH = 200;
        private static int PERCENTAGE = 0;
        private Node root;

        // builds the quad tree from the pixels, maxDepth is the maximum depth of the
        // tree
        private QuadTree(int[][][] pixels, int maxDepth, int percentage) {
            MAX_DEPTH = maxDepth;
            PERCENTAGE = percentage;
            root = buildQuadTree(pixels, 0, 0, pixels.length, pixels[0].length, 0);
        }

        // builds the quad tree from the pixels, with a default max depth of 200
        // (probably lossless)
        private QuadTree(int[][][] pixels) {

            root = buildQuadTree(pixels, 0, 0, pixels.length, pixels[0].length, 0);
        }

        // builds the quad tree recursively
        private Node buildQuadTree(int[][][] pixels, int x, int y, int width, int height, int depth) {
            Node node = new Node(x, y, width, height);
            node.setColor(pixels);
            if (depth < MAX_DEPTH) {
                boolean isLeaf = true;

                for (int i = x; i < x + width; i++) {
                    for (int j = y; j < y + height; j++) {
                        // if the color of the pixel is not within PERCENTAGE% of the average color of
                        // the node, then the node is not a leaf
                        if (Math.abs((pixels[i][j][0] & 0xff) - ((node.color >> 16) & 0xff)) > ((node.color >> 16)
                                & 0xff) * PERCENTAGE / 100) {
                            isLeaf = false;
                            break;
                        }
                    }
                    if (!isLeaf) {
                        break;
                    }
                }
                if (!isLeaf) {
                    divide(node, pixels, depth + 1);
                }
            }
            return node;
        }

        // divides the node into four children
        public void divide(Node node, int[][][] pixels, int depth) {
            node.children = new Node[4];
            int halfWidth = node.width / 2;
            int halfHeight = node.height / 2;
            int remainderWidth = node.width - halfWidth;
            int remainderHeight = node.height - halfHeight;
            // For odd numbered sizes, the first child will be one pixel larger than the
            // others
            node.children[0] = buildQuadTree(pixels, node.x, node.y, halfWidth, halfHeight, depth);
            node.children[1] = buildQuadTree(pixels, node.x + halfWidth, node.y, remainderWidth, halfHeight, depth);
            node.children[2] = buildQuadTree(pixels, node.x, node.y + halfHeight, halfWidth, remainderHeight, depth);
            node.children[3] = buildQuadTree(pixels, node.x + halfWidth, node.y + halfHeight, remainderWidth,
                    remainderHeight, depth);
        }

        public Node getRoot() {
            return root;
        }

        public ArrayList<simpleNode> getLeafs() {
            return convertLeafs(findLeafs(root, new ArrayList<Node>()));
        }

        public ArrayList<Node> findLeafs(Node node, ArrayList<Node> leafs) {
            if (node.isLeaf()) {
                leafs.add(node);
            } else {
                for (Node child : node.children) {
                    findLeafs(child, leafs);
                }
            }
            return leafs;
        }

        // converts arraylist of nodes to arrayList of simplenodes
        public ArrayList<simpleNode> convertLeafs(ArrayList<Node> leafs) {
            ArrayList<simpleNode> simpleLeafs = new ArrayList<simpleNode>();
            for (Node leaf : leafs) {
                simpleLeafs.add(new simpleNode(leaf.x, leaf.y, leaf.width, leaf.height, leaf.color));
            }
            return simpleLeafs;
        }

        public void mergeSimilarNodes(Node node) {
            if (node.isLeaf()) {
                return;
            }

            mergeSimilarNodes(node.children[0]);
            mergeSimilarNodes(node.children[1]);
            mergeSimilarNodes(node.children[2]);
            mergeSimilarNodes(node.children[3]);

            if (!node.children[0].isLeaf() || !node.children[1].isLeaf() || !node.children[2].isLeaf()
                    || !node.children[3].isLeaf()) {
                return;
            }

            int color0 = node.children[0].color;
            int color1 = node.children[1].color;
            int color2 = node.children[2].color;
            int color3 = node.children[3].color;

            // if the color of the child nodes are within PERCENTAGE% of the average color of
            // the node, then the node is a leaf
            if (Math.abs(((color0 >> 16) & 0xff) - ((node.color >> 16) & 0xff)) <= ((node.color >> 16) & 0xff)
                    * PERCENTAGE / 100
                    && Math.abs(((color1 >> 16) & 0xff) - ((node.color >> 16) & 0xff)) <= ((node.color >> 16) & 0xff)
                            * PERCENTAGE / 100
                    && Math.abs(((color2 >> 16) & 0xff) - ((node.color >> 16) & 0xff)) <= ((node.color >> 16) & 0xff)
                            * PERCENTAGE / 100
                    && Math.abs(((color3 >> 16) & 0xff) - ((node.color >> 16) & 0xff)) <= ((node.color >> 16) & 0xff)
                            * PERCENTAGE / 100
                    && Math.abs(((color0 >> 8) & 0xff) - ((node.color >> 8) & 0xff)) <= ((node.color >> 8) & 0xff)
                            * PERCENTAGE / 100
                    && Math.abs(((color1 >> 8) & 0xff) - ((node.color >> 8) & 0xff)) <= ((node.color >> 8) & 0xff)
                            * PERCENTAGE / 100
                    && Math.abs(((color2 >> 8) & 0xff) - ((node.color >> 8) & 0xff)) <= ((node.color >> 8) & 0xff)
                            * PERCENTAGE / 100
                    && Math.abs(((color3 >> 8) & 0xff) - ((node.color >> 8) & 0xff)) <= ((node.color >> 8) & 0xff)
                            * PERCENTAGE / 100
                    && Math.abs((color0 & 0xff) - (node.color & 0xff)) <= ((node.color & 0xff) * PERCENTAGE / 100)
                    && Math.abs((color1 & 0xff) - (node.color & 0xff)) <= ((node.color & 0xff) * PERCENTAGE / 100)
                    && Math.abs((color2 & 0xff) - (node.color & 0xff)) <= ((node.color & 0xff) * PERCENTAGE / 100)
                    && Math.abs((color3 & 0xff) - (node.color & 0xff)) <= ((node.color & 0xff) * PERCENTAGE / 100)) {
                node.children = null;
                    }
        }
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
        // maxDepth is the maximum depth of the quad tree, edit this to change the depth
        int maxDepth = 9;
        // similarityPercent is the percentage of the average color of the node that the
        // color of the pixel must be within to be considered part of the node, edit
        // this to change the percentage
        int similarityPercent = 5;

        // create the quad tree with the pixels, maxDepth, and similarityPercent
        QuadTree quadtree = new QuadTree(pixels, maxDepth, similarityPercent);
        Node rootNode = quadtree.getRoot();
        for(int i = 0; i < 10; i++) {
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

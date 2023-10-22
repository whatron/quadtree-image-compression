package src;

import java.util.*;

public class QuadTree {

        // the maximum depth of the quad tree, the tree will not go deeper than this,
        // edit this to change the depth
        private static int MAX_DEPTH = 200;
        private static int PERCENTAGE = 0;
        private Node root;

        // builds the quad tree from the pixels, maxDepth is the maximum depth of the
        // tree
        public QuadTree(int[][][] pixels, int maxDepth, int percentage) {
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

            // if the color of the child nodes are within PERCENTAGE% of the average color
            // of
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
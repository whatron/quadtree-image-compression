package src;
public class Node extends simpleNode {
    // children is an array of the four children of the node
    Node[] children;

    public Node(int x, int y, int width, int height) {
        super(x, y, width, height, 0);
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

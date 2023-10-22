package src;
public class simpleNode {
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
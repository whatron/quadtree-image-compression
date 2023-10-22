# Quadtree Image Compression CLI
This is a command line interface (CLI) tool for compressing images using quadtree compression. The tool is written in Java and can be run from the command line.

### Installation
To use this tool, you need to have Java installed on your system. You can download the latest version of Java from the official website.

Once you have Java installed, you can clone this repository and start running the code.

### Usage
To compress your images using quadtree compression, place your images in the Original folder.

Then, run the following file:`App.java`

This will compress the images in the Original folder using quadtree compression and save the compressed image in the Compressed Folder.
The compressed files are then decompressed into the Decompressed Folder.
The compression and decompression time, file size, and loss percentage will be displayed in the console.

You can also specify the maximum depth of the quadtree and percentage node color variance in `Utility.java`.

### Room for Improvement
1. Store similar colors as an average of the same color
2. Implement Huffman Coding
3. Split encoding for nodes with smaller variables to save few bytes per node.

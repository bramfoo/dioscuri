package nl.kbna.dioscuri;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;


/**
 * Class StartupCanvas draws a buffered image on a canvas.
 * 
 */
public class StartupCanvas extends Canvas
{
    // Attributes
    private BufferedImage image = null;
    private boolean paint = false;

    
    // Constructor
    public StartupCanvas()
    {
    }

    
    // Methods
    /**
     * Clear the image on canvas
     * 
     */
    public void clearImage()
    {
        // Clear Image Area
        paint = false;
        this.repaint();
    }

    /**
     * Draw an image on the canvas
     * This is a standard method used by Graphics
     * 
     * @param Graphics g - standard graphics component
     * 
     */
    public void paint(Graphics g)
    {
        // Paint image on canvas
        if (paint)
        {
            g.drawImage(image, 0, 0, this);
        }
        else
        {
            // Paint canvas black
            g.setColor(Color.blue);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
    

    /**
     * Update the canvas
     * This method is called automatically when repaint() is called.
     * It is necesarry to doublebuffer the canvas.
     * 
     * @param Graphics g - the standard graphics component
     */
    public void update(Graphics g)
    {
         paint(g);
    } 
    

    /**
     * Set the given image to current and redraw canvas
     * 
     * @param Image i
     */
    public void setImage(BufferedImage i)
    {
        // Paint image object
        paint = true;
        image = i;
        this.repaint();
    }
}
package nl.kbna.dioscuri;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;


/**
 * Class StartupCanvas draws a buffered image on a canvas.
 * 
 */
public class StartupPanel extends JPanel
{
    // Attributes
    private BufferedImage image = null;
    private boolean paint = false;

    
    // Constructor
    public StartupPanel()
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
     * Draw an image on the panel
     * This is a standard method used by Graphics
     * 
     * @param Graphics g - standard graphics component
     * 
     */
    public void paintComponent(Graphics g)
    {
        // Paint image on panel
        if (paint)
        {
            g.drawImage(image, 0, 0, this);
        }
        else
        {
            // Paint panel black
            g.setColor(Color.white);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
    

    /**
     * Update the panel
     * This method is called automatically when repaint() is called.
     * It is necesarry to doublebuffer the panel (to prevent it from blinking when updated).
     * 
     * @param Graphics g - the standard graphics component
     */
    public void update(Graphics g)
    {
         paint(g);
    } 
    

    /**
     * Set the given image to current and redraw panel
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
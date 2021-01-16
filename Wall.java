/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package project_lag;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author QDS
 */
public class Wall {
    int x;
    int y;
    int length;
    int height;
    Rectangle2D box;
    
    public Wall(int x, int y, int length, int height)
    {
        this.x = x;
        this.y = y;
        this.length = length;
        this.height = height;
        
        box = new Rectangle(x,y,length,height);
    }
    public void paint(Graphics g)
    {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.lightGray);
        g2d.fill(box);
    }
}

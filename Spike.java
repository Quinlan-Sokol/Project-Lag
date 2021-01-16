/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package project_lag;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;

/**
 *
 * @author QDS
 */
public class Spike {
    int x;
    int y;
    int base;
    int height;
    Polygon triangle;
    
    public Spike(int x, int y, int base, int height)
    {
        this.x = x;
        this.y = y;
        this.base = base;
        this.height = height;
        triangle.addPoint(x, y);
        triangle.addPoint(x + base, y);
        triangle.addPoint(x + Math.round(base / 2), y - height);
    }
    public void paint(Graphics g)
    {
        g.setColor(Color.red);
        g.fillPolygon(triangle);
    }
}

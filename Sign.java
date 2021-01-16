/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package project_lag;


import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author QDS
 */
public class Sign {
    String message;
    int x;
    int y;
    boolean showMessage = false;
    Rectangle2D box;
    public Sign(int x, int y, String message)
    {
        this.x = x;
        this.y = y;
        this.message = message;
        box = new Rectangle(x-5,y-10,15,18);
    }
    public boolean DistanceToPlayer(int pX, int pY)
    {
        Point p = new Point(pX, pY);
        int dis = Math.toIntExact(Math.round(p.distance(x, y)));
        return dis <= 30;
    }
    public void paint(Graphics g)
    {
        g.setColor(new Color(184, 134, 11));
        g.fillRect(x, y, 4, 8);//post
        g.fillRect(x-5, y-10, 15, 10);//board
        g.setColor(Color.black);
        g.drawRect(x, y, 4, 8);//post 
        g.drawRect(x-5, y-10, 15, 10);//board
        g.drawLine(x-4, y-7, x+8, y-7);
        g.drawLine(x-4, y-4, x+8, y-4);
        
        if(showMessage){
            g.setColor(Color.yellow);
            g.fillRect(x-35, y-66, 80, 50);
            g.setColor(Color.black);
            g.drawRect(x-35, y-66, 80, 50);
            
            g.setFont(new Font("Verdana",Font.PLAIN,8));
            g.setColor(new Color(148,0,211));
            int lineY = y - 57;
            for(String str : splitMessage(message))
            {
                g.drawString(str, x - 33, lineY);
                lineY += 8;
            }
            
        }
    }
    public ArrayList<String> splitMessage(String message)//16 chars max per line
    {
        ArrayList<String> res = new ArrayList<>();

        Pattern p = Pattern.compile("\\b.{1," + (16-1) + "}\\b\\W?");
        Matcher m = p.matcher(message);
        
	while(m.find()) {
            res.add(m.group());
        }
        return res;
    }
}

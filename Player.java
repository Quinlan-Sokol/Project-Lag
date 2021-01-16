/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package project_lag;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

/**
 *
 * @author QDS
 */
public class Player {
    int posX;
    int posY;
    int prevX;
    int prevY;
    int speedX;
    int speedY;
    int maxFallSpeed;
    int gravity;
    int ground;
    int slideLaunch;
    int wallJumpCounter;
    int radius;
    int particleCounter;
    int particleCounter2;
    int jumpX;
    int jumpY;
    int jumpParticleD;
    boolean inAir;
    boolean wallJump;
    boolean canJump;
    boolean wallSlide;
    boolean playJumpSound;
    boolean createWallJumpParticles;
    boolean createJumpParticles;
    boolean crouch;
    boolean reset;
    Rectangle2D hitBox;
    Image image;
    Image imageCrouch;
    ArrayList<Point> wallJumpParticles;
    ArrayList<Point> jumpParticles;
    
    public Player(int x, int y)
    {
        posX = x;
        posY = y;
        speedX = 0;
        speedY = 0;
        wallSlide = false;
        canJump = false;
        playJumpSound = false;
        gravity = 1;
        ground = 300;
        slideLaunch = 0;
        wallJumpCounter = 0;
        wallJump = true;
        hitBox = new Rectangle(x, y, 10, 10);
        maxFallSpeed = 10;
        image = null;
        imageCrouch = null;
        wallJumpParticles = new ArrayList();
        jumpParticles = new ArrayList();
        radius = 1;
        createWallJumpParticles = false;
        createJumpParticles = false;
        particleCounter = 20;
        particleCounter2 = 40;
        inAir = false;
        jumpParticleD = 0;
        crouch = false;
        reset = false;
    }
    public void jump()
    {
        if(canJump){
            speedY = -11;
            canJump = false;
            playJumpSound = true;
            jumpX = posX;
            jumpY = posY;
            if(wallSlide){
                //speedX = slideLaunch;
                wallSlide = false;
                wallJump = true;
                createWallJumpParticles = true;
                
            }else{
                createJumpParticles = true;
            }
        }
    }
    public void moveLeft()
    {
        speedX = -4;
    }
    public void moveRight()
    {
        speedX = 4;
    }
    public void update()
    {
        prevX = posX;
        prevY = posY;
        
        if(!wallJump){//normal jump
            posX += speedX;
            posY += speedY;
        }
        else{//jumping off a wall
            if(wallJumpCounter < 5 && !canJump){
                posX += slideLaunch;
                posY += speedY;
                wallJumpCounter++;
            }
            else{
                wallJumpCounter = 0;
                wallJump = false;
            }
        }
        
        hitBox.setRect(posX, posY, 10, 10);//reseting hitbox
        if(canJump){
            wallSlide = false;
        }

        if(speedY > maxFallSpeed){
            speedY = 10;
        }
        speedY += gravity;
        
        if(posY > ground - 10)
        {
            posY = ground - 10;
            canJump = true;
            speedY = 0;
        }
        
        //when the player dies
        if(reset){
            posX = 50;
            posY = 290;
            reset = false;
        }
    }
    public void updateHitBox()
    {
        if(!crouch){
            hitBox = new Rectangle(posX, posY, 10, 10);
        }else{
            hitBox = new Rectangle(posX, posY + 5, 10, 5);
        }
    }
    public void checkCollision(Platform plat)
    {
        updateHitBox();
        if(hitBox.intersects(plat.box)){
            if(prevX + 10 <= plat.x)
            {//from left
                canJump = true;
                speedY = 2;
                wallSlide = true;
                slideLaunch = -5;
                posX = plat.x - 10;
            }
            else if(prevX >= plat.x + plat.length)
            {//from right
                canJump = true;
                speedY = 2;
                wallSlide = true;
                slideLaunch = 5;
                posX = plat.x + plat.length;
            }
            else if(prevY <= plat.y)
            {//from top
                canJump = true;
                posY = plat.y - 9;
                speedY = 0;
            }
            else if(prevY >= plat.y )
            {//from bottom
                posY = plat.y + plat.height;
                speedY = 0;
            }
        }
    }
    public void checkCollision(Wall wall)
    {
        updateHitBox();
        if(hitBox.intersects(wall.box))
        {
            if(prevX + 10 <= wall.x)
            {//from left
                posX = wall.x - 10;
            }
            else if(prevX >= wall.x + wall.length)
            {//from right
                posX = wall.x + wall.length;
            }
            else if(prevY <= wall.y)
            {//from top
                canJump = true;
                posY = wall.y - 9;
                speedY = 0;
                inAir = false;
            }
            else if(prevY >= wall.y )
            {//from bottom
                posY = wall.y + wall.height + 1;
                speedY = 0;
            }
        }
    }
    
    public void paint(Graphics g, int alpha)
    {
        g.setColor(new Color(Color.red.getRed(),Color.red.getGreen(),Color.red.getBlue(),alpha));
        g.fillRect(posX, posY-1, 10, 10);
        g.setColor(new Color(Color.black.getRed(),Color.black.getGreen(),Color.black.getBlue(),alpha));
        g.drawRect(posX, posY-1, 10, 10);
        
    }
    public void paint(Graphics g)
    {
        //Drawing the player
        if(!crouch){
            g.drawImage(image, posX, posY, null);
        }else{
            g.drawImage(imageCrouch, posX, posY+5, null);
        }
        
        if(createWallJumpParticles){
            if(particleCounter == 20){
                if(radius <= 8){
                    wallJumpParticles.clear();
                    if(slideLaunch < 0){//left
                        getPointsOnCircle(jumpX + 10, jumpY + 10,radius);
                    }else{//right
                        getPointsOnCircle(jumpX, jumpY + 10,radius);
                    }
                    radius++;
                }
                else{
                    radius = 1;
                    createWallJumpParticles = false;
                    wallJumpParticles.clear();
                }
                particleCounter = 0;
            }
            else{
                particleCounter++;
            }
            for(Point p : wallJumpParticles){
                g.setColor(new Color(0,170,235));
                g.drawRect(p.x, p.y, 1, 1);
            }
        }
        if(createJumpParticles){
            if(particleCounter2 == 40){
                if(jumpParticleD <= 8){
                    jumpParticles.clear();
                    setJumpParticles(jumpX + 5, jumpY + 10, jumpParticleD);
                    jumpParticleD++;
                }
                else{
                    jumpParticleD = 0;
                    createJumpParticles = false;
                    jumpParticles.clear();
                }
                particleCounter2 = 0;
            }
            else{
                particleCounter2++;
            }
            for(Point p : jumpParticles){
                g.setColor(Color.black);
                g.drawRect(p.x, p.y, 1, 1);
            }
        }
    }
    public void getPointsOnCircle(int cx, int cy, int r)
    {
        int a = 0;
        
        while(a < 360){
            float x = (float) (cx + r * Math.cos(Math.toRadians(a)));
            float y = (float) (cy + r * Math.sin(Math.toRadians(a)));
            wallJumpParticles.add(new Point(Math.round(x), Math.round(y)));
            a += 35;
        }
    }
    public void setJumpParticles(int cx, int cy, int d)
    {
        float c = 10F;//the 'a' value in equation
        int f = 0;//offset for particles
        for(int i = 0; i < 3; i++){ //right side
            int x = d + f;
            float y = x - c;
            y *= x;
            y *= -1;
            f++;
            c--;
            jumpParticles.add(new Point(cx + x,Math.round(cy - y)));
        }
        c = 10F;
        f = 0;
        for(int i = 0; i < 3; i++){ //left side
            int x = d - f;
            float y = x - c;
            y *= x;
            y *= -1;
            f--;
            c--;
            jumpParticles.add(new Point(cx - x,Math.round(cy - y)));
        }
    }
}

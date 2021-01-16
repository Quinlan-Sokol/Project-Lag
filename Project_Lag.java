/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package project_lag;

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.Timer;

/**
 *
 * @author QDS
 */
public class Project_Lag extends Applet implements KeyListener, ActionListener, MouseListener {

    Timer timer;
    Image offscreen;
    Graphics offg;
    int mouseX, mouseY;
    int letterOffset, letterDelay;
    int alpha, alphaCounter;
    int animationDelay;
    int prevSize;
    ArrayList<Platform> platforms;
    ArrayList<Platform> animationPlatforms;
    ArrayList<Sign> signs;
    ArrayList<Wall> walls;
    ArrayList<String> controlBuffer;//creates "lag" effect
    ArrayList<String> controlOverflow;
    ArrayList<File> playerImages;
    
    ArrayList<Rectangle> characterGrid;
    ArrayList<Color> gridColors;
    Color selectedColor;
    BufferedImage playerImage;
    BufferedImage img;
    
    Player p;
    Player player;
    
    AudioClip jump;

    int level;
    boolean startLevel;
    boolean goingForward;

    boolean jumped;
    boolean inMenu, inAnimation, inLevelDesigner, inCreatedLevels, inCharacterEditor;
    boolean upKey, downKey, leftKey, rightKey, escapeKey;
    boolean hasClicked;
    boolean select;
    boolean placingSign;
    boolean deleteItem;
    boolean saving;
    String signMessage;
    String saveMessage;
    int dWidth, dHeight;
    Point mousePos;
    Point signPos;
    String itemType;
    int itemLoc;
    int imageIndex;
    
    boolean d1;
    int itemIndex;

    public void init() {
        //size of window
        this.setSize(600, 350);
        //creates a duplicate of window for double buffering
        offscreen = createImage(this.getWidth(), this.getHeight());
        //double buffering
        offg = offscreen.getGraphics();
        this.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        
        inMenu = true;
        inAnimation = true;
        inLevelDesigner = false;
        inCreatedLevels = false;
        inCharacterEditor = false;
        d1 = true;
        itemIndex = 1;
        upKey = false;
        downKey = false;
        leftKey = false;
        rightKey = false;
        escapeKey = false;
        hasClicked = false;
        select = false;
        placingSign = false;
        deleteItem = false;
        saving  = false;
        signPos = new Point();
        dWidth = 40;
        dHeight = 8;
        itemType = "";
        signMessage = "";
        saveMessage = "";
        itemLoc = 0;
        imageIndex = 0;
        
        jumped = false;
        letterOffset = 0;
        letterDelay = 0;
        alpha = 255;
        alphaCounter = 0;
        animationDelay = 0;
        prevSize = 0;

        timer = new Timer(30, this);

        p = new Player(15, 50);
        player = new Player(50, 290);
        
        jump = getAudioClip(getCodeBase(),"Sound/jump.wav");

        level = 1;
        startLevel = true;
        goingForward = true;

        platforms = new ArrayList();

        animationPlatforms = new ArrayList();
        animationPlatforms.add(new Platform(20, 50, 65, 10));
        animationPlatforms.add(new Platform(100, 100, 65, 10));
        animationPlatforms.add(new Platform(180, 150, 65, 10));

        walls = new ArrayList();
        signs = new ArrayList();
        
        playerImages = new ArrayList();

        controlBuffer = new ArrayList();
        controlOverflow = new ArrayList();
        
        characterGrid = new ArrayList();
        gridColors = new ArrayList();
        int gridX = 0;
        int gridY = 1;
        for(int i = 0; i < 100; i++){
            characterGrid.add(new Rectangle(gridX, gridY, 35, 35));
            gridColors.add(Color.white);
            gridX += 35;//moves one column over
            
            if(gridX == 350){//has reached end
                gridX = 0;//goes back to the left side
                gridY += 35;//moves one row down
            }
        }
        selectedColor = Color.black;
        playerImage = null;
        img = null;

        addKeyListener(this);
        this.addMouseListener(this);
        setFocusable(true);
        requestFocusInWindow();
    }

    public void start() {
        timer.start();
    }

    public void stop() {
        timer.stop();
    }

    public void actionPerformed(ActionEvent e) {

        if (!inMenu && !inLevelDesigner) {
            if(!inCreatedLevels){
                if (player.posX >= 590) {//goes forward a level
                    level++;
                    startLevel = true;
                    goingForward = true;
                }
                if (player.posX <= 0 && level > 1) {//goes back a level
                    level--;
                    startLevel = true;
                    goingForward = false;
                }
            }
            if (startLevel) {
                loadLevel(Integer.toString(level), false, goingForward);
                startLevel = false;
            }
            if(img != null){
                player.image = img;
                player.imageCrouch = img.getSubimage(0, 5, 10, 5);
            }
            player.update();
            playSounds();

            //COLLISIONS
            if (player.posX < 0) {//left side
                player.posX = 0;
            }
            if (player.posX + 10 > 600) {
                //END LEVEL
            }
            
            player.inAir = true; //must come before collisions
            for (Platform plat : platforms) {
                player.checkCollision(plat);
            }
            for (Wall wall : walls) {
                player.checkCollision(wall);
            }

            //sign distance check
            for (Sign sign : signs) {
                if (sign.DistanceToPlayer(player.posX, player.posY)) {
                    sign.showMessage = true;
                } else {
                    sign.showMessage = false;
                }
            }
            
            if(!leftKey && !rightKey && !upKey){
                controlBuffer.add("stop");
            }
            if(downKey){
                controlBuffer.add("crouch");
            }else{
                player.crouch = false;
            }
            //lag effect
            if (prevSize == controlBuffer.size()) {
                controlBuffer.add("none");
            }
            while (controlBuffer.size() > 7) {//7
                String command = controlBuffer.get(0);
                controlBuffer.remove(0);
                switch (command) {
                    case "none":
                        break;
                    case "jump":
                        player.jump();
                        break;
                    case "left":
                        player.speedX = -4;
                        break;
                    case "right":
                        player.speedX = 4;
                        break;
                    case "crouch":
                        player.crouch = true;
                        break;
                    case "stop":
                        player.speedX = 0;
                        break;
                }
            }
            prevSize = controlBuffer.size();
        }
        else if(inLevelDesigner){
            if(upKey){
                dHeight--;
            }
            if(downKey){
                dHeight++;
            }
            if(leftKey){
                dWidth--;
            }
            if(rightKey){
                dWidth++;
            }
            
            //check values
            if(dHeight <= 0){
                dHeight = 1;
            }
            if(dWidth <= 0){
                dWidth = 1;
            }
            mousePos = this.getMousePosition();
            
            //placing items
            if(hasClicked && !placingSign){//has left clicked
                switch(itemIndex){
                    case 0:
                        select = true;
                        if(hasClickedItem()){//has clicked on an item
                            itemType = getClickedItemType();
                            itemLoc = getClickedItemIndex();
                        }else{
                            itemType = "";
                        }
                        break;
                    case 1:
                        if(!checkPlatformCollision(new Platform(mousePos.x, mousePos.y, dWidth, dHeight)))
                            platforms.add(new Platform(mousePos.x, mousePos.y, dWidth, dHeight));
                        break;
                    case 2:
                        if(!checkWallCollision(new Wall(mousePos.x, mousePos.y, dWidth, dHeight)))
                            walls.add(new Wall(mousePos.x, mousePos.y, dWidth, dHeight));
                        break;
                    case 3:
                        if(!checkSignCollision(new Sign(mousePos.x, mousePos.y, ""))){
                            placingSign = true;
                            signPos.x = mousePos.x;
                            signPos.y = mousePos.y;
                        }
                        break;
                }
                hasClicked = false;
            }
            if(select){//opens item viewing
                select = false;
            }
        }
        else {//starting screens
            if (inAnimation) {//opening animation
                p.update();

                for (Platform plat : animationPlatforms) {
                    p.checkCollision(plat);
                }
            }
            else{//menu
                
            }
        }
        
        if(escapeKey){
            if(inLevelDesigner || inCreatedLevels || inCharacterEditor){
                inLevelDesigner = false; 
                inCreatedLevels = false; 
                inCharacterEditor = false;
                inMenu = true;
            }
            escapeKey = false;
        }
    }

    public void paint(Graphics g) {
        g.clearRect(0, 0, 600, 400);
        offg.setColor(Color.white);
        offg.fillRect(0, 0, 600, 350);
        if (inMenu) {
            if (inAnimation) {
                if (!jumped) {
                    /**
                     * ***STEPS****
                     */
                    if (p.posX < 298) {
                        p.moveRight();
                    } else {
                        p.speedX = 0;
                        jumped = true;
                    }
                    if (p.posX == 211) {
                        p.jump();
                    }
                } else {
                    //moving platforms
                    if (animationPlatforms.get(2).y > -20) {
                        animationPlatforms.set(0, new Platform(20, animationPlatforms.get(0).y - 1, 65, 10));
                        animationPlatforms.set(1, new Platform(100, animationPlatforms.get(1).y - 1, 65, 10));
                        animationPlatforms.set(2, new Platform(180, animationPlatforms.get(2).y - 1, 65, 10));
                    }
                    if (p.posY >= 75) {
                        p.speedY = 0;
                        p.gravity = 0;
                        if (p.posY != 78) {//placing player on the "j"
                            p.posY--;
                        }

                        if (letterOffset < 1000) {
                            letterOffset += 2;
                        } else {
                            if (letterDelay >= 500) {
                                if (alphaCounter == 5) {
                                    if (alpha != 0) {
                                        alpha--;
                                    } else {
                                        if (animationDelay == 100) {
                                            inAnimation = false;
                                        }
                                        animationDelay++;
                                    }
                                    alphaCounter = 0;
                                } else {
                                    alphaCounter++;
                                }
                            }
                            letterDelay++;
                        }

                        offg.setColor(new Color(Color.black.getRed(), Color.black.getGreen(), Color.black.getBlue(), alpha));
                        offg.setFont(new Font("Helvetica", Font.BOLD, 100));
                        offg.drawString("Project", 124, 1150 - letterOffset);//y = 150
                        offg.drawString("Lag", 220, 1250 - letterOffset);//y = 250

                        offg.setColor(Color.white);
                        offg.fillRect(295, 1075 - letterOffset, 20, 20);//covers the dot //y = 75
                    }
                }

                for (Platform plat : animationPlatforms) {
                    plat.paint(offg);
                }

                p.paint(offg, alpha);
                
            } else {//MENU
                offg.setFont(new Font("Helvetica", Font.BOLD, 50));
                offg.setColor(Color.red);
                offg.drawString("Play", 240, 75);
                offg.setFont(new Font("Helvetica", Font.BOLD, 30));
                offg.setColor(Color.cyan);
                offg.drawString("Level Designer", 185, 140);
                offg.setColor(Color.blue);
                offg.drawString("Created Levels", 185, 200);
                offg.setColor(Color.black);
                offg.drawString("Character Editor", 175, 260);
                
                File[] files = new File("src/Player Images/").listFiles();
                for(File f : files){
                    if(f.getName().endsWith(".png") && !playerImages.contains(f)){
                        playerImages.add(f);
                    }
                }
                try {
                    img = ImageIO.read(playerImages.get(imageIndex));
                } catch (IOException ex) {
                    Logger.getLogger(Project_Lag.class.getName()).log(Level.SEVERE, null, ex);
                }
                offg.drawImage(img.getScaledInstance(50, 50, 0), 270, 280, this);
                if(new Rectangle(270,280,50,50).contains(mouseX, mouseY)){
                    imageIndex++;
                    if(imageIndex > playerImages.size()-1){
                        imageIndex = 0;
                    }
                }
                
                Rectangle play = new Rectangle(240, 35, 105, 45);
                Rectangle LD = new Rectangle(185, 118, 215, 26);
                Rectangle CL = new Rectangle(185, 175, 215, 26);
                Rectangle CE = new Rectangle(175, 235, 235, 26);
                
                if(play.contains(mouseX, mouseY)){
                    inMenu = false;
                }
                if(LD.contains(mouseX, mouseY)){
                    inMenu = false;
                    inLevelDesigner = true;
                    hasClicked = false;
                }
                if(CL.contains(mouseX, mouseY)){
                    inMenu = false;
                    inCreatedLevels = true;
                    mouseX = 0;
                    mouseY = 0;
                }
                if(CE.contains(mouseX, mouseY)){
                    inMenu = false;
                    inCharacterEditor = true;
                    mouseX = 0;
                    mouseY = 0;
                }
            }
        } 
        else if(inLevelDesigner) 
        {
            offg.setColor(Color.white);
            offg.fillRect(0, 0, 600, 350);
            if(d1){
                //reset arrays
                platforms.clear();
                platforms.add(new Platform(0, 300, 600, 50));
                signs.clear();
                walls.clear();
                walls.add(new Wall(0, 0, 15, 250));
                walls.add(new Wall(585, 0, 15, 250));
                walls.add(new Wall(0, 0, 600, 15));

                player.posX = 15;
                player.posY = 390;
                d1 = false;
            }
            
            //switches selected item
            if(mousePos != null){
                switch(itemIndex){
                    case 0:
                        break;
                    case 1:
                        new Platform(mousePos.x, mousePos.y, dWidth, dHeight).paint(offg);
                        break;
                    case 2:
                        new Wall(mousePos.x, mousePos.y, dWidth, dHeight).paint(offg);
                        break;
                    case 3:
                        new Sign(mousePos.x, mousePos.y, "").paint(offg);
                        break;
                }
            }
            if(placingSign || saving){//draws sign GUI and message, 96 chars max
                offg.setFont(new Font("Verdana",Font.PLAIN,10));
                offg.setColor(Color.yellow);
                if(placingSign)
                    offg.fillRect(15, 15, offg.getFontMetrics().stringWidth(signMessage) + 4, 12);
                else
                    offg.fillRect(15, 15, offg.getFontMetrics().stringWidth(saveMessage) + 4, 12);
                offg.setColor(Color.black);
                if(placingSign){
                    offg.drawRect(15, 15, offg.getFontMetrics().stringWidth(signMessage) + 4, 12);
                    offg.drawString(signMessage, 17, 25);
                }else{
                    offg.drawRect(15, 15, offg.getFontMetrics().stringWidth(saveMessage) + 4, 12);
                    offg.drawString(saveMessage, 17, 25);
                }
                
                
                offg.setFont(new Font("Arial",Font.BOLD, 20));
                offg.setColor(Color.red);
                if(placingSign)
                    offg.fillRect(250, 50, 90, 27);
                else
                    offg.fillRect(250, 50, 75, 27);
                offg.setColor(Color.white);
                if(placingSign)
                    offg.drawString("CREATE", 255, 70);
                else
                    offg.drawString("SAVE", 260, 70);
                Rectangle create = new Rectangle(250, 50, 75, 27);
                if(create.contains(mouseX, mouseY)){
                    if(placingSign){
                        placingSign = false;
                        signs.add(new Sign(signPos.x, signPos.y, signMessage.toUpperCase()));
                        signMessage = "";
                    }
                    else if(saving){
                        saving = false;
                        saveLevel(saveMessage);
                        saveMessage = "";
                    }
                    hasClicked = false;
                }
            }
            
            for (Platform plat : platforms) {
                plat.paint(offg);
            }
            for (Wall wall : walls) {
                wall.paint(offg);
            }
            for (Sign sign : signs) {
                sign.paint(offg);
            }
            
            //edit bar
            if(!itemType.equals(""))//has selected an item
            {
                switch(itemType){
                    case "Platform":
                        Platform plat = platforms.get(itemLoc);
                        offg.setColor(Color.white);
                        offg.setFont(new Font("Arial",Font.BOLD, 15));
                        offg.drawString("X:  " + Integer.toString(plat.x), 20, 320);
                        offg.drawString("Y:  " + Integer.toString(plat.y), 20, 340);
                        offg.drawString("Width:  " + Integer.toString(plat.length), 100, 320);
                        offg.drawString("Height:  " + Integer.toString(plat.height), 100, 340);
                        if(deleteItem && itemLoc != 0){
                            platforms.remove(itemLoc);
                            itemType = "";
                            itemLoc = 0;
                        }
                        break;
                    case "Wall":
                        Wall wall = walls.get(itemLoc);
                        offg.setColor(Color.white);
                        offg.setFont(new Font("Arial",Font.BOLD, 15));
                        offg.drawString("X:  " + Integer.toString(wall.x), 20, 320);
                        offg.drawString("Y:  " + Integer.toString(wall.y), 20, 340);
                        offg.drawString("Width:  " + Integer.toString(wall.length), 100, 320);
                        offg.drawString("Height:  " + Integer.toString(wall.height), 100, 340);
                        if(deleteItem && itemLoc != 0 && itemLoc != 1 && itemLoc != 2){
                            walls.remove(itemLoc);
                            itemType = "";
                            itemLoc = 0;
                        }
                        break;
                    case "Sign":
                        Sign sign = signs.get(itemLoc);
                        offg.setColor(Color.white);
                        offg.setFont(new Font("Arial",Font.BOLD, 15));
                        offg.drawString("X:  " + Integer.toString(sign.x), 20, 320);
                        offg.drawString("Y:  " + Integer.toString(sign.y), 20, 340);
                        offg.drawString("Message:  " + sign.message, 100, 320);
                        if(deleteItem){
                            signs.remove(itemLoc);
                            itemType = "";
                            itemLoc = 0;
                        }
                        break;
                }
            }
            
            //save button
            offg.setColor(Color.red);
            offg.setFont(new Font("Arial",Font.BOLD, 20));
            offg.fillRect(520, 310, 65, 30);
            offg.setColor(Color.white);
            offg.drawString("SAVE", 525, 332);
            Rectangle save = new Rectangle(520, 310, 65, 30);
            if(save.contains(mouseX, mouseY)){
                saving = true;
            }
        }
        else if(inCreatedLevels)
        {
            File file = new File("src/Created Levels/");
            File[] files = file.listFiles();
            ArrayList<Rectangle> rects = new ArrayList();
            
            offg.setColor(Color.black);
            offg.setFont(new Font("Arial",Font.BOLD, 25));
            int posY = 25;
            int rectY = 1;
            for(File f : files){
                if(f.getName().endsWith(".txt")){
                    offg.drawString(f.getName(), 10, posY);
                    rects.add(new Rectangle(1, rectY, 600, 30));
                    posY += 30;
                    rectY += 30;
                }
            }
            
            for(int i = 0; i < rects.size(); i++){
                offg.drawRect(rects.get(i).x, rects.get(i).y, rects.get(i).width, rects.get(i).height);
                if(rects.get(i).contains(mouseX, mouseY)){
                    inCreatedLevels = false;
                    inMenu = false;
                    startLevel = false;
                    loadLevel(files[i+1].getName().replace(".txt", ""), true, true);
                }
            }
        }
        else if(inCharacterEditor)
        {
            for(int i = 0; i < characterGrid.size(); i++){//iterates through the grid array
                Rectangle rect = characterGrid.get(i);
                offg.setColor(gridColors.get(i));
                offg.fillRect(rect.x, rect.y, rect.width, rect.height);//draws the grid
                offg.setColor(Color.black);
                offg.drawRect(rect.x, rect.y, rect.width, rect.height);//draws the grid outline
                
                if(rect.contains(mouseX, mouseY)){//has clicked on grid
                    gridColors.set(i, selectedColor);
                }
            }
            
            //drawing 10x10 version of the drawing
            int gridX = 465;
            int gridY = 300;
            for(int i = 0; i < gridColors.size(); i++){
                offg.setColor(gridColors.get(i));
                offg.drawRect(gridX, gridY, 1, 1);
                gridX++;
                if(gridX == 475){
                    gridX = 465;
                    gridY++;
                }
            }
            
            offg.setColor(Color.black);
            offg.drawRect(360, 20, 50, 25);//white
            if(new Rectangle(360, 20, 50, 25).contains(mouseX, mouseY)){
                selectedColor = Color.white;
            }
            offg.setColor(Color.black);
            offg.fillRect(420, 20, 50, 25);
            if(new Rectangle(420, 20, 50, 25).contains(mouseX, mouseY)){
                selectedColor = Color.black;
            }
            offg.setColor(Color.blue);
            offg.fillRect(480, 20, 50, 25);
            if(new Rectangle(480, 20, 50, 25).contains(mouseX, mouseY)){
                selectedColor = Color.blue;
            }
            offg.setColor(Color.cyan);
            offg.fillRect(540, 20, 50, 25);
            if(new Rectangle(540, 20, 50, 25).contains(mouseX, mouseY)){
                selectedColor = Color.cyan;
            }
            offg.setColor(Color.darkGray);
            offg.fillRect(360, 65, 50, 25);
            if(new Rectangle(360, 65, 50, 25).contains(mouseX, mouseY)){
                selectedColor = Color.darkGray;
            }
            offg.setColor(Color.gray);
            offg.fillRect(420, 65, 50, 25);
            if(new Rectangle(420, 65, 50, 25).contains(mouseX, mouseY)){
                selectedColor = Color.gray;
            }
            offg.setColor(Color.lightGray);
            offg.fillRect(480, 65, 50, 25);
            if(new Rectangle(480, 65, 50, 25).contains(mouseX, mouseY)){
                selectedColor = Color.lightGray;
            }
            offg.setColor(Color.green);
            offg.fillRect(540, 65, 50, 25);
            if(new Rectangle(540, 65, 50, 25).contains(mouseX, mouseY)){
                selectedColor = Color.green;
            }
            offg.setColor(Color.magenta);
            offg.fillRect(360, 110, 50, 25);
            if(new Rectangle(360, 110, 50, 25).contains(mouseX, mouseY)){
                selectedColor = Color.magenta;
            }
            offg.setColor(Color.yellow);
            offg.fillRect(420, 110, 50, 25);
            if(new Rectangle(420, 110, 50, 25).contains(mouseX, mouseY)){
                selectedColor = Color.yellow;
            }
            offg.setColor(Color.orange);
            offg.fillRect(480, 110, 50, 25);
            if(new Rectangle(480, 110, 50, 25).contains(mouseX, mouseY)){
                selectedColor = Color.orange;
            }
            offg.setColor(Color.red);
            offg.fillRect(540, 110, 50, 25);
            if(new Rectangle(540, 110, 50, 25).contains(mouseX, mouseY)){
                selectedColor = Color.red;
            }
            
            offg.setFont(new Font("Arial",Font.BOLD, 19));
            offg.setColor(Color.black);
            offg.drawRect(360, 170, 230, 25);
            offg.drawString("CLEAR", 440, 189);
            if(new Rectangle(360, 170, 230, 25).contains(mouseX, mouseY)){
                for(int i = 0; i < 100; i++){
                    gridColors.set(i, Color.white);
                }
            }
            
            offg.setColor(Color.black);
            offg.fillRect(360, 210, 230, 25);
            offg.setColor(Color.white);
            offg.drawString("FILL", 455, 229);
            if(new Rectangle(360, 210, 230, 25).contains(mouseX, mouseY)){
                for(int i = 0; i < 100; i++){
                    gridColors.set(i, selectedColor);
                }
            }
            
            offg.setColor(Color.red);
            offg.fillRect(360, 250, 230, 25);
            offg.setColor(Color.white);
            offg.drawString("SAVE", 450, 269);
            if(new Rectangle(360, 250, 230, 25).contains(mouseX, mouseY)){
                playerImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
                int x = 0;
                int y = 0;
                for(int i = 0; i < 100; i++){
                    playerImage.setRGB(x, y, gridColors.get(i).getRGB());
                    x++;
                    if(x == 10){
                        x = 0;
                        y++;
                    }
                }
                
                try {
                    ImageIO.write(playerImage, "png", new File("src/Player Images/" + playerImage.hashCode() + ".png"));
                } catch (IOException ex) {
                    Logger.getLogger(Project_Lag.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        else{//MAIN GAME PAINT LOOP
            
            for (Platform plat : platforms) {
                plat.paint(offg);
            }
            for (Wall wall : walls) {
                wall.paint(offg);
            }
            for (Sign sign : signs) {
                sign.paint(offg);
            }
            player.paint(offg);
        }

        //must be last
        mouseX = 0;
        mouseY = 0;
        g.drawImage(offscreen, 0, 0, this);
        repaint();
    }

    public void loadLevel(String level, boolean created, boolean goingForward) {
        //reset arrays
        platforms.clear();
        platforms.add(new Platform(0, 300, 600, 50));
        signs.clear();
        walls.clear();
        walls.add(new Wall(0, 0, 15, 250));
        walls.add(new Wall(585, 0, 15, 250));
        walls.add(new Wall(0, 0, 600, 15));

        if(goingForward){
            player.posX = 15;
        }else{
            player.posX = 575;
        }
        player.posY = 290;
        
        String fileString;
        if(!created)
            fileString = "src/Levels/" + level + ".txt";
        else
            fileString = "src/Created Levels/" + level + ".txt";
        
        try {
            Scanner s = new Scanner(new File(fileString));

            while (s.hasNextLine()) {
                String[] parts = s.nextLine().split(":");
                switch (parts[0]) {
                    case "sign":
                        signs.add(new Sign(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), parts[3]));
                        break;
                    case "wall":
                        walls.add(new Wall(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]), Integer.parseInt(parts[4])));
                        break;
                    case "platform":
                        platforms.add(new Platform(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]), Integer.parseInt(parts[4])));
                        break;
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Project_Lag.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void saveLevel(String name)//creates text file and creates level data
    {
        try //creates text file and creates level data
        {
            try (PrintWriter writer = new PrintWriter("src/Created Levels/" + name + ".txt", "UTF-8")) {
                for(Platform plat : platforms){
                    writer.println("platform:"+Integer.toString(plat.x)+":"+Integer.toString(plat.y)+":"+Integer.toString(plat.length)+":"+Integer.toString(plat.height));
                }
                for(Wall wall : walls){
                    writer.println("wall:"+Integer.toString(wall.x)+":"+Integer.toString(wall.y)+":"+Integer.toString(wall.length)+":"+Integer.toString(wall.height));
                }
                for(Sign sign : signs){
                    writer.println("sign:"+Integer.toString(sign.x)+":"+Integer.toString(sign.y)+":"+sign.message);
                }
                writer.close();
            }
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            Logger.getLogger(Project_Lag.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void playSounds()
    {
        if(player.playJumpSound){
            jump.play();
            player.playJumpSound = false;
        }
    }
    
    public boolean checkPlatformCollision(Platform p)
    {
        for(Platform plat : platforms){
            if(p.box.intersects(plat.box)) return true;
        }
        for(Wall wall : walls){
            if(p.box.intersects(wall.box)) return true;
        }
        for(Sign sign : signs){
            if(p.box.intersects(sign.box)) return true;
        }
        return false;
    }
    public boolean checkWallCollision(Wall w)
    {
        for(Platform plat : platforms){
            if(w.box.intersects(plat.box)) return true;
        }
        for(Wall wall : walls){
            if(w.box.intersects(wall.box)) return true;
        }
        for(Sign sign : signs){
            if(w.box.intersects(sign.box)) return true;
        }
        return false;
    }
    public boolean checkSignCollision(Sign s)
    {
        for(Platform plat : platforms){
            if(s.box.intersects(plat.box)) return true;
        }
        for(Wall wall : walls){
            if(s.box.intersects(wall.box)) return true;
        }
        for(Sign sign : signs){
            if(s.box.intersects(sign.box)) return true;
        }
        return false;
    }
    
    public boolean hasClickedItem(){
        int x = mousePos.x;
        int y = mousePos.y;
        for (Platform plat : platforms) {
            if(plat.box.contains(x, y))
                return true;
        }
        for (Wall wall : walls) {
            if(wall.box.contains(x, y))
                return true;
        }
        for (Sign sign : signs) {
            if(sign.box.contains(x, y))
                return true;
        }
        return false;
    }
    
    public int getClickedItemIndex(){
        int x = mousePos.x;
        int y = mousePos.y;
        for (int i = 0; i < platforms.size(); i++) {
            if(platforms.get(i).box.contains(x, y))
                return i;
        }
        for (int i = 0; i < walls.size(); i++) {
            if(walls.get(i).box.contains(x, y))
                return i;
        }
        for (int i = 0; i < signs.size(); i++) {
            if(signs.get(i).box.contains(x, y))
                return i;
        }
        return -1;
    }
    public String getClickedItemType(){
        int x = mousePos.x;
        int y = mousePos.y;
        for (int i = 0; i < platforms.size(); i++) {
            if(platforms.get(i).box.contains(x, y))
                return "Platform";
        }
        for (int i = 0; i < walls.size(); i++) {
            if(walls.get(i).box.contains(x, y))
                return "Wall";
        }
        for (int i = 0; i < signs.size(); i++) {
            if(signs.get(i).box.contains(x, y))
                return "Sign";
        }
        return "";
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                //player.speedX = -5;
                leftKey = true;
                controlBuffer.add("left");
                break;
            case KeyEvent.VK_RIGHT:
                //player.speedX = 5;
                rightKey = true;
                controlBuffer.add("right");
                break;
            case KeyEvent.VK_UP:
                //player.jump();
                upKey = true;
                controlBuffer.add("jump");
                break;
            case KeyEvent.VK_DOWN:
                downKey = true;
                break;
            case KeyEvent.VK_0:
                itemIndex = 0;
                break;
            case KeyEvent.VK_1:
                itemIndex = 1;
                itemType = "";
                break;
            case KeyEvent.VK_2:
                itemIndex = 2;
                itemType = "";
                break;
            case KeyEvent.VK_3:
                itemIndex = 3;
                itemType = "";
                break;
            case KeyEvent.VK_BACK_SPACE:
                deleteItem = true;
                break;
            case KeyEvent.VK_ESCAPE:
                escapeKey = true;
                break;
        }
        if(placingSign && signMessage.length() <= 96){
            if(e.getKeyChar() == KeyEvent.VK_BACK_SPACE  && signMessage.length() > 0){
                signMessage = signMessage.substring(0, signMessage.length() - 1);
            }else if(Character.isAlphabetic(e.getKeyChar()) || Character.isDigit(e.getKeyChar()) || e.getKeyChar() == ' ' || e.getKeyChar() == '.' || e.getKeyChar() == ',' || e.getKeyChar() == '?' || e.getKeyChar() == '!'){
                signMessage += e.getKeyChar();
            }
        }
        if(saving && saveMessage.length() <= 96){
            if(e.getKeyChar() == KeyEvent.VK_BACK_SPACE  && saveMessage.length() > 0){
                saveMessage = saveMessage.substring(0, saveMessage.length() - 1);
            }else if(Character.isAlphabetic(e.getKeyChar()) || Character.isDigit(e.getKeyChar()) || e.getKeyChar() == '-' || e.getKeyChar() == '_'){
                saveMessage += e.getKeyChar();
            }
        }
    }

    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                leftKey = false;
                break;
            case KeyEvent.VK_RIGHT:
                rightKey = false;
                break;
            case KeyEvent.VK_UP:
                upKey = false;
                break;
            case KeyEvent.VK_DOWN:
                downKey = false;
                break;
            case KeyEvent.VK_BACK_SPACE:
                deleteItem = false;
                break;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        if(e.getButton() == 1){
            hasClicked = true;
        }
    }
    @Override
    public void mousePressed(MouseEvent e) {
        
    }
    @Override
    public void mouseReleased(MouseEvent e) {
    }
    @Override
    public void mouseEntered(MouseEvent e) {
    }
    @Override
    public void mouseExited(MouseEvent e) {
    }
}

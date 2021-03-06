package symbiote.client.screen;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import symbiote.client.Client;
import symbiote.client.InputListener;
import symbiote.entity.AbstractEntity;
import symbiote.entity.AbstractEntity.RenderType;
import symbiote.entity.client.ComplexDrawable;
import symbiote.entity.client.Drawable;
import symbiote.entity.client.Interactable;
import symbiote.misc.Util;

public class Screen {
    public Map<Integer, AbstractEntity> thingMap = new ConcurrentHashMap<>();
    
     /**
     * The looping code of this Screen. Generally used for game logic.
     */
    public void tick() {
        for (AbstractEntity t : thingMap.values()) {
            t.tick();
        }
        for (AbstractEntity t : thingMap.values()) {
            if (t instanceof Interactable) {
                Interactable i = (Interactable) t;
                Point p = Util.getMouseOnScreen();
                if (InputListener.leftMouseHeld) i.mouseHeld(p.x, p.y, InputListener.leftMouseEvent);
                if (InputListener.middleMouseHeld) i.mouseHeld(p.x, p.y, InputListener.middleMouseEvent);
                if (InputListener.rightMouseHeld) i.mouseHeld(p.x, p.y, InputListener.rightMouseEvent);
            }
        }
    }
    
    /**
     * Draws background entities, then draws foreground entities top to bottom.
     * @param g The Graphics2D the entities are being drawn on.
     */
    public void draw(Graphics2D g) { 
        //TODO: Keep objects from rendering over health bars and names
        HashMap<Double, List<AbstractEntity>> groupedEntities = new HashMap<>();
        List<AbstractEntity> frontEntities = new ArrayList<>();
        List<ComplexDrawable> complexes = new ArrayList<>();
        for (AbstractEntity e : thingMap.values()) {
            if (e instanceof Drawable) {
                if (e.renderType == RenderType.FOREGROUND) {
                    double bottomY = e.y + e.height;
                    List<AbstractEntity> similars = groupedEntities.get(bottomY);
                    if (similars == null) similars = new ArrayList<>();
                    similars.add(e);
                    groupedEntities.put(bottomY, similars);
                } else if (e.renderType == RenderType.BACKGROUND) {
                    ((Drawable)e).draw(g);
                } else {
                    frontEntities.add(e);
                }
                if (e instanceof ComplexDrawable) complexes.add((ComplexDrawable)e);
            }
        }
        List<Double> sortedDoubles = Util.asSortedList(groupedEntities.keySet());
        for (Double i : sortedDoubles) {
            for (AbstractEntity e : groupedEntities.get(i)) {
                ((Drawable)e).draw(g);
            }
        }
        for (AbstractEntity e : frontEntities) {
            Composite oldC = g.getComposite();
            AbstractEntity f = Client.focus;
            if (Client.focus != null && e.getBounds().intersects(f.x, f.y, f.width, f.height)) {        
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            }
            ((Drawable) e).draw(g);
            g.setComposite(oldC);
        }
        for (ComplexDrawable c : complexes) {
            c.finalDraw(g);
        }
    }
    
    //TODO: IMPLEMENT
    public void mouseEnter() {}   
    public void mouseLeave() {}   
    
    public void mouseClicked(int x, int y, MouseEvent m) {
        for (AbstractEntity t : thingMap.values()) {
            if (t instanceof Interactable)
                ((Interactable) t).mouseClicked(x, y, m);
        }
    }  
    
    public void mouseHeld(int x, int y, MouseEvent m) {
        for (AbstractEntity t : thingMap.values()) {
            if (t instanceof Interactable)
                ((Interactable) t).mouseHeld(x, y, m);
        }
    }  
    
    public void mouseReleased(int x, int y, MouseEvent m) {
        for (AbstractEntity t : thingMap.values()) {
            if (t instanceof Interactable)
                ((Interactable) t).mouseReleased(x, y, m);
        }
    }
    
    public void keyPressed(KeyEvent k) {
        for (AbstractEntity t : thingMap.values()) {
            if (t instanceof Interactable)
                ((Interactable) t).keyPressed(k);
        }
    }
    
    public void keyReleased(KeyEvent k) {
        for (AbstractEntity t : thingMap.values()) {
            if (t instanceof Interactable)
                ((Interactable) t).keyReleased(k);
        }
    }
}

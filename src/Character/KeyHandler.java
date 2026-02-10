package Character;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {

    // Directiile de miscare
    public boolean upPressed, downPressed, leftPressed, rightPressed;

    // Zoom
    public boolean zoomInPressed, zoomOutPressed;

    // Actiuni speciale
    public boolean attack, interact;

    //Butonul de pauza
    public boolean pausePressed;

    public boolean fillBucketPressed = false;




    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        // Controlul pentru miscare
        handleMovementKeys(code, true);

        // Controlul pentru zoom-in
        if (code == KeyEvent.VK_PLUS || code == KeyEvent.VK_EQUALS) {  // Tasta + sau =
            zoomInPressed = true;
        }

        // Controlul pentru zoom-out
        if (code == KeyEvent.VK_MINUS) {  // Tasta -
            zoomOutPressed = true;
        }

        // Controlul pentru atac
        if (code == KeyEvent.VK_K) {  // Tasta K pentru atac
            attack = true;
        }

        if (code == KeyEvent.VK_E) {  // Tasta E pentru obiecte cu care poti interactiona
            interact = true;
        }

        if (code == KeyEvent.VK_ESCAPE)
        {
            pausePressed = !pausePressed;
        }

        if (code == KeyEvent.VK_U) {
            fillBucketPressed = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        // Dezactivam miscarea cand tasta este eliberata
        handleMovementKeys(code, false);

        // Dezactivam zoom si actiuni speciale cand sunt eliberate tastele
        if (code == KeyEvent.VK_PLUS || code == KeyEvent.VK_EQUALS) {
            zoomInPressed = false;
        }

        if (code == KeyEvent.VK_MINUS) {
            zoomOutPressed = false;
        }

        if (code == KeyEvent.VK_K) {  // Atacul
            attack = false;
        }

        if (code == KeyEvent.VK_E) {  // Obiecte interactionabile
            interact = false;
        }

        if (code == KeyEvent.VK_U) {
            fillBucketPressed = false;
        }
    }

    // Metoda ajutatoare pentru a simplifica logica de miscare
    private void handleMovementKeys(int code, boolean isPressed) {
        switch (code) {
            case KeyEvent.VK_W:
            case KeyEvent.VK_UP:
                upPressed = isPressed;
                break;
            case KeyEvent.VK_S:
            case KeyEvent.VK_DOWN:
                downPressed = isPressed;
                break;
            case KeyEvent.VK_A:
            case KeyEvent.VK_LEFT:
                leftPressed = isPressed;
                break;
            case KeyEvent.VK_D:
            case KeyEvent.VK_RIGHT:
                rightPressed = isPressed;
                break;
        }
    }
}

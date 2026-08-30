/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package comicportascrapper;


import javax.swing.JFrame;
import javax.swing.ImageIcon;
/**
 *
 * @author Ryan
 */
public class ComicPortaScrapper {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args){
        mainJFrame main = new mainJFrame();
        main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        main.setSize(470, 140);
        main.setLocationRelativeTo(null);
        main.onStartUp();
        main.setVisible(true);
    }
    
}

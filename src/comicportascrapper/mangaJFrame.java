/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package comicportascrapper;


import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.SwingWorker;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 *
 * @author Ryan
 */
public class mangaJFrame extends javax.swing.JFrame {
    //Chapter Links to open when downloading
    ArrayList<String> chapterLinks = new ArrayList<>();
    Path DLPath;
    
    /**
     * Creates new form mangaJFrame
     */
    public mangaJFrame() {
        initComponents();
        ImageIcon logo = new ImageIcon(getClass().getResource("icon.png"));
        this.setIconImage(logo.getImage());
    }
    
    
    public void openMangaSite(String link){
        
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--disable-gpu");
        
        WebDriver driver = new ChromeDriver(options);
        driver.get(link);
        List<WebElement> episode = driver.findElements(By.className("episode"));
        String title = driver.findElement(By.cssSelector("h2.title")).getAttribute("textContent");
        mangaTitleJLabel.setText(title);
        
        for (WebElement chapter: episode){
            try{
                chapter.findElement(By.className("episode-btn")).isDisplayed();
                String chapterName = chapter.findElement(By.className("title")).getAttribute("textContent");
                chapterJComboBox.addItem(chapterName);
                String episodeLink = chapter.findElement(By.cssSelector("p.episode-btn>a")).getAttribute("href");
                System.out.printf("\nAdding %s to ArrayList", episodeLink);
                chapterLinks.add(episodeLink);
            }
            catch(NoSuchElementException ex){}
                continue;
            }
        
        
        driver.quit();
        System.out.println("\n----Closing: " + link);
    }
    
    public void downloadChapter(String link, String chapter) throws InterruptedException, IOException{
        statusJLabel.setText("Downloading...");
        mangaJButton.setEnabled(false);
        
        
        
        String mangaTitle = mangaTitleJLabel.getText();
        Path folder = Paths.get(getDLPath() + "\\" + mangaTitle + "\\" + chapter);
        
        
        SwingWorker<Void, Void> worker = new SwingWorker<>(){
            @Override
            protected Void doInBackground() throws Exception {
               try{
                    Files.createDirectories(folder);
                    System.out.printf("\nCreating: %s", folder);
                }
                catch(IOException ex){
                    System.out.printf("\n----%s already made; skipping...", folder);
                }
               
                int pgNum = 0;
                
                System.out.printf("\nDownloading: %s", link);
                ChromeOptions options = new ChromeOptions();
                options.addArguments("--headless=new");
                options.addArguments("--disable-gpu");
                options.addArguments("--window-size=1920,1080");
                options.addArguments("--no-sandbox");

                WebDriver driver = new ChromeDriver(options);
                driver.get(link);
                WebDriverWait wait = new WebDriverWait(driver, (Duration.ofSeconds(20)));
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.pt-img > div > img")));

                int pages = Integer.parseInt(driver.findElement(By.id("menu_slidercaption")).getAttribute("innerText").split("/")[1]);
                System.out.printf("\nPages: %d", pages);

                try{
                    ArrayList<String> imageURL = new ArrayList<>();
                    int iterations = (pages + 1) / 2;
                    for(int x = 0; x <=  iterations; x++){
                        System.out.printf("\nIteration %s/%s", x, iterations);
                        WebElement body = driver.findElement(By.tagName("body"));
                        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.pt-img")));

                        List<WebElement> images = driver.findElements(By.cssSelector("div.pt-img > div > img"));
                        TimeUnit.SECONDS.sleep(5);
                        String js_script = """
                                           var uri = arguments[0];
                                                           var callback = arguments[1];

                                                           fetch(uri)
                                                               .then(response => response.blob())
                                                               .then(blob => {
                                                                   var reader = new FileReader();
                                                                   reader.onloadend = function() {
                                                                       callback(reader.result);
                                                                   };
                                                                   reader.onerror = function() {
                                                                       callback(null);
                                                                   };
                                                                   reader.readAsDataURL(blob);
                                                               })
                                                               .catch(err => callback(null));
                                           """;

                        for (WebElement image: images.reversed()){
                            try{
                                String foundImage = image.getAttribute("src");

                                if (foundImage==null){
                                    continue;
                                }
                                if(foundImage.contains("banner") || foundImage.contains("AD")){
                                    continue;
                                }
                                if(imageURL.contains(foundImage)){
                                    continue;
                                }
                                imageURL.add(foundImage);


                                JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
                                String dataURL = (String) jsExecutor.executeAsyncScript(js_script, foundImage);

                                if (dataURL != null){
                                    String[] parts = dataURL.split(",", 2);

                                    if (parts.length == 2){
                                        String base64Data = parts[1];

                                        byte[] fileBytes = Base64.getDecoder().decode(base64Data);

                                        Path outputPath = Paths.get("%s\\%d_temp.png".formatted(
                                        folder.toString(), pgNum));
                                        Files.write(outputPath, fileBytes);
                                        System.out.printf("\nImage %d saved.", pgNum);
                                    }
                                }

                                pgNum+=1;
                            }
                            catch(StaleElementReferenceException ex){
                                continue;
                            } catch (IOException ex) {
                                System.getLogger(mangaJFrame.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                            }
                        }

                        body.sendKeys(Keys.ARROW_LEFT);

                    }
                }
                finally{
                    driver.quit();
                }
                stitchImages(folder, pages);
                return null;
            }
            
        };
        
        worker.execute();
        
    }
    
    private void stitchImages(Path folder, int pages) throws IOException{
        statusJLabel.setText("Stitching...");
        mangaJButton.setEnabled(false);
        
        SwingWorker<Void, Void> worker = new SwingWorker<>(){
            @Override
            protected Void doInBackground() throws Exception {
                try(Stream<Path> stream = Files.list(folder)){
                    List<Path> subdirectories = stream
                            .filter(Files::isRegularFile)
                            .filter(path -> path.getFileName().toString().matches("\\d+\\_temp.png"))
                            .collect(Collectors.toList());

                    subdirectories.sort(Comparator.comparingInt(path ->
                    Integer.parseInt(path.getFileName().toString().replace("_temp.png", ""))));

                    int stitchedPgCounter = 0;
                    for(int x = 0; x < subdirectories.size(); x+=3){
                        BufferedImage img1 = ImageIO.read(new File(subdirectories.get(x).toUri()));
                        BufferedImage img2 = ImageIO.read(new File(subdirectories.get(x + 1).toUri()));
                        BufferedImage img3 = ImageIO.read(new File(subdirectories.get(x + 2).toUri()));

                        int width = img1.getWidth();
                        int maxHeight = img1.getHeight() + img2.getHeight() + img3.getHeight();

                        BufferedImage stitchedImage = new BufferedImage(width, maxHeight, BufferedImage.TYPE_INT_RGB);

                        Graphics2D g2d = stitchedImage.createGraphics();
                        g2d.drawImage(img3, 0, 0, null);
                        g2d.drawImage(img2, 0, img3.getHeight(), null);
                        g2d.drawImage(img1, 0, img3.getHeight() + img2.getHeight(), null);
                        g2d.dispose();

                        Files.delete(subdirectories.get(x));
                        Files.delete(subdirectories.get(x + 1));
                        Files.delete(subdirectories.get(x + 2));

                        ImageIO.write(stitchedImage, "png", new File(folder + "\\" + String.valueOf(stitchedPgCounter) + ".png"));
                        System.out.printf("\nImage %d stitched.", stitchedPgCounter);
                        stitchedPgCounter += 1;
                    }
                }
                return null;
            }
                @Override
                protected void done(){
                    mangaJButton.setEnabled(true);
                    DLJButton.setEnabled(true);
                    statusJLabel.setText("Finished");
                }
        };
        
        worker.execute();
    }
                
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        chapterJComboBox = new javax.swing.JComboBox<>();
        mangaJButton = new javax.swing.JButton();
        goBackJButton = new javax.swing.JButton();
        mangaTitleJLabel = new javax.swing.JLabel();
        DLJButton = new javax.swing.JButton();
        DirPathLabel = new javax.swing.JLabel();
        statusJLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        chapterJComboBox.setFont(new java.awt.Font("MS Gothic", 0, 12)); // NOI18N
        chapterJComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All Chapters" }));
        chapterJComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chapterJComboBoxActionPerformed(evt);
            }
        });

        mangaJButton.setFont(new java.awt.Font("MS Gothic", 0, 12)); // NOI18N
        mangaJButton.setText("Download Chapter");
        mangaJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mangaJButtonActionPerformed(evt);
            }
        });

        goBackJButton.setFont(new java.awt.Font("MS Gothic", 0, 12)); // NOI18N
        goBackJButton.setText("<< All Manga");
        goBackJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                goBackJButtonActionPerformed(evt);
            }
        });

        mangaTitleJLabel.setFont(new java.awt.Font("MS Gothic", 0, 12)); // NOI18N
        mangaTitleJLabel.setText(" ");

        DLJButton.setFont(new java.awt.Font("MS Gothic", 0, 12)); // NOI18N
        DLJButton.setText("DL Location");
        DLJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DLJButtonActionPerformed(evt);
            }
        });

        DirPathLabel.setFont(new java.awt.Font("MS Gothic", 0, 12)); // NOI18N
        DirPathLabel.setText("Current Location:");

        statusJLabel.setFont(new java.awt.Font("MS Gothic", 0, 12)); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(DirPathLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(chapterJComboBox, 0, 301, Short.MAX_VALUE)
                                    .addComponent(mangaTitleJLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(goBackJButton)
                                .addGap(205, 205, 205)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(DLJButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(mangaJButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(statusJLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(31, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(mangaTitleJLabel)
                    .addComponent(DLJButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chapterJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(mangaJButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(goBackJButton)
                    .addComponent(statusJLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 26, Short.MAX_VALUE)
                .addComponent(DirPathLabel)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void chapterJComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chapterJComboBoxActionPerformed

    }//GEN-LAST:event_chapterJComboBoxActionPerformed

    private void mangaJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mangaJButtonActionPerformed
        DLJButton.setEnabled(false);
        
        SwingWorker<Void, Void> worker = new SwingWorker<>(){
            @Override
            protected Void doInBackground() throws Exception {
                Object selectedOption = chapterJComboBox.getSelectedItem();
                Object downloadAllOption = chapterJComboBox.getItemAt(0);
                if (selectedOption.equals(downloadAllOption)){
                    System.out.println("\nDownloading all chapters...");
                    for (int x = 1; x <  chapterJComboBox.getItemCount(); x++){
                        try {
                            downloadChapter(chapterLinks.get(x - 1 ), chapterJComboBox.getItemAt(x));
                        } catch (InterruptedException ex) {
                            System.getLogger(mangaJFrame.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                        } catch (IOException ex) {
                            System.getLogger(mangaJFrame.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                        }
                    }
                }
                else{
                    try {
                        downloadChapter(chapterLinks.get(chapterJComboBox.getSelectedIndex() - 1), selectedOption.toString());
                    } catch (InterruptedException ex) {
                        System.getLogger(mangaJFrame.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                    } catch (IOException ex) {
                        System.getLogger(mangaJFrame.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                    }
                }
                 return null;
            }
        };
        
        worker.execute();
        
    }//GEN-LAST:event_mangaJButtonActionPerformed

    private void goBackJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_goBackJButtonActionPerformed
        mainJFrame main = new mainJFrame();
        main.onStartUp();
        main.setLocationRelativeTo(null);
        main.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_goBackJButtonActionPerformed

    private Path openFolder(){
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.showOpenDialog(this);
        
        return fileChooser.getSelectedFile().toPath();
    }
    
    private void DLJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DLJButtonActionPerformed
        Path newDLPath = openFolder();
        
        setDLPath(newDLPath);
        DirPathLabel.setText("Current Location: %s".formatted(getDLPath()));
    }//GEN-LAST:event_DLJButtonActionPerformed

    public Path getDLPath(){
        return this.DLPath;
    }
    
    public void setDLPath(Path newDLPath){
        this.DLPath = newDLPath;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(mangaJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(mangaJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(mangaJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(mangaJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new mangaJFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton DLJButton;
    private javax.swing.JLabel DirPathLabel;
    private javax.swing.JComboBox<String> chapterJComboBox;
    private javax.swing.JButton goBackJButton;
    private javax.swing.JButton mangaJButton;
    private javax.swing.JLabel mangaTitleJLabel;
    private javax.swing.JLabel statusJLabel;
    // End of variables declaration//GEN-END:variables
}

package part2;
import part2.AquaFrame;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import java.awt.Graphics;

import java.awt.image.BufferedImage;

import q3.*;

/**
 * class AquaPanel:
 * JPanel of aquarium
 * 
 * @author Daniel Markov ,Anton Volkov 
 */
public class AquaPanel extends JPanel implements ActionListener{
    private AquaFrame frame;
    private JPanel p1;
    private JButton[] b_num;
    private String[] names = {"Add Animal","Sleep","Wake up","reset","Food","Info","Exit"};
    private JScrollPane scrollPane;
    private boolean isTableVisible = false;
    private boolean isTable2Visible = false;
    private HashSet<Swimmable> swimmables = new HashSet<Swimmable>();
    private BufferedImage wormImage=null;
    private boolean BackgroundeImageStatus=false;
    public static ExecutorService executorService = Executors.newFixedThreadPool(5);
        
     
	/**
	* this method is a constructor method to build a new AquaPanel .
    * @param AquaFrame -AquaFrame of the parent
	*/
    public AquaPanel(AquaFrame f) {
         frame = f;
         setBackground(new Color(255,255,255));
         p1=new JPanel();
         p1.setLayout(new GridLayout(1,7,0,0));
         p1.setBackground(new Color(0,150,255));
         b_num=new JButton[names.length];
         
         for(int i=0;i<names.length;i++) {
             b_num[i]=new JButton(names[i]);
             b_num[i].addActionListener(this);
             b_num[i].setBackground(Color.lightGray);
             p1.add(b_num[i]);		
         }
 
         setLayout(new BorderLayout());
         add("South", p1);

    }

    /**
    * return HashSet<Swimmable> of panel
    * @return HashSet<Swimmable>
    */
    public HashSet<Swimmable> getswimmables(){return swimmables;}
    
    /**
    * return size of swimmables
    * @return int
    */
    public int getswimmablessize(){return swimmables.size();}

    
    /** 
     * add swimmable to the hashset
     * @param s
     */
    public void addswimmables(Swimmable s){
        
        s.setCenter(getWidth()/2 ,getHeight()/2);
        executorService.execute(s);
        this.swimmables.add(s);
        
    }


    /** 
     * change image Background status
     */
    public void changeBackgroundimagestatus(){

        this.BackgroundeImageStatus= !this.BackgroundeImageStatus;
    }

    public boolean getimagestatus(){return this.BackgroundeImageStatus;};

    
    /** 
     * paint the Component
     * @param g
     */
    public void paintComponent(Graphics g)
    {    
        super.paintComponent(g);
        BufferedImage img = null;

        if (BackgroundeImageStatus==true)
        {
        try {
            img = ImageIO.read(new URL ("https://www.ubuy.com.tr/productimg/?image=aHR0cHM6Ly9tLm1lZGlhLWFtYXpvbi5jb20vaW1hZ2VzL0kvODFsbzVaTGJiOUwuX0FDX1NMMTUwMF8uanBn.jpg"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Image dimg = img.getScaledInstance(800, 600, Image.SCALE_DEFAULT);
        g.drawImage(dimg, 0, 0, null);
        }

        for (Swimmable s : swimmables){
            s.drawAnimal(g);
        }
        if (this.wormImage!=null)
        {   
            g.drawImage(wormImage,getWidth()/2 ,getHeight()/2 ,50, 50, null); 
        }

        repaint();
    }
    
        
    /** 
     * create a AddAnimalDialog
     */
    public void AddAnimal(){
        AddAnimalDialog dial = new AddAnimalDialog(frame,this,"Create post system");
        dial.setVisible(true);
    }

    /** 
     * stop the threads
     */
    public  void Sleep() {
        AquaFrame.STATE = "sleeping"; 
    }
    
     /** 
     * wake up the threads
     */
     public void Wakeup() {
        AquaFrame.STATE = "swiming";
        for (Swimmable swimmable : this.swimmables) {
            synchronized(swimmable) {
                swimmable.notify();
            }
        }
    }
 
     /** 
     * reset the panel from swimibles
     */
      public void Reset () {
          //stop all threads.
        for (Swimmable swimmable : this.swimmables) {
            swimmable.shutdown();
        }
        this.swimmables.clear();
    }

     /** 
     * shutdown the threads
     */
      public void Close(){
        this.executorService.shutdown();
      }

    /** 
     * feed the fish
     */
      public void food(){
        try {
            wormImage = ImageIO.read(new File("part2\\worm.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        CyclicBarrier newBarrier = new CyclicBarrier(swimmables.size());
        for (Swimmable swimmable : this.swimmables) {
            swimmable.setBarrier(newBarrier);
        }

        for (Swimmable swimmable : this.swimmables) {
            if (swimmable.getBarrier()==null)
                callback();
        }

        // for (Swimmable swimmable : this.swimmables) {
        //     if(swimmable.getFoodrace()==null)
        //     {
        //         for (Swimmable s : this.swimmables) {
        //             s.Foodrace(null);
        //         }
        // }



        // }
        

      }

    /** 
     * reset the CyclicBarrier
     */
      public void callback ()
      {
        for (Swimmable swimmable : this.swimmables) {
            swimmable.setBarrier(null);
      }
    }
 
     /** 
     * create and Show info table
     */     
     public void Info () {
        if(isTable2Visible == true) {
            this.Wakeup();
            scrollPane.setVisible(false);
            isTable2Visible = false;
            
        }
        if(isTableVisible == false) {
               this.Sleep();
               
               int i=0;
               String[] columnNames = {"Animal", "Color", "Size", "Hor. speed", "Ver. speed","Eat counter"};
               Object [][] data = new String[5][columnNames.length];
               for (Swimmable s : swimmables) {
                    data[i][0] = "" + s.getAnimalName();
                    data[i][1] = "" + s.getColor();
                    data[i][2] = "" + s.getSize();
                    data[i][3] = "" + s.gethorSpeed();
                    data[i][4] = "" + s.getverSpeed();
                    data[i][5] = "" + s.getEatCount();
                    i++;                  
                    }
               JTable table = new JTable(data, columnNames);
               scrollPane = new JScrollPane(table);
               scrollPane.setSize(450,table.getRowHeight()*(5)+24);
               add( scrollPane, BorderLayout.CENTER );
               isTableVisible = true;
               
        }
        else{
            isTableVisible = false;
            this.Wakeup();
        }
        
        scrollPane.setVisible(isTableVisible);
        repaint();
        
     }
     
    /** 
     * close the threads and exit
     */    
    public void Exit() {
        this.Close();
        System.exit(0);
    }
 
    
    
    
    /** 
     * Perform the action selected (AddAnimal,Sleep,Wakeup,Reset,food,Info,Exit)
     * @param e
     */
    public void actionPerformed(ActionEvent e) {
     if(e.getSource() == b_num[0]) 
        AddAnimal();
     else if(e.getSource() == b_num[1]) 
        Sleep();
     else if(e.getSource() == b_num[2])  
        Wakeup();
     else if(e.getSource() == b_num[3])  
        Reset(); 
     else if(e.getSource() == b_num[4])
        food();
     else if(e.getSource() == b_num[5])  
        Info();
     else if(e.getSource() == b_num[6])  
        Exit();
    }
 
    
}

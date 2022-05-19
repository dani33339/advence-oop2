package part2;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import q3.*;
import part3.*;

/**
 * class AquaPanel:
 * JPanel of aquarium
 * 
 * @author Daniel Markov ,Anton Volkov 
 */
public class AquaPanel extends JPanel implements ActionListener, Swimmable.Callback{
    private AquaFrame frame;
    private JPanel p1;
    private JButton[] b_num;
    private String[] names = {"Add Animal","Add Plant","Animal Duplicate","Sleep","Wake up","reset","Food","Info","Exit"};
    private JScrollPane scrollPane;
    private boolean isTableVisible = false;
    private boolean isTable2Visible = false;
    private HashSet<Swimmable> swimmables = new HashSet<Swimmable>();
    private HashSet<Immobile> immobiles = new HashSet<Immobile>();
    private boolean BackgroundeImageStatus=false;
    public ExecutorService executorService = Executors.newFixedThreadPool(5);
    public CyclicBarrier Barrier=null;
     
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
    * return size of Immobiles
    * @return int
    */
    public int getImmobilesize(){return immobiles.size();}

    
    /** 
     * add swimmable to the hashset
     * @param s
     */
    public void addswimmables(Swimmable s){
        executorService.execute(s);
        this.swimmables.add(s);
        s.setAnimalid(swimmables.size());  
    }

    /** 
     * add Immobile to the hashset
     * @param plant
     */
    public void addimmobiles(Immobile plant ){
        
        this.immobiles.add(plant);
        
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
            img = ImageIO.read(new File ("part2//aquarium.jpg"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Image dimg = img.getScaledInstance(800, 600, Image.SCALE_DEFAULT);
        g.drawImage(dimg, 0, 0, null);
        }

        for (Swimmable s : swimmables){
            s.drawCreature(g);
        }
        for (Immobile plant : immobiles){
            plant.drawCreature(g);
        }
        Singleton.getInstance().draw(g);
        repaint();
    }
    
        
    /** 
     * create a AddAnimalDialog
     */
    public void AddAnimal(){
        AddAnimalDialog dial = new AddAnimalDialog(frame,this,"Add Animal");
        dial.setVisible(true);
    }

    public void AddPlant(){
        AddPlanetDialog dial = new AddPlanetDialog(frame,this,"Add Plant");
        dial.setVisible(true);
    }


    public void DuplicateAnimal(){
        AddDuplicateAnimal dial = new AddDuplicateAnimal(frame,this,"Duplicate Animal",scrollPane);
        dial.setVisible(true);
    }

    /** 
     * stop the threads
     */
    public void Sleep() {
        AquaFrame.STATE = "sleeping"; 
    }
    
     /** 
     * wake up the threads
     */
     public void Wakeup() {
        if (AquaFrame.STATE == "sleeping")
        {
            AquaFrame.STATE = "swiming";
            for (Swimmable swimmable : this.swimmables) {
                synchronized(swimmable) {
                    swimmable.notify();
                }
            }
        }
    }
 
     /** 
     * reset the panel from swimibles
     */
      public void Reset () {
        //stop all threads.
        this.swimmables.clear(); //delete the swimmables
        executorService.shutdown();
        executorService = Executors.newFixedThreadPool(5);
        Barrier=null;
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
        if (Barrier==null && this.getswimmablessize()>0)
        {
            Singleton.getInstance().setVisible(true);//set worm to be unvisible
            Barrier=new CyclicBarrier(swimmables.size());
            for (Swimmable swimmable : this.swimmables) {
                swimmable.setBarrier(Barrier);
            }
        }
    }


    
    /** 
     * lift's the Barrier and gives the food to the fish
     * @param s
     */
    public void DisableBarrire (Swimmable s)
    {
        s.eatInc();
        Barrier=null;
        for (Swimmable i : swimmables) 
        {
            i.setBarrier(null);
        }
        Singleton.getInstance().setVisible(false);//set worm to be unvisible
    } 


    public JTable animmaltable()
    {
        int i=0;
        String[] columnNames = {"Animal", "Color", "Size", "Hor. speed", "Ver. speed","Eat counter"};
        Object [][] data = new String[5][columnNames.length];
        for (Swimmable s : swimmables) {
             data[i][0] = "" + s.getAnimalNameAndId();
             data[i][1] = "" + s.getColor();
             data[i][2] = "" + s.getSize();
             data[i][3] = "" + s.gethorSpeed();
             data[i][4] = "" + s.getverSpeed();
             data[i][5] = "" + s.getEatCount();
             i++;                  
             }
        JTable table = new JTable(data, columnNames);
        return table;
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
               
            //    int i=0;
            //    String[] columnNames = {"Animal", "Color", "Size", "Hor. speed", "Ver. speed","Eat counter"};
            //    Object [][] data = new String[5][columnNames.length];
            //    for (Swimmable s : swimmables) {
            //         data[i][0] = "" + s.getAnimalName();
            //         data[i][1] = "" + s.getColor();
            //         data[i][2] = "" + s.getSize();
            //         data[i][3] = "" + s.gethorSpeed();
            //         data[i][4] = "" + s.getverSpeed();
            //         data[i][5] = "" + s.getEatCount();
            //         i++;                  
            //         }
            //    JTable table = new JTable(data, columnNames);
            JTable table=animmaltable();
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
    * duplicateanimal make panel and display all the animal in the aquaframe
    *select an animal (select row) and the animal will clone
    */
    public void duplicateanimal(){

        if(this.getswimmablessize()<5) 
        {
            JFrame frame = new JFrame("Which Animal To Duplicate?");
            JTable info = animmaltable();

            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());

            JPanel apply_panel = new JPanel();
            JButton clone_select = new JButton("Select");
            clone_select.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if(e.getSource() == clone_select){
                        frame.dispose();
                        String name = info.getModel().getValueAt(info.getSelectedRow(),0).toString(); //get the name of animal from selected row
                        for(Swimmable s : swimmables){ //run over swimmables hashset
                            if(((Swimmable)s).getAnimalNameAndId().equalsIgnoreCase(name)){ //Comparison with name
                                if(s instanceof Fish){// check for fish object
                                    Swimmable clone = ((Fish)s).clone(); //clone
                                    addswimmables(clone); 
                                    break;
                                }
                        if(s instanceof Jellyfish){ 
                                Swimmable clone = ((Jellyfish)s).clone(); //clone
                                addswimmables(clone); 
                                break;
                            }

                            }
                        }
                        
                    }
                }
            });
            JButton clone_cancel = new JButton("Cancel");
            clone_cancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if(e.getSource() == clone_cancel){
                        frame.dispose();
                    }
                }
            });
            apply_panel.setLayout(new GridLayout());

            apply_panel.add(clone_cancel);
            apply_panel.add(clone_select);

            panel.add(info,BorderLayout.NORTH);
            panel.add(apply_panel, BorderLayout.SOUTH);
            frame.add(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        }
        else
        {
            JOptionPane.showMessageDialog(null,
                    "You have got the maximum amount of animals!",
                    "Error!",
                    JOptionPane.ERROR_MESSAGE);
        }
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
        AddPlant();
    else if(e.getSource() == b_num[2]) 
        duplicateanimal();
     else if(e.getSource() == b_num[3]) 
        Sleep();
     else if(e.getSource() == b_num[4])  
        Wakeup();
     else if(e.getSource() == b_num[5])  
        Reset(); 
     else if(e.getSource() == b_num[6])
        food();
     else if(e.getSource() == b_num[7])  
        Info();
     else if(e.getSource() == b_num[7])  
        Exit();
    }
 
}



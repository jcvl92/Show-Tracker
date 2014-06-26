import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.Serializable;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class JSPaint extends JFrame implements Serializable
{    
    private static final long serialVersionUID = -8787645153679803322L;
    private JFrame mainFrame;
    private JPanel bp;
    private JButton button;
    private DrawingArea da;

    public JSPaint()
    {
        setTitle("JS Paint");
        setSize(1024, 768);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Drawing area
        da = new DrawingArea();

        setLayout(new BorderLayout());

        // add the buttons to the panel
        buttonPanel();

        // Add the drawing area 
        add(bp, BorderLayout.SOUTH);
        bp.setBackground(Color.RED);
        add(da, BorderLayout.CENTER);
        da.setBackground(Color.BLUE);

        setVisible(true);        
    }

    // I put it here too just in case
    @Override
    public void update(Graphics g)
    {
        System.out.println("update in JSPaint called.");
        paint(g);
    }

   /*
    * Creates the panel for the buttons, creates the buttons and places them on
    * the panel
    */
    public void buttonPanel()
    {
        // Create the panel for the buttons to be placed in
        bp = new JPanel();

        saveButton = new JButton("Save");
        loadButton = new JButton("Load");
        //more buttons

        bp.add(saveButton);
        bp.add(loadButton);
        //more buttons

        // ActionListeners

        colorButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                System.out.println("color");
                da.color();
            }
        });            
    }

    public class DrawingArea extends JPanel
    {        
        private static final long serialVersionUID = -8299084743195098560L;
        boolean dragged = false;

        @Override
        public void update(Graphics g)
        {
            System.out.println("Update in DrawingArea called");
            paint(g);
        }

       /*
        * Draws the selected shape onto the screen and saves it into a Stack.
        *
        */
        public void draw()
        {
            this.addMouseMotionListener(new MouseMotionListener()
            {                
                public void mouseDragged(MouseEvent me)
                {
                    dragged = true;
                }

                public void mouseMoved(MouseEvent me) {}
            });

            //more listeners...
            });
        }

       /*
        * Draws the selected String onto the screen when the mouse is held down.
        *
        */
        public void brush()
        {
            this.addMouseMotionListener(new MouseMotionListener()
            {                
                public void mouseDragged(MouseEvent me)
                {
                    // If we are in drawing mode, draw the String. Create a new 
                    // Figure Object and push it onto the Stack
                    if(activeButton == "brush")
                    {
                        startPoint = me.getPoint();

                        Figure fig = new Figure("String", startPoint, null, currentColor);
//                        figures.push(calculate(fig));
                        toPaint.push(calculate(fig));
                        repaint();
                    }
                }

                public void mouseMoved(MouseEvent me) {}
            });           
        }

        // more of the same...

        public void paint(Graphics g)
        {                        
            toSave.addAll(toPaint);

            while(!toPaint.isEmpty())
            {
                Figure f = toPaint.pop();
                String t = f.type;

                if(f.color != null)
                {
                    g.setColor(f.color);
                }

                switch(t)
                {
                    case "Rectangle": g.drawRect(f.x1, f.y1, f.width, f.height);
                        break;
                    case "Oval": g.drawOval(f.x1, f.y1, f.width, f.height);
                        break;         
                    case "Line": g.drawLine(f.x1, f.y1, f.x2, f.y2);
                        break;
                    case "Clear": 
                        g.fillRect(0, 0, da.getWidth(), da.getHeight());
                        clearStack(toSave);
                        break;
                    case "String": g.drawString(f.toPrint, f.x1, f.y1);
                        break;
                }
            }
        }
    }

    private class Figure implements Serializable
    {
        private static final long serialVersionUID = 4690475365105752994L;
        String type, toPrint;
        Color color;
        Point start;
        Point end;
        int x1, y1, x2, y2, width, height;

        public Figure(String figureType, 
            Point startPoint, Point endPoint, Color figureColor)
        {
            type = figureType;
            color = figureColor;
            start = startPoint;
            end = endPoint;
        }

        // Rect, Oval
        public Figure(String figureType, int figureX, int figureY, 
            int figureWidth, int figureHeight, Color figureColor)
        {
            type = figureType;
            x1 = figureX;
            y1 = figureY;
            width = figureWidth;
            height = figureHeight;
            color = figureColor;
        }

        // more shapes
    }

    public static void main(String args[])
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                new JSPaint();
            }
        });
    }
}
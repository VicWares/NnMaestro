package nnmaestro20;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class View extends JComponent implements ActionListener, KeyListener
{
    private int width = (int) (Toolkit.getDefaultToolkit().getScreenSize().width);
    private int height = (int) (Toolkit.getDefaultToolkit().getScreenSize().height);
    private int column1xPos = (int) (width / 13 * 1); //width = 2560 on 30" screen
    private int column2xPos = (int) (width / 13 * 2);
    private int column3xPos = (int) (width / 13 * 3);
    private int column4xPos = (int) (width / 13 * 4);
    private int column45xPos = (int) (width / 13 * 4.5);
    private int column5xPos = (int) (width / 13 * 5);
    private int column6xPos = (int) (width / 13 * 6);
    private int column7xPos = (int) (width / 13 * 7);
    private int column8xPos = (int) (width / 13 * 8);
    private int column85xPos = (int) (width / 13 * 8.5);
    private int column9xPos = (int) (width / 13 * 9);
    private int column10xPos = (int) (width / 13 * 10);
    private int column11xPos = (int) (width / 13 * 11);
    private int column12xPos = (int) (width / 13 * 12);
    private int column13xPos = (int) (width / 13 * 13);
    private int row1yPos = height / 32;
    private int row2yPos = height / 15;
    private int row9yPos = (int) (height / 1.1);
    private int verticalSpacing = height / 300;
    private int numberOfInputPEs;
    private int numberOfHiddenPEs;
    private int numberOfOutputPEs;
    private ArrayList<PE> inputPElist;
    private ArrayList<PE> hiddenPElist;
    private ArrayList<PE> outputPElist;
    private ArrayList<Ellipse2D.Double> inputCirclesList = new ArrayList<Ellipse2D.Double>();
    private ArrayList<Ellipse2D.Double> hiddenCirclesList = new ArrayList<Ellipse2D.Double>();
    private ArrayList<Ellipse2D.Double> outputCirclesList = new ArrayList<Ellipse2D.Double>();
    private ArrayList<Line2D.Double> peLineList = new ArrayList<Line2D.Double>();
    private ArrayList<Long> rmsErrorPointsList = new ArrayList<Long>();
    private long epochCounter;
    private JFrame jf;
    private double rmsError;
    private long graphX;
    private double deltaRmsError;
    private int peCircleDiameter = height / 15;//allows for max of 7 PEs per layer showing...TODO:Random slection of visible PEs for large nets
    private int horizontalSpacing = width / 7;//allows for only three layers...input, hidden and output....TODO:Provide for more layers
    private int inputCircleXpos = horizontalSpacing;
    private int hiddenCircleXpos = horizontalSpacing * 3;
    private int outputCircleXpos = horizontalSpacing * 5;
    private int interLineVerticalSpacing = height / 150;
    private int inputFileLength;
    private DefaultTableModel model;
    private String[][] data;
    private String[] col;
    private DecimalFormatSymbols s = new DecimalFormatSymbols();
    private DecimalFormat f = new DecimalFormat("#,000", s);
    private boolean showTrialViewTable;
    private int[][] inputArray;
    private int[][] outputs;

    View(String version, int numberOfInputPEs, int numberOfHiddenPEs, final int numberOfOutputPEs, int inputFileLength)
    {
        s.setGroupingSeparator(',');
        this.inputFileLength = inputFileLength;
        this.numberOfInputPEs = numberOfInputPEs;
        this.numberOfHiddenPEs = numberOfHiddenPEs;
        this.numberOfOutputPEs = numberOfOutputPEs;
        outputs = new int[inputFileLength][numberOfOutputPEs];
        inputArray = new int[inputFileLength][numberOfInputPEs + numberOfOutputPEs];//Each row holds the complete set of inputs and desired outputs
        col = new String[numberOfInputPEs + numberOfOutputPEs];//Set up Headers for trial view table
        data = new String[inputFileLength][numberOfInputPEs + numberOfOutputPEs];//Set up Headers for trial view table
        rmsErrorPointsList.add(0L);//initial x value for RMS error graph
        rmsErrorPointsList.add(100L);//initial y value for RMS error graph
        jf = new JFrame("NN Maestro Version " + version);
        jf.add(this);//Add graphics
        jf.setSize(width, height);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setBackground(Color.black);
        jf.setVisible(true);
    }

    public void setUpTrialViewTable()
    {
        JFrame frame = new JFrame("Trial View");
        JPanel panel = new JPanel();
        model = new DefaultTableModel(data, col);
        JTable table = new JTable(model);
        JTableHeader header = table.getTableHeader();
        header.setBackground(Color.yellow);
        JScrollPane pane = new JScrollPane(table);
        panel.add(pane);
        frame.add(panel);
        frame.pack();
        if (showTrialViewTable)
        {
            frame.setVisible(true);
        }
    }

    public void setUpCircles()
    {
        for (int i = 0; i < numberOfInputPEs; i++)
        {
            inputCirclesList.add(new Ellipse2D.Double(inputCircleXpos, peCircleDiameter * (2 * i + 1), peCircleDiameter, peCircleDiameter));
        }
        for (int i = 0; i < numberOfHiddenPEs; i++)
        {
            hiddenCirclesList.add(new Ellipse2D.Double(hiddenCircleXpos, peCircleDiameter * (2 * i + 1), peCircleDiameter, peCircleDiameter));
        }
        for (int i = 0; i < numberOfOutputPEs; i++)
        {
            outputCirclesList.add(new Ellipse2D.Double(outputCircleXpos, peCircleDiameter * (2 * i + 1), peCircleDiameter, peCircleDiameter));
        }
        for (int i = 0; i < numberOfInputPEs; i++)//Make inter PE lines between input and hidden PEs
        {
            Ellipse2D.Double thisInputCircle = inputCirclesList.get(i);
            int thisInputCircleCenterX = (int) thisInputCircle.getCenterX();
            int thisInputCircleCenterY = (int) thisInputCircle.getCenterY();
            for (int j = 0; j < numberOfHiddenPEs; j++)
            {
                Ellipse2D.Double thisHiddenCircle = hiddenCirclesList.get(j);
                int thisHiddenCircleCenterX = (int) thisHiddenCircle.getCenterX();
                int thisHiddenCircleCenterY = (int) thisHiddenCircle.getCenterY();
                peLineList.add(new Line2D.Double(thisHiddenCircleCenterX, thisHiddenCircleCenterY, thisHiddenCircleCenterX, thisHiddenCircleCenterY - verticalSpacing));//Bias input line
                peLineList.add(new Line2D.Double(column1xPos, (int) thisInputCircle.getCenterY(), (int) thisInputCircle.getCenterX(), (int) thisInputCircle.getCenterY()));//InputLine
                peLineList.add(new Line2D.Double(thisInputCircleCenterX, thisInputCircleCenterY, thisHiddenCircleCenterX, thisHiddenCircleCenterY));//PE interconnect line
            }
        }
        for (int j = 0; j < numberOfHiddenPEs; j++)//Make inter PE lines between hidden and output PEs
        {
            Ellipse2D.Double thisHiddenCircle = hiddenCirclesList.get(j);
            int thisHiddenCircleCenterX = (int) thisHiddenCircle.getCenterX();
            int thisHiddenCircleCenterY = (int) thisHiddenCircle.getCenterY();
            for (Ellipse2D.Double thisOutputCircle : outputCirclesList)
            {
                int thisOutputCircleCenterX = (int) thisOutputCircle.getCenterX();
                int thisOutputCircleCenterY = (int) thisOutputCircle.getCenterY();
                peLineList.add(new Line2D.Double(thisOutputCircleCenterX, thisOutputCircleCenterY, thisOutputCircleCenterX, thisOutputCircleCenterY - verticalSpacing));//Bias input line
                peLineList.add(new Line2D.Double(thisHiddenCircleCenterX, thisHiddenCircleCenterY, thisOutputCircleCenterX, thisOutputCircleCenterY));
                peLineList.add(new Line2D.Double(thisOutputCircleCenterX, thisOutputCircleCenterY, thisOutputCircleCenterX + horizontalSpacing, thisOutputCircleCenterY));
            }
        }
    }

    @Override
    public void paint(Graphics g)
    {
        Graphics2D g2 = (Graphics2D) g;
        g2.setBackground(Color.white);
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(Color.green);
        for (int i = 0; i < height / 10; i++)//Draw RMS errorlegend ticks and scale
        {
            g2.drawLine(0, i * height / 4, width / 200, i * height / 4);
            g2.drawString("" + (Math.pow(10, -i)), 25, i * height / 4);
        }
        g2.setColor(Color.magenta);
        g2.setFont(new Font("Bank Gothic", Font.PLAIN, 10));
        if (deltaRmsError > 0)
        {
            g2.setColor(Color.red);

        } else
        {
            g2.setColor(Color.GREEN);
        }
        g2.setColor(Color.magenta);
        g2.setFont(new Font("Bank Gothic", Font.BOLD, 22));
        g2.drawString("RMS error", 0, height / 70);
        g2.drawString("" + rmsError, column1xPos, height / 70);//RMS error
        g2.drawString("Epoch", 0, row9yPos);
        g2.drawString("" + f.format(epochCounter), column1xPos, row9yPos);
        g2.setFont(new Font("Bank Gothic", Font.PLAIN, 10));
        g2.setColor(Color.cyan);
        g2.drawString("Input Value", column1xPos, height / 20);
        g2.drawString("Output Value", column3xPos, height / 20);
        g2.drawString("Input Value", column5xPos, height / 20);
        g2.drawString("Weight", column6xPos, height / 20);
        g2.drawString("Output Value", column7xPos, height / 20);
        g2.drawString("Input Value", column85xPos, height / 20);
        g2.drawString("Weight", column10xPos, height / 20);
        g2.drawString("Output Value", column11xPos, height / 20);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .3f));
        g2.setColor(Color.LIGHT_GRAY);
        g2.setStroke(new BasicStroke(1f));
        for (Line2D.Double peLine : peLineList)//Draw all lines
        {
            g2.draw(peLine);
        }
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

        /*************************************************************************************
         * Draw input PE circles and input PE inputs
         *************************************************************************************/
        for (int i = 0; i < inputPElist.size(); i++)
        {
            Ellipse2D.Double thisInputCircle = inputCirclesList.get(i);
            g2.setStroke(new BasicStroke(0.1f));
            g2.setColor(Color.lightGray);
            g2.setStroke(new BasicStroke(5.0f));
            g2.setColor(Color.green);
            g2.draw(thisInputCircle);
            g2.setColor(Color.black);
            g2.fill(thisInputCircle);
            g2.setColor(Color.green);
            g2.drawString(inputCirclesList.indexOf(thisInputCircle) + "", (int) thisInputCircle.getCenterX(), (int) thisInputCircle.getCenterY());//draw PE number
            g2.setColor(Color.yellow);
            g2.drawString(inputPElist.get(i).getInputValueList().get(0) + "", column1xPos, (int) inputCirclesList.get(i).y);//input PEs only have one input value
            g2.drawString(inputPElist.get(i).getOutputValue() + "", column3xPos, (int) inputCirclesList.get(i).y);
        }
        drawHiddenPEcirclesInputsWeightsAndOutputs(g2);
        drawOutputPEcirclesInputsWeightsAndOutputs(g2);
        drawRMSerrorGraph(g2);
    }

    public void drawHiddenPEcirclesInputsWeightsAndOutputs(Graphics2D g2)
    {
        for (int i = 0; i < numberOfHiddenPEs; i++)
        {
            PE thisHiddenPE = hiddenPElist.get(i);
            Ellipse2D.Double thisHiddenPEcircle = hiddenCirclesList.get(i);
            int thisHiddenPEcircleXpos = (int) thisHiddenPEcircle.x;
            int thisHiddenPEcircleYpos = (int) thisHiddenPEcircle.y;
            g2.setStroke(new BasicStroke(5.0f));
            g2.setColor(Color.green);
            g2.draw(thisHiddenPEcircle);
            g2.setColor(Color.black);
            g2.fill(thisHiddenPEcircle);
            g2.setColor(Color.green);
            g2.drawString(i + "", (int) thisHiddenPEcircle.getCenterX(), (int) thisHiddenPEcircle.getCenterY()); //draw PE number
            g2.setColor(Color.yellow);
            g2.drawString(thisHiddenPE.getOutputValue() + "", column7xPos, thisHiddenPEcircleYpos); //One output value per PE
            g2.setColor(Color.magenta);
            g2.drawString("Bias Input-->", column45xPos, thisHiddenPEcircleYpos);//Hidden PE Bias input label
            g2.setColor(Color.yellow);
            for (int j = 0; j < numberOfInputPEs + 1; j++)//Extra input is bias input...always 1.0
            {
                g2.drawString("" + thisHiddenPE.getInputValueList().get(j), column5xPos, thisHiddenPEcircleYpos + j * interLineVerticalSpacing);//Hidden PE input values
                g2.drawString("" + thisHiddenPE.getWeightList().get(j), column6xPos, thisHiddenPEcircleYpos + j * interLineVerticalSpacing);//Hidden PE weights
            }
        }
    }

    public void drawOutputPEcirclesInputsWeightsAndOutputs(Graphics2D g2)
    {
        for (int i = 0; i < numberOfOutputPEs; i++)
        {
            PE thisOutputPE = outputPElist.get(i);
            Ellipse2D.Double thisOutputPEcircle = outputCirclesList.get(i);
            int thisOutputPEcircleXpos = (int) thisOutputPEcircle.x;
            int thisOutputPEcircleYpos = (int) thisOutputPEcircle.y;
            float thisOutputPEcircleWidth = (float) thisOutputPEcircle.width;
            int thisOutputPEcircleCenterY = (int) thisOutputPEcircle.getCenterY();
            g2.setStroke(new BasicStroke(5.0f));
            g2.setColor(Color.green);
            g2.draw(thisOutputPEcircle);
            g2.setColor(Color.black);
            g2.fill(thisOutputPEcircle);
            g2.setColor(Color.green);
            g2.drawString(i + "", (int) thisOutputPEcircle.getCenterX(), (int) thisOutputPEcircle.getCenterY()); //draw PE number
            g2.setColor(Color.yellow);
            g2.drawString(thisOutputPE.getOutputValue() + "", column11xPos, thisOutputPEcircleYpos); //One output value per PE
            g2.setColor(Color.magenta);
            g2.drawString("Bias Input-->", column8xPos, thisOutputPEcircleYpos);//Output PE Bias input label
            g2.setColor(Color.yellow);
            for (int j = 0; j < numberOfHiddenPEs + 1; j++)//Extra input is bias input...always 1.0
            {
                g2.drawString("" + thisOutputPE.getInputValueList().get(j), column85xPos, thisOutputPEcircleYpos + j * interLineVerticalSpacing);//Output PE input values
                g2.drawString("" + thisOutputPE.getWeightList().get(j), column10xPos, thisOutputPEcircleYpos + j * interLineVerticalSpacing);//Output PE weights
            }
        }
    }

    public void drawRMSerrorGraph(Graphics2D g2)
    {
        /***********************************************
         * Draw RMS error graph
         ***********************************************/
        g2.setStroke(new BasicStroke(.0001f));
        graphX = (width * epochCounter / 20000000);
        rmsErrorPointsList.add(graphX);//x position for line
        rmsErrorPointsList.add((long) (height * (-Math.log10(rmsError) / 3.5)));//log y position for line
        for (int i = 0; i < rmsErrorPointsList.size() - 2; i += 2)
        {
            long x1 = rmsErrorPointsList.get(i);
            long y1 = rmsErrorPointsList.get(i + 1);
            long x2 = rmsErrorPointsList.get(i + 2);
            long y2 = rmsErrorPointsList.get(i + 3);
            g2.setColor(Color.GREEN);
            g2.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
        }
    }

    public JFrame getJf()
    {
        return jf;
    }

    public void setRmsError(double rmsError)
    {
        this.rmsError = rmsError;
    }

    public void setEpochCounter(long epochCounter)
    {
        this.epochCounter = epochCounter;
    }

    public void setPeLineList(ArrayList<Line2D.Double> peLineList)
    {
        this.peLineList = peLineList;
    }

    @Override
    public void keyTyped(KeyEvent e)
    {
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
        if (e.getKeyCode() == 32)//space bar sets up trial view table
        {
            showTrialViewTable = true;
            fillTrialTable();
            setUpTrialViewTable();//Sets up and turns on JFrame with trial view pane
            showTrialViewTable = false;
        }
    }

    private void fillTrialTable()
    {
        for (int i = 0; i < inputFileLength; i++) //Set up trial view table from input array
        {
            for (int j = 0; j < numberOfInputPEs; j++)
            {
                data[i][j] = "" + inputArray[i][j]; //put input values into trial view table
            }
            for (int j = 0; j < numberOfOutputPEs; j++)
            {
                outputs[i][j] = (int) Math.round(outputPElist.get(j).getOutputValue());
                data[i][j + numberOfInputPEs] = "" + outputs[i][j]; //put out values into trial view table
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent ae)
    {
        repaint();
    }

    public void setInputPElist(ArrayList<PE> inputPElist)
    {
        this.inputPElist = inputPElist;
    }

    public void setHiddenPElist(ArrayList<PE> hiddenPElist)
    {
        this.hiddenPElist = hiddenPElist;
    }

    public void setOutputPElist(ArrayList<PE> outputPElist)
    {
        this.outputPElist = outputPElist;
    }

    public void setData(String[][] data)
    {
        this.setData(data);
    }

    public String[][] getData()
    {
        return data;
    }

    public void setInputArray(int[][] inputArray)
    {
        this.inputArray = inputArray;
    }

    public void setOutputs(int[][] outputs)
    {
        this.outputs = outputs;
    }

    public boolean isShowTrialViewTable()
    {
        return showTrialViewTable;
    }

    void modifyData(int i, int j, double d)
    {
        data[i][numberOfInputPEs + j] = Integer.toString((int) Math.round(d));
    }
}
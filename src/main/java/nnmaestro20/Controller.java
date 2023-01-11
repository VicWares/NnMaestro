package nnmaestro20;
/*************************************************************************
 * NNmaestro20 Version220301
 * Copyright Vic Wintriss, Ryan Kemper, Sean Kemper and Duane DeSieno 2011
 * All rights reserved
 * ************************************************************************/
import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
public class Controller
{
    private String version = "230111";
    private int inputFileLength;//Set automatically by reading length of input file
    private int inputLineLength;
    private FileReader fileReader;
    private BufferedReader bufferedReader;
    private int[][] inputArray;
    private BufferedReader input;
    private File file = new File("/Users/vicwintriss/Desktop/SevenSegmentCodes.csv");
    public View view;
    private Timer paintTicker;
    private int numberOfInputPEs;//Set automatically by reading first line of input file
    private int numberOfHiddenPEs = 7;
    private int numberOfOutputPEs;//Set automatically by reading first line of input file
    private ArrayList<PE> inputPElist;
    private ArrayList<PE> hiddenPElist;
    private ArrayList<PE> outputPElist;
    private long epochCounter;
    private double rmsError = 1;
    private double learningRate = 0.02;
    private JFileChooser fileChooser;
    private String[][] data;
    private int[][] outputs;
    private int inputPEnumber;
    public Controller controller;
    public HashMap<String,String> testMap = new HashMap<>();
    public static void main(String[] args)
    {
        new Controller().getGoing();
    }
    private void getGoing()
    {
        controller = new Controller();
        /***********************************************************************
         * Check to see how many lines in input file and how many items per line
         ***********************************************************************/
        determineLengthOfInputFile();
        determineNumberOfInputAndOutputPEs();
        /***********************************************************************
         * Instantiate necessary Classes
         ***********************************************************************/
        data = new String[inputFileLength][numberOfOutputPEs];
        outputs = new int[inputFileLength][numberOfOutputPEs];
        inputArray = new int[inputFileLength][numberOfInputPEs + numberOfOutputPEs];//Each row holds the complete set of inputs and desired outputs
        inputPElist = new ArrayList<PE>();
        hiddenPElist = new ArrayList<PE>();
        outputPElist = new ArrayList<PE>();
        view = new View(version, numberOfInputPEs, numberOfHiddenPEs, numberOfOutputPEs, inputFileLength);
        view.addKeyListener(view);
        view.setInputArray(inputArray);
        view.setOutputs(outputs);
        paintTicker = new Timer(500, view);//Sets painter refresh rate
        view.setInputPElist(inputPElist);
        view.setHiddenPElist(hiddenPElist);
        view.setOutputPElist(outputPElist);
        view.setUpCircles();
        view.getJf().addKeyListener(view);
        initializeInputPEs();
        initializeHiddenPEs();
        initializeOutputPEs();
        readInputFile();//Reads training file into an array so that we don't have to keep reading file
        JOptionPane.showMessageDialog(null, "Hi:\n\nI found " + numberOfInputPEs + " inputs, and " + numberOfOutputPEs + " outputs in your training file:\n\n<" + file.getName() + ">\n\nwhich is " + inputFileLength + " lines long.\n\nPress the space bar when you want to check the results of this run.\n\nThanks,\n\nNNmaestro" + version);
        paintTicker.start();
        /************************************************
         * Main training loop 
         ***********************************************/
        while (true)
        {
            double lastRMSErr = 0.0;
            for (int i = 0; i < inputFileLength; i++)
            {
                loadPEsWithTrainingFileData(i);
                generateInputLayerOutputs();
                populateHiddenLayerInputList();
                generateHiddenLayerOutputs();
                populateOutputLayerInputList();
                double[] outputs = generateOutputLayerOutputsAndErrors();
                for (int j = 0; j < outputs.length; j++)
                {
                    view.modifyData(i, j, outputs[j]);
                }
                backProp();
                lastRMSErr = computeRMSerror();
            }
            view.setEpochCounter(epochCounter++);
            view.setRmsError(lastRMSErr);
        }
    }
    public void loadPEsWithTrainingFileData(int i)
    {
        /********************************************************************
         * Set input PE inputs and output PE desired outputs from input array
         ********************************************************************/
        for (int k = 0; k < numberOfInputPEs; k++)
        {
            inputPElist.get(k).getInputValueList().set(0, (double) inputArray[i][k]);
        }
        for (int k = 0; k < numberOfOutputPEs; k++)
        {
            outputPElist.get(k).setDesiredTrainingOutputValue((double) inputArray[i][k + numberOfInputPEs]);
        }
    }
    public int determineLengthOfInputFile()
    {
        String inputLine = "";
        try
        {
            fileReader = new FileReader(file);
            bufferedReader = new BufferedReader(fileReader);
            inputLineLength = bufferedReader.readLine().length();
            while ((inputLine = bufferedReader.readLine()) != null)
            {
                inputFileLength++;
            }
            inputFileLength++;
            fileReader.close();
        }
        catch (IOException ex)
        {
            System.out.println("Copntroller133 hiccup determining input file length");
        }
        return inputFileLength;
    }
    public void determineNumberOfInputAndOutputPEs()//Sets number of hidden PEs to the same as the number of output PEs
    {
        int inputPEnumber = 0;
        int outputPEnumber = 0;
        String s = "";
        String sChar = "";
        boolean breakMarker = false;
        try
        {
            fileReader = new FileReader(file);
            bufferedReader = new BufferedReader(fileReader);
            s = bufferedReader.readLine();
            for (int j = 0; j < inputLineLength; j++)//scan entire input line
            {
                sChar = Character.toString(s.charAt(j));
                if (sChar.equals("#"))
                {
                    breakMarker = true;
                    continue;
                }

                if ((sChar.equals("0") || sChar.equals("1")) && !breakMarker)//Reading only 0 or 1 inputs
                {
                    inputPEnumber++;
                }
                else
                {
                    if (sChar.equals("0") || sChar.equals("1"))// Reading only 0 or 1 outputs
                    {
                        outputPEnumber++;
                    }
                }
            }
            fileReader.close();
        }
        catch (IOException ex)
        {
            System.out.println("Controller178 Hiccup determining Number of Inputs and Outputs");
        }
        numberOfInputPEs = inputPEnumber;
        numberOfOutputPEs = outputPEnumber;
    }
    public int initializeInputPEs()
    {
        for (int i = 0; i < numberOfInputPEs; i++)//Add input PEs and input circles and populate input value list with dummy values
        {
            inputPEnumber = i + 1;
            inputPElist.add(new PE());
            inputPElist.get(i).getInputValueList().add(1.0);//Dummy input...to be replaced by training file read
        }
        return inputPEnumber;
    }
    public int initializeHiddenPEs()
    {
        int hiddenPEnumber = 0;
        for (int i = 0; i < numberOfHiddenPEs; i++)//Add hidden PEs and hidden circles and create input value list space and add initial weights
        {
            hiddenPEnumber = i + 1;
            hiddenPElist.add(new PE());
            PE thisHiddenPE = hiddenPElist.get(i);
            for (int j = 0; j < numberOfInputPEs + 1; j++)//+1 to account for bias input
            {
                thisHiddenPE.getInputValueList().add(1.0);//Make space in hidden PE input value list...1.0 for bias input
                thisHiddenPE.getWeightList().add((Math.random() * 2) - 1);//Initialize random weights
            }
        }
        return hiddenPEnumber;
    }
    public void initializeOutputPEs()
    {
        for (int i = 0; i < numberOfOutputPEs; i++)//Add output PEs and output circles and create space in output PE input value list and add initial weights
        {
            outputPElist.add(new PE());
            PE thisOutputPE = outputPElist.get(i);
            thisOutputPE.setDesiredTrainingOutputValue(1.0);//Dummy input...to be replaced by training file read
            for (int j = 0; j < numberOfHiddenPEs + 1; j++)//+1 to account for bias input
            {
                thisOutputPE.getInputValueList().add(1.0);//Make space in output PE input value list...1.0 for bias input
                thisOutputPE.getWeightList().add((Math.random() * 2) - 1);//Initialize weights
            }
        }
    }
    public void readInputFile()//Puts input file into input array
    {
        String inputLine = "";
        int value = 0;
        String digit = "";
        int lineCount = 0;
        ArrayList<String> input = new ArrayList<>();
        ArrayList<String> output = new ArrayList<>();
        try
        {
            fileReader = new FileReader(file);
            bufferedReader = new BufferedReader(fileReader);
            ArrayList<String> trainingSet = new ArrayList<>();
            while ((inputLine = bufferedReader.readLine()) != null)
            {
                int charCount = 0;
                for (int i = 0; i < inputLine.length(); i++)
                {
                    digit = inputLine.substring(i, i + 1);
                    if (!(digit.contains("#") || digit.contains(",")))
                    {
                        value = Integer.parseInt(digit);
                        inputArray[lineCount][charCount] = value;
                        charCount++;
                    }
                }
                lineCount++;
            }
            bufferedReader.close();
        }
        catch (IOException ex)
        {
            System.out.println("Controller254 Hiccup reading file");
        }
    }
    private void generateInputLayerOutputs()
    {
        for (int i = 0; i < numberOfInputPEs; i++)//Transfer input straight to PE output value
        {
            PE thisInputPE = inputPElist.get(i);
            thisInputPE.setOutputValue(thisInputPE.getInputValueList().get(0));
        }
    }
    private void populateHiddenLayerInputList()
    {
        PE thisHiddenPE;
        for (int i = 0; i < numberOfHiddenPEs; i++)
        {
            thisHiddenPE = hiddenPElist.get(i);
            thisHiddenPE.getInputValueList().set(0, 1.0);//Bias input
            for (int j = 0; j < numberOfInputPEs; j++)//The rest of the inputs
            {
                PE thisInputPE = inputPElist.get(j);
                thisHiddenPE.getInputValueList().set(j + 1, thisInputPE.getOutputValue());//because 0 item is bias
            }
        }
    }
    public void generateHiddenLayerOutputs()
    {
        for (int i = 0; i < numberOfHiddenPEs; i++)
        {
            PE thisHiddenPE = hiddenPElist.get(i);
            double output = thisHiddenPE.processInputs();
        }
    }
    private void populateOutputLayerInputList()
    {
        for (int i = 0; i < numberOfOutputPEs; i++)
        {
            PE thisOutputPE = outputPElist.get(i);
            thisOutputPE.getInputValueList().set(0, 1.0);//Bias input
            for (int j = 0; j < numberOfHiddenPEs; j++)
            {
                PE thisHiddenPE = hiddenPElist.get(j);
                thisOutputPE.getInputValueList().set(j + 1, thisHiddenPE.getOutputValue());
            }
        }
    }
    public double[] generateOutputLayerOutputsAndErrors()
    {
        double[] generatedOutputs = new double[numberOfOutputPEs];
        for (int i = 0; i < numberOfOutputPEs; i++)
        {
            PE thisOutputPE = outputPElist.get(i);
            thisOutputPE.processInputs();
            thisOutputPE.setOutputError(thisOutputPE.getOutputValue() - thisOutputPE.getDesiredTrainingOutputValue());
            generatedOutputs[i] = thisOutputPE.getOutputValue();
        }
        return generatedOutputs;
    }
    private double computeRMSerror()
    {
        double thisOutputPEerror = 0;
        double totalError = 0;
        for (int i = 0; i < outputPElist.size(); i++)
        {
            PE thisOutputPE = outputPElist.get(i);
            thisOutputPEerror = thisOutputPE.getOutputValue() - thisOutputPE.getDesiredTrainingOutputValue();
            totalError += Math.pow(thisOutputPEerror, 2.0);
        }
        rmsError = Math.sqrt(totalError / outputPElist.size());
        return rmsError;
    }
    private void backProp()
    {
        /**
         * ******************************************
         * Set output layer back prop bias weight 
         * ******************************************
         */
        for (int i = 0; i < outputPElist.size(); i++)
        {
            PE thisOutputPE = outputPElist.get(i);
            double thisPEoutputValue = thisOutputPE.getOutputValue();
            double thisPEoutputError = thisOutputPE.getOutputError();
            double squashingFactor = thisPEoutputValue * (1.0 - thisPEoutputValue);
            double oldBiasWeight = thisOutputPE.getWeightList().get(0);//Bias weight is first in list
            double deltaWeight = learningRate * squashingFactor * thisPEoutputError;
            double newBiasWeight = oldBiasWeight - deltaWeight;
            thisOutputPE.getWeightList().set(0, newBiasWeight);//Bias weight is first in list
            /**
             * ******************************************
             * Set output layer back prop weights 
             ******************************************
             */
            for (int j = 0; j < numberOfHiddenPEs; j++)
            {
                double thisInputValue = thisOutputPE.getInputValueList().get(j + 1);//Offset by one because first input is bias input (1)
                double oldWeight = thisOutputPE.getWeightList().get(j + 1);//Considering bias weight is first in list
                double newWeight = oldWeight - deltaWeight * thisInputValue;
                thisOutputPE.getWeightList().set(j + 1, newWeight);
                double iErr = squashingFactor * thisPEoutputError * oldWeight;//computing backprop input error
                hiddenPElist.get(j).setHerrSum(hiddenPElist.get(j).getHerrSum() + iErr);//putting hidden errors into hiddenPE
            }
        }

        /**
         * **************************************************
         * Set hidden layer back prop bias weight and weights 
         **************************************************
         */
        for (int i = 0; i < hiddenPElist.size(); i++)
        {
            PE thisHiddenPE = hiddenPElist.get(i);
            double thisOutputValue = thisHiddenPE.getOutputValue();
            double squashingFactor = thisOutputValue * (1.0 - thisOutputValue);
            double oldBiasWeight = thisHiddenPE.getWeightList().get(0);
            double deltaWeight = learningRate * squashingFactor * thisHiddenPE.getHerrSum();
            double newBiasWeight = oldBiasWeight - deltaWeight;
            thisHiddenPE.getWeightList().set(0, newBiasWeight);
            for (int j = 0; j < inputPElist.size(); j++)
            {
                double oldWeight = thisHiddenPE.getWeightList().get(j + 1);//Skip bias weight
                double thisInputValue = thisHiddenPE.getInputValueList().get(j + 1);
                thisHiddenPE.getWeightList().set(j + 1, oldWeight - deltaWeight * thisInputValue);//Skip bias input
            }
            thisHiddenPE.setHerrSum(0.0);
        }
    }
}

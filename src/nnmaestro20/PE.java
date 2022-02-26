package nnmaestro20;
/**
 * ***************************************************************
 * NNmaestro20 Version220226
 * No changes from 20.4
 * Copyright Vic Wintriss, Ryan Kemper, Sean Kemper and Duane DeSieno 2011
 * All rights reserved
 *************************************************************************
 */

import java.io.Serializable;
import java.util.ArrayList;

public class PE
{
    private ArrayList<Double> weightValueList;
    private ArrayList<Double> inputValueList;
    private double outputValue;
    private double desiredTrainingOutputValue;
    private double outputError;
    private double hErrSum;
    private double weightedInput;
    private int peNumber;

    public PE()
    {
        weightValueList = new ArrayList<Double>();
        inputValueList = new ArrayList<Double>();
    }

    public double processInputs()
    {
        weightedInput = 0;
        for (int i = 0; i < inputValueList.size(); i++)
        {
            weightedInput += (inputValueList.get(i) * weightValueList.get(i));
        }
        outputValue = sigmoid(weightedInput);
        return outputValue;
    }

    private double sigmoid(double input)
    {
        double ex = Math.exp(-input);
        double output = 1 / (1 + ex);
        return output;
    }

    public double getOutputValue()
    {
        return outputValue;
    }

    public ArrayList<Double> getWeightList()
    {
        return getWeightValueList();
    }

    public void addPEweight(double weight)
    {
        this.getWeightValueList().add(weight);
    }

    public double getWeight(int weightNumber)
    {
        return getWeightValueList().get(weightNumber);
    }

    public void setInputValue(int index, double inputValue)
    {
        inputValueList.set(index, inputValue);
    }

    public void addInputValue(double inputValue)
    {
        inputValueList.add(inputValue);
    }

    public ArrayList<Double> getInputValueList() {return inputValueList;}

    public void setDesiredTrainingOutputValue(double desiredTrainingOutputValue)
    {
        this.desiredTrainingOutputValue = desiredTrainingOutputValue;
    }

    public double getDesiredTrainingOutputValue()
    {
        return desiredTrainingOutputValue;
    }

    public double getOutputError()
    {
        return outputError;
    }

    public void setOutputError(double error)
    {
        this.outputError = error;
    }

    public double getHerrSum()
    {
        return hErrSum;
    }

    public void setHerrSum(double trainingInputError)
    {
        this.hErrSum = trainingInputError;
    }

    public void setOutputValue(double outputValue)
    {
        this.outputValue = outputValue;
    }

    public ArrayList<Double> getWeightValueList()
    {
        return weightValueList;
    }
    public int getPeNumber()
    {
        return peNumber;
    }
}

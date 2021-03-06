/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dias;

import Jama.Matrix;
import java.io.File;
import javax.mail.MessagingException;
import java.util.*;
//stuff we need for configuration
import org.apache.commons.configuration2.beanutils.BeanDeclaration;
import org.apache.commons.configuration2.beanutils.BeanHelper;
import org.apache.commons.beanutils.BeanIntrospector;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.configuration2.beanutils.XMLBeanDeclaration;
import org.apache.commons.configuration2.*;
import org.apache.commons.configuration2.ex.*;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.tree.xpath.XPathExpressionEngine;
import org.apache.commons.logging.*;

/**
 *
 * @author User
 */
public class DIAS {

    //Parameter : current processing environment. 
    // This allows us to switch between <processing> nodes in the configuration XML 
    // by using the @env attribute. 
    public static String configurationEnvironment = "none";
    public static Boolean verboseMode = false; 
    //Parameter: excel files to save/load variables
    //TODO make sure files exist in the given path
    public static String excelFilePath;
    //Parameter: File for Bodymedia read values
    //TODO make sure this file is the same as the xls generated by BodyMedia
    public static String bodymediaFileUrl;
    //Parameter: Email to receive messages
    public static String[] privateMails;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws MessagingException, Exception {
        //Set up configuration here so we can read from the configuration file for our previously-static 
        // variables. 
        boolean configureOK = configureSession();

        System.out.println(excelFilePath + " : " + new File(excelFilePath).exists());
        System.out.println(bodymediaFileUrl + " : " + new File(bodymediaFileUrl).exists());

        //Start Graphical interface
        //XXX restore this next line before merging with master. 
        if (configureOK) {
            new GUI().setVisible(true);
        }

        //  ChocaNonLinear ch = new ChocaNonLinear ();
        //   ch.Choca();
    }

    //Is it bad that this method is referencing our now-global variables? Eh, maybe. 
    //This only needs to run here, though. We can expand to a full class with a factory etc. 
    // for all our platform-specific global variables if we need to. 
    public static boolean configureSession() {
        boolean output = false; //be pessimistic. 
        Configurations configs = new Configurations();
        try {
            XMLConfiguration config = configs.xml("config/configuration.xml"); //this is a really nice factory implementation we're eliding
            //use XPATH so we can query attributes. NB that this means we'll be using slash-style lookup as in 
            // "processing/paths/excelFilePath" 
            // instead of 
            // "processing.paths.excelFilePath"
            config.setExpressionEngine(new XPathExpressionEngine());
            configurationEnvironment = config.getString("environment/env");
            verboseMode = Boolean.valueOf(config.getString("environment/verbose"));
            if (verboseMode) { System.out.println("User directory is " + System.getProperty("user.dir")); }
            if (verboseMode) { System.out.println(configurationEnvironment); } 
            excelFilePath = config.getString("processing[@env='" + configurationEnvironment + "']/paths/excelFilePath");
            bodymediaFileUrl = config.getString("processing[@env='" + configurationEnvironment + "']/paths/bodymediaFileUrl");
            //HierarchicalConfiguration node = (HierarchicalConfiguration) config.configurationAt("/nodes/node[@id='"+(str)+"']");
            List<String> emails = config.getList(String.class, "processing[@env='" + configurationEnvironment + "']/emails/email");
            privateMails = new String[emails.size()];
            privateMails = emails.toArray(privateMails);
            output = true;
        } catch (ConfigurationException cex) {
            //Something went wrong; we should probably check to see if the configuration file wasn't found, 
            // but otherwise just keep the output as false.
            System.out.println(cex.getMessage());
        }
        return output;
    }

    //////////////////////////////Matrix Handling functions for the Program/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * createNewMatrix - creates a new 2D matrix from an old matrix. 
     * Increases the size
     * @param newdimensionx
     * @param newdimensiony
     * @param oldmatrice
     * @return 
     */
    public static Matrix createnewMatrix(int newdimensionx, int newdimensiony, Matrix oldMatrix) {
        Matrix newMatrice = new Matrix(newdimensionx, newdimensiony);

        for (int i = 0; i < oldMatrix.getRowDimension(); i++) {
            for (int j = 0; j < oldMatrix.getColumnDimension(); j++) {
                newMatrice.set(i, j, oldMatrix.get(i, j));
            }
        }

        return newMatrice;
    }

    /**
     * printMatrix - print on terminal a 2D matrix an its name
     *
     * @param m
     * @param name
     */
    public static void printMatrix(Matrix m, String name) {
        System.out.print("\n " + name + ": \n{");
        for (double[] row : m.getArray()) {
            for (double val : row) {
                System.out.print(" " + val);
            }
            System.out.println();
        }
        System.out.println("}");
    }

    /**
     * print3DMatrix - - print on terminal a 3D matrix an its name
     *
     * @param x - 3D array (matrix)
     * @param matrixname
     */
    public static void print3DMatrix(double x[][][], String matrixname) {

        int[] valuex;
        valuex = lastValueReturnXYZ(x);

        System.out.println(matrixname);

        for (int k = 0; k < valuex[3] + 1; k++) {
            for (int i = 0; i < valuex[1] + 1; i++) {
                for (int j = 0; j < valuex[2] + 1; j++) {
                    System.out.print("\t\t\t" + x[i][j][k]);
                }
                System.out.println();
            }
            System.out.println("Matrice State:  " + (k + 1));
        }
        System.out.println("Matrice has written");

    }

    /**
     * lastValueReturnXYZ - returns the last non zero value in each of the
     * coordinates XYZ
     *
     * @param s - 3D array (matrix)
     * @return
     */
    public static int[] lastValueReturnXYZ(double s[][][]) {
        int lastvaluex = 0;
        int lastvaluey = 0;
        int lastvaluez = 0;

        for (int i = 0; i < s.length; i++) {
            for (int j = 0; j < s[0].length; j++) {
                for (int z = 0; z < s[0][0].length; z++) {
                    if (s[i][j][z] != 0) {
                        lastvaluex = i;
                        lastvaluey = j;
                        lastvaluez = z;
                    }
                }
            }
        }
        int[] dizi = new int[4];
        dizi[1] = lastvaluex;
        dizi[2] = lastvaluey;
        dizi[3] = lastvaluez;

        return dizi;
    }
    /**
     * matrixLastValueReturnX - return the position of the last non zero value
     * in the matrix
     * @param s
     * @return 
     */

    public static int[] matrixLastValueReturnXY(Matrix s) {
        int lastvaluex = 0;
        int lastvaluey = 0;

        for (int i = 0; i < s.getColumnDimension(); i++) {
            for (int j = 0; j < s.getRowDimension(); j++) {
                if (s.get(i, j) != 0) {
                    lastvaluex = i;
                    lastvaluey = j;
                }
            }
        }
        int[] resultlocation = new int[2];
        resultlocation[0] = lastvaluex;
        resultlocation[1] = lastvaluey;

        return resultlocation;
    }
    
    

}

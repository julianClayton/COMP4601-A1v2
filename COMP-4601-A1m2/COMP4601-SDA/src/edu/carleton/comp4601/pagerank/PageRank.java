package edu.carleton.comp4601.pagerank;

import Jama.Matrix;

import org.jgrapht.io.ExportException;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.io.*;

public class PageRank {

	
	final static double alpha =   0.5;
	public static Matrix generateTransitionMatrix(Matrix A) {
		Matrix B = A;
		for (int row = 0; row < A.getRowDimension(); row++) {
			double sum = sumRow(A, row);
			System.out.println("Sum row " + sum);
			for (int i = 0; i < A.getColumnDimension(); i++) {
				if (sum == 0) {
					B.getArray()[row][i] = 0;
				}
				else {
					B.getArray()[row][i] = A.getArray()[row][i]/sum;
				}
			}
		}
		System.out.println("transition matrix");
		B.print(B.getRowDimension(), B.getColumnDimension());
		return B;
	}
	

	public static Matrix multiplyMatrix(Matrix A, double a) {
		A = A.timesEquals(a);
		Matrix B = new Matrix(A.getRowDimension(), A.getColumnDimension(), a/A.getColumnDimension());
		return A.plus(B);
	}
	public static int sumRow(Matrix A, int row) {
	    int sum = 0;
	    for(int i = 0; i < A.getColumnDimension(); i++) {
	        sum += A.getArray()[row][i];
	    }
	    return sum;
	}
	
	private static Matrix getRow(Matrix A, int row) {
		Matrix rowMatrix = new Matrix(1, A.getColumnDimension());
		for (int i = 0; i < A.getColumnDimension(); i++) {
			rowMatrix.getArray()[0][i] = A.getArray()[row][i];
		}
		return rowMatrix;
	}
	public static Matrix generateProbabilityMatrix(Matrix A) {
		Matrix B = generateTransitionMatrix(A);
		Matrix C = multiplyMatrix(B, 1-alpha);
		return C;
	}
	private static Matrix convergeMatrix(Matrix P) {
		double diff = 10000;
		double threshold = 0.01;
		Matrix p = P;
		while (diff >= threshold) {
			p = p.times(P);
			diff = P.minus(p).normF();
			P = p;
		}
		return P;
	}
	public static Matrix generatePageRankMatrix(Matrix A) {
		Matrix P = generateProbabilityMatrix(A);
		System.out.println("Probability Matrix");
		P.print(P.getRowDimension(), P.getColumnDimension());
		return convergeMatrix(P);
	}
	public static Matrix generateAdjacencyMatrix(Graph g) {
		String fName = "adjacencymatrix";
		BufferedWriter writer = null;
		CSVExporter<String, DefaultEdge> csvExporter = new CSVExporter<String, DefaultEdge>(CSVFormat.MATRIX);
		try {
			writer = new BufferedWriter(new FileWriter(fName));
		} catch (IOException e) {
			e.printStackTrace();
		}
		csvExporter.exportGraph(g, writer);
		
		String thisLine; 
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(fName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		DataInputStream myInput = new DataInputStream(fis);
		ArrayList<String[]> lines = new ArrayList<String[]>();
		try {
			while ((thisLine = myInput.readLine()) != null) {
			     lines.add(thisLine.split(",", -1));
		
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		String[][] array = new String[g.vertexSet().size()][g.vertexSet().size()];
		lines.toArray(array);
		double[][] doubleArray = new double[g.vertexSet().size()][g.vertexSet().size()];

		for (int i = 0; i < g.vertexSet().size(); i++) {
			for (int j = 0; j < g.vertexSet().size(); j++) {
				if (array[i][j].equals("")) {
					array[i][j] = "0";
				}
				doubleArray[i][j] = Double.parseDouble(array[i][j]);
			}
		}
		
		Matrix matrix = new Matrix(doubleArray);
		return matrix;
	}
	
	public static void main(String[] args) throws ExportException {
		Graph<String, DefaultEdge> g = new SimpleDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
		double[][] array = {{1.,1.,0},{1.,1.,1.},{0.,1.,0.}};

        String v1 = "v1";
        String v2 = "v2";
        String v3 = "v3";

        // add the vertices
        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);


        // add edges to create a circuit
        g.addEdge(v1, v2);
        g.addEdge(v3, v2);
        g.addEdge(v2, v1);
        g.addEdge(v2, v3);

        Matrix matrix = generateAdjacencyMatrix(g);
        matrix.print(matrix.getColumnDimension(), matrix.getRowDimension());
        Matrix Pr = generatePageRankMatrix(matrix);
        System.out.print("final");
        Pr.print(Pr.getRowDimension(), Pr.getColumnDimension());
	}
}
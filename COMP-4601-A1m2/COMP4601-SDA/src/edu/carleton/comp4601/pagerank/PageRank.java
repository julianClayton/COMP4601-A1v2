package edu.carleton.comp4601.pagerank;
import Jama.Matrix;
import edu.carleton.comp4601.SDA.db.DatabaseManager;
import edu.carleton.comp4601.dao.Document;
import edu.carleton.comp4601.graph.PageGraph;
import edu.carleton.comp4601.graph.Vertex;
import edu.carleton.comp4601.networking.Marshaller;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import org.jgrapht.*;
import org.jgrapht.generate.GraphGenerator;
import org.jgrapht.graph.*;
import org.jgrapht.io.*;

public class PageRank {
	final  double alpha =   0.5;
	static Matrix pageRankMatrix;
	private static PageRank instance;
	private boolean rankComplete = false; 
	private ArrayList<Vertex> vertexList;
	private HashMap boostMap;
	
	PageGraph pg;
	
	public PageRank() {
		instance= this;
		try {
			pg = (PageGraph) Marshaller.deserializeObject(DatabaseManager.getInstance().getGraphData());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		vertexList = new ArrayList<Vertex>();
		boostMap = new HashMap<Integer, Float>();
	}
	
	public static PageRank getInstance() {
		if (instance == null) {
			instance = new PageRank();
		}
		return instance;
	}
	
	
	public  int sumRow(Matrix A, int row) {
	    int sum = 0;
	    for(int i = 0; i < A.getColumnDimension(); i++) {
	        sum += A.getArray()[row][i];
	    }
	    return sum;
	}
	
	public  Matrix generateTransitionMatrix(Matrix A) {
		Matrix B = A;
		for (int row = 0; row < A.getRowDimension(); row++) {
			double sum = sumRow(A, row);
			for (int i = 0; i < A.getColumnDimension(); i++) {
				if (sum == 0) {
					B.getArray()[row][i] = 0;
				}
				else {
					B.getArray()[row][i] = A.getArray()[row][i]/sum;
				}
			}
		}
		return B;
	}
	public  float getDocumentPageRank(int docId) {

		float documentRank = (float) pageRankMatrix.get(0, docId);
		return documentRank;
}
	private  Matrix multipleMatrixByAlpha(Matrix matrix) {

		matrix = matrix.times((1.0 - alpha));
		return matrix;

	}

	private  Matrix addMatrices(Matrix matrix) {
		int row = matrix.getRowDimension();
		int col = matrix.getColumnDimension();

		Matrix additionMa = new Matrix(row, col, (alpha / (double) col));
		Matrix result = matrix.plus(additionMa);

		return result;
	}
	public  ArrayList<HashMap<Integer, Float>> computePageRank() {
		
		Matrix adjacencyMatrix = generateAdjacencyMatrix(pg.getGraph());
		Set<Vertex> vertexSet = pg.getGraph().vertexSet();
		int size = adjacencyMatrix.getColumnDimension();
		Matrix matrix = new Matrix(1, size);
		matrix.set(0, 0, 1.0);

		Matrix transitionMatrix = generateTransitionMatrix(adjacencyMatrix);
		Matrix alphaMatrix = multipleMatrixByAlpha(transitionMatrix);
		Matrix addMatrix = addMatrices(alphaMatrix);
		double diff = 10000;
		double threshold = 0.00000001;
		
		while (diff >= threshold) {
			Matrix copy = matrix;
			matrix = matrix.times(addMatrix);
			matrix = matrix.times(1 / matrix.normInf());

			diff = copy.minus(matrix).normF();
		}
		pageRankMatrix = matrix;
		rankComplete = true;
		
		
		ArrayList<Document> documents = DatabaseManager.getInstance().getAllDocuments();
		ArrayList<HashMap<Integer, Float>> documentsWithRank = new ArrayList<HashMap<Integer, Float>>();
		pageRankMatrix.print(pageRankMatrix.getRowDimension(), pageRankMatrix.getColumnDimension());
		for (int i = 0; i < documents.size(); i++) {
			HashMap map = new HashMap<Integer, Float>();
			map.put(documents.get(i).getId(), (float) pageRankMatrix.get(0, i));
			boostMap.put(documents.get(i).getId(), (float) pageRankMatrix.get(0, i));
			map.put(documents.get(i).getId(), (float) pageRankMatrix.get(0, i));
			documentsWithRank.add(map);
		}
		return documentsWithRank;

	}
	public  Matrix generateAdjacencyMatrix(Graph g) {
		String fName = "adjacencymatrix";
		BufferedWriter writer = null;
		System.out.println(g.toString());
		CSVExporter<String, DefaultEdge> csvExporter = new CSVExporter<String, DefaultEdge>(CSVFormat.MATRIX);
		try {
			writer = new BufferedWriter(new FileWriter(fName));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		Set vertexSet = g.vertexSet();
		vertexList = new ArrayList(vertexSet);
		System.out.println("Size of vertex set: " + vertexList.size());
		
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
		System.out.println("ADJACENCY MATRIX");
		matrix.print(matrix.getRowDimension(), matrix.getColumnDimension());
		return matrix;
	}
	
	public HashMap<Integer, Float> generateBoostMap() {
		
		
		return new HashMap<Integer, Float>();
	}
	public static void main(String[] args) {
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

        //matrix.print(matrix.getColumnDimension(), matrix.getRowDimension());
        //Matrix Pr = computePageRank(g);
        //Pr.print(Pr.getRowDimension(), Pr.getColumnDimension());
       // Pr.print(Pr.getRowDimension(), Pr.getColumnDimension());
        
    	PageGraph graph = null;
		try {
			graph = (PageGraph) Marshaller.deserializeObject(DatabaseManager.getInstance().getGraphData());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        System.out.println(graph.getGraph());
        
        getInstance().computePageRank();
        
    	Path path = FileSystems.getDefault().getPath("").toAbsolutePath();
    	System.out.println(path.toString());
	}
	public HashMap<Integer, Float> getBoostMap() {
		return boostMap;
	}

}

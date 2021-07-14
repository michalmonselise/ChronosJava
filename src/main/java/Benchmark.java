import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.chronos.chronograph.api.structure.ChronoGraph;
import org.apache.commons.math3.stat.descriptive.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.lang.*;
import java.util.*;


public class Benchmark {

    public static void main(String[] args) throws IOException {
        ChronoGraph graph = ChronoGraph.FACTORY.create().inMemoryGraph().build();
        List<String> dateList = new ArrayList<String>();
        Set<List> edgeSet;
        try (ChronoGraph txGraph = graph.tx().createThreadedTx()) {
            List<Vertex> vertexList = new ArrayList<>();
            for (int i = 1; i <= 1899; i++) {
                vertexList.add(txGraph.addVertex(
                        T.id, Integer.toString(i)
                ));
            }


            List<String[]> rowList = new ArrayList<String[]>();

            edgeSet = new HashSet<>();
            int vertexID = 0;
            try (BufferedReader br = new BufferedReader(new FileReader("data/college_seq.csv"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] lineItems = line.split(",");
                    Vertex vertexFrom = vertexList.get(Integer.parseInt(lineItems[0]) - 1);
                    Vertex vertexTo = vertexList.get(Integer.parseInt(lineItems[1]) - 1);
                    vertexFrom.addEdge(Integer.toString(vertexID), vertexFrom, "src", vertexFrom, "dst", vertexTo, "date", lineItems[2]);
                    dateList.add(lineItems[2]);
                    edgeSet.add(Arrays.asList(vertexFrom, vertexTo));
                    vertexID += 1;

                }
            }
            txGraph.tx().commit();
        }
        Collections.sort(dateList);

        System.out.println("Graph: " + graph);
        System.out.println("Number of vertices: " + graph.traversal().V().toList().size());
        System.out.println("Number of edges: " + graph.traversal().E().toList().size());
        long startTime = System.nanoTime();


         for (List l : edgeSet) {
        burstiness(graph, l.get(0).toString(), l.get(1).toString(), dateList);
        }
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);

        System.out.println("total run time: " + duration);

    }


    public static Double meanInt(List<Integer> values) {
        double num = 0.0;
        double denom = 0.0;
        for (Integer i: values) {
            num += i;
            denom += 1.0;
        }
        if (denom > 0.0) {
            return num / denom;
        }
        else {
            return 0.0;
        }
    }

    public static Double stdInt(List<Integer> values) {
        double num = 0.0;
        double denom = 0.0;
        double mean = meanInt(values);
        for (Integer i: values) {
            num += (i - mean) * (i - mean);
            denom += 1.0;
        }
        if (denom > 0.0) {
            return Math.sqrt(num / denom);
        }
        else {
            return 0.0;
        }
    }

    public static Double burstiness(ChronoGraph graph, String src, String dst, List<String> dateList) {
        List<Integer> gaps = new ArrayList<Integer>();
        int counter = 0;
        for (String i: dateList) {
            List<Edge> edges = graph.traversal().E().has("date", i).has("dst", dst).has("src", src).toList();
            int isEdge = edges.size();
            if (isEdge == 0) {
                counter = counter + 1;
            }
        else {
            gaps.add(counter);
            counter = 0;
            }
        }
        return (stdInt(gaps) - meanInt(gaps)) / (stdInt(gaps) + meanInt(gaps));
    }


}
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.chronos.chronograph.api.structure.ChronoGraph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.lang.*;
import java.util.*;


public class Benchmark {

    public static void main(String[] args) throws IOException {
        ChronoGraph graph = ChronoGraph.FACTORY.create().inMemoryGraph().build();
        Set<Double> dateSet = new HashSet<>();
        Set<List> edgeSet;
        List<Vertex> vertexList = new ArrayList<>();
        for (int i = 1; i <= 1899; i++) {
            vertexList.add(graph.addVertex(
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
                dateSet.add(Double.valueOf(lineItems[2]));
                List<Vertex> vertices = Arrays.asList(vertexFrom, vertexTo);
                edgeSet.add(vertices);
                vertexID += 1;

            }
        }

        ArrayList<Double> dateList = new ArrayList<>(dateSet);
        Collections.sort(dateList);

        System.out.println("Graph: " + graph);
        System.out.println("Number of vertices: " + graph.traversal().V().toList().size());
        System.out.println("Number of edges: " + graph.traversal().E().toList().size());
        System.out.println("Dates: " + dateList);
        long startTime = System.nanoTime();


         for (List<Vertex> l : edgeSet) {
            burstiness(graph, l.get(0), l.get(1), dateList);
            //System.out.println(l.get(0).toString() + " " + l.get(1).toString());
        }
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);

        System.out.println("total run time: " + duration);

    }


    public static Double meanDouble(List<Double> values) {
        double num = 0.0;
        double denom = 0.0;
        for (Double i: values) {
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

    public static Double stdDouble(List<Double> values) {
        double num = 0.0;
        double denom = 0.0;
        double mean = meanDouble(values);
        for (Double i: values) {
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

    public static Double burstiness(ChronoGraph graph, Vertex src, Vertex dst, List<Double> dateList) {
        List<Double> gaps = new ArrayList<Double>();
        int counter = 0;
        //for (Double i: dateList) {
        List<Edge> edges = graph.traversal().E().has("dst", dst).has("src", src).toList();
            //Integer isEdge = edges.size();
            //System.out.println("Number of edges: "+ isEdge);
            //System.out.println("date :" + i);
            //if (isEdge == 0) {
            //    counter = counter + 1;
            //}
        //else {
        //    gaps.add(counter);
        //    counter = 0;
        //    }

        //}
        if (edges.size() > 0) {
            Edge prevEdge = edges.get(0);
            for (Edge e : edges) {
                gaps.add(getDate(prevEdge) - getDate(e));
            }
        }
        return (stdDouble(gaps) - meanDouble(gaps)) / (stdDouble(gaps) + meanDouble(gaps));
    }

    public static Double getDate(Edge e) {
        return e.value("date");
    }

}
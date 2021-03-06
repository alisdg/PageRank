package ir.ac.sbu;

import ir.ac.sbu.mapper.AlgorithmMapper;
import ir.ac.sbu.mapper.MapEdges;
import ir.ac.sbu.mapper.ReporterMap;
import ir.ac.sbu.mapper.SortMapper;
import ir.ac.sbu.reducer.AlgorithmReducer;
import ir.ac.sbu.reducer.PreReducer;
import ir.ac.sbu.reducer.ReporterReduce;
import ir.ac.sbu.reducer.SortReducer;
import ir.ac.sbu.types.Node;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;

import java.io.IOException;

public class App {
    public static void main( String[] args ) throws IOException, InterruptedException, ClassNotFoundException {
        boolean lastJobComplete = false ;
        Configuration conf = new Configuration();
        for (String arg : args) {
            String[] keyValue = arg.split("=");
            if (keyValue.length == 2) {
                conf.setDouble(keyValue[0], Double.parseDouble(keyValue[1]));
            }
        }
        Job initialize = Job.getInstance(conf, "pagerank-PreProcess");
        initialize.setMapperClass(MapEdges.class);
        //initialize.setCombinerClass(PreReducer.class);
        initialize.setReducerClass(PreReducer.class);
        initialize.setOutputFormatClass(SequenceFileOutputFormat.class);
        initialize.setOutputKeyClass(Text.class);
        initialize.setOutputValueClass(Node.class);
        initialize.setMapOutputValueClass(Text.class);
        initialize.setJar("pagerank.jar");
        FileInputFormat.addInputPath(initialize, new Path("/pagerank/input/facebook"));
        FileOutputFormat.setOutputPath(initialize, new Path("/pagerank/output/0"));
        lastJobComplete = initialize.waitForCompletion(true) ;

        if(!lastJobComplete)
            System.exit(1);

        int iterations = (int) conf.getDouble("iterations" , 1);

        for (int i = 0; i < iterations ; i++) {
                Job pagerank = Job.getInstance(conf, "pagerank-iteration "+(i+1));
                pagerank.setMapperClass(AlgorithmMapper.class);
                //pagerank.setCombinerClass(AlgorithmReducer.class);
                pagerank.setReducerClass(AlgorithmReducer.class);
                pagerank.setInputFormatClass(SequenceFileInputFormat.class);
                pagerank.setOutputFormatClass(SequenceFileOutputFormat.class);
                pagerank.setOutputKeyClass(Text.class);
                pagerank.setOutputValueClass(Node.class);
                pagerank.setJar("pagerank.jar");
                FileInputFormat.addInputPath(pagerank, new Path("/pagerank/output/"+(i)));
                FileOutputFormat.setOutputPath(pagerank, new Path("/pagerank/output/"+(i+1)));
                lastJobComplete = pagerank.waitForCompletion(true);
                if(!lastJobComplete)
                    System.exit(1);

        }

        Job sorter = Job.getInstance(conf, "pagerank-Sorter");
        sorter.setMapperClass(SortMapper.class);
        //sorter.setCombinerClass(SortReducer.class);
        sorter.setReducerClass(SortReducer.class);
        sorter.setInputFormatClass(SequenceFileInputFormat.class);
        sorter.setOutputKeyClass(Text.class);
        sorter.setOutputValueClass(DoubleWritable.class);
        sorter.setMapOutputKeyClass(DoubleWritable.class);
        sorter.setMapOutputValueClass(Text.class);
        sorter.setJar("pagerank.jar");
        FileInputFormat.addInputPath(sorter, new Path("/pagerank/output/"+(iterations)));
        FileOutputFormat.setOutputPath(sorter, new Path("/pagerank/output/result"));
        lastJobComplete = sorter.waitForCompletion(true);
        if(!lastJobComplete)
            System.exit(1);

        Job reporter = Job.getInstance(conf, "pagerank-Reporter");
        reporter.setMapperClass(ReporterMap.class);
        //reporter.setCombinerClass(ReporterReduce.class);
        reporter.setReducerClass(ReporterReduce.class);
        reporter.setInputFormatClass(SequenceFileInputFormat.class);
        reporter.setOutputKeyClass(Text.class);
        reporter.setOutputValueClass(Text.class);
        reporter.setMapOutputKeyClass(Text.class);
        reporter.setMapOutputValueClass(Text.class);
        reporter.setJar("pagerank.jar");
        FileInputFormat.addInputPath(reporter, new Path("/pagerank/output/"+(iterations)));
        FileOutputFormat.setOutputPath(reporter, new Path("/pagerank/output/report"));
        lastJobComplete = reporter.waitForCompletion(true);
        if(!lastJobComplete)
            System.exit(1);

    }
}

package ir.ac.sbu.mapper;

import ir.ac.sbu.types.Node;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class ReporterMap extends Mapper<Text, Node, Text, Text> {
    @Override
    protected void map(Text key, Node value, Context context) throws IOException, InterruptedException {
        String ss = value.toString();
        context.write(key,new Text(ss));
    }
}

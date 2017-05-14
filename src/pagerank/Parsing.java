package pagerank;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import TFIDF.Pair;

public class Parsing extends Configured implements Tool {
	
	static double  dumpingFactor =0.85;
	
   public static void main(String[] args) throws Exception {
      System.out.println(Arrays.toString(args));
      int res = ToolRunner.run(new Configuration(), new Parsing(), args);
      
      System.exit(res);
   }

   @Override
   public int run(String[] args) throws Exception {
      System.out.println(Arrays.toString(args));
      Configuration configuration = this.getConf();

     
      Job job = new Job(configuration, "ParsingPageRank");
      job.setNumReduceTasks(1);
      job.setJarByClass(Parsing.class);
      job.setOutputKeyClass(Text.class);
      job.setOutputValueClass(Text.class);

      job.setMapperClass(Map.class);
      job.setReducerClass(Reduce.class);

      job.setInputFormatClass(TextInputFormat.class);
      job.setOutputFormatClass(TextOutputFormat.class);

      FileInputFormat.addInputPath(job, new Path("inputPageRank"));
      FileOutputFormat.setOutputPath(job, new Path("output1PageRank"));
      FileSystem hdfs = FileSystem.get(getConf());
 	  if (hdfs.exists(new Path("output1PageRank")))
 	      hdfs.delete(new Path("output1PageRank"), true);
      job.waitForCompletion(true);
      
      return 0;
   }
   
   public static class Map extends Mapper<LongWritable, Text, Text, Text> {
      private final static IntWritable ONE = new IntWritable(1);
     

      
      @Override
      public void map(LongWritable key, Text value, Context context)
              throws IOException, InterruptedException {
    	  String [] fromTo = value.toString().split("\t");

    	 // System.out.println("Map parser " + fromTo[0] + " : " + fromTo[1]);
    		 context.write(new Text(fromTo[0]), new Text(fromTo[1]));

      }
 
   }

   public static class Reduce extends Reducer<Text, Text, Text, Text> {
	
	      TreeSet<String> froms = new TreeSet<String>();
	      TreeSet<String> tos = new TreeSet<String>();
	   
	  
      @Override
      public void reduce(Text key, Iterable<Text> values, Context context)
              throws IOException, InterruptedException {
    	
    	  String LinksToPages = "";
    	  
    	  froms.add(key.toString());
 		
    	  
    	  for(Text link : values){
    		  
    		  LinksToPages+=link.toString()+",";
    		  tos.add(link.toString());
    	  }
    	  if(LinksToPages.length()>0)
    		  LinksToPages = LinksToPages.substring(0, LinksToPages.length()-1);
    	  
    	 // System.out.println("Map reducer " + key + " : " + "1.0\t"+LinksToPages);
    	  context.write(key, new Text("1.0\t"+LinksToPages));
      
      }
      
      protected void cleanup(Context context) throws IOException,InterruptedException {
     	   //we call this fonction once at the end
   	  System.out.println("froms : "+froms);
   	  System.out.println("tos : "+tos);
   	  //context.write(new Text("1"), new Text());
   	  
   	  
   	  froms.removeAll(tos);
   	  System.out.println("tos after sub : "+tos);
   	 
   	  for(String s: froms){
   		context.write(new Text(s), new Text("1.0\tnull"));
   		System.out.println("Map reducer " + s + " : " + "1.0\tnull");
   	  }
   	  
   	  
     }
     

      
   }
}
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

public class Calculating extends Configured implements Tool {
	
	static double  dumpingFactor =0.85;
	static HashMap<String, String> hist = new HashMap<String, String>();
	
   public static void main(String[] args) throws Exception {
      System.out.println(Arrays.toString(args));
      int res = ToolRunner.run(new Configuration(), new Calculating(), args);
      
      System.exit(res);
   }

   @Override
   public int run(String[] args) throws Exception {
      System.out.println(Arrays.toString(args));
      Configuration configuration = this.getConf();

     
      Job job = new Job(configuration, "CalculatingPageRank");
      job.setNumReduceTasks(1);
      job.setJarByClass(Calculating.class);
      job.setOutputKeyClass(Text.class);
      job.setOutputValueClass(Text.class);

      job.setMapperClass(Map.class);
      job.setReducerClass(Reduce.class);

      job.setInputFormatClass(TextInputFormat.class);
      job.setOutputFormatClass(TextOutputFormat.class);

      FileInputFormat.addInputPath(job, new Path("output4PageRank"));
      FileOutputFormat.setOutputPath(job, new Path("output5PageRank"));
      FileSystem hdfs = FileSystem.get(getConf());
 	  if (hdfs.exists(new Path("output5PageRank")))
 	      hdfs.delete(new Path("output5PageRank"), true);
      job.waitForCompletion(true);
      
      return 0;
   }
   
   public static class Map extends Mapper<LongWritable, Text, Text, Text> {
      
      @Override
      public void map(LongWritable key, Text value, Context context)
              throws IOException, InterruptedException {
    	  
    	  
    	  System.out.println(value);
    	  
    	  String [] res = value.toString().split("\t");
    	  
    	  String [] vals = res[2].split(",");
    	  
    	  hist.put(res[0], res[2]);
    	  
    	  
    	  for(String s: vals){
    		  context.write(new Text(s), new Text(res[0]+"\t"+res[1]+"\t"+vals.length));
    	  }
    	  
      }
      
      protected void cleanup(Context context) throws IOException,InterruptedException {
 
    	  //context.write(new Text("C"), new Text());
     }
   }

   public static class Reduce extends Reducer<Text, Text, Text, Text> {
	   TreeSet<PageAndRank> topPages = new TreeSet<>();
      @Override
      public void reduce(Text key, Iterable<Text> values, Context context)
              throws IOException, InterruptedException {
    	
    	  double pageRankScore=(1.0-dumpingFactor);
    	  
    	  System.out.println("key : "+key);
    	  
    	  if(!key.toString().equals("null")){
    		 

    	  for(Text val : values){
    		 // System.out.println("val : "+val);
    		  String [] res =val.toString().split("\t");
    		  pageRankScore+=dumpingFactor*(Double.parseDouble(res[1])/Double.parseDouble(res[2]));
    		  
    	  }
    	  System.out.println(key + " : "+pageRankScore);
    	  context.write(key, new Text(""+pageRankScore+"\t"+hist.get(key.toString())));
    	  topPages.add(new PageAndRank(pageRankScore,key.toString()));
    	   if (topPages.size() > 10) {
         	  topPages.pollFirst();
               
               //we delete the first pair with the lowest frequency
           }
     	 
    	  }else{
    		//  System.out.println("Not null");
    		  for(Text val : values){
    			  
    			  String [] res =val.toString().split("\t");
    			  
    			  //System.out.println(res[0] + " : "+pageRankScore);
    			  context.write(new Text(res[0]), new Text(""+pageRankScore+"\t"+hist.get(res[0])));
    			  topPages.add(new PageAndRank(pageRankScore,res[0]));
    			   if (topPages.size() > 10) {
    		        	  topPages.pollFirst();         
    		              //we delete the first pair with the lowest frequency
    		          }
    		    	 
    		  }
    		  
    	  }

      }
      
      
      protected void cleanup(Context ctxt) throws IOException,InterruptedException {
   	   //we call this fonction once at the end
    	  System.out.println("results pagerank : ");
          while (!topPages.isEmpty()) {
        	  PageAndRank pr = topPages.pollLast();
              System.out.println(pr.getPage() + " : "+pr.rank);
          }
      }
   
      
   }
}
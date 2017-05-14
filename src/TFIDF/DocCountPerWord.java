package TFIDF;

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
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class DocCountPerWord extends Configured implements Tool {
   public static void main(String[] args) throws Exception {
      System.out.println(Arrays.toString(args));
      int res = ToolRunner.run(new Configuration(), new DocCountPerWord(), args);
      
      System.exit(res);
   }

   @Override
   public int run(String[] args) throws Exception {
      System.out.println(Arrays.toString(args));
      Configuration configuration = this.getConf();

     
      Job job = new Job(configuration, "DocCountPerWord");
      job.setNumReduceTasks(1);
      job.setJarByClass(DocCountPerWord.class);
      job.setOutputKeyClass(Text.class);
      job.setOutputValueClass(Text.class);

      job.setMapperClass(Map.class);
      job.setReducerClass(Reduce.class);

      job.setInputFormatClass(TextInputFormat.class);
      job.setOutputFormatClass(TextOutputFormat.class);

      FileInputFormat.addInputPath(job, new Path("outputtfidfwordcountperdoc"));
      FileOutputFormat.setOutputPath(job, new Path("tfidfword"));
      FileSystem hdfs = FileSystem.get(getConf());
 	  if (hdfs.exists(new Path("tfidfword")))
 	      hdfs.delete(new Path("tfidfword"), true);
      job.waitForCompletion(true);
      
      return 0;
   }
   
   public static class Map extends Mapper<LongWritable, Text, Text, Text> {
      private final static IntWritable ONE = new IntWritable(1);
     

      @Override
      public void map(LongWritable key, Text value, Context context)
              throws IOException, InterruptedException {
    	
    	  String[] words =  value.toString().split("\t");
         
        context.write(new Text(words[0]), new Text(words[1]+"\t"+words[2]+"\t"+words[3]));
      }
   }

   public static class Reduce extends Reducer<Text, Text, Text, Text> {
	   TreeSet<Pair> topWordsPair = new TreeSet<>();
      @Override
      public void reduce(Text key, Iterable<Text> values, Context context)
              throws IOException, InterruptedException {
       
          //System.out.println(key);
          ArrayList<String> words = new ArrayList<String>();    	  
    	  for (Text val : values) {  
    		 words.add(val.toString());
    	     // System.out.println(val.toString()); 
    	  }
          
    	  for(int i=0;i<words.size();i++){
    		  
    		  String [] tmp = words.get(i).split("\t");
    		  
    		  context.write(new Text(key+"\t"+tmp[0]), new Text(tmp[1]+"\t"+tmp[2]+"\t"+words.size()));
    		  
    		  System.out.println("tmp[1] : " + tmp[1]+ " tmp[2] : " + tmp[2] + " wordsize : " +words.size());
    		  
    		  double tfidf = (Double.parseDouble(tmp[1])/Double.parseDouble(tmp[2]))*Math.log(2.0/words.size());
    		  
    		  topWordsPair.add(new Pair(tfidf, key.toString(), tmp[0]));

              if (topWordsPair.size() > 20) {
                  topWordsPair.pollFirst();
                  
                  //we delete the first pair with the lowest frequency
              }
    		  
    	  }
    	
      
      }
      
      protected void cleanup(Context ctxt) throws IOException,InterruptedException {
   	   //we call this fonction once at the end
          while (!topWordsPair.isEmpty()) {
              Pair pair = topWordsPair.pollLast();
              System.out.println("["+pair.getWord()+"\t"+ pair.getDoc()+"] : " + pair.frequency);
          }
      }
      
   }
}
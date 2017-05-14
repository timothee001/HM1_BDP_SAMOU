package TFIDF;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

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

public class WordCountPerDoc extends Configured implements Tool {
   public static void main(String[] args) throws Exception {
      System.out.println(Arrays.toString(args));
      int res = ToolRunner.run(new Configuration(), new WordCountPerDoc(), args);
      
      System.exit(res);
   }

   @Override
   public int run(String[] args) throws Exception {
      System.out.println(Arrays.toString(args));
      Configuration configuration = this.getConf();

     
      Job job = new Job(configuration, "WordCounPerDoct");
      job.setNumReduceTasks(1);
      job.setJarByClass(WordCountPerDoc.class);
      job.setOutputKeyClass(Text.class);
      job.setOutputValueClass(Text.class);

      job.setMapperClass(Map.class);
      job.setReducerClass(Reduce.class);

      job.setInputFormatClass(TextInputFormat.class);
      job.setOutputFormatClass(TextOutputFormat.class);

      FileInputFormat.addInputPath(job, new Path("outputtfidfwordcount"));
      FileOutputFormat.setOutputPath(job, new Path("outputtfidfwordcountperdoc"));
      FileSystem hdfs = FileSystem.get(getConf());
 	  if (hdfs.exists(new Path("outputtfidfwordcountperdoc")))
 	      hdfs.delete(new Path("outputtfidfwordcountperdoc"), true);
      job.waitForCompletion(true);
      
      return 0;
   }
   
   public static class Map extends Mapper<LongWritable, Text, Text, Text> {
      private final static IntWritable ONE = new IntWritable(1);
     

      @Override
      public void map(LongWritable key, Text value, Context context)
              throws IOException, InterruptedException {
    	  String[] words =  value.toString().split("\t");
        /* System.out.println(words[0]);
         System.out.println(words[1]);
         System.out.println(words[2]);
*/
         
         context.write(new Text(words[1]), new Text(words[0]+"\t"+words[2]));
      }
   }

   public static class Reduce extends Reducer<Text, Text, Text, Text> {
      @Override
      public void reduce(Text key, Iterable<Text> values, Context context)
              throws IOException, InterruptedException {
       
    	  HashMap<String, String> hm = new HashMap<String,String>();
    	  int countWordsPerDoc = 0;
    	  
    	  for (Text val : values) {
    		  String[] words =  val.toString().split("\t");
    		  
    		  //System.out.println(words[0]);
    		  int wordCount = Integer.parseInt(words[1]);
    		  hm.put(words[0]+"\t"+key.toString(), ""+wordCount);
    		  
    		  countWordsPerDoc +=wordCount;
    		   
            }
    	  
    	  
    	  Set set2 = hm.entrySet();
          Iterator iterator2 = set2.iterator();
          while(iterator2.hasNext()) {
              Entry mentry2 = (Entry)iterator2.next();
             System.out.println(mentry2.getKey() +" : " + mentry2.getValue()+"\t"+countWordsPerDoc);
              context.write(new Text(mentry2.getKey().toString()), new Text(mentry2.getValue()+"\t"+countWordsPerDoc));
         
           }

    	  

      
      }
   }
}
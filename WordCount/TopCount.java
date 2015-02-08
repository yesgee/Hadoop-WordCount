package org.hwone;

import java.io.IOException;
import java.util.*;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.*;
import org.apache.hadoop.mapreduce.lib.output.*;
import org.apache.hadoop.util.*;

public class TopCount extends Configured implements Tool {

   public static class Map
       extends Mapper<LongWritable, Text, Text, IntWritable> {
     private final static IntWritable one = new IntWritable(1);
     private Text word = new Text();

     public void map(LongWritable key, Text value, Context context)
         throws IOException, InterruptedException {
       String line = value.toString().toLowerCase();
	//Strip punctuation marks
       StringTokenizer tokenizer = new StringTokenizer(line, " \t\\s\n\r\f,.:\";?![]'");
       while (tokenizer.hasMoreTokens()) {
         word.set(tokenizer.nextToken());
	 context.write(word, one);
       }
     }
   }

   public static class Combine
       extends Reducer<Text, IntWritable, Text, IntWritable> {
     public void reduce(Text key, Iterable<IntWritable> values,
         Context context) throws IOException, InterruptedException {

       int sum = 0;
       for (IntWritable val : values) {
         sum += val.get();
       }
	//Check if number of occurences is greater than or equal to 100
	if(sum>=100)
	{
 	      context.write(key, new IntWritable(sum));
	}
	
     }
   }

   public static class Reduce
       extends Reducer<Text, IntWritable, Text, IntWritable> {
     public void reduce(Text key,IntWritable values,
         Context context) throws IOException, InterruptedException {
	 int sum=values.get();
     
              context.write(key, new IntWritable(sum));
     }
   }

 

   public int run(String [] args) throws Exception {
     Job job = new Job(getConf());
     job.setJarByClass(TopCount.class);
     job.setJobName("topcount");

     job.setOutputKeyClass(Text.class);
     job.setOutputValueClass(IntWritable.class);

     job.setMapperClass(Map.class);
     job.setCombinerClass(Combine.class);
     job.setReducerClass(Reduce.class);
     

     job.setInputFormatClass(TextInputFormat.class);
     job.setOutputFormatClass(TextOutputFormat.class);

     FileInputFormat.setInputPaths(job, new Path(args[0]));
     FileOutputFormat.setOutputPath(job, new Path(args[1]));

     boolean success = job.waitForCompletion(true);
     return success ? 0 : 1;
   }

   public static void main(String[] args) throws Exception {
     int ret = ToolRunner.run(new TopCount(), args);
     System.exit(ret);
   }
}


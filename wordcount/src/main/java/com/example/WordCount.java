package com.example;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class WordCount {

  // 1. MAPPER CLASS
  public static class TokenizerMapper
      extends Mapper<Object, Text, Text, IntWritable> {

    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();

    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
      StringTokenizer itr = new StringTokenizer(value.toString());
      while (itr.hasMoreTokens()) {
        String token =itr.nextToken();
        
        if(token.contains("bang")){
          word.set(token);
          System.out.println("Map phase: We are processing word " + word);
          context.write(word, one);
        }
        
      }
    }
  }

  // 2. REDUCER CLASS
  public static class IntSumReducer
      extends Reducer<Text, IntWritable, Text, IntWritable> {
    private IntWritable result = new IntWritable();

    public void reduce(Text key, Iterable<IntWritable> values,
        Context context) throws IOException, InterruptedException {
      int sum = 0;
      for (IntWritable val : values) {
        System.out.println("Reduce phase: The key is " + key + " The value is " + val.get());
        sum += val.get();
      }
      result.set(sum);
      context.write(key, result);
    }
  }

  // 3. DRIVER (MAIN)
  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "word count");
    job.setJarByClass(WordCount.class);

    // Khai báo Mapper và Reducer
    job.setMapperClass(TokenizerMapper.class);
    job.setCombinerClass(IntSumReducer.class); // Bước tối ưu (Optional)
    job.setReducerClass(IntSumReducer.class);

    // Khai báo kiểu dữ liệu đầu ra
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(IntWritable.class);

    // Nhận Input/Output path từ dòng lệnh
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));

    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}

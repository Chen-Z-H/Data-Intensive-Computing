package topten;

import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Iterator;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.filter.FirstKeyOnlyFilter;

public class TopTen {
    // This helper function parses the stackoverflow into a Map for us.
    public static Map<String, String> transformXmlToMap(String xml) {
	Map<String, String> map = new HashMap<String, String>();
	try {
	    String[] tokens = xml.trim().substring(5, xml.trim().length() - 3).split("\"");
	    for (int i = 0; i < tokens.length - 1; i += 2) {
		String key = tokens[i].trim();
		String val = tokens[i + 1];
		map.put(key.substring(0, key.length() - 1), val);
	    }
	} catch (StringIndexOutOfBoundsException e) {
	    System.err.println(xml);
	}

	return map;
    }

    public static class TopTenMapper extends Mapper<Object, Text, NullWritable, Text> {
	// Stores a map of user reputation to the record
	TreeMap<Integer, Text> repToRecordMap = new TreeMap<Integer, Text>(new RepComparator());

	public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
	    // <FILL IN>
            StringTokenizer itr = new StringTokenizer(value.toString(), "\n");
            System.out.println(value.toString());
            while (itr.hasMoreTokens()) {
                String profile_text = itr.nextToken();
                Map<String, String> profile_map = transformXmlToMap(profile_text);
                if (profile_map.containsKey("id=")) {
                    repToRecordMap.put(Integer.valueOf(profile_map.get("reputation=")), new Text(profile_text));
                }
            }       
	}

	protected void cleanup(Context context) throws IOException, InterruptedException {
	    // Output our ten records to the reducers with a null key
	    // <FILL IN>
            // Now extract the top 10 records and pass them to reducer
            int counter = 1;
            for (Map.Entry<Integer, Text> entry: repToRecordMap.entrySet()) {
//                System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
                context.write(null, entry.getValue());
                if (++counter > 10) {
                    break;
                }
            }
	}
    }

    public static class TopTenReducer extends TableReducer<NullWritable, Text, NullWritable> {
	// Stores a map of user reputation to the record
	private TreeMap<Integer, Text> repToRecordMap = new TreeMap<Integer, Text>(new RepComparator());

	public void reduce(NullWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
	    // <FILL IN>
            for (Text profile_text: values) {
                Map<String, String> profile_map = transformXmlToMap(profile_text.toString());
                repToRecordMap.put(Integer.valueOf(profile_map.get("reputation=")), 
                        new Text(String.valueOf(profile_map.get("id="))));
            }
	}
        
        // Write the final top ten items to the hbase
        protected void cleanup(Context context) throws IOException, InterruptedException {
            int counter = 1;
            for (Map.Entry<Integer, Text> entry: repToRecordMap.entrySet()) {
                Put inHBase = new Put(Bytes.toBytes(entry.getKey()));
                inHBase.addColumn(Bytes.toBytes("info"), Bytes.toBytes("rep"), 
                        Bytes.toBytes(Integer.valueOf(entry.getValue().toString())));
                inHBase.addColumn(Bytes.toBytes("info"), Bytes.toBytes("id"), 
                        Bytes.toBytes(Integer.valueOf(entry.getKey())));
                context.write(null, inHBase);
                if (++counter > 10) {
                    break;
                }
            }
        }
    }
    
    static class RepComparator implements Comparator {
        @Override
        public int compare(Object o1, Object o2) {
            if ((int)o1 > (int)o2) {
                return -1;
            } else if ((int)o1 < (int)o2) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    public static void main(String[] args) throws Exception {
	// <FILL IN>
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "TopTen");
        job.setJarByClass(TopTen.class);

        job.setMapperClass(TopTenMapper.class);
        job.setCombinerClass(TopTenReducer.class);
        job.setReducerClass(TopTenReducer.class);
        job.setNumReduceTasks(1);
        
        FileInputFormat.addInputPath(job, new Path(args[0]));
        
        TableMapReduceUtil.initTableReducerJob("topten", TopTenReducer.class, job);
        
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}

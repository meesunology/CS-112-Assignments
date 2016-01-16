package search;

import java.io.*;
import java.util.*;

public class Driver{
	
	static Scanner testsc = new Scanner(System.in);
	static LittleSearchEngine testEngine = new LittleSearchEngine();
	
	public static void main(String args[])
			throws IOException{
		testEngine.makeIndex("docs.txt",  "noisewords.txt");

		String kw1 = "Deep";
		String kw2 = "World";
		
		int size = testEngine.keywordsIndex.size();
		System.out.println(size);
		System.out.println(testEngine.keywordsIndex.toString());

		testEngine.top5search(kw1, kw2);
	}
}

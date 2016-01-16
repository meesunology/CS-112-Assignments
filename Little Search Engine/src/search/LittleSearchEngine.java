package search;

import java.io.*;
import java.util.*;

/**
 * This class encapsulates an occurrence of a keyword in a document. It stores the
 * document name, and the frequency of occurrence in that document. Occurrences are
 * associated with keywords in an index hash table.
 * 
 * @author Sesh Venugopal
 * 
 */
class Occurrence {
	/**
	 * Document in which a keyword occurs.
	 */
	String document;
	
	/**
	 * The frequency (number of times) the keyword occurs in the above document.
	 */
	int frequency;
	
	/**
	 * Initializes this occurrence with the given document,frequency pair.
	 * 
	 * @param doc Document name
	 * @param freq Frequency
	 */
	public Occurrence(String doc, int freq) {
		document = doc;
		frequency = freq;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "(" + document + "," + frequency + ")";
	}
}

/**
 * This class builds an index of keywords. Each keyword maps to a set of documents in
 * which it occurs, with frequency of occurrence in each document. Once the index is built,
 * the documents can searched on for keywords.
 *
 */
public class LittleSearchEngine {
	
	/**
	 * This is a hash table of all keywords. The key is the actual keyword, and the associated value is
	 * an array list of all occurrences of the keyword in documents. The array list is maintained in descending
	 * order of occurrence frequencies.
	 */
	HashMap<String,ArrayList<Occurrence>> keywordsIndex;
	
	/**
	 * The hash table of all noise words - mapping is from word to itself.
	 */
	HashMap<String,String> noiseWords;
	
	/**
	 * Creates the keyWordsIndex and noiseWords hash tables.
	 */
	public LittleSearchEngine() {
		keywordsIndex = new HashMap<String,ArrayList<Occurrence>>(1000,2.0f);
		noiseWords = new HashMap<String,String>(100,2.0f);
	}
	
	/**
	 * This method indexes all keywords found in all the input documents. When this
	 * method is done, the keywordsIndex hash table will be filled with all keywords,
	 * each of which is associated with an array list of Occurrence objects, arranged
	 * in decreasing frequencies of occurrence.
	 * 
	 * @param docsFile Name of file that has a list of all the document file names, one name per line
	 * @param noiseWordsFile Name of file that has a list of noise words, one noise word per line
	 * @throws FileNotFoundException If there is a problem locating any of the input files on disk
	 */
	public void makeIndex(String docsFile, String noiseWordsFile) 
	throws FileNotFoundException {
		// load noise words to hash table
		Scanner sc = new Scanner(new File(noiseWordsFile));
		while (sc.hasNext()) {
			String word = sc.next();
			noiseWords.put(word,word);
		}
		
		// index all keywords
		sc = new Scanner(new File(docsFile));
		while (sc.hasNext()) {
			String docFile = sc.next();
			HashMap<String,Occurrence> kws = loadKeyWords(docFile);
			mergeKeyWords(kws);
		}
		
		//close scanner
		sc.close();
	}

	/**
	 * Scans a document, and loads all keywords found into a hash table of keyword occurrences
	 * in the document. Uses the getKeyWord method to separate keywords from other words.
	 * 
	 * @param docFile Name of the document file to be scanned and loaded
	 * @return Hash table of keywords in the given document, each associated with an Occurrence object
	 * @throws FileNotFoundException If the document file is not found on disk
	 */
	public HashMap<String,Occurrence> loadKeyWords(String docFile) 
	throws FileNotFoundException {
		
		//empty file
		if (docFile.length() == 0 || docFile == null){
			throw new FileNotFoundException();
		}
		
		//create the keyword HashMap and scanner for docfile
		//note that the input here is going to be the actual document, not the list of document names
		HashMap<String, Occurrence> resultant = new HashMap<String, Occurrence>();
		Scanner sc = new Scanner(new File (docFile));
		
		//going through each line of the document
		while (sc.hasNext()){
			String keyWord = getKeyWord(sc.next());
			
			if (keyWord != null){
				//HashMap already has the key
				if (resultant.containsKey(keyWord)){
					resultant.get(keyWord).frequency++;
					
				//HashMap doesn't have key
				} else{
					resultant.put(keyWord, new Occurrence(docFile, 1));
				}
			}
		}
		
		//closes scannner and returns resultant
		sc.close();
		return resultant;
	}
	
	/**
	 * Merges the keywords for a single document into the master keywordsIndex
	 * hash table. For each keyword, its Occurrence in the current document
	 * must be inserted in the correct place (according to descending order of
	 * frequency) in the same keyword's Occurrence list in the master hash table. 
	 * This is done by calling the insertLastOccurrence method.
	 * 
	 * @param kws Keywords hash table for a document
	 */
	public void mergeKeyWords(HashMap<String,Occurrence> kws) {

		//for each key, add it into the Master KeyWord HashTable
		for(String kwsKey : kws.keySet()){
			
			//Key is in the master KeyWord HashTable
			if (keywordsIndex.containsKey(kwsKey)){
				keywordsIndex.get(kwsKey).add(kws.get(kwsKey));
				insertLastOccurrence(keywordsIndex.get(kwsKey));
				
			} else{
				//Key is NOT in the master KeyWord HashTable
				//make an array for the Occurrence<>
				ArrayList <Occurrence> tempArray = new ArrayList<Occurrence>();
				tempArray.add(kws.get(kwsKey));
				
				//add the key into the Master HashKey
				keywordsIndex.put(kwsKey, tempArray);
			}
		}
	}
	
	/**
	 * Given a word, returns it as a keyword if it passes the keyword test,
	 * otherwise returns null. A keyword is any word that, after being stripped of any
	 * TRAILING punctuation, consists only of alphabetic letters, and is not
	 * a noise word. All words are treated in a case-INsensitive manner.
	 * 
	 * Punctuation characters are the following: '.', ',', '?', ':', ';' and '!'
	 * 
	 * @param word Candidate word
	 * @return Keyword (word without trailing punctuation, LOWER CASE)
	 */
	public String getKeyWord(String word) {
		
		//if word is null return null
		if (word == null){
			return null;
		}
		
		//lower case and remove the punctuation
		word = word.toLowerCase();
		word = word.replaceAll("[.,?!:;]+$", "");
		
		//checks if it's a nosieword
		if (noiseWords.containsKey(word)){
			return null;
		}
		
		//make sure that it's all letters
		for (int i = 0; i < word.length(); i++){
			if (!Character.isLetter(word.charAt(i))){
				return null;
			}
		}
		
		return word;
		
	}
	
	/**
	 * Inserts the last occurrence in the parameter list in the correct position in the
	 * same list, based on ordering occurrences on descending frequencies. The elements
	 * 0..n-2 in the list are already in the correct order. Insertion of the last element
	 * (the one at index n-1) is done by first finding the correct spot using binary search, 
	 * then inserting at that spot.
	 * 
	 * @param occs List of Occurrences
	 * @return Sequence of mid point indexes in the input list checked by the binary search process,
	 *         null if the size of the input list is 1. This returned array list is only used to test
	 *         your code - it is not used elsewhere in the program.
	 */
	public ArrayList<Integer> insertLastOccurrence(ArrayList<Occurrence> occs) {
		
		//if it's 'empty' then just return null
		if (occs.size() == 1){
			return null;
		}
		
		//making it into a binary search to re-arrange it
		int hi = 0;
		int lo = occs.size() - 2; //does not count the last element because that is the one you need to organize
		int target = occs.get(occs.size()-1).frequency;
		int mid = (hi+lo)/2;
		ArrayList <Integer> midIndex = new ArrayList<Integer>();
		midIndex.add(mid);
		
		//the binary search and adds mid into array
		while (hi < lo){
			if (occs.get(mid).frequency < target){
				lo = mid - 1;
			} else if (occs.get(mid).frequency > target){
				hi = mid + 1;
			} else{
				break;
			}
			mid = (hi + lo)/2;
			midIndex.add(mid);
		}
		
		//finds where target needs to go
		if (occs.get(mid).frequency > target){
			occs.add(mid+1, occs.get(occs.size()-1));
		} else{
			occs.add(mid, occs.get(occs.size()-1));
		}
		
		//removes last element and returns midIndex array
		occs.remove(occs.size()-1);
		return midIndex;
	}
	
	/**
	 * Search result for "kw1 or kw2". A document is in the result set if kw1 or kw2 occurs in that
	 * document. Result set is arranged in descending order of occurrence frequencies. (Note that a
	 * matching document will only appear once in the result.) Ties in frequency values are broken
	 * in favor of the first keyword. (That is, if kw1 is in doc1 with frequency f1, and kw2 is in doc2
	 * also with the same frequency f1, then doc1 will appear before doc2 in the result. 
	 * The result set is limited to 5 entries. If there are no matching documents, the result is null.
	 * 
	 * @param kw1 First keyword
	 * @param kw1 Second keyword
	 * @return List of NAMES of documents in which either kw1 or kw2 occurs, arranged in descending order of
	 *         frequencies. The result size is limited to 5 documents. If there are no matching documents,
	 *         the result is null.
	 */
	public ArrayList<String> top5search(String kw1, String kw2){
		ArrayList<String> results = new ArrayList<String>(5);
		
		//checks if they are keyWords
		if (keywordsIndex.get(kw1) == null && keywordsIndex.get(kw2) == null){ //both are not there
			return null;
		} else if (keywordsIndex.get(kw1) != null && keywordsIndex.get(kw2) == null){ //kw1 is there; but kw2 is not
			for (int i = 0; i < 5; i++){
				results.add(i, keywordsIndex.get(kw1).get(i).document);
			}
			return results;
		} else if (keywordsIndex.get(kw1) == null && keywordsIndex.get(kw2) != null){ //kw1 is not there, kw1 is
			for (int i = 0; i < 5; i++){
				results.add(i, keywordsIndex.get(kw2).get(i).document);
			}
			return results;
		}
		
		//get arrayList for words
		int i =0, j = 0; //for kw2
		
		//this keeps the array from getting larger than top 5
		//prevents from i being larger the size of kw1's array
		//prevents from j being larger the size of kw2's array
		while ((results.size() < 5) && (i < keywordsIndex.get(kw1).size()) && (j < keywordsIndex.get(kw2).size())){
			if(keywordsIndex.get(kw1).get(i).frequency >= keywordsIndex.get(kw2).get(j).frequency){
				if(isEmpty(results, keywordsIndex.get(kw1).get(i).document)){
					results.add(keywordsIndex.get(kw1).get(i).document);
				}
				i++;
			} else{
				if (isEmpty(results, keywordsIndex.get(kw2).get(j).document)){
					results.add(keywordsIndex.get(kw2).get(j).document);
				}
				j++;
			}
		}
		
		//doesn't have 5
		if(results.size() < 5){
			if (i < keywordsIndex.get(kw1).size() && j < keywordsIndex.get(kw2).size()){
				while (results.size() < 5 && i < keywordsIndex.get(kw1).size()) {
					if (isEmpty(results, keywordsIndex.get(kw1).get(i).document)){
						results.add(keywordsIndex.get(kw1).get(i).document);
					}
					i++;
				}
			} else if((i <keywordsIndex.get(kw1).size() == false) && j < keywordsIndex.get(kw2).size()){
				while (results.size() < 5 && j < keywordsIndex.get(kw2).size()){
					if (isEmpty(results, keywordsIndex.get(kw2).get(j).document)){
						results.add(keywordsIndex.get(kw2).get(j).document);
					}
					j++;
				}
			}
		}
		
		return results;
	}
	
	//input a string to see if the string is in the ar or not
	//helper method for top5
	private boolean isEmpty(ArrayList<String> ar, String docFile){
		if (ar.size() == 0){
			return true;
		}
		for (int i = 0; i < ar.size(); i++){
			if (ar.get(i).equals(docFile)){
				return false;
			}
		}
		return true;
	}
}

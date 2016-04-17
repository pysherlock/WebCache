package web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class LocalCache {

	private HashMap<String, ArrayList<byte[]>> Cache;
	private HashMap<String, Integer> TimeTable;  //Store the life count of each entry in Cache
	
	public LocalCache() {
		Cache = new HashMap<String, ArrayList<byte[]>>();
		TimeTable = new HashMap<String, Integer>();
	}
	
	public synchronized void Put(String key, ArrayList<byte[]> value) {
		if(!TimeTable.isEmpty()) {
			for(Entry<String, Integer> entry: TimeTable.entrySet()) //reduce the life count of old entries
				entry.setValue(entry.getValue() - 1);
		}
		Cache.put(key, value);
		TimeTable.put(key, 200);
	}
	
	public synchronized void Remove(String key) {
		Cache.remove(key);
		TimeTable.remove(key);
	}
	
	public synchronized ArrayList<byte[]> Get(String key) {
		return Cache.get(key);
	}
	
	public synchronized boolean containsKey(String key) {
		if(Cache.containsKey(key))
			return true;
		else
			return false;
	}
	
	public synchronized int Size(){
		return Cache.size();
	}
	
	public synchronized void ReFresh() {
		//Remove the entry whose life count is zero
		ArrayList<String> Entry_Remove = new ArrayList<String>();
		for(Entry<String, Integer> entry: TimeTable.entrySet()) {
			if(entry.getValue() <= 0)
			//	Remove(entry.getKey());
				Entry_Remove.add(entry.getKey());
		}
	
		if(Entry_Remove.size() > 0) {
			for(String url: Entry_Remove){
				System.out.println("Remove " + url);
				Remove(url);
			}
		}
		
		if(Cache.size() > 200) { //When Size of Cache is bigger the limitation, delete the entry whose life count is smallest
			int lifeCount = 200;
			String key = null;
			for(Entry<String, Integer> entry: TimeTable.entrySet()) {
				if(lifeCount < entry.getValue()) {
					key = entry.getKey();
					lifeCount = entry.getValue();
				}
			}
			Remove(key);
		}
		System.out.println("Cache Size after Refresh: " + Cache.size());
	}//Let it be the Contribution of Github
}

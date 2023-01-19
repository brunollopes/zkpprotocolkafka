package com.bole.zkpauth.cache;

import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache implementation with a periodic memory clean up process.
 */

public class ConcurrentMapCache<K, V> {

        private final Map<K, Holder<V>> mMap;
        private final long timeToLive;
        private final long cleanUpInterval;
        private final TimerTask cleanUpTask = new TimerTask() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                if(!mMap.isEmpty()){
                    Iterator<Map.Entry<K, Holder<V>>> mIterator = mMap.entrySet().iterator();
                    while(mIterator.hasNext()){
                        long expiry = timeToLive + mIterator.next().getValue().lastAccessed;
                        if(now > expiry){
                            mIterator.remove();
                        }
                    }
                }
            }
        };

        /**
         * @param elementTimeToLiveMillis The time (in milliseconds) each element stays alive after it was last accessed.
         * @param cleanUpIntervalMillis The interval (in milliseconds) between cache clean ups.
         * @param cacheSize The size of the cache.
         * */
        public ConcurrentMapCache(long elementTimeToLiveMillis, long cleanUpIntervalMillis, int cacheSize){
            mMap = new ConcurrentHashMap<>(cacheSize);
            this.timeToLive = elementTimeToLiveMillis;
            this.cleanUpInterval = cleanUpIntervalMillis;
            setupCleanUpProcess();
        }

        /**
         * Puts the specified value in the cache, overwrites any value previously mapped to the specified key.
         * @param key The key which the specified value is associated with.
         * @param value The value to be cached.
         * */
        public void put(K key, V value){
            mMap.put(key, new Holder<>(value));
        }

        /**
         * Puts the specified value in the cache, if a value is already mapped to the specified key that value is returned.
         * @param key The key which the specified value is associated with.
         * @param value The value to be cached.
         * */
        public V putIfAbsent(K key, V value){
            return mMap.putIfAbsent(key, new Holder<>(value)).getValue();
        }

        /**
         * Returns the value associated with the specified key, if no mapping is found the method returns null.
         * @param key The key associated with the value to be returned.
         * */
        public V get(K key){
            Holder<V> mHolder = mMap.get(key);
            if(mHolder != null){
                return mHolder.getValue();
            }else{
                return null;
            }
        }

        /**
         * Returns true if the Cache has a value associated with the specified key, else returns false;
         * @param key The key to be checked
         * @return true if the cache has a value mapped to the given key.
         * */
        public boolean containsKey(K key){
            return mMap.containsKey(key);
        }

        /**
         * Removes the value associated with the specified key.
         * @param key The key associated with the value to be removed.
         * @return
         * */
        public V remove(K key){
            Holder<V> mHolder = mMap.remove(key);
            if(mHolder != null){
                return mHolder.getValue();
            }else{
                return null;
            }
        }

        /**
         * Creates a daemon thread to take care of the clean up process
         * */
        private void setupCleanUpProcess(){
            new Timer(true).scheduleAtFixedRate(cleanUpTask, cleanUpInterval, cleanUpInterval);
        }

        /**
         * Holder class for cache entries to monitor access to the entry.
         * */
        private class Holder<T>{

            long lastAccessed;
            T value;

            Holder(T value){
                lastAccessed = System.currentTimeMillis();
                this.value = value;
            }

            T getValue(){
                lastAccessed = System.currentTimeMillis();
                return this.value;
            }

        }
}

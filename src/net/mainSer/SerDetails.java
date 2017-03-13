/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.mainSer;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.dataSer.IntDataSer;
import net.logSer.IntRemoteLog;

/**
 *
 * @author Neel Patel
 */
public class SerDetails {
     private SerDetails(){}
     private static IntRemoteLog log;
     private static IntDataSer mainDataSer;
     private final static Map<IntDataSer,Long> dataSer=new ConcurrentHashMap<>();
     
     public static String getDataSer(){
          for(;!(dataSer.isEmpty()&&mainDataSer==null);){
               String s=findDataSer();
               if(s!=null)
                    return s;
          }
          return null;
     }
     
     private synchronized static String findDataSer(){
          IntDataSer d=dataSer.keySet().parallelStream()
                              .reduce((x,y)->{
                                   if(dataSer.get(x)>dataSer.get(y))
                                        return y;
                                   return x;
                              }).orElse(mainDataSer);
          try {
               if(d.aya()){
                    dataSer.replace(d,System.currentTimeMillis());
                    return d.toUrl();
               }else{
                    assert true:"remote data Server say 'not alive'";
                    return null;
               }
          } catch (Exception ex) {
               dataSer.remove(d);
               return null;
          }
     }
     
     public static synchronized String getLogSer(){
          try {
               if(log.aya())
                    return log.toUrl();
               else{
                    assert true:"remote Log Server say 'not alive'";
                    return null;
               }
          } catch (Exception ex) {
               return null;
          }
     }
     
     public static boolean setLogSer(String url){
          try {
               IntRemoteLog ls=(IntRemoteLog)Naming.lookup(url);
               if(ls.aya()){
                    log=ls;
                    return true;
               }
               else{
                    assert true:"remote Log Server say 'not alive'";
                    return false;
               }
          } catch (Exception ex) {
               return false;
          } 
     }
     
     public static boolean registerDataSer(String url){
          try {
               IntDataSer ds=(IntDataSer)Naming.lookup(url);
               if(ds.aya()){
                    dataSer.put(ds,System.currentTimeMillis());
                    return true;
               }else{
                    assert true:"remote Log Server say 'not alive'";
                    return false;
               }
          } catch (Exception ex) {
               return false;
          }
     }
     
     public synchronized static boolean registerMainDataSer(String url){
          try {
               IntDataSer ds=(IntDataSer)Naming.lookup(url);
               if(ds.aya()){
                    mainDataSer=ds;
                    return true;
               }else{
                    assert true:"remote Log Server say 'not alive'";
                    return false;
               }
          } catch (Exception ex) {
               return false;
          }
     }
     
}

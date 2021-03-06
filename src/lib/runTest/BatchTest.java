/*
* Copyright 2017 Program Tester Team
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at

*     http://www.apache.org/licenses/LICENSE-2.0

* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* package lib.adminModule;
*/
package lib.runTest;
import java.util.*;
import lib.runDetails.*;
import java.util.stream.*;

/**
 * this class provide facility to run the executable command with
   specified inputs.
 * this class use multiple objects of {@code RunTest} class to perform such a task. 
 * @author Neel Patel
 */
public class BatchTest {
     private RunTest rt[];
     private IntInput[] ar; 
     private List<IntIODetail> io;
     //private Stream<IntIODetail> st;
     //private boolean f=false;
     private int index=-1; // indicate last returned result
     private Thread t; // the executer thread 
     private boolean flag; // this will usefull to terminate the executer thread
                           //manualy
     
     /**
      * creates object with executable command and multiple inputs.
      * @param cmd command to be executed.
      * @param ar Object of type IntInput to give inputs to the processes.
      */
     public BatchTest(String cmd,IntInput... ar){
          this.ar=ar.clone();
          rt=Arrays.stream(this.ar).map(s->new RunTest(s,cmd)).toArray(RunTest[]::new);
     }
     
     /**
      * execute the command for all provided inputs in sequence.
      * it creates a new thread known as {@code executer thread} which will
        execute the command for different inputs one by one.
      * it returns immediately, without waiting for tasks to done.
      * if the executer thread is already initiated it return with null immediately.
      * it returns stream of IntIODetail which gives Input and corresponding output.
      * the stream returned by this method is infinite stream, it will encounter null
        when there is no elements exist to process.
      * @return stream of IntIODetail, each element contain result of execution.
      */
     public synchronized Stream<IntIODetail> execute(){
          if(t!=null)
               return null;
          t=new Thread(this::run,"Executer Thread");
          t.start();
          return Stream.generate(this::supp);
     }
     
     /**
      * this method can be used as supplier by giving reference.
      * this method gives the next result of execution of given command.
      * this method will wait if next result is not available at the moment.
      * method will return null if there is no further results available.
      * @return next Object of IntIODetail, null if no further objects available.
      */
     private IntIODetail supp(){
          synchronized(io){
//               if(index+1>=rt.length ) // check if any further result exist.
//                    return null;
               for(;index+1<io.size();){ //wait for next result to be available.
                    if(!flag)
                         return null;
                    try {
                         io.wait(1000);
                    } catch(InterruptedException ex) {}
               }
               index++;
               return io.get(index);
          }
     }
     
     /**
      * this method can be used to create new thread which will execute
        the commands for multiple times in sequence.
      * this method call {@code getIODetail} method on all the elements of list 
        {@link rt}.
      * this method with puts current thread in waiting until the all executions
        completed or process of execution terminated by stop method.
      */
     private void run(){
          flag=true;
          for(RunTest i:rt){
               if(!flag) //check if the thread should to be terminated.
                    break;
               IntIODetail d=i.getIODetail();
               synchronized(io){
                    io.add(d);
               }
               io.notifyAll();
          }
          io.notifyAll();
          flag=false;
     }
     
     /**
      * this method change the flag to false to terminate the
        execution process.
      * although the currently running process will be executed as it is.
      * next tasks will not be initiated.
      */
     public void stop(){
          flag=false;
     }
     
     /**
      * this method returns the result as list of objects of IntIODetail.
      * this method will call execute method if it is not called before.
      * if the executer thread is not terminated yet then it will wait for it
        to join.
      * @return result as List of IntIODetail
      */
     public synchronized List<IntIODetail> getOutputs(){
          if(t==null) //check if the executer thread is not already initiated before.
               execute();
          for(;t.isAlive();) //wait for executer thread to be terminated.
               try {
                    t.join();
               } catch(InterruptedException ex) {
               }
          List<IntIODetail> lio=new ArrayList<>();
          lio.addAll(io);
          return lio;
     }
     
     /**
      * this method return array of IntInput.
      * changes made in the array returned by this method will not reflected.
        i.e. this object will not affected. 
      * @return returns the array of the IntInput
      */
     public IntInput[] getInputs(){
          return this.ar.clone();
     }
     
     @Override
     public void finalize() throws Throwable{
          try {
               stop();
          } finally {
               super.finalize();
          }
     }
}

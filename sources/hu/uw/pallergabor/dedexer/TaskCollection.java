/**
  * Task collection. It stores a list of tasks associated to the same address.
  * Its doTask and renderTask methods invoke the list members' doTask and renderTask
  * methods.
  */

package hu.uw.pallergabor.dedexer;

import java.util.ArrayList;
import java.io.IOException;

public class TaskCollection extends DedexerTask {

/**
  * Creates a TaskCollection with one initial stored task.
  */
    public TaskCollection( DexInstructionParser instrParser, 
                            DedexerTask initialTask ) {
        super( instrParser, 0L, 0L );
        taskList.add( initialTask );
    }

    public void doTask( boolean isSecondPass ) throws IOException {
        for( int i = 0 ; i < taskList.size() ; ++i )
            taskList.get( i ).doTask( isSecondPass );
    }

    public void renderTask( long position ) throws IOException {
        for( int i = 0 ; i < taskList.size() ; ++i )
            taskList.get( i ).renderTask( position );
    }

/**
  * This specialization returns true if the string value of any of the 
  * tasks in the collection matches the invocation parameter.
  * @param str String to match
  * @return true if the parameter string matches.
  */
    public boolean equals( String str ) {
        for( int i = 0 ; i < taskList.size() ; ++i )
            if( taskList.get( i ).equals( str ) )
                return true;
        return false;
    }

    public void addTask( DedexerTask task ) {
// First check if this task equals to some other task already in the list.
// We don't add if it equals
        for( int i = 0 ; i < taskList.size() ; ++i ) {
            DedexerTask existingTask = taskList.get( i );
            if( task.equals( existingTask ) )
                return;
        }
        int taskPriority = task.getPriority();
        boolean found = false;
// High-priority tasks are inserted closer to the list head
        for( int i = 0 ; i < taskList.size() ; ++i ) {
            int listTaskPriority = taskList.get( i ).getPriority();
            if( taskPriority > listTaskPriority ) {
                taskList.add( i, task );
                found = true;
                break;
            }
        }
        if( !found )
            taskList.add( task );
    }

/**
  * This implementation is not really needed, we don't add TaskCollections to
  * other TaskCollections
  */
    public int getPriority() {
        int priority = DedexerTask.MIN_PRIORITY;
        for( int i = 0 ; i < taskList.size() ; ++i ) {
            int taskPriority = taskList.get( i ).getPriority();
            if( taskPriority > priority )
                priority = taskPriority;
        }
        return priority;
    }

/**
  * If any of the task parses, we return true.
  */
    public boolean getParseFlag( long position ) {
        for( int i = 0 ; i < taskList.size() ; ++i ) {
            if( taskList.get( i ).getParseFlag( position ) )
                return true;
        }
        return false;
    }

    private ArrayList<DedexerTask> taskList = new ArrayList<DedexerTask>();
}


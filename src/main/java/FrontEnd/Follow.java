package FrontEnd;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Follow {
    /**
     * Method for adding follows
     * @param follows follows list
     * @param terminal terminal to be added to the list
     */
    private static void addFollow(List<Terminal> follows, Terminal terminal){
        if(terminal.getName().equals("Ɛ")) return;
        for(Terminal follow: follows){
            if(follow.getName().equals(terminal.getName())){
                return;
            }
        }
        follows.add(terminal);
    }
}
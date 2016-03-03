package mx.uson.cc.smed;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.util.Log;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import mx.uson.cc.smed.util.BPlusTree;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {

    BPlusTree<String, String> tree;

    public ApplicationTest() {
        super(Application.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        tree = new BPlusTree<>();
    }

    public void testPreconditions(){
        assertNotNull("null tree",tree);
    }

    public void testAdd(){
        tree.insert("holi", "crayoli");
        String actual = tree.get("holi");
        assertEquals("crayoli", actual);
        assertEquals("{n:1,root:{keys:[holi],values:[crayoli]}}", tree.toString());
        tree.empty();
    }

    public void testRemove(){
        tree.insert("holi", "crayoli");
        tree.remove("holi");
        assertEquals("{n:0,root:null}", tree.toString());
    }

    public void testSplitNSplat(){
        tree.insert("a","a");
        tree.insert("b","b");
        tree.insert("c","c");
        tree.insert("d","d");
        assertEquals("{n:4,root:{keys:[a,b,c,d],values:[a,b,c,d]}}", tree.toString());
        tree.insert("e", "e");
        assertEquals("{n:5,root:{keys:[c],children:[{keys:[a,b],values:[a,b]},{keys:[c,d,e],values:[c,d,e]}]}}", tree.toString());
        tree.remove("c");
        assertEquals("{n:4,root:{keys:[c],children:[{keys:[a,b],values:[a,b]},{keys:[d,e],values:[d,e]}]}}", tree.toString());
        tree.remove("a");
        assertEquals("{n:3,root:{keys:[b,d,e],values:[b,d,e]}}", tree.toString());
        tree.empty();
    }

    public void testQuery(){
        Comparator<String> comp = new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                if(lhs.startsWith(rhs)) return 0;
                else return lhs.compareTo(rhs);
            }
        };

        tree.insert("aa","a");
        tree.insert("ab","b");
        tree.insert("ac","c");
        tree.insert("ad","d");
        List<String> list = tree.query("a", comp);
        String s = "";
        for(int i=0; i<list.size(); ++i) s+=list.get(i)+",";
        assertEquals("a,b,c,d,",s);
        tree.insert("ae", "e");
        list = tree.query("a", comp);
        s = "";
        for(int i=0; i<list.size(); ++i) s+=list.get(i)+",";
        assertEquals("a,b,c,d,e,",s);

    }

}
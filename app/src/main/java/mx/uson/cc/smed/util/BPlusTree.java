package mx.uson.cc.smed.util;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

/**
 * Created by nancio on 29/10/15.
 */
public class BPlusTree<K extends Comparable<K>,V> {

    public static final int GRADE = 5;
    private static final int MIN = (GRADE-1)/2;

    Node root = null;
    int n = 0;

    public boolean insert(K key, V val){
        Node aux = root;
        while(aux != null){
            if(aux instanceof LeafNode){
                if(aux.insert(key, val, null)){
                    rebalance(aux);
                    ++n;
                    return true;
                }else return false;
            }else {
                aux = ((InnerNode)aux).getSon(key);
                if(aux == null) return false;
            }
        }
        root = new LeafNode<K,V>(key,val,null);
        ++n;
        return true;
    }

    public boolean remove(K key){
        Node aux = root;
        while(aux != null) {
            if (aux instanceof InnerNode) aux = ((InnerNode)aux).getSon(key);
            else if (aux.remove(key)) {
                rebalance(aux);
                --n;
                return true;
            } else return false;
        }
        return false; //TODO: throw shit
    }

    public V get(K key){
        Node actual = root;
        while(actual instanceof InnerNode)
            actual = ((InnerNode)actual).getSon(key);
        for(int i=0; i<actual.n; ++i)
            if(((K)actual.keys[i]).compareTo(key) == 0) return (V)((LeafNode)actual).vals[i];
        return null;
    }

    public List<V> query(K semikey, Comparator<K> comp){
        Node actual = root;
        ArrayList<V> arrayList = new ArrayList<>();
        while(actual instanceof InnerNode)
            actual = ((InnerNode)actual).getSon(semikey);
        int i=0;
        for(; i<actual.n; ++i)
            if(comp.compare((K) actual.keys[i], semikey) >= 0){
                //arrayList.add((V)((LeafNode)actual).vals[i++]);
                break;
            }
        if(comp.compare((K) actual.keys[i], semikey) > 0) return arrayList;
        do{
            arrayList.add((V)((LeafNode)actual).vals[i++]);
            if(i == actual.n){
                actual = ((LeafNode) actual).next;
                i=0;
            }

        }while(actual != null && comp.compare((K) actual.keys[i], semikey) == 0);

        return arrayList;
    }

    public void empty(){
        root = null;
        n = 0;
    }

    public int size(){
        return n;
    }

    private void rebalance(Node r){
        if(r.n == GRADE){
            //>=MAX
            if(r==root){
                r.dad = new InnerNode(r);
                root = r.dad;
            }
            r.split();
            if(r.dad != null) rebalance(r.dad);
        }else if (r.n < MIN){
            if(r == root){
                if(r.n == 0){
                    root = r instanceof InnerNode
                        ? (Node)((InnerNode)r).children[0]
                        : null;
                    //delete r;
                }
                return;
            }
            if(!r.pedirPrestado()){
                Node papi = r.dad;
                r.merge();
                rebalance(papi);
            }
        }
    }

    @Override
    public String toString() {
        return "{n:"+n+",root:"+(root != null ? root.toString() : "null")+"}";
    }

    private static abstract class Node<K extends Comparable<K>,V> {
        Object keys[] = new Object[GRADE];
        InnerNode dad;
        int n;

        public Node(K key, InnerNode<K,V> dad){
            this.keys[0] = key;
            this.dad = dad;
            n = 1;
        }

        public Node(){
            n = 0;
        }

        public abstract boolean insert(K key, V val, Node child);

        public abstract boolean remove(K key);

        public abstract void split();

        public abstract void merge();

        public abstract boolean pedirPrestado();

    }

    private static class InnerNode<K extends Comparable<K>, V> extends Node<K,V> {

        Object[] children = new Object[GRADE+1];

        public InnerNode(K key, InnerNode<K, V> dad) {
            super(key, dad);
        }

        public InnerNode(Node child) {
            super();
            children[0] = child;
        }

        @Override
        public boolean insert(K key, V val, Node child) {
            K kaux;
            Node naux;
            if(n == 0 || ((K)keys[n-1]).compareTo(key) <= -1){
                keys[n++] = key;
                children[n] = child;
                return true;
            }
            for(int i=0; i<n; ++i){
                if(((K)keys[i]).compareTo(key) >= 1){
                    keys[n] = keys[i];
                    keys[i] = key;
                    children[n+1] = children[i+1];
                    children[i+1] = child;
                    for(int j=n++ -1; j>i; --j){
                        kaux = (K)keys[j];
                        keys[j] = keys[j+1];
                        keys[j+1] = kaux;
                        naux = (Node)children[j+1];
                        children[j+1] = children[j+2];
                        children[j+2] = naux;
                    }
                    break;
                }else if(((K)keys[i]).compareTo(key) == 0) return false;
            }
            return true;
        }

        @Override
        public boolean remove(K key) {
            int i=0;
            for (; i < n; ++i)
                if(((K)keys[i]).compareTo(key) == 0) break;
            if(i == n) return false;

            for (int j = i; j < n-1; ++j) {
                keys[j] = keys[j + 1];
            }
            --n;
            return true;
        }

        @Override
        public void split() {
            int mitad = GRADE/2;
            InnerNode der = new InnerNode<K,V>((K)keys[mitad], dad);
            n = mitad;


            der.n--;
            for(int i=mitad+1; i<GRADE; ++i){
                der.keys[der.n] = keys[i];
                ((Node)children[i]).dad = der;
                der.children[der.n++] = children[i];
            }
            ((Node)children[GRADE]).dad = der;
            der.children[der.n] = children[GRADE];

            dad.insert((K) keys[mitad],null, der);

        }

        @Override
        public void merge() {
            //buscar indice en padre
            InnerNode hermano, aux = dad;

            int i=0;
            while(dad.children[i] != this) ++i;
            if(i==0){
                hermano = (InnerNode)dad.children[i+1];

                keys[n] = dad.keys[i];
                for(int j=0; j<hermano.n; ++j){
                    children[n] = hermano.children[j];
                    ((Node)children[n]).dad = this;
                    keys[n] = hermano.keys[j];
                }
                children[n] = hermano.children[hermano.n];
                ((Node)children[n]).dad = this;

                //delete hermano;
            }else{
                hermano = (InnerNode)dad.children[i-1];



                hermano.keys[hermano.n] = dad.keys[i-1];
                hermano.children[hermano.n] = children[0];
                ((Node)hermano.children[hermano.n]).dad = hermano;
                for(int j=0; j<n; ++j){
                    hermano.keys[hermano.n++] = keys[j];
                    hermano.children[hermano.n] = children[j+1];
                    ((Node)hermano.children[hermano.n]).dad = hermano;
                }

                //delete this;
                --i;
            }
            for(; i<aux.n-1; ++i){
                aux.keys[i] = aux.keys[i+1];
                aux.children[i+1] = aux.children[i+2];
            }
            --aux.n;
        }

        @Override
        public boolean pedirPrestado() {
            InnerNode hermano;
            int i=0;

            while(dad.children[i] != this) ++i;
            if(i==0 || (i!=dad.n && ((Node)dad.children[i+1]).n > ((Node)dad.children[i-1]).n)){
                hermano = (InnerNode)dad.children[i+1];

                if(hermano.n == (GRADE-1)/2) return false;

                keys[n++] = dad.keys[i];
                children[n] = hermano.children[0];
                ((Node)children[n]).dad = this;
                dad.keys[i] = hermano.keys[0];
                for(int j=0; j<hermano.n -1; ++j){
                    hermano.keys[j] = hermano.keys[j+1];
                    hermano.children[j] = hermano.children[j+1];
                }
                hermano.children[hermano.n-1] = hermano.children[hermano.n];

                --hermano.n;
            }else{
                hermano = (InnerNode)dad.children[i-1];

                if(hermano.n == (GRADE-1)/2) return false;


                children[n+1] = children[n];
                for(int j=n++; j>0; --j){
                    keys[i] = keys[j-1];
                    children[j] = children[j-1];
                }
                keys[0] = dad.keys[i-1];
                children[0] = hermano.children[hermano.n];
                ((Node)children[0]).dad = this;

            }
            return true;
        }

        public Node getSon(K valor) {
            int i=0;
            for(; i<n; ++i)
                if(((K)keys[i]).compareTo(valor) >= 1) return (Node)children[i];
            return (Node) children[n];
        }

        @Override
        public String toString() {
            String s = "{keys:[";
            for(int i=0; i<n; ++i) s +=keys[i].toString()+(i<n-1?",":"]");
            s+=",children:[";
            for(int i=0; i<=n; ++i) s+=children[i].toString()+(i<n?",":"]");
            return s + "}";
        }
    }

    private static class LeafNode<K extends Comparable<K>,V> extends Node<K,V> {

        Object vals[] = new Object[GRADE];
        LeafNode<K,V> next = null;

        public LeafNode(K key, V val, InnerNode<K, V> dad) {
            super(key, dad);
            vals[0] = val;
        }

        @Override
        public boolean insert(K key, V val, Node child) {
            K kaux;
            V vaux;
            if(n == 0 || ((K)keys[n-1]).compareTo(key) <= -1){
                keys[n] = key;
                vals[n++] = val;
                return true;
            }
            for(int i=0; i<n; ++i){
                if(((K)keys[i]).compareTo(key) >= 1){
                    vals[n] = vals[i];
                    vals[i] = val;
                    keys[n++] = keys[i];
                    keys[i] = key;
                    for(int j=n++ -1; j>i; --j){
                        vaux = (V)vals[j];
                        vals[j] = vals[j+1];
                        vals[j+1] = vaux;
                        kaux = (K)keys[j];
                        keys[j] = keys[j+1];
                        keys[j+1] = kaux;
                    }
                    break;
                }else if(((K)keys[i]).compareTo(key) == 0) return false;
            }
            return true;
        }

        @Override
        public boolean remove(K key) {
            int i=0;
            for (; i < n; ++i)
                if(((K)keys[i]).compareTo(key) == 0) break;
            if(i == n) return false;

            for (int j = i; j < n-1; ++j) {
                vals[j] = vals[j + 1];
                keys[j] = keys[j + 1];
            }
            --n;
            return true;
        }

        @Override
        public void split() {
            int mitad = GRADE/2;
            LeafNode der = new LeafNode<K,V>((K)keys[mitad], (V)vals[mitad], dad);
            n = mitad;

            for(int i=mitad+1; i<GRADE; ++i) {
                der.keys[der.n] = keys[i];
                der.vals[der.n++] = vals[i];
            }
            //lista enlazada b+
            der.next = next;
            next = der;
            dad.insert((K) keys[mitad], (V) vals[mitad], der);

        }

        @Override
        public void merge() {
            //buscar indice en padre
            LeafNode hermano;
            InnerNode aux = dad;

            int i=0;
            while(dad.children[i] != this) ++i;
            if(i==0){
                hermano = (LeafNode)dad.children[i+1];

                for(int j=0; j<hermano.n; ++j) {
                    keys[n] = hermano.keys[j];
                    vals[n++] = hermano.vals[j];
                }
                next = hermano.next;
                //delete hermano;
            }else{
                hermano = (LeafNode)dad.children[i-1];

                for(int j=0; j<n; ++j) {
                    hermano.keys[hermano.n] = keys[j];
                    hermano.vals[hermano.n++] = vals[j];
                }
                hermano.next = next;
                //delete this;
                --i;
            }
            for(; i<aux.n-1; ++i){
                aux.keys[i] = aux.keys[i+1];
                aux.children[i+1] = aux.children[i+2];
            }
            --aux.n;
        }

        @Override
        public boolean pedirPrestado() {
            LeafNode hermano;
            int i=0;

            while(dad.children[i] != this) ++i;
            if(i==0 || (i!=dad.n && ((Node)dad.children[i+1]).n > ((Node)dad.children[i-1]).n)){
                hermano = (LeafNode)dad.children[i+1];

                if(hermano.n == (GRADE-1)/2) return false;
                keys[n] = hermano.keys[0];
                vals[n++] = hermano.vals[0];
                dad.keys[i] = hermano.keys[1];
                for(int j=0; j<hermano.n -1; ++j) {
                    hermano.vals[j] = hermano.vals[j + 1];
                    hermano.keys[j] = hermano.keys[j + 1];
                }
                --hermano.n;
            }else{
                hermano = (LeafNode)dad.children[i-1];

                if(hermano.n == (GRADE-1)/2) return false;
                for(int j=n++; j>0; --j) {
                    vals[j] = vals[j - 1];
                    keys[j] = keys[j - 1];
                }
                vals[0] = hermano.vals[--hermano.n];
                keys[0] = hermano.keys[hermano.n];
                dad.keys[i-1] = keys[0];
            }
            return true;
        }

        @Override
        public String toString() {
            String s = "{keys:[";
            for(int i=0; i<n; ++i) s+=keys[i].toString()+(i<n-1?",":"]");
            s+=",values:[";
            for(int i=0; i<n; ++i) s+=vals[i].toString()+(i<n-1?",":"]");
            return s+"}";
        }
    }

    public class MapList<K,V> {
        List<K> keys = new ArrayList<>();
        List<V> values = new ArrayList<>();

        public MapList(List<K> keys, List<V> values){
            this.keys = keys;
            this.values = values;
        }

        public V getAt(int i){
            return values.get(i);
        }

        public V get(K key){
            int i = keys.indexOf(key);
            return values.get(i);
        }

        public K getKeyAt(int i){
            return keys.get(i);
        }

        public boolean containsKey(K key){
            return keys.contains(key);
        }

        public void reduce(K query, Comparator<K> comp){
            List<K> newKeys = new ArrayList<>();
            List<V> newVals = new ArrayList<>();
            for(int i=0; i<keys.size(); ++i){
                if(comp.compare(keys.get(i), query) == 0){
                    newKeys.add(keys.get(i));
                    newVals.add(values.get(i));
                }
            }
        }

    }
}
